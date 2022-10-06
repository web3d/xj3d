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

package org.web3d.x3d.sai.shape;

import org.web3d.x3d.sai.X3DMaterialNode;

/**
 * Defines the requirements of an X3D Material node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Material extends X3DMaterialNode {

    /**
     * Return the ambientIntensity float value.
     *
     * @return The ambientIntensity float value.
     */
    float getAmbientIntensity();

    /**
     * Set the ambientIntensity field.
     *
     * @param val The float to set.
     */
    void setAmbientIntensity(float val);

    /**
     * Return the diffuseColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getDiffuseColor(float[] val);

    /**
     * Set the diffuseColor field.
     *
     * @param val The float[] to set.
     */
    void setDiffuseColor(float[] val);

    /**
     * Return the emissiveColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getEmissiveColor(float[] val);

    /**
     * Set the emissiveColor field.
     *
     * @param val The float[] to set.
     */
    void setEmissiveColor(float[] val);

    /**
     * Return the shininess float value.
     *
     * @return The shininess float value.
     */
    float getShininess();

    /**
     * Set the shininess field.
     *
     * @param val The float to set.
     */
    void setShininess(float val);

    /**
     * Return the specularColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getSpecularColor(float[] val);

    /**
     * Set the specularColor field.
     *
     * @param val The float[] to set.
     */
    void setSpecularColor(float[] val);

    /**
     * Return the transparency float value.
     *
     * @return The transparency float value.
     */
    float getTransparency();

    /**
     * Set the transparency field.
     *
     * @param val The float to set.
     */
    void setTransparency(float val);
}
