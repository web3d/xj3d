/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.geom2d;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.TriangleArray;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BaseTriangleSet2D;

import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * OpenGL implementation of an TriangleSet2D
 * <p>
 *
 * The point set directly maps to Aviatrix3D's PointArray class.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class OGLTriangleSet2D extends BaseTriangleSet2D
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The impl for this class */
    private TriangleArray implGeom;

    /** The number of texture coordinate sets to be set by Shape Class */
    private int numTexCoordSets;

    /** Local set of normals */
    private float[] normals;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLTriangleSet2D() {
        numTexCoordSets = 0;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLTriangleSet2D(VRMLNodeType node) {
        super(node);
        numTexCoordSets = 0;
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
        return 0;
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
        return null;
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return implGeom;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        normals = new float[numVertices * 3 / 2];
        for(int i = 0; i < numVertices / 2; i++)
            normals[i * 3 + 2] = 1;

        implGeom = new TriangleArray();
        // TODO : check for explicit initialization of other fields as well
        implGeom.setVertices(TriangleArray.COORDINATE_2,
                             vfVertices,
                             numVertices / 2);
        implGeom.setNormals(normals);
        
        // Texture Coordinates should be generated only when Texture map is required.
        if (numTexCoordSets != 0) {
            // Turn into a flat triangle array
            GeometryUtils gutils = new GeometryUtils();
            GeometryHolder gholder = new GeometryHolder();

            float creaseAngle = 0;

            int numTriangle = numVertices / 6;
            float[] coords;
            int[] indexes;

            // Generate coord array
            coords = new float[numTriangle * 3 * 3];

            for (int i = 0; i < numTriangle * 3; i++) {
                coords[i * 3 + 0] = vfVertices[i * 2 + 0];  // x
                coords[i * 3 + 1] = vfVertices[i * 2 + 1];  // y
                coords[i * 3 + 2] = 0;                     // z
            }

            // Generate an index array
            int size = numTriangle * 4;
            indexes = new int[size];

            int currIndex = 0;
            int start = 0;

            for (int i = 0; i < numTriangle; i++) {
                indexes[currIndex++] = start;
                indexes[currIndex++] = start + 1;
                indexes[currIndex++] = start + 2;
                indexes[currIndex++] = -1;
                start += 3;
            }

            // only for texture mapping. It is from OGLExtrusion.java.
            gutils.generateTriangleArrays(  coords,
                                            null,   // float[] color
                                            null,   // float[] normal
                                            null,   // float[] texture
                                            1,      // changeFlags
                                            true,   // genTexCoords,
                                            false,  // genNormals,
                                            indexes,
                                            indexes.length,
                                            null,   // int[] vfColorIndex,
                                            null,   // int[] vfNormalIndex,
                                            null,   // int[] vfTexCoordIndex,
                                                    // TODO : are we obeying the ccw right-hand-
                                            true,   // rule or not?  this is far different
                                                    // than the meaning of vfCCW!
                                            true,   // Convex
                                            false,  // colorPerVertex,
                                            false,  // normalPerVertex,
                                            0,      // numColorComponents
                                            creaseAngle,
                                            true,   // initialBuild,
                                            gholder);

            // only for 1 texture mapping. It is from OGLBox.java.
            // Make an array of objects for the texture setting
            float[][] textures = gholder.textureCoordinates;
            int[] tex_type = { TriangleArray.TEXTURE_COORDINATE_2 };
            implGeom.setTextureCoordinates(tex_type, textures, 1);

            // Setup texture units
            int[] tex_maps = new int[numTexCoordSets];
            tex_maps[0] = 0;
            implGeom.setTextureSetMap(tex_maps, numTexCoordSets);
        }
    }

    //----------------------------------------------------------
    // Methods defined by UpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
        implGeom.setVertices(TriangleArray.COORDINATE_2,
                             vfVertices,
                             numVertices / 2);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {
        implGeom.setNormals(normals);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the value of the vertices field.
     *
     * @param coords The list of coordinates to use
     * @param numValid The number of valid values to copy from the array
     */
    @Override
    protected void setVertices(float[] coords, int numValid) {
        super.setVertices(coords, numValid);

        if(!inSetup) {
            if(normals.length < numValid * 3 / 2) {
                normals = new float[numVertices * 3 / 2];
                for(int i = 0; i < numVertices / 2; i++)
                    normals[i * 3 + 1] = 1;
            }

            if (implGeom.isLive()) {
                implGeom.boundsChanged(this);
                implGeom.dataChanged(this);
            } else {
                updateNodeDataChanges(implGeom);
                updateNodeBoundsChanges(implGeom);
            }
        }
    }
}
