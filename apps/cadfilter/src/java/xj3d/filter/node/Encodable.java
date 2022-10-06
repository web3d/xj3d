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

import org.web3d.vrml.sav.ContentHandler;

/**
 * Primary interface for encoding node representations in this package.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public interface Encodable {

    /**
     * Return the name of the node
     *
     * @return the name of the node
     */
    String getNodeName();

    /**
     * Return the DEF name of the node
     *
     * @return the DEF name of the node
     */
    String getDefName();

    /**
     * Set the DEF name of the node
     *
     * @param defName the DEF name of the node
     */
    void setDefName(String defName);

    /**
     * Return the USE name of the node
     *
     * @return the USE name of the node
     */
    String getUseName();

    /**
     * Set the USE name of the node
     *
     * @param useName the USE name of the node
     */
    void setUseName(String useName);

    /**
     * Clear the node fields to their initial values
     */
    void clear();

    /**
     * Push the node contents to the ContentHandler
     */
    void encode();

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    void setValue(String name, Object value);

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    void setValue(String name, Object value, int len);

    /**
     * Set the content handler.
     *
     * @param handler The ContentHandler instance to use
     */
    void setContentHandler(ContentHandler handler);

    /**
     * Set the reader to use for parsing field values.
     *
     * @param fieldReader The reader
     */
    void setFieldReader(VRMLFieldReader fieldReader);

    /**
     * Create and return a copy of this object.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children. false
     * if the clone should be a new instance of this node type.
     * Typically a full clone is done when the contents of the node
     * will require subsequent modification. A shallow clone is
     * done when a USE function is required.
     * @return a copy of this.
     */
    Encodable clone(boolean full);

    /**
     * Compares this appearance to another and checks if
     * all fields are the same and that all children fields
     * are the same.
     * @return 
     */
    boolean deepEquals(Encodable enc);
}
