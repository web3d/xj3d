/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.render;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;

/**
 * An abstract implementation of the LineSet node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public abstract class BaseLineSet extends BaseComponentGeometryNode {

    /** Index of the vertexCount field */
    protected static final int FIELD_VERTEXCOUNT = LAST_GEOMETRY_INDEX + 1;

    /** Last field declaration in this node */
    private static final int LAST_VERTEXCOUNT_INDEX = FIELD_VERTEXCOUNT;

    /** The number of fields in this node */
    private static final int NUM_FIELDS = FIELD_VERTEXCOUNT + 1;

    /** One of the vertexCount values was < 2 */
    private static final String BAD_COUNT_MSG =
        "Line count values less that 2 are not permitted. Offending value " +
        "found at index: ";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** The value of the vertexCount field */
    protected int[] vfVertexCount;

    /** Number of valid values in the line count field */
    protected int numVertexCount;

    /**
     * Static constructor sets up the field declarations
     */
    static {
        nodeFields = new int[] {
            FIELD_COORD,
            FIELD_COLOR,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_COORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coord");
        fieldDecl[FIELD_VERTEXCOUNT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFInt32",
                                     "vertexCount");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_COLOR;
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = FIELD_COORD;
        fieldMap.put("coord", idx);
        fieldMap.put("set_coord", idx);
        fieldMap.put("coord_changed", idx);

        idx = FIELD_VERTEXCOUNT;
        fieldMap.put("vertexCount", idx);
        fieldMap.put("set_vertexCount", idx);
        fieldMap.put("vertexCount_changed", idx);
    }

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     */
    protected BaseLineSet() {
        super("LineSet");

        vfVertexCount = FieldConstants.EMPTY_MFINT32;
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
    protected BaseLineSet(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLComponentGeometryNodeType)node);

        try {
            int index = node.getFieldIndex("vertexCount");
            VRMLFieldData field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfVertexCount = new int[field.numElements];
                System.arraycopy(field.intArrayValues,
                                 0,
                                 vfVertexCount,
                                 0,
                                 field.numElements);

                numVertexCount = field.numElements;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Specifies whether this node requires lighting. Lines are not lit so
     * always return false.
     *
     * @return false
     */
    @Override
    public boolean isLightingEnabled() {
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

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
            case FIELD_VERTEXCOUNT:
                fieldData.clear();
                fieldData.intArrayValues = vfVertexCount;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                fieldData.numElements = numVertexCount;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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
        if(index < 0  || index > LAST_VERTEXCOUNT_INDEX)
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

        switch(srcIndex) {
            case FIELD_VERTEXCOUNT:
                destNode.setValue(destIndex, vfVertexCount, numVertexCount);
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_VERTEXCOUNT:
                setVertexCount(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the value of the vertexCount field.
     *
     * @param counts The list of counts provided
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldValueException One or more values were &lt; 2
     */
    protected void setVertexCount(int[] counts, int numValid)
        throws InvalidFieldValueException {

        for(int i = 0; i < numValid; i++) {
            if(counts[i] < 2)
                throw new InvalidFieldValueException(BAD_COUNT_MSG + i);
        }

        if(vfVertexCount.length < numValid)
            vfVertexCount = new int[numValid];

        System.arraycopy(counts, 0, vfVertexCount, 0, numValid);
        numVertexCount = numValid;

        if(!inSetup) {
            hasChanged[FIELD_VERTEXCOUNT] = true;
            fireFieldChanged(FIELD_VERTEXCOUNT);
        }
    }
}
