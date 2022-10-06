/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.time;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTimeDependentNode;

/**
 * Base implementation of a custom TimeSensor 'like' node. The TimeController is
 * designed to be used as the basis for VCR type controls.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class BaseTimeController extends BaseTimeDependentNode
    implements VRMLSensorNodeType, VRMLTimeControlledNodeType, VRMLTimeListener {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.TimeControlledNodeType };

    // Field index constants

    /** The field index for finalTime */
    protected static final int FIELD_DIRECTION = LAST_NODE_INDEX + 1;

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 2;

    /** The field index for loop */
    protected static final int FIELD_LOOP = LAST_NODE_INDEX + 3;

    /** The field index for rate */
    protected static final int FIELD_RATE = LAST_NODE_INDEX + 4;

    /** The field index for rate */
    protected static final int FIELD_RUN = LAST_NODE_INDEX + 5;

    /** The field index for currentTime */
    protected static final int FIELD_CURRENT_TIME = LAST_NODE_INDEX + 6;

    /** The field index for startTime */
    protected static final int FIELD_START_TIME = LAST_NODE_INDEX + 7;

    /** The field index for stopTime */
    protected static final int FIELD_STOP_TIME = LAST_NODE_INDEX + 8;

    /** The field index for fraction */
    protected static final int FIELD_FRACTION = LAST_NODE_INDEX + 9;

    /** The field index for isActive */
    protected static final int FIELD_IS_ACTIVE = LAST_NODE_INDEX + 10;

    /** The last field index used by this class */
    protected static final int LAST_TIME_CONTROLLER_INDEX = FIELD_IS_ACTIVE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TIME_CONTROLLER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the direction field */
    protected boolean vfDirection;

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the loop field */
    protected boolean vfLoop;

    /** The value of the rate field */
    protected float vfRate;

    /** The value of the run field */
    protected boolean vfRun;

    /** The value of the currentTime field */
    protected double vfCurrentTime;

    /** The value of the startTime field */
    protected double vfStartTime;

    /** The value of the stopTime field */
    protected double vfStopTime;

    /** The value of the fraction interval */
    protected float vfFraction;

    /** The value of the isActive field */
    protected boolean vfIsActive;

    // Internal working variables

    /** Used to track the amount of elapse time between frames */
    private double previous_clock_time;

    /** State variable, used to track changes to the time fields while
     * the controller is in the stopped state. */
    private boolean time_changed;

    /**
     * Static constructor to build the field representations of this node once
     * for all users.
     */
    static {
        nodeFields = new int[]{FIELD_METADATA};

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFNode",
                "metadata");
        fieldDecl[FIELD_DIRECTION] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFBool",
                "direction");
        fieldDecl[FIELD_ENABLED] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFBool",
                "enabled");
        fieldDecl[FIELD_LOOP] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFBool",
                "loop");
        fieldDecl[FIELD_RATE] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFFloat",
                "rate");
        fieldDecl[FIELD_RUN] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFBool",
                "run");
        fieldDecl[FIELD_CURRENT_TIME] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFTime",
                "currentTime");
        fieldDecl[FIELD_START_TIME] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFTime",
                "startTime");
        fieldDecl[FIELD_STOP_TIME] =
                new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                "SFTime",
                "stopTime");
        fieldDecl[FIELD_IS_ACTIVE] =
                new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                "SFBool",
                "isActive");
        fieldDecl[FIELD_FRACTION] =
                new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                "SFFloat",
                "fraction_changed");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_DIRECTION;
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = FIELD_ENABLED;
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = FIELD_LOOP;
        fieldMap.put("loop", idx);
        fieldMap.put("set_loop", idx);
        fieldMap.put("loop_changed", idx);

        idx = FIELD_RATE;
        fieldMap.put("rate", idx);
        fieldMap.put("set_rate", idx);
        fieldMap.put("rate_changed", idx);

        idx = FIELD_RUN;
        fieldMap.put("run", idx);
        fieldMap.put("set_run", idx);
        fieldMap.put("run_changed", idx);

        idx = FIELD_CURRENT_TIME;
        fieldMap.put("currentTime", idx);
        fieldMap.put("set_currentTime", idx);
        fieldMap.put("currentTime_changed", idx);

        idx = FIELD_START_TIME;
        fieldMap.put("startTime", idx);
        fieldMap.put("set_startTime", idx);
        fieldMap.put("startTime_changed", idx);

        idx = FIELD_STOP_TIME;
        fieldMap.put("stopTime", idx);
        fieldMap.put("set_stopTime", idx);
        fieldMap.put("stopTime_changed", idx);

        idx = FIELD_IS_ACTIVE;
        fieldMap.put("isActive", idx);
        fieldMap.put("isActive_changed", idx);

        idx = FIELD_FRACTION;
        fieldMap.put("fraction", idx);
        fieldMap.put("fraction_changed", idx);
    }

    /**
     * Construct a new time sensor object
     */
    public BaseTimeController() {
        super("TimeController");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfDirection = true;
        vfEnabled = true;
        vfLoop = false;
        vfRate = 1;
        vfRun = false;
        vfCurrentTime = 0;
        vfStartTime = 0;
        vfStopTime = 0;
        vfIsActive = false;

    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node. If the node is not the same type, an exception will be thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseTimeController(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("direction");
            VRMLFieldData field = node.getFieldValue(index);
            vfDirection = field.booleanValue;

            index = node.getFieldIndex("enabled");
            field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("loop");
            field = node.getFieldValue(index);
            vfLoop = field.booleanValue;

            index = node.getFieldIndex("rate");
            field = node.getFieldValue(index);
            vfRate = field.floatValue;

            index = node.getFieldIndex("run");
            field = node.getFieldValue(index);
            vfRun = field.booleanValue;

            index = node.getFieldIndex("currentTime");
            field = node.getFieldValue(index);
            vfCurrentTime = field.doubleValue;

            index = node.getFieldIndex("startTime");
            field = node.getFieldValue(index);
            vfStartTime = field.doubleValue;

            index = node.getFieldIndex("stopTime");
            field = node.getFieldValue(index);
            vfStopTime = field.doubleValue;

        } catch (VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by BaseTimeDependentNode.
    //-------------------------------------------------------------
    /**
     * Set the vrmlClock that this time dependent node will be running with. The
     * vrmlClock provides all the information and listeners for keeping track of
     * time. If we are enabled at the time that this method is called we
     * automatically register the listener. Then, all the events that need to be
     * generated will be handled at the next vrmlClock tick we get issued.
     *
     * @param clk The vrmlClock to use for this node
     */
    @Override
    public void setVRMLClock(VRMLClock clk) {

        if (vrmlClock != null) {
            vrmlClock.removeTimeListener(this);
        }

        this.vrmlClock = clk;

        if (vrmlClock != null) {
            if (vfEnabled) {
                vrmlClock.addTimeListener(this);
            }
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLSensorNodeType
    //-------------------------------------------------------------
    /**
     * Set the sensor enabled or disabled.
     *
     * @param enabled The new enabled value
     */
    @Override
    public void setEnabled(boolean enabled) {

        if (vfEnabled != enabled) {
            vfEnabled = enabled;

            if (!inSetup) {
                hasChanged[FIELD_ENABLED] = true;
                fireFieldChanged(FIELD_ENABLED);
            }

            // setEnable(false) cancels the timer, propagate the remaining state fields
            if (!vfEnabled) {
                if (vfRun) {
                    vfRun = false;
                    if (!inSetup) {
                        hasChanged[FIELD_RUN] = true;
                        fireFieldChanged(FIELD_RUN);
                    }
                }

                if (vfIsActive) {
                    vfIsActive = false;
                    if (!inSetup) {
                        hasChanged[FIELD_IS_ACTIVE] = true;
                        fireFieldChanged(FIELD_IS_ACTIVE);
                    }
                }
            }

            if (vrmlClock != null) {
                if (vfEnabled) {
                    vrmlClock.addTimeListener(this);
                } else {
                    vrmlClock.removeTimeListener(this);
                }
            }
        }
    }

    /**
     * Accessor method to get current value to the enabled field.
     *
     * @return The value of the enabled field
     */
    @Override
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Accessor method to get current value of field
     * <code>isActive</code>.
     *
     * @return The current value of isActive
     */
    @Override
    public boolean getIsActive() {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode.
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
     * Get the list of indices that correspond to fields that contain nodes ie
     * MFNode and SFNode). Used for blind scene graph traversal without needing
     * to spend time querying for all fields etc. If a node does not have any
     * fields that contain nodes, this shall return null. The field list covers
     * all field types, regardless of whether they are readable or not at the
     * VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields or
     * null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give a
     * value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0 || index > LAST_TIME_CONTROLLER_INDEX) {
            return null;
        }

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
     * Get the primary type of this node. Replaces the instanceof mechanism for
     * use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.SensorNodeType;
    }

    /**
     * Get the secondary type of this node. Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType.
    //----------------------------------------------------------
    /**
     * Notification that the construction phase of this node has finished. If
     * the node would like to do any internal processing, such as setting up
     * geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if (!inSetup) {
            return;
        }

        super.setupFinished();
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will return
     * the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch (index) {
            case FIELD_FRACTION:
                fieldData.clear();
                fieldData.floatValue = vfFraction;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_IS_ACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_DIRECTION:
                fieldData.clear();
                fieldData.booleanValue = vfDirection;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_LOOP:
                fieldData.clear();
                fieldData.booleanValue = vfLoop;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_RATE:
                fieldData.clear();
                fieldData.floatValue = vfRate;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_RUN:
                fieldData.clear();
                fieldData.booleanValue = vfRun;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CURRENT_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfCurrentTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_START_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfStartTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_STOP_TIME:
                fieldData.clear();
                fieldData.doubleValue = vfStopTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
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
     * time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value should
     * be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that the
     * value should be sent to.
     */
    @Override
    public void sendRoute(double time,
            int srcIndex,
            VRMLNodeType destNode,
            int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch (srcIndex) {
                case FIELD_FRACTION:
                    destNode.setValue(destIndex, vfFraction);
                    break;
                case FIELD_IS_ACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;
                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;
                case FIELD_DIRECTION:
                    destNode.setValue(destIndex, vfDirection);
                    break;
                case FIELD_LOOP:
                    destNode.setValue(destIndex, vfLoop);
                    break;
                case FIELD_RATE:
                    destNode.setValue(destIndex, vfRate);
                    break;
                case FIELD_RUN:
                    destNode.setValue(destIndex, vfRun);
                    break;
                case FIELD_CURRENT_TIME:
                    destNode.setValue(destIndex, vfCurrentTime);
                    break;
                case FIELD_START_TIME:
                    destNode.setValue(destIndex, vfStartTime);
                    break;
                case FIELD_STOP_TIME:
                    destNode.setValue(destIndex, vfStopTime);
                    break;
                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch (InvalidFieldException | InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: "
                    + ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a double for the SFTime
     * fields. The fields effected by this cycleTime, cycleInterval, startTime,
     * stopTime, fraction and time fields. This method does not currently check
     * for negative values. Should it?
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range or
     * not appropriate for this field
     */
    @Override
    public void setValue(int index, double value)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_CURRENT_TIME:
                setCurrentTime(value);
                break;

            case FIELD_START_TIME:
                setStartTime(value);
                break;

            case FIELD_STOP_TIME:
                setStopTime(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is be
     * used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range or
     * not appropriate for this field
     */
    @Override
    public void setValue(int index, boolean value)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {

            case FIELD_IS_ACTIVE:
                throw new InvalidFieldValueException("Cannot set eventout");

            case FIELD_DIRECTION:
                setDirection(value);
                break;

            case FIELD_ENABLED:
                setEnabled(value);
                break;

            case FIELD_LOOP:
                setLoop(value);
                break;

            case FIELD_RUN:
                setRun(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would be
     * used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range or
     * not appropriate for this field
     */
    @Override
    public void setValue(int index, float value)
            throws InvalidFieldException, InvalidFieldValueException {

        switch (index) {
            case FIELD_RATE:
                setRate(value);
                break;

            case FIELD_FRACTION:
                throw new InvalidFieldValueException("Cannot set eventout");

            default:
                super.setValue(index, value);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLTimeListener
    //-------------------------------------------------------------
    /**
     * Notification that the time is now this value.
     *
     * @param time The current time
     */
    @Override
    public void timeClick(long time) {

        double clock_time = time * 0.001;

        if (vfEnabled) {

            int direction = (vfDirection) ? 1 : -1;
            double span = vfStopTime - vfStartTime;

            if (vfRun) {
                if (vfIsActive) {
                    // running and active, calculate the new current time
                    double virtual_elapse_time =
                            direction * vfRate * (clock_time - previous_clock_time);

                    double new_current_time = vfCurrentTime + virtual_elapse_time;

                    boolean over = new_current_time >= vfStartTime;
                    boolean under = new_current_time <= vfStopTime;
                    if (over && under) {
                        // new_current_time is within the span
                        vfCurrentTime = new_current_time;
                        vfFraction = (float) ((vfCurrentTime - vfStartTime) / span);

                    } else if (!over) {
                        // new_current_time is before the initial
                        if (vfLoop) {
                            vfCurrentTime = vfStopTime - (vfStartTime - new_current_time);
                            vfFraction = (float) ((vfCurrentTime - vfStartTime) / span);
                        } else {
                            vfCurrentTime = vfStartTime;
                            vfFraction = 0.0f;
                            vfIsActive = false;
                        }
                    } else if (!under) {
                        // new_current_time is after the final
                        if (vfLoop) {
                            vfCurrentTime = vfStartTime + (new_current_time - vfStopTime);
                            vfFraction = (float) ((vfCurrentTime - vfStartTime) / span);
                        } else {
                            vfCurrentTime = vfStopTime;
                            vfFraction = 1.0f;
                            vfIsActive = false;
                        }
                    }
                    hasChanged[FIELD_CURRENT_TIME] = true;
                    fireFieldChanged(FIELD_CURRENT_TIME);

                    hasChanged[FIELD_FRACTION] = true;
                    fireFieldChanged(FIELD_FRACTION);

                    if (!vfIsActive) {

                        vfRun = false;

                        hasChanged[FIELD_RUN] = true;
                        fireFieldChanged(FIELD_RUN);

                        hasChanged[FIELD_IS_ACTIVE] = true;
                        fireFieldChanged(FIELD_IS_ACTIVE);
                    }
                } else {
                    // running, but not active yet.
                    boolean over = vfCurrentTime >= vfStartTime;
                    boolean under = vfCurrentTime <= vfStopTime;
                    if (over && under) {
                        // vfCurrentTime is within the span. calculate and
                        // send the initial events.
                        vfIsActive = true;
                        vfFraction = (float) ((vfCurrentTime - vfStartTime) / span);

                        hasChanged[FIELD_CURRENT_TIME] = true;
                        fireFieldChanged(FIELD_CURRENT_TIME);

                        hasChanged[FIELD_FRACTION] = true;
                        fireFieldChanged(FIELD_FRACTION);

                        hasChanged[FIELD_IS_ACTIVE] = true;
                        fireFieldChanged(FIELD_IS_ACTIVE);

                    } else {
                        // vfCurrentTime is outside the span. notify that we're
                        // inactive, but nothing else.
                        vfRun = false;

                        hasChanged[FIELD_RUN] = true;
                        fireFieldChanged(FIELD_RUN);

                        hasChanged[FIELD_IS_ACTIVE] = true;
                        fireFieldChanged(FIELD_IS_ACTIVE);
                    }
                }

            } else {
                // stopped state. only fire events on changes to the time fields.
                if (time_changed && (span > 0) && (vfCurrentTime >= vfStartTime)
                        && (vfCurrentTime <= vfStopTime)) {

                    // note, events on the time fields have been sent from the setters

                    vfFraction = (float) ((vfCurrentTime - vfStartTime) / span);
                    hasChanged[FIELD_FRACTION] = true;
                    fireFieldChanged(FIELD_FRACTION);

                    time_changed = false;
                }
            }
        }
        previous_clock_time = clock_time;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLTimeControlledNodeType
    //-------------------------------------------------------------
    /**
     * Set the loop field value.
     *
     * @param loop Whether time loops or not
     */
    @Override
    public void setLoop(boolean loop) {

        if (loop != vfLoop) {
            vfLoop = loop;

            if (!inSetup) {
                hasChanged[FIELD_LOOP] = true;
                fireFieldChanged(FIELD_LOOP);
            }
        }
    }

    /**
     * Accessor method to get current value of field <b>loop</b>, default value
     * is
     * <code>false</code>
     *
     * @return The value of the loop field
     */
    @Override
    public boolean getLoop() {
        return vfLoop;
    }

    /**
     * Set a new value for the initial time. If the sensor is active then it is
     * ignored.
     *
     * @param time The new initial time
     */
    @Override
    public void setStartTime(double time) {

        if (!vfIsActive && (time != vfStartTime)) {
            vfStartTime = time;

            if (!inSetup) {
                hasChanged[FIELD_START_TIME] = true;
                fireFieldChanged(FIELD_START_TIME);
                time_changed = true;
            }
        }
    }

    /**
     * Accessor method to get current value of field <b>startTime</b>, default
     * value is
     * <code>0</code>.
     *
     * @return The current startTime
     */
    @Override
    public double getStartTime() {
        return vfStartTime;
    }

    /**
     * Set a new value for the final time. If the sensor is active then it is
     * ignored.
     *
     * @param time The new final time
     */
    @Override
    public void setStopTime(double time) {

        if (!vfIsActive && (time != vfStopTime)) {
            vfStopTime = time;

            if (!inSetup) {
                hasChanged[FIELD_STOP_TIME] = true;
                fireFieldChanged(FIELD_STOP_TIME);
                time_changed = true;
            }
        }
    }

    /**
     * Accessor method to get current value of field <b>stopTime</b>, default
     * value is
     * <code>0</code>
     *
     * @return The current stop Time
     */
    @Override
    public double getStopTime() {
        return vfStopTime;
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------
    /**
     * Set the direction field value.
     *
     * @param direction The new direction, true for forward, false for backward
     */
    public void setDirection(boolean direction) {

        if (direction != vfDirection) {
            vfDirection = direction;

            if (!inSetup) {
                hasChanged[FIELD_DIRECTION] = true;
                fireFieldChanged(FIELD_DIRECTION);
            }
        }
    }

    /**
     * Set the run field value.
     *
     * @param run The new run, true for running, false for stopped
     */
    public void setRun(boolean run) {

        if (run != vfRun) {
            vfRun = run;

            if (!inSetup) {
                hasChanged[FIELD_RUN] = true;
                fireFieldChanged(FIELD_RUN);
            }
            if (!vfRun) {
                if (vfIsActive) {
                    vfIsActive = false;

                    if (!inSetup) {
                        hasChanged[FIELD_IS_ACTIVE] = true;
                        fireFieldChanged(FIELD_IS_ACTIVE);
                    }
                }
            }
        }
    }

    /**
     * Set the rate field value.
     *
     * @param rate The multiplier of time passage
     */
    public void setRate(float rate) {

        if (rate != vfRate) {
            vfRate = rate;
            if (!inSetup) {
                hasChanged[FIELD_RATE] = true;
                fireFieldChanged(FIELD_RATE);
            }
        }
    }

    /**
     * Set a new value for the current time. If the sensor is active then it is
     * ignored.
     *
     * @param time The new current time
     */
    public void setCurrentTime(double time) {

        if (!vfIsActive && (time != vfCurrentTime)) {
            vfCurrentTime = time;

            if (!inSetup) {
                hasChanged[FIELD_CURRENT_TIME] = true;
                fireFieldChanged(FIELD_CURRENT_TIME);
                time_changed = true;
            }
        }
    }
}
