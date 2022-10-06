/*****************************************************************************
 *                        Yumetech, Inc Copyright (c) 2007-2008
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

// Local imports
import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserListener;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DScene;

import org.xj3d.sai.Xj3DBrowser;
import org.xj3d.sai.Xj3DStatusListener;

/**
 * A testcase for the Xj3DStatusListener functionality exposed in the Xj3DBrowser interface.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Xj3DStatusBrowser extends JFrame implements BrowserListener, Xj3DStatusListener {

    Container contentPane;

    X3DComponent x3dComponent;

    Xj3DBrowser browser;
    X3DScene scene;

    private long startTime;

    public Xj3DStatusBrowser() {
        super("Xj3DBrowser");

        System.setProperty("x3d.sai.factory.class",
            "org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl");

        //System.setProperty("org.xj3d.core.loading.threads", "4");

        contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        createBrowser();
        contentPane.add((Component)x3dComponent, BorderLayout.CENTER);
        setVisible(true);
    }

    //---------------------------------------------------------
    // Methods defined by BrowserListener
    //---------------------------------------------------------

    /** The Browser Listener.
     * @param be */
    @Override
    public void browserChanged(final BrowserEvent be) {
        final int id = be.getID();
        if (id == BrowserEvent.INITIALIZED) {
            System.out.println("Initial scene loaded. Load time: " + (System.currentTimeMillis() - startTime));
        }
        else if (id == BrowserEvent.SHUTDOWN) {
            System.out.println("SHUTDOWN");
        }
    }

    //---------------------------------------------------------
    // Methods defined by BrowserListener
    //---------------------------------------------------------

    /**
     * Notification that a single line status message has changed to the new
     * string. A null string means to clear the currently displayed message.
     *
     * @param msg The new message string to display for the status
     */
    @Override
    public void updateStatusMessage(String msg) {
    }

    /**
     * Notification that the calculated frames per second has changed to this
     * new value. It is expected that this is called frequently.
     * @param fps
     */
    @Override
    public void updateFramesPerSecond(float fps) {
        //System.out.println("Got fps: " + fps);
    }

    /**
     * Notification of a progress update. There may be several items in
     * progression at once (eg multithreaded texture and scripting loading)
     * so implementers should work appropriately for this situation. To keep
     * this aligned, each item that is reporting progress will have a unique
     * ID string (for this session) associated with it so you can keep track
     * of the multiples. Once 100% has been reached you can assume that the
     * tracking is complete for that object.
     *
     * @param id A unique ID string for the given item
     * @param msg A message to accompany the update
     * @param perc A percentage from 0-100 of the progress completion
     */
    @Override
    public void progressUpdate(String id, String msg, float perc) {
        System.out.println("pu: " + id + " msg: " + msg + " perc: " + perc);
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    public static void main(String[] args) {
        Xj3DStatusBrowser frame = new Xj3DStatusBrowser();

        frame.pack();
        frame.setSize(512, 512);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        frame.loadContent();
    }

    private void createBrowser() {
        Map<String, Object> params = new HashMap<>();
        params.put("Xj3D_ShowConsole", Boolean.FALSE);
        params.put("Xj3D_NavbarShown", Boolean.TRUE);
        params.put("Xj3D_StatusBarShown", Boolean.TRUE);
        params.put("Xj3D_FPSShown", Boolean.TRUE);
        params.put("Xj3D_NavbarPosition", "bottom");
        params.put("Xj3D_LocationShown", Boolean.TRUE);
        params.put("Xj3D_LocationPosition", "top");
        //params.put("Xj3D_ContentDirectory", System.getProperty("user.dir"));
        params.put("Xj3D_OpenButtonShown", Boolean.TRUE);
        params.put("Xj3D_ReloadButtonShown", Boolean.TRUE);
        params.put("Xj3D_Culling_Mode", "none");

        x3dComponent = BrowserFactory.createX3DComponent(params);
        browser = (Xj3DBrowser)x3dComponent.getBrowser();
        browser.addBrowserListener(this);
        browser.addStatusListener(this);
    }

    public void loadContent() {
        System.out.println("Loading content");
        startTime = System.currentTimeMillis();

//        scene = browser.createX3DFromURL(new String[] {"part3a-step00.x3db"});
        scene = browser.createX3DFromURL(new String[] {"TextureMemory.x3dv"});

        browser.replaceWorld(scene);
    }

    @Override
    public void loadUpdate(int numInProgress) {
        System.out.println("numInProgress is: " + numInProgress);
    }
}

