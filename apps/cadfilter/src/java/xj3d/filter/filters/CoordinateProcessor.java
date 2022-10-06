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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;

// Local imports
// None

/**
 * Package level class that will compact a coordinate array to
 * eliminate redundant vertices and reconfigure the
 * associated index array to account for the modifications to the
 * coordinate array. Used with the ReindexFilter.
 *
 * @author Rex Melton
 * @version $Revision: 1.7 $
 */
class CoordinateProcessor {

    /** The number of valid (non-redundant) vertices in the array */
    private int num_vertex;

    /** The full set of vertex indices that have been determined to be redundant */
    private Set<Integer> dupIndices;

    /**
     * Map of vertex indices from the original coordinate array.
     * key = index of initial occurrence of a vertex that is repeated.
     * value = HashSet<Integer>, containing subsequent indices of an
     *  equivalent vertex
     */
    private IntHashMap<Set<Integer>> indexMap;

    /** An array that contains the 'left shift' of vertex indices to
     * account for the removal of redundant vertices. */
    private int[] shift;

    /** An array of index values containing the replacement indices for
     * indices that pointed to redundant vertices. */
    private int[] replacement;

    /** The error handler for sending out messages */
    private ErrorReporter errorReporter;

    /**
     * Construct an instance of the processor that works on the given
     * set of base points.
     *
     * @param coordPoint The list of points to process
     * @param reporter Logging interface
     */
    CoordinateProcessor(float[] coordPoint, ErrorReporter reporter, float epsilon) {
        errorReporter = reporter;

        long start_time = System.currentTimeMillis();
        errorReporter.messageReport("Starting coords");

        dupIndices = new HashSet<>();
        indexMap = new IntHashMap<>();

        num_vertex = coordPoint.length/3;

        ///////////////////////////////////////////////////////////////////////////
        // the octree coordinate parser will walk through the array of vertex
        // coordinates and extract the indices of the vertices that are redundant
        // into the dupIndices HashSet, and will populate the indexMap with
        // HashSets of the indices of the redundant vertices keyed by the index of
        // the initial occurance of the equivalent vertex
        VertexData vdat = new VertexData(coordPoint, dupIndices, indexMap);
        RootCell root = new RootCell(vdat, epsilon);
        ///////////////////////////////////////////////////////////////////////////

        long mid_time = System.currentTimeMillis();

        if (dupIndices.size() > 0) {

            // there were duplicates
            Integer[] sortedDupes = dupIndices.toArray(new Integer[dupIndices.size()]);

            // get a sequential ordering of the duplicate indices
            Arrays.sort(sortedDupes);

            int coord_idx = 0;
            int dup_idx = 0;

            shift = new int[num_vertex];
            replacement = new int[num_vertex];

            // compact the coordinate array to overwrite redundant points
            for (int i = 0; i < num_vertex; i++) {
                if ((dup_idx < sortedDupes.length) && (i == sortedDupes[dup_idx])) {
                    dup_idx++;
                } else {
                    if (i != coord_idx) {
                        int dst_coord_idx = coord_idx*3;
                        int src_coord_idx = i*3;

                        coordPoint[dst_coord_idx] = coordPoint[src_coord_idx];
                        coordPoint[dst_coord_idx+1] = coordPoint[src_coord_idx+1];
                        coordPoint[dst_coord_idx+2] = coordPoint[src_coord_idx+2];
                    }

                    coord_idx++;
                }
                // initialize the data to be used to reconstruct the index
                shift[i] = dup_idx;
                replacement[i] = i;
            }
            num_vertex -= sortedDupes.length;
        }

        long end_time = System.currentTimeMillis();

        errorReporter.messageReport("Startup time:  ");
        errorReporter.messageReport("  coords matching " +
                                    (mid_time - start_time) + "ms");
        errorReporter.messageReport("  shuffling " +
                                    (end_time - mid_time) + "ms");
    }

    /**
     * Return whether the coordinate array has been compacted
     *
     * @return whether the coordinate array has been compacted
     */
    boolean hasDuplicates() {
        return dupIndices.size() > 0;
    }

    /**
     * Return the number of non-redundant vertices in the array
     *
     * @return the number of non-redundant vertices in the array
     */
    int getNumCoords() {
        return num_vertex;
    }

    /**
     * Rebuild the indices in the argument array to correspond to the
     * compacted vertex coordinate array.
     *
     * @param index An array of indices into the vertex coordinate array
     * @return true if the array has been modified, false if it remains unchanged
     */
    boolean processIndices(int[] index) {

        errorReporter.messageReport("processIndices");

        if (dupIndices.isEmpty())
            return false;

        long start_time = System.currentTimeMillis();

        // complete the initialization of the replacement indices array
        int[] key_idx = indexMap.keySet();
        int key;
        Set<Integer> redundants;
        for (int i = 0; i < key_idx.length; i++) {
            key = key_idx[i];

            redundants = indexMap.get(key);
            for (int idx : redundants) {
                replacement[idx] = key;
            }
        }

        // walk through the array of indices
        int orig_value;
        int rep_value;
        for (int i = 0; i < index.length; i++) {

            // get the original index
            orig_value = index[i];

            // find it's replacement
            rep_value = replacement[orig_value];

            // modify the replacement to account for the coordinate array being compacted
            index[i] = rep_value - shift[rep_value];
        }

        errorReporter.messageReport("  Reindex time " +
                                    (System.currentTimeMillis() - start_time) +
                                    "ms");
        return true;
    }
}
