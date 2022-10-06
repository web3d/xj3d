/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001 - 2007
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

package xj3d.browser;

// External imports
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.MalformedURLException;
import java.net.URL;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.*;

import javax.swing.*;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.management.*;
import org.j3d.aviatrix3d.output.audio.OpenALAudioDevice;
import org.j3d.aviatrix3d.output.graphics.*;
import org.j3d.aviatrix3d.pipeline.audio.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;

import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;

import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.Web3DResourceFactory;
import org.web3d.util.Version;

import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.renderer.common.input.DefaultSensorManager;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.scripting.ScriptEngine;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.xj3d.impl.core.eventmodel.*;
import org.xj3d.impl.core.eventmodel.OriginManagerFactory;
import org.xj3d.impl.core.loading.AbstractLoadManager;
import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.FramerateThrottle;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;
import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;
import org.xj3d.ui.awt.widgets.*;
import org.xj3d.ui.awt.widgets.IconLoader;

import org.xj3d.ui.construct.DeathTimer;

/**
 * A standalone X3D/VRML browser application.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.110 $
 */
public class Xj3DBrowser extends JFrame
    implements SurfaceManager, SensorStatusListener,
               KeyListener, BrowserCoreListener,
               Runnable {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in BrowserJPanel";

    /** App name to register preferences under */
    private static final String APP_NAME = "xj3d.Xj3DBrowser";

    /** Typical usage message with program options */
    private static final String USAGE_MSG =
      "Usage: Xj3DBrowser [options] [filename]\n" +
      "  -help                   Prints out this help message\n" +
      "  -fullscreen n           Runs the browser in fullscreen exclusive mode on screen n.  n is optional\n" +
      "  -stereo quad|alternate  Enables stereo projection output\n" +
      "  -antialias n            Use n number of multisamples to antialias scene\n" +
      "  -useMipMaps TRUE|FALSE  Forces mipmap usage on all textures\n" +
      "  -anisotropicDegree n    Forces anisotripic filtering of nTH degree\n" +
      "  -nice                   Do not use all the CPU for rendering\n" +
      "  -captureViewpoints      Generate a screenshot of each viewpoint of the file being loaded\n" +
      "  -zbuffer n              Select how many bits of zbuffer, 8, 16, 24, or 32\n" +
      "  -screenSize w h         Specify the screen size to use\n" +
      "  -disableAudio           Disable the audio output. Useful if you have sound card issues\n" +
      "  -enableOriginManager    Enable the dynamic origin manager in geospatial nodes\n" +
      "  -numLoaderThreads n     Number of threads to use for loading content\n" +
      "  -newt                   Use a GLWindow (NEWT) component to render\n" +
      "  -swing-lightweight      Use a GLJPanel (Swing) component to render\n" +
      "  -swing                  Use a GLCanvas (AWT) component to render" +
      "  -title                  Set a title to show in the broswer window frame" +
      "  -noredirect             System and error messages will not be redirected to the app console";

    /** The real component that is being rendered to */
    private Component canvas;

    /** The status bar */
    private SwingStatusBar statusBar;

    /** The content pane for the frame */
    private Container mainPane;

    /** Area to push error messages to */
    private ErrorReporter console;

    /** Created by the derived class */
    private OGLStandardBrowserCore universe;

    /** Manager for the scene graph handling */
    private SingleThreadRenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager;

    /** Our drawing surface */
    private GraphicsOutputDevice surface;

    /** Mapping of def'd Viewpoints to their real implementation */
    private Map<String, VRMLNode> viewpointDefMap;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    /** The load manager */
    private AbstractLoadManager loadManager;

    /** The input device manager */
    private InputDeviceManager idm;

    /* The KeyDevice sensor manager */
    private KeyDeviceSensorManager kdsm;

    /** The sensor manager */
    private SensorManager sensorManager;

    /** The script loader */
    private ScriptLoader scriptLoader;

    /** The location tool bar */
    private SwingLocationToolbar locToolbar;

    /** The viewpoint tool bar */
    private SwingViewpointToolbar viewpointToolbar;

    /** The viewpoint manager. */
    private ViewpointManager viewpointManager;

    /** The glCapabilities chosen */
    private GLCapabilities caps;

    /** The graphics pipeline */
    private DefaultGraphicsPipeline pipeline;

    /** The audio pipeline */
    private DefaultAudioPipeline audioPipeline;

    /** The framerate throttle */
    private FramerateThrottle frameThrottle;

    /** The antialiasing action */
    private AntialiasingAction antialiasingAction;

    /** Scene info action */
    private SceneInfoAction sceneInfoAction;

    /** Profiling info action */
    private ProfilingInfoAction profilingInfoAction;

    /** Scene tree action */
    private SceneTreeAction sceneTreeAction;

    /** Screen capture action */
    private ScreenShotAction screenShotAction;

    /** Movie Start action */
    private MovieAction movieStartAction;

    /** Movie End action */
    private MovieAction movieEndAction;

    /** Capture viewpoint action */
    private CaptureViewpointsAction capAction;

    /** Are we waiting for the load to finish */
    private boolean waitingForLoad;

    /** Has addNotify happened */
    private boolean addNotifyHandled;

    /** Should we use full screen mode */
    private boolean useFullscreen;

    // Command line arguments
    private static int fullscreen = -1;
    private static boolean stereo = false;
    private static int desiredSamples = 1;
    private static int numZBits = -1;
    private static boolean redirect = true;
    private static boolean nice = true;
    private static boolean useMipMaps = true;
    private static int anisotropicDegree = 1;
    private static int stereoMode = 0;
    private static boolean captureViewpoints = false;
    
    /* Note: current image loader only offers 32 bit navtives */
    private static boolean useImageLoader = false;
    private static boolean disableAudio = false;
    private static int[] screenSize = null;   // null means not provided
    private static boolean enableOriginManager = false;
    private static boolean useJoglNewt = false;
    private static boolean useSwingLite = false;
    private static boolean useSwing = false;
    public  static final String DEFAULT_TITLE = "Xj3D Browser - Aviatrix3D";
    private static       String title = DEFAULT_TITLE;

    /**
     * Create an instance of the demo class.
     */
    public Xj3DBrowser() {

        super(title);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.xj3dResources");

        addNotifyHandled = false;

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap<>();

        if (captureViewpoints) {
            console = new PlainTextErrorReporter();
        } else {
            console = new SwingConsoleWindow();
        }

        String renderMode;

        if (useSwing) {
            renderMode = "GLCanvas (AWT)";
        } else if (useJoglNewt) {
            renderMode = "GLWindow (NEWT)";
        } else if (useSwingLite) {
            renderMode = "GLJPanel (Swing)";
        } else {
            renderMode = "GLWindow (NEWT)";
        }

        String msg = "Initializing OpenGL X3D Browser in " + renderMode + " mode.\n";
        console.messageReport(msg);

        viewpointManager = setupAviatrix();

        int width = 800;
        int height = 600;

        if (screenSize != null) {
            // TODO: Bah, wish I knew how to get these window dressing params
            width = screenSize[0] + 8;
            height = screenSize[1] + 141;
        }

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev;

        if (fullscreen >= 0) {
            useFullscreen = true;
            GraphicsDevice[] gs = env.getScreenDevices();
            if (gs.length < fullscreen + 1) {
                System.out.println("Invalid fullscreen device.  Using default");
                dev = env.getDefaultScreenDevice();

                Dimension dmn = Toolkit.getDefaultToolkit().getScreenSize();
                width = (int) dmn.getWidth();
                height = (int) dmn.getHeight();
            } else {
                dev = gs[fullscreen];
                DisplayMode dm = dev.getDisplayMode();
                width = dm.getWidth();
                height = dm.getHeight();
            }
        } else {
            dev = env.getDefaultScreenDevice();
        }

        if(useFullscreen && !dev.isFullScreenSupported()) {
            System.out.println("Fullscreen not supported");
            useFullscreen = false;
        }

        // If we are fullscreen mode, make the frame do that,
        // but don't put any of the normal decorations like buttons,
        // URL bars etc.
        if(useFullscreen) {
            DisplayMode currentMode = dev.getDisplayMode();
            DisplayMode prefDisplayMode =
                new DisplayMode(width,
                                height,
                                currentMode.getBitDepth(),
                                DisplayMode.REFRESH_RATE_UNKNOWN);

            setUndecorated(true);
            dev.setFullScreenWindow(Xj3DBrowser.this);

            if (dev.isDisplayChangeSupported()) {
                dev.setDisplayMode(prefDisplayMode);
            } else {
                System.out.println("Fullscreen supported but display mode change is not");
            }
        }

        createUI();

        FileTransferHandler fth = new FileTransferHandler(locToolbar);
        setTransferHandler(fth);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));

        ImageIcon icon = IconLoader.loadIcon("images/branding/yumetech-16x16.gif", console);

        if (icon != null)
            setIconImage(icon.getImage());

        setPreferredSize(new Dimension(width, height));

        if (!useFullscreen && screenSize == null) {
            setLocation(40, 40);
        }

        pack();

        Runnable run = () -> {
            // Need to set visible first before starting the rendering thread due
            // to a bug in JOGL. See JOGL Issue #54 for more information on this.
            // http://jogl.dev.java.net
            setVisible(true);
        };
        SwingUtilities.invokeLater(run);
    }

    //---------------------------------------------------------------
    // Methods defined by SurfaceManager
    //---------------------------------------------------------------

    /**
     * Reset the surface with a new mode or parameters.
     */
    @Override
    public void resetSurface() {

        GraphicsResizeListener[] listeners = universe.getGraphicsResizeListeners();

        sceneManager.setEnabled(false);

        canvas.removeKeyListener(this);

        for (GraphicsResizeListener listener : listeners) {
            surface.removeGraphicsResizeListener(listener);
        }

        canvas.setVisible(false);
        mainPane.remove(canvas);
        surface.dispose();
        canvas = null;
        surface = null;

        if (stereo) {
            surface = new StereoAWTSurface(caps, stereoMode);
        } else if (useSwing) {
            surface = new SimpleAWTSurface(caps);
        } else if (useJoglNewt) {
            surface = new SimpleNEWTSurface(caps);
        } else if (useSwingLite) {
            surface = new SimpleAWTSurface(caps, useSwingLite);
        } else {

            // Default to -newt
            surface = new SimpleNEWTSurface(caps);
        }

        surface.enableTwoPassTransparentRendering(true);
        surface.setAlphaTestCutoff(0.5f);

        pipeline.setGraphicsOutputDevice(surface);

        canvas = (Component)surface.getSurfaceObject();

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        mainPane.add(canvas, BorderLayout.CENTER);
        mainPane.validate(); // <- Major important call here

        universe.setHardwareFOV(0);
        sceneManager.setEnabled(true);

        canvas.addKeyListener(this);

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            canvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        idm.reinitialize(deviceFactory);
        kdsm.reinitialize(deviceFactory);

        sensorManager.setInputManager(idm);
        sensorManager.setKeyDeviceSensorManager(kdsm);

        for (GraphicsResizeListener listener : listeners) {
            surface.addGraphicsResizeListener(listener);
        }
    }

    /**
     * Get the current capability bits.
     * @return
     */
    @Override
    public GLCapabilities getCapabilities() {
        return caps;
    }


    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run method for the shutdown hook. This is to deal with someone using
     * ctrl-C to kill the application. Makes sure that all the resources
     * are cleaned up properly.
     */
    @Override
    public void run()
    {
        shutdownApp();
    }

    //----------------------------------------------------------
    // Methods required by the KeyListener interface.
    //----------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {

            case KeyEvent.VK_A:
                if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                    antialiasingAction.actionPerformed(new ActionEvent(this, 0, "Cycle"));
                }
                break;

            case KeyEvent.VK_P:
                if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    Vector3f pos = new Vector3f();
                    AxisAngle4f ori = new AxisAngle4f();

                    getUserPosition(pos, ori);

                    console.messageReport("Viewpoint {");
                    console.messageReport("   position " + pos.x + " " + pos.y + " " + pos.z);
                    console.messageReport("   orientation " + ori.x + " " + ori.y + " " + ori.z + " " + ori.angle);
                    console.messageReport("}");
                }
                break;
            case KeyEvent.VK_S:
                if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    System.out.println("Straighten not implemented");
                } else if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                    // Enter/Exit Spherical mode

                    // TODO: Do we need to cleanup old sensor manager stuff?
                    resetSurface();
                }

                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    //---------------------------------------------------------------
    // Methods defined by Window
    //---------------------------------------------------------------

    @Override
    protected void processWindowEvent(WindowEvent e) {

        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSING:
                // Stop all theads and dispose all Frames to complete an exit
                shutdownApp();
                if (console instanceof SwingConsoleWindow)
                    ((Window)console).dispose();
//                dispose();
                // Crash issue w/ JOGL v2.4 until (fix)
                System.exit(0);
                break;
            case WindowEvent.WINDOW_CLOSED:
                System.out.println("Shutting down Xj3D");
                break;
            case WindowEvent.WINDOW_OPENED:
                canvas.requestFocus();
                break;
            case WindowEvent.WINDOW_ACTIVATED:
                canvas.requestFocus();
                break;
            default:
                break;
        }

        // Pass along all other events
        super.processWindowEvent(e);
    }

    //----------------------------------------------------------
    // Method defined by Frame
    //----------------------------------------------------------

    @Override
    public void setTitle(String aTitle) {
        title = aTitle;
        super.setTitle(title);
    }

    //----------------------------------------------------------
    // Methods required by the LinkSelectionListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    @Override
    public void deviceOver(int type, String desc) {
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    @Override
    public void deviceNotOver(int type) {
    }

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    @Override
    public void deviceActivated(int type) {
    }

    /**
     * Invoked when a tracker selects an object that represents a link to an
     * external source.
     *
     * @param urls The urls to load in the order of preference defined by
     *    the node
     * @param params The list of parameters provided with the node.
     *    Null if none.
     * @param desc The description that may be accompanying the link node
     */
    @Override
    public void linkActivated(String[] urls, String[] params, String desc) {
        boolean success = false;

        for (String url1 : urls) {
            if (url1.charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url1.substring(1);
                VRMLViewpointNodeType vp =
                        (VRMLViewpointNodeType)viewpointDefMap.get(def_name);
                if(vp != null) {
                    universe.changeViewpoint(vp);
                } else {
                    statusBar.setStatusText("Unknown Viewpoint " + def_name);
                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            } else {
                // load the world.
                try {
                    URL url = new URL(url1);
                    InputSource is = new InputSource(url);
                    if(success = load(is)) {
                        break;
                    }
                } catch (MalformedURLException mue) {
                    statusBar.setStatusText("Invalid URL");
                    console.warningReport("Invalid URL: " + url1, mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);
    }

    //----------------------------------------------------------
    // BrowserCoreListener methods
    //----------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given
     * is found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    @Override
    public void browserInitialized(VRMLScene scene) {
        waitingForLoad = false;

        if (addNotifyHandled)
            sceneManager.setEnabled(true);

        String loadedUri = scene.getLoadedURI();

        if (loadedUri != null) {

            int idx = loadedUri.indexOf("#");

            if (idx > 0) {
                String def_name = loadedUri.substring(idx+1);

                VRMLViewpointNodeType vp =
                    (VRMLViewpointNodeType)viewpointDefMap.get(def_name);


                if(vp != null) {
                    universe.changeViewpoint(vp);
                } else {
                    statusBar.setStatusText("Unknown Viewpoint " + def_name);
                    console.warningReport("Unknown Viewpoint " + def_name, null);
                }
            }
        }
    }

    /**
     * The tried to load a URL and failed. It is typically because none of
     * the URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    @Override
    public void urlLoadFailed(String msg) {
        waitingForLoad = false;

        if (captureViewpoints) {
            System.exit(0);
        }

        universe.setScene(null, "");

        // Can't do this here otherwise other loads will fail
        //sceneManager.setEnabled(false);

        if (console instanceof Window) {
            ((Window)console).toFront();
        } else {
            System.err.println("Can't put console to front");
        }
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    @Override
    public void browserShutdown() {
        waitingForLoad = false;
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    @Override
    public void browserDisposed() {
        waitingForLoad = false;
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Setup the aviatrix pipeline here
     */
    private ViewpointManager setupAviatrix() {

        // Assemble a simple single-threaded pipeline.
        caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        if (desiredSamples > 1)
            caps.setSampleBuffers(true);
        else
            caps.setSampleBuffers(false);

        caps.setNumSamples(desiredSamples);

        if (numZBits > 0)
            caps.setDepthBits(numZBits);

        GraphicsCullStage culler = new FrustumCullStage();
        culler.setOffscreenCheckEnabled(true);

        GraphicsSortStage sorter = new StateAndTransparencyDepthSortStage();

        if (stereo) {
            surface = new StereoAWTSurface(caps, stereoMode);
        } else if (useSwing) {
            surface = new SimpleAWTSurface(caps);
        } else if (useJoglNewt) {
            surface = new SimpleNEWTSurface(caps);
        } else if (useSwingLite) {
            surface = new SimpleAWTSurface(caps, useSwingLite);
        } else {

            // Default to -newt
            surface = new SimpleNEWTSurface(caps);
        }

        surface.enableTwoPassTransparentRendering(true);
        surface.setAlphaTestCutoff(0.5f);

        pipeline = new DefaultGraphicsPipeline();
        pipeline.setCuller(culler);
        pipeline.setSorter(sorter);
        pipeline.setGraphicsOutputDevice(surface);

        displayManager = new SingleDisplayCollection();
        displayManager.addPipeline(pipeline);

        if(!disableAudio) {
            AudioOutputDevice adevice = new OpenALAudioDevice();

            AudioCullStage aculler = new NullAudioCullStage();
            AudioSortStage asorter = new NullAudioSortStage();

            audioPipeline = new DefaultAudioPipeline();
            audioPipeline.setCuller(aculler);
            audioPipeline.setSorter(asorter);
            audioPipeline.setAudioOutputDevice(adevice);
            displayManager.addPipeline(audioPipeline);
        }

        // Render manager
        sceneManager = new SingleThreadRenderManager();
        sceneManager.disableInternalShutdown();
        sceneManager.addDisplay(displayManager);

        canvas = (Component)surface.getSurfaceObject();
        canvas.addKeyListener(this);

        mainPane = getContentPane();

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        mainPane.add(canvas, BorderLayout.CENTER);

        OGLSceneBuilderFactory builder_fac =
            new OGLSceneBuilderFactory(false,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true,
                                       true);

        VRMLParserFactory parser_fac = null;

        try {
            parser_fac = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError fce) {
            throw new RuntimeException("Failed to load parser factory");
        }

        loadManager = new MemCacheLoadManager();
        scriptLoader = new DefaultScriptLoader();
        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(scriptLoader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        PickingManager picker_manager = new DefaultPickingManager();
        picker_manager.setErrorReporter(console);

        sensorManager = new DefaultSensorManager();
        sensorManager.setPickingManager(picker_manager);
        ////////////////////////////////////////////////////////////////////////////////
        if(enableOriginManager) {
            System.setProperty("org.xj3d.core.eventmodel.OriginManager.enabled", "true");
        }
        sensorManager.setOriginManager(OriginManagerFactory.getInstance(state_manager));
        ////////////////////////////////////////////////////////////////////////////////

        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();

        NodeManager physics_manager = null;

        try {
            Object manager = Class.forName("org.xj3d.impl.core.eventmodel.DefaultRigidBodyPhysicsManager").getDeclaredConstructor().newInstance();
            physics_manager = (NodeManager) manager;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            console.warningReport("PhysicsManager not found, Physics disabled", null);
        }

        DefaultParticleSystemManager particle_manager = new DefaultParticleSystemManager();
        NetworkManager network_manager = new DefaultNetworkManager();

        // Assume binary DIS protocol handler unless othewise set
        String prop = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(
                OGLBrowserCanvas.DIS_PROTOCOL_HANDLER_PROP,
                "org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler") // privileged code goes here, for example:
        );

        if (prop != null) {
            try {
                Object handler = Class.forName(prop).getDeclaredConstructor().newInstance();
                network_manager.addProtocolHandler((NetworkProtocolHandler) handler);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                console.warningReport("DIS Protocol Handler not found, DIS handling disabled", e);
            }
        }

        eventModel = new DefaultEventModelEvaluator();
        universe = new OGLStandardBrowserCore(eventModel, sceneManager, displayManager);
        universe.addSensorStatusListener(this);
        universe.setErrorReporter(console);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                                           builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
                                          parser_fac);

        NodeManager[] node_mgrs;

        if (physics_manager != null) {
            node_mgrs = new NodeManager[] {
                network_manager,
                hanim_manager,
                physics_manager,
                particle_manager
            };
        } else {
            node_mgrs = new NodeManager[] {
                network_manager,
                hanim_manager,
                particle_manager
            };
        }

        OGLLayerManagerFactory lmf = new OGLLayerManagerFactory();
        lmf.setErrorReporter(console);

        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                              route_manager,
                              sensorManager,
                              state_manager,
                              loadManager,
                              vp_manager,
                              lmf, universe,
                              node_mgrs);
        eventModel.setErrorReporter(console);

        GraphicsResizeListener[] listeners = universe.getGraphicsResizeListeners();

        for (GraphicsResizeListener listener : listeners) {
            surface.addGraphicsResizeListener(listener);
        }

        DeviceFactory deviceFactory = new AWTDeviceFactory(
            canvas,
            Xj3DConstants.OPENGL_ID,
            surface,
            console);

        idm = new InputDeviceManager( deviceFactory );
        kdsm = new KeyDeviceSensorManager( deviceFactory );

        sensorManager.setInputManager(idm);
        sensorManager.setKeyDeviceSensorManager(kdsm);

        createScriptEngine("org.web3d.vrml.scripting.jsai.VRML97ScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        createScriptEngine("org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        createScriptEngine("org.web3d.vrml.scripting.sai.JavaSAIScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        createScriptEngine("org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine",
            universe, vp_manager, route_manager, state_manager, worldLoader);

        setupProperties(universe, worldLoader);

        if (stereo)
            universe.setStereoEnabled(true);

        return vp_manager;
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp()
    {
        universe.dispose();

        loadManager.shutdown();
        scriptLoader.shutdown();
        eventModel.shutdown();

        // Includes JOGL and JOAL resource disposal
        sceneManager.shutdown();
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return true if the world loaded correctly
     */
    private boolean load(InputSource is) {

        boolean ret_val;

        WorldLoader loader = worldLoader.fetchLoader();

        VRMLScene parsed_scene;

        try {
            parsed_scene = loader.loadNow(universe, is);
        } catch(IOException | VRMLParseException e) {
            console.errorReport("Failed to load ", e);
            worldLoader.releaseLoader(loader);
            return false;
        }

        worldLoader.releaseLoader(loader);

        universe.setScene(parsed_scene, null);

        ret_val = true;

        // Grab the list of viewpoints and place them into the toolbar.
        java.util.List<VRMLNode> vp_list =
            parsed_scene.getByPrimaryType(TypeConstants.ViewpointNodeType);

        if(vp_list.isEmpty())
            return ret_val;

        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
        viewpointDefMap.clear();
        Map<String, VRMLNode> def_map = parsed_scene.getDEFNodes();
        Iterator<String> itr = def_map.keySet().iterator();

        while(itr.hasNext()) {
            String key = itr.next();
            VRMLNode vp = def_map.get(key);

            if(vp instanceof VRMLViewpointNodeType)
                viewpointDefMap.put(key, vp);
        }

        statusBar.setStatusText("World Loaded Successfully");

        return ret_val;
    }

    /**
     * Create the window contents now. Assumes that the universe
     * variable is already set.
     */
    private void createUI() {

        java.util.List<Action> actionList = new ArrayList<>();

        org.xj3d.ui.awt.widgets.CursorManager cm =
            new org.xj3d.ui.awt.widgets.CursorManager(canvas, null, console);
        universe.addSensorStatusListener(cm);
        universe.addNavigationStateListener(cm);

        universe.addCoreListener(this);

        locToolbar =
            new SwingLocationToolbar(universe,
                                     worldLoader,
                                     false,
                                     true,
                                     true,
                                     null,
                                     null,
                                     console);

        SwingNavigationToolbar nav_tb =
            new SwingNavigationToolbar(universe,
                                       null,
                                       console);

        viewpointToolbar =
            new SwingViewpointToolbar(universe,
                                      viewpointManager,
                                      null,
                                      console);

        statusBar = new SwingStatusBar(universe,
                                       true,
                                       true,
                                       null,
                                       console);

        locToolbar.setProgressListener(statusBar.getProgressListener());

        JPanel p2 = new JPanel(new BorderLayout());

        if (!useFullscreen) {
            p2.add(nav_tb, BorderLayout.WEST);
            p2.add(viewpointToolbar, BorderLayout.CENTER);
            p2.add(statusBar, BorderLayout.SOUTH);

            mainPane.add(locToolbar, BorderLayout.NORTH);
            mainPane.add(p2, BorderLayout.SOUTH);

            SwingConsoleButton console_button;

            if (!captureViewpoints) {
                console_button = new SwingConsoleButton((SwingConsoleWindow) console, null);
                p2.add(console_button, BorderLayout.EAST);
            }
        }

        frameThrottle = new FramerateThrottle(universe, console);
        frameThrottle.setScriptLoader(scriptLoader);
        frameThrottle.setLoadManager(loadManager);
        locToolbar.setThrottle(frameThrottle);

        if (nice) {
            frameThrottle.setMinimumNoLoading(20);
        }

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;
        JRadioButtonMenuItem rbItem;

        // File Menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Open Item
        Action openAction = locToolbar.getOpenAction();
        actionList.add(openAction);
        fileMenu.add(new JMenuItem(openAction));

        ExitAction exitAction = new ExitAction();
        actionList.add(exitAction);

        // Exit Item
        fileMenu.add(new JMenuItem(exitAction));

        // View Menu
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        // Reload Item
        Action reloadAction = locToolbar.getReloadAction();
        actionList.add(reloadAction);
        viewMenu.add(new JMenuItem(reloadAction));

        // Render Style SubMenu
        JMenu renderStyle = new JMenu("Render Style");

        ButtonGroup rsGroup = new ButtonGroup();
        PointsStyleAction psa = new PointsStyleAction(universe, statusBar);
        actionList.add(psa);
        rbItem = new JRadioButtonMenuItem(psa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        LinesStyleAction lsa = new LinesStyleAction(universe, statusBar);
        actionList.add(lsa);
        rbItem = new JRadioButtonMenuItem(lsa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        ShadedStyleAction ssa = new ShadedStyleAction(universe, statusBar);
        rbItem = new JRadioButtonMenuItem(ssa);
        actionList.add(ssa);
        rsGroup.add(rbItem);
        renderStyle.add(rbItem);
        rbItem.setSelected(true);

        viewMenu.add(renderStyle);

        RenderStyle[] linkedStyles = new RenderStyle[1];
        linkedStyles[0] = psa;

        lsa.setLinkedStyles(linkedStyles);

        linkedStyles[0] = lsa;

        psa.setLinkedStyles(linkedStyles);

        linkedStyles = new RenderStyle[2];
        linkedStyles[0] = psa;
        linkedStyles[1] = lsa;

        ssa.setLinkedStyles(linkedStyles);

        // Screen Capture SubMenu
        JMenu captureMenu = new JMenu("Screen Capture");

        // Single Frame Item
        screenShotAction = new ScreenShotAction(console, universe);
        actionList.add(screenShotAction);

        menuItem = new JMenuItem(screenShotAction);
        captureMenu.add(menuItem);
        viewMenu.add(captureMenu);

        // Movie Start Item
        movieStartAction = new MovieAction(true,console, universe);
        actionList.add(movieStartAction);

        menuItem = new JMenuItem(movieStartAction);
        captureMenu.add(menuItem);
        viewMenu.add(captureMenu);

        // Movie End Item
        movieEndAction = new MovieAction(false,console, universe);
        actionList.add(movieEndAction);

        menuItem = new JMenuItem(movieEndAction);
        captureMenu.add(menuItem);

        capAction = new CaptureViewpointsAction(console, universe, viewpointManager);
        actionList.add(capAction);
        menuItem = new JMenuItem(capAction);
        captureMenu.add(menuItem);

        viewMenu.add(captureMenu);

        // Scene Info Item
        sceneInfoAction = new SceneInfoAction(console, displayManager);
        actionList.add(sceneInfoAction);
        menuItem = new JMenuItem(sceneInfoAction);
        viewMenu.add(menuItem);

        profilingInfoAction = new ProfilingInfoAction(console, universe);
        actionList.add(profilingInfoAction);
        menuItem = new JMenuItem(profilingInfoAction);
        viewMenu.add(menuItem);

        // Scene Tree Item
        sceneTreeAction = new SceneTreeAction(universe, BorderLayout.WEST, this);
        actionList.add(sceneTreeAction);
        menuItem = new JMenuItem(sceneTreeAction);
        viewMenu.add(menuItem);

        // Viewpoint Menu
        JMenu viewpointMenu = new JMenu("Viewpoint");
        menuBar.add(viewpointMenu);

        viewpointMenu.add(new JMenuItem(viewpointToolbar.getNextViewpointAction()));
        actionList.add(viewpointToolbar.getNextViewpointAction());
        viewpointMenu.add(new JMenuItem(viewpointToolbar.getPreviousViewpointAction()));
        actionList.add(viewpointToolbar.getPreviousViewpointAction());
        viewpointMenu.add(new JMenuItem(viewpointToolbar.getHomeViewpointAction()));
        actionList.add(viewpointToolbar.getHomeViewpointAction());

        JMenu navMenu = new JMenu("Navigation");
        menuBar.add(navMenu);
        navMenu.add(new JMenuItem(nav_tb.getFlyAction()));
        actionList.add(nav_tb.getFlyAction());
        navMenu.add(new JMenuItem(nav_tb.getWalkAction()));
        actionList.add(nav_tb.getWalkAction());
        navMenu.add(new JMenuItem(nav_tb.getExamineAction()));
        actionList.add(nav_tb.getExamineAction());
        navMenu.add(new JMenuItem(nav_tb.getTiltAction()));
        actionList.add(nav_tb.getTiltAction());
        navMenu.add(new JMenuItem(nav_tb.getPanAction()));
        actionList.add(nav_tb.getPanAction());
        navMenu.add(new JMenuItem(nav_tb.getTrackAction()));
        actionList.add(nav_tb.getTrackAction());
        navMenu.add(new JMenuItem(viewpointToolbar.getLookatAction()));
        actionList.add(viewpointToolbar.getLookatAction());
        navMenu.add(new JMenuItem(viewpointToolbar.getFitWorldAction()));
        actionList.add(viewpointToolbar.getFitWorldAction());

        JMenu optionsMenu = new JMenu("Options");
        NiceAction niceAction = new NiceAction(frameThrottle, statusBar, nice);
        actionList.add(niceAction);
        JCheckBoxMenuItem niceMenu = new JCheckBoxMenuItem(niceAction);
        niceMenu.setState(nice);
        optionsMenu.add(niceMenu);

        antialiasingAction = new AntialiasingAction(this, statusBar);
        actionList.add(antialiasingAction);

        JMenu antialiasingMenu = new JMenu("Anti-Aliasing");
        ButtonGroup antialiasingGroup = new ButtonGroup();

        int n = 2;

        rbItem = new JRadioButtonMenuItem("Disabled");
        if (desiredSamples <= 1)
            rbItem.setSelected(true);
        rbItem.setActionCommand("Disabled");
        rbItem.addActionListener(antialiasingAction);
        antialiasingMenu.add(rbItem);
        antialiasingGroup.add(rbItem);
        int maxSamples = antialiasingAction.getMaximumNumberOfSamples();

        while(n <= maxSamples) {
            rbItem = new JRadioButtonMenuItem(n + " Samples", n == desiredSamples);

            rbItem.addActionListener(antialiasingAction);
            rbItem.setActionCommand(Integer.toString(n));
            antialiasingMenu.add(rbItem);
            antialiasingGroup.add(rbItem);

            n = n * 2;
        }

        optionsMenu.add(antialiasingMenu);

        menuBar.add(optionsMenu);

        try {
            HelpAction helpAction = new HelpAction(false, null, console);
            actionList.add(helpAction);

            JMenu helpMenu = new JMenu("Help");
            menuBar.add(helpMenu);

            helpMenu.add(helpAction);
        } catch(NoClassDefFoundError nc) {
            console.warningReport("JavaHelp not found, help disabled", null);
        }

        if (!useFullscreen) {
            setJMenuBar(menuBar);
        } else {
            // Need to register all actions with canvas manually
            JComponent comp = (JComponent) getContentPane();
            KeyStroke ks;
            String actionName;

            Iterator<Action> itr = actionList.iterator();
            Action action;

            while(itr.hasNext()) {
                action = itr.next();

                ks = (KeyStroke) action.getValue(AbstractAction.ACCELERATOR_KEY);
                actionName = (String) action.getValue(AbstractAction.SHORT_DESCRIPTION);

                comp.getInputMap().put(ks, actionName);
                comp.getActionMap().put(actionName, action);
            }
        }
    }

    /**
     * Load content on the browser.  This is only called if a file is provided
     * on the command-line.  Otherwise the location toolbar handles it.
     *
     * @param url The url to load
     */
    public void loadURL(String url) {
        String basename;
        int idx = url.indexOf("\\");
        if (idx > -1)
            basename = url.substring(idx);
        else
            basename = url;

        capAction.setBasename(basename);

        if (captureViewpoints) {
            waitingForLoad = true;
        }

        try {
            locToolbar.loadURL(url);
        } catch(IOException ioe) {
            console.errorReport("Error loading file: " + url, ioe);
        }

        if (captureViewpoints) {
            DeathTimer dt = new DeathTimer(20_000);
            dt.start();

            while(waitingForLoad) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {}
            }

            while(!frameThrottle.isInitialLoadDone()) {
                try {
                    Thread.sleep(200);
                } catch(InterruptedException e) {}
            }

            dt.exit();

            capAction.actionPerformed(new ActionEvent(this, 0, "Capture"));

            captureViewpoints = false;

            System.exit(0);
        }
    }

    /**
     * Redirect system messages to the console.
     */
    public void redirectSystemMessages() {
        if (console != null && console instanceof SwingConsoleWindow)
            ((SwingConsoleWindow)console).redirectSystemMessages();
    }

    /**
     * Get the current user position.
     *
     * @param pos The position
     * @param ori The orientation
     */
    private void getUserPosition(Vector3f pos, AxisAngle4f ori) {
        universe.getUserPosition(pos, ori);
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     *
     * @param core The core representation of the browser
     * @param loader Loader manager for doing async calls
     */
    protected void setupProperties(final BrowserCore core, final WorldLoaderManager loader) {

        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                
                // Disable font cache to fix getBounds nullPointer bug
                System.setProperty("sun.awt.font.advancecache", "off");
                System.setProperty("uri.content.handler.pkgs", "vlc.net.content");
                System.setProperty("uri.protocol.handler.pkgs", "vlc.net.protocol");
                System.setProperty("java.content.handler.pkgs", "vlc.content");
                
                if (useImageLoader) {
                    try {
                        // check if the image loader can be instantiated successfully
                        Class<?> cls = Class.forName("vlc.net.content.image.ImageDecoder");
                        Object obj = cls.getDeclaredConstructor().newInstance();
                        // if so, then -enable- the image loaders
                        System.setProperty("java.content.handler.pkgs",
                                "vlc.net.content");
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException t) {
                        console.warningReport("Native image loaders not available", null);
                    }
                }
                
                if (useMipMaps) {
                    System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.useMipMaps", "true");
                }
                if (anisotropicDegree > 1) {
                    System.setProperty("org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree", Integer.toString(anisotropicDegree));
                }
                
                URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                if (!(res_fac instanceof Web3DResourceFactory)) {
                    res_fac = new Web3DResourceFactory(res_fac);
                    URI.setURIResourceStreamFactory(res_fac);
                }
                
                ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
                if (!(c_fac instanceof AWTContentHandlerFactory)) {
                    c_fac = new AWTContentHandlerFactory(core, loader, c_fac);
                    URI.setContentHandlerFactory(c_fac);
                }
                
                FileNameMap fn_map = URI.getFileNameMap();
                if (!(fn_map instanceof VRMLFileNameMap)) {
                    fn_map = new VRMLFileNameMap(fn_map);
                    URI.setFileNameMap(fn_map);
                }
                
                return null;
            });
        } catch (PrivilegedActionException pae) {
            console.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }

    /**
     * Create a script engine.  Use Class.forName to avoid direct linking.  Will issue
     * a warning to the console if it fails.
     *
     * @param name The script engine to create
     * @param vpManager The Viewpoint manager to use
     * @param universe The universe to use
     * @param routeManager The route manager to use
     * @param stateManager The state manager to use
     * @param worldLoader The loader manager to use
     */
     public void createScriptEngine(String name, OGLStandardBrowserCore universe,
        ViewpointManager vpManager, RouteManager routeManager, FrameStateManager stateManager,
        WorldLoaderManager worldLoader) {

        Class<?> scriptClass;
        Object[] paramTypes;
        Object[] constParams1 = new Object[] {universe, routeManager, stateManager, worldLoader};
        Object[] constParams2 = new Object[] {universe, vpManager, routeManager, stateManager, worldLoader};
        ScriptEngine script;
        boolean found = false;

        try {
            scriptClass = Class.forName(name);

            Constructor<?>[] consts = scriptClass.getConstructors();

            for (Constructor<?> const1 : consts) {
                paramTypes = const1.getParameterTypes();
                if (paramTypes.length == constParams1.length) {
                    script = (ScriptEngine) const1.newInstance(constParams1);
                    script.setErrorReporter(console);
                    scriptLoader.registerScriptingEngine(script);
                    found = true;
                    break;
                } else if (paramTypes.length == constParams2.length) {
                    script = (ScriptEngine) const1.newInstance(constParams2);
                    script.setErrorReporter(console);
                    scriptLoader.registerScriptingEngine(script);
                    found = true;
                    break;
                }
            }

            if (!found) {
                console.warningReport("Cannot start " + name + " Scripting engine", null);
            }
        } catch(ClassNotFoundException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            console.warningReport("Cannot start " + name + " Scripting engine", null);
        }
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        addNotifyHandled = true;
        sceneManager.setEnabled(true);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param args The list of arguments for this application.
     */
    public static void main(String[] args) {
        int lastUsed = -1;

        for(int i = 0; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                if(args[i].equals("-fullscreen")) {
                    fullscreen = 0;
                    lastUsed = i;

                    try {
                        String val = args[i+1];
                        fullscreen = Integer.valueOf(val);
                        lastUsed = i + 1;
                    } catch(NumberFormatException e) {}
                } else if(args[i].equals("-screenSize")) {
                    lastUsed = i;

                    screenSize = new int[2];

                    try {
                        String val = args[i+1];
                        screenSize[0] = Integer.valueOf(val);
                        screenSize[1] = Integer.valueOf(val);
                        lastUsed = i + 1;
                    } catch(NumberFormatException e) {
                        System.err.println("Invalid screen size");
                        screenSize = null;
                    }
                } else if(args[i].equals("-stereo")) {
                    stereo = true;
                    String val = args[++i];
                    if (val.equalsIgnoreCase("quad")) {
                        stereoMode = GraphicsOutputDevice.QUAD_BUFFER_STEREO;
                    } else if (val.equalsIgnoreCase("alternate")) {
                        stereoMode = GraphicsOutputDevice.ALTERNATE_FRAME_STEREO;
                    } else {
                        System.err.println("Unknown stereo mode: " + val);
                    }

                    lastUsed = i;
                } else if(args[i].equals("-help")) {
                    System.out.println(USAGE_MSG);
                    return;
                } else if (args[i].equals("-useMipMaps")) {
                    String val = args[++i];
                    useMipMaps = Boolean.parseBoolean(val);
                    lastUsed = i;
                } else if (args[i].equals("-useImageLoader")) {
                    String val = args[++i];
                    boolean new_val = Boolean.parseBoolean(val);

                    if (new_val != useImageLoader) {
                        System.out.println("Using native image loaders");
                    }

                    useImageLoader = new_val;

                    if (useImageLoader) {
                        // TODO: Need to single thread image loader right now as it crashes otherwise
                        //System.setProperty("org.xj3d.core.loading.threads", "1");
                    }

                    lastUsed = i;
                } else if (args[i].equals("-anisotropicDegree")) {
                    String val = args[++i];
                    anisotropicDegree = Integer.valueOf(val);
                    if (anisotropicDegree > 1)
                        useMipMaps = true;
                    lastUsed = i;
                } else if (args[i].equals("-antialias")) {
                    String val = args[++i];
                    desiredSamples = Integer.valueOf(val);
                    lastUsed = i;
                } else if (args[i].equals("-noredirect")) {
                    System.err.println("Redirect cancelled");
                    redirect = false;
                    lastUsed = i;
                } else if (args[i].equals("-zbuffer")) {
                    String val = args[++i];
                    numZBits = Integer.valueOf(val);
                    lastUsed = i;
                } else if (args[i].equals("-nice")) {
                    lastUsed = i;
                    nice = true;
                } else if (args[i].equals("-captureViewpoints")) {
                    lastUsed = i;
                    captureViewpoints = true;
                } else if (args[i].equals("-disableAudio")) {
                    lastUsed = i;
                    disableAudio = true;
                } else if (args[i].equals("-enableOriginManager")) {
                    lastUsed = i;
                    enableOriginManager = true;
                } else if (args[i].equals("-numLoaderThreads")) {
                    lastUsed = i;
                    String val = args[i+1];
                    try {
                        Integer numLoaderThreads = Integer.valueOf(val);
                        lastUsed = i + 1;

                        if (numLoaderThreads > 0) {
                            System.setProperty(
                                "org.xj3d.core.loading.threads",
                                numLoaderThreads.toString());
                        } else {
                            System.err.println("Invalid number of loader threads: "+ val);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number of loader threads: "+ val);
                    }
                } else if (args[i].equals("-newt")) { // synonomous with BrowserInterfaceTypes.NEWT
                    useJoglNewt = true;
                    lastUsed = i;
                } else if (args[i].equals("-swing-lightweight")) { // synonomous with BrowserInterfaceTypes.LIGHTWEIGHT
                    useSwingLite = true;
                    lastUsed = i;
                } else if (args[i].equals("-swing")) { // synonomous with BrowserInterfaceTypes.PARTIAL_LIGHTWEIGHT
                    useSwing = true;
                    lastUsed = i;
                } else if(args[i].equals("-title")) {
                    lastUsed = i;
                    try {
                        String val = args[i+1];
                        title = val;
                        lastUsed = i + 1;
                    } catch(Exception e) {
                        System.err.println("title string not provided");
                    }
                } else if (args[i].startsWith("-")) {
                    System.err.println("Unknown flag: " + args[i]);
                    lastUsed = i;
                }
            }
        }

        Xj3DBrowser browser = new Xj3DBrowser();

        if (redirect)
            browser.redirectSystemMessages();

        // The last argument is the filename parameter
        String filename;

        if((args.length > 0) && (lastUsed + 1 < args.length)) {
            filename = args[args.length - 1];

            browser.loadURL(filename);
        }
    }
}
