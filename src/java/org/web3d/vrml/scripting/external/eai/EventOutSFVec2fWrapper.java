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

import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.*;

/**
 *  EventOutSVec2fWrapper provides the functional implementation of
 *  EventOutSFVec2f for the SimpleBrowser class.  The functionality of this 
 *  class is specified in the EAI 2.0 specification.
 */

class EventOutSFVec2fWrapper extends EventOutSFVec2f 
implements ExternalOutputBuffer, EventWrapper {
	
	/** Default field value for null field values */
	static final float DEFAULT_FIELD_VALUE[]=new float[]{0.0f,0.0f};
	
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
    EventOutSFVec2fWrapper(
        VRMLNodeType aNode, int ID, EAIEventAdapterFactory anAdapterFactory, 
        boolean buffer
    ) {
        fieldType=SFVec2f;
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

    /** @see vrml.eai.field.EventOutSFVec2f#getValue */
        @Override
    public void getValue(float[] newValue) {
        if (isStored)
            System.arraycopy(storedValue,0,newValue,0,2);
        else
            try {
            	VRMLFieldData value=theNode.getFieldValue(theFieldID);
            	if (value.floatArrayValues==null)
            		System.arraycopy(DEFAULT_FIELD_VALUE,0,newValue,0,2);
            	else
            		System.arraycopy(value.floatArrayValues,
							0,newValue,0,2
            		);
            } catch (InvalidFieldException ife) {
                throw new RuntimeException("Error reading field value",ife);
            }
    }

    /** @see vrml.eai.field.EventOutSFVec2f#getValue */
        @Override
    public float[] getValue() {
        float returnValue[]=new float[2];
        getValue(returnValue);
        return returnValue;
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
        if (storedValue==null)
            storedValue=new float[2];
        getValue(storedValue);
        isStored=true;
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
            throw new RuntimeException("Error setting user data",ife);
        }
    }

    /** Re-initialize the buffer so that it can service another node
      * @param aNode The new underlying node
      * @param ID The new field ID
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
