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

package org.web3d.vrml.renderer.common.nodes.cadgeometry;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;

/**
 * Common IndexedQuadSet handling.
 * <p>
 *
 *
 * @author Vincent Marchetti
 * @version $Revision: 1.6 $
 * copied from the BaseQuadSet.java class by Alan Hudson
 *
 * extra functions added for the 'index' node attribute inspired by
 * the implementation of the BaseIndexedGeometryNode
 */
public abstract class BaseIndexedQuadSet extends BaseComponentGeometryNode {

    /* in this node the field is name 'index'  */
    /* will continue to use the coordIndex label in the code */
    /* to maintain the connection with the implementation in */
    /* BaseIndexedGeometryNode */
    /** Index of the coordIndex SFInt32 field */
    protected static final int FIELD_COORDINDEX = LAST_GEOMETRY_INDEX + 1;

    /** Index of the set_coordIndex SFInt32 eventIn */
    protected static final int FIELD_SET_COORDINDEX = LAST_GEOMETRY_INDEX + 2;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = LAST_GEOMETRY_INDEX + 3;

    /** field MFInt32 coordIndex */
    protected int[] vfCoordIndex;

    /** Number of valid values in vfColorIndex */
    protected int numCoordIndex;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_NORMAL,
            FIELD_TEXCOORD,
            FIELD_COLOR,
            FIELD_FOG_COORD,
            FIELD_ATTRIBS,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFInt32",
                                     "index");
        fieldDecl[FIELD_SET_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_index");

        fieldDecl[FIELD_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "normal");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "texCoord");
        fieldDecl[FIELD_FOG_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fogCoord");
        fieldDecl[FIELD_ATTRIBS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "attrib");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");
        fieldDecl[FIELD_NORMALPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "normalPerVertex");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_COLOR;
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = FIELD_COORDINDEX;
        fieldMap.put("index", idx);
        fieldMap.put("set_index", idx);
        fieldMap.put("index_changed", idx);


        idx = FIELD_COORD;
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = FIELD_NORMAL;
        fieldMap.put("normal", idx);
        fieldMap.put("set_normal", idx);
        fieldMap.put("normal_changed", idx);

        idx = FIELD_TEXCOORD;
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        idx = FIELD_FOG_COORD;
        fieldMap.put("fogCoord", idx);
        fieldMap.put("set_fogCoord", idx);
        fieldMap.put("fogCoord_changed", idx);

        idx = FIELD_ATTRIBS;
        fieldMap.put("attrib", idx);
        fieldMap.put("set_attrib", idx);
        fieldMap.put("attrib_changed", idx);

        fieldMap.put("solid", FIELD_SOLID);
        fieldMap.put("ccw", FIELD_CCW);
        fieldMap.put("colorPerVertex", FIELD_COLORPERVERTEX);
        fieldMap.put("normalPerVertex", FIELD_NORMALPERVERTEX);
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     */
    protected BaseIndexedQuadSet() {
        super("IndexedQuadSet");
        vfCoordIndex = FieldConstants.EMPTY_MFINT32;
        numCoordIndex = 0;
        changeFlags=0;
        System.out.println("OGLIndexedQuadSet: FIELD_COORDINDEX " + FIELD_COORDINDEX);
        System.out.println("OGLIndexedQuadSet: FIELD_SET_COORDINDEX " + FIELD_SET_COORDINDEX);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseIndexedQuadSet(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);
    }

    //----------------------------------------------------------
    // Methods overriding VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    @Override
    public boolean hasLocalColors() {
        return localColors;
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addLocalColorsListener(LocalColorsListener l) {
        if (l != null)
            localColorsListeners.add(l);
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeLocalColorsListener(LocalColorsListener l) {
        localColorsListeners.remove(l);
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
        System.out.println("TexCoordGenMode changes not implemented");
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

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specifies whether a geometry object is a solid opject.
     * If true, then back-face culling can be performed
     *
     * @return The current value of solid
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

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

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
            case FIELD_COORDINDEX:
                fieldData.clear();
                fieldData.intArrayValues = vfCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numCoordIndex;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }


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
        if(index < 0  || index > NUM_FIELDS-1)
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
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        // correct the parameter if is zero length. The code assumes that
        // a field that is not set will be a null value, rather than a
        // zero length array. We may somehow end up with a zero length
        // array here as we can't trust the sender, so just make sure
        // everything is consistent.
        if(value != null && numValid == 0)
            value = null;

        switch(index) {
            case FIELD_SET_COORDINDEX:
                if(inSetup)
                    throwInputOnlyWriteException("set_coordIndex");

                vfCoordIndex = value;
                numCoordIndex = numValid;
                changeFlags |= COORDS_INDEX_CHANGED;
                stateManager.addEndOfThisFrameListener(this);
                break;

            case FIELD_COORDINDEX :
                if(!inSetup)
                    throwInitOnlyWriteException("coordIndex");

                vfCoordIndex = value;
                numCoordIndex = numValid;
                break;

        }

    }

    /**
     * Set the coordIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The list of index values to use
     * @param numValid The number of valid indices in the array
     */
    protected void setCoordIndex(int[] value, int numValid) {
        vfCoordIndex = value;
        numCoordIndex = numValid;
        changeFlags |= COORDS_INDEX_CHANGED;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLNodeComponentListener interface.
    //-------------------------------------------------------------

    /**
     * Notification that the field from the node has changed.
     *
     * @param node The component node that changed
     * @param index The index of the field that has changed
     */
    @Override
    public void fieldChanged(VRMLNodeType node, int index) {
    }
}
