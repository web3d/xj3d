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
 * Base abstract impl wrapper for Geometry nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class BaseGeometry extends BaseEncodable implements IGeometry {
    
    /** The Coordinate node */
    private Encodable coord;
    
    /** The Color node */
    private Encodable color;
    
    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected BaseGeometry(String name, String defName) {
        super(name, defName);
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
        coord = null;
        color = null;
    }
    
    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            
            super.encode();
            
            if (coord != null) {
                handler.startField("coord");
                coord.encode();
                handler.endField();
            }
            
            if (color != null) {
                handler.startField("color");
                color.encode();
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
            case "coord":
                if (value instanceof ICoordinate) {
                    coord = (Encodable)value;
                }   break;
            case "color":
                if (value instanceof IColor) {
                    color = (Encodable)value;
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
        if (deep && (enc instanceof BaseGeometry)) {
            BaseGeometry that = (BaseGeometry)enc;
            if (this.coord != null) {
                that.coord = this.coord.clone(true);
            }
            if (this.color != null) {
                that.color = this.color.clone(true);
            }
        }
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Set the Coordinate node wrapper
     *
     * @param coord The Coordinate node wrapper
     */
    public void setCoordinate(Encodable coord) {
        this.coord = coord;
    }
    
    /**
     * Get the Coordinate node wrapper
     *
     * @return The Coordinate node wrapper
     */
    public Encodable getCoordinate() {
        return(coord);
    }
    
    /**
     * Set the Color node wrapper
     *
     * @param color The Color node wrapper
     */
    public void setColor(Encodable color) {
        this.color = color;
    }
    
    /**
     * Get the Color node wrapper
     *
     * @return The Color node wrapper
     */
    public Encodable getColor() {
        return(color);
    }
}
