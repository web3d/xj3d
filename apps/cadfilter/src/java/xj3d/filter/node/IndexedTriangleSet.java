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
 * Wrapper for an X3D IndexedTriangleSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class IndexedTriangleSet extends BaseComposedGeometry {
    
    /** Field value */
    public int[] index;
    
    /** Number of indices in the index array */
    public int num_index;
    
    /**
     * Constructor
     */
    public IndexedTriangleSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public IndexedTriangleSet(String defName) {
        super("IndexedTriangleSet", defName);
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
        index = null;
        num_index = 0;
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
                
                if (index != null) {
                    handler.startField("index");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(index, num_index);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(index, num_index));
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
        
        if (name.equals("index")) {
            if (value instanceof String) {
                index = fieldReader.MFInt32((String)value);
                num_index = index.length ;
            } else if (value instanceof String[]) {
                index = fieldReader.MFInt32((String[])value);
                num_index = index.length;
            } else if (value instanceof int[]) {
                index = (int[])value;
                num_index = index.length;
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
        
        if (name.equals("index")) {
            if (value instanceof int[]) {
                index = (int[])value;
                num_index = len;
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
        IndexedTriangleSet its = new IndexedTriangleSet();
        copy(its, full);
        if (full) {
            if (index != null) {
                its.num_index = this.num_index;
                its.index = new int[this.num_index];
                System.arraycopy(this.index, 0, its.index, 0, this.num_index);
            }
        }
        return(its);
    }
}
