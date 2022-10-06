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

package org.xj3d.sai;

// External imports
import java.awt.image.BufferedImage;

// Local imports
// None

/**
 * Notification of Screen captures from the internals of Xj3D
 * <p>
 *
 * This is the callback that is registered with the {@link Xj3DBrowser} for
 * capturing screenshots. This will be called in a blocking fashion from the
 * browser. Time spent in this method will prevent the browser from executing
 * the next frame. In order to keep performance high end user code should
 * offload processing the values coming from this method to a separate thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Xj3DScreenCaptureListener {

    /**
     * Notification of a new screen capture presented as an image. A new
     * image instance will be generated for each frame.
     *
     * @param img The screen captured image
     */
    void screenCaptured(BufferedImage img);
}
