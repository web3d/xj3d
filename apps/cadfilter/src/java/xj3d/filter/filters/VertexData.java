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
import java.util.Set;

import org.j3d.util.IntHashMap;

// Local imports
// None

/**
 * Container for the vertex coordinates and associated collection objects
 * used by the CoordinateProcessor to reindex the geometry vertices.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class VertexData {

    /**
     * The array of vertex coordinates
     */
    final float[] coord;

    /**
     * The full set of vertex indices that have been determined to be redundant
     */
    final Set<Integer> dupIndices;

    /**
     * Map of vertex indices from the original coordinate array. key = index of
     * initial occurrence of a vertex that is repeated. value =
     * HashSet<Integer>, containing subsequent indices of an equivalent vertex
     */
    final IntHashMap<Set<Integer>> indexMap;

    /**
     * Constructor
     */
    VertexData(float[] coord, Set<Integer> dupIndices, IntHashMap<Set<Integer>> indexMap) {
        this.coord = coord;
        this.dupIndices = dupIndices;
        this.indexMap = indexMap;
    }
}
