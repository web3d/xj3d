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

package org.web3d.x3d.sai.texturing;

import org.web3d.x3d.sai.X3DNode;

/**
 * Defines the requirements of an X3D TextureProperties node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface TextureProperties extends X3DNode {

    /**
     * Return the boundaryColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getBoundaryColor(float[] val);

    /**
     * Set the boundaryColor field.
     *
     * @param val The float[] to set.
     */
    void setBoundaryColor(float[] val);

    /**
     * Return the boundaryWidth int value.
     *
     * @return The boundaryWidth int value.
     */
    int getBoundaryWidth();

    /**
     * Set the boundaryWidth field.
     *
     * @param val The int to set.
     */
    void setBoundaryWidth(int val);

    /**
     * Return the boundaryModeS String value.
     *
     * @return The boundaryModeS String value.
     */
    String getBoundaryModeS();

    /**
     * Set the boundaryModeS field.
     *
     * @param val The String to set.
     */
    void setBoundaryModeS(String val);

    /**
     * Return the boundaryModeT String value.
     *
     * @return The boundaryModeT String value.
     */
    String getBoundaryModeT();

    /**
     * Set the boundaryModeT field.
     *
     * @param val The String to set.
     */
    void setBoundaryModeT(String val);

    /**
     * Return the magnificationFilter String value.
     *
     * @return The magnificationFilter String value.
     */
    String getMagnificationFilter();

    /**
     * Set the magnificationFilter field.
     *
     * @param val The String to set.
     */
    void setMagnificationFilter(String val);

    /**
     * Return the minificationFilter String value.
     *
     * @return The minificationFilter String value.
     */
    String getMinificationFilter();

    /**
     * Set the minificationFilter field.
     *
     * @param val The String to set.
     */
    void setMinificationFilter(String val);

    /**
     * Return the generateMipMaps boolean value.
     *
     * @return The generateMipMaps boolean value.
     */
    boolean getGenerateMipMaps();

    /**
     * Set the generateMipMaps field.
     *
     * @param val The boolean to set.
     */
    void setGenerateMipMaps(boolean val);

    /**
     * Return the anisotropicMode String value.
     *
     * @return The anisotropicMode String value.
     */
    String getAnisotropicMode();

    /**
     * Set the anisotropicMode field.
     *
     * @param val The String to set.
     */
    void setAnisotropicMode(String val);

    /**
     * Return the anisotropicFilterDegree float value.
     *
     * @return The anisotropicFilterDegree float value.
     */
    float getAnisotropicFilterDegree();

    /**
     * Set the anisotropicFilterDegree field.
     *
     * @param val The float to set.
     */
    void setAnisotropicFilterDegree(float val);
}
