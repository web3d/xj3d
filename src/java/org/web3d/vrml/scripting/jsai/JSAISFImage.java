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

package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.field.ConstSFImage;
import vrml.field.SFImage;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the SFFloat field when extracted from part
 * of a node.
 * <p>
 *
 * The node assumes that the index and node have been checked before use by
 * this class.
 * <p>
 *
 * An interesting implementation question is dealing with the methods that
 * fetch an individual color component. The current implementation does not
 * force a re-fetch of the value from the underlying node. Perhaps it should.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
class JSAISFImage extends SFImage
    implements VRMLNodeListener {

    /** The node that this field references */
    private VRMLNodeType node;

    /** The index of the field this representation belongs to */
    private int fieldIndex;

    /**
     * Create a new field that represents the underlying instance
     *
     * @param n The node to fetch information from
     * @param index The index of the field to use
     */
    JSAISFImage(VRMLNodeType n, int index) {
        node = n;
        fieldIndex = index;
        valueChanged = true;
        updateLocalData();

        node.addNodeListener(this);
    }

    /**
     * Get the width value of the image. Overrides the basic implementation to
     * make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    @Override
    public int getWidth() {
        updateLocalData();

        return imageWidth;
    }

    /**
     * Get the height value of the image. Overrides the basic implementation to
     * make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    @Override
    public int getHeight() {
        updateLocalData();

        return imageHeight;
    }

    /**
     * Get the number of color components of the image. Overrides the basic
     * implementation to make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    @Override
    public int getComponents() {
        updateLocalData();

        return imageComponents;
    }

    /**
     * Get the pixels of the image. Overrides the basic
     * implementation to make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    @Override
    public void getPixels(byte pixels[]) {
        updateLocalData();
    }

    @Override
    public void setValue(int width, int height, int components,
                        byte pixels[]) {
    }

    @Override
    public void setValue(ConstSFImage image) {
    }

    @Override
    public void setValue(SFImage image) {
    }

    /**
     * Create a cloned copy of this node
     *
     * @return A copy of this field
     */
    @Override
    public Object clone() {
        return new JSAISFImage(node, fieldIndex);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    @Override
    public void fieldChanged(int index) {
        if(index != fieldIndex)
            return;

        valueChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Fetch the field from the core and update any values internally.
     */
    private void updateLocalData() {
        if(!valueChanged)
            return;

        try {
            VRMLFieldData fd = node.getFieldValue(fieldIndex);

            if(fd == null) {
                imageWidth = 0;
                imageHeight = 0;
                imageComponents = 0;
            } else {
                imageWidth = fd.intArrayValues[0];
                imageHeight = fd.intArrayValues[1];
                imageComponents = fd.intArrayValues[2];
            }

            // NFI about the pixels yet...
        } catch(FieldException ife) {
        }
    }
}
