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

// Local imports
// none

/**
 * A stack that has a minimal implementation.
 *  <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 * @see java.util.Stack
 */
public interface SimpleStackInterface {

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */
    int size();

    /**
     * Tests if this stack maps no values.
     *
     * @return  <code>true</code> if this stack has no values
     */
    boolean isEmpty();

    /**
     * Push a new value onto the top of the stack. The value may be any legal
     * reference to an object including null.
     *
     * @param val The new value for the stack.
     */
    void push(Object val);

    /**
     * Peek at the value on the top of the stack without removing it. If the
     * value pushed was null, then null is returned here.
     *
     * @return A reference to the object on the top of the stack
     * @throws EmptyStackException The stack is currently empty
     */
    Object peek() throws EmptyStackException;

    /**
     * Pop the value from the top of the stack. If the last value in the stack
     * was null then this will return null.
     *
     * @return The top object on the stack
     */
    Object pop();

    /**
     * Returns true if this stack contains an instance of the value. The checl
     * looks at both the reference comparison (==) and the equality using
     * <code>.equals()</code>. If the stack is currently empty this will always
     * return false. The search order is from the top of the stack towards the
     * bottom.
     *
     * @param value The value whose presence in this stack is to be tested.
     * @return true if this stack contains the value.
     */
    boolean contains(Object value);

    /**
     * Remove the given object from the stack if it exists. If it is not
     * in the stack, then ignore it quietly.
     *
     * @param obj The object to be removed
     */
    void remove(Object obj);

    /**
     * Clears this stack so that it contains no values.
     */
    void clear();
}
