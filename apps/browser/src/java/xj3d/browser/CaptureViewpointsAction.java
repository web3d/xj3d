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
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import java.nio.Buffer;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.web3d.browser.ScreenCaptureListener;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.xj3d.core.eventmodel.ViewpointManager;

/**
 * An action that takes a screen shot of all the top-level viewpoints
 *
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */
public class CaptureViewpointsAction extends AbstractAction
    implements ScreenCaptureListener {

    /** The render manager */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** The current name */
    private String currentName;

    /** The current unknown count */
    private int unknownCount;

    /** The manager of viewpoints that we use to change them on the fly */
    private ViewpointManager vpManager;

    /** Has the capture happened */
    private boolean captured;

    /** The basename */
    private String basename;

    /**
     * Create an instance of the action class.
     *
     * @param console our ErrorReporter
     * @param core the BrowserCore instance
     * @param vpMgr our ViewPointManager
     */
    public CaptureViewpointsAction(ErrorReporter console,
        OGLStandardBrowserCore core, ViewpointManager vpMgr) {
        super("Capture Viewpoints");

        vpManager = vpMgr;
        this.console = console;
        this.core = core;

        //KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN,0);

        //putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Capture Viewpoints");
    }

    /**
     * Set the basename to use on file output
     *
     * @param basename The basename
     */
    public void setBasename(String basename) {
        this.basename = basename;
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

        ScreenSaver saver = new ScreenSaver();
        saver.saveScreen(buffer, currentName, width, height);

        console.messageReport("Screen shot saved to: " + currentName + ".png");
        captured = true;
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

        List<VRMLViewpointNodeType> vp_list = vpManager.getActiveViewpoints();

        int len = vp_list.size();

        if (len == 0) {
            currentName = basename + ".x3d" + "_VP_Unnamed_1";
            capture();
            return;
        }

        for (VRMLViewpointNodeType vp : vp_list) {
            if(!vp.getIsBound()) {
//                System.out.println("Binding to: " + vp);
                vpManager.setViewpoint(vp);
            }
            String description = vp.getDescription();
            if (description != null) {
                description = description.replace(" ", "_");
                description = description.replace("\\","_");
                description = description.replace("/", "_");
				// omit special characters: apostrophe and quotation mark
				description = description.replace("'",  "");
				description = description.replace("\"", "");
				description = description.replace("+",  "");
            } else {
                unknownCount++;
                description = "ViewpointMissingDescription_" + unknownCount;
            }
            currentName = basename + ".x3d" + "._VP_" + description;

            // This is key in letting the viewpoint fully render before an
            // adequate screep capture can occur
            try {
                Thread.sleep(250);
            } catch(InterruptedException e) {}
            capture();
        }
    }

    /**
     * Capture the current screen.
     */
    private void capture() {
        captured = false;
        core.captureScreenOnce(this);

        while(!captured) {
            Thread.yield();
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    /**
     * Set the surface we are rendering on.
     * @param surface
     */
    public void setSurface(GraphicsOutputDevice surface) {
        this.surface = surface;
    }
}
