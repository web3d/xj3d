/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
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
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.exporter.TriangleCounter;

import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * Counts the number of triangles in a world. The current
 * implementation will process the following nodes:
 * <p>
 * <ul>
 * <li>ElevationGrid</li>
 * <li>IndexedFaceSet</li>
 * <li>IndexedQuadSet</li>
 * <li>IndexedTriangleFanSet</li>
 * <li>IndexedTriangleSet</li>
 * <li>IndexedTriangleStripSet</li>
 * <li>QuadSet</li>
 * <li>TriangleFanSet</li>
 * <li>TriangleSet</li>
 * <li>TriangleStripSet</li>
 * </ul>
 * <p>
 *
 * <b>Filter Options</b>
 * <br>
 * <code>none</code>
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class TriangleCountInfoFilter extends CommonEncodedBaseFilter implements TriangleCounter {

    /** DEF's */
    private HashMap<String, CommonEncodable> defMap;

    /** The triangle count */
    private int triCnt;

    /**
     * Create an instance of the filter.
     */
    public TriangleCountInfoFilter() {

        defMap = new HashMap<>(100);

        triCnt = 0;
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

        defMap.clear();
        super.endDocument();

        System.out.println("Total Triangle Cnt: " + triCnt);
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

        super.startNode(name, defName);

        if (defName != null) {
            CommonEncodable ce = (CommonEncodable)encStack.peek();
            defMap.put(defName, ce);
        }
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

        CommonEncodable node = (CommonEncodable)encStack.peek();
        if (node.isType(TYPE.X3DGeometryNode)) {

            String nodeName = node.getNodeName();
            node = checkUse(node);

            switch (nodeName) {
                case "IndexedFaceSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData index_data = (ArrayData)node.getValue("coordIndex");
                            if (index_data != null) {
                                triCnt += numTriByIndex(index_data);
                            }
                        }
                    }       break;
                    }
                case "IndexedTriangleSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData index_data = (ArrayData)node.getValue("index");
                            if (index_data != null) {
                                int num_index = index_data.num;
                                triCnt += num_index / 3;
                            }
                        }
                }       break;
                    }
                case "TriangleSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            int num_point = point_data.num;
                            triCnt += num_point / 9;
                    }
                }       break;
                    }
                case "IndexedTriangleStripSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData index_data = (ArrayData)node.getValue("index");
                            if (index_data != null) {
                                triCnt += numTriByIndex(index_data);
                            }
                    }
                }       break;
                }
                case "TriangleStripSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData count_data = (ArrayData)node.getValue("stripCount");
                            if (count_data != null) {
                                triCnt += numTriByCount(count_data);
                            }
                    }
                }       break;
                }
                case "IndexedTriangleFanSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData index_data = (ArrayData)node.getValue("index");
                            if (index_data != null) {
                                triCnt += numTriByIndex(index_data);
                        }
                    }
                    }       break;
                }
                case "TriangleFanSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData count_data = (ArrayData)node.getValue("fanCount");
                            if (count_data != null) {
                                triCnt += numTriByCount(count_data);
                        }
                    }
                    }       break;
                }
                case "IndexedQuadSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            ArrayData index_data = (ArrayData)node.getValue("index");
                            if (index_data != null) {
                                int num_index = index_data.num;
                                triCnt += num_index / 4;
                        }
                        }
                    }       break;
                }
                case "QuadSet":{
                    CommonEncodable coord = checkUse((CommonEncodable)node.getValue("coord"));
                    if (coord != null) {
                        ArrayData point_data = (ArrayData)coord.getValue("point");
                        if (point_data != null) {
                            int num_point = point_data.num;
                        triCnt += num_point / 12;
                        }
                    }       break;
                }
                case "ElevationGrid":
                    ArrayData height_data = (ArrayData)node.getValue("height");
                    if (height_data != null) {
                        Integer xDim = (Integer)node.getValue("xDimension");
                        Integer zDim = (Integer)node.getValue("zDimension");
                        if ((xDim != null) && (zDim != null)) {
                            int num_col = xDim - 1;
                            int num_row = zDim - 1;
                            if ((num_col > 0) && (num_row > 0)) {
                            triCnt += (num_col * num_row * 2);
                        }
                        }
                    }
                    break;
            }
        }
        super.endNode();
    }

    //----------------------------------------------------------
    // Methods defined by TriangleCounter
    //----------------------------------------------------------

    /**
     * Get the triangle count for the whole stream.
     *
     * @return The triangle count.
     */
    @Override
    public int getTriangleCount() {
        return triCnt;
    }

    /**
     * Determine whether the argument node use's a def'ed node.
     * Return the def if so, otherwise return the argument
     */
    private CommonEncodable checkUse(CommonEncodable ce) {
        CommonEncodable rval = null;
        if (ce != null) {
            String useName = ce.getUseName();
            if (useName != null) {
                rval = defMap.get(useName);
            } else {
                rval = ce;
            }
        }
        return(rval);
    }

    /**
     * Return the number of triangles for non-indexed strip/fan type geometry.
     *
     * @param count_data The count field from a strip/fan type geometry.
     * @return The number of triangles.
     */
    private int numTriByCount(ArrayData count_data) {
        int num_tri = 0;
        if (count_data != null) {
            int num_count = count_data.num;
            int[] count = (int[])count_data.data;
            for (int i = 0; i < num_count; i++) {
                int cnt = count[i];
                if (cnt >= 3) {
                    num_tri += (cnt - 2);
                }
            }
        }
        return(num_tri);
    }

    /**
     * Return the number of triangles for indexed strip/fan type geometry.
     *
     * @param index_data The index field from an indexed strip/fan type geometry.
     * @return The number of triangles.
     */
    private int numTriByIndex(ArrayData index_data) {
        int num_tri = 0;
        if (index_data != null) {
            int num_index = index_data.num;
            int[] index = (int[])index_data.data;
            int strip_cnt = 0;
            for (int i = 0; i < num_index; i++) {
                if (index[i] == -1) {
                    if (strip_cnt >= 3) {
                        num_tri += (strip_cnt - 2);
                    }
                    strip_cnt = 0;
                } else {
                    strip_cnt++;
                }
            }
            if (strip_cnt > 0) {
                // if the last run of indices was not terminated by a -1
                if (strip_cnt >= 3) {
                    num_tri += (strip_cnt - 2);
                }
            }
        }
        return(num_tri);
    }
}
