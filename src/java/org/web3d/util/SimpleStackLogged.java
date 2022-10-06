/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.util;

// External imports
import java.util.EmptyStackException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Local imports
// none

/**
 * A stack that has a minimal implementation and no sychronisation.
 * Looks all activity to help in debugging.
 *  <p>
 *
 * This stack is designed to be used in a high-speed, single threaded
 * environment. It is directly backed by an array for fast access.
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 * @see java.util.Stack
 */
public class SimpleStackLogged extends SimpleStack  {

    /** History of operations */
    private List<String> history;

    /** Name to use for debug logs */
    private String name;

    /** Should we debug calls */
    private boolean debugCalls;

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <tt>20</tt> respectively.
     */
    public SimpleStackLogged() {
        this(STACK_START_SIZE, "Unnamed");
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is <tt>20</tt> respectively.
     * @param name
     */
    public SimpleStackLogged(String name) {
        this(STACK_START_SIZE, name);
    }

    /**
     * Constructs a new, empty hashtable with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param  initialCapacity the initial capacity of the hashtable.
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public SimpleStackLogged(int initialCapacity) {
        this(initialCapacity, "Unnamed");

        history = new ArrayList<>();

    }

    /**
     * Constructs a new, empty hashtable with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param  initialCapacity the initial capacity of the hashtable.
     * @param name
     * @throws IllegalArgumentException if the initial capacity is less
     *   than zero.
     */
    public SimpleStackLogged(int initialCapacity, String name) {
        super(initialCapacity);

        history = new ArrayList<>();
        this.name = name;
        debugCalls = false;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Should we display all calls to this stack.
     *
     * @param debug Debug calls
     */
    public void setDebugCalls(boolean debug) {
        debugCalls = debug;
    }


    /**
     * Push a new value onto the top of the stack. The value may be any legal
     * reference to an object including null.
     *
     * @param val The new value for the stack.
     */
    @Override
    public void push(Object val) {
        super.push(val);

        String st = getIndent(size()-1) + "PUSH: " + val;
        history.add(st);

        if(debugCalls) {
            System.out.println(st);
        }
    }

    /**
     * Pop the value from the top of the stack. If the last value in the stack
     * was null then this will return null.
     *
     * @return The top object on the stack
     */
    @Override
    public Object pop() {
        Object ret_val = null;

        try {
            ret_val = super.pop();
        } catch(EmptyStackException ese) {
            System.out.println("History: ");
            System.out.println(toStringHistory());
            throw ese;
        }

        String st = getIndent(size()) + "POP: " + ret_val;
        history.add(st);

        if(debugCalls) {
            System.out.println(st);
        }

        return ret_val;
    }

    /**
     * Peek at the value on the top of the stack without removing it. If the
     * value pushed was null, then null is returned here.
     *
     * @return A reference to the object on the top of the stack
     * @throws EmptyStackException The stack is currently empty
     */
    @Override
    public Object peek() throws EmptyStackException {
        try {
            return super.peek();
        } catch(EmptyStackException ese) {
            System.out.println("History: ");
            System.out.println(toStringHistory());
            throw ese;
        }
    }

    /**
     * Remove the given object from the stack if it exists. If it is not
     * in the stack, then ignore it quietly.
     *
     * @param obj The object to be removed
     */
    @Override
    public void remove(Object obj) {
        super.remove(obj);


        String st = getIndent(size()-1) + "REMOVE: " + obj;
        history.add(st);

        if(debugCalls) {
            System.out.println(st);
        }

    }

    /**
     * Clears this stack so that it contains no values.
     */
    @Override
    public void clear() {
        super.clear();

        String st = getIndent(size()-1) + "CLEAR";
        history.add(st);

        if(debugCalls) {
            System.out.println(st);
        }
    }

    /**
     * Get a string representation of the history.
     *
     * @return The history
     */
    public String toStringHistory() {
        StringBuilder bldr = new StringBuilder();

        bldr.append("Name: ").append(name).append(" HC: ").append(this.hashCode()).append("\n");
        Iterator<String> itr = history.iterator();

        while(itr.hasNext()) {
            String st = itr.next();
            bldr.append(st);
            bldr.append("\n");
        }

        return bldr.toString();
    }

    /**
     * Get an indent string based on the current size.
     *
     * @param The string
     */
    private String getIndent(int len) {
        String st = "   ";
        StringBuilder bldr = new StringBuilder();

        for(int i=0; i < len; i++) {
            bldr.append(st);
        }

        return bldr.toString();
    }
}
