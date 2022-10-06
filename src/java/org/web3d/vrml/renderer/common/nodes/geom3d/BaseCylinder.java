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

package org.web3d.vrml.renderer.common.nodes.geom3d;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a Cylinder.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public class BaseCylinder extends AbstractNode
    implements VRMLGeometryNodeType {

    /** Index of the solid field */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 1;

    /** Index for field radius */
    protected static final int FIELD_RADIUS = LAST_NODE_INDEX + 2;

    /** Index for field height */
    protected static final int FIELD_HEIGHT = LAST_NODE_INDEX + 3;

    /** Index for field bottom */
    protected static final int FIELD_BOTTOM = LAST_NODE_INDEX + 4;

    /** Index for field side */
    protected static final int FIELD_SIDE = LAST_NODE_INDEX + 5;

    /** Index for field top */
    protected static final int FIELD_TOP = LAST_NODE_INDEX + 6;

    /** The last index used */
    protected static final int LAST_CYLINDER_INDEX = FIELD_TOP;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_CYLINDER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** SFFloat radius 1 */
    protected float vfRadius;

    /** SFFloat height 2 */
    protected float vfHeight;

    /** SFBool bottom true */
    protected boolean vfBottom;

    /** SFBool side true */
    protected boolean vfSide;

    /** SFBool top true */
    protected boolean vfTop;

    /** field SFBool solid TRUE */
    protected boolean vfSolid;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_RADIUS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "radius");
        fieldDecl[FIELD_HEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "height");
        fieldDecl[FIELD_BOTTOM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "bottom");
        fieldDecl[FIELD_SIDE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "side");
        fieldDecl[FIELD_TOP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "top");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("radius", FIELD_RADIUS);
        fieldMap.put("height", FIELD_HEIGHT);
        fieldMap.put("bottom", FIELD_BOTTOM);
        fieldMap.put("side", FIELD_SIDE);
        fieldMap.put("top", FIELD_TOP);
        fieldMap.put("solid", FIELD_SOLID);
    }

    /**
     * Construct a default cylinder.
     */
    protected BaseCylinder() {
        super("Cylinder");

        hasChanged = new boolean[NUM_FIELDS];

        vfRadius = 1;
        vfHeight = 2;
        vfBottom = true;
        vfSide = true;
        vfTop = true;
        vfSolid = true;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseCylinder(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        vfSolid = ((VRMLGeometryNodeType)node).isSolid();

        try {
            int index = node.getFieldIndex("bottom");
            VRMLFieldData field = node.getFieldValue(index);
            vfBottom = field.booleanValue;

            index = node.getFieldIndex("radius");
            field = node.getFieldValue(index);
            vfRadius = field.floatValue;

            index = node.getFieldIndex("height");
            field = node.getFieldValue(index);
            vfHeight = field.floatValue;

            index = node.getFieldIndex("side");
            field = node.getFieldValue(index);
            vfSide = field.booleanValue;

            index = node.getFieldIndex("top");
            field = node.getFieldValue(index);
            vfTop = field.booleanValue;
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
     *
     * @param count The number of texture coordinate sets to add
     */
    @Override
    public void setTextureCount(int count) {
        // Default implementaiton does nothing
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
        return vfSolid;
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
        if(index < 0  || index > LAST_CYLINDER_INDEX)
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
        return TypeConstants.GeometryNodeType;
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
            case FIELD_SOLID:
                if(vrmlMajorVersion < 3)
                    throw new InvalidFieldException("Field solid not defined for VRML97");

                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_RADIUS:
                fieldData.clear();
                fieldData.floatValue = vfRadius;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_HEIGHT:
                fieldData.clear();
                fieldData.floatValue = vfHeight;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_TOP:
                fieldData.clear();
                fieldData.booleanValue = vfTop;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_BOTTOM:
                fieldData.clear();
                fieldData.booleanValue = vfBottom;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_SIDE:
                fieldData.clear();
                fieldData.booleanValue = vfSide;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_RADIUS:
                if(!inSetup)
                    throwInitOnlyWriteException("radius");

                vfRadius = value;
                return;

            case FIELD_HEIGHT:
                if(!inSetup)
                    throwInitOnlyWriteException("height");

                vfHeight = value;
                return;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BOTTOM:
                if(!inSetup)
                    throwInitOnlyWriteException("bottom");

                vfBottom = value;
                break;

            case FIELD_SIDE:
                if(!inSetup)
                    throwInitOnlyWriteException("side");

                vfSide = value;
                break;

            case FIELD_TOP:
                if(!inSetup)
                    throwInitOnlyWriteException("top");

                vfTop = value;
                break;

            case FIELD_SOLID:
                if(vrmlMajorVersion < 3)
                    throw new InvalidFieldException("Field solid not defined for VRML97");

                if(!inSetup)
                    throwInitOnlyWriteException("solid");

                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}
