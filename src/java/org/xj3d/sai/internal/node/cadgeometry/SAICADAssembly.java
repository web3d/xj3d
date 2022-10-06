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

package org.xj3d.sai.internal.node.cadgeometry;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.cadgeometry.CADAssembly;

/** A concrete implementation of the CADAssembly node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAICADAssembly extends BaseNode implements CADAssembly {

    /** The children inputOutput field */
    private MFNode children;

    /** The addChildren inputOnly field */
    private MFNode addChildren;

    /** The removeChildren inputOnly field */
    private MFNode removeChildren;

    /** The bboxCenter initializeOnly field */
    private SFVec3f bboxCenter;

    /** The bboxSize initializeOnly field */
    private SFVec3f bboxSize;

    /** The name inputOutput field */
    private SFString name;

    /**
     * Constructor
     * @param bnf
     */
    public SAICADAssembly(
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
     * Return the name String value.
     *
     * @return The name String value.
     */
    @Override
    public String getName() {
        if (name == null) {
            name = (SFString) getField("name");
        }
        return (name.getValue());
    }

    /**
     * Set the name field.
     *
     * @param val The String to set.
     */
    @Override
    public void setName(String val) {
        if (name == null) {
            name = (SFString) getField("name");
        }
        name.setValue(val);
    }
}
