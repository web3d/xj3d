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
package org.web3d.vrml.renderer.common.nodes.cadgeometry;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a Sphere.
 *
 * @author Russell Dodds
 * @version $Revision: 1.3 $
 */
public abstract class BasePointBREP extends AbstractNode implements
        VRMLGeometryNodeType, VRMLBREPNodeType {

    /**
     * Field Index for vertices
     */
    protected static final int FIELD_VERTEX = LAST_NODE_INDEX + 1;

    /**
     * The last index in this node
     */
    protected static final int LAST_INDEX = FIELD_VERTEX;

    /**
     * Number of fields constant
     */
    protected static final int NUM_FIELDS = LAST_INDEX + 1;

    /**
     * List of per-vertex attribute nodes provided
     */
    protected List<VRMLNodeType> vfVertex;

    /**
     * Array of VRMLFieldDeclarations
     */
    private static VRMLFieldDeclaration[] fieldDecl;

    /**
     * Hashmap between a field name and its index
     */
    private static final Map<String, Integer> fieldMap;

    /**
     * Listing of field indexes that have nodes
     */
    private static int[] nodeFields;

    protected BaseCADKernelRenderer cadKernelRenderer;

    // Static constructor

    static {

        nodeFields = new int[]{FIELD_METADATA, FIELD_VERTEX};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] = new VRMLFieldDeclaration(
                FieldConstants.EXPOSEDFIELD, "SFNode", "metadata");

        fieldDecl[FIELD_VERTEX] = new VRMLFieldDeclaration(
                FieldConstants.EXPOSEDFIELD, "MFNode", "coord");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_VERTEX;
        fieldMap.put("vertex", idx);
        fieldMap.put("set_vertex", idx);
        fieldMap.put("attrib_vertex", idx);

    }

    /**
     * Construct a default PointBREP instance
     */
    protected BasePointBREP() {
        super("PointBREP");

        hasChanged = new boolean[NUM_FIELDS];

        vfVertex = new ArrayList<>();
    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node. If the node is not a Box node, an exception will be thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BasePointBREP(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

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

    @Override
    public int[] getSecondaryType() {
        return new int[]{TypeConstants.BREPPointBREPType};
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

        switch (index) {

            case FIELD_VERTEX:
                VRMLNodeType kids[] = new VRMLNodeType[vfVertex.size()];
                vfVertex.toArray(kids);
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
            case FIELD_VERTEX:
                if (child instanceof VRMLCoordinateNodeType) {
                    cadKernelRenderer.addRelation(this, child);
                } else {
                    throw new InvalidNodeTypeException(child.getVRMLNodeName());
                }

                vfVertex.add(child);
                break;

        }
    }

    @Override
    public void render() {
        cadKernelRenderer.render();
    }

    /*
     * No need to set renderer for Brep object root, because it instanciates its own.
     * Method left empty.
     */
    @Override
    public void set_renderer(Object renderer) {
    }

    @Override
    public boolean isSolid() {
        // TODO Auto-generated method stub
        return false;
    }
}
