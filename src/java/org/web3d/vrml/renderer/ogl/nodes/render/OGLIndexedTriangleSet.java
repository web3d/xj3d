/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.render;

// External imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.common.nodes.render.BaseIndexedTriangleSet;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * OpenGL implementation of an IndexedTriangleSet.
 * <p>
 *
 * The point set directly maps to Aviatrix3D's IndexedTriangleArray class. When the
 * coordinates change to a different length than the current set, it will
 * notify the geometry listener to fetch the new information.
 * <p>
 * If the VRML file did not provide a Coordinate node, then this class will
 * not present any geometry from the {@link #getGeometry()} or
 * {@link #getSceneGraphObject()} calls. If the user later specifies the
 * renderety through an event, the listener(s) will be notified.
 * <p>
 * In this implementation, if the length of the color array is shorter that
 * the length of the coordinate array, colors will be ignored.
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.19 $
 */
public class OGLIndexedTriangleSet extends BaseIndexedTriangleSet
    implements OGLGeometryNodeType,
               NodeUpdateListener {

    /** The impl for this class */
    private VertexGeometry implGeom;

    /** The number of texture coordinate sets to be set by Shape Class */
    private int numTexCoordSets;

    /** temp array to copy values from the texCoord field to the geometry */
    private float[][] texCoords;

    /** temp array to copy values from the texCoord field to the geometry */
    private int[] texSets;

    /** Final number of texture sets to send to the graphics card */
    private int numTexSets;

    /** Final processed version of the texture set mapping of each stage */
    private int[] texSetMap;

    /** temp array to copy values from the texCoord field to the geometry */
    private int[] texTypes;

    /** Final number of texture sets to send to the graphics card */
    private int numUniqueTexSets;

    /** Flag to indicate the coordinates changed, and not the index */
    private boolean coordChanged;

    /** Flag to indicate the colors changed */
    private boolean colorChanged;

    /** Flag to indicate the texture coords changed */
    private boolean texCoordChanged;

    /** Flag to indicate the normals changed */
    private boolean normalChanged;

    /** Flag to indicate the index list changed */
    private boolean indexChanged;

    /** Are we using indexed geometry */
    private boolean indexed;

    /** The geometryUtils used */
    private GeometryUtils gutils;

    /** The number of geometry builds.  Optimize for static till proven dynamic */
    private int numBuilds;

    /** The converted vfIndex arrays */
    private int[] indexes;

    /** Did the vbo state change */
    private boolean vboChanged;

    /** Is the internal geometry representation ccw */
    private boolean geomCcw;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLIndexedTriangleSet() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLIndexedTriangleSet(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /*
     * Returns a OGL Geometry collection that represents this piece of
     * geometry. If there is only one piece of geometry this will return
     * an array of lenght 1.
     *
     * @return The geometry needed to represent this object
     */
    @Override
    public Geometry getGeometry() {
        return implGeom;
    }

    /**
     * Set the number of textures that were found on the accompanying Appearance
     * node. Used to set the number of texture coordinates that need to be
     * passed in to the renderer when no explicit texture coordinates were
     * given.
     *
     * @param count The number of texture coordinate sets to add
     */
    @Override
    public void setTextureCount(int count) {
        numTexCoordSets = count;
    }
    
    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    @Override
    public int getNumSets() {
        int ret_val = 0;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getNumSets();

        return ret_val;
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    @Override
    public String getTexCoordGenMode(int setNum) {
        String ret_val = null;

        if(vfTexCoord != null)
            ret_val = vfTexCoord.getTexCoordGenMode(setNum);

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {

        if(coordChanged) {
            if(vfCoord == null) {
                implGeom.setValidVertexCount(0);
            } else {
                implGeom.setVertices(IndexedTriangleArray.COORDINATE_3,
                                     geomData.coordinates,
                                     geomData.vertexCount);
            }
        }

        if(indexChanged) {
            if (indexed) {
                IndexedTriangleArray ita = (IndexedTriangleArray) implGeom;

                ita.setIndices(vfIndex, numIndex);
            }

            indexChanged = false;
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {

        if(colorChanged) {
            if(vfColor == null) {
                implGeom.setColors(false, null);
            } else {
                boolean alpha = (vfColor.getNumColorComponents() == 4);
                implGeom.setColors(alpha, geomData.colors);
            }

            colorChanged = false;
        }

        if(texCoordChanged) {
            if( numTexCoordSets == 0 ) {
                implGeom.setTextureCoordinates(null, null, 0);
            } else {
                implGeom.setTextureCoordinates(texTypes,
                                               texCoords,
                                               numUniqueTexSets);
                implGeom.setTextureSetMap(texSetMap, numTexSets);
            }

            texCoordChanged = false;
        }

        if(normalChanged) {
            if(vfNormal == null) {
                implGeom.setNormals(geomData.normals);
            } else {
                updateNormalArray();
                implGeom.setNormals(geomData.normals);
            }

            normalChanged = false;
        }

        if (vboChanged) {
            // Only ever change to false after activity
            implGeom.setVBOEnabled(false);

            vboChanged = false;
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Build the implementation.  May have to change underlying Aviatrix3D representation
     * if the normal node changes.
     */
    @Override
    protected void buildImpl() {
        boolean newImpl = false;

        // Decide whether to rebuild
        if (vfNormal != null) {
			geomCcw = vfCcw;
            geomData.geometryType = GeometryData.INDEXED_TRIANGLES;

            if (!indexed || implGeom == null) {
                implGeom = new IndexedTriangleArray(true, VertexGeometry.VBO_HINT_STATIC);

                OGLUserData u_data = new OGLUserData();
                u_data.geometryData = geomData;

                implGeom.setUserData(u_data);

                indexed = true;
                newImpl = true;
            }

            if(newImpl || (changeFlags & COORDS_INDEX_CHANGED) != 0) {
                updateIndexMaps();
            }

            if(((changeFlags & COORDS_CHANGED) != 0)) {
                updateCoordinateArray();
            }

            if(((changeFlags & COLORS_CHANGED) != 0)) {
                updateColorArray();
            }

            if(((changeFlags & NORMALS_CHANGED) != 0)) {
                if(!vfNormalPerVertex) {
                    errorReporter.warningReport("Normal per face not supported in IndexedTriangleSet", null);
                } else
                    updateNormalArray();
            }

            if(((changeFlags & TEXCOORDS_CHANGED) != 0)) {
                numTexSets = vfTexCoord.getNumSets();
                int numRealSets = numTexSets;
                int max_set_size = vfTexCoord.getSize(0);
                int num_tex_comp = vfTexCoord.getNumTextureComponents();
                int tex_format = 0;

                for(int i = 0; i < numTexSets; i++) {
                    if (i != vfTexCoord.isShared(i))
                        numRealSets--;
                }

                numUniqueTexSets = numRealSets;

                // first check for max required size

                for(int i = 1; i < numTexSets; i++) {
                    int size = vfTexCoord.getSize(i);
                    if(max_set_size > size)
                        max_set_size = size;
                }

                if(max_set_size < (geomData.vertexCount * num_tex_comp))
                    max_set_size = geomData.vertexCount * num_tex_comp;

                if (texCoords == null || texCoords.length != numRealSets)
                    texCoords = new float[numRealSets][];


                if (texSetMap == null || texSetMap.length != numTexSets)
                    texSetMap = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++)
                    texSetMap[i] = vfTexCoord.isShared(i);

                if (texTypes == null || texTypes.length != numUniqueTexSets)
                    texTypes = new int[numUniqueTexSets];

                switch(num_tex_comp) {
                    case 1:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_1;
                        break;

                    case 2:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_4;
                        break;

                    default:
                        // we should never get this, but just in case
                        System.out.println("Invalid number of texture " +
                                           "components " +
                                           vfTexCoord.getNumTextureComponents());
                }

                for(int i = 0; i < numUniqueTexSets; i++) {
                    texTypes[i] = tex_format;
                }

                int currSet = 0;

                for(int i = 0; i < numTexSets; i++) {
                    texSetMap[i] = vfTexCoord.isShared(i);

                    if(texSetMap[i] == currSet) {
                        texCoords[currSet] = new float[max_set_size];
                        vfTexCoord.getPoint(i, texCoords[currSet++]);
                    }
                }
            }
        } else {
            geomData.geometryType = GeometryData.TRIANGLES;

            if (indexed || implGeom == null) {
                implGeom = new TriangleArray(true, VertexGeometry.VBO_HINT_STATIC);

                OGLUserData u_data = new OGLUserData();
                u_data.geometryData = geomData;

                implGeom.setUserData(u_data);

                indexed = false;
                newImpl = true;
            }

            // Turn into a flat triangle array
            if (gutils == null)
                gutils = new GeometryUtils();

            GeometryHolder gholder = new GeometryHolder();

            float creaseAngle;

            if (vfNormalPerVertex)
                creaseAngle = (float)Math.PI;
            else
                creaseAngle = 0;

            // We need to add -1's for each triangle
            if (indexes == null || (changeFlags & COORDS_INDEX_CHANGED) != 0) {

                if (numIndex == 0) {
                    indexChanged = true;

                    if (implGeom.isLive()) {
                        implGeom.boundsChanged(this);
                        implGeom.dataChanged(this);
                    } else {
                        updateNodeBoundsChanges(null);
                        updateNodeDataChanges(null);
                    }

                    return;
                }

                // Allocate array to numVerts plus a -1 per triangle
                int size = numIndex + numIndex / 3;

                if (indexes == null || indexes.length < size)
                    indexes = new int[size];

                int pos = 0;
                int currIndex = 0;

                try {
                    while (pos < numIndex - 1) {
                        indexes[currIndex++] = vfIndex[pos++];
                        indexes[currIndex++] = vfIndex[pos++];
                        indexes[currIndex++] = vfIndex[pos++];
                        indexes[currIndex++] = -1;
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    errorReporter.warningReport("Invalid index in TriangleSet at position: " + pos, null);
                }
            }

            gutils.generateTriangleArrays(changeFlags, true, true,  //Always generate textureCoordinates
               vfCoord, vfColor, vfNormal, vfTexCoord,
               indexes, indexes.length, indexes, indexes,
               indexes, vfCcw, true, true, vfNormalPerVertex,
               creaseAngle, newImpl, gholder);

			// generateTriangleArrays re-orders the index if ccw is false,
			// so ccw should always be true at this point
			geomCcw = true;

            if (newImpl && gholder.coordinates == null)
                return;

            if (!newImpl) {
                vboChanged = true;

                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            }

            gutils.copyData(gholder, geomData);

            texCoords = gholder.textureCoordinates;
            numTexSets = gholder.numTexSets;
            numUniqueTexSets = gholder.numUniqueTexSets;

            if(numTexCoordSets == 0) {
                 // Clear tex coords
                if (implGeom.isLive()) {
                    texTypes = null;
                    texCoords = null;
                }
            } else {
             /* This block is removed, because it is never used. 
                We are leaving it visible just in case it is needed 
                for 3D texture, multi texture or something else.
                
                int tex_format = 0;
                int num_tex_comp = vfTexCoord.getNumTextureComponents();

                switch(num_tex_comp) {
                    case 1:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_1;
                        break;

                    case 2:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_format = TriangleArray.TEXTURE_COORDINATE_4;
                        break;

                    default:
                        // we should never get this, but just in case
                        System.out.println("Invalid number of texture " +
                                           "components " +
                                           vfTexCoord.getNumTextureComponents());
                }
                */

                int tex_format = TriangleArray.TEXTURE_COORDINATE_2;

                if (texSetMap == null || texSetMap.length != numTexSets)
                    texSetMap = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++)
                    texSetMap[i] = 0;

                if (texTypes == null || texTypes.length != numTexSets)
                    texTypes = new int[numTexSets];

                for(int i = 0; i < numTexSets; i++) {
                    texTypes[i] = tex_format;
                }
            }
        }

        if (newImpl) {
            int num_items = vfCoord.getNumPoints();

            if (num_items < VBO_MIN_VERTICES) {
                vboChanged = true;

                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            }

            indexChanged = true;
            coordChanged = true;
            colorChanged = true;
            normalChanged = true;
            texCoordChanged = true;

            if (implGeom.isLive()) {
                implGeom.boundsChanged(this);
                implGeom.dataChanged(this);
            } else {
                updateNodeBoundsChanges(null);
                updateNodeDataChanges(null);
            }
        } else {
            if((changeFlags & COORDS_CHANGED) != 0) {
                coordChanged = true;
                if (implGeom.isLive())
                    implGeom.boundsChanged(this);
                else
                    updateNodeBoundsChanges(implGeom);
            }

            if((((changeFlags & COORDS_CHANGED) != 0) && (vfNormal == null)) ||
               ((changeFlags & NORMALS_CHANGED) != 0)) {

                normalChanged = true;
                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            }

            // Build stuff that we're missing in the texture department
            if(((changeFlags & TEXCOORDS_CHANGED) != 0)) {
                if(numTexCoordSets == 0) {
                     // Clear tex coords
                    if (implGeom.isLive()) {
                        texTypes = null;
                        texCoords = null;

                        texCoordChanged = true;
                        implGeom.dataChanged(this);
                    } else {
                        implGeom.setTextureCoordinates(null, null);
                    }
                } else {
                    texCoordChanged = true;

                    if (implGeom.isLive())
                        implGeom.dataChanged(this);
                    else
                        updateNodeDataChanges(implGeom);
                }
            }

            if(((changeFlags & COLORS_CHANGED) != 0)) {
                colorChanged = true;
                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);
            }
        }

        changeFlags = 0;

        if(isStatic || numBuilds < 1) {
            if (gutils != null) {
                gutils.reset();
                gutils = null;

                indexes = null;
            }

            if (inSetup) {
                // We can ditch the tex coords as well
                texCoords = null;
                texTypes = null;
                texSetMap = null;
            }
        }

        numBuilds++;
    }

    /**
     * Common initialization functionality.
     */
    private void init() {
        geomData = new GeometryData();
        geomCcw = true;   // TODO : check for explicit initialization of other fields as well
        
        numTexCoordSets = 0;    // Add number of Texture to be set by Shape Class

        indexChanged = false;
        coordChanged = false;
        colorChanged = false;
        normalChanged = false;
        texCoordChanged = false;
        vboChanged = false;
    }

    /**
     * Get the value of the CCW field.
     *
     * @return true Vertices are declared in counter-clockwise order
     */
    @Override
    public boolean isCCW() {
        return geomCcw;
    }
}
