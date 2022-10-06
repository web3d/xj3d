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
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.*;
import org.j3d.util.ErrorReporter;

/**
 * An action that can be used to view an HTML help set
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class HelpAction extends AbstractAction {

    private HelpBroker hb;
    private ErrorReporter console;

    /**
     * Create an instance of the action class.
     *
     * @param standAlone Is this standalone or in a menu
     * @param icon The icon
     * @param console the ErrorReporter for this UI
     */
    public HelpAction(boolean standAlone, Icon icon, ErrorReporter console) {

        this.console = console;
        if (standAlone && icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, "Help");
        }

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_F1,0);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_H);

        putValue(SHORT_DESCRIPTION, "Help");

        buildJavaHelp();
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener interface
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {

        // if there was a problem with buildJavaHelp, we could be null here
        if (hb != null) {
            hb.setDisplayed(true);
            hb.setCurrentView("TOC");
        } else {
            console.warningReport("Help set not available\n", null);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    private void buildJavaHelp()
    {
        File helpsetFile = new File("./doc/javaHelp","Xj3DHelpSet.hs");

        if (!helpsetFile.exists()) {
            console.warningReport("HelpAction.buildJavaHelp(): could not find " + helpsetFile, null);
            return;
        }

        HelpSet hs;
        try {
            URL url = helpsetFile.toURI().toURL();
            hs = new HelpSet(null, url);
        }
        catch (MalformedURLException | HelpSetException e) {
          console.errorReport(e.getMessage(), e);
          return;
        }
        hb = hs.createHelpBroker();
    }
}
