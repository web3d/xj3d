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
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

/**
 * Common implementation of an GeoSRFParametersInfo node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class BaseGeoSRFParametersInfo extends BaseGeoSRFParametersInfoNode {

    /** Index of the srfParameters field */
    protected static final int FIELD_SRF_PARAMETERS =
        LAST_SRF_PARAM_INFO_INDEX + 1;

    /** The last index of the fields used by the metadata */
    protected static final int LAST_PARAMS_INFO_INDEX =
        FIELD_SRF_PARAMETERS;

    /** Number of fields constant */
    private static final int NUM_FIELDS=LAST_PARAMS_INFO_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Message for when the proto is not a SrfParameters */
    private static final String SRF_PARAMS_PROTO_MSG =
        "Proto does not describe a X3DGeoSRFParametersNode object";

    /** Message for when the node in setValue() is not a SrfParameters */
    private static final String SRF_PARAMS_NODE_MSG =
        "Node does not describe a X3DGeoSRFParametersNode object";

    // VRML Field declarations

    /** initializeOnly SFNode srfParametersInfo  */
    protected VRMLNodeType vfSrfParameters;

    /** Proto version of the srfParametersInfo node */
    protected VRMLProtoInstance pSrfParameters;

    /**
     * Initialise all the field declaration values
     */
    static {
        nodeFields = new int[] { FIELD_SRF_PARAMETERS, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_SRF_PARAMETERS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "srfParameters");

        fieldDecl[FIELD_RTCODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "rtCode");


        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("rtCode", FIELD_RTCODE);
        fieldMap.put("srfParameters", FIELD_SRF_PARAMETERS);
    }

    /**
     * Construct a default GeoMetaData instance
     */
    public BaseGeoSRFParametersInfo() {
        super("GeoSRFParametersInfo");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoSRFParametersInfo(VRMLNodeType node) {
        this(); // invoke default constructor

        copy((BaseGeoSRFParametersInfoNode)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pSrfParameters != null)
            pSrfParameters.setupFinished();
        else if(vfSrfParameters != null)
            vfSrfParameters.setupFinished();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        return (index < 0 || index > LAST_PARAMS_INFO_INDEX) ?
            null : fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return fieldDecl.length;
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
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_SRF_PARAMETERS:
                fieldData.clear();
                if(pSrfParameters != null)
                    fieldData.nodeValue = pSrfParameters;
                else
                    fieldData.nodeValue = vfSrfParameters;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SRF_PARAMETERS:
	        if(!inSetup)
	            throwInitOnlyWriteException("srfParameters");

                setSrfParams(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setSrfParams(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        BaseGeoSRFParametersNode node;
        VRMLNodeType old_node;

        if(pSrfParameters != null)
            old_node = pSrfParameters;
        else
            old_node = vfSrfParameters;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoSRFParametersNode))
                throw new InvalidFieldValueException(SRF_PARAMS_PROTO_MSG);

            node = (BaseGeoSRFParametersNode)impl;
            pSrfParameters = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoSRFParametersNode)) {
            throw new InvalidFieldValueException(SRF_PARAMS_NODE_MSG);
        } else {
            pSrfParameters = null;
            node = (BaseGeoSRFParametersNode)geo;
        }

        vfSrfParameters = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }
}
