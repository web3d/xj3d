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
 * The root oct-tree cell for spatial subdivision of triangle edges
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class EdgeCellRoot extends EdgeCell {

	/** The maximum number of levels to the tree */
	static final int DEFAULT_MAX_LEVEL = 5;
	
    /**
     * Constructor
	 *
	 * @param data The triangle coordinates, indices and bounds.
     */
    EdgeCellRoot(ITSData data) {
		super(data);
        level = 0;
		max_level = DEFAULT_MAX_LEVEL;
        
		this.min = new float[]{data.bound[0], data.bound[2], data.bound[4]};
		this.max = new float[]{data.bound[1], data.bound[3], data.bound[5]};

        int num_tri = data.index.length / 3;
		int idx, i0, i1, i2;
		int[] index = data.index;
        for (int t = 0; t < num_tri; t++) {
			
			idx = t * 3;
			i0 = index[idx];
			i1 = index[idx+1];
			i2 = index[idx+2];
			
            if (!add(i0, i1)) {
				throw new IllegalArgumentException("Out Of Bounds: "+ t);
			}
			if (!add(i1, i2)) {
				throw new IllegalArgumentException("Out Of Bounds: "+ t);
			}
			if (!add(i2, i0)) {
				throw new IllegalArgumentException("Out Of Bounds: "+ t);
			}
        }
    }
}
