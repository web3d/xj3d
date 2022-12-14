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

package org.xj3d.sai.external.node.dis;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.dis.DISEntityManager;

/** A concrete implementation of the DISEntityManager node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIDISEntityManager extends SAINode implements DISEntityManager {

/** The siteID inputOutput field */
private SFInt32 siteID;

/** The applicationID inputOutput field */
private SFInt32 applicationID;

/** The address inputOutput field */
private SFString address;

/** The port inputOutput field */
private SFInt32 port;

/** The addedEntities outputOnly field */
private MFNode addedEntities;

/** The removedEntities outputOnly field */
private MFNode removedEntities;

/** The mapping inputOutput field */
private MFNode mapping;

/** Constructor
 * @param node
 * @param nodeFactory
 * @param fieldFactory
 * @param queue
 */
public SAIDISEntityManager (
  VRMLNodeType node,
  SAINodeFactory nodeFactory,
  SAIFieldFactory fieldFactory,
  ExternalEventQueue queue ) {
    super( node, nodeFactory, fieldFactory, queue );
}

/** Return the siteID int value.
 * @return The siteID int value.  */
@Override
public int getSiteID() {
  if ( siteID == null ) {
    siteID = (SFInt32)getField( "siteID" );
  }
  return( siteID.getValue( ) );
}

/** Set the siteID field.
 * @param val The int to set.  */
@Override
public void setSiteID(int val) {
  if ( siteID == null ) {
    siteID = (SFInt32)getField( "siteID" );
  }
  siteID.setValue( val );
}

/** Return the applicationID int value.
 * @return The applicationID int value.  */
@Override
public int getApplicationID() {
  if ( applicationID == null ) {
    applicationID = (SFInt32)getField( "applicationID" );
  }
  return( applicationID.getValue( ) );
}

/** Set the applicationID field.
 * @param val The int to set.  */
@Override
public void setApplicationID(int val) {
  if ( applicationID == null ) {
    applicationID = (SFInt32)getField( "applicationID" );
  }
  applicationID.setValue( val );
}

/** Return the address String value.
 * @return The address String value.  */
@Override
public String getAddress() {
  if ( address == null ) {
    address = (SFString)getField( "address" );
  }
  return( address.getValue( ) );
}

/** Set the address field.
 * @param val The String to set.  */
@Override
public void setAddress(String val) {
  if ( address == null ) {
    address = (SFString)getField( "address" );
  }
  address.setValue( val );
}

/** Return the port int value.
 * @return The port int value.  */
@Override
public int getPort() {
  if ( port == null ) {
    port = (SFInt32)getField( "port" );
  }
  return( port.getValue( ) );
}

/** Set the port field.
 * @param val The int to set.  */
@Override
public void setPort(int val) {
  if ( port == null ) {
    port = (SFInt32)getField( "port" );
  }
  port.setValue( val );
}

/** Return the number of MFNode items in the addedEntities field.
 * @return the number of MFNode items in the addedEntities field.  */
@Override
public int getNumAddedEntities() {
  if ( addedEntities == null ) {
    addedEntities = (MFNode)getField( "addedEntities" );
  }
  return( addedEntities.getSize( ) );
}

/** Return the addedEntities value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
@Override
public void getAddedEntities(X3DNode[] val) {
  if ( addedEntities == null ) {
    addedEntities = (MFNode)getField( "addedEntities" );
  }
  addedEntities.getValue( val );
}

/** Return the number of MFNode items in the removedEntities field.
 * @return the number of MFNode items in the removedEntities field.  */
@Override
public int getNumRemovedEntities() {
  if ( removedEntities == null ) {
    removedEntities = (MFNode)getField( "removedEntities" );
  }
  return( removedEntities.getSize( ) );
}

/** Return the removedEntities value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
@Override
public void getRemovedEntities(X3DNode[] val) {
  if ( removedEntities == null ) {
    removedEntities = (MFNode)getField( "removedEntities" );
  }
  removedEntities.getValue( val );
}

/** Return the number of MFNode items in the mapping field.
 * @return the number of MFNode items in the mapping field.  */
@Override
public int getNumMapping() {
  if ( mapping == null ) {
    mapping = (MFNode)getField( "mapping" );
  }
  return( mapping.getSize( ) );
}

/** Return the mapping value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
@Override
public void getMapping(X3DNode[] val) {
  if ( mapping == null ) {
    mapping = (MFNode)getField( "mapping" );
  }
  mapping.getValue( val );
}

/** Set the mapping field.
 * @param val The X3DNode[] to set.  */
@Override
public void setMapping(X3DNode[] val) {
  if ( mapping == null ) {
    mapping = (MFNode)getField( "mapping" );
  }
  mapping.setValue( val.length, val );
}

}
