/*****************************************************************************
 *                        xj3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters.manifold;

/**
 * Container for indexed triangle set data
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class ITSData {
	
	/** The coordinates */
	float[] coord;
	
	/** The indices */
	int[] index;
	
	/** The coordinate bounds, [x_min, x_max, y_min, y_max, z_min, z_max] */
	float[] bound;
	
	/**
	 * Constructor
	 *
	 * @param coord The coordinates
	 * @param index The indices
	 * @param bound The coordinate bounds, [x_min, x_max, y_min, y_max, z_min, z_max].
	 * If null, the bounds will be calculated.
	 */
	ITSData(float[] coord, int[] index, float[] bound) {
		this.coord = coord;
		this.index = index;
		if (bound != null) {
			this.bound = bound;
		} else {
			this.bound = getBounds(coord);
		}
	}
	
    /**
     * Return the bounds of the geometry. An array containing
     * [x_min, x_max, y_min, y_max, x_min, z_max].
     *
     * @param coord The coordinates of the geometry
     * @return The bounds of the geometry
     */
    private float[] getBounds(float[] coord) {

		float x = coord[0];
		float x_min = x;
		float x_max = x;
		
        float y = coord[1];
		float y_min = y;
		float y_max = y;
		
        float z = coord[2];
		float z_min = z;
		float z_max = z;
		
		int num_vtx = coord.length / 3;
        for (int i = 1; i < num_vtx; i++) {
			
            x = coord[i * 3];
            y = coord[i * 3 + 1];
            z = coord[i * 3 + 2];

            if (x < x_min) {
                x_min = x;
			} else if (x > x_max) {
                x_max = x;
            }
            if (y < y_min) {
                y_min = y;
			} else if (y > y_max) {
                y_max = y;
            }
            if (z < z_min) {
                z_min = z;
			} else if (z > z_max) {
                z_max = z;
            }
        }
        float[] bounds = new float[] {x_min, x_max, y_min, y_max, z_min, z_max};
        return(bounds);
    }
}
