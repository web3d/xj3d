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

package org.web3d.vrml.renderer.common.nodes.extensions;

// External imports
import java.util.HashMap;
import java.util.Map;

import org.j3d.device.input.*;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;

/**
 * Common base implementation of a GamepadSensor
 * <p>
 *
 * This node is an custom extension to Xj3D node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class BaseGamepadSensor extends BaseHIDSensor {

    /** Index of the rumblerX field */
    private static final int FIELD_RUMBLER_X = LAST_HIDSENSOR_INDEX + 1;

    /** Index of the rumblerY field */
    private static final int FIELD_RUMBLER_Y = LAST_HIDSENSOR_INDEX + 2;

    /** Index of the l1Button field */
    private static final int FIELD_L1_BUTTON = LAST_HIDSENSOR_INDEX + 3;

    /** Index of the r1Button field */
    private static final int FIELD_R1_BUTTON = LAST_HIDSENSOR_INDEX + 4;

    /** Index of the leftHatX field */
    private static final int FIELD_LEFT_HAT_X = LAST_HIDSENSOR_INDEX + 5;

    /** Index of the leftHatY field */
    private static final int FIELD_LEFT_HAT_Y = LAST_HIDSENSOR_INDEX + 6;

    /** Index of the rightStickX field */
    private static final int FIELD_RIGHT_STICK_X = LAST_HIDSENSOR_INDEX + 7;

    /** Index of the rightStickY field */
    private static final int FIELD_RIGHT_STICK_Y = LAST_HIDSENSOR_INDEX + 8;

    /** Index of the startButton field */
    private static final int FIELD_START_BUTTON = LAST_HIDSENSOR_INDEX + 9;

    /** Index of the throttleSlider field */
    private static final int FIELD_THROTTLE_SLIDER = LAST_HIDSENSOR_INDEX + 10;

    /** Index of the featuresAvailable field */
    private static final int FIELD_FEATURES_AVAILABLE = LAST_HIDSENSOR_INDEX + 11;

    /** Index of the leftStickX field */
    private static final int FIELD_LEFT_STICK_X = LAST_HIDSENSOR_INDEX + 12;

    /** Index of the leftStickY field */
    private static final int FIELD_LEFT_STICK_Y = LAST_HIDSENSOR_INDEX + 13;

    /** The last field index used by this class */
    private static final int LAST_GAMEPAD_SENSOR_INDEX = FIELD_LEFT_STICK_Y;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_GAMEPAD_SENSOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /** The featuresAvailable field */
    private String[] vfFeaturesAvailable;

    /** The rumblerX eventOut */
    private float vfRumblerX;

    /** The rumblerY eventOut */
    private float vfRumblerY;

    /** The leftStickX eventOut */
    private float vfLeftStickX;

    /** The leftStickY eventOut */
    private float vfLeftStickY;

    /** The leftHatX eventOut */
    private float vfLeftHatX;

    /** The leftHatY eventOut */
    private float vfLeftHatY;

    /** The rightStickX eventOut */
    private float vfRightStickX;

    /** The rightStickY eventOut */
    private float vfRightStickY;

    /** The l1Button eventOut */
    private boolean vfL1Button;

    /** The r1Button eventOut */
    private boolean vfR1Button;

    /** The startButton eventOut */
    private boolean vfStartButton;

    /** The throttleSlider eventOut */
    private float vfThrottleSlider;

    /** The real device backing this node, could be null */
    private GamepadDevice realdevice;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");

        fieldDecl[FIELD_NAME] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "name");

        fieldDecl[FIELD_IS_ACTIVE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isActive");

        fieldDecl[FIELD_AXIS_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisValue");

        fieldDecl[FIELD_AXIS_MIN_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisMinValue");

        fieldDecl[FIELD_AXIS_MAX_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisMaxValue");

        fieldDecl[FIELD_AXIS_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "axisName");

        fieldDecl[FIELD_AXIS_RESOLUTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "axisResolution");

        fieldDecl[FIELD_AXIS_WRAP] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFBool",
                                     "axisWrap");

        fieldDecl[FIELD_OUTPUT_MIN_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputMinValue");

        fieldDecl[FIELD_OUTPUT_MAX_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputMaxValue");

        fieldDecl[FIELD_OUTPUT_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "outputName");

        fieldDecl[FIELD_OUTPUT_RESOLUTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFFloat",
                                     "outputResolution");

        fieldDecl[FIELD_OUTPUT_WRAP] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFBool",
                                     "outputWrap");

        fieldDecl[FIELD_NUM_AXES] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "numAxes");

        fieldDecl[FIELD_NUM_OUTPUTS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "numOutputs");

        fieldDecl[FIELD_MANUFACTURER_NAME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFString",
                                     "manufacturerName");

        // Convenvience fields

        fieldDecl[FIELD_RUMBLER_X] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "rumblerX");

        fieldDecl[FIELD_RUMBLER_Y] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "rumblerY");

        fieldDecl[FIELD_FEATURES_AVAILABLE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFString",
                                     "featuresAvailable");

        fieldDecl[FIELD_LEFT_STICK_X] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "leftStickX");

        fieldDecl[FIELD_LEFT_STICK_Y] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "leftStickY");

        fieldDecl[FIELD_LEFT_HAT_X] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "leftHatX");

        fieldDecl[FIELD_LEFT_HAT_Y] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "leftHatY");

        fieldDecl[FIELD_RIGHT_STICK_X] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "rightStickX");

        fieldDecl[FIELD_RIGHT_STICK_Y] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "rightStickY");

        fieldDecl[FIELD_L1_BUTTON] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "l1Button");

        fieldDecl[FIELD_R1_BUTTON] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "r1Button");

        fieldDecl[FIELD_START_BUTTON] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "startButton");

        fieldDecl[FIELD_THROTTLE_SLIDER] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFFloat",
                                     "throttleSlider");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_NAME;
        fieldMap.put("name", idx);

        idx = FIELD_ENABLED;
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = FIELD_IS_ACTIVE;
        fieldMap.put("isActive", idx);
        fieldMap.put("isActive_changed", idx);

        idx = FIELD_OUTPUT_VALUE;
        fieldMap.put("outputValue", idx);
        fieldMap.put("outputValue_changed", idx);

        idx = FIELD_AXIS_VALUE;
        fieldMap.put("axisValue", idx);
        fieldMap.put("axisValue_changed", idx);

        idx = FIELD_AXIS_MIN_VALUE;
        fieldMap.put("axisMinValue", idx);
        fieldMap.put("axisMinValue_changed", idx);

        idx = FIELD_AXIS_MAX_VALUE;
        fieldMap.put("axisMaxValue", idx);
        fieldMap.put("axisMaxValue_changed", idx);

        idx = FIELD_AXIS_NAME;
        fieldMap.put("axisName", idx);
        fieldMap.put("axisName_changed", idx);

        idx = FIELD_AXIS_RESOLUTION;
        fieldMap.put("axisResolution", idx);
        fieldMap.put("axisResolution_changed", idx);

        idx = FIELD_AXIS_WRAP;
        fieldMap.put("axisWrap", idx);
        fieldMap.put("axisWrap_changed", idx);

        idx = FIELD_OUTPUT_MIN_VALUE;
        fieldMap.put("outputMinValue", idx);
        fieldMap.put("outputMinValue_changed", idx);

        idx = FIELD_OUTPUT_MAX_VALUE;
        fieldMap.put("outputMaxValue", idx);
        fieldMap.put("outputMaxValue_changed", idx);

        idx = FIELD_OUTPUT_NAME;
        fieldMap.put("outputName", idx);
        fieldMap.put("outputName_changed", idx);

        idx = FIELD_OUTPUT_RESOLUTION;
        fieldMap.put("outputResolution", idx);
        fieldMap.put("outputResolution_changed", idx);

        idx = FIELD_OUTPUT_WRAP;
        fieldMap.put("outputWrap", idx);
        fieldMap.put("outputWrap_changed", idx);

        idx = FIELD_NUM_AXES;
        fieldMap.put("numAxes", idx);
        fieldMap.put("numAxes_changed", idx);

        idx = FIELD_NUM_OUTPUTS;
        fieldMap.put("numOutputs", idx);
        fieldMap.put("numOutputs_changed", idx);

        idx = FIELD_FEATURES_AVAILABLE;
        fieldMap.put("featuresAvailable", idx);
        fieldMap.put("featuresAvailable_changed", idx);

        idx = FIELD_MANUFACTURER_NAME;
        fieldMap.put("manufacturerName", idx);
        fieldMap.put("manufacturerName_changed", idx);

        // Convience Fields
        idx = FIELD_LEFT_STICK_X;
        fieldMap.put("leftStickX", idx);
        fieldMap.put("leftStickX_changed", idx);

        idx = FIELD_LEFT_STICK_Y;
        fieldMap.put("leftStickY", idx);
        fieldMap.put("leftStickY_changed", idx);

        idx = FIELD_RIGHT_STICK_X;
        fieldMap.put("rightStickX", idx);
        fieldMap.put("rightStickX_changed", idx);

        idx = FIELD_RIGHT_STICK_Y;
        fieldMap.put("rightStickY", idx);
        fieldMap.put("rightStickY_changed", idx);

        idx = FIELD_LEFT_HAT_X;
        fieldMap.put("leftHatX", idx);
        fieldMap.put("leftHatX_changed", idx);

        idx = FIELD_LEFT_HAT_Y;
        fieldMap.put("leftHatY", idx);
        fieldMap.put("leftHatY_changed", idx);

        idx = FIELD_RUMBLER_X;
        fieldMap.put("rumblerX", idx);
        fieldMap.put("rumblerX_changed", idx);

        idx = FIELD_RUMBLER_Y;
        fieldMap.put("rumblerY", idx);
        fieldMap.put("rumblerY_changed", idx);

        idx = FIELD_L1_BUTTON;
        fieldMap.put("l1Button", idx);
        fieldMap.put("l1Button_changed", idx);

        idx = FIELD_R1_BUTTON;
        fieldMap.put("r1Button", idx);
        fieldMap.put("r1Button_changed", idx);

        idx = FIELD_START_BUTTON;
        fieldMap.put("startButton", idx);
        fieldMap.put("startButton_changed", idx);

        idx = FIELD_THROTTLE_SLIDER;
        fieldMap.put("throttleSlider", idx);
        fieldMap.put("throttleSlider_changed", idx);

    }

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public BaseGamepadSensor() {
        super("GamepadSensor");

        hasChanged = new boolean[NUM_FIELDS];
        vfFeaturesAvailable = FieldConstants.EMPTY_MFSTRING;
        vfRumblerX = 0;
        vfRumblerY = 0;
        vfLeftStickX  = 0;
        vfLeftStickY = 0;
        vfLeftHatX = 0;
        vfLeftHatY = 0;
        vfRightStickX  = 0;
        vfRightStickY  = 0;
        vfL1Button = false;
        vfR1Button = false;
        vfStartButton = false;
        vfThrottleSlider = -1f;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseGamepadSensor(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy(node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLDeviceNodeType
    //----------------------------------------------------------

    /**
     * Set the real device backing this node.  This will not be called
     * if the device name does not map to a live device.
     *
     * @param device The real device.
     */
    @Override
    public void setDevice(InputDevice device) {
        realdevice = (GamepadDevice) device;
    }

    /**
     * Update this nodes field from the underlying device.
     *
     * @param state The device state.
     */
    @Override
    public void update(DeviceState state) {
        GamepadState gpState = (GamepadState) state;

        if (gpState.leftStickX_changed) {
            vfLeftStickX = gpState.leftStickX;

            hasChanged[FIELD_LEFT_STICK_X] = true;
            fireFieldChanged(FIELD_LEFT_STICK_X);
        }

        if (gpState.leftStickY_changed) {
            vfLeftStickY = gpState.leftStickY;

            hasChanged[FIELD_LEFT_STICK_Y] = true;
            fireFieldChanged(FIELD_LEFT_STICK_Y);
        }

        if (gpState.rightStickX_changed) {
            vfRightStickX = gpState.rightStickX;

            hasChanged[FIELD_RIGHT_STICK_X] = true;
            fireFieldChanged(FIELD_RIGHT_STICK_X);
        }

        if (gpState.rightStickY_changed) {
            vfRightStickY = gpState.rightStickY;

            hasChanged[FIELD_RIGHT_STICK_Y] = true;
            fireFieldChanged(FIELD_RIGHT_STICK_Y);
        }

        if (gpState.leftHatX_changed) {
            vfLeftHatX = gpState.leftHatX;

            hasChanged[FIELD_LEFT_HAT_X] = true;
            fireFieldChanged(FIELD_LEFT_HAT_X);
        }

        if (gpState.leftHatY_changed) {
            vfLeftHatY = gpState.leftHatY;

            hasChanged[FIELD_LEFT_HAT_Y] = true;
            fireFieldChanged(FIELD_LEFT_HAT_Y);
        }

        if (gpState.l1Button_changed) {
            vfL1Button = gpState.l1Button;

            hasChanged[FIELD_L1_BUTTON] = true;
            fireFieldChanged(FIELD_L1_BUTTON);
        }

        if (gpState.r1Button_changed) {
            vfR1Button = gpState.r1Button;

            hasChanged[FIELD_R1_BUTTON] = true;
            fireFieldChanged(FIELD_R1_BUTTON);
        }

        if (gpState.startButton_changed) {
            vfStartButton = gpState.startButton;

            hasChanged[FIELD_START_BUTTON] = true;
            fireFieldChanged(FIELD_START_BUTTON);
        }

        if (gpState.throttleSlider_changed) {
            vfThrottleSlider = gpState.throttleSlider;

            hasChanged[FIELD_THROTTLE_SLIDER] = true;
            fireFieldChanged(FIELD_THROTTLE_SLIDER);
        }

    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType.
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
        if(index < 0  || index > LAST_GAMEPAD_SENSOR_INDEX)
            return null;

        return fieldDecl[index];
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
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_FEATURES_AVAILABLE:
                fieldData.clear();
                fieldData.stringArrayValues = vfFeaturesAvailable;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfFeaturesAvailable.length;
                break;

            case FIELD_LEFT_STICK_X:
                fieldData.clear();
                fieldData.floatValue = vfLeftStickX;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LEFT_STICK_Y:
                fieldData.clear();
                fieldData.floatValue = vfLeftStickY;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_RIGHT_STICK_X:
                fieldData.clear();
                fieldData.floatValue = vfRightStickX;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_RIGHT_STICK_Y:
                fieldData.clear();
                fieldData.floatValue = vfRightStickY;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LEFT_HAT_X:
                fieldData.clear();
                fieldData.floatValue = vfLeftHatX;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LEFT_HAT_Y:
                fieldData.clear();
                fieldData.floatValue = vfLeftHatY;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_THROTTLE_SLIDER:
                fieldData.clear();
                fieldData.floatValue = vfThrottleSlider;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_L1_BUTTON:
                fieldData.clear();
                fieldData.booleanValue = vfL1Button;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_R1_BUTTON:
                fieldData.clear();
                fieldData.booleanValue = vfR1Button;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_START_BUTTON:
                fieldData.clear();
                fieldData.booleanValue = vfStartButton;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_LEFT_STICK_X:
                    destNode.setValue(destIndex, vfLeftStickX);
                    break;

                case FIELD_LEFT_STICK_Y:
                    destNode.setValue(destIndex, vfLeftStickY);
                    break;

                case FIELD_LEFT_HAT_X:
                    destNode.setValue(destIndex, vfLeftHatX);
                    break;

                case FIELD_LEFT_HAT_Y:
                    destNode.setValue(destIndex, vfLeftHatY);
                    break;

                case FIELD_RIGHT_STICK_X:
                    destNode.setValue(destIndex, vfRightStickX);
                    break;

                case FIELD_RIGHT_STICK_Y:
                    destNode.setValue(destIndex, vfRightStickY);
                    break;

                case FIELD_THROTTLE_SLIDER:
                    destNode.setValue(destIndex, vfThrottleSlider);
                    break;

                case FIELD_L1_BUTTON:
                    destNode.setValue(destIndex, vfL1Button);
                    break;

                case FIELD_R1_BUTTON:
                    destNode.setValue(destIndex, vfR1Button);
                    break;

                case FIELD_START_BUTTON:
                    destNode.setValue(destIndex, vfStartButton);
                    break;

                case FIELD_FEATURES_AVAILABLE:
                    destNode.setValue(destIndex, vfFeaturesAvailable,
                        vfFeaturesAvailable.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float. This is
     * be used to set SFFloat field types name.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_RUMBLER_X:
                setRumblerX(value);
                break;

            case FIELD_RUMBLER_Y:
                setRumblerY(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the rumblerX value.
     *
     * @param val The X axis value.
     */
    protected void setRumblerX(float val) {
        if (realdevice != null) {
            realdevice.setRumblerX(val);
        }
    }

    /**
     * Set the rumblerY value.
     *
     * @param val The Y axis value.
     */
    protected void setRumblerY(float val) {
        if (realdevice != null) {
            realdevice.setRumblerY(val);
        }
    }
}
