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

package org.xj3d.sai.internal.node.grouping;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.grouping.StaticGroup;

/**
 * A concrete implementation of the StaticGroup node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIStaticGroup extends BaseNode implements StaticGroup {

    /**
     * The children initializeOnly field
     */
    private MFNode children;

    /**
     * The bboxCenter initializeOnly field
     */
    private SFVec3f bboxCenter;

    /**
     * The bboxSize initializeOnly field
     */
    private SFVec3f bboxSize;

    /**
     * Constructor
     * @param bnf
     */
    public SAIStaticGroup(
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
}
