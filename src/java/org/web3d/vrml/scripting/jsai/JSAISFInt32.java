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
import vrml.field.ConstSFInt32;
import vrml.field.SFInt32;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the SFInt32 field when extracted from part
 * of a node.
 * <p>
 *
 * The node assumes that the index and node have been checked before use by
 * this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class JSAISFInt32 extends SFInt32
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
    JSAISFInt32(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Get the value of the field. Overrides the basic implementation to
     * make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    @Override
    public int getValue() {
        updateLocalData();

        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param val The new value to use
     */
    @Override
    public void setValue(int val) {
        data = val;
        try {
            node.setValue(fieldIndex, val);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    @Override
    public void setValue(SFInt32 val) {
        data = val.getValue();
        try {
            node.setValue(fieldIndex, data);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    @Override
    public void setValue(ConstSFInt32 val) {
        data = val.getValue();
        try {
            node.setValue(fieldIndex, data);
        } catch(FieldException ife) {
        }
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    @Override
    public Object clone() {
        return new JSAISFInt32(node, fieldIndex);
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

            data = (fd == null) ? 0 : fd.intValue;
            valueChanged = false;
        } catch(FieldException ife) {
        }
    }

}
