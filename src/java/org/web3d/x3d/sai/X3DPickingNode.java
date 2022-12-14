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

package org.web3d.x3d.sai;

// Standard library imports
// None

// Local imports
// None
/**
 * Defines the requirements of an X3DPickingNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface X3DPickingNode extends X3DSensorNode {

    /**
     * Return the pickingGeometry X3DNode value.
     *
     * @return The pickingGeometry X3DNode value.
     */
    X3DNode getPickingGeometry();

    /**
     * Set the pickingGeometry field.
     *
     * @param val The X3DNode to set.
     */
    void setPickingGeometry(X3DNode val);

    /**
     * Return the number of MFNode items in the pickTarget field.
     *
     * @return the number of MFNode items in the pickTarget field.
     */
    int getNumPickTarget();

    /**
     * Return the pickTarget value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getPickTarget(X3DNode[] val);

    /**
     * Set the pickTarget field.
     *
     * @param val The X3DNode[] to set.
     */
    void setPickTarget(X3DNode[] val);

    /**
     * Return the intersectionType String value.
     *
     * @return The intersectionType String value.
     */
    String getIntersectionType();

    /**
     * Set the intersectionType field.
     *
     * @param val The String to set.
     */
    void setIntersectionType(String val);

    /**
     * Return the number of MFNode items in the pickedGeometry field.
     *
     * @return the number of MFNode items in the pickedGeometry field.
     */
    int getNumPickedGeometry();

    /**
     * Return the pickedGeometry value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getPickedGeometry(X3DNode[] val);

    /**
     * Return the sortOrder String value.
     *
     * @return The sortOrder String value.
     */
    String getSortOrder();

    /**
     * Set the sortOrder field.
     *
     * @param val The String to set.
     */
    void setSortOrder(String val);

    /**
     * Return the number of MFString items in the objectType field.
     *
     * @return the number of MFString items in the objectType field.
     */
    int getNumObjectType();

    /**
     * Return the objectType value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    void getObjectType(String[] val);

    /**
     * Set the objectType field.
     *
     * @param val The String[] to set.
     */
    void setObjectType(String[] val);
}
