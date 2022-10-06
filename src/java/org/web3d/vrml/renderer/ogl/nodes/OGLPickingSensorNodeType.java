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

package org.web3d.vrml.renderer.ogl.nodes;

// External imports
import java.util.Map;

import org.j3d.aviatrix3d.Group;
import org.j3d.aviatrix3d.picking.PickableObject;
import org.web3d.vrml.lang.VRMLNode;

// Local imports
import org.web3d.vrml.nodes.VRMLPickingSensorNodeType;

/**
 * OpenGL abstract representation of a picking sensor node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface OGLPickingSensorNodeType
    extends OGLVRMLNode, VRMLPickingSensorNodeType {

    /**
     * Set a parent to this sensor. Really doesn't matter if one overwrites
     * another. The parent should only be needed to work out where the sensor
     * is in world space coordinates, since the sensor itself never has an
     * underlying scene graph object representation. In addition, if a picking
     * sensor has more than one parent in the transformation hierarchy, the
     * results are entirely bogus anyway.
     *
     * @param group The parent group of this sensor
     */
    void setParentGroup(Group group);

    /**
     * Fetch the parent grouping node from this sensor so that we can track
     * back up the stack for the world transformation.
     *
     * @return The currently set parent transform
     */
    Group getParentGroup();

    /**
     * Get the set of target PickableObjects that this sensor manages. If there
     * are none, return an empty set.
     *
     * @return A set of OGL nodes mapped to their VRML wrapper
     */
    Map<PickableObject, VRMLNode> getTargetMapping();

    /**
     * Get the collection of target PickableObjects that this sensor manages.
     *
     * @return A set of OGL nodes for pick testing
     */
    PickableObject[] getTargetObjects();

    /**
     * Set the flag convertor that will be used to map the object type strings
     * to the internal pick masks. A value of null will clear the current
     * instance.
     *
     * @param conv The convertor instance to use, or null
     */
    void setTypeConvertor(OGLPickingFlagConvertor conv);

    /**
     * Get the int mask used to perform picking with. This is the mask
     * generated after passing the collection of pick string flags through the
     * picking flag convertor.
     *
     * @return The pick bitmask to use
     */
    int getPickMask();
}

