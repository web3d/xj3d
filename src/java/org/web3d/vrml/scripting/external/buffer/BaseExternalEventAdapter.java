/**
 * Created on 2004/04/09
 *
 *
 */
package org.web3d.vrml.scripting.external.buffer;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * BaseExternalEventAdapter
 *
 * Base outgoing event adapter which attempts to abstract
 * the common logic between the SAI and EAI notification system.
 *
 * @author Brad Vender
 *
 */
public abstract class BaseExternalEventAdapter implements ExternalEventAdapter {

    /**
     * The VRMLNodeType from the underlying implementation that we broadcast
     * for.
     */
    protected VRMLNodeType parentNode;

    /**
     * The mapping of fieldID to listener lists
     */
    Vector<Object> listenerArray[];

    /**
     * The queue of eventOut buffers for each field with a listener.
     */
    protected List<Object> buffers[];

    /**
     * The clock for producing consistent timestamps
     */
    protected VRMLClock timeClock;

    /**
     * Construct the basic event adapter.
     *
     * @param aNode The underlying node that we are adapting for.
     * @param clock The clock to use for timestamps.
     */
    public BaseExternalEventAdapter(VRMLNodeType aNode, VRMLClock clock) {
        parentNode = aNode;
        timeClock = clock;
    }

    /**
     * Add a listener for one of the fields of this node.
     * @param who
     */
    @Override
    public void addListener(int fieldID, Object who) {
        Vector<Object> listeners = checkListeners(fieldID);
        listeners.addElement(who);
    }

    /**
     * Lazy initialization of listener array. Listener vectors will be
     * constructed, and the array in which they are stored grown to accommodate,
     * as they are requested. This method performs a similar function for the
     * output buffers.
     */
    @SuppressWarnings("unchecked") // generic array types
    synchronized Vector<Object> checkListeners(int fieldID) {
        if (fieldID < 0) {
            throw new IllegalArgumentException("Invalid field ID");
        }
        if (listenerArray != null) {
            // Both listenerArray and buffers are initialized together.
            if (fieldID >= listenerArray.length) {
                // This should probably be the next power of two for speed
                Vector<Object> newListenerArray[] = new Vector[fieldID + 1];
                List<Object> newBuffers[] = new LinkedList[fieldID + 1];
                System.arraycopy(listenerArray, 0, newListenerArray, 0,
                        listenerArray.length);
                System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
                newListenerArray[fieldID] = new Vector<>();
                newBuffers[fieldID] = new LinkedList<>();
                listenerArray = newListenerArray;
                buffers = newBuffers;
            } else if (listenerArray[fieldID] == null) {
                listenerArray[fieldID] = new Vector<>();
                buffers[fieldID] = new LinkedList<>();
            }
        } else {
            // Both listenerArray and buffers start as null together.
            Vector<Object> newListenerArray[] = new Vector[fieldID + 1];
            List<Object> newBuffers[] = new LinkedList[fieldID + 1];
            newListenerArray[fieldID] = new Vector<>();
            newBuffers[fieldID] = new LinkedList<>();
            listenerArray = newListenerArray;
            buffers = newBuffers;
        }
        return listenerArray[fieldID];
    }

    /**
     * Generate the event notification for a field of one of VRMLNodeType node
     * this object is registered with.
     *
     * @param fieldID The ID of the field which changed.
     */
    public void fieldChanged(int fieldID) {
        generateBroadcast(fieldID, timeClock.getTime());
    }

    /**
     * Broadcast an eventOutChanged event for a given field Modifications to the
     * buffers arrays are synchronized so this needs to be too for safety.
     *
     * @param fieldID The fieldID which changed
     * @param timestamp The timestamp to use
     */
    @Override
    public abstract void generateBroadcast(int fieldID, double timestamp);

    /**
     * Get the listeners for a given fieldID. This doesn't perform
     * initialization, and returns null for elements which don't exist yet.
     *
     * @param fieldID The ID of the field.
     * @return
     */
    protected synchronized Vector<Object> getListeners(int fieldID) {
        if (fieldID < 0) {
            throw new IllegalArgumentException("Invalid field ID");
        }
        if ((listenerArray == null) || (listenerArray.length <= fieldID)) {
            return null;
        } else {
            return listenerArray[fieldID];
        }
    }

    /**
     * Remove a listener for one of the fields of this node. Will also prune the
     * buffer size if all listeners are removed.
     *
     * @param fieldID The ID of the field.
     * @param who The listener to remove.
     */
    @Override
    public void removeListener(int fieldID, Object who) {
        Vector<Object> listeners = getListeners(fieldID);
        if (listeners != null) {
            listeners.removeElement(who);
            // If no one is listening, there's no point in keeping the buffer.
            if (listeners.isEmpty()) // Modifications to the buffers arrays are synchronized
            // so this needs to be too for safety.
            {
                synchronized (this) {
                    buffers[fieldID].clear();
                }
            }
        }
    }
}
