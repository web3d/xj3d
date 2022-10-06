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
import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.sav.StringContentHandler;

/**
 * Common impl for encoding node representations in this package.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class BaseEncodable implements Encodable {

    /** Content Handler Types */
    public static final int HANDLER_BINARY = 0;
    public static final int HANDLER_STRING = 1;
    public static final int HANDLER_NULL = 2;

    /** The name of this node */
    public final String nodeName;

    /** The node's DEF name */
    public String defName;

    /** The node's USE name */
    public String useName;

    /** The field parser */
    protected VRMLFieldReader fieldReader;

    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, a StringContentHandler, or null */
    protected int handlerType;

    /** Content Handler reference */
    protected ContentHandler handler;

    /** Binary Content Handler reference */
    protected BinaryContentHandler bch;

    /** String Content Handler reference */
    protected StringContentHandler sch;

    /** The Metadata node */
    private Encodable metadata;

    /**
     * Constructor
     * @param nodeName
     */
    protected BaseEncodable(String nodeName) {
        this.nodeName = nodeName;
        handlerType = HANDLER_NULL;
    }

    /**
     * Constructor
     * @param defName
     */
    protected BaseEncodable(String nodeName, String defName) {
        this.nodeName = nodeName;
        this.defName = defName;
        handlerType = HANDLER_NULL;
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Return the name of the node
     *
     * @return the name of the node
     */
    @Override
    public String getNodeName() {
        return(nodeName);
    }
    /**
     * Return the DEF name of the node
     *
     * @return the DEF name of the node
     */
    @Override
    public String getDefName() {
        return(defName);
    }

    /**
     * Set the DEF name of the node
     *
     * @param defName the DEF name of the node
     */
    @Override
    public void setDefName(String defName) {
        this.defName = defName;
    }

    /**
     * Return the USE name of the node
     *
     * @return the USE name of the node
     */
    @Override
    public String getUseName() {
        return(useName);
    }

    /**
     * Set the USE name of the node
     *
     * @param useName the USE name of the node
     */
    @Override
    public void setUseName(String useName) {
        this.useName = useName;
    }

    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        metadata = null;
        defName = null;
        useName = null;
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {

        if (handler != null) {
            if (metadata != null) {
                handler.startField("metadata");
                metadata.encode();
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
        if (name.equals("metadata")) {
            if (value instanceof IMetadata) {
                metadata = (Encodable)value;
            }
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
    }

    /**
     * Set the reader to use for parsing field values.
     *
     * @param fieldReader The reader
     */
    @Override
    public void setFieldReader(VRMLFieldReader fieldReader) {
        this.fieldReader = fieldReader;
    }

    /**
     * Set the content handler.
     *
     * @param handler The ContentHandler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler handler) {

        this.handler = handler;
        if (handler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)handler;
            sch = null;
            handlerType = HANDLER_BINARY;
        } else if (handler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)handler;
            handlerType = HANDLER_STRING;
        } else {
            bch = null;
            sch = null;
            handlerType = HANDLER_NULL;
        }
    }

    /**
     * Create and return a copy of this object.
     * Must be overridden, by default returns null.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children,
     * false returns a new instance of this node type.
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        return(null);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the Metadata node wrapper
     *
     * @param metadata The Metadata node wrapper
     */
    public void setMetadata(Encodable metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the Metadata node wrapper
     *
     * @return The Metadata node wrapper
     */
    public Encodable getMetadata() {
        return(metadata);
    }

    /**
     * Copy the working objects of this into the argument. Used
     * by subclasses to initialize a clone. The defName and useName
     * are never copied. The deep flag has no effect on this level,
     * the base objects are always copied.
     *
     * @param enc The encodable to initialize.
     * @param deep true to initialize this nodes fields, false
     * otherwise.
     */
    protected void copy(BaseEncodable enc, boolean deep) {
        enc.fieldReader = this.fieldReader;
        enc.handlerType = this.handlerType;
        enc.handler = this.handler;
        enc.bch = this.bch;
        enc.sch = this.sch;
    }

    /**
     * Compares this appearance to another and checks if
     * all fields are the same and that all children fields
     * are the same.
     * @return 
     */
    @Override
    public boolean deepEquals(Encodable enc) {
        return false;
    }
}
