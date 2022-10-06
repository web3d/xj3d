/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
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
import java.util.List;
import java.util.Map;

/**
 * Data binding for Collada <sampler> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Sampler {

    /** id attribute */
    String id;

    /** The array of input elements */
    Input[] input;

    /** The number of input elements */
    int num_inputs;

    /**
     * Constructor
     *
     * @param sampler_element The Element
     */
    Sampler(CElement sampler_element) {

        id = sampler_element.getAttribute(ColladaStrings.ID);

        List<CElement> input_list =
			sampler_element.getElementsByTagName(ColladaStrings.INPUT);
        input = Input.getInputs(input_list);
        num_inputs = input.length;
    }

    /**
     * Return a Map of Sampler objects contained in the List,
     * key'ed by id.
     *
     * @param sampler_list An ArrayList of <sampler> Elements
     * @return A Map of Sampler objects corresponding to the argument list
     */
    static Map<String, Sampler> getSamplerMap(List<CElement> sampler_list) {
        int num_samplers = sampler_list.size();
        Map<String, Sampler> map = new HashMap<>();
        for (int i = 0; i < num_samplers; i++) {
            CElement sampler_element = sampler_list.get(i);
            Sampler sampler = new Sampler(sampler_element);
            map.put(sampler.id, sampler);
        }
        return(map);
    }
}
