/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
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
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.parser.DefaultFieldParserFactory;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.ScriptHandler;
import org.web3d.vrml.sav.SAVException;

import xj3d.filter.AbstractFilter;
import xj3d.filter.FieldValueHandler;

import xj3d.filter.node.*;

/**
 * Filter for combining multiple Shapes into a single Shape node.
 * <p>
 * The input to this filter is presumed to have been run through
 * the FlattenTransformFilter. Input must have the Shape nodes as
 * the root nodes of the Scene, otherwise they will be ignored.
 * At present, only IndexedTriangleSets are combined and output.
 *
 * @author Rex Melton
 * @version $Revision: 1.7 $
 */
public class CombineShapeFilter extends EncodedBaseFilter {

    /** Geometry node wrapper converter */
    private GeometryConverter converter;

    /** Have we issued the suppress.  */
    private boolean issuedSuppress;

    /**
     * Create an instance of the filter.
     */
    public CombineShapeFilter() {

        converter = new GeometryConverter(factory);
        issuedSuppress = false;
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

        suppressCalls(false);

        Encodable[] enc = scene.getRootNodes();
        combine(enc);

		scene = null;

        super.endDocument();
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (!issuedSuppress) {
            suppressCalls(true);
            issuedSuppress = true;
        }

        super.startNode(name, defName);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Walk the nodes, combine the coordinates and indices of the Shapes,
     * then encode them into a single Shape.
     *
     * @param enc An array of nodes
     */
    private void combine(Encodable[] enc) {

        ArrayList<Shape> shapeList = new ArrayList<>();
        for (Encodable node : enc) {
            if (node instanceof Shape) {
                Shape shape = (Shape)node;
                IGeometry geometry = (IGeometry)shape.getGeometry();

                // Only add shape if geometry has coordinates and
                if (geometry instanceof IndexedTriangleSet) {
                    IndexedTriangleSet its = (IndexedTriangleSet) geometry;
                    if (!isEmptyITS(its)) {
                        shapeList.add(shape);
                    }
                } else if (geometry instanceof IndexedTriangleFanSet) {
                    IndexedTriangleSet its = converter.toITS((IndexedTriangleFanSet)geometry);
                    if (!isEmptyITS(its)) {
                        shape.setGeometry(its);
                        shapeList.add(shape);
                    }
                } else if (geometry instanceof IndexedTriangleStripSet) {
                    IndexedTriangleSet its = converter.toITS((IndexedTriangleStripSet)geometry);
                    if (!isEmptyITS(its)) {
                        shape.setGeometry(its);
                        shapeList.add(shape);
                    }
                }
            } else if (node instanceof Viewpoint) {
                Viewpoint viewpoint = (Viewpoint)node;
                viewpoint.encode();
            }
        }
        // get all the coordinates and indices
        int num_shapes = shapeList.size();
        if (num_shapes > 0) {
            float[][] point = new float[num_shapes][];
            int[] num_point = new int[num_shapes];
            int[][] index = new int[num_shapes][];
            int[] num_index = new int[num_shapes];
            int total_num_point = 0;
            int total_num_index = 0;
            for (int i = 0; i < num_shapes; i++) {
                Shape shape = shapeList.get(i);
                IndexedTriangleSet its = (IndexedTriangleSet)shape.getGeometry();
                Coordinate c = (Coordinate)its.getCoordinate();
                point[i] = c.point;
                num_point[i] = c.num_point;
                total_num_point += num_point[i];
                index[i] = its.index;
                num_index[i] = its.num_index;
                total_num_index += num_index[i];
            }
            // put all the coordinates into a single array
            int idx = 0;
            float[] all_point = new float[total_num_point*3];
            for (int i = 0; i < num_shapes; i++) {
                int num_floats = num_point[i]*3;
                System.arraycopy(point[i], 0, all_point, idx, num_floats);
                idx += num_floats;
            }
            // adjust the indices to account for the concatenated coordinates
            int offset = 0;
            for (int i = 1; i < num_shapes; i++) {
                offset += num_point[i-1];
                for (int j = 0; j < num_index[i]; j++) {
                    index[i][j] += offset;
                }
            }
            // put all the indices into a single array
            idx = 0;
            int[] all_index = new int[total_num_index];
            for (int i = 0; i < num_shapes; i++) {
                System.arraycopy(index[i], 0, all_index, idx, num_index[i]);
                idx += num_index[i];
            }
            //////////////////////////////////////////////////////////////////////
            // skip the re-indexing for now.....
            //CoordinateProcessor cp = new CoordinateProcessor(all_point);
            //if(cp.hasDuplicates()) {
            //    cp.processIndices(all_index);
            //    total_num_point = cp.getNumCoords();
            //}
            //////////////////////////////////////////////////////////////////////
            // reuse the first shape (i.e. it's Appearance)
            Shape shape = shapeList.get(0);
            IndexedTriangleSet its = (IndexedTriangleSet)shape.getGeometry();
            its.index = all_index;
            its.num_index = total_num_index;
            Coordinate c = (Coordinate)its.getCoordinate();
            c.point = all_point;
            c.num_point = total_num_point;
            its.setColor(null);
            its.setNormal(null);
            its.setTextureCoordinate(null);
            shape.encode();
        }
    }

    private boolean isEmptyITS(IndexedTriangleSet its) {
    	Coordinate c = (Coordinate)its.getCoordinate();
    	if (c == null || c.point == null || its.index == null) {
    		return true;
    	}
    	return false;
    }
}
