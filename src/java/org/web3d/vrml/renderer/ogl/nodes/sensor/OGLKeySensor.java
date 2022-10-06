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

package org.web3d.vrml.renderer.ogl.nodes.sensor;

// Standard imports
import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.sensor.BaseKeySensor;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * OpenGL implementation of a KeySensor node.
 * <p>
 *
 * The definitions of action keys between VRML and Java are a little
 * different. VRML defines the arrow keys as action keys where Java does
 * not.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLKeySensor extends BaseKeySensor
    implements OGLVRMLNode {

    /**
     * Construct a default viewpoint instance
     */
    public OGLKeySensor() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLKeySensor(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
