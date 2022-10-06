/*****************************************************************************
 *                        Yumetech Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util.spatial;

// External Imports

// Internal Imports

/**
 * A triangle object.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class Triangle {

    /** The coordinates */
    public float[] coords;

    /** The ID used for uniqueness */
    public int id;

    /**
     * Constructor.
     *
     * @param coords The 9 coords making up the triangle
     * @param id
     */
    public Triangle(float[] coords, int id) {
        this.coords = new float[9];
        this.id = id;

        // Profiled this, arraycopy slower
        this.coords[0] = coords[0];
        this.coords[1] = coords[1];
        this.coords[2] = coords[2];
        this.coords[3] = coords[3];
        this.coords[4] = coords[4];
        this.coords[5] = coords[5];
        this.coords[6] = coords[6];
        this.coords[7] = coords[7];
        this.coords[8] = coords[8];
    }

    /**
     * Get the area of this triangle.  Uses Heron's Formula
     *
     * @return The area
     */
    public float getArea() {
        double diff1,diff2,diff3;

        diff1 = coords[3] - coords[0];
        diff2 = coords[4] - coords[1];
        diff3 = coords[5] - coords[2];

        double s1 = Math.sqrt(diff1 * diff1 +  diff2 * diff2 + diff3 * diff3);

        diff1 = coords[6] - coords[3];
        diff2 = coords[7] - coords[4];
        diff3 = coords[8] - coords[5];

        double s2 = Math.sqrt(diff1 * diff1 +  diff2 * diff2 + diff3 * diff3);

        diff1 = coords[0] - coords[6];
        diff2 = coords[1] - coords[7];
        diff3 = coords[2] - coords[8];

        double s3 = Math.sqrt(diff1 * diff1 +  diff2 * diff2 + diff3 * diff3);

        double hp = (s1 + s2 + s3) / 2.0;

        return(float) Math.sqrt(hp * (hp - s1) * (hp - s2) * (hp - s3));
    }

    /**
     * Get the largest side.
     * @return
     */
    public double getLargestSide() {
        float[] v0 = new float[3];
        float[] v1 = new float[3];
        float[] v2 = new float[3];

        Side[] sides = new Side[3];

        // Sort coords from longest to shortest sides
        double diffx = (double) (coords[3] - coords[0]);
        double diffy = (double) (coords[4] - coords[1]);
        double diffz = (double) (coords[5] - coords[2]);

        double s1_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[0] = new Side(0,s1_length_sq);

        diffx = (double) (coords[6] - coords[3]);
        diffy = (double) (coords[7] - coords[4]);
        diffz = (double) (coords[8] - coords[5]);

        double s2_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[1] = new Side(1,s2_length_sq);

        diffx = (double) (coords[6] - coords[0]);
        diffy = (double) (coords[7] - coords[1]);
        diffz = (double) (coords[8] - coords[2]);

        double s3_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[2] = new Side(2,s3_length_sq);

        java.util.Arrays.sort(sides);

        return Math.sqrt(sides[0].len);
    }

    /**
     * Calc the ratio between the smallest side to largest.
     * @return
     */
    public double getSideRatio() {
        float[] v0 = new float[3];
        float[] v1 = new float[3];
        float[] v2 = new float[3];

        Side[] sides = new Side[3];

        // Sort coords from longest to shortest sides
        double diffx = (double) (coords[3] - coords[0]);
        double diffy = (double) (coords[4] - coords[1]);
        double diffz = (double) (coords[5] - coords[2]);

        double s1_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[0] = new Side(0,s1_length_sq);

        diffx = (double) (coords[6] - coords[3]);
        diffy = (double) (coords[7] - coords[4]);
        diffz = (double) (coords[8] - coords[5]);

        double s2_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[1] = new Side(1,s2_length_sq);

        diffx = (double) (coords[6] - coords[0]);
        diffy = (double) (coords[7] - coords[1]);
        diffz = (double) (coords[8] - coords[2]);

        double s3_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[2] = new Side(2,s3_length_sq);

        java.util.Arrays.sort(sides);

/*
        for(int i=0; i < 3; i++) {
            System.out.println("side: " + sides[i].num + " len: " + sides[i].len);
        }
*/
        return sides[0].len / sides[2].len;
    }

    /**
     * Split a triangle into pieces.  Uses the mid-point of two longest edges
     * to create 3 triangles.
     *
     * @param nextID The next triangle ID to insure uniqueness of new id's.  Increment total
     * by 2 as first will be reused.
     * @return
     */
    public Triangle[] splitTriangle(int nextID) {
        float[] v0 = new float[3];
        float[] v1 = new float[3];
        float[] v2 = new float[3];

        Side[] sides = new Side[3];

        // Sort coords from longest to shortest sides
        double diffx = (double) (coords[3] - coords[0]);
        double diffy = (double) (coords[4] - coords[1]);
        double diffz = (double) (coords[5] - coords[2]);

        double s1_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[0] = new Side(0,s1_length_sq);  // v1 - v0

        diffx = (double) (coords[6] - coords[3]);
        diffy = (double) (coords[7] - coords[4]);
        diffz = (double) (coords[8] - coords[5]);

        double s2_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[1] = new Side(1,s2_length_sq);  // v2 - v1

        diffx = (double) (coords[6] - coords[0]);
        diffy = (double) (coords[7] - coords[1]);
        diffz = (double) (coords[8] - coords[2]);

        double s3_length_sq = diffx * diffx + diffy * diffy + diffz * diffz;
        sides[2] = new Side(2,s3_length_sq);  // v2 - v0

        java.util.Arrays.sort(sides);

        for(int i=0; i < 3; i++) {
            System.out.println("side: " + sides[i].num + " len: " + sides[i].len);
        }

        v0[0] = coords[sides[0].num * 3 + 0];
        v0[1] = coords[sides[0].num * 3 + 1];
        v0[2] = coords[sides[0].num * 3 + 2];

        v1[0] = coords[sides[1].num * 3 + 0];
        v1[1] = coords[sides[1].num * 3 + 1];
        v1[2] = coords[sides[1].num * 3 + 2];

        v2[0] = coords[sides[2].num * 3 + 0];
        v2[1] = coords[sides[2].num * 3 + 1];
        v2[2] = coords[sides[2].num * 3 + 2];

        float[] s1half = new float[3];
        float[] s2half = new float[3];

        s1half[0] = (float) (v0[0] + (((double)v2[0] - v0[0]) / 2.0));
        s1half[1] = (float) (v0[1] + (((double)v2[1] - v0[1]) / 2.0));
        s1half[2] = (float) (v0[2] + (((double)v2[2] - v0[2]) / 2.0));

        s2half[0] = (float) (v1[0] + (((double)v2[0] - v1[0])) / 2.0);
        s2half[1] = (float) (v1[1] + (((double)v2[1] - v1[1])) / 2.0);
        s2half[2] = (float) (v1[2] + (((double)v2[2] - v1[2])) / 2.0);

        float[] new_coords = new float[9];
        Triangle[] ret_val = new Triangle[3];

        outputTriangle(coords);

//System.out.println("Original coords: " + java.util.Arrays.toString(coords));
        // tri 1 = 2, s2/2, s1/2
        new_coords[0] = v2[0];
        new_coords[1] = v2[1];
        new_coords[2] = v2[2];
        new_coords[3] = s2half[0];
        new_coords[4] = s2half[1];
        new_coords[5] = s2half[2];
        new_coords[6] = s1half[0];
        new_coords[7] = s1half[1];
        new_coords[8] = s1half[2];
//System.out.println("Tri 1: " + java.util.Arrays.toString(new_coords));
        outputTriangle(new_coords);

        ret_val[0] = new Triangle(new_coords, id);
//System.out.println("  Tri 1  area: " + ret_val[0].getArea());

        // tri 2 = s1/2, s2/2, 0
        new_coords[0] = s1half[0];
        new_coords[1] = s1half[1];
        new_coords[2] = s1half[2];
        new_coords[3] = s2half[0];
        new_coords[4] = s2half[1];
        new_coords[5] = s2half[2];
        new_coords[3] = v0[0];
        new_coords[4] = v0[1];
        new_coords[5] = v0[2];
        outputTriangle(new_coords);

        ret_val[1] = new Triangle(new_coords, nextID++);
//System.out.println("Tri 2: " + java.util.Arrays.toString(new_coords));
//System.out.println("  Tri 2  area: " + ret_val[1].getArea());

        // tri 3 = 0, s2/2, 1
        new_coords[0] = v0[0];
        new_coords[1] = v0[1];
        new_coords[2] = v0[2];
        new_coords[3] = s2half[0];
        new_coords[4] = s2half[1];
        new_coords[5] = s2half[2];
        new_coords[6] = v1[0];
        new_coords[7] = v1[1];
        new_coords[8] = v1[2];
        outputTriangle(new_coords);

        ret_val[2] = new Triangle(new_coords, nextID++);
//System.out.println("tri3: " + java.util.Arrays.toString(new_coords));
//System.out.println("  Tri 3  area: " + ret_val[2].getArea());

        return ret_val;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Triangle))
            return false;
        else
            return equals((Triangle)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Triangle ta) {
        return (ta.id == this.id);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException {

        Triangle geom = (Triangle)o;
        return compareTo(geom);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Triangle ta) {

        if(ta == null)
            return 1;

        if(ta == this) {
            return 0;
        }

        return (this.id < ta.id ? -1 : 1);
    }

    /**
     * Calculate a bounding box for the triangle.
     *
     * @param min The array to fill in the min bounds
     * @param max The array to fill in the max bounds
     */
    public void calcBounds(float[] min, float[] max) {
        min[0] = Float.POSITIVE_INFINITY;
        min[1] = Float.POSITIVE_INFINITY;
        min[2] = Float.POSITIVE_INFINITY;

        max[0] = Float.NEGATIVE_INFINITY;
        max[1] = Float.NEGATIVE_INFINITY;
        max[2] = Float.NEGATIVE_INFINITY;

        if (coords[0] < min[0]) {
            min[0] = coords[0];
        }

        if (coords[1] < min[1]) {
            min[1] = coords[1];
        }

        if (coords[2] < min[2]) {
            min[2] = coords[2];
        }

        if (coords[0] > max[0]) {
            max[0] = coords[0];
        }

        if (coords[1] > max[1]) {
            max[1] = coords[1];
        }

        if (coords[2] > max[2]) {
            max[2] = coords[2];
        }

        if (coords[3] < min[0]) {
            min[0] = coords[3];
        }

        if (coords[4] < min[1]) {
            min[1] = coords[4];
        }

        if (coords[5] < min[2]) {
            min[2] = coords[5];
        }

        if (coords[3] > max[0]) {
            max[0] = coords[3];
        }

        if (coords[4] > max[1]) {
            max[1] = coords[4];
        }

        if (coords[5] > max[2]) {
            max[2] = coords[5];
        }

        if (coords[6] < min[0]) {
            min[0] = coords[6];
        }

        if (coords[7] < min[1]) {
            min[1] = coords[7];
        }

        if (coords[8] < min[2]) {
            min[2] = coords[8];
        }

        if (coords[6] > max[0]) {
            max[0] = coords[6];
        }

        if (coords[7] > max[1]) {
            max[1] = coords[7];
        }

        if (coords[8] > max[2]) {
            max[2] = coords[8];
        }
    }

    /**
     * Calculate a bounding box for the triangle.
     *
     * @param min The array to fill in the min bounds
     * @param max The array to fill in the max bounds
     */
    public void calcBounds(double[] min, double[] max) {
        min[0] = Double.POSITIVE_INFINITY;
        min[1] = Double.POSITIVE_INFINITY;
        min[2] = Double.POSITIVE_INFINITY;

        max[0] = Double.NEGATIVE_INFINITY;
        max[1] = Double.NEGATIVE_INFINITY;
        max[2] = Double.NEGATIVE_INFINITY;

        if (coords[0] < min[0]) {
            min[0] = coords[0];
        }

        if (coords[1] < min[1]) {
            min[1] = coords[1];
        }

        if (coords[2] < min[2]) {
            min[2] = coords[2];
        }

        if (coords[0] > max[0]) {
            max[0] = coords[0];
        }

        if (coords[1] > max[1]) {
            max[1] = coords[1];
        }

        if (coords[2] > max[2]) {
            max[2] = coords[2];
        }

        if (coords[3] < min[0]) {
            min[0] = coords[3];
        }

        if (coords[4] < min[1]) {
            min[1] = coords[4];
        }

        if (coords[5] < min[2]) {
            min[2] = coords[5];
        }

        if (coords[3] > max[0]) {
            max[0] = coords[3];
        }

        if (coords[4] > max[1]) {
            max[1] = coords[4];
        }

        if (coords[5] > max[2]) {
            max[2] = coords[5];
        }

        if (coords[6] < min[0]) {
            min[0] = coords[6];
        }

        if (coords[7] < min[1]) {
            min[1] = coords[7];
        }

        if (coords[8] < min[2]) {
            min[2] = coords[8];
        }

        if (coords[6] > max[0]) {
            max[0] = coords[6];
        }

        if (coords[7] > max[1]) {
            max[1] = coords[7];
        }

        if (coords[8] > max[2]) {
            max[2] = coords[8];
        }
    }

    private void outputTriangle(float[] coords) {
        System.out.println("Shape { geometry IndexedTriangleSet { solid FALSE ");
        System.out.print("   index [0 1 2] ");
        System.out.println("coord Coordinate { point ");
        System.out.println(java.util.Arrays.toString(coords));
        System.out.println("}}}");
    }
}

class Side implements Comparable {
    public int num;
    public double len;

    Side(int num, double len) {
        this.num = num;
        this.len = len;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Side))
            return false;
        else
            return equals((Side)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Side ta) {
        return (ta.len == this.len);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The object to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException {

        Side geom = (Side)o;
        return compareTo(geom);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Side ta) {

        if(ta == null)
            return 1;

        if(ta == this) {
            return 0;
        }

        return (this.len < ta.len ? 1 : -1);
    }

}
