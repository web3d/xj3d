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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL;

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
public class ScreenShotAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /**
     * Create an instance of the action class.
     *
     * @param core
     */
    public ScreenShotAction(ErrorReporter console, OGLStandardBrowserCore core) {
        super("Single Frame");

        this.console = console;
        this.core = core;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN,0);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Take a screen shot");
    }

    //----------------------------------------------------------
    // Methods required for ScreenCaptureListener
    //----------------------------------------------------------

    /**
     * Notification of a new screen capture.  This will be in openGL pixel order.
     *
     * @param buffer The screen capture
     */
    @Override
     public void screenCaptured(Buffer buffer, int width, int height) {
        ByteBuffer pixelsRGB = (ByteBuffer) buffer;

        ScreenSaver saver = new ScreenSaver();

        int idx = 0;
        String filename = "capture" + idx;
        File file = new File(filename + ".png");

        while(file.exists()) {
            idx++;
            filename = "capture" + idx;
            file = new File(filename + ".png");
        }

        saver.saveScreen(buffer, filename, width, height);

        console.messageReport("Screen shot saved to " + filename + ".png");
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
        core.captureScreenOnce(this);
    }
}
