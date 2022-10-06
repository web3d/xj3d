/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of the abstract node type X3DSRFParametersNode node.
 * <p>
 *
 * This abstract node type does not have any fields, so mostly it is a placeholder
 * class to make our spec handling easier.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseGeoSRFParametersNode extends AbstractNode {

    /** The last index of the nodes used by the SRFParametersInfoNode */
    protected static final int LAST_SRF_PARAM_INDEX = LAST_NODE_INDEX;

    /**
     * Construct a default instance of this node type. The defaults are set by the
     * X3D specification.
     *
     * @param name The name of the type of node
     */
    protected BaseGeoSRFParametersNode(String name) {
        super(name);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected void copy(BaseGeoSRFParametersNode node) {
       // does nothing
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.GeoSRFParamNodeType;
    }
}

