package org.web3d.vrml.renderer.common.nodes.cadgeometry;
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

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Implementation of the abstract X3DRigidJointNode type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class BaseFace extends AbstractNode implements VRMLBREPFaceNode, VRMLGeometryNodeType {

    // Field index constants

    /** The field index for mustOutput */
    protected static final int FIELD_INNER_WIRE= LAST_NODE_INDEX +1;

    /** The field index for mustOutput */
    protected static final int FIELD_SURFACE= LAST_NODE_INDEX +2 ;

    /** Last index used by this base node */
    protected static final int LAST_INDEX = FIELD_SURFACE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_INDEX + 1;

    // The VRML field values

    /** The number of valid values in vfMustOutput */
    protected int numMustOutput;

    /** Converted version of the output index list */
    protected int[] outputIndices;

    /** The number of valid values in the output list */
    protected int numOutputIndices;

    /**
     * Listing of field indexes that have nodes
     */
    private static int[] nodeFields;

    /**
     * List of attribute nodes provided
     */
    protected List<VRMLNodeType> vfInnerWire;

    /**
     * List of attribute nodes provided
     */
    protected VRMLNodeType vfSurface;

    /**
     * Array of VRMLFieldDeclarations
     */
    private static VRMLFieldDeclaration[] fieldDecl;

    /**
     * Hashmap between a field name and its index
     */
    private static final Map<String, Integer> fieldMap;

    protected BaseCADKernelRenderer renderer;

    /**
     * To avoid recomputing the whole BREP at each call to setupFinished
     */
    protected boolean recompute = true;

    // Static constructor

    static {

        nodeFields = new int[]{FIELD_METADATA, FIELD_INNER_WIRE, FIELD_SURFACE};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] = new VRMLFieldDeclaration(
                FieldConstants.EXPOSEDFIELD, "SFNode", "metadata");

        fieldDecl[FIELD_INNER_WIRE] = new VRMLFieldDeclaration(
                FieldConstants.EXPOSEDFIELD, "MFNode", "innerWire");

        fieldDecl[FIELD_SURFACE] = new VRMLFieldDeclaration(
                FieldConstants.EXPOSEDFIELD, "SFNode", "surface");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_INNER_WIRE;
        fieldMap.put("innerWire", idx);
        fieldMap.put("set_innerWire", idx);
        fieldMap.put("attrib_innerWire", idx);

        idx = FIELD_SURFACE;
        fieldMap.put("surface", idx);
        fieldMap.put("set_surface", idx);
        fieldMap.put("attrib_surface", idx);

    }

    public BaseFace() {
        super("Face");

        hasChanged = new boolean[NUM_FIELDS];

        vfInnerWire = new ArrayList<>();

    }

    public BaseFace(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLRigidJointNodeType
    //----------------------------------------------------------
    /**
     * Get the number of valid fields that the user has requested updates for.
     *
     * @return a value greater than or equal to zero
     */
    public int numOutputs() {
        return numOutputIndices;
    }

    /**
     * Get the array of output field indices for this joint. These are
     * previously mapped internally from the output listing to the field index
     * values corresponding to the user-supplied field names, as well as
     * processing for the special NONE and ALL types.
     *
     * @return an array of field indices that are to be used
     */
    public int[] getOutputFields() {
        return outputIndices;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------
    /**
     * Get the primary type of this node. Replaces the instanceof mechanism for
     * use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.BREPNodeType;
    }

    /**
     * Notification that the construction phase of this node has finished. If
     * the node would like to do any internal processing, such as setting up
     * geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if (!inSetup) {
            return;
        }

        super.setupFinished();

    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will return
     * the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();
        VRMLNodeType kids[];
        switch (index) {

            case FIELD_INNER_WIRE:
                kids = new VRMLNodeType[vfInnerWire.size()];
                vfInnerWire.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValues = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
                break;

//		case FIELD_SURFACE:
//			kids = new VRMLNodeType[vfSurface.size()];
//			vfSurface.toArray(kids);
//			fieldData.clear();
//			fieldData.nodeArrayValues = kids;
//			fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
//			fieldData.numElements = kids.length;
//			break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a single float. This
     * would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
            throws InvalidFieldValueException, InvalidFieldException {

        switch (index) {

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
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
            throws InvalidFieldException, InvalidFieldValueException {
        switch (index) {
            case FIELD_INNER_WIRE:
                if (child instanceof BaseWire) {
                    ((VRMLBREPNodeType) child).set_renderer(renderer);
                    renderer.addRelation(this, child);
                } else {
                    throw new InvalidNodeTypeException(child.getVRMLNodeName());
                }
                vfInnerWire.add(child);
                break;

            case FIELD_SURFACE:
                if (child instanceof VRMLBREPSurfaceNode) {
                    ((VRMLBREPNodeType) child).set_renderer(renderer);
                    renderer.addRelation(this, child);
                } else {
                    throw new InvalidNodeTypeException(child.getVRMLNodeName());
                }
                vfSurface = child;
                break;

        }
    }

    @Override
    public void set_renderer(Object renderer) {
        this.renderer = (BaseCADKernelRenderer) renderer;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------
    /**
     * Specified whether this node has color information. If so, then it will be
     * used for diffuse terms instead of materials.
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
     * Add a listener for local color changes. Nulls and duplicates will be
     * ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Remove a listener for local color changes. Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Add a listener for texture coordinate generation mode changes. Nulls and
     * duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Remove a listener for texture coordinate generation mode changes. Nulls
     * will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Get the texture coordinate generation mode. NULL is returned if the
     * texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    @Override
    public String getTexCoordGenMode(int setNum) {
        return null;
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

    @Override
    public boolean isSolid() {
        return false;
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
     * Get the list of indices that correspond to fields that contain nodes ie
     * MFNode and SFNode). Used for blind scene graph traversal without needing
     * to spend time querying for all fields etc. If a node does not have any
     * fields that contain nodes, this shall return null. The field list covers
     * all field types, regardless of whether they are readable or not at the
     * VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields or
     * null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give a
     * value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if ((index < 0) || (index > LAST_INDEX)) {
            return null;
        }

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
     * @see org.web3d.vrml.nodes.VRMLBREPNodeType#render()
     */
    @Override
    public void render() {
        try {
            renderer.renderFaceBREPNonBlocking(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(System.err);
        }
    }
}
