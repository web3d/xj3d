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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.j3d.util.DefaultErrorReporter;

// Rhino
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Application specific imports
// none

/**
 * Base representation of all field objects in ecmascript scripting.
 *  <p>
 *
 * All fields must extend this class. All implementations should make sure
 * to set the dataChanged field whenever their internal data state has been
 * modified.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class FieldScriptableObject implements Scriptable {

    /**
     * Error message when given a string that you parse for a number and it
     * is not a valid format. The message assumes that you will tack on the
     * passed in string as part of the message.
     */
    protected static final String BAD_FORMAT_MSG =
        "Attempting to set a property that we have determined is a number " +
        "but you passed to us as a String. The string you passed us was: ";

    /**
     * Error message when a property is being set and it is not a compatible
     * javascript type with what is required.
     */
    protected static final String INVALID_TYPE_MSG =
        "The type passed to the underlying object is invalid ";

    /** Error message for when the field is marked readOnly */
    protected static final String READONLY_MSG =
        "You are not allowed to directly set this field because the script " +
        "is marked as read only (directOutput is set to FALSE)";

    /** The prefix of the name for any function call we dynamically look up */
    private static final String JS_FUNCTION_PREFIX = "jsFunction_";

    private static final Object[] EMPTY_IDS = new Object[0];

    /** The parent scope of this object */
    private Scriptable parentScope;

    /** The prototype definition of this node */
    private Scriptable prototype;

    /** The name of this field type */
    private final String className;

    /** Flag to be set if the data has changed */
    protected boolean dataChanged;

    /** Flag to say this field is read only (directOutput == true) */
    protected boolean readOnly;

    /** The function objects to maintain */
    private Map<String, Object> functionObjects;

    /**
     * Flag to say this is a script variable and that the readOnly rules
     * are modified somewhat to deal with it. It allows direct writing to
     * this field, but does not allow writing to any children field.
     */
    protected boolean scriptField;

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     * @param name the name of this Scriptable object
     */
    protected FieldScriptableObject(String name) {
        className = name;
        functionObjects = new HashMap<>();
        readOnly = false;
    }

    //----------------------------------------------------------
    // Methods required by the Scriptable interface.
    //----------------------------------------------------------

    /**
     * The class name as defined to the Javascript function.
     *
     * @return the name of this class
     */
    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Object getDefaultValue(Class hint) {
        return null;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return false;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return functionObjects.containsKey(name);
    }

    @Override
    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
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
        return functionObjects.get(name);
    }

    /**
     * Sets a property based on the index. Since we don't support indexes,
     * this is ignored.
     *
     * @param index The index of the property to set
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(int index, Scriptable start, Object value) {
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFVec3f(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        // ignore anything else
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param index The index of the property to delete
     */
    @Override
    public void delete(int index) {
        // Do nothing
    }

    /**
     * Delete a property. There are no dynamic fields, so this does nothing.
     *
     * @param name The name of the property to delete
     */
    @Override
    public void delete(String name) {
        // Do nothing
    }

    // These are the standard methods below here. No need to override them.

    @Override
    public Object[] getIds() {
        return EMPTY_IDS;
    }

    @Override
    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this))
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }

    @Override
    public Scriptable getParentScope() {
        return parentScope;
    }

    @Override
    public void setParentScope(Scriptable parent) {
        parentScope = parent;
    }

    @Override
    public Scriptable getPrototype() {
        return prototype;
    }

    /**
     * Ignored. Set the prototype for this context. The context does not have
     * a prototype that we want the user to play with.
     *
     * @param proto The prototype definition to use
     */
    @Override
    public void setPrototype(Scriptable proto) {
        prototype = proto;
    }

    //----------------------------------------------------------
    // Public local methods
    //----------------------------------------------------------

    /**
     * Set this field to be a script field. Once set, cannot be turned off.
     */
    public void setScriptField() {
        scriptField = true;
    }

    /**
     * Set this field to be read only. Once set, cannot be turned off.
     */
    public void setReadOnly() {
        readOnly = true;
    }

    /**
     * Register a function object with this field type
     *
     * @param name The name to associate it with
     * @param value The object to keep this as
     */
    protected void registerFunction(String name, Object value) {
        functionObjects.put(name, value);
    }

    /**
     * Convenience method to locate a function name for this object and
     * create an appropriate Function instance to represent it. It assumes that
     * the name you give it is the normal name and will add a "jsFunction_"
     * prefix to locate that from the method details. There is also the
     * implicit assumption that you have made a check for this name being a
     * valid function for this object before you call this method. If a
     * function object is found for this method, it will automatically be
     * registered and you can also have a copy of it returned to use.
     *
     * @param methodName The real method name to look for
     * @return The function object corresponding to the munged method name
     */
    protected FunctionObject locateFunction(String methodName) {
        String real_name = JS_FUNCTION_PREFIX + methodName;

        Method[] methods = getClass().getMethods();

        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(real_name)) {
                method = m;
                break;
            }
        }

        FunctionObject function;
        try {
          function = new FunctionObject(methodName, method, this);
        } catch (Exception ex) {
            DefaultErrorReporter.getDefaultReporter().errorReport("Unknown function: " + real_name +
                                        " on: " + getClass(), (Throwable)ex);
            return null;
        }

        registerFunction(methodName, function);

        return function;
    }

    /**
     * Query this field object to see if it has changed since the last time
     * this method was called. In a single-threaded environment, calling this
     * method twice should return true and then false (assuming that data had
     * changed since the previous calls).
     *
     * @return true if the data has changed.
     */
    public boolean hasChanged() {
        boolean ret_val = dataChanged;
        dataChanged = false;

        return ret_val;
    }
}
