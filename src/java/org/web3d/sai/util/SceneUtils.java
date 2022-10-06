/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.sai.util;

// External imports
import java.util.Vector;

// Local imports
import org.web3d.x3d.sai.ComponentInfo;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.ProfileInfo;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DGroupingNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;
import org.web3d.x3d.sai.X3DShapeNode;
import org.web3d.x3d.sai.X3DRoute;

/**
 * Utility methods for working with X3D Scenes
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class SceneUtils {

    /**
     * Return whether the argument scene meets the version, profile and
     * component requirements specified.
     *
     * @param scene The scene instance to validate
     * @param version_major The major version number
     * @param version_minor The minor version number
     * @param profileInfo The profile identifier
     * @param componentInfo The array of component identifiers
     * @return true if the scene instance meets the requirements, false otherwise.
     */
    public static boolean validateSceneCompatibility(
        X3DScene scene,
        int version_major,
        int version_minor,
        ProfileInfo profileInfo,
        ComponentInfo[] componentInfo ) {

        // validate the version
        String scene_version = scene.getSpecificationVersion();
        int idx = scene_version.indexOf(".");
        if (idx != -1) {
            try {
                int scene_version_major =
                    Integer.parseInt(scene_version.substring(0, idx));
                int scene_version_minor =
                    Integer.parseInt(scene_version.substring(idx+1));
                if (!((scene_version_major > version_major)||
                    ((scene_version_major == version_major)&&
                    (scene_version_minor >= version_minor)))) {
                    // version too low
                    return(false);
                }
            } catch ( NumberFormatException e ) {
                // can't parse out the version components
                return(false);
            }
        } else {
            // something wrong with the version string,
            // no decimal point separator
            return(false);
        }
        // validate the profile
        ProfileInfo scene_profileInfo = scene.getProfile();
        if (!scene_profileInfo.getName().equals(profileInfo.getName())) {
            // not the same profile
            return(false);
        }
        // validate the components
        if ((componentInfo != null)&&(componentInfo.length > 0)) {
            ComponentInfo[] scene_componentInfo = scene.getComponents();
            if (scene_componentInfo != null) {
                for (ComponentInfo componentInfo1 : componentInfo) {
                    boolean found = false;
                    String name = componentInfo1.getName();
                    int level = componentInfo1.getLevel();
                    for (ComponentInfo scene_componentInfo1 : scene_componentInfo) {
                        if (scene_componentInfo1.getName().equals(name) && (scene_componentInfo1.getLevel() >= level)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // specific component not found
                        return(false);
                    }
                }
            } else {
                // the scene contains no additional components,
                // while the request does
                return(false);
            }
        }
        return(true);
    }

    /**
     * Return all the valid pick targets from the argument X3DScene.
     *
     * @param scene The X3DScene to search for pick targets.
     * @return The array of pick target Nodes from the scene.
     */
    public static X3DNode[] getPickTargets(X3DScene scene) {

        Vector<X3DNode> targetList = new Vector<>();
        X3DNode[] node = scene.getRootNodes();
        getPickTargets(node, targetList);
        return targetList.toArray(new X3DNode[targetList.size()]);
    }

    /**
     * Recursively search through the argument X3DNode array for valid pick targets
     * and place the reference into the argument list.
     *
     * @param node The X3DNode array to search for pick targets.
     * @param targetList The list to populate with the found pick targets.
     */
    public static void getPickTargets(X3DNode[] node, Vector<X3DNode> targetList) {

        for (X3DNode n : node) {
            if ((n instanceof X3DGroupingNode)||(n instanceof X3DShapeNode)) {
                //if ( ( n instanceof X3DShapeNode ) ) {
                targetList.add(n);
            } else {
                X3DFieldDefinition[] field_def = n.getFieldDefinitions();
                for (X3DFieldDefinition field_def1 : field_def) {
                    int field_type = field_def1.getFieldType();
                    int access_type = field_def1.getAccessType();
                    if ((access_type == X3DFieldTypes.INPUT_OUTPUT)||
                            (access_type == X3DFieldTypes.INITIALIZE_ONLY)) {
                        if (field_type == X3DFieldTypes.MFNODE) {
                            MFNode parent = (MFNode) n.getField(field_def1.getName());
                            X3DNode[] children = new X3DNode[parent.getSize()];
                            parent.getValue(children);
                            getPickTargets(children, targetList);
                        } else if (field_type == X3DFieldTypes.SFNODE) {
                            SFNode parent = (SFNode) n.getField(field_def1.getName());
                            X3DNode[] child = new X3DNode[1];
                            child[0] = parent.getValue();
                            if (child[0] != null) {
                                getPickTargets(child, targetList);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Copy the contents of one scene into another.  This will remove the contents
     * from the old scene.
     *
     * @param src The source scene
     * @param dest The destination scene
     */
    public static void copyScene(X3DScene src, X3DScene dest) {
        X3DNode[] relo_node = src.getRootNodes();
        int size;

        size = relo_node.length;
        for (int i = 0; i < size; i++) {
            X3DNode node = relo_node[i];
            src.removeRootNode(node);
            dest.addRootNode(node);
        }

        X3DRoute[] routes = src.getRoutes();
        size = routes.length;

        for(int i=0; i < size; i++) {
            X3DRoute route = routes[i];
            src.removeRoute(route);
            dest.addRoute(route.getSourceNode(), route.getSourceField(),
               route.getDestinationNode(), route.getDestinationField());
        }
    }
}
