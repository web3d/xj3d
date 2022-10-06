/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// External imports

import org.web3d.vrml.lang.VRMLNode;

// None

// Local imports
// None

/**
 * A sensor that handles pick intersection tests.
 * <p>
 *
 * The picking sensor capabilities is an Xj3D extension specification. You
 * can find more details about it at
 * <a href="http://www.xj3d.org/extensions/picking.html">
 * http://www.xj3d.org/extensions/picking.html</a>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface VRMLPickingSensorNodeType extends VRMLSensorNodeType {

    /** The picking type is not defined because there isn't a pickGeometry given */
    int UNDEFINED_PICK = 0;

    /** The picking type is point */
    int POINT_PICK = 1;

    /** The picking type is line */
    int LINE_PICK = 2;

    /** The picking type is sphere */
    int SPHERE_PICK = 3;

    /** The picking type is box */
    int BOX_PICK = 4;

    /** The picking type is cone */
    int CONE_PICK = 5;

    /** The picking type is cylinder */
    int CYLINDER_PICK = 6;

    /** The picking type is polytope/volume */
    int VOLUME_PICK = 7;

    /** Sort order is CLOSEST */
    int SORT_CLOSEST = 1;

    /** Sort order is ALL */
    int SORT_ALL = 2;

    /** Sort order is ALL_SORTED */
    int SORT_ALL_SORTED = 3;

    /** Sort order is ANY */
    int SORT_ANY = 4;

    /** Intersection test type is BOUNDS */
    int INTERSECT_BOUNDS = 1;

    /** Intersection test type is GEOMETRY */
    int INTERSECT_GEOMETRY = 2;

    /**
     * Set the list of picking targets that this object corresponds to.
     * These can be an array of strings.
     *
     * @param types The list of object type strings to use
     * @param numValid The number of valid values to read from the array
     */
    void setObjectType(String[] types, int numValid);

    /**
     * Get the current number of valid object type strings.
     *
     * @return a number &gt;= 0
     */
    int numObjectType();

    /**
     * Fetch the number of object type values in use currently.
     *
     * @param val An array to copy the values to
     */
    void getObjectType(String[] val);

    /**
     * Get the picking type that this class represents. A shortcut way of
     * quickly determining the picking strategy to be used by the internal
     * implementation to avoid unnecessary calculations.
     *
     * @return One of the *_PICK constants
     */
    int getPickingType();

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the SORT_* constants
     */
    int getSortOrder();

    /**
     * Get the intersection type requested for this node
     *
     * @return one of the INTERSECT_* constants
     */
    int getIntersectionType();


    /**
     * Set the geometry used to perform the picking.
     *
     * @param geom VRMLGeometryNodeType
     */
    void setPickingGeometry(VRMLNodeType geom);

    /**
     * Fetch the real node that is being used to pick the geometry. This
     * returns the real node that may be buried under one or more proto
     * instances as part of the geometry picking scheme. If the picker is an
     * externproto that hasn't resolved, obviously this will return null.
     *
     * @return The valid geometry node or null if not set
     */
    VRMLNodeType getPickingGeometry();

    /**
     * Get the list of nodes that are used for the target geometry. This can
     * be a internal listing of children. Any node valid entries in the can be
     * set to null.
     * @return an array of picking targets
     */
    VRMLNodeType[] getPickingTargets();

    /**
     * Notification that this sensor has just been clicked on to start the
     * pick action.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    void notifyPickStart(int numPicks,
                                VRMLNode[] nodes,
                                float[] points,
                                float[] normals,
                                float[] texCoords);

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param numPicks The number of items picked in the array
     * @param nodes The geometry that was picked
     * @param points Optional array of points that are the intersection points
     * @param normals Optional array of normals that are the intersection points
     * @param texCoords Optional array of texture coordinates that are the intersection points
     */
    void notifyPickChange(int numPicks,
                                 VRMLNode[] nodes,
                                 float[] points,
                                 float[] normals,
                                 float[] texCoords);

    /**
     * Notification that this sensor has finished a picking action.
     */
    void notifyPickEnd();
}
