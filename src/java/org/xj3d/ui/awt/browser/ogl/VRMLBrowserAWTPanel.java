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
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.web3d.browser.BrowserComponent;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;
import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.VRML97ResourceFactory;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.browser.VRML97CommonBrowser;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.eai.EAIBrowser;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoader;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.sai.BrowserConfig;
import org.xj3d.ui.awt.device.AWTDeviceFactory;

import org.xj3d.ui.awt.widgets.*;

import vrml.eai.Browser;
import vrml.eai.VrmlComponent;

/**
 * A browser that uses the AWT panel and labels to draw render the
 * UI with.
 *  <p>
 *
 * VRMLBrowserAWTPanel is the AWT based alternative to BrowserJPanel.
 * At the moment, it offers minimal functionality.
 * The "dashboard" is a text label, there aren't any navigation functions.
 * That, and urlReadOnly, urlTop, and dashTop are ignored.
 *
 * @author Brad Vender, Justin Couch
 * @version $Revision: 1.9 $
 */
public class VRMLBrowserAWTPanel extends Panel
    implements VrmlComponent,
               BrowserComponent,
               ComponentListener,
               WindowListener {

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** The Browser instance this is the display for */
    private EAIBrowser eaiBrowser;

    /** The real component that is being rendered to */
    private Canvas glCanvas;

    /** The canvas used to display the world */
    private OGLBrowserCanvas mainCanvas;

    /** The universe to place our scene into */
    private OGLStandardBrowserCore universe;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The Label to show the description text on */
    private Label descriptionLabel;

    /** The Label to show the current URL text */
    private Label urlLabel;

    /** Area to push error messages to */
    private AWTConsoleWindow console;

    /** The frame cycle interval set, -1 if unset */
    private int frameCycleTime;

    /** The OpenGL drawing surface */
    private GraphicsOutputDevice surface;

    /**
     * Create a VrmlComponent that belongs to an AWT panel.
     * and in that process construct the
     *  corresponding Browser, and the infrastructure required.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    public VRMLBrowserAWTPanel( BrowserConfig parameters ) {
        super(new BorderLayout());

        setSize(800, 600);

        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        surface = new SimpleAWTSurface(caps);

        glCanvas = (Canvas)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas, Xj3DConstants.OPENGL_ID,
            surface,
            console );

        mainCanvas = new OGLBrowserCanvas(surface, deviceFactory, parameters);
        mainCanvas.initialize();
        mainCanvas.setErrorReporter(console);

        glCanvas.addComponentListener(VRMLBrowserAWTPanel.this);
        //glCanvas.addKeyListener(this);

        descriptionLabel = new Label();
        urlLabel = new Label();

        add(glCanvas, BorderLayout.CENTER);

        // Atleast humor the idea of parameters.
        if (parameters.showDash) {
          add(descriptionLabel, BorderLayout.SOUTH);
          add(urlLabel, BorderLayout.NORTH);
        }

        RouteManager route_manager = mainCanvas.getRouteManager();
        universe = mainCanvas.getUniverse();

        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        worldLoader = mainCanvas.getWorldLoaderManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();

        console = new AWTConsoleWindow();

        ScriptEngine jsai = new VRML97ScriptEngine(universe,
                                                   route_manager,
                                                   state_manager,
                                                   worldLoader);
        jsai.setErrorReporter(console);

        ScriptEngine ecma = new JavascriptScriptEngine(universe,
                                                       route_manager,
                                                       state_manager,
                                                       worldLoader);
        ecma.setErrorReporter(console);

        s_loader.registerScriptingEngine(jsai);
        s_loader.registerScriptingEngine(ecma);

        setupProperties(universe, worldLoader);

        VRMLNodeFactory fac;
        /** This code was originally trying to use the browser ID, which doesn't work
         * when trying to substitute 'aviatrix3d' for 'ogl' in the profile loading.
         * Manually map between the two and then force the VRML97 profile since
         * the factory will only be used for replaceWorld scene building.
         */
        switch (universe.getRendererType()) {
        case Xj3DConstants.JAVA3D_RENDERER:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.JAVA3D_RENDERER);
            break;
        case Xj3DConstants.OPENGL_RENDERER:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.OPENGL_RENDERER);
            break;
        default:
            fac=DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
            break;
        }
        fac.setSpecVersion(2,0);
        fac.setProfile("VRML97");

        VRML97CommonBrowser browser_impl =
            new VRML97CommonBrowser(universe,
                                    route_manager,
                                    state_manager,
                                    worldLoader,
                                    fac);

        browser_impl.setErrorReporter(console);

        ExternalEventQueue eventQueue = new ExternalEventQueue(console);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        eaiBrowser = new EAIBrowser(universe,
                                    browser_impl,
                                    eventQueue,
                                    console);

        mainCanvas.setErrorReporter(console);

        if(parameters.showConsole)
            console.setVisible(true);
    }

    //-----------------------------------------------------------------------
    // Methods defined by VRMLComponent
    //-----------------------------------------------------------------------

    @Override
    public Browser getBrowser() {
        return eaiBrowser;
    }

    @Override
    public Object getImplementation() {
        return surface;
    }

    @Override
    public void shutdown() {
        eaiBrowser.dispose();
    }

    //----------------------------------------------------------
    // Methods defined by BrowserComponent
    //----------------------------------------------------------

    @Override
    public int supportedSpecificationVersion() {
        return 3;
    }

    @Override
    public Object getCanvas() {
        return glCanvas;
    }

    @Override
    public int getRendererType() {
        return universe.getRendererType();
    }

    @Override
    public BrowserCore getBrowserCore() {
        return universe;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return console;
    }

    @Override
    public void setMinimumFrameInterval(int millis, boolean userSet) {
        mainCanvas.setMinimumFrameInterval(millis, userSet);
        frameCycleTime = millis;
    }

    @Override
    public void start() {
        mainCanvas.setEnabled(true);
    }

    @Override
    public void stop() {
        mainCanvas.setEnabled(false);
    }

    @Override
    public void destroy() {
        mainCanvas.setEnabled(false);
        mainCanvas.browserShutdown();
    }

    //----------------------------------------------------------
    // Methods defined by ComponentListener
    //----------------------------------------------------------

    @Override
    public void componentHidden(ComponentEvent evt) {
        mainCanvas.setMinimumFrameInterval(1_000 / PAUSED_FPS, false);
    }

    @Override
    public void componentMoved(ComponentEvent evt) {
    }

    @Override
    public void componentResized(ComponentEvent evt) {
        // Reget the parent each time as it might have changed.  Que changes
        // by resize, correct?
        Container cnt = this.getParent();
        Container tmpCnt;

        while(true) {
            tmpCnt = cnt.getParent();
            if (tmpCnt == null)
                break;

            cnt = tmpCnt;
        }

        ((Window)cnt).addWindowListener(this);
    }

    @Override
    public void componentShown(ComponentEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt) {
    }


    @Override
    public void windowClosed(WindowEvent evt) {
    }

    @Override
    public void windowClosing(WindowEvent evt) {
    }

    @Override
    public void windowDeactivated(WindowEvent evt) {
    }

    @Override
    public void windowDeiconified(WindowEvent evt) {
        if (frameCycleTime < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameCycleTime, false);
    }

    @Override
    public void windowIconified(WindowEvent evt) {
        mainCanvas.setMinimumFrameInterval(1_000 / PAUSED_FPS, false);
    }

    @Override
    public void windowOpened(WindowEvent evt) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the scene object being rendered by this panel.
     *
     * @return The current scene.
     */
    public VRMLScene getScene() {
        return universe.getScene();
    }

    /**
     * Get the universe underlying this panel.
     *
     * @return The universe.
     */
    public OGLStandardBrowserCore getUniverse() {
        return universe;
    }

    /**
     * Change the panels content to the provided URL.
     *
     * @param url The URL to load.
     * @throws IOException On a failed load
     */
    public void loadURL(String url) throws IOException {
        WorldLoader wl = worldLoader.fetchLoader();
        InputSource source = new InputSource(url);

        VRMLScene scene = wl.loadNow(universe, source, false, 2, 0);
        universe.setScene(scene, null);
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    private void setupProperties(final BrowserCore core,
                                 final WorldLoaderManager wlm) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                    public Object run() {
                        String prop = System.getProperty("uri.content.handler.pkgs","");
                        if (!prop.contains("vlc.net.content")) {
                            System.setProperty("uri.content.handler.pkgs",
                                "vlc.net.content");
                        }

                        prop = System.getProperty("uri.protocol.handler.pkgs","");
                        if (!prop.contains("vlc.net.protocol")) {
                            System.setProperty("uri.protocol.handler.pkgs",
                                "vlc.net.protocol");
                        }

                        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                        if(!(res_fac instanceof VRML97ResourceFactory)) {
                            res_fac = new VRML97ResourceFactory(res_fac);
                            URI.setURIResourceStreamFactory(res_fac);
                        }

                        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();

                        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
                            c_fac = new VRMLContentHandlerFactory(core, wlm);
                            URI.setContentHandlerFactory(c_fac);
                        }

                        FileNameMap fn_map = URI.getFileNameMap();
                        if(!(fn_map instanceof VRMLFileNameMap)) {
                            fn_map = new VRMLFileNameMap(fn_map);
                            URI.setFileNameMap(fn_map);
                        }
                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
            System.err.println("Error setting Properties in BrowserJPanel");
        }
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        mainCanvas.setEnabled(true);
    }
}
