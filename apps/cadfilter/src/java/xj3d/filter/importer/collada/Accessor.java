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
import java.util.List;

// Local imports
// None

/**
 * Data binding for Collada <accessor> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Accessor {

    /** The number of times the source array is accessed */
    int count;

    /** Index of the first value in the source array */
    int offset;

    /** The source location */
    String source;

    /** The number of array values that form a coordinate */
    int stride;

    /** The array of parameter elements */
    Param[] param;

    /** The number of param elements */
    int num_params;

    /**
     * Constructor
     *
     * @param element The Element
     */
    Accessor(CElement element) {

        count = ColladaParserUtils.getIntValue(element, ColladaStrings.COUNT);
        offset = ColladaParserUtils.getIntValue(element, ColladaStrings.OFFSET, 0);
        stride = ColladaParserUtils.getIntValue(element, ColladaStrings.STRIDE, 1);

        source = element.getAttribute(ColladaStrings.SOURCE);

        List<CElement> param_element_list =
			element.getElementsByTagName(ColladaStrings.PARAM);
        num_params = param_element_list.size();
        param = new Param[num_params];
        for (int i = 0; i < num_params; i++) {
            param[i] = new Param(param_element_list.get(i));
        }
    }
}
