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

package org.web3d.x3d.sai.geometry3d;

import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;

/**
 * Defines the requirements of an X3D ElevationGrid node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface ElevationGrid extends X3DGeometryNode {

    /**
     * Return the color X3DNode value.
     *
     * @return The color X3DNode value.
     */
    X3DNode getColor();

    /**
     * Set the color field.
     *
     * @param val The X3DColorNode to set.
     */
    void setColor(X3DColorNode val);

    /**
     * Set the color field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setColor(X3DProtoInstance val);

    /**
     * Return the normal X3DNode value.
     *
     * @return The normal X3DNode value.
     */
    X3DNode getNormal();

    /**
     * Set the normal field.
     *
     * @param val The X3DNormalNode to set.
     */
    void setNormal(X3DNormalNode val);

    /**
     * Set the normal field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setNormal(X3DProtoInstance val);

    /**
     * Return the texCoord X3DNode value.
     *
     * @return The texCoord X3DNode value.
     */
    X3DNode getTexCoord();

    /**
     * Set the texCoord field.
     *
     * @param val The X3DTextureCoordinateNode to set.
     */
    void setTexCoord(X3DTextureCoordinateNode val);

    /**
     * Set the texCoord field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setTexCoord(X3DProtoInstance val);

    /**
     * Return the ccw boolean value.
     *
     * @return The ccw boolean value.
     */
    boolean getCcw();

    /**
     * Set the ccw field.
     *
     * @param val The boolean to set.
     */
    void setCcw(boolean val);

    /**
     * Return the colorPerVertex boolean value.
     *
     * @return The colorPerVertex boolean value.
     */
    boolean getColorPerVertex();

    /**
     * Set the colorPerVertex field.
     *
     * @param val The boolean to set.
     */
    void setColorPerVertex(boolean val);

    /**
     * Return the creaseAngle float value.
     *
     * @return The creaseAngle float value.
     */
    float getCreaseAngle();

    /**
     * Set the creaseAngle field.
     *
     * @param val The float to set.
     */
    void setCreaseAngle(float val);

    /**
     * Return the number of MFFloat items in the height field.
     *
     * @return the number of MFFloat items in the height field.
     */
    int getNumHeight();

    /**
     * Return the height value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getHeight(float[] val);

    /**
     * Set the height field.
     *
     * @param val The float[] to set.
     */
    void setHeight(float[] val);

    /**
     * Return the normalPerVertex boolean value.
     *
     * @return The normalPerVertex boolean value.
     */
    boolean getNormalPerVertex();

    /**
     * Set the normalPerVertex field.
     *
     * @param val The boolean to set.
     */
    void setNormalPerVertex(boolean val);

    /**
     * Return the solid boolean value.
     *
     * @return The solid boolean value.
     */
    boolean getSolid();

    /**
     * Set the solid field.
     *
     * @param val The boolean to set.
     */
    void setSolid(boolean val);

    /**
     * Return the xDimension int value.
     *
     * @return The xDimension int value.
     */
    int getXDimension();

    /**
     * Set the xDimension field.
     *
     * @param val The int to set.
     */
    void setXDimension(int val);

    /**
     * Return the xSpacing float value.
     *
     * @return The xSpacing float value.
     */
    float getXSpacing();

    /**
     * Set the xSpacing field.
     *
     * @param val The float to set.
     */
    void setXSpacing(float val);

    /**
     * Return the zDimension int value.
     *
     * @return The zDimension int value.
     */
    int getZDimension();

    /**
     * Set the zDimension field.
     *
     * @param val The int to set.
     */
    void setZDimension(int val);

    /**
     * Return the zSpacing float value.
     *
     * @return The zSpacing float value.
     */
    float getZSpacing();

    /**
     * Set the zSpacing field.
     *
     * @param val The float to set.
     */
    void setZSpacing(float val);
}
