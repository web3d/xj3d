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

// Local imports

/**
 * Wrapper for an X3D Collision node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Collision extends BaseGroup {
    public boolean enabled;

    /**
     * Constructor
     */
    public Collision() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Collision(String defName) {
        super("Collision", defName);

        enabled = true;
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
        enabled = true;
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

                if (enabled != true) {
                    handler.startField("enabled");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(enabled);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(enabled ? "TRUE" : "FALSE");
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

        if (name.equals("enabled")) {
            if (value instanceof String) {
                enabled = Boolean.parseBoolean((String)value);
            } else if (value instanceof Boolean) {
                enabled = ((Boolean)value);
            }
        } else {
            super.setValue(name, value);
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
        Collision c = new Collision();
        copy(c, full);

        if (full) {
            c.enabled = this.enabled;
        }

        return(c);
    }
}
