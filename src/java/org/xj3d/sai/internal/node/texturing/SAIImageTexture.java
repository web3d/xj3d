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

package org.xj3d.sai.internal.node.texturing;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.texturing.ImageTexture;

/**
 * A concrete implementation of the ImageTexture node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIImageTexture extends BaseNode implements ImageTexture {

    /**
     * The textureProperties initializeOnly field
     */
    private SFNode textureProperties;

    /**
     * The repeatS initializeOnly field
     */
    private SFBool repeatS;

    /**
     * The repeatT initializeOnly field
     */
    private SFBool repeatT;

    /**
     * The url inputOutput field
     */
    private MFString url;

    /**
     * Constructor
     * @param bnf
     */
    public SAIImageTexture(
            VRMLNodeType node,
            ReferenceQueue<X3DField> refQueue,
            FieldFactory fac,
            FieldAccessListener fal,
            BaseNodeFactory bnf) {
        super(node, refQueue, fac, fal, bnf);
    }

    /**
     * Return the textureProperties X3DNode value.
     *
     * @return The textureProperties X3DNode value.
     */
    @Override
    public X3DNode getTextureProperties() {
        if (textureProperties == null) {
            textureProperties = (SFNode) getField("textureProperties");
        }
        return (textureProperties.getValue());
    }

    /**
     * Set the textureProperties field.
     *
     * @param val The X3DNode to set.
     */
    @Override
    public void setTextureProperties(X3DNode val) {
        if (textureProperties == null) {
            textureProperties = (SFNode) getField("textureProperties");
        }
        textureProperties.setValue(val);
    }

    /**
     * Return the repeatS boolean value.
     *
     * @return The repeatS boolean value.
     */
    @Override
    public boolean getRepeatS() {
        if (repeatS == null) {
            repeatS = (SFBool) getField("repeatS");
        }
        return (repeatS.getValue());
    }

    /**
     * Set the repeatS field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setRepeatS(boolean val) {
        if (repeatS == null) {
            repeatS = (SFBool) getField("repeatS");
        }
        repeatS.setValue(val);
    }

    /**
     * Return the repeatT boolean value.
     *
     * @return The repeatT boolean value.
     */
    @Override
    public boolean getRepeatT() {
        if (repeatT == null) {
            repeatT = (SFBool) getField("repeatT");
        }
        return (repeatT.getValue());
    }

    /**
     * Set the repeatT field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setRepeatT(boolean val) {
        if (repeatT == null) {
            repeatT = (SFBool) getField("repeatT");
        }
        repeatT.setValue(val);
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
}
