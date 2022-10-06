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

package org.web3d.vrml.renderer.common.nodes.texture;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureTransformNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureTransformListener;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * norender implementation of a texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public abstract class BaseTextureTransform extends AbstractNode
  implements VRMLTextureTransformNodeType {

    /** Field index for center */
    protected static final int FIELD_CENTER = LAST_NODE_INDEX + 1;

    /** Field index for rotation */
    protected static final int FIELD_ROTATION = LAST_NODE_INDEX + 2;

    /** Field index for scale */
    protected static final int FIELD_SCALE = LAST_NODE_INDEX + 3;

    /** Field index for translation */
    protected static final int FIELD_TRANSLATION = LAST_NODE_INDEX + 4;

    /** ID of the last field index in this class */
    protected static final int LAST_TEXTURETRANSFORM_INDEX = FIELD_TRANSLATION;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_TEXTURETRANSFORM_INDEX + 1;

    /* VRML Field declarations */
    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField SFVec2f center 0 0 */
    protected float[] vfCenter;

    /** exposedField SFFloat rotation 0 */
    protected float vfRotation;

    /** exposedField SFVec2f scale 1 1 */
    protected float[] vfScale;

    /** exposedField SFVec2f translation 0 0 */
    protected float[] vfTranslation;

    /** Combined Matrix */
    protected Matrix4f matrix;

    /** Class Vars for speed */
    protected Vector3f v1;

    /**
     *
     */
    protected Vector3f v2;

    /**
     *
     */
    protected Vector3f v3;

    /**
     *
     */
    protected Matrix4f T;

    /**
     *
     */
    protected Matrix4f C;

    /**
     *
     */
    protected Matrix4f R;

    /**
     *
     */
    protected Matrix4f S;

    /**
     *
     */
    protected AxisAngle4f al;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "center");
        fieldDecl[FIELD_ROTATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "rotation");
        fieldDecl[FIELD_SCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "scale");
        fieldDecl[FIELD_TRANSLATION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec2f",
                                     "translation");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_SCALE;
        fieldMap.put("scale", idx);
        fieldMap.put("set_scale", idx);
        fieldMap.put("scale_changed", idx);

        idx = FIELD_CENTER;
        fieldMap.put("center", idx);
        fieldMap.put("set_center", idx);
        fieldMap.put("center_changed", idx);

        idx = FIELD_ROTATION;
        fieldMap.put("rotation", idx);
        fieldMap.put("set_rotation", idx);
        fieldMap.put("rotation_changed", idx);

        idx = FIELD_TRANSLATION;
        fieldMap.put("translation", idx);
        fieldMap.put("set_translation", idx);
        fieldMap.put("translation_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseTextureTransform() {
        super("TextureTransform");

        vfCenter = new float[] { 0, 0 };
        vfRotation = 0;
        vfScale = new float[] { 1, 1 };
        vfTranslation = new float[] { 0, 0 };

        hasChanged = new boolean[LAST_TEXTURETRANSFORM_INDEX + 1];

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseTextureTransform(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);
            vfCenter[0] = field.floatArrayValues[0];
            vfCenter[1] = field.floatArrayValues[1];

            index = node.getFieldIndex("scale");
            field = node.getFieldValue(index);
            vfScale[0] = field.floatArrayValues[0];
            vfScale[1] = field.floatArrayValues[1];

            index = node.getFieldIndex("translation");
            field = node.getFieldValue(index);
            vfTranslation[0] = field.floatArrayValues[0];
            vfTranslation[1] = field.floatArrayValues[1];

            index = node.getFieldIndex("rotation");
            field = node.getFieldValue(index);
            vfRotation = field.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }

        init();
    }

    /**
     * Common initialization code for constructors.
     */
    private void init() {
        matrix = new Matrix4f();

        v1 = new Vector3f();
        v2 = new Vector3f();
        v3 = new Vector3f();
        T = new Matrix4f();
        C = new Matrix4f();
        R = new Matrix4f();
        S = new Matrix4f();
        al = new AxisAngle4f();
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
        if (index < 0  || index > LAST_TEXTURETRANSFORM_INDEX) {
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.TextureTransformNodeType;
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

        fieldData.clear();

        switch(index) {
            case FIELD_CENTER :
                fieldData.floatArrayValues = vfCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_ROTATION:
                fieldData.floatValue = vfRotation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SCALE:
                fieldData.floatArrayValues = vfScale;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_TRANSLATION:
                fieldData.floatArrayValues = vfTranslation;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
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
                case FIELD_CENTER:
                    destNode.setValue(destIndex, vfCenter, 2);
                    break;

                case FIELD_ROTATION:
                    destNode.setValue(destIndex, vfRotation);
                    break;

                case FIELD_SCALE:
                    destNode.setValue(destIndex, vfScale, 2);
                    break;

                case FIELD_TRANSLATION:
                    destNode.setValue(destIndex, vfTranslation, 2);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("TextureTransform sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("TextureTransform sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_ROTATION:
                vfRotation = value;
                if(!inSetup) {
                    hasChanged[FIELD_ROTATION] = true;
                    fireFieldChanged(FIELD_ROTATION);
                }
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CENTER:
                vfCenter[0] = value[0];
                vfCenter[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_CENTER] = true;
                    fireFieldChanged(FIELD_CENTER);
                }
                break;

            case FIELD_SCALE:
                vfScale[0] = value[0];
                vfScale[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_SCALE] = true;
                    fireFieldChanged(FIELD_SCALE);
                }
                break;

            case FIELD_TRANSLATION:
                vfTranslation[0] = value[0];
                vfTranslation[1] = value[1];

                if(!inSetup) {
                    hasChanged[FIELD_TRANSLATION] = true;
                    fireFieldChanged(FIELD_TRANSLATION);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }

    }

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float rot)
            throws InvalidFieldValueException {

        vfRotation = rot;

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_ROTATION] = true;
            fireFieldChanged(FIELD_ROTATION);
        }
    }

    /**
     * Get the current rotation component of the transform.
     *
     * @return The current rotation
     */
    public float getRotation() {
        return vfRotation;
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx)
            throws InvalidFieldValueException {

        if(tx == null)
            throw new InvalidFieldValueException("Translation value null");

        vfTranslation[0] = tx[0];
        vfTranslation[1] = tx[1];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_TRANSLATION] = true;
            fireFieldChanged(FIELD_TRANSLATION);
        }
    }

    /**
     * Get the current translation component of the transform.
     *
     * @return The current translation
     */
    public float[] getTranslation() {
        return vfTranslation;
    }

    /**
     * Set the scale component of the of transform. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    public void setScale(float[] scale)
            throws InvalidFieldValueException {

        if(scale == null)
            throw new InvalidFieldValueException("Scale value null");

        vfScale[0] = scale[0];
        vfScale[1] = scale[1];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_SCALE] = true;
            fireFieldChanged(FIELD_SCALE);
        }
    }

    /**
     * Get the current scale component of the transform.
     *
     * @return The current scale
     */
    public float[] getScale() {
        return vfScale;
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    public void setCenter(float[] center)
            throws InvalidFieldValueException {

        if(center == null)
            throw new InvalidFieldValueException("Center value null");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];

        // Save recalcs during the setup phase
        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_CENTER] = true;
            fireFieldChanged(FIELD_CENTER);
        }
    }

    /**
     * Get the current center component of the transform.
     *
     * @return The current center
     */
    public float[] getCenter() {
        return vfCenter;
    }

    /**
     *
     */
    protected void updateTransform() {
//System.out.println("UPDATE trans: " + vfTranslation[0] + "," + vfTranslation[1]
//    + " scale: " + vfScale[0] + "," + vfScale[1] + " rot: " + vfRotation);
        //v1.set(vfTranslation[0],vfTranslation[1],0.0);
        v1.x = vfTranslation[0];
        v1.y = vfTranslation[1];
        v1.z = 0;

        //T.set(v1);
        T.setIdentity();
        T.setTranslation(v1);

        //v2.set(vfCenter[0],vfCenter[1],0.0);
        v2.x = -vfCenter[0];
        v2.y = -vfCenter[1];
        v2.z = 0;

        //C.set(v2);
        C.setIdentity();
        C.setTranslation(v2);

        //al.set(0.0f,0.0f,1.0f,vfRotation);
        al.x = 0;
        al.y = 0;
        al.z = 1;
        al.angle = vfRotation;

        //R.setRotation(al);
        R.setIdentity();
        R.setRotation(al);

        v3.x = vfScale[0];
        v3.y = vfScale[1];
        v3.z = 1;

        //v3.set(vfScale[0],vfScale[1],1.0);
        //S.setScale(v3);
        S.setIdentity();
        S.m00 = vfScale[0];
        S.m11 = vfScale[1];
        S.m22 = 1.0f;

        matrix.setIdentity();

        matrix.mul(C);
        matrix.mul(S);
        matrix.mul(R);

        v2.negate();
        C.setIdentity();
        C.set(v2);

        matrix.mul(C);
        matrix.mul(T);

        //System.out.println("Mat: \n" + matrix);
    }
}
