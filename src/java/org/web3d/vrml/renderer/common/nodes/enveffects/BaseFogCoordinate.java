/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 3006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.enveffects;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation of a Coordinate node.
 * <p>
 *
 * This node is an custom extension to Xj3D node.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseFogCoordinate extends BaseGeometricPropertyNode {

    /** Field Index */
    protected static final int FIELD_DEPTH = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_COORDINATE_INDEX = FIELD_DEPTH;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_COORDINATE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** inputOut MFFloat depth */
    protected float[] vfDepth;

    /** actual length of vfDepth */
    protected int numDepth;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_DEPTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "depth");
        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_DEPTH;
        fieldMap.put("depth", idx);
        fieldMap.put("set_depth", idx);
        fieldMap.put("depth_changed", idx);
    }

    /**
     * Default constructor creates a default instance of this node.
     */
    public BaseFogCoordinate() {
        super("FogCoordinate");

        vfDepth = FieldConstants.EMPTY_MFFLOAT;
        hasChanged = new boolean[LAST_COORDINATE_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseFogCoordinate(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("depth");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfDepth = new float[field.numElements];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfDepth,
                                 0,
                                 field.numElements);

                numDepth = field.numElements;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLFogCoordinateNodeType
    //-------------------------------------------------------------

    /**
     * Set a new value for the depth field. Depth is an array of floats.
     *
     * @param depths New value for the depth field
     * @param numValid The number of valid values to copy from the array
     */
    public void setDepth(float[] depths, int numValid) {
        if(numValid > vfDepth.length)
            vfDepth = new float[numValid];

        numDepth = numValid;
        System.arraycopy(depths,0, vfDepth, 0, numDepth);

        // We have to send the new value here because it will be the
        // correct length.
        if(!inSetup) {
            fireComponentChanged(FIELD_DEPTH);

            hasChanged[FIELD_DEPTH] = true;
            fireFieldChanged(FIELD_DEPTH);
        }
    }

    /**
     * Get the number of items in the point array now. The number returned is
     * the total number of values in the flat array. This will allow the caller
     * to construct the correct size array for the getPoint() call.
     *
     * @return The number of values in the array
     */
    public int getNumDepth() {
        return numDepth;
    }

    /**
     * Get current value of the depth field. Depth is an array of floats. Don't
     * call if there are no values in the array.
     *
     * @param depths The array to copy the values into
     */
    public void getDepth(float[] depths) {
        if(vfDepth != null)
            System.arraycopy(vfDepth, 0, depths, 0, numDepth);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        if (index < 0  || index > LAST_COORDINATE_INDEX)
            return null;

        return fieldDecl[index];
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.CoordinateNodeType;
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
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_DEPTH:
                fieldData.clear();
                fieldData.floatArrayValues = vfDepth;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numDepth;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_DEPTH:
                    destNode.setValue(destIndex, vfDepth, numDepth);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DEPTH:
                setDepth(value, numValid);
                break;

            default :
                super.setValue(index, value, numValid);
        }
    }
}
