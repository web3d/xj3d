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

package org.web3d.x3d.sai.rendering;

import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DGeometryNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;

/**
 * Defines the requirements of an X3D LineSet node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface LineSet extends X3DGeometryNode {

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
     * Return the number of MFInt32 items in the vertexCount field.
     *
     * @return the number of MFInt32 items in the vertexCount field.
     */
    int getNumVertexCount();

    /**
     * Return the vertexCount value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    void getVertexCount(int[] val);

    /**
     * Set the vertexCount field.
     *
     * @param val The int[] to set.
     */
    void setVertexCount(int[] val);
}
