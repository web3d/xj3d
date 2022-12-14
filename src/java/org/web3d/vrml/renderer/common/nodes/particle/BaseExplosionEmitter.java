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

package org.web3d.vrml.renderer.common.nodes.particle;

// External imports
import java.util.HashMap;
import java.util.Map;

import org.j3d.geom.particle.ExplosionPointEmitter;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

/**
 * Common implementation of a ExplosionEmitter node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public abstract class BaseExplosionEmitter extends BaseEmitter {

    /** The field index for velocity */
    protected static final int FIELD_POSITION = LAST_EMITTER_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_EXPL_EMITTER_INDEX = FIELD_POSITION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_EXPL_EMITTER_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // The VRML field values

    /** The value of the cycle interval field */
    protected float[] vfPosition;

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
        fieldDecl[FIELD_POSITION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "position");
        fieldDecl[FIELD_VARIATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "variation");
        fieldDecl[FIELD_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "speed");
        fieldDecl[FIELD_MASS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "mass");
        fieldDecl[FIELD_SURFACE_AREA] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "surfaceArea");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_POSITION;
        fieldMap.put("position", idx);
        fieldMap.put("set_position", idx);
        fieldMap.put("position_changed", idx);

        idx = FIELD_SPEED;
        fieldMap.put("speed", idx);
        fieldMap.put("set_speed", idx);
        fieldMap.put("speed_changed", idx);

        idx = FIELD_VARIATION;
        fieldMap.put("variation", idx);
        fieldMap.put("set_variation", idx);
        fieldMap.put("variation_changed", idx);

        idx = FIELD_MASS;
        fieldMap.put("mass", idx);
        fieldMap.put("set_mass", idx);
        fieldMap.put("mass_changed", idx);

        idx = FIELD_SURFACE_AREA;
        fieldMap.put("surfaceArea", idx);
        fieldMap.put("set_surfaceArea", idx);
        fieldMap.put("surfaceArea_changed", idx);

    }

    /**
     * Construct a new time sensor object
     */
    protected BaseExplosionEmitter() {
        super("ExplosionEmitter");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfPosition = new float[] {0, 0, 0};

        initializer = new ExplosionPointEmitter();
        initializer.setMass(vfMass);
        initializer.setSurfaceArea(vfSurfaceArea);
        initializer.setSpeed(vfSpeed);
        initializer.setParticleVariation(vfVariation);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BaseExplosionEmitter(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        copy((VRMLParticleEmitterNodeType)node);

        try {
            int index = node.getFieldIndex("position");
            VRMLFieldData field = node.getFieldValue(index);
            vfPosition[0] = field.floatArrayValues[0];
            vfPosition[1] = field.floatArrayValues[1];
            vfPosition[2] = field.floatArrayValues[2];

            ((ExplosionPointEmitter)initializer).setPosition(vfPosition[0],
                                                    vfPosition[1],
                                                    vfPosition[2]);
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
        if (index < 0  || index > LAST_EXPL_EMITTER_INDEX)
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
            case FIELD_POSITION:
                fieldData.clear();
                fieldData.floatArrayValues = vfPosition;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
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
                case FIELD_POSITION:
                    destNode.setValue(destIndex, vfPosition, 3);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a double for the
     * SFRotation fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_POSITION:
                setPosition(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * The position value has just been set.
     *
     * @param pos The new position of the emitter.
     */
    protected void setPosition(float[] pos) {
        vfPosition[0] = pos[0];
        vfPosition[1] = pos[1];
        vfPosition[2] = pos[2];

        ((ExplosionPointEmitter)initializer).setPosition(pos[0], pos[1], pos[2]);

        if(!inSetup) {
            hasChanged[FIELD_POSITION] = true;
            fireFieldChanged(FIELD_POSITION);
        }
    }
}
