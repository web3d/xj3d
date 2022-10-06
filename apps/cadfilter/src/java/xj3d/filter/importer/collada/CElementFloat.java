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
import java.util.ArrayList;

import org.xml.sax.Attributes;

// Local imports
import org.web3d.vrml.sav.ImportFileFormatException;

import xj3d.filter.FieldValueHandler;

/**
 * Subclass of CElementBase that contains floating point 
 * values in it's text content.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class CElementFloat extends CElementBase implements FloatContent, Appendable {
	
	/** The content float values */
	private float[] value;
	
	/** tmp storage of content */
	private ArrayList<float[]> stor;
	
	/**
	 * Constructor
	 */
	CElementFloat(String tag_name, Attributes atts) {
		super(tag_name, atts);
	}
	
	/**
	 * Add to the text content of the CElement
	 *
	 * @param content The text content to add to the CElement
	 */
        @Override
	public void appendTextContent(String content) {
		if (stor == null) {
			stor = new ArrayList<>();
		}
		try {
			float[] tmp_value = FieldValueHandler.toFloat(content);
			stor.add(tmp_value);
		} catch (NumberFormatException nfe) {
			throw new ImportFileFormatException(
				"ColladaFileParser: "+ 
				"NumberFormatException in <"+ tag_name +">: "+ 
				nfe.getMessage());
		}
	}
	
	/**
	 * Set the text content of the CElement
	 *
	 * @param content The text content of the CElement
	 */
        @Override
	public void setTextContent(String content) {
		if (stor == null) {
			try {
				value = FieldValueHandler.toFloat(content);
			} catch (NumberFormatException nfe) {
				throw new ImportFileFormatException(
					"ColladaFileParser: "+ 
					"NumberFormatException in <"+ tag_name +">: "+ 
					nfe.getMessage());
			}
		} else {
			appendTextContent(content);
			int num_array = stor.size();
			if (num_array > 0) {
				float[][] val = new float[num_array][];
				int num_value = 0;
				for (int i = 0; i < num_array; i++) {
					val[i] = stor.get(i);
					num_value += val[i].length;
				}
				value = FieldValueHandler.flatten(val, num_value);
			}
			stor = null;
		}
	}
	
	/**
	 * Null out the content of the CElement
	 */
        @Override
	public void clearContent() {
		value = null;
		super.clearContent();
	}
	
	/**
	 * Return the content of the Element
	 *
	 * @return The content of the Element
	 */
        @Override
	public float[] getFloatContent() {
		return(value);
	}
}
