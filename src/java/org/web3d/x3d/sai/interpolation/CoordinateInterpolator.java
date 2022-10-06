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

package org.web3d.x3d.sai.interpolation;

import org.web3d.x3d.sai.X3DInterpolatorNode;

/**
 * Defines the requirements of an X3D CoordinateInterpolator node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface CoordinateInterpolator extends X3DInterpolatorNode {

    /**
     * Return the number of MFVec3f items in the keyValue field.
     *
     * @return the number of MFVec3f items in the keyValue field.
     */
    int getNumKeyValue();

    /**
     * Return the keyValue value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getKeyValue(float[] val);

    /**
     * Set the keyValue field.
     *
     * @param val The float[] to set.
     */
    void setKeyValue(float[] val);

    /**
     * Return the number of MFVec3f items in the value field.
     *
     * @return the number of MFVec3f items in the value field.
     */
    int getNumValue();

    /**
     * Return the value value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getValue(float[] val);
}
