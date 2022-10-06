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
import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

// Local imports
// None

/**
 * An extension of HashMap which stored weak references to the values
 * in the hash map.
 * <p>
 *
 * Like it says in the documentation for WeakHashMap, this class
 * breaks several immutability assumptions that are assumed for
 * HashMap, since values will be disappearing at random.
 *
 * @author Brad Vender
 * @version $Revision: 1.2 $
 */
public class WeakValueHashMap implements Map<Object, KeyedWeakReference> {

    /** Used for clearing out the weak references */
    private ReferenceQueue<KeyedWeakReference> theReferenceQueue;

    /** The internal implementation */
    private Map<Object, KeyedWeakReference> internalHashMap;

    /** Construct a new map of default size */
    public WeakValueHashMap() {
        internalHashMap=new HashMap<>();
        theReferenceQueue=new ReferenceQueue<>();
    }

    /** Construct a new map with specified starting size
     * @param size
     */
    public WeakValueHashMap(int size) {
        internalHashMap=new HashMap<>(size);
        theReferenceQueue=new ReferenceQueue<>();
    }

    /** @see Map#clear */
    @Override
    public void clear() {
        // Throw away the old reference queue for simplicity.
        theReferenceQueue=new ReferenceQueue<>();
        internalHashMap.clear();
    }

    /** @see Map#containsKey */
    @Override
    public boolean containsKey(Object key) {
        return internalHashMap.containsKey(key);
    }

    /** @see Map#containsValue */
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Set<Map.Entry<Object, KeyedWeakReference>> entrySet() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /** Check for and remove references from the map which have
     *  become cleared.
     **/
    protected void flushEmptyReferences() {
        KeyedWeakReference r;
        for (r=(KeyedWeakReference)(theReferenceQueue.poll());
                r!=null;
             r=(KeyedWeakReference)(theReferenceQueue.poll()) ) {
            internalHashMap.remove(r.key());
        }
    }

    @Override
    public KeyedWeakReference get(Object key) {
        flushEmptyReferences();
        KeyedWeakReference ref=internalHashMap.get(key);
        if (ref!=null) {
            KeyedWeakReference value=ref.get();
            if (value==null)
                internalHashMap.remove(key);
            return value;
        } else
            return null;
    }

    /** @see Object#hashCode */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** @see Map#isEmpty */
    @Override
    public boolean isEmpty() {
        return internalHashMap.isEmpty();
    }

    /** @see Map#keySet */
    @Override
    public Set<Object> keySet() {
        return internalHashMap.keySet();
    }

    @Override
    public KeyedWeakReference put(Object key, KeyedWeakReference value) {
        if (value==null || key==null)
            throw new IllegalArgumentException();
        flushEmptyReferences();
        KeyedWeakReference ref=new KeyedWeakReference(key,value,theReferenceQueue);
        KeyedWeakReference oldRef=internalHashMap.put(key,ref);
        if (oldRef!=null)
            return oldRef.get();
        else
            return null;
    }

    @Override
    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    /** @see Map#remove */
    @Override
    public KeyedWeakReference remove(Object key) {
        KeyedWeakReference ref=internalHashMap.remove(key);
        // It's already removed, so no point in doing it again.
        if (ref!=null)
            return ref.get();
        else
            return null;
    }

    /** @see Map#size */
    @Override
    public int size() {
        return internalHashMap.size();
    }

    @Override
    public Collection<KeyedWeakReference> values() {
        flushEmptyReferences();
        Vector<KeyedWeakReference> result=new Vector<>();
        Iterator<KeyedWeakReference> i=internalHashMap.values().iterator();
        while (i.hasNext()) {
            KeyedWeakReference ref=i.next();
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

/** Supplemental class for weak value hash map.  Links the value's weak reference
 *  to its key in the map so that the key can be removed when the value is released.
 */
class KeyedWeakReference extends WeakReference<KeyedWeakReference> {

    /** The key in the containing Map which refers to this reference. */
    Object theKey;

    /** Construct a weak reference with a key value.
      * The reference is registered with ReferenceQueue aQueue and
      * contains value aValue. */
    KeyedWeakReference(Object key, KeyedWeakReference aValue, ReferenceQueue<KeyedWeakReference> aQueue) {
        super(aValue,aQueue);
        theKey=key;
    }

    /** Return the key that this reference contains. */
    Object key() { return theKey; }

}
