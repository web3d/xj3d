package org.web3d.vrml.scripting.external.sai;

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

import org.web3d.x3d.sai.*;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

/**
 * Representation of a SFBool field.
 *
 */
class SFBoolWrapper extends BaseFieldWrapper implements SFBool, ExternalEvent, ExternalOutputBuffer {

	/** The value stored in this buffer iff storedInput */
	boolean storedInputValue;

	/** The value stored in this buffer iff storedOutput */
	boolean storedOutputValue;

	/** Basic constructor for wrappers without preloaded values
	 * @param node The underlying Xj3D node
	 * @param field The field on the underlying node
	 * @param aQueue The event queue to send events to
	 * @param factory The adapter factory for registering interest
	 */
    SFBoolWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory
    ) {
        super(node,field,aQueue,factory);
    }

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     * @param isInput if isInput load value into storedInputValue, else load into storedOutputValue
     */
    SFBoolWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
            SAIEventAdapterFactory factory, boolean isInput
    ) {
        this(node,field,aQueue,factory);
        if (isInput)
            loadInputValue();
        else
            loadOutputValue();
    }

    /** Post any queued field values to the target field */
    @Override
    public void doEvent() {
		try {
			theNode.setValue(fieldIndex,storedInputValue);
		} finally {
			storedInput=false;
		}
	}

    /**
     * Get the value in the given eventOut.
     *  <p>
     * @return The boolean value of the eventOut
     */
        @Override
    public boolean getValue() {
		if (storedOutput)
			return storedOutputValue;
		else {
	    	checkReadAccess();
			VRMLFieldData data=theNode.getFieldValue(fieldIndex);
			return data.booleanValue;
		}
    }

	/**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#initialize(org.web3d.vrml.nodes.VRMLNodeType, int)
	 */
        @Override
	public void initialize(VRMLNodeType srcNode, int fieldNumber) {
		theNode=srcNode;
		fieldIndex=fieldNumber;
	}

    /**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
	 */
        @Override
	public boolean isConglomerating() {
		return false;
	}

	/** Load the current field value from the underlying node and store it as the input value.
	 *
	 */
	private void loadInputValue() {
		VRMLFieldData value=theNode.getFieldValue(fieldIndex);
		storedInputValue=value.booleanValue;
		storedInput=true;
	}

	/** Load the current field value from the underlying node and store it as the output value.
	 *
	 */
        @Override
	public void loadOutputValue() {
		VRMLFieldData value=theNode.getFieldValue(fieldIndex);
		storedOutputValue=value.booleanValue;
		storedOutput=true;
	}

	/**
	 * @see org.web3d.vrml.scripting.external.buffer.ExternalOutputBuffer#reset()
	 */
        @Override
	public void reset() {
        theNode=null;
        fieldIndex=-1;
        storedOutput=false;
	}

    /**
     * Set the value in the given eventIn.
     *  <p>
     * @param value The boolean value to set the eventIn to.
     */
        @Override
    public void setValue(boolean value) {
    	checkWriteAccess();
		SFBoolWrapper queuedElement=this;
		// Input and output buffers do not mix
		if (storedInput || storedOutput)
			queuedElement=new SFBoolWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
		queuedElement.storedInput=true;
		queuedElement.storedInputValue=value;
		theEventQueue.processEvent(queuedElement);
    }

}
