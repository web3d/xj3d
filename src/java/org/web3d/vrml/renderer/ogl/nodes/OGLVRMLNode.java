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

package org.web3d.vrml.renderer.ogl.nodes;

// Standard imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of the basic VRMLNodeType specific to the OpenGL render
 * rendering system.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface OGLVRMLNode extends VRMLNodeType {

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OpenGL representation.
     */
    SceneGraphObject getSceneGraphObject();
}
