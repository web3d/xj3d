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

// Standard library imports
// None

// Local imports
// None

/**
 * Defines the requirements of an X3DEnvironmentalSensorNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public interface X3DEnvironmentalSensorNode extends X3DSensorNode {

    /**
     * Return the center value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getCenter(float[] val);

    /**
     * Set the center field.
     *
     * @param val The float[] to set.
     */
    void setCenter(float[] val);

    /**
     * Return the size value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getSize(float[] val);

    /**
     * Set the size field.
     *
     * @param val The float[] to set.
     */
    void setSize(float[] val);

    /**
     * Return the enterTime double value.
     *
     * @return The enterTime double value.
     */
    double getEnterTime();

    /**
     * Return the exitTime double value.
     *
     * @return The exitTime double value.
     */
    double getExitTime();
}
