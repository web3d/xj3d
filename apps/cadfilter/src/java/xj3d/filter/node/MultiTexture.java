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
import java.util.ArrayList;

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D MultiTexture node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class MultiTexture extends BaseEncodable implements ITexture {

    /** Field value */
    public float alpha;

    /** Field value */
    public float[] color;

    /** Field value */
    public String[] function;

    /** Number of functions in the array */
    public int num_function;

    /** Field value */
    public String[] mode;

    /** Number of modes in the array */
    public int num_mode;

    /** Field value */
    public String[] source;

    /** Number of sources in the array */
    public int num_source;

    /** The Texture nodes */
    private ArrayList<Encodable> texture;

    /**
     * Constructor
     */
    public MultiTexture() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public MultiTexture(String defName) {
        super("MultiTexture", defName);
        texture = new ArrayList<>();

        alpha = 1;
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

        alpha = 1;
        color = null;
        function = null;
        num_function = 0;
        mode = null;
        num_mode = 0;
        source = null;
        num_source = 0;
        texture.clear();
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

                if ((alpha >= 0) && (alpha <= 1)) {
                    handler.startField("alpha");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(alpha);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(alpha));
                        break;
                    }
                }

                if (color != null) {
                    handler.startField("color");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(color, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(color, 3));
                        break;
                    }
                }

                if (function != null) {
                    handler.startField("function");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(function, num_function);
                        break;
                    case HANDLER_STRING:
                        // ? no length, could this cause problems ?
                        sch.fieldValue(function);
                        break;
                    }
                }

                if (mode != null) {
                    handler.startField("mode");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(mode, num_mode);
                        break;
                    case HANDLER_STRING:
                        // ? no length, could this cause problems ?
                        sch.fieldValue(mode);
                        break;
                    }
                }

                if (source != null) {
                    handler.startField("source");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(source, num_source);
                        break;
                    case HANDLER_STRING:
                        // ? no length, could this cause problems ?
                        sch.fieldValue(source);
                        break;
                    }
                }

                for (Encodable e : texture) {
                    e.encode();
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
            case "alpha":
                if (value instanceof String) {
                    alpha = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    alpha = (Float) value;
                }
                break;
            case "color":
                if (value instanceof String) {
                    color = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    color = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    color = (float[])value;
                }   break;
            case "function":
                if (value instanceof String) {
                    this.function = fieldReader.MFString((String)value);
                    num_function = this.function.length;
                } else if (value instanceof String[]) {
                    this.function = fieldReader.MFString((String[])value);
                    num_function = this.function.length;
                }   break;
            case "mode":
                if (value instanceof String) {
                    this.mode = fieldReader.MFString((String)value);
                    num_mode = this.mode.length;
                } else if (value instanceof String[]) {
                    this.mode = fieldReader.MFString((String[])value);
                    num_mode = this.mode.length;
                }   break;
            case "source":
                if (value instanceof String) {
                    this.source = fieldReader.MFString((String)value);
                    num_source = this.source.length;
                } else if (value instanceof String[]) {
                    this.source = fieldReader.MFString((String[])value);
                num_source = this.source.length;
            }   break;
            case "texture":
                if (value instanceof ITexture) {
                    texture.add((Encodable) value);
                } else if (value instanceof Encodable[]) {
                    Encodable[] enc = (Encodable[])value;
                    for (Encodable enc1 : enc) {
                        if (enc1 instanceof ITexture) {
                            texture.add(enc1);
                        }
                    }
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

        switch (name) {
            case "color":
                if (value instanceof float[]) {
                    color = (float[])value;
                }   break;
            case "function":
                if (value instanceof String[]) {
                    this.function = (String[])value;
                    num_function = len;
                }   break;
            case "mode":
                if (value instanceof String[]) {
                    this.mode = (String[])value;
                    num_mode = len;
            }   break;
            case "source":
                if (value instanceof String[]) {
                    this.source = (String[])value;
                    num_source = len;
            }   break;
            case "texture":
                if (value instanceof Encodable[]) {
                    Encodable[] enc = (Encodable[])value;
                    for (int i = 0; i < len; i++) {
                        if (enc[i] instanceof ITexture) {
                            texture.add(enc[i]);
                        }
                }
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
        MultiTexture mt = new MultiTexture();
        copy(mt, full);
        if (full) {
            mt.alpha = this.alpha;
            if (this.color != null) {
                mt.color = new float[3];
                System.arraycopy(this.color, 0, mt.color, 0, 3);
            }
            if (function != null) {
                mt.num_function = this.num_function;
                mt.function = new String[this.num_function];
                System.arraycopy(this.function, 0, mt.function, 0, this.num_function);
            }
            if (mode != null) {
                mt.num_mode = this.num_mode;
                mt.mode = new String[this.num_mode];
                System.arraycopy(this.mode, 0, mt.mode, 0, this.num_mode);
            }
            if (source != null) {
                mt.num_source = this.num_source;
                mt.source = new String[this.num_source];
                System.arraycopy(this.source, 0, mt.source, 0, this.num_source);
            }
            for (Encodable e : texture) {
                mt.addTexture(e.clone(true));
            }
        }
        return(mt);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Clear and set the Textures of this node.
     *
     * @param enc The Texture nodes to set.
     * A value of null just performs a clear.
     */
    public void setTexture(Encodable[] enc) {
        texture.clear();
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof ITexture) {
                    texture.add(enc1);
                }
            }
        }
    }

    /**
     * Return the texture of this node.
     *
     * @return The Texture nodes of this node.
     */
    public Encodable[] getTexture() {
        return(texture.toArray(new Encodable[texture.size()]));
    }

    /**
     * Add Textures to this node.
     *
     * @param enc The Texture nodes to add to this node.
     */
    public void addTexture(Encodable[] enc) {
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof ITexture) {
                    texture.add(enc1);
                }
            }
        }
    }

    /**
     * Add a Texture to this node.
     *
     * @param enc A Texture node to add to this node.
     */
    public void addTexture(Encodable enc) {
        if ((enc != null) && (enc instanceof ITexture)) {
            texture.add(enc);
        }
    }
}
