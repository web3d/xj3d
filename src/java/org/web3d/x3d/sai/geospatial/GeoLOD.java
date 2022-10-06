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

package org.web3d.x3d.sai.geospatial;

import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DNode;

/**
 * Defines the requirements of an X3D GeoLOD node
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public interface GeoLOD extends X3DChildNode, X3DBoundedObject {

    /**
     * Return the number of MFNode items in the children field.
     *
     * @return the number of MFNode items in the children field.
     */
    int getNumChildren();

    /**
     * Return the children value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getChildren(X3DNode[] val);

    /**
     * Return the number of MFNode items in the rootNode field.
     *
     * @return the number of MFNode items in the rootNode field.
     */
    int getNumRootNode();

    /**
     * Return the rootNode value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getRootNode(X3DNode[] val);

    /**
     * Set the rootNode field.
     *
     * @param val The X3DNode[] to set.
     */
    void setRootNode(X3DNode[] val);

    /**
     * Return the number of MFString items in the rootUrl field.
     *
     * @return the number of MFString items in the rootUrl field.
     */
    int getNumRootUrl();

    /**
     * Return the rootUrl value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getRootUrl(String[] val);

    /**
     * Set the rootUrl field.
     *
     * @param val The String[] to set.
     */
    void setRootUrl(String[] val);

    /**
     * Return the number of MFString items in the child1Url field.
     *
     * @return the number of MFString items in the child1Url field.
     */
    int getNumChild1Url();

    /**
     * Return the child1Url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getChild1Url(String[] val);

    /**
     * Set the child1Url field.
     *
     * @param val The String[] to set.
     */
    void setChild1Url(String[] val);

    /**
     * Return the number of MFString items in the child2Url field.
     *
     * @return the number of MFString items in the child2Url field.
     */
    int getNumChild2Url();

    /**
     * Return the child2Url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getChild2Url(String[] val);

    /**
     * Set the child2Url field.
     *
     * @param val The String[] to set.
     */
    void setChild2Url(String[] val);

    /**
     * Return the number of MFString items in the child3Url field.
     *
     * @return the number of MFString items in the child3Url field.
     */
    int getNumChild3Url();

    /**
     * Return the child3Url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getChild3Url(String[] val);

    /**
     * Set the child3Url field.
     *
     * @param val The String[] to set.
     */
    void setChild3Url(String[] val);

    /**
     * Return the number of MFString items in the child4Url field.
     *
     * @return the number of MFString items in the child4Url field.
     */
    int getNumChild4Url();

    /**
     * Return the child4Url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getChild4Url(String[] val);

    /**
     * Set the child4Url field.
     *
     * @param val The String[] to set.
     */
    void setChild4Url(String[] val);

    /**
     * Return the geoOrigin X3DNode value.
     *
     * @return The geoOrigin X3DNode value.
     */
    X3DNode getGeoOrigin();

    /**
     * Set the geoOrigin field.
     *
     * @param val The X3DNode to set.
     */
    void setGeoOrigin(X3DNode val);

    /**
     * Return the number of MFString items in the geoSystem field.
     *
     * @return the number of MFString items in the geoSystem field.
     */
    int getNumGeoSystem();

    /**
     * Return the geoSystem value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getGeoSystem(String[] val);

    /**
     * Set the geoSystem field.
     *
     * @param val The String[] to set.
     */
    void setGeoSystem(String[] val);

    /**
     * Return the center value in the argument double[]
     *
     * @param val The double[] to initialize.
     */
    void getCenter(double[] val);

    /**
     * Set the center field.
     *
     * @param val The double[] to set.
     */
    void setCenter(double[] val);

    /**
     * Return the range float value.
     *
     * @return The range float value.
     */
    float getRange();

    /**
     * Set the range field.
     *
     * @param val The float to set.
     */
    void setRange(float val);

    /**
     * Return the level int value.
     *
     * @return The level int value.
     */
    int getLevel();
}
