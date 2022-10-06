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
// none

// Local imports
// none

/**
 * Defines the requirements for a node that will dynamically shift its
 * notion of the origin.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface OriginListener {

    /**
     * Notification that the origin has changed.
     */
    void originChanged();
}
