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
 * Wrapper for the X3D Appearance node.
 * <p>
 * Note: This impl is missing the shaders field.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class Appearance extends BaseEncodable implements IAppearance {

    /** The FillProperties node */
    private Encodable fillProperties;

    /** The LineProperties node */
    private Encodable lineProperties;

    /** The Material node */
    private Encodable material;

    /** The Texture node */
    private Encodable texture;

    /** The TextureTransform node */
    private Encodable textureTransform;

    /**
     * Constructor
     */
    public Appearance() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Appearance(String defName) {
        super("Appearance", defName);
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
        fillProperties = null;
        lineProperties = null;
        material = null;
        texture = null;
        textureTransform = null;
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {

        if (handler != null) {

            super.encode();

            if (useName == null) {
                handler.startNode(nodeName, defName);

                if (fillProperties != null) {
                    handler.startField("fillProperties");
                    fillProperties.encode();
                    handler.endField();
                }

                if (lineProperties != null) {
                    handler.startField("lineProperties");
                    lineProperties.encode();
                    handler.endField();
                }

                if (material != null) {
                    handler.startField("material");
                    material.encode();
                    handler.endField();
                }

                if (texture != null) {
                    handler.startField("texture");
                    texture.encode();
                    handler.endField();
                }

                if (textureTransform != null) {
                    handler.startField("textureTransform");
                    textureTransform.encode();
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
            case "fillProperties":
                if (value instanceof FillProperties) {
                    fillProperties = (Encodable)value;
                }   break;
            case "lineProperties":
                if (value instanceof LineProperties) {
                    lineProperties = (Encodable)value;
                }   break;
            case "material":
                if (value instanceof IMaterial) {
                    material = (Encodable)value;
            }   break;
            case "texture":
                if (value instanceof ITexture) {
                    texture = (Encodable)value;
            }   break;
            case "textureTransform":
                if (value instanceof ITextureTransform) {
                    textureTransform = (Encodable)value;
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
        Appearance a = new Appearance();
        copy(a, full);
        if (full) {
            if (this.fillProperties != null) {
                a.fillProperties = this.fillProperties.clone(full);
            }
            if (this.lineProperties != null) {
                a.lineProperties = this.lineProperties.clone(full);
            }
            if (this.material != null) {
                a.material = this.material.clone(full);
            }
            if (this.texture != null) {
                a.texture = this.texture.clone(full);
            }
            if (this.textureTransform != null) {
                a.textureTransform = this.textureTransform.clone(full);
            }
        }
        return(a);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the FillProperties node wrapper
     *
     * @param fillProperties The FillProperties node wrapper
     */
    public void setFillProperties(Encodable fillProperties) {
        this.fillProperties = fillProperties;
    }

    /**
     * Get the FillProperties node wrapper
     *
     * @return The FillProperties node wrapper
     */
    public Encodable getFillProperties() {
        return(fillProperties);
    }

    /**
     * Set the LineProperties node wrapper
     *
     * @param lineProperties The LineProperties node wrapper
     */
    public void setLineProperties(Encodable lineProperties) {
        this.lineProperties = lineProperties;
    }

    /**
     * Get the LineProperties node wrapper
     *
     * @return The LineProperties node wrapper
     */
    public Encodable getLineProperties() {
        return(lineProperties);
    }

    /**
     * Set the Material node wrapper
     *
     * @param material The Material node wrapper
     */
    public void setMaterial(Encodable material) {
        this.material = material;
    }

    /**
     * Get the Material node wrapper
     *
     * @return The Material node wrapper
     */
    public Encodable getMaterial() {
        return(material);
    }

    /**
     * Set the Texture node wrapper
     *
     * @param texture The Texture node wrapper
     */
    public void setTexture(Encodable texture) {
        this.texture = texture;
    }

    /**
     * Get the Texture node wrapper
     *
     * @return The Texture node wrapper
     */
    public Encodable getTexture() {
        return(texture);
    }

    /**
     * Set the TextureTransform node wrapper
     *
     * @param textureTransform The TextureTransform node wrapper
     */
    public void setTextureTransform(Encodable textureTransform) {
        this.textureTransform = textureTransform;
    }

    /**
     * Get the TextureTransform node wrapper
     *
     * @return The TextureTransform node wrapper
     */
    public Encodable getTextureTransform() {
        return(textureTransform);
    }

    /**
     * Compares this appearance to another and checks if
     * all fields are the same and that all children fields
     * are the same.
     * @return 
     */
    @Override
    public boolean deepEquals(Encodable enc) {
        if (!(enc instanceof Appearance))
            return false;

        Appearance app = (Appearance) enc;

        Encodable node2 = app.getFillProperties();
        if (fillProperties != null) {
            if (node2 == null)
                return false;

            if (!fillProperties.deepEquals(node2))
                return false;
        } else if (node2 != null)
            return false;

        node2 = app.getLineProperties();
        if (lineProperties != null) {
            if (node2 == null)
                return false;

            if (!lineProperties.deepEquals(node2))
                return false;
        } else if (node2 != null)
            return false;


        node2 = app.getMaterial();
        if (material != null) {
            if (node2 == null)
                return false;

            if (!material.deepEquals(node2))
                return false;
        } else if (node2 != null)
            return false;

        node2 = app.getTexture();
        if (texture != null) {
            if (node2 == null)
                return false;

            if (!texture.deepEquals(node2))
                return false;
        } else if (node2 != null)
            return false;

        node2 = app.getTextureTransform();
        if (textureTransform != null) {
            if (node2 == null)
                return false;

            if (!textureTransform.deepEquals(node2))
                return false;
        } else if (node2 != null)
            return false;

        return true;
    }
}
