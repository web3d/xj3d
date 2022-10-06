/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2009
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

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Data binding for Collada <vcount> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Vcount {
    
    /** String array version of the content */
    private int[] num_vertices_per_poly;
    
    /**
     * Constructor
     * 
     * @param vcount_element The Element
     */
    Vcount(IntContent vcount_element) {
        num_vertices_per_poly = vcount_element.getIntContent();
    }
    
    /**
     * Return the array containing the number of vertices per polygon
     *
     * @return the array containing the number of vertices per polygon
     */
    int[] getVerticesPerPoly() {
        return(num_vertices_per_poly);
    }
    
    /**
     * Return the number of polygons described by this
     *
     * @return the number of polygons described by this
     */
    int getNumPolys() {
        return(num_vertices_per_poly.length);
    }
    
    /**
     * Return the total number of vertices described by this
     *
     * @return the total number of vertices described by this
     */
    int getNumVertices() {
        int total = 0;
        for (int i = 0; i < num_vertices_per_poly.length; i++) {
            total += num_vertices_per_poly[i];
        }
        return(total);
    }
}
