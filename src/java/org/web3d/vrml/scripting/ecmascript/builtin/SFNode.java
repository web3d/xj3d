/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
import java.util.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.j3d.util.HashSet;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.ecmascript.x3d.Browser;
import org.web3d.vrml.scripting.ecmascript.x3d.X3DExecutionContext;

/**
 * SFNode field object wrapper for ECMAScript.
 *  <p>
 *
 * The node implementation works by dynamically querying the underlying node
 * for fields. Once it finds a field, it will cache the details internally.
 * We do this because scripts generally tend to operate on only a few fields
 * of a given node, but when they do, they access that field very regularly.
 *
 * @author Justin Couch
 * @version $Revision: 1.26 $
 */
public class SFNode extends NodeFieldObject implements NodeImplSource {

    /** Error message string for when no content was supplied to the node */
    private static final String NO_CONTENT_MSG =
        "The class was constructed with a null or empty string, which is not " +
        "valid for VRML";

    private static final String FIELD_ACCESS_MSG =
        "You have attempted to access a value that has field access: ";

    private static final String EVENTIN_ACCESS_MSG =
        "You have attempted to access a value that has eventIn access: ";

    /** The real node instance that we're wrapping */
    private VRMLNodeType node;

    /** Handler for getting extra scripting support */
    private FieldExtras extras;

    /** Factory used to generate field wrapper objects for our fields */
    private FieldFactory fieldFactory;

    /** Map of the event out field names to wrapper objects */
    private HashSet<String> eventOuts;

    /** Map of the exposed field names to wrapper objects */
    private HashSet<String> exposedFields;

    /** Map of the event in field names to wrapper objects */
    private HashSet<String> eventIns;

    /**
     * Fields that we have looked up and determined that they are not
     * accessible to the runtime. Usually these are described as field
     * access type.
     */
    private HashSet<String> fields;

    /** The values of fields that have changed since the last check */
    private Map<String, Object> changedValues;

    /**
     * The set of names that represent SF/MFnode fields. These are kept so
     * that we can check to see if any recursively fetch node fields can
     * be checked for changes. For example, the user does
     * thisNode.somefield.x = 1.0 allows us to track the nested change. Will
     * contain exposedFields only. No eventIn/eventOut/field.
     *
     * If a field has been changed by direct assignment
     * (e.g. myfield = new SFNode()); then this list and changedValues will
     * contain the same value. No need to double count, so when checking this
     * list, make sure the name is not a key in the that map.
     */
    private List<String> nodeFieldNames;

    /** Mapping of all field names to their objects */
    private Map<String, Object> fieldObjects;

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    static {
        functionNames = new HashSet<>();
        functionNames.add("toString");
        functionNames.add("equals");
        functionNames.add("getNodeName");
        functionNames.add("getFieldDefinitions");
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public SFNode() {
        super("SFNode");

        exposedFields = new HashSet<>();
        eventOuts = new HashSet<>();
        fields = new HashSet<>();
        eventIns = new HashSet<>();
        changedValues = new HashMap<>();
        nodeFieldNames = new ArrayList<>();
        fieldObjects = new HashMap<>();
    }

    /**
     * Construct the class based on a real node.
     *
     * @param n The node that this wraps
     */
    public SFNode(VRMLNodeType n) {
        this(null, 0, n);
    }

    /**
     * Construct the class based on a real node.
     *
     * @param n The node that this wraps
     * @param parent The parent node of this field
     * @param fieldIndex The index of the field that this field wraps
     */
    public SFNode(VRMLNodeType parent, int fieldIndex, VRMLNodeType n) {
        this(); // invoke default constructor
        node = n;
        parentNode = parent;
        parentFieldIndex = fieldIndex;

        // Don't populate the properties map right now. Wait for on-demand
        // queries.
    }

    //----------------------------------------------------------
    // Methods used by ScriptableObject reflection
    //----------------------------------------------------------

    public void jsConstructor(String vrmlSyntax) {
        if((vrmlSyntax == null) || (vrmlSyntax.trim().length() == 0))
            throw new IllegalArgumentException(NO_CONTENT_MSG);

        // need to do some parsing here.
    }

    //----------------------------------------------------------
    // Methods defined by Scriptable
    //----------------------------------------------------------

    @Override
    public boolean has(String name, Scriptable start) {
        boolean ret_val;

        if(fields.contains(name)) {
            Context.reportRuntimeError(FIELD_ACCESS_MSG + name);
            ret_val = false;
        } else if(eventOuts.contains(name) ||
                  exposedFields.contains(name) ||
                  eventIns.contains(name)) {
            ret_val = true;
        } else {
            // Maybe we should also keep a collection of fields that the
            // user has asked for but found that they are not valid. I suspect
            // that it will be a common error - some wrong assumption in
            // the user code and they'll keep asking for the same field name.
            ret_val = loadField(name);
        }

        return ret_val;
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = null;

        if(fields.contains(name)) {
            Context.reportRuntimeError(FIELD_ACCESS_MSG + name);
            ret_val = Context.getUndefinedValue();
        } else if(eventIns.contains(name)) {
            Context.reportRuntimeError(EVENTIN_ACCESS_MSG + name);
            ret_val = Context.getUndefinedValue();
        } else if(eventOuts.contains(name) || exposedFields.contains(name)) {
            ret_val = getFieldValue(name);
        } else if(loadField(name)) {
            // go look up the node to see if it exists
            if(fields.contains(name)) {
                Context.reportRuntimeError(FIELD_ACCESS_MSG + name);
                ret_val = Context.getUndefinedValue();
            } else if(eventIns.contains(name)) {
                Context.reportRuntimeError(EVENTIN_ACCESS_MSG + name);
                ret_val = Context.getUndefinedValue();
            } else if(eventOuts.contains(name) ||
                      exposedFields.contains(name)) {
                ret_val = getFieldValue(name);
            }
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registered by the
            // runtime. Let's check to see if it is a standard method for
            // this object and then create and return a corresponding Function
            // instance.
            if((ret_val == null) && functionNames.contains(name))
                ret_val = locateFunction(name);
        }

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
//System.out.println("putting " + node.getVRMLNodeName() + " " +name + "  0x" +hashCode() + " object " + value.hashCode());
        if(fields.contains(name)) {
            if(node.isSetupFinished() || scriptField) {
                Context.reportRuntimeError("Not a writable field " + name);
            } else {
                if(!loadField(name)) {
                    Context.reportRuntimeError("Not a field of this node " + name);
                    return;
                }

                // Don't put in local version, only keep in changed set as
                // later on this could be overwritten in the local store.
                // due to other script reading actions (e.g. to print out
                // field value).
                //fieldObjects.put(name, value);
                changedValues.put(name, value);

                if(value instanceof NodeFieldObject)
                    ((NodeFieldObject)value).realize();

                dataChanged = true;
            }

        } if(eventOuts.contains(name)) {
            Context.reportRuntimeError("Not a writable field " + name);
        } else if(eventIns.contains(name) || exposedFields.contains(name)) {
            if(readOnly && !scriptField) {
                Context.reportError(READONLY_MSG);
                return;
            }

            if(!loadField(name)) {
                Context.reportRuntimeError("Not a field of this node " + name);
                return;
            }

            // Don't put in local version, only keep in changed set as
            // later on this could be overwritten in the local store.
            // due to other script reading actions (e.g. to print out
            // field value).
            //fieldObjects.put(name, value);
            changedValues.put(name, value);

            if(value instanceof NodeFieldObject)
                ((NodeFieldObject)value).realize();

            dataChanged = true;
        } else if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    /**
     * Comparison of this object to another of the same type. Just calls
     * the standard equals() method of the object.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean jsFunction_equals(Object val) {
        return equals(val);
    }

    /**
     * Get the objects node name
     *
     * @return The node name
     */
    public String jsFunction_getNodeName() {
        return node.getVRMLNodeName();
    }

    /**
     * Get the objects field declarations.
     *
     * @return The field declarations
     */
    public FieldDefinitionArray jsFunction_getFieldDefinitions() {
        List<X3DFieldDefinition> fields = new ArrayList<>();

        // TODO:
        // this is a real dodgy way of doing things for now. It assumes
        // that field indexes are sequential, with no holes. This won't
        // really work correctly for the general case - particularly
        // dynamic nodes like Protos and Scripts. Also, in nodes that
        // have two different indexes for the same field but different
        // names because of VRML97 v X3D, then it will double up those and
        // present the whole lot, which it shouldn't. The main issue is
        // that there should be a getAllFields() method on VRMLNode that
        // allows a caller to introspect all fields on a given node. We
        // don't have one right now. Should add.
        for(int i = 0; ; i++) {
            VRMLFieldDeclaration f_decl = node.getFieldDeclaration(i);
            if(f_decl == null)
                break;

            fields.add(new X3DFieldDefinition(f_decl.getName(),
                                             f_decl.getAccessType(),
                                             f_decl.getFieldType()));
        }

        X3DFieldDefinition[] fa = new X3DFieldDefinition[fields.size()];
        fields.toArray(fa);

        FieldDefinitionArray ret_val = new FieldDefinitionArray(fa);

        return ret_val;
    }


    //----------------------------------------------------------
    // Methods defined by NodeFieldObject.
    //----------------------------------------------------------

    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    @Override
    @SuppressWarnings("unchecked") // cast from a Object type
    public Object getChangedFields() {
        int size = changedValues.size();

        Object ret_val = null;

        Set<Map.Entry<String, Object>> entries = changedValues.entrySet();
        Iterator<Map.Entry<String, Object>> itr = entries.iterator();
        NodeFieldData data;
        String name;
        Map.Entry<String, Object> entry;

        // First look for objects that have been directly changed through
        // assignment of new values - e.g. thisNode.field = new SFVec3f();
        // or indirect assignment e.g. thisNode.field.x = 1.0;

        if(size == 1) {
            entry = itr.next();
            name = entry.getKey();
            Object value = entry.getValue();

//System.out.println("found local change " + name + " " + node.getVRMLNodeName() + " 0x" + hashCode() + " object " + value.hashCode());
//if(!(value instanceof NodeFieldData))
//System.out.println("value " + value);

            data = new NodeFieldData();
            data.node = node;
            data.fieldIndex = node.getFieldIndex(name);
            data.value = value;
            ret_val = data;

        } else if(size > 1) {
            List<NodeFieldData> list =
                new ArrayList<>(size);

            while(itr.hasNext()) {
                entry = itr.next();
                name = entry.getKey();
                Object value = entry.getValue();

                data = new NodeFieldData();
                data.node = node;
                data.fieldIndex = node.getFieldIndex(name);
                data.value = value;

                list.add(data);
            }

            ret_val = list;
        }

        // Clear references just to avoid concurrent mod exceptions
        entries = null;
        itr = null;
        entry = null;

        // Check the fields referenced by this node for indirect assignment
        // eg

        // Now check the node fields for compatibility
        int i;
        int n_size = nodeFieldNames.size();

        for(i = 0; i < n_size; i++) {
            name = nodeFieldNames.get(i);

            // Check to avoid duplication. If we have found something that
            // duplicates, then replace the current field object instance with
            // the new one. This is so that an assignment to change the
            // instance object is caught and refected next time.
            if(changedValues.containsKey(name)) {
//System.out.println("update local node change value " + name + " " + node.getVRMLNodeName() + " 0x" + hashCode() + " object " + changedValues.get(name).hashCode());
                fieldObjects.put(name, changedValues.get(name));
            }

            Object nfo = fieldObjects.get(name);

            // These can be null, ex. ball_bounce.x3dv, so, keep going
            if (nfo == null)
                continue;

            Object values = null;

            if (nfo instanceof NodeFieldObject) {
                if (!((FieldScriptableObject) nfo).hasChanged()) {continue;}
                values = ((NodeFieldObject) nfo).getChangedFields();
            }
            else if (nfo instanceof X3DExecutionContext) {
                Browser b = (Browser) ((Scriptable) nfo).getParentScope();
                if (!b.hasSceneChanged()) {continue;}
                values = ((X3DExecutionContext) nfo).getChangedData();
            }

            // Just keep going if nothing has changed.
            if(values == null)
                continue;

            if(ret_val != null) {
                List<Object> l;

                if(ret_val instanceof List) {
                    l = (List<Object>)ret_val;
                } else {
                    l = new ArrayList<>();
                    l.add(ret_val);
                }

                if(values instanceof List)
                    l.addAll((Collection<? extends Object>)values);
                else
                    l.add(values);

                ret_val = l;
            } else {
                ret_val = values;
            }
        }

        // Finally, clear the map of changed values
        changedValues.clear();

        return ret_val;
    }

    /**
     * If the node contains a node instance, check and call its setupFinished
     * if needed.
     */
    @Override
    public void realize() {
        if(node != null && !node.isSetupFinished()) {
            node.setupFinished();
        }
    }

    //----------------------------------------------------------
    // Methods defined by FieldScriptablObject.
    //----------------------------------------------------------

    /**
     * Query this field object to see if it has changed since the last time
     * this method was called. In a single-threaded environment, calling this
     * method twice should return true and then false (assuming that data had
     * changed since the previous calls).
     *
     * @return true if the data has changed.
     */
    @Override
    public boolean hasChanged() {
        boolean ret_val = super.hasChanged();

        // do an explicit check of the referenced fields to see if
        // something nested has changed. The super class will only
        // catch direct assignment changes of local variable values but
        // won't catch something like thisNode.color.r = 0.7;
        //
        // Note that we always check this regardless of whether the line
        // above has returned true. This is used to clear out changes
        // in nodes down the scene graph from this location. If we don't
        // then this leaves it open to signal a change the next time it
        // is queried, forcing extra routing to happen that should not.
        Set<Map.Entry<String, Object>> s = fieldObjects.entrySet();
        Iterator<Map.Entry<String, Object>> itr = s.iterator();
        while(itr.hasNext()) {
            Map.Entry<String, Object> entry = itr.next();
            Object value = entry.getValue();

            if(value instanceof FieldScriptableObject) {
                FieldScriptableObject fso = (FieldScriptableObject)value;

                // treat nodes separately as we have to do some extra stuff
                // for them.
                if(fso.hasChanged()) {
                    ret_val = true;
                    if(!(value instanceof NodeFieldObject))
                        changedValues.put(entry.getKey(), fso);
                }
            }
        }

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by NodeImplSource
    //----------------------------------------------------------

    /**
     * Get the underlying node that this object represents.
     *
     * @return The node reference
     */
    @Override
    public VRMLNodeType getImplNode() {
        return node;
    }

    //----------------------------------------------------------
    // Methods defined by Object.
    //----------------------------------------------------------

    /**
     * Format the internal values of this field as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this field
     */
    @Override
    public String toString() {
        if(node == null)
            return "NULL";
        else {
            StringBuilder buf = new StringBuilder(node.getVRMLNodeName());
            buf.append(" {\n");
            int field_idx = 0;
            VRMLFieldDeclaration decl;
            int numFields = node.getNumFields();
            while(field_idx < numFields &&
                  (decl = node.getFieldDeclaration(field_idx)) != null) {
                int access = decl.getAccessType();
                if (access == FieldConstants.EXPOSEDFIELD) {
                    String field_name = decl.getName();
                    Object obj = get(field_name, this);
                    buf.append("  ");
                    buf.append(field_name);
                    buf.append(' ');
                    buf.append(obj.toString());
                    buf.append('\n');
                }
                field_idx++;
            }

            buf.append("}\n");

            return buf.toString();
        }
    }

    /**
     * Compares two objects for equality base on the components being
     * the same.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    @Override
    public boolean equals(Object val) {
        if(!(val instanceof SFNode))
            return false;

        return ((SFNode)val).node == node;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Load the named field from this node. Cache the ecmascript representation
     * so that other lookups will have the value too.
     */
    private boolean loadField(String name) {
        if(node == null)
            return false;

        // Could be quite expensive to do this all the time. May be better off
        // using a hashset
        if(nodeFieldNames.contains(name))
            return true;

        int index = node.getFieldIndex(name);

        if(index == -1)
            return false;

        VRMLFieldDeclaration decl = node.getFieldDeclaration(index);

        if(decl == null)
            return false;

        switch(decl.getAccessType()) {
            case FieldConstants.EVENTIN:
                eventIns.add(name);
                break;

            case FieldConstants.EVENTOUT:
                eventOuts.add(name);
                break;

            case FieldConstants.EXPOSEDFIELD:
                exposedFields.add(name);
                int type = decl.getFieldType();
                if(type == FieldConstants.SFNODE ||
                   type == FieldConstants.MFNODE) {

                    nodeFieldNames.add(name);
                }
                break;

            case FieldConstants.FIELD:
                fields.add(name);

                type = decl.getFieldType();
                if(type == FieldConstants.SFNODE ||
                   type == FieldConstants.MFNODE) {

                    nodeFieldNames.add(name);
                }
                break;
        }

        return true;
    }

    /**
     * Internal convenience method that fetches a field value
     * and converts it into a Javascript type value.
     */
    private Object getFieldValue(String name) {
        Object ret_val;

        ret_val = fieldObjects.get(name);

        if(fieldFactory == null)
            locateFieldFactory();

        if(ret_val != null) {
            ret_val = fieldFactory.updateField(ret_val, node, name, false);
            fieldObjects.put(name, ret_val);
            return ret_val;
        }

        ret_val = fieldFactory.createField(node, name, false);

        // Set the parent scope so that if someone is doing something
        // nasty like traversing a long chain of fields you will still be
        // able to locate the parent FieldExtra instance.
        if(ret_val instanceof FieldScriptableObject) {
            FieldScriptableObject fso = (FieldScriptableObject)ret_val;
            fso.setParentScope(this);
            if(readOnly)
                fso.setReadOnly();
        }

        fieldObjects.put(name, ret_val);

        return ret_val;
    }

    /**
     * Look through the parent scopes looking for the the field factory.
     */
    private void locateFieldFactory() {
        Scriptable parent = getParentScope();
        Scriptable child = this;

        while(!(parent instanceof FieldExtras) && (parent != null)) {
            child = parent;
            parent = parent.getParentScope();
        }

        if(parent instanceof FieldExtras) {
            extras = (FieldExtras)parent;
            fieldFactory = extras.getFieldFactory();
        } else if(child instanceof FieldExtras) {
            extras = (FieldExtras)child;
            fieldFactory = extras.getFieldFactory();
        }
    }
}
