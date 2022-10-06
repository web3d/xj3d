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
 * Wrapper for an X3D LineSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class LineSet extends BaseGeometry {
    
    /** Field value */
    public int[] vertexCount;
    
    /** Number of lines in the array */
    public int num_line;
    
    /**
     * Constructor
     */
    public LineSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public LineSet(String defName) {
        super("LineSet", defName);
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
        vertexCount = null;
        num_line = 0;
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
                
                if ( vertexCount != null ) {
                    handler.startField("vertexCount");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(vertexCount, num_line);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(vertexCount, num_line));
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
        
        if (name.equals("vertexCount")) {
            if (value instanceof String) {
                vertexCount = fieldReader.MFInt32((String)value);
                num_line = vertexCount.length ;
            } else if (value instanceof String[]) {
                vertexCount = fieldReader.MFInt32((String[])value);
                num_line = vertexCount.length;
            } else if (value instanceof int[]) {
                vertexCount = (int[])value;
                num_line = vertexCount.length;
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
        
        if (name.equals("vertexCount")) {
            if (value instanceof int[]) {
                vertexCount = (int[])value;
                num_line = len;
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
        LineSet ls = new LineSet();
        copy(ls, full);
        if (full) {
            if (vertexCount != null) {
                ls.num_line = this.num_line;
                ls.vertexCount = new int[this.num_line];
                System.arraycopy(this.vertexCount, 0, ls.vertexCount, 0, this.num_line);
            }
        }
        return(ls);
    }
}
