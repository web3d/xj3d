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

package org.web3d.vrml.renderer.ogl.nodes.texture;

// Standard imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import java.util.ArrayList;
import java.util.List;

import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseTextureTransform;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureCoordinateTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureTransformListener;

/**
 * OpenGL renderer implementation of a texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public class OGLTextureTransform extends BaseTextureTransform
    implements OGLTextureCoordinateTransformNodeType {

    /** Message when we get an exception sending out the transform message */
    private static final String TX_SEND_ERROR =
        "There was an exception generation sending a texture transform " +
        "listener event in OGLTextureTransform";

    /** Index flag for the listener firing */
    private static final boolean[] UPDATE_FLAGS = { true };

    /** List of those want to know about TextureTransform changes */
    private List<OGLTextureTransformListener> listenerList;

    /** List of changed transforms to send along to the listeners */
    private Matrix4f[] changedTransforms;

    /**
     * Construct a new default instance of this class.
     */
    public OGLTextureTransform() {
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
    public OGLTextureTransform(VRMLNodeType node) {
        super(node);

        init();
    }

    /**
     * Common initialization code for constructors.
     */
    private void init() {
        listenerList = new ArrayList<>(1);
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

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

        updateTransform();
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        updateTransform();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

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

        super.setValue(index, value);

        if (!inSetup) {
            updateTransform();
            fireTransformChanged();
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

        super.setValue(index, value, numValid);

        if (!inSetup) {
            updateTransform();
            fireTransformChanged();
        }
    }

    //----------------------------------------------------------
    // Methods from OGLTextureTransformNodeType interface.
    //----------------------------------------------------------

    /**
     * Request the Transformation used to represent a texture transformation.
     * The transform will contain all of the warp, scale and rotation
     *
     * @return The transform used to modify this texture
     */
    @Override
    public Matrix4f[] getTransformMatrix() {
        return new Matrix4f[] {matrix};
    }

    /**
     * Add a listener for textureTransform changes
     *
     * @param tl The listener to add
     */
    @Override
     public void addTransformListener(OGLTextureTransformListener tl) {
        if (!listenerList.contains(tl)) {
            listenerList.add(tl);
        }
     }

    /**
     * Remove a listener for textureTransform changes
     *
     * @param tl The listener to remove
     */
    @Override
     public void removeTransformListener(OGLTextureTransformListener tl) {
        listenerList.remove(tl);
     }

    //----------------------------------------------------------
    // Methods internal to OGLTextureTransform
    //----------------------------------------------------------

    /**
     * Notify listeners that the transform has changed.
     */
    private void fireTransformChanged() {

        // Notify listeners of new value
        int size = listenerList.size();

        for(int i = 0; i < size; i++) {
            OGLTextureTransformListener l = listenerList.get(i);

            try {
                if(changedTransforms == null)
                    changedTransforms = new Matrix4f[] {matrix};

                l.textureTransformChanged(this, changedTransforms, UPDATE_FLAGS);

            } catch(Exception e) {
                errorReporter.errorReport(TX_SEND_ERROR, e);
            }
        }
    }

}
