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

package org.xj3d.loaders.ogl;

// External imports
import java.util.Collections;
import java.util.Map;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

// Local imports
import org.web3d.browser.*;

import org.j3d.util.ErrorReporter;

import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Representation of the browser core interfaces set up for a static scene
 * load.
 * <p>
 *
 * There is no runtime infrastructure here - all it does is present a basic,
 * functional core representation to allow scripts to behave somewhat
 * realistically.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
class StaticBrowserCore
    implements BrowserCore {

    /** Description string of this world */
    private String worldDescription;

    /** The Scene we are currently working with */
    private VRMLScene currentScene;

    /** The space that represents the complete world we are running */
    private VRMLExecutionSpace currentSpace;

    /** List of the current DEF mappings */
    private Map<String, VRMLNode> defMap;

    /** The clock instance used be the core */
    private VRMLClock vrmlClock;

    /**
     * Construct a default, empty universe that contains no scenegraph. A flag
     * is provided so that you may give the code a hint as to how the branch
     * group will be used. The most common user of this code will be the Java3D
     * loaders, which may use this as a purely static geometry setup and does
     * not need any of the dynamic behaviours like bindable nodes, navigation
     * etc.
     *
     * @param staticOnly true if this is only used to load static geometry
     * @param eme The evaluator for the event model
     * @throws IllegalArgumentException The evaluator does not contain
     *    appropriate managers for Java3D handler
     */
    @SuppressWarnings("unchecked")
    public StaticBrowserCore() {
        defMap = Collections.EMPTY_MAP;
        vrmlClock = new StaticClock();
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCore
    //----------------------------------------------------------

    /**
     * Get the type of renderer that implements the browser core. The only
     * valid values returned are the constants in this interface.
     *
     * @return The renderer type
     */
    @Override
    public int getRendererType() {
        return Xj3DConstants.OPENGL_RENDERER;
    }

    /**
     * Get the ID string for this renderer.
     *
     * @return The String token for this renderer.
     */
    @Override
    public String getIDString() {
        return Xj3DConstants.OPENGL_ID;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        //errorReporter = ( reporter == null ) ?
        //    DefaultErrorReporter.getDefaultReporter( ) : reporter;
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    @Override
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    @Override
    public int getRenderingStyle() {
        return Xj3DConstants.RENDER_SHADED;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources taken up
     * by the 3D renderer. By default it will use all of them. The second
     * parameter is used to control whether this is a user-set hard minimum or
     * something set by the browser internals. User set values are always
     * treated as the minimum unless the browser internals set a value that is
     * a slower framerate than the user set. If the browser then sets a faster
     * framerate than the user set value, the user value is used instead.
     *
     * @param millis The minimum time in milliseconds.
     * @param userSet true if this is an end-user set minimum
     */
    @Override
    public void setMinimumFrameInterval(int millis, boolean userSet) {
    }

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     */
    @Override
    public int getMinimumFrameInterval() {
        return 0;
    }

    /**
     * Get the clock instance in use by the core. We need this for when
     * new nodes are added to the scene to make sure they are all appropriately
     * configured.
     *
     * @return The clock used by the browser core
     */
    @Override
    public VRMLClock getVRMLClock() {
        return null;
    }

    /**
     * Get the mapping of DEF names to the node instances that they represent.
     * Primarily used for the EAI functionality. The map instance changes each
     * time a new world is loaded so will need to be re-fetched. If no mappings
     * are available (e.g. scripting replaceWorld() type call) then the map will
     * be empty.
     *
     * @return The current mapping of DEF names to node instances
     */
    @Override
    public Map<String,VRMLNode> getDEFMappings() {
        return defMap;
    }

    /**
     * Request notification of profiling information.
     *
     * @param l The listener
     */
    @Override
    public void addProfilingListener(ProfilingListener l) {
        System.out.println("Profiling not supported in StaticBrowserCore");
    }

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    @Override
    public void removeProfilingListener(ProfilingListener l) {
        System.out.println("Profiling not supported in StaticBrowserCore");
    }

    /**
     * Convenience method to ask for the execution space that the world is
     * currently operating in. Sometimes this is not known, particularly if
     * the end user has called a loadURL type function that is asynchronous.
     * This will change each time a new scene is loaded.
     *
     * @return The current world execution space.
     */
    @Override
    public VRMLExecutionSpace getWorldExecutionSpace() {
        return currentSpace;
    }

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    @Override
    public String getDescription() {
        return worldDescription;
    }

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     */
    @Override
    public void setDescription(String desc) {
        worldDescription = desc;
    }

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    @Override
    public float getCurrentSpeed() {
        return 0.0f;
    }

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    @Override
    public float getCurrentFrameRate() {
        return 0;
    }

    /**
     * Set the last frame render time used for FPS calculations.  Only the
     * per frame manager should call this.
     *
     * @param long The time it took to render the last frame in milliseconds.
     */
    @Override
    public void setLastRenderTime(long lastTime) {
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    @Override
    public void setEventModelStatusListener(EventModelStatusListener l) {
    }

    /**
     * Add an observer for a specific node type. A single instance may be
     * registered for more than one type. Each type registered will result in
     * a separate call per frame - one per type. If the observer is currently
     * added for this type ID, the request is ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to add
     */
    @Override
    public void addNodeObserver(int nodeType, NodeObserver obs) {
    }

    /**
     * Remove the given node observer instance for the specific node type. It
     * will not be removed for any other requested node types. If the instance
     * is not registered for the given node type ID, the request will be
     * silently ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to remove
     */
    @Override
    public void removeNodeObserver(int nodeType, NodeObserver obs) {
    }

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addNavigationStateListener(NavigationStateListener l) {
    }

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeNavigationStateListener(NavigationStateListener l) {
    }

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addSensorStatusListener(SensorStatusListener l) {
    }

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeSensorStatusListener(SensorStatusListener l) {
    }

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addViewpointStatusListener(ViewpointStatusListener l) {
    }

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeViewpointStatusListener(ViewpointStatusListener l) {
    }

    /**
     * Notify the core that it can dispose all resources.  The core cannot be used for
     * rendering after that.
     */
    @Override
    public void dispose() {
    }

    /**
     * Get the fully qualified URL of the currently loaded world. This returns
     * the entire URL including any possible arguments that might be associated
     * with a CGI call or similar mechanism. If the initial world is replaced
     * with <code>loadURL</code> then the string will reflect the new URL. If
     * <code>replaceWorld</code> is called then the URL still represents the
     * original world.
     *
     * @return A string of the URL or null if not supported.
     */
    @Override
    public String getWorldURL() {
        String ret_val = null;

        if(currentScene != null)
            ret_val = currentScene.getWorldRootURL();

        return ret_val;
    }

    /**
     * Set the scene to use within this universe. If null, this will clear this
     * scene and de-register all listeners. The View will be detached from the
     * ViewPlatform and therefore the canvas will go blank.
     *
     * @param viewpoint The viewpoint.description to bind to or null for default
     */
    @Override
    public void setScene(VRMLScene scene, String viewpoint) {
        currentScene = scene;

        defMap = currentScene.getDEFNodes();
        VRMLNodeType vrml_root = (VRMLNodeType)currentScene.getRootNode();
        currentSpace = (VRMLExecutionSpace)vrml_root;
        vrmlClock.resetTimeZero();
    }

    /**
     * Request that this viewpoint object is bound at the start of the next
     * frame. This method should only be called by external users such as
     * UI toolkits etc that need to synchronize the viewpoint change with
     * rendering loop, but are not able to synchronize themselves because they
     * exist on a different thread that cannot block.
     *
     * @param vp The new viewpoint instance to bind to
     */
    @Override
    public void changeViewpoint(VRMLViewpointNodeType vp) {
    }

    /**
     * Add a listener for browser core events. These events are used to notify
     * all listeners of internal structure changes, such as the browser
     * starting and stopping. A listener can only be added once. Duplicate
     * requests are ignored.
     *
     * @param l The listener to add
     */
    @Override
    public void addCoreListener(BrowserCoreListener l) {
        // ignored
    }

    /**
     * Remove a browser core listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    @Override
    public void removeCoreListener(BrowserCoreListener l) {
        // ignored
    }

    /**
     * Send to the core listeners the error message that a URL failed to load
     * for some reason. This is for the EAI/ESAI spec conformance.
     *
     * @param msg The message to send
     */
    @Override
    public void sendURLFailEvent(String msg) {
        // ignored
    }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transition be animated.  Defaults to FALSE.
     */
    @Override
    public void setNavigationMode(String mode) {
        // Ignored
     }

    /**
     * Get the user's location and orientation.  This will use the viewpoint
     * bound in the active layer.
     *
     * @param pos The current user position
     * @param ori The current user orientation
     */
    public void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        // ignored
    }

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transition be animated.  Defaults to FALSE.
     */
    @Override
    public void fitToWorld(boolean animated) {
        // ignored
    }

    /**
     * Sync UI updates with the Application thread.  This method alls the core
     * to push work off to the app thread.
     */
    @Override
    public void syncUIUpdates() {
        // ignored
    }


    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     */
    @Override
    public void captureScreenOnce(ScreenCaptureListener listener) {
        // ignored
    }

    /**
     * Capture the screen on each render until told to stop.
     *
     * @param listener Listener for capture results
     */
    @Override
    public void captureScreenStart(ScreenCaptureListener listener) {

        // ignored
    }

    /**
     * Stop capturing the screen on each render.
     */
    @Override
    public void captureScreenEnd() {
        // ignored
    }
}
