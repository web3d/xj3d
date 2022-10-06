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
// None

// Application specific imports
import net.jgeom.nurbs.NurbsSurface;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.j3d.aviatrix3d.IndexedQuadArray;
import org.j3d.aviatrix3d.VertexGeometry;

/**
 * An extension of GeometryArray when the points defining the vertices are
 * guaranteed to form a rectangular array in parametric space
 *
 *
 * @author Vincent Marchetti
 * @version 0.1
 */
public class RectangularMesh implements RectangularGeometryArray, AviatrixConnector {

    protected int useg, vseg;

    protected int udim, vdim;

    protected float[][] uvRange;

    protected Point3f[] euc_coords;

    protected Point2f[] par_coords;

    protected int[] indices;

    public RectangularMesh(NurbsSurface surface, int segU, int segV) {
        useg = segU;
        vseg = segV;
        udim = useg + 1;
        vdim = vseg + 1;

        /* set up the ranges
         It is assumed that the knot values follow the
         condition that the first p+1 values
         and the last (p+1) values lie on the boundary
         or outside the range of validity of the spline
         (and that the knot values are ordered)
         where p is the polynomial degree
         */
        uvRange = new float[2][2];
        float[] uKnots = surface.getUKnots();
        int up = surface.getUDegree();
        uvRange[0][0] = uKnots[up];
        uvRange[0][1] = uKnots[uKnots.length - 1 - up];

        float[] vKnots = surface.getVKnots();
        int vp = surface.getVDegree();
        uvRange[1][0] = vKnots[vp];
        uvRange[1][1] = vKnots[vKnots.length - 1 - vp];

        float udelt = (uvRange[0][1] - uvRange[0][0]) / useg;
        float vdelt = (uvRange[1][1] - uvRange[1][0]) / vseg;
        // initialize the par_coords
        par_coords = new Point2f[udim * vdim];
        euc_coords = new Point3f[udim * vdim];
        float u = 0.0f, v = 0.0f;
        for (int j = 0, k = 0; j < vdim; ++j) {
            if (j == 0) {
                v = uvRange[1][0];
            } else if (j == vseg) {
                v = uvRange[1][1];
            } else {
                v += vdelt;
            }
            for (int i = 0; i < udim; ++i, ++k) {
                if (i == 0) {
                    u = uvRange[0][0];
                } else if (i == useg) {
                    u = uvRange[0][1];
                } else {
                    u += udelt;
                }

                par_coords[k] = new Point2f(u, v);
                euc_coords[k] = surface.pointOnSurface(u, v);
            }
        }

        if (false) {
            System.out.println("RectangularMesh construction:");
            System.out.println(String.format(
                    "u range: %f to %f", uvRange[0][0], uvRange[0][1]));
            System.out.println(String.format(
                    "v range: %f to %f", uvRange[1][0], uvRange[1][1]));

            for (int i = 0; i < udim; ++i) {
                for (int j = 0; j < vdim; ++j) {
                    int k = indexInArray(i, j);

                    System.out.println(String.format(
                            "pt %d: (%d,%d) %12s %s",
                            k,
                            i, j,
                            par_coords[k].toString(),
                            euc_coords[k].toString()));
                }
            }
        }


        indices = new int[4 * useg * vseg];

        for (int i = 0, k = 0; i < useg; i++) {
            for (int j = 0; j < vseg; j++, k += 4) {
                int z = indexInArray(i, j);  // index of "lower left" corner
                indices[k] = z;
                indices[k + 1] = z + 1;         // lower right corner
                indices[k + 2] = z + 1 + udim; // upper right corner
                indices[k + 3] = z + udim;      //upperleft corner
            }
        }

        if (false) {
            System.out.println("indices: ");
            for (int k = 0; k < indices.length; k += 4) {
                System.out.println(String.format(
                        "k: %d  (%d %d %d %d)",
                        k,
                        indices[k],
                        indices[k + 1],
                        indices[k + 2],
                        indices[k + 3]));
            }
        }
    }

    /*
     implement RectangularGeometryArray methods
     */

    /**
     * Return the number of segments (intervals, steps) in parameter space      *
     * @return size 2 array {number of u segment, number of v segments}
     */
    @Override
    public int[] numSegments() {
        int retVal[] = {useg, vseg};
        return retVal;
    }

    /**
     * numDimensions[0] = numSegments[0] + 1 numDimensions[1] = numSegments[1] +
     * 1
     *
     * @return index to use for coordinates array
     */
    @Override
    public int[] numDimensions() {
        int retVal[] = {udim, vdim};
        return retVal;
    }

    /**
     * 0 &lt;= i &lt; numDimensions[0] 0 &lt;= j &lt; numDimensions[1]
     *
     * @return index to use for coordinates array
     */
    @Override
    public int indexInArray(int i, int j) {
        return j * (udim) + i;
    }

    /**
     * Return the range of parameters
     *
     * parameter_range[0] is a 2-array of min, max of u parameter
     * parameter_range[0][0] the minimum of u parameter_range[0][1] the maximum
     * of u
     *
     * parameter_range[1][0] the minimum of v parameter_range[1][1] the maximum
     * of v
     *
     * @return size 2 array {number of u segment, number of v segments}
     */
    @Override
    public float[][] parameter_range() {
        return uvRange;
    }

    /*
     implement GeometryArray methods
     */
    @Override
    public int numPoints() {
        return udim * vdim;
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
        qa.setIndices(indices, 4 * useg * vseg);

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