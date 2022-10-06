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

import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/**
 * Defines the requirements of an X3D CollisionCollection node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface CollisionCollection extends X3DChildNode {

    /**
     * Return the number of MFNode items in the collidables field.
     *
     * @return the number of MFNode items in the collidables field.
     */
    int getNumCollidables();

    /**
     * Return the collidables value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getCollidables(X3DNode[] val);

    /**
     * Set the collidables field.
     *
     * @param val The X3DNode[] to set.
     */
    void setCollidables(X3DNode[] val);

    /**
     * Return the enabled boolean value.
     *
     * @return The enabled boolean value.
     */
    boolean getEnabled();

    /**
     * Set the enabled field.
     *
     * @param val The boolean to set.
     */
    void setEnabled(boolean val);

    /**
     * Return the bounce float value.
     *
     * @return The bounce float value.
     */
    float getBounce();

    /**
     * Set the bounce field.
     *
     * @param val The float to set.
     */
    void setBounce(float val);

    /**
     * Return the minBounceSpeed float value.
     *
     * @return The minBounceSpeed float value.
     */
    float getMinBounceSpeed();

    /**
     * Set the minBounceSpeed field.
     *
     * @param val The float to set.
     */
    void setMinBounceSpeed(float val);

    /**
     * Return the frictionCoefficients value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getFrictionCoefficients(float[] val);

    /**
     * Set the frictionCoefficients field.
     *
     * @param val The float[] to set.
     */
    void setFrictionCoefficients(float[] val);

    /**
     * Return the slipCoefficients value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getSlipCoefficients(float[] val);

    /**
     * Set the slipCoefficients field.
     *
     * @param val The float[] to set.
     */
    void setSlipCoefficients(float[] val);

    /**
     * Return the surfaceSpeed value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getSurfaceSpeed(float[] val);

    /**
     * Set the surfaceSpeed field.
     *
     * @param val The float[] to set.
     */
    void setSurfaceSpeed(float[] val);

    /**
     * Return the number of MFString items in the appliedParameters field.
     *
     * @return the number of MFString items in the appliedParameters field.
     */
    int getNumAppliedParameters();

    /**
     * Return the appliedParameters value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getAppliedParameters(String[] val);

    /**
     * Set the appliedParameters field.
     *
     * @param val The String[] to set.
     */
    void setAppliedParameters(String[] val);

    /**
     * Return the softnessConstantForceMix float value.
     *
     * @return The softnessConstantForceMix float value.
     */
    float getSoftnessConstantForceMix();

    /**
     * Set the softnessConstantForceMix field.
     *
     * @param val The float to set.
     */
    void setSoftnessConstantForceMix(float val);

    /**
     * Return the softnessErrorCorrection float value.
     *
     * @return The softnessErrorCorrection float value.
     */
    float getSoftnessErrorCorrection();

    /**
     * Set the softnessErrorCorrection field.
     *
     * @param val The float to set.
     */
    void setSoftnessErrorCorrection(float val);
}
