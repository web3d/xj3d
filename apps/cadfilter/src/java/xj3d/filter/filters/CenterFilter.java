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

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Processes each geometry item and centers it in it's local coordinate system
 * based on the bounds of the coordinates.
 * <p>
 *
 * <b>Filter Options</b><p>
 * None.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class CenterFilter extends EncodedBaseFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The logging identifier of this app */
    private static final String LOG_NAME = "CenterFilter";

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /**
     * Recenters the geometry
     */
    public CenterFilter() {
        
		intercept = false;
		// disable encoding, only encode nodes of the required types
		encode(false);
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

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
				
				// center
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

        // The center is where we need to offset to
        float offset_x = (max_x + min_x) * -0.5f;
        float offset_y = (max_y + min_y) * -0.5f;
        float offset_z = (max_z + min_z) * -0.5f;

        idx = 0;

        for(int i = 0; i < numCoords; i++) {
            coords[idx] += offset_x;
            coords[idx + 1] += offset_y;
            coords[idx + 2] += offset_z;

            idx += 3;
        }
    }
}
