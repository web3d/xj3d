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

package org.web3d.browser;

// External imports
import java.util.Map;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

/**
 * Abstract representation of the core requirements of a browser implementation
 * regardless of the renderer used.
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public interface BrowserCore {

    /**
     * Get the type of renderer that implements the browser core. The only
     * valid values returned are the constants in this interface. The constants
     * returned are defined in {@link Xj3DConstants}.
     *
     * @return The renderer type
     */
    int getRendererType();

    /**
     * Get the ID string for this renderer. The constants returned are
     * defined in {@link Xj3DConstants}.
     *
     * @return The String token for this renderer.
     */
    String getIDString();

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    void setErrorReporter(ErrorReporter reporter);

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
    void setMinimumFrameInterval(int millis, boolean userSet);

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The current cycle interval time in milliseconds
     */
    int getMinimumFrameInterval();

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    void setRenderingStyle(int style)
        throws IllegalArgumentException;

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants from org.xj3d.sai.Xj3DBrowser
     */
    int getRenderingStyle();

    /**
     * Get the clock instance in use by the core. We need this for when
     * new nodes are added to the scene to make sure they are all appropriately
     * configured.
     *
     * @return The clock used by the browser core
     */
    VRMLClock getVRMLClock();

    /**
     * Set the scene to use within this universe with the specifically named
     * viewpoint. If null, this will clear this scene and de-register all listeners.
     *
     * @param scene The new scene to load, or null
     * @param viewpoint The viewpoint.description to bind to or null for default
     */
    void setScene(VRMLScene scene, String viewpoint);

    /**
     * Get the mapping of DEF names to the node instances that they represent.
     * Primarily used for the EAI functionality. The map instance changes each
     * time a new world is loaded so will need to be re-fetched. If no mappings
     * are available (eg scripting replaceWorld() type call) then the map will
     * be empty.
     *
     * @return The current mapping of DEF names to node instances
     */
    Map<String, VRMLNode> getDEFMappings();

    /**
     * Convenience method to ask for the execution space that the world is
     * currently operating in. Sometimes this is not known, particularly if
     * the end user has called a loadURL type function that is asynchronous.
     * This will change each time a new scene is loaded.
     *
     * @return The current world execution space.
     */
    VRMLExecutionSpace getWorldExecutionSpace();

    /**
     * Get the description string currently used by the world. Returns null if
     * not set or supported.
     *
     * @return The current description string or null
     */
    String getDescription();

    /**
     * Set the description of the current world. If the world is operating as
     * part of a web browser then it shall attempt to set the title of the
     * window. If the browser is from a component then the result is dependent
     * on the implementation
     *
     * @param desc The description string to set.
     */
    void setDescription(String desc);

    /**
     * Get the current velocity of the bound viewpoint in meters per second.
     * The velocity is defined in terms of the world values, not the local
     * coordinate system of the viewpoint.
     *
     * @return The velocity in m/s or 0.0 if not supported
     */
    float getCurrentSpeed();

    /**
     * Get the current frame rate of the browser in frames per second.
     *
     * @return The current frame rate or 0.0 if not supported
     */
    float getCurrentFrameRate();

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
    String getWorldURL();

    /**
     * Send to the core listeners the error message that a URL failed to load
     * for some reason. This is for the EAI/ESAI spec conformance.
     *
     * @param msg The message to send
     */
    void sendURLFailEvent(String msg);

    /**
     * Set the desired navigation mode. The mode string is one of the
     * spec-defined strings for the NavigationInfo node in the VRML/X3D
     * specification.
     *
     * @param mode The requested mode.
     */
    void setNavigationMode(String mode);

    /**
     * Move the user's location to see the entire world.  Change the users
     * orientation to look at the center of the world.
     *
     * @param animated Should the transition be animated.  Defaults to FALSE.
     */
    void fitToWorld(boolean animated);

    /**
     * Request that this viewpoint object is bound at the start of the next
     * frame. This method should only be called by external users such as
     * UI toolkits etc that need to synchronize the viewpoint change with
     * rendering loop, but are not able to synchronize themselves because they
     * exist on a different thread that cannot block.
     *
     * @param vp The new viewpoint instance to bind to
     */
    void changeViewpoint(VRMLViewpointNodeType vp);

    /**
     * Set the last frame render time used for FPS calculations.  Only the
     * per frame manger should call this.
     *
     * @param lastTime The time it took to render the last frame in milliseconds.
     */
    void setLastRenderTime(long lastTime);

    /**
     * Add a listener for browser core events. These events are used to notify
     * all listeners of internal structure changes, such as the browser
     * starting and stopping. A listener can only be added once. Duplicate
     * requests are ignored.
     *
     * @param l The listener to add
     */
    void addCoreListener(BrowserCoreListener l);

    /**
     * Remove a browser core listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    void removeCoreListener(BrowserCoreListener l);

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    void setEventModelStatusListener(EventModelStatusListener l);

    /**
     * Add a listener for navigation state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    void addNavigationStateListener(NavigationStateListener l);

    /**
     * Remove a navigation state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    void removeNavigationStateListener(NavigationStateListener l);

    /**
     * Add a listener for sensor state changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    void addSensorStatusListener(SensorStatusListener l);

    /**
     * Remove a sensor state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    void removeSensorStatusListener(SensorStatusListener l);

    /**
     * Add a listener for viewpoint status changes.  A listener can only be added once.
     * Duplicate requests are ignored.
     *
     * @param l The listener to add
     */
    void addViewpointStatusListener(ViewpointStatusListener l);

    /**
     * Remove a viewpoint state listener. If the reference is null or not known,
     * the request is silently ignored.
     *
     * @param l The listener to remove
     */
    void removeViewpointStatusListener(ViewpointStatusListener l);

    /**
     * Add an observer for a specific node type. A single instance may be
     * registered for more than one type. Each type registered will result in
     * a separate call per frame - one per type. If the observer is currently
     * added for this type ID, the request is ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to add
     */
    void addNodeObserver(int nodeType, NodeObserver obs);

    /**
     * Remove the given node observer instance for the specific node type. It
     * will not be removed for any other requested node types. If the instance
     * is not registered for the given node type ID, the request will be
     * silently ignored.
     *
     * @param nodeType The type identifier of the node being observed
     * @param obs The observer instance to remove
     */
    void removeNodeObserver(int nodeType, NodeObserver obs);

    /**
     * Notify the core that it can dispose all resources.  The core cannot be used for
     * rendering after that.
     */
    void dispose();

    /**
     * Sync UI updates with the Application thread.  This method calls the core
     * to push work off to the app thread.
     */
    void syncUIUpdates();

    /**
     * Request notification of profiling information.
     *
     * @param l The listener
     */
    void addProfilingListener(ProfilingListener l);

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    void removeProfilingListener(ProfilingListener l);

    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     */
    void captureScreenOnce(ScreenCaptureListener listener);

    /**
     * Capture the screen on each render until told to stop.
     *
     * @param listener Listener for capture results
     */
    void captureScreenStart(ScreenCaptureListener listener);

    /**
     * Stop capturing the screen on each render.
     */
    void captureScreenEnd();
}
