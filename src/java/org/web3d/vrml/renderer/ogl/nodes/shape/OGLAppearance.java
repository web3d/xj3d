/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.shape;

// External imports
import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.vecmath.Matrix4f;

import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.shape.BaseAppearance;
import org.web3d.vrml.renderer.common.nodes.shape.TextureStage;

import org.web3d.vrml.renderer.ogl.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.TextureCache;

/**
 * OpenGL implementation of an Appearance node.
 * <p>
 *
 * MultiTexture Notes: function and source not mapped
 *
 * 3D Texture Notes: Getting a rescale problem, not working.
 *
 * Cubic Environment Notes: Not implemented.
 *
 * @author Alan Hudson
 * @version $Revision: 1.70 $
 */
public class OGLAppearance extends BaseAppearance
        implements OGLAppearanceNodeType,
        OGLTextureTransformListener,
        NodeUpdateListener,
        FrameStateListener {

    /**
     * Error message when something goes wrong in createTexture()
     */
    private static final String TEXTURE_CREATE_FAIL_MSG
            = "Failed for an unknown reason during the creation of the base "
            + "texture image in OGLAppearance.createTexture(). URL is: ";

    /**
     * The texture field contains a Multitexture with a nested multitexture
     * instance as a child. This is baaaaad and not legal by the spec. Hopefully
     * the parsing or scene building has picked this up, but we'll do another
     * check anyway. This is the error message for that redundant check as we're
     * building the scene graph.
     */
    private static final String NESTED_MULTITEXTURE_ERR
            = "Discovered a nested MultiTexture node while processing a "
            + "MultiTexture node. This is not legal. See 19775-1 18.4.3 "
            + "paragraph 2 for more information";

    /**
     * The default mode to use for CLAMP. X3D spec is silent right now
     */
    private static final int DEFAULT_CLAMP_MODE = TextureConstants.BM_CLAMP_EDGE;

    /**
     * A map between TexCoordGen modes and AV3D modes
     */
    private static final Map<String, Integer> texGenModeMap;

    /**
     * The OpenGL Implementation node for the appearance
     */
    private Appearance oglImplNode;

    /**
     * The OpenGL Implementation node for the solid flag handling
     */
    private PolygonAttributes implPA;

    /**
     * The current Texture attribute information
     */
    private TextureAttributes[] texAttrs;

    /**
     * The TextureUnitStates used for multitexturing
     */
    private TextureUnit[] texUnits;

    /**
     * A map between texture unit and texture gen mode
     */
    private Map<Integer, String> texGenMap;

    /**
     * The OGL texture objects
     */
    private Texture[] texObjs;

    /**
     * The vrml texture objects
     */
    private VRMLTextureNodeType[] vrmlTexs;

    /**
     * The mode for each stage
     */
    private int[] modes;

    /**
     * The function for each stage
     */
    private int[] functions;

    /**
     * The source for each stage
     */
    private int[] sources;

    /**
     * Flag to indicate a textureUnit has changed off thread
     */
    private boolean threadedUnitChanged;

    /**
     * Flag indicating that the material has changed
     */
    private boolean materialChanged;

    /**
     * Flag indicating that the fill properties changed
     */
    private boolean fillPropsChanged;

    /**
     * Flag indicating that the line properties changed
     */
    private boolean linePropsChanged;

    /**
     * Flag indicating that the point properties changed
     */
    private boolean pointPropsChanged;

    /**
     * Did all the texture units get changed
     */
    private boolean textureUnitsChanged;

    /**
     * Flag indicating that the texture transform node has changed
     */
    private boolean textureTransformChanged;

    /**
     * Maps the AV3D TextureUnit to the TextureStage that has changed.
     */
    private Map<TextureUnit, TextureStage> changedTransforms;

    /**
     * Maps the AV3D TextureUnit to the TextureStage that has changed.
     */
    private Map<TextureUnit, TextureStage> changedTexCoordGeneration;

    /**
     * Flag indicating that the texture changed
     */
    private boolean textureChanged;

    /**
     * Solid state passed in from the geometry flags
     */
    private boolean savedSolidState;

    /**
     * Solid state passed in from the geometry flags
     */
    private boolean savedCCWState;

    /**
     * A double buffered list of tex units changed
     */
    private ConcurrentLinkedQueue<SceneGraphObject> texUnitsChanged;

    /**
     * The current lighting state
     */
    private boolean lightingState;

    /**
     * Whether to override diffuse color with local colors
     */
    private boolean localColor;

    /**
     * Whether the local color also includes alpha values
     */
    private boolean localColorAlpha;

    /**
     * Should we force lighting off
     */
    boolean lightingOverride;

    /**
     * The local texture cache for AV3D textures
     */
    private TextureCache textureCache;

    /**
     * Should we query for point sprites
     */
    private boolean queryPointSprites;

    /**
     * PointSprites supported
     */
    private static boolean pointSpritesSupported;

    static {
        texGenModeMap = new HashMap<>(3);
        texGenModeMap.put("SPHERE", TexCoordGeneration.MAP_SPHERICAL);
        texGenModeMap.put("CAMERASPACENORMAL", TexCoordGeneration.MAP_NORMALS);
        texGenModeMap.put("CAMERASPACEREFLECTIONVECTOR", TexCoordGeneration.MAP_REFLECTIONS);

        // TODO: Not positive this is the correct mapping from spec language
        texGenModeMap.put("CAMERASPACEPOSITION", TexCoordGeneration.MAP_EYE_LINEAR);
    }

    /**
     * Empty constructor
     */
    public OGLAppearance() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node. If the node is not a Appearance node, an exception will be thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLAppearance(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin.
     */
    @Override
    public void allEventsComplete() {

        if (materialChanged || linePropsChanged || pointPropsChanged
                || fillPropsChanged) {
            oglImplNode.dataChanged(this);
        } else if (threadedUnitChanged) {

            while (!texUnitsChanged.isEmpty()) {

                SceneGraphObject unit
                        = texUnitsChanged.poll();

                if (unit.isLive()) {
                    unit.dataChanged(this);
                } else {
                    updateNodeDataChanges(unit);
                }

                threadedUnitChanged = false;
            }
        } else if (queryPointSprites) {
            queryPointSprites = false;

            if (vfPointProperties != null) {
                OGLVRMLNode o_n = (OGLVRMLNode) vfPointProperties;
                PointAttributes pa = (PointAttributes) o_n.getSceneGraphObject();

                pointSpritesSupported = pa.isPointSpriteAllowed();

                if (!pointSpritesSupported) {
                    System.out.println("OGL Point sprites not supported, unroll");
                    pa.setPointSpriteEnabled(false);
                    pa.setAntiAliased(true);

                    // TODO: Disable texturing.  This is not exactly right if someone provided
                    // PointProperties for a non pointset
                    vfTexture = null;

                    textureChanged = true;

                    if (oglImplNode.isLive()) {
                        oglImplNode.dataChanged(this);
                    } else {
                        updateNodeDataChanges(oglImplNode);
                    }
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLAppearanceNodeType
    //----------------------------------------------------------
    /**
     * Set the material that should be used for this appearance. Setting a value
     * of null will clear the current material.
     *
     * @param newMaterial The new material instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setMaterial(VRMLNodeType newMaterial)
            throws InvalidFieldValueException {

        super.setMaterial(newMaterial);

        if (inSetup) {
            return;
        }

        if (vfMaterial != null && !lightingOverride) {
            ((OGLMaterialNodeType) vfMaterial).setLightingEnable(lightingState);
        }

        materialChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set the texture that should be used for this appearance. Setting a value
     * of null will clear the current texture.
     *
     * @param newTexture The new texture instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setTexture(VRMLNodeType newTexture)
            throws InvalidFieldValueException {

        VRMLTextureNodeType old_node = vfTexture;

        super.setTexture(newTexture);

        if (old_node != null) {
            old_node.removeTextureListener(this);
        }

        if (vfTexture != null) {
            vfTexture.addTextureListener(this);
        }

        if (inSetup) {
            return;
        }

        textureChanged = true;

        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set the texture transform that should be used for this appearance.
     * Setting a value of null will clear the current texture transform.
     *
     * @param newTransform The new texture transform instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setTextureTransform(VRMLNodeType newTransform)
            throws InvalidFieldValueException {

        OGLTextureCoordinateTransformNodeType old_node
                = (OGLTextureCoordinateTransformNodeType) vfTextureTransform;

        super.setTextureTransform(newTransform);

        OGLTextureCoordinateTransformNodeType node
                = (OGLTextureCoordinateTransformNodeType) vfTextureTransform;

        if (node != null) {
            node.addTransformListener(this);
        }

        if (old_node != null) {
            old_node.removeTransformListener(this);
        }

        if (inSetup) {
            return;
        }

        textureTransformChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set the line properties that should be used for this appearance. Setting
     * a value of null will clear the current property.
     *
     * @param prop The new property instance instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setLineProperties(VRMLNodeType prop)
            throws InvalidFieldValueException {

        super.setLineProperties(prop);

        if (inSetup) {
            return;
        }

        linePropsChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set the point properties that should be used for this appearance. Setting
     * a value of null will clear the current property.
     *
     * @param prop The new property instance instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setPointProperties(VRMLNodeType prop)
            throws InvalidFieldValueException {

        super.setPointProperties(prop);

        if (inSetup) {
            return;
        }

        pointPropsChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set the fillProperties that should be used for this appearance. Setting a
     * value of null will clear the current property.
     *
     * @param prop The new fillProperties instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     * type.
     */
    @Override
    public void setFillProperties(VRMLNodeType prop)
            throws InvalidFieldValueException {

        super.setFillProperties(prop);

        if (inSetup) {
            return;
        }

        fillPropsChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLAppearanceNodeType
    //----------------------------------------------------------

    /**
     * Get the appearance instance used to represent this object.
     *
     * @return The appearance instance
     */
    @Override
    public Appearance getAppearance() {
        return oglImplNode;
    }

    /**
     * Set the texture coordinate generation mode for a texture set. If its not
     * set then texture coordinates will be used. A value of null will clear the
     * setting.
     *
     * @param setNum The set which this tex gen mode refers
     * @param mode The mode to use. Straight VRML field value
     */
    @Override
    public void setTexCoordGenMode(int setNum, String mode) {
        texGenMap.put(setNum, mode);

        if (inSetup) {
            return;
        }

        Integer imode = texGenModeMap.get(mode);

        if (imode != null) {
            TexCoordGeneration newtcg = new TexCoordGeneration();

            newtcg.setParameter(TexCoordGeneration.TEXTURE_S,
                    TexCoordGeneration.MODE_GENERIC, imode,
                    null);
            newtcg.setParameter(TexCoordGeneration.TEXTURE_T,
                    TexCoordGeneration.MODE_GENERIC, imode,
                    null);

            if (vfTexture != null
                    && vfTexture.getTextureType() == TextureConstants.TYPE_SINGLE_3D) {

                newtcg.setParameter(TexCoordGeneration.TEXTURE_R,
                        TexCoordGeneration.MODE_GENERIC, imode,
                        null);
            }

            stages[setNum].texCoordGeneration = newtcg;

            changedTexCoordGeneration.put(texUnits[setNum], stages[setNum]);

            if (texUnits[setNum].isLive()) {
                texUnits[setNum].dataChanged(this);
            } else {
                updateNodeDataChanges(texUnits[setNum]);
            }

        } else {
            // Clear

            stages[setNum].texCoordGeneration = null;

            changedTexCoordGeneration.put(texUnits[setNum], stages[setNum]);

            if (texUnits[setNum].isLive()) {
                texUnits[setNum].dataChanged(this);
            } else {
                updateNodeDataChanges(texUnits[setNum]);
            }
        }
    }

    /**
     * Specify whether an object is solid. The default is true. This will
     * determine if we do backface culling and flip backface normals. Can only
     * be set during setup
     *
     * @param solid Whether the object is solid
     */
    @Override
    public void setSolid(boolean solid) {
        savedSolidState = solid;
        if (implPA.isLive()) {
            implPA.dataChanged(this);
        } else {
            updateNodeDataChanges(implPA);
        }

        // May need to add or remove the polyAttr from the appearance
        // depending on the combined state of this and CCW
        fillPropsChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    /**
     * Set whether lighting will be used for this appearance. In general you
     * should let the material node decide this. Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting be
     * turned off. This method will notify the related Material node for this
     * Appearance.
     *
     * @param enable Whether lighting is enabled
     */
    @Override
    public void setLightingEnabled(boolean enable) {
        lightingState = enable;

        if (vfMaterial != null && !lightingOverride) {
            ((OGLMaterialNodeType) vfMaterial).setLightingEnable(enable);
        }
    }

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     *
     * @param enable Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha values
     */
    @Override
    public void setLocalColor(boolean enable, boolean hasAlpha) {
        localColor = enable;
        localColorAlpha = hasAlpha;

        if (vfMaterial != null) {
            ((OGLMaterialNodeType) vfMaterial).setLocalColor(enable, hasAlpha);
        }
    }

    /**
     * Specify whether the geometry's triangles are in counter clockwise order
     * (the default) or clockwise. The default is true. This will determine if
     * we do backface culling and flip backface normals. Can only be set during
     * setup
     *
     * @param ccw True for counter-clockwise ordering
     */
    @Override
    public void setCCW(boolean ccw) {
        savedCCWState = ccw;
        if (implPA.isLive()) {
            implPA.dataChanged(this);
        } else {
            updateNodeDataChanges(implPA);
        }

        // May need to add or remove the polyAttr from the appearance
        // depending on the combined state of this and solid
        fillPropsChanged = true;
        if (oglImplNode.isLive()) {
            oglImplNode.dataChanged(this);
        } else {
            updateNodeDataChanges(oglImplNode);
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLTextureTransformListener
    //----------------------------------------------------------

    /**
     * Invoked when a textureTransform has changed.
     *
     * @param src The node instance that was the source of this change
     * @param tmatrix The new TransformMatrix array
     * @param updated Flag for each index illustrating whether it has been
     * updated or not.
     */
    @Override
    public void textureTransformChanged(OGLVRMLNode src,
            Matrix4f[] tmatrix,
            boolean[] updated) {

        int cnt = tmatrix.length;

        insureStageSize(cnt, false);

        for (int i = 0; i < cnt; i++) {
            if (!updated[i]) {
                continue;
            }

            changedTransforms.put(texUnits[i], stages[i]);

            stages[i].transform = tmatrix[i];

            if (texUnits[i].isLive()) {
                texUnits[i].dataChanged(this);
            } else {
                updateNodeDataChanges(texUnits[i]);
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed. Synchronized due to
     * competition for the image arrays.
     *
     * @param idx The image idx which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    @Override
    public synchronized void textureImageChanged(
            int idx,
            VRMLNodeType node,
            NIOBufferImage image,
            String url) {

        super.textureImageChanged(idx, node, image, url);

        if (idx < numStages && vrmlTexs != null) {
            int type = vfTexture.getTextureType();
            switch (type) {
                case TextureConstants.TYPE_SINGLE_2D:
                    // rem /////////////////////////////////////////////////////
                    // set ignore diffuse on the material node in the
                    // case of a single texture, but not a multi-texture
                    TextureStage tstage = stages[idx];
                    if ((tstage.images != null) && (tstage.images.length > 0)) {
                        setIgnoreDiffuse(tstage.images[0]);
                    }
                    // TDN 02 APR 2013 /////////////////////////////////////////
                    // A break statement here WILL break texturing, therefore a
                    // deliberate fallthrough is permitted here.
                    ////////////////////////////////////////////////////////////
                case TextureConstants.TYPE_MULTI:
                    texObjs[idx] = createTexture(stages[idx],
                            url,
                            vrmlTexs[idx].getTextureType());
                    /*
                     System.out.println("Forcing call to texture atts");
                     texAttrs[idx] = createTextureAttributes(idx,stages[idx]);
                     */
                    texUnitsChanged.add(texUnits[idx]);

                    ((OGLTextureNodeType) vfTexture).setTexture(idx,
                            texObjs[idx]);
                    threadedUnitChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                    break;

                case TextureConstants.TYPE_SINGLE_3D:
                    texObjs[0] = createTexture(stages[0], url, type);
                    texUnitsChanged.add(texUnits[0]);
                    ((OGLTextureNodeType) vfTexture).setTexture(0, texObjs[0]);
                    threadedUnitChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                    break;
            }
        } else {
            //System.out.println("** Create TU: " + url);
            createTextureUnits();
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

        super.textureImageChanged(len, node, image, url);

        // rem //////////////////////////////////////////////////////////////////
        // this has not been tested - doesn't look right
        if (len <= numStages) {
            int type = vfTexture.getTextureType();

            if (type == TextureConstants.TYPE_SINGLE_3D) {
                stages[0].mode = getMode(image[0], true);
                texObjs[0] = createTexture(stages[0], null, type);
                texUnits[0].setTexture(texObjs[0]);
                ((OGLTextureNodeType) vfTexture).setTexture(0, texObjs[0]);

            } else {
                for (int idx = 0; idx < len; idx++) {
                    stages[idx].mode = getMode(image[idx], (idx == 0));
                    texObjs[idx] = createTexture(stages[idx],
                            url[idx],
                            vrmlTexs[idx].getTextureType());
                    // TODO: shouldn't this call dataChanged?
                    texUnits[idx].setTexture(texObjs[idx]);
                    ((OGLTextureNodeType) vfTexture).setTexture(idx, texObjs[idx]);
                }
            }
        } else {
            createTextureUnits();
        }
        //////////////////////////////////////////////////////////////////////////
    }

    /**
     * Invoked when the texture parameters have changed. The most efficient
     * route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it. 3 Component color.
     */
    @Override
    public void textureParamsChanged(int idx, int mode,
            int source, int function, float alpha, float[] color) {

        super.textureParamsChanged(idx, mode, source, function, alpha, color);
    }

    /**
     * Invoked when the texture parameters have changed. The most efficient
     * route is to set the parameters before the image.
     *
     * @param len The number of valid entries in the arrays.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it. An array of 3
     * component colors.
     */
    @Override
    public void textureParamsChanged(int len,
            int mode[],
            int[] source,
            int[] function,
            float alpha,
            float[] color) {

        super.textureParamsChanged(len, mode, source, function, alpha, color);

        if (len <= numStages) {
            for (int idx = 0; idx < len; idx++) {
                stages[idx].mode = mode[idx];
                setTextureMode(idx, stages[idx], texAttrs[idx]);
            }
        } else {
            createTextureUnits();
        }
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the scene graph object representation of this node. This will need to
     * be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return oglImplNode;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

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

        OGLMaterialNodeType node = (OGLMaterialNodeType) vfMaterial;
        if (vfMaterial != null) {
            node.setLightingEnable(lightingState);
            node.setLocalColor(localColor, localColorAlpha);
        }

        Material mat = (node == null) ? null : node.getMaterial();
        oglImplNode.setMaterial(mat);

        if (vfLineProperties != null) {
            OGLVRMLNode o_n = (OGLVRMLNode) vfLineProperties;
            LineAttributes la = (LineAttributes) o_n.getSceneGraphObject();
            oglImplNode.setLineAttributes(la);
        }

        if (vfPointProperties != null) {
            queryPointSprites = true;
            // Find out whether we have point sprite support
            stateManager.addEndOfThisFrameListener(this);

            OGLVRMLNode o_n = (OGLVRMLNode) vfPointProperties;
            PointAttributes pa = (PointAttributes) o_n.getSceneGraphObject();

            if (pointSpritesSupported && vfTexture != null) {
                pa.setAntiAliased(false);
                pa.setPointSpriteEnabled(true);
            } else {
                pa.setPointSpriteEnabled(false);
                pa.setAntiAliased(true);

                // TODO: Disable texturing.  This is not exactly right if
                // someone provided PointProperties for a non pointset
                vfTexture = null;
            }

            oglImplNode.setPointAttributes(pa);
        }

        createTextureTransform();
        createTextureUnits();

        materialChanged = false;
        fillPropsChanged = false;
        linePropsChanged = false;
        pointPropsChanged = false;
        textureUnitsChanged = false;
        textureTransformChanged = false;
        textureChanged = false;
        threadedUnitChanged = false;
    }

    @Override
    public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
            throws InvalidFieldValueException {

        if (inSetup) {
            return;
        }

        super.notifyExternProtoLoaded(index, node);

        OGLVRMLNode kid = (OGLVRMLNode) node;

        switch (index) {
            case FIELD_MATERIAL:
                if (oglImplNode.isLive()) {
                    materialChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                } else {
                    Material mat = (Material) kid.getSceneGraphObject();
                    oglImplNode.setMaterial(mat);
                }
                break;

            case FIELD_TEXTURE:
                createTextureUnits();
                break;

            case FIELD_TEXTURE_TRANSFORM:
                createTextureTransform();
                break;

            case FIELD_LINE_PROPERTIES:
                if (oglImplNode.isLive()) {
                    linePropsChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                } else {
                    LineAttributes la = (LineAttributes) kid.getSceneGraphObject();
                    oglImplNode.setLineAttributes(la);
                }
                break;
            case FIELD_POINT_PROPERTIES:
                if (oglImplNode.isLive()) {
                    linePropsChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                } else {
                    PointAttributes pa = (PointAttributes) kid.getSceneGraphObject();
                    oglImplNode.setPointAttributes(pa);
                }
                break;

            case FIELD_FILL_PROPERTIES:
                if (oglImplNode.isLive()) {
                    fillPropsChanged = true;
                    stateManager.addEndOfThisFrameListener(this);
                } else {
                //                    PolygonAttributes pa =
                    //                        (PolygonAttributes)kid.getSceneGraphObject();
                    //                    oglImplNode.setPolygonAttributes(pa);
                }
                break;
        }
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {
        if (src == oglImplNode) {
            if (materialChanged) {
                materialChanged = false;

                OGLMaterialNodeType node = (OGLMaterialNodeType) vfMaterial;
                Material mat = (node == null) ? null : node.getMaterial();
                oglImplNode.setMaterial(mat);
            }

            if (textureChanged) {
                textureChanged = false;

                // TODO: Can we optimize this?
                createTextureUnits();
            }

            if (textureTransformChanged) {
                textureTransformChanged = false;

                // TODO: Can we optimize this?
                createTextureTransform();
            }

            if (textureUnitsChanged) {
                textureUnitsChanged = false;
                oglImplNode.setTextureUnits(texUnits, numStages);
            }

            if (linePropsChanged) {
                linePropsChanged = false;

                LineAttributes la = null;

                if (vfLineProperties != null) {
                    OGLVRMLNode o_n = (OGLVRMLNode) vfLineProperties;
                    la = (LineAttributes) o_n.getSceneGraphObject();
                }

                oglImplNode.setLineAttributes(la);
            }

            if (pointPropsChanged) {
                pointPropsChanged = false;

                PointAttributes pa = null;

                if (vfPointProperties != null) {
                    OGLVRMLNode o_n = (OGLVRMLNode) vfPointProperties;
                    pa = (PointAttributes) o_n.getSceneGraphObject();
                }

                oglImplNode.setPointAttributes(pa);
            }

            if (fillPropsChanged) {
                fillPropsChanged = false;

                if (!savedSolidState || !savedCCWState) {
                    oglImplNode.setPolygonAttributes(implPA);
                } else {
                    oglImplNode.setPolygonAttributes(null);
                }

                /*
                 PolygonAttributes pa = null;

                 if(vfFillProperties != null) {
                 OGLVRMLNode o_n = (OGLVRMLNode)vfFillProperties;
                 pa = (PolygonAttributes)o_n.getSceneGraphObject();
                 }

                 oglImplNode.setPolygonAttributes(pa);
                 */
            }

        } else if (src == implPA) {
            if (savedSolidState) {
                implPA.setCulledFace(PolygonAttributes.CULL_BACK);
                implPA.setTwoSidedLighting(false);
            } else {
                implPA.setCulledFace(PolygonAttributes.CULL_NONE);
                implPA.setTwoSidedLighting(true);
            }

            implPA.setCCW(savedCCWState);
        } else if (src instanceof TextureUnit) {
            // Check for textureUnit
            for (int i = 0; i < numStages; i++) {
                if (texUnits[i] == src) {
                    if (stages[i].mode != TextureConstants.MODE_OFF) {
                        texUnits[i].setTexture(texObjs[i]);
                    } else {
                        // rem: for the multi-texture mode OFF
                        // null out the Texture in the TextureUnit
                        texUnits[i].setTexture(null);
                    }
                    break;
                }
            }

            // Remove the processed unit
            texUnitsChanged.remove((SceneGraphObject) src);

            TextureStage ts = changedTransforms.get((TextureUnit) src);

            if (ts != null) {
                TextureUnit tu = (TextureUnit) src;
                tu.setTextureTransform((Matrix4f) ts.transform);
                changedTransforms.remove(tu);
            }

            ts = changedTexCoordGeneration.get((TextureUnit) src);

            if (ts != null) {
                TextureUnit tu = (TextureUnit) src;
                tu.setTexCoordGeneration((TexCoordGeneration) ts.texCoordGeneration);
                changedTexCoordGeneration.remove(tu);
            }
        }
    }

    //----------------------------------------------------------
    // Private methods
    //----------------------------------------------------------

    /**
     * Internal convenient method to do common initialization.
     */
    private void init() {
        lightingState = true;
        localColor = false;
        localColorAlpha = false;
        savedSolidState = true;
        savedCCWState = true;
        lightingOverride = false;
        queryPointSprites = false;

        oglImplNode = new Appearance();

        implPA = new PolygonAttributes();
        texGenMap = new HashMap<>(2);

        changedTransforms = new HashMap<>();
        changedTexCoordGeneration = new HashMap<>();

        if (!savedCCWState || !savedSolidState) {
            oglImplNode.setPolygonAttributes(implPA);
        }

        if (useTextureCache) {
            textureCache = TextureCache.getInstance();
        }

        numStages = 0;

        texUnitsChanged = new ConcurrentLinkedQueue<>();
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createTextureUnits() {
        // Find out how many stages to setup
        numStages = 0;

        if (vfTexture != null) {
            switch (vfTexture.getTextureType()) {
                case TextureConstants.TYPE_SINGLE_2D:
                    numStages++;
                    vrmlTexs = new VRMLTextureNodeType[numStages];
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];
                    vrmlTexs[numStages - 1] = vfTexture;

                    modes[numStages - 1]
                            = getMode(((VRMLTexture2DNodeType) vfTexture).getImage(),
                                    true);

                    if (modes[numStages - 1] == TextureConstants.MODE_REPLACE) {
                        OGLMaterialNodeType node = (OGLMaterialNodeType) vfMaterial;
                        Material mat = (node == null) ? null : node.getMaterial();

                        if (mat != null && vfMaterial.getTransparency() == 0) {
                            mat.setLightingEnabled(false);
                            lightingOverride = true;
                        } else {
                            if (mat != null && lightingState) {
                                mat.setLightingEnabled(true);
                            }
                            lightingOverride = false;
                        }
                    }
                    // TODO: Fill in source and function
                    break;

                case TextureConstants.TYPE_MULTI:
                    VRMLMultiTextureNodeType mtex
                            = ((VRMLMultiTextureNodeType) vfTexture);

                    numStages += mtex.getNumberTextures();

                    vrmlTexs = new VRMLTextureNodeType[numStages];
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];

                    mtex.getTextures(0, vrmlTexs);
                    mtex.getTextureParams(0, modes, functions, sources);
                    break;

                case TextureConstants.TYPE_SINGLE_3D:
                    VRMLComposedTextureNodeType ctex
                            = ((VRMLComposedTextureNodeType) vfTexture);

                    vrmlTexs = new VRMLTextureNodeType[numStages
                            + ctex.getNumberTextures()];
                    numStages++;
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];

                    ctex.getTextures(0, vrmlTexs);
                    modes[numStages - 1] = TextureConstants.MODE_REPLACE;
                    break;

                case TextureConstants.TYPE_PBUFFER:
                    numStages++;
                    vrmlTexs = new VRMLTextureNodeType[numStages];
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];

                    vrmlTexs[numStages - 1] = vfTexture;
                    modes[numStages - 1] = TextureConstants.MODE_REPLACE;
                    break;

                case TextureConstants.TYPE_CUBIC_ENVIRONMAP:
                    break;
            }
        }

        texObjs = new Texture[numStages];
        texUnits = new TextureUnit[numStages];
        texAttrs = new TextureAttributes[numStages];

        insureStageSize(numStages, true);

        int currStage = 0;

        // Setup the TextureStage variables
        for (int i = currStage; i < numStages; i++) {
            TextureStage tstage = stages[i];
            TexCoordGeneration tcg = null;
            boolean have_texture = false;

            // Common variables
            if (vfTextureProperties != null) {
                // Only use 2D properties
                VRMLTextureProperties2DNodeType tp
                        = vfTextureProperties;

                tstage.boundaryModeS = tp.getBoundaryModeS();
                tstage.boundaryModeT = tp.getBoundaryModeT();
                tstage.boundaryColor = new float[4];
                tp.getBorderColor(tstage.boundaryColor);
                tstage.minFilter = tp.getMinificationFilter();
                tstage.magFilter = tp.getMagnificationFilter();
            }

            // Texture Type specific
            int type = vrmlTexs[i].getTextureType();

            switch (type) {
                case TextureConstants.TYPE_MULTI:
                    errorReporter.warningReport(NESTED_MULTITEXTURE_ERR, null);
                    break;

                case TextureConstants.TYPE_SINGLE_2D:
                    type = vrmlTexs[i].getTextureType();

                    if (tstage.images == null || tstage.images.length == 0) {
                        NIOBufferImage img = ((VRMLTexture2DNodeType) vrmlTexs[i]).getImage();
                        if (img != null) {
                            tstage.images = processImage(0, img, null);
                            setIgnoreDiffuse(img);
                        }
                    }

                    tstage.mode = modes[i];
                    if (vfTextureProperties == null) {
                        VRMLTexture2DNodeType sn
                                = (VRMLTexture2DNodeType) vrmlTexs[i];

                        tstage.boundaryModeS = sn.getRepeatS()
                                ? TextureConstants.BM_WRAP
                                : DEFAULT_CLAMP_MODE;

                        tstage.boundaryModeT = sn.getRepeatT()
                                ? TextureConstants.BM_WRAP
                                : DEFAULT_CLAMP_MODE;
                    }

                    String modeString
                            = texGenMap.get(currStage);

                    if (modeString != null) {
                        Integer mode = texGenModeMap.get(modeString);

                        if (mode != null) {
                            tcg = new TexCoordGeneration();

                            tcg.setParameter(TexCoordGeneration.TEXTURE_S,
                                    TexCoordGeneration.MODE_GENERIC, mode,
                                    null);
                            tcg.setParameter(TexCoordGeneration.TEXTURE_T,
                                    TexCoordGeneration.MODE_GENERIC, mode,
                                    null);

                            tstage.texCoordGeneration = tcg;
                        } else {
                            System.out.println("TexCoordGen mode not found: " + mode);
                            tstage.texCoordGeneration = null;
                        }
                    }
                    break;

                case TextureConstants.TYPE_SINGLE_3D:

                    tstage.mode = modes[i];
                    // TODO: need to switch to 3D properties
                    if (vfTextureProperties != null) {
                        VRMLTextureProperties2DNodeType tp
                                = vfTextureProperties;
                        //tstage.boundaryModeR = tp.getBoundaryModeR();
                    } else {
                        VRMLTexture3DNodeType sn
                                = (VRMLTexture3DNodeType) vfTexture;

                        tstage.boundaryModeS = sn.getRepeatS()
                                ? TextureConstants.BM_WRAP
                                : DEFAULT_CLAMP_MODE;

                        tstage.boundaryModeT = sn.getRepeatT()
                                ? TextureConstants.BM_WRAP
                                : DEFAULT_CLAMP_MODE;

                        tstage.boundaryModeR = sn.getRepeatR()
                                ? TextureConstants.BM_WRAP
                                : DEFAULT_CLAMP_MODE;

                        tstage.depth = sn.getDepth();
                    }

                    modeString = texGenMap.get(currStage);

                    if (modeString != null) {
                        Integer mode = texGenModeMap.get(modeString);

                        if (mode != null) {
                            tcg = new TexCoordGeneration();

                            tcg.setParameter(TexCoordGeneration.TEXTURE_S,
                                    TexCoordGeneration.MODE_GENERIC, mode,
                                    null);
                            tcg.setParameter(TexCoordGeneration.TEXTURE_T,
                                    TexCoordGeneration.MODE_GENERIC, mode,
                                    null);

                            tcg.setParameter(TexCoordGeneration.TEXTURE_R,
                                    TexCoordGeneration.MODE_GENERIC, mode,
                                    null);

                            tstage.texCoordGeneration = tcg;
                        } else {
                            System.out.println("TexCoordGen mode not found: " + mode);
                            tstage.texCoordGeneration = null;
                        }
                    }
                    break;

                case TextureConstants.TYPE_PBUFFER:
                    have_texture = true;
                    OGLVRMLNode o_tex = (OGLVRMLNode) vfTexture;
                    texObjs[i] = (Texture) o_tex.getSceneGraphObject();
                    break;

                case TextureConstants.TYPE_CUBIC_ENVIRONMAP:
                    break;
            }

            if (!have_texture) {
                texObjs[i] = createTexture(tstage, urls[i], type);
                ((OGLTextureNodeType) vfTexture).setTexture(i, texObjs[i]);
            }

            texAttrs[i] = createTextureAttributes(i, tstage);
            texUnits[i] = new TextureUnit(texObjs[i], texAttrs[i], tcg);

            if (tstage.transform != null) {
                texUnits[i].setTextureTransform((Matrix4f) tstage.transform);
            }
        }

        //System.out.println("CTU: end: " + numStages);
        oglImplNode.setTextureUnits(texUnits, numStages);
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createTextureTransform() {
        if (vfTextureTransform != null) {
            Matrix4f[] tt = ((OGLTextureCoordinateTransformNodeType) vfTextureTransform).getTransformMatrix();

            int cnt = tt.length;

            // Hold all texture transforms, but don't increase valid stages(numStages)
            insureStageSize(cnt, false);

            for (int i = 0; i < cnt; i++) {
                stages[i].transform = tt[i];
            }
        }
    }

    /**
     * Create the Texture object for this stage.
     *
     * @param tstage The stage representation for this texture
     * @param url The url of the image, if there is one
     * @param type The textureType (SINGLE, MULTI, 3D etc)
     * @return A Texture object that corresponds to this stage
     */
    private Texture createTexture(TextureStage tstage, String url, int type) {
        Texture ret_val = null;

        // If we have no image in the first place, just return with nothing
        // created.
        boolean no_image_data = (tstage.images == null
                || tstage.images.length < 1
                || tstage.images[0] == null);

        TextureComponent[] comps;
        int len;
        int texType;
        int width;
        int height;

        // Setup texture stage variables
        if (vfTextureProperties != null) {
            VRMLTextureProperties2DNodeType tps
                    = vfTextureProperties;

            tstage.generateMipMaps = tps.getGenerateMipMaps();
            tstage.boundaryModeS = tps.getBoundaryModeS();
            tstage.boundaryModeT = tps.getBoundaryModeT();
            tstage.minFilter = tps.getMinificationFilter();
            tstage.magFilter = tps.getMagnificationFilter();
            tstage.anisotropicMode = tps.getAnisotropicMode();
            tstage.anisotropicDegree = tps.getAnisotropicDegree();
        } else {
            switch (type) {
                case TextureConstants.TYPE_SINGLE_2D:
                    VRMLTexture2DNodeType tex2d
                            = (VRMLTexture2DNodeType) vrmlTexs[tstage.stageNumber];

                    tstage.boundaryModeS = tex2d.getRepeatS()
                            ? TextureConstants.BM_WRAP
                            : DEFAULT_CLAMP_MODE;

                    tstage.boundaryModeT = tex2d.getRepeatT()
                            ? TextureConstants.BM_WRAP
                            : DEFAULT_CLAMP_MODE;

                    break;

                case TextureConstants.TYPE_SINGLE_3D:
                    VRMLTexture3DNodeType tex3d
                            = (VRMLTexture3DNodeType) vrmlTexs[tstage.stageNumber];

                    tstage.boundaryModeS = tex3d.getRepeatS()
                            ? TextureConstants.BM_WRAP
                            : DEFAULT_CLAMP_MODE;

                    tstage.boundaryModeT = tex3d.getRepeatT()
                            ? TextureConstants.BM_WRAP
                            : DEFAULT_CLAMP_MODE;

                    tstage.boundaryModeR = tex3d.getRepeatR()
                            ? TextureConstants.BM_WRAP
                            : DEFAULT_CLAMP_MODE;
                    break;
            }

            if (anisotropicDegree > 1) {
                tstage.anisotropicMode
                        = TextureConstants.ANISOTROPIC_MODE_SINGLE;
                tstage.anisotropicDegree = anisotropicDegree;
            }
        }

        // Create the Texture object
        try {
            switch (type) {
                case TextureConstants.TYPE_MULTI:
                    break;

                case TextureConstants.TYPE_SINGLE_2D:
                    ret_val = new Texture2D();

                    int val = OGLTextureConstConverter.convertAnisotropicMode(tstage.anisotropicMode);
                    ret_val.setAnisotropicFilterMode(val);
                    ret_val.setAnisotropicFilterDegree(tstage.anisotropicDegree);

                    val = OGLTextureConstConverter.convertBoundary(tstage.boundaryModeS);
                    ret_val.setBoundaryModeS(val);

                    val = OGLTextureConstConverter.convertBoundary(tstage.boundaryModeT);
                    ((Texture2D) ret_val).setBoundaryModeT(val);

                    val = OGLTextureConstConverter.convertMinFilter(tstage.minFilter);
                    ret_val.setMinFilter(val);

                    val = OGLTextureConstConverter.convertMagFilter(tstage.magFilter);
                    ret_val.setMagFilter(val);

                    if (!no_image_data) {
                        NIOBufferImage image = tstage.images[0];
                        int format = getFormat(image);
                        comps = new TextureComponent2D[1];

                        comps[0] = new ByteBufferTextureComponent2D(
                                format,
                                image.getWidth(),
                                image.getHeight(),
                                image.getBuffer(null));
                        texType = getTextureFormat(comps[0]);

                        len = tstage.images.length;

                        // Release reference to save memory
                        for (int i = 0; i < len; i++) {
                            tstage.images[i] = null;
                        }

                        tstage.images = null;

                        // rem /////////////////////////////////////////////////
                        // new image handling - just because mipmap generation
                        // is enabled - does not mean we have mipmaps available.
                        //if (tstage.generateMipMaps == false &&
                        //  useMipMaps == false) {
                        if (image.getLevels() == 1) {
                            ////////////////////////////////////////////////////
                            ret_val.setSources(Texture2D.MODE_BASE_LEVEL,
                                    texType,
                                    comps,
                                    1);
                        } else {
                            ret_val.setSources(Texture2D.MODE_MIPMAP,
                                    texType,
                                    comps,
                                    1);
                        }

                        comps[0].clearLocalData();
                    }

                    break;

                case TextureConstants.TYPE_SINGLE_3D:
                    ret_val = new Texture3D();

                    //                val = convertAnisotropicMode(tstage.anisotropicMode);
                    ret_val.setAnisotropicFilterMode(Texture.ANISOTROPIC_MODE_SINGLE);
                    ret_val.setAnisotropicFilterDegree(tstage.anisotropicDegree);

                    val = OGLTextureConstConverter.convertBoundary(tstage.boundaryModeS);
                    ret_val.setBoundaryModeS(val);

                    val = OGLTextureConstConverter.convertBoundary(tstage.boundaryModeT);
                    ((Texture3D) ret_val).setBoundaryModeT(val);

                    val = OGLTextureConstConverter.convertBoundary(tstage.boundaryModeR);
                    ((Texture3D) ret_val).setBoundaryModeR(val);

                    val = OGLTextureConstConverter.convertMinFilter(tstage.minFilter);
                    ret_val.setMinFilter(val);

                    val = OGLTextureConstConverter.convertMagFilter(tstage.magFilter);
                    ret_val.setMagFilter(val);

                    // Do a couple more checks on the texture object
                    // and whether we should load the image information.
                    // Not enough images in the array yet.
                    if (tstage.images.length < tstage.depth) {
                        no_image_data = true;
                    }

                    // Don't create a texture until we have all the images
                    // specified.
                    if (!no_image_data) {
                        int format = getFormat(tstage.images[0]);
                        for (int i = 0; i < tstage.depth; i++) {
                            if (tstage.images[i] == null) {
                                no_image_data = true;
                            }

                            if (getFormat(tstage.images[i]) != format) {
                                no_image_data = true;
                            }
                        }
                    }

                    if (!no_image_data) {
                        int format = getFormat(tstage.images[0]);
                        width = tstage.images[0].getWidth();
                        height = tstage.images[0].getHeight();

                        TextureComponent3D[] comp_3
                                = new TextureComponent3D[1];

                        int num_img = tstage.images.length;
                        ByteBuffer[] img_buffer = new ByteBuffer[num_img];
                        for (int i = 0; i < num_img; i++) {
                            img_buffer[i] = tstage.images[i].getBuffer();
                        }
                        comp_3[0] = new ByteBufferTextureComponent3D(
                                width,
                                height,
                                format,
                                img_buffer);

                        texType = getTextureFormat(comp_3[0]);
                        ret_val.setSources(Texture3D.MODE_BASE_LEVEL,
                                texType,
                                comp_3,
                                1);

                        tstage.images = null;
                    }

                    break;
            }
        } catch (InvalidWriteTimingException e) {
            errorReporter.errorReport(TEXTURE_CREATE_FAIL_MSG + url, e);
            return ret_val;
        }

        // rem //////////////////////////////////////////////////////////////////////
        // the following is a legacy comment from the previous caching system.
        // preserved as there will probably still be an issue with 3D textures
        // using the new texture cache
        //////////////////////////////////////////////////////////////////////////////
        // TODO:
        // There's an implementation issue here with 3D textures, particularly for
        // textures that use multiple separate images. Only the last loaded image is
        // actually cached. If the image is reloaded, then if one of the other URLs is
        // the last to load, it will return null for the texture as it says it has the
        // texture pre-loaded, but doesn't have it cached. This is.... unfortunate.
        /*
         if (useTextureCache && url != null) {
         ((AVTextureCache)textureCache).registerTexture(ret_val, url);
         }
         */
        //
        //////////////////////////////////////////////////////////////////////////////
        if (useTextureCache) {
            ret_val = textureCache.register(url, ret_val);
        }

        return ret_val;
    }

    /**
     * Create the textureAttributes for a texture stage.
     */
    private TextureAttributes createTextureAttributes(int stage,
            TextureStage tstage) {

        TextureAttributes ret_val = new TextureAttributes();

        // this MUST stay here, before the vfPointProperties check !
        setTextureMode(stage, tstage, ret_val);

        if (pointSpritesSupported && vfPointProperties != null) {
            ret_val.setPointSpriteCoordEnabled(true);
            ret_val.setTextureMode(TextureAttributes.MODE_COMBINE);
            ret_val.setCombineMode(true, TextureAttributes.COMBINE_MODULATE);

            switch (vfPointProperties.getColorMode()) {
                case VRMLPointPropertiesNodeType.POINT_COLOR_MODE:
                    // use point color only
                    ret_val.setCombineSource(false, 0, TextureAttributes.SOURCE_BASE_COLOR);
                    ret_val.setCombineMode(false, TextureAttributes.COMBINE_REPLACE);
                    break;
                case VRMLPointPropertiesNodeType.TEXTURE_AND_POINT_COLOR_MODE:
                    // use both point color and texture color
                    ret_val.setCombineMode(false, TextureAttributes.COMBINE_ADD);
                    break;
                default:
                    ret_val.setCombineMode(false, TextureAttributes.COMBINE_REPLACE);
                    break;
            }
        } else {
            ret_val.setPointSpriteCoordEnabled(false);
        }

        /*
         if (tstage.transform != null)
         ret_val.setTextureTransform((Transform3D)tstage.transform);
         */
        return ret_val;
    }

    /**
     * Set the texture mode based on stage information.
     */
    private void setTextureMode(int stage,
            TextureStage tstage,
            TextureAttributes atts) {

        switch (tstage.mode) {
            case TextureConstants.MODE_REPLACE:
                atts.setTextureMode(TextureAttributes.MODE_REPLACE);
                break;
            case TextureConstants.MODE_MODULATE:
                atts.setTextureMode(TextureAttributes.MODE_MODULATE);
                break;
            case TextureConstants.MODE_MODULATE_2X:
                atts.setTextureMode(TextureAttributes.MODE_MODULATE);
                atts.setCombineScale(false, 2);
                break;
            case TextureConstants.MODE_MODULATE_4X:
                atts.setTextureMode(TextureAttributes.MODE_MODULATE);
                atts.setCombineScale(false, 4);
                break;
            case TextureConstants.MODE_DOTPRODUCT3:
                atts.setTextureMode(TextureAttributes.MODE_COMBINE);
                atts.setCombineMode(false, TextureAttributes.COMBINE_DOT3_RGB);
                atts.setCombineMode(true, TextureAttributes.COMBINE_REPLACE);
                atts.setCombineSource(false, 0, TextureAttributes.SOURCE_CURRENT_TEXTURE);

            //                atts.setBlendColor(0.5f, 0, 0, 1);
                //                atts.setCombineSource(true, 0, TextureAttributes.SOURCE_CONSTANT_COLOR);
                break;
            case TextureConstants.MODE_ADD:
                atts.setTextureMode(TextureAttributes.MODE_COMBINE);
                atts.setCombineMode(false, TextureAttributes.COMBINE_ADD);
                atts.setCombineMode(true, TextureAttributes.MODE_REPLACE);
                break;
            case TextureConstants.MODE_ADD_SIGNED:
                atts.setTextureMode(TextureAttributes.MODE_COMBINE);
                atts.setCombineMode(false, TextureAttributes.COMBINE_ADD_SIGNED);
                break;
            case TextureConstants.MODE_ADD_SIGNED_2X:
                atts.setTextureMode(TextureAttributes.MODE_COMBINE);
                atts.setCombineMode(false, TextureAttributes.COMBINE_ADD_SIGNED);
                atts.setCombineScale(false, 2);
                break;
            case TextureConstants.MODE_SUBTRACT:
                atts.setTextureMode(TextureAttributes.MODE_COMBINE);
                atts.setCombineMode(false, TextureAttributes.COMBINE_SUBTRACT);
                break;
            case TextureConstants.MODE_OFF:
                if (texUnits[stage] != null) {
                    texUnits[stage].setTexture(null);
                }
                break;

            default:
                System.err.println("Unknown TextureConstants.Mode: " + tstage.mode);
        }
    }

    /**
     * From the image component format, generate the appropriate texture format.
     *
     * @param comp The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    protected int getTextureFormat(TextureComponent comp) {

        int ret_val = Texture.FORMAT_RGB;

        switch (comp.getFormat(0)) {
            case TextureComponent.FORMAT_SINGLE_COMPONENT:

                // could also be alpha, but we'll punt for now. We really need
                // the user to pass in this information. Need to think of a
                // good way of doing this.
                ret_val = Texture.FORMAT_LUMINANCE;
                break;

            case TextureComponent.FORMAT_INTENSITY_ALPHA:
                ret_val = Texture.FORMAT_LUMINANCE_ALPHA;
                break;

            case TextureComponent.FORMAT_RGB:
                ret_val = Texture.FORMAT_RGB;
                break;

            case TextureComponent.FORMAT_RGBA:
                ret_val = Texture.FORMAT_RGBA;
                break;
        }

        return ret_val;
    }

    /**
     * From the image information, generate the appropriate TextureComponent
     * type.
     *
     * @param image The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    protected int getFormat(NIOBufferImage image) {

        int format = 0;
        NIOBufferImageType type = image.getType();

        if (type == NIOBufferImageType.INTENSITY) {

            format = TextureComponent.FORMAT_SINGLE_COMPONENT;

        } else if (type == NIOBufferImageType.INTENSITY_ALPHA) {

            format = TextureComponent.FORMAT_INTENSITY_ALPHA;

        } else if (type == NIOBufferImageType.RGB) {

            format = TextureComponent.FORMAT_RGB;

        } else if (type == NIOBufferImageType.RGBA) {

            format = TextureComponent.FORMAT_RGBA;

        } else {

            System.err.println("Unknown NIOBufferImageType: " + type.name);
        }

        return format;
    }

    /**
     * Given the image format return the texturing mode to use. For the X3D
     * lighting model, whether to use MODULATE or REPLACE.
     *
     * @param image The image component to get the value from
     * @param configMaterial Should this affect the material
     * @return the texturing mode to use
     */
    private int getMode(NIOBufferImage image, boolean configMaterial) {

        int mode = TextureConstants.MODE_MODULATE;
        if (configMaterial) {
            setIgnoreDiffuse(image);
        }
        return mode;
    }

    /**
     * Configure the ignoreDiffuse property of the material based on the image.
     */
    private void setIgnoreDiffuse(NIOBufferImage image) {

        boolean ignoreDiffuse = true;
        if (image != null) {

            // NOTE: Why would we care to ignore diffuse color if the texture
            // image is not gray scale?  !image.isGrayScale() doesn't seem
            // right. (TDN)
            ignoreDiffuse = !image.hasTransparency();
        }

        if (vfMaterial != null) {
            vfMaterial.setIgnoreDiffuse(ignoreDiffuse);
        }
    }
}
