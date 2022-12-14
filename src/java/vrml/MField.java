/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package vrml;

/**
 * Java binding for multiple value fields.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class MField extends Field {

    /** The number of elements registered in this class */
    protected int numElements;

    /**
     * Get the number of elements in the current field
     *
     * @return The number of elements
     */
    public int getSize() {
        return numElements;
    }

    /**
     * Clear all values from this field. Leaves the size of the field empty
     */
    public abstract void clear();

    /**
     * Delete the element at the given index. All elements above it are
     * shuffled down to take its place
     *
     * @param index the index of the element to delete
     */
    public abstract void delete(int index);
}
