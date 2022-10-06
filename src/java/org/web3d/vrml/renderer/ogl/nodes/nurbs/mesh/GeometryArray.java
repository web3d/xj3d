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
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;


/**
 * An interface that represents the result of tesselating a parametric surface
 *
 * Presents the result as arrays of points on the surface, points are specified
 * in real (3D space) coordinates and via the parametric coordinates
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public interface GeometryArray {

    /**
     * Return the number of points in tesselation
     *
     * @return size of real_coordinates and parametric_coordinate
     */
    int numPoints();

    /**
     * Return the real (3D Euclidean space) coordinates of points. Design intent
     * is that the returned value should not be modified by user
     *
     * @return array of coordinates
     */
    Point3f[] euclidean_coordinates();

    /**
     * Return the parametric (2D) coordinates of points. Design intent is that
     * the returned value should not be modified by user
     *
     * @return array of parametric coordinates
     */
    Point2f[] parametric_coordinates();
}