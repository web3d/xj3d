/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package vrml.field;

// Standard imports
// none

// Application specific imports
import vrml.ConstField;

/**
 * Constant VRML JSAI type class containing a single String value.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFString extends ConstField {

    /** The data that the field contains */
    protected String data;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFString() {
    }

    /**
     * Construct a new constant field based on the given value.
     *
     * @param value The value to copy
     */
    public ConstSFString(String value) {
        data = value;
    }

    /**
     * Fetch the value of the field.
     *
     * @return The value at that position
     */
    public String getValue() {
        return data;
    }

    /**
     * Create a string representation of the field value.
     *
     * @return A string representing the value.
     */
    @Override
    public String toString() {
        return "\"" + data + "\"";
    }

    /**
     * Make a clone of this object.
     *
     * @return A copy of the field and its data
     */
    @Override
    public Object clone() {
        return new ConstSFString(data);
    }
}
