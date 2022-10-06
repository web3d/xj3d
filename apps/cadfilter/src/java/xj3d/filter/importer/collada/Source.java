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
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.ImportFileFormatException;

import xj3d.filter.FilterExitCodes;
import xj3d.filter.FilterProcessingException;

/**
 * Data binding for Collada <source> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Source {

    /** id attribute */
    String id;

    /** name attribute */
    String name;

    /** array object */
    Array array;

    /** accessor object */
    Accessor accessor;

    /**
     * Constructor
     *
     * @param element The Element
     */
    Source(CElement element) {

        id = element.getAttribute(ColladaStrings.ID);
        name = element.getAttribute(ColladaStrings.NAME);

        List<CElement> children = element.getElements();

        // extract the content we'll use, ignore <asset> & <technique>
        for (CElement child : children) {
            String tagName = child.getTagName();
            if (tagName.endsWith("_array")) {
                // array element is required
                array = new Array(child);

            } else if (tagName.equals(ColladaStrings.TECHNIQUE_COMMON)) {
                // if technique common is present, accessor is required
                CElement accessor_element =
                        child.getFirstElementByTagName(ColladaStrings.ACCESSOR);
                
                String accessor_source =
                        accessor_element.getAttribute(ColladaStrings.SOURCE);
                
                boolean relative_id = false;
                if (accessor_source.startsWith("#")) {
                    accessor_source = accessor_source.substring(1);
                    relative_id = true;
                }

                if (array == null) {
                    // TODO: Need a resolver here

                }

                if (array == null || !accessor_source.equals(array.id)) {
                    // don't know how to source coordinates that are not contained
                    // in the local array.....
                    I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSourceArray", I18nUtils.CRIT_MSG, new String[] {accessor_source});
                    throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
                }
                accessor = new Accessor(accessor_element);
            }
        }
    }

    /**
     * Return the data from the argument source element in the order and form specified.
     *
     * @param binary true if a primitive array is required, false for a String.
     * @return The source data, either a primitive or String array
     */
    Object getSourceData() {

        // if the accessor is null, this will throw an NPE....
        int num_params = accessor.num_params;
        boolean[] valid = new boolean[num_params];
        int num_valid = 0;
        for (int i = 0; i < num_params; i++) {
            valid[i] = accessor.param[i].isNamed;
            if (valid[i]) {
                num_valid++;
            }
        }

        Object rval = null;
        if ((num_valid == num_params) && (accessor.offset == 0) && (accessor.stride == num_params)) {
            // the array content is good to go
            rval = array.content;
        } else {
            // the array content needs some processing
            switch (array.type_identifier) {
            case Array.FLOAT:
                rval = configFloat((float[])array.content, num_params, num_valid, valid, accessor);
                break;
            case Array.INT:
                rval = configInt((int[])array.content, num_params, num_valid, valid, accessor);
                break;
            case Array.NAME:
            case Array.IDREF:
                rval = configString((String[])array.content, num_params, num_valid, valid, accessor);
                break;
            case Array.BOOL:
                rval = configBoolean((boolean[])array.content, num_params, num_valid, valid, accessor);
                break;
            }
        }
        return(rval);
    }

    /**
     * Return a Map of Source objects contained in the List,
     * key'ed by id.
     *
     * @param source_list An ArrayList of <source> Elements
     * @return A Map of Source objects corresponding to the argument list
     */
    static Map<String, Source> getSourceMap(List<CElement> source_list) {
        int num_sources = source_list.size();
        Map<String, Source> map = new HashMap<>();
        for (int i = 0; i < num_sources; i++) {
            CElement source_element = source_list.get(i);
            Source source = new Source(source_element);
            map.put(source.id, source);
        }
        return(map);
    }

    /**
     * Return a reconfigured array
     */
    private float[] configFloat(float[] src, int num_params, int num_valid, boolean[] valid,
        Accessor accessor ) {

        int src_index = accessor.offset;
        float[] dst = new float[accessor.count*num_valid];
        int dst_index = 0;
        for (int i = 0; i < accessor.count; i++) {
            for (int j = 0; j < num_params; j++) {
                if (valid[j]) {
                    dst[dst_index++] = src[src_index + j];
                }
            }
            src_index += accessor.stride;
        }
        return(dst);
    }

    /**
     * Return a reconfigured array
     */
    private int[] configInt(int[] src, int num_params, int num_valid, boolean[] valid,
        Accessor accessor ) {

        int src_index = accessor.offset;
        int[] dst = new int[accessor.count*num_valid];
        int dst_index = 0;
        for (int i = 0; i < accessor.count; i++) {
            for (int j = 0; j < num_params; j++) {
                if (valid[j]) {
                    dst[dst_index++] = src[src_index + j];
                }
            }
            src_index += accessor.stride;
        }
        return(dst);
    }

    /**
     * Return a reconfigured array
     */
    private String[] configString(String[] src, int num_params, int num_valid, boolean[] valid,
        Accessor accessor ) {

        int src_index = accessor.offset;
        String[] dst = new String[accessor.count*num_valid];
        int dst_index = 0;
        for (int i = 0; i < accessor.count; i++) {
            for (int j = 0; j < num_params; j++) {
                if (valid[j]) {
                    dst[dst_index++] = src[src_index + j];
                }
            }
            src_index += accessor.stride;
        }
        return(dst);
    }

    /**
     * Return a reconfigured array
     */
    private boolean[] configBoolean(boolean[] src, int num_params, int num_valid, boolean[] valid,
        Accessor accessor ) {

        int src_index = accessor.offset;
        boolean[] dst = new boolean[accessor.count*num_valid];
        int dst_index = 0;
        for (int i = 0; i < accessor.count; i++) {
            for (int j = 0; j < num_params; j++) {
                if (valid[j]) {
                    dst[dst_index++] = src[src_index + j];
                }
            }
            src_index += accessor.stride;
        }
        return(dst);
    }
}
