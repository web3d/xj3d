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

package xj3d.filter;

// External Imports
import java.util.*;

// Internal Imports
// None

/**
 * Validator which does nothing.  Useful in validating that
 * the resultant stream at least parses.
 *
 * @author Alan Hudson
 * @version
 */

// External Imports
import xj3d.filter.filters.NodeCountInfoFilter;


/**
 * Validate Node counts.  All countNode operations must be true
 * <p>
 * Pass in arguments as in:  -countNode1 "Shape LT 1"  -countNode2 "Transform GTE 2"
 *
 * Operations supported:
 *    LT   <
 *    LTE  <=
 *    GT   >
 *    GTE  >=
 *    EQ   =
 *    NE   !=
 *
 * @author Alan Hudson
 * @version
 */
public class CountFilterValidator extends NodeCountInfoFilter {
    private static final Set<String> OPS;

    /** Node names */
    private List<String> nodes;

    /** Operations */
    private List<String> ops;

    /** Values */
    private List<Integer> vals;


    static {
        OPS = new HashSet<String>();
        OPS.add("LT");
        OPS.add("LTE");
        OPS.add("GT");
        OPS.add("GTE");
        OPS.add("EQ");
        OPS.add("NE");
    }

    public CountFilterValidator() {
        nodes = new ArrayList<String>();
        ops = new ArrayList<String>();
        vals = new ArrayList<Integer>();
    }

    @Override
    public void endDocument() {
        boolean fail = false;
        String msg = "Unknown failure";

        for(int i=0; i < nodes.size(); i++) {
            String nodeName = nodes.get(i);
            String op = ops.get(i);
            Integer val = vals.get(i);

            Integer cnt = counts.get(nodeName);

            if (cnt == null) {
                cnt = 0;
            }

System.out.println("Testing: " + nodeName + " op: " + op + " val: " + val + " against: " + cnt);
            if (op.equals("LTE")) {
                if (!(cnt <= val.intValue())) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else if (op.equals("GTE")) {
                if (!(cnt >= val.intValue())) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else if (op.equals("LT")) {
                if (!(cnt < val.intValue())) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else if (op.equals("GT")) {
                if (!(cnt > val.intValue())) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else if (op.equals("EQ")) {
                if (!(val.intValue() == cnt)) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else if (op.equals("NE")) {
                if (!(val.intValue() != cnt)) {
                    fail = true;
                    msg = nodeName + " " + op + " " + cnt + " count was: " + val.intValue();
                }
            } else {

                msg = "Unknown operation: " + op;

                fail = true;
            }
        }

        if (fail) {
            System.out.println("Failure was: " + msg);
            lastErrorCode = -1;
            FilterProcessingException fpe = new FilterProcessingException(msg, -1);
            throw fpe;

            //throw new VRMLException("Told to fail");
        }
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        nodes = new ArrayList<String>();
        ops = new ArrayList<String>();
        vals = new ArrayList<Integer>();

        for( int i = 0; i< args.length; i++) {
            String argument = args[i];
            if (args[i].startsWith("-countNode")) {
                String st = args[++i];
                String[] res = st.split(" ");
                nodes.add(res[0]);
                ops.add(res[1]);
                vals.add(Integer.valueOf(res[2]));
            }
        }
    }
}