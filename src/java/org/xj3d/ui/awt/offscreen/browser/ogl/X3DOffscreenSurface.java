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

package org.xj3d.ui.awt.offscreen.browser.ogl;

// External imports
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.ietf.uri.*;

import org.j3d.aviatrix3d.output.graphics.PbufferSurface;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.X3DResourceFactory;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.renderer.ogl.browser.OGLBrowserCanvas;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.browser.X3DCommonBrowser;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIBrowser;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;
import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.InvalidBrowserException;
import org.web3d.x3d.sai.X3DComponent;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.ContentLoadManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.WorldLoaderManager;
import org.xj3d.impl.core.loading.MemCacheLoadManager;
import org.xj3d.sai.BrowserConfig;
import org.xj3d.ui.awt.net.content.AWTContentHandlerFactory;

/**
 * Swing JPanel implementation that wraps the functionality of a X3D browser
 * into a convenient, easy to use form for the SAI.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class X3DOffscreenSurface implements X3DComponent {

    /** Error message when setting up the system properties */
    private static final String PROPERTY_SETUP_ERR =
        "Error setting up system properties in X3DOffscreenSurface";

    /** The SAIBrowser instance */
    private SAIBrowser saiBrowser;

    /** The offscreen surface that we're rendering to */
    private PbufferSurface renderSurface;

    /** The canvas used to display the world */
    private OGLBrowserCanvas mainCanvas;

    /** The internal universe */
    private OGLStandardBrowserCore universe;

    /**
     * Create an instance of the panel configured to show or hide the controls
     * and only shows VRML97 content.
     *
     * @param parameters The object containing the browser's configuration parameters
     */
    public X3DOffscreenSurface(BrowserConfig parameters) {

        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setDoubleBuffered(false);
        caps.setHardwareAccelerated(true);

        renderSurface = new PbufferSurface(caps,
                                           parameters.preferredWidth,
                                           parameters.preferredHeight);

        mainCanvas = new OGLBrowserCanvas(renderSurface, null, parameters);
        mainCanvas.initialize();
        universe = mainCanvas.getUniverse();

        setupProperties(parameters.textureQuality);

        universe = mainCanvas.getUniverse();
        RouteManager route_manager = mainCanvas.getRouteManager();
        FrameStateManager state_manager = mainCanvas.getFrameStateManager();
        WorldLoaderManager wlm = mainCanvas.getWorldLoaderManager();
        ViewpointManager vp_manager = mainCanvas.getViewpointManager();
        ContentLoadManager load_manager = new MemCacheLoadManager();

        ScriptManager sm = mainCanvas.getScriptManager();
        ScriptLoader s_loader = sm.getScriptLoader();

        ScriptEngine java_sai = new JavaSAIScriptEngine(universe,
                                                        vp_manager,
                                                        route_manager,
                                                        state_manager,
                                                        wlm);

        ScriptEngine ecma = new ECMAScriptEngine(universe,
                                                 vp_manager,
                                                 route_manager,
                                                 state_manager,
                                                 wlm);

        s_loader.registerScriptingEngine(java_sai);
        s_loader.registerScriptingEngine(ecma);

        X3DCommonBrowser browser_impl =
            new X3DCommonBrowser(universe,
                                 vp_manager,
                                 route_manager,
                                 state_manager,
                                 wlm);

        ExternalEventQueue eventQueue = new ExternalEventQueue(null);
        mainCanvas.getEventModelEvaluator().addExternalView(eventQueue);

        saiBrowser = new SAIBrowser(universe,
                                    browser_impl,
                                    route_manager,
                                    state_manager,
                                    load_manager,
                                    eventQueue,
                                    null,
                                    null);


        mainCanvas.setEnabled(true);
    }

    //-----------------------------------------------------------------------
    // Methods defined by X3DComponent
    //-----------------------------------------------------------------------

    @Override
    public ExternalBrowser getBrowser() {
        return saiBrowser;
    }

    @Override
    public Object getImplementation() {
        return renderSurface;
    }

    @Override
    public void shutdown() {
        try {
            // the external browser instance can be
            // explicitly disposed by the user, without
            // our knowledge. calling dispose a second
            // time will throw an exception - javadoc
            // says we're suppose to quitely ignore
            // repeated calls.
            saiBrowser.dispose();

        } catch (InvalidBrowserException e) {
        }

        if(universe != null)
            universe.dispose();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------


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
                } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException t) {
                    //console.warningReport("Native image loaders not available", null);
                }
                
                BrowserCore core = mainCanvas.getUniverse();
                WorldLoaderManager wlm =
                        mainCanvas.getWorldLoaderManager();
                
                ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
                
                URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
                if(!(res_fac instanceof X3DResourceFactory)) {
                    res_fac = new X3DResourceFactory(res_fac);
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
//            console.warningReport(PROPERTY_SETUP_ERR, null);
        }
    }
}
