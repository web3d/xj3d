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

package org.xj3d.sai.external.node.core;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.core.MetadataSet;

/** A concrete implementation of the MetadataSet node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMetadataSet extends SAINode implements MetadataSet {

/** The name inputOutput field */
private SFString name;

/** The reference inputOutput field */
private SFString reference;

/** The value inputOutput field */
private MFNode value;

/** Constructor
     * @param queue */ 
public SAIMetadataSet ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the name String value. 
 * @return The name String value.  */
    @Override
    public String getName() {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  return( name.getValue( ) );
}

/** Set the name field. 
 * @param val The String to set.  */
    @Override
    public void setName(String val) {
  if ( name == null ) { 
    name = (SFString)getField( "name" ); 
  }
  name.setValue( val );
}

/** Return the reference String value. 
 * @return The reference String value.  */
    @Override
    public String getReference() {
  if ( reference == null ) { 
    reference = (SFString)getField( "reference" ); 
  }
  return( reference.getValue( ) );
}

/** Set the reference field. 
 * @param val The String to set.  */
    @Override
    public void setReference(String val) {
  if ( reference == null ) { 
    reference = (SFString)getField( "reference" ); 
  }
  reference.setValue( val );
}

/** Return the number of MFNode items in the value field. 
 * @return the number of MFNode items in the value field.  */
    @Override
    public int getNumValue() {
  if ( value == null ) { 
    value = (MFNode)getField( "value" ); 
  }
  return( value.getSize( ) );
}

/** Return the value value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
    @Override
    public void getValue(X3DNode[] val) {
  if ( value == null ) { 
    value = (MFNode)getField( "value" ); 
  }
  value.getValue( val );
}

/** Set the value field. 
 * @param val The X3DNode[] to set.  */
    @Override
    public void setValue(X3DNode[] val) {
  if ( value == null ) { 
    value = (MFNode)getField( "value" ); 
  }
  value.setValue( val.length, val );
}

}
