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
import java.util.List;

/**
 * Data binding for Collada <bind_material> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class BindMaterial {

    /** material instance objects */
    InstanceMaterial[] instance;

    /** material instance elements */
    CElement[] element;

    /**
     * Constructor
     *
     * @param bind_material_element The Element
     */
    BindMaterial(CElement bind_material_element) {

        if (bind_material_element == null) {

            instance = new InstanceMaterial[0];

        } else {

            CElement technique_common_element =
				bind_material_element.getFirstElementByTagName(ColladaStrings.TECHNIQUE_COMMON);
            List<CElement> list =
				technique_common_element.getElementsByTagName(ColladaStrings.INSTANCE_MATERIAL);
            int num_instance_material = list.size();
            instance = new InstanceMaterial[num_instance_material];
            element = new CElement[num_instance_material];
            for (int i = 0; i < num_instance_material; i++) {
                CElement instance_material_element = list.get(i);
                element[i] = instance_material_element;
                instance[i] = new InstanceMaterial(instance_material_element);
            }
        }
    }

    /**
     * Return the targeted <instance_material> Element for the requested symbol.
     *
     * @param symbol The material instance symbol to match
     * @return target The targeted <instance_material> Element, or null if no match is found.
     */
    CElement getTarget(String symbol) {
        CElement target = null;
        for (int i = 0; i < instance.length; i++) {
            if (instance[i].symbol.equals(symbol)) {
                target = element[i];
                break;
            }
        }
        return(target);
    }
}
