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

package org.web3d.vrml.renderer.common.input;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLDragSensorNodeType;

/**
 * A listener to notify that a {@link org.web3d.vrml.nodes.VRMLDragSensorNodeType}
 * has been activated by the user interface.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface DragSensorSelectionListener {

    /**
     * Invoked when a pointing device is in contact with a tracker capable of picking.
     * @param node
     */
    void deviceSelectable(VRMLDragSensorNodeType node);

    /**
     * Invoked when a link node is contact with a tracker capable of picking.
     * @param node
     */
    void deviceNonSelectable(VRMLDragSensorNodeType node);
}
