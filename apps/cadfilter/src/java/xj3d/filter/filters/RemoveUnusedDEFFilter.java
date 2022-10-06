/*****************************************************************************
 *                        Yumetech Copyright (c) 2011
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
//import org.web3d.util.*;

import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;

/**
 * Remove any DEF names not used in this file.
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class RemoveUnusedDEFFilter extends CommonEncodedBaseFilter {

	/** The logging identifier of this app */
    private static final String LOG_NAME = "RemoveUnusedDEF";

    /** Except Node param, comma sep */
    private static final String EXCEPT_NAMES = "-exceptNodes";

    /** Have we issued the suppress.  */
    private boolean issuedSuppress;

    /** DEF's */
    private Map<String,CommonEncodable> defMap;

    /** NodeTypes to exclude */
    private Set<String> excludes;

    /**
     * Create an instance of the filter.
     */
    public RemoveUnusedDEFFilter() {
        defMap = new HashMap<>(1_000);
        excludes = new HashSet<>();
        encodeRoutes = true;

        issuedSuppress = false;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------
    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        suppressCalls(false);

        filter();
        scene.encode();
        scene = null;

        super.endDocument();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not given
     * for this node.
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
            throws SAVException, VRMLException {

        if (!issuedSuppress) {
            suppressCalls(true);
            issuedSuppress = true;
        }

        super.startNode(name, defName);

        if (defName != null && !excludes.contains(name)) {
            CommonEncodable ce = (CommonEncodable) encStack.peek();
            defMap.put(defName, ce);
        }
    }

    /**
     * The field value is a USE for the given node name. This is a terminating
     * call for startField as well. The next call will either be another
     * <CODE>startField()</CODE> or
     * <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

        super.useDecl(defName);

        defMap.remove(defName);
    }

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNodeName The name of the DEF of the source node
     * @param srcFieldName The name of the field to route values from
     * @param destNodeName The name of the DEF of the destination node
     * @param destFieldName The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void routeDecl(String srcNodeName,
            String srcFieldName,
            String destNodeName,
            String destFieldName)
            throws SAVException, VRMLException {


        defMap.remove(srcNodeName);
        defMap.remove(destNodeName);

        super.routeDecl(srcNodeName, srcFieldName, destNodeName, destFieldName);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    /**
     * Remove the redundancies
     */
    private void filter() {

        int num_nodes = defMap.size();
        Iterator<CommonEncodable> itr = defMap.values().iterator();

        while (itr.hasNext()) {
            CommonEncodable src = itr.next();
            src.setDefName(null);
        }

        System.out.println(LOG_NAME + ": removed " + num_nodes + " unused DEFs");
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        String prefix = "-" + LOG_NAME + ":";
        String arg;

        for (int i = 0; i < args.length; i++) {
            arg = args[i];

            if (arg.startsWith(prefix)) {
                arg = "-" + arg.substring(prefix.length());
            }

            if (arg.equals(EXCEPT_NAMES)) {
                if (i + 1 >= args.length) {

                    throw new IllegalArgumentException(
                            "Not enough args for " + LOG_NAME + ".  "
                            + "Expecting one more to specify nodeName.");
                }
                String exceptName = args[i + 1];

                String[] results = exceptName.split("\\s|[,]");
                excludes.addAll(Arrays.asList(results));
            }
        }
    }
}
