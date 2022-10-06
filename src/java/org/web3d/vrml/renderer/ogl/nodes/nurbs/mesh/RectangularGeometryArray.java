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
// None

/**
 * An extension of GeometryArray when the points defining the vertices are
 * guaranteed to form a rectangular array in parametric space
 *
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public interface RectangularGeometryArray extends GeometryArray {

    /**
     * Return the number of segments (intervals, steps) in parameter space      *
     * @return size 2 array {number of u segment, number of v segments}
     */
    int[] numSegments();

    /**
     * numDimensions[0] = numSegments[0] + 1 numDimensions[1] = numSegments[1] +
     * 1
     *
     * @return index to use for coordinates array
     */
    int[] numDimensions();

    /**
     * 0 &lt;= i &lt; numDimensions[0] 0 &lt;= j &lt; numDimensions[1]
     *
     * @param i
     * @param j
     * @return index to use for coordinates array
     */
    int indexInArray(int i, int j);

    /**
     * Return the range of parameters
     *
     * parameter_range[0] is a 2-array of min, max of u parameter
     * parameter_range[0][0] the minimum of u parameter_range[0][1] the maximum
     * of u
     *
     * parameter_range[1][0] the minimum of v parameter_range[1][1] the maximum
     * of v
     *
     * @return size 2 array {number of u segment, number of v segments}
     */
    float[][] parameter_range();
}