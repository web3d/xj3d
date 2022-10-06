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

package org.xj3d.sai.internal.node.networking;

// Standard Library imports
import java.lang.ref.ReferenceQueue;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.networking.Inline;

/**
 * A concrete implementation of the Inline node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIInline extends BaseNode implements Inline {

    /**
     * The url inputOutput field
     */
    private MFString url;

    /**
     * The bboxCenter initializeOnly field
     */
    private SFVec3f bboxCenter;

    /**
     * The bboxSize initializeOnly field
     */
    private SFVec3f bboxSize;

    /**
     * The load inputOutput field
     */
    private SFBool load;

    /**
     * Constructor
     * @param bnf
     */
    public SAIInline(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the number of MFString items in the url field.
     *
     * @return the number of MFString items in the url field.
     */
    @Override
    public int getNumUrl() {
        if (url == null) {
            url = (MFString) getField("url");
        }
        return (url.getSize());
    }

    /**
     * Return the url value in the argument String[]
     *
     * @param val The String[] to initialize.
     */
    @Override
    public void getUrl(String[] val) {
        if (url == null) {
            url = (MFString) getField("url");
        }
        url.getValue(val);
    }

    /**
     * Set the url field.
     *
     * @param val The String[] to set.
     */
    @Override
    public void setUrl(String[] val) {
        if (url == null) {
            url = (MFString) getField("url");
        }
        url.setValue(val.length, val);
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
     * Return the load boolean value.
     *
     * @return The load boolean value.
     */
    @Override
    public boolean getLoad() {
        if (load == null) {
            load = (SFBool) getField("load");
        }
        return (load.getValue());
    }

    /**
     * Set the load field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setLoad(boolean val) {
        if (load == null) {
            load = (SFBool) getField("load");
        }
        load.setValue(val);
    }
}
