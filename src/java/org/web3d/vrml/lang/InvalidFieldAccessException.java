/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

// External imports
// none

// Local imports
// none

/**
 * Exception indicating that a field accessType violation has occurred.
 *  <p>
 *
 * This is generated when a field is modified/read outside its field access
 * parameters.  Typically an initializeOnly field is written to.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class InvalidFieldAccessException extends FieldException {

    /**
     * Create a new exception with no message.
     */
    public InvalidFieldAccessException() {
    }

    /**
     * Create a new exception with a message.
     *
     * @param msg The message associated with this exception
     */
    public InvalidFieldAccessException(String msg) {
        super(msg);
    }

    /**
     * Create a new exception with a message and the name of the field
     * that had the error.
     *
     * @param msg The message associated with this exception
     * @param field The name of the field with the error
     */
    public InvalidFieldAccessException(String msg, String field) {
        super("Invalid field " + field + ": " + msg);
    }

    /**
     * Create a new exception for dealing with an invalid field access value.
     *
     * @param field The index of the field being set
     * @param node The node that we are getting the field set in
     */
    public InvalidFieldAccessException(String field, VRMLNode node) {
        super("Invalid field access: " + field +
              " in node " + node.getClass().getName());
    }

}
