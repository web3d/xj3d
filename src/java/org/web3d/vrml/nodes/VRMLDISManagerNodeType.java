/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
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

// External imports
// None

// Local imports
import edu.nps.moves.dis.EntityStatePdu;

/**
 * Common interface for all DIS Managers.
 * <p>
 *
 * Manages new and removed entities and any simulation PDU packets.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public interface VRMLDISManagerNodeType extends VRMLDISNodeType {

    /**
     * A new entity has arrived.
     *
     * @param espdu The new entity.
     */
    void entityArrived(EntityStatePdu espdu);

    /**
     * An entity has been removed from the simulation.
     *
     * @param node The entity being removed
     */
    void entityRemoved(VRMLDISNodeType node);
}
