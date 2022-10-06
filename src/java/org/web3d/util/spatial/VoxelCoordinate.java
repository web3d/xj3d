/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

/**
 * Voxel coordinate holder.  Used to reference a voxels position.
 * Typically stored in a Java collection object.
 *
 * This structure will be optimized for memory savings in the future.
 *
 * @author Alan Hudson
 */

public class VoxelCoordinate {
    protected int x;
    protected int y;
    protected int z;

    public VoxelCoordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof VoxelCoordinate))
            return false;
        else
            return equals((VoxelCoordinate)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(VoxelCoordinate ta) {
        return (ta.x == this.x && ta.y == this.y && this.z == ta.z);
    }

    @Override
    public int hashCode() {
        int ret_val = 31 * 31 * x + 31 * y + z;
        // potential change: 31 * 31 * x ^ 31 * y ^ z
        // pot 2:
        // h ^= x;
        // h ^= ( h << 5 ) + ( h >> 2 ) + y;
        // h ^= ( h << 5 ) + ( h >> 2 ) + z;

        return ret_val;
    }

    public void setValue(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
