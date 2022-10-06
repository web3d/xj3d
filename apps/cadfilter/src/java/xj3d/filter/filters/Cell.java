/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008 - 2009
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
import java.util.HashSet;
import java.util.Set;

// Local imports
import org.web3d.util.IntArray;

/**
 * Base implementation of a vertex partitioning octree cell.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
class Cell {

    /** Default Epsilon value for comparing vertices for equality */
    public static float EPSILON_DEFAULT = 0.0000000001f;

    /** Epsilon value for comparing vertices for equality */
    protected static float epsilon;

    /** The minimum coordinates of this Cell's bounding box */
    float[] min;

    /** The maximum coordinates of this Cell's bounding box */
    float[] max;

    /** The minimum coordinates of this Cell's bounding box minus the epsilon */
    float[] emin;

    /** The maximum coordinates of this Cell's bounding box plus the epsilon */
    float[] emax;

    /** The children of this Cell. */
    Cell[] children;

    /** The vertex coordinates and working objects for the CoordinateProcessor. */
    VertexData data;

    /** The vertex indices contained in this Cell */
    IntArray containedIndicesArray;

    /**
     * The identifier of the subdivision level that contains data.
     * 'higher', branch levels will contain only children Cells
     */
    int leaf_level;

    /** The identifier of this subdivision level */
    int level;

    /** Flag indicating this is a branch, not a leaf (i.e. the data level) */
    boolean isBranch;

    /** The identifier of the location of this Cell within it's parent */
    int cell_index;

    /**
     * Default Constructor
     */
    Cell() {
        epsilon = EPSILON_DEFAULT;
    }

    /**
     * Constructor
     *
     * @param data The vertex coordinates and working collection objects.
     * @param parent The parent Cell of this.
     * @param index The identifier of the location of this Cell within it's parent.
     * @param min The minimum bounds assigned to this Cell.
     * @param max The maximum bounds assigned to this Cell.
     * @param eps The epsilon value for comparing coordinates
     */
    Cell(VertexData data, Cell parent, int index, float[] min, float[] max) {
        this.data = data;
        cell_index = index;

        leaf_level = parent.leaf_level;

        level = parent.level + 1;
        isBranch = level != leaf_level;
        this.min = new float[]{min[0], min[1], min[2]};
        this.max = new float[]{max[0], max[1], max[2]};

        this.emin = new float[]{min[0]-epsilon, min[1]-epsilon, min[2]-epsilon};
        this.emax = new float[]{max[0]+epsilon, max[1]+epsilon, max[2]+epsilon};
    }

    /**
     * Add the vertex specified by the argument index to the appropriate
     * Cell if the vertex is contained within this Cell's bounds.
     *
     * @param index The index of the vertex within the coord array
     * to process.
     * @return true if the vertex specified by the index argument is
     * contained within this cell, false otherwise.
     */
    protected boolean add(int index) {
        float[] coord = data.coord;

        int idx = index * 3;
        float x = coord[idx];
        float y = coord[idx+1];
        float z = coord[idx+2];

        boolean isContained =
            x >= min[0] && x <= max[0] &&
            y >= min[1] && y <= max[1] &&
            z >= min[2] && z <= max[2];

        boolean isWithinEpsilon =
            x >= emin[0] && x <= emax[0] &&
            y >= emin[1] && y <= emax[1] &&
            z >= emin[2] && z <= emax[2];

        if (isContained || isWithinEpsilon) {
            if (isBranch) {
                if (children == null) {
                    createChildren();
                }
                for (int i = 0; i < 8; i++) {
                    if (children[i].add(index)) {
                        break;
                    }
                }
            } else {
                if (containedIndicesArray == null) {
                    containedIndicesArray = new IntArray();
                    containedIndicesArray.add(index);
                } else {
                    boolean duplicate_found = false;
                    for (int i = 0; i < containedIndicesArray.size(); i++) {

                        int vertex_idx = containedIndicesArray.get(i);
                        int coord_idx = vertex_idx * 3;

                        float dx = coord[coord_idx] - x;
                        float dy = coord[coord_idx+1] - y;
                        float dz = coord[coord_idx+2] - z;

                        // Must use sqrt to align with grid spacing distances
                        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                        if (d < epsilon) {
                            if (index == vertex_idx) {
                                continue;
                            }
                            data.dupIndices.add(index);

                            // aggregate the indices of redundant coordinates from the
                            // data.coord array into a HashSet - stored in a map keyed
                            // by the index of the first occurance of that vertex. the
                            // redundant indices (from the HashSet) will eventually be
                            // replaced by the key

                            Set<Integer> dup_set = data.indexMap.get(vertex_idx);
                            if (dup_set == null) {
                                dup_set = new HashSet<>();
                                data.indexMap.put(vertex_idx, dup_set);
                            }
                            dup_set.add(index);
                            duplicate_found = true;
                            break;
                        }
                    }
                    if (!duplicate_found) {
                        containedIndicesArray.add(index);
                }
            }
        }
        }
        return(isContained);
    }

    /**
     * Create the children Cells.
     * <br>
     * <blockquote>
     * <pre>
     *    Top        Bottom
     * ---------    ---------
     * | 1 | 3 |    | 5 | 7 |
     * ---------    ---------
     * | 0 | 2 |    | 4 | 6 |
     * ---------    ---------
     * </pre>
     * </blockquote>
     */
    protected void createChildren() {

        children = new Cell[8];

        float[] center = new float[3];
        center[0] = (max[0] + min[0]) * 0.5f;
        center[1] = (max[1] + min[1]) * 0.5f;
        center[2] = (max[2] + min[2]) * 0.5f;

        float[] cmin = new float[3];
        float[] cmax = new float[3];

        /////////////////////////////////////////////////
        cmin[0] = min[0];
        cmin[1] = center[1];
        cmin[2] = center[2];

        cmax[0] = center[0];
        cmax[1] = max[1];
        cmax[2] = max[2];

        children[0] = new Cell(data, this, 0, cmin, cmax);
        /////////////////////////////////////////////////
        cmin[0] = min[0];
        cmin[1] = center[1];
        cmin[2] = min[2];

        cmax[0] = center[0];
        cmax[1] = max[1];
        cmax[2] = center[2];

        children[1] = new Cell(data, this, 1, cmin, cmax);
        /////////////////////////////////////////////////
        children[2] = new Cell(data, this, 2, center, max);
        /////////////////////////////////////////////////
        cmin[0] = center[0];
        cmin[1] = center[1];
        cmin[2] = min[2];

        cmax[0] = max[0];
        cmax[1] = max[1];
        cmax[2] = center[2];

        children[3] = new Cell(data, this, 3, cmin, cmax);
        /////////////////////////////////////////////////
        cmin[0] = min[0];
        cmin[1] = min[1];
        cmin[2] = center[2];

        cmax[0] = center[0];
        cmax[1] = center[1];
        cmax[2] = max[2];

        children[4] = new Cell(data, this, 4, cmin, cmax);
        /////////////////////////////////////////////////
        children[5] = new Cell(data, this, 5, min, center);
        /////////////////////////////////////////////////
        cmin[0] = center[0];
        cmin[1] = min[1];
        cmin[2] = center[2];

        cmax[0] = max[0];
        cmax[1] = center[1];
        cmax[2] = max[2];

        children[6] = new Cell(data, this, 6, cmin, cmax);
        /////////////////////////////////////////////////
        cmin[0] = center[0];
        cmin[1] = min[1];
        cmin[2] = min[2];

        cmax[0] = max[0];
        cmax[1] = center[1];
        cmax[2] = center[2];

        children[7] = new Cell(data, this, 7, cmin, cmax);
        /////////////////////////////////////////////////
    }

    /**
     * Suggested epsilon routine that could be more applicable over a wider
     * range of inputs.  Need to test before putting into production.
     *
     * Reference: http://floating-point-gui.de/errors/comparison/
     *
     * @param a Value a
     * @param b Value b
     * @return True if equal
     */
    protected boolean nearlyEqual(float a, float b) {
        //  suggested epsilon = 0.00001?

        if (a == 0.0){
            return Math.abs(b) < epsilon;
        } else {
            return Math.abs((a-b)/a) < epsilon;
        }
    }
}
