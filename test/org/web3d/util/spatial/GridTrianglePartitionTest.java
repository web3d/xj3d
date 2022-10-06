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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import org.web3d.BaseTestCase;


/**
 * Tests the functionality of the GridTrianglePartition
 *
 * @author Alan Hudson
 * @version
 */
public class GridTrianglePartitionTest extends BaseTestCase {
    private static final float EPS = 0.001f;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(GridTrianglePartitionTest.class);
    }

    public void testTunnelOnGrid() {
        GridTrianglePartition grid = new GridTrianglePartition(0.6f, 8,1);

        float[] coords = new float[] {

            -0.5f, -1.0f, -1.0f,
            -0.5f, 1.0f, -1.0f,
            0.5f, -1.0f, -1.0f
        };

        Triangle tri = new Triangle(coords,0);

        grid.insert(tri, true);

        grid.printStats();
/*
        Triangle tri2 = new Triangle(coords2,1);
        grid.insert(tri2, true);
*/
    }

/*
    public void testAAAWorldCoords() {
        int gridSize = 10;
        int half = gridSize / 2;
        GridTrianglePartition grid = new GridTrianglePartition(0.25f, gridSize);

        int[] coords = new int[3];
        float[] vcoords = new float[3];

        // Test on neg grid edge
        grid.findGridCoordsFromWorldCoords(new float[] {-1.25f,-1.25f,-1.25f},coords);
        assertArrayEquals("Origin", new int[] {0,0,0}, coords);
        grid.findVoxelInWorldCoords(coords[0],coords[1],coords[2], vcoords);

        assertTrue(vcoords[0] > -1.25f);
        assertTrue(vcoords[0] < -1f);
        assertTrue(vcoords[1] > -1.25f);
        assertTrue(vcoords[1] < -1f);
        assertTrue(vcoords[2] > -1.25f);
        assertTrue(vcoords[2] < -1f);

        // Test near edge
        grid.findGridCoordsFromWorldCoords(new float[] {-1.125f,-1.125f,-1.125f},coords);
        assertArrayEquals("Origin", new int[] {0,0,0}, coords);


        // Test origin
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,0},coords);
        assertArrayEquals("Origin", new int[] {5,5,5}, coords);


        // Test origin
        grid.findGridCoordsFromWorldCoords(new float[] {0.1f,0.1f,0.1f},coords);
        assertArrayEquals("Origin", new int[] {5,5,5}, coords);
        grid.findVoxelInWorldCoords(coords[0],coords[1],coords[2], vcoords);

        assertTrue(vcoords[0] > 0);
        assertTrue(vcoords[0] < 0.25f);
        assertTrue(vcoords[1] > 0f);
        assertTrue(vcoords[1] < 0.25f);
        assertTrue(vcoords[2] > 0f);
        assertTrue(vcoords[2] < 0.25f);


        // Test on pos grid edge
        grid.findGridCoordsFromWorldCoords(new float[] {1.25f,1.25f,1.25f},coords);
        assertArrayEquals("Origin", new int[] {9,9,9}, coords);
        grid.findVoxelInWorldCoords(coords[0],coords[1],coords[2], vcoords);

        assertTrue(vcoords[0] > 1);
        assertTrue(vcoords[0] < 1.25f);
        assertTrue(vcoords[1] > 1f);
        assertTrue(vcoords[1] < 1.25f);
        assertTrue(vcoords[2] > 1f);
        assertTrue(vcoords[2] < 1.25f);

    }

    public void testTunnelOnGrid() {
        GridTrianglePartition grid = new GridTrianglePartition(0.5f, 8);

        float[] coords = new float[] {

            -0.5f, -1.0f, -1.0f,
            -0.5f, 1.0f, -1.0f,
            0.5f, -1.0f, -1.0f
        };

        float[] coords2 = new float[] {
            -0.5f, -1.0f, 1.0f,
            0.5f, -1.0f, 1.0f,
            -0.5f, 1.0f, 1.0f
        };

        Triangle tri = new Triangle(coords,0);

        grid.insert(tri, true);

        Triangle tri2 = new Triangle(coords2,1);
        grid.insert(tri2, true);

        double rayX = -0.25;
        double rayY = -0.75;

        int sloc1 = grid.findGridCoordsFromWorldCoords((float)rayX);
        int sloc2 = grid.findGridCoordsFromWorldCoords((float)rayY);

System.out.println(rayY + " sloc1: " + sloc1 + " sloc2: " + sloc2);
        TunnelRegion region = new TunnelRegion(TunnelRegion.Axis.Z,sloc1,sloc2,1);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(2, list.size());

System.out.println("list: " + list);

    }

    public void testCalcBoundsForTriangle() {
        GridTrianglePartition grid = new GridTrianglePartition(10, 16);

        float[] coords = new float[] {0,0,0,2,8,-3,4,0,-4};

        Triangle tri = new Triangle(coords,0);

        float[] min = new float[3];
        float[] max = new float[3];

        grid.calcBoundsForTriangle(tri,min,max);

        assertArrayEquals("Min array correct",new float[] {0,0,-4},min,EPS);
        assertArrayEquals("Max array correct",new float[] {4,8,0},max,EPS);
    }

    public void testFindGridCoordsFromWorldCoords1() {
        int gridSize = 4;
        int half = gridSize / 2;
        GridTrianglePartition grid = new GridTrianglePartition(1f, gridSize);

        int[] coords = new int[3];

        grid.findGridCoordsFromWorldCoords(new float[] {-1f,0,0},coords);
        //System.out.println("coords0 are: " + java.util.Arrays.toString(coords));

        grid.findGridCoordsFromWorldCoords(new float[] {-0.75f,0,0},coords);
        System.out.println("coords1 are: " + java.util.Arrays.toString(coords));
        //assertArrayEquals("Origin", new int[] {1,2,2}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {-0.4f,0,0},coords);
        //System.out.println("coords2 are: " + java.util.Arrays.toString(coords));

        grid.findGridCoordsFromWorldCoords(new float[] {-0.5f,0,0},coords);
        //System.out.println("coords3 are: " + java.util.Arrays.toString(coords));

    }

    public void testFindGridCoordsFromWorldCoords() {
        int gridSize = 16;
        int half = gridSize / 2;
        GridTrianglePartition grid = new GridTrianglePartition(1.5f, gridSize);

        int[] coords = new int[3];

        grid.findGridCoordsFromWorldCoords(new float[] {0,0,0},coords);
        assertArrayEquals("Origin", new int[] {8,8,8}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {0.5f,0,0},coords);
        assertArrayEquals("Low Side", new int[] {8,8,8}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {1.49f,0,0},coords);
        assertArrayEquals("High Side", new int[] {8,8,8}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {1.5f,0,0},coords);
        assertArrayEquals("Equals", new int[] {9,8,8}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {1.75f,1.75f,1.75f},coords);
        assertArrayEquals("Non Zero", new int[] {9,9,9}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {-3.2f,-3.2f,-3.25f},coords);
        assertArrayEquals("Negative", new int[] {6,6,6}, coords);

        grid.findGridCoordsFromWorldCoords(new float[] {-12f,-12f,-12f},coords);
        assertArrayEquals("Negative", new int[] {0,0,0}, coords);
    }

    public void testGetObjectsCellRegion1() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 20);

        float[] coords = new float[] {0,0,0,2,8,-3,4,0,-4};
        Triangle tri = new Triangle(coords,0);

        grid.insert(tri, true);

        // Test verts of triangle

        CellRegion region = new CellRegion(10,10,10);

        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 2,10 + 8,10 - 3);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 4,10 + 0,10 - 4);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);


        // Spot check insides
        region = new CellRegion(10 + 0,10 + 8,10 + 0);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 4,10 + 8,10 + 0);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 0,10 + 8,10 -3);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 4,10 + 8,10 - 3);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        region = new CellRegion(10 + 2,10 + 3,10 - 1);

        list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        // Spot Regions it shouldn't be in
        region = new CellRegion(19,19,19);

        list = grid.getObjects(region);

        assertNull(list);

        // Spot Regions it shouldn't be in
        region = new CellRegion(15,10,10);

        list = grid.getObjects(region);

        assertNull(list);

    }

    public void testGetObjectsCellRegion2() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 20);

        float[] coords = new float[] {0,0,0,2,8,-3,4,0,-4};
        Triangle tri1 = new Triangle(coords,0);

        coords = new float[] {2,4,0,9,2,0,9,6,0};
        Triangle tri2 = new Triangle(coords,1);

        Triangle[] tris = new Triangle[] {tri1,tri2};

        grid.insert(tris, true);

        // Test overlap area

        CellRegion region = new CellRegion(10 + 2,10 + 4,10 + 0);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 2);
    }

    public void testGetObjectsDuplicateTriangles() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 20);

        float[] coords = new float[] {0,0,0,2,8,-3,4,0,-4};
        Triangle tri1 = new Triangle(coords,0);


        grid.insert(tri1, true);
        grid.insert(tri1, true);

        // Test verts of triangle

        CellRegion region = new CellRegion(10,10,10);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);
    }

    public void testGetObjectsSliceRegionX() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 20);

        float[] coords = new float[] {0,0,0,0,4,2,0,0,4};
        Triangle tri1 = new Triangle(coords,0);

        coords = new float[] {0,2,4,0,9,2,0,9,6};
        Triangle tri2 = new Triangle(coords,1);

        Triangle[] tris = new Triangle[] {tri1,tri2};

        grid.insert(tris, true);

        int[] loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,0}, loc);

        // Test x slice contains data

        SliceRegion region = new SliceRegion(SliceRegion.Axis.X,loc[0],1);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 2);

        loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,0}, loc);

        // Test x slice, no data

        region = new SliceRegion(SliceRegion.Axis.X,loc[0] + 1,1);
        list = grid.getObjects(region);

        assertNull(list);
    }

    public void testGetObjectsSliceRegionY() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 10);

        float[] coords = new float[] {-2,1,-2,1,1,0,-2,1,1};
        Triangle tri1 = new Triangle(coords,0);

        Triangle[] tris = new Triangle[] {tri1};

        grid.insert(tris, true);

        int[] loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,1,0}, loc);

        // Test x slice contains data

        SliceRegion region = new SliceRegion(SliceRegion.Axis.Y,loc[1],1);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,0}, loc);

        // Test x slice, no data

        region = new SliceRegion(SliceRegion.Axis.Y,loc[1],1);
        list = grid.getObjects(region);

        assertNull(list);
    }

    public void testGetObjectsSliceRegionZ() {
        GridTrianglePartition grid = new GridTrianglePartition(1, 10);

        float[] coords = new float[] {-2,-2,-2,1,0,-2,-2,1,-2};
        Triangle tri1 = new Triangle(coords,0);

        Triangle[] tris = new Triangle[] {tri1};

        grid.insert(tris, true);

        int[] loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,-2}, loc);

        // Test x slice contains data

        SliceRegion region = new SliceRegion(SliceRegion.Axis.Z,loc[2],1);
        Set<Object> list = grid.getObjects(region);

        assertNotNull(list);
        assertEquals(list.size(), 1);

        loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,-5}, loc);

        // Test x slice, no data

        region = new SliceRegion(SliceRegion.Axis.Z,loc[2],3);
        list = grid.getObjects(region);

        assertNull(list);

        loc = new int[3];
        grid.findGridCoordsFromWorldCoords(new float[] {0,0,-1}, loc);

        // Test x slice, no data

        region = new SliceRegion(SliceRegion.Axis.Z,loc[2],6);
        list = grid.getObjects(region);

        assertNull(list);
    }
*/
}
