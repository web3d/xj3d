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
 * All space covered by the spatial structure
 *
 * @author Alan Hudson.
 * @version
 */
public class AllRegion implements Region {

    /**
     * Constructor.
     */
    public AllRegion() {
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof AllRegion))
            return false;
        else
            return equals((AllRegion)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(AllRegion ta) {
        return true;
    }
 }
