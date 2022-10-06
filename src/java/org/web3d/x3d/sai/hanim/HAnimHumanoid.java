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

package org.web3d.x3d.sai.hanim;

import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/**
 * Defines the requirements of an X3D HAnimHumanoid node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface HAnimHumanoid extends X3DChildNode, X3DBoundedObject {

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

    /**
     * Return the name String value.
     *
     * @return The name String value.
     */
    String getName();

    /**
     * Set the name field.
     *
     * @param val The String to set.
     */
    void setName(String val);

    /**
     * Return the number of MFString items in the info field.
     *
     * @return the number of MFString items in the info field.
     */
    int getNumInfo();

    /**
     * Return the info value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getInfo(String[] val);

    /**
     * Set the info field.
     *
     * @param val The String[] to set.
     */
    void setInfo(String[] val);

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
     * Return the number of MFNode items in the segments field.
     *
     * @return the number of MFNode items in the segments field.
     */
    int getNumSegments();

    /**
     * Return the segments value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getSegments(X3DNode[] val);

    /**
     * Set the segments field.
     *
     * @param val The X3DNode[] to set.
     */
    void setSegments(X3DNode[] val);

    /**
     * Return the number of MFNode items in the sites field.
     *
     * @return the number of MFNode items in the sites field.
     */
    int getNumSites();

    /**
     * Return the sites value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getSites(X3DNode[] val);

    /**
     * Set the sites field.
     *
     * @param val The X3DNode[] to set.
     */
    void setSites(X3DNode[] val);

    /**
     * Return the number of MFNode items in the skeleton field.
     *
     * @return the number of MFNode items in the skeleton field.
     */
    int getNumSkeleton();

    /**
     * Return the skeleton value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getSkeleton(X3DNode[] val);

    /**
     * Set the skeleton field.
     *
     * @param val The X3DNode[] to set.
     */
    void setSkeleton(X3DNode[] val);

    /**
     * Return the number of MFNode items in the skin field.
     *
     * @return the number of MFNode items in the skin field.
     */
    int getNumSkin();

    /**
     * Return the skin value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getSkin(X3DNode[] val);

    /**
     * Set the skin field.
     *
     * @param val The X3DNode[] to set.
     */
    void setSkin(X3DNode[] val);

    /**
     * Return the skinCoord X3DNode value.
     *
     * @return The skinCoord X3DNode value.
     */
    X3DNode getSkinCoord();

    /**
     * Set the skinCoord field.
     *
     * @param val The X3DNode to set.
     */
    void setSkinCoord(X3DNode val);

    /**
     * Return the skinNormal X3DNode value.
     *
     * @return The skinNormal X3DNode value.
     */
    X3DNode getSkinNormal();

    /**
     * Set the skinNormal field.
     *
     * @param val The X3DNode to set.
     */
    void setSkinNormal(X3DNode val);

    /**
     * Return the version String value.
     *
     * @return The version String value.
     */
    String getVersion();

    /**
     * Set the version field.
     *
     * @param val The String to set.
     */
    void setVersion(String val);

    /**
     * Return the number of MFNode items in the viewpoints field.
     *
     * @return the number of MFNode items in the viewpoints field.
     */
    int getNumViewpoints();

    /**
     * Return the viewpoints value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getViewpoints(X3DNode[] val);

    /**
     * Set the viewpoints field.
     *
     * @param val The X3DNode[] to set.
     */
    void setViewpoints(X3DNode[] val);
}
