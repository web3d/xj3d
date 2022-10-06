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
 * Common implementation of an GeoLCE3DParameters node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BaseGeoLCE3DParameters extends BaseGeoSRFTParametersNode {

    /** Index of the lococenter field */
    protected static final int FIELD_LOCOCENTER =
        LAST_SRFT_PARAM_INDEX + 1;

    /** Index of the primaryAxis field */
    protected static final int FIELD_PRIMARY_AXIS =
        LAST_SRFT_PARAM_INDEX + 2;

    /** Index of the secondaryAxis field */
    protected static final int FIELD_SECONDARY_AXIS =
        LAST_SRFT_PARAM_INDEX + 3;

    /** The last index of the nodes used by the GeoLCE3DParameters */
    protected static final int LAST_TMPARAMS_INDEX = FIELD_SECONDARY_AXIS;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TMPARAMS_INDEX + 1;

    /** Error message generated when an SFVec3f is out of valid range */
    private static final String OUT_OF_RANGE_MSG =
        "Found an out of range value for a SFVec3f field. Valid values lie " +
        "in the range [-1,1]";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** initializeOnly SFVec3f lococenter 0, 0, 0 */
    protected float[] vfLococenter;

    /** initializeOnly SFVec3f primaryAxis 0, 1, 0*/
    protected float[] vfPrimaryAxis;

    /** initializeOnly SFVec3f secondaryAxis 0, 1, 0*/
    protected float[] vfSecondaryAxis;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_LOCOCENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "lococenter");

        fieldDecl[FIELD_PRIMARY_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "primaryAxis");

        fieldDecl[FIELD_SECONDARY_AXIS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "secondaryAxis");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("lococenter", FIELD_LOCOCENTER);
        fieldMap.put("primaryAxis", FIELD_PRIMARY_AXIS);
        fieldMap.put("secondaryAxis", FIELD_SECONDARY_AXIS);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoLCE3DParameters() {
        super("GeoLCE3DParameters");

        hasChanged = new boolean[NUM_FIELDS];

        vfLococenter = new float[3];
        vfPrimaryAxis = new float[] { 0, 1, 0 };
        vfSecondaryAxis = new float[] { 0, 0, 1 };
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoLCE3DParameters(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("lococenter");
            VRMLFieldData field = node.getFieldValue(index);
            vfLococenter[0] = field.floatArrayValues[0];
            vfLococenter[1] = field.floatArrayValues[1];
            vfLococenter[2] = field.floatArrayValues[2];

            index = node.getFieldIndex("primaryAxis");
            field = node.getFieldValue(index);
            vfPrimaryAxis[0] = field.floatArrayValues[0];
            vfPrimaryAxis[1] = field.floatArrayValues[1];
            vfPrimaryAxis[2] = field.floatArrayValues[2];

            index = node.getFieldIndex("secondaryAxis");
            field = node.getFieldValue(index);
            vfSecondaryAxis[0] = field.floatArrayValues[0];
            vfSecondaryAxis[1] = field.floatArrayValues[1];
            vfSecondaryAxis[2] = field.floatArrayValues[2];


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
        return (index < 0 || index > LAST_TMPARAMS_INDEX) ?
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
            case FIELD_LOCOCENTER:
                fieldData.clear();
                fieldData.floatArrayValues = vfLococenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_PRIMARY_AXIS:
                fieldData.clear();
                fieldData.floatArrayValues = vfPrimaryAxis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SECONDARY_AXIS:
                fieldData.clear();
                fieldData.floatArrayValues = vfSecondaryAxis;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an integer.
     * This would be used to set SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param len
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, float[] value, int len)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_LOCOCENTER:
                if(!inSetup)
                    throwInitOnlyWriteException("lococenter");

                checkSFVec3fRange(value, "lococenter");

                vfLococenter[0] = value[0];
                vfLococenter[1] = value[1];
                vfLococenter[2] = value[2];
                break;

            case FIELD_PRIMARY_AXIS:
                if(!inSetup)
                    throwInitOnlyWriteException("primaryAxis");

                checkSFVec3fRange(value, "primaryAxis");
                vfPrimaryAxis[0] = value[0];
                vfPrimaryAxis[1] = value[1];
                vfPrimaryAxis[2] = value[2];
                break;

            case FIELD_SECONDARY_AXIS:
                if(!inSetup)
                    throwInitOnlyWriteException("secondaryAxis");

                checkSFVec3fRange(value, "secondaryAxis");

                vfSecondaryAxis[0] = value[0];
                vfSecondaryAxis[1] = value[1];
                vfSecondaryAxis[2] = value[2];
                break;

            default:
                super.setValue(index, value, len);
        }
    }

    /**
     * Internal convenience method to check the valid range of the SFVec3f
     * field value and generate an appropriate exception if out of range.
     * If the vector is all in range, this will exit normally, otherwise it
     * will thrown an exception.
     *
     * @param value The vector to check
     * @param field The name of the field to issue the event for
     * @throws InvalidFieldValueException Value was out of range
     */
    private void checkSFVec3fRange(float[] value, String field) {
	if(value[0] < -1 || value[0] > -1) {
	     String msg = OUT_OF_RANGE_MSG + value[0];
	     throw new InvalidFieldValueException(msg, field);
	}

	if(value[1] < -1 || value[1] > -1) {
	     String msg = OUT_OF_RANGE_MSG + value[1];
	     throw new InvalidFieldValueException(msg, field);
	}

	if(value[2] < -1 || value[2] > -1) {
	     String msg = OUT_OF_RANGE_MSG + value[2];
	     throw new InvalidFieldValueException(msg, field);
	}
    }
}

