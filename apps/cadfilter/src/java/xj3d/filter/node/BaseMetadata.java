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
 * Base abstract impl wrapper for Metadata nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public abstract class BaseMetadata extends BaseEncodable implements IMetadata {
    
    /** Field value */
    public String name;
    
    /** Field value */
    public String reference;
    
    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected BaseMetadata(String name, String defName) {
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
        name = null;
        reference = null;
    }
    
    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            
            super.encode();
            
            if (name != null) {
                handler.startField("name");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(name);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(name);
                    break;
                }
            }
            if (reference != null) {
                handler.startField("reference");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(reference);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(reference);
                    break;
                }
            }
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param fieldName The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String fieldName, Object value) {
        
        switch (fieldName) {
            case "name":
                if (value instanceof String) {
                    name = (String)value;
                }   break;
            case "reference":
                if (value instanceof String) {
                    reference = (String)value;
                }   break;
            default:
                super.setValue(fieldName, value);
                break;
        }
    }
    
    /**
     * Set the value of the named field.
     *
     * @param fieldName The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String fieldName, Object value, int len) {
        super.setValue(fieldName, value, len);
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
        if (deep && (enc instanceof BaseMetadata)) {
            BaseMetadata that = (BaseMetadata)enc;
            that.name = this.name;
            that.reference = this.reference;
        }
    }
}
