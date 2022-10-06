/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;
//Standard library imports
// None

// Local imports
// None

/** Defines the requirements of an X3DTouchSensorNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public interface X3DTouchSensorNode extends X3DPointingDeviceSensorNode {

    /**
     * Return the touchTime double value.
     *
     * @return The touchTime double value.
     */
    double getTouchTime();
}