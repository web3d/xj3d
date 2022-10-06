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
import java.util.HashMap;

import org.xml.sax.Attributes;

// Local imports

/**
 * Factory for producing CElement instances.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class CElementFactory {
    
	/** Map of element content types, keyed by element name */
	private HashMap<String, Object> typeMap;
	
    /**
     * Constructor
     */
    CElementFactory() {
		typeMap = new HashMap<>();
		typeMap.put(ColladaStrings.FLOAT_ARRAY, (float) 0);
		typeMap.put(ColladaStrings.FLOAT, (float) 0);
		typeMap.put(ColladaStrings.COLOR, (float) 0);
		typeMap.put(ColladaStrings.LOOKAT, (float) 0);
		typeMap.put(ColladaStrings.MATRIX, (float) 0);
		typeMap.put(ColladaStrings.ROTATE, (float) 0);
		typeMap.put(ColladaStrings.SCALE, (float) 0);
		typeMap.put(ColladaStrings.SKEW, (float) 0);
		typeMap.put(ColladaStrings.TRANSLATE, (float) 0);
		typeMap.put(ColladaStrings.INT_ARRAY, 0);
		typeMap.put(ColladaStrings.P, 0);
		typeMap.put(ColladaStrings.VCOUNT, 0);
    }
    
    /**
     * Return the CElement per the argument name
     *
     * @param tag_name The tag name of the Element
	 * @param atts The Attributes of the Element
     */
    CElement getCElement(String tag_name, Attributes atts) {
        
        CElement ce = null;
		Object type = typeMap.get(tag_name);
		if (type == null) {
			ce = new CElementBase(tag_name, atts);
		} else if (type instanceof Float) {
			ce = new CElementFloat(tag_name, atts);
		} else if (type instanceof Integer) {
			ce = new CElementInt(tag_name, atts);
		}
        return(ce);
    }
}
