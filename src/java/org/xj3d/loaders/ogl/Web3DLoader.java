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

package org.xj3d.loaders.ogl;

// External imports
import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

// Local imports
import org.web3d.net.protocol.Web3DResourceFactory;

/**
 * A Aviatrix3D file loader implementation for reading all Web3D file formats
 * building a scenegraph with them.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class Web3DLoader extends BaseLoader {

    /**
     * Construct a default loader implementation with no flags set. When asked
     * to load a file it will not produce anything unless flags have been
     * set through the <code>setFlags()</code> method.
     */
    public Web3DLoader() {
        vrml97Only = false;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseLoader
    //---------------------------------------------------------------

    /**
     * Set up the system properties needed to run the browser within the
     * context of a privileged block.
     */
    @Override
    void setupPropertiesProtected() {

        URIResourceStreamFactory res_fac =
            URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof Web3DResourceFactory)) {
            res_fac = new Web3DResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
    }

}
