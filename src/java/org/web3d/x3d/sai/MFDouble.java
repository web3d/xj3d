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
 * Representation of a MFDouble field.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface MFDouble extends MField {

   /**
     * Places a new value at the end of the existing value, increasing the field
     * length accordingly.
     *
     * @param value The value to append
     */
    void append(double value);

    /**
     * Removes all values in the field and changes the field size to zero.
     */
    void clear();

    /**
     * Write the value of the array of the doubles to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too
     * small
     */
    void getValue(double[] values);

    /**
     * Get the value of an individual item in the eventOut's value.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value to at that position
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the
     * current data array bounds.
     */
    double get1Value(int index)
            throws ArrayIndexOutOfBoundsException;

    /**
     * Inserts a value into an existing index of the field. Current field values
     * from the index to the end of the field are shifted down and the field
     * length is increased by one to accommodate the new element.
     *
     * If the index is out of the bounds of the current field an
     * ArrayIndexOutofBoundsException will be generated.
     *
     * @param index The position at which to insert
     * @param value The new element to insert
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the
     * current field size.
     */
    void insertValue(int index, double value)
            throws ArrayIndexOutOfBoundsException;

    /**
     * Removes one value from the field. Values at indices above the removed
     * element will be shifted down by one and the size of the field will be
     * reduced by one.
     *
     * @param index The position of the value to remove.
     * @exception ArrayIndexOutOfBoundsException The index was outside the
     * current field size.
     */
    void removeValue(int index)
            throws ArrayIndexOutOfBoundsException;

    /**
     * Set the value of the eventIn to the new array of double values. This
     * array is copied internally so that the parameter array can be reused
     * without effecting the valid values of the eventIn.
     *
     * @param size The number of items to copy from the array
     * @param value The array of values to be used.
     */
    void setValue(int size, double[] value);

    /**
     * Set the value of an individual item in the eventIn's value. This results
     * in a new event being generated that includes all of the array items with
     * the single element set.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the double value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at
     * least three values for the colour component
     */
    void set1Value(int index, double value)
            throws ArrayIndexOutOfBoundsException;
}
