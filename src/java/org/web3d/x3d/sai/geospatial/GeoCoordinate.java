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

import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DNode;

/** Defines the requirements of an X3D GeoCoordinate node
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface GeoCoordinate extends X3DCoordinateNode {

    /**
     * Return the number of MFVec3d items in the point field.
     *
     * @return the number of MFVec3d items in the point field.
     */
    int getNumPoint();

    /**
     * Return the point value in the argument double[]
     *
     * @param val The double[] to initialize.
     */
    void getPoint(double[] val);

    /**
     * Set the point field.
     *
     * @param val The double[] to set.
     */
    void setPoint(double[] val);

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
}
