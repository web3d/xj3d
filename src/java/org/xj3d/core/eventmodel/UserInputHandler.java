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

package org.xj3d.core.eventmodel;

// External imports
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.device.input.TrackerState;

// Local imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.browser.NavigationStateListener;
import org.web3d.browser.SensorStatusListener;

/**
 * A complete handler for all user input information within a scene.
 * <p>
 *
 * The handler takes care of all the handling needed for sensors, anchors,
 * navigation and keyboard. However, it does not define a way of sourcing
 * those events as it assumes that a user will either delegate or extend this
 * class with more specific information such as an AWT listener or Java3D
 * behavior.
 * <p>
 *
 * The current key handling does not allow keyboard navigation of the world.
 * It passes all key events directly through to the current key sensor if one
 * is registered.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public interface UserInputHandler extends OriginListener {

    /**
     * Process a tracker press event. This may be used to start a touchSensor,
     * start of a drag sensor or navigation
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerPressed(int tracker, TrackerState evt);

    /**
     * Process a tracker press event. This may be used to start a touchtracker,
     * start of a drag tracker or navigation
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerMoved(int tracker, TrackerState evt);

    /**
     * Process a tracker press event. This may be used to start a touchtracker,
     * start of a drag tracker or navigation
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerDragged(int tracker, TrackerState evt);

    /**
     * Process a tracker press event. This may be used to start a touchtracker,
     * start of a drag tracker or navigation
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerReleased(int tracker, TrackerState evt);

    /**
     * Process a tracker click event. The click is used only on touch trackers
     * and anchors. We treat it like a cross between a select and unselect.
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerClicked(int tracker, TrackerState evt);

    /**
     * Process a tracker orientation event. This is for trackers like HMDs that
     * can change orientation without changing position or other state.
     *
     * @param tracker The id of the tracker calling this handler
     * @param evt The event that caused the method to be called
     */
    void trackerOrientation(int tracker, TrackerState evt);

    /**
     * Process any navigation velocity.  Call every frame while a drag
     * is active.
     */
    void processNavigation();

    /**
     * Process the buttons on a tracker.  No other state will be read.
     *
     * @param tracker The id of the tracker calling this handler
     * @param state The current state.
     */
    void trackerButton(int tracker, TrackerState state);

    /**
     * Process the wheel on a tracker.  No other state will be read.
     *
     * @param tracker The id of the tracker calling this handler
     * @param state The current state.
     */
    void trackerWheel(int tracker, TrackerState state);

    /**
     * Did the last tracker interaction intersect any active sensors.
     *
     * @return true if the tracker intersection an active sensor.
     */
    boolean trackerIntersected();

    /**
     * Sets whether this tracker is eligible to active a sensor.
     *
     * @param val Whether its eligible
     */
    void setActivateSensors(boolean val);

    /**
     * Set the desired navigation mode.
     *
     * @param mode The requested mode.
     * @return Whether the mode is valid.
     */
    boolean setNavigationMode(String mode);

    /**
     * Set the navigation info that is used for this scene. The canvas can
     * then use it to build any user interface interactions it desires. A
     * value of null will remove the info and is typically used when the
     * canvas is about to be removed from a universe.
     *
     * @param navInfo The new navigation information to be used
     */
    void setNavigationInfo(VRMLNavigationInfoNodeType navInfo);

    /**
     * Add a navigationStateListener. Duplicates and null will be ignored.
     *
     * @param l The listener to add
     */
    void addNavigationStateListener(NavigationStateListener l);

    /**
     * Remove a navigationStateListener.
     *
     * @param l The listener to remove
     */
    void removeNavigationStateListener(NavigationStateListener l);

    /**
     * Add a sensorStatusListener. Duplicates and null will be ignored.
     *
     * @param l The listener to add
     */
    void addSensorStatusListener(SensorStatusListener l);

    /**
     * Remove a sensorStatusListener.
     *
     * @param l The listener to remove
     */
    void removeSensorStatusListener(SensorStatusListener l);

    /**
     * Should pointing devices be tested for.
     *
     * @param enabled Test for intersection when true
     */
    void setTestPointingDevices(boolean enabled);

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    void setWorldScale(float scale);

    /**
     * Get the current user orientation.
     *
     * @param ori The orientation vector to fill in
     */
    void getOrientation(AxisAngle4f ori);

    /**
     * Get the current user position.
     *
     * @param pos The position vector to fill in
     */
    void getPosition(Vector3f pos);

    /**
     * Set the center of rotation explicitly to this place. Coordinates must
     * be in the coordinate space of the current view transform group. The
     * provided array must be of least length 3. Center of rotation is used
     * in examine mode.
     *
     * @param center The new center to use
     */
    void setCenterOfRotation(float[] center);

    /**
     * Get the currently set navigation state.
     *
     * @return true for the current state
     */
    boolean getNavigationEnabled();

    /**
     * Enable or disable navigation processing sub-section of the
     * user input processing. By default the navigation processing is enabled.
     * Navigation also means responding to changes in the environment, even
     * without there being user input to process.
     *
     * @param state true to enable navigation
     */
    void setNavigationEnabled(boolean state);

    /**
     * The layer that contains this handler has just been made the active
     * navigation layer, so send out to the navigation state listeners the
     * current navigation state for this layer. This allows the UI to update
     * based on the currently active layer.
     */
    void sendCurrentNavState();

    /**
     * Set the clock we are going to operate from when generating events. A
     * null value will remove the clock.
     *
     * @param clk The new clock to use
     */
    void setVRMLClock(VRMLClock clk);

    /**
     * Clear all the values, listeners etc, except for the clock. Returns the
     * input handler back to being empty, with no state set.
     */
    void clear();

    /**
     * Set the manager for handling dynamic origin calculation.
     *
     * @param manager Reference to the manager instance to use or null
     */
    void setOriginManager(OriginManager manager);
}
