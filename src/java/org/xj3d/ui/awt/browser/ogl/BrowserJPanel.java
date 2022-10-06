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
import java.awt.event.*;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.Properties;

import javax.swing.JPanel;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.output.graphics.SimpleAWTSurface;
import org.j3d.aviatrix3d.output.graphics.SimpleNEWTSurface;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.browser.BrowserComponent;
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;

import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.core.eventmodel.DeviceFactory;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

import org.xj3d.sai.BrowserConfig;

import org.xj3d.ui.awt.device.AWTDeviceFactory;
import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;
import org.xj3d.ui.awt.widgets.*;

/**
 * Common Swing JPanel implementation of the browser component for use in
 * either SAI or EAI, that wraps the functionality of a VRML browser
 * into a convenient, easy to use form.
 * <p>
 *
 * This base class needs to be extended to provide the SAI or EAI-specific
 * implementation interfaces, as well as any startup required for both of
 * those environments, such as scripting engines etc.
 *
 * @author Justin Couch, Brad Vender
 * @version $Revision: 1.30 $
 */
public abstract class BrowserJPanel extends JPanel
    implements BrowserComponent,
               BrowserCoreListener,
               ComponentListener,
               WindowListener,
               KeyListener {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in BrowserJPanel";

    /** Wireframe rendering mode message */
    private static final String WIREFRAME_RENDERING_MODE_MSG =
        "Wireframe rendering mode enabled";

    /** Point rendering mode message */
    private static final String POINT_RENDERING_MODE_MSG =
        "Point rendering mode enabled";

    /** SHaded rendering mode message */
    private static final String SHADED_RENDERING_MODE_MSG =
        "Shaded rendering mode enabled";

    /** Framerate for paused mode */
    private static final int PAUSED_FPS = 1;

    /** The top level component that this component descends from  */
    private Window window;

    /** The real component that is being rendered to */
    private Component glCanvas;

    /** Our drawing surface */
    protected GraphicsOutputDevice surface;

    /** The toolbar holding viewpoint information */
    private ViewpointManager vpManager;

    /** The toolbar holding navigation information */
    protected SwingNavigationToolbar navToolbar;

    /** The toolbar holding location information */
    protected SwingLocationToolbar locToolbar;

    /** The status bar */
    protected SwingStatusBar statusBar;

    /** Area to push error messages to */
    protected SwingConsoleWindow console;

    /** The canvas used to display the world */
    protected OGLBrowserCanvas mainCanvas;

    /** The cursorManager */
    protected CursorManager cursorManager;

    /** The internal universe */
    protected OGLStandardBrowserCore universe;

    /** The frame cycle interval set, -1 if unset */
    private int frameMillis;

    /** Wireframe or filled mode */
    private boolean wireframe;

    /** point or filled mode */
    private boolean pointrender;

    /** The glCapabilities chosen */
    private GLCapabilities caps;

    /** Number of antialiasing samples */
    private int numSamples;

    /** HAve we used the sample chooser yet? */
    private boolean maxChooserStarted;

    /** Chooser for dealing with max multisampling */
    private SampleChooser sampleChooser;

    /** Browser config parameters */
    private BrowserConfig parameters;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    protected BrowserJPanel(BrowserConfig parameters) {
        super(new BorderLayout());

        this.parameters = parameters;

        Properties skinProperties =
            (parameters.browserSkin == null) ? new Properties() : parameters.browserSkin;

        numSamples = 1;
        frameMillis = -1;
        wireframe = false;
        pointrender = false;
        maxChooserStarted = false;

        setSize(800, 600);

        console = new SwingConsoleWindow();

        String browserType;
        if(parameters.vrml97Only) {
            browserType = "VRML";
        } else {
            browserType = "X3D";
        }

        String renderMode;
        switch (parameters.interfaceType) {
            case PARTIAL_LIGHTWEIGHT:
                renderMode = "GLCanvas (AWT)";
                break;
            case LIGHTWEIGHT:
                renderMode = "GLJPanel (Swing)";
                break;
            case NEWT:
                renderMode = "GLWindow (NEWT)";
                break;
            default:
                renderMode = "GLWindow (NEWT)";
        }

        console.messageReport("Initializing OpenGL "+ browserType +" browser in " + renderMode + " mode.\n");

        caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        int maxNum = getMaximumNumSamples();

        if(parameters.antialiased) {
            caps.setSampleBuffers(true);
            switch (parameters.antialiasingQuality) {
                case "low":
                    // Depending on max sample rate, don't let this get below 1
                    numSamples = (maxNum/4 > 2) ? numSamples : maxNum/4;
                    break;
                case "medium":
                    numSamples = maxNum/2;
                    break;
                case "high":
                    numSamples = maxNum;
                    break;
            }
            caps.setNumSamples(numSamples);
            console.messageReport("Graphics architecture will support " + numSamples + " samples of full screen antialiasing.");
        }

        switch (parameters.interfaceType) {
            case LIGHTWEIGHT:
                surface = new SimpleAWTSurface(caps, true);
                break;
            case PARTIAL_LIGHTWEIGHT:
                surface = new SimpleAWTSurface(caps);
                break;
            case NEWT:
                surface = new SimpleNEWTSurface(caps);
                break;
            default:
                surface = new SimpleNEWTSurface(caps);
        }

        surface.enableTwoPassTransparentRendering(true);
        surface.setAlphaTestCutoff(0.5f);

        glCanvas = (Component)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        mainCanvas = new OGLBrowserCanvas(surface, deviceFactory, parameters);
        mainCanvas.initialize();
        mainCanvas.setErrorReporter(console);

        glCanvas.addComponentListener(BrowserJPanel.this);
        glCanvas.addKeyListener(BrowserJPanel.this);

        cursorManager = new CursorManager(glCanvas, skinProperties, console);

        universe = mainCanvas.getUniverse();
        universe.addCoreListener(BrowserJPanel.this);
        universe.addSensorStatusListener(cursorManager);
        universe.addNavigationStateListener(cursorManager);

        vpManager = mainCanvas.getViewpointManager();

        // setup of the system properties requires the OGLBrowserCanvas
        setupProperties(parameters.textureQuality);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        add(glCanvas, BorderLayout.CENTER);

        // setup the UI components as specified in the parameters
        if((parameters.showDash && parameters.showUrl) &&
            (parameters.dashTop == parameters.urlTop)) {

            // the user has specified that they want both location and
            // navigation toolbars - and they want them both in the same
            // place. confusion reigns. the location bar will go on top -
            // end of story.
            parameters.urlTop = true;
            parameters.dashTop = false;
        }

        if(parameters.showUrl) {
            locToolbar = new SwingLocationToolbar(
                universe,
                mainCanvas.getWorldLoaderManager(),
                parameters.urlReadOnly,
                parameters.showOpenButton,
                parameters.showReloadButton,
                parameters.contentDirectory,
                skinProperties,
                console);

            if(parameters.urlTop) {
                add(locToolbar, BorderLayout.NORTH);
            } else {
                add(locToolbar, BorderLayout.SOUTH);
            }
        }

        if(parameters.showDash) {
            JPanel p2 = new JPanel(new BorderLayout());

            if(parameters.dashTop) {
                add(p2, BorderLayout.NORTH);
            } else {
                add(p2, BorderLayout.SOUTH);
            }
            navToolbar = new SwingNavigationToolbar(
                universe,
                skinProperties,
                console);

            SwingViewpointToolbar vp_tb = new SwingViewpointToolbar(
                universe,
                vpManager,
                skinProperties,
                console);

            SwingConsoleButton console_button =
                new SwingConsoleButton(console, skinProperties);

            p2.add(navToolbar, BorderLayout.WEST);
            p2.add(vp_tb, BorderLayout.CENTER);
            p2.add(console_button, BorderLayout.EAST);

            if(parameters.showFPS || parameters.showStatusBar) {
                statusBar = new SwingStatusBar(
                    universe,
                    parameters.showStatusBar,
                    parameters.showFPS,
                    skinProperties,
                    console);

                if(locToolbar != null) {
                    locToolbar.setProgressListener(statusBar.getProgressListener());
                }

                p2.add(statusBar, BorderLayout.SOUTH);
            }
        }
    }

    //----------------------------------------------------------
    // Methods overridden in Component
    //----------------------------------------------------------

    /**
     * This panel, or a container in which it 'lives' is being
     * removed from it's parent. Inform the canvas to stop rendering
     * before it's removeNotify() method is called, otherwise the
     * ui will lockup.
     */
    @Override
    public void removeNotify() {
        stop();
        if(window != null) {
            window.removeWindowListener(this);
            window = null;
        }
        super.removeNotify();
    }

    /**
     * This panel, or a container in which it 'lives' is being
     * added to a parent. Inform the canvas to start rendering.
     * By default the canvas should be enabled initially. This
     * method is in place to restart rendering in the instance
     * that the component has been removed and is being reinserted
     * into the ui.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        start();
    }

    //----------------------------------------------------------
    // Methods defined by KeyListener
    //----------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_A:
            if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {

                numSamples = numSamples * 2;

                int max = getMaximumNumSamples();

                // Busy wait till answer comes.  Should already be here
                while(max < 0) {
                    try {
                        Thread.sleep(50);
                    } catch(InterruptedException e2) {}
                    max = getMaximumNumSamples();
                }

                if(numSamples > max)
                    numSamples = 1;

                setStatusText("Antialiasing samples: "+ numSamples +" out of max: "+ max);

                caps.setSampleBuffers(true);
                caps.setNumSamples(numSamples);

                resetSurface();
            }
            break;

        case KeyEvent.VK_PAGE_DOWN:
            vpManager.nextViewpoint();
            break;

        case KeyEvent.VK_PAGE_UP:
            vpManager.previousViewpoint();
            break;

        case KeyEvent.VK_HOME:
            vpManager.firstViewpoint();
            break;

        case KeyEvent.VK_END:
            vpManager.lastViewpoint();
            break;

        case KeyEvent.VK_F:
            if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                universe.setNavigationMode("FLY");
            }
            break;

        case KeyEvent.VK_P:
            if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                pointrender = !pointrender;

                if(pointrender) {
                    setStatusText(POINT_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_POINTS);
                } else if(wireframe) {
                    setStatusText(WIREFRAME_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
                } else {
                    setStatusText(SHADED_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
                }
            }
            break;

        case KeyEvent.VK_W:
            if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                universe.setNavigationMode("WALK");
            } else if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                wireframe = !wireframe;

                if(wireframe) {
                    setStatusText(WIREFRAME_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_LINES);
                } else if(pointrender) {
                    setStatusText(POINT_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_POINTS);
                } else {
                    setStatusText(SHADED_RENDERING_MODE_MSG);
                    universe.setRenderingStyle(Xj3DConstants.RENDER_SHADED);
                }
            }
            break;

        case KeyEvent.VK_E:
            if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                universe.setNavigationMode("EXAMINE");
            }
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt) {
        glCanvas.requestFocusInWindow();
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
        if(frameMillis < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameMillis, false);

        glCanvas.requestFocusInWindow();
    }

    @Override
    public void windowIconified(WindowEvent evt) {
        mainCanvas.setMinimumFrameInterval(1_000 / PAUSED_FPS, false);
    }

    @Override
    public void windowOpened(WindowEvent evt) {
        glCanvas.requestFocusInWindow();
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
        // not explicitly stated in the javadoc -
        // componentResized is called whenever this
        // panel is realized. use it as an opportunity
        // to determine the top level component window
        // and establish a listener on it.
        if(window == null) {

            Container cnt = this;
            Container parentCnt;

            while (true) {
                parentCnt = cnt.getParent();
                if(parentCnt == null) {
                    window = (Window)cnt;
                    window.addWindowListener(this);
                    break;
                }
                cnt = parentCnt;
            }
        }
    }

    @Override
    public void componentShown(ComponentEvent evt) {
        if(frameMillis < 0)
            mainCanvas.setMinimumFrameInterval(0, false);
        else
            mainCanvas.setMinimumFrameInterval(frameMillis, false);
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
        
        // includes all JOGL and JOAL resources
        if(universe != null) {
            // if the core has already been shutdown then the
            // reference will be null. otherwise - call dispose
            // on the BrowserCore directly.
            universe.dispose();
        }
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * Ignored. Notification that the browser is shutting down the current content.
     */
    @Override
    public void browserShutdown() {
    }

    /**
     * The browser has been disposed by the user calling the
     * dispose method on the ExternalBrowser instance. Release
     * our reference to the browser core.
     */
    @Override
    public void browserDisposed() {
        universe = null;
        console.dispose();
        // rem !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // note: this is temporary to eliminate a persistant
        // reference to root that will prevent gc. this will
        // 'impact' other running browsers (if there are any).
        URI.setContentHandlerFactory(null);
        URI.setURIResourceStreamFactory(null);
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    /**
     * Ignored. The browser tried to load a URL and failed.
     *
     * @param msg An error message to go with the failure
     */
    @Override
    public void urlLoadFailed(String msg) {
    }

    /**
     * Ignored. Notification that a world has been loaded into the browser.
     *
     * @param scene The new scene that has been loaded
     */
    @Override
    public void browserInitialized(VRMLScene scene) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Update the surface to change the rendering mode, if set.
     */
    private void resetSurface() {
        remove(glCanvas);
        glCanvas.removeComponentListener(this);
        glCanvas.removeKeyListener(this);
        glCanvas.setVisible(false);
        surface.dispose();
        glCanvas = null;
        surface = null;

        switch (parameters.interfaceType) {
            case LIGHTWEIGHT:
                surface = new SimpleAWTSurface(caps, true);
                break;
            case PARTIAL_LIGHTWEIGHT:
                surface = new SimpleAWTSurface(caps);
                break;
            case NEWT:
                surface = new SimpleNEWTSurface(caps);
                break;
            default:
                surface = new SimpleNEWTSurface(caps);
        }

        surface.enableTwoPassTransparentRendering(true);
        surface.setAlphaTestCutoff(0.5f);

        glCanvas = (Component)surface.getSurfaceObject();

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            glCanvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        mainCanvas.setSurface(surface, deviceFactory);

        glCanvas.addComponentListener(this);
        glCanvas.addKeyListener(this);
        glCanvas.requestFocusInWindow();

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        add(glCanvas, BorderLayout.CENTER);
        validate();

        universe.setHardwareFOV(0);
    }

    /**
     * Get the maximum number of samples we can use.
     */
    private int getMaximumNumSamples() {
        int ret_val;

        if(!maxChooserStarted) {
            sampleChooser = new SampleChooser();

            ret_val = sampleChooser.getMaxSamples();

            maxChooserStarted = true;
        } else {
            ret_val = sampleChooser.getMaxSamples();
        }

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties(final String textureQuality) {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                String prop = System.getProperty("uri.content.handler.pkgs","");
                if(!prop.contains("vlc.net.content")) {
                    System.setProperty("uri.content.handler.pkgs",
                            "vlc.net.content");
                }
                
                prop = System.getProperty("uri.protocol.handler.pkgs","");
                if(!prop.contains("vlc.net.protocol")) {
                    System.setProperty("uri.protocol.handler.pkgs",
                            "vlc.net.protocol");
                }
                
                try {
                    // check if the image loader can be instantiated successfully
                    Class<?> cls = Class.forName("vlc.net.content.image.ImageDecoder");
                    Object obj = cls.getDeclaredConstructor().newInstance();
                    // if so, then -enable- the image loaders
                    prop = System.getProperty("java.content.handler.pkgs","");
                    if(!prop.contains("vlc.net.content")) {
                        System.setProperty("java.content.handler.pkgs",
                                "vlc.net.content");
                    }
                } catch(ClassNotFoundException | InstantiationException | IllegalAccessException t) {
                    console.warningReport("Native image loading unavailable, using default.", null);
                }
                
                BrowserCore core = mainCanvas.getUniverse();
                WorldLoaderManager wlm =
                        mainCanvas.getWorldLoaderManager();
                
                ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
                
                URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                if(!(res_fac instanceof Web3DResourceFactory)) {
                    res_fac = new Web3DResourceFactory(res_fac);
                    URI.setURIResourceStreamFactory(res_fac);
                }
                
                if(!(c_fac instanceof AWTContentHandlerFactory)) {
                    c_fac = new AWTContentHandlerFactory(core, wlm);
                    URI.setContentHandlerFactory(c_fac);
                }
                
                FileNameMap fn_map = URI.getFileNameMap();
                if(!(fn_map instanceof VRMLFileNameMap)) {
                    fn_map = new VRMLFileNameMap(fn_map);
                    URI.setFileNameMap(fn_map);
                }
                
                switch (textureQuality) {
                    case "medium":
                        System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                        System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "2");
                        break;
                    case "high":
                        System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                        System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", "16");
                        break;
                }
                
                return null;
            });
        } catch (PrivilegedActionException pae) {
            console.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }

    /**
     * Forward a message to the status bar - if it exists
     *
     * @param msg - The message to display
     */
    private void setStatusText(String msg) {
        if(statusBar != null) {
            statusBar.setStatusText(msg);
        }
    }
}
