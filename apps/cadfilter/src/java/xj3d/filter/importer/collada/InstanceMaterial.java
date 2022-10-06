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
 * Data binding for Collada <instance_material> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class InstanceMaterial {
    
    /** sid attribute */
    String sid;
    
    /** name attribute */
    String name;
    
    /** target attribute */
    String target;
    
    /** symbol attribute */
    String symbol;
    
    /**
     * Constructor
     * 
     * @param element The Element
     */
    InstanceMaterial(CElement element) {
        
        sid = element.getAttribute(ColladaStrings.SID);
        name = element.getAttribute(ColladaStrings.NAME);
        target = element.getAttribute(ColladaStrings.TARGET);
        symbol = element.getAttribute(ColladaStrings.SYMBOL);
    }
}
