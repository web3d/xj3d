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
 * Defines the requirements of an X3D SingleAxisHingeJoint node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface SingleAxisHingeJoint extends X3DRigidJointNode {

    /**
     * Return the anchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getAnchorPoint(float[] val);

    /**
     * Set the anchorPoint field.
     *
     * @param val The float[] to set.
     */
    void setAnchorPoint(float[] val);

    /**
     * Return the body1AnchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getBody1AnchorPoint(float[] val);

    /**
     * Return the body2AnchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getBody2AnchorPoint(float[] val);

    /**
     * Return the Axis value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getAxis(float[] val);

    /**
     * Set the Axis field.
     *
     * @param val The float[] to set.
     */
    void setAxis(float[] val);

    /**
     * Return the minAngle float value.
     *
     * @return The minAngle float value.
     */
    float getMinAngle();

    /**
     * Set the minAngle field.
     *
     * @param val The float to set.
     */
    void setMinAngle(float val);

    /**
     * Return the maxAngle float value.
     *
     * @return The maxAngle float value.
     */
    float getMaxAngle();

    /**
     * Set the maxAngle field.
     *
     * @param val The float to set.
     */
    void setMaxAngle(float val);

    /**
     * Return the angle float value.
     *
     * @return The angle float value.
     */
    float getAngle();

    /**
     * Return the angleRate float value.
     *
     * @return The angleRate float value.
     */
    float getAngleRate();

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
