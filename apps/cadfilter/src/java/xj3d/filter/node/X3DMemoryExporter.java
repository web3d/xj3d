/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
import org.web3d.vrml.sav.*;

// Local imports
import org.web3d.vrml.lang.VRMLException;

/**
 * An X3D exporter that makes a memory graph of the output.
 * Returns a CommonScene representation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.0 $
 */
public class X3DMemoryExporter extends CommonEncodedBaseFilter {

    /**
     * Construct a default instance of the field handler
     */
    public X3DMemoryExporter() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param debug Should we run in debug mode
     */
    public X3DMemoryExporter(boolean debug) {
        super(debug);

    }

    /**
     * Get the scene.
     *
     * @return The scene
     */
    public CommonScene getScene() {
        return scene;
    }

    public CommonEncodableFactory getEncodableFactory() {
        return factory;
    }

    /**
     * Clear the retained scene
     */
    public void clear() {
        scene = null;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        super.endDocument();
        encStack.clear();
        encMap.clear();

        // Do not clear the scene, call clear explicity
        //scene = null;
    }

}
