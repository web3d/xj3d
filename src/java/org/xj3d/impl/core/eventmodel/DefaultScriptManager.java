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

package org.xj3d.impl.core.eventmodel;

// External imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;

// Local imports
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.util.NodeArray;

import org.xj3d.core.eventmodel.ScriptManager;
import org.xj3d.core.loading.ScriptLoader;
import org.xj3d.core.loading.ScriptLoadStatusListener;

/**
 * Default implementation of the ScriptManager interface that implements VRML97
 * and X3D semantics.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class DefaultScriptManager
    implements ScriptManager, VRMLUrlListener, ScriptLoadStatusListener {

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Loader for handling scripts */
    private ScriptLoader loader;

    /** The loaded scripts that have not been initialised yet. */
    private NodeArray loadedList;

    /** All scripts that have been processed since the last call. */
    private NodeArray processedList;

    /** Scripts ready to have shutdown called due to URL changes */
    private NodeArray shutdownList;

    /** Scripts ready to have shutdown called because node deleted */
    private NodeArray deletedList;

    /** All the nodes currently in place */
    private HashSet<VRMLNode> allScripts;

    /** All scripts that are now active and running */
    private NodeArray activeScripts;

    /** Mutex used for processing the loadedList/initialise calls */
    private final Object loadedListMutex;

    /** Mutex used the processed nodes list */
    private final Object processedListMutex;

    /**
     * Construct a new instance of the script manager
     */
    public DefaultScriptManager() {

        loadedListMutex = new Object();
        processedListMutex = new Object();

        loadedList = new NodeArray();
        activeScripts = new NodeArray();
        shutdownList = new NodeArray();
        deletedList = new NodeArray();

        processedList = new NodeArray();

        allScripts = new HashSet<>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-------------------------------------------------------------
    // Methods required by VRMLUrlListener
    //-------------------------------------------------------------

    /**
     * Notification that the Url content for this node has changed
     *
     * @param node
     * @param index The index of the field that has changed
     */
    @Override
    public void urlChanged(VRMLNodeType node, int index) {
        // Take this node and put it onto the "must call shutdown" list
        shutdownList.add(node);
    }

    //-------------------------------------------------------------
    // Methods required by ScriptLoadStatusListener
    //-------------------------------------------------------------

    /**
     * Notification of a successful load of a script. Not used for failed
     * loads.
     *
     * @param script The script that was loaded correctly
     */
    @Override
    public void loadCompleted(VRMLScriptNodeType script) {

        // Synchronise here because we may also be processing the initialize()
        // call in a separate thread. The list itself is not multithreaded
        // protected so we do this explicitly.
        synchronized(loadedListMutex) {
            loadedList.add(script);
        }

        synchronized(processedListMutex) {
            processedList.add(script);
        }
    }

    /**
     * Notification of a failed load of a script when none of the URLs could
     * be loaded.
     *
     * @param script The script that failed to load
     */
    @Override
    public void loadFailed(VRMLScriptNodeType script) {
        synchronized(processedListMutex) {
            processedList.add(script);
        }
    }

    //-------------------------------------------------------------
    // Methods required by ScriptManager
    //-------------------------------------------------------------

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

        if(loader != null)
            loader.setErrorReporter(errorReporter);
    }

    /**
     * Set the script loader to be used by this manager. Setting a null value
     * will clear the current instance and disable all script loading.
     *
     * @param ldr The loader instance to use
     */
    @Override
    public void setScriptLoader(ScriptLoader ldr) {

        if(loader != null)
            loader.setStatusListener(null);

        loader = ldr;

        if(loader != null) {
            loader.setErrorReporter(errorReporter);
            loader.setStatusListener(this);
        }
    }

    /**
     * Get the current script loader to be used by this manager. If none is
     * currently in use, null will be returned.
     *
     * @return The current loader instance or null
     */
    @Override
    public ScriptLoader getScriptLoader() {
        return loader;
    }

    /**
     * The loader should now shutdown any scripts that have had their set_url
     * events called.
     */
    @Override
    public void shutdownActiveScripts() {
        int size = shutdownList.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)shutdownList.get(i);
            scr.shutdown();
        }

        shutdownList.clear();
    }

    /**
     * Shutdown all scripts as the system is about to shutdown all of the
     * current world and is starting again.
     */
    @Override
    public void shutdown() {
        Object[] scr_list = allScripts.toArray();

        for (Object scr_list1 : scr_list) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType) scr_list1;
            //scr.shutdownAll();
            scr.shutdown();
        }
	allScripts.clear();

        shutdownList.clear();
        activeScripts.clear();

	loadedList.clear();
        deletedList.clear();
        processedList.clear();
    }

    /**
     * Initialise any newly loaded scripts and then put them into the completed
     * basket.
     *
     * @param timestamp The VRML time that the initialization occurred at
     */
    @Override
    public void initializeScripts(double timestamp) {
        int size = loadedList.size();

        // Only process if the list is not empty. However, we also need the
        // extra mutex guard while we're processing the list because the
        // script loader may complete loading another script while we're doing
        // the init calls. This should prevent it dumping another item onto
        // the list and having us not notice it and then do a clear().
        if(size != 0) {
            synchronized(loadedListMutex) {
                for(int i = 0; i < size; i++) {
                    VRMLScriptNodeType scr =
                        (VRMLScriptNodeType)loadedList.get(i);
                    scr.initialize(timestamp);

                    activeScripts.add(scr);
                }

                loadedList.clear();
            }
        }
    }

    /**
     * Setup the scripts for the new timestamp. For X3D, this will also call
     * the prepareEvents() method/function on the script, if it has one.
     *
     * @param timestamp The current time in VRML time
     */
    @Override
    public void prepareEvents(double timestamp) {
        int size = activeScripts.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)activeScripts.get(i);
            scr.prepareEvents(timestamp);
        }
    }

    /**
     * Process any events that scripts need to send. This looks for any inputs
     * that have changed, and also any outputs and propagates the values to the
     * eventOuts of the script. If the eventOuts are routed somewhere, it is
     * the RouteManager's responsibility to look after sending those changed
     * values.
     */
    @Override
    public void processEvents() {
        int size = activeScripts.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)activeScripts.get(i);
            scr.processEvents();
        }
    }

    /**
     * Notification that all of the processing is finished and that
     * eventsProcessed() should now be called.
     */
    @Override
    public void eventsProcessed() {
        int size = activeScripts.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)activeScripts.get(i);
            scr.eventsProcessed();
        }
    }

    /**
     * Remove this list of scripts from active service. They have been deleted
     * from the scene graph and therefore the listener should no longer be
     * registered with them.
     *
     * @param list The list of scripts to remove
     */
    @Override
    public void removeScripts(NodeArray list) {
        int size = list.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)list.get(i);
            scr.removeUrlListener(this);
            allScripts.remove(scr);
            activeScripts.remove(scr);
        }
    }

    /**
     * Queue the scripts to add to the scene.
     *
     * @param list The list of script node instances to load
     */
    @Override
    public void addScripts(NodeArray list) {
        if(loader == null)
            return;

        int size = list.size();

        for(int i = 0; i < size; i++) {
            VRMLScriptNodeType scr = (VRMLScriptNodeType)list.get(i);
            scr.addUrlListener(this);
            allScripts.add(scr);

            // Add the script to the script loader
            loader.loadScript(scr);
        }
    }

    /**
     * Copy all the processed scripts to date into the given list. After this
     * is done, clear the internal collection. The calling script should be
     * empty because it will replace the contents of the given list with this
     * collection of values.
     *
     * @param list The list to copy values into
     */
    @Override
    public void getProcessedScripts(NodeArray list) {
        synchronized(processedListMutex) {
            list.set(processedList);
            processedList.clear();
        }
    }
}
