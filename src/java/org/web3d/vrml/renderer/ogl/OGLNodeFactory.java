
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

package org.web3d.vrml.renderer.ogl;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.renderer.DefaultNodeFactory;

/**
 * Singleton implementation of the {@link org.web3d.vrml.lang.VRMLNodeFactory}
 * that produces OpenGL implementations of the nodes.
 * <p>
 * Changed from a singleton to support multi-threading.  See details
 * in DefaultNodeFactory.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLNodeFactory extends DefaultNodeFactory {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private OGLNodeFactory() {
        super(OPENGL_RENDERER);
    }

    /**
     * Get the current instance of this factory class. If an instance has
     * not been created yet, this will automatically create it
     *
     * @return The global instance of the factory
     */
    public static OGLNodeFactory getOGLNodeFactory() {
        return new OGLNodeFactory();
    }
}
