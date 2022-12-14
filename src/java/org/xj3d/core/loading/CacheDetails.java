/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.loading;

// External imports
// None

// Local imports
// None

/**
 * A data holder class that contains information for file caching during the
 * load process.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface CacheDetails {

    /** The URI that represents this object
     * @return URI that represents this object
     */
    String getURI();

    /** Get the content type of this cached object
     * @return  the content type of this cached object
     */
    String getContentType();

    /** Get the actual content of this object
     * @return  the actual content of this object
     */
    Object getContent();
}
