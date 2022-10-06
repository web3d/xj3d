/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
// None

// Local imports
// none

/**
 * Container for field - type mappings.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class FieldInfo {
	
	/** The field name */
	public final String name;
	
	/** The data type of the field */
	public final int type;
	
	public FieldInfo(String name, int type) {
		this.name = name;
		this.type = type;
	}
}
