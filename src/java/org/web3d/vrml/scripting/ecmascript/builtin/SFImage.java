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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.j3d.util.HashSet;

/**
 * MFInt field object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class SFImage extends FieldScriptableObject {

    private static final String OBJECT_NOT_Int32_MSG =
        "The object you attempted to assign was not a int instance";

    /** The value of the x property */
    private int x;

    /** The value of the y property */
    private int y;

    /** The value of the comp property */
    private int comp;

    /** The array of pixels */
    private MFInt32 array;

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    /** The Javascript Undefined value */
    private static Object jsUndefined;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("x");
        propertyNames.add("y");
        propertyNames.add("comp");
        propertyNames.add("array");

        functionNames = new HashSet<>();
        functionNames.add("toString");
        functionNames.add("equals");

        jsUndefined = Context.getUndefinedValue();
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public SFImage() {
        super("SFImage");
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node).
     * @param numValid The number of valid values to copy from the array
     */
    public SFImage(int[] vals, int numValid) {
        this(); // invoke default constructor

        switch(vals.length) {
            case 0:
                // do nothing - everything empty;
                array = new MFInt32();
                break;

            case 1:
                x = vals[0];
                array = new MFInt32();
                break;

            case 2:
                x = vals[0];
                y = vals[1];
                array = new MFInt32();
                break;

            case 3:
                x = vals[0];
                y = vals[1];
                comp = vals[2];
                array = new MFInt32();
                break;

            default:
                x = vals[0];
                y = vals[1];
                comp = vals[2];

                // The rest of the array gets shuffled off to MFInt32
                array = new MFInt32(vals, 3, numValid - 3);
                break;
        }
    }

    /**
     * Construct a field based on an array of SFInt32 objects.
     *
     * @param args the objects
     */
    public SFImage(Object[] args) {
        this(); // invoke default constructor

        if(!(args[0] instanceof Number))
            throw new IllegalArgumentException("Non number given for x");

        x = ((Number)args[0]).intValue();
        if(args.length == 1)
            return;

        if(!(args[1] instanceof Number))
            throw new IllegalArgumentException("Non number given for y");

        y = ((Number)args[1]).intValue();
        if(args.length == 2)
            return;

        if(!(args[2] instanceof Number))
            throw new IllegalArgumentException("Non number given for comp");

        comp = ((Number)args[2]).intValue();
        if(args.length == 3)
            return;

        if(!(args[3] instanceof MFInt32))
            throw new IllegalArgumentException("Non MFInt32 given for array");

        array = (MFInt32)args[3];
//        if(args.length == 4)
//            return;
     }

     /**
      * Constructor for a new Rhino object
     * @return 
      */
     public static Scriptable jsConstructor(Context cx, Object[] args,
                                            Function ctorObj,
                                            boolean inNewExpr) {

         SFImage result = new SFImage(args);

         return result;
    }

    /**
     * Check for the indexed property presence.
     * @return 
     */
    @Override
    public boolean has(int index, Scriptable start) {
        return (index >= 0);
    }

    /**
     * Check for the named property presence.
     *
     * @param start
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        boolean ret_val = false;

        if(propertyNames.contains(name))
            ret_val = true;
        else
            ret_val = super.has(name, start);

        return ret_val;
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
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            char ch = name.charAt(0);

            switch(ch) {
                case 'x':
                    ret_val = x;
                    break;

                case 'y':
                    ret_val = y;
                    break;

                case 'c':
                    ret_val = comp;
                    break;

                case 'a':
                    ret_val = array;
                    break;
            }
        } else {
            ret_val = super.get(name, start);

            // it could be that this instance is dynamically created and so
            // the function name is not automatically registex by the
            // runtime. Let's check to see if it is a standard method for
            // this object and then create and return a corresponding Function
            // instance.
            if((ret_val == null) && functionNames.contains(name))
                ret_val = locateFunction(name);
        }

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Sets the named property with a new value. We don't allow the users to
     * dynamically change the length property of this node. That would cause
     * all sorts of problems. Therefore it is read-only as far as this
     * implementation is concerned.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            registerFunction(name, value);
        } else if(propertyNames.contains(name)) {
            char ch = name.charAt(0);
            Number num;

            switch(ch) {
                case 'x':
                    if(!(value instanceof Number))
                        throw new IllegalArgumentException("Non number given for y");

                    x = ((Number)value).intValue();
                    break;

                case 'y':
                    if(!(value instanceof Number))
                        throw new IllegalArgumentException("Non number given for y");

                    y = ((Number)value).intValue();
                    break;

                case 'c':
                    if(!(value instanceof Number))
                        throw new IllegalArgumentException("Non number given for comp");

                    comp = ((Number)value).intValue();
                    break;

                case 'a':
                    if(!(value instanceof MFInt32))
                        throw new IllegalArgumentException("Non MFInt32 given for array");

                    array = (MFInt32)value;
                    break;
            }
        } else {
            throw new IllegalArgumentException("Unknown property");
        }

    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.24
    //

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    /**
     * Comparison of this object to another of the same type. Just calls
     * the standard equals() method of the object.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    public boolean jsFunction_equals(Object val) {
        return equals(val);
    }

    /**
     * Format the internal values of this matrix as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this matrix
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(x);
        buf.append(' ');
        buf.append(y);
        buf.append(' ');
        buf.append(comp);
        buf.append(' ');
        buf.append(array.toString());

        return buf.toString();
    }

    /**
     * Compares two objects for equality base on the components being
     * the same.
     *
     * @param val The value to compare to this object
     * @return true if the components of the object are the same
     */
    @Override
    public boolean equals(Object val) {
        if(!(val instanceof SFImage))
            return false;

        SFImage o = (SFImage)val;

        if(!((o.x == x) && (o.y == y) && (o.comp == comp)))
            return false;

        return array.equals(o.array);
    }

    /**
     * Get the array of underlying int values.
     *
     * @return An array of the values
     */
    public int[] getRawData() {
        int[] pixels = array.getRawData();
        int[] ret_val = new int[pixels.length + 3];

        ret_val[0] = x;
        ret_val[1] = y;
        ret_val[2] = comp;

        System.arraycopy(pixels, 0, ret_val, 3, pixels.length);

        return ret_val;
    }

    /**
     * Alternative form to fetch the raw value by copying it into the provided
     * array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(int[] value) {
        int[] pixels = array.getRawData();

        value[0] = x;
        value[1] = y;
        value[2] = comp;

        System.arraycopy(pixels, 0, value, 3, pixels.length);
    }
}
