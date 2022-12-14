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

package org.xj3d.sai.internal.node.navigation;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.navigation.Collision;

/**
 * A concrete implementation of the Collision node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAICollision extends BaseNode implements Collision {

    /**
     * The children inputOutput field
     */
    private MFNode children;

    /**
     * The addChildren inputOnly field
     */
    private MFNode addChildren;

    /**
     * The removeChildren inputOnly field
     */
    private MFNode removeChildren;

    /**
     * The bboxCenter initializeOnly field
     */
    private SFVec3f bboxCenter;

    /**
     * The bboxSize initializeOnly field
     */
    private SFVec3f bboxSize;

    /**
     * The collide inputOutput field
     */
    private SFBool collide;

    /**
     * The proxy initializeOnly field
     */
    private SFNode proxy;

    /**
     * The collideTime outputOnly field
     */
    private SFTime collideTime;

    /**
     * The isActive outputOnly field
     */
    private SFBool isActive;

    /**
     * The enabled inputOutput field
     */
    private SFBool enabled;

    /**
     * Constructor
     * @param bnf
     */
    public SAICollision(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFNode items in the children field.
     *
     * @return the number of MFNode items in the children field.
     */
    @Override
    public int getNumChildren() {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        return (children.getSize());
    }

    /**
     * Return the children value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    @Override
    public void getChildren(X3DNode[] val) {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        children.getValue(val);
    }

    /**
     * Set the children field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void setChildren(X3DNode[] val) {
        if (children == null) {
            children = (MFNode) getField("children");
        }
        children.setValue(val.length, val);
    }

    /**
     * Set the addChildren field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void addChildren(X3DNode[] val) {
        if (addChildren == null) {
            addChildren = (MFNode) getField("addChildren");
        }
        addChildren.setValue(val.length, val);
    }

    /**
     * Set the removeChildren field.
     *
     * @param val The X3DNode[] to set.
     */
    @Override
    public void removeChildren(X3DNode[] val) {
        if (removeChildren == null) {
            removeChildren = (MFNode) getField("removeChildren");
        }
        removeChildren.setValue(val.length, val);
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
     * Return the collide boolean value.
     *
     * @return The collide boolean value.
     */
    @Override
    public boolean getCollide() {
        if (collide == null) {
            collide = (SFBool) getField("collide");
        }
        return (collide.getValue());
    }

    /**
     * Set the collide field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setCollide(boolean val) {
        if (collide == null) {
            collide = (SFBool) getField("collide");
        }
        collide.setValue(val);
    }

    /**
     * Return the proxy X3DNode value.
     *
     * @return The proxy X3DNode value.
     */
    @Override
    public X3DNode getProxy() {
        if (proxy == null) {
            proxy = (SFNode) getField("proxy");
        }
        return (proxy.getValue());
    }

    /**
     * Set the proxy field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setProxy(X3DNode val) {
        if (proxy == null) {
            proxy = (SFNode) getField("proxy");
        }
        proxy.setValue(val);
    }

    /**
     * Return the collideTime double value.
     *
     * @return The collideTime double value.
     */
    @Override
    public double getCollideTime() {
        if (collideTime == null) {
            collideTime = (SFTime) getField("collideTime");
        }
        return (collideTime.getValue());
    }

    /**
     * Return the isActive boolean value.
     *
     * @return The isActive boolean value.
     */
    @Override
    public boolean getIsActive() {
        if (isActive == null) {
            isActive = (SFBool) getField("isActive");
        }
        return (isActive.getValue());
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
}
