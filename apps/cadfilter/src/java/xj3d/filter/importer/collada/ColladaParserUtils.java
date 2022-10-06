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

// Local imports
import org.web3d.vrml.sav.ImportFileFormatException;

/**
 * Utility methods for parsing Collada elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
abstract class ColladaParserUtils {
	
	/**
	 * Return the named int attribute value from the argument element. If 
	 * the attribute is not found, an exception is thrown.
	 *
	 * @param element The element containing the attribute
	 * @param name The attribute name
	 * @return The int value
	 */
	static int getIntValue(CElement element, String name) throws 
		ImportFileFormatException {
		
		int rval = 0;
		String value = element.getAttribute(name);
		if (value == null) {
			throw new ImportFileFormatException(
				ColladaParserConstants.LOG_NAME + 
				": <"+ element.getTagName() +">: Missing required attribute: "+ 
				name);
		} else {
			try {
				rval = Integer.parseInt(value);
			} catch (NumberFormatException nfe) {
				throw new ImportFileFormatException(
					ColladaParserConstants.LOG_NAME + 
					": NumberFormatException: <"+ element.getTagName() +" "+ 
					name +"\"="+ value +"\">");
			}
		}
		return(rval);
	}
	
	/**
	 * Return the named int attribute value from the argument element. If
	 * the attribute is not found, return the specified default value.
	 *
	 * @param element The element containing the attribute
	 * @param name The attribute name
	 * @param def The default value
	 * @return The int value
	 */
	static int getIntValue(CElement element, String name, int def) throws 
		ImportFileFormatException {
		
		int rval = def;
		String value = element.getAttribute(name);
		if (value != null) {
			try {
				rval = Integer.parseInt(value);
			} catch (NumberFormatException nfe) {
				throw new ImportFileFormatException(
					ColladaParserConstants.LOG_NAME + 
					": NumberFormatException: <"+ element.getTagName() +" "+ 
					name +"\"="+ value +"\">");
			}
		}
		return(rval);
	}
	
	/**
	 * Return the named float attribute value from the argument element. If 
	 * the attribute is not found, an exception is thrown.
	 *
	 * @param element The element containing the attribute
	 * @param name The attribute name
	 * @return The float value
	 */
	static float getFloatValue(CElement element, String name) throws 
		ImportFileFormatException {
		
		float rval = 0;
		String value = element.getAttribute(name);
		if (value == null) {
			throw new ImportFileFormatException(
				ColladaParserConstants.LOG_NAME + 
				": <"+ element.getTagName() +">: Missing required attribute: "+ 
				name);
		} else {
			try {
				rval = Float.parseFloat(value);
			} catch (NumberFormatException nfe) {
				throw new ImportFileFormatException(
					ColladaParserConstants.LOG_NAME + 
					": NumberFormatException: <"+ element.getTagName() +" "+ 
					name +"\"="+ value +"\">");
			}
		}
		return(rval);
	}
	
	/**
	 * Return the named float attribute value from the argument element. If
	 * the attribute is not found, return the specified default value.
	 *
	 * @param element The element containing the attribute
	 * @param name The attribute name
	 * @param def The default value
	 * @return The float value
	 */
	static float getFloatValue(CElement element, String name, float def) throws 
		ImportFileFormatException {
		
		float rval = def;
		String value = element.getAttribute(name);
		if (value != null) {
			try {
				rval = Float.parseFloat(value);
			} catch (NumberFormatException nfe) {
				throw new ImportFileFormatException(
					ColladaParserConstants.LOG_NAME + 
					": NumberFormatException: <"+ element.getTagName() +" "+ 
					name +"\"="+ value +"\">");
			}
		}
		return(rval);
	}
}
