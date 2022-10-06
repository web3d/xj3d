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

import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * Geometry holder that represents the X3D IndexedFaceSet node.
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
 * @version $Revision: 2.1 $
 */
class IndexedFaceSetGeometry extends TriangulationGeometry  {

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

    /** The fog coordinate values from the IFS */
    private float[] fogCoords;

    /** A single set of 2D texture coordinate values from the TFS */
    private float[] texCoords;

    /** The number of valid coordinate indices in the coordIndex array */
    private int numCoordIndex;

    /** The set of coordinate indices from the IFS */
    private int[] coordIndices;

    /** The set of normal indices from the IFS */
    private int[] normalIndices;

    /** The set of color indices from the IFS */
    private int[] colorIndices;

    /** The set of texture indices from the IFS */
    private int[] texCoordIndices;

    /** Indicator if this IFS is solid or not */
    private boolean solid;

    /** Indicator if this IFS is wound conterclockwise */
    private boolean ccw;

    /** The crease angle for generating normals */
    private float creaseAngle;

    /** Indicator if this IFS contains only convex polygons */
    private boolean convex;

    /** Are colours provided per vertex or per face */
    private boolean colorPerVertex;

    /** Are normals provided per vertex or per face */
    private boolean normalPerVertex;

    /** Flag indicating the colour has 4 components and not 3 */
    private boolean colorHasAlpha;

    /** Temp holding on to the number of polygons in the system */
    private int polygonCount;

    /** Holder of number vertices per face during processing */
    private int[] rawVerticesPerFace;

    /** The def name of the geometry */
    private String defName;

    /**
     * Construct a default instance of this converter.
     */
    IndexedFaceSetGeometry() {
        solid = true;
        ccw = true;
        colorPerVertex = true;
        convex = true;
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
        solid = true;
        ccw = true;
        colorPerVertex = true;
        convex = true;
        normalPerVertex = true;
        colorHasAlpha = false;

        coordinates = null;
        normals = null;
        colors = null;
        fogCoords = null;
        texCoords = null;
        coordIndices = null;
        normalIndices = null;
        colorIndices = null;
        texCoordIndices = null;
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
            if(value instanceof String) {
                colorPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                colorPerVertex = ((Boolean)value);
            }
        } else if(name.equals("convex")) {
            if(value instanceof String) {
                convex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                convex = ((Boolean)value);
            }
        } else if(name.equals("normalPerVertex")) {
            if(value instanceof String) {
                normalPerVertex = fieldReader.SFBool((String)value);
            } else if(value instanceof Boolean) {
                normalPerVertex = ((Boolean)value);
            }
        } else if(name.equals("creaseAngle")) {
            if(value instanceof String) {
               creaseAngle = fieldReader.SFFloat((String)value);
            } else if(value instanceof Float) {
                creaseAngle = (Float) value;
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
                colors = fieldReader.MFColor((String)value);
            } else if(value instanceof String[]) {
                colors = fieldReader.MFColor((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.equals("coordIndex")) {
            if(value instanceof String) {
                coordIndices = fieldReader.MFInt32((String)value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = coordIndices.length;
            }
        } else if(name.equals("normalIndex")) {
            if(value instanceof String) {
                normalIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                normalIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                normalIndices = (int[])value;
            }
        } else if(name.equals("colorIndex")) {
            if(value instanceof String) {
                colorIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                colorIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                colorIndices = (int[])value;
            }
        } else if(name.equals("texCoordIndex")) {
            if(value instanceof String) {
                texCoordIndices = fieldReader.MFInt32((String)value);
            } else if(value instanceof String[]) {
                texCoordIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                texCoordIndices = (int[])value;
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
        if(name.equals("Coordinate")) {
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
                colors = fieldReader.MFColor((String[])value);
            } else if(value instanceof float[]) {
                colors = (float[])value;
            }
        } else if(name.equals("coordIndex")) {
            if(value instanceof String[]) {
                coordIndices = fieldReader.MFInt32((String[])value);
                numCoordIndex = coordIndices.length;
            } else if(value instanceof int[]) {
                coordIndices = (int[])value;
                numCoordIndex = len;

                if (coordIndices == null) {
                    System.out.println("Invalid len in filter, please fix");
                    new Exception().printStackTrace(System.err);
                    numCoordIndex = 0;
                }
            }
        } else if(name.equals("normalIndex")) {
            if(value instanceof String[]) {
                normalIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                normalIndices = (int[])value;
            }
        } else if(name.equals("colorIndex")) {
            if(value instanceof String[]) {
                colorIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                colorIndices = (int[])value;
            }
        } else if(name.equals("texCoordIndex")) {
            if(value instanceof String[]) {
                texCoordIndices = fieldReader.MFInt32((String[])value);
            } else if(value instanceof int[]) {
                texCoordIndices = (int[])value;
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


        ch.startNode("TriangleSet", defName);
        ch.startField("coord");
        ch.startNode("Coordinate", null );
        ch.startField("point");

        GeometryUtils gutils = new GeometryUtils();
        GeometryHolder gholder = new GeometryHolder();

        // Assume 3 component colour here and that someone downstream has
        // remove RGBA for now.
        gutils.generateTriangleArrays(coordinates, colors, normals,
           texCoords, 0, true, true,
           coordIndices, coordIndices.length, colorIndices, normalIndices,
           texCoordIndices, ccw, convex, colorPerVertex, normalPerVertex,
           3, creaseAngle, true, gholder);

        float[] coords = gholder.coordinates;

        if (coords != null) {
            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(coords, coords.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < coords.length; i++) {
                    buf.append(coords[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }
        }

        ch.endNode();  // Coordinate
        ch.endField(); // coord


        if(normals != null && gholder.normals != null) {
            ch.startField("normal");
            ch.startNode("Normal", null );
            ch.startField("vector");

            float[] n = gholder.normals;

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(n, n.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < n.length; i++) {
                    buf.append(n[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Normal
            ch.endField(); // normals
        }

        if(colors != null && gholder.colors != null) {
            ch.startField("color");
            ch.startNode("Color", null );
            ch.startField("color");

            float[] c = gholder.colors;

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(c, c.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < c.length; i++) {
                    buf.append(c[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // Color
            ch.endField(); // color
        }

        if(texCoords != null && gholder.textureCoordinates != null) {
            ch.startField("texCoord");
            ch.startNode("TextureCoordinate", null );
            ch.startField("point");

            float[] c = gholder.textureCoordinates[0];

            if(ch instanceof BinaryContentHandler) {
                ((BinaryContentHandler)ch).fieldValue(c, c.length);
            } else if(ch instanceof StringContentHandler) {
                StringBuilder buf = new StringBuilder();
                for(int i = 0; i < c.length; i++) {
                    buf.append(c[i]);
                    buf.append(' ');
                }

                ((StringContentHandler)ch).fieldValue(buf.toString());
            }

            ch.endNode();  // TextureCoordinate
            ch.endField(); // texCoord
        }

        // all other fields are taken care of in the triangluation step
        // so no need to generate them as output here.
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
        int cur_size = 0;
        int max_size = 0;
        polygonCount = 0;

        if (coordIndices == null)
            return 0;

        for(int i = 0; i < numCoordIndex; i++) {

            if(coordIndices[i] == -1) {
                if(cur_size > max_size)
                    max_size = cur_size;

                cur_size = 0;
                polygonCount++;
            } else {
                cur_size++;
            }
        }

        // One last check on the last index. The spec allows the user to not
        // need to specify -1 as the last value. If we don't check for this,
        // the max size would never be set.
        if((numCoordIndex != 0) && (coordIndices[numCoordIndex - 1] != -1)) {
            if(cur_size > max_size)
                max_size = cur_size;

            polygonCount++;
        }

        rawVerticesPerFace = new int[polygonCount];
        int current_face = 0;

        for(int i = 0; i < numCoordIndex; i++) {
            if(coordIndices[i] != -1) {
                rawVerticesPerFace[current_face]++;
            } else {
                current_face++;
                if(current_face < polygonCount)
                    rawVerticesPerFace[current_face] = 0;
            }
        }

        return max_size;
    }

    /**
     * Rebuild the index list based on the logic defined in the spec.
     */
    private void buildIndexList(int fieldIndex) {
        int[] src_list = null;
        int[] final_list;
        boolean per_vertex = false;

        switch(fieldIndex) {
            case NORMALS:
                src_list = normalIndices;
                per_vertex = normalPerVertex;
                break;

            case COLORS:
                src_list = colorIndices;
                per_vertex = colorPerVertex;
                break;

            case TEXCOORDS:
                src_list = texCoordIndices;
                per_vertex = true;
                break;
        }

        // Construct a per-vertex list for internal use out of whatever
        // the source data is. This is based on the rules for the color field
        // defined in clause 13.3.6 of Part 1 of the X3D abstract spec. Assumes
        // that the coordIndex list is valid. If not set, we wouldn't be
        // rendering
        //
        // if the per-vertex flag is false
        //   if the index list is not empty
        //      per-face indexes are expanded to be per vertex
        //      based on the index list info in the coordIndex
        //   else
        //      build a per-vertex list that just lists each face
        //      for x number of times for the corresponding face in
        //      the coordIndex list
        // else
        //   if the index list is not empty
        //      use the index list
        //   else
        //      use the coordIndex values directly
        if(!per_vertex) {
            final_list = new int[numCoordIndex];

            if((src_list != null) && (src_list.length != 0)) {
                // Each index in the list is the index for the face, so just
                // repeat it for the number of times that the coordIndex
                // defines vertices for the face
                int src_pos = 0;
                for(int i = 0; i < numCoordIndex; i++) {
                    if(coordIndices[i] != -1)
                        final_list[i] = src_list[src_pos];
                    else {
                        final_list[i] = -1;
                        src_pos++;
                    }
                }
            } else {
                // We don't have anything, so the list becomes an index starting
                // at 0 and then just incrementing each time we hit a new face
                int src_pos = 0;
                for(int i = 0; i < numCoordIndex; i++) {
                    if(coordIndices[i] != -1)
                        final_list[i] = src_pos;
                    else {
                        final_list[i] = -1;
                        src_pos++;
                    }
                }
            }
        } else {
            if((src_list != null) && (src_list.length != 0)) {
                final_list = src_list;
            } else {
                final_list = coordIndices;
            }
        }

        // Now copy it back to the original list
        switch(fieldIndex) {
            case NORMALS:
                normalIndices = final_list;
                break;

            case COLORS:
                colorIndices = final_list;
                break;

            case TEXCOORDS:
                texCoordIndices = final_list;
                break;
        }
    }

    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param res A temporary value containing the normal value
     */
    private void createFaceNormal(float[] coords,
                                  int[] coordIndex,
                                  int start,
                                  int numVertex,
                                  float[] res) {

        // Uses the Newell method to calculate the face normal
        float nx = 0;
        float ny = 0;
        float nz = 0;
        int x1, y1, z1, x2, y2, z2;

        x1 = coordIndex[start] * 3;
        y1 = x1 + 1;
        z1 = x1 + 2;

        for(int i = 0; i < numVertex - 1; i++) {

            x2 = coordIndex[start + i + 1] * 3;
            y2 = x2 + 1;
            z2 = x2 + 2;

            nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
            ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
            nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

            x1 = x2;
            y1 = y2;
            z1 = z2;
        }

        // The last vertex uses the start position
        x2 = coordIndex[start] * 3;
        y2 = x2 + 1;
        z2 = x2 + 2;

        nx += (coords[y1] - coords[y2]) * (coords[z1] + coords[z2]);
        ny += (coords[z1] - coords[z2]) * (coords[x1] + coords[x2]);
        nz += (coords[x1] - coords[x2]) * (coords[y1] + coords[y2]);

        res[0] = nx;
        res[1] = ny;
        res[2] = nz;

        double len = nx * nx + ny * ny + nz * nz;
        if(len != 0) {
            len = (ccw ? 1 : -1) / Math.sqrt(len);
            res[0] *= len;
            res[1] *= len;
            res[2] *= len;
        }
    }
}
