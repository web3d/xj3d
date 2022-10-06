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

package org.web3d.vrml.renderer.common.nodes.nurbs;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of the NurbsCurve node.
 * <p>
 *
 * Because NURBS implementations involve complex retessellation,
 * the implementation will automatically register itself with the
 * frame state manager whenever any field changes.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseContourPolyline2D extends AbstractNode
    implements VRMLParametricGeometryNodeType {

    /** Field index for controlPoint */
    protected static final int FIELD_CONTROL_POINT = LAST_NODE_INDEX + 1;

    /** The last index in this node */
    protected static final int LAST_CURVE_INDEX = FIELD_CONTROL_POINT;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CURVE_INDEX + 1;


    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** The value of the controlPoint field */
    protected double[] vfControlPoint;


    /** Flag indicating if the control points have changed since last update */
    private boolean controlPointsChanged;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CONTROL_POINT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFVec2d",
                                 "controlPoint");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_CONTROL_POINT;
        fieldMap.put("controlPoint", idx);
        fieldMap.put("set_controlPoint", idx);
        fieldMap.put("controlPoint_changed", idx);

    }

    /**
     * Create a new default instance of the node.
     */
    protected BaseContourPolyline2D() {
        super("ContourPolyline2D");

        hasChanged = new boolean[NUM_FIELDS];
        vfControlPoint = new double[0];

        controlPointsChanged = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseContourPolyline2D(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("controlPoint");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfControlPoint = new double[field.numElements * 4];
                System.arraycopy(field.doubleArrayValues,
                                 0,
                                 vfControlPoint,
                                 0,
                                 field.numElements);
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    @Override
    public boolean hasLocalColors() {
        return false;
    }

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    @Override
    public boolean hasLocalColorAlpha() {
        return false;
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
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

    /**
     * Set the number of textures that were found on the accompanying Appearance
     * node. Used to set the number of texture coordinates that need to be
     * passed in to the renderer when no explicit texture coordinates were
     * given.
     *     * @param count The number of texture coordinate sets to add
     */
    @Override
    public void setTextureCount(int count) {
        // default implementation does nothing
    }


    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    @Override
    public int getNumSets() {
        return 0;
    }

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    @Override
    public boolean isSolid() {
        return false;
    }

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    @Override
    public boolean isCCW() {
        return true;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    @Override
    public boolean isLightingEnabled() {
        return true;
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
        if(index < 0  || index > LAST_CURVE_INDEX)
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
        return TypeConstants.ParametricGeometryNodeType;
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
            case FIELD_CONTROL_POINT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfControlPoint;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfControlPoint == null ? 0 :
                                        vfControlPoint.length / 4;
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
                case FIELD_CONTROL_POINT:
                    destNode.setValue(destIndex, vfControlPoint, vfControlPoint.length);
                    break;


                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }


    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {

            case FIELD_CONTROL_POINT:
                setControlPoints(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }


	@Override
	public void setupFinished() {
        super.setupFinished();
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------


    /**
     * Internal convenience method to setup the control points.
     *
     * @param points The new point array to use
     */
    private void setControlPoints(double[] points) {
        int num_points = 0;
        if(points != null) {
            vfControlPoint = new double[points.length];
            num_points = points.length;
        }

        if(num_points != 0)
            System.arraycopy(points, 0, vfControlPoint, 0, num_points);

        if(!inSetup) {
            controlPointsChanged = true;
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_CONTROL_POINT] = true;
            fireFieldChanged(FIELD_CONTROL_POINT);
        }
    }

}