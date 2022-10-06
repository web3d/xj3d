/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
// None

/**
 * Interception listener for anchor handling that allows an external user to
 * replace or supplement the browser's built in processing.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Xj3DAnchorListener {

    /**
     * Notification that the given link has been activated. If your code wants
     * to process this URL then return a value of true and the browser will
     * not do anything further. If you do not wish to process this URL in order
     * to let the browser handle it, then return false
     *
     * @param url The value of the URL field of the Anchor node that was
     *   selected
     * @param param The value of the parameter field of the Anchor node that
     *   was selected
     * @return true if the browser should not perform any further processing,
     *   false when the browser should continue normal functionality
     */
    boolean processLinkActivation(String[] url, String[] param);
}
