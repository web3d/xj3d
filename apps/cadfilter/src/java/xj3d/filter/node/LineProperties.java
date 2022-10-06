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
 * Wrapper for the X3D LineProperties node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class LineProperties extends BaseEncodable implements IAppearanceChild {

    /** Field value */
    public boolean applied;

    /** Field value */
    public int linetype;

    /** Field value */
    public float linewidthScaleFactor;

    /**
     * Constructor
     */
    public LineProperties() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public LineProperties(String defName) {
        super("LineProperties", defName);

        applied = true;
        linetype = 1;
        linewidthScaleFactor = 0;
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
        applied = true;
        linetype = 1;
        linewidthScaleFactor = 0;
    }

    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {

        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);

                super.encode();

                handler.startField("applied");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(applied);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(applied));
                    break;
                }

                if (linetype >= 1) {
                    handler.startField("linetype");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(linetype);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Integer.toString(linetype));
                        break;
                    }
                }

                handler.startField("linewidthScaleFactor");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(linewidthScaleFactor);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Float.toString(linewidthScaleFactor));
                    break;
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
            case "applied":
                if (value instanceof String) {
                    applied = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    applied = ((Boolean)value);
                }
                break;
            case "linetype":
                if (value instanceof String) {
                    linetype = Integer.parseInt((String)value);
                } else if (value instanceof Integer) {
                    linetype = (Integer) value;
                }
                break;
            case "linewidthScaleFactor":
                if (value instanceof String) {
                    linewidthScaleFactor = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    linewidthScaleFactor = (Float) value;
                }
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
        super.setValue(name, value, len);
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
        LineProperties lp = new LineProperties();
        copy(lp, full);
        if (full) {
            lp.applied = this.applied;
            lp.linetype = this.linetype;
            lp.linewidthScaleFactor = this.linewidthScaleFactor;
        }
        return(lp);
    }
}
