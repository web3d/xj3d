/*****************************************************************************
 *                        Shapeway Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.exporter;

// External Imports

// Internal Imports

/**
 * A class which can count the number of triangles in a stream.
 *
 * @author Alan Hudson
 */
public interface TriangleCounter {
    /**
     * Get the triangle count for the whole stream.
     *
     * @return The triangle count.
     */
    public int getTriangleCount();
}