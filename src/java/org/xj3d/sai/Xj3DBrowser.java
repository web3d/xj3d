/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.InvalidBrowserException;
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * Extension Xj3D-specific browser methods.
 * <p>
 * Only external browsers are able to make use of this interface.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface Xj3DBrowser extends ExternalBrowser {

    /** The rendering style uses point mode */
    int RENDER_POINTS = 1;

    /** The rendering style uses wireframe mode */
    int RENDER_LINES = 2;

    /** The rendering style uses flat shading mode */
    int RENDER_FLAT = 3;

    /** The rendering style uses a generic shading model */
    int RENDER_SHADED = 4;

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param millis The minimum time in milliseconds.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void setMinimumFrameInterval(int millis)
        throws InvalidBrowserException;

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    int getMinimumFrameInterval()
        throws InvalidBrowserException;

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void setRenderingStyle(int style)
        throws IllegalArgumentException, InvalidBrowserException;

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    int getRenderingStyle()
        throws InvalidBrowserException;

    /**
     * Set the handler for error messages. This can be used to replace the
     * stock console. Passing a value of null removes the currently registered
     * reporter. Setting this will replace the current reporter with this
     * instance. If the current reporter is the default system console, then
     * the console will not receive any further messages.
     *
     * @param reporter The error reporter instance to use
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void setErrorReporter(Xj3DErrorReporter reporter)
        throws InvalidBrowserException;

    /**
     * Add a listener for status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void addStatusListener(Xj3DStatusListener l)
        throws InvalidBrowserException;

    /**
     * Remove a listener for status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void removeStatusListener(Xj3DStatusListener l)
        throws InvalidBrowserException;

    /**
     * Fetch the interface that allows an external application to implement
     * their own navigation user interface. This is guaranteed to be unique
     * per browser instance.
     *
     * @return An interface allowing end-user code to manipulate the
     *    navigation.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    Xj3DNavigationUIManager getNavigationManager()
        throws InvalidBrowserException;

    /**
     * Fetch the interface that allows an external application to implement
     * their own cursor user interface.
     *
     * @return An interface allowing end-user code to manipulate the
     *    cursor.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    Xj3DCursorUIManager getCursorManager();

    /**
     * Fetch the component-specific interface for managing a CAD scene. This
     * interface exposes CAD structures
     *
     * @return An interface allowing end-user code to manipulate the
     *    the CAD-specific structures in the scene.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    Xj3DCADView getCADView()
        throws InvalidBrowserException;

    /**
     * Set the interceptor for the anchor node handling. This can be used to
     * process the clicks on Anchor nodes and replace or supplement the
     * existing behaviour. See documentation of the listener for more specific
     * details on usage patterns.
     * <p>
     * Only one interceptor instance can be registered. Setting a new item will
     * replace an existing registered instance. Setting a null value will clear
     * the current set instance.
     *
     * @param l The interceptor instance to register or null to clear
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void setAnchorListener(Xj3DAnchorListener l)
        throws InvalidBrowserException;

    /**
     * Request that the browser capture the next <i>n</i> number of frames as
     * images. This will begin from the frame after this method is called. If
     * no screen capture listener is registered at the time this is called, the
     * request is ignored.
     *
     * @param n The number of frames to capture
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void captureFrames(int n)
        throws InvalidBrowserException;

    /**
     * Set the handler for processing screen captures. This can be used to
     * process images for saving to disc, processing or other task.
     * <p>
     * Only one listener instance can be registered. Setting a new item will
     * replace an existing registered instance. Setting a null value will clear
     * the current set instance.
     *
     * @param l The processing instance to register or null to clear
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    void setScreenCaptureListener(Xj3DScreenCaptureListener l)
        throws InvalidBrowserException;

    /**
     * Start the render and event cascade evaluation system again after a pause
     * or stop. If this is a start after a stop, it should be treated as the
     * world having just been loaded. All scripts and sensors should be told to
     * initialise again. If it is after a pause, the world continues as before
     * as though nothing has happened, except TimeSensors will have their time
     * values updated to reflect the current time and the fraction adjusted
     * accordingly.
     */
    void startRender();

    /**
     * Pause the render and event cascade evaluation system.
     */
    void pauseRender();

    /**
     * Stop the render and event cascade evaluation system completely. This will
     * trigger the proper shutdown notifications being sent to all nodes and
     * scripts.
     */
    void stopRender();
}
