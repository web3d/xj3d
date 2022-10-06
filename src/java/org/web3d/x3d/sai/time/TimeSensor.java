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

package org.web3d.x3d.sai.time;

//Standard library imports
// None

// Local imports
import org.web3d.x3d.sai.X3DSensorNode;
import org.web3d.x3d.sai.X3DTimeDependentNode;

/**
 * Defines the requirements of an X3D TimeSensor node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface TimeSensor extends X3DTimeDependentNode, X3DSensorNode {

    /**
     * Return the cycleInterval double value.
     *
     * @return The cycleInterval double value.
     */
    double getCycleInterval();

    /**
     * Set the cycleInterval field.
     *
     * @param val The double to set.
     */
    void setCycleInterval(double val);

    /**
     * Return the fraction float value.
     *
     * @return The fraction float value.
     */
    float getFraction();

    /**
     * Return the time double value.
     *
     * @return The time double value.
     */
    double getTime();

    /**
     * Return the cycleTime double value.
     *
     * @return The cycleTime double value.
     */
    double getCycleTime();
}
