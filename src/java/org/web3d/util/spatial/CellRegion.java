/*****************************************************************************
 *                        Yumetech Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util.spatial;

// External Imports

// Internal Imports

/**
 * A structure which describes a region of space.
 *
 * @author Alan Hudson.
 */
public class CellRegion implements Region {
    
    /** The x voxel coordinate */
    private int x;

    /** The y voxel coordinate */
    private int y;

    /** The z voxel coordinate */
    private int z;

    /**
     * Constructor.
     *
     * @param x - The x voxel coordinate
     * @param y - The y voxel coordinate
     * @param z - The z voxel coordinate
     */
    public CellRegion(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the x coordinate.
     *
     * @return the value
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y coordinate.
     *
     * @return the value
     */
    public int getY() {
        return y;
    }

    /**
     * Get the z coordinate.
     *
     * @return the value
     */
    public int getZ() {
        return z;
    }
}
