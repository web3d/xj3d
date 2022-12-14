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

package org.web3d.vrml.renderer.norender.nodes.sound;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.sound.BaseAudioClip;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Implementation of an AudioClip.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NRAudioClip extends BaseAudioClip
    implements NRVRMLNode {

    /**
     * Default constructor.
     */
    public NRAudioClip() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not an audioclip node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public NRAudioClip(VRMLNodeType node) {
        this(); // invoke default constructor
        copy(node);
    }
}
