/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.input;

// External imports
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import javax.vecmath.*;

import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.picking.TransformPickTarget;
import org.j3d.aviatrix3d.rendering.BoundingVolume;

import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.vrml.renderer.common.input.BaseLayerSensorManager;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.nodes.*;

import org.xj3d.core.eventmodel.*;

/**
 * Default implementation of the LayerSensorManager interface for the OpenGL
 * renderer.
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public class DefaultLayerSensorManager extends BaseLayerSensorManager {

    /** Property defining a user input handler implementation */
    public static final String USER_INPUT_HANDLER_PROP =
        "org.xj3d.user.input.handler.class";

    /** Default class for the user input handler */
    private static final String DEFAULT_USER_INPUT_HANDLER =
    	"org.web3d.vrml.renderer.ogl.input.DefaultUserInputHandler";

    /** Attempt to load an invalid user input handler class */
    private static final String INVALID_USER_INPUT_HANDLER_MSG =
		"The class specified for the user input handler is not a " +
		"valid instance of UserInputHandler";

    /** Attempt to load an invalid user input handler class */
    private static final String SET_DEFAULT_USER_INPUT_HANDLER_MSG =
		"Configuring the default UserInputHandler";

    /** The -z axis to get for the orientation */
    private static final Vector3f Z_AXIS = new Vector3f(0, 0, -1);

    /** The bound viewpoint used last frame */
    private OGLViewpointNodeType currentViewpoint;

    /** The bound background used last frame */
    private OGLBackgroundNodeType currentBackground;

    /** The bound fog used last frame */
    private VRMLFogNodeType currentFog;

    /** The NavigationInfo node used last frame */
    private VRMLNavigationInfoNodeType currentNavInfo;

    /** Matrix for fetching vp position. */
    private Matrix4f vpMatrix;

    /** Local viewpoint position matrix */
    private Matrix4f localMatrix;

    /** Scratch var for the VP position from the transform */
    private Vector3f t3dPosition;

    /** Scratch var for getting the position of the VP */
    private Point3f position;

    /** Scratch var for getting the orientation of the VP */
    private Vector3f orientation;

    /** Scratch var for getting the orientation of the VP */
    private AxisAngle4f oriAxisAngle;

    /** List of available sensor handlers */
    private VisibilityManager visibilityHandler;

    /** Global effects manager for viewpoints, fogs etc */
    private OGLGlobalEffectsHandler globalEffects;

    /** List of available view-dependent node handlers */
    private AreaManager areaHandler;

    /** Working list of texture objects for the background updates */
    private Texture2D[] tmpTextures;

    /** Working list of texture objects for the background updates */
    private boolean[] tmpTextureFlags;

    /** Working list to get the background colors */
    private float[] tmpColor;

    /** Working list to get the background angles */
    private float[] tmpAngle;

    /** List for collecting path information to calculate inverse matrices */
    private List<Object> pathList;

    /** Array for fetching lists of shared parent nodes */
    private Node[] pathNodes;

    /** MatrixUtils for gc free inversion */
    private MatrixUtils matrixUtils;

    /** The world root */
    private Group worldRoot;

    /** The View that we use for everything */
    private ViewEnvironment viewEnvironment;

    /** A scratch vector for position updates */
    private Vector3f posVector;

    /** Scratch var to hold bounds extents */
    private float[] boundsMin;

    /** Scratch var to hold bounds extents */
    private float[] boundsMax;

    /** The current near clip */
    private float nearClip;

    /** The current far clip */
    private float farClip;

    /**
     * Create a new default instance of the manager. It will only register a
     * handler for TimeSensors. Anything other than that will require the end
     * user code to register an appropriate manager.
     */
    public DefaultLayerSensorManager() {
        super();

        posVector = new Vector3f();
        boundsMin = new float[3];
        boundsMax = new float[3];

        matrixUtils = new MatrixUtils();
        vpMatrix = new Matrix4f();
        localMatrix = new Matrix4f();
        t3dPosition = new Vector3f();
        position = new Point3f();
        orientation = new Vector3f();
        oriAxisAngle = new AxisAngle4f();

        ///////////////////////////////////////////////////////////////
        // allow for a user defined input handler to be instantiated
        String prop = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            // privileged code for getting class name property
            String ret_val = System.getProperty(
                    USER_INPUT_HANDLER_PROP,
                    DEFAULT_USER_INPUT_HANDLER);
            return(ret_val);
        });

        // instantiate the class
        Object obj = loadClass(prop, false, INVALID_USER_INPUT_HANDLER_MSG);
        if ((obj != null) && (obj instanceof OGLUserInputHandler)) {
                inputHandler = (UserInputHandler)obj;
        } else {
                // if something has gone wrong, fall back to the default
                errorReporter.warningReport(SET_DEFAULT_USER_INPUT_HANDLER_MSG, null);
                inputHandler = new DefaultUserInputHandler();
        }
        ///////////////////////////////////////////////////////////////

        visibilityHandler = new VisibilityManager();
        areaHandler = new AreaManager();

        pathList = new ArrayList<>();
        pathNodes = new Node[20];

        tmpTextures = new Texture2D[6];
        tmpTextureFlags = new boolean[6];
        tmpColor = new float[24];
        tmpAngle = new float[7];
    }

    //-------------------------------------------------------------
    // Methods defined by LayerSensorManager
    //-------------------------------------------------------------

    /**
     * Set the global effects handler for this sensor manager.
     *
     * @param handler The new handler instance to use
     */
    public void setGlobalEffectsHandler(OGLGlobalEffectsHandler handler) {
        globalEffects = handler;
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    public void setWorldRoot(Group root) {
        worldRoot = root;

        areaHandler.setWorldRoot(root);
        visibilityHandler.setWorldRoot(root);
    }

    /**
     * Set the container for the view environment data. Used to process the
     * view frustum when doing visibility sensor handling.
     *
     * @param data The current env data to use
     */
    public void setViewEnvironment(ViewEnvironment data) {
        viewEnvironment = data;
        visibilityHandler.setViewEnvironment(data);
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    public void setWorldScale(float scale) {
        worldScale = scale;
        invWorldScale = 1f / scale;

        if (currentViewpoint != null)
            currentViewpoint.setWorldScale(scale);

        visibilityHandler.setWorldScale(scale);
    }

    @Override
    public boolean processUserInput(long time) {
        if(!initialised)
            initialise();

        VRMLNavigationInfoNodeType nav =
            (VRMLNavigationInfoNodeType)navInfoStack.getBoundNode();
        if(nav != currentNavInfo) {
            inputHandler.setNavigationInfo(nav);
            currentNavInfo = nav;
        }

        updateClipPlanes();

        // Fetch the view global position and orientation for use in the
        // sensor evaluation.
        OGLViewpointNodeType vp =
            (OGLViewpointNodeType)viewStack.getBoundNode();

        if(vp != currentViewpoint) {
            currentViewpoint = vp;
            currentViewpoint.setWorldScale(worldScale);
        }

        OGLBackgroundNodeType bg =
            (OGLBackgroundNodeType)backgroundStack.getBoundNode();

        if(bg != currentBackground) {
            Texture2D[] textures = bg.getBackgroundTextures();
            for(int i = 0; i < 6; i++)
                tmpTextureFlags[i] = true;

            globalEffects.updateBackgroundTextures(textures,
                                                   tmpTextureFlags);
            // Trick the system into updating
            backgroundSkyChanged = true;
            backgroundGroundChanged = true;

            updateBackgroundSphere(bg);

            if(currentBackground != null)
                currentBackground.removeNodeListener(backgroundListener);

            bg.addNodeListener(backgroundListener);

            currentBackground = bg;
        } else {
            // Has anything changed this frame?
            if(bg.getChangedTextures(tmpTextures, tmpTextureFlags))
                globalEffects.updateBackgroundTextures(tmpTextures,
                                                       tmpTextureFlags);

            updateBackgroundSphere(currentBackground);
        }

        VRMLFogNodeType fog = (VRMLFogNodeType)fogStack.getBoundNode();

        float dist = fog.getVisibilityRange();
        fog.getColor(tmpColor);
        int type = fog.getFogType();

        if(fog != currentFog) {
            globalEffects.enableFog(type);
            globalEffects.setFogDetails(dist,
                                        tmpColor[0],
                                        tmpColor[1],
                                        tmpColor[2]);

            if(currentFog != null)
                currentFog.removeNodeListener(fogListener);

            fog.addNodeListener(fogListener);

            currentFog = fog;
        } else {
            // Has anything changed this frame?
            if(fogTypeChanged)
                globalEffects.enableFog(type);

            if(fogDetailsChanged)
                globalEffects.setFogDetails(dist,
                                            tmpColor[0],
                                            tmpColor[1],
                                            tmpColor[2]);
        }

        clearChangeFlags();

        TransformGroup transform = currentViewpoint.getPlatformGroup();

        getLocalToVworld(transform, vpMatrix, true);

        vpMatrix.transform(Z_AXIS, orientation);

        vpMatrix.get(t3dPosition);
        position.set(t3dPosition);
        oriAxisAngle.set(vpMatrix);

        if(numVisibilityListeners > 0)
            visibilityHandler.processFrame(position, oriAxisAngle, vpMatrix);

        float vis_limit = nav.getVisibilityLimit();

        if (numAreaListeners > 0) {
            matrixUtils.inverse(vpMatrix, vpMatrix);
            areaHandler.processFrame(position, orientation, vpMatrix, vis_limit);
        }

        /////////////////////////////////////////////////////////////////////////
        if (useOriginManager) {
            if (!originListeners.isEmpty()) {
                if (originManager.updateViewPosition(t3dPosition)) {
                    inputHandler.originChanged();
                    originListeners.forEach((ol) -> {
                        ol.originChanged();
                    });
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////

        // Now that the viewpoint is complete, reuse the matrix for the
        // background position information.
        Group node = (Group)currentBackground.getSceneGraphObject();

        getLocalToVworld(node, vpMatrix, false);

        if(globalEffects != null)
            globalEffects.setBackgroundMatrix(vpMatrix);

        node = (Group)((OGLVRMLNode)currentFog).getSceneGraphObject();

        getLocalToVworld(node, vpMatrix, false);

        if(globalEffects != null)
            globalEffects.setFogMatrix(vpMatrix);

// LAYERS:
// Need to fix this so that it returns the appropriate state for touch sensors or navigation.

        if(pickManager != null)
            pickManager.processPickSensors(time);

        return true;
    }

    @Override
    public void updateViewMatrix() {
        if(globalEffects == null)
            return;

        if(currentViewpoint != null) {
            TransformGroup transform = currentViewpoint.getPlatformGroup();
            getLocalToVworld(transform, vpMatrix, true);
            globalEffects.setViewMatrix(vpMatrix);
        }
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    @Override
    public void clear() {
        super.clear();

        areaHandler.clear();
        visibilityHandler.clear();

        pathList.clear();
        for (int i = 0; i < pathNodes.length; i++){
            pathNodes[i] = null;
        }

        currentFog = null;
        currentNavInfo = null;
        currentViewpoint = null;
        currentBackground = null;

        worldRoot = null;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to update the background colours used in the sky/ground
     * sphere.
     *
     * @param bg The background node to use as the basis
     */
    private void updateBackgroundSphere(VRMLBackgroundNodeType bg) {
        globalEffects.updateBackgroundTransparency(bg.getTransparency());

        if(backgroundGroundChanged) {
            int num_col = bg.getNumGroundColors();

            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                bg.getGroundValues(tmpColor, tmpAngle);

            globalEffects.updateBackgroundGround(tmpColor, tmpAngle, num_col);
        }

        if(backgroundSkyChanged) {
            int num_col = bg.getNumSkyColors();
            if(num_col * 3 > tmpColor.length) {
                tmpColor = new float[num_col * 3];
                tmpAngle = new float[num_col - 1];
            }

            if(num_col != 0)
                bg.getSkyValues(tmpColor, tmpAngle);

            globalEffects.updateBackgroundSky(tmpColor, tmpAngle, num_col);
        }
    }

    /**
     * Convenience method to walk to the root of the scene and calculate the
     * root to virtual world coordinate location of the given node. If a
     * sharedGroup is found, then take the first parent listed always.
     *
     * @param terminal The end node to calculate from
     * @param mat The matrix to put the final result into
     */
    private void getLocalToVworld(Node terminal, Matrix4f mat, boolean subst) {

        Node parent = terminal.getParent();
        OGLUserData udata;

        if(terminal instanceof TransformGroup) {
            if (subst) {
                udata = (OGLUserData) terminal.getUserData();
                if (udata != null) {
                    pathList.add(udata.owner);
                }
            } else {
                pathList.add(terminal);
            }
        }

        while(parent != null) {
            if(parent instanceof SharedGroup) {
                SharedGroup sg = (SharedGroup)parent;

                int num_parents = sg.numParents();

                if(num_parents == 0)
                    break;
                else if(num_parents > pathNodes.length)
                    pathNodes = new Node[num_parents];

                sg.getParents(pathNodes);
                parent = pathNodes[0];
            } else if(parent instanceof SharedNode) {
                SharedNode sg = (SharedNode)parent;

                int num_parents = sg.numParents();

                if(num_parents == 0)
                    break;
                else if(num_parents > pathNodes.length)
                    pathNodes = new Node[num_parents];

                sg.getParents(pathNodes);
                parent = pathNodes[0];
            } else {
                if(parent instanceof TransformGroup) {
                    ((TransformPickTarget)parent).getTransform(localMatrix);

                    if(subst) {
                        Object o = parent.getUserData();
                        if(o instanceof OGLUserData) {
                            udata = (OGLUserData)o;
                            pathList.add(udata.owner);
                        }

                    } else {
                        pathList.add(parent);
                    }
                }
                parent = parent.getParent();
            }
        }

        int num_nodes = pathList.size();
        mat.setIdentity();

        // use vwTransform for fetching the tx. It is only a temp var anyway
        Object n;
        for(int i = num_nodes - 1; i >= 0; i--) {
            n = pathList.get(i);
            if (n instanceof OGLTransformNodeType) {
                OGLTransformNodeType tg = (OGLTransformNodeType) n;
                Matrix4f tx = tg.getTransform();
                mat.mul(tx);
            } else {
                TransformGroup tg = (TransformGroup) n;
                tg.getTransform(localMatrix);
                mat.mul(localMatrix);
            }
        }

        pathList.clear();
    }

    /**
     * Update the clip planes based on the current navigation info and user position.
     */
    private void updateClipPlanes() {
        // TODO: Should we move this code to ViewpointManager?

        if (viewEnvironment == null) {
            return;
        }

        inputHandler.getPosition(posVector);

        int VIS_MAX = 3_000;

        float[] avatar_size;
        float near;
        float far;
        float visibilityLimit;

        if (currentNavInfo != null) {
            visibilityLimit = currentNavInfo.getVisibilityLimit();
            avatar_size = currentNavInfo.getAvatarSize();
            near = avatar_size[0] / 2;
        } else {
            visibilityLimit = 0;
            near = 0.25f / 2;
        }

        if (near < 0.001)
            near = 0.001f;

        if (visibilityLimit == 0) {
            BoundingVolume bounds = worldRoot.getBounds();

            float max = getMaxExtent(bounds, posVector);

            // Extend far clip plane a little.  Not sure why we need to do this.
            max = max * 1.1f;

            if (max > Float.MAX_VALUE) {
                max = Float.MAX_VALUE;
            }

            // Allow large worlds to grow far clip till we fix autosize
            if (max > VIS_MAX)
                far = max;
            else
                far = VIS_MAX;

        } else {
            far = visibilityLimit;
        }


//far = 1e12f;
        if (near > far)
            far = near * 3_000;

        if ((Math.abs(nearClip - near) >= 0.001) || (Math.abs(farClip - far) > 0.1)) {
            viewEnvironment.setClipDistance(near, far);
            nearClip = near;
            farClip = far;
        }
    }

    /**
     * Get the maximum extents of a bounds object and a position.
     *
     * @param bounds The bounds
     * @param pos The position
     * @return The maximum length of the bounds
     */
    private float getMaxExtent(BoundingVolume bounds, Vector3f pos) {
/*
        // Calc distance to center + dist(size * 0.5)
        bounds.getCenter(boundsMin);

        // Distance to center
        double dc = 0;

        // Distance from center to boundary edge
        double dce = 0;

        float diffx;
        float diffy;
        float diffz;

        diffx = (boundsMin[0] - pos.x);
        diffy = (boundsMin[1] - pos.y);
        diffz = (boundsMin[2] - pos.z);
        dc = diffx * diffx + diffy * diffy + diffz * diffz;

        if (bounds instanceof BoundingBox) {
            ((BoundingBox)bounds).getSize(boundsMin);

            dce = boundsMin[0] * boundsMin[0] + boundsMin[1] * boundsMin[1] +
                  boundsMin[2] * boundsMin[2];
        } else {
            System.out.println("Non bounding box not supported in ApplicationFrameManager");
            System.out.println(bounds);
            return (float)Math.sqrt(dc + dc);
        }

        return (float) Math.sqrt(dc + dce);

*/
        bounds.getExtents(boundsMin, boundsMax);
        float xrange = boundsMax[0] - boundsMin[0];
        float xrange2 = pos.x - boundsMin[0];
        float xrange3 = pos.x - boundsMax[0];

        float yrange = boundsMax[1] - boundsMin[1];
        float yrange2 = pos.y - boundsMin[1];
        float yrange3 = pos.y - boundsMax[1];

        float zrange = boundsMax[2] - boundsMin[2];
        float zrange2 = pos.z - boundsMin[2];
        float zrange3 = pos.z - boundsMax[2];

        double dist;
        double max;

        dist = xrange * xrange + yrange * yrange + zrange * zrange;

        max = dist;

        dist = xrange * xrange2 + yrange2 * yrange2 + zrange2 * zrange2;
        if (dist > max)
            max = dist;

        dist = xrange3 * xrange3 + yrange3 * yrange3 + zrange3 * zrange3;
        if (dist > max)
            max = dist;

        return (float) Math.sqrt(max);
    }

    /**
     * Load a factory class instance as needed. Separated out as a new method
     * because it will need to perform the privileged action of class loading,
     * which is not permitted under an applet.
     *
     * @param classname The fully qualified name of the class to load
     * @param required Is this class required for operation
     * @param msg The message to issue if its not found.
     * @return factory class instance
     */
    private Object loadClass(final String classname, final boolean required, final String msg) {
        Object ret_val = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Class<?> cls = Class.forName(classname);
                return(cls.getDeclaredConstructor().newInstance());
            } catch (Error err) {
                if (required) {
                    Exception ewrapper = new Exception(err.getMessage(), err);
                    errorReporter.errorReport(msg, ewrapper);
                } else {
                    errorReporter.warningReport(msg, null);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                if(required) {
                    errorReporter.errorReport(msg, e);
                } else {
                    errorReporter.warningReport(msg, null);
                }
            }
            return(null);
        });
        return(ret_val);
    }
}
