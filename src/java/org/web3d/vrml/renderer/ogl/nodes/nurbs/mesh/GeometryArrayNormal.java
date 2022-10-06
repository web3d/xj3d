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
import javax.vecmath.Vector3f;

/**
 * An interface that represents the result of tesselating a parametric surface
 * when the tesselation also returns vectors normal to the surface
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public interface GeometryArrayNormal {

    /**
     * Returns normal vectors
     *
     * Normals are oriented consistently (assuming parametric surface is
     * non-degenerate)
     *
     * Oriented so that following system of vectors in 3D Euclidean space is
     * right handed: "x" -- the direction of the "u", first parametric
     * coordinate "y" -- the direction of the "v", second, parametric coordinate
     * "z" -- the normal vector
     *
     * normals are of unit magnitude
     *
     * @return array (of length numPoints) of normal vectors
     */
    Vector3f[] normals();
}