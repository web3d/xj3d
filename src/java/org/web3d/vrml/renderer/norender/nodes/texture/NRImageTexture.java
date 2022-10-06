/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.texture;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRTextureNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseImageTexture;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a ImageTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public class NRImageTexture extends BaseImageTexture
    implements NRTextureNodeType, NRVRMLNode {

    /**
     * Default constructor.
     */
    public NRImageTexture() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRImageTexture(VRMLNodeType node) {
        super(node);
    }
}
