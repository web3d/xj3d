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

package org.web3d.x3d.sai.networking;

import org.web3d.x3d.sai.X3DBoundedObject;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DUrlObject;

/**
 * Defines the requirements of an X3D Inline node
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface Inline extends X3DChildNode, X3DBoundedObject, X3DUrlObject {

    /**
     * Return the load boolean value.
     *
     * @return The load boolean value.
     */
    boolean getLoad();

    /**
     * Set the load field.
     *
     * @param val The boolean to set.
     */
    void setLoad(boolean val);
}
