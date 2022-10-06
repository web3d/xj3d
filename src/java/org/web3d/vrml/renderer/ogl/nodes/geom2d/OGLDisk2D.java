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
import org.j3d.aviatrix3d.VertexGeometry;
import org.j3d.aviatrix3d.TriangleFanArray;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.SceneGraphObject;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.common.nodes.geom2d.BaseDisk2D;

/**
 * OpenGL implementation of an Disc2D
 * <p>
 *
 * The point set directly maps to Aviatrix3D's PointArray class.
 * <p>
 *
 * @author Vincent Marchetti
 * @version $Revision: 1.3 $
 */
public class OGLDisk2D extends BaseDisk2D
    implements OGLGeometryNodeType {

    /** The impl for this class */
    private VertexGeometry implGeom;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLDisk2D() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLDisk2D(VRMLNodeType node) {
        super(node);
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
        int NP=12;
        int NX=4*NP+1;
        float[] uc = new float[2*NX];
        double wedge_a = Math.PI/(2*NP);
        for (int i=0; i< 4*NP; ++i){
            double angle = i*wedge_a;
            uc[2*i]= (float) Math.cos(angle);
            uc[2*i+1]= (float) Math.sin(angle);
        }
        uc[8*NP] = 1.0f;
        uc[8*NP+1] = 0.0f;

        float[] vertices;

        if (vfInnerRadius == 0.0){
            int NVert = NX+1;

            vertices=new float[3*NVert];

            for (int i=0; i<NVert-1; ++i){
                vertices[3*i+3] = vfOuterRadius*uc[2*i];
                vertices[3*i+4] = vfOuterRadius*uc[2*i+1];
                vertices[3*i+5] = 0.0f;
            }

            TriangleFanArray fan = new TriangleFanArray();
            fan.setVertices(TriangleFanArray.COORDINATE_3, vertices,NVert);
            int[] counts = {NVert};
            fan.setFanCount(counts, 1);
            implGeom = fan;
        }
        else{
            int NVert = 2 * NX;

            vertices=new float[3*NVert];

            for (int i=0; i< NVert/2; ++i){
                vertices[6*i+0] = vfInnerRadius*uc[2*i];
                vertices[6*i+1] = vfInnerRadius*uc[2*i+1];
                vertices[6*i+2] = 0.0f;
                vertices[6*i+3] = vfOuterRadius*uc[2*i];
                vertices[6*i+4] = vfOuterRadius*uc[2*i+1];
                vertices[6*i+5] = 0.0f;
            }

            TriangleStripArray strip = new TriangleStripArray();
            strip.setVertices(TriangleStripArray.COORDINATE_3, vertices,NVert);
            int[] counts = {NVert};
            strip.setStripCount(counts, 1);
            implGeom = strip;
        }
        /*
        float size_x = vfSize[0] / 2;
        float size_y = vfSize[1] / 2;

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
    }
}
