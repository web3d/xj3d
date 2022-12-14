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

import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/**
 * Defines the requirements of an X3D HAnimSegment node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface HAnimSegment extends X3DGroupingNode {

    /**
     * Return the centerOfMass value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getCenterOfMass(float[] val);

    /**
     * Set the centerOfMass field.
     *
     * @param val The float[] to set.
     */
    void setCenterOfMass(float[] val);

    /**
     * Return the coord X3DNode value.
     *
     * @return The coord X3DNode value.
     */
    X3DNode getCoord();

    /**
     * Set the coord field.
     *
     * @param val The X3DCoordinateNode to set.
     */
    void setCoord(X3DCoordinateNode val);

    /**
     * Set the coord field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setCoord(X3DProtoInstance val);

    /**
     * Return the number of MFNode items in the displacers field.
     *
     * @return the number of MFNode items in the displacers field.
     */
    int getNumDisplacers();

    /**
     * Return the displacers value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getDisplacers(X3DNode[] val);

    /**
     * Set the displacers field.
     *
     * @param val The X3DNode[] to set.
     */
    void setDisplacers(X3DNode[] val);

    /**
     * Return the mass float value.
     *
     * @return The mass float value.
     */
    float getMass();

    /**
     * Set the mass field.
     *
     * @param val The float to set.
     */
    void setMass(float val);

    /**
     * Return the number of MFFloat items in the momentsOfInertia field.
     *
     * @return the number of MFFloat items in the momentsOfInertia field.
     */
    int getNumMomentsOfInertia();

    /**
     * Return the momentsOfInertia value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getMomentsOfInertia(float[] val);

    /**
     * Set the momentsOfInertia field.
     *
     * @param val The float[] to set.
     */
    void setMomentsOfInertia(float[] val);

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
}
