package org.xj3d.impl.core.eventmodel;

import java.util.ArrayList;
import java.util.List;

import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.CADKernelType;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.xj3d.core.eventmodel.NodeManager;

public class DefaultBrepManager implements NodeManager {

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.BREPNodeType,
        TypeConstants.ParametricGeometryNodeType
    };

    private CADKernelType cadRenderer=null;

    /**
     * Stack used to notify the manager that a Face tesselation has been successfully computed and is ready to be included in the scenegraph
     */
    List<VRMLNodeType> readyFace=new ArrayList<>();

    static DefaultBrepManager instance=null;


    /**
     * Singleton pattern to expose the Manager to the Renderer
     * @return DefaultBrepManager
     */
    static public DefaultBrepManager getInstance()
    {
    	return instance;

    }

    public void setCadRenderer(CADKernelType renderer)
    {
    	this.cadRenderer=renderer;
    }

    /**
     * Notifies the manager that a face has been successfully tesselated and is ready to be rendered
     * (Note : the tesselation is no carried here, the flow is the following :
     * 1-Face tells CadRendered that it's been parsed and need a tesselation
     * 2-CadRenderer starts a thread to compute face tesselation
     * 3-CadRenderer tells BrepManager that a the face tesselation has been computed
     * 4-Manager waits for a safe time to update sceneGraph, then tells the Face it needs to update its geometry thru aviatrix updateBounds callback.
     * 5-Face queries the CadRenderer for the tesselation the "ready to render" tesselation.
     * @param n
     */
    synchronized public void addReadyFace(VRMLNodeType n)
    {
    	readyFace.add(n);
    }

	public DefaultBrepManager() {
		instance=this;
		errorReporter = DefaultErrorReporter.getDefaultReporter();
		errorReporter.messageReport("Instanciate BREP Manager");
	}

    @Override
	public void addManagedNode(VRMLNodeType node) {
	}



    /**
     * Run the pre-event modeling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    @Override
    public void executePreEventModel(long time) {

    }


    /**
     * Run the post-event modeling for this frame now. This is a blocking call
     * and does not return until the event model is complete for this frame.
     * The time should be system clock time, not VRML time.
     *
     * @param time The timestamp of this frame to evaluate
     */
    @Override
    public void executePostEventModel(long time) {

    	//goes over list of faces notified to be rendered
    	List<VRMLNodeType> l=(List<VRMLNodeType>) ((ArrayList<VRMLNodeType>)readyFace).clone();
    	for(Object d:l)//d is a face
    	{
    		NodeUpdateListener f = (NodeUpdateListener) d;
    		cadRenderer.updateGeometry(f);
    		readyFace.remove(f);
    	}
	}


    /**
	 * Get the list of component names that this manager would normally manage.
	 * The component definition is assumed to be the same across all versions
	 * of the specifications that the browser supports. The level of the
	 * component is assumed to be the lowest level supported (ie if the given
	 * level fails, then levels above this cannot be supported, but those below
	 * can still be).
	 * <p>
	 * Mostly this is used for when initialization fails and we wish to disable
	 * support for loading of nodes in that component.
	 *
	 * @return The collection of components that this manager supports
	 */
    @Override
    public ComponentInfo[] getSupportedComponents() {
        return new ComponentInfo[] {
            new ComponentInfo("xj3d_BREP", 1)
        };
    }

    /**
     * Initialise the node manager now with any per-manager setup that is
     * needed. If this returns false, then the node manager is assumed to have
     * failed some part of the setup and will be removed from the system
     *
     * @return true if initialization was successful
     */
    @Override
    public boolean initialize() {
        return true;
    }

    @Override
	public void removeManagedNode(VRMLNodeType node) {
		// TODO Auto-generated method stub

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
        if(reporter == null) {
			errorReporter = DefaultErrorReporter.getDefaultReporter();
		}
    }


    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    @Override
    public void shutdown() {
    }

    /**
     * Set the VRMLClock instance in use by this manager. Ignored for this
     * manager.
     *
     * @param clk A reference to the clock to use
     */
    @Override
    public void setVRMLClock(VRMLClock clk) {
    }

    /**
     * Reset the local time zero for the manager. This is called when a new
     * root world has been loaded and any manager that needs to rely on delta
     * time from the start of the world loading can reset it's local reference
     * from the passed in {@link VRMLClock} instance.
     */
    @Override
    public void resetTimeZero() {
    }

    /**
     * Get the list of node type IDs that this manager wants to handle. These
     * should be the constants from {@link org.web3d.vrml.lang.TypeConstants}.
     *
     * @return A list of managed node identifiers
     */
    @Override
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    /**
     * Ask whether this manager needs to be run before the event model
     * has been evaluated for this frame.
     *
     * @return true if this is to be run pre-event model, false otherwise
     */
    @Override
    public boolean evaluatePreEventModel() {
        return true;
    }

    /**
     * Ask whether this manager should run after the event model has been
     * evaluated for this frame.
     *
     * @return true if this is post event model, false otherwise
     */
    @Override
    public boolean evaluatePostEventModel() {
        return true;
    }



    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    @Override
    public void clear() {
    }

}
