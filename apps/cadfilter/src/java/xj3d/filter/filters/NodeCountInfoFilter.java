/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.util.*;

// Local imports
import xj3d.filter.NewAbstractFilter;
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

/**
 * Counts the number of nodes in a file.  Will output its list in
 * descending sorted order.
 * <p>
 *
 * USE of a node will increase its count
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public class NodeCountInfoFilter extends NewAbstractFilter implements Comparator<Map.Entry<String,Integer>> {

    /** The node counts, Node Name -&gt; Count */
    protected Map<String, Integer> counts;

    /** Mapping between DEF and nodeName */
    protected Map<String, String> defMap;

    /**
     * Construct a default instance of the filter.
     */
    public NodeCountInfoFilter() {
        defMap = new HashMap<>();
        counts = new HashMap<>();
    }

    /**
     * Parse the output from this filter to get a map.
     *
     * @param txt The output
     * @return The count map
     */
    public Map<String, Integer> parseOutput(String txt) {
        String[] result = txt.split("\n");
        Map<String, Integer> ret_val = new HashMap<>(result.length);

        for (String result1 : result) {
            String[] result2 = result1.split(":");
            ret_val.put(result2[0], Integer.valueOf(result2[1].trim()));
        }

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        super.endDocument();

        @SuppressWarnings("unchecked") // generic array type
        Map.Entry<String,Integer>[] vals = new Map.Entry[counts.size()];
        Iterator<Map.Entry<String,Integer>> itr = counts.entrySet().iterator();

        int cnt = 0;
        while(itr.hasNext()) {
            Map.Entry<String,Integer> entry = itr.next();
            vals[cnt++] = entry;
        }

        Arrays.sort(vals, this);

        for (Map.Entry<String, Integer> entry : vals) {
            System.out.print(entry.getKey());
            System.out.print(": ");
            System.out.println(entry.getValue());
        }
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {


        if (defName != null) {
            defMap.put(defName, name);
        }

        increaseCount(name);

        super.startNode(name, defName);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        String nodeName = defMap.get(defName);

        increaseCount(nodeName);

        super.useDecl(defName);
    }

    //----------------------------------------------------------
    // Methods defined by Comparator
    //----------------------------------------------------------
    @Override
    public int compare(Map.Entry<String,Integer> o1, Map.Entry<String,Integer> o2) {
        Integer i1 = o1.getValue();
        Integer i2 = o2.getValue();

        return -i1.compareTo(i2);
    }
/*
    public equals(Object obj) {
        return
    }
*/

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
    /**
     * Increase the count on a node.
     *
     * @param node The node name
     */
    private void increaseCount(String node) {
        Integer cnt = counts.get(node);

        if (cnt == null) {
            cnt = 1;
        } else {
            cnt = cnt + 1;
        }

        counts.put(node, cnt);
    }
}
