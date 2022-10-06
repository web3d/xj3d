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

/**
 * Defines the requirements of an X3DShapeNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public interface X3DShapeNode extends X3DChildNode, X3DBoundedObject {

    /**
     * Return the appearance X3DNode value.
     *
     * @return The appearance X3DNode value.
     */
    X3DNode getAppearance();

    /**
     * Set the appearance field.
     *
     * @param val The X3DAppearanceNode to set.
     */
    void setAppearance(X3DAppearanceNode val);

    /**
     * Set the appearance field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setAppearance(X3DProtoInstance val);

    /**
     * Return the geometry X3DNode value.
     *
     * @return The geometry X3DNode value.
     */
    X3DNode getGeometry();

    /**
     * Set the geometry field.
     *
     * @param val The X3DGeometryNode to set.
     */
    void setGeometry(X3DGeometryNode val);

    /**
     * Set the geometry field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setGeometry(X3DProtoInstance val);
}
