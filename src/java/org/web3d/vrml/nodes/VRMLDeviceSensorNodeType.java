/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// External imports
import org.j3d.device.input.DeviceState;
import org.j3d.device.input.InputDevice;

// Local Imports
// None

/**
 * A node representing an input / output device.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public interface VRMLDeviceSensorNodeType extends VRMLSensorNodeType {

    /**
     * Get the name field.
     *
     * @return The name.
     */
    String getName();

    /**
     * Update this nodes field from the underlying device.
     *
     * @param state The state information.
     */
    void update(DeviceState state);

    /**
     * Set the real device backing this node.  This will not be called
     * if the device name does not map to a live device.
     *
     * @param device The real device.
     */
    void setDevice(InputDevice device);
}
