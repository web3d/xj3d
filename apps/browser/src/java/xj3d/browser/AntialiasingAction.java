/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
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

// Standard library imports
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.jogamp.opengl.GLCapabilities;
import javax.swing.SwingUtilities;

// Application specific imports
import org.xj3d.ui.awt.widgets.SwingStatusBar;

/**
 * An action that can be used to change antialiasing modes.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.2 $
 */
public class AntialiasingAction extends AbstractAction {

    /** Maximum Number of antialiasing samples */
    private int maxSamples = -1;

    /** Number of antialiasing samples */
    private int numSamples = 1;

    /** The surfaceManager */
    private SurfaceManager surfaceManager;

    /** The glCapabilities chosen */
    private GLCapabilities caps;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /**
     * Create an instance of the action class.
     *
     * @param manager The surface manager
     * @param statusBar a SwingStatusBar
     */
    public AntialiasingAction(SurfaceManager manager, SwingStatusBar statusBar) {
        super("");

        surfaceManager = manager;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Cycles the antialising");

        // Get the system maximum samples
        maxSamples = MultisampleChooser.getMaximumNumSamples();
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed. This is the result of ALT-A being
     * selected to cycle through antialiasing sample rates that the
     * graphics card can support through cycling AA off.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        String val = evt.getActionCommand();

        switch (val) {
            case "Disabled":
                numSamples = 1;
                break;
            case "Cycle":
                cycleAntialiasing();
                return;
            default:
                numSamples = Integer.parseInt(val);
                break;
        }

        changeSamples();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the maximum number of samples.
     *
     * @return the maximum number of samples
     */
    public int getMaximumNumberOfSamples() {
        return maxSamples;
    }

    /**
     * Set the desired samples.  This will be capped at the current system maximum.
     *
     * @param desired The desired number of samples
     */
    public void setDesiredSamples(int desired) {
        if (desired > maxSamples)
            numSamples = maxSamples;
        else
            numSamples = desired;

        changeSamples();
    }

    /**
     * Cycle through antialiasing options.
     *
     * @param p1 The panel the surface is in.
     */
    private void cycleAntialiasing() {
        numSamples = numSamples * 2;
        if (numSamples > maxSamples)
            numSamples = 1;

        changeSamples();
    }

    /**
     * Change to the current numSamples.
     */
    private void changeSamples() {
        if (numSamples == 1)
            statusBar.setStatusText("Antialiasing disabled");
        else
            statusBar.setStatusText("Antialiasing samples: " + numSamples + " out of max: " + maxSamples);

        caps = surfaceManager.getCapabilities();

        caps.setSampleBuffers(numSamples > 1);

        caps.setNumSamples(numSamples);

        surfaceManager.resetSurface();
    }
}
