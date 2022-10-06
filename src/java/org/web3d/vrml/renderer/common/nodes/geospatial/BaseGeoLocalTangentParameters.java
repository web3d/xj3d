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
 * Common implementation of an GeoLocalTangentParameters node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BaseGeoLocalTangentParameters extends BaseGeoSRFTParametersNode {

    /** Index of the azimuth field */
    protected static final int FIELD_AZIMUTH =
        LAST_SRFT_PARAM_INDEX + 1;

    /** Index of the geodeticLatitude field */
    protected static final int FIELD_GEODETIC_LATITUDE =
        LAST_SRFT_PARAM_INDEX + 2;

    /** Index of the geodeticLongitude field */
    protected static final int FIELD_GEODETIC_LONGITUDE =
        LAST_SRFT_PARAM_INDEX + 3;

    /** Index of the xFalseOrigin field */
    protected static final int FIELD_X_FALSE_ORIGIN =
        LAST_SRFT_PARAM_INDEX + 4;

    /** Index of the yFalseOrigin field */
    protected static final int FIELD_Y_FALSE_ORIGIN =
        LAST_SRFT_PARAM_INDEX + 5;

    /** Index of the heightOffset field */
    protected static final int FIELD_HEIGHT_OFFSET =
        LAST_SRFT_PARAM_INDEX + 6;

    /** The last index of the nodes used by the GeoLocalTangentarameters */
    protected static final int LAST_TMPARAMS_INDEX = FIELD_HEIGHT_OFFSET;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TMPARAMS_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** initializeOnly SFDouble azimuth 0 */
    protected double vfAzimuth;

    /** initializeOnly SFDouble geodeticLatitude 0 */
    protected double vfGeodeticLatitude;

    /** initializeOnly SFDouble geodeticLongitude 0 */
    protected double vfGeodeticLongitude;

    /** initializeOnly SFDouble xFalseOrigin 0 */
    protected double vfXFalseOrigin;

    /** initializeOnly SFDouble yFalseOrigin 0 */
    protected double vfYFalseOrigin;

    /** initializeOnly SFDouble heightOffset 0 */
    protected double vfHeightOrigin;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");

        fieldDecl[FIELD_AZIMUTH] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "azimuth");

        fieldDecl[FIELD_GEODETIC_LATITUDE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "geodeticLatitude");

        fieldDecl[FIELD_GEODETIC_LONGITUDE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "geodeticLongitude");

        fieldDecl[FIELD_X_FALSE_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "xFalseOrigin");

        fieldDecl[FIELD_Y_FALSE_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "yFalseOrigin");

        fieldDecl[FIELD_HEIGHT_OFFSET] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "heightOffset");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("azimuth", FIELD_AZIMUTH);
        fieldMap.put("geodeticLatitude", FIELD_GEODETIC_LATITUDE);
        fieldMap.put("geodeticLongitude", FIELD_GEODETIC_LONGITUDE);
        fieldMap.put("xFalseOrigin", FIELD_X_FALSE_ORIGIN);
        fieldMap.put("yFalseOrigin", FIELD_Y_FALSE_ORIGIN);
        fieldMap.put("heightOffset", FIELD_HEIGHT_OFFSET);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * X3D specification.
     */
    public BaseGeoLocalTangentParameters() {
        super("GeoLocalTangentParameters");

        hasChanged = new boolean[NUM_FIELDS];

        vfAzimuth = 0;
        vfGeodeticLatitude = 0;
        vfGeodeticLongitude = 0;
        vfXFalseOrigin = 0;
        vfYFalseOrigin = 0;
        vfHeightOrigin = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoLocalTangentParameters(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("azimuth");
            VRMLFieldData field = node.getFieldValue(index);
            vfAzimuth = field.doubleValue;

            index = node.getFieldIndex("geodeticLatitude");
            field = node.getFieldValue(index);
            vfGeodeticLatitude = field.doubleValue;

            index = node.getFieldIndex("geodeticLongitude");
            field = node.getFieldValue(index);
            vfGeodeticLongitude = field.doubleValue;

            index = node.getFieldIndex("xFalseOrigin");
            field = node.getFieldValue(index);
            vfXFalseOrigin = field.doubleValue;

            index = node.getFieldIndex("yFalseOrigin");
            field = node.getFieldValue(index);
            vfYFalseOrigin = field.doubleValue;

            index = node.getFieldIndex("heightOffset");
            field = node.getFieldValue(index);
            vfHeightOrigin = field.doubleValue;

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
            case FIELD_AZIMUTH:
                fieldData.clear();
                fieldData.doubleValue = vfAzimuth;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_GEODETIC_LATITUDE:
                fieldData.clear();
                fieldData.doubleValue = vfGeodeticLatitude;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_GEODETIC_LONGITUDE:
                fieldData.clear();
                fieldData.doubleValue = vfGeodeticLongitude;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_X_FALSE_ORIGIN:
                fieldData.clear();
                fieldData.doubleValue = vfXFalseOrigin;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_Y_FALSE_ORIGIN:
                fieldData.clear();
                fieldData.doubleValue = vfYFalseOrigin;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_HEIGHT_OFFSET:
                fieldData.clear();
                fieldData.doubleValue = vfHeightOrigin;
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
            case FIELD_AZIMUTH:
                if(!inSetup)
                    throwInitOnlyWriteException("azimuth");

                vfAzimuth = value;
                break;

            case FIELD_GEODETIC_LATITUDE:
                if(!inSetup)
                    throwInitOnlyWriteException("geodeticLatitude");

                vfGeodeticLatitude = value;
                break;

            case FIELD_GEODETIC_LONGITUDE:
                if(!inSetup)
                    throwInitOnlyWriteException("geodeticLongitude");

                vfGeodeticLongitude = value;
                break;

            case FIELD_X_FALSE_ORIGIN:
                if(!inSetup)
                    throwInitOnlyWriteException("xFalseOrigin");

                vfXFalseOrigin = value;
                break;

            case FIELD_Y_FALSE_ORIGIN:
                if(!inSetup)
                    throwInitOnlyWriteException("yFalseOrigin");

                vfYFalseOrigin = value;
                break;

            case FIELD_HEIGHT_OFFSET:
                if(!inSetup)
                    throwInitOnlyWriteException("heightOffset");

                vfHeightOrigin = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}

