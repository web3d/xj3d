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

package org.web3d.vrml.renderer.norender.nodes.core;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.core.BaseMetadataInteger;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of the MetadataInteger node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRMetadataInteger extends BaseMetadataInteger implements NRVRMLNode {

    /**
     * Construct an instance of this node.
     */
    public NRMetadataInteger() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRMetadataInteger(VRMLNodeType node) {
        super(node);
    }
}
