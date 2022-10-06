/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.geospatial;

// External imports
import javax.vecmath.Vector3d;

import org.j3d.aviatrix3d.*;

import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.j3d.geom.GeometryData;

import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoElevationGrid;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoOrigin;
import org.web3d.vrml.renderer.common.nodes.geospatial.ElevationGridGenerator;
import org.web3d.vrml.renderer.common.nodes.BaseComponentGeometryNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

import org.xj3d.core.eventmodel.OriginListener;
import org.xj3d.impl.core.eventmodel.OriginManagerFactory;

/**
 * OpenGL implementation of an GeoElevationGrid
 * <p>
 *
 * In order to handle colorPerVertex=FALSE and normalPerVertex=FALSE we
 * have to two representations.  Under normal conditions(TRUE,TRUE) we can
 * use TriStrips.  Otherwise we must revert back to Quads to get the right
 * coloring.
 *
 * Issues:
 *
 * If you specify normalPerVertex = FALSE but colorPerVertex = TRUE then
 * the colorInterpolation is different then Cosmo/Cortona.  Is it wrong, hard
 * to say.
 *
 * CreaseAngle is not supported.  All terrains are smooth shaded.
 *
 * Not sure ccw handling is correct.
 *
 * TODO:
 *    Needed Observers: color, normal, texCoord
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class OGLGeoElevationGrid extends BaseGeoElevationGrid
    implements OGLGeometryNodeType, NodeUpdateListener, OriginListener {

    /** The impl for this class */
//    private TriangleStripArray implGeom;
    private IndexedQuadArray implGeom;

    /** The generator used to construct nex e-grid parts */
    private ElevationGridGenerator generator;

    /** Describes the data for representation */
    private GeometryData data;

    /** Did the vbo state change */
    private boolean vboChanged;

    /** Flag indicating that the geometry vertices have changed due to a
     *  change in the origin */
    private boolean vertices_changed;

    /** Local copy of the grid vertices, in gcc */
    private double[] vertex;

    /**
     * Default constructor
     */
    public OGLGeoElevationGrid() {
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
    public OGLGeoElevationGrid(VRMLNodeType node) {
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

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNodeType
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

        originManager = OriginManagerFactory.getInstance(stateManager);
        useOriginManager = originManager.getEnabled();

        implGeom = new IndexedQuadArray(true, VertexGeometry.VBO_HINT_STATIC);
        buildImpl();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, child);

        if(inSetup)
            return;

        switch(index) {
            case FIELD_NORMAL:
            case FIELD_COLOR:
            case FIELD_TEXCOORD:
                if (implGeom.isLive())
                    implGeom.boundsChanged(this);
                else
                    updateNodeBoundsChanges(implGeom);
                break;
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

    /**
     * Set the components that compose a geometry object.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException {

        super.setComponents(comps);

        if(inSetup)
            return;

        if (implGeom.isLive())
            implGeom.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGeom);
    }

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setComponent(VRMLNodeType comp)
        throws InvalidFieldValueException {

        super.setComponent(comp);

        if(inSetup)
            return;

        if (implGeom.isLive())
            implGeom.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGeom);
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
        if (vertices_changed) {
            implGeom.setVertices(
                    VertexGeometry.COORDINATE_3,
                    data.coordinates,
                    data.vertexCount);
        } else {
            // rem: this is the original 'sum' of this method - and is
            // certain to be broken. buildImpl() calls back into the
            // avaitrix management routines, which will cause exceptions
            buildImpl();
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
        if (vboChanged) {
            // Only ever change to false after activity
            implGeom.setVBOEnabled(false);

            vboChanged = false;
        }
    }

    //----------------------------------------------------------
    // Methods defined by OriginListener
    //----------------------------------------------------------

    /**
     * Notification that the origin has changed.
     */
    @Override
    public void originChanged() {
        Vector3d origin = originManager.getOrigin();

        boolean update = false;
        if (origin == null) {
            if (local_origin != null) {
                local_origin = null;
                update = true;
            }
        } else {
            if (local_origin == null) {
                    local_origin = new double[3];
            }
            if ((local_origin[0] != origin.x) || (local_origin[1] != origin.y)
                    || (local_origin[2] != origin.z)) {

                local_origin[0] = origin.x;
                local_origin[1] = origin.y;
                local_origin[2] = origin.z;
                update = true;
            }
        }

        if (update) {
            int idx = 0;
            int num_vertex = vertex.length / 3;
            float[] coords = data.coordinates;
            if (local_origin == null) {
                for (int i = 0; i < num_vertex; i++) {
                    idx = i * 3;
                    coords[idx] = (float) vertex[idx];
                    coords[idx + 1] = (float) vertex[idx + 1];
                    coords[idx + 2] = (float) vertex[idx + 2];
                }
            } else {
                for (int i = 0; i < num_vertex; i++) {
                    idx = i * 3;
                    coords[idx] = (float) (vertex[idx] - local_origin[0]);
                    coords[idx + 1] = (float) (vertex[idx + 1] - local_origin[1]);
                    coords[idx + 2] = (float) (vertex[idx + 2] - local_origin[2]);
                }
            }
            vertices_changed = true;
            if (implGeom.isLive()) {
                implGeom.boundsChanged(this);
            } else {
                updateNodeBoundsChanges(implGeom);
            }
        }
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Build the OGL representation from the VRML field data
     */
    private void buildImpl() {
        if(vfHeight == null || vfHeight.length == 0)
            return;

        if (vfHeight.length < BaseComponentGeometryNode.VBO_MIN_VERTICES) {
            // Turns off VBO's
            vboChanged = true;
            if (implGeom.isLive())
                implGeom.dataChanged(this);
            else
                updateNodeDataChanges(implGeom);
        }

        generator = new ElevationGridGenerator((vfXDimension - 1) * vfXSpacing,
                                               (vfZDimension - 1) * vfZSpacing,
                                               vfXDimension, vfZDimension, vfYScale,
                                               vfHeight);

	vertex = new double[vfXDimension * vfZDimension * 3];
//        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryType = GeometryData.INDEXED_QUADS;

        boolean generated_textures = false;

        if(vfTexCoord == null && pTexCoord == null) {
            generated_textures = true;
            data.geometryComponents = GeometryData.TEXTURE_2D_DATA;
        }

        if(vfNormal == null && pNormal == null) {
            data.geometryComponents = data.geometryComponents |
                                      GeometryData.NORMAL_DATA;
        }

        try {
            if(geoCoordSwap) {
                double tmp = vfGeoGridOrigin[0];
                vfGeoGridOrigin[0] = vfGeoGridOrigin[1];
                vfGeoGridOrigin[1] = tmp;
                generator.generateIndexedQuads(data,
                                   geoTransform,
                                   vfGeoGridOrigin,
				   null,
                                   false,
                                   vfCreaseAngle,
                                   vertex);
                tmp = vfGeoGridOrigin[0];
                vfGeoGridOrigin[0] = vfGeoGridOrigin[1];
                vfGeoGridOrigin[1] = tmp;
            } else {
                generator.generateIndexedQuads(data,
                                   geoTransform,
                                   vfGeoGridOrigin,
                                   null,
                                   false,
                                   vfCreaseAngle,
                                   vertex);
            }
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
            return;
        }

        if (useOriginManager) {
            Vector3d origin = originManager.getOrigin();
            if (origin == null) {
                local_origin = null;
            } else {
                if (local_origin == null) {
                    local_origin = new double[3];
                }
                if ((local_origin[0] != origin.x) || (local_origin[1] != origin.y)
                        || (local_origin[2] != origin.z)) {

                    local_origin[0] = origin.x;
                    local_origin[1] = origin.y;
                    local_origin[2] = origin.z;
                }
            }
        } else if (vfGeoOrigin != null) {
            local_origin = ((VRMLLocalOriginNodeType) vfGeoOrigin).getConvertedCoordRef();
        }
        if (local_origin != null) {
            int idx = 0;
            int num_vertex = vertex.length / 3;
            float[] coords = data.coordinates;
            for (int i = 0; i < num_vertex; i++) {
                idx = i * 3;
                coords[idx] = (float) (vertex[idx] - local_origin[0]);
                coords[idx + 1] = (float) (vertex[idx + 1] - local_origin[1]);
                coords[idx + 2] = (float) (vertex[idx + 2] - local_origin[2]);
            }
        }

        OGLUserData u_data = new OGLUserData();
        u_data.geometryData = data;
        implGeom.setUserData(u_data);

        implGeom.setVertices(VertexGeometry.COORDINATE_3,
                             data.coordinates,
                             data.vertexCount);

//        implGeom.setStripCount(data.stripCounts, data.numStrips);

        implGeom.setIndices(data.indexes, data.indexesCount);

        if(generated_textures) {
            int[] tex_types = new int[]{
                VertexGeometry.TEXTURE_COORDINATE_2,
                VertexGeometry.TEXTURE_COORDINATE_2};
            float[][] tex_coords = {
                data.textureCoordinates,
                data.textureCoordinates};
            implGeom.setTextureCoordinates(tex_types, tex_coords, 2);
        } else {
            VRMLTextureCoordinateNodeType coords;
            if(pTexCoord != null) {
                coords = (VRMLTextureCoordinateNodeType)
                    pTexCoord.getImplementationNode();
            } else {
                coords = vfTexCoord;
            }

            int size;
            int num_tex_sets = coords.getNumSets();
            float[][] lfTexCoord = new float[num_tex_sets][];
            int[] tex_set_map = new int[num_tex_sets];
            int[] tex_types = new int[num_tex_sets];
            //float[] tcd_tmp = new float[coords.getSize(0) * 2];

            for(int i = 0; i < num_tex_sets; i++) {
                int num_comps = coords.getNumTextureComponents();

                switch(num_comps) {
                    case 1:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_1;
                        break;

                    case 2:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_2;
                        break;

                    case 3:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_3;
                        break;

                    case 4:
                        tex_types[i] = VertexGeometry.TEXTURE_COORDINATE_4;
                        break;
                }

                size = coords.getSize(i);

                tex_set_map[i] = coords.isShared(i);

                int req_coord_num = data.vertexCount * num_comps;

                if(size < req_coord_num)
                    size = req_coord_num;

                if(lfTexCoord[i] == null || lfTexCoord[i].length < size)
                    lfTexCoord[i] = new float[size];

                //if(tcd_tmp.length < size)
                //    tcd_tmp = new float[size];

                //coords.getPoint(i, tcd_tmp);
                coords.getPoint(i, lfTexCoord[i]);

                /*
                // Now convert this to triangle strip values. 0
                int row_size = vfZDimension * num_comps;
                int ts_offset = req_coord_num - 1;
                int base_offset =
                    vfXDimension * vfZDimension * num_comps - 1;
                for(int j = 0; j < (vfXDimension - 1); j++) {
                    for(int k = 0; k < vfZDimension; k++) {

                        lfTexCoord[i][ts_offset--] = tcd_tmp[base_offset];
                        lfTexCoord[i][ts_offset--] = tcd_tmp[base_offset - 1];

                        lfTexCoord[i][ts_offset--] =
                            tcd_tmp[base_offset - row_size];
                        lfTexCoord[i][ts_offset--] =
                            tcd_tmp[base_offset - row_size - 1];

                        base_offset -= num_comps;
                    }
                }
                */
            }

            implGeom.setTextureCoordinates(tex_types,
                                           lfTexCoord,
                                           num_tex_sets);
            implGeom.setTextureSetMap(tex_set_map, num_tex_sets);
        }

        if(vfNormal != null || pNormal != null) {
            VRMLNormalNodeType normalNode;

            if (pNormal != null) {
                normalNode = (VRMLNormalNodeType)
                    pNormal.getImplementationNode();
            } else {
                normalNode = vfNormal;
            }

            float[] normals = new float[normalNode.getNumNormals()];
            normalNode.getVector(normals);

            float[] newNormals = new float[data.vertexCount * 3];
            setupArray(normals, newNormals, vfNormalPerVertex);
            implGeom.setNormals(newNormals);
        } else {
            if(vfCcw)
                implGeom.setNormals(data.normals);
            else {
                // Bother, need to turn normals around
                int i = 1;
                while(i < data.normals.length / 3) {
                    data.normals[i] = -data.normals[i];
                    i += 3;
                }

                implGeom.setNormals(data.normals);
            }
        }

        if(vfColor != null || pColor != null) {
            VRMLColorNodeType color_node;

            if(pColor != null) {
                color_node = (VRMLColorNodeType)pColor.getImplementationNode();
            } else {
                color_node = vfColor;
            }

            int num_comps = color_node.getNumColorComponents();
            int num_cols = color_node.getNumColors();
            float[] colors = new float[num_cols];

            color_node.getColor(colors);

            float[] newColors = new float[data.vertexCount * num_comps];
            setupArray(colors, newColors, vfColorPerVertex);

            implGeom.setColors((num_comps == 4), newColors);
        }
    }

    /**
     * Setup the array to feed to VertexGeometry for colors and normals.  Uses
     * the data.geometryType to determine the correct format
     *
     * @param in The raw data
     * @parm out The data in the correct format
     * @param perVertex true if this is per-vertex values
     */
    private void setupArray(float[] in, float[] out, boolean perVertex) {

        int count = 0;
        int base_count = 0;
        int width_inc = vfXDimension * 3;

        int total_points = vfXDimension * (vfZDimension - 1);

        if(perVertex) {
            if(vfCcw) {
                for(int i = total_points; --i >= 0; ) {
                    out[count++] = in[base_count];
                    out[count++] = in[base_count + 1];
                    out[count++] = in[base_count + 2];

                    out[count++] = in[base_count + width_inc];
                    out[count++] = in[base_count + width_inc + 1];
                    out[count++] = in[base_count + width_inc + 2];

                    base_count += 3;
                }
            } else {
                for(int i = total_points; --i >= 0; ) {
                    out[count++] = in[base_count + width_inc];
                    out[count++] = in[base_count + width_inc + 1];
                    out[count++] = in[base_count + width_inc + 2];

                    out[count++] = in[base_count];
                    out[count++] = in[base_count + 1];
                    out[count++] = in[base_count + 2];
                    base_count += 3;
                }
            }
        } else {
            for(int i = total_points; --i >= 0; ) {
                out[count++] = in[base_count];
                out[count++] = in[base_count + 1];
                out[count++] = in[base_count + 2];
                out[count++] = in[base_count];
                out[count++] = in[base_count + 1];
                out[count++] = in[base_count + 2];

                if(i % 2 != 0)
                    base_count += 3;
            }
        }
    }

    /**
     * Common internal initialization.
     */
    private void init() {
        data = new GeometryData();
        //TODO : should initialize vfCcw
    }
}
