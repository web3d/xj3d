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
// None

// Local imports
import org.web3d.vrml.sav.*;

/**
 * Geometry holder that represents the X3D ElevationGrid node.
 * <p>
 *
 * Splits quads in the grid into triangles. Generated in numerical
 * order:
 *
 * -----
 * |\ 2|
 * | \ |
 * |1 \|
 * -----
 *
 *	TODO: Error checking - xDimension, zDimension must be > 0.  If xDimension and zDimension are less than 2
 *  the grid has no quads.
 *
 * @author Christopher Shankland
 * @version $Revision: 1.4 $
 */
class ElevationGridGeometry extends TriangulationGeometry  {

    /** Indicator if this elevation grid is solid or not. */
    private boolean solid;

    /** Indicator if this elevation grid has normals per vertex. */
    private boolean normalPerVertex;

    /** Indicator if this elevation grid has color per vertex. */
    private boolean colorPerVertex;

    /** Indicator if this elevation grid is wound CCW. */
    private boolean ccw;

    /** The crease angle for this elevation grid. */
    private float creaseAngle;

    /** The array defining each vertex's Y coordinate value. */
    private float[] height;

    /** The number of divisions in the X direction to make for this grid. */
    private int xDimension;

    /** The spacing between each X subdivision. */
    private float xSpacing;

    /** The number of divisions in the Z direction to make for this grid. */
    private int zDimension;

    /** The spacing between each Z subdivision. */
    private float zSpacing;

    /** The normal values. */
    private float[] normals;

    /** The color values. */
    private float[] colors;

    /** The texture coordinates. */
    private float[] texCoords;

	/** The def name of the geometry */
	private String defName;

    /**
     * Construct a default instance of this ElevationGrid.
     */
    ElevationGridGeometry() {
        ccw = true;
        colorPerVertex = true;
        creaseAngle = 0;
        normalPerVertex = true;
        solid = true;
        xDimension = 0;
        xSpacing = 1.0f;
        zDimension = 0;
        zSpacing = 1.0f;

        colors = null;
        normals = null;
        texCoords = null;
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
    	ccw = true;
        colorPerVertex = true;
        creaseAngle = 0;
        normalPerVertex = true;
        solid = true;
        xDimension = 0;
        xSpacing = 1.0f;
        zDimension = 0;
        zSpacing = 1.0f;

        height = null;
        normals = null;
        colors = null;
        texCoords = null;
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
        switch (name) {
            case "solid":
                if (value instanceof String) {
                    solid = fieldReader.SFBool((String)value);
                } else if (value instanceof Boolean) {
                    solid = ((Boolean)value);
                }
                break;
            case "ccw":
                if (value instanceof String) {
                    ccw = fieldReader.SFBool((String) value);
                } else if (value instanceof Boolean) {
                    ccw = ((Boolean)value);
                }
                break;
            case "colorPerVertex":
                if (value instanceof String) {
                    colorPerVertex = fieldReader.SFBool((String) value);
                } else if (value instanceof Boolean) {
                    colorPerVertex = ((Boolean)value);
                }
                break;
            case "normalPerVertex":
                if (value instanceof String) {
                    normalPerVertex = fieldReader.SFBool((String) value);
                } else if (value instanceof Boolean) {
                    normalPerVertex = ((Boolean)value);
                }
                break;
            case "xDimension":
                if (value instanceof String) {
                    xDimension = fieldReader.SFInt32((String) value);
                } else if (value instanceof Integer) {
                    xDimension = (Integer) value;
                }
                break;
            case "xSpacing":
                if (value instanceof String) {
                    xSpacing = fieldReader.SFFloat((String) value);
                } else if (value instanceof Float) {
                    xSpacing = (Float) value;
                }
                break;
            case "zDimension":
                if (value instanceof String) {
                    zDimension = fieldReader.SFInt32((String) value);
                } else if (value instanceof Integer) {
                    zDimension = (Integer) value;
                }
                break;
            case "zSpacing":
                if (value instanceof String) {
                    zSpacing = fieldReader.SFFloat((String) value);
                } else if (value instanceof Float) {
                    zSpacing = (Float) value;
                }
                break;
            case "height":
                if (value instanceof String) {
                    height = fieldReader.MFFloat((String) value);
                } else if (value instanceof String[]) {
                    height = fieldReader.MFFloat((String[]) value);
                } else if (value instanceof float[]) {
                    height = (float[])value;
                }
                break;
            case "Normal":
                if(value instanceof String) {
                    normals = fieldReader.MFVec3f((String)value);
                } else if(value instanceof String[]) {
                    normals = fieldReader.MFVec3f((String[])value);
                } else if(value instanceof float[]) {
                    normals = (float[])value;
                }   break;
            case "Color":
                if(value instanceof String) {
                    colors = fieldReader.MFVec3f((String)value);
                } else if(value instanceof String[]) {
                    colors = fieldReader.MFVec3f((String[])value);
                } else if(value instanceof float[]) {
                    colors = (float[])value;
                }   break;
            case "TextureCoordinate":
                if (value instanceof String) {
                    texCoords = fieldReader.MFVec3f((String) value);
                } else if (value instanceof String[]) {
                    texCoords = fieldReader.MFVec3f((String[]) value);
                } else if (value instanceof float[]) {
        		texCoords = (float[])value;
        	}
                break;
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
        switch (name) {
            case "height":
                if (value instanceof String[]) {
                    height = fieldReader.MFFloat((String[]) value);
                } else if (value instanceof float[]) {
                    height = (float[])value;
                }
                break;
            case "Normal":
                if(value instanceof String[]) {
                    normals = fieldReader.MFVec3f((String[])value);
                } else if(value instanceof float[]) {
                    normals = (float[])value;
                }   break;
            case "Color":
                if(value instanceof String[]) {
                    colors = fieldReader.MFVec3f((String[])value);
                } else if(value instanceof float[]) {
                    colors = (float[])value;
                }   break;
            case "TextureCoordinate":
                if (value instanceof String[]) {
                    texCoords = fieldReader.MFVec3f((String[]) value);
                } else if (value instanceof float[]) {
        		texCoords = (float[])value;
        	}
                break;
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

    	// Create the IndexedTriangleSet.
    	ch.startNode("IndexedTriangleSet", defName);
    	if (xDimension < 2 || zDimension < 2) {
    		ch.endNode();
    		if (errorReporter != null) {
    			errorReporter.errorReport("ElevationGridGeometry: insufficient dimensions to produce at least one quad (must be at least 2), creating empty shape. "
						+ "xDimension=" + xDimension + ", zDimension=" + zDimension, null);
    		}
    		return;
    	}

    	// Set the winding pattern.
    	ch.startField("ccw");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(ccw);
    	} else if (ch instanceof StringContentHandler) {
    		((StringContentHandler)ch).fieldValue(((Boolean)ccw).toString().toUpperCase());
    	}
    	/*
    	// Set the color per vertex flag.
    	ch.startField("colorPerVertex");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(colorPerVertex);
    	} else if (ch instanceof StringContentHandler) {
    		((StringContentHandler)ch).fieldValue(((Boolean)colorPerVertex).toString().toUpperCase());
    	}

    	// Set the normal per vertex flag.
    	ch.startField("normalPerVertex");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(normalPerVertex);
    	} else if (ch instanceof StringContentHandler) {
    		((StringContentHandler)ch).fieldValue(((Boolean)normalPerVertex).toString().toUpperCase());
    	}
    	*/
    	// Set the solid flag.
    	ch.startField("solid");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(solid);
    	} else if (ch instanceof StringContentHandler) {
    		((StringContentHandler)ch).fieldValue(((Boolean)solid).toString().toUpperCase());
    	}

    	// Create the triangle geometry
    	float[] coord = new float[xDimension * zDimension * 3];

    	// This is a grid with the height parameter set as the Y value
    	for (int i = 0; i < zDimension; i++) {
    		for (int j = 0; j < xDimension; j++) {
    			// Convenience indexing variable
    			int offset = (i*xDimension+j)*3;

    			coord[offset] = xSpacing * j;
    			coord[offset + 1] = height[j + i*xDimension];
    			coord[offset + 2] = zSpacing * i;
    		}
    	}

    	// Create the index for the geometry
    	int[] index = new int[6 * xDimension * (zDimension-1)];

    	// Split each quad in the grid into 2 triangles
    	for (int i = 0; i < xDimension * (zDimension - 1); i++) {
    		// Edge case, skip to the next iteration
    		if ((i % xDimension) == (xDimension - 1))
    			continue;

    		// Convenience indexing variable
    		int offset = i * 6;

    		// First triangle in the quad
    		index[offset] = i;
    		index[offset + 1] = i + xDimension;
    		index[offset + 2] = i + xDimension + 1;

    		// Increment the offset for more ease
    		offset += 3;

    		// Second triangle in the quad
    		index[offset] = i;
    		index[offset + 1] = i + xDimension + 1;
    		index[offset + 2] = i + 1;
    	}

    	// Change the winding per the specified flag
    	if (!ccw) {
    		int temp;
    		for ( int i = 0; i < index.length; i += 3) {
    			temp = index[i*3 + 1];
    			index[i*3 + 1] = index[i*3 + 2];
    			index[i*3 + 2] = temp;
    		}
    	}

    	// Write the output

    	// Coordinates
    	ch.startField("coord");
    	ch.startNode("Coordinate", null);
    	ch.startField("point");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(coord, coord.length);
    	} else if (ch instanceof StringContentHandler) {
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < coord.length; i++) {
    			sb.append(coord[i]);
    			sb.append(' ');
    		}
    		((StringContentHandler)ch).fieldValue(sb.toString().trim());
    	}
    	ch.endNode();
    	ch.endField();

    	// Indexes
    	ch.startField("index");
    	if (ch instanceof BinaryContentHandler) {
    		((BinaryContentHandler)ch).fieldValue(index, index.length);
    	} else if (ch instanceof StringContentHandler) {
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < index.length; i++) {
    			sb.append(index[i]);
    			sb.append(' ');
    		}
    		((StringContentHandler)ch).fieldValue(sb.toString().trim());
    	}
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
}
