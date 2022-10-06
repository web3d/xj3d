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

package org.web3d.x3d.sai.sound;

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DSoundNode;

/**
 * Defines the requirements of an X3D Sound node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Sound extends X3DSoundNode {

    /**
     * Return the direction value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getDirection(float[] val);

    /**
     * Set the direction field.
     *
     * @param val The float[] to set.
     */
    void setDirection(float[] val);

    /**
     * Return the intensity float value.
     *
     * @return The intensity float value.
     */
    float getIntensity();

    /**
     * Set the intensity field.
     *
     * @param val The float to set.
     */
    void setIntensity(float val);

    /**
     * Return the location value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getLocation(float[] val);

    /**
     * Set the location field.
     *
     * @param val The float[] to set.
     */
    void setLocation(float[] val);

    /**
     * Return the maxBack float value.
     *
     * @return The maxBack float value.
     */
    float getMaxBack();

    /**
     * Set the maxBack field.
     *
     * @param val The float to set.
     */
    void setMaxBack(float val);

    /**
     * Return the maxFront float value.
     *
     * @return The maxFront float value.
     */
    float getMaxFront();

    /**
     * Set the maxFront field.
     *
     * @param val The float to set.
     */
    void setMaxFront(float val);

    /**
     * Return the minBack float value.
     *
     * @return The minBack float value.
     */
    float getMinBack();

    /**
     * Set the minBack field.
     *
     * @param val The float to set.
     */
    void setMinBack(float val);

    /**
     * Return the minFront float value.
     *
     * @return The minFront float value.
     */
    float getMinFront();

    /**
     * Set the minFront field.
     *
     * @param val The float to set.
     */
    void setMinFront(float val);

    /**
     * Return the priority float value.
     *
     * @return The priority float value.
     */
    float getPriority();

    /**
     * Set the priority field.
     *
     * @param val The float to set.
     */
    void setPriority(float val);

    /**
     * Return the source X3DNode value.
     *
     * @return The source X3DNode value.
     */
    X3DNode getSource();

    /**
     * Set the source field.
     *
     * @param val The X3DNode to set.
     */
    void setSource(X3DNode val);

    /**
     * Return the spatialize boolean value.
     *
     * @return The spatialize boolean value.
     */
    boolean getSpatialize();

    /**
     * Set the spatialize field.
     *
     * @param val The boolean to set.
     */
    void setSpatialize(boolean val);
}
