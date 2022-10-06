/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Processes the world and calculates the global bounds of the entire world.
 * <p>
 *
 * Assumes that the FlattenTransform filter is run before this and has squashed
 * all the objects down to single shape nodes that are at the file root. Works
 * on the coordinates of the geometry, not any explicit bbox definitions.
 * <p>
 *
 * Output is through the message reporter and generates it in the order
 * center x, center y, center z, size x, size y, size z, where size is the total
 * length along that axis, based on the center.
 *
 * <b>Filter Options</b><p>
 * None.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class GlobalBoundsFilter extends EncodedBaseFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The logging identifier of this app */
    private static final String LOG_NAME = "GlobalBoundsFilter";

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /** min bounding point of the object in the order x,y,z */
    private float[] minBound;

    /** max bounding point of the object in the order x,y,z */
    private float[] maxBound;

    /**
     * Recenters the geometry
     */
    public GlobalBoundsFilter() {
		
        intercept = false;

        minBound = new float[3];
        maxBound = new float[3];
		
		// disable encoding, only encode nodes of the required types
		encode(false);
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

        // Reset the bounds at the start of the run
        minBound[0] = Float.POSITIVE_INFINITY;
        minBound[1] = Float.POSITIVE_INFINITY;
        minBound[2] = Float.POSITIVE_INFINITY;

        maxBound[0] = Float.NEGATIVE_INFINITY;
        maxBound[1] = Float.NEGATIVE_INFINITY;
        maxBound[2] = Float.NEGATIVE_INFINITY;
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

        super.endDocument();

        float center_x = (maxBound[0] + minBound[0]) * 0.5f;
        float center_y = (maxBound[1] + minBound[1]) * 0.5f;
        float center_z = (maxBound[2] + minBound[2]) * 0.5f;

        float size_x = maxBound[0] - minBound[0];
        float size_y = maxBound[1] - minBound[1];
        float size_z = maxBound[2] - minBound[2];

        // Print the bounds out for someone to use
        StringBuilder bldr = new StringBuilder();
        bldr.append(center_x);
        bldr.append(' ');
        bldr.append(center_y);
        bldr.append(' ');
        bldr.append(center_z);
        bldr.append(' ');

        bldr.append(size_x);
        bldr.append(' ');
        bldr.append(size_y);
        bldr.append(' ');
        bldr.append(size_z);
        bldr.append(' ');

        System.out.println(bldr.toString());

        // Don't use error reporter because we specifically want this written
        // to stdout all the time.
        //errorHandler.messageReport(bldr.toString());
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (name.equals("Coordinate")) {
			intercept = true;
			encode(true);
			suppressCalls(true);
        }

        super.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

		if (intercept) {
			
			NodeMarker marker = (NodeMarker)nodeStack.peek();
			String nodeName = marker.nodeName;
		
            if (nodeName.equals("Coordinate")) {
				// a node that we are responsible for has ended.
				
				// get it's encoded object
            	Coordinate coord = (Coordinate)encStack.peek();
				
				// clean up the super's state (before enabling again)
				super.endNode();
				
				// calculate
				processCoords(coord.point, coord.num_point);
				
				// push it along....
				coord.encode();
				
				// return to 'idle' mode
                intercept = false;
				encode(false);
				suppressCalls(false);
				
			} else {
				super.endNode();
			}
        } else {
            super.endNode();
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Take the array of points, find the bounds and then recenter and generate
     * the output from there.
     *
     * @param coords The list of coordinates to look at
     * @param numCoords The number of coords to process from the list
     */
    private void processCoords(float[] coords, int numCoords) {
		
        float min_x = Float.POSITIVE_INFINITY;
        float min_y = Float.POSITIVE_INFINITY;
        float min_z = Float.POSITIVE_INFINITY;

        float max_x = Float.NEGATIVE_INFINITY;
        float max_y = Float.NEGATIVE_INFINITY;
        float max_z = Float.NEGATIVE_INFINITY;

        int idx = 0;

        for(int i = 0; i < numCoords; i++) {
            if(coords[idx] < min_x)
                min_x = coords[idx];

            if(coords[idx] > max_x)
                max_x = coords[idx];

            if(coords[idx + 1] < min_y)
                min_y = coords[idx + 1];

            if(coords[idx + 1] > max_y)
                max_y = coords[idx + 1];

            if(coords[idx + 2] < min_z)
                min_z = coords[idx + 2];

            if(coords[idx + 2] > max_z)
                max_z = coords[idx + 2];

            idx += 3;
        }

        // Now update the world bounds
        if(minBound[0] > min_x)
            minBound[0] = min_x;

        if(minBound[1] > min_y)
            minBound[1] = min_y;

        if(minBound[2] > min_z)
            minBound[2] = min_z;

        if(maxBound[0] < max_x)
            maxBound[0] = max_x;

        if(maxBound[1] < max_y)
            maxBound[1] = max_y;

        if(maxBound[2] < max_z)
            maxBound[2] = max_z;
    }
}
