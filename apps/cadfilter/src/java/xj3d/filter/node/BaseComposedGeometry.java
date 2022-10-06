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
 * Base abstract impl wrapper for X3DComposedGeometry nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class BaseComposedGeometry extends BaseGeometry implements IComposedGeometry {
    
    /** Field value */
    public boolean ccw;
    
    /** Field value */
    public boolean colorPerVertex;
    
    /** Field value */
    public boolean normalPerVertex;
    
    /** Field value */
    public boolean solid;
    
    /** The Normal node */
    private Encodable normal;
    
    /** The TextureCoordinate node */
    private Encodable texCoord;
    
    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected BaseComposedGeometry(String name, String defName) {
        super(name, defName);
        
        ccw = true;
        colorPerVertex = true;
        normalPerVertex = true;
        solid = true;
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
        
        normal = null;
        texCoord = null;
        ccw = true;
        colorPerVertex = true;
        normalPerVertex = true;
        solid = true;
    }
    
    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            
            super.encode();
            
            if (normal != null) {
                handler.startField("normal");
                normal.encode();
                handler.endField();
            }
            
            if (texCoord != null) {
                handler.startField("texCoord");
                texCoord.encode();
                handler.endField();
            }
            
            handler.startField("ccw");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(ccw);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(ccw));
                break;
            }
            
            handler.startField("colorPerVertex");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(colorPerVertex);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(colorPerVertex));
                break;
            }
            
            handler.startField("normalPerVertex");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(normalPerVertex);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(normalPerVertex));
                break;
            }
            
            handler.startField("solid");
            switch (handlerType) {
            case HANDLER_BINARY:
                bch.fieldValue(solid);
                break;
            case HANDLER_STRING:
                sch.fieldValue(Boolean.toString(solid));
                break;
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
            case "normal":
                if (value instanceof INormal) {
                    normal = (Encodable)value;
                }   break;
            case "texCoord":
                if (value instanceof ITextureCoordinate) {
                    texCoord = (Encodable)value;
                }   break;
            case "ccw":
                if (value instanceof String) {
                    ccw = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    ccw = ((Boolean)value);
                }
                break;
            case "colorPerVertex":
                if (value instanceof String) {
                    colorPerVertex = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    colorPerVertex = ((Boolean)value);
                }
                break;
            case "normalPerVertex":
                if (value instanceof String) {
                    normalPerVertex = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    normalPerVertex = ((Boolean)value);
                }
                break;
            case "solid":
                if (value instanceof String) {
                    solid = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    solid = ((Boolean)value);
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
        if (deep && (enc instanceof BaseComposedGeometry)) {
            BaseComposedGeometry that = (BaseComposedGeometry)enc;
            that.ccw = this.ccw;
            that.colorPerVertex = this.colorPerVertex;
            that.normalPerVertex = this.normalPerVertex;
            that.solid = this.solid;
            if (this.normal != null) {
                that.normal = this.normal.clone(true);
            }
            if (this.texCoord != null) {
                that.texCoord = this.texCoord.clone(true);
            }
        }
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Set the Normal node wrapper
     *
     * @param normal The Normal node wrapper
     */
    @Override
    public void setNormal(Encodable normal) {
        this.normal = normal;
    }
    
    /**
     * Get the Normal node wrapper
     *
     * @return The Normal node wrapper
     */
    @Override
    public Encodable getNormal() {
        return(normal);
    }
    
    /**
     * Set the TextureCoordinate node wrapper
     *
     * @param texCoord The TextureCoordinate node wrapper
     */
    @Override
    public void setTextureCoordinate(Encodable texCoord) {
        this.texCoord = texCoord;
    }
    
    /**
     * Get the TextureCoordinate node wrapper
     *
     * @return The TextureCoordinate node wrapper
     */
    @Override
    public Encodable getTextureCoordinate() {
        return(texCoord);
    }
}
