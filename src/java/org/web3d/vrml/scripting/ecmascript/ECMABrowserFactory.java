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

package org.web3d.vrml.scripting.ecmascript;

// External imports
import java.util.Map;
import java.util.WeakHashMap;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.BrowserCoreListener;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.scripting.ecmascript.x3d.Browser;

import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * A factory implementation for creating and caching specific instances of
 * the script {@link vrml.Browser} interface.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
class ECMABrowserFactory implements BrowserCoreListener {

    /**
     * Class that represents the external reporter
     */
    private ErrorReporter errorReporter;

    /**
     * The basic browser core functionality that this script hooks to
     */
    private BrowserCore core;

    /**
     * Route manager for dealing with add/remove ROUTE methods
     */
    private RouteManager routeManager;

    /**
     * Viewpoint manager for dealing with next/previous VP methods
     */
    private ViewpointManager viewpointManager;

    /**
     * FrameState manager for creating nodes
     */
    private FrameStateManager stateManager;

    /**
     * World load manager for file loading
     */
    private WorldLoaderManager loadManager;

    /**
     * Mapping of execution spaces to browser instances
     */
    private Map<VRMLExecutionSpace, Browser> browsers;

    /**
     * Create a factory that represents the given universe details.
     *
     * @param browser The core representation of the browser
     * @param vpm The manager for viewpoints
     * @param rm A route manager for users creating/removing routes
     * @param wlm Loader for full files
     * @throws IllegalArgumentException Any one of the parameters is null
     */
    ECMABrowserFactory(BrowserCore browser,
            ViewpointManager vpm,
            RouteManager rm,
            FrameStateManager fsm,
            WorldLoaderManager wlm) {

        core = browser;
        routeManager = rm;
        loadManager = wlm;
        stateManager = fsm;
        viewpointManager = vpm;

        core.addCoreListener(ECMABrowserFactory.this);

        browsers = new WeakHashMap<>();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Create or fetch the browser instance of the browser interface that
     * corresponds to the given execution space.
     *
     * @param space The execution space we need a browser for
     * @return A corresponding {@link vrml.Browser} instance
     */
    public Browser getBrowser(VRMLExecutionSpace space) {
        Browser browser = browsers.get(space);

        if (browser == null) {
            browser = new Browser(space,
                    core,
                    viewpointManager,
                    routeManager,
                    stateManager,
                    loadManager);

            browser.setErrorReporter(errorReporter);
            browsers.put(space, browser);
        }

        return browser;
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if (reporter == null) {
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        }
    }

    //----------------------------------------------------------
    // Methods defined by BrowserCoreListener
    //----------------------------------------------------------

    /**
     * The browser has been initialised with new content. The content given is
     * found in the accompanying scene and description.
     *
     * @param scene The scene of the new content
     */
    @Override
    public void browserInitialized(VRMLScene scene) {
    }

    /**
     * The tried to load a URL and failed. It is typically because none of the
     * URLs resolved to anything valid or there were network failures.
     *
     * @param msg An error message to go with the failure
     */
    @Override
    public void urlLoadFailed(String msg) {
    }

    /**
     * The browser has been shut down and the previous content is no longer
     * valid.
     */
    @Override
    public void browserShutdown() {
        browsers.clear();
    }

    /**
     * The browser has been disposed, all resources may be freed.
     */
    @Override
    public void browserDisposed() {
        browsers.clear();
    }
}

