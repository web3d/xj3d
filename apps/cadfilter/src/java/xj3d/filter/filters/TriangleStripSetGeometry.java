/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
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

// Local imports
import org.web3d.vrml.sav.*;


/**
 * Geometry holder that represents the X3D IndexedTriangleStripSet node.
 * <p>
 *
 * <b>Implementation Notes</b>
 * <p>
 * <ul>
 * <li>Only handles a single set of 2D texture coordinates. Multitexture is
 * ignored and effectively stripped.</li>
 * <li>Does not handle vertex attributes. It doesn't retriangulate the
 * underlying node.</li>
 * <li>Does not handle fog coordinates</li>
 * <li>ColorRGBA is not supported. Just trashes the output</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class TriangleStripSetGeometry extends TriangulationGeometry  {

    /** The data to be processed is normals */
    private static final int NORMALS = 1;

    /** The data to be processed is normals */
    private static final int COLORS = 2;

    /** The data to be processed is normals */
    private static final int TEXCOORDS = 3;

    /** The coordinate values from the IFS */
    private float[] coordinates;

    /** The normal values from the IFS */
    private float[] normals;

    /** The color values from the IFS */
    private float[] colors;

    /** A single set of 2D texture coordinate values from the TFS */
    private float[] texCoords;

    /** The fog coordinate values from the IFS */
    private float[] fogCoords;

    /** The number of valid coordinates per set in the coordinates array */
    private int[] setCount;

    /** Indicator if this IFS is solid or not */
    private boolean solid;

    /** Indicator if this IFS is wound conterclockwise */
    private boolean ccw;

    /** Are normals provided per vertex or per face */
    private boolean normalPerVertex;

    /** Flag indicating the colour has 4 components and not 3 */
    private boolean colorHasAlpha;

    /** Number of triangles found in the set */
    private int polygonCount;

    /** Holder of number vertices per face during processing */
    private int[] rawVerticesPerFace;

	/** The def name of the geometry */
	private String defName;
	
    /**
     * Construct a default instance of this converter.
     */
    TriangleStripSetGeometry() {
        solid = true;
        ccw = true;
        normalPerVertex = true;
        colorHasAlpha = false;
    }

    //----------------------------------------------------------
    // Methods defined by TriangulationGeometry
    //----------------------------------------------------------

    /**
     * Clear the currently stored values and return to the defaults for
     * this geometry type.
     */
    @Override
    void reset() {
        polygonCount = 0;
        solid = true;
        ccw = true;
        normalPerVertex = true;
        colorHasAlpha = false;

        coordinates = null;
        normals = null;
        colors = null;
        texCoords = null;
        fogCoords = null;
        setCount = null;
    }

    /**
     * Add a new field value to the geometry. The form of the value is
     * not defined and is up to the implementing class to interpret it
     * according to the needed fields. Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     */
    @Override
    void addFieldValue(String name, Object value) {
        if(name.equals("solid")) {
            if(value instanceof String) {
                solid = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                solid = ((Boolean)value);
            }
        } else if(name.equals("ccw")) {
            if(value instanceof String) {
                ccw = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                ccw = ((Boolean)value);
            }
        } else if(name.equals("colorPerVertex")) {
            // Ignored. Spec says to treat always as true
        } else if(name.equals("normalPerVertex")) {
            if(value instanceof String) {
                normalPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                normalPerVertex = ((Boolean)value);
            }
        } else if(name.equals("stripCount")) {
            if(value instanceof String) {
                setCount = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                setCount = fieldReader.MFInt32((String[])value);
            } else if(value instanceof float[]) {
                setCount = (int[])value;
            }
        } else if(name.equals("Coordinate")) {
            if(value instanceof String) {
                coordinates = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
            }
        } else if(name.equals("Normal")) {
            if(value instanceof String) {
                normals = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                normals = (float[])value;
            }
        } else if(name.equals("Color")) {
            if(value instanceof String) {
                colors = fieldReader.MFVec3f((String)value);
            } else if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.startsWith("TextureCoordinate")) {
            if(value instanceof String) {
                texCoords = fieldReader.MFVec2f((String)value);
            } else if(value instanceof String[]) {
                texCoords = fieldReader.MFVec2f((String[])value);
            } else if(value instanceof float[]) {
                texCoords = (float[])value;
            }
        }
    }

    /**
     * Add a new field value to the geometry using array data. The
     * form of the value is  not defined and is up to the implementing
     * class to interpret it according to the needed fields.  The
     * array length is the number of valid items in the passed array.
     * <p>
     * Note that field names will be
     * compressed from the X3D structure. The coordinate node's point
     * field may be just "coordinate".
     *
     * @param name The name of the field that is to be added
     * @param value The value of the field
     * @param len The length of the valid data in the array
     */
    @Override
    void addFieldValue(String name, Object value, int len) {
        if(name.equals("stripCount")) {
            if(value instanceof String[]) {
                setCount = fieldReader.MFInt32((String[])value);
            } else if(value instanceof float[]) {
                setCount = (int[])value;
            }
        } else if(name.equals("Coordinate")) {
            if(value instanceof String[]) {
                coordinates = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                coordinates = (float[])value;
            }
        } else if(name.equals("Normal")) {
            if(value instanceof String[]) {
                normals = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                normals = (float[])value;
            }
        } else if(name.equals("Color")) {
            if(value instanceof String[]) {
                colors = fieldReader.MFVec3f((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.startsWith("TextureCoordinate")) {
            if(value instanceof String[]) {
                texCoords = fieldReader.MFVec2f((String[])value);
            } else if(value instanceof float[]) {
                texCoords = (float[])value;
            }
        }
    }

    /**
     * The geometry definition is now finished so take the given field
     * values and generate the triangle output.
     *
     *
     * @param ch The content handler instance to write to
     * @param sh The script handler instance to write to
     * @param ph The proto handler instance to write to
     * @param rh The route handler instance to write to
     */
    @Override
    void generateOutput(ContentHandler ch,
                        ScriptHandler sh,
                        ProtoHandler ph,
                        RouteHandler rh) {

        // Check the output and adjust accordingly. For max size 3 then just
        // drop the coordinates out now and not do any processing. For anything
        // more than three we need to go through and triangulate whatever we
        // find.
        int max_poly_size = checkMaxPolySize();

        switch(max_poly_size) {
            case 0:
                errorReporter.messageReport("No valid polygons found: Zero sized polygons");
                return;

            case 1:
            case 2:
                errorReporter.messageReport("No valid polygons. Max size " +
                                            max_poly_size);
                return;
        }


        ch.startNode("TriangleStripSet", defName);
        ch.startField("coord");
        ch.startNode("Coordinate", null );
        ch.startField("point");

        float[] output_coords = null;
        float[] output_normals = null;
        float[] output_colors = null;
        float[] output_texcoords = null;
        int output_idx = 0;
        int input_idx = 0;

        if(ccw) {
            output_coords = coordinates;
            output_normals = normals;
            output_texcoords = texCoords;
            output_colors = colors;

            if(normals != null) {
                output_normals = new float[coordinates.length];

                if(normalPerVertex) {
                    for(int i = 0; i < polygonCount; i++) {

                        switch(setCount[i]) {
                            case 0:
                                continue;

                            case 1:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                input_idx += 3;
                                output_idx += 3;
                                continue;

                            case 2:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx+ 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                output_normals[output_idx + 3] =
                                    normals[input_idx + 3];
                                output_normals[output_idx + 4] =
                                    normals[input_idx + 4];
                                output_normals[output_idx + 5] =
                                    normals[input_idx + 5];

                                input_idx += 6;
                                output_idx += 6;
                                continue;

                            default:
                                for(int j = 0; j < setCount[i] - 1; j += 2) {

                                    output_normals[output_idx] =
                                        normals[input_idx];
                                    output_normals[output_idx + 1] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 2] =
                                        normals[input_idx + 2];

                                    output_normals[output_idx + 3] =
                                        normals[input_idx + 3];
                                    output_normals[output_idx + 4] =
                                        normals[input_idx + 4];
                                    output_normals[output_idx + 5] =
                                        normals[input_idx + 5];

                                    input_idx += 6;
                                    output_idx += 6;
                                }
                        }

                        if (setCount[i] % 2 == 1) {
                            output_normals[output_idx] =
                                normals[input_idx];
                            output_normals[output_idx + 1] =
                                normals[input_idx + 1];
                            output_normals[output_idx + 2] =
                                normals[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                        }
                    }

                } else {

                    for(int i = 0; i < polygonCount; i++) {

                        switch(setCount[i]) {
                            case 0:
                                continue;

                            case 1:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                input_idx += 3;
                                output_idx += 3;
                                continue;

                            case 2:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                output_normals[output_idx + 3] =
                                    normals[input_idx];
                                output_normals[output_idx + 4] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 5] =
                                    normals[input_idx + 2];

                                input_idx += 6;
                                output_idx += 6;
                                continue;

                            default:
                                for(int j = 0; j < setCount[i] - 1; j += 2) {

                                    output_normals[output_idx] =
                                        normals[input_idx];
                                    output_normals[output_idx + 1] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 2] =
                                        normals[input_idx + 2];

                                    output_normals[output_idx + 3] =
                                        normals[input_idx];
                                    output_normals[output_idx + 4] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 5] =
                                        normals[input_idx + 2];

                                    input_idx += 6;
                                    output_idx += 6;
                                }
                        }

                        if (setCount[i] % 2 == 1) {
                            output_normals[output_idx] =
                                normals[input_idx];
                            output_normals[output_idx + 1] =
                                normals[input_idx + 1];
                            output_normals[output_idx + 2] =
                                normals[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                        }
                    }
                }
            }

            if(colors != null) {
                output_colors = new float[coordinates.length];

                for(int i = 0; i < polygonCount; i++) {

                    switch(setCount[i]) {
                        case 0:
                            continue;

                        case 1:
                            output_colors[output_idx] =
                                colors[input_idx];
                            output_colors[output_idx + 1] =
                                colors[input_idx + 1];
                            output_colors[output_idx + 2] =
                                colors[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                            continue;

                        case 2:
                            output_colors[output_idx] =
                                colors[input_idx];
                            output_colors[output_idx + 1] =
                                colors[input_idx + 1];
                            output_colors[output_idx + 2] =
                                colors[input_idx + 2];

                            output_colors[output_idx + 3] =
                                colors[input_idx + 3];
                            output_colors[output_idx + 4] =
                                colors[input_idx + 4];
                            output_colors[output_idx + 5] =
                                colors[input_idx + 5];

                            input_idx += 6;
                            output_idx += 6;
                            continue;

                        default:
                            for(int j = 0; j < setCount[i] - 1; j += 2) {

                                output_colors[output_idx] =
                                    colors[input_idx];
                                output_colors[output_idx + 1] =
                                    colors[input_idx + 1];
                                output_colors[output_idx + 2] =
                                    colors[input_idx + 2];

                                output_colors[output_idx + 3] =
                                    colors[input_idx + 3];
                                output_colors[output_idx + 4] =
                                    colors[input_idx + 4];
                                output_colors[output_idx + 5] =
                                    colors[input_idx + 5];

                                input_idx += 6;
                                output_idx += 6;
                            }
                    }

                    if (setCount[i] % 2 == 1) {
                        output_colors[output_idx] =
                            colors[input_idx];
                        output_colors[output_idx + 1] =
                            colors[input_idx + 1];
                        output_colors[output_idx + 2] =
                            colors[input_idx + 2];

                        input_idx += 3;
                        output_idx += 3;
                    }
                }
            }

            if(texCoords != null) {
                output_texcoords = new float[coordinates.length];

                for(int i = 0; i < polygonCount; i++) {

                    switch(setCount[i]) {
                        case 0:
                            continue;

                        case 1:
                            output_texcoords[output_idx] =
                                texCoords[input_idx];
                            output_texcoords[output_idx + 1] =
                                texCoords[input_idx + 1];

                            input_idx += 2;
                            output_idx += 2;
                            continue;

                        case 2:
                            output_texcoords[output_idx] =
                                texCoords[input_idx];
                            output_texcoords[output_idx + 1] =
                                texCoords[input_idx + 1];

                            output_texcoords[output_idx + 2] =
                                texCoords[input_idx + 2];
                            output_texcoords[output_idx + 3] =
                                texCoords[input_idx + 3];

                            input_idx += 4;
                            output_idx += 4;
                            continue;

                        default:
                            for(int j = 0; j < setCount[i] - 1; j += 2) {

                                output_texcoords[output_idx] =
                                    texCoords[input_idx];
                                output_texcoords[output_idx + 1] =
                                    texCoords[input_idx + 1];

                                output_texcoords[output_idx + 2] =
                                    texCoords[input_idx + 2];
                                output_texcoords[output_idx + 3] =
                                    texCoords[input_idx + 3];

                                input_idx += 4;
                                output_idx += 4;
                            }
                    }

                    if (setCount[i] % 2 == 1) {
                        output_texcoords[output_idx] =
                            texCoords[input_idx];
                        output_texcoords[output_idx + 1] =
                            texCoords[input_idx + 1];

                        input_idx += 2;
                        output_idx += 2;
                    }
                }
            }
        } else {
            output_coords = new float[coordinates.length];

            for(int i = 0; i < polygonCount; i++) {

                switch(setCount[i]) {
                    case 0:
                        continue;

                    case 1:
                        output_coords[output_idx++] =
                            coordinates[input_idx];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 1];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 2];

                        input_idx += 3;
                        continue;

                    case 2:
                        output_coords[output_idx++] =
                            coordinates[input_idx + 3];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 4];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 5];

                        output_coords[output_idx++] =
                            coordinates[input_idx];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 1];
                        output_coords[output_idx++] =
                            coordinates[input_idx + 2];

                        input_idx += 6;
                        continue;

                    default:
                        for(int j = 0; j < setCount[i] - 1; j+=2) {

                            output_coords[output_idx++] =
                                coordinates[input_idx + 3];
                            output_coords[output_idx++] =
                                coordinates[input_idx + 4];
                            output_coords[output_idx++] =
                                coordinates[input_idx + 5];

                            output_coords[output_idx++] =
                                coordinates[input_idx];
                            output_coords[output_idx++] =
                                coordinates[input_idx + 1];
                            output_coords[output_idx++] =
                                coordinates[input_idx + 2];

                            input_idx += 6;

                        }
                }

                if (setCount[i] % 2 == 1) {
                    output_coords[output_idx++] =
                        coordinates[input_idx];
                    output_coords[output_idx++] =
                        coordinates[input_idx + 1];
                    output_coords[output_idx++] =
                        coordinates[input_idx + 2];

                    input_idx += 3;
                }
            }

            if(normals != null) {
                output_normals = new float[coordinates.length];

                if(normalPerVertex) {
                    for(int i = 0; i < polygonCount; i++) {

                        switch(setCount[i]) {
                            case 0:
                                continue;

                            case 1:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                input_idx += 3;
                                output_idx += 3;
                                continue;

                            case 2:
                                output_normals[output_idx] =
                                    normals[input_idx + 3];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 4];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 5];

                                output_normals[output_idx + 3] =
                                    normals[input_idx];
                                output_normals[output_idx + 4] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 5] =
                                    normals[input_idx + 2];

                                input_idx += 6;
                                output_idx += 6;
                                continue;

                            default:
                                for(int j = 0; j < setCount[i] - 1; j += 2) {

                                    output_normals[output_idx] =
                                        normals[input_idx + 3];
                                    output_normals[output_idx + 1] =
                                        normals[input_idx + 4];
                                    output_normals[output_idx + 2] =
                                        normals[input_idx + 5];

                                    output_normals[output_idx + 3] =
                                        normals[input_idx];
                                    output_normals[output_idx + 4] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 5] =
                                        normals[input_idx + 2];

                                    input_idx += 6;
                                    output_idx += 6;
                                }
                        }

                        if (setCount[i] % 2 == 1) {
                            output_normals[output_idx] =
                                normals[input_idx];
                            output_normals[output_idx + 1] =
                                normals[input_idx + 1];
                            output_normals[output_idx + 2] =
                                normals[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                        }
                    }

                } else {

                    for(int i = 0; i < polygonCount; i++) {

                        switch(setCount[i]) {
                            case 0:
                                continue;

                            case 1:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                input_idx += 3;
                                output_idx += 3;
                                continue;

                            case 2:
                                output_normals[output_idx] =
                                    normals[input_idx];
                                output_normals[output_idx + 1] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 2] =
                                    normals[input_idx + 2];

                                output_normals[output_idx + 3] =
                                    normals[input_idx];
                                output_normals[output_idx + 4] =
                                    normals[input_idx + 1];
                                output_normals[output_idx + 5] =
                                    normals[input_idx + 2];

                                input_idx += 6;
                                output_idx += 6;
                                continue;

                            default:
                                for(int j = 0; j < setCount[i] - 1; j += 2) {

                                    output_normals[output_idx] =
                                        normals[input_idx];
                                    output_normals[output_idx + 1] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 2] =
                                        normals[input_idx + 2];

                                    output_normals[output_idx + 3] =
                                        normals[input_idx];
                                    output_normals[output_idx + 4] =
                                        normals[input_idx + 1];
                                    output_normals[output_idx + 5] =
                                        normals[input_idx + 2];

                                    input_idx += 6;
                                    output_idx += 6;
                                }
                        }

                        if (setCount[i] % 2 == 1) {
                            output_normals[output_idx] =
                                normals[input_idx];
                            output_normals[output_idx + 1] =
                                normals[input_idx + 1];
                            output_normals[output_idx + 2] =
                                normals[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                        }
                    }
                }
            }

            if(colors != null) {
                output_colors = new float[coordinates.length];

                for(int i = 0; i < polygonCount; i++) {

                    switch(setCount[i]) {
                        case 0:
                            continue;

                        case 1:
                            output_colors[output_idx] =
                                colors[input_idx];
                            output_colors[output_idx + 1] =
                                colors[input_idx + 1];
                            output_colors[output_idx + 2] =
                                colors[input_idx + 2];

                            input_idx += 3;
                            output_idx += 3;
                            continue;

                        case 2:
                            output_colors[output_idx] =
                                colors[input_idx + 3];
                            output_colors[output_idx + 1] =
                                colors[input_idx + 4];
                            output_colors[output_idx + 2] =
                                colors[input_idx + 5];

                            output_colors[output_idx + 3] =
                                colors[input_idx];
                            output_colors[output_idx + 4] =
                                colors[input_idx + 1];
                            output_colors[output_idx + 5] =
                                colors[input_idx + 2];

                            input_idx += 6;
                            output_idx += 6;
                            continue;

                        default:
                            for(int j = 0; j < setCount[i] - 1; j += 2) {

                                output_colors[output_idx] =
                                    colors[input_idx + 3];
                                output_colors[output_idx + 1] =
                                    colors[input_idx + 4];
                                output_colors[output_idx + 2] =
                                    colors[input_idx + 5];

                                output_colors[output_idx + 3] =
                                    colors[input_idx];
                                output_colors[output_idx + 4] =
                                    colors[input_idx + 1];
                                output_colors[output_idx + 5] =
                                    colors[input_idx + 2];

                                input_idx += 6;
                                output_idx += 6;
                            }
                    }

                    if (setCount[i] % 2 == 1) {
                        output_colors[output_idx] =
                            colors[input_idx];
                        output_colors[output_idx + 1] =
                            colors[input_idx + 1];
                        output_colors[output_idx + 2] =
                            colors[input_idx + 2];

                        input_idx += 3;
                        output_idx += 3;
                    }
                }
            }

            if(texCoords != null) {
                output_texcoords = new float[coordinates.length];

                for(int i = 0; i < polygonCount; i++) {

                    switch(setCount[i]) {
                        case 0:
                            continue;

                        case 1:
                            output_texcoords[output_idx] =
                                texCoords[input_idx];
                            output_texcoords[output_idx + 1] =
                                texCoords[input_idx + 1];

                            input_idx += 2;
                            output_idx += 2;
                            continue;

                        case 2:
                            output_texcoords[output_idx] =
                                texCoords[input_idx + 2];
                            output_texcoords[output_idx + 1] =
                                texCoords[input_idx + 3];

                            output_texcoords[output_idx + 2] =
                                texCoords[input_idx];
                            output_texcoords[output_idx + 3] =
                                texCoords[input_idx + 1];

                            input_idx += 4;
                            output_idx += 4;
                            continue;

                        default:
                            for(int j = 0; j < setCount[i] - 1; j += 2) {

                                output_texcoords[output_idx] =
                                    texCoords[input_idx + 2];
                                output_texcoords[output_idx + 1] =
                                    texCoords[input_idx + 3];

                                output_texcoords[output_idx + 2] =
                                    texCoords[input_idx];
                                output_texcoords[output_idx + 3] =
                                    texCoords[input_idx + 1];

                                input_idx += 4;
                                output_idx += 4;
                            }
                    }

                    if (setCount[i] % 2 == 1) {
                        output_texcoords[output_idx] =
                            texCoords[input_idx];
                        output_texcoords[output_idx + 1] =
                            texCoords[input_idx + 1];

                        input_idx += 2;
                        output_idx += 2;
                    }
                }
            }
        }

        if(ch instanceof BinaryContentHandler) {
            ((BinaryContentHandler)ch).fieldValue(output_coords, output_coords.length);
        } else if(ch instanceof StringContentHandler) {
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < output_coords.length; i++) {
                buf.append(output_coords[i]);
                buf.append(' ');
            }

            ((StringContentHandler)ch).fieldValue(buf.toString());
        }

        ch.endNode();  // Coordinate
        ch.endField(); // coord

        if(setCount != null) {
            ch.startField("stripCount");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(setCount, setCount.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuffer buf = new StringBuffer();
                for(int i = 0; i < setCount.length; i++) {
                    buf.append(setCount[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endField(); // fanCount
        }

        if(normals != null) {
            ch.startField("normal");
            ch.startNode("Normal", null );
            ch.startField("vector");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(output_normals,
                                                      output_normals.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < output_normals.length; i++) {
                    buf.append(output_normals[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Normal
            ch.endField(); // normals
        }

        if(colors != null) {
            ch.startField("color");
/*
            if(colorHasAlpha)
                ch.startNode("ColorRGBA", null );
            else
*/
                ch.startNode("Color", null );

            ch.startField("color");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(output_colors,
                                                      output_colors.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < output_colors.length; i++) {
                    buf.append(output_colors[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Color[RGBA]
            ch.endField(); // color
        }

        if(texCoords != null) {
            ch.startField("texCoord");
            ch.startNode("TextureCoordinate", null );
            ch.startField("point");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(texCoords, texCoords.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < colors.length; i++) {
                    buf.append(colors[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // TextureCoordinate
            ch.endField(); // texCoord
        }

        if(!solid) {
            ch.startField("solid");

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(solid);
            } else if(ch instanceof StringContentHandler) {

                ((StringContentHandler)ch).fieldValue("FALSE");
            }
        }

        ch.endNode();  // IndexedTriangleSet
    }

	/**
	 * Set the def name of the geometry
	 *
	 * @param defName Set the def name of the geometry
	 */
    @Override
	void setDefName(String defName) {
		this.defName = defName;
	}
	
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Go through the coordIndex array and work out what the maximum polygon
     * size will be before we've done any processing. It does not define the
     * current maxPolySize variable.
     *
     * @return The maximum size that this check found
     */
    private int checkMaxPolySize() {
        int max_size = 0;
        polygonCount = 0;

        for(int i = 0; i < setCount.length; i++) {

            if(setCount[i] > max_size) {
                max_size = setCount[i];
            }

            polygonCount++;

        }

        return max_size;
    }
}
