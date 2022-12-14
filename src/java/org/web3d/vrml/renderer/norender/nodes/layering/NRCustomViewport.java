/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.norender.nodes.layering;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.layering.BaseCustomViewport;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null-renderer implementation of the CustomViewport node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRCustomViewport extends BaseCustomViewport
    implements NRVRMLNode {

    /**
     * Create a new, default instance of this class.
     */
    public NRCustomViewport() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     *  <p>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    public NRCustomViewport(VRMLNodeType node) {
        super(node);
    }
}
