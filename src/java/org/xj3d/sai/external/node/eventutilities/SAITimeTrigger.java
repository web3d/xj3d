/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.xj3d.sai.external.node.eventutilities;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.eventutilities.TimeTrigger;

/** A concrete implementation of the TimeTrigger node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITimeTrigger extends SAINode implements TimeTrigger {

/** The set_boolean inputOnly field */
private SFBool set_boolean;

/** The triggerTime outputOnly field */
private SFTime triggerTime;

/** Constructor
     * @param node
     * @param queue 
     * @param nodeFactory 
     * @param fieldFactory */ 
public SAITimeTrigger ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Set the set_boolean field. 
 * @param val The boolean to set.  */
    @Override
    public void setBoolean(boolean val) {
  if ( set_boolean == null ) { 
    set_boolean = (SFBool)getField( "set_boolean" ); 
  }
  set_boolean.setValue( val );
}

/** Return the triggerTime double value. 
 * @return The triggerTime double value.  */
    @Override
    public double getTriggerTime() {
  if ( triggerTime == null ) { 
    triggerTime = (SFTime)getField( "triggerTime" ); 
  }
  return( triggerTime.getValue( ) );
}

}
