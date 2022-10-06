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
 * SFVec3f field object.
 *  <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class SFVec3f extends FieldScriptableObject {

    /** The properties of this class */
    private double x;
    private double y;
    private double z;

    /** Set of the valid property names for this object */
    private static final HashSet<String> propertyNames;

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    static {
        propertyNames = new HashSet<>();
        propertyNames.add("x");
        propertyNames.add("y");
        propertyNames.add("z");

        functionNames = new HashSet<>();
        functionNames.add("add");
        functionNames.add("cross");
        functionNames.add("divide");
        functionNames.add("dot");
        functionNames.add("length");
        functionNames.add("multiply");
        functionNames.add("negate");
        functionNames.add("normalize");
        functionNames.add("subtract");
        functionNames.add("toString");
        functionNames.add("equals");
    }


    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public SFVec3f() {
        super("SFVec3f");
    }

    /**
     * Construct a field based on the given array of data (sourced from a
     * node).
     * @param vec the given array of data
     */
    public SFVec3f(float[] vec) {
        this(); // invoke default constructor

        if(vec == null || vec.length == 0)
            return;

        x = vec[0];
        y = vec[1];
        z = vec[2];
    }

    /**
     * Alternate constructor that takes a double array rather than a float
     * array. Used primarily by the VrmlMatrix code.
     * @param vec the given array of data
     */
    public SFVec3f(double[] vec) {
        this(); // invoke default constructor

        if(vec == null)
            return;

        x = vec[0];
        y = vec[1];
        z = vec[2];
    }

    /**
     * Constructor for all three values that rhino calls.
     *
     * @param x The X component of the vector
     * @param y The Y component of the vector
     * @param z The Z component of the vector
     */
    public void jsConstructor(double x, double y, double z) {
        this.x = Double.isNaN(x) ? 0 : x;
        this.y = Double.isNaN(y) ? 0 : y;
        this.z = Double.isNaN(z) ? 0 : z;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return (index >= 0 && index < 2);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        boolean ret_val;

        if(propertyNames.contains(name))
            ret_val = true;
        else
            ret_val = super.has(name, start);

        return ret_val;
    }

    @Override
    public Object get(int index, Scriptable start) {
        Object ret_val = NOT_FOUND;

        switch(index) {
            case 0:
                ret_val = x;
                break;

            case 1:
                ret_val = y;
                break;

            case 2:
                ret_val = z;
                break;
        }

        return ret_val;
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = null;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);
            double dbl_val = 0;

            switch(prop) {
                case 'x':
                    dbl_val = x;
                    break;

                case 'y':
                    dbl_val = y;
                    break;

                case 'z':
                    dbl_val = z;
                    break;
            }

            ret_val = dbl_val;
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
     * Sets a property based on the index.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(int index, Scriptable start, Object value) {

        Number num = (Number)value;

        switch(index) {
            case 0:
                x = num.doubleValue();
                break;

            case 1:
                y = num.doubleValue();
                break;

            case 2:
                z = num.doubleValue();
                break;
        }

        dataChanged = true;
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if(propertyNames.contains(name)) {
            if(readOnly && !scriptField) {
                Context.reportError(READONLY_MSG);
                return;
            }

            char prop = name.charAt(0);
            double dbl_val;

            if(value instanceof Number) {
                Number num = (Number)value;
                dbl_val = num.doubleValue();
            } else if(value instanceof String) {
                try {
                    dbl_val = Double.parseDouble((String)value);
                } catch(NumberFormatException nfe) {
                    Context.reportRuntimeError(BAD_FORMAT_MSG + value);
                    return;
                }
            } else {
                Context.reportRuntimeError(INVALID_TYPE_MSG);
                return;
            }

            switch(prop) {
                case 'x':
                    x = dbl_val;
                    break;

                case 'y':
                    y = dbl_val;
                    break;

                case 'z':
                    z = dbl_val;
                    break;
            }

            dataChanged = true;
       } else if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.12
    //

    /**
     * Add this vector to the given vector and return the value as a new
     * vector.
     *
     * @param newVector The vector to add to this vector
     * @return The addition of the two objects
     */
    public SFVec3f jsFunction_add(Scriptable newVector) {
        if (!(newVector instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }

        SFVec3f vec = (SFVec3f) newVector;
        SFVec3f ret_val = new SFVec3f();

        ret_val.x = x + vec.x;
        ret_val.y = y + vec.y;
        ret_val.z = z + vec.z;

        return ret_val;
    }

    /**
     * Calculate the cross product of this vector and the given vector
     *
     * @param newVector The vector to do the cross product with
     * @return The product
     */
    public SFVec3f jsFunction_cross(Scriptable newVector) {
        if (!(newVector instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }

        SFVec3f vec = (SFVec3f) newVector;
        SFVec3f ret_val = new SFVec3f();

        ret_val.x = y * vec.z - z * vec.y;
        ret_val.y = z * vec.x - x * vec.z;
        ret_val.z = x * vec.y - y * vec.x;

        return ret_val;
    }

    /**
     * Divide this vector by the given scalar value and return the result in
     * a new vector
     *
     * @param n The scalar to divide the objects by
     * @return The addition of the two objects
     */
    public SFVec3f jsFunction_divide(double n) {
        SFVec3f ret_val = new SFVec3f();
        ret_val.x = x / n;
        ret_val.y = y / n;
        ret_val.z = z / n;

        return ret_val;
    }

    /**
     * Calculate the dot product of this vector and the passed vector.
     *
     * @param newVector The vector to make the calculation with
     * @return The value of the dot product
     */
    public double jsFunction_dot(Scriptable newVector) {
        if (!(newVector instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }

        SFVec3f vec = (SFVec3f) newVector;

        return x * vec.x + y * vec.y + z * vec.z;
    }

    /**
     * Calculate the length of the vector.
     *
     * @return The length of the vector
     */
    public double jsFunction_length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Multiply this vector by the scalar amount and return the result in a
     * new vector.
     *
     * @param n The scalar to divide the objects by
     * @return The addition of the two objects
     */
    public SFVec3f jsFunction_multiply(double n) {
        SFVec3f ret_val = new SFVec3f();
        ret_val.x = x * n;
        ret_val.y = y * n;
        ret_val.z = z * n;

        return ret_val;
    }

    /**
     * Generate a vector with the negated version of each of the components.
     *
     * @return A negated version of this vector
     */
    public SFVec3f jsFunction_negate() {
        SFVec3f ret_val = new SFVec3f();
        ret_val.x = -x;
        ret_val.y = -y;
        ret_val.z = -z;

        return ret_val;
    }

    /**
     * Generate the normalised version of this vector and return it.
     *
     * @return The normalised form of this vector
     */
    public SFVec3f jsFunction_normalize() {
        SFVec3f ret_val = new SFVec3f();

        double length = jsFunction_length();

        ret_val.x = x / length;
        ret_val.y = y / length;
        ret_val.z = z / length;

        return ret_val;
    }

    /**
     * Subtract the given vector from this vector and return the results in
     * a new object.
     *
     * @param newVector The vector to subtract from this one
     * @return The difference between the two
     */
    public SFVec3f jsFunction_subtract(Scriptable newVector) {
        if (!(newVector instanceof SFVec3f)) {
            Context.reportRuntimeError(INVALID_TYPE_MSG);
        }

        SFVec3f vec = (SFVec3f) newVector;

        SFVec3f ret_val = new SFVec3f();
        ret_val.x = x - vec.x;
        ret_val.y = y - vec.y;
        ret_val.z = z - vec.z;

        return ret_val;
    }

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

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Format the internal values of this field as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this field
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(x);
        buf.append(' ');
        buf.append(y);
        buf.append(' ');
        buf.append(z);

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
        if(!(val instanceof SFVec3f))
            return false;

        SFVec3f o = (SFVec3f)val;

        return (o.x == x) && (o.y == y) && (o.z == z);
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array as float values.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(float[] value) {
        value[0] = (float)x;
        value[1] = (float)y;
        value[2] = (float)z;
    }

    /**
     * Fetch the raw data held by this instance and copy it into the
     * provided array.
     *
     * @param value The array to copy the data into
     */
    public void getRawData(double[] value) {
        value[0] = x;
        value[1] = y;
        value[2] = z;
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(float[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param values The new values to use
     */
    public void setRawData(double[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
    }

    /**
     * Convenience method to reset the values used by this instance. Typically
     * used by the VrmlMatrix class, but others may use this too. It does not
     * set the data changed flag when called.
     *
     * @param x The new x value to use
     * @param y The new y value to use
     * @param z The new z value to use
     */
    public void setRawData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
