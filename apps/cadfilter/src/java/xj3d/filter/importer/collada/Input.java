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
 * Data binding for the Collada <input> element.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Input {

    /** offset attribute */
    int offset;

    /** semantic attribute */
    String semantic;

    /** source attribute */
    String source;

    /** set attribute */
    int set;

    /**
     * Constructor
     *
     * @param element The Element
     */
    Input(CElement element) {

        offset = ColladaParserUtils.getIntValue(element, ColladaStrings.OFFSET, 0);
        set = ColladaParserUtils.getIntValue(element, ColladaStrings.SET, -1);

        semantic = element.getAttribute(ColladaStrings.SEMANTIC);
        source = element.getAttribute(ColladaStrings.SOURCE);
    }

    /**
     * Return the number of unique offsets contained in the array of
     * Input objects.
     *
     * @param input An array of Input objects.
     * @return The number of unique offsets.
     */
    static int getNumberOfOffsets(Input[] input) {
        int num_offsets;
        int num_inputs = input.length;
        if ( num_inputs > 1 ) {
            int max_index = 0;
            for (Input input1 : input) {
                if (input1.offset > max_index) {
                    max_index = input1.offset;
                }
            }
            num_offsets = max_index + 1;
        } else {
            num_offsets = num_inputs;
        }
        return(num_offsets);
    }

    /**
     * Return the Input object from the array that matches the
     * requested semantic attribute
     *
     * @param input An array of Input objects.
     * @param semantic The semantic attribute to match
     * @return The requested Input object, or null if it could not be found.
     */
    static Input getInput(Input[] input, String semantic) {
        Input rval = null;
        for (Input input1 : input) {
            if (input1.semantic.equals(semantic)) {
                rval = input1;
                break;
            }
        }
        return(rval);
    }

    /**
     * Return the set of Input objects contained in the List
     *
     * @param input_list An ArrayList of <input> Elements
     * @return The array of Input objects corresponding to the argument list
     */
    static Input[] getInputs(List<CElement> input_list) {
        int num_inputs = input_list.size();
        Input[] input = new Input[num_inputs];
        for (int i = 0; i < num_inputs; i++) {
            CElement input_element = input_list.get(i);
            input[i] = new Input(input_element);
        }
        return(input);
    }
}
