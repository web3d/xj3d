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
 * A structure which describes a region of space described
 * by a plane.
 *
 * @author Alan Hudson.
 * @version $Id: $
 */
public class SliceRegion implements Region {
    
    public enum Axis {X,Y,Z};

    /** The direction */
    private Axis dir;

    /** The location, based on free variable according to axis */
    private int loc;

    /** How many cells high is it, where height is in axis dir */
    private int height;

    /**
     * Constructor.
     *
     * @param dir The axis of the slice
     * @param loc The location in grid coordinates
     * @param height The number of cells high
     *
     */
    public SliceRegion(Axis dir, int loc, int height) {
        this.dir = dir;
        this.loc = loc;
        this.height = height;
    }

    /**
     * Get the direction
     *
     * @return the value
     */
    public Axis getDir() {
        return dir;
    }

    /**
     * Get the location
     *
     * @return the value
     */
    public int getLoc() {
        return loc;
    }

    /**
     * Get the height
     *
     * @return the value
     */
    public int getHeight() {
        return height;
    }
}
