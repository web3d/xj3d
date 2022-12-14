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
package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.field.SFVec3f;
import vrml.field.MFVec3f;
import vrml.field.ConstMFVec3f;
import vrml.field.ConstSFVec3f;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * VRML JSAI type class containing multiple vector3f fields
 * <p>
 *
 * Internally, the class stores the values as a single, flat array so that is
 * the most efficient method to use to avoid reallocation. All methods make
 * internal copies of the values.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.7 $
 */
class JSAIMFVec3f extends MFVec3f
    implements VRMLNodeListener {

    /** The node that this field references */
    private VRMLNodeType node;

    /** The index of the field this representation belongs to */
    private int fieldIndex;

    /**
     * Create a new field that represents the underlying instance
     *
     * @param n The node to fetch information from
     * @param index The index of the field to use
     */
    JSAIMFVec3f(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param vec3s The target array to copy values into
     */
    @Override
    public void getValue(float[][] vec3s) {
        updateLocalData();
        super.getValue(vec3s);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param vec3s The target array to copy values into
     */
    @Override
    public void getValue(float[] vec3s) {
        updateLocalData();
        super.getValue(vec3s);
    }

    /**
     * Copy the value of the vector at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param vecs The array to copy the vector value to
     */
    @Override
    public void get1Value(int index, float[] vec3s) {
        updateLocalData();
        super.get1Value(index, vec3s);
    }

    /**
     * Copy the vector value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param vec The field to copy the vector value to
     */
    @Override
    public void get1Value(int index, SFVec3f vec) {
        updateLocalData();
        super.get1Value(index, vec);
    }

    /**
     * Set the field to the new values.
     *
     * @param vecs The new vec values to use
     */
    @Override
    public void setValue(float[][] vec3s) {
        super.setValue(vec3s);
        updateCoreData();
    }

    /**
     * Set the field to the new values.
     *
     * @param vecs The new vec values to use
     */
    @Override
    public void setValue(float[] vec3s) {
        super.setValue(vec3s);
        updateCoreData();
    }

    /**
     * Set the value of this field given limited array of vecs.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of vecs is size / 3.
     * @param vecs Color triplicates flattened.
     */
    @Override
    public void setValue(int size, float[] vec3s) {
        super.setValue(size, vec3s);
        updateCoreData();
    }

    /**
     * Set the value of this field based on the values in the given field.
     *
     * @param vecs The field to copy from
     */
    @Override
    public void setValue(MFVec3f vecs) {
        super.setValue(vecs);
        updateCoreData();
    }

    /**
     * Set the value of this field to the values in the given field.
     *
     * @param vecs The field to copy from
     */
    @Override
    public void setValue(ConstMFVec3f vecs) {
        super.setValue(vecs);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    @Override
    public void set1Value(int index, ConstSFVec3f vec) {
        super.set1Value(index, vec);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the value from the given field.
     *
     * @param index The index of the element to set
     * @param vec The field to copy the data from
     */
    @Override
    public void set1Value(int index, SFVec3f vec) {
        super.set1Value(index, vec);
        updateCoreData();
    }

    /**
     * Set the element at the given index with the given vec components.
     *
     * @param index The index of the element to set
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    @Override
    public void set1Value(int index, float x, float y, float z) {
        super.set1Value(index, x, y, z);
        updateCoreData();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    @Override
    public void addValue(ConstSFVec3f vec) {
        super.addValue(vec);
        updateCoreData();
    }

    /**
     * Append the field as a new vec value to the end of this field.
     *
     * @param vec The field to append
     */
    @Override
    public void addValue(SFVec3f vec) {
        super.addValue(vec);
        updateCoreData();
    }

    /**
     * Append the components as a new vector value to the end of this field.
     *
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    @Override
    public void addValue(float x, float y, float z) {
        super.addValue(x, y, z);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    @Override
    public void insertValue(int index, ConstSFVec3f vec) {
        super.insertValue(index, vec);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the given field at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param vec The vec field to insert
     */
    @Override
    public void insertValue(int index, SFVec3f vec) {
        super.insertValue(index, vec);
        updateCoreData();
    }

    /**
     * Insert the vector represented by the components at the given position
     * in the array.
     *
     * @param index The position to insert the value
     * @param x The x component to use
     * @param y The y component to use
     * @param z The z component to use
     */
    @Override
    public void insertValue(int index, float x, float y, float z) {
        super.insertValue(index, x, y, z);
        updateCoreData();
    }

    /**
     * Clear the field of all elements
     */
    @Override
    public void clear() {
System.out.println("JSAIMFVec3f clear not implemented yet");
    }

    /**
     * Remove the element at the given position and shift any other items
     * down.
     *
     * @param index The position to delete the item at
     */
    @Override
    public void delete(int index) {
        super.delete(index);
        updateCoreData();
    }

    /**
     * Create a string representation of the field values.
     *
     * @return A string representing the values.
     */
    @Override
    public String toString() {
        updateLocalData();
        return super.toString();
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    @Override
    public Object clone() {
        return new JSAIMFVec2f(node, fieldIndex);
    }


    //----------------------------------------------------------
    // Methods required by the VRMLNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    @Override
    public void fieldChanged(int index) {
        if(index != fieldIndex)
            return;

        valueChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Fetch the field from the core and update any values internally.
     */
    private void updateLocalData() {
        if(!valueChanged)
            return;

        try {
            VRMLFieldData fd = node.getFieldValue(fieldIndex);

            if (fd == null)
                return;

            if (fd.numElements == 0) {
                numElements = 0;
                data = new float[0];
            } else
                super.setValue(fd.numElements * 3, fd.floatArrayValues);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Update the core with the local field data after a change.
     */
    private void updateCoreData() {
        try {
            node.setValue(fieldIndex, data, numElements * 3);
        } catch(FieldException fe) {
        }
    }
}
