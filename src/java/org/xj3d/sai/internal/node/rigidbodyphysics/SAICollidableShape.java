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
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.CollidableShape;

/**
 * A concrete implementation of the CollidableShape node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAICollidableShape extends BaseNode implements CollidableShape {

    /**
     * The bboxSize initializeOnly field
     */
    private SFVec3f bboxSize;

    /**
     * The bboxCenter initializeOnly field
     */
    private SFVec3f bboxCenter;

    /**
     * The enabled inputOutput field
     */
    private SFBool enabled;

    /**
     * The rotation inputOutput field
     */
    private SFRotation rotation;

    /**
     * The translation inputOutput field
     */
    private SFVec3f translation;

    /**
     * The shape initializeOnly field
     */
    private SFNode shape;

    /**
     * Constructor
     * @param bnf
     */
    public SAICollidableShape(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the bboxSize value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBboxSize(float[] val) {
        if (bboxSize == null) {
            bboxSize = (SFVec3f) getField("bboxSize");
        }
        bboxSize.getValue(val);
    }

    /**
     * Set the bboxSize field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setBboxSize(float[] val) {
        if (bboxSize == null) {
            bboxSize = (SFVec3f) getField("bboxSize");
        }
        bboxSize.setValue(val);
    }

    /**
     * Return the bboxCenter value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getBboxCenter(float[] val) {
        if (bboxCenter == null) {
            bboxCenter = (SFVec3f) getField("bboxCenter");
        }
        bboxCenter.getValue(val);
    }

    /**
     * Set the bboxCenter field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setBboxCenter(float[] val) {
        if (bboxCenter == null) {
            bboxCenter = (SFVec3f) getField("bboxCenter");
        }
        bboxCenter.setValue(val);
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
     * Return the rotation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getRotation(float[] val) {
        if (rotation == null) {
            rotation = (SFRotation) getField("rotation");
        }
        rotation.getValue(val);
    }

    /**
     * Set the rotation field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setRotation(float[] val) {
        if (rotation == null) {
            rotation = (SFRotation) getField("rotation");
        }
        rotation.setValue(val);
    }

    /**
     * Return the translation value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getTranslation(float[] val) {
        if (translation == null) {
            translation = (SFVec3f) getField("translation");
        }
        translation.getValue(val);
    }

    /**
     * Set the translation field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setTranslation(float[] val) {
        if (translation == null) {
            translation = (SFVec3f) getField("translation");
        }
        translation.setValue(val);
    }

    /**
     * Return the shape X3DNode value.
     *
     * @return The shape X3DNode value.
     */
    @Override
    public X3DNode getShape() {
        if (shape == null) {
            shape = (SFNode) getField("shape");
        }
        return (shape.getValue());
    }

    /**
     * Set the shape field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setShape(X3DNode val) {
        if (shape == null) {
            shape = (SFNode) getField("shape");
        }
        shape.setValue(val);
    }
}
