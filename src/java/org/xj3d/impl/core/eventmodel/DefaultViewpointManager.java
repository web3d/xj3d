/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// External imports
import java.util.ArrayList;
import java.util.List;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.ViewpointStatusListener;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.eventmodel.ViewpointStatusListenerMulticaster;

/**
 * Default implementation of a manager for Viewpoint nodes.
 * <p>
 *
 * Keeps track of all viewpoints, manages the viewpoint list based on the
 * sets of active viewpoints. Next and previous commands will work on the
 * currently active navigation layer.
 * <p>
 * Creating an instance of this class will automatically register it as
 * viewpoint and navigation state listeners with the browser core. The end user
 * is not required to do this.
 *
 *
 * @author Alan Hudson
 * @version $Revision: 1.15 $
 */
public class DefaultViewpointManager
    implements ViewpointManager,
               ViewpointStatusListener {

    /** Error message when the user code barfs */
    private static final String REMOVE_ERROR_MSG =
        "Error sending viewpoint remove message: ";

    /** Error message when the user code barfs */
    private static final String ADD_ERROR_MSG =
        "Error sending viewpoint add message: ";

    /** Error message when the user code barfs */
    private static final String CHANGE_ERROR_MSG =
        "Error sending viewpoint change message: ";

    /** Error message when we get a barf sending VP bound events */
    private static final String SELECT_ERROR_MSG =
        "Error sending viewpoint bound notification events: ";

    /** Default error message when sending the error message fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending viewpoint state change event: ";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The next viewpoint to view.  NULL if none */
    private VRMLViewpointNodeType nextViewpoint;

    /** The current viewpoint being viewed.  NULL if none */
    private VRMLViewpointNodeType currentViewpoint;

    /**
     * Viewpoint nodes indexed by the layer they are in. The array contains
     * another ArrayList in each index, or null if that layer is no longer
     * valid. Each of these nested arrays contains a list of the Viewpoint
     * nodes in that layer.
     */
    private List<List<VRMLViewpointNodeType>> viewpointsByLayer;

    /**
     * The default viewpoint for each layer ID. Contains a list of
     * VRMLViewpointNodeType instances.
     */
    private List<VRMLViewpointNodeType> defaultViewpoints;

    /**
     * The bound viewpoint for each layer ID. Contains a list of
     * VRMLViewpointNodeType instances.
     */
    private List<VRMLViewpointNodeType> boundViewpoints;

    /** The currently active layer ID */
    private int activeLayerId;

    /** External user status listeners */
    private org.xj3d.core.eventmodel.ViewpointStatusListener externalListeners;

    /** The browser core reference for VP management */
    private BrowserCore browserCore;

    /**
     * Create a new, empty instance of the humanoid manager.
     *
     * @param core The browser core
     */
    public DefaultViewpointManager(BrowserCore core) {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        viewpointsByLayer = new ArrayList<>();
        defaultViewpoints = new ArrayList<>();
        boundViewpoints = new ArrayList<>();
        activeLayerId = -1;

        browserCore = core;
        browserCore.addViewpointStatusListener(DefaultViewpointManager.this);
    }

    //-------------------------------------------------------------
    // Methods defined by ViewpointManager
    //-------------------------------------------------------------

    /**
     * Update the viewpoint.  Called at the beginning of the event model.
     *
     * @param time The time.
     */
    @Override
    public void updateViewpoint(long time) {
        if (nextViewpoint != null) {
            nextViewpoint.setBind(true, true, time);
            currentViewpoint = nextViewpoint;
            nextViewpoint = null;
        }
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the current viewpoint.
     *
     * @param viewpoint The new current viewpoint.
     */
    @Override
    public void setViewpoint(VRMLViewpointNodeType viewpoint) {
        nextViewpoint = viewpoint;
    }

    /**
     * Reset the current viewpoint.
     */
    @Override
    public void resetViewpoint() {
        nextViewpoint = currentViewpoint;
    }

    /**
     * Go to the first declared viewpoint at the next available opportunity.
     * This corresponds to the SAI Browser.firstViewpoint() call.
     */
    @Override
    public void firstViewpoint() {
        firstViewpoint(activeLayerId);
    }


    /**
     * Go to the first declared viewpoint int the specified layer at the
     * next available opportunity. This corresponds to the SAI
     * Browser.firstViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    @Override
    public void firstViewpoint(int layer) {
        List<VRMLViewpointNodeType> l = viewpointsByLayer.get(layer);
        if(!l.isEmpty()) {
            nextViewpoint = l.get(0);
            if(nextViewpoint == defaultViewpoints.get(layer))
                nextViewpoint = l.get(1);
        }
    }

    /**
     * Go to the last declared viewpoint at the next available opportunity.
     * This corresponds to the SAI Browser.lastViewpoint() call.
     */
    @Override
    public void lastViewpoint() {
        lastViewpoint(activeLayerId);
    }

    /**
     * Go to the last declared viewpoint of the specified layer at the next
     * available opportunity. This corresponds to the SAI
     * Browser.lastViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    @Override
    public void lastViewpoint(int layer) {
        List<VRMLViewpointNodeType> l = viewpointsByLayer.get(layer);
        int num_vps = l.size();

        if(num_vps != 0) {
            nextViewpoint = l.get(num_vps - 1);
            if(nextViewpoint == defaultViewpoints.get(layer))
                nextViewpoint = l.get(num_vps - 2);
        }
    }

    /**
     * Go to the next viewpoint at the next available opportunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * next index it can find. This corresponds to the SAI
     * Browser.nextViewpoint() call.
     */
    @Override
    public void nextViewpoint() {
        nextViewpoint(activeLayerId);
    }

    /**
     * Go to the next viewpoint at the next available opportunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * next index it can find for the specific layer ID. This corresponds to the
     * SAI Browser.previousViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    @Override
    public void nextViewpoint(int layer) {
        List<VRMLViewpointNodeType> l =
            viewpointsByLayer.get(layer);

        if(l.size() < 2)
            return;

        int idx = l.indexOf(currentViewpoint);

        if(idx != -1) {
            if(idx == (l.size() - 1))
                idx = 0;
            else
                idx++;

            // Skip the default viewpoint
            Object def_vp = defaultViewpoints.get(layer);
            if(idx == l.indexOf(def_vp)) {
                idx++;
            }
            // recorrect if we're running off the end of the list
            if(idx > (l.size() - 1)) {
                idx = 0;
            }
            nextViewpoint = l.get(idx);
        }
    }

    /**
     * Go to the previous viewpoint at the next available opportunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * previous index it can find. This corresponds to the SAI
     * Browser.previousViewpoint() call.
     */
    @Override
    public void previousViewpoint() {
        previousViewpoint(activeLayerId);
    }

    /**
     * Go to the previous viewpoint at the next available opportunity. It looks at
     * the added list for the index of the current viewpoint and moves to the
     * previous index it can find for the specific layer ID. This corresponds to the
     * SAI Browser.previousViewpoint() call.
     *
     * @param layer The ID of the layer. Must be between 0 and the maximum
     *   layer number
     */
    @Override
    public void previousViewpoint(int layer) {
        List<VRMLViewpointNodeType> l =
            viewpointsByLayer.get(layer);

        if(l.size() < 2)
            return;

        int idx = l.indexOf(currentViewpoint);

        if(idx != -1) {
            if(idx == 0)
                idx = l.size() - 1;
            else
                idx--;

            // Skip the default viewpoint
            Object def_vp = defaultViewpoints.get(layer);
            if(idx == l.indexOf(def_vp))
                idx--;

            // recorrect if we're running off the end of the list
            if(idx < 0)
                idx = l.size() - 1;

            nextViewpoint = l.get(idx);
        }
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    @Override
    public void shutdown() {
        browserCore.removeViewpointStatusListener(this);
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    @Override
    public void clear() {
        viewpointsByLayer.clear();
        defaultViewpoints.clear();
        boundViewpoints.clear();

        currentViewpoint = null;
        nextViewpoint = null;
    }

    /**
     * Add a listener for viewpoint status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    @Override
    public void addViewpointListener(org.xj3d.core.eventmodel.ViewpointStatusListener l) {
        externalListeners =
            ViewpointStatusListenerMulticaster.add(externalListeners, l);
    }

    /**
     * Remove a listener for viewpoint status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    @Override
    public void removeViewpointListener(org.xj3d.core.eventmodel.ViewpointStatusListener l) {
        externalListeners =
            ViewpointStatusListenerMulticaster.remove(externalListeners, l);
    }

    //----------------------------------------------------------------
    // Methods defined by ViewpointStatusListener
    //----------------------------------------------------------------

    @Override
    public void viewpointLayerAdded(int layerId) {
        if(layerId >= viewpointsByLayer.size()) {
            for(int i = viewpointsByLayer.size(); i <= layerId; i++)
                viewpointsByLayer.add(null);

            defaultViewpoints.add(null);
            boundViewpoints.add(null);
        }

        viewpointsByLayer.set(layerId, new ArrayList<VRMLViewpointNodeType>());
    }

    /**
     * Notification that a Layer ID is no longer valid. Any viewpoints that
     * have been made available for that layer should now be removed from the
     * layer. The {@link #viewpointRemoved} callback will not be made for this
     * case.
     *
     * @param layerId The ID of the layer to be added
     */
    @Override
    public void viewpointLayerRemoved(int layerId) {
        viewpointsByLayer.set(layerId, null);
        defaultViewpoints.set(layerId, null);
        boundViewpoints.set(layerId, null);
    }

    /**
     * The given layer is now made the active layer. If there is a viewpoint
     * list being maintained per-layer then the UI can perform some sort of
     * highlighting to indicate this. Viewpoints in other layers are still
     * allowed to be bound by the user interface. If there was a previously
     * active layer, ignore it.
     * <p>
     * The code will guarantee that if the active layer is removed, then this
     * method will be called first to set a different valid layer, before
     * removing that layer ID.
     * <p>
     *
     * If a value of -1 is provided, that means no layers are active and that
     * we currently have a completely clear browser with no world loaded. The
     * UI should act appropriately.
     *
     * @param layerId The ID of the layer to be made current or -1
     */
    @Override
    public void viewpointLayerActive(int layerId) {
        activeLayerId = layerId;

        if(layerId != -1) {
            List<VRMLViewpointNodeType> l =
                viewpointsByLayer.get(layerId);

            currentViewpoint = boundViewpoints.get(layerId);

            if(externalListeners != null) {
                VRMLViewpointNodeType[] nodes = null;

                if((l != null) && (!l.isEmpty()))
                    nodes = (VRMLViewpointNodeType[])l.toArray();

                try {
                    externalListeners.availableViewpointsChanged(nodes);
                } catch(Throwable th) {
                    if(th instanceof Exception)
                        errorReporter.errorReport(CHANGE_ERROR_MSG +
                                                  externalListeners,
                                                  (Exception)th);
                    else {
                        System.err.println(DEFAULT_ERR_MSG + th);
                        th.printStackTrace(System.err);
                    }
                }
            }
        }
    }

    /**
     * Invoked when a viewpoint has been added
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is added to
     * @param isDefault Is the node a default
     */
    @Override
    public void viewpointAdded(VRMLViewpointNodeType node,
                               int layerId,
                               boolean isDefault) {
        if(isDefault) {
            defaultViewpoints.set(layerId, node);

            if(boundViewpoints.get(layerId) == null)
                boundViewpoints.set(layerId, node);
        }

        List<VRMLViewpointNodeType> l = viewpointsByLayer.get(layerId);
        if (!l.contains(node)){
            l.add(node);
        }

        if((layerId == activeLayerId) && (externalListeners != null)) {
            try {
                externalListeners.viewpointAdded(node);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(ADD_ERROR_MSG + externalListeners,
                                              (Exception)th);
                else {
                    System.err.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Invoked when a viewpoint has been removed
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is removed from
     */
    @Override
    public void viewpointRemoved(VRMLViewpointNodeType node, int layerId) {
        List<VRMLViewpointNodeType> l = viewpointsByLayer.get(layerId);
        l.remove(node);

        if(defaultViewpoints.get(layerId) == node)
            defaultViewpoints.set(layerId, null);

        if(boundViewpoints.get(layerId) == node)
            boundViewpoints.set(layerId, defaultViewpoints.get(layerId));

        if((layerId == activeLayerId) && (externalListeners != null)) {
            try {
                externalListeners.viewpointRemoved(node);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(REMOVE_ERROR_MSG +
                                              externalListeners,
                                              (Exception)th);
                else {
                    System.err.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Invoked when a viewpoint has been bound.
     *
     * @param node The viewpoint
     * @param layerId The ID of the layer the viewpoint is bound on
     */
    @Override
    public void viewpointBound(VRMLViewpointNodeType node, int layerId) {
        boundViewpoints.set(layerId, node);

        if(layerId == activeLayerId) {
            currentViewpoint = node;

            if(externalListeners != null) {
                try {
                    externalListeners.selectedViewpointChanged(node);
                } catch(Throwable th) {
                    if(th instanceof Exception)
                        errorReporter.errorReport(SELECT_ERROR_MSG +
                                                  externalListeners,
                                                  (Exception)th);
                    else {
                        System.err.println(DEFAULT_ERR_MSG + th);
                        th.printStackTrace(System.err);
                    }
                }
            }
        }
    }

    @Override
    public List<VRMLViewpointNodeType> getActiveViewpoints() {
        return viewpointsByLayer.get(activeLayerId);
    }

    //----------------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------------
}
