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

package org.xj3d.sai.internal.node.lighting;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.lighting.PointLight;

/**
 * A concrete implementation of the PointLight node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIPointLight extends BaseNode implements PointLight {

    /**
     * The ambientIntensity inputOutput field
     */
    private SFFloat ambientIntensity;

    /**
     * The color inputOutput field
     */
    private SFColor color;

    /**
     * The intensity inputOutput field
     */
    private SFFloat intensity;

    /**
     * The on inputOutput field
     */
    private SFBool on;

    /**
     * The global inputOutput field
     */
    private SFBool global;

    /**
     * The attenuation inputOutput field
     */
    private SFVec3f attenuation;

    /**
     * The location inputOutput field
     */
    private SFVec3f location;

    /**
     * The radius inputOutput field
     */
    private SFFloat radius;

    /**
     * Constructor
     * @param bnf
     */
    public SAIPointLight(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the ambientIntensity float value.
     *
     * @return The ambientIntensity float value.
     */
    @Override
    public float getAmbientIntensity() {
        if (ambientIntensity == null) {
            ambientIntensity = (SFFloat) getField("ambientIntensity");
        }
        return (ambientIntensity.getValue());
    }

    /**
     * Set the ambientIntensity field.
     *
     * @param val The float to set.
     */
    @Override
    public void setAmbientIntensity(float val) {
        if (ambientIntensity == null) {
            ambientIntensity = (SFFloat) getField("ambientIntensity");
        }
        ambientIntensity.setValue(val);
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
     * Return the intensity float value.
     *
     * @return The intensity float value.
     */
    @Override
    public float getIntensity() {
        if (intensity == null) {
            intensity = (SFFloat) getField("intensity");
        }
        return (intensity.getValue());
    }

    /**
     * Set the intensity field.
     *
     * @param val The float to set.
     */
    @Override
    public void setIntensity(float val) {
        if (intensity == null) {
            intensity = (SFFloat) getField("intensity");
        }
        intensity.setValue(val);
    }

    /**
     * Return the on boolean value.
     *
     * @return The on boolean value.
     */
    @Override
    public boolean getOn() {
        if (on == null) {
            on = (SFBool) getField("on");
        }
        return (on.getValue());
    }

    /**
     * Set the on field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setOn(boolean val) {
        if (on == null) {
            on = (SFBool) getField("on");
        }
        on.setValue(val);
    }

    /**
     * Return the global boolean value.
     *
     * @return The global boolean value.
     */
    @Override
    public boolean getGlobal() {
        if (global == null) {
            global = (SFBool) getField("global");
        }
        return (global.getValue());
    }

    /**
     * Set the global field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setGlobal(boolean val) {
        if (global == null) {
            global = (SFBool) getField("global");
        }
        global.setValue(val);
    }

    /**
     * Return the attenuation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getAttenuation(float[] val) {
        if (attenuation == null) {
            attenuation = (SFVec3f) getField("attenuation");
        }
        attenuation.getValue(val);
    }

    /**
     * Set the attenuation field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setAttenuation(float[] val) {
        if (attenuation == null) {
            attenuation = (SFVec3f) getField("attenuation");
        }
        attenuation.setValue(val);
    }

    /**
     * Return the location value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getLocation(float[] val) {
        if (location == null) {
            location = (SFVec3f) getField("location");
        }
        location.getValue(val);
    }

    /**
     * Set the location field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setLocation(float[] val) {
        if (location == null) {
            location = (SFVec3f) getField("location");
        }
        location.setValue(val);
    }

    /**
     * Return the radius float value.
     *
     * @return The radius float value.
     */
    @Override
    public float getRadius() {
        if (radius == null) {
            radius = (SFFloat) getField("radius");
        }
        return (radius.getValue());
    }

    /**
     * Set the radius field.
     *
     * @param val The float to set.
     */
    @Override
    public void setRadius(float val) {
        if (radius == null) {
            radius = (SFFloat) getField("radius");
        }
        radius.setValue(val);
    }
}
