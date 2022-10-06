/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer.collada;

// External imports
// None

// Local imports
import org.web3d.util.I18nUtils;

/**
 * Index generator.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
abstract class Indexer {

    /**
     * Return indices for <triangles>.
     *
     * @param num_triangles The number of triangles
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getTrianglesIndices(int[] p_indices, int num_triangles, int initial_offset, int index_offset) {
        int num_indices = num_triangles * 3;
        int[] indices = new int[num_indices];
        int p_index = initial_offset;
        for (int i = 0; i < num_indices; i++) {
            indices[i] = p_indices[p_index];
            p_index += index_offset;
        }
        return(indices);
    }

    /**
     * Return indices for <triangles> that will
     * be transformed to IndexedFaceSets.
     *
     * @param num_triangles The number of triangles
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getPolyIndices(int[] p_indices, int num_triangles, int initial_offset, int index_offset) {
        int num_indices = num_triangles * 4;
        int[] indices = new int[num_indices];
        int p_index = initial_offset;
        for (int i = 0; i < num_indices;) {
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = -1;
        }
        return(indices);
    }

    /**
     * Return indices for <trifans> that will
     * be transformed to IndexedFaceSets.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getPolyIndicesForTrifan(int[] p_indices, int initial_offset, int index_offset) {
        int num_triangles = (p_indices.length / index_offset) - 2;
        int num_indices = num_triangles * 4;
        int[] indices = new int[num_indices];
        int center = initial_offset;
        int v_index = initial_offset + index_offset;
        int index = 0;
        for (int i = 0; i < num_triangles; i++) {
            indices[index++] = p_indices[center];
            indices[index++] = p_indices[v_index];
            v_index += index_offset;
            indices[index++] = p_indices[v_index];
            indices[index++] = -1;
        }
        return(indices);
    }

    /**
     * Return indices for <tristrips> that will
     * be transformed to IndexedFaceSets.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getPolyIndicesForTristrip(int[] p_indices, int initial_offset, int index_offset) {
        int num_triangles = (p_indices.length / index_offset) - 2;
        int num_indices = num_triangles * 4;
        int[] indices = new int[num_indices];
        int p_index = initial_offset;
        int index = 0;
        boolean even = true;
        for (int i = 0; i < num_triangles; i++) {
            if (even) {
                indices[index++] = p_indices[p_index];
                p_index += index_offset;
                indices[index++] = p_indices[p_index];
                indices[index++] = p_indices[p_index+index_offset];
            } else {
                indices[index++] = p_indices[p_index+index_offset];
                indices[index++] = p_indices[p_index];
                p_index += index_offset;
                indices[index++] = p_indices[p_index+index_offset];
            }
            indices[index++] = -1;
            even = !even;
        }
        return(indices);
    }

    /**
     * Return indices for <polylist>.
     *
     * @param v The Vcount object, containg poly vertex information
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getPolyIndices(int[] p_indices, Vcount v, int initial_offset, int index_offset) {
        int num_polys = v.getNumPolys();
        int num_indices = v.getNumVertices() + num_polys;
        int[] indices = new int[num_indices];
        int[] num_vertices_per_poly = v.getVerticesPerPoly();
        int v_index = 0;
        int p_index = initial_offset;
        for (int i = 0; i < num_polys; i++) {
            int num_vertices = num_vertices_per_poly[i];
            for (int j = 0; j < num_vertices; j++) {
                indices[v_index++] = p_indices[p_index];
                p_index += index_offset;
            }
            indices[v_index++] = -1;
        }
        return(indices);
    }

    /**
     * Return indices for <lines>.
     *
     * @param num_lines The number of lines
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getLinesIndices(int[] p_indices, int num_lines, int initial_offset, int index_offset) {
        int num_indices = num_lines * 3;
        int[] indices = new int[num_indices];
        int p_index = initial_offset;

        if (num_indices > p_indices.length) {
            num_indices = p_indices.length - 1;

            String err = "xj3d.filter.importer.ColladaFileParser.lineCountWrong";
            I18nUtils.printMsg(err, I18nUtils.EXT_MSG, null);
        }

        for (int i = 0; i < num_indices;) {
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = p_indices[p_index];
            p_index += index_offset;
            indices[i++] = -1;
        }
        return(indices);
    }

    /**
     * Return indices for <linestrips>, <polygons>
     * <trifans> and <tristrips>.
     *
     * @param initial_offset The initial offset into the array
     * @param index_offset The offset between indices in the array
     * @return The array of indices.
     */
    static int[] getIndices(int[] p_indices, int initial_offset, int index_offset) {
        int num_indices = p_indices.length / index_offset;
        int[] indices = new int[num_indices + 1];
        int p_index = initial_offset;
        for (int i = 0; i < num_indices; i++) {
            indices[i] = p_indices[p_index];
            p_index += index_offset;
        }
        indices[num_indices] = -1;
        return(indices);
    }
}
