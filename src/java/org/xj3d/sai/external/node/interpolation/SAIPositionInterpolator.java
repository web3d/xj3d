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

package org.xj3d.sai.external.node.interpolation;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.interpolation.PositionInterpolator;

/** A concrete implementation of the PositionInterpolator node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPositionInterpolator extends SAINode implements PositionInterpolator {

/** The key inputOutput field */
private MFFloat key;

/** The keyValue inputOutput field */
private MFVec3f keyValue;

/** The set_fraction inputOnly field */
private SFFloat set_fraction;

/** The value_changed outputOnly field */
private SFVec3f value_changed;

/** Constructor
     * @param queue */ 
public SAIPositionInterpolator ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFFloat items in the key field. 
 * @return the number of MFFloat items in the key field.  */
    @Override
    public int getNumKey() {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  return( key.getSize( ) );
}

/** Return the key value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getKey(float[] val) {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  key.getValue( val );
}

/** Set the key field. 
 * @param val The float[] to set.  */
    @Override
    public void setKey(float[] val) {
  if ( key == null ) { 
    key = (MFFloat)getField( "key" ); 
  }
  key.setValue( val.length, val );
}

/** Return the number of MFVec3f items in the keyValue field. 
 * @return the number of MFVec3f items in the keyValue field.  */
    @Override
    public int getNumKeyValue() {
  if ( keyValue == null ) { 
    keyValue = (MFVec3f)getField( "keyValue" ); 
  }
  return( keyValue.getSize( ) );
}

/** Return the keyValue value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getKeyValue(float[] val) {
  if ( keyValue == null ) { 
    keyValue = (MFVec3f)getField( "keyValue" ); 
  }
  keyValue.getValue( val );
}

/** Set the keyValue field. 
 * @param val The float[] to set.  */
    @Override
    public void setKeyValue(float[] val) {
  if ( keyValue == null ) { 
    keyValue = (MFVec3f)getField( "keyValue" ); 
  }
  keyValue.setValue( val.length/3, val );
}

/** Set the set_fraction field. 
 * @param val The float to set.  */
    @Override
    public void setFraction(float val) {
  if ( set_fraction == null ) { 
    set_fraction = (SFFloat)getField( "set_fraction" ); 
  }
  set_fraction.setValue( val );
}

/** Return the value_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getValue(float[] val) {
  if ( value_changed == null ) { 
    value_changed = (SFVec3f)getField( "value_changed" ); 
  }
  value_changed.getValue( val );
}

}
