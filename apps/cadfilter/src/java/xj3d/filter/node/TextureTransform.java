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
 * Wrapper for an X3D TextureTransform node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class TextureTransform extends BaseEncodable implements ITextureTransform {

    /** Field value */
    public float[] center;

    /** Field value */
    public float rotation;

    /** Field value */
    public float[] scale;

    /** Field value */
    public float[] translation;

    /**
     * Constructor
     */
    public TextureTransform() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TextureTransform(String defName) {
        super("TextureTransform", defName);
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
        center = null;
        rotation = 0;
        scale = null;
        translation = null;
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

                if (center != null) {
                    handler.startField("center");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(center, 2);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(center, 2));
                        break;
                    }
                }

                if (scale != null) {
                    handler.startField("scale");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(scale, 2);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(scale, 2));
                        break;
                    }
                }

                if (translation != null) {
                    handler.startField("translation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(translation, 2);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(translation, 2));
                        break;
                    }
                }

                handler.startField("rotation");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(rotation);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Float.toString(rotation));
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
            case "center":
                if (value instanceof String) {
                    center = fieldReader.SFVec2f((String)value);
                } else if (value instanceof String[]) {
                    center = fieldReader.SFVec2f((String[])value);
                } else if (value instanceof float[]) {
                    center = (float[])value;
                }   break;
            case "rotation":
                if (value instanceof String) {
                    rotation = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    rotation = (Float) value;
                }
                break;
            case "scale":
                if (value instanceof String) {
                    scale = fieldReader.SFVec2f((String)value);
                } else if (value instanceof String[]) {
                    scale = fieldReader.SFVec2f((String[])value);
                } else if (value instanceof float[]) {
                    scale = (float[])value;
            }   break;
            case "translation":
                if (value instanceof String) {
                    translation = fieldReader.SFVec2f((String)value);
                } else if (value instanceof String[]) {
                    translation = fieldReader.SFVec2f((String[])value);
                } else if (value instanceof float[]) {
                    translation = (float[])value;
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

        switch (name) {
            case "center":
                if (value instanceof float[]) {
                    center = (float[])value;
                }   break;
            case "scale":
                if (value instanceof float[]) {
                    scale = (float[])value;
                }   break;
            case "translation":
                if (value instanceof float[]) {
                    translation = (float[])value;
            }   break;
            default:
                super.setValue(name, value, len);
                break;
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
        TextureTransform tt = new TextureTransform();
        copy(tt, full);
        if (full) {
            if (this.translation != null) {
                tt.translation = new float[2];
                System.arraycopy(this.translation, 0, tt.translation, 0, 2);
            }
            if (this.center != null) {
                tt.center = new float[2];
                System.arraycopy(this.center, 0, tt.center, 0, 2);
            }
            tt.rotation = this.rotation;
            if (this.scale != null) {
                tt.scale = new float[2];
                System.arraycopy(this.scale, 0, tt.scale, 0, 2);
            }
        }
        return(tt);
    }
}
