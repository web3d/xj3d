package org.web3d.vrml.scripting.external.eai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import vrml.eai.field.*;
import vrml.eai.event.VrmlEventListener;

/**
 *  EventOutMFFloatWrapper provides the functional implementation of
 *  EventOutMFFloat for the SimpleBrowser class.  The functionality of this 
 *  class is specified in the EAI 2.0 specification.
 */

class EventOutMFFloatWrapper extends EventOutMFFloat 
implements ExternalOutputBuffer, EventWrapper {
    /** Indicates whether to load the value locally or retrieve from node */
    boolean isStored;

    /** The stored value if isStored is true.  Unused otherwise. */
    float[] storedValue;

    /** Used to get the ExternalEventAdapter. */
    EAIEventAdapterFactory theEventAdapterFactory;

    /** The underlying Node implementation uses unique integer field ID's */
    int theFieldID;

    /** Reference to the underlying Node implementation */
    VRMLNodeType theNode;

    /** Construct the EventOut wrapper instance.
      * @param buffer Should the value be loaded immediately.
      * @param aNode  The underlying VRMLNodeType instance.
      * @param ID     The field ID on the underlying node.
      * @param anAdapterFactory The Event Adapter factory.
      */
    EventOutMFFloatWrapper(
        VRMLNodeType aNode, int ID, EAIEventAdapterFactory anAdapterFactory, 
        boolean buffer
    ) {
        fieldType=MFFloat;
        theNode=aNode;
        theFieldID=ID;
        theEventAdapterFactory=anAdapterFactory;
        if (buffer)
            loadOutputValue();
    }

    /** @see vrml.eai.field.EventOut#addVrmlEventListener */
    @Override
    public void addVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).addListener(
            theFieldID,listener
        );
    }

    /** Two eventOut's are equal if they are connected to the same actual 
      * node and field.
      * @param other The object to compare against.
      */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        else if (other instanceof EventWrapper) {
            EventWrapper otherWrapper=(EventWrapper)other;
            return (
                otherWrapper.getFieldNode()==theNode && 
                otherWrapper.getFieldID()==theFieldID && 
                otherWrapper.getType()==getType()
            );
        } else
            return super.equals(other);
    }

    /** The underlying field ID 
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldID
      */
    @Override
    public int getFieldID() {
        return theFieldID;
    }

    /** The underlying implementation node. 
      * @see org.web3d.vrml.scripting.external.eai.EventWrapper#getFieldNode
      */
    @Override
    public VRMLNodeType getFieldNode() {
        return theNode;
    }

    /** @see vrml.eai.field.EventOut#getUserData */
    @Override
    public Object getUserData() {
        try {
            return theNode.getUserData(theFieldID);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error getting user data",ife);
        }
    }

    /** @see vrml.eai.field.EventOutMFFloat#get1Value */
    @Override
    public float get1Value(int index) {
        if (isStored)
            return storedValue[index];
        else
            try {
                VRMLFieldData fieldValue=theNode.getFieldValue(theFieldID);
                if (index<0 || index>=fieldValue.numElements)
                    throw new ArrayIndexOutOfBoundsException();
                return fieldValue.floatArrayValues[index];
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field value");
            }
    }

    /** @see vrml.eai.field.EventOutMFFloat#getValue */
    @Override
    public void getValue(float[] dest) {
        if (isStored)
            System.arraycopy(storedValue,0,dest,0,storedValue.length);
        else
            try {
                VRMLFieldData fieldValue=theNode.getFieldValue(theFieldID);
                if (fieldValue.numElements!=0)
                	System.arraycopy(fieldValue.floatArrayValues,0,dest,0,fieldValue.numElements
                	);
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting value.");
            }
    }

    /** @see vrml.eai.field.EventOutMFFloat#getValue */
    @Override
    public float[] getValue() {
        float result[]=new float[size()];
        getValue(result);
        return result;
    }

	/** Since the equals implementation is given by the spec,
	 *  it is implied that hashCode is defined so that equal
	 *  items have equal hash codes.
	  * @see java.lang.Object#hashCode
	 **/
    @Override
	public int hashCode() {
		return theNode.hashCode()+theFieldID;
	}

    /** @see ExternalOutputBuffer#loadOutputValue */
    @Override
    public void loadOutputValue() {
        isStored=false;
        // Set isStored false to read live value.
        // Do this because we re-use instances.
        int size=size();
        if (storedValue==null || storedValue.length!=size)
            storedValue=new float[size];
         getValue(storedValue);
        isStored=true;
    }

    /** @see vrml.eai.field.EventOutMField#size */ 
    @Override
    public int size() {
        if (isStored)
            return storedValue.length;
        else
            try {
                return theNode.getFieldValue(theFieldID).numElements;
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error getting field size");
            }
    }

    /** @see vrml.eai.field.EventOut#removeVrmlEventListener */
    @Override
    public void removeVrmlEventListener(VrmlEventListener listener) {
        theEventAdapterFactory.getAdapter(theNode).removeListener(
            theFieldID,listener
        );
    }

    /** @see vrml.eai.field.EventOut#setUserData */
    @Override
    public void setUserData(Object data) {
        try {
            theNode.setUserData(theFieldID,data);
        } catch (org.web3d.vrml.lang.InvalidFieldException ife) {
            throw new RuntimeException("Error setting user data");
        }
    }

    /** Re-initialize the buffer so that it can service another node
     *  @param aNode The new underlying node
     *  @param ID The new field ID 
     */
    @Override
    public void initialize(VRMLNodeType aNode, int ID) {
        theNode=aNode;
        theFieldID=ID;
    }

    /** Clear out any stored VRMLNodeType references */
    @Override
    public void reset() {
        theNode=null;
        theFieldID=-1;
        isStored=false;
    }

}
