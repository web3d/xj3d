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

import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.EventOutSFBool;

/**
 * Client side implementation of EventOutSFBoolWrapper field.
 * EventOut fields which are used by the vrmlEventChanged
 * broadcast system report only their stored value.
 * EventOut fields produced by Node.getEventOut report only
 * the 'live' value of the field.
 */
public class EventOutSFBoolWrapper extends EventOutSFBool
    implements EventWrapper {

    /** Does this field have a stored value? */
    boolean hasStoredValue;

    /** The stored value iff hasStoredValue */
    boolean storedValue;

    /** The network ID of this field */
    int fieldID;

    /** Handler for field services */
    FieldAndNodeRequestProcessor requestProcessor;

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     */
    public EventOutSFBoolWrapper(
        int fieldID,
        FieldAndNodeRequestProcessor requestProcessor
    ) {
        this.fieldID=fieldID;
        this.requestProcessor=requestProcessor;
    }

    /**
     * @param fieldID The network field ID
     * @param requestProcessor Handler for field services
     * @param source The stream to read field values from
     * @throws IOException
     */
    public EventOutSFBoolWrapper(
        int fieldID,
        FieldAndNodeRequestProcessor requestProcessor,
        DataInputStream source
    ) throws IOException {
        this(fieldID,requestProcessor);
        loadFieldValue(source);
    }

    /** Two fields are equal if they point to the same actual node and
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

    @Override
    public int getFieldID() {
        return fieldID;
    }

    @Override
    public boolean getValue() {
        if (!hasStoredValue)
            requestProcessor.getFieldValue(fieldID,this);
        return storedValue;
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

    /** * @see vrml.eai.field.EventOut#addVrmlEventListener(vrml.eai.event.VrmlEventListener)  */
    @Override
    public void addVrmlEventListener(VrmlEventListener l) {
        requestProcessor.addVrmlEventListener(fieldID,getType(),l);
    }

    /** * @see vrml.eai.field.EventOut#removeVrmlEventListener(vrml.eai.event.VrmlEventListener)  */
    @Override
    public void removeVrmlEventListener(VrmlEventListener l) {
        requestProcessor.removeVrmlEventListener(fieldID,l);
    }

    /** * @see vrml.eai.field.EventOut#setUserData(java.lang.Object)  */
    @Override
    public void setUserData(Object data) {
        requestProcessor.setUserData(fieldID,data);
    }

    @Override
    public Object getUserData() {
        return requestProcessor.getUserData(fieldID);
    }

    @Override
    public void loadFieldValue(DataInputStream input) throws IOException {
        storedValue=input.readBoolean();
    }

    @Override
    public void writeFieldValue(DataOutputStream output) throws IOException {
        throw new RuntimeException("Not supported");
    }

}
