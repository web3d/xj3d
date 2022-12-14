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

//Standard library imports
// None

// Local imports
// None

/** Defines the requirements of an X3DInterpolatorNode abstract node type
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public interface X3DInterpolatorNode extends X3DChildNode {

    /**
     * Return the number of MFFloat items in the key field.
     *
     * @return the number of MFFloat items in the key field.
     */
    int getNumKey();

    /**
     * Return the key value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getKey(float[] val);

    /**
     * Set the key field.
     *
     * @param val The float[] to set.
     */
    void setKey(float[] val);

    /**
     * Set the fraction field.
     *
     * @param val The float to set.
     */
    void setFraction(float val);
}
