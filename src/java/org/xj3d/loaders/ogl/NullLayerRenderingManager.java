/*****************************************************************************
 *                      Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.loaders.ogl;

// External imports
// None

// Local imports
import org.web3d.browser.Xj3DConstants;
import org.j3d.util.ErrorReporter;
import org.xj3d.core.eventmodel.LayerRenderingManager;
import org.xj3d.core.eventmodel.LayerManager;

/**
 * Empty implementation of the layer render manager for the Loader process.
 * <p>
 *
 * NOTE:
 *   This is probably a temporary class until we decide what to do with the
 * loader API and layers.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class NullLayerRenderingManager implements LayerRenderingManager {

    /** The rendering style currently in use */
    private int renderingStyle;

    /**
     * Construct a default instance of the manager
     */
    NullLayerRenderingManager() {
        renderingStyle = Xj3DConstants.RENDER_SHADED;
    }

    //----------------------------------------------------------
    // Methods defined by LayerRenderingManager
    //----------------------------------------------------------

    /**
     * Change the rendering style that the browser should currently be using
     * for all layers. Various options are available based on the constants
     * defined in this interface.
     *
     * @param style One of the RENDER_* constants from LayerManager or
     *    Xj3DBrowser
     * @throws IllegalArgumentException A style constant that is not recognized
     *    by the implementation was provided
     */
    @Override
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {

        renderingStyle = style;
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants from LayerManager or Xj3DBrowser
     */
    @Override
    public int getRenderingStyle() {
        return renderingStyle;
    }

    /**
     * Change the rendering style that the browser should currently be using
     * for for a specific layer. Various options are available based on the
     * constants defined in this interface.
     *
     * @param style One of the RENDER_* constants
     * @param layerId The ID of the layer that should have the style changed
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    @Override
    public void setRenderingStyle(int style, int layerId)
        throws IllegalArgumentException {

        // ignored for now.
    }

    /**
     * Get the currently set rendering style for a specific layer. The default
     * style is RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    @Override
    public int getRenderingStyle(int layerId) {
        return renderingStyle;
    }

    /**
     * Set the list of current layers that should be rendered.
     *
     * @param layers The list of layer managers to be rendered
     * @param numLayers The number of active items in the list
     */
    @Override
    public void setActiveLayers(LayerManager[] layers, int numLayers) {
    }

    /**
     * Set the rendering order for all the layers on this manager
     *
     * @param order The index of the list of rendered layers ids
     * @param numValid The number of valid items in the order list
     */
    @Override
    public void setRenderOrder(int[] order, int numValid) {
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    @Override
    public void shutdown() {
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
    }

    /**
     * Force clearing all currently managed layers from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    @Override
    public void clear() {
    }
}

