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
 * Utility to perform a manifold check on an indexed triangle set.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class ValidateITSData {
	
	/** The root of the edge oct tree */
	private EdgeCellRoot root;
	
	/**
	 * Constructor
	 *
	 * @param data The triangle data
	 */
	ValidateITSData(ITSData data) {
		root = new EdgeCellRoot(data);
	}
	
	/**
	 * Perform a check on the triangle data
	 *
	 * @return true if the data represents a valid manifold mesh,
	 * false otherwise.
	 */
	public boolean check() {
		return(root.check());
	}
}
