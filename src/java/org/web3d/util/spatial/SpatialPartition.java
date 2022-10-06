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
// None

// Internal Imports

/**
 * A structure which can spatial partition items.
 *
 * @author Alan Hudson.
 */
public interface SpatialPartition {
    /**
     * Clear the structure of all data.
     */
    void clear();

    /**
     * Gets the objects in the specified region. Any object which is contained
     * or overlaps the region will be returned.  Objects exactly on a voxel
     * boundary shall be returned in all touching regions.
     *
     * @param region The region of interest.
     * @return objs The list of object ids
     */
    int[] getObjects(Region region);
}
