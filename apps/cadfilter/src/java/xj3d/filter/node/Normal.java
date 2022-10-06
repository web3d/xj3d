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
 * Wrapper for the X3D Normal node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class Normal extends BaseEncodable implements INormal {
    
    /** Field value */
    public float[] vector;
    
    /** Number of coordinate values in the vector array */
    public int num_vector;
    
    /**
     * Constructor
     */
    public Normal() {
        super("Normal");
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Normal(String defName) {
        super("Normal", defName);
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
        vector = null;
        num_vector = 0;
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
                
                if (vector != null) {
                    handler.startField("vector");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(vector, num_vector*3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(vector, num_vector*3));
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
        
        if (name.equals("vector")) {
            if (value instanceof String) {
                vector = fieldReader.MFVec3f((String)value);
                num_vector = vector.length / 3;
            } else if (value instanceof String[]) {
                vector = fieldReader.MFVec3f((String[])value);
                num_vector = vector.length / 3;
            } else if (value instanceof float[]) {
                vector = (float[])value;
                num_vector = vector.length / 3;
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
        
        if (name.equals("vector")) {
            if (value instanceof float[]) {
                vector = (float[])value;
                num_vector = len / 3;
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
        Normal n = new Normal();
        copy(n, full);
        if (full) {
            if (vector != null) {
                n.num_vector = this.num_vector;
                n.vector = new float[this.num_vector*3];
                System.arraycopy(this.vector, 0, n.vector, 0, this.num_vector*3);
            }
        }
        return(n);
    }
}
