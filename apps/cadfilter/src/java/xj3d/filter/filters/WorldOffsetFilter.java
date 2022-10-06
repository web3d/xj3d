/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
// None

// Local imports
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.BaseFilter;

/**
 * Takes the contents of the entire file and offsets it by the given
 * argument amount by placing a TransformGroup at the root of the scene
 * graph that encapsulates the entire world.
 * <p>
 *
 * <b>Filter Options</b>
 * <p>
 * <code>worldOffset x y z</code> The amount to shift in each direction.
 * <p>
 * <code>worldScale x y z</code> The amount to scale in each direction.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class WorldOffsetFilter extends BaseFilter {

    /** The scale argument option identifier */
    private static final String OFFSET_ARG = "-worldOffset";

    /** The scale argument option identifier */
    private static final String SCALE_ARG = "-worldScale";

    /** The logging identifier of this app */
    private static final String LOG_NAME = "WorldOffsetFilter";

    /** The amount to move the world */
    private float[] offsetAmount;

    /** The amount to scale the world */
    private float[] scaleAmount;

    /** This boolean flag ensures that we only start the transform Node once */
    private boolean ranOnce;

    /**
     * Recenters the geometry
     */
    public WorldOffsetFilter() {
        offsetAmount = new float[3];
        scaleAmount = new float[3];

        scaleAmount[0] = 1;
        scaleAmount[1] = 1;
        scaleAmount[2] = 1;
        ranOnce = false;
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        super.startDocument(uri,url, encoding, type, version, comment);

    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        // end the root transform's child field and node
        contentHandler.endField();
        contentHandler.endNode();

        super.endDocument();
    }

    //---------------------------------------------------------------
    // AbstractFilter Methods
    //---------------------------------------------------------------


    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if ( !ranOnce ) {

            contentHandler.startNode("Transform", null);
            contentHandler.startField("translation");

            fieldHandler.setFieldValue("Transform",
                                       "translation",
                                       offsetAmount,
                                       3);

            contentHandler.startField("scale");
            fieldHandler.setFieldValue("Transform",
                                       "scale",
                                       scaleAmount,
                                       3);

            contentHandler.startField("children");

            ranOnce = true;
        }

        super.startNode(name, defName);
    }


    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {

        String offset_x_arg = null;
        String offset_y_arg = null;
        String offset_z_arg = null;

        String scale_x_arg = null;
        String scale_y_arg = null;
        String scale_z_arg = null;

        //////////////////////////////////////////////////////////////////////
        //
        // parse the arguments
        //
        for(int i = 0; i < arg.length; i++) {
            String argument = arg[i];
            if(argument.startsWith("-")) {
                try {
                    switch (argument) {
                        case OFFSET_ARG:
                            offset_x_arg = arg[i + 1];
                            offset_y_arg = arg[i + 2];
                            offset_z_arg = arg[i + 3];
                            i += 3;  // skip the next one that we've just read
                            break;
                        case SCALE_ARG:
                            scale_x_arg = arg[i + 1];
                            scale_y_arg = arg[i + 2];
                            scale_z_arg = arg[i + 3];
                            i += 3;  // skip the next one that we've just read
                            break;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        LOG_NAME + ": Error parsing filter arguments");
                }
            }
        }

        // validate the arguments
        if(offset_x_arg != null) {

            try {
                offsetAmount[0] = Float.parseFloat(offset_x_arg);
                offsetAmount[1] = Float.parseFloat(offset_y_arg);
                offsetAmount[2] = Float.parseFloat(offset_z_arg);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    LOG_NAME + ": Illegal value for argument " + OFFSET_ARG);
            }
        }

        if(scale_x_arg != null) {

            try {
                scaleAmount[0] = Float.parseFloat(scale_x_arg);
                scaleAmount[1] = Float.parseFloat(scale_y_arg);
                scaleAmount[2] = Float.parseFloat(scale_z_arg);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    LOG_NAME + ": Illegal value for argument " + SCALE_ARG);
            }
        }
    }
}
