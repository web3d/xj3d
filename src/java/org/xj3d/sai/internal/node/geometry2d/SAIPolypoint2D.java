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

package org.xj3d.sai.internal.node.geometry2d;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.geometry2d.Polypoint2D;

/** A concrete implementation of the Polypoint2D node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIPolypoint2D extends BaseNode implements Polypoint2D {

    /** The point initializeOnly field */
    private MFVec2f point;

    /**
     * Constructor
     * @param bnf
     */
    public SAIPolypoint2D(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFVec2f items in the point field.
     *
     * @return the number of MFVec2f items in the point field.
     */
    @Override
    public int getNumPoint() {
        if (point == null) {
            point = (MFVec2f) getField("point");
        }
        return (point.getSize());
    }

    /**
     * Return the point value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getPoint(float[] val) {
        if (point == null) {
            point = (MFVec2f) getField("point");
        }
        point.getValue(val);
    }

    /**
     * Set the point field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setPoint(float[] val) {
        if (point == null) {
            point = (MFVec2f) getField("point");
        }
        point.setValue(val.length / 2, val);
    }
}
