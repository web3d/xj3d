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

package org.xj3d.sai.internal.node.interpolation;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.interpolation.ScalarInterpolator;

/**
 * A concrete implementation of the ScalarInterpolator node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIScalarInterpolator extends BaseNode implements ScalarInterpolator {

    /**
     * The key inputOutput field
     */
    private MFFloat key;

    /**
     * The keyValue inputOutput field
     */
    private MFFloat keyValue;

    /**
     * The set_fraction inputOnly field
     */
    private SFFloat set_fraction;

    /**
     * The value_changed outputOnly field
     */
    private SFFloat value_changed;

    /**
     * Constructor
     * @param bnf
     */
    public SAIScalarInterpolator(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
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
     * Return the number of MFFloat items in the keyValue field.
     *
     * @return the number of MFFloat items in the keyValue field.
     */
    @Override
    public int getNumKeyValue() {
        if (keyValue == null) {
            keyValue = (MFFloat) getField("keyValue");
        }
        return (keyValue.getSize());
    }

    /**
     * Return the keyValue value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getKeyValue(float[] val) {
        if (keyValue == null) {
            keyValue = (MFFloat) getField("keyValue");
        }
        keyValue.getValue(val);
    }

    /**
     * Set the keyValue field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setKeyValue(float[] val) {
        if (keyValue == null) {
            keyValue = (MFFloat) getField("keyValue");
        }
        keyValue.setValue(val.length, val);
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
     * Return the value_changed float value.
     *
     * @return The value_changed float value.
     */
    @Override
    public float getValue() {
        if (value_changed == null) {
            value_changed = (SFFloat) getField("value_changed");
        }
        return (value_changed.getValue());
    }
}
