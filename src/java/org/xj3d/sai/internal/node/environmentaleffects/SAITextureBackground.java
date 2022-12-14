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
import org.web3d.x3d.sai.MFColor;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.environmentaleffects.TextureBackground;

/** A concrete implementation of the TextureBackground node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAITextureBackground extends BaseNode implements TextureBackground {

    /** The set_bind inputOnly field */
    private SFBool set_bind;

    /** The bindTime outputOnly field */
    private SFTime bindTime;

    /** The isBound outputOnly field */
    private SFBool isBound;

    /** The groundAngle inputOutput field */
    private MFFloat groundAngle;

    /** The groundColor inputOutput field */
    private MFColor groundColor;

    /** The skyAngle inputOutput field */
    private MFFloat skyAngle;

    /** The skyColor inputOutput field */
    private MFColor skyColor;

    /** The backTexture inputOutput field */
    private MFNode backTexture;

    /** The frontTexture inputOutput field */
    private MFNode frontTexture;

    /** The leftTexture inputOutput field */
    private MFNode leftTexture;

    /** The rightTexture inputOutput field */
    private MFNode rightTexture;

    /** The bottomTexture inputOutput field */
    private MFNode bottomTexture;

    /** The topTexture inputOutput field */
    private MFNode topTexture;

    /** The transparency inputOutput field */
    private SFFloat transparency;

    /**
     * Constructor
     * @param bnf
     */
    public SAITextureBackground(
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
     * Return the number of MFFloat items in the groundAngle field.
     *
     * @return the number of MFFloat items in the groundAngle field.
     */
    @Override
    public int getNumGroundAngle() {
        if (groundAngle == null) {
            groundAngle = (MFFloat) getField("groundAngle");
        }
        return (groundAngle.getSize());
    }

    /**
     * Return the groundAngle value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getGroundAngle(float[] val) {
        if (groundAngle == null) {
            groundAngle = (MFFloat) getField("groundAngle");
        }
        groundAngle.getValue(val);
    }

    /**
     * Set the groundAngle field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setGroundAngle(float[] val) {
        if (groundAngle == null) {
            groundAngle = (MFFloat) getField("groundAngle");
        }
        groundAngle.setValue(val.length, val);
    }

    /**
     * Return the number of MFColor items in the groundColor field.
     *
     * @return the number of MFColor items in the groundColor field.
     */
    @Override
    public int getNumGroundColor() {
        if (groundColor == null) {
            groundColor = (MFColor) getField("groundColor");
        }
        return (groundColor.getSize());
    }

    /**
     * Return the groundColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getGroundColor(float[] val) {
        if (groundColor == null) {
            groundColor = (MFColor) getField("groundColor");
        }
        groundColor.getValue(val);
    }

    /**
     * Set the groundColor field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setGroundColor(float[] val) {
        if (groundColor == null) {
            groundColor = (MFColor) getField("groundColor");
        }
        groundColor.setValue(val.length / 3, val);
    }

    /**
     * Return the number of MFFloat items in the skyAngle field.
     *
     * @return the number of MFFloat items in the skyAngle field.
     */
    @Override
    public int getNumSkyAngle() {
        if (skyAngle == null) {
            skyAngle = (MFFloat) getField("skyAngle");
        }
        return (skyAngle.getSize());
    }

    /**
     * Return the skyAngle value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getSkyAngle(float[] val) {
        if (skyAngle == null) {
            skyAngle = (MFFloat) getField("skyAngle");
        }
        skyAngle.getValue(val);
    }

    /**
     * Set the skyAngle field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setSkyAngle(float[] val) {
        if (skyAngle == null) {
            skyAngle = (MFFloat) getField("skyAngle");
        }
        skyAngle.setValue(val.length, val);
    }

    /**
     * Return the number of MFColor items in the skyColor field.
     *
     * @return the number of MFColor items in the skyColor field.
     */
    @Override
    public int getNumSkyColor() {
        if (skyColor == null) {
            skyColor = (MFColor) getField("skyColor");
        }
        return (skyColor.getSize());
    }

    /**
     * Return the skyColor value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getSkyColor(float[] val) {
        if (skyColor == null) {
            skyColor = (MFColor) getField("skyColor");
        }
        skyColor.getValue(val);
    }

    /**
     * Set the skyColor field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setSkyColor(float[] val) {
        if (skyColor == null) {
            skyColor = (MFColor) getField("skyColor");
        }
        skyColor.setValue(val.length / 3, val);
    }

    /**
     * Return the number of MFNode items in the backTexture field.
     *
     * @return the number of MFNode items in the backTexture field.
     */
    @Override
    public int getNumBackTexture() {
        if (backTexture == null) {
            backTexture = (MFNode) getField("backTexture");
        }
        return (backTexture.getSize());
    }

    /**
     * Return the backTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getBackTexture(X3DNode[] val) {
        if (backTexture == null) {
            backTexture = (MFNode) getField("backTexture");
        }
        backTexture.getValue(val);
    }

    /**
     * Set the backTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setBackTexture(X3DNode[] val) {
        if (backTexture == null) {
            backTexture = (MFNode) getField("backTexture");
        }
        backTexture.setValue(val.length, val);
    }

    /**
     * Return the number of MFNode items in the frontTexture field.
     *
     * @return the number of MFNode items in the frontTexture field.
     */
    @Override
    public int getNumFrontTexture() {
        if (frontTexture == null) {
            frontTexture = (MFNode) getField("frontTexture");
        }
        return (frontTexture.getSize());
    }

    /**
     * Return the frontTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getFrontTexture(X3DNode[] val) {
        if (frontTexture == null) {
            frontTexture = (MFNode) getField("frontTexture");
        }
        frontTexture.getValue(val);
    }

    /**
     * Set the frontTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setFrontTexture(X3DNode[] val) {
        if (frontTexture == null) {
            frontTexture = (MFNode) getField("frontTexture");
        }
        frontTexture.setValue(val.length, val);
    }

    /**
     * Return the number of MFNode items in the leftTexture field.
     *
     * @return the number of MFNode items in the leftTexture field.
     */
    @Override
    public int getNumLeftTexture() {
        if (leftTexture == null) {
            leftTexture = (MFNode) getField("leftTexture");
        }
        return (leftTexture.getSize());
    }

    /**
     * Return the leftTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getLeftTexture(X3DNode[] val) {
        if (leftTexture == null) {
            leftTexture = (MFNode) getField("leftTexture");
        }
        leftTexture.getValue(val);
    }

    /**
     * Set the leftTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setLeftTexture(X3DNode[] val) {
        if (leftTexture == null) {
            leftTexture = (MFNode) getField("leftTexture");
        }
        leftTexture.setValue(val.length, val);
    }

    /**
     * Return the number of MFNode items in the rightTexture field.
     *
     * @return the number of MFNode items in the rightTexture field.
     */
    @Override
    public int getNumRightTexture() {
        if (rightTexture == null) {
            rightTexture = (MFNode) getField("rightTexture");
        }
        return (rightTexture.getSize());
    }

    /**
     * Return the rightTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getRightTexture(X3DNode[] val) {
        if (rightTexture == null) {
            rightTexture = (MFNode) getField("rightTexture");
        }
        rightTexture.getValue(val);
    }

    /**
     * Set the rightTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setRightTexture(X3DNode[] val) {
        if (rightTexture == null) {
            rightTexture = (MFNode) getField("rightTexture");
        }
        rightTexture.setValue(val.length, val);
    }

    /**
     * Return the number of MFNode items in the bottomTexture field.
     *
     * @return the number of MFNode items in the bottomTexture field.
     */
    @Override
    public int getNumBottomTexture() {
        if (bottomTexture == null) {
            bottomTexture = (MFNode) getField("bottomTexture");
        }
        return (bottomTexture.getSize());
    }

    /**
     * Return the bottomTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getBottomTexture(X3DNode[] val) {
        if (bottomTexture == null) {
            bottomTexture = (MFNode) getField("bottomTexture");
        }
        bottomTexture.getValue(val);
    }

    /**
     * Set the bottomTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setBottomTexture(X3DNode[] val) {
        if (bottomTexture == null) {
            bottomTexture = (MFNode) getField("bottomTexture");
        }
        bottomTexture.setValue(val.length, val);
    }

    /**
     * Return the number of MFNode items in the topTexture field.
     *
     * @return the number of MFNode items in the topTexture field.
     */
    @Override
    public int getNumTopTexture() {
        if (topTexture == null) {
            topTexture = (MFNode) getField("topTexture");
        }
        return (topTexture.getSize());
    }

    /**
     * Return the topTexture value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getTopTexture(X3DNode[] val) {
        if (topTexture == null) {
            topTexture = (MFNode) getField("topTexture");
        }
        topTexture.getValue(val);
    }

    /**
     * Set the topTexture field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setTopTexture(X3DNode[] val) {
        if (topTexture == null) {
            topTexture = (MFNode) getField("topTexture");
        }
        topTexture.setValue(val.length, val);
    }

    /**
     * Return the transparency float value.
     *
     * @return The transparency float value.
     */
    @Override
    public float getTransparency() {
        if (transparency == null) {
            transparency = (SFFloat) getField("transparency");
        }
        return (transparency.getValue());
    }

    /**
     * Set the transparency field.
     *
     * @param val The float to set.
     */
    @Override
    public void setTransparency(float val) {
        if (transparency == null) {
            transparency = (SFFloat) getField("transparency");
        }
        transparency.setValue(val);
    }
}
