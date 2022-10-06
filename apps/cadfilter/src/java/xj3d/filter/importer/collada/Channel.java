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

/**
 * Data binding for Collada <channel> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Channel {

    /** source attribute */
    String source;

    /** target attribute */
    String target;

    /**
     * Constructor
     *
     * @param channel_element The Element
     */
    Channel(CElement channel_element) {

        source = channel_element.getAttribute(ColladaStrings.SOURCE);
        target = channel_element.getAttribute(ColladaStrings.TARGET);
    }

    /**
     * Return the set of Channel objects contained in the NodeList
     *
     * @param channel_list An ArrayList of <channel> Elements
     * @return The array of Channel objects corresponding to the argument list
     */
    static Channel[] getChannels(List<CElement> channel_list) {
        int num_channels = channel_list.size();
        Channel[] channel = new Channel[num_channels];
        for (int i = 0; i < num_channels; i++) {
            CElement channel_element = channel_list.get(i);
            channel[i] = new Channel(channel_element);
        }
        return(channel);
    }
}
