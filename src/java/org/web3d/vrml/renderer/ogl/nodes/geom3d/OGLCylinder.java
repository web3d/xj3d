/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.geom3d;

// External imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;
import org.j3d.aviatrix3d.NodeUpdateListener;

import org.j3d.geom.GeometryData;
import org.j3d.geom.CylinderGenerator;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseCylinder;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * OGL implementation of a Cylinder.
 * <p>
 *
 * A box is a fixed size object in VRML. Once set at startup, the size cannot
 * be changed. All dynamic requests to modify the size of this implementation
 * will generate an exception.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.13 $
 */
public class OGLCylinder extends BaseCylinder
    implements OGLGeometryNodeType, NodeUpdateListener {

    /** The geometry implementation */
    private TriangleStripArray impl;

    /** The number of texture coordinate sets to use */
    private int numTexCoordSets;

    /**
     * Construct new default box object.
     */
    public OGLCylinder() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Cylinder node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLCylinder(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType 
    //-------------------------------------------------------------

    /*
     * Returns a OGL Geometry node.
     *
     * @return A Geometry node
     */
    @Override
    public Geometry getGeometry() {
        return impl;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType 
    //-------------------------------------------------------------

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

        if(inSetup)
            return;

        if(impl.isLive())
            impl.dataChanged(this);
         else
            updateNodeDataChanges(impl);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return impl;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

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

        if (vfTop == false && vfBottom == false && vfSide == false) {
            return;
        }

        CylinderGenerator generator = new CylinderGenerator(vfHeight,
                                                    vfRadius,
                                                    vfTop,
                                                    vfBottom,
                                                    vfSide);
        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        impl = new TriangleStripArray(false, VertexGeometry.VBO_HINT_STATIC);
        impl.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        impl.setStripCount(data.stripCounts, data.numStrips);
        impl.setNormals(data.normals);

        // Make an array of objects for the texture setting
        float[][] textures = { data.textureCoordinates };
        int[] tex_type = { TriangleStripArray.TEXTURE_COORDINATE_2 };

        impl.setTextureCoordinates(tex_type, textures, 1);

        // Setup texture units
        int[] tex_maps = new int[numTexCoordSets];

        for(int i=0; i < numTexCoordSets; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps, numTexCoordSets);

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = data;

        impl.setUserData(u_data);

        // Release generator
        generator = null;
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {
        int[] tex_maps = new int[numTexCoordSets];

        for(int i=0; i < numTexCoordSets; i++)
            tex_maps[i] = 0;

        impl.setTextureSetMap(tex_maps, numTexCoordSets);
    }
}
