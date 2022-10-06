/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External imports
import org.j3d.aviatrix3d.BoundingBox;
import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.PointLight;
import org.j3d.aviatrix3d.SimpleLayer;
import org.j3d.aviatrix3d.SimpleScene;
import org.j3d.aviatrix3d.SimpleViewport;
import org.j3d.aviatrix3d.management.DisplayCollection;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import javax.vecmath.*;

// Local imports
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;

import org.web3d.vrml.nodes.FrameStateListener;
import org.web3d.vrml.nodes.FrameStateManager;

import org.web3d.vrml.renderer.ogl.nodes.OGLLightNodeType;

import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * A utility that inserts three point light nodes to the scene root.
 *
 * @author Chris Shankland
 * @version $Revision: 1.10 $
 */
public class ConfigureThreePointLights implements FrameStateListener {

    /**
     * Index of key light
     */
    private static int KEY_LIGHT = 0;

    /**
     * Index of fill light
     */
    private static int FILL_LIGHT = 1;

    /**
     * Index of back light
     */
    private static int BACK_LIGHT = 2;

    /**
     * Default def name used to specify a viewpoint
     */
    private static final String DEFAULT_VIEWPOINT = "ICON_VIEWPOINT";

    /**
     * Number of available lights
     */
    private static final int NUM_LIGHTS = 3;

    /**
     * 45 degree in radians
     */
    private static final float ROT_RADIANS = 0.785398163f;

    /**
     * Offset outside of the bounding box to ensure lights are infront of and
     * behind the object
     */
    // Commented out by Sang Park
    //private static final float OFFSET_FRACTION = 10f;
    /**
     * Diffuse color of the light
     */
    private static float[][] DIFFUSE_COLOR = {{0.475f, 0.475f, 0.475f},
                                              {0.475f, 0.475f, 0.475f},
                                              {0.435f, 0.435f, 0.435f}};

    /**
     * Specular color of the light
     */
    private static float[][] SPECULAR_COLOR = {{0.0f, 0.0f, 0.0f},
                                               {0.0f, 0.0f, 0.0f},
                                               {0.0f, 0.0f, 0.0f}};

    /**
     * Ambient color of the light
     */
    private static float[][] AMBIENT_COLOR = {{0.7f, 0.7f, 0.7f},
                                              {0.7f, 0.7f, 0.7f},
                                              {0.7f, 0.7f, 0.7f}};

    /**
     * Position of the point lights
     */
    private static float[][] pointLightPositions = {{10, 5, 10},
                                                    {10, 10, -10},
                                                    {-10, 10, 10}};

    /**
     * Three point light nodes
     */
    private OGLLightNodeType[] pointLightNodes;

    /**
     * The scene root, parent for the nav info and viewpoint nodes
     */
    protected VRMLWorldRootNodeType root;

    /**
     * The frame state manager
     */
    protected FrameStateManager fsm;

    /**
     * Synchronization flag
     */
    protected boolean configComplete;

    /**
     * Flag used in the end of frame listener, indicating that the new nodes may
     * be added to the scene
     */
    protected boolean addNodes;

    public ConfigureThreePointLights() {
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    @Override
    public void allEventsComplete() {
        if (addNodes) {
            root.addChild(pointLightNodes[0]);
            root.addChild(pointLightNodes[1]);
            root.addChild(pointLightNodes[2]);

            addNodes = false;
            fsm.addEndOfThisFrameListener(this);

        } else {
            synchronized (this) {
                configComplete = true;
                notify();
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * This function adds three-point lights to a scene.
     *
     * @author Sang Park
     * @param construct
     */
    public void addLights(OGLConstruct construct) {
        fsm = construct.getFrameStateManager();

        VRMLScene x3dScene = construct.getBrowserCore().getScene();
        VRMLNodeType node = (VRMLNodeType) (x3dScene.getDEFNodes().get(DEFAULT_VIEWPOINT));

        float[] camScaledPosition = new float[]{0, 0, 0};
        float[] scenePosition;

        // Get the layers within the display
        DisplayCollection displayManager = construct.getDisplayCollection();
        Layer[] layers = new Layer[displayManager.numLayers()];
        displayManager.getLayers(layers);
        SimpleScene currentScene;
        SimpleViewport viewport;
        SimpleLayer layer;

        scenePosition = new float[]{0, 0, 0};

        for (Layer layer1 : layers) {
            if (!(layer1 instanceof SimpleLayer)) {
                continue;
            }
            layer = (SimpleLayer) layer1;
            if (!(layer.getViewport() instanceof SimpleViewport)) {
                continue;
            }
            viewport = (SimpleViewport) layer.getViewport();
            if (!(viewport.getScene() instanceof SimpleScene)) {
                continue;
            }
            currentScene = viewport.getScene();
            Group renderedGeom = currentScene.getRenderedGeometry();
            BoundingVolume bounds = renderedGeom.getBounds();
            if (bounds instanceof BoundingBox) {
                float[] max = new float[3];
                float[] min = new float[3];
                bounds.getExtents(min, max);

                scenePosition[0] = (max[0] - min[0]) / 2.0f;
                scenePosition[1] = (max[1] - min[1]) / 2.0f;
                scenePosition[2] = (max[2] - min[2]) / 2.0f;

                float[] camPosition;

                // If viewpoint position is defined by the user
                if ((node != null) && (node instanceof VRMLViewpointNodeType)) {

                    VRMLViewpointNodeType vp = (VRMLViewpointNodeType) node;
                    int position_index = vp.getFieldIndex("position");
                    VRMLFieldData data = vp.getFieldValue(position_index);

                    camPosition = data.floatArrayValues;
                } // Or else if not defined
                else {

                    camPosition = new float[]{scenePosition[0],
                        (max[1] - min[1]) * 0.55f,
                        max[2]};
                }

                Vector3f dirToCam
                        = new Vector3f(camPosition[0] - scenePosition[0],
                                camPosition[1] - scenePosition[1],
                                camPosition[2] - scenePosition[2]);
                float distance = dirToCam.length();

                // We want to set the key light position further behind from the
                // camera position so distance is scaled by some constant value
                distance *= 2.5f;

                dirToCam.normalize();
                dirToCam.scale(distance);

                camScaledPosition[0] = camPosition[0] + dirToCam.x;
                camScaledPosition[1] = camPosition[1] + dirToCam.y;
                camScaledPosition[2] = camPosition[2] + dirToCam.z;

                break;
            }
        }

        Vector3f midPoint
                = new Vector3f((1.0f - 0.82f) * camScaledPosition[0] + 0.82f * scenePosition[0],
                        (1.0f - 0.82f) * camScaledPosition[1] + 0.82f * scenePosition[1],
                        (1.0f - 0.82f) * camScaledPosition[2] + 0.82f * scenePosition[2]);

        pointLightPositions[KEY_LIGHT][0] = midPoint.x;
        pointLightPositions[KEY_LIGHT][1] = midPoint.y;
        pointLightPositions[KEY_LIGHT][2] = midPoint.z;

        Vector3f lookVec = new Vector3f(scenePosition[0] - midPoint.x,
                scenePosition[1] - midPoint.y,
                scenePosition[2] - midPoint.z);

        Vector3f lookVecN = new Vector3f(lookVec);
        lookVecN.normalize();

        Vector3f sideVec = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f upVec = new Vector3f();
        upVec.cross(lookVecN, sideVec);

        Matrix3f rotMat = new Matrix3f();
        rotMat.set(new AxisAngle4f(upVec, -ROT_RADIANS));

        Vector3f invLookVec
                = new Vector3f(-lookVec.x,
                        -lookVec.y,
                        -lookVec.z);

        Vector3f rotatedPos = new Vector3f();
        rotatedPos.x = rotMat.m00 * invLookVec.x
                + rotMat.m01 * invLookVec.y
                + rotMat.m02 * invLookVec.z
                + midPoint.x;
        rotatedPos.y = rotMat.m10 * invLookVec.x
                + rotMat.m11 * invLookVec.y
                + rotMat.m12 * invLookVec.z
                + midPoint.y;
        rotatedPos.z = rotMat.m20 * invLookVec.x
                + rotMat.m21 * invLookVec.y
                + rotMat.m22 * invLookVec.z
                + midPoint.z;

        pointLightPositions[FILL_LIGHT][0] = rotatedPos.x;
        pointLightPositions[FILL_LIGHT][1] = rotatedPos.y;
        pointLightPositions[FILL_LIGHT][2] = rotatedPos.z;

        pointLightPositions[BACK_LIGHT][0] = midPoint.x + invLookVec.x;
        pointLightPositions[BACK_LIGHT][1] = midPoint.y + invLookVec.y;
        pointLightPositions[BACK_LIGHT][2] = midPoint.z + invLookVec.z;

        // Add lights regardless of setting nice positions
        pointLightNodes = new OGLLightNodeType[NUM_LIGHTS];

        VRMLScene scene = construct.getBrowserCore().getScene();
        VRMLNodeFactory factory = scene.getNodeFactory();
        factory.addComponent("Lighting", 2);

        // TODO: Need to upgrade to 3.1 to use global
        pointLightNodes[0] = (OGLLightNodeType) factory.createVRMLNode(
                "PointLight", false);
        pointLightNodes[1] = (OGLLightNodeType) factory.createVRMLNode(
                "PointLight", false);
        pointLightNodes[2] = (OGLLightNodeType) factory.createVRMLNode(
                "PointLight", false);

        /*
		pointLightNodes[KEY_LIGHT].setColor(lightColor[KEY_LIGHT]);
		pointLightNodes[KEY_LIGHT].setColor(lightColor[KEY_LIGHT]);
		pointLightNodes[BACK_LIGHT].setColor(lightColor[BACK_LIGHT]);

		int idx = ((VRMLNode)pointLightNodes[0]).getFieldIndex("location");

		((VRMLNodeType)pointLightNodes[0]).setValue(idx, pointLightPositions[KEY_LIGHT], 3);
		((VRMLNodeType)pointLightNodes[1]).setValue(idx, pointLightPositions[FILL_LIGHT], 3);
		((VRMLNodeType)pointLightNodes[2]).setValue(idx, pointLightPositions[BACK_LIGHT], 3);

		pointLightNodes[KEY_LIGHT].setGlobal(true);
		pointLightNodes[FILL_LIGHT].setGlobal(true);
		pointLightNodes[BACK_LIGHT].setGlobal(true);
         */
        PointLight[] lights = {(PointLight) pointLightNodes[KEY_LIGHT].getLight(),
            (PointLight) pointLightNodes[FILL_LIGHT].getLight(),
            (PointLight) pointLightNodes[BACK_LIGHT].getLight()};

        lights[0].setDiffuseColor(DIFFUSE_COLOR[KEY_LIGHT]);
        lights[0].setAmbientColor(AMBIENT_COLOR[KEY_LIGHT]);
        lights[0].setSpecularColor(SPECULAR_COLOR[KEY_LIGHT]);

        lights[1].setDiffuseColor(DIFFUSE_COLOR[FILL_LIGHT]);
        lights[1].setAmbientColor(AMBIENT_COLOR[FILL_LIGHT]);
        lights[1].setSpecularColor(SPECULAR_COLOR[FILL_LIGHT]);

        lights[2].setDiffuseColor(DIFFUSE_COLOR[BACK_LIGHT]);
        lights[2].setAmbientColor(AMBIENT_COLOR[BACK_LIGHT]);
        lights[2].setSpecularColor(SPECULAR_COLOR[BACK_LIGHT]);

        lights[0].setPosition(pointLightPositions[KEY_LIGHT][0],
                pointLightPositions[KEY_LIGHT][1], pointLightPositions[KEY_LIGHT][2]);
        lights[1].setPosition(pointLightPositions[FILL_LIGHT][0],
                pointLightPositions[FILL_LIGHT][1], pointLightPositions[FILL_LIGHT][2]);
        lights[2].setPosition(pointLightPositions[BACK_LIGHT][0],
                pointLightPositions[BACK_LIGHT][1], pointLightPositions[BACK_LIGHT][2]);

        lights[KEY_LIGHT].setEnabled(true);
        lights[FILL_LIGHT].setEnabled(true);
        lights[BACK_LIGHT].setEnabled(true);

        lights[KEY_LIGHT].setGlobalOnly(true);
        lights[FILL_LIGHT].setGlobalOnly(true);
        lights[BACK_LIGHT].setGlobalOnly(true);

        root = (VRMLWorldRootNodeType) scene.getRootNode();

        addNodes = true;

        // wait for the new nodes to be added to the scene and bound before returning.
        synchronized (this) {
            fsm.addEndOfThisFrameListener(this);
            configComplete = false;
            while (!configComplete) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }
}
