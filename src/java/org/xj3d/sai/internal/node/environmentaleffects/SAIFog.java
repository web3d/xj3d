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
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.environmentaleffects.Fog;

/** A concrete implementation of the Fog node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIFog extends BaseNode implements Fog {

    /** The set_bind inputOnly field */
    private SFBool set_bind;

    /** The bindTime outputOnly field */
    private SFTime bindTime;

    /** The isBound outputOnly field */
    private SFBool isBound;

    /** The color inputOutput field */
    private SFColor color;

    /** The fogType inputOutput field */
    private SFString fogType;

    /** The visibilityRange inputOutput field */
    private SFFloat visibilityRange;

    /**
     * Constructor
     * @param bnf
     */
    public SAIFog(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Set the set_bind field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setBind(boolean val) {
        if (set_bind == null) {
            set_bind = (SFBool) getField("set_bind");
        }
        set_bind.setValue(val);
    }

    /**
     * Return the bindTime double value.
     *
     * @return The bindTime double value.
     */
    @Override
    public double getBindTime() {
        if (bindTime == null) {
            bindTime = (SFTime) getField("bindTime");
        }
        return (bindTime.getValue());
    }

    /**
     * Return the isBound boolean value.
     *
     * @return The isBound boolean value.
     */
    @Override
    public boolean getIsBound() {
        if (isBound == null) {
            isBound = (SFBool) getField("isBound");
        }
        return (isBound.getValue());
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
