/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.FieldValidator;

import org.web3d.vrml.renderer.common.nodes.BaseTextureNode;

/**
 * Base implementation of a MultiTexture node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.14 $
 */
public class BaseMultiTexture extends BaseTextureNode
    implements VRMLMultiTextureNodeType, VRMLTextureListener {

    /** Mode Index */
    protected static final int FIELD_MODE = LAST_NODE_INDEX + 1;

    /** Texture Index */
    protected static final int FIELD_TEXTURE = LAST_NODE_INDEX + 2;

    /** Color Index */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 3;

    /** Alpha Index */
    protected static final int FIELD_ALPHA = LAST_NODE_INDEX + 4;

    /** function Index */
    protected static final int FIELD_FUNCTION = LAST_NODE_INDEX + 5;

    /** source Index */
    protected static final int FIELD_SOURCE = LAST_NODE_INDEX + 6;

    /** Index of the last field of this node */
    protected static final int LAST_MULTITEXTURE_INDEX = FIELD_SOURCE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_MULTITEXTURE_INDEX + 1;

    /** Message for when the proto is not a Texture */
    private static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Texture */
    private static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // Field values

    /** exposedField MFString mode [] */
    protected String[] vfMode;

    /** exposedField MFString function [] */
    protected String[] vfFunction;

    /** exposedField MFString source [] */
    protected String[] vfSource;

    /** exposedField MFNode texture [] */
    protected List<VRMLTextureNodeType> vfTexture;

    /** exposedField color 1 1 1 */
    protected float[] vfColor;

    /** exposedField alpha 1 */
    protected float vfAlpha;

    /** A map of mode strings to texture constants */
    protected static final Map<String, Integer> modeMap;

    /** A map of function strings to texture constants */
    protected static final Map<String, Integer> functionMap;

    /** A map of source strings to texture constants */
    protected static final Map<String, Integer> sourceMap;

    /** Whether a texture has loaded. */
    protected boolean[] loaded;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_TEXTURE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_MODE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "mode");
        fieldDecl[FIELD_FUNCTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "function");
        fieldDecl[FIELD_SOURCE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "source");
        fieldDecl[FIELD_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "texture");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFColor",
                                     "color");
        fieldDecl[FIELD_ALPHA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "alpha");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_MODE;
        fieldMap.put("mode", idx);
        fieldMap.put("set_mode", idx);
        fieldMap.put("mode_changed", idx);

        idx = FIELD_SOURCE;
        fieldMap.put("source", idx);
        fieldMap.put("set_source", idx);
        fieldMap.put("source_changed", idx);

        idx = FIELD_FUNCTION;
        fieldMap.put("function", idx);
        fieldMap.put("set_function", idx);
        fieldMap.put("function_changed", idx);

        idx = FIELD_TEXTURE;
        fieldMap.put("texture", idx);
        fieldMap.put("set_texture", idx);
        fieldMap.put("texture_changed", idx);

        idx = FIELD_COLOR;
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = FIELD_ALPHA;
        fieldMap.put("alpha", idx);
        fieldMap.put("set_alpha", idx);
        fieldMap.put("alpha_loaded", idx);

        modeMap = new HashMap<>(19);
        modeMap.put("MODULATE", TextureConstants.MODE_MODULATE);
        modeMap.put("REPLACE", TextureConstants.MODE_REPLACE);
        modeMap.put("MODULATE2X", TextureConstants.MODE_MODULATE_2X);
        modeMap.put("MODULATE4X", TextureConstants.MODE_MODULATE_4X);
        modeMap.put("ADD", TextureConstants.MODE_ADD);
        modeMap.put("ADDSIGNED", TextureConstants.MODE_ADD_SIGNED);
        modeMap.put("ADDSIGNED2X", TextureConstants.MODE_ADD_SIGNED_2X);
        modeMap.put("SUBTRACT", TextureConstants.MODE_SUBTRACT);
        modeMap.put("ADDSMOOTH", TextureConstants.MODE_ADD_SMOOTH);
        modeMap.put("BLENDDIFFUSEALPHA", TextureConstants.MODE_BLEND_DIFFUSE_ALPHA);
        modeMap.put("BLENDTEXTUREALPHA", TextureConstants.MODE_BLEND_TEXTURE_ALPHA);
        modeMap.put("BLENDFACTORALPHA", TextureConstants.MODE_BLEND_FACTOR_ALPHA);
        modeMap.put("BLENDCURRENTALPHA", TextureConstants.MODE_BLEND_CURRENT_ALPHA);
        modeMap.put("MODULATEALPHA_ADDCOLOR", TextureConstants.MODE_MODULATE_ALPHA_ADD_COLOR);
        modeMap.put("MODULATEINVCOLOR_ADDALPHA", TextureConstants.MODE_MODULATE_INVCOLOR_ADD_ALPHA);
        modeMap.put("OFF", TextureConstants.MODE_OFF);
        modeMap.put("SELECTARG1", TextureConstants.MODE_SELECT_ARG1);
        modeMap.put("SELECTARG2", TextureConstants.MODE_SELECT_ARG2);
        modeMap.put("DOTPRODUCT3", TextureConstants.MODE_DOTPRODUCT3);

        sourceMap = new HashMap<>(4);
        sourceMap.put("", TextureConstants.SRC_COMBINE_PREVIOUS);
        sourceMap.put("DIFFUSE", TextureConstants.SRC_DIFFUSE);
        sourceMap.put("SPECULAR", TextureConstants.SRC_SPECULAR);
        sourceMap.put("FACTOR", TextureConstants.SRC_FACTOR);

        functionMap = new HashMap<>(3);
        functionMap.put("", TextureConstants.FUNC_NONE);
        functionMap.put("COMPLEMENT", TextureConstants.FUNC_COMPLEMENT);
        functionMap.put("ALPHAREPLICATE", TextureConstants.FUNC_ALPHA_REPLICATE);
    }

    /**
     * Empty constructor.
     */
    protected BaseMultiTexture() {
        super("MultiTexture");

        vfMode = new String[0];
        vfFunction = new String[0];
        vfSource = new String[0];

        vfTexture = new ArrayList<>();
        vfColor = new float[] {1.0f,1.0f,1.0f};
        vfAlpha = 1.0f;

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseMultiTexture(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("mode");
            VRMLFieldData field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfMode = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfMode, 0,
                  field.numElements);
            }

            index = node.getFieldIndex("function");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfFunction = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfFunction, 0,
                  field.numElements);
            }

            index = node.getFieldIndex("source");
            field = node.getFieldValue(index);
            if (field.numElements != 0) {
                vfSource = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfSource, 0,
                  field.numElements);
            }

            index = node.getFieldIndex("color");
            field = node.getFieldValue(index);
            vfColor[0] = field.floatArrayValues[0];
            vfColor[1] = field.floatArrayValues[1];
            vfColor[2] = field.floatArrayValues[2];

            index = node.getFieldIndex("alpha");
            field = node.getFieldValue(index);
            vfAlpha = field.floatValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureNodeType
    //----------------------------------------------------------

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    @Override
    public int getTextureType() {
        return TextureConstants.TYPE_MULTI;
    }

    /**
     * Get a string for caching this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    @Override
    public String getCacheString(int stage) {
        VRMLTextureNodeType tex = vfTexture.get(stage);

        if (tex == null)
            return null;

        return tex.getCacheString(0);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLComposedTextureNodeType
    //----------------------------------------------------------
    @Override
    public int getNumberTextures() {
        return vfTexture.size();
    }

    /**
     * Get the texture info which make up this multitexture.
     *
     * @param start Where in the array to start filling in textures.
     * @param texs The preallocated array to return texs in.  Error if too small.
//     * @param modes The TextureConstant modes.
     */
    @Override
    public void getTextures(int start, VRMLTextureNodeType[] texs) {
        int len = vfTexture.size();
        for(int i=0; i < len; i++) {
            texs[i + start] = vfTexture.get(i);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiTextureNodeType
    //----------------------------------------------------------
    /**
     * Get the texture params for each stage of this texture.
     *
     * @param start Where in the array to start filling in textures.
//     * @param texs The preallocated array to return texs in.  Error if too small.
     * @param modes The TextureConstant modes.
     */
    @Override
    public void getTextureParams(int start, int[] modes, int[] function, int[] source) {
        int len = vfTexture.size();
        for(int i=0; i < len; i++) {
            if (vfMode.length > i) {
                modes[i + start] = getModeConst(vfMode[i]);

            } else
                modes[i + start] = TextureConstants.MODE_OFF;
            if(vfSource.length > i)
                source[i + start] = getSourceConst(vfSource[i]);
            else
                source[i + start] = TextureConstants.SRC_COMBINE_PREVIOUS;
            if (vfFunction.length > i)
                function[i + start] = getFunctionConst(vfFunction[i]);
            else
                function[i + start] = TextureConstants.FUNC_NONE;
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The image idx which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    @Override
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    NIOBufferImage image,
                                    String url) {
        int len = vfTexture.size();
        VRMLTextureNodeType tex;

        for(int i=0; i < len; i++) {
            tex = vfTexture.get(i);
            if (tex == node) {
                loaded[i] = true;

                fireTextureImageChanged(i, node, image, url);
            }
        }
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    @Override
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    NIOBufferImage[] image,
                                    String[] url) {
        // Not implemented yet.
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * efficient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  3 Component color.
     */
    @Override
    public void textureParamsChanged(int idx,
                                     int mode,
                                     int source,
                                     int function,
                                     float alpha,
                                     float[] color) {

        // Will never be called by children of multitexture
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * efficient route is to set the parameters before the image.
     *
     * @param len The number of valid entries in the arrays.
//     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3 component colors.
     */
    @Override
    public void textureParamsChanged(int len,
                                     int mode[],
                                     int[] source,
                                     int[] function,
                                     float alpha,
                                     float[] color) {

        // Will never be called by children of multitexture
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------
    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if (!inSetup)
            return;

        super.setupFinished();

        int len = vfTexture.size();
        VRMLTextureNodeType tex;
        loaded = new boolean[len];

        for(int i=0; i < len; i++) {
            tex = vfTexture.get(i);
            tex.setupFinished();

            switch(tex.getTextureType()) {
                case TextureConstants.TYPE_SINGLE_2D:
                    if (((VRMLTexture2DNodeType)tex).getImage() != null)
                        loaded[i] = true;
                    break;
                default:
                    System.out.println("Unhandled texture type in BaseMultiTexture");
            }
        }

        inSetup = false;
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
        if(index < 0 || index > LAST_MULTITEXTURE_INDEX)
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
        return TypeConstants.TextureNodeType;
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
            case FIELD_MODE:
                fieldData.clear();
                fieldData.stringArrayValues = vfMode;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfMode.length;
                break;

            case FIELD_SOURCE:
                fieldData.clear();
                fieldData.stringArrayValues = vfSource;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfSource.length;
                break;

            case FIELD_FUNCTION:
                fieldData.clear();
                fieldData.stringArrayValues = vfFunction;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfFunction.length;
                break;

            case FIELD_TEXTURE:
                VRMLNodeType kids[] = new VRMLNodeType[vfTexture.size()];
                vfTexture.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValues = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
                break;

            case FIELD_COLOR:
                fieldData.clear();
                fieldData.floatArrayValues = vfColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ALPHA:
                fieldData.clear();
                fieldData.floatValue = vfAlpha;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
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
                case FIELD_MODE :
                    destNode.setValue(destIndex, vfMode, vfMode.length);
                    break;

                case FIELD_FUNCTION:
                    destNode.setValue(destIndex, vfFunction, vfFunction.length);
                    break;

                case FIELD_SOURCE:
                    destNode.setValue(destIndex, vfSource, vfSource.length);
                    break;

                case FIELD_COLOR:
                    destNode.setValue(destIndex, vfColor, 3);
                    break;

                case FIELD_TEXTURE :
                    VRMLNodeType kids[] = new VRMLNodeType[vfTexture.size()];
                    vfTexture.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;

                case FIELD_ALPHA :
                    destNode.setValue(destIndex, vfAlpha);
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
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldFormatException,
            InvalidFieldValueException {

        switch(index) {
            case FIELD_ALPHA:
                vfAlpha = value;

                if(!inSetup) {
                    hasChanged[FIELD_ALPHA] = true;
                    fireFieldChanged(FIELD_ALPHA);
                }
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
     * @param value The raw value string to be parsed
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldFormatException,
            InvalidFieldValueException {

        switch(index) {
            case FIELD_COLOR:
                FieldValidator.checkColorVector("BaseMultitexture", value);

                vfColor[0] = value[0];
                vfColor[1] = value[1];
                vfColor[2] = value[2];

                if(!inSetup) {
                    hasChanged[FIELD_COLOR] = true;
                    fireFieldChanged(FIELD_COLOR);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MODE :
                setMode(new String[] {value});
                break;

            case FIELD_FUNCTION :
                setFunction(new String[] {value});
                break;

            case FIELD_SOURCE :
                setSource(new String[] {value});
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MODE :
                setMode(value);
                break;

            case FIELD_FUNCTION :
                setFunction(value);
                break;

            case FIELD_SOURCE :
                setSource(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TEXTURE:
                if(!inSetup)
                    vfTexture.clear();

                loaded = new boolean[1];
                if(child != null)
                    addTextureNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURE] = true;
                    fireFieldChanged(FIELD_TEXTURE);
                }
                break;

            default:
                super.setValue(index, child);
        }

    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param children The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TEXTURE:
                if(!inSetup)
                    vfTexture.clear();

                loaded = new boolean[numValid];
                for(int i = 0; i < numValid; i++)
                    addTextureNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURE] = true;
                    fireFieldChanged(FIELD_TEXTURE);
                }
                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    /**
     * Add a single texture node to the list of textures.  Override this
     * to add render-specific behavior, but remember to call this method.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addTextureNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLTextureNodeType))
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);

// NOTE:
// JC: This seems dodgy - particularly if we try to remove them. Should it
// be adding the impl or the outer node here? In other classes we still always
// add the outer to this array.

            vfTexture.add((VRMLTextureNodeType) impl);
            ((VRMLTextureNodeType)node).addTextureListener(this);
        } else {
            if (!(node instanceof VRMLTextureNodeType))
                throw new InvalidFieldValueException(TEXTURE_NODE_MSG);

            vfTexture.add((VRMLTextureNodeType) node);
            ((VRMLTextureNodeType)node).addTextureListener(this);
        }

        // TODO: Need to update loaded field
    }

    /**
     * Set the current mode.
     *
     * @param val The new set of values to use for the mode
     */
    private void setMode(String[] val) {
        vfMode = val;
        if (!inSetup) {
            sendTexParams();
            hasChanged[FIELD_MODE] = true;
            fireFieldChanged(FIELD_MODE);
        }
    }

    /**
     * Set the current function.
     *
     * @param val The new set of values to use for the mode
     */
    private void setFunction(String[] val) {
        vfFunction = val;
        if (!inSetup) {
            sendTexParams();
            hasChanged[FIELD_FUNCTION] = true;
            fireFieldChanged(FIELD_FUNCTION);
        }
    }

    /**
     * Set the current function.
     *
     * @param val The new set of values to use for the mode
     */
    private void setSource(String[] val) {
        vfFunction = val;
        if (!inSetup) {
            sendTexParams();
            hasChanged[FIELD_SOURCE] = true;
            fireFieldChanged(FIELD_SOURCE);
        }
    }

    /**
     * Convert a mode to a TextureConstants const.  Returns -1 for unknown modes.
     *
     * @param mode The mode string
     * @return The const
     */
    protected int getModeConst(String mode) {
        Integer ret_val = modeMap.get(mode);

        if (ret_val == null)
            return -1;

        return ret_val;
    }

    /**
     * Convert a function to a TextureConstants const.  Returns -1 for unknown functions.
     *
     * @param mode The mode string
     * @return The const
     */
    protected int getFunctionConst(String mode) {
        Integer ret_val = functionMap.get(mode);

        if (ret_val == null)
            return -1;

        return ret_val;
    }

    /**
     * Convert a source to a TextureConstants const.  Returns -1 for unknown sources.
     *
     * @param mode The mode string
     * @return The const
     */
    protected int getSourceConst(String mode) {
        Integer ret_val = sourceMap.get(mode);

        if (ret_val == null)
            return -1;

        return ret_val;
    }

    /**
     * Send all of the textureParams out as one message.
     */
    private void sendTexParams() {
        int len = Math.max(vfMode.length,vfFunction.length);
        len = Math.max(len, vfSource.length);
        int[] modes = new int[len];
        int[] sources = new int[len];
        int[] functions = new int[len];

        int cnt=0;
        for(int i=0; i < len; i++) {
            if (vfMode.length > i)
                modes[i] = getModeConst(vfMode[i]);
            if (vfFunction.length > i)
                functions[i] = getFunctionConst(vfFunction[i]);
            if (vfSource.length > i)
                sources[i] = getSourceConst(vfSource[i]);
        }
        fireTextureParamsChanged(vfMode.length, this, modes, sources,
                functions, vfAlpha, vfColor);
    }
}
