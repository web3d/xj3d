/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External imports
import java.io.*;

import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.browser.ScreenCaptureListener;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

/**
 * An action that takes a screen shot of the current content.
 *
 * Currently saves to user.dir/foo.png should add a file dialog box.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class MovieAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /** Is this the start or end action */
    private boolean start;

    /** The frame number */
    private int frame;

    /**
     * Create an instance of the action class.
     *
     * @param core
     */
    public MovieAction(boolean start, ErrorReporter console, OGLStandardBrowserCore core) {
        super("Single Frame");

        if (start) {
            putValue(Action.NAME, "Start Movie");
            putValue(SHORT_DESCRIPTION, "Start Movie Recording");
        } else {
            putValue(Action.NAME, "End Movie");
            putValue(SHORT_DESCRIPTION, "End Movie Recording");
        }

        this.start = start;
        this.console = console;
        this.core = core;
        frame = 0;
    }

    //----------------------------------------------------------
    // Methods required for ScreenCaptureListener
    //----------------------------------------------------------

    /**
     * Notification of a new screen capture.  This will be in openGL pixel order.
     *
     * @param buffer The screen capture
     * @param width The width of the captured image in pixels
     * @param height The height of the captured image in pixels
     */
    @Override
     public void screenCaptured(Buffer buffer, int width, int height) {
        ByteBuffer pixelsRGB = (ByteBuffer) buffer;

        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the
        // rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL.

        int p = width * height * 3; // Points to first byte (red) in each row.
        int q;                  // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int w3 = width*3;         // Number of bytes in each row

        for (int row = 0; row < height; row++) {
            p -= w3;
            q = p;
            for (int col = 0; col < width; col++) {
                int iR = pixelsRGB.get(q++);
                int iG = pixelsRGB.get(q++);
                int iB = pixelsRGB.get(q++);

                pixelInts[i++] = 0xFF00_0000
                             | ((iR & 0x0000_00FF) << 16)
                             | ((iG & 0x0000_00FF) << 8)
                             | (iB & 0x0000_00FF);
            }

        }

        BufferedImage bufferedImage =
               new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);

        try {
            File outputFile = new File("capture_" + String.format("%0,4d",frame) + ".png");
            ImageIO.write(bufferedImage, "PNG", outputFile);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        frame++;
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        if (start) {
            core.captureScreenStart(this);
        } else {
            core.captureScreenEnd();
        }
    }
}
