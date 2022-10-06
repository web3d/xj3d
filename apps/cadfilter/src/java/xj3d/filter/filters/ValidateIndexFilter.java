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
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.FilterExitCodes;
import xj3d.filter.FilterProcessingException;

import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * For indexed type geometry nodes, check that the values
 * contained in the Coordinate, Normal, Color and TextureCoordinate
 * nodes are valid for the index for those values. If an invalid
 * condition is found, a FilterProcessingException is thrown.
 * <p>
 * The following node types are checked:
 * <ul>
 * <li>IndexedFaceSet</li>
 * <li>IndexedLineSet</li>
 * <li>IndexedQuadSet</li>
 * <li>IndexedTriangleFanSet</li>
 * <li>IndexedTriangleSet</li>
 * <li>IndexedTriangleStripSet</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class ValidateIndexFilter extends CommonEncodedBaseFilter {

    /**
     * Identifier
     */
    private static final String LOG_NAME = "ValidateIndex";

    /**
     * DEF's
     */
    private Map<String, CommonEncodable> defMap;

    /**
     * Coordinate counts
     */
    private Map<CommonEncodable, Integer> countMap;

    /**
     * Create an instance of the filter.
     */
    public ValidateIndexFilter() {

        defMap = new HashMap<>(100);
        countMap = new HashMap<>(100);
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
        countMap.clear();

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
            if (name.equals("Coordinate")
                    || name.equals("Normal")
                    || name.equals("Color")
                    || name.equals("TextureCoordinate")
                    || name.equals("MultiTextureCoordinate")) {

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
     * Determine whether the argument geometry type node is indexed. If so, then
     * check that the number of coordinates is sufficient to cover the max
     * index.
     *
     * @param ce An X3DGeometryType node
     */
    private void validate(CommonEncodable ce) {

        String nodeName = ce.getNodeName();
        switch (nodeName) {
            case "IndexedTriangleSet":
            case "IndexedTriangleFanSet":
            case "IndexedTriangleStripSet":
            case "IndexedQuadSet":
                ArrayData index_data = (ArrayData) ce.getValue("index");
                if (index_data != null) {
                    int max_index = getMaxIndex(index_data);

                    CommonEncodable coord = (CommonEncodable) ce.getValue("coord");
                    int num_point = getNumCoord(coord);

                    if (num_point <= max_index) {
                        throw new FilterProcessingException(
                                LOG_NAME,
                                FilterExitCodes.INVALID_INPUT_FILE,
                                "An " + nodeName + " contains too few coordinates.  Verts: " + num_point + " max index: " + max_index);
                    }

                    CommonEncodable normal = (CommonEncodable) ce.getValue("normal");
                    if (normal != null) {

                        int num_vector = getNumVector(normal);
                        if (num_vector <= max_index) {
                            throw new FilterProcessingException(
                                    LOG_NAME,
                                    FilterExitCodes.INVALID_INPUT_FILE,
                                    "An " + nodeName + " contains too few normals.  Normals: " + num_vector + " max index: " + max_index);
                        }
                    }

                    CommonEncodable color = (CommonEncodable) ce.getValue("color");
                    if (color != null) {

                        int num_color = getNumColor(color);
                        if (num_color <= max_index) {
                            throw new FilterProcessingException(
                                    LOG_NAME,
                                    FilterExitCodes.INVALID_INPUT_FILE,
                                    "An " + nodeName + " contains too few colors.  Colors: " + num_point + " max index: " + max_index);
                        }
                    }

                    CommonEncodable texCoord = (CommonEncodable) ce.getValue("texCoord");
                    if (texCoord != null) {

                        String tc_name = texCoord.getNodeName();
                        if (tc_name.equals("MultiTextureCoordinate")) {
                            CommonEncodable multiTexCoord = texCoord;

                            @SuppressWarnings("unchecked") // cast from Object type
                            List<CommonEncodable> tx_list
                                    = (List<CommonEncodable>) multiTexCoord.getValue("texCoord");
                            int num_tc = tx_list.size();
                            for (int i = 0; i < num_tc; i++) {
                                texCoord = tx_list.get(i);
                                if (texCoord != null) {
                                    int num_texCoord = getNumTexCoord(texCoord);
                                    if (num_texCoord <= max_index) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains too few texture coordinates. TexCoords: " + num_texCoord + " max index: " + max_index);
                                    }
                                }
                            }
                        } else {
                            int num_texCoord = getNumTexCoord(texCoord);
                            if (num_texCoord <= max_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few texture coordinates. TexCoords: " + num_texCoord + " max index: " + max_index);
                            }
                        }
                    }
                }
                break;
            case "IndexedFaceSet": {
                int max_coord_index = 0;
                ArrayData coord_index_data = (ArrayData) ce.getValue("coordIndex");
                if (coord_index_data != null) {
                    max_coord_index = getMaxIndex(coord_index_data);

                    CommonEncodable coord = (CommonEncodable) ce.getValue("coord");
                    int num_point = getNumCoord(coord);

                    if (num_point <= max_coord_index) {
                        throw new FilterProcessingException(
                                LOG_NAME,
                                FilterExitCodes.INVALID_INPUT_FILE,
                                "An " + nodeName + " contains too few coordinates");
                    }
                }
                CommonEncodable color = (CommonEncodable) ce.getValue("color");
                if (color != null) {

                    int num_color = getNumColor(color);

                    ArrayData color_index_data = (ArrayData) ce.getValue("colorIndex");

                    boolean colorPerVertex = true;
                    Object value = ce.getValue("colorPerVertex");
                    if ((value != null) && (value instanceof Boolean)) {
                        colorPerVertex = (Boolean) value;
                    }
                    if (!colorPerVertex) {
                        int num_faces = getNumFaces(coord_index_data);
                        if (color_index_data != null) {

                            int num_color_index = color_index_data.num;
                            if (num_color_index < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few color indices");
                            }
                            int max_color_index = getMaxIndex(color_index_data);
                            if (num_color <= max_color_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                            int[] color_index = (int[]) color_index_data.data;
                            for (int i = 0; i < num_color_index; i++) {
                                if (color_index[i] == -1) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains a color index of -1");
                                }
                            }
                        } else {
                            if (num_color < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains fewer colors than faces");
                            }
                        }
                    } else {
                        if (color_index_data != null) {
                            if (coord_index_data != null) {
                                int num_coord_index = coord_index_data.num;
                                if (color_index_data.num < num_coord_index) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains fewer color indices than coord indices");
                                }
                                int[] coord_index = (int[]) coord_index_data.data;
                                int[] color_index = (int[]) color_index_data.data;
                                for (int i = 0; i < num_coord_index; i++) {
                                    if ((coord_index[i] == -1) && (color_index[i] != -1)) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains mismatched color and coord indices");
                                    }
                                }
                            } else {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains no coordIndex field");
                            }
                            int max_color_index = getMaxIndex(color_index_data);
                            if (num_color <= max_color_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                        } else {
                            if (num_color <= max_coord_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                        }
                    }
                }
                CommonEncodable normal = (CommonEncodable) ce.getValue("normal");
                if (normal != null) {

                    int num_normal = getNumVector(normal);

                    ArrayData normal_index_data = (ArrayData) ce.getValue("normalIndex");

                    boolean normalPerVertex = true;
                    Object value = ce.getValue("normalPerVertex");
                    if ((value != null) && (value instanceof Boolean)) {
                        normalPerVertex = (Boolean) value;
                    }
                    if (!normalPerVertex) {
                        int num_faces = getNumFaces(coord_index_data);
                        if (normal_index_data != null) {

                            int num_normal_index = normal_index_data.num;
                            if (num_normal_index < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few normal indices");
                            }
                            int max_normal_index = getMaxIndex(normal_index_data);
                            if (num_normal <= max_normal_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few normals");
                            }
                            int[] normal_index = (int[]) normal_index_data.data;
                            for (int i = 0; i < num_normal_index; i++) {
                                if (normal_index[i] == -1) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains a normal index of -1");
                                }
                            }
                        } else {
                            if (num_normal < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains fewer normals than faces");
                            }
                        }
                    } else {
                        if (normal_index_data != null) {
                            if (coord_index_data != null) {
                                int num_coord_index = coord_index_data.num;
                                if (normal_index_data.num < num_coord_index) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains fewer normal indices than coord indices");
                                }
                                int[] coord_index = (int[]) coord_index_data.data;
                                int[] normal_index = (int[]) normal_index_data.data;
                                for (int i = 0; i < num_coord_index; i++) {
                                    if ((coord_index[i] == -1) && (normal_index[i] != -1)) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains mismatched normal and coord indices");
                                    }
                                }
                            } else {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains no coordIndex field");
                            }
                            int max_normal_index = getMaxIndex(normal_index_data);
                            if (num_normal <= max_normal_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few normals");
                            }
                        } else {
                            if (num_normal <= max_coord_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few normals");
                            }
                        }
                    }
                }
                CommonEncodable texCoord = (CommonEncodable) ce.getValue("texCoord");
                if (texCoord != null) {

                    String tc_name = texCoord.getNodeName();
                    if (tc_name.equals("MultiTextureCoordinate")) {
                        CommonEncodable multiTexCoord = texCoord;
                        String useName = multiTexCoord.getUseName();
                        if (useName != null) {
                            multiTexCoord = defMap.get(useName);
                        }

                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> tx_list
                                = (List<CommonEncodable>) multiTexCoord.getValue("texCoord");
                        int num_tc = tx_list.size();
                        for (int i = 0; i < num_tc; i++) {
                            texCoord = tx_list.get(i);
                            if (texCoord != null) {

                                int num_texCoord = getNumTexCoord(texCoord);

                                ArrayData texCoord_index_data = (ArrayData) ce.getValue("texCoordIndex");

                                if (texCoord_index_data != null) {
                                    if (coord_index_data != null) {
                                        int num_coord_index = coord_index_data.num;
                                        if (texCoord_index_data.num < num_coord_index) {
                                            throw new FilterProcessingException(
                                                    LOG_NAME,
                                                    FilterExitCodes.INVALID_INPUT_FILE,
                                                    "An " + nodeName + " contains fewer texCoord indices than coord indices");
                                        }
                                        int[] coord_index = (int[]) coord_index_data.data;
                                        int[] texCoord_index = (int[]) texCoord_index_data.data;
                                        for (int j = 0; j < num_coord_index; j++) {
                                            if ((coord_index[j] == -1) && (texCoord_index[j] != -1)) {
                                                throw new FilterProcessingException(
                                                        LOG_NAME,
                                                        FilterExitCodes.INVALID_INPUT_FILE,
                                                        "An " + nodeName + " contains mismatched texCoord and coord indices");
                                            }
                                        }
                                    } else {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains no coordIndex field");
                                    }
                                    int max_texCoord_index = getMaxIndex(texCoord_index_data);
                                    if (num_texCoord <= max_texCoord_index) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains too few texCoords");
                                    }
                                } else {
                                    if (num_texCoord <= max_coord_index) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains too few texture coordinates");
                                    }
                                }
                            }
                        }
                    } else {

                        int num_texCoord = getNumTexCoord(texCoord);

                        ArrayData texCoord_index_data = (ArrayData) ce.getValue("texCoordIndex");

                        if (texCoord_index_data != null) {
                            if (coord_index_data != null) {
                                int num_coord_index = coord_index_data.num;
                                if (texCoord_index_data.num < num_coord_index) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains fewer texCoord indices than coord indices");
                                }
                                int[] coord_index = (int[]) coord_index_data.data;
                                int[] texCoord_index = (int[]) texCoord_index_data.data;
                                for (int i = 0; i < num_coord_index; i++) {
                                    if ((coord_index[i] == -1) && (texCoord_index[i] != -1)) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains mismatched texCoord and coord indices");
                                    }
                                }
                            } else {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains no coordIndex field");
                            }
                            int max_texCoord_index = getMaxIndex(texCoord_index_data);
                            if (num_texCoord <= max_texCoord_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few texCoords");
                            }
                        } else {
                            if (num_texCoord <= max_coord_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few texture coordinates");
                            }
                        }
                    }
                }
                break;
            }
            case "IndexedLineSet": {
                int max_coord_index = 0;
                ArrayData coord_index_data = (ArrayData) ce.getValue("coordIndex");
                if (coord_index_data != null) {
                    max_coord_index = getMaxIndex(coord_index_data);

                    CommonEncodable coord = (CommonEncodable) ce.getValue("coord");
                    int num_point = getNumCoord(coord);

                    if (num_point <= max_coord_index) {
                        throw new FilterProcessingException(
                                LOG_NAME,
                                FilterExitCodes.INVALID_INPUT_FILE,
                                "An " + nodeName + " contains too few coordinates");
                    }
                }
                CommonEncodable color = (CommonEncodable) ce.getValue("color");
                if (color != null) {

                    int num_color = getNumColor(color);

                    ArrayData color_index_data = (ArrayData) ce.getValue("colorIndex");

                    boolean colorPerVertex = true;
                    Object value = ce.getValue("colorPerVertex");
                    if ((value != null) && (value instanceof Boolean)) {
                        colorPerVertex = (Boolean) value;
                    }
                    if (!colorPerVertex) {
                        int num_faces = getNumFaces(coord_index_data);
                        if (color_index_data != null) {

                            int num_color_index = color_index_data.num;
                            if (num_color_index < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few color indices");
                            }
                            int max_color_index = getMaxIndex(color_index_data);
                            if (num_color <= max_color_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                            int[] color_index = (int[]) color_index_data.data;
                            for (int i = 0; i < num_color_index; i++) {
                                if (color_index[i] == -1) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains a color index of -1");
                                }
                            }
                        } else {
                            if (num_color < num_faces) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains fewer colors than faces");
                            }
                        }
                    } else {
                        if (color_index_data != null) {
                            if (coord_index_data != null) {
                                int num_coord_index = coord_index_data.num;
                                if (color_index_data.num < num_coord_index) {
                                    throw new FilterProcessingException(
                                            LOG_NAME,
                                            FilterExitCodes.INVALID_INPUT_FILE,
                                            "An " + nodeName + " contains fewer color indices than coord indices");
                                }
                                int[] coord_index = (int[]) coord_index_data.data;
                                int[] color_index = (int[]) color_index_data.data;
                                for (int i = 0; i < num_coord_index; i++) {
                                    if ((coord_index[i] == -1) && (color_index[i] != -1)) {
                                        throw new FilterProcessingException(
                                                LOG_NAME,
                                                FilterExitCodes.INVALID_INPUT_FILE,
                                                "An " + nodeName + " contains mismatched color and coord indices");
                                    }
                                }
                            } else {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains no coordIndex field");
                            }
                            int max_color_index = getMaxIndex(color_index_data);
                            if (num_color <= max_color_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                        } else {
                            if (num_color <= max_coord_index) {
                                throw new FilterProcessingException(
                                        LOG_NAME,
                                        FilterExitCodes.INVALID_INPUT_FILE,
                                        "An " + nodeName + " contains too few colors");
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * Return the number of vertices in the argument node
     *
     * @param coord A Coordinate node wrapper
     * @return The number of vertices
     */
    private int getNumCoord(CommonEncodable coord) {
        int num_point = 0;
        if (coord != null) {
            String useName = coord.getUseName();
            if (useName != null) {
                coord = defMap.get(useName);
            }
            Integer count = countMap.get(coord);
            if (count != null) {
                num_point = count;
            } else {
                ArrayData point_data = (ArrayData) coord.getValue("point");
                if (point_data != null) {
                    int num_value = point_data.num;
                    num_point = num_value / 3;
                    if (useName != null) {
                        countMap.put(coord, num_point);
                    }
                }
            }
        }
        return (num_point);
    }

    /**
     * Return the number of normals in the argument node
     *
     * @param normal A Normal node wrapper
     * @return The number of normals
     */
    private int getNumVector(CommonEncodable normal) {
        int num_vector = 0;
        if (normal != null) {
            String useName = normal.getUseName();
            if (useName != null) {
                normal = defMap.get(useName);
            }
            Integer count = countMap.get(normal);
            if (count != null) {
                num_vector = count;
            } else {
                ArrayData vector_data = (ArrayData) normal.getValue("vector");
                if (vector_data != null) {
                    int num_value = vector_data.num;
                    num_vector = num_value / 3;
                    if (useName != null) {
                        countMap.put(normal, num_vector);
                    }
                }
            }
        }
        return (num_vector);
    }

    /**
     * Return the number of colors in the argument node
     *
     * @param color A Color node wrapper
     * @return The number of colors
     */
    private int getNumColor(CommonEncodable color) {
        int num_color = 0;
        if (color != null) {
            int num_cmp = 3;
            if (color.getNodeName().equals("ColorRGBA")) {
                num_cmp = 4;
            }
            String useName = color.getUseName();
            if (useName != null) {
                color = defMap.get(useName);
            }
            Integer count = countMap.get(color);
            if (count != null) {
                num_color = count;
            } else {
                ArrayData color_data = (ArrayData) color.getValue("color");
                if (color_data != null) {
                    int num_value = color_data.num;
                    num_color = num_value / num_cmp;
                    if (useName != null) {
                        countMap.put(color, num_color);
                    }
                }
            }
        }
        return (num_color);
    }

    /**
     * Return the number of texCoords in the argument node
     *
     * @param texCoord A TextureCoordinate node wrapper
     * @return The number of texCoords
     */
    private int getNumTexCoord(CommonEncodable texCoord) {
        int num_point = 0;
        if (texCoord != null) {
            String useName = texCoord.getUseName();
            if (useName != null) {
                texCoord = defMap.get(useName);
            }
            Integer count = countMap.get(texCoord);
            if (count != null) {
                num_point = count;
            } else {
                ArrayData point_data = (ArrayData) texCoord.getValue("point");
                if (point_data != null) {
                    int num_value = point_data.num;
                    num_point = num_value / 2;
                    if (useName != null) {
                        countMap.put(texCoord, num_point);
                    }
                }
            }
        }
        return (num_point);
    }

    /**
     * Return the maximum index from the argument array of indices
     *
     * @param index_data The index data
     * @return The maximum value from the array.
     */
    private int getMaxIndex(ArrayData index_data) {

        int max_index = 0;
        if ((index_data != null) && (index_data.type == ArrayData.INT)) {

            int num_index = index_data.num;
            int[] index = (int[]) index_data.data;

            for (int i = 0; i < num_index; i++) {
                if (index[i] > max_index) {
                    max_index = index[i];
                }
            }
        }
        return (max_index);
    }

    /**
     * Return the number of faces defined by the argument indices
     *
     * @param index_data The index data
     * @return The maximum value from the array.
     */
    private int getNumFaces(ArrayData index_data) {

        int num_faces = 0;
        if ((index_data != null) && (index_data.type == ArrayData.INT)) {

            int num_index = index_data.num;
            int last_index = num_index - 1;
            int[] index = (int[]) index_data.data;

            for (int i = 0; i < num_index; i++) {
                if (index[i] == -1) {
                    num_faces++;
                }
            }
            if (index[last_index] != -1) {
                num_faces++;
            }
        }
        return (num_faces);
    }
}
