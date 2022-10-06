/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.browser.ProfilingInfo;
import org.web3d.browser.ProfilingListener;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.browser.OGLProfilingInfo;

/**
 * An action that prints profiling about the scene.
 *
 * Currently prints to console.  Might be better as a Dialog.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ProfilingInfoAction extends AbstractAction
   implements ProfilingListener {

    /** The core */
    private OGLStandardBrowserCore core;

    /** The console to print information to */
    private ErrorReporter console;


    /** Are we actively printing */
    private boolean active;

    /**
     * Create an instance of the action class.
     *
     * @param console The error reporter
     * @param core The browser core
     */
    public ProfilingInfoAction(ErrorReporter console, OGLStandardBrowserCore core) {
        super("Profiling Info");

        this.console = console;
        this.core = core;
        active = false;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_I,
                                                   KeyEvent.ALT_DOWN_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        putValue(SHORT_DESCRIPTION, "View Profiling Information");
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        if (!active) {
            console.messageReport("Starting Profiling Log");
            active = true;
            core.addProfilingListener(this);
        } else {
            console.messageReport("Ending Profiling Log");
            active = false;
            core.removeProfilingListener(this);
        }
    }

    //----------------------------------------------------------
    // Methods required for OGLProfilingListener
    //----------------------------------------------------------

    /**
     * The profiling data has changed.  This will happen at the end of each frame render.
     *
     * @param info The profiling data
     */
    @Override
    public void profilingDataChanged(ProfilingInfo info) {
		OGLProfilingInfo data = (OGLProfilingInfo)info;
        console.messageReport("Timing(ms): Scene: " + (data.sceneRenderTime / 1_000_000) + " Cull: " + (data.sceneCullTime / 1_000_000) + " Sort: " + (data.sceneSortTime / 1_000_000) + " draw: " + (data.sceneDrawTime / 1_000_000));
    }

}
