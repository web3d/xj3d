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

package org.web3d.vrml.scripting.ecmascript.builtin;

// Standard imports
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.j3d.util.HashSet;

/**
 * ProfileInfo miscellaneous object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class FieldDefinitionArray extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private final static HashSet<String> propertyNames;

    /** List of components in the array */
    private final X3DFieldDefinition[] fields;

    /** Length of the array as a property */
    private Integer length;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("length");
    }

    /**
     * Allow creating an empty list of objects. Local only access as it
     * is only used by the execution context handling.
     */
    FieldDefinitionArray() {
        super("FieldDefinitionArray");
        length = 0;
        fields = null;
    }

    /**
     * Construct a profile descriptor for the given information.
     * @param fields our X3D field definitions
     */
    public FieldDefinitionArray(X3DFieldDefinition[] fields) {
        super("FieldDefinitionArray");

        this.fields = fields;
        length = fields.length;
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        return propertyNames.contains(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            ret_val = length;
        }

        return ret_val;
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param index The index into the array
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(index >= 0 && index < length)
            ret_val = fields[index];

        return ret_val;
    }
}
