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
 * Defines the requirements of an X3D RigidBodyCollection node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface RigidBodyCollection extends X3DChildNode {

    /**
     * Set the contacts field.
     *
     * @param val The X3DNode[] to set.
     */
    void setContacts(X3DNode[] val);

    /**
     * Return the autoDisable boolean value.
     *
     * @return The autoDisable boolean value.
     */
    boolean getAutoDisable();

    /**
     * Set the autoDisable field.
     *
     * @param val The boolean to set.
     */
    void setAutoDisable(boolean val);

    /**
     * Return the number of MFNode items in the bodies field.
     *
     * @return the number of MFNode items in the bodies field.
     */
    int getNumBodies();

    /**
     * Return the bodies value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getBodies(X3DNode[] val);

    /**
     * Set the bodies field.
     *
     * @param val The X3DNode[] to set.
     */
    void setBodies(X3DNode[] val);

    /**
     * Return the constantForceMix float value.
     *
     * @return The constantForceMix float value.
     */
    float getConstantForceMix();

    /**
     * Set the constantForceMix field.
     *
     * @param val The float to set.
     */
    void setConstantForceMix(float val);

    /**
     * Return the contactSurfaceThickness float value.
     *
     * @return The contactSurfaceThickness float value.
     */
    float getContactSurfaceThickness();

    /**
     * Set the contactSurfaceThickness field.
     *
     * @param val The float to set.
     */
    void setContactSurfaceThickness(float val);

    /**
     * Return the disableAngularSpeed float value.
     *
     * @return The disableAngularSpeed float value.
     */
    float getDisableAngularSpeed();

    /**
     * Set the disableAngularSpeed field.
     *
     * @param val The float to set.
     */
    void setDisableAngularSpeed(float val);

    /**
     * Return the disableLinearSpeed float value.
     *
     * @return The disableLinearSpeed float value.
     */
    float getDisableLinearSpeed();

    /**
     * Set the disableLinearSpeed field.
     *
     * @param val The float to set.
     */
    void setDisableLinearSpeed(float val);

    /**
     * Return the disableTime float value.
     *
     * @return The disableTime float value.
     */
    float getDisableTime();

    /**
     * Set the disableTime field.
     *
     * @param val The float to set.
     */
    void setDisableTime(float val);

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
     * Return the errorCorrectionFactor float value.
     *
     * @return The errorCorrectionFactor float value.
     */
    float getErrorCorrectionFactor();

    /**
     * Set the errorCorrectionFactor field.
     *
     * @param val The float to set.
     */
    void setErrorCorrectionFactor(float val);

    /**
     * Return the gravity value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getGravity(float[] val);

    /**
     * Set the gravity field.
     *
     * @param val The float[] to set.
     */
    void setGravity(float[] val);

    /**
     * Return the iterations int value.
     *
     * @return The iterations int value.
     */
    int getIterations();

    /**
     * Set the iterations field.
     *
     * @param val The int to set.
     */
    void setIterations(int val);

    /**
     * Return the number of MFNode items in the joints field.
     *
     * @return the number of MFNode items in the joints field.
     */
    int getNumJoints();

    /**
     * Return the joints value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getJoints(X3DNode[] val);

    /**
     * Set the joints field.
     *
     * @param val The X3DNode[] to set.
     */
    void setJoints(X3DNode[] val);

    /**
     * Return the maxCorrectionSpeed float value.
     *
     * @return The maxCorrectionSpeed float value.
     */
    float getMaxCorrectionSpeed();

    /**
     * Set the maxCorrectionSpeed field.
     *
     * @param val The float to set.
     */
    void setMaxCorrectionSpeed(float val);

    /**
     * Return the preferAccuracy boolean value.
     *
     * @return The preferAccuracy boolean value.
     */
    boolean getPreferAccuracy();

    /**
     * Set the preferAccuracy field.
     *
     * @param val The boolean to set.
     */
    void setPreferAccuracy(boolean val);

    /**
     * Return the collider X3DNode value.
     *
     * @return The collider X3DNode value.
     */
    X3DNode getCollider();

    /**
     * Set the collider field.
     *
     * @param val The X3DNode to set.
     */
    void setCollider(X3DNode val);
}
