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

package vrml.field;

// Standard imports
// none

// Application specific imports
import vrml.ConstMField;

import org.web3d.util.ArrayUtils;

/**
 * Constant VRML JSAI type class containing multiple 3 component vector fields.
 * <p>
 *
 * Internally the class keeps data as a flat array, so that is the most
 * efficient way of interacting with this class.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstMFVec3f extends ConstMField {

    /** The data of the underlying field */
    protected float[] data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstMFVec3f() {
        data = new float[0];
    }

    /**
     * Construct a new field based on all the given 2D array of values.
     *
     * @param vecs The vec values to use
     */
    public ConstMFVec3f(float vecs[][]) {

        numElements = vecs.length;
        data = new float[vecs.length * 3];
        ArrayUtils.flatten3(vecs, vecs.length, data);

    }

    /**
     * Construct a new field based on all the given data values
     *
     * @param vecs The vec values to use
     */
    public ConstMFVec3f(float vecs[]) {
        numElements = vecs.length /3;

        // use int rounding to get an exact multiple of 3
        int size = numElements * 3;
        data = new float[size];
        System.arraycopy(vecs, 0, data, 0, size);
    }

    /**
     * Construct a new field based on subsection the given data values
     *
     * @param size The number of items to use. numElements = size / 3
     * @param vecs The vec values to use
     */
    public ConstMFVec3f(int size, float vecs[]) {
        numElements = size /3;

        // use int rounding to get an exact multiple of 3
        int real_size = numElements * 3;
        data = new float[real_size];
        System.arraycopy(vecs, 0, data, 0, real_size);
    }

    /**
     * Get the vec values used in this field and copy them into the user
     * provided array.
     *
     * @param vecs The array to copy values to
     */
    public void getValue(float[][] vecs) {
        ArrayUtils.raise3(data, numElements, vecs);
    }

    /**
     * Copy the vec values used in this field into the user provided array.
     *
     * @param vecs The array to copy values to
     */
    public void getValue(float[] vecs) {
        System.arraycopy(data, 0, vecs, 0, data.length);
    }

    /**
     * Get the vec value at the given position and copy it into the user
     * provided array.
     *
     * @param index The position of the vec to copy
     * @param vec The array to copy data to
     */
    public void get1Value(int index, float[] vec) {
        int offset = index * 3;
        vec[0] = data[offset++];
        vec[1] = data[offset++];
        vec[2] = data[offset];
    }

    /**
     * Get the vec value at the given position and copy it into the user
     * provided field.
     *
     * @param index The position of the vec to copy
     * @param vec The field to copy data to
     */
    public void get1Value(int index, SFVec3f vec) {
        int offset = index * 3;
        vec.setValue(data[offset], data[offset + 1], data[offset + 2]);
    }

    /**
     * Create a string representation of the field values.
     *
     * @return A string representing the values.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("[\n  ");

        int count = 0;

        for(int i = 0; i < numElements; i++) {
            buf.append(data[count++]);
            buf.append(' ');
            buf.append(data[count++]);
            buf.append(' ');
            buf.append(data[count++]);
            buf.append(' ');

            if(i % 4 == 0)
                buf.append("\n  ");
        }

        buf.append("]");

        return buf.toString();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    @Override
    public Object clone() {
        return new ConstMFVec3f(data);
    }
}
