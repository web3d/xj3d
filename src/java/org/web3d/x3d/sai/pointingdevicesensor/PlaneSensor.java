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

package org.web3d.x3d.sai.pointingdevicesensor;

import org.web3d.x3d.sai.X3DDragSensorNode;

/**
 * Defines the requirements of an X3D PlaneSensor node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface PlaneSensor extends X3DDragSensorNode {

    /**
     * Return the maxPosition value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getMaxPosition(float[] val);

    /**
     * Set the maxPosition field.
     *
     * @param val The float[] to set.
     */
    void setMaxPosition(float[] val);

    /**
     * Return the minPosition value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getMinPosition(float[] val);

    /**
     * Set the minPosition field.
     *
     * @param val The float[] to set.
     */
    void setMinPosition(float[] val);

    /**
     * Return the translation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getTranslation(float[] val);

    /**
     * Return the offset value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getOffset(float[] val);

    /**
     * Set the offset field.
     *
     * @param val The float[] to set.
     */
    void setOffset(float[] val);
}
