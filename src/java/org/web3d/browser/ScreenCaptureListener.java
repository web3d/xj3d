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

package org.web3d.browser;

// External imports
import java.nio.Buffer;

// Local imports
// None

/**
 * Notification of Screen captures.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface ScreenCaptureListener {

    /**
     * Notification of a new screen capture.  This will be in openGL pixel
     * order.
     *
     * @param buffer The screen capture
     * @param width The width in pixels of the captured screen
     * @param height The height in pixels of the captured screen
     */
    void screenCaptured(Buffer buffer, int width, int height);
}
