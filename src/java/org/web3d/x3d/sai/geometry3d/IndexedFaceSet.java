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

import org.web3d.x3d.sai.X3DComposedGeometryNode;

/** Defines the requirements of an X3D IndexedFaceSet node
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface IndexedFaceSet extends X3DComposedGeometryNode {

    /**
     * Return the number of MFInt32 items in the colorIndex field.
     *
     * @return the number of MFInt32 items in the colorIndex field.
     */
    int getNumColorIndex();

    /**
     * Return the colorIndex value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    void getColorIndex(int[] val);

    /**
     * Set the colorIndex field.
     *
     * @param val The int[] to set.
     */
    void setColorIndex(int[] val);

    /**
     * Return the number of MFInt32 items in the coordIndex field.
     *
     * @return the number of MFInt32 items in the coordIndex field.
     */
    int getNumCoordIndex();

    /**
     * Return the coordIndex value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    void getCoordIndex(int[] val);

    /**
     * Set the coordIndex field.
     *
     * @param val The int[] to set.
     */
    void setCoordIndex(int[] val);

    /**
     * Return the number of MFInt32 items in the texCoordIndex field.
     *
     * @return the number of MFInt32 items in the texCoordIndex field.
     */
    int getNumTexCoordIndex();

    /**
     * Return the texCoordIndex value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    void getTexCoordIndex(int[] val);

    /**
     * Set the texCoordIndex field.
     *
     * @param val The int[] to set.
     */
    void setTexCoordIndex(int[] val);

    /**
     * Return the number of MFInt32 items in the normalIndex field.
     *
     * @return the number of MFInt32 items in the normalIndex field.
     */
    int getNumNormalIndex();

    /**
     * Return the normalIndex value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    void getNormalIndex(int[] val);

    /**
     * Set the normalIndex field.
     *
     * @param val The int[] to set.
     */
    void setNormalIndex(int[] val);

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
     * Return the convex boolean value.
     *
     * @return The convex boolean value.
     */
    boolean getConvex();

    /**
     * Set the convex field.
     *
     * @param val The boolean to set.
     */
    void setConvex(boolean val);
}
