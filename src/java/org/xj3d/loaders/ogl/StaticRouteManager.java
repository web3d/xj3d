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

package org.xj3d.loaders.ogl;

// External imports
// None

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.xj3d.core.eventmodel.RouteManager;
import org.xj3d.core.eventmodel.RouterFactory;

/**
 * An empty implementation of the route manager that does nothing.
 * <p>
 *
 * Used be the static loader where geometry-only loading is being performed. It
 * does nothing with any of the requests.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class StaticRouteManager implements RouteManager {

    StaticRouteManager() {
    }

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
    }

    /**
     * Set the factory needed to create new router instances for a new
     * execution space. If the reference is null, then this clears the current
     * factory so that we don't accept sub-space handling.
     *
     * @param fac The factory instance to use
     */
    @Override
    public void setRouterFactory(RouterFactory fac) {
    }

    /**
     * Process all of the available routes until there are no more to process.
     * This method will not return until all routes have been processed.
     *
     * @param timestamp The timestamp for when these routes should be executed
     * @return false No event outs needed processing this call
     */
    @Override
    public boolean processRoutes(double timestamp) {
        return false;
    }

    /**
     * Add a route to the system. If the route exists in the system, this
     * silently ignores the request. If the space reference is null then
     *
     * @param space The execution space for the route
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    @Override
    public void addRoute(VRMLExecutionSpace space,
                         VRMLNodeType srcNode,
                         int srcIndex,
                         VRMLNodeType destNode,
                         int destIndex) {
    }

    /**
     * Add a route object to the system. If the route exists in the system,
     * this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to add
     */
    @Override
    public void addRoute(VRMLExecutionSpace space, ROUTE route) {
    }

    /**
     * Remove a route from the system. If the route does not exist in the
     * system, this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    @Override
    public void removeRoute(VRMLExecutionSpace space,
                            VRMLNodeType srcNode,
                            int srcIndex,
                            VRMLNodeType destNode,
                            int destIndex) {
    }

    /**
     * Remove a route object from the system. If the route does not exist in
     * the system, this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to remove
     */
    @Override
    public void removeRoute(VRMLExecutionSpace space, ROUTE route) {
    }

    /**
     * Add an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this space has
     * already been added, the request will be ignored.
     *
     * @param space The execution space to add
     */
    @Override
    public void addSpace(VRMLExecutionSpace space) {
    }

    /**
     * Remove an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this request has
     * not been added it will be ignored.
     *
     * @param space The execution space to add
     */
    @Override
    public void removeSpace(VRMLExecutionSpace space) {
    }

    /**
     * Notification that the route manager should now propagate all added and
     * removed spaces from this list into the core evaluatable system. It should
     * call the normal addRoute method of the space and not directly propagate
     * the route modifications immediately. They should wait for the separate
     * updateRoutes() call.
     */
    @Override
    public void updateSpaces() {
    }

    /**
     * Notification that the route manager should now propagate all added and
     * removed routes from this list into the core evaluatable space.
     */
    @Override
    public void updateRoutes() {
    }

    /**
     * Do all the end of cascade processing. Primarily consists of calling the
     * eventsProcessed() method on all scripts that received events in the last
     * cascade.
     */
    public void endCascade() {
    }

    /**
     * Clear all the routes currently being managed here. The space this router
     * represents is being deleted.
     */
    @Override
    public void clear() {
    }
}
