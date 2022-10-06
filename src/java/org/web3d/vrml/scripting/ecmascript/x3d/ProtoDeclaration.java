/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.x3d;

// External imports
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.j3d.util.DefaultErrorReporter;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.j3d.util.HashSet;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoDeclare;
import org.web3d.vrml.scripting.ecmascript.builtin.*;

/**
 * ProtoDeclaration prototype object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class ProtoDeclaration extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    /** The function objects to maintain */
    private Map<String, Object> functionObjects;

    /** The name of the proto */
    private final String name;

    /** The collection of fields for the proto */
    private final FieldDefinitionArray fields;

    /** The template of the node itself */
    private VRMLProtoDeclare protoDecl;

    /** The scene that contains this root node */
    private BasicScene realScene;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("name");
        propertyNames.add("fields");
        propertyNames.add("isExternProto");

        functionNames = new HashSet<>();
        functionNames.add("newInstance");
    }

    /**
     * Construct an ExternProto representation from the given internal
     * representation.
     *
     * @param decl The declaration to build the Rhino representative from
     * @param scene The scene that owns this decl
     */
    public ProtoDeclaration(VRMLProtoDeclare decl, BasicScene scene) {
        super("ProtoDeclaration");

        protoDecl = decl;
        realScene = scene;

        functionObjects = new HashMap<>();
        name = decl.getVRMLNodeName();

        List<VRMLFieldDeclaration> field_list = decl.getAllFields();
        Iterator<VRMLFieldDeclaration> itr = field_list.iterator();

        int idx = 0;
        X3DFieldDefinition[] fa = new X3DFieldDefinition[field_list.size()];

        while(itr.hasNext()) {
            VRMLFieldDeclaration f_decl = itr.next();

            // Need to validate that the constants are exactly the same for the
            // internal representation as the SAI-defined type.
            fa[idx] = new X3DFieldDefinition(f_decl.getName(),
                                             f_decl.getAccessType(),
                                             f_decl.getFieldType());
            idx++;
        }

        fields = new FieldDefinitionArray(fa);
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        return (propertyNames.contains(name) || functionNames.contains(name));
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);

            switch(prop) {
                case 'n':
                    ret_val = this.name;
                    break;

                case 'f':
                    ret_val = fields;
                    break;

                case 'i':   // isExternProto - always false
                    ret_val = Boolean.FALSE;
                    break;
            }
        } else if(functionNames.contains(name)) {
            ret_val = locateFunction(name);
        }

        return ret_val;
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            functionObjects.put(name, value);
        }
    }

    /**
     * newInstance() function defined by From table 6.71.
     * @return an SFNode created from this new instance
     */
    public SFNode jsFunction_newInstance() {

        VRMLNodeType node =
            (VRMLNodeType)protoDecl.createNewInstance(realScene.getRootNode(),
                                                      false);

        SFNode ret_val = new SFNode(node);
        ret_val.setParentScope(this);

        return ret_val;
    }

    /**
     * Convenience method to locate a function name for this object and
     * create an appropriate Function instance to represent it. It assumes that
     * the name you give it is the normal name and will add a "jsFunction_"
     * prefix to locate that from the method details. There is also the
     * implicit assumption that you have made a check for this name being a
     * valid function for this object before you call this method. If a
     * function object is found for this method, it will automatically be
     * registered and you can also have a copy of it returned to use.
     *
     * @param methodName The real method name to look for
     * @return The function object corresponding to the munged method name
     */
    private FunctionObject locateFunction(String methodName) {
        String real_name = JS_FUNCTION_PREFIX + methodName;

        Method[] methods = getClass().getMethods();

        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(real_name)) {
                method = m;
                break;
            }
        }

        FunctionObject function;
        try {
          function = new FunctionObject(methodName, method, this);
        } catch (Exception ex) {
            DefaultErrorReporter.getDefaultReporter().errorReport("Unknown function: " + real_name +
                                        " on: " + getClass(), (Throwable)ex);
            return null;
        }

        functionObjects.put(methodName, function);

        return function;
    }
}
