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
import org.j3d.aviatrix3d.QuadArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TriangleArray;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BaseRectangle2D;

import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * OpenGL implementation of an Rectangle2D
 * <p>
 *
 * The point set directly maps to Aviatrix3D's PointArray class.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class OGLRectangle2D extends BaseRectangle2D
    implements OGLGeometryNodeType {

    /** The impl for this class */
    //private QuadArray implGeom;
    private TriangleArray implGeom;  // To use texture map change class
    
    /** The number of texture coordinate sets to be set by Shape Class */
    private int numTexCoordSets;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLRectangle2D() {
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
    public OGLRectangle2D(VRMLNodeType node) {
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

        float size_x = vfSize[0] / 2;
        float size_y = vfSize[1] / 2;

        /* To use texture map, Change class from QuadArray() to TriangleArray()
         *
        float[] coords = {
            -size_x, -size_y,
             size_x, -size_y,
             size_x,  size_y,
            -size_x,  size_y
        };

        float[] normals = {
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1
        };

        implGeom = new QuadArray();
        implGeom.setVertices(QuadArray.COORDINATE_2, coords, 4);
        implGeom.setNormals(normals);
        */
        
        float[] coords = {
            -size_x, -size_y,
             size_x, -size_y,
             size_x,  size_y,
             size_x,  size_y,
            -size_x,  size_y,
            -size_x, -size_y
        };

        float[] normals = {
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1
        };

        implGeom = new TriangleArray();
        // TODO : check for explicit initialization of other fields as well
        implGeom.setVertices(TriangleArray.COORDINATE_2,
                             coords,
                             6);
        implGeom.setNormals(normals);
        
        // Texture Coordinates should be generated only when Texture map is required.
        if (numTexCoordSets != 0) {
            // Turn into a flat triangle array
            GeometryUtils gutils = new GeometryUtils();
            GeometryHolder gholder = new GeometryHolder();

            float creaseAngle = 0;

            // Generate 3D coord array
            float[] coords3D = {
                -size_x, -size_y, 0,
                 size_x, -size_y, 0,
                 size_x,  size_y, 0,
                 size_x,  size_y, 0,
                -size_x,  size_y, 0,
                -size_x, -size_y, 0                
            };
            int[] indexes = {
                0, 1, 2, -1,
                3, 4, 5, -1
            };

            // only for texture mapping. It is from OGLExtrusion.java.
            gutils.generateTriangleArrays(  coords3D,
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
            int[] tex_type = { QuadArray.TEXTURE_COORDINATE_2 };
            implGeom.setTextureCoordinates(tex_type, textures, 1);

            // Setup texture units
            int[] tex_maps = new int[numTexCoordSets];
            tex_maps[0] = 0;
            implGeom.setTextureSetMap(tex_maps, numTexCoordSets);
        }
    }
}
