/*****************************************************************************
 *                        Web3d.org Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.nurbs.mesh;

// Standard library imports
// none

// External imports
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import net.jgeom.nurbs.evaluators.TrimSurfaceEvaluator;
import net.jgeom.nurbs.NurbsSurface;
import net.jgeom.nurbs.UVCoord2f;

import org.j3d.aviatrix3d.IndexedQuadArray;
import org.j3d.aviatrix3d.VertexGeometry;

/**
 *
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public class TrimmedNurbsMesh implements GeometryArray, AviatrixConnector {

    /*
     protected int useg, vseg;
     protected int udim, vdim;

     protected float[][] uvRange;
     */
    protected Point3f[] euc_coords;

    protected Point2f[] par_coords;

    protected int[] indices;

    public TrimmedNurbsMesh(NurbsSurface surface) {
        
        /*
         useg=segU;
         vseg=segV;
         udim = useg + 1;
         vdim = vseg + 1;
         */
        /* set up the ranges
         It is assumed that the knot values follow the
         condition that the first p+1 values
         and the last (p+1) values lie on the boundary
         or outside the range of validity of the spline
         (and that the knot values are ordered)
         where p is the polynomial degree
         */
        TrimSurfaceEvaluator evaluator = new TrimSurfaceEvaluator();
        evaluator.evaluateSurface(surface, 40, 40);

        int Np = evaluator.vertexList.size();
        euc_coords = new Point3f[Np];
        par_coords = new Point2f[Np];
        for (int i = 0; i < Np; ++i) {
            UVCoord2f p2 = evaluator.uvCoords.get(i);
            par_coords[i] = new Point2f(p2.x, p2.y);
            euc_coords[i] = evaluator.vertexList.get(i);
        }

        int Ni = evaluator.indexes.size();
        indices = new int[Ni];
        for (int i = 0; i < Ni; ++i) {
            indices[i] = evaluator.indexes.get(i);
        }

    }
    /*
     implement GeometryArray methods
     */

    @Override
    public int numPoints() {
        return euc_coords.length;
    }

    @Override
    public Point3f[] euclidean_coordinates() {
        return euc_coords;
    }

    @Override
    public Point2f[] parametric_coordinates() {
        return par_coords;
    }

    /*
     implement AviatrixConnector methods
     see  http://aviatrix3d.j3d.org/javadoc/org/j3d/aviatrix3d/IndexedQuadArray.html
     */
    @Override
    public VertexGeometry sceneGraphObject() {
        IndexedQuadArray qa = new IndexedQuadArray(true, IndexedQuadArray.VBO_HINT_STATIC);

        int np = numPoints();
        float[] coordinates = new float[3 * np];
        for (int i = 0, k = 0; i < np; i++, k += 3) {
            coordinates[k] = euc_coords[i].x;
            coordinates[k + 1] = euc_coords[i].y;
            coordinates[k + 2] = euc_coords[i].z;
        }
        qa.setVertices(IndexedQuadArray.COORDINATE_3, coordinates);
        qa.setIndices(indices, indices.length);

        float[] parc = new float[2 * np];
        for (int i = 0; i < par_coords.length; ++i) {
            parc[2 * i] = par_coords[i].x;
            parc[2 * i + 1] = par_coords[i].y;
        }
        int textureMaps[] = {0};

        int[] textureTypes = {IndexedQuadArray.TEXTURE_COORDINATE_2};

        float[][] textureCoordinateSets = new float[1][];
        textureCoordinateSets[0] = parc;

        qa.setTextureCoordinates(textureTypes, textureCoordinateSets);
        qa.setTextureSetMap(textureMaps);
        return qa;

    }
}