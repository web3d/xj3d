/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of a BooleanTrigger Node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class BaseBooleanTrigger extends AbstractNode implements VRMLChildNodeType {

    // Field index constants

    /** The field index for set_boolean */
    protected static final int FIELD_SET_TRIGGER_TIME = LAST_NODE_INDEX + 1;

    /** The field index for triggerTime */
    protected static final int FIELD_TRIGGER_TRUE = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_BOOLEAN_TRIGGER_INDEX = FIELD_TRIGGER_TRUE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_BOOLEAN_TRIGGER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

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
        fieldDecl[FIELD_SET_TRIGGER_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFTime",
                                     "set_triggerTime");

        fieldDecl[FIELD_TRIGGER_TRUE] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "triggerTrue");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_SET_TRIGGER_TIME;
        fieldMap.put("set_triggerTime", idx);

        idx = FIELD_TRIGGER_TRUE;
        fieldMap.put("triggerTrue", idx);
    }

    /**
     * Construct a new time sensor object
     */
    public BaseBooleanTrigger() {
        super("BooleanTrigger");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseBooleanTrigger(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
        if(index < 0  || index > LAST_BOOLEAN_TRIGGER_INDEX)
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
            case FIELD_TRIGGER_TRUE:
                fieldData.clear();
                fieldData.booleanValue = true;
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

        try {
            switch(srcIndex) {
                case FIELD_TRIGGER_TRUE:
                    destNode.setValue(destIndex, true);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("BaseBooleanTrigger.sendRoute: InvalidField: " + srcIndex);
            ife.printStackTrace(System.err);
        } catch(InvalidFieldValueException ifve) {
            System.err.println("BaseBooleanTrigger.sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a double. This is
     * be used to set SFTime field types triggerTime.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SET_TRIGGER_TIME:
                setTriggerTime();
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Private methods
    //----------------------------------------------------------

    /**
     * Handle setBoolean events.
     */
    private void setTriggerTime() {
        if (!inSetup) {
            hasChanged[FIELD_TRIGGER_TRUE] = true;
            fireFieldChanged(FIELD_TRIGGER_TRUE);
        }
    }
}
