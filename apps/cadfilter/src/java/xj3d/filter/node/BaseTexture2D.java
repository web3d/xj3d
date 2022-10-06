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
 * Base abstract impl wrapper for X3DTexture2DNodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class BaseTexture2D extends BaseEncodable implements ITexture2D {

    /** Field value */
    public boolean repeatS;

    /** Field value */
    public boolean repeatT;

    /** The TextureProperties node */
    private Encodable textureProperties;

    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected BaseTexture2D(String name, String defName) {
        super(name, defName);

        repeatS = true;
        repeatT = true;
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

        repeatS = true;
        repeatT = true;
        textureProperties = null;
    }

    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {

        if (handler != null) {

            super.encode();

            if(!repeatS) {
                handler.startField("repeatS");
                switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(repeatS);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Boolean.toString(repeatS));
                        break;
                }
            }

            if(!repeatT) {
                handler.startField("repeatT");
                switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(repeatT);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Boolean.toString(repeatT));
                        break;
                }
            }

            if (textureProperties != null) {
                handler.startField("textureProperties");
                textureProperties.encode();
                handler.endField();
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
            case "repeatS":
                if (value instanceof String) {
                    repeatS = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    repeatS = ((Boolean)value);
                }
                break;
            case "repeatT":
                if (value instanceof String) {
                    repeatT = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    repeatT = ((Boolean)value);
                }
                break;
            case "textureProperties":
                if (value instanceof TextureProperties) {
                    textureProperties = (Encodable)value;
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

    //----------------------------------------------------------
    // Methods defined by BaseEncodable
    //----------------------------------------------------------

    /**
     * Copy the working objects of this into the argument. Used
     * by subclasses to initialize a clone.
     *
     * @param enc The encodable to initialize.
     * @param deep true to initialize this nodes fields, false
     * otherwise.
     */
    @Override
    protected void copy(BaseEncodable enc, boolean deep) {
        super.copy(enc, deep);
        if (deep && (enc instanceof BaseTexture2D)) {
            BaseTexture2D that = (BaseTexture2D)enc;
            that.repeatS = this.repeatS;
            that.repeatT = this.repeatT;
            if (this.textureProperties != null) {
                that.textureProperties = this.textureProperties.clone(true);
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the TextureProperties node wrapper
     *
     * @param textureProperties The TextureProperties node wrapper
     */
    public void setTextureProperties(Encodable textureProperties) {
        this.textureProperties = textureProperties;
    }

    /**
     * Get the TextureProperties node wrapper
     *
     * @return The TextureProperties node wrapper
     */
    public Encodable getTextureProperties() {
        return(textureProperties);
    }

}
