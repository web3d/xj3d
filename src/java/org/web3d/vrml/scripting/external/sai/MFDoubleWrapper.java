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
 * Implementation of the MFDouble wrapper.
 *
 */
class MFDoubleWrapper extends BaseFieldWrapper
    implements MFDouble, ExternalEvent, ExternalOutputBuffer {

    /** Is this the result of set1Value calls? */
    private boolean isSetOneValue;

    /** The number of elements used in the input buffer */
    private int storedInputLength;

    /** The value to be sent to the rendering system iff storedInput */
    private double[] storedInputValue;

    /** The value to be sent to the rendering system iff storedOutput */
    private double[] storedOutputValue;

    /** Constructor to use when a value needs to be preloaded
     * @param node The underlying Xj3D node
     * @param field The field on the underlying node
     * @param aQueue The event queue to send events to
     * @param factory The adapter factory for registering interest
     */
    MFDoubleWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
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
    MFDoubleWrapper(VRMLNodeType node, int field, ExternalEventQueue aQueue,
        SAIEventAdapterFactory factory, boolean isInput) {
        this(node,field,aQueue,factory);
        if (isInput)
            loadInputValue();
        else
            loadOutputValue();
    }

    /**
     * @see org.web3d.x3d.sai.MFColorRGBA#append(float[])
     */
    @Override
    public void append(double value) {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFDoubleWrapper queuedElement=
                (MFDoubleWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFDoubleWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+1);
            queuedElement.storedInputValue[queuedElement.storedInputLength++]=value;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.x3d.sai.MFColorRGBA#clear()
     */
    @Override
    public void clear() {
        setValue(0,new double[0]);
    }

    /** Post any queued field values to the target field */
    @Override
    public void doEvent() {
        try {
            theNode.setValue(fieldIndex,storedInputValue,storedInputLength);
        } finally {
            isSetOneValue=false;
            storedInput=false;
        }
    }

    /** Ensures that there is atleast a certain number of elements
     *  in the storedInputValue array.
     * @param newSize The size to ensure.
     */
    protected void ensureArraySize(int newSize) {
        if (storedInputValue!=null && newSize<storedInputValue.length)
            return;
        double newArray[]=new double[newSize];
        if (storedInputValue!=null)
            System.arraycopy(storedInputValue,0,newArray,0,storedInputLength);
        storedInputValue=newArray;
    }

    /**
     * Get the value of an individual item in the eventOut's value.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to be retrieved
     * @return The value to at that position
     *
     * @exception ArrayIndexOutOfBoundsException The index was outside the current data
     *    array bounds.
     */
    @Override
    public double get1Value(int index)
        throws ArrayIndexOutOfBoundsException {
        if (storedOutput)
            return storedOutputValue[index];
        else {
            checkReadAccess();
            VRMLFieldData value=theNode.getFieldValue(fieldIndex);
            // doubleArrayValues may be null if numElements == 0
            if (index<0 || index>=value.numElements)
                throw new ArrayIndexOutOfBoundsException();
            else
                return value.doubleArrayValues[index];
        }
    }

    /**
     * Write the value of the array of the doubles to the given array.
     *
     * @param values The array to be filled in
     * @exception ArrayIndexOutOfBoundsException The provided array was too small
     */
    @Override
    public void getValue(double[] values) {
        if (storedOutput)
            System.arraycopy(storedOutputValue,0,values,0,storedOutputValue.length);
        else {
            checkReadAccess();
            VRMLFieldData data=theNode.getFieldValue(fieldIndex);
            if (data.numElements!=0)
                System.arraycopy(data.doubleArrayValues,0,values,0,data.numElements);
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
     * @see org.web3d.x3d.sai.MFDouble#insertValue(int, double)
     */
    @Override
    public void insertValue(int index, double value) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFDoubleWrapper queuedElement=
                (MFDoubleWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFDoubleWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.ensureArraySize(queuedElement.storedInputLength+1);
            System.arraycopy(queuedElement.storedInputValue,index,queuedElement.storedInputValue,index+1,queuedElement.storedInputLength-index);
            queuedElement.storedInputValue[index]=value;
            queuedElement.storedInputLength++;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    @Override
    public boolean isConglomerating() {
        return isSetOneValue;
    }

    /** Load the current field value from the underlying node and store it as the input value.
     *
     */
    private void loadInputValue() {
        if(!isReadable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedInputValue == null || storedInputValue.length!=value.numElements)
            storedInputValue=new double[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.doubleArrayValues,0,storedInputValue,0,value.numElements);
        storedInput=true;
        storedInputLength=storedInputValue.length;
    }

    /** Load the current field value from the underlying node and store it as the output value.
     *
     */
    @Override
    public void loadOutputValue() {
        if(!isWritable())
            return;
        VRMLFieldData value=theNode.getFieldValue(fieldIndex);
        if (storedOutputValue == null || storedOutputValue.length!=value.numElements)
            storedOutputValue=new double[value.numElements];
        if (value.numElements!=0)
            System.arraycopy(value.doubleArrayValues,0,storedOutputValue,0,value.numElements);
        storedOutput=true;
    }

    /**
     * @see org.web3d.x3d.sai.MFDouble#removeValue(int)
     */
    @Override
    public void removeValue(int index) throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFDoubleWrapper queuedElement=
                (MFDoubleWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFDoubleWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            if (queuedElement.storedInputLength>0) {
                if (index+1<queuedElement.storedInputLength)
                    System.arraycopy(queuedElement.storedInputValue,
                    		index+1,
                    		queuedElement.storedInputValue,
                    		index,
                    		queuedElement.storedInputLength-index-1);
                queuedElement.storedInputLength--;
                if (newEvent)
                    theEventQueue.processEvent(queuedElement);
            } else {
                // Free up the buffer before throwing the exception
                if (newEvent)
                    queuedElement.isSetOneValue=false;
                throw new ArrayIndexOutOfBoundsException();
            }
        }
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
     * Set the value of an individual item in the eventIn's value. This results in
     * a new event being generated that includes all of the array items with the
     * single element set.
     *
     * If the index is out of the bounds of the current array of data values an
     * ArrayIndexOutOfBoundsException will be generated.
     *
     * @param index The position to set the double value
     * @param value The value to be set
     *
     * @exception ArrayIndexOutOfBoundsException A value did not contain at least
     *    three values for the colour component
     */
    @Override
    public void set1Value(int index, double value)
    throws ArrayIndexOutOfBoundsException {
        checkReadAccess();
        checkWriteAccess();
        synchronized(theEventQueue.eventLock) {
            MFDoubleWrapper queuedElement=
                (MFDoubleWrapper) theEventQueue.getLast(this);
            boolean newEvent=false;
            if (queuedElement==null || !queuedElement.isSetOneValue) {
                // Input and output buffers do not mix
                if (!storedInput && !storedOutput) {
                    queuedElement=this;
                    loadInputValue();
                    isSetOneValue=true;
                } else {
                    queuedElement=new MFDoubleWrapper(
                        theNode,fieldIndex,theEventQueue,theEventAdapterFactory,true
                    );
                    queuedElement.isSetOneValue=true;
                }
                newEvent=true;
            }
            queuedElement.storedInputValue[index]=value;
            if (newEvent)
                theEventQueue.processEvent(queuedElement);
        }
    }

    /**
     * Set the value of the eventIn to the new array of double values. This array
     * is copied internally so that the parameter array can be reused without
     * effecting the valid values of the eventIn.
     *
     * @param size The number of items to copy from the array
     * @param value The array of values to be used.
     */
    @Override
    public void setValue(int size, double[] value) {
        checkWriteAccess();
        MFDoubleWrapper queuedElement=this;
        // Input and output buffers do not mix and further don't overwrite
        // set1Value calls
        if (isSetOneValue || storedInput || storedOutput) {
            queuedElement=new MFDoubleWrapper(theNode, fieldIndex, theEventQueue, theEventAdapterFactory);
        }
        queuedElement.storedInput=true;
        if (queuedElement.storedInputValue==null || queuedElement.storedInputValue.length!=size)
            queuedElement.storedInputValue=new double[size];
        System.arraycopy(value,0,queuedElement.storedInputValue,0,size);
        queuedElement.storedInputLength=size;
        theEventQueue.processEvent(queuedElement);
    }

    /**
     * Get the size of the underlying data array. The size is the number of
     * elements for that data type. So for an MFFloat the size would be the
     * number of float values, but for an MFVec3f, it is the number of vectors
     * in the returned array (where a vector is 3 consecutive array indexes in
     * a flat array).
     *
     * @return The number of elements in this field
     */
    @Override
    public int getSize() {
        if (storedOutput) {
            return storedOutputValue.length;
        } else {
            return theNode.getFieldValue(fieldIndex).numElements;
        }
    }
}
