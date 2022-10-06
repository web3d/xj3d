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
 * Common implementation of an GeoTMParameters node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BaseGeoTMParameters extends BaseGeoSRFTParametersNode {

    /** Index of the centralScale field */
    protected static final int FIELD_CENTRAL_SCALE =
        LAST_SRFT_PARAM_INDEX + 1;

    /** Index of the falseEasting field */
    protected static final int FIELD_FALSE_EASTING =
        LAST_SRFT_PARAM_INDEX + 2;

    /** Index of the falseNorthing field */
    protected static final int FIELD_FALSE_NORTHING =
        LAST_SRFT_PARAM_INDEX + 3;

    /** Index of the originLongitude field */
    protected static final int FIELD_ORIGIN_LONGITUDE =
        LAST_SRFT_PARAM_INDEX + 4;

    /** Index of the originLaitude field */
    protected static final int FIELD_ORIGIN_LATITUDE =
        LAST_SRFT_PARAM_INDEX + 5;

    /** The last index of the nodes used by the GeoTMParameters */
    protected static final int LAST_TMPARAMS_INDEX = FIELD_ORIGIN_LATITUDE;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TMPARAMS_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** initializeOnly SFDouble centralScale 0 */
    protected double vfCentralScale;

    /** initializeOnly SFDouble falseEasting 0 */
    protected double vfFalseEasting;

    /** initializeOnly SFDouble falseNorthing 0 */
    protected double vfFalseNorthing;

    /** initializeOnly SFDouble originLongitude 0 */
    protected double vfOriginLongitude;

    /** initializeOnly SFDouble originLatitude 0 */
    protected double vfOriginLatitude;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_CENTRAL_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "centralScale");

        fieldDecl[FIELD_FALSE_EASTING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "falseEasting");

        fieldDecl[FIELD_FALSE_NORTHING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "falseNorthing");

        fieldDecl[FIELD_ORIGIN_LONGITUDE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "originLongitude");

        fieldDecl[FIELD_ORIGIN_LATITUDE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "originLatitude");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("centralScale", FIELD_CENTRAL_SCALE);
        fieldMap.put("falseEasting", FIELD_FALSE_EASTING);
        fieldMap.put("falseNorthing", FIELD_FALSE_NORTHING);
        fieldMap.put("originLongitude", FIELD_ORIGIN_LONGITUDE);
        fieldMap.put("originLatitude", FIELD_ORIGIN_LATITUDE);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoTMParameters() {
        super("GeoTMParameters");

        hasChanged = new boolean[NUM_FIELDS];

        vfCentralScale = 0;
        vfFalseEasting = 0;
        vfFalseNorthing = 0;
        vfOriginLongitude = 0;
        vfOriginLatitude = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoTMParameters(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("centralScale");
            VRMLFieldData field = node.getFieldValue(index);
            vfCentralScale = field.doubleValue;

            index = node.getFieldIndex("falseEasting");
            field = node.getFieldValue(index);
            vfFalseEasting = field.doubleValue;

            index = node.getFieldIndex("falseNorthing");
            field = node.getFieldValue(index);
            vfFalseNorthing = field.doubleValue;

            index = node.getFieldIndex("originLongitude");
            field = node.getFieldValue(index);
            vfOriginLongitude = field.doubleValue;

            index = node.getFieldIndex("originLatitude");
            field = node.getFieldValue(index);
            vfOriginLatitude = field.doubleValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
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
        return (index < 0 || index > LAST_TMPARAMS_INDEX) ?
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
            case FIELD_CENTRAL_SCALE:
                fieldData.clear();
                fieldData.doubleValue = vfCentralScale;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_FALSE_EASTING:
                fieldData.clear();
                fieldData.doubleValue = vfFalseEasting;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_FALSE_NORTHING:
                fieldData.clear();
                fieldData.doubleValue = vfFalseNorthing;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_ORIGIN_LONGITUDE:
                fieldData.clear();
                fieldData.doubleValue = vfOriginLongitude;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_ORIGIN_LATITUDE:
                fieldData.clear();
                fieldData.doubleValue = vfOriginLatitude;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an double.
     * This would be used to set SFDouble or SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTRAL_SCALE:
                if(!inSetup)
                    throwInitOnlyWriteException("centralScale");

                vfCentralScale = value;
                break;

            case FIELD_FALSE_EASTING:
                if(!inSetup)
                    throwInitOnlyWriteException("falseEasting");

                vfFalseEasting = value;
                break;

            case FIELD_FALSE_NORTHING:
                if(!inSetup)
                    throwInitOnlyWriteException("falseNorthing");

                vfFalseNorthing = value;
                break;

            case FIELD_ORIGIN_LONGITUDE:
                if(!inSetup)
                    throwInitOnlyWriteException("originLongitude");

                vfOriginLongitude = value;
                break;

            case FIELD_ORIGIN_LATITUDE:
                if(!inSetup)
                    throwInitOnlyWriteException("originLatitude");

                vfOriginLatitude = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}

