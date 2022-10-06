/*****************************************************************************
 *                        xj3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters.manifold;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * Check that each geometry consists of a manifold (watertight) mesh.
 * <p>
 * The following node types are checked:
 * <ul>
 * <li>IndexedTriangleSet</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class ManifoldInfoFilter extends CommonEncodedBaseFilter {

    /**
     * Identifier
     */
    private static final String LOG_NAME = "ManifoldInfo";

    /**
     * DEF's
     */
    private Map<String, CommonEncodable> defMap;

    /**
     * Create an instance of the filter.
     */
    public ManifoldInfoFilter() {
        defMap = new HashMap<>();
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        defMap.clear();

        super.endDocument();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not given
     * for this node.
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
            throws SAVException, VRMLException {

        super.startNode(name, defName);

        if (defName != null) {
            if (name.equals("Coordinate")) {

                CommonEncodable ce = (CommonEncodable) encStack.peek();
                defMap.put(defName, ce);
            }
        }
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

        CommonEncodable ce = (CommonEncodable) encStack.peek();
        if (ce.isType(TYPE.X3DGeometryNode)) {
            validate(ce);
        }

        super.endNode();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------
    
    /**
     * Determine whether the argument geometry type node consists of a manifold
     * (watertight) mesh.
     *
     * @param ce The geometry node to check
     */
    private void validate(CommonEncodable ce) {

        String nodeName = ce.getNodeName();
        if (nodeName.equals("IndexedTriangleSet")) {

            ArrayData index_data = (ArrayData) ce.getValue("index");
            if (index_data != null) {
                int[] index_array = (int[]) index_data.data;
                CommonEncodable coord = (CommonEncodable) ce.getValue("coord");

                if (coord != null) {
                    String useName = coord.getUseName();
                    if (useName != null) {
                        coord = defMap.get(useName);
                    }
                    ArrayData point_data = (ArrayData) coord.getValue("point");
                    if (point_data != null) {
                        float[] coord_array = (float[]) point_data.data;

                        ITSData its = new ITSData(coord_array, index_array, null);
                        ValidateITSData validator = new ValidateITSData(its);
                        boolean isManifold = validator.check();
                        System.out.println(LOG_NAME + ": node " + nodeName + " isManifold: " + isManifold);
                    }
                }
            }
        }
    }
}
