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

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import java.util.Properties;

import javax.swing.*;

import org.ietf.uri.ResourceConnection;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLScene;

/**
 * A swing panel that implements a simple status bar capability with a
 * text readout and frames per second counter.
 * <p>
 *
 * A status bar automatically registers a global
 * {@link SwingProgressListener}, so there is no need to create your own in
 * your application.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class SwingStatusBar extends JPanel implements BrowserCoreListener {

    /** Default properties object */
    private static final Properties DEFAULT_PROPERTIES = new Properties();

    /** 0.5 seconds */
    private static final int DELAY = 500;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The label for status messages */
    private JLabel statusLabel;

    /** The last FPS, used to avoid garbage generation. */
    private float lastFPS;

    /** Label for frames per second. */
    private JLabel fpsLabel;

    /** A progress bar for main file loading */
    private JProgressBar progressBar;

    /** The core of the browser to register nav changes with */
    private BrowserCore browserCore;

    /** The progress listener */
    private SwingProgressListener dlListener;

    /** The run state of the statusThread */
    private boolean runStatusThread;

    class myUpdateFpsTask implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (!runStatusThread) {
                ((Timer)e.getSource()).stop();
                return;
            }

            float fps = browserCore.getCurrentFrameRate();

            if (Math.abs(lastFPS - fps) > 0.1) {

                // TODO: Need todo this in a non-garbage generating way
                String txt = Float.toString(fps);
                if (txt.equals("Infinity")) {
                    lastFPS = 999.9f;
                    txt = "999.9";
                }

                int len = txt.length();

                txt = txt.substring(0, Math.min(5, len));
                if (len < 5) {
                    len = 5 - len;
                    for (int i = 0; i < len; i++) {
                        txt += " ";
                    }
                }

                fpsLabel.setText(txt);
                lastFPS = fps;
            }
        }
    }

    /**
     * Create an instance of the panel configured to show or hide the controls
     * as described.
     *
     * @param core The browser core implementation to send nav changes to
     * @param showStatusBar true to show a status bar
     * @param showFPS true to show the current FPS
     * @param skinProperties Properties object specifying image names
     * @param reporter The reporter instance to use or null
     */
    public SwingStatusBar(BrowserCore core,
        boolean showStatusBar,
        boolean showFPS,
        Properties skinProperties,
        ErrorReporter reporter) {

        super(new BorderLayout());

        browserCore = core;
        browserCore.addCoreListener( SwingStatusBar.this );

        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        if(skinProperties == null)
            skinProperties = DEFAULT_PROPERTIES;

        JPanel rightPanel = new JPanel(new BorderLayout());

        add(rightPanel, BorderLayout.EAST);

        if(showFPS) {
            fpsLabel = new JLabel();
            rightPanel.add(fpsLabel, BorderLayout.EAST);

            new Timer(DELAY, new myUpdateFpsTask()).start();
            runStatusThread = true;
        }

        if(showStatusBar) {
            statusLabel = new JLabel();
            add(statusLabel, BorderLayout.WEST);

            progressBar = new JProgressBar();

            dlListener =
                new SwingProgressListener(statusLabel, progressBar, rightPanel, BorderLayout.WEST, reporter);

            ResourceConnection.addGlobalProgressListener(dlListener);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------------

    @Override
    public void browserInitialized( VRMLScene scene ) {
    }

    @Override
    public void urlLoadFailed( String msg ) {
    }

    @Override
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed, release the progress listener and
     * stop the fps status thread if necessary.
     */
    @Override
    public void browserDisposed() {
        if ( dlListener != null ) {
            ResourceConnection.removeGlobalProgressListener( dlListener );
        }
        runStatusThread = false;
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Return the progress listener for download progress.
     * @return the progress listener for download progress
     */
    public ProgressListener getProgressListener() {
        return dlListener;
    }

    /**
     * Update the status bar text message to say this.
     *
     * @param msg The message to display
     */
    public void setStatusText(final String msg) {

        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (statusLabel != null) {
                    statusLabel.setText(msg);
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }
}
