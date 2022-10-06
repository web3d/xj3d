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
// none

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Performs an Absolute scale on the coordinates during the filter.  The
 * filter converts the points of a Coordinate node.
 *
 *
 * @author Russell Dodds
 * @version $Revision: 1.10 $
 */
public class AbsScaleFilter extends EncodedBaseFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The scale argument option identifier */
    private static final String SCALE_ARG = "-scale";

    /** The logging identifier of this app */
    private static final String LOG_NAME = "AbsScaleFilter";

    /** The default scale */
    private static final float DEFAULT_SCALE = 1.0f;

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /** The scale of the scene */
    private float scale;

    /**
     * Create a new instance of this filter
     */
    public AbsScaleFilter() {
		
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
				
				// scale
				float[] point = coord.point;
				int num_coord = coord.num_point * 3;
				for (int i = 0; i < num_coord; i++) {
					point[i] *= scale;
				}
				
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
    // AbstractFilter Methods
    //---------------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {

        int argIndex = -1;
        String scaleArg = String.valueOf(DEFAULT_SCALE);

        //////////////////////////////////////////////////////////////////////
        // parse the arguments
        for (int i = 0; i < arg.length; i++) {
            String argument = arg[i];
            if (argument.startsWith("-")) {
                try {
                    if (argument.equals(SCALE_ARG)) {
                        scaleArg = arg[i+1];
                        argIndex = i+1;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        LOG_NAME + ": Error parsing filter arguments");
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        // validate the arguments
        if (scaleArg != null) {

            try {
                scale = Float.parseFloat(scaleArg);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        LOG_NAME + ": Illegal value for argument: " + scaleArg);
            }

        } else {
            scale = DEFAULT_SCALE;
        }
    }
}
