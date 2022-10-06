/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
import java.util.ArrayList;
import java.util.List;

import org.web3d.vrml.lang.FieldConstants;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.*;

// Local imports
import xj3d.filter.FieldValueHandler;

import xj3d.filter.node.X3DConstants.TYPE;

/**
 * A generic container for X3D content parsed from a file. This representation
 * is minimalistic. The nodes contain only the fields extracted from the
 * file, no 'default' values are initialized. inputOnly and outputOnly fields
 * are not supported. This container is a static representation of an X3D file
 * and is not suited for runtime purposes.
 * <p>
 * A VRMLFieldReader instance must be set to this before initializing fields.
 * String Field values will automatically be converted into their
 * respective 'primitive' forms.
 * The specific types of field data returned are:
 * <ul>
 * <li>Primitive type field values are returned in their respective
 * Java wrapper.</li>
 * <li>Array type field values are returned in an ArrayData object.</li>
 * <li>SFNode type field values are returned as CommonEncodables.</li>
 * <li>MFNode type field values are returned in an List&lt;CommonEncodable&gt;.</li>
 * </ul>
 * <p>
 * A ContentHandler instance must be set to this to support encoding.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class CommonEncodable implements Encodable {

    /** Empty array used as return value */
    private static final String[] NO_FIELDS = new String[0];

    /** Content Handler Types */
    public static final int HANDLER_BINARY = 0;
    public static final int HANDLER_STRING = 1;
    public static final int HANDLER_NULL = 2;

    /** The name of the node */
    private final String nodeName;

    /** The node's DEF name */
    public String defName;

    /** The node's USE name */
    public String useName;

    /** The field parser */
    protected VRMLFieldReader fieldReader;

    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, a StringContentHandler, or null */
    protected int handlerType;

    /** Content Handler reference */
    protected ContentHandler handler;

    /** Content Handler reference */
    protected ScriptHandler scriptHandler;

    /** Binary Content Handler reference */
    protected BinaryContentHandler bch;

    /** String Content Handler reference */
    protected StringContentHandler sch;

    /** The fields and types */
    private final FieldInfo[] fieldInfo;

    /** The number of fields */
    private int num_field;

    /** The array of all field names */
    private String[] allFieldNames;

    /** The array of node type field names */
    private String[] nodeFieldNames;

    /** The field data  */
    private Object[] fieldData;

    /** The type info  */
    private TYPE[] nodeType;

    /** The parent node to this */
    private CommonEncodable parent;

    /**
     * Constructor
     *
     * @param nodeName The node's name
     * @param fieldInfo The node's field information.
     * @param nodeType The nodes's inheritance information
     */
    public CommonEncodable(String nodeName, FieldInfo[] fieldInfo, TYPE[] nodeType) {

        this.nodeName = nodeName;
        this.fieldInfo = fieldInfo;
        this.nodeType = nodeType;

        num_field = fieldInfo.length;
        fieldData = new Object[num_field];
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Return the node name
     *
     * @return The node name
     */
    @Override
    public String getNodeName() {
        return(nodeName);
    }

    /**
     * Return the DEF name of the node. null is
     * returned if the DEF name is not set.
     *
     * @return the DEF name of the node
     */
    @Override
    public String getDefName() {
        return(defName);
    }

    /**
     * Set the DEF name of the node. If the DEF name
     * is non-null, this will clear the USE name if
     * one is set.
     *
     * @param defName the DEF name of the node
     */
    @Override
    public void setDefName(String defName) {
        this.defName = defName;
        if (defName != null) {
            useName = null;
        }
    }

    /**
     * Return the USE name of the node. null is
     * returned if the USE name is not set.
     *
     * @return the USE name of the node
     */
    @Override
    public String getUseName() {
        return(useName);
    }

    /**
     * Set the USE name of the node. If the USE name
     * is non-null, this will clear the DEF name if
     * one is set. The fieldData of this node will
     * remain unchanged by setting the USE name.
     *
     * @param useName the USE name of the node
     */
    @Override
    public void setUseName(String useName) {
        this.useName = useName;
        if (useName != null) {
            defName = null;
        }
    }

    /**
     * Clear the node fields to their initial (empty) values
     */
    @Override
    public void clear() {
        for (int i = 0; i < num_field; i++) {
            fieldData[i] = null;
        }
    }

    /**
     * Push the node contents to the ContentHandler.  Does not push
     * MFNode children along, caller is reponsible for those.
     */
    public void encodeShallow() {
        encodeNode(false);
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {
        encodeNode(true);
    }

    /**
     * Push the node contents to the ContentHandler
     * @param full
     */
    public void encodeNode(boolean full) {
        boolean inScript = false;

        if (handler != null) {
            if (useName == null) {
                if (nodeName.equals("Script")) {
                    inScript = true;
                    handler.startNode("Script", defName);
                    scriptHandler.startScriptDecl();
                } else {
                    handler.startNode(nodeName, defName);
                }

                for (int i = 0; i < num_field; i++) {
                    Object data = fieldData[i];
                    if (data != null) {
                        FieldInfo info = fieldInfo[i];

                        if (full) {
                            handler.startField(info.name);
                        } else {
                            if (!(data instanceof List)) {
                                handler.startField(info.name);
                            }
                        }

                        if (data instanceof ArrayData) {
                            // an mffield type
                            ArrayData ad = (ArrayData)data;
                            switch (ad.type) {
                            case ArrayData.FLOAT:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((float[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    sch.fieldValue(FieldValueHandler.toString(
                                        (float[])ad.data,
                                        ad.num));
                                    break;
                                }
                                break;
                            case ArrayData.INT:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((int[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    sch.fieldValue(FieldValueHandler.toString(
                                        (int[])ad.data,
                                        ad.num));
                                    break;
                                }
                                break;
                            case ArrayData.DOUBLE:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((double[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    sch.fieldValue(FieldValueHandler.toString(
                                        (double[])ad.data,
                                        ad.num));
                                    break;
                                }
                                break;
                            case ArrayData.STRING:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((String[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    // rem: potential problem? if the array contains
                                    // more values than the ad.num specifies....
                                    sch.fieldValue((String[])ad.data);
                                    break;
                                }
                                break;
                            case ArrayData.BOOLEAN:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((boolean[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    sch.fieldValue(FieldValueHandler.toString(
                                        (boolean[])ad.data,
                                        ad.num));
                                    break;
                                }
                                break;
                            case ArrayData.LONG:
                                switch (handlerType) {
                                case HANDLER_BINARY:
                                    bch.fieldValue((long[])ad.data, ad.num);
                                    break;
                                case HANDLER_STRING:
                                    sch.fieldValue(FieldValueHandler.toString(
                                        (long[])ad.data,
                                        ad.num));
                                    break;
                                }
                                break;
                            }
                        } else if (data instanceof List) {
                            // this is an mfnode type

                            @SuppressWarnings("unchecked") // cast from Object type
                            List<CommonEncodable> node_list = (List<CommonEncodable>)data;
                            int num_node = node_list.size();
                            for (int j = 0; j < num_node; j++) {
                                CommonEncodable node = node_list.get(j);
                                node.encode();
                            }
                        } else if (data instanceof CommonEncodable) {
                            // this is an sfnode type
                            CommonEncodable node = (CommonEncodable)data;
                            node.encode();
                        } else {
                            // this is an sffield
                            switch (handlerType) {
                            case HANDLER_BINARY:
                                if (data instanceof Float) {
                                    bch.fieldValue((Float)data);
                                } else if (data instanceof Integer) {
                                    bch.fieldValue((Integer)data);
                                } else if (data instanceof Double) {
                                    bch.fieldValue((Double)data);
                                } else if (data instanceof String) {
                                    bch.fieldValue((String)data);
                                } else if (data instanceof Boolean) {
                                    bch.fieldValue((Boolean)data);
                                } else if (data instanceof Long) {
                                    bch.fieldValue((Long)data);
                                }
                                break;
                            case HANDLER_STRING:
                                sch.fieldValue(data.toString());
                                break;
                            }
                        }
                    }
                }

                if (inScript) {
                    scriptHandler.endScriptDecl();
                }

                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param fieldName The field identifier
     * @param value The field value
     */
    @Override
    public void setValue(String fieldName, Object value) {
        int index = getFieldIndex(fieldName);

        // Fix VRML to X3D spec renaming of choice and level fields to children
        if (index == -1 && (fieldName.equals("choice") || fieldName.equals("level"))) {
            // convert choice to children
            fieldName = "children";
            index = getFieldIndex(fieldName);
        }

        if (index != -1) {
            int fieldType = fieldInfo[index].type;
            switch (fieldType) {
            case FieldConstants.MFNODE:
                if ((value != null) && (value instanceof CommonEncodable)) {
                    @SuppressWarnings("unchecked") // cast from Object type
                    List<CommonEncodable> nodeList =
                        (List<CommonEncodable>)fieldData[index];
                    if (nodeList == null) {
                        nodeList = new ArrayList<>();
                        fieldData[index] = nodeList;
                    }
                    CommonEncodable ce = (CommonEncodable)value;
                    if (ce.isType(TYPE.X3DChildNode)) {
                        nodeList.add(ce);
                    } else {
                        System.out.println("Attempted to add non-child node "+ ce
                            +" to "+ fieldName +" field of "+ nodeName + " node.");
                    }
                }
                break;

            case FieldConstants.SFNODE:
                if (value == null) {
                    fieldData[index] = null;
                } else if (value instanceof CommonEncodable) {
                    fieldData[index] = value;
                } else {
                    System.out.println("Attempted to add non-node value "+ value
                        +" to "+ fieldName +" field of "+ nodeName + " node.");
                }
                break;

            default:
                if (value == null) {
                    fieldData[index] = null;
                } else {
                    Object parsed_value;
                    if (value instanceof String) {
                        parsed_value = getData(fieldType, (String)value);
                    } else if (value instanceof String[]) {
                        parsed_value = getData(fieldType, (String[])value);
                    } else {
                        // TODO: this should be further checked. a user could
                        // pass in a bogus value that will probably cause bad
                        // things to happen eventually. additionally, it would
                        // be desirable to allow arrays to be passed in without
                        // first being wrapped in an ArrayData object
                        parsed_value = value;
                    }
                    fieldData[index] = parsed_value;
                }
            }
        } else {
            System.out.println("Unknown field: "+ fieldName +": for node: "+ nodeName);
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param fieldName The field identifier
     * @param value The field value. This must be a properly typed array.
     * @param len The number of valid values in the array.
     */
    @Override
    public void setValue(String fieldName, Object value, int len) {
        int index = getFieldIndex(fieldName);
        if (index != -1) {
            fieldData[index] = new ArrayData(value, len);
        } else {
            System.out.println("Unknown field: "+ fieldName +": for node: "+ nodeName);
        }
    }

    /**
     * Set the reader to use for parsing field values.
     *
     * @param fieldReader The reader
     */
    @Override
    public void setFieldReader(VRMLFieldReader fieldReader) {
        this.fieldReader = fieldReader;
    }

    /**
     * Set the content handler.
     *
     * @param handler The ContentHandler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler handler) {

        this.handler = handler;
        if (handler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)handler;
            sch = null;
            handlerType = HANDLER_BINARY;
        } else if (handler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)handler;
            handlerType = HANDLER_STRING;
        } else {
            bch = null;
            sch = null;
            handlerType = HANDLER_NULL;
        }

        for (int i = 0; i < fieldData.length; i++) {
            Object value = fieldData[i];
            if (value != null) {
                int fieldType = fieldInfo[i].type;
                switch (fieldType) {
                    case FieldConstants.MFNODE:

                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> srcList =
                            (List<CommonEncodable>)value;
                        int num_children = srcList.size();
                        if (num_children > 0) {
                            for (int j = 0; j < num_children; j++) {
                                CommonEncodable ce = srcList.get(j);
                                ce.setContentHandler(handler);
                            }
                        }
                        break;

                    case FieldConstants.SFNODE:
                        CommonEncodable ce = (CommonEncodable)value;
                        ce.setContentHandler(handler);
                        break;
                }
            }
        }
    }

    /**
     * Set the script handler.
     *
     * @param handler The ContentHandler instance to use
     */
    public void setScriptHandler(ScriptHandler handler) {

        this.scriptHandler = handler;

        for (int i = 0; i < fieldData.length; i++) {
            Object value = fieldData[i];
            if (value != null) {
                int fieldType = fieldInfo[i].type;
                switch (fieldType) {
                    case FieldConstants.MFNODE:

                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> srcList =
                            (List<CommonEncodable>)value;
                        int num_children = srcList.size();
                        if (num_children > 0) {
                            for (int j = 0; j < num_children; j++) {
                                CommonEncodable ce = srcList.get(j);
                                ce.setScriptHandler(handler);
                            }
                        }
                        break;

                    case FieldConstants.SFNODE:
                        CommonEncodable ce = (CommonEncodable)value;
                        ce.setScriptHandler(handler);
                        break;
                }
            }
        }
    }

    /**
     * Create and return a unique copy of this object. A full
     * clone will copy the node's field data. A non-full clone
     * will have it's field data uninitialized. Neither case
     * will copy the defName, useName or parent. Setting these
     * is the user's responsibility.
     *
     * @param full true to copy the node's fields.
     * false returns a new instance of this node type with it's
     * field data uninitialized.
     * @return a copy of this, initialized as specified.
     */
    @Override
    public CommonEncodable clone(boolean full) {

        CommonEncodable copy = new CommonEncodable(nodeName, fieldInfo, nodeType);
        copy.setContentHandler(handler);
        copy.setScriptHandler(scriptHandler);
        copy.setFieldReader(fieldReader);

        if (full) {
            for (int i = 0; i < fieldData.length; i++) {
                Object value = fieldData[i];
                if (value != null) {
                    int fieldType = fieldInfo[i].type;
                    switch (fieldType) {
                    case FieldConstants.MFNODE:

                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> srcList =
                            (List<CommonEncodable>)value;
                        int num_children = srcList.size();
                        if (num_children > 0) {
                            List<CommonEncodable> dstList =
                                new ArrayList<>();
                            for (int j = 0; j < num_children; j++) {
                                CommonEncodable ce = srcList.get(j);
                                dstList.add(ce.clone(full));
                            }
                            copy.fieldData[i] = dstList;
                        }
                        break;

                    case FieldConstants.SFNODE:

                        CommonEncodable ce = (CommonEncodable)value;
                        copy.fieldData[i] = ce.clone(full);
                        break;

                    default:

                        if (value instanceof ArrayData) {
                            // mffield
                            ArrayData ad = (ArrayData)value;
                            copy.fieldData[i] = ad.clone();

                        } else {
                            // sffield, this should be a primitive
                            // type wrapper, and therefore immutable
                            copy.fieldData[i] = value;
                        }
                    }
                }
            }
        }
        return(copy);
    }

    /**
     * Compares this node to another and checks if
     * all fields are the same and that all children fields
     * are the same.
     * <p>
     * DEF / USE handling
     * <ul>
     * <li> If both nodes are DEF'ed, they are not equal, regardless
     * of their field values.</li>
     * <li> If one node USE's the other, they are equal.</li>
     * <li> If both nodes USE the same DEF'ed node, they are equal.</li>
     * </ul>
     * <p>
     * The parent node is not considered in the comparison. Two nodes
     * with different parents may be equal depending on their DEF/USE
     * names and field values.
     *
     * @param enc The node to compare with
     * @return true if the nodes are determined to be equivalent,
     * false if not.
     */
    @Override
    public boolean deepEquals(Encodable enc) {
        boolean rval;
        if ((enc != null) &&
            (enc instanceof CommonEncodable) &&
            (nodeName.equals(enc.getNodeName()))) {

            CommonEncodable that = (CommonEncodable)enc;

            boolean thisDefNameIsSet = (this.defName != null);
            boolean thatDefNameIsSet = (that.defName != null);

            boolean thisUseNameIsSet = (this.useName != null);
            boolean thatUseNameIsSet = (that.useName != null);

            if (thisDefNameIsSet && thatDefNameIsSet) {
                // both nodes are uniquely def'ed.
                // declare them to be not equal regardless
                // of the remainder of their state
                return(false);

            } else if (thatDefNameIsSet) {
                // this is a bit of a hack
                // presuming that 'this' is the first node found in a file,
                // if 'that' is DEF'ed and 'this' is not, then the USERedundantNodeFilter
                // should not find them equal - otherwise a USE of the DEF'ed node may
                // get hosed later in the file
                return(false);

            } else if (thisDefNameIsSet && thatUseNameIsSet) {
                if (this.defName.equals(that.useName)) {
                    return(true);
                } else {
                    // the argument 'use's a def other than this def.
                    // therefore they are not equal
                    return(false);
                }
            } else if (thatDefNameIsSet && thisUseNameIsSet) {
                if (that.defName.equals(this.useName)) {
                    return(true);
                } else {
                    // this 'use's a def other than the argument's def.
                    // therefore they are not equal
                    return(false);
                }
            }

            if (thisUseNameIsSet && thatUseNameIsSet) {
                if (this.useName.equals(that.useName)) {
                    // both use the same def
                    return(true);
                } else {
                    // each use's a separate def
                    return(false);
                }
            } else if (thisUseNameIsSet || thatUseNameIsSet) {
                // one use's one doesn't
                return(false);
            }

            // compare the fields of the argument to this
            rval = true;
            for (int i = 0; i < num_field; i++) {

                Object d0 = this.fieldData[i];
                boolean d0isNull = (d0 == null);

                Object d1 = that.fieldData[i];
                boolean d1isNull = (d1 == null);

                if (d0isNull && d1isNull) {
                    // in this case, both being null is a positive
                    continue;

                } else if (d0isNull || d1isNull) {
                    // TODO: if this is an mfnode field and
                    // the child list exists but is empty,
                    // then this should be equivalent to null.
                    rval = false;
                    break;

                } else {
                    FieldInfo info = fieldInfo[i];

                    // both have a value, compare them
                    if (info.type == FieldConstants.SFNODE) {
                        // sfnode
                        CommonEncodable x0 = (CommonEncodable)d0;
                        CommonEncodable x1 = (CommonEncodable)d1;
                        if (!x0.deepEquals(x1)) {
                            rval = false;
                            break;
                        }
                    } else if (info.type == FieldConstants.MFNODE){
                        // mfnode
                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> list0 = (List<CommonEncodable>)d0;
                        int num_list0 = list0.size();

                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> list1 = (List<CommonEncodable>)d1;
                        int num_list1 = list1.size();

                        if (num_list0 == num_list1) {
                            //////////////////////////////////////
                            // checking the contents of two lists
                            // against each other is more complex
                            // than just a serial check. there
                            // could be equivalent nodes but in a
                            // different order - and that would be
                            // equal for our purposes...
                            // so this isn't correct, for now.
                            rval = false;
                            break;
                            //////////////////////////////////////
                        } else {
                            rval = false;
                            break;
                        }
                    } else if (d0 instanceof ArrayData) {
                        // mffield
                        ArrayData a0 = (ArrayData)d0;
                        ArrayData a1 = (ArrayData)d1;
                        if (!a0.equals(a1)) {
                            rval = false;
                            break;
                        }
                    } else {
                        // sffield
                        if (!d0.equals(d1)) {
                            rval = false;
                            break;
                        }
                    }
                }
            }
        } else {
            rval = false;
        }
        return(rval);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the parent node of this
     *
     * @param parent The parent node of this
     */
    public void setParent(CommonEncodable parent) {
        this.parent = parent;
    }

    /**
     * Return the parent node of this
     *
     * @return The parent node of this
     */
    public CommonEncodable getParent() {
        return(parent);
    }

    /**
     * Return the array of X3D types that this node inherits from.
     * If this node inherits from no known types, an empty array is
     * returned. If this node is of an unknown type, null is
     * returned.
     *
     * @return The array of X3D types that this node implements
     */
    public TYPE[] getNodeTypes() {
        TYPE[] types = null;
        if (nodeType != null) {
            int num = nodeType.length;
            types = new TYPE[num];
            System.arraycopy(nodeType, 0, types, 0, num);
        }
        return(types);
    }

    /**
     * Return whether this node inherits from the specified type
     *
     * @param type The type to check
     * @return true if this node inherits from the specified type,
     * false if not.
     */
    public boolean isType(TYPE type) {
        boolean is_type = false;
        if (nodeType != null) {
            for (TYPE nodeType1 : nodeType) {
                if (type == nodeType1) {
                    is_type = true;
                    break;
                }
            }
        }
        return(is_type);
    }

    /**
     * Return field data. If none is available, or the field is unknown,
     * return null.
     * <p>
     * The specific types of field data returned are:
     * <ul>
     * <li>Primitive type field values are returned in their respective
     * Java wrapper.</li>
     * <li>Array type field values are returned in an ArrayData object.</li>
     * <li>SFNode type field values are returned as CommonEncodables.</li>
     * <li>MFNode type field values are returned in an List&lt;CommonEncodable&gt;.</li>
     * </ul>
     *
     * @param fieldName The field identifier
     * @return The field data
     */
    public Object getValue(String fieldName) {
        Object data = null;
        if (fieldData != null) {
            int index = getFieldIndex(fieldName);
            if (index != -1) {
                data = fieldData[index];
            }
        }
        return(data);
    }

    /**
     * Return the field type. If the field does not exist
     * for this node, -1 is returned.
     *
     * @param fieldName The field identifier
     * @return The field type.
     */
    public int getFieldType(String fieldName) {
        int type = -1;
        if (fieldName != null) {
            for (FieldInfo fieldInfo1 : fieldInfo) {
                if (fieldInfo1.name.equals(fieldName)) {
                    type = fieldInfo1.type;
                    break;
                }
            }
        }
        return(type);
    }

    /**
     * Return the array of all field names.
     *
     * @return The array of all field names.
     */
    public String[] getAllFieldNames() {
        if (allFieldNames == null) {
            allFieldNames = new String[fieldInfo.length];
            for (int i = 0; i < fieldInfo.length; i++) {
                allFieldNames[i] = fieldInfo[i].name;
            }
        }
        return(allFieldNames);
    }

    /**
     * Return the array of used field names. If none are
     * used, an empty String[] is returned.
     *
     * @return The array of used field names.
     */
    public String[] getUsedFieldNames() {
        String[] rname;
        if (fieldData == null) {
            rname = NO_FIELDS;
        } else {
            String[] fname = new String[fieldInfo.length];
            int idx = 0;
            for (int i = 0; i < num_field; i++) {
                if (fieldData[i] != null) {
                    // TODO: if this is an mfnode field and
                    // the child list exists but is empty,
                    // then this shouldn't be added
                    fname[idx++] = fieldInfo[i].name;
                }
            }
            if (idx == num_field) {
                rname = fname;
            } else {
                String[] uname = new String[idx];
                System.arraycopy(fname, 0, uname, 0, idx);
                rname = uname;
            }
        }
        return(rname);
    }

    /**
     * Return the array of node type field names. If none are
     * used, an empty String[] is returned.
     *
     * @return The array of node type field names.
     */
    public String[] getNodeFieldNames() {
        if (nodeFieldNames == null) {
            String[] fname = new String[fieldInfo.length];
            int idx = 0;
            for (int i = 0; i < num_field; i++) {
                FieldInfo info = fieldInfo[i];
                if ((info.type == FieldConstants.SFNODE) ||
                    (info.type == FieldConstants.MFNODE)) {
                    fname[idx++] = fieldInfo[i].name;
                }
            }
            if (idx == 0) {
                nodeFieldNames = NO_FIELDS;
            } else if (idx == num_field) {
                nodeFieldNames = fname;
            } else {
                String[] uname = new String[idx];
                System.arraycopy(fname, 0, uname, 0, idx);
                nodeFieldNames = uname;
            }
        }
        return(nodeFieldNames);
    }

    /**
     * Return a String representation of this. The String
     * contains the node name, DEF name and USE name.
     *
     * @return A String representation of this
     */
    @Override
    public String toString() {
        return(nodeName +": def = "+ defName +": use = "+ useName);
    }

    /**
     * Return the index into the data array for the named field.
     * If the field does not exist for this node, -1 is returned.
     *
     * @param fieldName The field identifier
     * @return The field index.
     */
    private int getFieldIndex(String fieldName) {
        int index = -1;
        if (fieldName != null) {
            for (int i = 0; i < fieldInfo.length; i++) {
                if (fieldInfo[i].name.equals(fieldName)) {
                    index = i;
                    break;
                }
            }
        }
        return(index);
    }

    /**
     * Return the data values contained in the argument String for the
     * specified fieldType
     *
     * @param fieldType The field type
     * @param value The String representation of the data
     */
    private Object getData(int fieldType, String value) {

        switch(fieldType) {
        case FieldConstants.SFINT32:
            int i = fieldReader.SFInt32(value);
            return(i);

        case FieldConstants.MFINT32:
            int[] i_array = fieldReader.MFInt32(value);
            return(new ArrayData(i_array, ArrayData.INT, i_array.length));

        case FieldConstants.SFFLOAT:
            float f = fieldReader.SFFloat(value);
            return(f);

        case FieldConstants.SFTIME:
            double d = fieldReader.SFTime(value);
            return(d);

        case FieldConstants.SFDOUBLE:
            d = fieldReader.SFDouble(value);
            return(d);

        case FieldConstants.MFTIME:
            double[] d_array = fieldReader.MFTime(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFDOUBLE:
            d_array = fieldReader.MFDouble(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFLONG:
            long l = fieldReader.SFLong(value);
            return(l);

        case FieldConstants.MFLONG:
            long[] l_array = fieldReader.MFLong(value);
            return(new ArrayData(l_array, ArrayData.LONG, l_array.length));

        case FieldConstants.SFBOOL:
            boolean b = fieldReader.SFBool(value);
            return(b);

        case FieldConstants.SFROTATION:
            float[] f_array = fieldReader.SFRotation(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFROTATION:
            f_array = fieldReader.MFRotation(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFBOOL:
            boolean[] b_array = fieldReader.MFBool(value);
            return(new ArrayData(b_array, ArrayData.BOOLEAN, b_array.length));

        case FieldConstants.MFFLOAT:
            f_array = fieldReader.MFFloat(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC2F:
            f_array = fieldReader.SFVec2f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC3F:
            f_array = fieldReader.SFVec3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC4F:
            f_array = fieldReader.SFVec4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC2F:
            f_array = fieldReader.MFVec2f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC3F:
            f_array = fieldReader.MFVec3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC4F:
            f_array = fieldReader.MFVec4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC3D:
            d_array = fieldReader.SFVec3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFVEC4D:
            d_array = fieldReader.SFVec4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFVEC3D:
            d_array = fieldReader.MFVec3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFVEC4D:
            d_array = fieldReader.MFVec4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFSTRING:
            return(value);

        case FieldConstants.MFSTRING:
            String[] s_array = fieldReader.MFString(value);
            return(new ArrayData(s_array, ArrayData.STRING, s_array.length));

        case FieldConstants.SFCOLOR:
            f_array = fieldReader.SFColor(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFCOLOR:
            f_array = fieldReader.MFColor(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFCOLORRGBA:
            f_array = fieldReader.SFColorRGBA(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFCOLORRGBA:
            f_array = fieldReader.MFColorRGBA(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX3F:
            f_array = fieldReader.SFMatrix3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX4F:
            f_array = fieldReader.SFMatrix4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFMATRIX3F:
            f_array = fieldReader.MFMatrix3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFMATRIX4F:
            f_array = fieldReader.MFMatrix4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX3D:
            d_array = fieldReader.SFMatrix3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFMATRIX4D:
            d_array = fieldReader.SFMatrix4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFMATRIX3D:
            d_array = fieldReader.MFMatrix3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFMATRIX4D:
            d_array = fieldReader.MFMatrix4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFIMAGE:
            i_array = fieldReader.SFImage(value);
            return(new ArrayData(i_array, ArrayData.INT, i_array.length));

            // these cases are not primitive types
        //case FieldConstants.SFIMAGE:
        case FieldConstants.MFIMAGE:
        case FieldConstants.SFNODE:
        case FieldConstants.MFNODE:
            //throw new IllegalArgumentException(
            //  "fieldType: "+ fieldType +" cannot contain an array");
            return(null);

        default:
            //throw new IllegalArgumentException(
            //    "FieldValueHandler: Unknown fieldType: "+ fieldType);
            return(null);
        }
    }

    /**
     * Return the data values contained in the argument String[] for the
     * specified fieldType
     *
     * @param The field type
     * @param The String[] representation of the data
     */
    private Object getData(int fieldType, String[] value) {

        switch(fieldType) {

        case FieldConstants.MFINT32:
            int[] i_array = fieldReader.MFInt32(value);
            return(new ArrayData(i_array, ArrayData.INT, i_array.length));

        case FieldConstants.MFTIME:
            double[] d_array = fieldReader.MFTime(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFDOUBLE:
            d_array = fieldReader.MFDouble(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFLONG:
            long[] l_array = fieldReader.MFLong(value);
            return(new ArrayData(l_array, ArrayData.LONG, l_array.length));

        case FieldConstants.SFROTATION:
            float[] f_array = fieldReader.SFRotation(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFROTATION:
            f_array = fieldReader.MFRotation(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFBOOL:
            boolean[] b_array = fieldReader.MFBool(value);
            return(new ArrayData(b_array, ArrayData.BOOLEAN, b_array.length));

        case FieldConstants.MFFLOAT:
            f_array = fieldReader.MFFloat(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC2F:
            f_array = fieldReader.SFVec2f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC3F:
            f_array = fieldReader.SFVec3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC4F:
            f_array = fieldReader.SFVec4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC2F:
            f_array = fieldReader.MFVec2f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC3F:
            f_array = fieldReader.MFVec3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFVEC4F:
            f_array = fieldReader.MFVec4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFVEC3D:
            d_array = fieldReader.SFVec3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFVEC4D:
            d_array = fieldReader.SFVec4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFVEC3D:
            d_array = fieldReader.MFVec3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFVEC4D:
            d_array = fieldReader.MFVec4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFSTRING:
            String[] s_array = fieldReader.MFString(value);
            return(new ArrayData(s_array, ArrayData.STRING, s_array.length));

        case FieldConstants.SFCOLOR:
            f_array = fieldReader.SFColor(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFCOLOR:
            f_array = fieldReader.MFColor(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFCOLORRGBA:
            f_array = fieldReader.SFColorRGBA(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFCOLORRGBA:
            f_array = fieldReader.MFColorRGBA(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX3F:
            f_array = fieldReader.SFMatrix3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX4F:
            f_array = fieldReader.SFMatrix4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFMATRIX3F:
            f_array = fieldReader.MFMatrix3f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.MFMATRIX4F:
            f_array = fieldReader.MFMatrix4f(value);
            return(new ArrayData(f_array, ArrayData.FLOAT, f_array.length));

        case FieldConstants.SFMATRIX3D:
            d_array = fieldReader.SFMatrix3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFMATRIX4D:
            d_array = fieldReader.SFMatrix4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFMATRIX3D:
            d_array = fieldReader.MFMatrix3d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.MFMATRIX4D:
            d_array = fieldReader.MFMatrix4d(value);
            return(new ArrayData(d_array, ArrayData.DOUBLE, d_array.length));

        case FieldConstants.SFIMAGE:
            i_array = fieldReader.SFImage(value);
            return(new ArrayData(i_array, ArrayData.INT, i_array.length));

            // these cases are not primitive array types
        //case FieldConstants.SFIMAGE:
        case FieldConstants.MFIMAGE:
        case FieldConstants.SFNODE:
        case FieldConstants.MFNODE:
        case FieldConstants.SFINT32:
        case FieldConstants.SFFLOAT:
        case FieldConstants.SFTIME:
        case FieldConstants.SFDOUBLE:
        case FieldConstants.SFLONG:
        case FieldConstants.SFBOOL:
        case FieldConstants.SFSTRING:
            //throw new IllegalArgumentException(
            //    "fieldType: "+ fieldType +" cannot contain an array");
            return(null);

        default:
            //throw new IllegalArgumentException(
            //    "Unknown fieldType: "+ fieldType );
            return(null);
        }
    }
}
