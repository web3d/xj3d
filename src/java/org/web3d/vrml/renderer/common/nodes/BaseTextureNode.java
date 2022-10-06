/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes;

// External imports
import java.util.ArrayList;
import java.util.List;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.nodes.*;


/**
 * Base implementation of a texture object.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class BaseTextureNode extends AbstractNode
    implements VRMLTextureNodeType {

    /** List to contain interested listeners. */
    protected List<VRMLTextureListener> listenerList;

    /**
     * Construct a new base representation of a texture node/
     *
     * @param name The name of the type of node
     */
    protected BaseTextureNode(String name) {
        super(name);

        // Intial size is 1 because most will not be USEed
        listenerList = new ArrayList<>(1);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureNodeType
    //----------------------------------------------------------

    /**
     * Get a string for caching this object. Default implementation returns
     * null.
     *
     * @param stage The stage number, 0 for all single stage textures
     * @return A string to use in lookups. Typically the url loaded.
     */
    @Override
    public String getCacheString(int stage) {
        return null;
    }

    /**
     * Add a listener for texture changes. If the listener is already
     * registered then this request is ignored.
     *
     * @param l The listener instance to be added
     */
    @Override
    public void addTextureListener(VRMLTextureListener l) {
        if (!listenerList.contains(l)) {
            listenerList.add(l);
        }
    }

    /**
     * Removes a listener for texture changes. If the listener is not already
     * registered, the request is ignored.
     *
     * @param l The listener to be removed
     */
    @Override
    public void removeTextureListener(VRMLTextureListener l) {
        listenerList.remove(l);
    }

    //----------------------------------------------------------
    // Helper methods for all textures
    //----------------------------------------------------------

    /**
     * Fire a textureImageChanged event to the listeners.
     *
     * @param idx The stage
     * @param node The node which changed
     * @param image The new image
     * @param url The url used to load or null.
     */
    protected void fireTextureImageChanged(int idx,
                                           VRMLNodeType node,
                                           NIOBufferImage image,
                                           String url) {

        for(VRMLTextureListener l : listenerList) {
            try {
                l.textureImageChanged(idx, node, image, url);
            } catch(Exception e) {
                errorReporter.errorReport("Error in textureImageChanged", e);
            }
        }
    }

    /**
     * Fire a textureParamsChanged event to the listeners.
     *
     * @param len The number of stages
     * @param node The node which changed
     * @param mode The list of modes
     * @param source
     * @param function
     * @param alpha
     * @param color
     */
    protected void fireTextureParamsChanged(int len,
                                            VRMLNodeType node,
                                            int[] mode,
                                            int[] source,
                                            int[] function,
                                            float alpha,
                                            float[] color) {

        for(VRMLTextureListener l : listenerList) {
            try {
                l.textureParamsChanged(len,
                                       mode,
                                       source,
                                       function,
                                       alpha,
                                       color);
            } catch(Exception e) {
                errorReporter.errorReport("Error in textureParamChanged", e);
            }
        }
    }

}
