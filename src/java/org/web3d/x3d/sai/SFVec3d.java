/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;


/**
 * Representation of a SFVec3d field.
 *
 * @version 1.0 30 April 1998
 */
public interface SFVec3d extends X3DField {

    /**
     * Write the vector value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where <br>
     *    vec[0] = X <br>
     *    vec[1] = Y <br>
     *    vec[2] = Z
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    void getValue(double[] vec);

    /**
     * Set the vector value in the given eventIn.
     * <p>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first 3 values will be used and
     * the rest ignored.
     * <p>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param value The array of vector components where <br>
     *    value[0] = X <br>
     *    value[1] = Y <br>
     *    value[2] = Z
     *
     * @exception ArrayIndexOutOfBoundsException The value did not contain at least three
     *    values for the vector
     */
    void setValue(double[] value);
}
