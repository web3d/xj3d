/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.IndexedTriangleArray;
import org.j3d.aviatrix3d.TriangleArray;
import org.j3d.aviatrix3d.TriangleFanArray;
import org.j3d.aviatrix3d.VertexGeometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.PointArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLCoordinateNodeType;
import org.web3d.vrml.nodes.VRMLColorNodeType;

import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;
import org.web3d.vrml.renderer.common.nodes.render.BasePointSet;

/**
 * OpenGL implementation of an PointSet.
 * <p>
 *
 * The point set directly maps to Aviatrix3D's PointArray class. When the
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
 * @author Justin Couch
 * @version $Revision: 1.13 $
 */
public class OGLPointSet extends BasePointSet
    implements OGLGeometryNodeType,
               NodeUpdateListener {

    /** The impl for this class */
    private PointArray implGeom;

    /** temp array to copy values from the color field to the geometry */
    private float[] tmpColors;

    /** Holder for the unlit line color. Only assigned when needed. */
    private float[] unlitColor;

    /** Flag indicating coordinates changed this last time */
    private boolean coordChanged;

    /** Flag to indicate the colors changed */
    private boolean colorChanged;

    /**
     * Construct a new point set instance that contains no child nodes.
     */
    public OGLPointSet() {
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
    public OGLPointSet(VRMLNodeType node) {
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
    // Methods defined by VRMLComponentGeometryNodeType
    //----------------------------------------------------------

    /**
     * Check to see if this geometry implementation type requires unlit color
     * values to be set. For the most part this will always return false, but
     * some will need it (points and lines). This value should be constant for
     * the geometry regardless of whether a Color component has been provided
     * or not. It is up to the implementation to decide when to pass these
     * values on to the underlying rendering structures or not.
     * <p>
     *
     * The default implementation returns false. Override if different
     * behaviour is needed.
     *
     * @return true if we need unlit colour information
     */
    @Override
    public boolean requiresUnlitColor() {
        return true;
    }

    /**
     * Set the local colour override for this geometry. Typically used to set
     * the emissiveColor from the Material node into the geometry for the line
     * and point-type geometries which are unlit in the X3D/VRML model.
     * <p>
     *
     * The default implementation does nothing. Override to do something useful.
     *
     * @param color The colour value to use
     */
    @Override
    public void setUnlitColor(float[] color) {
        changeFlags |= UNLIT_COLORS_CHANGED;

        if(unlitColor == null)
            unlitColor = new float[3];

        unlitColor[0] = color[0];
        unlitColor[1] = color[1];
        unlitColor[2] = color[2];

        if(implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);
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
                               
                implGeom.setVertices(
                        PointArray.COORDINATE_3, 
                        geomData.coordinates,
                        geomData.vertexCount);
                
            }
            
            coordChanged = false;
          
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
                implGeom.setSingleColor(false, unlitColor);
            } else {
                boolean alpha = (vfColor.getNumColorComponents() == 4);

                implGeom.setColors(alpha, geomData.colors);
            }
            
            colorChanged = false;
            
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
        
        if (implGeom == null) {
            implGeom = new PointArray(true, VertexGeometry.VBO_HINT_STATIC);
            newImpl = true;
        }
        
        if (newImpl) {
            coordChanged = true;
            colorChanged = true;
            
            updateCoordinateArray();
            updateColorArray();
            
            if (implGeom.isLive()) {
                implGeom.boundsChanged(this);
                implGeom.dataChanged(this);
            } else {
                updateNodeBoundsChanges(null);
                updateNodeDataChanges(null);
            }
            
        } else {
            
            if(((changeFlags & COORDS_CHANGED) != 0)) {
                updateCoordinateArray();
                
                coordChanged = true;
                if (implGeom.isLive())
                    implGeom.boundsChanged(this);
                else
                    updateNodeBoundsChanges(implGeom);

            }

            if(((changeFlags & COLORS_CHANGED) != 0)) {
                updateColorArray();
                
                colorChanged = true;
                if (implGeom.isLive())
                    implGeom.dataChanged(this);
                else
                    updateNodeDataChanges(implGeom);

            }
       
        }
        
        changeFlags = 0;
        
    }

    
    /**
     * Common initialization functionality.
     */
    private void init() {
        
        geomData = new GeometryData();

        coordChanged = false;
        colorChanged = false;

    }
}
