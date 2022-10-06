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
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D FillProperties node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class FillProperties extends BaseEncodable implements IAppearanceChild {

    /** Field value */
    public boolean filled;

    /** Field value */
    public float[] hatchColor;

    /** Field value */
    public boolean hatched;

    /** Field value */
    public int hatchStyle;

    /**
     * Constructor
     */
    public FillProperties() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public FillProperties(String defName) {
        super("FillProperties", defName);

        filled = true;
        hatched = true;
        hatchStyle = 1;
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
        filled = true;
        hatchColor = null;
        hatched = true;
        hatchStyle = 1;
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

                handler.startField("filled");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(filled);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(filled));
                    break;
                }

                if (hatchColor != null) {
                    handler.startField("hatchColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(hatchColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(hatchColor, 3));
                        break;
                    }
                }

                handler.startField("hatched");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(hatched);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(hatched));
                    break;
                }

                if (hatchStyle >= 0) {
                    handler.startField("hatchStyle");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(hatchStyle);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Integer.toString(hatchStyle));
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
            case "filled":
                if (value instanceof String) {
                    filled = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    filled = ((Boolean)value);
                }
                break;
            case "hatchColor":
                if (value instanceof String) {
                    hatchColor = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    hatchColor = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    hatchColor = (float[])value;
                }   break;
            case "hatched":
                if (value instanceof String) {
                    hatched = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    hatched = ((Boolean)value);
                }
                break;
            case "hatchStyle":
                if (value instanceof String) {
                    hatchStyle = Integer.parseInt((String)value);
                } else if (value instanceof Integer) {
                    hatchStyle = (Integer) value;
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

        if (name.equals("hatchColor")) {
            if (value instanceof float[]) {
                hatchColor = (float[])value;
            }
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
        FillProperties fp = new FillProperties();
        copy(fp, full);
        if (full) {
            fp.filled = this.filled;
            fp.hatched = this.hatched;
            fp.hatchStyle = this.hatchStyle;
            if (this.hatchColor != null) {
                fp.hatchColor = new float[3];
                System.arraycopy(this.hatchColor, 0, fp.hatchColor, 0, 3);
            }
        }
        return(fp);
    }
}
