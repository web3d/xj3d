/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureCoordinateNodeType;
import org.web3d.vrml.renderer.common.nodes.BaseGeometricPropertyNode;

/**
 * Common base implementation of a texture coordinate node for 3D coordinates.
 * <p>
 *
 * Points are held internally as a flat array of values. The point list
 * returned will always be flat. We do this because renderers like point values
 * as a single flat array. The array returned will always contain exactly the
 * number of points specified.
 * <p>
 * The effect of this is that point values may be routed out of this node as
 * a flat array of points rather than a 2D array. Receiving nodes should check
 * for this version as well. This implementation will handle being routed
 * either form.
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public abstract class BaseTextureCoordinate3D extends BaseGeometricPropertyNode
    implements VRMLTextureCoordinateNodeType {

    /** Field index for point */
    protected static final int FIELD_POINT = LAST_NODE_INDEX + 1;

    /** The last field index used by this class */
    protected static final int LAST_TEXTURECOORDINATE_INDEX = FIELD_POINT;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TEXTURECOORDINATE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations


    // Internally the arrays are kept as max of whatever was sent to us in
    // a setValue.  So a length variable is needed to know the "real" length
    // This will speed setValue calls.  It will cost on sendRoute

    /** exposedField MFVec3f */
    protected float[] vfPoint;

    /** Number of valid values in vfPoint */
    protected int numPoint;

    //----------------------------------------------------------
    // Methods internal to NRTextureCoordinate
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFVec3f",
                                     "point");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_POINT;
        fieldMap.put("point", idx);
        fieldMap.put("set_point", idx);
        fieldMap.put("point_changed", idx);
    }

    /**
     * Empty constructor
     */
    protected BaseTextureCoordinate3D() {
        super("TextureCoordinate3D");

        hasChanged = new boolean[LAST_TEXTURECOORDINATE_INDEX + 1];

        vfPoint = FieldConstants.EMPTY_MFVEC3F;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseTextureCoordinate3D(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("point");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfPoint = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfPoint,
                                 0,
                                 field.numElements * 3);
                numPoint = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-----------------------------------------------------------------
    // Methods required by the VRMLTextureCoordinateNodeType interface.
    //-----------------------------------------------------------------

    /**
     * Get the number of components defined for this texture type. SHould
     * be one of 2, 3 or 4 for 2D, 3D or time-driven textures. Simple VRML
     * textures only allow 2D texture coordinates to be used.
     *
     * @return one of 2, 3 or 4
     */
    @Override
    public int getNumTextureComponents() {
        return 3;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    @Override
    public int getNumSets() {
        return 1;
    }

    /**
     * Get the size of the specified set.
     *
     * @param setNum The set to size
     */
    @Override
    public int getSize(int setNum) {
        return numPoint;
    }

    /**
     * Accessor method to set a new value for field attribute point.  Attempts
     * to set nodes &gt; numSets will throw an exception.
     *
     * @param setNum The set which this point belongs.
     * @param newPoint New value for the point field
     * @param numValid The number of valid values to copy from the array
     * @throws ArrayIndexOutOfBoundsException
     */
    @Override
    public void setPoint(int setNum, float[] newPoint, int numValid) {
        if (setNum != 0)
            throw new ArrayIndexOutOfBoundsException();

        if(numValid > vfPoint.length)
            vfPoint = new float[numValid];

        numPoint = numValid;
        System.arraycopy(newPoint,0, vfPoint, 0, numPoint);

        // We have to send the new value here because it will be the
        // correct length.

        if(!inSetup) {
            fireComponentChanged(FIELD_POINT);

            hasChanged[FIELD_POINT] = true;
            fireFieldChanged(FIELD_POINT);
        }
    }

    /**
     * Accessor method to get current value of field point.  Sets outside
     * the numSize will throw an exception.
     *
     * @param point The current value of point
     * @throws ArrayIndexOutOfBoundsException
     */
    @Override
    public void getPoint(int setNum, float[] point) {
        if(setNum != 0)
            throw new ArrayIndexOutOfBoundsException();

        System.arraycopy(vfPoint,0,point,0,numPoint);
    }

    /**
     * Determine if this index is shared via DEF/USE inside this set
     *
     * @param index The index to check
     * @return The index if not shared or the original index DEFed
     */
    @Override
    public int isShared(int index) {
        return index;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    @Override
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if (index < 0  || index > LAST_TEXTURECOORDINATE_INDEX)
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
        return TypeConstants.TextureCoordinateNodeType;
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
            case FIELD_POINT:
                fieldData.clear();
                fieldData.floatArrayValues = vfPoint;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numPoint / 3;
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
                case FIELD_POINT :
                    destNode.setValue(destIndex, vfPoint, numPoint);
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
            case FIELD_POINT:
                setPoint(0, value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }
}
