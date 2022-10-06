/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports
import java.util.ArrayList;

// Local imports

/**
 * Wrapper for an X3D Scene.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Scene extends BaseEncodable {

    /** The root nodes */
    public ArrayList<Encodable> nodeList;

    /** The routes */
    private ArrayList<Route> routeList;

    /**
     * Constructor
     * @param name
     */
    public Scene(String name) {
        super("Scene");
        nodeList = new ArrayList<>();
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        super.clear();
        nodeList.clear();
        if (routeList != null) {
            routeList.clear();
        }
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {
        for (Encodable e : nodeList) {
            e.encode();
        }
        if (routeList != null) {
            for (Route r : routeList) {
                r.encode();
            }
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String name, Object value) {
        if (value instanceof Encodable) {
            nodeList.add((Encodable)value);
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String name, Object value, int len) {
        if (value instanceof Encodable[]) {
            Encodable[] e = (Encodable[])value;
            for (Encodable e1 : e) {
                nodeList.add(e1);
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Return the root nodes of the Scene
     *
     * @return the root nodes of the Scene
     */
    public Encodable[] getRootNodes() {
        return(nodeList.toArray(new Encodable[nodeList.size()]));
    }

    /**
     * Remove a root node.
     *
     * @param node The root node to remove
     */
    public void removeRootNode(Encodable node) {
        nodeList.remove(node);
    }

    /**
     * Add a root node.
     *
     * @param node The root node to remove
     */
    public void addRootNode(Encodable node) {
        nodeList.add(node);
    }

    /**
     * Add a route
     *
     * @param route The route to add
     */
    public void addRoute(Route route) {
        if (routeList == null) {
            routeList = new ArrayList<>();
        }
        routeList.add(route);
    }
}
