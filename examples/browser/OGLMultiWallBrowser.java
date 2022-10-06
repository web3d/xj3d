/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.j3d.aviatrix3d.management.*;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.output.graphics.*;

// Local imports
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;
import org.xj3d.impl.core.eventmodel.*;

import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.Xj3DConstants;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.common.input.dis.DISProtocolHandler;
import org.web3d.vrml.renderer.ogl.browser.OGLLayerManagerFactory;
import org.web3d.vrml.renderer.ogl.OGLSceneBuilderFactory;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.renderer.ogl.input.DefaultPickingManager;
import org.web3d.vrml.renderer.common.input.DefaultSensorManager;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import org.xj3d.impl.core.loading.DefaultScriptLoader;
import org.xj3d.impl.core.loading.DefaultWorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;

import org.xj3d.ui.awt.device.AWTDeviceFactory;

/**
 * A demonstration application that shows multiwall support in the OGL browser.
 * <p>
 *
 * The simple browser does not respond to changes in the list of viewpoints
 * in the virtual world. This is OK because scripts are not used or needed in
 * this simple environment. Once we implement scripts, we have to look at
 * something different.
 *
 * This program requires each canvas to have a 1:1 aspect ratio.  Hence the
 * width and height of each wall must be the same.
 *
 * @author Justin Couch
 * @version $Revision: 1.26 $
 */
public class OGLMultiWallBrowser extends DemoFrame
    implements WindowListener, SensorStatusListener,
               Runnable, ComponentListener {

    /** Manager for the scene graph handling */
    private RenderManager sceneManager;

    /** Manager for the layers etc */
    private SingleDisplayCollection displayManager1;
    private SingleDisplayCollection displayManager2;
    private SingleDisplayCollection displayManager3;

    /** Our primary drawing surface */
    private GraphicsOutputDevice surface1;

    /** Our secondary drawing surfaces */
    private GraphicsOutputDevice surface2;
    private GraphicsOutputDevice surface3;
    private GraphicsOutputDevice surface4;
    private GraphicsOutputDevice surface5;
    private GraphicsOutputDevice surface6;

    /** Flag to indicate we are in the setup of the scene currently */
    private boolean inSetup;

    /** Mapping of def'd Viewpoints to their real implementation */
    private Map viewpointDefMap;

    /** World load manager to help us load files */
    private WorldLoaderManager worldLoader;

    /** The world's event model */
    private EventModelEvaluator eventModel;

    private ContentLoadManager loadManager;
    private ScriptLoader scriptLoader;

    /**
     * Create an instance of the demo class.
     */
    public OGLMultiWallBrowser() {
        super("OpenGL/Aviatrix3D Multi-Wall Demo");
        addWindowListener(OGLMultiWallBrowser.this);
        setSize(1_132, 450);

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        viewpointDefMap = new HashMap();

        Container content_pane = getContentPane();

        JPanel p1 = new JPanel(new BorderLayout());
        content_pane.add(p1, BorderLayout.CENTER);

        setupAviatrix(p1);

        JPanel p2 = new JPanel(new BorderLayout());
        p1.add(p2, BorderLayout.SOUTH);

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));

        Runnable run = new Runnable() {

            @Override
            public void run() {

                // Need to set visible first before starting the rendering thread due
                // to a bug in JOGL. See JOGL Issue #54 for more information on this.
                // http://jogl.dev.java.net
                setVisible(true);
            }
        };
        SwingUtilities.invokeLater(run);
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

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent evt)
    {
    }

    @Override
    public void windowClosed(WindowEvent evt)
    {
    }

    @Override
    public void windowClosing(WindowEvent evt)
    {
        shutdownApp();
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent evt)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent evt)
    {
    }

    @Override
    public void windowIconified(WindowEvent evt)
    {
    }

    @Override
    public void windowOpened(WindowEvent evt)
    {
        sceneManager.setEnabled(true);
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
     * @param url_list The urls to load in the order of preference defined by
     *    the node
     * @param params The list of parameters provided with the node.
     *    Null if none.
     * @param desc The description that may be accompanying the link node
     */
    @Override
    public void linkActivated(String[] url_list, String[] params, String desc) {
        boolean success = false;

        for (String url_list1 : url_list) {
            if (url_list1.charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url_list1.substring(1);
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
                    URL url = new URL(url_list1);
                    InputSource is = new InputSource(url);
                    if(success = load(is))
                        break;
                } catch (MalformedURLException mue) {
                    statusBar.setStatusText("Invalid URL");
                    console.warningReport("Invalid URL: " + url_list1, mue);
                }
            }
        }

        if(!success)
            console.errorReport("No valid URLs were found", null);
    }

    //----------------------------------------------------------
    // Implmentation of base class abstract methods
    //----------------------------------------------------------

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open
     */
    public void gotoLocation(URL url) {
        InputSource is = new InputSource(url);

        load(is);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load
     */
    public void gotoLocation(File file) {
        InputSource is = new InputSource(file);

        load(is);
    }

    protected void setWarning(String msg) {
        statusBar.setStatusText(msg);
        console.warningReport(msg, null);
    }

    protected void setError(String msg) {
        statusBar.setStatusText(msg);
        console.errorReport(msg, null);
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Setup the aviatrix pipeline here
     */
    private void setupAviatrix(JPanel panel)
    {
        // Assemble a simple single-threaded pipeline.
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

//        CullStage culler1 = new NullCullStage();
//        CullStage culler2 = new NullCullStage();
//        CullStage culler3 = new NullCullStage();

        GraphicsCullStage culler1 = new SimpleFrustumCullStage();
        GraphicsCullStage culler2 = new SimpleFrustumCullStage();
        GraphicsCullStage culler3 = new SimpleFrustumCullStage();

        culler1.setOffscreenCheckEnabled(false);
        culler2.setOffscreenCheckEnabled(false);
        culler3.setOffscreenCheckEnabled(false);

//        SortStage sorter = new NullSortStage();
//        SortStage sorter = new SimpleTransparencySortStage();
//        SortStage sorter = new DepthSortedTransparencyStage();
        GraphicsSortStage sorter1 = new StateAndTransparencyDepthSortStage();
        GraphicsSortStage sorter2 = new StateAndTransparencyDepthSortStage();
        GraphicsSortStage sorter3 = new StateAndTransparencyDepthSortStage();

        surface1 = new SimpleAWTSurface(caps);
        surface2 = new SimpleAWTSurface(caps);
        surface3 = new SimpleAWTSurface(caps);
//        surface = new StereoAWTSurface(caps);

//        surface1.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
//        surface2.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);
//        surface3.setStereoRenderingPolicy(DrawableSurface.ALTERNATE_FRAME_STEREO);

/*
        AudioOutputDevice adevice = new OpenALAudioDevice();

        AudioCullStage aculler = new NullAudioCullStage();
        AudioSortStage asorter = new NullAudioSortStage();

        DefaultAudioPipeline audioPipeline = new DefaultAudioPipeline();
        audioPipeline.setCuller(aculler);
        audioPipeline.setSorter(asorter);
        audioPipeline.setAudioDevice(adevice);
*/

        DefaultGraphicsPipeline pipeline1 = new DefaultGraphicsPipeline();
        DefaultGraphicsPipeline pipeline2 = new DefaultGraphicsPipeline();
        DefaultGraphicsPipeline pipeline3 = new DefaultGraphicsPipeline();

        pipeline1.setCuller(culler1);
        pipeline2.setCuller(culler2);
        pipeline3.setCuller(culler3);

        pipeline1.setSorter(sorter1);
        pipeline2.setSorter(sorter2);
        pipeline3.setSorter(sorter3);

        pipeline1.setGraphicsOutputDevice(surface1);
        pipeline2.setGraphicsOutputDevice(surface2);
        pipeline3.setGraphicsOutputDevice(surface3);

        // Two panels, side by side, assuming 45deg field of view
//        pipeline1.setEyePointOffset(0.0765f, 0, 0);
//        pipeline2.setEyePointOffset(-0.0765f, 0, 0);

        pipeline1.setScreenOrientation(0, 1, 0, -(float)(Math.PI / 2));
        pipeline3.setScreenOrientation(0, 1, 0, (float)(Math.PI / 2));

        displayManager1 = new SingleDisplayCollection();
        displayManager1.addPipeline(pipeline1);
        displayManager2 = new SingleDisplayCollection();
        displayManager2.addPipeline(pipeline2);
        displayManager3 = new SingleDisplayCollection();
        displayManager3.addPipeline(pipeline3);

        // Render manager
//        sceneManager = new MultiThreadRenderManager();
        sceneManager = new SingleThreadRenderManager();

        sceneManager.addDisplay(displayManager1);
        sceneManager.addDisplay(displayManager2);
        sceneManager.addDisplay(displayManager3);
        sceneManager.disableInternalShutdown();

//        sceneManager.setMinimumFrameInterval(20);
        //sceneManager.addAudioPipeline(audioPipeline);

        // Before putting the pipeline into run mode, put the canvas on
        // screen first.
        Component comp1 = (Component)surface1.getSurfaceObject();
        Component comp2 = (Component)surface2.getSurfaceObject();
        Component comp3 = (Component)surface3.getSurfaceObject();
        comp1.addComponentListener(this);
        comp2.addComponentListener(this);
        comp3.addComponentListener(this);

        JPanel p1 = new JPanel(new GridLayout(1, 3));

        p1.add(comp1);
        p1.add(comp2);
        p1.add(comp3);

        panel.add(p1, BorderLayout.CENTER);

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
            throw new RuntimeException("Failed to load factory");
        }

//        ContentLoadManager lm = new SimpleLoadManager();
        loadManager = new MemCacheLoadManager();
        scriptLoader = new DefaultScriptLoader();

        ScriptManager script_manager = new DefaultScriptManager();
        script_manager.setScriptLoader(scriptLoader);

        FrameStateManager state_manager = new DefaultFrameStateManager();

        PickingManager picker_manager = new DefaultPickingManager();
        picker_manager.setErrorReporter(console);

        SensorManager sensor_manager = new DefaultSensorManager();
        sensor_manager.setPickingManager(picker_manager);

        RouteManager route_manager = new DefaultRouteManager();
//        route_manager.setRouterFactory(new SimpleRouterFactory());
        route_manager.setRouterFactory(new ListsRouterFactory());

        DefaultHumanoidManager hanim_manager = new DefaultHumanoidManager();
        DefaultRigidBodyPhysicsManager physics_manager =
            new DefaultRigidBodyPhysicsManager();
        DefaultParticleSystemManager particle_manager =
            new DefaultParticleSystemManager();
        NetworkManager network_manager = new DefaultNetworkManager();
        DISProtocolHandler dis_handler = new DISProtocolHandler();
        network_manager.addProtocolHandler(dis_handler);

        eventModel = new DefaultEventModelEvaluator();

        // TODO: What to do about the other displayManagers?
        universe = new OGLStandardBrowserCore(eventModel, sceneManager, displayManager1);
        universe.addSensorStatusListener(this);
        universe.setErrorReporter(console);
        ((OGLStandardBrowserCore)universe).setHardwareFOV(90);

        worldLoader = new DefaultWorldLoaderManager(universe,
                                                    state_manager,
                                                    route_manager);
        worldLoader.setErrorReporter(console);
        worldLoader.registerBuilderFactory(Xj3DConstants.OPENGL_RENDERER,
                                           builder_fac);
        worldLoader.registerParserFactory(Xj3DConstants.OPENGL_RENDERER,
                                          parser_fac);

        NodeManager[] node_mgrs = {
            network_manager,
            hanim_manager,
            physics_manager,
            particle_manager
        };

        OGLLayerManagerFactory lmf = new OGLLayerManagerFactory();
        lmf.setErrorReporter(console);

        ViewpointManager vp_manager = new DefaultViewpointManager(universe);

        eventModel.initialize(script_manager,
                              route_manager,
                              sensor_manager,
                              state_manager,
                              loadManager,
                              vp_manager,
                              lmf, ((OGLStandardBrowserCore)universe),
                              node_mgrs);
        eventModel.setErrorReporter(console);

        // TODO: Needed on all surfaces?
        GraphicsResizeListener[] listeners = ((OGLStandardBrowserCore)universe).getGraphicsResizeListeners();

        for (GraphicsResizeListener listener : listeners) {
            surface1.addGraphicsResizeListener(listener);
        }

        // TODO: What to do about other components?
        DeviceFactory deviceFactory = new AWTDeviceFactory(
            comp1,
            Xj3DConstants.OPENGL_ID,
            surface2,
            console );

        InputDeviceManager idm = new InputDeviceManager( deviceFactory );

/*
        KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

        InputDeviceManager idm = new InputDeviceManager(universe.getIDString(),
                                                        comp2,
                                                        surface2);
*/
        KeyDeviceSensorManager kdsm = new KeyDeviceSensorManager( deviceFactory );

        sensor_manager.setInputManager(idm);
        sensor_manager.setKeyDeviceSensorManager(kdsm);

//        comp1.addKeyListener(idm);
//        comp2.addKeyListener(idm);
//        comp3.addKeyListener(idm);

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

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        worldLoader);
        java_sai.setErrorReporter(console);

        ScriptEngine ecma_sai = new ECMAScriptEngine(universe,
                                                     vp_manager,
                                                     route_manager,
                                                     state_manager,
                                                     worldLoader);
        ecma_sai.setErrorReporter(console);

        scriptLoader.registerScriptingEngine(jsai);
        scriptLoader.registerScriptingEngine(ecma);
        scriptLoader.registerScriptingEngine(java_sai);
        scriptLoader.registerScriptingEngine(ecma_sai);

        //universe.addNavigationStateListener(navToolbar);
        universe.addSensorStatusListener(this);

        setupProperties(universe, worldLoader);

        Runnable r = new Runnable() {

            @Override
            public void run() {
                console.setVisible(true);
            }
        };
        SwingUtilities.invokeLater(r);

/*
        DownloadProgressListener dl_list =
            new DownloadProgressListener(statusBar, console);

        ResourceConnection.addGlobalProgressListener(dl_list);
*/
    }

    //----------------------------------------------------------
    // Methods required by the ComponentListener interface.
    //----------------------------------------------------------

    @Override
    public void componentHidden(ComponentEvent evt) {
    }

    @Override
    public void componentMoved(ComponentEvent evt) {
    }

    @Override
    public void componentResized(ComponentEvent evt) {
/*
        Component canvas = (Component) evt.getSource();

        Dimension size = canvas.getSize();

        int width;
        int height;

        width = (int) size.getWidth();
        height = (int) size.getHeight();

        if (width > 0 && height > 0)
            universe.setViewport(new Rectangle(0,0,width,height));
*/
    }

    @Override
    public void componentShown(ComponentEvent evt) {
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    private void shutdownApp()
    {
        sceneManager.shutdown();
        eventModel.shutdown();
        loadManager.shutdown();
        scriptLoader.shutdown();
        surface1.dispose();
        surface2.dispose();
        surface3.dispose();
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param is The inputsource for this reader
     * @return true if the world loaded correctly
     */
    private boolean load(InputSource is) {
        inSetup = true;

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

        if (statusBar != null)
            statusBar.setStatusText("World Loaded Successfully");
        ret_val = true;
        inSetup = false;

        return ret_val;
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    @Override
    public void addNotify() {
        super.addNotify();

System.out.println ("Enabling rendering");
        sceneManager.setEnabled(true);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param argv The list of arguments for this application.
     */
    public static void main(String[] argv) {
        OGLMultiWallBrowser browser = new OGLMultiWallBrowser();

        browser.gotoLocation(new File(argv[1]));
    }
}
