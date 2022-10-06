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
 * Container for edge indices and a use count
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class EdgeData {
	
	/** The count for an edge */
	int count;
	
	/** The first index */
	int i0;
	
	/** The second index */
	int i1;
	
	/**
	 * Constructor
	 */
	EdgeData(int count, int i0, int i1) {
		this.count = count;
		this.i0 = i0;
		this.i1 = i1;
	}
}