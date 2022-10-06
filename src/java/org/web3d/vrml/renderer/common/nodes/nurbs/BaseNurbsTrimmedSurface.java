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

import java.util.ArrayList;

// External imports
import java.util.HashMap;
import java.util.List;
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
public abstract class BaseNurbsTrimmedSurface extends AbstractNode
    implements VRMLParametricGeometryNodeType{

    /** Field index for controlPoint */
    protected static final int FIELD_CONTROL_POINT = LAST_NODE_INDEX + 1;

    /** Field index for texCoord */
    protected static final int FIELD_TEXCOORD = LAST_NODE_INDEX + 2;

    /** Field index for uTessellation */
    protected static final int FIELD_UTESSELLATION = LAST_NODE_INDEX + 3;

    /** Field index for vTessellation */
    protected static final int FIELD_VTESSELLATION = LAST_NODE_INDEX + 4;

    /** Field index for weight */
    protected static final int FIELD_WEIGHT = LAST_NODE_INDEX + 5;

    /** Field index for solid */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 6;

    /** Field index for uDimension */
    protected static final int FIELD_UDIMENSION = LAST_NODE_INDEX + 7;

    /** Field index for uKnot */
    protected static final int FIELD_UKNOT = LAST_NODE_INDEX + 8;

    /** Field index for uOrder */
    protected static final int FIELD_UORDER = LAST_NODE_INDEX + 9;

    /** Field index for vDimension */
    protected static final int FIELD_VDIMENSION = LAST_NODE_INDEX + 10;

    /** Field index for vKnot */
    protected static final int FIELD_VKNOT = LAST_NODE_INDEX + 11;

    /** Field index for vOrder */
    protected static final int FIELD_VORDER = LAST_NODE_INDEX + 12;

    /** Field index for trimmingContour */
    protected static final int FIELD_TRIMMING = LAST_NODE_INDEX + 13;

    /** Field index for uClosed */
    protected static final int FIELD_UCLOSED = LAST_NODE_INDEX + 14;

    /** Field index for vClosed */
    protected static final int FIELD_VCLOSED = LAST_NODE_INDEX + 15;

    /** The last index in this node */
    protected static final int LAST_CURVE_INDEX = FIELD_VCLOSED;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CURVE_INDEX + 1;

    /** Message for when the proto is not a Geometry */
    protected static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Geometry */
    protected static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

     /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations
    /** Proto version of the coord */
    protected VRMLProtoInstance pCoord;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** Proto version of the texCoord */
    protected VRMLProtoInstance pTexCoord;

    /** SFNode texCoord NULL */
    protected VRMLTextureCoordinateNodeType vfTexCoord;

    /** The value of the controlPoint field */
    //protected double[] vfControlPoint;

    /** The value of the vTessellation field  */
    protected int vfUTessellation;

    /** The value of the vTessellation field  */
    protected int vfVTessellation;

    /** The value of the weight field */
    protected double[] vfWeight;

    /** The value of the solid field */
    protected boolean vfSolid;

    /** The value of the uDimension field */
    protected int vfUDimension;

    /** The value of the uKnot field */
    protected double[] vfUKnot;

    /** The value of the uOrder field */
    protected int vfUOrder;

    /** The valve of the vDimension field */
    protected int vfVDimension;

    /** The valve of the vKnot field */
    protected double[] vfVKnot;

    /** The valve of the vOrder field */
    protected int vfVOrder;

    /** The valve of the vClosed field */
    protected boolean vfUClosed;

    /** The valve of the vClosed field */
    protected boolean vfVClosed;

    /* attached trimming contours will be implemented as a java ListArray */

    /**
     *
     */
    
    protected List<VRMLNodeType> vfContour;

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
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFNode",
                                 "texCoord");
        fieldDecl[FIELD_UTESSELLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFInt32",
                                 "uTessellation");
        fieldDecl[FIELD_VTESSELLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "SFInt32",
                                 "vTessellation");
        fieldDecl[FIELD_WEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                 "MFDouble",
                                 "weight");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFBool",
                                 "solid");
        fieldDecl[FIELD_VKNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFDouble",
                                 "vKnot");
        fieldDecl[FIELD_VDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "vDimension");
        fieldDecl[FIELD_VORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "vOrder");
        fieldDecl[FIELD_UKNOT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFDouble",
                                 "uKnot");
        fieldDecl[FIELD_UDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "uDimension");
        fieldDecl[FIELD_UORDER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFInt32",
                                 "uOrder");

        fieldDecl[FIELD_TRIMMING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFNode",
                                 "trimmingContour");

        fieldDecl[FIELD_UCLOSED] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFBool",
                                 "uClosed");

        fieldDecl[FIELD_VCLOSED] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "SFBool",
                                 "vClosed");


        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_UTESSELLATION;
        fieldMap.put("uTessellation", idx);
        fieldMap.put("set_uTessellation", idx);
        fieldMap.put("uTessellation_changed", idx);

        idx = FIELD_VTESSELLATION;
        fieldMap.put("vTessellation", idx);
        fieldMap.put("set_vTessellation", idx);
        fieldMap.put("vTessellation_changed", idx);

        idx = FIELD_CONTROL_POINT;
        fieldMap.put("controlPoint", idx);
        fieldMap.put("set_controlPoint", idx);
        fieldMap.put("controlPoint_changed", idx);

        idx = FIELD_WEIGHT;
        fieldMap.put("weight", idx);
        fieldMap.put("set_weight", idx);
        fieldMap.put("weight_changed", idx);

        idx = FIELD_TEXCOORD;
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        fieldMap.put("solid", FIELD_SOLID);

        fieldMap.put("uDimension", FIELD_UDIMENSION);
        fieldMap.put("vDimension", FIELD_VDIMENSION);

        fieldMap.put("uKnot", FIELD_UKNOT);
        fieldMap.put("uOrder", FIELD_UORDER);
        fieldMap.put("uClosed", FIELD_UCLOSED);

        fieldMap.put("vKnot", FIELD_VKNOT);
        fieldMap.put("vOrder", FIELD_VORDER);
        fieldMap.put("vClosed", FIELD_VCLOSED);

        fieldMap.put("trimmingContour", FIELD_TRIMMING);
    }

    /**
     * Create a new default instance of the node.
     */
    protected BaseNurbsTrimmedSurface() {
        super("NurbsTrimmedSurface");

        hasChanged = new boolean[NUM_FIELDS];
        vfUOrder = 3;
        vfVOrder = 3;
        vfSolid = true;
        vfUTessellation=0;
        vfVTessellation=0;
        vfUClosed=false;
        vfVClosed=false;

        controlPointsChanged = false;
        weightsChanged = false;
        vfContour = new ArrayList<>();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
//     public BaseNurbsTrimmedSurface(VRMLNodeType node) {
//         this(); // invoke default constructor
//
//         checkNodeType(node);
//
//         try {
//             int index = node.getFieldIndex("controlPoint");
//             VRMLFieldData field = node.getFieldValue(index);
//             if(field.numElements != 0) {
//                 vfControlPoint = new double[field.numElements * 3];
//                 System.arraycopy(field.doubleArrayValues,
//                                  0,
//                                  vfControlPoint,
//                                  0,
//                                  field.numElements);
//             }
//
//             index = node.getFieldIndex("uTessellation");
//             field = node.getFieldValue(index);
//             vfUTessellation = field.intValue;
//
//             index = node.getFieldIndex("vTessellation");
//             field = node.getFieldValue(index);
//             vfVTessellation = field.intValue;
//
//             index = node.getFieldIndex("solid");
//             field = node.getFieldValue(index);
//             vfSolid = field.booleanValue;
//
//             index = node.getFieldIndex("uOrder");
//             field = node.getFieldValue(index);
//             vfUOrder = field.intValue;
//
//             index = node.getFieldIndex("vOrder");
//             field = node.getFieldValue(index);
//             vfVOrder = field.intValue;
//
//             index = node.getFieldIndex("weight");
//             field = node.getFieldValue(index);
//             if(field.numElements != 0) {
//                 vfWeight = new double[field.numElements];
//                 System.arraycopy(field.doubleArrayValues,
//                                  0,
//                                  vfWeight,
//                                  0,
//                                  field.numElements);
//             }
//
//
//             index = node.getFieldIndex("uKnot");
//             field = node.getFieldValue(index);
//             if(field.numElements != 0) {
//                 vfUKnot = new double[field.numElements];
//                 System.arraycopy(field.doubleArrayValues,
//                                  0,
//                                  vfUKnot,
//                                  0,
//                                  field.numElements);
//             }
//
//             index = node.getFieldIndex("vKnot");
//             field = node.getFieldValue(index);
//             if(field.numElements != 0) {
//                 vfVKnot = new double[field.numElements];
//                 System.arraycopy(field.doubleArrayValues,
//                                  0,
//                                  vfVKnot,
//                                  0,
//                                  field.numElements);
//             }
//         } catch(VRMLException ve) {
//             throw new IllegalArgumentException(ve.getMessage());
//         }
//     }

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

    /*
    Methods to modify contour collection
    */

    /**
     *
     * @param c
     */
    

    public void addContour(VRMLNodeType c){
        vfContour.add(c);
    }

    /**
     *
     * @param c
     */
    public void removeContour(VRMLNodeType c){
        vfContour.remove(c);
    }

    /**
     *
     * @param children
     * @param numValid
     * @throws InvalidFieldValueException
     */
    public void setContour(VRMLNodeType[] children, int numValid)
        throws InvalidFieldValueException {
        clearContour();
        for (int i = 0; i < numValid; ++i){
            addContour( children[i] );
        }

    }

    /**
     *
     */
    public void clearContour(){
        vfContour.clear();
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


        if(pTexCoord != null)
            pTexCoord.setupFinished();

        if(vfTexCoord != null)
            vfTexCoord.setupFinished();

        for (VRMLNodeType contour: vfContour)
            contour.setupFinished();

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

            case FIELD_TEXCOORD:
                fieldData.clear();
                if(pTexCoord != null)
                    fieldData.nodeValue = pTexCoord;
                else
                    fieldData.nodeValue = vfTexCoord;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_WEIGHT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfWeight;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfWeight == null ? 0 : vfWeight.length;
                break;

            case FIELD_UKNOT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfUKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfUKnot == null ? 0 : vfUKnot.length;
                break;

            case FIELD_VKNOT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfVKnot;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = vfVKnot == null ? 0 : vfVKnot.length;
                break;

            case FIELD_UORDER:
                fieldData.clear();
                fieldData.intValue = vfUOrder;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VORDER:
                fieldData.clear();
                fieldData.intValue = vfVOrder;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_UDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfUDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfVDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_UTESSELLATION:
                fieldData.clear();
                fieldData.intValue = vfUTessellation;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_VTESSELLATION:
                fieldData.clear();
                fieldData.intValue = vfVTessellation;
                fieldData.dataType = VRMLFieldData.INT_DATA;
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

                case FIELD_UTESSELLATION:
                    destNode.setValue(destIndex, vfUTessellation);
                    break;

                case FIELD_VTESSELLATION:
                    destNode.setValue(destIndex, vfVTessellation);
                    break;

                case FIELD_TEXCOORD:
                    if(pTexCoord != null)
                        destNode.setValue(destIndex, pTexCoord);
                    else
                        destNode.setValue(destIndex, vfTexCoord);
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

            case FIELD_SOLID:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to field solid");

                vfSolid = value;
                break;

            case FIELD_UCLOSED:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to field uClosed");

                vfUClosed = value;
                break;

            case FIELD_VCLOSED:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot write to field vClosed");

                vfVClosed = value;
                break;

            default:
                super.setValue(index, value);
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
            case FIELD_UORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field uOrder");

                vfUOrder = value;
//                if(vfUOrder < 2)
//                    throw new InvalidFieldValueException("uOrder < 2: "+value);
                break;

            case FIELD_VORDER:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field vOrder");

                vfVOrder = value;
//                if(vfVOrder < 2)
//                    throw new InvalidFieldValueException("vOrder < 2: "+value);
                break;

            case FIELD_UTESSELLATION:
                vfUTessellation = value;
                break;

            case FIELD_VTESSELLATION:
                vfVTessellation = value;
                break;

            case FIELD_UDIMENSION:
                vfUDimension = value;
                break;

            case FIELD_VDIMENSION:
                vfVDimension = value;
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

            case FIELD_UKNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field uKnot");

                if(value != null) {
                    vfUKnot = new double[value.length];
                    System.arraycopy(value, 0, vfUKnot, 0, value.length);
                }

                break;

            case FIELD_VKNOT:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set field vKnot");

                if(value != null) {
                    vfVKnot = new double[value.length];
                    System.arraycopy(value, 0, vfVKnot, 0, value.length);
                }

                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_TEXCOORD:
                setTexCoord(node);
                break;

            case FIELD_CONTROL_POINT:
                setControlPoints(node);
                break;

            case FIELD_TRIMMING:
                //System.out.println("BaseNurbsTrimmedSurface:setValue: trimmingContour: 1 node " + child.getVRMLNodeName());
                addContour(node);
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @param numValid The number of valid children in array
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

//        VRMLNodeType[] nodes = children;

        switch(index) {
            case FIELD_TRIMMING:
                System.out.println("call to setValue: trimmingContour: numValid:"+numValid);
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //-------------------------------------------------------------
    // Internal convenience methods
    //-------------------------------------------------------------

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
                throw new InvalidFieldValueException(TEXTURE_NODE_MSG);
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
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param app The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setTexCoord(VRMLNodeType tex)
        throws InvalidFieldValueException {

        VRMLTextureCoordinateNodeType node;

        VRMLNodeType old_node;

        if(pTexCoord != null)
            old_node = pTexCoord;
        else
            old_node = vfTexCoord;

        if (tex instanceof VRMLProtoInstance) {
            node = (VRMLTextureCoordinateNodeType)
                ((VRMLProtoInstance)tex).getImplementationNode();
            pTexCoord = (VRMLProtoInstance)tex;
            if ((node != null) && !(node instanceof VRMLTextureCoordinateNodeType)) {
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);
            }
        } else if (tex != null &&
            (!(tex instanceof VRMLTextureCoordinateNodeType))) {
            throw new InvalidFieldValueException(TEXTURE_NODE_MSG);
        } else {
            pTexCoord = null;
            node = (VRMLTextureCoordinateNodeType)tex;
        }

        vfTexCoord = node;

        if(tex != null)
            updateRefs(tex, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(tex != null)
                stateManager.registerAddedNode(tex);

            hasChanged[FIELD_TEXCOORD] = true;
            fireFieldChanged(FIELD_TEXCOORD);
        }
    }

}
