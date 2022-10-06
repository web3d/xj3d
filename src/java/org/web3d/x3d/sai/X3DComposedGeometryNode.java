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

package org.web3d.x3d.sai;

// Standard library imports
// None

// Local imports
// None

/** Defines the requirements of an X3DComposedGeometryNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.7 $
 */
public interface X3DComposedGeometryNode extends X3DGeometryNode {

    /**
     * Return the number of MFNode items in the attrib field.
     *
     * @return the number of MFNode items in the attrib field.
     */
    int getNumAttrib();

    /**
     * Return the attrib value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getAttrib(X3DNode[] val);

    /**
     * Set the attrib field.
     *
     * @param val The X3DNode[] to set.
     */
    void setAttrib(X3DNode[] val);

    /**
     * Return the fogCoord X3DNode value.
     *
     * @return The fogCoord X3DNode value.
     */
    X3DNode getFogCoord();

    /**
     * Set the fogCoord field.
     *
     * @param val The X3DNode to set.
     */
    void setFogCoord(X3DNode val);

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
}
