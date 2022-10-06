/*****************************************************************************
 *                        Web3d.org Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

// Local imports
// none

/**
 * Defines the requirements for the manager of the origin for nodes
 * that will dynamically shift.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface OriginManager {

    /**
     * Return the coordinate to be used as the origin.
     *
     * @return The object containing the origin, or null if one has
     * not been calculated.
     */
    Vector3d getOrigin();

    /**
     * Notify the manager of a change in view position, return
     * whether this change has caused a recalculation of the origin.
     *
     * @param position The new view position
     * @return true if the origin has changed, false otherwise.
     */
    boolean updateViewPosition(Vector3f position);

    /**
     * Return whether Geo* nodes should use this manager as their source
     * for origin data. If disabled, then Geo* nodes should use the GeoOrigin
     * node for data.
     *
     * @return true if Geo* nodes should use this manager.
     * false if nodes should the GeoOrigin node.
     */
    boolean getEnabled();
}
