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
 * Wrapper for an X3D TriangleStripSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class TriangleStripSet extends BaseComposedGeometry {
    
    /** Field value */
    public int[] stripCount;
    
    /** Number of strips in the array */
    public int num_strip;
    
    /**
     * Constructor
     */
    public TriangleStripSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TriangleStripSet(String defName) {
        super("TriangleStripSet", defName);
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
        stripCount = null;
        num_strip = 0;
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
                
                if ( stripCount != null ) {
                    handler.startField("stripCount");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(stripCount, num_strip);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(stripCount, num_strip));
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
        
        if (name.equals("stripCount")) {
            if (value instanceof String) {
                stripCount = fieldReader.MFInt32((String)value);
                num_strip = stripCount.length ;
            } else if (value instanceof String[]) {
                stripCount = fieldReader.MFInt32((String[])value);
                num_strip = stripCount.length;
            } else if (value instanceof int[]) {
                stripCount = (int[])value;
                num_strip = stripCount.length;
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
        
        if (name.equals("stripCount")) {
            if (value instanceof int[]) {
                stripCount = (int[])value;
                num_strip = len;
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
        TriangleStripSet tss = new TriangleStripSet();
        copy(tss, full);
        if (full) {
            if (stripCount != null) {
                tss.num_strip = this.num_strip;
                tss.stripCount = new int[this.num_strip];
                System.arraycopy(this.stripCount, 0, tss.stripCount, 0, this.num_strip);
            }
        }
        return(tss);
    }
}
