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

// Standard library imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

// Application specific imports
import org.xj3d.ui.awt.widgets.SwingStatusBar;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

/**
 * An action that can be used to change rendering style to lines
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class LinesStyleAction extends AbstractAction implements RenderStyle {

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** The browser core */
    protected BrowserCore universe;

    /** Is rendering style active */
    private boolean active;

    /** The other styles */
    private RenderStyle[] otherStyles;

    /**
     * Create an instance of the action class.
     *
     * @param core The Browser core instance
     * @param statusBar the SwingStatusBar instance
     */
    public LinesStyleAction(BrowserCore core, SwingStatusBar statusBar) {
        super("Lines");

        universe = core;
        this.statusBar = statusBar;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                   KeyEvent.ALT_DOWN_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(SHORT_DESCRIPTION, "Wireframe style rendering");

    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

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
        if (!active) {
            for (RenderStyle otherStyle : otherStyles) {
                otherStyle.reset();
            }

            statusBar.setStatusText("Wireframe rendering mode enabled");
            universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
            active = true;
        } else {
            statusBar.setStatusText("Shaded rendering mode enabled");
            universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
            active = false;
        }
    }

    //---------------------------------------------------------------
    // Methods defined by RenderStyle
    //---------------------------------------------------------------

    /**
     * Sets this style to non active.  Used when Shaded mode is selected.
     */
    @Override
    public void reset() {
        active = false;
    }

    /**
     * Set the render styles linked to this one.  If one is enabled
     * the others will be disabled.
     *
     * @param linked The linked styles
     */
    @Override
    public void setLinkedStyles(RenderStyle[] linked) {
        this.otherStyles = new RenderStyle[linked.length];
        System.arraycopy(linked, 0, this.otherStyles, 0, linked.length);
    }
}
