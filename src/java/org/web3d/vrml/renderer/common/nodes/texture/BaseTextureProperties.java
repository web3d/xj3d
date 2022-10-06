/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
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

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a TextureProperties node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class BaseTextureProperties extends AbstractNode implements VRMLTextureProperties2DNodeType {

    /** Index of the borderColor field */
    protected static final int FIELD_BORDER_COLOR = LAST_NODE_INDEX + 1;

    /** Index of the borderWidth field */
    protected static final int FIELD_BORDER_WIDTH = LAST_NODE_INDEX + 2;

    /** Index of the boundaryModeS field */
    protected static final int FIELD_BOUNDARY_MODE_S = LAST_NODE_INDEX + 3;

    /** Index of the boundaryModeT field */
    protected static final int FIELD_BOUNDARY_MODE_T = LAST_NODE_INDEX + 4;

    /** Index of the magnificationFilter field */
    protected static final int FIELD_MAGNIFICATION_FILTER = LAST_NODE_INDEX + 5;

    /** Index of the minificationFilter field */
    protected static final int FIELD_MINIFICATION_FILTER = LAST_NODE_INDEX + 6;

    /** Index of the generateMipMaps field */
    protected static final int FIELD_GENERATE_MIPMAPS = LAST_NODE_INDEX + 7;

    /** Index of the anistropicMode field */
    protected static final int FIELD_ANISOTROPIC_MODE = LAST_NODE_INDEX + 8;

    /** Index of the anistropicMode field */
    protected static final int FIELD_ANISOTROPIC_DEGREE = LAST_NODE_INDEX + 9;

    /** Index of the anistropicMode field */
    protected static final int FIELD_TEXTURE_COMPRESSION = LAST_NODE_INDEX + 10;

    /** Index of the anistropicMode field */
    protected static final int FIELD_TEXTURE_PRIORITY = LAST_NODE_INDEX + 11;

    /** Index of the boundaryModeT field */
    protected static final int FIELD_BOUNDARY_MODE_R = LAST_NODE_INDEX + 12;

    /** The last field index used by this class */
    protected static final int LAST_TEXTUREPROPS_INDEX =
            FIELD_BOUNDARY_MODE_R;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TEXTUREPROPS_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** SFColorRGBA borderColor */
    protected float[] vfBorderColor;

    /** SFInt32 boundary width */
    protected int vfBorderWidth;

    /** SFString boundaryModeS */
    protected String vfBoundaryModeS;

    /** SFString boundaryModeT */
    protected String vfBoundaryModeT;

    /** SFString boundaryModeR */
    protected String vfBoundaryModeR;

    /** SFString magnificationFilter */
    protected String vfMagnificationFilter;

    /** SFString magnificationFilter */
    protected String vfMinificationFilter;

    /** SFBool generateMipMaps */
    protected boolean vfGenerateMipMaps;

    /** SFString anistropicMode */
    protected String vfAnisotropicMode;

    /** SFFloat anistropicFilterDegree */
    protected float vfAnisotropicDegree;

    /** SFString textureCompression */
    protected String vfTextureCompression;

    /** SFFloat textureCompression */
    protected float vfTexturePriority;


    /** Boundary Mode mapping from X3D String to Texture Constants */
    private final static Map<String, Integer> bmMap;

    /** MaxificationFilter mapping from X3D String to Texture Constants */
    private final static Map<String, Integer> maxfMap;

    /** MinificationFilter mapping from X3D String to Texture Constants */
    private final static Map<String, Integer> minfMap;

    /** AnistoropicMode mapping from X3D String to Texture Constants */
    private final static Map<String, Integer> aniMap;


    /**
     * Static constructor to initialise all of the field values.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BORDER_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColorRGBA",
                                     "borderColor");

        fieldDecl[FIELD_BORDER_WIDTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFInt32",
                                     "borderWidth");

        fieldDecl[FIELD_BOUNDARY_MODE_S] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "boundaryModeS");

        fieldDecl[FIELD_BOUNDARY_MODE_T] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "boundaryModeT");

        fieldDecl[FIELD_BOUNDARY_MODE_R] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                        "SFString",
                        "boundaryModeR");

        fieldDecl[FIELD_MAGNIFICATION_FILTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "magnificationFilter");

        fieldDecl[FIELD_MINIFICATION_FILTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "minificationFilter");

        fieldDecl[FIELD_GENERATE_MIPMAPS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "generateMipMaps");

        fieldDecl[FIELD_ANISOTROPIC_MODE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "anisotropicMode");

        fieldDecl[FIELD_ANISOTROPIC_DEGREE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "anisotropicDegree");

        fieldDecl[FIELD_TEXTURE_PRIORITY] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                        "SFFloat",
                        "texturePriority");

        fieldDecl[FIELD_TEXTURE_COMPRESSION] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                        "SFString",
                        "textureCompression");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_BORDER_COLOR;
        fieldMap.put("borderColor", idx);
        fieldMap.put("set_borderColor", idx);
        fieldMap.put("borderColor_changed", idx);

        idx = FIELD_BORDER_WIDTH;
        fieldMap.put("borderWidth", idx);
        fieldMap.put("set_borderWidth", idx);
        fieldMap.put("borderWidth_changed", idx);

        idx = FIELD_BOUNDARY_MODE_S;
        fieldMap.put("boundaryModeS", idx);
        fieldMap.put("set_boundaryModeS", idx);
        fieldMap.put("boundaryModeS_changed", idx);

        idx = FIELD_BOUNDARY_MODE_T;
        fieldMap.put("boundaryModeT", idx);
        fieldMap.put("set_boundaryModeT", idx);
        fieldMap.put("boundaryModeT_changed", idx);

        idx = FIELD_BOUNDARY_MODE_R;
        fieldMap.put("boundaryModeR", idx);
        fieldMap.put("set_boundaryModeR", idx);
        fieldMap.put("boundaryModeR_changed", idx);

        idx = FIELD_MAGNIFICATION_FILTER;
        fieldMap.put("magnificationFilter", idx);
        fieldMap.put("set_magnificationFilter", idx);
        fieldMap.put("magnificationFilter_changed", idx);

        idx = FIELD_MINIFICATION_FILTER;
        fieldMap.put("minificationFilter", idx);
        fieldMap.put("set_minificationFilter", idx);
        fieldMap.put("minificationFilter_changed", idx);

        idx = FIELD_GENERATE_MIPMAPS;
        fieldMap.put("generateMipMaps", idx);

        idx = FIELD_ANISOTROPIC_MODE;
        fieldMap.put("anisotropicMode", idx);
        fieldMap.put("set_anisotropicMode", idx);
        fieldMap.put("anisotropicMode_changed", idx);

        idx = FIELD_ANISOTROPIC_DEGREE;
        fieldMap.put("anisotropicDegree", idx);
        fieldMap.put("set_anisotropicDegree", idx);
        fieldMap.put("anisotropicDegree_changed", idx);

        idx = FIELD_TEXTURE_COMPRESSION;
        fieldMap.put("textureCompression", idx);
        fieldMap.put("set_textureCompression", idx);
        fieldMap.put("textureCompression_changed", idx);

        idx = FIELD_TEXTURE_PRIORITY;
        fieldMap.put("texturePriority", idx);
        fieldMap.put("set_texturePriority", idx);
        fieldMap.put("texturePriority_changed", idx);

        bmMap = new HashMap<>();
        bmMap.put("CLAMP", TextureConstants.BM_CLAMP);
        bmMap.put("REPEAT", TextureConstants.BM_WRAP);
        bmMap.put("CLAMP_EDGE", TextureConstants.BM_CLAMP_EDGE);
        bmMap.put("CLAMP_BOUNDARY", TextureConstants.BM_CLAMP_BOUNDARY);
        bmMap.put("MIRRORED_REPEAT", TextureConstants.BM_MIRRORED_REPEAT);

        maxfMap = new HashMap<>();
        maxfMap.put("FASTEST", TextureConstants.MAGFILTER_FASTEST);
        maxfMap.put("NICEST", TextureConstants.MAGFILTER_NICEST);
        maxfMap.put("DEFAULT", TextureConstants.MAGFILTER_NICEST);
        maxfMap.put("NEAREST_PIXEL", TextureConstants.MAGFILTER_BASE_LEVEL_POINT);
        maxfMap.put("AVG_PIXEL", TextureConstants.MAGFILTER_BASE_LEVEL_LINEAR);

        // TODO: Need to validate these mappings not sure they are correct
        minfMap = new HashMap<>();
        minfMap.put("FASTEST", TextureConstants.MINFILTER_FASTEST);
        minfMap.put("NICEST", TextureConstants.MINFILTER_NICEST);
        minfMap.put("DEFAULT", TextureConstants.MINFILTER_NICEST);
        minfMap.put("NEAREST_PIXEL", TextureConstants.MINFILTER_BASE_LEVEL_POINT);
        minfMap.put("AVG_PIXEL", TextureConstants.MINFILTER_BASE_LEVEL_LINEAR);
        minfMap.put("AVG_PIXEL_AVG_MIPMAP", TextureConstants.MINFILTER_BASE_LEVEL_LINEAR);
        minfMap.put("AVG_PIXEL_NEAREST_MIPMAP", TextureConstants.MINFILTER_BASE_LEVEL_LINEAR);
        minfMap.put("NEAREST_PIXEL_AVG_MIPMAP", TextureConstants.MINFILTER_MULTI_LEVEL_POINT);
        minfMap.put("NEAREST_PIXEL_NEAREST_MIPMAP", TextureConstants.MINFILTER_MULTI_LEVEL_LINEAR);

        aniMap = new HashMap<>();
        aniMap.put("NONE", TextureConstants.ANISOTROPIC_MODE_NONE);
        aniMap.put("SINGLE", TextureConstants.ANISOTROPIC_MODE_SINGLE);
    }

    /**
     * Construct a default node with all of the values set to the given types.
     */
    protected BaseTextureProperties() {
        super("TextureProperties");

        hasChanged = new boolean[NUM_FIELDS];

        vfBorderColor = new float[] {0,0,0,0};
        vfBorderWidth = 0;
        vfBoundaryModeS = "REPEAT";
        vfBoundaryModeT = "REPEAT";
        vfBoundaryModeT = "REPEAT";
        vfMagnificationFilter = "FASTEST";
        vfMinificationFilter = "FASTEST";
        vfGenerateMipMaps = false;
        vfAnisotropicMode = "NONE";
        vfAnisotropicDegree = 1.0f;
        vfTextureCompression = "FASTEST";
        vfTexturePriority = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseTextureProperties(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("borderColor");
            VRMLFieldData field = node.getFieldValue(index);

            vfBorderColor[0] = field.floatArrayValues[0];
            vfBorderColor[1] = field.floatArrayValues[1];
            vfBorderColor[2] = field.floatArrayValues[2];
            vfBorderColor[3] = field.floatArrayValues[3];

            index = node.getFieldIndex("borderWidth");
            field = node.getFieldValue(index);
            vfBorderWidth = field.intValue;

            index = node.getFieldIndex("boundaryModeS");
            field = node.getFieldValue(index);
            vfBoundaryModeS = field.stringValue;

            index = node.getFieldIndex("boundaryModeT");
            field = node.getFieldValue(index);
            vfBoundaryModeT = field.stringValue;

            index = node.getFieldIndex("boundaryModeR");
            field = node.getFieldValue(index);
            vfBoundaryModeR = field.stringValue;

            index = node.getFieldIndex("minificationFilter");
            field = node.getFieldValue(index);
            vfMinificationFilter = field.stringValue;

            index = node.getFieldIndex("maxificationFilter");
            field = node.getFieldValue(index);
            vfMinificationFilter = field.stringValue;

            index = node.getFieldIndex("generateMipMaps");
            field = node.getFieldValue(index);
            vfGenerateMipMaps = field.booleanValue;

            index = node.getFieldIndex("anisotropicMode");
            field = node.getFieldValue(index);
            vfAnisotropicMode = field.stringValue;

            index = node.getFieldIndex("anisotropicFilterDegree");
            field = node.getFieldValue(index);
            vfAnisotropicDegree = field.floatValue;

            index = node.getFieldIndex("texturePriority");
            field = node.getFieldValue(index);
            vfTexturePriority = field.floatValue;

            index = node.getFieldIndex("textureCompression");
            field = node.getFieldValue(index);
            vfTextureCompression = field.stringValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        super.setupFinished();

        if(!inSetup)
            return;

        inSetup = false;
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_TEXTUREPROPS_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.AppearanceChildNodeType;
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
            case FIELD_BORDER_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfBorderColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BORDER_WIDTH:
                fieldData.clear();
                fieldData.intValue = vfBorderWidth;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_MAGNIFICATION_FILTER:
                fieldData.clear();
                fieldData.stringValue = vfMagnificationFilter;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_MINIFICATION_FILTER:
                fieldData.clear();
                fieldData.stringValue = vfMinificationFilter;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_GENERATE_MIPMAPS:
                fieldData.clear();
                fieldData.booleanValue = vfGenerateMipMaps;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ANISOTROPIC_MODE:
                fieldData.clear();
                fieldData.stringValue = vfAnisotropicMode;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_ANISOTROPIC_DEGREE:
                fieldData.clear();
                fieldData.floatValue = vfAnisotropicDegree;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_TEXTURE_PRIORITY:
                fieldData.clear();
                fieldData.floatValue = vfTexturePriority;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_TEXTURE_COMPRESSION:
                fieldData.clear();
                fieldData.stringValue = vfTextureCompression;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            default:
                return(super.getFieldValue(index));
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

        // Simple impl for now.  ignores time and looping. Note that for a
        // couple of the fields, if the array size is greater than the number
        // of components in it, we create a temporary array to send. This is
        // a negative hit, but it is very rare that someone will route out of
        // these fields, so we don't consider it to be a major impact compared
        // to the performance of having to reallocate the arrays every time
        // someone sets the values, which will happen much, much more often.

        try {
            switch(srcIndex) {
                case FIELD_BORDER_COLOR:
                    destNode.setValue(destIndex, vfBorderColor, 4);
                    break;

                case FIELD_BORDER_WIDTH:
                    destNode.setValue(destIndex, vfBorderWidth);
                    break;

                case FIELD_BOUNDARY_MODE_S:
                    destNode.setValue(destIndex, vfBoundaryModeS);
                    break;

                case FIELD_BOUNDARY_MODE_T:
                    destNode.setValue(destIndex, vfBoundaryModeT);
                    break;

                case FIELD_BOUNDARY_MODE_R:
                    destNode.setValue(destIndex, vfBoundaryModeR);
                    break;

                case FIELD_MAGNIFICATION_FILTER:
                    destNode.setValue(destIndex, vfMagnificationFilter);
                    break;

                case FIELD_MINIFICATION_FILTER:
                    destNode.setValue(destIndex, vfMinificationFilter);
                    break;

                case FIELD_GENERATE_MIPMAPS:
                    destNode.setValue(destIndex, vfGenerateMipMaps);
                    break;

                case FIELD_ANISOTROPIC_MODE:
                    destNode.setValue(destIndex, vfAnisotropicMode);
                    break;

                case FIELD_ANISOTROPIC_DEGREE:
                    destNode.setValue(destIndex, vfAnisotropicDegree);
                    break;

                case FIELD_TEXTURE_COMPRESSION:
                    destNode.setValue(destIndex, vfTextureCompression);
                    break;

                case FIELD_TEXTURE_PRIORITY:
                    destNode.setValue(destIndex, vfTexturePriority);
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
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types headlight and bind.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GENERATE_MIPMAPS:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Cannot set generateMipMaps field.");

                vfGenerateMipMaps = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types speed and visibilityLimit.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ANISOTROPIC_DEGREE:
                vfAnisotropicDegree = value;
                break;

            case FIELD_TEXTURE_PRIORITY:
                vfTexturePriority = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BORDER_WIDTH:
                vfBorderWidth = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat field type avatarSize.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BORDER_COLOR:
                vfBorderColor[0] = value[0];
                vfBorderColor[1] = value[1];
                vfBorderColor[2] = value[2];
                vfBorderColor[3] = value[3];
                break;

            default :
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string.
     * This would be used to set the SFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BOUNDARY_MODE_S:
                setBoundaryModeS(value);
                break;

            case FIELD_BOUNDARY_MODE_T:
                setBoundaryModeT(value);
                break;

            case FIELD_BOUNDARY_MODE_R:
                setBoundaryModeR(value);
                break;

            case FIELD_MAGNIFICATION_FILTER:
                vfMagnificationFilter = value;
                break;

            case FIELD_MINIFICATION_FILTER:
                vfMinificationFilter = value;
                break;

            case FIELD_ANISOTROPIC_MODE:
                vfAnisotropicMode = value;
                break;

            case FIELD_TEXTURE_COMPRESSION:
                vfTextureCompression = value;
                break;

            default :
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureProperties2DNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the boundary color.  This is a 4 component color.
     *
     * @param color A preallocated 4 component color array;
     */
    @Override
    public void getBorderColor(float[] color) {
        color[0] = vfBorderColor[0];
        color[1] = vfBorderColor[1];
        color[2] = vfBorderColor[2];
        color[3] = vfBorderColor[3];
    }

    /**
     * Get the boundary width.
     *
     * @return The boundary width
     */
    @Override
    public int getBorderWidth() {
        return vfBorderWidth;
    }

    /**
     * Get the boundary mode for S.
     *
     * @return The boundary mode.  Defined in TextureConstants.
     */
    @Override
    public int getBoundaryModeS() {
        return bmMap.get(vfBoundaryModeS);
    }

    /**
     * Get the boundary mode for T.
     *
     * @return The boundary mode.  Defined in TextureConstants.
     */
    @Override
    public int getBoundaryModeT() {
        return bmMap.get(vfBoundaryModeT);
    }

    /**
     * Get the magnification filter.
     *
     * @return The mag filter.  Defined in TextureConstants.
     */
    @Override
    public int getMagnificationFilter() {
        return maxfMap.get(vfMagnificationFilter);
    }

    /**
     * Get the minification filter.
     *
     * @return The min filter.  Defined in TextureConstants.
     */
    @Override
    public int getMinificationFilter() {
        return minfMap.get(vfMinificationFilter);
    }

    /**
     * Get the generateMipsMaps field.
     *
     * @return Should mips be generated for this object.
     */
    @Override
    public boolean getGenerateMipMaps() {
        return vfGenerateMipMaps;
    }

    /**
     * Get the Anisotropic Mode.
     *
     * @return The anisotropic mode.  Defined in TextureConstants.
     */
    @Override
    public int getAnisotropicMode() {
        return aniMap.get(vfAnisotropicMode);
    }

    /**
     * Get the AnistropicFilter Degree.
     *
     * @return The anisotropic degree.
     */
    @Override
    public float getAnisotropicDegree() {
        return vfAnisotropicDegree;
    }

    /**
     * Get the texture compression setting.
     *
     * @return What texture compression mode to use
     */
    @Override
    public String getTextureCompression() {
        return vfTextureCompression;
    }

    /**
     * Get the texture priority.
     *
     * @return The texture priority for memory management.
     */
    @Override
    public float getTexturePriority() {
        return vfTexturePriority;
    }

    /**
     * Set the current boundary mode.
     *
     * @param val The new set of values to use for the mode
     */
    private void setBoundaryModeS(String val) {
        vfBoundaryModeS = val;
        if (!inSetup) {
            // TODO: likely need a fire*
            hasChanged[FIELD_BOUNDARY_MODE_S] = true;
            fireFieldChanged(FIELD_BOUNDARY_MODE_S);
        }
    }

    /**
     * Set the current boundary mode.
     *
     * @param val The new set of values to use for the mode
     */
    private void setBoundaryModeT(String val) {
        vfBoundaryModeT = val;
        if (!inSetup) {
            // TODO: likely need a fire*
            hasChanged[FIELD_BOUNDARY_MODE_T] = true;
            fireFieldChanged(FIELD_BOUNDARY_MODE_T);
        }
    }

    /**
     * Set the current boundary mode.
     *
     * @param val The new set of values to use for the mode
     */
    private void setBoundaryModeR(String val) {
        vfBoundaryModeR = val;
        if (!inSetup) {
            // TODO: likely need a fire*
            hasChanged[FIELD_BOUNDARY_MODE_R] = true;
            fireFieldChanged(FIELD_BOUNDARY_MODE_R);
        }
    }

}
