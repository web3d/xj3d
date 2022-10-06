/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

/**
 * Common implementation of an GeoSRFSet node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BaseGeoSRFSet extends BaseGeoSRFParametersNode {

    /** Index of the ormCode field */
    protected static final int FIELD_ORM_CODE = LAST_SRF_PARAM_INDEX + 1;

    /** Index of the srfsCode field */
    protected static final int FIELD_SRFS_CODE = LAST_SRF_PARAM_INDEX + 2;

    /** Index of the srfsMember field */
    protected static final int FIELD_SRFS_MEMBER = LAST_SRF_PARAM_INDEX + 3;

    /** The last index of the nodes used by the GeoSRFSet */
    protected static final int LAST_SRF_SET_INDEX = FIELD_SRFS_MEMBER;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_SRF_SET_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** initializeOnly SFInt32 ormCode 250 */
    protected int vfOrmCode;

    /** initializeOnly MFInt32 srfsCode 0 */
    protected int[] vfSrfsCode;

    /** initializeOnly SFInt32 srfsMember 0 */
    protected int vfSrfsMember;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_ORM_CODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "ormMember");

        fieldDecl[FIELD_SRFS_CODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "srfsCode");

        fieldDecl[FIELD_SRFS_MEMBER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "srfsMember");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("ormCode", FIELD_ORM_CODE);
        fieldMap.put("srfsCode", FIELD_SRFS_CODE);
        fieldMap.put("srfsMember", FIELD_SRFS_MEMBER);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoSRFSet() {
        super("GeoSRFSet");

        hasChanged = new boolean[NUM_FIELDS];

        vfOrmCode = 250;
        vfSrfsCode = new int[1];
        vfSrfsMember = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoSRFSet(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("ormCode");
            VRMLFieldData field = node.getFieldValue(index);
            vfOrmCode = field.intValue;

            index = node.getFieldIndex("srfsCode");
            field = node.getFieldValue(index);
            vfSrfsCode = new int[field.numElements];
            System.arraycopy(field.intArrayValues,
                             0,
                             vfSrfsCode,
                             0,
                             field.numElements);

            index = node.getFieldIndex("srfsMember");
            field = node.getFieldValue(index);
            vfSrfsMember = field.intValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        return (index < 0 || index > LAST_SRF_SET_INDEX) ?
            null : fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_ORM_CODE:
                fieldData.clear();
                fieldData.intValue = vfOrmCode;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_SRFS_CODE:
                fieldData.clear();
                fieldData.intArrayValues = vfSrfsCode;
                fieldData.numElements = vfSrfsCode.length;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                break;

            case FIELD_SRFS_MEMBER:
                fieldData.clear();
                fieldData.intValue = vfSrfsMember;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an integer.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ORM_CODE:
                if(!inSetup)
                    throwInitOnlyWriteException("ormCode");

                vfOrmCode = value;
                break;

            case FIELD_SRFS_MEMBER:
                if(!inSetup)
                    throwInitOnlyWriteException("srfsMember");

                vfSrfsMember = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_SRFS_CODE:
                if(!inSetup)
                    throwInitOnlyWriteException("srfsCode");

                if(vfSrfsCode.length != numValid)
                    vfSrfsCode = new int[numValid];

                System.arraycopy(value, 0, vfSrfsCode, 0, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}

