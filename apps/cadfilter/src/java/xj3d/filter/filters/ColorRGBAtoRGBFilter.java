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
import org.j3d.util.I18nManager;
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Downsamples a ColorRGBA node to Color node, by stripping the alpha
 * component.
 *
 * <b>Filter Options</b><p>
 * None.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ColorRGBAtoRGBFilter extends EncodedBaseFilter {

    /** The logging identifier of this app */
    private static final String LOG_NAME = "ColorRGBAtoRGBFilter";

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /** Flag indicating a transparent value was present */
    private boolean transparentFound;

    /**
     * Recenters the geometry
     */
    public ColorRGBAtoRGBFilter() {
		intercept = false;
        transparentFound = false;
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

        if (name.equals("ColorRGBA")) {
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

            if (nodeName.equals("ColorRGBA")) {
				// a node that we are responsible for has ended.

				// get it's encoded object
            	ColorRGBA color_rgba = (ColorRGBA)encStack.peek();

				// clean up the super's state (before enabling again)
				super.endNode();

				// build the new node
				float[] rgb = processColors(color_rgba.color, color_rgba.num_color);
				Color color = (Color)factory.getEncodable("Color", color_rgba.defName);
				color.setValue("color", rgb, rgb.length);

				// push it along....
				color.encode();

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

    @Override
    public void endDocument() throws SAVException, VRMLException {
        if (transparentFound) {
            I18nUtils.printMsg("xj3d.filter.filters.ColorRGBAToRGBFilter", I18nUtils.EXT_MSG,
                    null);
        }
        super.endDocument();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Trim the RGBA field value down to RGBA. Not quite a decimation, but
     * a quadimation?
     *
     * @param colors The colour list to process
     */
    private float[] processColors(float[] colors, int len) {

        int shorten_len = len * 3;
        float[] rgb = new float[shorten_len];

        int in_idx = 0;
        int out_idx = 0;

        for(int i = 0; i < len; i++) {
            rgb[out_idx] = colors[in_idx];
            rgb[out_idx + 1] = colors[in_idx + 1];
            rgb[out_idx + 2] = colors[in_idx + 2];

            if (colors[in_idx + 3] != 1) {
                transparentFound = true;
            }
            in_idx += 4;
            out_idx += 3;
        }

		return(rgb);
    }
}
