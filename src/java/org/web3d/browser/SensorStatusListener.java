/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
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
// None

// Local imports
// None

/**
 * A listener to notify that a sensor's status has changed in relation to
 * an input device.
 * <p>
 * Any VRML/X3D node that interacts with a tracker will issue these events.
 *
 * TODO: This interface does not allow you to separate events when the user
 * has multiple picking devices like two gloves.  In the future we will
 * add parameters to determine which device was used.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface SensorStatusListener {

    /** The sensor is an Anchor */
    int TYPE_ANCHOR = 0;

    /** The sensor is a TouchSensor */
    int TYPE_TOUCH_SENSOR = 1;

    /** The sensor is a DragSensor  */
    int TYPE_DRAG_SENSOR = 2;

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    void deviceOver(int type, String desc);

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    void deviceNotOver(int type);

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    void deviceActivated(int type);

    /**
     * Invoked when a tracker selects an object that represents a link to an
     * external source.
     *
     * @param urls The urls to load in the order of preference defined by
     *    the node
     * @param params The list of parameters provided with the node.
     *    Null if none.
     * @param desc The description that may be accompanying the link node
     */
    void linkActivated(String[] urls, String[] params, String desc);
}
