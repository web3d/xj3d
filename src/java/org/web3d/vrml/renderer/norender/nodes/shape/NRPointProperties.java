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

package org.web3d.vrml.renderer.norender.nodes.shape;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;
import org.web3d.vrml.renderer.common.nodes.shape.BasePointProperties;

/**
 *
 * @author terry
 */
public class NRPointProperties extends BasePointProperties
    implements NRVRMLNode {

    /**
     * Empty constructor
     */
    public NRPointProperties() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a LineProperties node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public NRPointProperties(VRMLNodeType node) {
        super(node);
    }
    
    /**
     * Accessor method to get current value of field <b>colorMode</b>.
     * @return The current value of colorMode
     */
    @Override
    public int getColorMode() {
        return colorMode;
    }
}
