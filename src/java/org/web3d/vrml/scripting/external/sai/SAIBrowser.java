/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.*;

import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.w3c.dom.Node;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.x3d.sai.*;
import org.xj3d.sai.*;

import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.web3d.browser.SensorStatusListener;
import org.web3d.browser.ScreenCaptureListener;
import org.web3d.browser.Xj3DConstants;

import org.j3d.util.ErrorReporter;

import org.web3d.vrml.lang.SceneMetaData;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.WriteableSceneMetaData;
import org.web3d.vrml.lang.InvalidNodeTypeException;
import org.web3d.vrml.lang.VRMLNode;

import org.web3d.vrml.scripting.browser.X3DCommonBrowser;

import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.web3d.vrml.scripting.sai.SAIVRMLScene;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.NavigationManager;
import org.xj3d.core.eventmodel.CursorManager;

import org.xj3d.core.loading.ContentLoadManager;

import org.xj3d.impl.core.eventmodel.DefaultNavigationManager;

/**
 * SAIBrowser implements the {@link Browser} interface, largely by
 * translating and interfacing between the wrapper classes and the
 * implementation class represented by {org.web3d.vrml.scripting.CommonBrowser}.
 *  <p>
 * To function correctly, SAIBrowser needs to be constructed using
 * CommonBrowser and BrowserCore instances.  The SAIBrowser then registers
 * as a listener so that BrowserCoreListener BrowserInitialized and
 * browserShutdown messages.  The BrowserCore instance is necessary mainly
 * for the global namespace and VRMLExecutionSpace information.
 *
 *  <p>
 *
 * @author Brad Vender, Rex Melton, Justin Couch
 * @version $Revision: 1.64 $
 */
public class SAIBrowser
    implements Xj3DBrowser,
               BrowserCoreListener,
               SensorStatusListener,
               ScreenCaptureListener {

    /** Error message when the browser has been disposed of */
    private static final String INVALID_BROWSER_MSG =
        "Cannot access the Browser object. It has been disposed of.";

    /** Error message when they give us neither profile or component */
    private static final String NULL_CREATE_SCENE_ARGS_MSG =
        "Both arguments are null. 19775-2, 6.3.11 requires that one or other " +
        "argument is to be non-null.";

    /** Error message when we catch an external exception whilst processing the
     * user's anchor listener link activation method.
     */
    private static final String LINK_PROCESS_MSG =
        "External code generated an exception during the " +
        "Xj3DAnchorListener's processLinkActivation() method. Falling back " +
        "to default internal processing behaviour";

    /** Message string when the selected VP is not a viewpoint */
    private static final String NOT_VP_MSG =
        "Referenced item is not a viewpoint";

    /** The default profile to use */
    private static final String DEFAULT_PROFILE = "Core";

    /**
     * A minimal X3D scene for use in querying supported capabilities
     * in the absence of a scene.
     */
    private static final String MINIMAL_X3D_SCENE =
        "#X3D V3.0 utf8\n"+"PROFILE Core\n";

    /** String for getBrowserName */
    private static final String BROWSER_NAME ="Xj3D SAI External Browser";

    /** The class name of a potential node factory */
    private String NODE_FACTORY_CLASS_NAME =
        "org.xj3d.sai.external.MappingSAINodeFactory";

    /** The list of browser listeners for browser events */
    private BrowserListener browserListener;

    /** List of external status event listeners.  */
    private StatusAdapter statusAdapter;

    /** Executor service used to send out browser events */
    private ExecutorService eventExecutor;

    /** The secondary implementation of the Browser*/
    private BrowserCore browserCore;

    /** The main implementation of the Browser */
    private X3DCommonBrowser browserImpl;

    /** The ErrorReporter to send errors and warnings to. */
    private ExternalErrorReporterAdapter errorReporter;

    /** The queue to post events to.*/
    private ExternalEventQueue eventQueue;

    /** The SAINodeFactory for use in mapping between VRMLNodeType and
    *  X3DNode instances. */
    private SAINodeFactory saiNodeFactory;

    /** The node factory for getting profile and component information,
    * and not actually used for constructing nodes here */
    private VRMLNodeFactory vrmlNodeFactory;

    /** The event adapter factory.
    * The event adapter system is reachable through this object. */
    private BufferedMappingSAIEventAdapterFactory adapterFactory;

    /** Route manager for handling user added/removed routes */
    private RouteManager routeManager;

    /** FrameState manager for creating nodes */
    private FrameStateManager stateManager;

    /** The load manager */
    private ContentLoadManager loadManager;

    /** The CursorManager */
    private CursorManager cursorManager;

    /** The current execution space */
    private VRMLExecutionSpace currentSpace;

    /** The current context */
    private X3DExecutionContext currentContext;

    /** Mapping of def'd Viewpoints to their real implementation */
    private Map<String, VRMLViewpointNodeType> viewpointDefMap;

    /** The set of rendering properties that the browser supports */
    private Map<String, Object> renderingProperties;

    /** The set of browser properties that the browser supports */
    private Map<String, Object> browserProperties;

    /**
     * External navigation manager for extended SAI use. Only created if
     * requested by the end user.
     */
    private NavigationUIManagerAdapter externalNavManager;

    /**
     * External cursor manager for extended SAI use. Only created if
     * requested by the end user.
     */
    private CursorUIManagerAdapter externalCursorManager;

    /**
     * External interface for those that want to mess with the CAD-specific
     * view.
     */
    private CADViewAdapter externalCADView;

    /**
     * Handler for the anchor processing by an external application. If this
     * is non-null, query it first before doing our own anchor processing.
     */
    private Xj3DAnchorListener anchorListener;

    /** Flag for controlling when the sensor messages have been sent */
    private boolean sensorDescriptionActive;

    /** Listener for screen capture processing by an external application. */
    private Xj3DScreenCaptureListener captureListener;

    /** The current number of frames left to capture */
    private int frameCapturesPendingCount;

    /**
     * Construct an SAIBrowser for the given VrmlDisplayPanel
     *
     *
     * @param browserImpl The delegated browser implementation
     * @param browserCore The BrowserCore to use as the implementation.
     * @param rm The route manager
     * @param fsm The frame state manager
     * @param clm The content load manager
     * @param eventQueue The buffer to send events to.
     * @param cm The cursor manager
     * @param reporter The ErrorReporter to use.  If null, will use
     *     DefaultErrorReporter's default.
     */
    public SAIBrowser(BrowserCore browserCore,
                      X3DCommonBrowser browserImpl,
                      RouteManager rm,
                      FrameStateManager fsm,
		      ContentLoadManager clm,
                      ExternalEventQueue eventQueue,
                      CursorManager cm,
                      ErrorReporter reporter) {

        if(browserCore == null)
            throw new IllegalArgumentException("Null BrowserCore");

        if(browserImpl == null)
            throw new IllegalArgumentException("Null CommonBrowser");

        this.browserCore = browserCore;
        this.browserImpl = browserImpl;
        this.eventQueue = eventQueue;
        routeManager = rm;
        stateManager = fsm;
		loadManager = clm;
        cursorManager = cm;

        sensorDescriptionActive = false;
        frameCapturesPendingCount = 0;

        viewpointDefMap = new HashMap<>();

        errorReporter = new ExternalErrorReporterAdapter(reporter);
        BrowserListenerMulticaster.setErrorReporter(errorReporter);

        eventExecutor = Executors.newSingleThreadExecutor();

        browserCore.addCoreListener(SAIBrowser.this);
        browserCore.addSensorStatusListener(SAIBrowser.this);

        Map<String, Object> browser_props = new HashMap<>();
        // Fill in details of browser properties here

        Map<String, Object> render_props = new HashMap<>();
        // Fill in details of rendering properties here

        renderingProperties = Collections.unmodifiableMap(render_props);

        // The null factory reference is corrected in the
        // NonMappingSAINodeFactory constructor.

        SimpleSAIFieldFactory fieldFactory =
            new SimpleSAIFieldFactory(eventQueue);

        adapterFactory =
            new BufferedMappingSAIEventAdapterFactory(2, browserCore.getVRMLClock());

        //////////////////////////////////////////////////////////////////////////////
        // rem: commented out pending relo-ing the scripting
        // packages to the org.xj3d.sai hierarchy
        //saiNodeFactory = getSAINodeFactory( fieldFactory, eventQueue );
        //if( saiNodeFactory == null ) {
        //////////////////////////////////////////////////////////////////////////////
        boolean use_concrete_nodes = false;
        try {
            // if ya ain't got Group, ya ain't got nodes
            Class<?> c = Class.forName( "org.xj3d.sai.external.node.grouping.SAIGroup" );
            use_concrete_nodes = true;
        } catch (ClassNotFoundException cnfe) {
        }

        if(use_concrete_nodes) {
            saiNodeFactory = new MappingSAINodeFactory(fieldFactory, eventQueue);
            browser_props.put("ABSTRACT_NODES", Boolean.TRUE);
            browser_props.put("CONCRETE_NODES", Boolean.TRUE);
        } else {
            saiNodeFactory = new NonMappingSAINodeFactory(fieldFactory, eventQueue);
            browser_props.put("ABSTRACT_NODES", Boolean.FALSE);
            browser_props.put("CONCRETE_NODES", Boolean.FALSE);
        }

        adapterFactory.setFieldFactory(fieldFactory);
        fieldFactory.setNodeFactory(saiNodeFactory);
        fieldFactory.setSAIEventAdapterFactory(adapterFactory);

        browser_props.put("PROTOTYPE_CREATE", Boolean.FALSE);
        browser_props.put("DOM_IMPORT", Boolean.TRUE);
        browser_props.put("EXTERNAL_INTERACTIONS", Boolean.TRUE);
        browser_props.put("XML_ENCODING", Boolean.TRUE);
        browser_props.put("CLASSIC_VRML_ENCODING", Boolean.TRUE);
        browser_props.put("BINARY_ENCODING", Boolean.TRUE);
        browserProperties = Collections.unmodifiableMap(browser_props);
    }

    //-------------------------------------------------------------------
    // Methods defined by Xj3DBrowser
    //-------------------------------------------------------------------

    /**
     * Set the handler for error messages. This can be used to replace the
     * stock console. Passing a value of null removes the currently registered
     * reporter. Setting this will replace the current reporter with this
     * instance. If the current reporter is the default system console, then
     * the console will not receive any further messages.
     *
     * @param reporter The error reporter instance to use
     */
    @Override
    public void setErrorReporter(Xj3DErrorReporter reporter) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        errorReporter.setErrorReporter(reporter);
        //statusAdapter.setErrorReporter(reporter);
    }

    /**
     * Add a listener for status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    @Override
    public void addStatusListener(Xj3DStatusListener l) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if (statusAdapter == null) {
            statusAdapter = new StatusAdapter(
				browserCore,
				stateManager,
				loadManager,
				errorReporter);
        }
        statusAdapter.addStatusListener(l);
    }

    /**
     * Remove a listener for status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    @Override
    public void removeStatusListener(Xj3DStatusListener l) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if (statusAdapter == null) {
            statusAdapter = new StatusAdapter(
				browserCore,
				stateManager,
				loadManager,
				errorReporter);
        }
        statusAdapter.removeStatusListener(l);
    }

    /**
     * Fetch the interface that allows an external application to implement
     * their own navigation user interface. This is guaranteed to be unique
     * per browser instance.
     *
     * @return An interface allowing end-user code to manipulate the
     *    navigation.
     */
    @Override
    public Xj3DNavigationUIManager getNavigationManager() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalNavManager == null) {
            NavigationManager mgr =
                new DefaultNavigationManager(browserCore);

            mgr.setErrorReporter(errorReporter);

            externalNavManager =
                new NavigationUIManagerAdapter(browserImpl.getViewpointManager(),
                                               mgr,
                                               browserCore);

            externalNavManager.setErrorReporter(errorReporter);
        }

        return externalNavManager;
    }

    /**
     * Fetch the interface that allows an external application to implement
     * their own cursor user interface.
     *
     * @return An interface allowing end-user code to manipulate the
     *    cursor.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public Xj3DCursorUIManager getCursorManager() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalCursorManager == null) {

            externalCursorManager =
                new CursorUIManagerAdapter(cursorManager, browserCore);

            externalCursorManager.setErrorReporter(errorReporter);
        }

        return externalCursorManager;
    }

    /**
     * Fetch the component-specific interface for managing a CAD scene. This
     * interface exposes CAD structures
     *
     * @return An interface allowing end-user code to manipulate the
     *    the CAD-specific structures in the scene.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public Xj3DCADView getCADView()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(externalCADView == null) {
            externalCADView = new CADViewAdapter(browserCore, saiNodeFactory);
            externalCADView.setErrorReporter(errorReporter);
        }

        return externalCADView;
    }

    /**
     * Set the minimum frame interval time to limit the CPU resources
     * taken up by the 3D renderer.  By default it will use all of them.
     *
     * @param millis The minimum time in milliseconds.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void setMinimumFrameInterval(int millis)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserCore.setMinimumFrameInterval(millis, true);
    }

    /**
     * Get the currently set minimum frame cycle interval. Note that this is
     * the minimum interval, not the actual frame rate. Heavy content loads
     * can easily drag this down below the max frame rate that this will
     * generate.
     *
     * @return The cycle interval time in milliseconds
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public int getMinimumFrameInterval()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserCore.getMinimumFrameInterval();
    }

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void setRenderingStyle(int style)
        throws IllegalArgumentException, InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserCore.setRenderingStyle(style);
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public int getRenderingStyle()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserCore.getRenderingStyle();
    }

    /**
     * Set the interceptor for the anchor node handling. This can be used to
     * process the clicks on Anchor nodes and replace or supplement the
     * existing behaviour. See documentation of the listener for more specific
     * details on usage patterns.
     * <p>
     * Only one interceptor instance can be registered. Setting a new item will
     * replace an existing registered instance. Setting a null value will clear
     * the current set instance.
     *
     * @param l The interceptor instance to register or null to clear
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void setAnchorListener(Xj3DAnchorListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        anchorListener = l;
    }

    /**
     * Request that the browser capture the next <i>n</i> number of frames as
     * images. This will begin from the frame after this method is called. If
     * no screen capture listener is registered at the time this is called, the
     * request is ignored.
     *
     * @param n The number of frames to capture
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void captureFrames(int n)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(captureListener == null)
            return;

        frameCapturesPendingCount = n;

        browserCore.captureScreenStart(this);
    }

    /**
     * Set the handler for processing screen captures. This can be used to
     * process images for saving to disc, processing or other task.
     * <p>
     * Only one listener instance can be registered. Setting a new item will
     * replace an existing registered instance. Setting a null value will clear
     * the current set instance.
     *
     * @param l The processing instance to register or null to clear
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void setScreenCaptureListener(Xj3DScreenCaptureListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        captureListener = l;
    }

    //-------------------------------------------------------------------
    // Methods defined by ExternalBrowser
    //-------------------------------------------------------------------

    /**
     * addBrowserListener adds the specified listener to the set of listeners
     * for this browser.
     *
     * @param l The listener to add to the list of listeners for this browser
     */
    @Override
    public void addBrowserListener(BrowserListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserListener = BrowserListenerMulticaster.add(browserListener, l);
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#beginUpdate */
    @Override
    public void beginUpdate() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        eventQueue.beginUpdate();
    }

    /** @see org.web3d.x3d.sai.ExternalBrowser#endUpdate */
    @Override
    public void endUpdate() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        eventQueue.endUpdate();
    }

    /** removeBrowserListener removes the specified listener from the set of
     *  listeners for this browser.
     * @param l The listener to remove from the list.
     * @see org.web3d.x3d.sai.ExternalBrowser#removeBrowserListener
     */
    @Override
    public void removeBrowserListener(BrowserListener l)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserListener =
            BrowserListenerMulticaster.remove(browserListener, l);
    }

    /** @see org.xj3d.sai.Xj3DBrowser#startRender */
    @Override
    public void startRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /** @see org.xj3d.sai.Xj3DBrowser#pauseRender */
    @Override
    public void pauseRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /** @see org.xj3d.sai.Xj3DBrowser#stopRender */
    @Override
    public void stopRender() {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        /** Should do something here. */
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Clean up and get rid of this browser.  When this method is called,
     * the event queue will be processed, the browser will shut down,
     * and any subsequent calls to browser methods will result in
     * InvalidBrowserException's being generated.
     */
    @Override
    public void dispose() throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        endUpdate();
        browserImpl = null;

        eventExecutor.shutdown();
        adapterFactory.shutdown();
        if (statusAdapter != null) {
            statusAdapter.shutdown();
            statusAdapter = null;
        }
        errorReporter.setErrorReporter((ErrorReporter)null);
//        externalCADView.shutdown();
//        externalNavManager.shutdown();
    }

    //-------------------------------------------------------------------
    // Methods defined by Browser
    //-------------------------------------------------------------------

    @Override
    public X3DScene createScene(ProfileInfo profile,
                                ComponentInfo[] components)
                                throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if((profile == null) &&
            ((components == null) || (components.length == 0)))
            throw new IllegalArgumentException(NULL_CREATE_SCENE_ARGS_MSG);

        VRMLNodeFactory new_fac;

        try {
            new_fac = (VRMLNodeFactory)vrmlNodeFactory.clone();
        } catch(CloneNotSupportedException cnse) {
            throw new InvalidBrowserException("Error cloning node factory");
        }

        String profile_name = profile == null ? DEFAULT_PROFILE : profile.getName();

        // JC: Do we need to set the spec version here from the parent factory or can we
        // assume that everything is still the same?
        //         new_fac.setSpecVersion();
        new_fac.setProfile(profile_name);

        int num_comp = (components == null) ? 0 : components.length;
        for(int i = 0; i < num_comp; i++) {
            new_fac.addComponent(components[i].getName(),
                components[i].getLevel());
        }

        VRMLWorldRootNodeType root_node =
            (VRMLWorldRootNodeType)new_fac.createVRMLNode("WorldRoot",
            false);
        root_node.setFrameStateManager(stateManager);
        root_node.setErrorReporter(errorReporter);

        root_node.setupFinished();

        int[] version = new_fac.getSpecVersion();

        WriteableSceneMetaData md =
            new WriteableSceneMetaData(version[0] + " " + version[1],
                                       false,
                                       SceneMetaData.SCRIPTED_ENCODING);

        SAIVRMLScene v_scene = new SAIVRMLScene(md, version[0], version[1]);

        v_scene.setNodeFactory(new_fac);
        v_scene.setWorldRootURL(browserImpl.getWorldURL());
        v_scene.setRootNode(root_node);

        // TODO: need to generateProtoCreator and call setTemplateCreator

        root_node.setContainedScene(v_scene);

        SAIScene x3dScene = new SAIScene(v_scene,
                                         routeManager,
                                         stateManager,
                                         saiNodeFactory,
                                         eventQueue, 
                                         root_node,
                                         errorReporter);

        return x3dScene;
    }

    @Override
    public X3DScene createX3DFromString(String string)
        throws InvalidBrowserException, InvalidX3DException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene = null;

        try {
            scene = browserImpl.createX3DFromString(string);
        } catch(IOException | InvalidNodeTypeException e) {
            errorReporter.errorReport(e.getMessage(), e);
        }

        return createScene(scene);
    }

    @Override
    public X3DScene createX3DFromStream(String fileParent, InputStream is)
        throws InvalidBrowserException, InvalidX3DException, IOException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return createScene(browserImpl.createX3DFromStream(fileParent, is));
    }

    @Override
    public X3DScene createX3DFromURL(String[] url)
        throws InvalidBrowserException, InvalidURLException, InvalidX3DException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLScene scene=browserImpl.createX3DFromURL(url);

        if(scene == null)
            throw new InvalidURLException("Unable to load any URLS");

        return createScene(scene);
    }

    private X3DScene createScene(VRMLScene scene) {
        VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

        return new SAIScene(scene,
                            routeManager,
                            stateManager,
                            saiNodeFactory,
                            eventQueue,
                            space,
                            errorReporter);
    }

    @Override
    public ComponentInfo getComponentInfo(String name, int level)
        throws InvalidBrowserException, NotSupportedException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ComponentInfo retVal = null;

        ComponentInfo components[]=getSupportedComponents();
        for (ComponentInfo component : components) {
            if (component.getName().equals(name)) {
                if (component.getLevel() >= level) {
                    retVal = new SAIComponentInfo(name, level, component.getTitle(), component.getProviderURL());
                    break;
                } else {
                    throw new NotSupportedException();
                }
            }
        }

        // Not found in our local list? Try asking the node factory again.
        if(retVal == null) {
            VRMLNodeFactory node_fac = getVRMLNodeFactory();

            org.web3d.vrml.lang.ComponentInfo info =
                node_fac.findComponent(name, level);

            if(info == null)
                throw new NotSupportedException("Component not supported by Xj3D: " + name);

            retVal = new SAIComponentInfo(info);
        }

        return retVal;
    }

    /** @see org.web3d.x3d.sai.Browser#getCurrentSpeed */
    @Override
    public float getCurrentSpeed() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getCurrentSpeed();
    }

    /** @see org.web3d.x3d.sai.Browser#getCurrentFrameRate */
    @Override
    public float getCurrentFrameRate() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getCurrentFrameRate();
    }

    /** @see org.web3d.x3d.sai.Browser#getExecutionContext */
    @Override
    public X3DExecutionContext getExecutionContext()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        VRMLExecutionSpace space = browserCore.getWorldExecutionSpace();

        if(space != currentSpace) {
            X3DExecutionContext ctx = new
                SAIScene((VRMLScene)space.getContainedScene(),
                routeManager,
                stateManager,
                saiNodeFactory,
                eventQueue,
                browserCore.getWorldExecutionSpace(),
                errorReporter);

            currentContext = ctx;
        }

        return currentContext;
    }

    /** Returns the name of the Browser.
     *  @return The name of the Browser
     *  @see org.web3d.x3d.sai.Browser#getName
     */
    @Override
    public String getName() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return BROWSER_NAME;
    }

    /** @see org.web3d.x3d.sai.Browser#getProfile */
    @Override
    public ProfileInfo getProfile(String name) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        ProfileInfo retVal = null;

        ProfileInfo[] profiles=getSupportedProfiles();
        for (ProfileInfo profile : profiles) {
            if (profile.getName().equals(name)) {
                retVal = profile;
                break;
            }
        }

        if(retVal == null) {
            VRMLNodeFactory node_fac = getVRMLNodeFactory();

            org.web3d.vrml.lang.ProfileInfo info =
                node_fac.findProfile(name);

            if(info == null)
                throw new NotSupportedException("Profile not supported by Xj3D: " + name);

            retVal = new SAIProfileInfo(info);
        }

        return retVal;
    }

    /** @see org.web3d.x3d.sai.Browser#getSupportedComponents */
    @Override
    public ComponentInfo[] getSupportedComponents() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        org.web3d.vrml.lang.ComponentInfo[] components=
            getVRMLNodeFactory().getAvailableComponents();
        ComponentInfo results[] = new ComponentInfo[components.length];

        for(int counter=0;counter<results.length;counter++)
            results[counter]=new SAIComponentInfo(components[counter]);

        return results;
    }

    /** @see org.web3d.x3d.sai.Browser#getSupportedProfiles */
    @Override
    public ProfileInfo[] getSupportedProfiles() {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        org.web3d.vrml.lang.ProfileInfo profiles[]=
            getVRMLNodeFactory().getAvailableProfiles();
        if(profiles==null)
            throw new RuntimeException("Null array from getAvailableProfiles");
        else {
            ProfileInfo[] result=new ProfileInfo[profiles.length];
            for(int counter=0; counter<profiles.length; counter++) {
                result[counter]=new SAIProfileInfo(profiles[counter]);
            }
            return result;
        }
    }

    /**
     * Returns the version string for this Browser.
     * @return The version string for this Browser
     * @see org.web3d.x3d.sai.Browser#getVersion
     */
    @Override
    public String getVersion() throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return Xj3DConstants.VERSION;
    }

    @Override
    public X3DScene importDocument(Node aDocument) {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);
        try {
            VRMLScene scene = browserImpl.importDocument(aDocument);

            VRMLExecutionSpace space = (VRMLExecutionSpace)scene.getRootNode();

            return new SAIScene(scene,
                routeManager,
                stateManager,
                saiNodeFactory,
                eventQueue,
                space,
                errorReporter);
        } catch (NotSupportedException e) {
            throw new InvalidDocumentException("Unable to process document.  Reason:  "+e.getMessage());
        }
    }

    @Override
    public void loadURL(String[] urls, Map<String, Object> params)
        throws InvalidBrowserException, InvalidURLException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);


        browserImpl.loadURL(urls,params);
    }

    /** @see org.web3d.x3d.sai.Browser#replaceWorld */
    @Override
    public void replaceWorld(X3DScene scene)
        throws IllegalArgumentException, InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(scene instanceof SAIScene)
            browserImpl.replaceWorld(((SAIScene)scene).getRealScene());
        else if(scene == null)
            browserImpl.replaceWorld(null);
        else
            throw new IllegalArgumentException("Incorrect scene type.");

        currentContext = null;
        eventQueue.clear();
        adapterFactory.clear();
    }

    /**
     * Get the description of the current world.
     *
     * @return A description string or null if none set
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    @Override
    public String getDescription()
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserImpl.getDescription();
    }

    @Override
    public void setDescription(String newDescription)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.setDescription(newDescription);
    }

    /**
     * Get the collection of rendering properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    @Override
    public Map<String, Object> getRenderingProperties()
        throws InvalidBrowserException, InvalidOperationTimingException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return renderingProperties;
    }

    /**
     * Get the collection of browser properties that the browser provides.
     * Rendering properties are key/value pairs, as defined in table 9.2 of
     * ISO/IEC 19775-1. Keys are instances of Strings, while the value is
     * dependent on the property. If the property is not defined in the
     * returned map, treat it as not being supported by the browser.
     *
     * @return A read-only map of the list of properties defined by the browser
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     * @throws InvalidOperationTimingException This was not called during the
     *    correct timing during a script (may be called at any time from
     *    external)
     */
    @Override
    public Map<String, Object> getBrowserProperties()
        throws InvalidBrowserException, InvalidOperationTimingException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        return browserProperties;
    }

    @Override
    public void nextViewpoint()
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.nextViewpoint();
    }

    @Override
    public void nextViewpoint(int layer)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.nextViewpoint(layer);
    }

    @Override
    public void previousViewpoint()
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.previousViewpoint();
    }

    @Override
    public void previousViewpoint(int layer)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.previousViewpoint(layer);
    }

    @Override
    public void firstViewpoint()
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.firstViewpoint();
    }

    @Override
    public void firstViewpoint(int layer)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.firstViewpoint(layer);
    }

    @Override
    public void lastViewpoint()
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.lastViewpoint();
    }

    @Override
    public void lastViewpoint(int layer)
        throws InvalidBrowserException {
        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        browserImpl.lastViewpoint(layer);
    }

    /**
     * Print the message to the browser console without wrapping a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void print(Object msg)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(msg != null)
            errorReporter.partialReport(msg.toString());
    }

    /**
     * Print the message to the browser console and append a new line
     * onto it.
     *
     * @param msg The object to be printed
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    @Override
    public void println(Object msg)
        throws InvalidBrowserException {

        if(browserImpl == null)
            throw new InvalidBrowserException(INVALID_BROWSER_MSG);

        if(msg != null)
            errorReporter.messageReport(msg.toString());
    }

    //---------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //---------------------------------------------------------

    /** @see org.web3d.browser.BrowserCoreListener#browserInitialized */
    @Override
    public void browserInitialized(VRMLScene scene) {

        // Finally set up the viewpoint def name list. Have to start from
        // the list of DEF names as the Viewpoint nodes don't store the DEF
        // name locally.
        Map<String, VRMLNode> def_map = scene.getDEFNodes();
        Iterator<String> itr = def_map.keySet().iterator();

        while (itr.hasNext()) {
            String key = itr.next();
            Object vp = def_map.get(key);

            if(vp instanceof VRMLViewpointNodeType)
                viewpointDefMap.put(key, (VRMLViewpointNodeType)vp);
        }

        broadcastEvent(new BrowserEvent(this, BrowserEvent.INITIALIZED));

        String loadedUri = scene.getLoadedURI();

        if (loadedUri != null) {

            int idx = loadedUri.indexOf("#");

            if (idx > 0) {
                String def_name = loadedUri.substring(idx+1);

                VRMLViewpointNodeType vp =
                        viewpointDefMap.get(def_name);


                if(vp != null) {
                    browserCore.changeViewpoint(vp);
                } else {
                    errorReporter.messageReport("Unknown Viewpoint " + def_name);
                }
            }
        }
    }

    /**
     * The browser tried to load a URL and failed. It is typically because
     * none of the URLs resolved to anything valid or there were network
     * failures.
     *
     * @param msg An error message to go with the failure
     */
    @Override
    public void urlLoadFailed(String msg) {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.URL_ERROR));
    }

    /** @see org.web3d.browser.BrowserCoreListener#browserShutdown */
    @Override
    public void browserShutdown() {
        broadcastEvent(new BrowserEvent(this, BrowserEvent.SHUTDOWN));

        viewpointDefMap.clear();
    }

    /** @see org.web3d.browser.BrowserCoreListener#browserDisposed */
    @Override
    public void browserDisposed() {
        if(browserImpl != null) {
            // if the browser core is being disposed and this client
            // has not already been disposed - take care of it now.
            dispose();
        }
    }

    //----------------------------------------------------------
    // Methods defined by SensorStatusListener
    //----------------------------------------------------------

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    @Override
    public void deviceOver(int type, String desc) {
        if((desc != null) && (statusAdapter != null)) {
            sensorDescriptionActive = true;
            statusAdapter.sendStatusMessage(desc);
        }
    }

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    @Override
    public void deviceNotOver(int type) {
        if(statusAdapter != null && sensorDescriptionActive) {
            sensorDescriptionActive = false;
            statusAdapter.sendStatusMessage(null);
        }
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

        boolean process_link = true;

        if(anchorListener != null) {
            try {
                process_link =
                    !anchorListener.processLinkActivation(urls, params);

            } catch(Exception e) {
                errorReporter.warningReport(LINK_PROCESS_MSG, e);
            }
        }

        if(!process_link)
            return;

        for (String url : urls) {
            if (url.charAt(0) == '#') {
                // move to the viewpoint.
                String def_name = url.substring(1);
                VRMLBindableNodeType vp =
                        viewpointDefMap.get(def_name);
                if(vp != null) {
                    VRMLClock clk = browserCore.getVRMLClock();

                    vp.setBind(true, true, clk.getTime());
                } else {
                    errorReporter.warningReport(NOT_VP_MSG, null);
                }
            } else {
                // TODO:
                // Not sure how to convert the normal params from the anchor
                // now "name=value" form to the map here. Are those parameters
                // even acceptable?
                browserImpl.loadURL(urls, null);
            }
        }
    }

    //-------------------------------------------------------------------
    // Methods defined by ScreenCaptureListener
    //-------------------------------------------------------------------

    /**
     * Notification of a new screen capture.  This will be in openGL pixel
     * order.
     *
     * @param buffer The screen capture
     * @param width The width in pixels of the captured screen
     * @param height The height in pixels of the captured screen
     */
    @Override
    public void screenCaptured(Buffer buffer, int width, int height) {
        ByteBuffer pixelsRGB = (ByteBuffer) buffer;

        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the
        // rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL.

        int p = width * height * 3; // Points to first byte (red) in each row.
        int q;                  // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int w3 = width * 3;         // Number of bytes in each row

        for(int row = 0; row < height; row++) {
            p -= w3;
            q = p;
            for(int col = 0; col < width; col++) {
                int iR = pixelsRGB.get(q++);
                int iG = pixelsRGB.get(q++);
                int iB = pixelsRGB.get(q++);

                pixelInts[i++] = 0xFF00_0000
                             | ((iR & 0x0000_00FF) << 16)
                             | ((iG & 0x0000_00FF) << 8)
                             | (iB & 0x0000_00FF);
            }

        }

        BufferedImage bufferedImage =
               new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

        bufferedImage.setRGB( 0, 0, width, height, pixelInts, 0, width );

        frameCapturesPendingCount--;

        if(frameCapturesPendingCount == 0)
            browserCore.captureScreenEnd();


        try {
            captureListener.screenCaptured( bufferedImage );
        } catch ( Exception e ) {
            errorReporter.errorReport(e.getMessage(), e);
        }
    }

    //---------------------------------------------------------
    // Internal implementation methods
    //---------------------------------------------------------

    /**
     * Initialize the world to a known state.
     * This is necessary to ensure that all of the underlying behaviors
     * are active (which the buffering system relies on).  As such, this
     * method calls down to the browser core and common browser instances
     * directly rather than using the SAI methods.
     */
    public void initializeWorld() {
    }

    /**
     * Internal convenience routine for sending events to all listeners.
     * Not very efficient, but faithful to the wording of the spec.
     */
    private void broadcastEvent(BrowserEvent e) {
        if(browserListener != null)
            eventExecutor.submit(new BrowserEventTask(browserListener, e));
    }

    /**
     * Get a valid VRMLNodeFactory instance
     */
    private VRMLNodeFactory getVRMLNodeFactory() {
        if(vrmlNodeFactory == null) {
            X3DExecutionContext context=getExecutionContext();
            if(context == null) {
                // resort to making a new scene and getting its node factory
                // Would be really nice to have a method on X3DCommonBrowser
                // that produced an appropriate VRMLNodeFactory.
                SAIScene basicScene =
                    (SAIScene)createX3DFromString(MINIMAL_X3D_SCENE);
                vrmlNodeFactory=basicScene.getVRMLNodeFactory();
            } else {
                vrmlNodeFactory=((SAIScene)context).getVRMLNodeFactory();
            }
        }
        return vrmlNodeFactory;
    }

    /**
     * Search for and return a higher conformance level node factory
     *
     * @return An SAINodeFactory, or null if it could not be found
     */
    private SAINodeFactory getSAINodeFactory(SAIFieldFactory fieldFactory,
                                             ExternalEventQueue queue) {
        SAINodeFactory factory = null;
        try {
            // get the class instance
            Class<?> factoryClass = Class.forName(NODE_FACTORY_CLASS_NAME);

            // get the class's constructor with the appropriate arguments
            Constructor<?> constructor =
                    factoryClass.getConstructor(new Class<?>[]{
                                            SAIFieldFactory.class,
                                            ExternalEventQueue.class } );

            // instantiate the object
            Object object =
                constructor.newInstance(new Object[]{ fieldFactory, queue});

            // cast it to ensure it's the type we want
            factory = (SAINodeFactory)object;

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }

        return factory;
    }
}
