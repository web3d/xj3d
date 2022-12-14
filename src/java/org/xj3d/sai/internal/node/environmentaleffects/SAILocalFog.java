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

package org.xj3d.sai.internal.node.environmentaleffects;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.environmentaleffects.LocalFog;

/** A concrete implementation of the LocalFog node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAILocalFog extends BaseNode implements LocalFog {

    /** The enabled inputOutput field */
    private SFBool enabled;

    /** The color inputOutput field */
    private SFColor color;

    /** The fogType initializeOnly field */
    private SFString fogType;

    /** The visibilityRange inputOutput field */
    private SFFloat visibilityRange;

    /**
     * Constructor
     * @param bnf
     */
    public SAILocalFog(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the enabled boolean value.
     *
     * @return The enabled boolean value.
     */
    @Override
    public boolean getEnabled() {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        return (enabled.getValue());
    }

    /**
     * Set the enabled field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setEnabled(boolean val) {
        if (enabled == null) {
            enabled = (SFBool) getField("enabled");
        }
        enabled.setValue(val);
    }

    /**
     * Return the color value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getColor(float[] val) {
        if (color == null) {
            color = (SFColor) getField("color");
        }
        color.getValue(val);
    }

    /**
     * Set the color field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setColor(float[] val) {
        if (color == null) {
            color = (SFColor) getField("color");
        }
        color.setValue(val);
    }

    /**
     * Return the fogType String value.
     *
     * @return The fogType String value.
     */
    @Override
    public String getFogType() {
        if (fogType == null) {
            fogType = (SFString) getField("fogType");
        }
        return (fogType.getValue());
    }

    /**
     * Set the fogType field.
     *
     * @param val The String to set.
     */
    @Override
    public void setFogType(String val) {
        if (fogType == null) {
            fogType = (SFString) getField("fogType");
        }
        fogType.setValue(val);
    }

    /**
     * Return the visibilityRange float value.
     *
     * @return The visibilityRange float value.
     */
    @Override
    public float getVisibilityRange() {
        if (visibilityRange == null) {
            visibilityRange = (SFFloat) getField("visibilityRange");
        }
        return (visibilityRange.getValue());
    }

    /**
     * Set the visibilityRange field.
     *
     * @param val The float to set.
     */
    @Override
    public void setVisibilityRange(float val) {
        if (visibilityRange == null) {
            visibilityRange = (SFFloat) getField("visibilityRange");
        }
        visibilityRange.setValue(val);
    }
}
