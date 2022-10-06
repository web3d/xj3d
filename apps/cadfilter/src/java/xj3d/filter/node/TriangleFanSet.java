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
 * Wrapper for an X3D TriangleFanSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class TriangleFanSet extends BaseComposedGeometry {
    
    /** Field value */
    public int[] fanCount;
    
    /** Number of fans in the array */
    public int num_fan;
    
    /**
     * Constructor
     */
    public TriangleFanSet() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public TriangleFanSet(String defName) {
        super("TriangleFanSet", defName);
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
        fanCount = null;
        num_fan = 0;
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
                
                if ( fanCount != null ) {
                    handler.startField("fanCount");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(fanCount, num_fan);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(fanCount, num_fan));
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
        
        if (name.equals("fanCount")) {
            if (value instanceof String) {
                fanCount = fieldReader.MFInt32((String)value);
                num_fan = fanCount.length ;
            } else if (value instanceof String[]) {
                fanCount = fieldReader.MFInt32((String[])value);
                num_fan = fanCount.length;
            } else if (value instanceof int[]) {
                fanCount = (int[])value;
                num_fan = fanCount.length;
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
        
        if (name.equals("fanCount")) {
            if (value instanceof int[]) {
                fanCount = (int[])value;
                num_fan = len;
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
        TriangleFanSet tfs = new TriangleFanSet();
        copy(tfs, full);
        if (full) {
            if (fanCount != null) {
                tfs.num_fan = this.num_fan;
                tfs.fanCount = new int[this.num_fan];
                System.arraycopy(this.fanCount, 0, tfs.fanCount, 0, this.num_fan);
            }
        }
        return(tfs);
    }
}
