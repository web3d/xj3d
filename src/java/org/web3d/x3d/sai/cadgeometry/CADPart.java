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

package org.web3d.x3d.sai.cadgeometry;

import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DProductStructureChildNode;

/**
 * Defines the requirements of an X3D CADPart node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface CADPart extends X3DGroupingNode, X3DProductStructureChildNode {

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
     * Return the rotation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getRotation(float[] val);

    /**
     * Set the rotation field.
     *
     * @param val The float[] to set.
     */
    void setRotation(float[] val);

    /**
     * Return the scale value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getScale(float[] val);

    /**
     * Set the scale field.
     *
     * @param val The float[] to set.
     */
    void setScale(float[] val);

    /**
     * Return the scaleOrientation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getScaleOrientation(float[] val);

    /**
     * Set the scaleOrientation field.
     *
     * @param val The float[] to set.
     */
    void setScaleOrientation(float[] val);

    /**
     * Return the translation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getTranslation(float[] val);

    /**
     * Set the translation field.
     *
     * @param val The float[] to set.
     */
    void setTranslation(float[] val);
}
