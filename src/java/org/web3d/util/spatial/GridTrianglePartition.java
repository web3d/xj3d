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
import java.util.*;
import java.text.NumberFormat;

//import toxi.geom.Vec3D;
import toxi.math.MathUtils;

// Internal Imports
import org.web3d.util.IntHashSet;
import java.math.*;

/**
 * A spatial structure using a grid pattern and triangle objects.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class GridTrianglePartition implements SpatialPartition {

    /** A typical factor of exterior voxels in a grid */
    private static final float EXTERIOR_FACTOR = 0.0008f;

    /** Maximum ratio to detect long thin triangles */
//    private static final float TRIANGLE_AREA_MIN = 0.000001f;
    private static final float TRIANGLE_RATIO_MAX = 16;

    /** Tri Mapping growth factor */
    private static final float TRI_MAPPING_GROWTH = 1.30f;

    private static final boolean COLLECT_STATS = true;

    /** The voxel size */
    private double voxelSize;

    /** The half voxel size */
    private double halfVoxel;

    /** The half voxel size increased a bit to avoid voxel grid issue */
    private double halfVoxelIncreased;

    /** The minimum grid position in world coordinates */
    private float minGridWorldCoord;

    /** The maximum grid position in world coordinates */
    private float maxGridWorldCoord;

    /** The number of voxels each axis */
    private int numVoxels;

    /** Half number of voxels for quick math */
    private int half;

    /** The triangles by voxel. */
    private Map<VoxelCoordinate, int[]> data;

    /** Scratch variables */
    private float[] minBounds;
    private float[] maxBounds;
    private int[] minCoords;
    private int[] maxCoords;
    private int[] gpos;
    private float[] wpos;
    private VoxelCoordinate svc;

    // Stats
    /** How many cells have been filled by inserts */

    public int cellsFilled;
    public int numInserts;

    // Scratch vars
    private float[] vcoords;
    private Vec3DDouble vmin;
    private Vec3DDouble vmax;
    private float[] lineeq;
    private float[] linea;
    private float[] lineb;

    private float[][] triMapping;

    /** The next triangleID to use */
    private int nextID;

    private int splitDepth;

    private Vec3DDouble v0;
    private Vec3DDouble v1;
    private Vec3DDouble v2;

    /**
     * Constructor.
     *
     * @param voxelSize The size of each voxel in meters.
     * @param numVoxelsPerSide The number of voxels in each axis.  Must be even.
     * @param numTris
     */
    public GridTrianglePartition(double voxelSize, int numVoxelsPerSide, int numTris) {
        if (numVoxelsPerSide % 2 != 0)
            throw new IllegalArgumentException("Number of voxels per side must be even");

        triMapping = new float[numTris][9];
        nextID = numTris;

        this.voxelSize = voxelSize;
        halfVoxel = voxelSize / 2.0;
        halfVoxelIncreased = halfVoxel * 1.02;

        this.numVoxels = numVoxelsPerSide;
        half = numVoxels / 2;

        minGridWorldCoord = (float) (- numVoxels / 2 * voxelSize);
        maxGridWorldCoord = (float) (numVoxels / 2 * voxelSize);

//System.out.println("Initialize spatial grid at: " + numVoxels + " tot: " + (numVoxels * numVoxels) + " size: " + voxelSize);

        long grid_size = (long) numVoxelsPerSide * numVoxelsPerSide * numVoxelsPerSide;
        int size = Math.round(grid_size * EXTERIOR_FACTOR);

//        System.out.println("Max HashMap size: " + grid_size + " start: " + size);
        data = new HashMap<>(size);
        minBounds = new float[3];
        maxBounds = new float[3];
        minCoords = new int[3];
        maxCoords = new int[3];

        vcoords = new float[3];
        vmin = new Vec3DDouble();
        vmax = new Vec3DDouble();
        lineeq = new float[6];
        linea = new float[3];
        lineb = new float[3];
        gpos = new int[3];
        wpos = new float[3];

        svc = new VoxelCoordinate(0,0,0);
        v0 = new Vec3DDouble(0,0,0);
        v1 = new Vec3DDouble(0,0,0);
        v2 = new Vec3DDouble(0,0,0);
    }

    //-------------------------------------------------------------
    // Methods defined by SpatialPartition
    //-------------------------------------------------------------


    /**
     * Clear the structure of all data.
     */
    @Override
    public void clear() {
        data = null;
    }

    /**
     * Gets the objects in the specified region. Any object which is contained
     * or overlaps the region will be returned.  Objects exactly on a voxel
     * boundary shall be returned in all touching regions.
     *
     * @param region The region of interest.
     * @return objs The list of triangles
     */
    @Override
    public int[] getObjects(Region region) {

        int[] ret_val = null;

        if (region instanceof CellRegion) {
            CellRegion cell = (CellRegion) region;

            ret_val = getData(cell.getX(),cell.getY(),cell.getZ());
        } else if (region instanceof SliceRegion) {
            SliceRegion slice = (SliceRegion) region;

            // TODO: implement thread local for minCoords / maxCoords

//            int[] minCoords = new int[3];
//            int[] maxCoords = new int[3];

            switch(slice.getDir()) {
                case X:
                    // calc bounds of slice
                    minCoords[0] = slice.getLoc();
                    minCoords[1] = 0;
                    minCoords[2] = 0;
                    maxCoords[0] = slice.getLoc() + slice.getHeight() - 1;
                    maxCoords[1] = numVoxels - 1;
                    maxCoords[2] = numVoxels - 1;

                    if (maxCoords[0] >= numVoxels)
                        maxCoords[0] = numVoxels - 1;

                    ret_val = getObjects(minCoords, maxCoords);
                    break;
                case Y:
                    // calc bounds of slice
                    minCoords[0] = 0;
                    minCoords[1] = slice.getLoc();
                    minCoords[2] = 0;
                    maxCoords[0] = numVoxels - 1;
                    maxCoords[1] = slice.getLoc() + slice.getHeight() - 1;
                    maxCoords[2] = numVoxels - 1;

                    if (maxCoords[1] >= numVoxels)
                        maxCoords[1] = numVoxels - 1;

                    ret_val = getObjects(minCoords, maxCoords);
                    break;
                case Z:
                    // calc bounds of slice
                    minCoords[0] = 0;
                    minCoords[1] = 0;
                    minCoords[2] = slice.getLoc();
                    maxCoords[0] = numVoxels - 1;
                    maxCoords[1] = numVoxels - 1;
                    maxCoords[2] = slice.getLoc() + slice.getHeight() - 1;

                    if (maxCoords[2] >= numVoxels)
                        maxCoords[2] = numVoxels - 1;


//System.out.println("Region: " + java.util.Arrays.toString(minCoords) + " max: " + java.util.Arrays.toString(maxCoords));

                    ret_val = getObjects(minCoords, maxCoords);
                    break;
            }
        } else if (region instanceof TunnelRegion) {
            TunnelRegion slice = (TunnelRegion) region;

//            int[] minCoords = new int[3];
//            int[] maxCoords = new int[3];

            final int height = slice.getHeight();

            switch(slice.getDir()) {
                case X:
                    // calc bounds of slice
                    minCoords[0] = 0;
                    minCoords[1] = slice.getLoc1() - height;
                    minCoords[2] = slice.getLoc2() - height;
                    maxCoords[0] = numVoxels - 1;
                    maxCoords[1] = slice.getLoc1() + height;
                    maxCoords[2] = slice.getLoc2() + height;

                    if (height > 0) {
                        if (minCoords[1] < 0)
                            minCoords[1] = 0;

                        if (minCoords[2] < 0)
                            minCoords[2] = 0;

                        if (maxCoords[1] >= numVoxels)
                            maxCoords[1] = numVoxels - 1;

                        if (maxCoords[2] >= numVoxels)
                            maxCoords[2] = numVoxels - 1;
                    }

//System.out.println("Region: " + java.util.Arrays.toString(minCoords) + " max: " + java.util.Arrays.toString(maxCoords));

                        ret_val = getObjects(minCoords, maxCoords);
                        break;
                case Y:
                    // calc bounds of slice
                    minCoords[0] = slice.getLoc1() - height;
                    minCoords[1] = 0;
                    minCoords[2] = slice.getLoc2() - height;
                    maxCoords[0] = slice.getLoc1() + height;
                    maxCoords[1] = numVoxels - 1;
                    maxCoords[2] = slice.getLoc2() + height;

                    if (height > 0) {
                        if (minCoords[0] < 0)
                            minCoords[0] = 0;

                        if (minCoords[2] < 0)
                            minCoords[2] = 0;

                        if (maxCoords[0] >= numVoxels)
                            maxCoords[0] = numVoxels - 1;

                        if (maxCoords[2] >= numVoxels)
                            maxCoords[2] = numVoxels - 1;
                    }

//System.out.println("Region: " + java.util.Arrays.toString(minCoords) + " max: " + java.util.Arrays.toString(maxCoords));

                        ret_val = getObjects(minCoords, maxCoords);
                        break;
                case Z:
                    // calc bounds of slice
                    minCoords[0] = slice.getLoc1() - height;
                    minCoords[1] = slice.getLoc2() - height;
                    minCoords[2] = 0;
                    maxCoords[0] = slice.getLoc1() + height;
                    maxCoords[1] = slice.getLoc2() + height;
                    maxCoords[2] = numVoxels - 1;

                    if (height > 0) {
                        if (minCoords[0] < 0)
                            minCoords[0] = 0;

                        if (minCoords[1] < 0)
                            minCoords[1] = 0;

                        if (maxCoords[0] >= numVoxels)
                            maxCoords[0] = numVoxels - 1;

                        if (maxCoords[1] >= numVoxels)
                            maxCoords[1] = numVoxels - 1;
                    }

                    ret_val = getObjects(minCoords, maxCoords);
                    break;
            }
        } else if (region instanceof AllRegion) {
            int[] min = new int[] {0,0,0};
            int[] max = new int[] {numVoxels - 1, numVoxels - 1, numVoxels -1};
            return getObjects(min, max);
        } else {
            throw new IllegalArgumentException("Unsupported region: " + region);
        }

        return ret_val;
    }

    /**
     * Gets the objects in the specified region. Any object which is contained
     * or overlaps the region will be returned.  Objects exactly on a voxel
     * boundary shall be returned in all touching regions.
     *
     * @param min The min grid cell location
     * @param max The max grid cell location
     * @return The objects or null if none
     */
    public int[] getObjects(int[] min, int[] max) {

        final int len_x = max[0] - min[0] + 1;
        final int len_y = max[1] - min[1] + 1;
        final int len_z = max[2] - min[2] + 1;

        // TODO: not sure of a good guess on size here.
        IntHashSet set = new IntHashSet(53);

        int i,j,k;
//System.out.println("GetObjects");
//System.out.println("Min: " + java.util.Arrays.toString(min));
//System.out.println("Max: " + java.util.Arrays.toString(max));
        int[] val;
        int idx = 0;
        int cnt = 0;
        int len;

        for(int x = 0; x < len_x; x++) {
            for(int y = 0; y < len_y; y++) {
                for(int z = 0; z < len_z; z++) {
                    i = min[0] + x;
                    j = min[1] + y;
                    k = min[2] + z;

                    val =  getData(i,j,k);

                    if (val != null) {
                        len = val.length;

                        for(int n=0; n < len; n++) {
                            set.add(val[n]);
                        }
                    }
                }
            }
        }

        if (set.isEmpty()) {
            return null;
        }

        int[] ret_val = set.toArray();

        return ret_val;
    }

    /**
     * Get the triangle mapping for an id.
     * @param id
     * @return
     */
    public float[] getTriangle(int id) {
        return triMapping[id];
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------

    /**
     * Insert an object into the structure.
     *
     * @param tri The triangle
     * @param useBounds Should the bounds be used instead of the exact object.
     */
    public void insert(Triangle tri, boolean useBounds) {

        triMapping[tri.id] = tri.coords;

        if (COLLECT_STATS) {
            numInserts++;
        }

        if (useBounds) {
            calcBoundsForTriangle(tri, minBounds, maxBounds);

//    System.out.println("Orig.minBounds: " + java.util.Arrays.toString(minBounds));
//    System.out.println("Orig.maxBounds: " + java.util.Arrays.toString(maxBounds));
            findGridCoordsFromWorldCoords(minBounds, minCoords);
            findGridCoordsFromWorldCoords(maxBounds, maxCoords);
//System.out.println("Orig.minCoords: " + java.util.Arrays.toString(minCoords));
//System.out.println("Orig.maxCoords: " + java.util.Arrays.toString(maxCoords));


            // Handle on voxel boundary issues
            for(int j=0; j < 3; j++) {
                if (minBounds[j] % voxelSize == 0) {
                    minBounds[j] -= halfVoxel;
                    if (minBounds[j] < minGridWorldCoord) {
                        minBounds[j] = minGridWorldCoord;
                    }
                }
            }
            for(int j=0; j < 3; j++) {
                if (maxBounds[j] % voxelSize == 0) {
                    maxBounds[j] += halfVoxel;
                    if (maxBounds[j] > maxGridWorldCoord) {
                        maxBounds[j] = maxGridWorldCoord;
                    }
                }
            }

/*
System.out.println("Triangle: " + java.util.Arrays.toString(tri.coords));
System.out.println("minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("maxBounds: " + java.util.Arrays.toString(maxBounds));
*/
            findGridCoordsFromWorldCoords(minBounds, minCoords);
            findGridCoordsFromWorldCoords(maxBounds, maxCoords);

/*
System.out.println("minCoords: " + java.util.Arrays.toString(minCoords));
System.out.println("maxCoords: " + java.util.Arrays.toString(maxCoords));
*/
            fillCells(minCoords, maxCoords, tri);
        } else {
            // Tri / Box intersection code does not deal with thin triangles well
            double area = tri.getArea();
            double ratio = tri.getSideRatio();

            if (ratio > TRIANGLE_RATIO_MAX) {
//                System.out.println("Suspect triangle.  Area: " + area + " id: " + tri.id + " ratio: " + ratio);
/*
                growMapping(nextID+2);
                Triangle[] tris = tri.splitTriangle(nextID);
                nextID += 2;
                System.out.println("New Ratios: ");
                for(int i=0; i < tris.length; i++) {
                    System.out.println(tris[i].getSideRatio());
                    insert(tris[i], false);
                }

                return;
*/
            }

            calcBoundsForTriangle(tri, minBounds, maxBounds);

//    System.out.println("Orig.minBounds: " + java.util.Arrays.toString(minBounds));
//    System.out.println("Orig.maxBounds: " + java.util.Arrays.toString(maxBounds));
            findGridCoordsFromWorldCoords(minBounds, minCoords);
            findGridCoordsFromWorldCoords(maxBounds, maxCoords);
//System.out.println("Orig.minCoords: " + java.util.Arrays.toString(minCoords));
//System.out.println("Orig.maxCoords: " + java.util.Arrays.toString(maxCoords));


            // Handle on voxel boundary issues
            for(int j=0; j < 3; j++) {
                if (minBounds[j] % voxelSize == 0) {
                    minBounds[j] -= halfVoxel;
                    if (minBounds[j] < minGridWorldCoord) {
                        minBounds[j] = minGridWorldCoord;
                    }
                }
            }
            for(int j=0; j < 3; j++) {
                if (maxBounds[j] % voxelSize == 0) {
                    maxBounds[j] += halfVoxel;
                    if (maxBounds[j] > maxGridWorldCoord) {
                        maxBounds[j] = maxGridWorldCoord;
                    }
                }
            }

/*
System.out.println("Triangle: " + java.util.Arrays.toString(tri.coords));
System.out.println("minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("maxBounds: " + java.util.Arrays.toString(maxBounds));
*/
            findGridCoordsFromWorldCoords(minBounds, minCoords);
            findGridCoordsFromWorldCoords(maxBounds, maxCoords);


//System.out.println("minCoords: " + java.util.Arrays.toString(minCoords));
//System.out.println("maxCoords: " + java.util.Arrays.toString(maxCoords));


            fillCellsExact(minCoords, maxCoords, tri);
        }
    }

    /**
     * Insert an array of Triangles.
     *
     * @param tris The triangles
     * @param useBounds Should the bounds be used instead of the exact object.
     */
    public void insert(Triangle[] tris,boolean useBounds) {
        if (COLLECT_STATS) {
            numInserts++;
        }

        if (useBounds) {
            for (Triangle tri : tris) {
                calcBoundsForTriangle(tri, minBounds, maxBounds);
                findGridCoordsFromWorldCoords(minBounds, minCoords);
                findGridCoordsFromWorldCoords(maxBounds, maxCoords);
                fillCells(minCoords, maxCoords, tri);
            }
        } else {
System.out.println("geom not implemented");

/*
            for(int i=0; i < tris.length; i++) {
                voxelizeTriangle(tris[i]);
            }
*/
        }
    }


    /**
     * Calculate a bounding box for a triangle.
     *
     * @param tri
     * @param min The array to fill in the min bounds
     * @param max The array to fill in the max bounds
     */
    protected void calcBoundsForTriangle(Triangle tri, float[] min, float[] max) {
        // TODO: Moved to Triangle, can delete

        min[0] = Float.POSITIVE_INFINITY;
        min[1] = Float.POSITIVE_INFINITY;
        min[2] = Float.POSITIVE_INFINITY;

        max[0] = Float.NEGATIVE_INFINITY;
        max[1] = Float.NEGATIVE_INFINITY;
        max[2] = Float.NEGATIVE_INFINITY;

        if (tri.coords[0] < min[0]) {
            min[0] = tri.coords[0];
        }

        if (tri.coords[1] < min[1]) {
            min[1] = tri.coords[1];
        }

        if (tri.coords[2] < min[2]) {
            min[2] = tri.coords[2];
        }

        if (tri.coords[0] > max[0]) {
            max[0] = tri.coords[0];
        }

        if (tri.coords[1] > max[1]) {
            max[1] = tri.coords[1];
        }

        if (tri.coords[2] > max[2]) {
            max[2] = tri.coords[2];
        }

        if (tri.coords[3] < min[0]) {
            min[0] = tri.coords[3];
        }

        if (tri.coords[4] < min[1]) {
            min[1] = tri.coords[4];
        }

        if (tri.coords[5] < min[2]) {
            min[2] = tri.coords[5];
        }

        if (tri.coords[3] > max[0]) {
            max[0] = tri.coords[3];
        }

        if (tri.coords[4] > max[1]) {
            max[1] = tri.coords[4];
        }

        if (tri.coords[5] > max[2]) {
            max[2] = tri.coords[5];
        }

        if (tri.coords[6] < min[0]) {
            min[0] = tri.coords[6];
        }

        if (tri.coords[7] < min[1]) {
            min[1] = tri.coords[7];
        }

        if (tri.coords[8] < min[2]) {
            min[2] = tri.coords[8];
        }

        if (tri.coords[6] > max[0]) {
            max[0] = tri.coords[6];
        }

        if (tri.coords[7] > max[1]) {
            max[1] = tri.coords[7];
        }

        if (tri.coords[8] > max[2]) {
            max[2] = tri.coords[8];
        }
    }

    /**
     * Find voxel grid coordinates for a given world coordinate
     *
     * @param coords The world coordinates
     * @param pos The position in the grid, preallocate to 3.
//     * @return The grid coords, x,y,z
     */
    public void findGridCoordsFromWorldCoords(float[] coords, int[] pos) {
        // TODO: Why not use a positive only grid to avoid all this math.

//System.out.println("find grid: " + coords[0] + " float; " + (coords[0] / voxelSize) + " int: " + ((int)(coords[0] / voxelSize)) + " ceil: " + Math.ceil((coords[0] / voxelSize)) + " floor: " + Math.floor(coords[0] / voxelSize));

/*
        pos[0] = half + (int) (coords[0] / voxelSize);
        pos[1] = half + (int) (coords[1] / voxelSize);
        pos[2] = half + (int) (coords[2] / voxelSize);

        // TODO: remove this check should fix outer code
        if (pos[0] > numVoxels - 1)
            pos[0] = numVoxels - 1;
        if (pos[1] > numVoxels - 1)
            pos[1] = numVoxels - 1;
        if (pos[2] > numVoxels - 1)
            pos[2] = numVoxels - 1;

*/


        if (coords[0] < 0) {
            pos[0] = half + (int) Math.floor((coords[0] / voxelSize));
            if (pos[0] < 0)
                pos[0] = 0;
        } else {
            pos[0] = half + (int) Math.floor((coords[0] / voxelSize));
            if (pos[0] > numVoxels - 1)
                pos[0] = numVoxels - 1;
        }

        if (coords[1] < 0) {
            pos[1] = half + (int) Math.floor((coords[1] / voxelSize));
            if (pos[1] < 0)
                pos[1] = 0;
        } else {
            pos[1] = half + (int) Math.floor((coords[1] / voxelSize));
            if (pos[1] > numVoxels - 1)
                pos[1] = numVoxels - 1;
        }

        if (coords[2] < 0) {
            pos[2] = half + (int) Math.floor((coords[2] / voxelSize));
            if (pos[2] < 0)
                pos[2] = 0;
        } else {
            pos[2] = half + (int) Math.floor((coords[2] / voxelSize));
            if (pos[2] > numVoxels - 1)
                pos[2] = numVoxels - 1;
        }
    }

    /**
     * Find voxel grid coordinates for a given world coordinate
     *
     * @param coord The world coordinates
//     * param pos The position in the grid, preallocate to 3.
     * @return The grid coords, x,y,z
     */
    public int findGridCoordsFromWorldCoords(float coord) {
/*
        int ret_val = half + (int) (coord / voxelSize);

        if (ret_val > numVoxels - 1)
            ret_val = numVoxels - 1;

        return ret_val;
*/
//System.out.println(coord + " div = " + (coord / voxelSize) + " as int: " + ((int) (coord / voxelSize)) + " half: " + half + " ret: " + ret_val);

        int pos;

        if (coord < 0) {
            pos = half + (int) Math.floor((coord / voxelSize));
            if (pos < 0)
                pos = 0;
        } else {
            pos = half + (int) Math.floor((coord / voxelSize));
            if (pos > numVoxels - 1)
                pos = numVoxels - 1;
        }

        return pos;
    }

    /**
     * Find voxel world coordinates for a given voxel coordinate.
     *
     * @param x The x coord
     * @param y The y coord
     * @param z The z coord
     * @param pos The returned position, preallocate to 3
     */
    public void findVoxelInWorldCoords(int x, int y, int z, float[] pos) {
        pos[0] = (float) ((x - half) * voxelSize + halfVoxel);
        pos[1] = (float) ((y - half) * voxelSize + halfVoxel);
        pos[2] = (float) ((z - half) * voxelSize + halfVoxel);
    }


    /**
     * Fill in the grid given a box of grid coordinates.
     *
     * @param min The min bounds in cell coords
     * @param max The max bounds in cell coords
     * @param tri
     */
    protected void fillCells(int[] min, int[] max, Triangle tri) {
        final int len_x = max[0] - min[0] + 1;
        final int len_y = max[1] - min[1] + 1;
        final int len_z = max[2] - min[2] + 1;

        int i,j,k;
        int[] val;

        for(int x = 0; x < len_x; x++) {
            for(int y = 0; y < len_y; y++) {
                for(int z = 0; z < len_z; z++) {
                    i = min[0] + x;
                    j = min[1] + y;
                    k = min[2] + z;

                    findVoxelInWorldCoords(i,j,k, vcoords);

                    addData(i,j,k,tri.id);

                    if (COLLECT_STATS) {
                        cellsFilled++;
                    }
                }
            }
        }
    }

    /**
     * Fill in the grid given a box of grid coordinates.
     *
     * @param min The min bounds in cell coords
     * @param max The max bounds in cell coords
     * @param tri
     */
    protected void fillCellsExact(int[] min, int[] max, Triangle tri) {
// TODO: I think either the bounds check is off or something/
// Testing with full range of voxels gives more results then just using the bounds

/*
System.out.println("*** expanded bounds to all grid for testing");
// TODO: remove me
min[0] = 0;
min[1] = 0;
min[2] = 0;
max[0] = numVoxels - 1;
max[1] = numVoxels - 1;
max[2] = numVoxels - 1;
*/
//System.out.println("min: " + java.util.Arrays.toString(min) + " max: " + java.util.Arrays.toString(max));
        final int len_x = max[0] - min[0] + 1;
        final int len_y = max[1] - min[1] + 1;
        final int len_z = max[2] - min[2] + 1;

        int i,j,k;
        int cells = len_x * len_y * len_z;
        int[] val;
        VoxelCoordinate vc;

        if (cells == 1) {
            // single cell, just fill it
            i = min[0];
            j = min[1];
            k = min[2];

            addData(i,j,k,tri.id);

            if (COLLECT_STATS) {
                cellsFilled++;
            }

            return;
        } else if (cells == 2) {

            i = min[0];
            j = min[1];
            k = min[2];

            addData(i,j,k,tri.id);

            i = max[0];
            j = max[1];
            k = max[2];

            addData(i,j,k,tri.id);

            if (COLLECT_STATS) {
                cellsFilled = cellsFilled + 2;
            }

            return;
        }

        boolean direct = false;
        double OVER_SAMPLE = 0.01;

        if (direct) {

// TODO: For larger areas would it be better to sample triangle lines
//       and directly calculate voxels instead?

            // v0 to v1
            linea[0] = tri.coords[0];
            linea[1] = tri.coords[1];
            linea[2] = tri.coords[2];
            lineb[0] = tri.coords[3];
            lineb[1] = tri.coords[4];
            lineb[2] = tri.coords[5];
//System.out.println("linea: " + java.util.Arrays.toString(linea));
//System.out.println("lineb: " + java.util.Arrays.toString(lineb));
            calcLineEq(linea,lineb, lineeq);

            float x = lineeq[1];
            float y = lineeq[3];
            float z = lineeq[5];

            double dist = Math.sqrt(x * x + y * y + z * z);
            // slightly oversample to avoid float alignment problems
            double vsize = voxelSize * OVER_SAMPLE;
            int steps = (int) Math.ceil(dist / (vsize));
            double stepSize = vsize;
            double t;
            int last_i, last_j, last_k;

            last_i = -1;
            last_j = -1;
            last_k = -1;
//System.out.println("dist: " + dist + " steps: " + steps + " stepSize: " + stepSize);

            for(int n=0; n < steps; n++) {
                t = n * stepSize;
//System.out.println("linex: " + lineeq[0] + " " + lineeq[1] + " t: " + t);
//System.out.println("liney: " + lineeq[2] + " " + lineeq[3] + " t: " + t);
//System.out.println("linez: " + lineeq[4] + " " + lineeq[5] + " t: " + t);

                wpos[0] = (float) (lineeq[0] + lineeq[1] * t);
                wpos[1] = (float) (lineeq[2] + lineeq[3] * t);
                wpos[2] = (float) (lineeq[4] + lineeq[5] * t);

//System.out.println("t: " + t + " wpos: " + java.util.Arrays.toString(wpos));
                findGridCoordsFromWorldCoords(wpos, gpos);
                i = gpos[0];
                j = gpos[1];
                k = gpos[2];

                // TODO: Can go past target, is it worth check to avoid extra tris?

//    System.out.println("Check cell: " + i + " " + j + " " + k);

                if (i == last_i && j == last_j && k == last_k) {
                } else {
//    System.out.println("Add cell: " + i + " " + j + " " + k);
                    addData(i,j,k,tri.id);

                    last_i = i;
                    last_j = j;
                    last_k = k;
                }
            }
            if (COLLECT_STATS) {
                // TODO: This is wrong each step doesnt equal fill
                cellsFilled += steps;
            }

            // v1 to v2
            linea[0] = tri.coords[3];
            linea[1] = tri.coords[4];
            linea[2] = tri.coords[5];
            lineb[0] = tri.coords[6];
            lineb[1] = tri.coords[7];
            lineb[2] = tri.coords[8];
            calcLineEq(linea,lineb, lineeq);

            x = lineeq[1];
            y = lineeq[3];
            z = lineeq[5];

            dist = (float) Math.sqrt(x * x + y * y + z * z);
            steps = (int) Math.ceil(dist / (vsize));
            stepSize = vsize;

//System.out.println("v1 -&gt; v2 dist: " + dist + " steps: " + steps + " stepSize: " + stepSize);


            for(int n=0; n < steps; n++) {
                t = n * stepSize;

                wpos[0] = (float) (lineeq[0] + lineeq[1] * t);
                wpos[1] = (float) (lineeq[2] + lineeq[3] * t);
                wpos[2] = (float) (lineeq[4] + lineeq[5] * t);

                findGridCoordsFromWorldCoords(wpos, gpos);
                i = gpos[0];
                j = gpos[1];
                k = gpos[2];

//System.out.println("Check cell: " + i + " " + j + " " + k);

                if (i == last_i && j == last_j && k == last_k) {
                } else {
                    addData(i,j,k,tri.id);
                    last_i = i;
                    last_j = j;
                    last_k = k;
                }
            }
            if (COLLECT_STATS) {
                cellsFilled += steps;
            }

            // v0 to v2
            linea[0] = tri.coords[0];
            linea[1] = tri.coords[1];
            linea[2] = tri.coords[2];
            lineb[0] = tri.coords[6];
            lineb[1] = tri.coords[7];
            lineb[2] = tri.coords[8];
            calcLineEq(linea,lineb, lineeq);

            x = lineeq[1];
            y = lineeq[3];
            z = lineeq[5];

            dist = (float) Math.sqrt(x * x + y * y + z * z);
            steps = (int) Math.ceil(dist / (vsize));
            stepSize = vsize;

//System.out.println("v0 to v2: dist: " + dist + " steps: " + steps + " stepSize: " + stepSize);


            for(int n=0; n < steps; n++) {
                t = n * stepSize;

                wpos[0] = (float) (lineeq[0] + lineeq[1] * t);
                wpos[1] = (float) (lineeq[2] + lineeq[3] * t);
                wpos[2] = (float) (lineeq[4] + lineeq[5] * t);

                findGridCoordsFromWorldCoords(wpos, gpos);
                i = gpos[0];
                j = gpos[1];
                k = gpos[2];

//System.out.println("Check cell: " + i + " " + j + " " + k);


                if (i == last_i && j == last_j && k == last_k) {
                } else {
                    addData(i,j,k,tri.id);
                    last_i = i;
                    last_j = j;
                    last_k = k;
                }
            }
            if (COLLECT_STATS) {
                cellsFilled += steps;
            }
        }

        if (!direct) {
/*
System.out.println("Filling old way:  min: " + java.util.Arrays.toString(min));
findVoxelInWorldCoords(0,0,0, vcoords);
float[] wpos = new float[3];
int[] vpos = new int[3];
int idx = 0;
wpos[0] = tri.coords[idx++];
wpos[1] = tri.coords[idx++];
wpos[2] = tri.coords[idx++];
findGridCoordsFromWorldCoords(wpos, vpos);
System.out.println("v0: " + java.util.Arrays.toString(wpos) + " --> " + java.util.Arrays.toString(vpos));
wpos[0] = tri.coords[idx++];
wpos[1] = tri.coords[idx++];
wpos[2] = tri.coords[idx++];
findGridCoordsFromWorldCoords(wpos, vpos);
System.out.println("v1: " + java.util.Arrays.toString(wpos) + " --> " + java.util.Arrays.toString(vpos));
wpos[0] = tri.coords[idx++];
wpos[1] = tri.coords[idx++];
wpos[2] = tri.coords[idx++];
findGridCoordsFromWorldCoords(wpos, vpos);
System.out.println("v2: " + java.util.Arrays.toString(wpos) + " --> " + java.util.Arrays.toString(vpos));
*/
/*
            Vec3DDouble v0 = new Vec3DDouble(tri.coords[0], tri.coords[1], tri.coords[2]);
            Vec3DDouble v1 = new Vec3DDouble(tri.coords[3], tri.coords[4], tri.coords[5]);
            Vec3DDouble v2 = new Vec3DDouble(tri.coords[6], tri.coords[7], tri.coords[8]);
*/
            v0.set(tri.coords[0],tri.coords[1],tri.coords[2]);
            v1.set(tri.coords[3],tri.coords[4],tri.coords[5]);
            v2.set(tri.coords[6],tri.coords[7],tri.coords[8]);

            int cnt = 0;

//System.out.println("bounds: " + len_x + " " + len_y + " " + len_z);
            for(int xc = 0; xc < len_x; xc++) {
                for(int yc = 0; yc < len_y; yc++) {
                    for(int zc = 0; zc < len_z; zc++) {
                        i = min[0] + xc;
                        j = min[1] + yc;
                        k = min[2] + zc;

                        findVoxelInWorldCoords(i,j,k, vcoords);

//System.out.println("Testing: " + i + " " + j + " " + k + " " + java.util.Arrays.toString(vcoords));
                        if (intersectsTriangle(v0,v1,v2, vcoords)) {
                            addData(i,j,k, tri.id);

                            cnt++;
                        }
                    }
                }
            }

            if (cnt == 0) {
                System.out.println("ERROR: No cells marked from triangle: " + tri.id);
                System.out.println("   area: " + tri.getArea() + " ratio: " + tri.getSideRatio());
                printVertexCells(tri);


                if (triSmallerVoxel(tri)) {
System.out.println("Smaller then voxel, fill: " + tri.id);
                    fillVertexCells(tri);

                    cellsFilled += 3;  // This may overcount.
                } else {
                    // The other idea would be walk the affected voxels in a 3D Bresenham algo
                    growMapping(nextID+2);
                    Triangle[] tris = tri.splitTriangle(nextID);
                    nextID += 2;


                    if (splitDepth == 0) {
                        System.out.println("Splitting triangle: " + java.util.Arrays.toString(tri.coords));
                    } else {

                        System.out.println("Split Depth: " + splitDepth);
                    }

                    splitDepth++;

                    if (splitDepth > 2) {
                        System.out.println("Too much splitting, bailing");
                        fillVertexCells(tri);

                        cellsFilled += 3;  // This may overcount.
                        return;
                    }

                    for (Triangle tri1 : tris) {
                        insert(tri1, false);
                    }
                    splitDepth--;
                }

            } else {
                cellsFilled += cnt;
            }

        }
    }

    /**
     * Return the grid counts in strin form.
     *
     * @return The grid counts
     */
    public String gridCountsToString() {
        NumberFormat numberFormater = NumberFormat.getNumberInstance();
        numberFormater.setMaximumFractionDigits(0);
        numberFormater.setGroupingUsed(false);

        StringBuilder buff = new StringBuilder();

        for(int x = 0; x < numVoxels; x++) {
            buff.append("XROW:");
            buff.append(x);
            buff.append("\n");

            StringBuilder buff2 = new StringBuilder();
            boolean nonzero = false;
            int[] val;

            for(int y = 0; y < numVoxels; y++) {
                for(int z = 0; z < numVoxels; z++) {
                    int cnt;

                    val = getData(x,y,z);
                    if (val == null)
                        cnt = 0;
                    else {
                        nonzero = true;
                        cnt = val.length;
                    }

                    buff2.append(numberFormater.format(cnt));
                    buff2.append(" ");

                    if (z == half - 1)
                        buff2.append("| ");
                }

                buff2.append("\n");

                if (y == half - 1) {
                    for(int i=0; i < numVoxels; i++) {
                        buff2.append("--");
                    }

                    buff2.append("\n");
                }
            }

            if (nonzero) {
                buff.append(buff2.toString());
            } else {
                buff.append("EMPTY");
            }

            buff2.setLength(0);
        }

        return buff.toString();
    }

    public void printStats() {
        System.out.println("GridTrianglePartition Stats:");
        System.out.println("   Cells filled: " + cellsFilled);
        System.out.println("   Inserts: " + numInserts);
        System.out.println("   avg Cells Per Triangle: " + ((float) cellsFilled / numInserts));

        //System.out.println("Counts: \n" + gridCountsToString());

        long empty = 0;
        int max = 0;
        int thresh = 1;
        long tcount = 0;
        int[] val;

        for(int x = 0; x < numVoxels; x++) {
            for(int y = 0; y < numVoxels; y++) {
                for(int z = 0; z < numVoxels; z++) {
                    val = getData(x,y,z);

                    if (val == null) {
                        empty++;
                        continue;
                    }

                    int size = val.length;

                    if (size > max) {
                        max = size;
                    }

                    if (size >= thresh) {
                        tcount++;
                    }
                }
            }
        }

        long tot = numVoxels * numVoxels * numVoxels;

        System.out.println("Cells: " + (tot) + " empty: " + empty + " percent: " + ((float) empty / tot) + " filled: " + (tot - empty));
        System.out.println("max: " + max + " exc thresh: " + tcount);
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doVoxelization.

    Current method follows the voxelization algorithm strategy proposed
    in [DACH2000], but actual implementation only accounts for binary voxels.
    It is spected that with few changes, this algorithm manages the scalar
    (multivalued) voxel case for antialiased voxelization.
    */
/*
    private void voxelizeTriangle(Triangle tri) {
System.out.println("Voxelizing tri: " + java.util.Arrays.toString(tri.coords));
        Vector3D pVolume;
        int[] p0Vol = new int[3];
        int[] p1Vol = new int[3];
        int[] p2Vol = new int[3];

        float[] v0 = new float[] {tri.coords[0], tri.coords[1], tri.coords[2]};
        float[] v1 = new float[] {tri.coords[3], tri.coords[4], tri.coords[5]};
        float[] v2 = new float[] {tri.coords[6], tri.coords[7], tri.coords[8]};

        findGridCoordsFromWorldCoords(v0, p0Vol);
        findGridCoordsFromWorldCoords(v1, p1Vol);
        findGridCoordsFromWorldCoords(v2, p2Vol);

        Vector3D p0Volume, p1Volume, p2Volume;

        p0Volume = new Vector3D(p0Vol[0], p0Vol[1], p0Vol[2]);
        p1Volume = new Vector3D(p1Vol[0], p1Vol[1], p1Vol[2]);
        p2Volume = new Vector3D(p2Vol[0], p2Vol[1], p2Vol[2]);

        // Voxel volume control
        int i, j, k;
        // Structural algorithm control variables
        int t;
        int status;

        int[] minBounds = new int[3];
        int[] maxBounds = new int[3];

        findBoundsForTriangle(tri, minBounds, maxBounds);

        final int len_x = maxBounds[0] - minBounds[0] + 1;
        final int len_y = maxBounds[1] - minBounds[1] + 1;
        final int len_z = maxBounds[2] - minBounds[2] + 1;

        // TODO: Don't recalc
        // Rasterize triangle in voxel space
        //double distanceTolerance = 2.0 / (double) voxelSize;
        double distanceTolerance = halfVoxel;

System.out.println("distanceTol: " + distanceTolerance);
        float[] vcoords = new float[3];

        int filled = 0;

System.out.println("CheckV0a: " + p0Volume);
System.out.println("CheckV1: " + p1Volume);
System.out.println("CheckV2: " + p2Volume);

        for ( i = minBounds[0]; i <= maxBounds[0]; i++ ) {
            for ( j = minBounds[1]; j <= maxBounds[1]; j++ ) {
                for ( k = minBounds[2]; k <= maxBounds[2]; k++ ) {

                    findVoxelInWorldCoords(i,j,k, vcoords);
System.out.println("i: " + i + " j: " + j + " k: " + k + " coords: " + java.util.Arrays.toString(vcoords));
                    pVolume = new Vector3D(vcoords[0], vcoords[1], vcoords[2]);
System.out.println("pVolume: " + pVolume.x + " " + pVolume.y + " " + pVolume.z);

// TODO: Why plane test with distance?

                    status = ComputationalGeometry.triangleContainmentTest(
                        p0Volume, p1Volume, p2Volume,
                        pVolume, 8, distanceTolerance);
                    if ( status != Geometry.OUTSIDE ) {
                        if (data[i][j][k] == null) {
                            data[i][j][k] = new HashSet<Integer>(1);
                        }

System.out.println("Fill cell: " + i + " " + j + " " + k);
                        data[i][j][k].add(getIntegerID(tri));
                        filled++;
                    }
                }
            }
        }

        System.out.println("Bounds method: " + (len_x * len_y * len_z) + " exact: " + filled);

        if (COLLECT_STATS) {
            cellsFilled += filled;
        }
    }

*/
    public void findBoundsForTriangle(Triangle tri, int[] minCoords, int[] maxCoords) {

        calcBoundsForTriangle(tri, minBounds, maxBounds);

        boolean altered = false;


        // Handle on voxel boundary issues
        for(int j=0; j < 3; j++) {
System.out.println("minBounds: " + minBounds[j] + " on grid? " + (minBounds[j] % voxelSize) + " minGridWorldCoord: " + minGridWorldCoord);
            if (minBounds[j] % voxelSize == 0) {
                minBounds[j] -= halfVoxel;
                    altered = true;
                if (minBounds[j] < minGridWorldCoord) {
                    minBounds[j] = minGridWorldCoord;
                }
            }
        }
        for(int j=0; j < 3; j++) {
System.out.println("maxBounds: " + maxBounds[j] + " on grid? " + (maxBounds[j] % voxelSize) + " maxGridWorldCoord: " + maxGridWorldCoord);
            if (maxBounds[j] % voxelSize == 0) {
                maxBounds[j] += halfVoxel;
                    altered = true;
                if (maxBounds[j] > maxGridWorldCoord) {
                    maxBounds[j] = maxGridWorldCoord;
                }
            }
        }


if (altered) {
System.out.println("A.minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("A.maxBounds: " + java.util.Arrays.toString(maxBounds));
}


        findGridCoordsFromWorldCoords(minBounds, minCoords);
        findGridCoordsFromWorldCoords(maxBounds, maxCoords);
System.out.println("Triangle: " + java.util.Arrays.toString(tri.coords));
System.out.println("minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("maxBounds: " + java.util.Arrays.toString(maxBounds));
System.out.println("minCoords: " + java.util.Arrays.toString(minCoords));
System.out.println("maxCoords: " + java.util.Arrays.toString(maxCoords));
    }

    /**
     * Does triangle overlap a voxel.
     *
     * From paper: Fast 3D Triangle-Box Overlap Testing
     * TODO: this paper mentions having errors with long thin polygons
     *
     * @param a  first triangle coordinate
     * @param b second triangle coordinate
     * @param c  third triangle coordinate
     * @param pos The voxel center position
     */
    public boolean intersectsTriangle(Vec3DDouble a, Vec3DDouble b, Vec3DDouble c, float[] pos) {
        // use separating axis theorem to test overlap between triangle and box
        // need to test for overlap in these directions:
        //
        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
        // triangle
        // we do not even need to test these)
        // 2) normal of the triangle
        // 3) crossproduct(edge from tri, {x,y,z}-directin)
        // this gives 3x3=9 more tests
        Vec3DDouble v0, v1, v2;
        Vec3DDouble normal, e0, e1, e2, f;

        // move everything so that the boxcenter is in (0,0,0)
        v0 = a.sub(pos[0],pos[1],pos[2]);
        v1 = b.sub(pos[0],pos[1],pos[2]);
        v2 = c.sub(pos[0],pos[1],pos[2]);

        // compute triangle edges
        e0 = v1.sub(v0);

//        float hv = (float) halfVoxel;
        double hv = halfVoxelIncreased;

        // test the 9 tests first (this was faster)
        f = e0.getAbs();
        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, hv,
                hv)) {
            return false;
        }

        e1 = v2.sub(v1);
        f = e1.getAbs();
        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, hv,
                hv)) {
            return false;
        }

        e2 = v0.sub(v2);
        f = e2.getAbs();

        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, hv,
                hv)) {
            return false;
        }
        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, hv,
                hv)) {
            return false;
        }

        // first test overlap in the {x,y,z}-directions
        // find min, max of the triangle each direction, and test for overlap in
        // that direction -- this is equivalent to testing a minimal AABB around
        // the triangle against the AABB

        // test in X-direction
        if (MathUtils.min(v0.x, v1.x, v2.x) > hv
                || MathUtils.max(v0.x, v1.x, v2.x) < -hv) {
            return false;
        }

        // test in Y-direction
        if (MathUtils.min(v0.y, v1.y, v2.y) > hv
                || MathUtils.max(v0.y, v1.y, v2.y) < -hv) {
            return false;
        }

        // test in Z-direction
        if (MathUtils.min(v0.z, v1.z, v2.z) > hv
                || MathUtils.max(v0.z, v1.z, v2.z) < -hv) {
            return false;
        }

        // test if the box intersects the plane of the triangle
        // compute plane equation of triangle: normal*x+d=0
        normal = e0.cross(e1);
//        float d = -normal.dot(v0);
        double d = -normal.dot(v0);

        return planeBoxOverlap(normal, d, hv);
    }

    /**
     * Does a plane and box overlap.
     *
     * @param normal Normal to the plane
     * @param d Distance
     * @param hv Half voxel size
     */
    private boolean planeBoxOverlap(Vec3DDouble normal, double d, double hv) {
        if (normal.x > 0.0f) {
            vmin.x = -hv;
            vmax.x = hv;
        } else {
            vmin.x = hv;
            vmax.x = -hv;
        }

        if (normal.y > 0.0f) {
            vmin.y = -hv;
            vmax.y = hv;
        } else {
            vmin.y = hv;
            vmax.y = -hv;
        }

        if (normal.z > 0.0f) {
            vmin.z = -hv;
            vmax.z = hv;
        } else {
            vmin.z = hv;
            vmax.z = -hv;
        }
        if (normal.dot(vmin) + d > 0.0f) {
            return false;
        }
        return normal.dot(vmax) + d >= 0.0f;
    }

    /**
     * Does a plane and box overlap.
     *
     * @param normal Normal to the plane
     * @param d Distance
     * @param hv Half voxel size
     */
/*
    private boolean planeBoxOverlap(double[] normal, double d, double hv) {
        if (normal[0] > 0.0f) {
            vmin.x = -hv;
            vmax.x = hv;
        } else {
            vmin.x = hv;
            vmax.x = -hv;
        }

        if (normal.y > 0.0f) {
            vmin.y = -hv;
            vmax.y = hv;
        } else {
            vmin.y = hv;
            vmax.y = -hv;
        }

        if (normal.z > 0.0f) {
            vmin.z = -hv;
            vmax.z = hv;
        } else {
            vmin.z = hv;
            vmax.z = -hv;
        }
        if (normal.dot(vmin) + d > 0.0f) {
            return false;
        }
        if (normal.dot(vmax) + d >= 0.0f) {
            return true;
        }
        return false;
    }
*/

    /**
     * Test and axis intersection.
     */
    private boolean testAxis(float a, float b, float fa, float fb, float va,
            float vb, float wa, float wb, float ea, float eb) {
        float p0 = a * va + b * vb;
        float p2 = a * wa + b * wb;
        float min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        float rad = fa * ea + fb * eb;
        return (min > rad || max < -rad);
    }

    /**
     * Test and axis intersection.
     */
    private boolean testAxis(double a, double b, double fa, double fb, double va,
            double vb, double wa, double wb, double ea, double eb) {
        double p0 = a * va + b * vb;
        double p2 = a * wa + b * wb;
        double min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        double rad = fa * ea + fb * eb;
        return (min > rad || max < -rad);
    }

    /**
     * Calc parametric line equation for 2 points.
     * Returns 3 equations of x=a - bt, y=c - dt, z=e - ft
     *
     * @param a the a vector
     * @param b the b vector
     * @return lineeq The 6 params for a 3d line
     */
    private void calcLineEq(float[] a, float[] b, float[] lineeq) {
        lineeq[0] = a[0];
        lineeq[1] = b[0] - a[0];
        lineeq[2] = a[1];
        lineeq[3] = b[1] - a[1];
        lineeq[4] = a[2];
        lineeq[5] = b[2] - a[2];
    }

    HashMap<Integer, Integer> idCache = new HashMap<>();
    int miss = 0;

    private Integer getIntegerID(Triangle tri) {

        // TODO: This uses more memory?

        Integer id = tri.id;

        Integer ret_val = idCache.get(id);

        if (ret_val != null)
            return ret_val;

        idCache.put(id, id);
        miss++;

/*
        if (miss % 1000 == 0) {
            System.out.println("miss: " + miss);
        }
*/
        return id;
    }

    private int[] getData(VoxelCoordinate vc) {
        int[] ret_val = data.get(vc);

        return ret_val;
    }

    private int[] getData(int x, int y, int z) {
        svc.setValue(x,y,z);

        int[] ret_val = data.get(svc);

        return ret_val;
    }

    private void addData(int i, int j, int k, int id) {
        VoxelCoordinate vc = new VoxelCoordinate(i,j,k);
        int[] val = getData(vc);

        if (val == null) {
            val = new int[] {id};
            data.put(vc, val);
        } else {
            int new_size = val.length + 1;

            // TODO:  Would arrayCopy be faster, with the small
            // expected size I think not
            int[] new_val = new int[new_size];
            System.arraycopy(val, 0, new_val, 0, val.length);
            new_val[val.length] = id;
            data.put(vc, new_val);
        }
    }

    /**
     * Grow the triangle mapping to at least the specified param.
     *
     * @param min The new minimum
     */
    private void growMapping(int min) {
        int len = triMapping.length;

        // TODO: Can we arrayCopy to speed this?

        if (len < min) {
System.out.println("Growing map");
            float [][] new_mapping = new float[(int) (min * TRI_MAPPING_GROWTH)][9];

            for(int i=0; i < len; i++) {
                new_mapping[i][0] = triMapping[i][0];
                new_mapping[i][1] = triMapping[i][1];
                new_mapping[i][2] = triMapping[i][2];
                new_mapping[i][3] = triMapping[i][3];
                new_mapping[i][4] = triMapping[i][4];
                new_mapping[i][5] = triMapping[i][5];
                new_mapping[i][6] = triMapping[i][6];
                new_mapping[i][7] = triMapping[i][7];
                new_mapping[i][8] = triMapping[i][8];
            }

            triMapping = new_mapping;
        }
    }

    private boolean triSmallerVoxel(Triangle tri) {
        double size = tri.getLargestSide();

        return size < voxelSize;
    }

    /**
     * Fill the cells the vertices of a triangle are in.
     *
     * @param tri The triangle
     */
    private void fillVertexCells(Triangle tri) {
        float[] wcoords = new float[] {tri.coords[0], tri.coords[1], tri.coords[2]};
        int[] gcoords1 = new int[3];
        int[] gcoords2 = new int[3];
        int[] gcoords3 = new int[3];

        findGridCoordsFromWorldCoords(wcoords, gcoords1);
System.out.println("v0: " + java.util.Arrays.toString(wcoords));

        wcoords[0] = tri.coords[3];
        wcoords[1] = tri.coords[4];
        wcoords[2] = tri.coords[5];
System.out.println("v1: " + java.util.Arrays.toString(wcoords));

        findGridCoordsFromWorldCoords(wcoords, gcoords2);

        wcoords[0] = tri.coords[6];
        wcoords[1] = tri.coords[7];
        wcoords[2] = tri.coords[8];
System.out.println("v2: " + java.util.Arrays.toString(wcoords));

        findGridCoordsFromWorldCoords(wcoords, gcoords3);

System.out.println("v0: coords: " + java.util.Arrays.toString(gcoords1));
System.out.println("v1: coords: " + java.util.Arrays.toString(gcoords2));
System.out.println("v2: coords: " + java.util.Arrays.toString(gcoords3));

        // All within in voxel, fill
        System.out.println("Filling cells: " + gcoords1[0] + " " + gcoords1[1] + " " + gcoords1[2]);
        addData(gcoords1[0],gcoords1[1],gcoords1[2],tri.id);
        System.out.println("Filling cells: " + gcoords2[0] + " " + gcoords2[1] + " " + gcoords2[2]);
        addData(gcoords2[0],gcoords2[1],gcoords2[2],tri.id);
        System.out.println("Filling cells: " + gcoords3[0] + " " + gcoords3[1] + " " + gcoords3[2]);
        addData(gcoords3[0],gcoords3[1],gcoords3[2],tri.id);
    }

    /**
     * Print the cells the vertices of a triangle are in.
     *
     * @param tri The triangle
     */
    private void printVertexCells(Triangle tri) {
        float[] wcoords = new float[] {tri.coords[0], tri.coords[1], tri.coords[2]};
        int[] gcoords1 = new int[3];
        int[] gcoords2 = new int[3];
        int[] gcoords3 = new int[3];

        findGridCoordsFromWorldCoords(wcoords, gcoords1);

        wcoords[0] = tri.coords[3];
        wcoords[1] = tri.coords[4];
        wcoords[2] = tri.coords[5];

        findGridCoordsFromWorldCoords(wcoords, gcoords2);

        wcoords[0] = tri.coords[6];
        wcoords[1] = tri.coords[7];
        wcoords[2] = tri.coords[8];

        findGridCoordsFromWorldCoords(wcoords, gcoords3);

System.out.println("v0: coords: " + java.util.Arrays.toString(gcoords1));
System.out.println("v1: coords: " + java.util.Arrays.toString(gcoords2));
System.out.println("v2: coords: " + java.util.Arrays.toString(gcoords3));
    }

    /**
     * Is this triangle aligned to a grid line.  If so it returns
     * the axis its aligned too.
     *
     * @return The axes aligned(0=x,1=y,2=z) or null if not aligned
     */
    private int[] isGridAlignedOld(Triangle tri) {
        boolean x_aligned = false;
        boolean y_aligned = false;
        boolean z_aligned = false;
        int cnt = 0;

        float[] v0 = new float[] {tri.coords[0], tri.coords[1], tri.coords[2]};
        float[] v1 = new float[] {tri.coords[3], tri.coords[4], tri.coords[5]};
        float[] v2 = new float[] {tri.coords[6], tri.coords[7], tri.coords[8]};
        float fvoxel = (float) voxelSize;

System.out.println("x : " + v0[0] + " / " + (v0[0] / voxelSize) + " mod: " + (v0[0] % voxelSize));
System.out.println("y : " + v0[1] + " / " + (v0[1] / voxelSize) + " mod: " + (v0[1] % voxelSize));
System.out.println("z : " + v0[2] + " / " + (v0[2] / voxelSize) + " mod: " + (v0[2] % voxelSize));


        if (v0[0] == v1[0] && v0[0] == v2[0]) {
            if (v0[0] % fvoxel == 0) {
System.out.println("x_aligned");
                x_aligned = true;
                cnt++;
            }
        }

System.out.println("Testing y values");
        if (v0[1] == v1[1] && v0[1] == v2[1]) {
System.out.println("All equal.  val: " + v0[1] + " vs: " + fvoxel + " mod: " + (v0[1] % fvoxel));
            if (v0[1] % fvoxel == 0) {
System.out.println("y_aligned");
                y_aligned = true;
                cnt++;
            }

            System.out.println("y times: " + v0[1] / fvoxel);
        }

        if (v0[2] == v1[2] && v0[2] == v2[2]) {
            if (v0[2] % fvoxel == 0) {
System.out.println("z_aligned");
                z_aligned = true;
                cnt++;
            }
        }

        if (cnt == 0)
            return null;

        int[] ret_val = new int[cnt];
        cnt = 0;

        if (x_aligned)
            ret_val[cnt++] = 0;

        if (y_aligned)
            ret_val[cnt++] = 1;

        if (z_aligned)
            ret_val[cnt++] = 2;

System.out.println("ret: " + java.util.Arrays.toString(ret_val));
        return ret_val;
    }

    /**
     * Is this triangle aligned to a grid line.  If so it returns
     * the axis its aligned too.
     *
     * @return The axes aligned(0=x,1=y,2=z) or null if not aligned
     */
    private int[] isGridAligned(Triangle tri) {
        boolean x_aligned = false;
        boolean y_aligned = false;
        boolean z_aligned = false;
        int cnt = 0;

//        MathContext mc = MathContext.DECIMAL128;
        MathContext mc = new MathContext(5, RoundingMode.HALF_EVEN);

        BigDecimal[] v0 = new BigDecimal[] {
            new BigDecimal(tri.coords[0], mc), new BigDecimal(tri.coords[1], mc), new BigDecimal(tri.coords[2], mc)};
        BigDecimal[] v1 = new BigDecimal[] {
            new BigDecimal(tri.coords[3], mc), new BigDecimal(tri.coords[4], mc), new BigDecimal(tri.coords[5], mc)};
        BigDecimal[] v2 = new BigDecimal[] {
            new BigDecimal(tri.coords[6], mc), new BigDecimal(tri.coords[7], mc), new BigDecimal(tri.coords[8], mc)};

//        BigDecimal vsize = new BigDecimal(voxelSize, mc);
        double vs = 0.0003;
        BigDecimal vsize = new BigDecimal("0.0003", mc);
        BigDecimal vsize1 = new BigDecimal(vs, mc);

System.out.println("string: " + vsize + " double: " + vsize1);
        BigDecimal zero = new BigDecimal(0,mc);

        if ((v0[0].compareTo(v1[0]) == 0) && (v0[0].compareTo(v2[0]) == 0)) {
            if (v0[0].remainder(vsize).compareTo(zero) == 0) {
System.out.println("x_aligned");
                x_aligned = true;
                cnt++;
            }
        }

        if ((v0[1].compareTo(v1[1]) == 0) && (v0[1].compareTo(v2[1]) == 0)) {
System.out.println("y equals.  Val: " + v0[1] + " vs: " + vsize + " div: " + (v0[1].divide(vsize,mc)));
System.out.println("Remainder: " + v0[1].remainder(vsize));
            if (v0[1].remainder(vsize).compareTo(zero) == 0) {
System.out.println("y_aligned");
                y_aligned = true;
                cnt++;
            }
        }

        if ((v0[2].compareTo(v1[2]) == 0) && (v0[2].compareTo(v2[2]) == 0)) {
            if (v0[2].remainder(vsize).compareTo(zero) == 0) {
System.out.println("z_aligned");
                z_aligned = true;
                cnt++;
            }
        }

        if (cnt == 0)
            return null;

        int[] ret_val = new int[cnt];
        cnt = 0;

        if (x_aligned)
            ret_val[cnt++] = 0;

        if (y_aligned)
            ret_val[cnt++] = 1;

        if (z_aligned)
            ret_val[cnt++] = 2;

System.out.println("ret: " + java.util.Arrays.toString(ret_val));
        return ret_val;

    }

}
