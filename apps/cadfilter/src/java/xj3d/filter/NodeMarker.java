/*****************************************************************************
 *                        Web3d.org Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
// none

// Local imports
// none

/**
 * A container for placekeeping node and field data
 * streamed through a filter.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class NodeMarker {
	
	/** The name of the node */
	public final String nodeName;
	
	/** The name of the currently active field. If no field
	 *  is currently active, this will be null. */
	public String fieldName;
	
	/** The type of the currently active field. If no field
	 *  is currently active, this will be -1 */
	public int fieldType;

	/** 
	 * Constructor 
	 * 
	 * @param nodeName The name of the node that this marker represents
	 */
	public NodeMarker(String nodeName) {
		this(nodeName, null, -1);
	}
	
	/** 
	 * Constructor 
	 * 
	 * @param nodeName The name of the node that this marker represents
	 * @param fieldName The name of the currently active field
	 * @param fieldType The type of the currently active field
	 */
	public NodeMarker(String nodeName, String fieldName, int fieldType) {
		this.nodeName = nodeName;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}
	
	/**
	 * Set the field data.
	 *
	 * @param fieldName The name of the currently active field
	 * @param fieldType The type of the currently active field
	 */
	public void setFieldData(String fieldName, int fieldType) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}
	
	/**
	 * Clear the field data
	 */
	public void clearFieldData() {
		fieldName = null;
		fieldType = -1;
	}
	
	/**
	 * Return the node name
	 *
	 * @return The node name
	 */
	public String getNodeName() {
		return(nodeName);
	}
	
	/**
	 * Return the field name
	 *
	 * @return The currently active field name
	 */
	public String getFieldName() {
		return(fieldName);
	}
	
	/**
	 * Return the field type
	 *
	 * @return The currently active field type
	 */
	public int getFieldType() {
		return(fieldType);
	}
	
	/**
	 * Return a String representation of this
	 *
	 * @return A String representation of this
	 */
        @Override
	public String toString() {
		return("node = "+ nodeName +", field = "+ fieldName +", type = "+ fieldType);
	}
}
