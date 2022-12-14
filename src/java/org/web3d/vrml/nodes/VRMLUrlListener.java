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
package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// none

/**
 * An listener for changes in a nodes URL content.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface VRMLUrlListener {

    /**
     * Notification that the Url content for this node has changed
     *
     * @param node
     * @param index The index of the field that has changed
     */
    void urlChanged(VRMLNodeType node, int index);
}
