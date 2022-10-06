/*****************************************************************************
 *                        Web3d.org Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.nurbs.mesh;

// External imports
import org.j3d.aviatrix3d.VertexGeometry;

// SEE http://aviatrix3d.j3d.org/javadoc/index.html
// SEE http://aviatrix3d.j3d.org/javadoc/org/j3d/aviatrix3d/VertexGeometry.html

/**
 * Interface that returns an Aviatrix instance representation of this meshing
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public interface AviatrixConnector {

    /**
     * Returns aviatrix scene graph object
     *
     * @return Returns aviatrix scene graph object
     */
    VertexGeometry sceneGraphObject();
}