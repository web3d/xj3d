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

package org.xj3d.sai.internal.node.geometry3d;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.geometry3d.Cylinder;

/** A concrete implementation of the Cylinder node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICylinder extends BaseNode implements Cylinder {

    /** The solid initializeOnly field */
    private SFBool solid;

    /** The radius initializeOnly field */
    private SFFloat radius;

    /** The height initializeOnly field */
    private SFFloat height;

    /** The bottom initializeOnly field */
    private SFBool bottom;

    /** The side initializeOnly field */
    private SFBool side;

    /** The top initializeOnly field */
    private SFBool top;

    /**
     * Constructor
     * @param bnf
     */
    public SAICylinder(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the solid boolean value.
     *
     * @return The solid boolean value.
     */
    @Override
    public boolean getSolid() {
        if (solid == null) {
            solid = (SFBool) getField("solid");
        }
        return (solid.getValue());
    }

    /**
     * Set the solid field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setSolid(boolean val) {
        if (solid == null) {
            solid = (SFBool) getField("solid");
        }
        solid.setValue(val);
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

    /**
     * Return the height float value.
     *
     * @return The height float value.
     */
    @Override
    public float getHeight() {
        if (height == null) {
            height = (SFFloat) getField("height");
        }
        return (height.getValue());
    }

    /**
     * Set the height field.
     *
     * @param val The float to set.
     */
    @Override
    public void setHeight(float val) {
        if (height == null) {
            height = (SFFloat) getField("height");
        }
        height.setValue(val);
    }

    /**
     * Return the bottom boolean value.
     *
     * @return The bottom boolean value.
     */
    @Override
    public boolean getBottom() {
        if (bottom == null) {
            bottom = (SFBool) getField("bottom");
        }
        return (bottom.getValue());
    }

    /**
     * Set the bottom field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setBottom(boolean val) {
        if (bottom == null) {
            bottom = (SFBool) getField("bottom");
        }
        bottom.setValue(val);
    }

    /**
     * Return the side boolean value.
     *
     * @return The side boolean value.
     */
    @Override
    public boolean getSide() {
        if (side == null) {
            side = (SFBool) getField("side");
        }
        return (side.getValue());
    }

    /**
     * Set the side field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setSide(boolean val) {
        if (side == null) {
            side = (SFBool) getField("side");
        }
        side.setValue(val);
    }

    /**
     * Return the top boolean value.
     *
     * @return The top boolean value.
     */
    @Override
    public boolean getTop() {
        if (top == null) {
            top = (SFBool) getField("top");
        }
        return (top.getValue());
    }

    /**
     * Set the top field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setTop(boolean val) {
        if (top == null) {
            top = (SFBool) getField("top");
        }
        top.setValue(val);
    }
}
