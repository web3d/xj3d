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

import org.j3d.geom.GeometryData;

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
 * @version $Revision: 1.14 $
 */
public abstract class BaseNurbsCurve extends AbstractNode
    implements VRMLBREPCurve3DNode, VRMLBREPNodeType {

    /** Field index for controlPoint */
    protected static final int FIELD_CONTROL_POINT = LAST_NODE_INDEX + 1;

    /** Field index for controlPoint */
    protected static final int FIELD_TESSELLATION = LAST_NODE_INDEX + 2;

    /** Field index for controlPoint */
    protected static final int FIELD_WEIGHT = LAST_NODE_INDEX + 3;

    /** Field index for controlPoint */
    protected static final int FIELD_KNOT = LAST_NODE_INDEX + 4;

    /** Field index for order */
    protected static final int FIELD_ORDER = LAST_NODE_INDEX + 5;

    /** Field index for order */
    protected static final int FIELD_CLOSED = LAST_NODE_INDEX + 6;

    /** The last index in this node */
    protected static final int LAST_CURVE_INDEX = FIELD_CLOSED;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CURVE_INDEX + 1;

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String COORDINATE_NODE_MSG =
        "Node does not describe a Coordinate object";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** The value of the tessellation field  */
    protected int vfTessellation;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** The value of the weight field */
    protected double[] vfWeight;

    /** The value of the knot field */
    protected double[] vfKnot;

    /** The value of the order field */
    protected int vfOrder;

    /**
     *
     */
    protected boolean vfClosed;

    /** Generator used to tessellate the curve */
//    private BSplineGenerator generator;

    /** Data from the last tessellation run, LINE_STRIPS by default. */
    protected GeometryData geometryData;

    /** Flag indicating if the control points have changed since last update */
    private boolean controlPointsChanged;

    /** Flag indicating if the weights have changed since last update */
    private boolean weightsChanged;

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
                                 "SFNode",
                                 "controlPoint");
        fieldDecl[FIELD_WEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFDouble",
                                 "weight");
        fieldDecl[FIELD_KNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFDouble",
                                 "knot");
        fieldDecl[FIELD_ORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "order");

        fieldDecl[FIELD_CLOSED] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFBool",
                                 "closed");

        fieldDecl[FIELD_TESSELLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFInt32",
                                 "tessellation");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_TESSELLATION;
        fieldMap.put("tessellation", idx);
        fieldMap.put("set_tessellation", idx);
        fieldMap.put("tessellation_changed", idx);

        idx = FIELD_CONTROL_POINT;
        fieldMap.put("controlPoint", idx);
        fieldMap.put("set_controlPoint", idx);
        fieldMap.put("controlPoint_changed", idx);

        idx = FIELD_WEIGHT;
        fieldMap.put("weight", idx);
        fieldMap.put("set_weight", idx);
        fieldMap.put("weight_changed", idx);

        fieldMap.put("knot", FIELD_KNOT);
        fieldMap.put("order", FIELD_ORDER);
        fieldMap.put("closed", FIELD_CLOSED);
    }

    /**
     * Create a new default instance of the node.
     */
    protected BaseNurbsCurve() {
        super("NurbsCurve");

        hasChanged = new boolean[NUM_FIELDS];
        vfOrder = 2;
        vfClosed=false;

//        generator = new BSplineGenerator();
        geometryData = new GeometryData();
        geometryData.geometryType = GeometryData.LINE_STRIPS;

        controlPointsChanged = false;
        weightsChanged = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */

    public BaseNurbsCurve(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
//            int index = node.getFieldIndex("controlPoint");
//            VRMLFieldData field = node.getFieldValue(index);
            //vfCoord = field.nodeValue;

            int index = node.getFieldIndex("order");
            VRMLFieldData field = node.getFieldValue(index);
            vfOrder = field.intValue;

            index = node.getFieldIndex("tessellation");
            field = node.getFieldValue(index);
            vfTessellation = field.intValue;

            index = node.getFieldIndex("closed");
            field = node.getFieldValue(index);
            vfClosed = field.booleanValue;

            index = node.getFieldIndex("weight");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfWeight = new double[field.numElements];
                System.arraycopy(field.doubleArrayValues,
                                 0,
                                 vfWeight,
                                 0,
                                 field.numElements);
            }


            index = node.getFieldIndex("knot");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfKnot = new double[field.numElements];
                System.arraycopy(field.doubleArrayValues,
                                 0,
                                 vfKnot,
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
    // Methods required by the NRVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

//        boolean valid_data = true;
//
//        if(vfControlPoint != null)
//            generator.setControlPoints(vfControlPoint);
//        else
//            valid_data = false;
//
//        if(vfKnot != null &&
//           (vfKnot.length >= ((vfControlPoint.length / 4) + vfOrder)))
//            generator.setKnots(vfOrder - 1, vfKnot);
//        else
//            valid_data = false;
//
//        if(vfWeight != null)
//            generator.setWeights(vfWeight);
//
//        if(!valid_data || vfOrder < 2)
//            geometryData.vertexCount = 0;
//
//        updateFacetCount();
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
                fieldData.nodeValue = vfCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_WEIGHT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfWeight;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfWeight == null ? 0 : vfWeight.length;
                break;

            case FIELD_KNOT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfKnot == null ? 0 : vfKnot.length;
                break;

            case FIELD_ORDER:
                fieldData.clear();
                fieldData.intValue = vfOrder;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_TESSELLATION:
                fieldData.clear();
                fieldData.intValue = vfTessellation;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_CLOSED:
                fieldData.clear();
                fieldData.booleanValue = vfClosed;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                    destNode.setValue(destIndex, vfCoord);
                    break;

                case FIELD_TESSELLATION:
                    destNode.setValue(destIndex, vfTessellation);
                    break;

                case FIELD_ORDER:
                    destNode.setValue(destIndex, vfOrder);
                    break;

                case FIELD_CLOSED:
                    destNode.setValue(destIndex, vfClosed);
                    break;


                case FIELD_KNOT:
                    destNode.setValue(destIndex, vfKnot, vfKnot.length);
                    break;

                case FIELD_WEIGHT:
                    destNode.setValue(destIndex, vfWeight, vfWeight.length);
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
     * Set the value of the field at the given index as an int.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field order");

                vfOrder = value;
                if(vfOrder < 2)
                    throw new InvalidFieldValueException("Order < 2: "+value);
                break;

            case FIELD_TESSELLATION:
                vfTessellation = value;
                updateFacetCount();
                break;

            default:
                super.setValue(index, value);
        }

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);

            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean.
     * This would be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {

            case FIELD_CLOSED:
                vfClosed = value;
                updateFacetCount();
                break;

            default:
                super.setValue(index, value);
        }

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);

            hasChanged[index] = true;
            fireFieldChanged(index);
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
            case FIELD_WEIGHT:
                setWeight(value, numValid);
                break;

            case FIELD_KNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field knot");

                setKnot(value);
                break;


            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an Node
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CONTROL_POINT:
                setControlPoints(child);
                break;


            default:
                super.setValue(index, child);
        }
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

    /**
     * Internal convenience method to update the knot values. Note that it
     * assumes that the knots cannot be changed after setup is complete.
     *
     * @param knots The list of knot values to use
     */
    private void setKnot(double[] knots) {

        // Always reallocate the array. We're going to assume that this
        // very rarely changes so optimise for this case.
        if(knots != null) {
            if(vfKnot==null || knots.length > vfKnot.length)
                vfKnot = new double[knots.length];

            System.arraycopy(knots, 0, vfKnot, 0, knots.length);
        } else {
            vfKnot = null;
        }
    }

    /**
     * Internal convenience method to update the weight values.
     *
     * @param weights The list of weight values to use
     */
    private void setWeight(double[] weights, int numValid) {

        // Always reallocate the array. We're going to assume that this
        // very rarely changes so optimise for this case.
        if(numValid != 0) {
            if(vfWeight == null || numValid > vfWeight.length)
                vfWeight = new double[numValid];

            System.arraycopy(weights, 0, vfWeight, 0, numValid);
        } else {
            vfWeight = null;
        }

        if(!inSetup) {
            weightsChanged = true;
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_WEIGHT] = true;
            fireFieldChanged(FIELD_WEIGHT);
        }
    }

    /**
     * Internal convenience method to setup the control points.
     *
     * @param points The new point array to use
     */
    private void setControlPoints(VRMLNodeType node)
        throws InvalidFieldValueException{
        VRMLNodeType old_node = vfCoord;    // save previous value

        if (node != null){
            if (node instanceof VRMLCoordinateNodeType)
                vfCoord = (VRMLCoordinateNodeType) node;
            else
                throw new InvalidFieldValueException(COORDINATE_NODE_MSG);
            updateRefs(node,true);
        }

        if (old_node != null) updateRefs(old_node, true);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(node != null)
                stateManager.registerAddedNode(node);

            hasChanged[FIELD_CONTROL_POINT] = true;
            fireFieldChanged(FIELD_CONTROL_POINT);
        }

    }

    /**
     * Calculate the facetCount needed for the current state of the curve. The
     * spec states:
     * <p>
     * 1. if a tessellation value is greater than 0, the number of tessellation
     * points is tessellation+1;
     * <br>
     * 2. if a tessellation value is smaller than 0, the number of tessellation
     * points is (-tessellation * (number of control points)+1)
     * <br>
     * 3. if a tessellation value is 0, the number of tessellation points is
     * (2 * (number of control points)+1.
     */
    private void updateFacetCount() {
//        int facets = 0;
//
//        if(vfTessellation > 0)
//            facets = vfTessellation;
//        else if(vfTessellation < 0)
//            facets = -vfTessellation * vfControlPoint.length / 4;
//        else
//            facets = 2 * vfControlPoint.length / 4;
//
//        generator.setFacetCount(facets);
    }

    /**
     * Request to regenerate the curve data now. The regenerated data will end
     * up in the geometryData object. If some of the data is invalid, then the
     * generation will create a curve with zero points in it and return false.
     *
     * @return true The generation succeeded
     */
    protected boolean regenerateCurve() {

//        // sanity check to make sure all the data is valid before attempting to
//        if(vfControlPoint == null || vfKnot == null || vfOrder < 2) {
//            geometryData.vertexCount = 0;
//            return false;
//        }
//
//        if(controlPointsChanged) {
//            generator.setControlPoints(vfControlPoint);
//            controlPointsChanged = false;
//        }
//
//        if(weightsChanged) {
//            generator.setWeights(vfWeight);
//            weightsChanged = false;
//        }
//
//        generator.generate(geometryData);
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getNurbsImpl(){return null;}
}
