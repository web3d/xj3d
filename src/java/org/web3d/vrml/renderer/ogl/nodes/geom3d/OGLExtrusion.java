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

package org.web3d.vrml.renderer.ogl.nodes.geom3d;

// External imports
import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;

// Local import
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geom3d.BaseExtrusion;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;
import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

/**
 * OpenGL implGeomementation of an Extrusion
 * <p> The 1.16 version of OGLExtrusion generates
 * end caps correctly for both convex and concave
 * crossSections.  More thorough testing is needed
 * to see if textures are handled properly, however.
 *
 * @author Justin Couch, Andrzej Kapolka, Rick Goldberg
 * @version $Revision: 1.20 $
 */
public class OGLExtrusion extends BaseExtrusion
    implements OGLGeometryNodeType, NodeUpdateListener{

    /** Message when we detect a solid of revolution */
    private static final String SOR_ERR =
        "Invalid Extrusion data; looks like a solid of revolution";

    /** When the normalisation of the Y axis fails because it is zero length */
    private static final String Y_NORM_MSG =
        "Error normalizing Y in Extrusion";

    /** The OpenGL geometry implGeommentation
     * Previous implementation of OGLExtrusion used an IndexedTriangleStripArray;
     * more recently implGeom is a TriangleArray.        */
    private Geometry implGeom;

    /** The number of texture coordinate sets to be set by Shape Class */
    private int numTexCoordSets;
    
    /** The coord array defines the 3D vertices referenced by the coordIndex field. */
    private float[] coords;

    /** The coordIndex array specifies polygonal faces by indexing into coordinates
     * in the 'coords' array.  An index of -1 indicates that the current face has
     * ended and the next one begins.     */
    private int[] coordIndex;

    /** The Point3f[] version of vfSpine.
     * One point for every spine.      */
    private Point3f[] spines;

    /** The Vector3f[] version of vfScale. */
    private Vector3f[] scales;

    /** The AxisAngle4f[] version of vfOrientation. */
    private AxisAngle4f[] orientations;

    /** rotations will contain the per spine transform composed with
     * orientation after the call to calculateSCP(); */
    private Matrix3f[] rotations;
    private Matrix4f[] transforms;

    private boolean collinear;

    /** Flag to say normals have changed when updating the geometry */
    private boolean normalsChanged;

    /** Flag to say texture coords have changed when updating the geometry */
    private boolean texCoordsChanged;

    /** Flag to say colors have changed when updating the geometry */
    private boolean colorsChanged;

    /** Did the vbo state change */
    private boolean vboChanged;

    /** Flag indicating if the spine is closed as a surface of revolution */
    private boolean spineClosed;

    /** Flag to say if the crossSection is closed or not.
     * Fundamental enough that it may belong in the base class */
    boolean crossSectionClosed;

    /** Userdata kept in the triangle geometry */
    protected GeometryData geomData;

    /** Set to "numCrossSection-1" if(crossSectionClosed)
     * else equal to numCrossSection.  */
    int uniqueCrossSectionPoints;

    /**
     * Construct a default sphere instance
     */
    public OGLExtrusion() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLExtrusion(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
        ((VertexGeometry)implGeom).setVertices(  TriangleArray.COORDINATE_3,
                                                geomData.coordinates,
                                                geomData.vertexCount);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {

/*        if(colorsChanged) {
            int num_comp =
                (vfColor == null) ? 0 : vfColor.getNumColorComponents();
            boolean has_alpha = num_comp == 4;

            implGeom.setColors(has_alpha, geomData.colors);
            colorsChanged = false;

            if (vfColor == null) {
                localColors = false;
            }
        }

        if(normalsChanged) {*/
            ((VertexGeometry)implGeom).setNormals(geomData.normals);
            /*normalsChanged = false;
        }

        if(texCoordsChanged) {
            implGeom.setTextureCoordinates(texTypes,
                                           texCoords,
                                           numUniqueTexSets);
            implGeom.setTextureSetMap(texSetMap, numTexSets);

            texCoordsChanged = false;
        }

        if (vboChanged) {
            // Only ever change to false after activity
            implGeom.setVBOEnabled(false);

            vboChanged = false;
        }*/
    }

    //-------------------------------------------------------------
    // Methods used to set implGeom as TriangleArray
    //-------------------------------------------------------------

    /**
     * Build the implGeom, set the normals, etc.:
     * Basically let GeometryUtils do all the work.
     *
     * @author Eric Fickenscher
     */
    private void buildImplTriangleArray(){

        // convert vrml data to intermediate form
        initSetup();
        // calculate per spine SCP transforms
        // results in transforms[] being filled withs SCP info
        // complete with scale, translation and orientation from
        // fields
        if(!calculateSCP()) return;

        // transform the crossSections to coordinates
        createExtrusionCoordinates();

        // set coordIndex to create an IndexedFaceSet representing this extrusion
        createIndicesTriangleArray();
        //printIndices();

        GeometryUtils gutils = new GeometryUtils();
        GeometryHolder gholder = new GeometryHolder();

        gutils.generateTriangleArrays(  coords,
                                        null,   // float[] color
                                        null,   // float[] normal
                                        null,   // float[] texture
                                        1,
                                        true,   // genTexCoords,
                                        true,   // genNormals,
                                        coordIndex,
                                        coordIndex.length,
                                        null,   // int[] vfColorIndex,
                                        null,   // int[] vfNormalIndex,
                                        null,   // int[] vfTexCoordIndex,
                                                // TODO : are we obeying the ccw right-hand-
                                        true,   // rule or not?  this is far different
                                                // than the meaning of vfCCW!
                                        vfConvex,
                                        false,  // colorPerVertex,
                                        true,   // normalPerVertex,
                                        0,
                                        vfCreaseAngle,
                                        true,   // initialBuild,
                                        gholder);

        geomData = new GeometryData();
        //TODO : should initialize vfCCW   // TODO : check for explicit initialization of other fields as well
        geomData.geometryType = GeometryData.TRIANGLES;
        gutils.copyData(gholder, geomData);

        if (implGeom.isLive())
            implGeom.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGeom);

        if (implGeom.isLive())
            implGeom.dataChanged(this);
        else
            updateNodeDataChanges(implGeom);

        // Make an array of objects for the texture setting
        float[][] textures = gholder.textureCoordinates;
        int[] tex_type = { TriangleArray.TEXTURE_COORDINATE_2 };
        ((VertexGeometry)implGeom).setTextureCoordinates(tex_type, textures, 1);

        // Setup texture units
        int[] tex_maps = new int[numTexCoordSets];

        for(int i=0; i < numTexCoordSets; i++)
            tex_maps[i] = 0;

        ((VertexGeometry)implGeom).setTextureSetMap(tex_maps, numTexCoordSets);
    }

    /**
     * Result: Completed "coords" array: An array of all the float information
     * describing each vertex in the extrusion, created by applying the
     * transforms to the vfCrossSection points
     *
     * @author Eric Fickenscher
     */
    private void createExtrusionCoordinates(){

        // calculate the number of coordinates needed for the sides
        // of the extrusion: 3 coordinates per vertex, one vertex per
        // crossSectionPoint, and one set of crossSectionPoints per spinePoint
        coords = new float[ numSpine * uniqueCrossSectionPoints * 3 ];

        for(int i = 0; i < numSpine; i++) {

            Matrix4f tx = transforms[i];

            for(int j = 0; j < uniqueCrossSectionPoints; j++) {

                int ind = (i * uniqueCrossSectionPoints + j) * 3;

                // basically a transform, in place
                float c_x = vfCrossSection[j*2   ];
                float c_z = vfCrossSection[j*2 +1];

                float x = c_x * tx.m00 + c_z * tx.m02 + tx.m03;
                float y = c_x * tx.m10 + c_z * tx.m12 + tx.m13;
                float z = c_x * tx.m20 + c_z * tx.m22 + tx.m23;

                coords[ind] = x;
                coords[ind + 1] = y;
                coords[ind + 2] = z;
            }
        }
    }

    /**
     * Result: Completed "coordIndex" array: an int array representing an
     * IndexedFaceSet representation of the extrusion.
     *
     * @author Eric Fickenscher
     */
    private void createIndicesTriangleArray(){

        int sizeOfCoordIndex = 5*(numCrossSection-1) * (numSpine-1);
        if( vfBeginCap) sizeOfCoordIndex += uniqueCrossSectionPoints+1;
        if( vfEndCap)   sizeOfCoordIndex += uniqueCrossSectionPoints+1;

        coordIndex = new int[sizeOfCoordIndex];

        int indx = 0;
        int curIndex;

        // for each separate segment between two spine points
        for(int i = 0; i<numSpine-1; i++){

            curIndex = i*uniqueCrossSectionPoints;

            // build a quadrilateral for every crossSection-to-crossSection side around that segment
            // note that Xj3D wireframe mode shows triangulation even though quads are being built here
            for(int j = 0; j < numCrossSection-1; j++){

                if(vfCCW){
                    coordIndex[ indx++ ] = j + curIndex;
                    coordIndex[ indx++ ] = j + curIndex +1;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints +1;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints;
                } else {
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints;
                    coordIndex[ indx++ ] = j + curIndex + uniqueCrossSectionPoints +1;
                    coordIndex[ indx++ ] = j + curIndex +1;
                    coordIndex[ indx++ ] = j + curIndex;
                }

                coordIndex[ indx++ ] = -1;
            }

            if(crossSectionClosed){
                coordIndex[indx -4] -= uniqueCrossSectionPoints;
                coordIndex[indx -3] -= uniqueCrossSectionPoints;
            }
        }
        
        //if spineClosed, then index of last cross section can be changed into index of 1st cross section.
        if(spineClosed)
            for(int j = 0; j < uniqueCrossSectionPoints; j++)
            {
                coordIndex[indx - j * 5 - 3] -= (uniqueCrossSectionPoints * (numSpine-1));
                coordIndex[indx - j * 5 - 2] -= (uniqueCrossSectionPoints * (numSpine-1));
            }
                
        // note that Xj3D wireframe mode shows triangulation even though N-sided polygons are being built here
        if( vfBeginCap) {

            for(int i = 0; i < uniqueCrossSectionPoints; i++){
                if(vfCCW) coordIndex[ indx++ ] = uniqueCrossSectionPoints -i -1;
                else coordIndex[ indx++ ] = i;
            }
            coordIndex[ indx++ ] = -1;
        }
        if( vfEndCap) {

            for(int i = 0; i < uniqueCrossSectionPoints; i++){
                if(vfCCW) coordIndex[ indx++ ] = (numSpine-1)*uniqueCrossSectionPoints + i;
                else coordIndex[ indx++ ] = numSpine*uniqueCrossSectionPoints -i -1;
            }
            coordIndex[ indx ] = -1;
        }
    }

    //-------------------------------------------------------------
    // Methods defined by OGLGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
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

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    @Override
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value, numValid);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the j3d representation
     * only once per frame.
     */
    @Override
    public void allEventsComplete() {
        buildImplTriangleArray();
// TODO private trace variable
//        printDebugMessagesToConsole ();
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

        // if using a TriangleArray instead of an IndexedTriangleStripArray {
        implGeom = new TriangleArray(true, VertexGeometry.VBO_HINT_STATIC);
        //OGLUserData u_data = new OGLUserData();
        //u_data.geometryData = geomData;
        //implGeom.setUserData(u_data);

        buildImplTriangleArray();
// TODO private trace variable
//        printDebugMessagesToConsole ();

        //} else {
        //    implGeom = new IndexedTriangleStripArray(true, VertexGeometry.VBO_HINT_STATIC);
        //    buildImpl();
        //}
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Instantiate the various variables needed for processing.
     */
    private void initSetup() {

        numTexCoordSets = 0;
        normalsChanged = false;
        texCoordsChanged = false;
        colorsChanged = false;
        vboChanged = false;

        collinear = false;
        // spineClosed :   1st Spine == last Spine
        //              && 1st Scale == last Scale 
        spineClosed =  (vfSpine[0] == vfSpine[vfSpine.length-3] &&
                        vfSpine[1] == vfSpine[vfSpine.length-2] &&
                        vfSpine[2] == vfSpine[vfSpine.length-1] &&
                        vfScale[0] == vfScale[vfScale.length-2] &&
                        vfScale[1] == vfScale[vfScale.length-1]);

        uniqueCrossSectionPoints = numCrossSection;
        crossSectionClosed =
            (vfCrossSection[0] == vfCrossSection[vfCrossSection.length-2] &&
             vfCrossSection[1] == vfCrossSection[vfCrossSection.length-1]);
        if(crossSectionClosed) uniqueCrossSectionPoints--;

        if (numSpine != (vfSpine.length / 3)) {
            errorReporter.warningReport("A numSpine differs from (vfSpine.length/3) at initSetup() in OGLExtrusion.java", null);
        }
        
        // Convert the spine array into a Point3f array for
        // easier manipulation later
        spines = new Point3f[numSpine];
        for(int i = 0; i < spines.length; i++) {
            spines[i] = new Point3f(vfSpine[i * 3],
                                    vfSpine[i * 3 + 1],
                                    vfSpine[i * 3 + 2]);
        }

        // Convert the orientation points so they match specification
        //
        // Note: if the number of scale or orientation points is greater
        // than the number of spine points, the excess values are ignored.
        // If they contain one value, it is applied at all spine points.
        // (results are 'undefined' if the number of sets of scale or orientation
        // values is greater than one but less than the number of spine
        // points... in such a case, we repeat the final set of values for
        // the remainder of spine points)
        orientations = new AxisAngle4f[numSpine];
        for(int i = 0; i < orientations.length; i++) {
            if(i * 4 + 3 < vfOrientation.length)
                orientations[i] = new AxisAngle4f(
                    vfOrientation[i * 4],
                    vfOrientation[i * 4 + 1],
                    vfOrientation[i * 4 + 2],
                    vfOrientation[i * 4 + 3]
               );
            else
                orientations[i] = new AxisAngle4f(orientations[i-1]);
        }

        // Convert the scale points so they match specification
        //
        // Note that scales are really just 2D scalars,
        // but this version uses a 3f for some reason,
        // leaving the "y" value as 1.
        scales = new Vector3f[numSpine];
        for(int i = 0; i < scales.length; i++) {
            if(i * 2 + 1 < vfScale.length)
                scales[i] = new Vector3f(vfScale[i * 2],
                                         1,
                                         vfScale[i * 2 + 1]);
            else  // if vfScale.length is short, copy previous scale.
                scales[i] = new Vector3f(scales[i-1]); 
        }

        rotations = new Matrix3f[vfSpine.length / 3];

        // if entirely collinear
        Vector3d v2 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v0 = new Vector3d();
        double d = 0;
        for(int i = 1; i < spines.length - 1; i++) {
            v2.set(spines[i+1]);
            v1.set(spines[i]);
            v0.set(spines[i - 1]);
            v2.sub(v1);
            v1.sub(v0);
            v0.cross(v2,v1);
            d += v0.dot(v0);
        }

        collinear = (d == 0);
    }

    /**
     * Create the spine information and verify that this is a valid object.
     *
     * @return true if everything is valid, false otherwise
     */
    private boolean calculateSCP() {
        // find an orthonormal basis and construct rotation matrix
        // for each spine. handle special cases in second pass
        Vector3f u, v;

        int last = numSpine - 1;
        Vector3f[] x, y, z;

        x = new Vector3f[numSpine];
        y = new Vector3f[numSpine];
        z = new Vector3f[numSpine];

        if (collinear) {
            if (spineClosed) {
                errorReporter.warningReport(SOR_ERR, null);
                StringBuilder buf = new StringBuilder("Spine data:");

                for (Point3f spine : spines) {
                    buf.append(spine);
                    buf.append(' ');
                }

                errorReporter.messageReport(buf.toString());

                return false;
            }

            // Direction is the first spine point that does not equal to
            // spines[0]
            Vector3f direction = null;
            for (Point3f spine : spines) {
                if (!spines[0].equals(spine)) {
                    direction = new Vector3f(spine);
                }
            }

            y[0] = new Vector3f();
            if (direction != null) {
                y[0].sub(direction, spines[0]);
// fixed NPE C:/x3d-code/www.web3d.org/x3d/content/examples/X3dForWebAuthors/KelpForestExhibit/CircleFishPrototype.wrl
// https://docs.oracle.com/cd/E17802_01/j2se/javase/technologies/desktop/java3d/forDevelopers/j3dapi/javax/vecmath/Tuple3d.html
            } else {
                errorReporter.warningReport("Direction is null at calculateSCP() in OGLExtrusion.java", null);
            }

            if (!norm(y[0])) {
                errorReporter.warningReport(Y_NORM_MSG, null);
            }

            // Create an initial x[0]
            if (y[0].x == 1) {
                x[0] = new Vector3f(0, -1, 0);
            } else if (y[0].x == -1) {
                x[0] = new Vector3f(0, 1, 0);
            } else {
                x[0] = new Vector3f(1, 0, 0);
            }
            // Create z[0]
            z[0] = new Vector3f();
            z[0].cross(x[0], y[0]);

            // Create final x[0]
            x[0].cross(y[0], z[0]);
            for (int i = 1; i < spines.length; i++) {
                // redo, this should take the direction of y
                // redone by Pasi Paasiala < < check this >  >
                x[i] = new Vector3f(x[0]);
                y[i] = new Vector3f(y[0]);
                z[i] = new Vector3f(z[0]);
            }
        } else { // "collinear" is false

            // find y[i] for all but first and last
            // most times the exception cases are bad data and hopefully
            // wont happen. It is free to try catch you later, so hopes
            // 99% cases will be one if faster by not checking the if
            for (int i = 1; i < last; i++) {
                y[i] = new Vector3f();
                y[i].sub(spines[i + 1], spines[i - 1]);
                if (!norm(y[i])) {
                    // spines[i+1] equals spines[i - 1]
                    y[i].sub(spines[i + 1], spines[i]);
                    if (!norm(y[i])) {
                        // spines[i+1] equaled spines[i]
                        y[i].sub(spines[i], spines[i - 1]);
                        if (!norm(y[i])) {
                            // spines[i] equaled spines[i - 1]
                            // real bad case, do something
                            int w = i + 2;
                            while ((w < last + 1) && (spines[i - 1].equals(spines[w]))) {
                                w++;
                            }
                            if (w < last + 1) {
                                y[i].sub(spines[w], spines[i - 1]);
                                norm(y[i]); // should never divide by zero here
                            } else { // worst worst case
                                y[i] = new Vector3f(0, 1, 0);
                            }
                        }
                    }
                }
            } // find y[i] in the case of non-collinear, 

            // y for ends
            if (spineClosed) {
                // spineClosed and not collinear - >  not all one point
                y[0] = new Vector3f();
                y[0].sub(spines[1], spines[last - 1]);
                if (!norm(y[0])) {
                    // bad case that the spine[n-2] == spine[1]
                    int w = last - 2;
                    while ((w > 1) && (spines[1].equals(spines[w]))) {
                        w--;
                    }
                    if (w > 1) {
                        y[0].sub(spines[1], spines[w]);
                        norm(y[0]); // should never divide by zero here
                    } else // how did this happen?
                    {
                        y[0].set(0, 0, 1);
                    }
                }
                y[last] = new Vector3f(y[0]);
            } else {
                y[0] = new Vector3f();
                y[last] = new Vector3f();
                y[0].sub(spines[1], spines[0]);
                if (!norm(y[0])) {
                    int w = 2;
                    while ((w < last) && (spines[0].equals(spines[w]))) {
                        w++;
                    }
                    if (w < last) {
                        y[0].sub(spines[w], spines[0]);
                        norm(y[0]); // should not divide by zero here
                    } else {
                        y[0].set(0, 0, 1);
                    }
                }
                y[last] = new Vector3f();
                y[last].sub(spines[last], spines[last - 1]);

                if (!norm(y[last])) {
                    int w = last - 2;
                    while ((w > -1) && (spines[last].equals(spines[w]))) {
                        w--;
                    }
                    if (w > -1) {
                        y[last].sub(spines[last], spines[w]);
                        norm(y[last]);
                    } else {
                        y[last].set(0, 0, 1);
                    }
                }
            }

            // now z axis for each spine
            // first all except first and last
            boolean recheck = false;
            for (int i = 1; i < last; i++) {
                u = new Vector3f();
                v = new Vector3f();
                z[i] = new Vector3f();
                u.sub(spines[i - 1], spines[i]);
                v.sub(spines[i + 1], spines[i]);
                // spec seems backwards on u and v
                // shouldn't it be z[i].cross(u,v)???
                //z[i].cross(v,u);
                //-- >  z[i].cross(u,v); is correct < < check this >  >
                // Modified by Pasi Paasiala (Pasi.Paasiala@solibri.com)
                // skwon : Modified by Sungmin Kwon (lucidaim@gmail.com)
                //-- >  z[i].cross(v,u) is correct by right hand rule.
                z[i].cross(v, u);
                if (!norm(z[i])) {
                    recheck = true;
                }
            }
            if (spineClosed) {
                z[0] = z[last] = new Vector3f();
                u = new Vector3f();
                v = new Vector3f();
                u.sub(spines[last - 1], spines[0]);
                v.sub(spines[1], spines[0]);
                try {
                    // skwon : Modified by Sungmin Kwon (lucidaim@gmail.com)
                    // z[0].cross(v, u) is correct by right hand rule.
                    z[0].cross(v, u);
                    // skwon : z[0] should be normalized.
                    norm(z[0]);
                } catch (ArithmeticException ae) {
                    recheck = true;
                }
            } else { // not spineClosed
                z[0] = new Vector3f(z[1]);
                z[last] = new Vector3f(z[last - 1]);
            }

            if (recheck) { // found adjacent collinear spines
                // first z has no length ?
                if (z[0].dot(z[0]) == 0) {
                    for (int i = 1; i < spines.length; i++) {
                        if (z[i].dot(z[i]) > 0) {
                            z[0] = new Vector3f(z[i]);
                        }
                    }
                    // test again could be most degenerate of cases
                    if (z[0].dot(z[0]) == 0) {
                        z[0] = new Vector3f(0, 0, 1);
                    }
                }

                // check rest of z's
                for (int i = 1; i < last + 1; i++) {
                    if (z[i].dot(z[i]) == 0) {
                        z[i] = new Vector3f(z[i - 1]);
                    }
                }
            }

            // finally, do a neighbor comparison
            // and evaluate the x's
            for (int i = 0; i < spines.length; i++) {
                if ((i > 0) && (z[i].dot(z[i - 1]) < 0)) {
                    z[i].negate();
                }

                // at this point, y and z should be nice
                x[i] = new Vector3f();

                //Original was: x[i].cross(z[i],y[i]); < < check this >  >
                //but it doesn't result in right handed coordinates
                // Modified by Pasi Paasiala
                x[i].cross(y[i], z[i]);
                norm(x[i]);
            }
        }

        // should now have orthonormal vectors for each
        // spine. create the rotation matrix with scale for
        // each spine. spec is unclear whether a twist imparted
        // at one of the spines is inherited by its "children"
        // so assume not.
        // also, the order looks like SxTxRscpxRo , ie ,
        // the spec doc looks suspect, double check
        Matrix3f m = new Matrix3f();
        transforms = new Matrix4f[spines.length];
        for (int i = 0; i < spines.length; i++) {
            rotations[i] = new Matrix3f();
            // Original had setRow. This is correct < < check this >  >
            // Modified by Pasi Paasiala
            rotations[i].setColumn(0, x[i]);
            rotations[i].setColumn(1, y[i]);
            rotations[i].setColumn(2, z[i]);
        }

        // skwon : I am not sure a purpose of correctionRotations[i] matrix
        //         No correctionRotation brings same result with other viewers.
        //Matrix3f[] correctionRotations = createCorrectionRotations(z);
        Vector3f tmp = new Vector3f();
        // Create the transforms
        for (int i = 0; i < spines.length; i++) {
            // skwon : I am not sure a purpose of correctionRotations[i] matrix
            //         No correctionRotation brings same result with other viewers.
            //rotations[i].mul(correctionRotations[i]);
            m.set(orientations[i]);
            rotations[i].mul(m);
            transforms[i] = new Matrix4f();
            transforms[i].setIdentity();
            
            // skwon : Modified by Sungmin Kwon (lucidaim@gmail.com)
            //-->matrix m is contaminated when used for an orientation.
            //   m must be reset before being used for scaling.
            //   otherwise, the rest entries from rotation will bother you.
            m.setZero();;
            m.m00 = scales[i].x;
            m.m11 = scales[i].y; // should always be '1'
            m.m22 = scales[i].z;
            
            // skwon : Modified by Sungmin Kwon (lucidaim@gmail.com)
            //--> ((ROTATION)(ORIENTATION))(SCALE) is correct
            //    instead of old code : (SCALE)((ROTATION)(ORIENTATION))
            // [[ old code ]]
            //m.mul(rotations[i]);
            //transforms[i].setRotationScale(m);
            rotations[i].mul(m);
            transforms[i].setRotationScale(rotations[i]);

            tmp.set(spines[i]);
            transforms[i].setTranslation(tmp);
        }

        return true;
    }

    /**
     * Creates a rotation for each spine point to avoid twisting of the profile
     * when the orientation of SCP changes.
     * This method appears to be a prior bugfix work around that is no longer required. 
     * @author Pasi Paasiala
     * @param z the vector containing the z unit vectors for each spine point
     */
    @Deprecated 
    private Matrix3f[] createCorrectionRotations(Vector3f[] z) {

        Matrix3f[] correctionRotations = new Matrix3f[spines.length];
        correctionRotations[0] = new Matrix3f();
        correctionRotations[0].setIdentity();
        AxisAngle4f checkAngle = new AxisAngle4f();

        // testPoint is used to find the angle that gives the smallest distance
        // between the previous and current rotation. Find a point that is not
        // in the origin.
        Point3f testPoint = new Point3f(vfCrossSection[0], 0, vfCrossSection[1]);

        for(int i = 0; i < numCrossSection; i++) {
            if(vfCrossSection[i*2] != 0 || vfCrossSection[i*2 +1] != 0) {
                testPoint = new Point3f(vfCrossSection[i*2], 0, vfCrossSection[i*2 +1]);
                break;
            }
        }

        // Fix the orientations by using the angle between previous z and current z
        for(int i = 1; i < spines.length; i++) {
            float angle = z[i].angle(z[i - 1]);
            correctionRotations[i] = correctionRotations[i - 1];
            if(angle != 0) {
                correctionRotations[i] = new Matrix3f(correctionRotations[i - 1]);
                Point3f previous = new Point3f();
                //Point3f previous = testPoint;
                // Test with negative angle:
                Matrix3f previousRotation = new Matrix3f(rotations[i - 1]);
                previousRotation.mul(correctionRotations[i - 1]);
                previousRotation.transform(testPoint, previous);
                Matrix3f delta = new Matrix3f();
                delta.setIdentity();
                delta.rotY(-angle);
                correctionRotations[i].mul(delta);

                Matrix3f negativeRotation = new Matrix3f(rotations[i]);
                negativeRotation.mul(correctionRotations[i]);

                Point3f pointNegative = new Point3f();
                negativeRotation.transform(testPoint,pointNegative);

                float distNegative = pointNegative.distance(previous);

                // Test with positive angle
                delta.rotY(angle*2);
                correctionRotations[i].mul(delta);
                Matrix3f positiveRotation = new Matrix3f(rotations[i]);
                positiveRotation.mul(correctionRotations[i]);
                Point3f pointPositive = new Point3f();
                positiveRotation.transform(pointPositive);
                float distPositive = pointPositive.distance(previous);

                if(distPositive > distNegative) {
                    // Reset correctionRotations to negative angle
                    delta.rotY(-angle*2);
                    correctionRotations[i].mul(delta);
                }

                // Check that the angle is not more than PI.
                // If it is subtract PI from angle
                checkAngle.set(correctionRotations[i]);
                if(((float)Math.PI - checkAngle.angle) < 0.001) {
                    correctionRotations[i].rotY((float)(checkAngle.angle - Math.PI));
                }
            }
        }

        return correctionRotations;
    }

    /**
     * Variant on the traditional normalisation process. If the length is zero
     * we've had something go wrong so should let the user know, courtesy of
     * the return value.
     *
     * @param n The vector to normalise
     * @return false when the vector is zero length
     */
    private boolean norm(Vector3f n) {

        float norml = n.x*n.x + n.y*n.y + n.z*n.z;

        if(norml == 0)
            return false;

        norml = 1 / (float)Math.sqrt(norml);

        n.x *= norml;
        n.y *= norml;
        n.z *= norml;

        return true;
    }

    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printCrossSectionPoints(){
        System.out.println("printCrossSectionPoints: ");
        if (vfCrossSection.length == 0)
           System.out.println("none");
        for(int i = 0; i < vfCrossSection.length; i++){
//            if(vfCrossSection[i] >= 0)
//                System.out.print(" ");
            System.out.print("[" + i/2 + "] (" + vfCrossSection[i++]);
            System.out.print(", " + vfCrossSection[i] + "), ");
            if(((i+1)/2)%5 == 0)
                 System.out.println(); // skip line after every 5 pairs
            else System.out.print("\t");
        }
        if (crossSectionClosed)
             System.out.println("\ncrossSection is closed");
        else System.out.println("\ncrossSection is open");
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }
    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printCoords(){
        System.out.println("printCoords: ");
        if (coords.length == 0)
             System.out.println("none");
        else System.out.print  ("[0] ");
        for(int i = 0; i < coords.length; i++){
            System.out.print(coords[i]);
//            if(i%(uniqueCrossSectionPoints*3) == (uniqueCrossSectionPoints*3-1))
//                 System.out.println();
//            else if(i%3 == 2)
//                 System.out.print("\t");
//            else System.out.print(",\t");
//            if(i%3 == 0) i++;
            if      (((i+1)%(3*numCrossSection) == 0) && (i < coords.length - 1))
                 System.out.print(",\n\n" + "[" + (i+1)/3 + "] "); // skip 2 lines every numCrossSection coordinates, except at end
            else if (((i+1)%3  == 0) && (i < coords.length - 1))   // 3 coordinates per line
                 System.out.print(",\n"   + "[" + (i+1)/3 + "] "); // skip 1 line, except at end
            else System.out.print(",\t");
        }
        System.out.println("\n=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }
    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printIndices(){
        System.out.println("printIndices: ");
        if (coordIndex.length == 0)
           System.out.println("none");
        for(int i = 0; i < coordIndex.length; i++){
            if(coordIndex[i] == -1)
                 System.out.println(coordIndex[i] + ",");
            else System.out.print(  coordIndex[i] + ",\t");
        }
        System.out.println("\n=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }

    /**
     * Just a helper method that may prove useful for debugging purposes.
     */
    private void printSpines(){
        System.out.println("printSpines: ");
        if (spines.length == 0)
           System.out.println("none");
        for(int i = 0; i < spines.length; i++){
            System.out.print("[" + i + "] " + spines[i]);
            if ((i+1)%10 == 0)
                 System.out.print(",\n\n"); // skip 2 lines
            else if ((i+1)%5  == 0)
                 System.out.print(",\n"  ); // skip 1 line
            else System.out.print(",\t");
        }
        if (spineClosed)
             System.out.println("\nspine is closed");
        else System.out.println("\nspine is open");
        System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
    }

    /**
     * Debug diagnostics
     */
    @SuppressWarnings("DeadBranch")
    private void printDebugMessagesToConsole ()
    {
        // debug
        System.out.println("\n=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~");
        if (false) // compile unused invocations to avoid IDE warnings
        {
            printCrossSectionPoints();
            printSpines();
            printIndices();
            printCoords();
        }
        printCrossSectionPoints();
        printSpines();
        printIndices();
        printCoords();
    }
}
