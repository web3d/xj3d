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

package org.xj3d.sai.internal.node.eventutilities;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.eventutilities.IntegerSequencer;

/** A concrete implementation of the IntegerSequencer node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIIntegerSequencer extends BaseNode implements IntegerSequencer {

    /** The previous inputOnly field */
    private SFBool previous;

    /** The next inputOnly field */
    private SFBool next;

    /** The set_fraction inputOnly field */
    private SFFloat set_fraction;

    /** The key inputOutput field */
    private MFFloat key;

    /** The keyValue inputOutput field */
    private MFInt32 keyValue;

    /** The value_changed outputOnly field */
    private SFInt32 value_changed;

    /**
     * Constructor
     * @param bnf
     */
    public SAIIntegerSequencer(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Set the previous field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setPrevious(boolean val) {
        if (previous == null) {
            previous = (SFBool) getField("previous");
        }
        previous.setValue(val);
    }

    /**
     * Set the next field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setNext(boolean val) {
        if (next == null) {
            next = (SFBool) getField("next");
        }
        next.setValue(val);
    }

    /**
     * Set the set_fraction field.
     *
     * @param val The float to set.
     */
    @Override
    public void setFraction(float val) {
        if (set_fraction == null) {
            set_fraction = (SFFloat) getField("set_fraction");
        }
        set_fraction.setValue(val);
    }

    /**
     * Return the number of MFFloat items in the key field.
     *
     * @return the number of MFFloat items in the key field.
     */
    @Override
    public int getNumKey() {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        return (key.getSize());
    }

    /**
     * Return the key value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getKey(float[] val) {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        key.getValue(val);
    }

    /**
     * Set the key field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setKey(float[] val) {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        key.setValue(val.length, val);
    }

    /**
     * Return the number of MFInt32 items in the keyValue field.
     *
     * @return the number of MFInt32 items in the keyValue field.
     */
    @Override
    public int getNumKeyValue() {
        if (keyValue == null) {
            keyValue = (MFInt32) getField("keyValue");
        }
        return (keyValue.getSize());
    }

    /**
     * Return the keyValue value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    @Override
    public void getKeyValue(int[] val) {
        if (keyValue == null) {
            keyValue = (MFInt32) getField("keyValue");
        }
        keyValue.getValue(val);
    }

    /**
     * Set the keyValue field.
     *
     * @param val The int[] to set.
     */
    @Override
    public void setKeyValue(int[] val) {
        if (keyValue == null) {
            keyValue = (MFInt32) getField("keyValue");
        }
        keyValue.setValue(val.length, val);
    }

    /**
     * Return the value_changed int value.
     *
     * @return The value_changed int value.
     */
    @Override
    public int getValue() {
        if (value_changed == null) {
            value_changed = (SFInt32) getField("value_changed");
        }
        return (value_changed.getValue());
    }
}
