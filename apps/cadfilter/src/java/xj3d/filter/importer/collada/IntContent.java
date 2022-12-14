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


/**
 * Defines the reauirements of a CElement that contains integer
 * values in it's text content.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
interface IntContent {
	
	/**
	 * Return the content of the Element
	 *
	 * @return The content of the Element
	 */
	public int[] getIntContent();
}
