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

package org.web3d.x3d.sai.rigidbodyphysics;

import org.web3d.x3d.sai.X3DRigidJointNode;

/**
 * Defines the requirements of an X3D SliderJoint node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface SliderJoint extends X3DRigidJointNode {

    /**
     * Return the axis value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getAxis(float[] val);

    /**
     * Set the axis field.
     *
     * @param val The float[] to set.
     */
    void setAxis(float[] val);

    /**
     * Return the minSeparation float value.
     *
     * @return The minSeparation float value.
     */
    float getMinSeparation();

    /**
     * Set the minSeparation field.
     *
     * @param val The float to set.
     */
    void setMinSeparation(float val);

    /**
     * Return the maxSeparation float value.
     *
     * @return The maxSeparation float value.
     */
    float getMaxSeparation();

    /**
     * Set the maxSeparation field.
     *
     * @param val The float to set.
     */
    void setMaxSeparation(float val);

    /**
     * Return the separation float value.
     *
     * @return The separation float value.
     */
    float getSeparation();

    /**
     * Return the separationRate float value.
     *
     * @return The separationRate float value.
     */
    float getSeparationRate();

    /**
     * Return the stopBounce float value.
     *
     * @return The stopBounce float value.
     */
    float getStopBounce();

    /**
     * Set the stopBounce field.
     *
     * @param val The float to set.
     */
    void setStopBounce(float val);

    /**
     * Return the stopErrorCorrection float value.
     *
     * @return The stopErrorCorrection float value.
     */
    float getStopErrorCorrection();

    /**
     * Set the stopErrorCorrection field.
     *
     * @param val The float to set.
     */
    void setStopErrorCorrection(float val);
}
