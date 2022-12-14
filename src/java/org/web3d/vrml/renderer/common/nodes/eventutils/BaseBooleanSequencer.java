/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.eventutils;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.j3d.util.interpolator.BooleanInterpolator;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLSequencerNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseSequencerNode;

/**
 * Abstract implementation of a boolean sequencer so that specific
 * renderer instances can derive from it.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class BaseBooleanSequencer extends BaseSequencerNode {

    /** The last field index used by this class */
    protected static final int LAST_BOOLEAN_SEQUENCER_INDEX = LAST_SEQUENCER_INDEX;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_BOOLEAN_SEQUENCER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the keyValue field */
    protected boolean[] vfKeyValue;

    /** The number of valid values in vfKeyValue */
    protected int numKeyValue;

    /** The value of the value field */
    protected boolean vfValueChanged;

    /** The interpolator we use to do the heavy work for us */
    private BooleanInterpolator interpolator;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_NEXT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "next");
        fieldDecl[FIELD_PREVIOUS] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "previous");
        fieldDecl[FIELD_SET_FRACTION] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFFloat",
                                     "set_fraction");
        fieldDecl[FIELD_KEY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "key");
        fieldDecl[FIELD_KEY_VALUE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFBool",
                                     "keyValue");
        fieldDecl[FIELD_VALUE_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "value_changed");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("next", FIELD_NEXT);
        fieldMap.put("previous", FIELD_PREVIOUS);
        fieldMap.put("set_fraction", FIELD_SET_FRACTION);

        idx = FIELD_KEY;
        fieldMap.put("key", idx);
        fieldMap.put("set_key", idx);
        fieldMap.put("key_changed", idx);

        idx = FIELD_KEY_VALUE;
        fieldMap.put("keyValue", idx);
        fieldMap.put("set_keyValue", idx);
        fieldMap.put("keyValueChanged", idx);

        fieldMap.put("value_changed", FIELD_VALUE_CHANGED);
    }

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    protected BaseBooleanSequencer() {
        super("BooleanSequencer");

        hasChanged = new boolean[NUM_FIELDS];

        vfKeyValue = FieldConstants.EMPTY_MFBOOL;
        vfValueChanged = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseBooleanSequencer(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLSequencerNodeType)node);

        try {
            int index = node.getFieldIndex("keyValue");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfKeyValue = new boolean[field.numElements];
                System.arraycopy(field.booleanArrayValues,
                                 0,
                                 vfKeyValue,
                                 0,
                                 field.numElements);

                numKeyValue = field.numElements;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLSequencerNodeType
    //-------------------------------------------------------------

    /**
     * Set a new value for the fraction field. This will always evaluate
     * even if the fraction is the same as the old fraction.
     *
     * @param newFraction The new value for fraction
     */
    @Override
    public void setFraction(float newFraction) {
        vfFraction = newFraction;

        setCurrentKey();

        if(vfKey != null && vfKey.length > 0) {
            if(interpolator != null && vfKeyValue.length > 0) {
                boolean value = interpolator.booleanValue(vfFraction);
                setValueChanged(value);
            }
        }
    }

    /**
     * Set a new value for the key field. Null will delete all key values.
     *
     * @param keys The new key values to use
     * @param numValid The number of valid values to copy from the array
     */
    @Override
    public void setKey(float[] keys, int numValid) {

        super.setKey(keys, numValid);

        if(!inSetup) {
            rebuildInterpolator();
            // now force a re-interpolation given the current fraction
            setFraction(vfFraction);
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

        rebuildInterpolator();
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

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
        if(index < 0  || index > LAST_BOOLEAN_SEQUENCER_INDEX)
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
            case FIELD_KEY_VALUE:
                fieldData.clear();
                fieldData.booleanArrayValues = vfKeyValue;
                fieldData.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                fieldData.numElements = numKeyValue;
                break;

            case FIELD_VALUE_CHANGED:
                fieldData.clear();
                fieldData.booleanValue = vfValueChanged;
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
                case FIELD_KEY_VALUE:
                    destNode.setValue(destIndex, vfKeyValue, numKeyValue);
                    break;

                case FIELD_VALUE_CHANGED:
                    destNode.setValue(destIndex, vfValueChanged);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseBooleanSequencer sendRoute: No field!" + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseBooleanSequencer sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of booleans.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_KEY_VALUE:
                setKeyValue(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------

    /**
     * Internal method to rebuild the interpolator from the latest lot of key
     * and value information. This is an expensive method as you must clear
     * the entire interpolator and start from scratch. Obviously we don't
     * expect people to be changing their key values every other frame.
     * <p>
     * If the key and keyValue fields are not the same length then we take
     * the lesser of the two and only assign that many vertices to the
     * interpolator to handle.
     */
    private void rebuildInterpolator() {
        if((vfKey == null) || (vfKeyValue == null)) {
            interpolator = null;
            return;
        }

        int size = (vfKey.length < vfKeyValue.length) ?
                   vfKey.length :
                   vfKeyValue.length;

        interpolator = new BooleanInterpolator(size, BooleanInterpolator.STEP);

        for(int i = 0; i < size; i++)
            interpolator.addKeyFrame(vfKey[i], vfKeyValue[i]);
    }

    /**
     * Set the key value (MFVec3f) to the new value. If the value is null, it
     * will clear the currently set list and return it to an empty list.
     *
     * @param keyValues The new values to use
     */
    private void setKeyValue(boolean[] keyValues, int numValid) {

        numKeyValue = numValid;

        if(numValid != 0) {
            if(numValid > vfKeyValue.length)
                vfKeyValue = new boolean[numValid];

            System.arraycopy(keyValues, 0, vfKeyValue, 0, numValid);
        }

        if(!inSetup) {
            rebuildInterpolator();
            // now force a re-interpolation given the current fraction
            setFraction(vfFraction);

            hasChanged[FIELD_KEY_VALUE] = true;
            fireFieldChanged(FIELD_KEY_VALUE);
        }
    }

    /**
     * Set the value_changed to the new value. Null values will be ignored.
     *
     * @param newValue The new value to use
     */
    private void setValueChanged(boolean value) {
        vfValueChanged = value;

        if (!inSetup) {
            hasChanged[FIELD_VALUE_CHANGED] = true;
            fireFieldChanged(FIELD_VALUE_CHANGED);
        }
    }
}
