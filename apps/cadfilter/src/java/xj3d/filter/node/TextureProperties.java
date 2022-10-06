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
 * Wrapper for the X3D TextureProperties node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class TextureProperties extends BaseEncodable implements IAppearanceChild {

    /** Field value */
    public float anisotropicDegree;

    /** Field value */
    public float[] borderColor;

    /** Field value */
    public int borderWidth;

    /** Field value */
    public String boundaryModeS;

    /** Field value */
    public String boundaryModeT;

    /** Field value */
    public String boundaryModeR;

    /** Field value */
    public String magnificationFilter;

    /** Field value */
    public String minificationFilter;

    /** Field value */
    public String textureCompression;

    /** Field value */
    public float texturePriority;

    /** Field value */
    public boolean generateMipMaps;

    /**
     * Constructor
     */
    public TextureProperties() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TextureProperties(String defName) {
        super("TextureProperties", defName);

        anisotropicDegree = 1;
        generateMipMaps = true;
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
        anisotropicDegree = 1;
        borderColor = null;
        borderWidth = 0;
        boundaryModeS = null;
        boundaryModeT = null;
        boundaryModeR = null;
        magnificationFilter = null;
        minificationFilter = null;
        textureCompression = null;
        texturePriority = 0;
        generateMipMaps = true;
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

                if (anisotropicDegree >= 1) {
                    handler.startField("anisotropicDegree");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(anisotropicDegree);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(anisotropicDegree));
                        break;
                    }
                }

                if (borderColor != null) {
                    handler.startField("borderColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(borderColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(borderColor, 3));
                        break;
                    }
                }

                if ((borderWidth >= 0) && (borderWidth <= 1)) {
                    handler.startField("borderWidth");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(borderWidth);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Integer.toString(borderWidth));
                        break;
                    }
                }

                if (boundaryModeS != null) {
                    handler.startField("boundaryModeS");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(boundaryModeS);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(boundaryModeS);
                        break;
                    }
                }

                if (boundaryModeT != null) {
                    handler.startField("boundaryModeT");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(boundaryModeT);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(boundaryModeT);
                        break;
                    }
                }

                if (boundaryModeR != null) {
                    handler.startField("boundaryModeR");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(boundaryModeR);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(boundaryModeR);
                        break;
                    }
                }

                if (magnificationFilter != null) {
                    handler.startField("magnificationFilter");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(magnificationFilter);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(magnificationFilter);
                        break;
                    }
                }

                if (minificationFilter != null) {
                    handler.startField("minificationFilter");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(minificationFilter);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(minificationFilter);
                        break;
                    }
                }

                if (textureCompression != null) {
                    handler.startField("textureCompression");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(textureCompression);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(textureCompression);
                        break;
                    }
                }

                if ((texturePriority >= 0) && (texturePriority <= 1)) {
                    handler.startField("texturePriority");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(texturePriority);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(texturePriority));
                        break;
                    }
                }

                handler.startField("generateMipMaps");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(generateMipMaps);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(generateMipMaps));
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
            case "anisotropicDegree":
                if (value instanceof String) {
                    anisotropicDegree = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    anisotropicDegree = (Float) value;
                }
                break;
            case "borderColor":
                if (value instanceof String) {
                    borderColor = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    borderColor = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    borderColor = (float[])value;
                }   break;
            case "borderWidth":
                if (value instanceof String) {
                    borderWidth = Integer.parseInt((String)value);
                } else if (value instanceof Integer) {
                    borderWidth = (Integer) value;
                }
                break;
            case "boundaryModeS":
                if (value instanceof String) {
                    boundaryModeS = (String)value;
            }   break;
            case "boundaryModeT":
                if (value instanceof String) {
                    boundaryModeT = (String)value;
            }   break;
            case "boundaryModeR":
                if (value instanceof String) {
                    boundaryModeR = (String)value;
            }   break;
            case "magnificationFilter":
                if (value instanceof String) {
                    magnificationFilter = (String)value;
            }   break;
            case "minificationFilter":
                if (value instanceof String) {
                    minificationFilter = (String)value;
            }   break;
            case "textureCompression":
                if (value instanceof String) {
                    textureCompression = (String)value;
            }   break;
            case "texturePriority":
                if (value instanceof String) {
                    texturePriority = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    texturePriority = (Float) value;
                }
                break;
            case "generateMipMaps":
                if (value instanceof String) {
                    generateMipMaps = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    generateMipMaps = ((Boolean)value);
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

        if (name.equals("borderColor")) {
            if (value instanceof float[]) {
                borderColor = (float[])value;
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
        TextureProperties tp = new TextureProperties();
        copy(tp, full);
        if (full) {
            tp.anisotropicDegree = this.anisotropicDegree;
            if (this.borderColor != null) {
                tp.borderColor = new float[3];
                System.arraycopy(this.borderColor, 0, tp.borderColor, 0, 3);
            }
            tp.borderWidth = this.borderWidth;
            tp.boundaryModeS = this.boundaryModeS;
            tp.boundaryModeT = this.boundaryModeT;
            tp.boundaryModeR = this.boundaryModeR;
            tp.magnificationFilter = this.magnificationFilter;
            tp.minificationFilter = this.minificationFilter;
            tp.textureCompression = this.textureCompression;
            tp.texturePriority = this.texturePriority;
            tp.generateMipMaps = this.generateMipMaps;
        }
        return(tp);
    }
}
