/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
// none

// Local imports
// none

/**
 * The root Cell of a vertex partitioning octree.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class RootCell extends Cell {

    /**
     * Constructor
     */
    RootCell(VertexData data, float eps) {
        epsilon = eps;
        this.data = data;
        cell_index = 0;
        level = 0;
        isBranch = true;
        initBounds();

        int num_vertex = data.coord.length / 3;
        calculateLeafLevel(num_vertex);

        createChildren();

        for (int vtx = 0; vtx < num_vertex; vtx++) {
            for (int i = 0; i < 8; i++) {
                if (children[i].add(vtx)) {
                    break;
                }
            }
        }
    }

    /**
     * Calculate the number of subdivisions to use. This currently is
     * bounded to a max of 5 subdivisions which yields 8^5 (i.e. 2^15 or 32678)
     * cells, and a min of 1 subdivision which yields 8 cells;
     *
     * @param num The number of vertices in the coordinate array to be partitioned
     */
    private void calculateLeafLevel(int num) {

        int powerOf2 = 0;

        while ((num >>= 1) > 1) {
            powerOf2++;
        }
        leaf_level = (powerOf2 / 3) - 1;

        if (leaf_level > 5) {
            leaf_level = 5;
        } else if (leaf_level < 1) {
            leaf_level = 1;
        }
    }

    /**
     * Initialize the bounds of the geometry.
     */
    private void initBounds() {

        float[] coord = data.coord;
        float cx, cy, cz;

        float x_min = coord[0];
        float y_min = coord[1];
        float z_min = coord[2];

        float x_max = coord[0];
        float y_max = coord[1];
        float z_max = coord[2];

        int num = coord.length;
        for (int i = 3; i < num;) {

            cx = coord[i++];
            cy = coord[i++];
            cz = coord[i++];

            // get max and min bounds
            if (cx < x_min) {
                x_min = cx;
            } else if (cx > x_max) {
                x_max = cx;
            }
            if (cy < y_min) {
                y_min = cy;
            } else if (cy > y_max) {
                y_max = cy;
            }
            if (cz < z_min) {
                z_min = cz;
            } else if (cz > z_max) {
                z_max = cz;
            }
        }
        min = new float[]{x_min, y_min, z_min};
        max = new float[]{x_max, y_max, z_max};
    }
}
