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
 * Wrapper for the X3D TextureCoordinate node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class TextureCoordinate extends BaseEncodable implements ITextureCoordinate {
    
    /** Field value */
    public float[] point;
    
    /** Number of coordinate values in the point array */
    public int num_point;
    
    /**
     * Constructor
     */
    public TextureCoordinate() {
        super("TextureCoordinate");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TextureCoordinate(String defName) {
        super("TextureCoordinate", defName);
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
        point = null;
        num_point = 0;
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
                
                if (point != null) {
                    handler.startField("point");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(point, num_point*2);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(point, num_point*2));
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
        
        if (name.equals("point")) {
            if (value instanceof String) {
                point = fieldReader.MFVec2f((String)value);
                num_point = point.length / 2;
            } else if (value instanceof String[]) {
                point = fieldReader.MFVec2f((String[])value);
                num_point = point.length / 2;
            } else if (value instanceof float[]) {
                point = (float[])value;
                num_point = point.length / 2;
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
        
        if (name.equals("point")) {
            if (value instanceof float[]) {
                point = (float[])value;
                num_point = len / 2;
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
        TextureCoordinate tc = new TextureCoordinate();
        copy(tc, full);
        if (full) {
            if (point != null) {
                tc.num_point = this.num_point;
                tc.point = new float[this.num_point*2];
                System.arraycopy(this.point, 0, tc.point, 0, this.num_point*2);
            }
        }
        return(tc);
    }
}
