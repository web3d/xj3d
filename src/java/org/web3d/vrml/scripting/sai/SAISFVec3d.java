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
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Representation of a SFVec3d field.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class SAISFVec3d extends BaseField implements SFVec3d {

    /** The local field value */
    private double[] localValue;

    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     */
    SAISFVec3d(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);

        localValue = new double[3];
    }

    /**
     * Write the vector value to the given eventOut
     *
     * @param vec The array of vector values to be filled in where <br>
     *    vec[0] = X <br>
     *    vec[1] = Y <br>
     *    vec[2] = Z
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(double[] vec) {
        checkAccess(false);

        vec[0] = localValue[0];
        vec[1] = localValue[1];
        vec[2] = localValue[2];
    }

    /**
     * Set the vector value in the given eventIn.
     *  <p>
     * The value array must contain at least three elements. If the array
     * contains more than 3 values only the first 3 values will be used and
     * the rest ignored.
     *  <p>
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
    @Override
    public void setValue(double[] value) {
        checkAccess(true);

        localValue[0] = value[0];
        localValue[1] = value[1];
        localValue[2] = value[2];
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
        node.setValue(fieldIndex, localValue, 3);
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
        localValue[0] = data.doubleArrayValues[0];
        localValue[1] = data.doubleArrayValues[1];
        localValue[2] = data.doubleArrayValues[2];
        dataChanged = false;
    }
}
