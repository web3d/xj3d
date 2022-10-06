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
 * Wrapper for the X3D Shape node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class Shape extends BaseEncodable implements IShape {

    /** The geometry node */
    private Encodable geometry;

    /** The appearance node */
    private Encodable appearance;

    /**
     * Constructor
     */
    public Shape() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Shape(String defName) {
        super("Shape", defName);
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
        geometry = null;
        appearance = null;
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

                if (appearance != null) {
                    handler.startField("appearance");
                    appearance.encode();

                    // TODO: SFNodes should not have endField called
                    handler.endField();
                }

                if (geometry != null) {
                    handler.startField("geometry");
                    geometry.encode();

                    // TODO: SFNodes should not have endField called
                    handler.endField();
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
            case "appearance":
                if (value instanceof IAppearance) {
                    appearance = (Encodable)value;
                }   break;
            case "geometry":
                if (value instanceof IGeometry) {
                    geometry = (Encodable)value;
                }   break;
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
        Shape s = new Shape();
        copy(s, full);
        if (full) {
            if (this.geometry != null) {
                s.geometry = this.geometry.clone(full);
            }
            if (this.appearance != null) {
                s.appearance = this.appearance.clone(full);
            }
        }
        return(s);
    }

    //----------------------------------------------------------
    // Methods defined by IShape
    //----------------------------------------------------------

    /**
     * Set the Geometry node wrapper
     *
     * @param geometry The Geometry node wrapper
     */
    @Override
    public void setGeometry(Encodable geometry) {
        this.geometry = geometry;
    }

    /**
     * Get the Geometry node wrapper
     *
     * @return The Geometry node wrapper
     */
    @Override
    public Encodable getGeometry() {
        return(geometry);
    }

    /**
     * Set the Appearance node wrapper
     *
     * @param appearance The Appearance node wrapper
     */
    @Override
    public void setAppearance(Encodable appearance) {
        this.appearance = appearance;
    }

    /**
     * Get the Appearance node wrapper
     *
     * @return The Appearance node wrapper
     */
    @Override
    public Encodable getAppearance() {
        return(appearance);
    }
}
