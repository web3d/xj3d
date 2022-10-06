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

// internal imports
import org.web3d.vrml.sav.ImportFileFormatException;

/**
 * Data binding for Collada <*_array> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Array {

    /** identifiers of the array content type */
    static final int BOOL = 0;
    static final int FLOAT = 1;
    static final int INT = 2;
    static final int NAME = 3;
    static final int IDREF = 4;

    /** id attribute */
    String id;

    /** name attribute */
    String name;

    /** The content */
    Object content;

    /** the number of values in the content */
    int count;

    /** the type, "float", "int", "boolean", etc. */
    String type;

    /** the type identifier */
    int type_identifier;

    /**
     * Constructor
     *
     * @param element The Element
     */
    Array(CElement element) {

		count = ColladaParserUtils.getIntValue(element, ColladaStrings.COUNT);

        id = element.getAttribute(ColladaStrings.ID);
        name = element.getAttribute(ColladaStrings.NAME);
		
		// TODO: bool & String content types
		if (element instanceof FloatContent) {
			content = ((FloatContent)element).getFloatContent();
/*
            // This is fairly common should we issue an warning?
            if (count != ((float[])content).length) {
                System.out.println("Count does not match actual length");
            }
            */
		} else if (element instanceof IntContent) {
			content = ((IntContent)element).getIntContent();
		} else {
			content = element.getTextContent();
		}

        String tagName = element.getTagName();
        int idx = tagName.indexOf("_");
        type = tagName.substring(0, idx);

        switch (type) {
            case "bool":
                type_identifier = BOOL;
                break;
            case "float":
                type_identifier = FLOAT;
                break;
            case "int":
                type_identifier = INT;
                break;
            case "Name":
                type_identifier = NAME;
                break;
            case "IDREF":
                type_identifier = IDREF;
                break;
            default:
                // shouldn't happen since these are the valid known types.
                // will probably cause an exception eventually down the line if it does.
                type_identifier = -1;
                break;
        }
    }
}
