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

// External imports
import java.util.ArrayList;

/**
 * An oct-tree cell for spatial subdivision of triangle edges
 * and subsequent validation of the mesh as manifold.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class EdgeCell {
	
	/** Coordinates and indices of an IndexedTriangleSet */
	ITSData data;
	
	/** The final tree level */
	int max_level;
	
	/** The minimum bounds */
	float[] min;
	
	/** The maximum bounds */
	float[] max;
	
	/** Children cells */
	EdgeCell[] children;
	
	/** The edges contained */
	ArrayList<EdgeData> edge_list;
	
	/** The invalid edges contained */
	//ArrayList<EdgeData> invalid_edge_list;
	
	/** The level */
	int level;
	
	/**
	 * Constructor (for the root EdgeCell)
	 *
	 * @param data The triangle coordinates, indices and bounds.
	 */
	EdgeCell(ITSData data) {
		this.data = data;
	}
	
	/**
	 * Constructor
	 *
	 * @param data The triangle coordinates, indices and bounds.
	 * @param parent The parent.
	 * @param min The minimum bounds of this.
	 * @param max The maximum bounds of this.
	 */
	EdgeCell(ITSData data, EdgeCell parent, float[] min, float[] max) {
		
		this(data);
		this.max_level = parent.max_level;
		
		level = parent.level + 1;
		
		this.min = new float[]{min[0], min[1], min[2]};
		this.max = new float[]{max[0], max[1], max[2]};
	}
	
	/**
	 * Add the edge specified by the argument indicies to this
	 * if the edge is contained within our bounds.
	 *
	 * @param i0 The first edge index
	 * @param i1 The second edge index
	 * @return true if the edge specified by the arguments is
	 * contained within this, false otherwise.
	 */
	boolean add(int i0, int i1) {
		
		float[] coord = data.coord;
		
		int idx = i0 * 3;
		float x0 = coord[idx];
		float y0 = coord[idx+1];
		float z0 = coord[idx+2];
		
		idx = i1 * 3;
		float x1 = coord[idx];
		float y1 = coord[idx+1];
		float z1 = coord[idx+2];
		
		// get the bounds of the edge
		float x_max, x_min, y_max, y_min, z_max, z_min;
		if (x0 > x1) {
			x_max = x0;
			x_min = x1;
		} else {
			x_max = x1;
			x_min = x0;
		}
		if (y0 > y1) {
			y_max = y0;
			y_min = y1;
		} else {
			y_max = y1;
			y_min = y0;
		}
		if (z0 > z1) {
			z_max = z0;
			z_min = z1;
		} else {
			z_max = z1;
			z_min = z0;
		}
		
		boolean isInBounds =
			x_min >= min[0] && x_max <= max[0] &&
			y_min >= min[1] && y_max <= max[1] &&
			z_min >= min[2] && z_max <= max[2];
		
		if (isInBounds) {
			boolean addedToChild = false;
			if (level < max_level) {
				if (children == null) {
					createChildren();
				}
				for (int i = 0; i < 8; i++) {
					if (children[i].add(i0, i1)) {
						addedToChild = true;
						break;
					}
				}
			}
			if (!addedToChild) {
				// add the edge to this 
				if (edge_list == null) {
					edge_list = new ArrayList<>();
					edge_list.add(new EdgeData(1, i0, i1));
				} else {
					boolean duplicate_found = false;
					int num_edge = edge_list.size();
					for (int i = 0; i < num_edge; i++) {
						
						EdgeData ed = edge_list.get(i);
						if (((ed.i0 == i0) && (ed.i1 == i1)) ||
							((ed.i0 == i1) && (ed.i1 == i0))) {
							
							ed.count++;
							duplicate_found = true;
						}
					}
					if (!duplicate_found) {
						edge_list.add(new EdgeData(1, i0, i1));
					}
				}
			}
		}
		return(isInBounds);
	}
	
	/**
	 * Create the children
	 */
	void createChildren() {
		
		children = new EdgeCell[8];
		
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
		
		children[0] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
		cmin[0] = min[0];
		cmin[1] = center[1];
		cmin[2] = min[2];
		
		cmax[0] = center[0];
		cmax[1] = max[1];
		cmax[2] = center[2];
		
		children[1] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
		children[2] = new EdgeCell(data, this, center, max);
		/////////////////////////////////////////////////
		cmin[0] = center[0];
		cmin[1] = center[1];
		cmin[2] = min[2];
		
		cmax[0] = max[0];
		cmax[1] = max[1];
		cmax[2] = center[2];
		
		children[3] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
		cmin[0] = min[0];
		cmin[1] = min[1];
		cmin[2] = center[2];
		
		cmax[0] = center[0];
		cmax[1] = center[1];
		cmax[2] = max[2];
		
		children[4] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
		children[5] = new EdgeCell(data, this, min, center);
		/////////////////////////////////////////////////
		cmin[0] = center[0];
		cmin[1] = min[1];
		cmin[2] = center[2];
		
		cmax[0] = max[0];
		cmax[1] = center[1];
		cmax[2] = max[2];
		
		children[6] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
		cmin[0] = center[0];
		cmin[1] = min[1];
		cmin[2] = min[2];
		
		cmax[0] = max[0];
		cmax[1] = center[1];
		cmax[2] = center[2];
		
		children[7] = new EdgeCell(data, this, cmin, cmax);
		/////////////////////////////////////////////////
	}
	
	/**
	 * Check the edges
	 * 
	 * @return true if each edge has exactly two matching triangles,
	 * false otherwise.
	 */
	boolean check() {
		boolean clean = true;
		if (edge_list != null) {
			int num_edge = edge_list.size();
			for (int i = 0; i < num_edge; i++) {
				
				EdgeData ed = edge_list.get(i);
				if (ed.count != 2) {
					System.out.println("An edge has "+ ed.count +" faces: "+ ed.i0 +", "+ ed.i1);
					clean = false;
					/*
					if (invalid_edge_list == null) {
						invalid_edge_list = new ArrayList<EdgeData>();
					}
					invalid_edge_list.add(ed);
					*/
				}
			}
		}
		if (children != null) {
			int num_children = children.length;
			for (int i = 0; i < num_children; i++) {
				clean &= children[i].check();
			}
		}
		return(clean);
	}
}
