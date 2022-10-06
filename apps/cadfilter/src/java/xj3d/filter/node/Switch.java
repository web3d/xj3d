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

package xj3d.filter.node;

// External imports
// None

// Local imports
// None

/**
 * Wrapper for an X3D Switch node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Switch extends BaseGroup {
    public int whichChoice;

    /**
     * Constructor
     */
    public Switch() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Switch(String defName) {
        super("Switch", defName);
        whichChoice = -1;
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        super.clear();
        whichChoice = -1;
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {

        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);

                super.encode();

                if (whichChoice != -1) {
                    handler.startField("whichChoice");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(whichChoice);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Integer.toString(whichChoice));
                        break;
                    }
                }

                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String name, Object value) {

        switch (name) {
            case "whichChoice":
                if (value instanceof String) {
                    whichChoice = Integer.parseInt((String)value);
                } else if (value instanceof Integer) {
                    whichChoice = (Integer) value;
                }
                break;
            case "choice":
                super.setValue("children", value);
                break;
            default:
                super.setValue(name, value);
                break;
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String name, Object value, int len) {

        if (name.equals("choice")) {
            super.setValue("children", value, len);
        } else {
            super.setValue(name, value, len);
        }
    }

    /**
     * Create and return a copy of this object.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children,
     * false returns a new instance of this node type.
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        Switch g = new Switch();
        copy(g, full);
        if (full) {
            g.whichChoice = this.whichChoice;
        }
        return(g);
    }
}
