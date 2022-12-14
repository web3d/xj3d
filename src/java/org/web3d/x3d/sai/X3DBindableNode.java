/**
 * ***************************************************************************
 * Web3d.org Copyright (c) 2007 Java Source
 *
 * This source is licensed under the GNU LGPL v2.1 Please read
 * http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any purpose.
 * Use it at your own risk. If there's a problem you get to fix it.
 *
 ***************************************************************************
 */
package org.web3d.x3d.sai;

/**
 * Defines the requirements of an X3DBindableNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public interface X3DBindableNode extends X3DChildNode {

    /**
     * Set the bind field.
     *
     * @param val The boolean to set.
     */
    void setBind(boolean val);

    /**
     * Return the bindTime double value.
     *
     * @return The bindTime double value.
     */
    double getBindTime();

    /**
     * Return the isBound boolean value.
     *
     * @return The isBound boolean value.
     */
    boolean getIsBound();
}
