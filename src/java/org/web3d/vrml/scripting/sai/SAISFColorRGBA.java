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

package org.web3d.vrml.scripting.sai;

// Standard imports
// None

// Application specific imports
import org.web3d.x3d.sai.SFColorRGBA;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.util.FieldValidator;

/**
 * Implementation of a SFColorRGBA field.
 *  <p>
 * Colour values are represented as floating point numbers between [0 - 1]
 * as per the VRML IS specification Section 4.4.5 Standard units and
 * coordinate system.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class SAISFColorRGBA extends BaseField implements SFColorRGBA {

    /** The local field value */
    private float[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFColorRGBA(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);

        localValue = new float[4];
    }

    /**
     * Write the value of the colour to the given array.
     *
     * @param col The array of colour values to be filled in where <br>
     *    value[0] = Red component [0-1]  <br>
     *    value[1] = Green component [0-1]  <br>
     *    value[2] = Blue component [0-1]  <br>
     *    value[3] = Alpha component [0-1]  <br>
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(float[] col) {

        checkAccess(false);

        col[0] = localValue[0];
        col[1] = localValue[1];
        col[2] = localValue[2];
        col[3] = localValue[3];
    }

    /**
     * Set the colour value in the given eventIn.  Colour values are required
     * to be in the range [0-1].
     *  <p>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first three values will be used and
     * the rest ignored.
     *  <p>
     * If the array of values does not contain at least 3 elements an
     * ArrayIndexOutOfBoundsException will be generated. If the colour values are
     * out of range an IllegalArgumentException will be generated.
     *
     * @param value The array of colour values where <br>
     *    value[0] = Red component [0-1]  <br>
     *    value[1] = Green component [0-1]  <br>
     *    value[2] = Blue component [0-1]  <br>
     *    value[3] = Alpha component [0-1]  <br>
     *
     * @exception IllegalArgumentException A colour value(s) was out of range
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least three
     *    values for the colour component
     */
    @Override
    public void setValue(float[] value) {

        checkAccess(true);

        FieldValidator.checkColorAlphaVector("SAI.SFColorRGBA", value);

        localValue[0] = value[0];
        localValue[1] = value[1];
        localValue[2] = value[2];
        localValue[3] = value[3];
        dataChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    @Override
    void updateNode() {
        node.setValue(fieldIndex, localValue, 4);
        dataChanged = false;
    }

    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    @Override
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);
        localValue[0] = data.floatArrayValues[0];
        localValue[1] = data.floatArrayValues[1];
        localValue[2] = data.floatArrayValues[2];
        localValue[3] = data.floatArrayValues[3];
        dataChanged = false;
    }
}
