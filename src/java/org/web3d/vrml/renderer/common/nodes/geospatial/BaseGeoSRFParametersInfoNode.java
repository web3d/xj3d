/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of the abstract node type X3DSRFParametersInfoNode node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class BaseGeoSRFParametersInfoNode extends AbstractNode {

    /** Index of the rtCode field */
    protected static final int FIELD_RTCODE = LAST_NODE_INDEX + 1;

    /** The last index of the nodes used by the SRFParametersInfoNode */
    protected static final int LAST_SRF_PARAM_INFO_INDEX = FIELD_RTCODE;

    // VRML Field declarations

    /** field SFInt32 rtCode */
    protected int vfRtCode;

    /**
     * Construct a default instance of this node type. The defaults are set by the
     * X3D specification.
     *
     * @param name The name of the type of node
     */
    protected BaseGeoSRFParametersInfoNode(String name) {
        super(name);

        vfRtCode = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected void copy(BaseGeoSRFParametersInfoNode node) {

        vfRtCode = node.getRtCode();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.GeoSRFParamInfoNodeType;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_RTCODE:
                fieldData.clear();
                fieldData.intValue = vfRtCode;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a single int value.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_RTCODE:
                if(!inSetup)
                    throwInitOnlyWriteException("rtCode");

                vfRtCode = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the value of the rtCode field. The value is specified by section
     * 11.2.7.6 of ISO/IEC 18026
     *
     * @return A value RT code value.
     */
    public int getRtCode() {
        return vfRtCode;
    }
}
