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
 * by a tunnel.
 *
 * @author Alan Hudson.
 * @version $Id: $
 */
public class TunnelRegion implements Region {

    public enum Axis {X,Y,Z};

    /** The direction */
    private Axis dir;

    /** The location, based on free1 variable according to axis */
    private int loc1;

    /** The location, based on free2 variable according to axis */
    private int loc2;

    /** How many cells high is it, where height is in both free axis dirs */
    private int height;

    /**
     * Constructor.
     *
     * @param dir The axis of the slice
     * @param loc1 The location in grid coordinates
     * @param loc2 The location in grid coordinates
     * @param height The number of cells high
     *
     */
    public TunnelRegion(Axis dir, int loc1, int loc2, int height) {
        this.dir = dir;
        this.loc1 = loc1;
        this.loc2 = loc2;
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
     * Get the location1
     *
     * @return the value
     */
    public int getLoc1() {
        return loc1;
    }

    /**
     * Get the location2
     *
     * @return the value
     */
    public int getLoc2() {
        return loc2;
    }

    /**
     * Get the height
     *
     * @return the value
     */
    public int getHeight() {
        return height;
    }


    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof TunnelRegion))
            return false;
        else
            return equals((TunnelRegion)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(TunnelRegion ta) {
        if (dir != ta.dir)
            return false;

        if (loc1 != ta.loc1)
            return false;

        if (loc2 != ta.loc2)
            return false;

        return height == ta.height;
    }

    @Override
    public int hashCode() {
        float ret_val = 0;

        if (dir == Axis.X)
            ret_val = 0;
        else if (dir == Axis.Y)
            ret_val = 1;
        else if (dir == Axis.Z)
            ret_val = 3;

        ret_val = 31 * ret_val + Float.floatToIntBits(loc1);
        ret_val = 31 * ret_val + Float.floatToIntBits(loc2);
        ret_val = 31 * ret_val + Float.floatToIntBits(height);

        long ans = Float.floatToIntBits(ret_val);

        return (int)(ans & 0xFFFFFFFF);
    }

    @Override
    public String toString() {
        return "TunnelRegion@" + this.hashCode() + " loc1: " + loc1 + " loc2: " + loc2 + " axis: " + dir;
    }
 }
