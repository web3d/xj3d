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

package org.xj3d.sai.internal.node.rigidbodyphysics;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.SingleAxisHingeJoint;

/**
 * A concrete implementation of the SingleAxisHingeJoint node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAISingleAxisHingeJoint extends BaseNode implements SingleAxisHingeJoint {

    /**
     * The forceOutput inputOutput field
     */
    private MFString forceOutput;

    /**
     * The body1 inputOutput field
     */
    private SFNode body1;

    /**
     * The body2 inputOutput field
     */
    private SFNode body2;

    /**
     * The anchorPoint inputOutput field
     */
    private SFVec3f anchorPoint;

    /**
     * The body1AnchorPoint outputOnly field
     */
    private SFVec3f body1AnchorPoint;

    /**
     * The body2AnchorPoint outputOnly field
     */
    private SFVec3f body2AnchorPoint;

    /**
     * The Axis inputOutput field
     */
    private SFVec3f Axis;

    /**
     * The minAngle inputOutput field
     */
    private SFFloat minAngle;

    /**
     * The maxAngle inputOutput field
     */
    private SFFloat maxAngle;

    /**
     * The angle outputOnly field
     */
    private SFFloat angle;

    /**
     * The angleRate outputOnly field
     */
    private SFFloat angleRate;

    /**
     * The stopBounce inputOutput field
     */
    private SFFloat stopBounce;

    /**
     * The stopErrorCorrection inputOutput field
     */
    private SFFloat stopErrorCorrection;

    /**
     * Constructor
     * @param bnf
     */
    public SAISingleAxisHingeJoint(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFString items in the forceOutput field.
     *
     * @return the number of MFString items in the forceOutput field.
     */
    @Override
    public int getNumForceOutput() {
        if (forceOutput == null) {
            forceOutput = (MFString) getField("forceOutput");
        }
        return (forceOutput.getSize());
    }

    /**
     * Return the forceOutput value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getForceOutput(String[] val) {
        if (forceOutput == null) {
            forceOutput = (MFString) getField("forceOutput");
        }
        forceOutput.getValue(val);
    }

    /**
     * Set the forceOutput field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setForceOutput(String[] val) {
        if (forceOutput == null) {
            forceOutput = (MFString) getField("forceOutput");
        }
        forceOutput.setValue(val.length, val);
    }

    /**
     * Return the body1 X3DNode value.
     *
     * @return The body1 X3DNode value.
     */
    @Override
    public X3DNode getBody1() {
        if (body1 == null) {
            body1 = (SFNode) getField("body1");
        }
        return (body1.getValue());
    }

    /**
     * Set the body1 field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setBody1(X3DNode val) {
        if (body1 == null) {
            body1 = (SFNode) getField("body1");
        }
        body1.setValue(val);
    }

    /**
     * Return the body2 X3DNode value.
     *
     * @return The body2 X3DNode value.
     */
    @Override
    public X3DNode getBody2() {
        if (body2 == null) {
            body2 = (SFNode) getField("body2");
        }
        return (body2.getValue());
    }

    /**
     * Set the body2 field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setBody2(X3DNode val) {
        if (body2 == null) {
            body2 = (SFNode) getField("body2");
        }
        body2.setValue(val);
    }

    /**
     * Return the anchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getAnchorPoint(float[] val) {
        if (anchorPoint == null) {
            anchorPoint = (SFVec3f) getField("anchorPoint");
        }
        anchorPoint.getValue(val);
    }

    /**
     * Set the anchorPoint field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setAnchorPoint(float[] val) {
        if (anchorPoint == null) {
            anchorPoint = (SFVec3f) getField("anchorPoint");
        }
        anchorPoint.setValue(val);
    }

    /**
     * Return the body1AnchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBody1AnchorPoint(float[] val) {
        if (body1AnchorPoint == null) {
            body1AnchorPoint = (SFVec3f) getField("body1AnchorPoint");
        }
        body1AnchorPoint.getValue(val);
    }

    /**
     * Return the body2AnchorPoint value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBody2AnchorPoint(float[] val) {
        if (body2AnchorPoint == null) {
            body2AnchorPoint = (SFVec3f) getField("body2AnchorPoint");
        }
        body2AnchorPoint.getValue(val);
    }

    /**
     * Return the Axis value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getAxis(float[] val) {
        if (Axis == null) {
            Axis = (SFVec3f) getField("Axis");
        }
        Axis.getValue(val);
    }

    /**
     * Set the Axis field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setAxis(float[] val) {
        if (Axis == null) {
            Axis = (SFVec3f) getField("Axis");
        }
        Axis.setValue(val);
    }

    /**
     * Return the minAngle float value.
     *
     * @return The minAngle float value.
     */
    @Override
    public float getMinAngle() {
        if (minAngle == null) {
            minAngle = (SFFloat) getField("minAngle");
        }
        return (minAngle.getValue());
    }

    /**
     * Set the minAngle field.
     *
     * @param val The float to set.
     */
    @Override
    public void setMinAngle(float val) {
        if (minAngle == null) {
            minAngle = (SFFloat) getField("minAngle");
        }
        minAngle.setValue(val);
    }

    /**
     * Return the maxAngle float value.
     *
     * @return The maxAngle float value.
     */
    @Override
    public float getMaxAngle() {
        if (maxAngle == null) {
            maxAngle = (SFFloat) getField("maxAngle");
        }
        return (maxAngle.getValue());
    }

    /**
     * Set the maxAngle field.
     *
     * @param val The float to set.
     */
    @Override
    public void setMaxAngle(float val) {
        if (maxAngle == null) {
            maxAngle = (SFFloat) getField("maxAngle");
        }
        maxAngle.setValue(val);
    }

    /**
     * Return the angle float value.
     *
     * @return The angle float value.
     */
    @Override
    public float getAngle() {
        if (angle == null) {
            angle = (SFFloat) getField("angle");
        }
        return (angle.getValue());
    }

    /**
     * Return the angleRate float value.
     *
     * @return The angleRate float value.
     */
    @Override
    public float getAngleRate() {
        if (angleRate == null) {
            angleRate = (SFFloat) getField("angleRate");
        }
        return (angleRate.getValue());
    }

    /**
     * Return the stopBounce float value.
     *
     * @return The stopBounce float value.
     */
    @Override
    public float getStopBounce() {
        if (stopBounce == null) {
            stopBounce = (SFFloat) getField("stopBounce");
        }
        return (stopBounce.getValue());
    }

    /**
     * Set the stopBounce field.
     *
     * @param val The float to set.
     */
    @Override
    public void setStopBounce(float val) {
        if (stopBounce == null) {
            stopBounce = (SFFloat) getField("stopBounce");
        }
        stopBounce.setValue(val);
    }

    /**
     * Return the stopErrorCorrection float value.
     *
     * @return The stopErrorCorrection float value.
     */
    @Override
    public float getStopErrorCorrection() {
        if (stopErrorCorrection == null) {
            stopErrorCorrection = (SFFloat) getField("stopErrorCorrection");
        }
        return (stopErrorCorrection.getValue());
    }

    /**
     * Set the stopErrorCorrection field.
     *
     * @param val The float to set.
     */
    @Override
    public void setStopErrorCorrection(float val) {
        if (stopErrorCorrection == null) {
            stopErrorCorrection = (SFFloat) getField("stopErrorCorrection");
        }
        stopErrorCorrection.setValue(val);
    }
}
