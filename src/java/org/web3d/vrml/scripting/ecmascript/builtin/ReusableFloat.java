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
// none

// Application specific imports
// none

/**
/**
 * An floating point extension of the {@link java.lang.Number}
 * abstract base class that allows the user to reset the internal value.
 * <p>
 *
 * A typical issue with the Javascript runtime is that everywhere it uses
 * object values rather than primitives. With a lot of method calling, that
 * leads to a large amount of garbage being generated. As the runtime only
 * really looks for Number instances rather than specific values, whenever
 * we can, we use this class. That allows us to re use the instances rather
 * than creating them and then throwing them away.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ReusableFloat extends Number {

    /** Storage of the number */
    private double value;

    /**
     * Create a new value based on the given value
     *
     * @param val The first value to use
     */
    public ReusableFloat(double val) {
        value = val;
    }

    //----------------------------------------------------------
    // Methods required by the Number base class
    //----------------------------------------------------------

    /**
     * Get the current value as a double.
     *
     * @return The value as a double
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Get the current value as a float.
     *
     * @return The value as a float
     */
    @Override
    public float floatValue() {
        return (float)value;
    }

    /**
     * Get the current value as an integer.
     *
     * @return The value as a int
     */
    @Override
    public int intValue() {
        return (int)value;
    }

    /**
     * Get the current value as a long.
     *
     * @return The value as a long
     */
    @Override
    public long longValue() {
        return (long)value;
    }

    /**
     * Convert to String.
     * @return 
     */
    @Override
    public String toString() {
        return Double.toString(value);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the class to represent the new value.
     *
     * @param val The new value to use
     */
    public void setValue(double val) {
        value = val;
    }
}
