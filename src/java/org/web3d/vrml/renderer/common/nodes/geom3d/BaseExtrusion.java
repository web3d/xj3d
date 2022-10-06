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

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of an Extrusion.
 * An Extrusion node specifies geometric shapes based on a
 * two-dimensional cross-section extruded along a three dimensional
 * spine in the local coordinate system.<p>
 * The cross-sections of an Extrusion can be scaled and rotated
 * at each spine point to produce a wide variety of shapes.  For
 * each spine point, the cross-section shape is scaled by the
 * vfScale parameter, translated by the spine parameter, and then
 * oriented using the orientation parameter.  Corresponding
 * vertices of the first and second cross-sections are then
 * connected, forming a quadrilateral polygon between each pair
 * of vertices.
 *
 * @author Andrzej Kapolka, additional commenting by Eric Fickenscher
 * @version $Revision: 1.19 $
 */
public abstract class BaseExtrusion extends AbstractNode
    implements VRMLGeometryNodeType {

    /** Field Index for beginCap */
    protected static final int FIELD_BEGIN_CAP = LAST_NODE_INDEX + 1;

    /** Field Index for ccw */
    protected static final int FIELD_CCW = LAST_NODE_INDEX + 2;

    /** Field Index for convex */
    protected static final int FIELD_CONVEX = LAST_NODE_INDEX + 3;

    /** Field Index for creaseAngle */
    protected static final int FIELD_CREASE_ANGLE = LAST_NODE_INDEX + 4;

    /** Field Index for crossSection */
    protected static final int FIELD_CROSS_SECTION = LAST_NODE_INDEX + 5;

    /** Field Index for endCap */
    protected static final int FIELD_END_CAP = LAST_NODE_INDEX + 6;

    /** Field Index for orientation */
    protected static final int FIELD_ORIENTATION = LAST_NODE_INDEX + 7;

    /** Field Index for scale */
    protected static final int FIELD_SCALE = LAST_NODE_INDEX + 8;

    /** Field Index for solid */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 9;

    /** Field Index for spine */
    protected static final int FIELD_SPINE = LAST_NODE_INDEX + 10;

    /** Field Index for set_crossSection */
    protected static final int FIELD_SET_CROSS_SECTION = LAST_NODE_INDEX + 11;

    /** Field Index for set_orientation */
    protected static final int FIELD_SET_ORIENTATION = LAST_NODE_INDEX + 12;

    /** Field Index for set_scale */
    protected static final int FIELD_SET_SCALE = LAST_NODE_INDEX + 13;

    /** Field Index for set_spine */
    protected static final int FIELD_SET_SPINE = LAST_NODE_INDEX + 14;

    /** The last index in this node */
    protected static final int LAST_EXTRUSION_INDEX = FIELD_SET_SPINE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_EXTRUSION_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /**
     * This field specifies the vertex ordering -
     * if TRUE, the right-hand rule is used, meaning
     * the vertices are ordered in a counter-clockwise
     * direction. <p> SFBool ccw
     */
    protected boolean vfCCW;

    /**
     * This field specifies if the cross-section is convex
     * or concave.  If FALSE, the cross-section is concave;
     * the browser will split the cross-section into
     * smaller convex cross sections. <p>
     * SFBool convex
     */
    protected boolean vfConvex;

    /**
     * creaseAngle specifies an angle threshold.  If two
     * adjacent faces make an angle bigger than the
     * creaseAngle, viewer will clearly see where the two
     * faces meet - the edge linking the two faces is sharp.
     * Otherwise the edge linking the two faces will be smooth.
     * <p>SFFloat creaseAngle
     */
    protected float vfCreaseAngle;

    /**
     * This is an array of floats that represents
     * the extrusion's 2D cross-section. The 2D cross-
     * section is replicated once for each 'spine'
     * point, and manipulated with the 'scale' and
     * 'orientation' fields.
     * <p> This float array represents a 2D cross-section
     * in the y=0 plane.  Thus, every two float values
     * constitute a point (index [0] is the "x" value of the
     * first point, index [1] is the "z" value of the first
     * point; index [2] is the "x" value of the second
     * point, etc.).  When the final two values match the
     * first two values, the 2D cross-section is closed
     * (the first and last points are the same). If
     * crossSection is not a closed curve, note that
     * beginCaps or endCaps can still be generated by
     * adding a final point to the crossSection that is
     * equal to the initial point.  A simple case of a
     * capped open surface is a shape analogous to a soda
     * can sliced in half vertically.
     *  <p>
     * MFVec2f crossSection
     */
    protected float[] vfCrossSection;

    /**
     * This int keeps track of the number of valid items in
     * vfCrossSection.  Since vfCrossSection represents a
     * number of points in a 2D plane, there are two values
     * needed to represent each point.  Thus, the number of
     * points is always equal to half the length of
     * vfCrossSection.
     */
    protected int numCrossSection;

    /**
     * This field specifies if the extruded shape is open
     * or closed at one end.  If TRUE, the 'beginning'
     * spine point will be closed.  If vfEndCap is also
     * TRUE, then user will _not_ be able to see into the
     * object. <p>
     * SFBool beginCap
     */
    protected boolean vfBeginCap;

    /**
     * This field specifies if the extruded shape is open
     * or closed at one end.  If TRUE, the final 'end
     * spine point will be closed.  If vfBeginCap is also
     * TRUE, then user will _not_ be able to see into the
     * object. <p>
     * SFBool endCap
     */
    protected boolean vfEndCap;

    /**
     * This field specifies cross-section rotation.
     * The final orientation of each cross-section is computed
     * by first orienting it relative to the spine segments
     * on either side of the point at which the cross-
     * section is placed (the "spine-aligned cross-section
     * plane", or SCP) and then rotating it according to
     * the appropriate orientation value. <p> Each orientation
     * value is defined by four float values.  The first
     * three floats specify an axis corresponding to a ray cast
     * from the relative-to-the-SCP origin through an x-, y-,
     * and z- point.  The fourth float specifies the amount
     * (in radians) of rotation around this axis; 3.14
     * corresponds to 180 degrees of rotation, .523
     * corresponds to 60 degrees of rotation, etc. <p> As
     * is the case with the 'scale' field, if the number
     * of orientation values is greater than the number
     * of spine points, the excess values are ignored.
     * If there is only one orientation value, it is
     * applied at all spine points.
     * <p> MFRotation orientation
     */
    protected float[] vfOrientation;

    /**
     * This int keeps track of the number of valid items
     * in vfOrientation.  Since orientation specifies
     * a rotation around an axis, four float values are
     * needed - the first three specify the axis of
     * rotation via x-, y-, and z- values (ie: yaw,
     * pitch, and roll), and the fourth float value
     * specifies the amount (in radians) of rotation
     * around this axis.  Thus, the the number of
     * orientation points is always equal to one-
     * fourth the length of vfOrientation.
     */
    protected int numOrientation;

    /**
     * This field specifies scaling of the cross-sections.
     * Since the cross-section represents a 2-dimensional
     * shape, two values are needed to scale a cross-section.
     * Each cross-section is scaled about its relative origin
     * by the appropriate scale parameter (the first value
     * scales in X, second values scales in Z). <p>  As is
     * the case with the 'orientation' field, if the number
     * of scale values is greater than the number of spine
     * points, the excess values are ignored.  If there is
     * only one scale value, it is applied at all spine points.
     * <p> MFVec2f scale
     */
    protected float[] vfScale;

    /**
     * This int keeps track of the number of valid items
     * in vfScale.  Since vfScale specifies the scaling
     * of a two-dimensional object, two float values are
     * needed - the first specifies the amount of scaling
     * in the x-direction and the second specifies the
     * amount of scaling in the z-direction.  Thus, the
     * number of scale points is always equal to one-half
     * the length of vfScale.
     */
    protected int numScale;

    /**
     * This field specifies if the extruded shape is
     * solid or not.  If TRUE, the object is treated
     * as a solid, which means that the _inside_
     * surfaces will not be rendered.  IE, if the ends
     * are not capped and vfSolid is TRUE, user will
     * only see the faces of the object that directly
     * face the viewer.  If FALSE, the extruded shape
     * is treated as hollow, and user will see all
     * faces of the shape if the ends are not capped.
     * <p>
     * SFBool solid
     */
    protected boolean vfSolid;

    /**
     * This field represents the placement of the
     * cross-sections in 3D space.  In other words, the
     * spine defines the path that the cross-section will
     * travel.  The corners of each cross-section are connected
     * to matching corners of adjacent cross-sections, thus
     * defining a volume.  <p> In a two-point spine, the
     * cross-section is oriented so that the y-axis coincides
     * with the direction defined by the two spine points.
     * When using more spine points, this holds true for the
     * first spine point, and second and subsequent spine points
     * should orient the cross-section so that it is
     * perpendicular to the tangent of the spine.
     * <p> Every three values constitute a point
     * (indices [0],[1], and [2] represent the first point,
     * indices [3],[4], and [5] represent the second point, etc.).
     * The simplest spine consists of only two points. <p>
     * MFVec3f spine
     */
    protected float[] vfSpine;

    /**
     * This int keeps track of the number of valid items in
     * vfSpine.  Since vfSpine represents a number of points
     * in a 3D plane, there are three values needed to
     * represent each point.  Thus, the number of spine
     * points is always equal to one-third the length of
     * vfSpine.
     */
    protected int numSpine;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BEGIN_CAP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "beginCap");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_CONVEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "convex");
        fieldDecl[FIELD_CREASE_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "creaseAngle");
        fieldDecl[FIELD_CROSS_SECTION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec2f",
                                     "crossSection");
        fieldDecl[FIELD_END_CAP] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "endCap");
        fieldDecl[FIELD_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFRotation",
                                     "orientation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec2f",
                                     "scale");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_SPINE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFVec3f",
                                     "spine");
        fieldDecl[FIELD_SET_CROSS_SECTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec2f",
                                     "set_crossSection");
        fieldDecl[FIELD_SET_ORIENTATION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFRotation",
                                     "set_orientation");
        fieldDecl[FIELD_SET_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec2f",
                                     "set_scale");
        fieldDecl[FIELD_SET_SPINE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFVec3f",
                                     "set_spine");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("beginCap", FIELD_BEGIN_CAP);
        fieldMap.put("endCap", FIELD_END_CAP);
        fieldMap.put("ccw", FIELD_CCW);
        fieldMap.put("convex", FIELD_CONVEX);
        fieldMap.put("creaseAngle", FIELD_CREASE_ANGLE);
        fieldMap.put("solid", FIELD_SOLID);

        fieldMap.put("scale", FIELD_SCALE);
        fieldMap.put("set_scale", FIELD_SET_SCALE);

        fieldMap.put("orientation", FIELD_ORIENTATION);
        fieldMap.put("set_orientation", FIELD_SET_ORIENTATION);

        fieldMap.put("crossSection", FIELD_CROSS_SECTION);
        fieldMap.put("set_crossSection", FIELD_SET_CROSS_SECTION);

        fieldMap.put("spine", FIELD_SPINE);
        fieldMap.put("set_spine", FIELD_SET_SPINE);
    }

    /**
     * Construct a default extrusion instance
     */
    protected BaseExtrusion() {
        super("Extrusion");

        hasChanged = new boolean[NUM_FIELDS];

        vfBeginCap = true;
        vfCCW = true;
        vfConvex = true;
        vfCreaseAngle = 0.9f;
        vfCrossSection = new float[] { 1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
                                       -1.0f, -1.0f, 1.0f, 1.0f, 1.0f };
        numCrossSection = vfCrossSection.length / 2;

        vfEndCap = true;
        vfOrientation = new float[] { 0.0f, 0.0f, 1.0f, 0.0f };
        numOrientation = vfOrientation.length / 4;

        vfScale = new float[] { 1.0f, 1.0f };
        numScale = vfScale.length / 2;
        vfSolid = true;
        vfSpine = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };
        numSpine = vfSpine.length / 3;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseExtrusion(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("beginCap");
            VRMLFieldData field = node.getFieldValue(index);
            vfBeginCap = field.booleanValue;

            index = node.getFieldIndex("ccw");
            field = node.getFieldValue(index);
            vfCCW = field.booleanValue;

            index = node.getFieldIndex("convex");
            field = node.getFieldValue(index);
            vfConvex = field.booleanValue;

            index = node.getFieldIndex("creaseAngle");
            field = node.getFieldValue(index);
            vfCreaseAngle = field.floatValue;

            index = node.getFieldIndex("crossSection");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfCrossSection = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfCrossSection,
                                 0,
                                 field.numElements * 2);

                numCrossSection = field.numElements;
            }

            index = node.getFieldIndex("endCap");
            field = node.getFieldValue(index);
            vfEndCap = field.booleanValue;

            index = node.getFieldIndex("orientation");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfOrientation = new float[field.numElements * 4];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfOrientation,
                                 0,
                                 field.numElements * 4);

                numOrientation = field.numElements;
            }

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfScale = new float[field.numElements * 2];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfScale,
                                 0,
                                 field.numElements * 2);

                numScale = field.numElements;
            }

            index = node.getFieldIndex("solid");
            field = node.getFieldValue(index);
            vfSolid = field.booleanValue;

            index = node.getFieldIndex("spine");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSpine = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValues,
                                 0,
                                 vfSpine,
                                 0,
                                 field.numElements * 3);

                numSpine = field.numElements;
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
     *
     * @param count The number of texture coordinate sets to add
     */
    @Override
    public void setTextureCount(int count) {
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

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

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
        if(index < 0  || index > LAST_EXTRUSION_INDEX)
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

        fieldData.clear();

        switch(index) {
            case FIELD_BEGIN_CAP:
                fieldData.booleanValue = vfBeginCap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CCW:
                fieldData.booleanValue = vfCCW;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CONVEX:
                fieldData.booleanValue = vfConvex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CREASE_ANGLE:
                fieldData.floatValue = vfCreaseAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_CROSS_SECTION:
                fieldData.floatArrayValues = vfCrossSection;
                fieldData.numElements = numCrossSection;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_END_CAP:
                fieldData.booleanValue = vfEndCap;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ORIENTATION:
                fieldData.floatArrayValues = vfOrientation;
                fieldData.numElements = numOrientation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValues = vfScale;
                fieldData.numElements = numScale;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_SOLID:
                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_SPINE:
                fieldData.floatArrayValues = vfSpine;
                fieldData.numElements = numSpine;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {


        switch(index) {
            case FIELD_BEGIN_CAP:
                if(!inSetup)
                    throwInitOnlyWriteException("beginCap");

                vfBeginCap = value;
                break;

            case FIELD_CCW:
                if(!inSetup)
                    throwInitOnlyWriteException("ccw");

                vfCCW = value;
                break;

            case FIELD_CONVEX:
                if(!inSetup)
                    throwInitOnlyWriteException("convex");

                vfConvex = value;
                break;

            case FIELD_END_CAP:
                if(!inSetup)
                    throwInitOnlyWriteException("endCap");

                vfEndCap = value;
                break;

            case FIELD_SOLID:
                if(!inSetup)
                    throwInitOnlyWriteException("solid");

                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index)
        {
            case FIELD_CREASE_ANGLE:
                if(!inSetup)
                    throwInitOnlyWriteException("creaseAngle");

                vfCreaseAngle = value;
                break;

            default:
                super.setValue(index, value);
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
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_CROSS_SECTION:
                if(!inSetup)
                    throwInitOnlyWriteException("crossSection");

                vfCrossSection = value;
                numCrossSection = numValid / 2;
                break;

            case FIELD_ORIENTATION:
                if(!inSetup)
                    throwInitOnlyWriteException("orientation");

                vfOrientation = value;
                numOrientation = numValid / 4;
                break;

            case FIELD_SCALE:
                if(!inSetup)
                    throwInitOnlyWriteException("scale");

                vfScale = value;
                numScale = numValid / 2;
                break;

            case FIELD_SPINE:
                if(!inSetup)
                    throwInitOnlyWriteException("spine");

                vfSpine = value;
                numSpine = numValid / 3;
                break;

            case FIELD_SET_CROSS_SECTION:
                if(inSetup)
                    throwInputOnlyWriteException("set_crossSection");

                if(vfCrossSection.length < numValid)
                    vfCrossSection = new float[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfCrossSection, 0, numValid);

                numCrossSection = numValid / 2;
                break;

            case FIELD_SET_ORIENTATION:
                if(inSetup)
                    throwInputOnlyWriteException("set_orientation");

                if(vfOrientation.length < numValid)
                    vfOrientation = new float[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfOrientation, 0, numValid);

                numOrientation = numValid / 4;
                break;

            case FIELD_SET_SCALE:
                if(inSetup)
                    throwInputOnlyWriteException("set_scale");

                if(vfScale.length < numValid)
                    vfScale = new float[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfScale, 0, numValid);

                numScale = numValid / 2;
                break;

            case FIELD_SET_SPINE:
                if(inSetup)
                    throwInputOnlyWriteException("set_spine");

                if(vfSpine.length < numValid)
                    vfSpine = new float[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfSpine, 0, numValid);

                numSpine = numValid / 3;
                break;

            default:
                super.setValue(index, value, numValid);
        }

//        hasChanged[index] = true;
//        fireFieldChanged(index);
    }
}
