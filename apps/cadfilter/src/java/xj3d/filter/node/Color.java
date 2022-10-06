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
 * Wrapper for the X3D Color node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class Color extends BaseEncodable implements IColor {
    
    /** Field value */
    public float[] color;
    
    /** Number of coordinate values in the color array */
    public int num_color;
    
    /**
     * Constructor
     */
    public Color() {
        super("Color");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Color(String defName) {
        super("Color", defName);
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
        color = null;
        num_color = 0;
    }
    
    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            handler.startNode(nodeName, defName);
            
           super.encode();
                
            if (color != null) {
                handler.startField("color");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(color, num_color*3);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(FieldValueHandler.toString(color, num_color*3));
                    break;
                }
            }
            handler.endNode();
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
        
        if (name.equals("color")) {
            if (value instanceof String) {
                color = fieldReader.MFColor((String)value);
                num_color = color.length / 3;
            } else if (value instanceof String[]) {
                color = fieldReader.MFColor((String[])value);
                num_color = color.length / 3;
            } else if (value instanceof float[]) {
                color = (float[])value;
                num_color = color.length / 3;
            }
        } else {
            super.setValue(name, value);
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
        
        if (name.equals("color")) {
            if (value instanceof float[]) {
                color = (float[])value;
                num_color = len / 3;
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
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        Color c = new Color();
        copy(c, full);
        if (full) {
            if (color != null) {
                c.num_color = this.num_color;
                c.color = new float[this.num_color*3];
                System.arraycopy(this.color, 0, c.color, 0, this.num_color*3);
            }
        }
        return(c);
    }
}
