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

package org.xj3d.sai.internal.node.rendering;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DCoordinateNode;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.rendering.LineSet;

/**
 * A concrete implementation of the LineSet node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAILineSet extends BaseNode implements LineSet {

    /**
     * The coord inputOutput field
     */
    private SFNode coord;

    /**
     * The color inputOutput field
     */
    private SFNode color;

    /**
     * The vertexCount inputOutput field
     */
    private MFInt32 vertexCount;

    /**
     * Constructor
     * @param bnf
     */
    public SAILineSet(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the coord X3DNode value.
     *
     * @return The coord X3DNode value.
     */
    @Override
    public X3DNode getCoord() {
        if (coord == null) {
            coord = (SFNode) getField("coord");
        }
        return (coord.getValue());
    }

    /**
     * Set the coord field.
     *
     * @param val The X3DCoordinateNode to set.
     */
    @Override
    public void setCoord(X3DCoordinateNode val) {
        if (coord == null) {
            coord = (SFNode) getField("coord");
        }
        coord.setValue(val);
    }

    /**
     * Set the coord field.
     *
     * @param val The X3DProtoInstance to set.
     */
    @Override
    public void setCoord(X3DProtoInstance val) {
        if (coord == null) {
            coord = (SFNode) getField("coord");
        }
        coord.setValue(val);
    }

    /**
     * Return the color X3DNode value.
     *
     * @return The color X3DNode value.
     */
    @Override
    public X3DNode getColor() {
        if (color == null) {
            color = (SFNode) getField("color");
        }
        return (color.getValue());
    }

    /**
     * Set the color field.
     *
     * @param val The X3DColorNode to set.
     */
    @Override
    public void setColor(X3DColorNode val) {
        if (color == null) {
            color = (SFNode) getField("color");
        }
        color.setValue(val);
    }

    /**
     * Set the color field.
     *
     * @param val The X3DProtoInstance to set.
     */
    @Override
    public void setColor(X3DProtoInstance val) {
        if (color == null) {
            color = (SFNode) getField("color");
        }
        color.setValue(val);
    }

    /**
     * Return the number of MFInt32 items in the vertexCount field.
     *
     * @return the number of MFInt32 items in the vertexCount field.
     */
    @Override
    public int getNumVertexCount() {
        if (vertexCount == null) {
            vertexCount = (MFInt32) getField("vertexCount");
        }
        return (vertexCount.getSize());
    }

    /**
     * Return the vertexCount value in the argument int[]
     *
     * @param val The int[] to initialize.
     */
    @Override
    public void getVertexCount(int[] val) {
        if (vertexCount == null) {
            vertexCount = (MFInt32) getField("vertexCount");
        }
        vertexCount.getValue(val);
    }

    /**
     * Set the vertexCount field.
     *
     * @param val The int[] to set.
     */
    @Override
    public void setVertexCount(int[] val) {
        if (vertexCount == null) {
            vertexCount = (MFInt32) getField("vertexCount");
        }
        vertexCount.setValue(val.length, val);
    }
}
