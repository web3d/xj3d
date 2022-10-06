/*****************************************************************************
 * Copyright North Dakota State University, 2003
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.util;

// External imports
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

// Local imports
// None

/**
 * An extension of HashMap which stores soft references to the values
 * in the hash map.
 * <p>
 * Like it says in the documentation for WeakHashMap, this class
 * breaks several immutability assumptions that are assumed for
 * HashMap, since values will be disappearing at random.
 *
 * @author Brad Vender
 * @version $Revision: 1.2 $
 */
public class SoftValueHashMap implements Map<Object, KeyedSoftReference> {

    /**
     * Used for clearing out the weak references
     */
    ReferenceQueue<KeyedSoftReference> theReferenceQueue;

    /**
     * The internal implementation
     */
    Map<Object, KeyedSoftReference> internalHashMap;

    public SoftValueHashMap() {
        internalHashMap = new HashMap<>();
        theReferenceQueue = new ReferenceQueue<>();
    }

    /**
     * @see Map#clear
     */
    @Override
    public void clear() {
        // Throw away the old reference queue for simplicity.
        theReferenceQueue = new ReferenceQueue<>();
        internalHashMap.clear();
    }

    /**
     * @see Map#containsKey
     */
    @Override
    public boolean containsKey(Object key) {
        return internalHashMap.containsKey(key);
    }

    /**
     * @see Map#containsValue
     */
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<Object, KeyedSoftReference>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Check for and remove references from the map which have become cleared.
     *
     */
    protected void flushEmptyReferences() {
        KeyedSoftReference r;
        for (r = (KeyedSoftReference) (theReferenceQueue.poll());
                r != null;
                r = (KeyedSoftReference) (theReferenceQueue.poll())) {
            internalHashMap.remove(r.key());
        }
    }

    @Override
    public KeyedSoftReference get(Object key) {
        flushEmptyReferences();
        KeyedSoftReference ref = internalHashMap.get(key);
        if (ref != null) {
            KeyedSoftReference value = ref.get();
            if (value == null) {
                internalHashMap.remove(key);
            }
            return value;
        } else {
            return null;
        }
    }

    /**
     * @see Object#hashCode
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see Map#isEmpty
     */
    @Override
    public boolean isEmpty() {
        return internalHashMap.isEmpty();
    }

    /**
     * @see Map#keySet
     */
    @Override
    public Set<Object> keySet() {
        return internalHashMap.keySet();
    }

    @Override
    public KeyedSoftReference put(Object key, KeyedSoftReference value) {
        if (value == null || key == null) {
            throw new IllegalArgumentException();
        }
        flushEmptyReferences();
        KeyedSoftReference ref = new KeyedSoftReference(key, value, theReferenceQueue);
        KeyedSoftReference oldRef = internalHashMap.put(key, ref);
        if (oldRef != null) {
            return oldRef.get();
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeyedSoftReference remove(Object key) {
        KeyedSoftReference ref = internalHashMap.remove(key);
        // Its already removed, so no point in doing it again.
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    /**
     * @see Map#size
     */
    @Override
    public int size() {
        return internalHashMap.size();
    }

    @Override
    public Collection<KeyedSoftReference> values() {
        flushEmptyReferences();
        Vector<KeyedSoftReference> result = new Vector<>();
        Iterator<KeyedSoftReference> i = internalHashMap.values().iterator();
        while (i.hasNext()) {
            KeyedSoftReference ref = i.next();
//            Object val = ref.get();
//            if (val != null) {
//                result.add(val);
//            }
            if (ref != null) {
                result.add(ref);
            }
        }
        return result;
    }
}

/**
 * An extension of SoftReference which is needed to make SoftValueHashMap usable
 * without insanity. The purpose of the key field is to allow a map
 * implementation to find out what key should now be removed, and this should
 * work for more than just bijections as long as multiple KeyedSoftReference
 * instances are used per value.
 */
class KeyedSoftReference extends SoftReference<KeyedSoftReference> {

    /**
     * The key in the containing Map which refers to this reference.
     */
    Object theKey;

    /**
     * Construct a weak reference with a key value. The reference is registered
     * with ReferenceQueue aQueue and contains value aValue.
     */
    KeyedSoftReference(Object key, KeyedSoftReference aValue, ReferenceQueue<KeyedSoftReference> aQueue) {
        super(aValue, aQueue);
        theKey = key;
    }

    /**
     * Return the key that this reference contains.
     */
    Object key() {
        return theKey;
    }
}
