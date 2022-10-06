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
 * Common implementation of an GeoSRFTParametersInfo node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class BaseGeoSRFTemplate extends BaseGeoSRFParametersNode {

    /** Index of the ormCode field */
    protected static final int FIELD_ORM_CODE =
        LAST_SRF_PARAM_INDEX + 1;

    /** Index of the srtfCode field */
    protected static final int FIELD_SRFT_CODE =
        LAST_SRF_PARAM_INDEX + 2;

    /** Index of the srtfParameters field */
    protected static final int FIELD_SRFT_PARAMETERS =
        LAST_SRF_PARAM_INDEX + 3;

    /** The last index of the fields used by the metadata */
    protected static final int LAST_PARAMS_INFO_INDEX =
        FIELD_SRFT_PARAMETERS;

    /** Number of fields constant */
    private static final int NUM_FIELDS=LAST_PARAMS_INFO_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Message for when the proto is not a SrfParameters */
    private static final String SRFT_PARAMS_PROTO_MSG =
        "Proto does not describe a X3DGeoSRFTParametersNode object";

    /** Message for when the node in setValue() is not a SrfParameters */
    private static final String SRFT_PARAMS_NODE_MSG =
        "Node does not describe a X3DGeoSRFTParametersNode object";


    // VRML Field declarations

    /** initializeOnly SFNode srtfParametersInfo  */
    protected VRMLNodeType vfSrfParameters;

    /** Proto version of the srtfParametersInfo node */
    protected VRMLProtoInstance pSrfParameters;

    /** field SFInt32 srftCode */
    protected int vfSrftCode;

    /** field SFInt32 ormCode */
    protected int vfOrmCode;

    /**
     * Initialise all the field declaration values
     */
    static {
        nodeFields = new int[] { FIELD_SRFT_PARAMETERS, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_SRFT_PARAMETERS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "srtfParameters");

        fieldDecl[FIELD_ORM_CODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "ormCode");


        fieldDecl[FIELD_SRFT_CODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "srftCode");


        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("ormCode", FIELD_ORM_CODE);
        fieldMap.put("srftCode", FIELD_SRFT_CODE);
        fieldMap.put("srtfParameters", FIELD_SRFT_PARAMETERS);
    }

    /**
     * Construct a default GeoMetaData instance
     */
    public BaseGeoSRFTemplate() {
        super("GeoSRFTemplate");

        hasChanged = new boolean[NUM_FIELDS];
        vfOrmCode = 250;
        vfSrftCode = 1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoSRFTemplate(VRMLNodeType node) {
        this(); // invoke default constructor
        checkNodeType(node);

        try {
            int index = node.getFieldIndex("ormCode");
            VRMLFieldData field = node.getFieldValue(index);
            vfOrmCode = field.intValue;

            index = node.getFieldIndex("srftCode");
            field = node.getFieldValue(index);
            vfSrftCode = field.intValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }

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
           case FIELD_ORM_CODE:
                fieldData.clear();
                fieldData.intValue = vfOrmCode;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_SRFT_CODE:
                fieldData.clear();
                fieldData.intValue = vfSrftCode;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_SRFT_PARAMETERS:
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
     * Set the value of the field at the given index as an integer.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ORM_CODE:
                if(!inSetup)
                    throwInitOnlyWriteException("ormCode");

                vfOrmCode = value;
                break;

            case FIELD_SRFT_CODE:
                if(!inSetup)
                    throwInitOnlyWriteException("srftCode");

                vfSrftCode = value;
                break;

            default:
                super.setValue(index, value);
        }
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
            case FIELD_SRFT_PARAMETERS:
                setSrftParams(child);
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
    private void setSrftParams(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throwInitOnlyWriteException("srtfParameters");

        BaseGeoSRFTParametersNode node;
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

            if((impl != null) && !(impl instanceof BaseGeoSRFTParametersNode))
                throw new InvalidFieldValueException(SRFT_PARAMS_PROTO_MSG);

            node = (BaseGeoSRFTParametersNode)impl;
            pSrfParameters = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoSRFTParametersNode)) {
            throw new InvalidFieldValueException(SRFT_PARAMS_NODE_MSG);
        } else {
            pSrfParameters = null;
            node = (BaseGeoSRFTParametersNode)geo;
        }

        vfSrfParameters = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }
}
