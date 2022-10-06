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
import org.web3d.x3d.sai.MFRotation;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.interpolation.OrientationInterpolator;

/** A concrete implementation of the OrientationInterpolator node interface
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class SAIOrientationInterpolator extends SAINode implements OrientationInterpolator {

/** The key inputOutput field */
private MFFloat key;

/** The keyValue inputOutput field */
private MFRotation keyValue;

/** The set_fraction inputOnly field */
private SFFloat set_fraction;

/** The value_changed outputOnly field */
private SFRotation value_changed;

/** Constructor
     * @param node
     * @param nodeFactory
     * @param fieldFactory
     * @param queue */
public SAIOrientationInterpolator (
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

@Override
public void getKey(float[] val) {
  if ( key == null ) {
    key = (MFFloat)getField( "key" );
  }
  key.getValue( val );
}

@Override
public void setKey(float[] val) {
  if ( key == null ) {
    key = (MFFloat)getField( "key" );
  }
  key.setValue( val.length, val );
}

@Override
public int getNumKeyValue() {
  if ( keyValue == null ) {
    keyValue = (MFRotation)getField( "keyValue" );
  }
  return( keyValue.getSize( ) );
}

@Override
public void getKeyValue(float[] val) {
  if ( keyValue == null ) {
    keyValue = (MFRotation)getField( "keyValue" );
  }
  keyValue.getValue( val );
}

@Override
public void setKeyValue(float[] val) {
  if ( keyValue == null ) {
    keyValue = (MFRotation)getField( "keyValue" );
  }
  keyValue.setValue( val.length/4, val );
}

@Override
public void setFraction(float val) {
  if ( set_fraction == null ) {
    set_fraction = (SFFloat)getField( "set_fraction" );
  }
  set_fraction.setValue( val );
}

@Override
public void getValue(float[] val) {
  if ( value_changed == null ) {
    value_changed = (SFRotation)getField( "value_changed" );
  }
  value_changed.getValue( val );
}

}
