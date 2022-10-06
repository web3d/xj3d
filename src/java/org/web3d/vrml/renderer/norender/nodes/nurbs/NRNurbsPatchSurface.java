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

package org.web3d.vrml.renderer.norender.nodes.nurbs;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsPatchSurface;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null-renderer implementation of NurbsSurface.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NRNurbsPatchSurface extends BaseNurbsPatchSurface
        implements NRVRMLNode {

    /**
     * Create a new default instance of the node.
     */
    public NRNurbsPatchSurface() {
    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node. If the node is not the same type, an exception will be thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public NRNurbsPatchSurface(VRMLNodeType node) {
        //super(node);
    }

    /**
     *
     */
    @Override
    public void render() {
        // TODO Auto-generated method stub
    }

    /**
     *
     * @param renderer
     */
    @Override
    public void set_renderer(Object renderer) {
        // TODO Auto-generated method stub
    }
}
