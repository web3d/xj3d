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
 * Wrapper for the X3D MetadataString node.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class MetadataString extends BaseMetadata {
    
    /** Field value */
    public String[] value;
    
    /** Number of values in the array */
    public int num_value;
    
    /**
     * Constructor
     */
    public MetadataString() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public MetadataString(String defName) {
        super("MetadataString", defName);
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
        value = null;
        num_value = 0;
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
                
                if (value != null) {
                    handler.startField("value");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(value, num_value);
                        break;
                    case HANDLER_STRING:
                        // ? no length, could this cause problems ?
                        sch.fieldValue(value);
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
        
        if (name.equals("value")) {
            if (value instanceof String) {
                this.value = fieldReader.MFString((String)value);
                num_value = this.value.length;
            } else if (value instanceof String[]) {
                this.value = fieldReader.MFString((String[])value);
                num_value = this.value.length;
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
        
        if (name.equals("value")) {
            if (value instanceof String[]) {
                this.value = (String[])value;
                num_value = len;
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
        MetadataString ms = new MetadataString();
        copy(ms, full);
        if (full) {
            if (value != null) {
                ms.num_value = this.num_value;
                ms.value = new String[this.num_value];
                System.arraycopy(this.value, 0, ms.value, 0, this.num_value);
            }
        }
        return(ms);
    }
}
