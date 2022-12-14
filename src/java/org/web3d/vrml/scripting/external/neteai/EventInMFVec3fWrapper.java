/*****************************************************************************
 * Copyright North Dakota State University, 2004
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.vrml.scripting.external.neteai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.web3d.util.ArrayUtils;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.NetworkEventQueue;

import vrml.eai.field.EventInMFVec3f;

/**
 * Client side implementation of EventInMFVec3f field.
 * EventIn objects double as buffers for data inbound to the event system.
 */
public class EventInMFVec3fWrapper extends EventInMFVec3f 
    implements ExternalEvent, EventWrapper {

    /** The stored value iff hasStoredValue */
    float[] storedValue;
    
    /** Does this field have a stored value? */
    boolean hasStoredValue;
    
    /** Flag indicating that this event is a Set1Value or accumulation of
     ** Set1Value calls.  This flag is checked during the processing of
     ** set1Value calls in order to correctly either generate a new event,
     ** or merge with a previous set1Value call.*/
    boolean isSet1Value;

    /** The queue for managing events */
    NetworkEventQueue eventQueue;

    /** The network ID of this field */
    int fieldID;
    
    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     */
    public EventInMFVec3fWrapper(
        int fieldID, 
        FieldAndNodeRequestProcessor requestProcessor,
        NetworkEventQueue eventQueue
    ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
        this.eventQueue=eventQueue;
    }

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param eventQueue The queue to send events to
     * @param newValue The value to initialize the field with
     */
    public EventInMFVec3fWrapper(
            int fieldID,  
            FieldAndNodeRequestProcessor requestProcessor,
            NetworkEventQueue eventQueue,
            float newValue[][]) {
        this(fieldID,requestProcessor,eventQueue);
        storeValue(newValue);
    }
    
    /** The EventIn*Wrapper classes implement doEvent by posting their
      * stored values to the underlying implementation.
      * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent
     **/
    @Override
    public void doEvent() {
        try {
            try {
                requestProcessor.setFieldValue(fieldID,this);
            } finally {
                hasStoredValue=false;
                isSet1Value=false;
            }
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException(
                "InvalidFieldException setting EventIn value.",ife
            );
        } catch (org.web3d.vrml.lang.InvalidFieldValueException ifve) {
            throw new RuntimeException(
                "InvalidFieldValueException setting EventIn value.",ifve
            );
        }
    }

    /** Two eventIn's are equal if they point to the same actual node and 
      * field
      * @param other The object to compare against
      */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldID()==fieldID && 
                otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** The underlying field ID 
      * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#getFieldID
      */
    @Override
    public int getFieldID() {
        return fieldID;
    }
  
    /** In order to make the event queueing system easier, and since
      * an equals method is required by the specification, compute the hashcode
      * based on the field number and underlying node hashcode.
      * @see java.lang.Object#hashCode
     **/
    @Override
    public int hashCode() {
        return fieldID;
    }

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
    @Override
	public boolean isConglomerating() {
		return isSet1Value;
	}
    
    /** * @see vrml.eai.field.EventInMFVec3f#setValue(float[][])  */
    @Override
    public void setValue(float[][] value) {
        EventInMFVec3fWrapper queuedElement;
        if (!hasStoredValue) {
            queuedElement=this;
            storeValue(value);
        } else
            queuedElement=new EventInMFVec3fWrapper(
                fieldID,requestProcessor,eventQueue,value
            );
        eventQueue.processEvent(queuedElement);

    	throw new RuntimeException("Not yet implemented.");
    }

    /** * @see vrml.eai.field.EventInMFVec3f#set1Value(int, float[])  */
    @Override
    public void set1Value(int index, float[] value) {
        synchronized(eventQueue.eventLock) {
            EventInMFVec3fWrapper queuedElement=(EventInMFVec3fWrapper)
                eventQueue.getLast(this);
            if (queuedElement==null || !queuedElement.isSet1Value) {
                if (!hasStoredValue) {
                    queuedElement=this;
                    requestProcessor.getFieldValue(fieldID,queuedElement);
                    store1Value(index,value);
                } else {
                    queuedElement=new EventInMFVec3fWrapper(
                        fieldID,requestProcessor,eventQueue);
                	queuedElement.store1Value(index,value);
                } eventQueue.processEvent(queuedElement);
            } else
                queuedElement.store1Value(index,value);
        }
    }

    /** * @see vrml.eai.field.EventIn#setUserData(java.lang.Object)  */
    @Override
    public void setUserData(Object data) {
        requestProcessor.setUserData(fieldID,data);
    }

    /** Store a value in this EventIn as a place holder for the buffering
    system.  */
    private void store1Value(int index, float[] newValue) {
        isSet1Value=true;
        System.arraycopy(newValue,0,storedValue,index*3,3);
    }

    /** Store a value in this EventIn as a place holder for the buffering
     * system.  */
    private void storeValue(float[][] newValue) {
        if (newValue!=null) {
            if (storedValue==null || storedValue.length!=(newValue.length*3))
                storedValue=new float[newValue.length*3];
            ArrayUtils.flatten3(newValue,newValue.length,storedValue);
        } else
            storedValue=null;
        hasStoredValue=true;
    }

    /**
     * @return  * @see vrml.eai.field.EventIn#getUserData()  */
    @Override
    public Object getUserData() {
        return requestProcessor.getUserData(fieldID);
    }

    /**
     * @throws java.io.IOException * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#loadFieldValue(java.io.DataInputStream)  */
    @Override
    public void loadFieldValue(DataInputStream input) throws IOException {
        int numFields=input.readInt();
        storedValue=new float[numFields*3];
        int innerCounter=0;
        for (int counter=0; counter<numFields; counter++) {
            storedValue[innerCounter++]=input.readFloat();
            storedValue[innerCounter++]=input.readFloat();
            storedValue[innerCounter++]=input.readFloat();
        }
    }

    /**
     * @throws java.io.IOException * @see org.web3d.vrml.scripting.external.neteai.EventWrapper#writeFieldValue(java.io.DataOutputStream)  */
    @Override
    public void writeFieldValue(DataOutputStream output) throws IOException {
        output.writeInt(storedValue.length/3);
        for (int counter=0; counter<storedValue.length; counter++)
            output.writeFloat(storedValue[counter]);
    }

}
