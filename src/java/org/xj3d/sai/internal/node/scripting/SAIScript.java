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

package org.xj3d.sai.internal.node.scripting;

import java.lang.ref.ReferenceQueue;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.sai.BaseNode;
import org.web3d.vrml.scripting.sai.BaseNodeFactory;
import org.web3d.vrml.scripting.sai.FieldAccessListener;
import org.web3d.vrml.scripting.sai.FieldFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.scripting.Script;

/**
 * A concrete implementation of the Script node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIScript extends BaseNode implements Script {

    /**
     * The url inputOutput field
     */
    private MFString url;

    /**
     * The mustEvaluate initializeOnly field
     */
    private SFBool mustEvaluate;

    /**
     * The directOutput initializeOnly field
     */
    private SFBool directOutput;

    /**
     * Constructor
     * @param bnf
     */
    public SAIScript(
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
     * Return the mustEvaluate boolean value.
     *
     * @return The mustEvaluate boolean value.
     */
    @Override
    public boolean getMustEvaluate() {
        if (mustEvaluate == null) {
            mustEvaluate = (SFBool) getField("mustEvaluate");
        }
        return (mustEvaluate.getValue());
    }

    /**
     * Set the mustEvaluate field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setMustEvaluate(boolean val) {
        if (mustEvaluate == null) {
            mustEvaluate = (SFBool) getField("mustEvaluate");
        }
        mustEvaluate.setValue(val);
    }

    /**
     * Return the directOutput boolean value.
     *
     * @return The directOutput boolean value.
     */
    @Override
    public boolean getDirectOutput() {
        if (directOutput == null) {
            directOutput = (SFBool) getField("directOutput");
        }
        return (directOutput.getValue());
    }

    /**
     * Set the directOutput field.
     *
     * @param val The boolean to set.
     */
    @Override
    public void setDirectOutput(boolean val) {
        if (directOutput == null) {
            directOutput = (SFBool) getField("directOutput");
        }
        directOutput.setValue(val);
    }
}
