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

package org.xj3d.sai.internal.node.geospatial;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.MFVec3d;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoPositionInterpolator;

/** A concrete implementation of the GeoPositionInterpolator node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIGeoPositionInterpolator extends BaseNode implements GeoPositionInterpolator {

    /** The key inputOutput field */
    private MFFloat key;

    /** The keyValue inputOutput field */
    private MFVec3d keyValue;

    /** The set_fraction inputOnly field */
    private SFFloat set_fraction;

    /** The value_changed outputOnly field */
    private SFVec3f value_changed;

    /** The geovalue_changed outputOnly field */
    private SFVec3d geovalue_changed;

    /** The geoOrigin initializeOnly field */
    private SFNode geoOrigin;

    /** The geoSystem initializeOnly field */
    private MFString geoSystem;

    /**
     * Constructor
     * @param bnf
     */
    public SAIGeoPositionInterpolator(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFFloat items in the key field.
     *
     * @return the number of MFFloat items in the key field.
     */
    @Override
    public int getNumKey() {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        return (key.getSize());
    }

    /**
     * Return the key value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getKey(float[] val) {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        key.getValue(val);
    }

    /**
     * Set the key field.
     *
     * @param val The float[] to set.
     */
    @Override
    public void setKey(float[] val) {
        if (key == null) {
            key = (MFFloat) getField("key");
        }
        key.setValue(val.length, val);
    }

    /**
     * Return the number of MFVec3d items in the keyValue field.
     *
     * @return the number of MFVec3d items in the keyValue field.
     */
    @Override
    public int getNumKeyValue() {
        if (keyValue == null) {
            keyValue = (MFVec3d) getField("keyValue");
        }
        return (keyValue.getSize());
    }

    /**
     * Return the keyValue value in the argument double[]
     *
     * @param val The double[] to initialize.
     */
    @Override
    public void getKeyValue(double[] val) {
        if (keyValue == null) {
            keyValue = (MFVec3d) getField("keyValue");
        }
        keyValue.getValue(val);
    }

    /**
     * Set the keyValue field.
     *
     * @param val The double[] to set.
     */
    @Override
    public void setKeyValue(double[] val) {
        if (keyValue == null) {
            keyValue = (MFVec3d) getField("keyValue");
        }
        keyValue.setValue(val.length / 3, val);
    }

    /**
     * Set the set_fraction field.
     *
     * @param val The float to set.
     */
    @Override
    public void setFraction(float val) {
        if (set_fraction == null) {
            set_fraction = (SFFloat) getField("set_fraction");
        }
        set_fraction.setValue(val);
    }

    /**
     * Return the value_changed value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    @Override
    public void getValue(float[] val) {
        if (value_changed == null) {
            value_changed = (SFVec3f) getField("value_changed");
        }
        value_changed.getValue(val);
    }

    /**
     * Return the geovalue_changed value in the argument double[]
     *
     * @param val The double[] to initialize.
     */
    @Override
    public void getGeovalue(double[] val) {
        if (geovalue_changed == null) {
            geovalue_changed = (SFVec3d) getField("geovalue_changed");
        }
        geovalue_changed.getValue(val);
    }

    /**
     * Return the geoOrigin X3DNode value.
     *
     * @return The geoOrigin X3DNode value.
     */
    @Override
    public X3DNode getGeoOrigin() {
        if (geoOrigin == null) {
            geoOrigin = (SFNode) getField("geoOrigin");
        }
        return (geoOrigin.getValue());
    }

    /**
     * Set the geoOrigin field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setGeoOrigin(X3DNode val) {
        if (geoOrigin == null) {
            geoOrigin = (SFNode) getField("geoOrigin");
        }
        geoOrigin.setValue(val);
    }

    /**
     * Return the number of MFString items in the geoSystem field.
     *
     * @return the number of MFString items in the geoSystem field.
     */
    @Override
    public int getNumGeoSystem() {
        if (geoSystem == null) {
            geoSystem = (MFString) getField("geoSystem");
        }
        return (geoSystem.getSize());
    }

    /**
     * Return the geoSystem value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getGeoSystem(String[] val) {
        if (geoSystem == null) {
            geoSystem = (MFString) getField("geoSystem");
        }
        geoSystem.getValue(val);
    }

    /**
     * Set the geoSystem field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setGeoSystem(String[] val) {
        if (geoSystem == null) {
            geoSystem = (MFString) getField("geoSystem");
        }
        geoSystem.setValue(val.length, val);
    }
}
