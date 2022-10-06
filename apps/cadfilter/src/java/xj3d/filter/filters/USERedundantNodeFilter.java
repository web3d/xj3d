/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Local imports
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;

/**
 * For the specified nodes, compare like nodes for equivalence.
 * DEF and USE equivalent nodes as possible.
 * <p>
 * Nodes that are already DEF'ed are ignored. Default values for
 * node fields are not considered in the determination of
 * equivalence. If one node does not initialize a field and
 * another node initializes that field to a default value -
 * they are NOT considered to be equivalent.
 * <p>
 *
 * <b>Filter Options</b>
 * <p>
 * <code>nodeName "name0 name1 name2"</code> The nodes to check
 * <br> or <br>
 * <code>nodeName name0,name1,name2</code> The nodes to check
 *
 * @author Rex Melton
 * @version $Revision: 1.18 $
 */
public class USERedundantNodeFilter extends CommonEncodedBaseFilter {

    /**
     * The logging identifier of this app
     */
    private static final String LOG_NAME = "USERedundantNode";

    /**
     * Node Name param
     */
    private static final String NODE_NAME = "-nodeName";

    /**
     * Have we issued the suppress.
     */
    private boolean issuedSuppress;

    /**
     * Have the arguments been set, and the node collection objects initialized
     */
    private boolean gather_enabled;

    /**
     * The Node names to gather for filtering
     */
    private List<String> nodeName;

    /**
     * Lists of nodes to inspect
     */
    private List<List<CommonEncodable>> node_list_list;

    /**
     * Cache of used def names
     */
    private List<String> defNames;

    /**
     * Suffix for generated def names
     */
    private int def_inc;

    /**
     * Create an instance of the filter.
     */
    public USERedundantNodeFilter() {

        issuedSuppress = false;
        encodeRoutes = true;
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

        if (gather_enabled) {
            filter();
            nodeName.clear();
            node_list_list.clear();
            defNames.clear();
        }
        scene.encode();

        // release the scene
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
        if (gather_enabled) {
            int idx = nodeName.indexOf(name);
            if (idx != -1) {
                List<CommonEncodable> node_list = node_list_list.get(idx);
                if (node_list == null) {
                    node_list = new ArrayList<>();
                    node_list_list.set(idx, node_list);
                }
                node_list.add((CommonEncodable) encStack.peek());
            }
            if (defName != null) {
                defNames.add(defName);
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
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

            if (arg.equals(NODE_NAME)) {
                if (i + 1 >= args.length) {

                    throw new IllegalArgumentException(
                            "Not enough args for " + LOG_NAME + ".  "
                            + "Expecting one more to specify nodeName.");
                }
                String nodeNames = args[i + 1];

                String[] results = nodeNames.split("\\s|[,]");

                int num_node = results.length;
                nodeName = new ArrayList<>(num_node);
                node_list_list = new ArrayList<>(num_node);

                for (int j = 0; j < num_node; j++) {
                    nodeName.add(results[j]);
                    node_list_list.add(null);
                }
                defNames = new ArrayList<>(100);

                gather_enabled = true;
            }
        }
    }

    /**
     * Remove the redundancies
     */
    private void filter() {
        int num_list = node_list_list.size();
        for (int n = 0; n < num_list; n++) {
            int num_nodes = 0;
            int rem_nodes = 0;
            String name = nodeName.get(n);
            List<CommonEncodable> node_list = node_list_list.get(n);
            if (node_list != null) {
                num_nodes = node_list.size();
                rem_nodes = num_nodes;
                if (num_nodes > 1) {
                    int last_idx = num_nodes - 1;
                    int[][] dup = new int[last_idx][];
                    int[] num_dup = new int[last_idx];
                    int[] ignore = new int[num_nodes];
                    int num_ignore = 0;

                    Arrays.fill(ignore, -1);

                    for (int i = 0; i < last_idx; i++) {

                        dup[i] = new int[num_nodes - i];

                        CommonEncodable src = node_list.get(i);
                        if (src.getDefName() == null) {
                            for (int j = i + 1; j < num_nodes; j++) {
                                boolean skip = false;
                                for (int k = 0; k < num_nodes; k++) {
                                    int x = ignore[k];
                                    if (x == -1) {
                                        break;
                                    }
                                    if (x == j) {
                                        skip = true;
                                        break;
                                    }
                                }
                                if (!skip) {
                                    CommonEncodable tgt = node_list.get(j);
                                    if ((tgt.getDefName() == null) && (tgt.getUseName() == null)) {
                                        if (src.deepEquals(tgt)) {
                                            boolean isAppearance = name.equals("Appearance");
                                            if (isAppearance) {
                                                boolean geomPropsEqual = checkGeomProps(src, tgt);
                                                if (!geomPropsEqual) {
                                                    continue;
                                                }
                                            }
                                            int d_idx = num_dup[i];
                                            dup[i][d_idx] = j;
                                            num_dup[i] += 1;
                                            ignore[num_ignore++] = j;
                                        }
                                    } else {
                                        ignore[num_ignore++] = j;
                                    }
                                }
                            }
                        }
                    }
                    String def_name_base = name.toUpperCase() + "_";
                    def_inc = 0;
                    for (int i = 0; i < last_idx; i++) {
                        if (num_dup[i] > 0) {
                            rem_nodes -= num_dup[i];
                            String def_name = getDefName(def_name_base);
                            defNames.add(def_name);
                            //System.out.println(i +": "+ def_name +": "+ java.util.Arrays.toString(dup[i]));
                            CommonEncodable src = node_list.get(i);
                            src.setDefName(def_name);
                            for (int j = 0; j < num_dup[i]; j++) {
                                CommonEncodable tgt = node_list.get(dup[i][j]);
                                tgt.setUseName(def_name);
                            }
                        }
                    }
                }
            }
            System.out.println(LOG_NAME + ": starting " + name + " = " + num_nodes + ": remaining = " + rem_nodes);
        }
    }

    /**
     * Create and return a unique def name
     *
     * @param def_name_base The prefix of the def name
     * @return A def name unique to the document
     */
    private String getDefName(String def_name_base) {
        String def_name = def_name_base + def_inc;
        while (defNames.contains(def_name)) {
            def_inc++;
            def_name = def_name_base + def_inc;
        }
        return (def_name);
    }

    /**
     * Return whether the Appearance type properties of the geometry associated
     * with the argument Appearance nodes is equivalent.
     *
     * @param src The source Appearance wrapper.
     * @param tgt The target Appearance wrapper.
     * @return true if the properties can be considered equal, false if not.
     */
    private boolean checkGeomProps(CommonEncodable src, CommonEncodable tgt) {
        CommonEncodable srcShape = src.getParent();
        CommonEncodable srcGeom = (CommonEncodable) srcShape.getValue("geometry");
        CommonEncodable tgtShape = tgt.getParent();
        CommonEncodable tgtGeom = (CommonEncodable) tgtShape.getValue("geometry");

        boolean srcIsNull = (srcGeom == null);
        boolean tgtIsNull = (tgtGeom == null);
        if (srcIsNull && tgtIsNull) {
            // both null is equivalent
            return (true);
        }

        boolean srcDefNameIsSet = (src.defName != null);
        boolean tgtDefNameIsSet = (tgt.defName != null);

        boolean srcUseNameIsSet = (src.useName != null);
        boolean tgtUseNameIsSet = (tgt.useName != null);

        if (srcDefNameIsSet && tgtDefNameIsSet) {
            // both nodes are uniquely def'ed.
            // declare them to be not equal regardless
            // of the remainder of their state
            return (false);

        } else if (srcDefNameIsSet && tgtUseNameIsSet) {
            if (src.defName.equals(tgt.useName)) {
                return (true);
            } else {
                // the argument 'use's a def other than src def.
                // therefore they are not equal
                return (false);
            }
        } else if (tgtDefNameIsSet && srcUseNameIsSet) {
            if (tgt.defName.equals(src.useName)) {
                return (true);
            } else {
                // src 'use's a def other than the argument's def.
                // therefore they are not equal
                return (false);
            }
        }

        if (srcUseNameIsSet && tgtUseNameIsSet) {
            if (src.useName.equals(tgt.useName)) {
                // both use the same def
                return (true);
            } else {
                // each use's a separate def
                return (false);
            }
        } else if (srcUseNameIsSet || tgtUseNameIsSet) {
            // one use's one doesn't
            return (false);
        }

        boolean srcIsCCW = true;
        boolean srcIsSolid = true;
        if (srcGeom != null) {
            Boolean srcCCW = (Boolean) srcGeom.getValue("ccw");
            if (srcCCW != null) {
                srcIsCCW = srcCCW;
            }
            Boolean srcSolid = (Boolean) srcGeom.getValue("solid");
            if (srcSolid != null) {
                srcIsSolid = srcSolid;
            }
        }
        boolean tgtIsCCW = true;
        boolean tgtIsSolid = true;
        if (tgtGeom != null) {
            Boolean tgtCCW = (Boolean) tgtGeom.getValue("ccw");
            if (tgtCCW != null) {
                tgtIsCCW = tgtCCW;
            }
            Boolean tgtSolid = (Boolean) tgtGeom.getValue("solid");
            if (tgtSolid != null) {
                tgtIsSolid = tgtSolid;
            }
        }
        if (srcIsCCW != tgtIsCCW) {
            return (false);
        }
        if (srcIsSolid != tgtIsSolid) {
            return (false);
        }
        return (true);
    }
}
