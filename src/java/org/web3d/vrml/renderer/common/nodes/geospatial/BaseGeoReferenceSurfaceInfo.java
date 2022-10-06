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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of an GeoSRFParametersInfo node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class BaseGeoReferenceSurfaceInfo extends AbstractNode
    implements VRMLChildNodeType  {

    /** Index of the dssCode field */
    protected static final int FIELD_DSS_CODE = LAST_NODE_INDEX + 1;

    /** Index of the name field */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 2;

    /** Index of the srfParametersInfo field */
    protected static final int FIELD_SRF_PARAMETERS_INFO =
        LAST_NODE_INDEX + 3;

    /** The last index of the fields used by the metadata */
    protected static final int LAST_PARAMS_INFO_INDEX =
        FIELD_SRF_PARAMETERS_INFO;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_PARAMS_INFO_INDEX + 1;

    /**
     * Error message when the user tries to override one of the standard
     * GeoSystem names with the name field of this node.
     */
    private static final String INVALID_GEOSYS_MSG =
        "Attempting to redefine one of the standard SRFs is not permitted: ";

    /** The standard names that are not allowed to be used for this node */
    private static final Set<String> STANDARD_GEOSYSTEMS;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Message for when the proto is not a SrfParametersInfo */
    private static final String SRF_PARAMS_PROTO_MSG =
        "Proto does not describe a X3DGeoSRFParametersNode object";

    /** Message for when the node in setValue() is not a SrfParametersInfo */
    private static final String SRF_PARAMS_NODE_MSG =
        "Node does not describe a X3DGeoSRFParametersNode object";


    // VRML Field declarations

    /** initializeOnly SFNode srfParametersInfo  */
    protected VRMLNodeType vfSrfParametersInfo;

    /** Proto version of the srfParametersInfo node */
    protected VRMLProtoInstance pSrfParametersInfo;

    /** initializeOnly SFInt32 dssCode 0 */
    protected int vfDssCode;

    /** initializeOnly SFString name "" */
    protected String vfName;

    /**
     * Initialise all the field declaration values
     */
    static {
        nodeFields = new int[] { FIELD_SRF_PARAMETERS_INFO, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_DSS_CODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "dssCode");

        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "name");

        fieldDecl[FIELD_SRF_PARAMETERS_INFO] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "srfParametersInfo");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("dssCode", FIELD_DSS_CODE);
        fieldMap.put("name", FIELD_NAME);
        fieldMap.put("srfParametersInfo", FIELD_SRF_PARAMETERS_INFO);

        STANDARD_GEOSYSTEMS = new HashSet<>();
        STANDARD_GEOSYSTEMS.add("GD");
        STANDARD_GEOSYSTEMS.add("GC");
        STANDARD_GEOSYSTEMS.add("UTM");
    }

    /**
     * Construct a default GeoMetaData instance
     */
    public BaseGeoReferenceSurfaceInfo() {
        super("GeoReferenceSurfaceInfo");

        hasChanged = new boolean[NUM_FIELDS];

        vfName = "";
        vfDssCode = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoReferenceSurfaceInfo(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);
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

        if(pSrfParametersInfo != null)
            pSrfParametersInfo.setupFinished();
        else if(vfSrfParametersInfo != null)
            vfSrfParametersInfo.setupFinished();
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
        return TypeConstants.ChildNodeType;
    }


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
            case FIELD_DSS_CODE:
                fieldData.clear();
                fieldData.intValue = vfDssCode;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_NAME:
                fieldData.clear();
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_SRF_PARAMETERS_INFO:
                fieldData.clear();
                if(pSrfParametersInfo != null)
                    fieldData.nodeValue = pSrfParametersInfo;
                else
                    fieldData.nodeValue = vfSrfParametersInfo;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as a int. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DSS_CODE:
                if(!inSetup)
                    throwInitOnlyWriteException("dssCode");

                vfDssCode = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_NAME:
                if(!inSetup)
                    throwInitOnlyWriteException("name");

                if(STANDARD_GEOSYSTEMS.contains(value))
                    throw new InvalidFieldValueException(INVALID_GEOSYS_MSG +
                                                         value);

                vfName = value;
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
            case FIELD_SRF_PARAMETERS_INFO:
                if(!inSetup)
                    throwInitOnlyWriteException("srfParametersInfo");

                setSrfParamsInfo(child);
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
    private void setSrfParamsInfo(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        BaseGeoSRFParametersInfoNode node;
        VRMLNodeType old_node;

        if(pSrfParametersInfo != null)
            old_node = pSrfParametersInfo;
        else
            old_node = vfSrfParametersInfo;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoSRFParametersInfoNode))
                throw new InvalidFieldValueException(SRF_PARAMS_PROTO_MSG);

            node = (BaseGeoSRFParametersInfoNode)impl;
            pSrfParametersInfo = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoSRFParametersInfoNode)) {
            throw new InvalidFieldValueException(SRF_PARAMS_NODE_MSG);
        } else {
            pSrfParametersInfo = null;
            node = (BaseGeoSRFParametersInfoNode)geo;
        }

        vfSrfParametersInfo = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);
    }
}
