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
import vrml.field.ConstMFVec2f;
import vrml.field.SFVec2f;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * VRML type class containing multiple rotation fields
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
class JSAIConstMFVec2f extends ConstMFVec2f
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
    JSAIConstMFVec2f(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Copy the values from this field into the given array.
     *
     * @param vec2s The target array to copy values into
     */
    @Override
    public void getValue(float[][] vec2s) {
        updateLocalData();
        super.getValue(vec2s);
    }

    /**
     * Copy the value of this field into the given flat array.
     *
     * @param vec2s The target array to copy values into
     */
    @Override
    public void getValue(float[] vec2s) {
        updateLocalData();
        super.getValue(vec2s);
    }

    /**
     * Copy the value of the vector at the given index into the user array.
     *
     * @param index The index in the array of values to read
     * @param vecs The array to copy the vector value to
     */
    @Override
    public void get1Value(int index, float[] vec2s) {
        updateLocalData();
        super.get1Value(index, vec2s);
    }

    /**
     * Copy the vector value at the given index into the supplied field.
     *
     * @param index The index in the array of values to read
     * @param vec The field to copy the vector value to
     */
    @Override
    public void get1Value(int index, SFVec2f vec) {
        updateLocalData();
        super.get1Value(index, vec);
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
        return new JSAIConstMFVec2f(node, fieldIndex);
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
                setValue(fd.numElements * 2, fd.floatArrayValues);

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of this field given limited array of vecs.
     * x1, y1, z1, x2, y2, z2, ....
     *
     * @param size The number of vecs is size / 2.
     * @param vecs Color triplicates flattened.
     */
    private void setValue(int size, float[] vec2s) {

        if(size > data.length)
            data = new float[size];

        numElements = size / 2;

        System.arraycopy(vec2s, 0, data, 0, size);
    }

}
