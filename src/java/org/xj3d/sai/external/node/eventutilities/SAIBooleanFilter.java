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
import org.web3d.x3d.sai.eventutilities.BooleanFilter;

/** A concrete implementation of the BooleanFilter node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIBooleanFilter extends SAINode implements BooleanFilter {

/** The set_boolean inputOnly field */
private SFBool set_boolean;

/** The inputFalse outputOnly field */
private SFBool inputFalse;

/** The inputTrue outputOnly field */
private SFBool inputTrue;

/** The inputNegate outputOnly field */
private SFBool inputNegate;

/** Constructor
     * @param node
     * @param queue 
     * @param nodeFactory 
     * @param fieldFactory */ 
public SAIBooleanFilter ( 
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

/** Return the inputFalse boolean value. 
 * @return The inputFalse boolean value.  */
    @Override
    public boolean getInputFalse() {
  if ( inputFalse == null ) { 
    inputFalse = (SFBool)getField( "inputFalse" ); 
  }
  return( inputFalse.getValue( ) );
}

/** Return the inputTrue boolean value. 
 * @return The inputTrue boolean value.  */
    @Override
    public boolean getInputTrue() {
  if ( inputTrue == null ) { 
    inputTrue = (SFBool)getField( "inputTrue" ); 
  }
  return( inputTrue.getValue( ) );
}

/** Return the inputNegate boolean value. 
 * @return The inputNegate boolean value.  */
    @Override
    public boolean getInputNegate() {
  if ( inputNegate == null ) { 
    inputNegate = (SFBool)getField( "inputNegate" ); 
  }
  return( inputNegate.getValue( ) );
}

}
