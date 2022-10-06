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
 * Defines the requirements of an X3DSoundSourceNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public interface X3DSoundSourceNode extends X3DTimeDependentNode {

    /**
     * Return the description String value.
     *
     * @return The description String value.
     */
    String getDescription();

    /**
     * Set the description field.
     *
     * @param val The String to set.
     */
    void setDescription(String val);

    /**
     * Return the pitch float value.
     *
     * @return The pitch float value.
     */
    float getPitch();

    /**
     * Set the pitch field.
     *
     * @param val The float to set.
     */
    void setPitch(float val);
}
