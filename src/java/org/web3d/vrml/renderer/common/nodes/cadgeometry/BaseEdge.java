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
public abstract class BaseEdge extends AbstractNode implements VRMLBREPNodeType {

    // Field index constants
    /** The field index for mustOutput */
    protected static final int FIELD_CURVE = LAST_NODE_INDEX +1;

    /** The field index for mustOutput */
    protected static final int FIELD_PCURVE = LAST_NODE_INDEX +2;

    /** Last index used by this base node */
    protected static final int LAST_INDEX = FIELD_PCURVE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_INDEX + 1;

    // The VRML field values

    /** The number of valid values in vfMustOutput */
    protected int numMustOutput;

    /** Converted version of the output index list */
    protected int[] outputIndices;

    /** The number of valid values in the output list */
    protected int numOutputIndices;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** List of attribute nodes provided */
    protected List<VRMLNodeType> vfCurve;

    /** List of attribute nodes provided */
    protected List<VRMLNodeType> vfPCurve;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    protected BaseCADKernelRenderer renderer;

    // Static constructor
    static {

        nodeFields = new int[] { FIELD_METADATA,  FIELD_CURVE, FIELD_PCURVE};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] = new VRMLFieldDeclaration(
                        FieldConstants.EXPOSEDFIELD, "SFNode", "metadata");

        fieldDecl[FIELD_CURVE] = new VRMLFieldDeclaration(
                        FieldConstants.EXPOSEDFIELD, "MFNode", "curve");

        fieldDecl[FIELD_PCURVE] = new VRMLFieldDeclaration(
                        FieldConstants.EXPOSEDFIELD, "MFNode", "pcurve");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_CURVE;
        fieldMap.put("curve", idx);
        fieldMap.put("set_curve", idx);
        fieldMap.put("attrib_curve", idx);

        idx = FIELD_PCURVE;
        fieldMap.put("pcurve", idx);
        fieldMap.put("set_pcurve", idx);
        fieldMap.put("attrib_pcurve", idx);

    }

    public BaseEdge() {
        super("Edge");

        hasChanged = new boolean[NUM_FIELDS];

        vfCurve = new ArrayList<>();
        vfPCurve = new ArrayList<>();
    }

    public BaseEdge(VRMLNodeType node)
    {
        this(); // invoke default constructor

        checkNodeType(node);
    }

    /**
     * Get the number of valid fields that the user has requested updates for.
     *
     * @return a value greater than or equal to zero
     */
    public int numOutputs() {
        return numOutputIndices;
    }

    /**
     * @return an array of field indices that are to be used
     */
    public int[] getOutputFields() {
        return outputIndices;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.BREPNodeType;
    }

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

        VRMLNodeType kids[];
        switch(index) {

            case FIELD_CURVE:
                    kids = new VRMLNodeType[vfCurve.size()];
                    vfCurve.toArray(kids);
                    fieldData.clear();
                    fieldData.nodeArrayValues = kids;
                    fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                    fieldData.numElements = kids.length;
                    break;

            case FIELD_PCURVE:
                    kids = new VRMLNodeType[vfPCurve.size()];
                    vfPCurve.toArray(kids);
                    fieldData.clear();
                    fieldData.nodeArrayValues = kids;
                    fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                    fieldData.numElements = kids.length;
                    break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a single float.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldValueException, InvalidFieldException {

        switch(index) {

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
            case FIELD_CURVE:
                if (child instanceof VRMLBREPCurve3DNode) {
                    ((VRMLBREPNodeType) child).set_renderer(renderer);
                    renderer.addRelation(this, child);
                } else {
                    throw new InvalidNodeTypeException(child.getVRMLNodeName());
                }

                vfCurve.add(child);
                break;

            case FIELD_PCURVE:
                if (child instanceof VRMLBREPCurve2DNode) {
                    ((VRMLBREPNodeType) child).set_renderer(renderer);
                    renderer.addRelation(this, child);
                } else {
                    throw new InvalidNodeTypeException(child.getVRMLNodeName());
                }

                vfPCurve.add(child);
                break;

        }
    }

    @Override
    public void set_renderer(Object renderer) {
        this.renderer = (BaseCADKernelRenderer) renderer;
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
        if (fieldName.equals("geometry")) {
            return -2; //<-- ignore code
        }
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
        if (index < 0 || index > LAST_INDEX) {
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

}
