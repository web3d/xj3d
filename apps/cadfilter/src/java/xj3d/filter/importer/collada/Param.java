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

/**
 * Data binding for Collada <param> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class Param {
    
    /** name attribute */
    String name;
    
    /** is there a name attribute */
    boolean isNamed;
    
    /** the type, "float", "int", "boolean", etc.) */
    String type;
    
    /**
     * Constructor
     * 
     * @param param_element The Element
     */
    Param(CElement param_element) {
        
        name = param_element.getAttribute(ColladaStrings.NAME);
        //isNamed = !name.equals("");
		isNamed = (name != null);
        type = param_element.getAttribute(ColladaStrings.TYPE);
    }
}
