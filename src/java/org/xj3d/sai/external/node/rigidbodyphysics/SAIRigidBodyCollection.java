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

package org.xj3d.sai.external.node.rigidbodyphysics;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.rigidbodyphysics.RigidBodyCollection;

/** A concrete implementation of the RigidBodyCollection node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIRigidBodyCollection extends SAINode implements RigidBodyCollection {

/** The set_contacts inputOnly field */
private MFNode set_contacts;

/** The autoDisable inputOutput field */
private SFBool autoDisable;

/** The bodies inputOutput field */
private MFNode bodies;

/** The constantForceMix inputOutput field */
private SFFloat constantForceMix;

/** The contactSurfaceThickness inputOutput field */
private SFFloat contactSurfaceThickness;

/** The disableAngularSpeed inputOutput field */
private SFFloat disableAngularSpeed;

/** The disableLinearSpeed inputOutput field */
private SFFloat disableLinearSpeed;

/** The disableTime inputOutput field */
private SFFloat disableTime;

/** The enabled inputOutput field */
private SFBool enabled;

/** The errorCorrectionFactor inputOutput field */
private SFFloat errorCorrectionFactor;

/** The gravity inputOutput field */
private SFVec3f gravity;

/** The iterations inputOutput field */
private SFInt32 iterations;

/** The joints inputOutput field */
private MFNode joints;

/** The maxCorrectionSpeed inputOutput field */
private SFFloat maxCorrectionSpeed;

/** The preferAccuracy inputOutput field */
private SFBool preferAccuracy;

/** The collider initializeOnly field */
private SFNode collider;

/** Constructor
 * @param node
 * @param queue
 * @param nodeFactory
 * @param fieldFactory
 */
public SAIRigidBodyCollection (
  VRMLNodeType node,
  SAINodeFactory nodeFactory,
  SAIFieldFactory fieldFactory,
  ExternalEventQueue queue ) {
    super( node, nodeFactory, fieldFactory, queue );
}

/** Set the set_contacts field.
 * @param val The X3DNode[] to set.  */
@Override
public void setContacts(X3DNode[] val) {
  if ( set_contacts == null ) {
    set_contacts = (MFNode)getField( "set_contacts" );
  }
  set_contacts.setValue( val.length, val );
}

/** Return the autoDisable boolean value.
 * @return The autoDisable boolean value.  */
@Override
public boolean getAutoDisable() {
  if ( autoDisable == null ) {
    autoDisable = (SFBool)getField( "autoDisable" );
  }
  return( autoDisable.getValue( ) );
}

/** Set the autoDisable field.
 * @param val The boolean to set.  */
@Override
public void setAutoDisable(boolean val) {
  if ( autoDisable == null ) {
    autoDisable = (SFBool)getField( "autoDisable" );
  }
  autoDisable.setValue( val );
}

/** Return the number of MFNode items in the bodies field.
 * @return the number of MFNode items in the bodies field.  */
@Override
public int getNumBodies() {
  if ( bodies == null ) {
    bodies = (MFNode)getField( "bodies" );
  }
  return( bodies.getSize( ) );
}

/** Return the bodies value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
@Override
public void getBodies(X3DNode[] val) {
  if ( bodies == null ) {
    bodies = (MFNode)getField( "bodies" );
  }
  bodies.getValue( val );
}

/** Set the bodies field.
 * @param val The X3DNode[] to set.  */
@Override
public void setBodies(X3DNode[] val) {
  if ( bodies == null ) {
    bodies = (MFNode)getField( "bodies" );
  }
  bodies.setValue( val.length, val );
}

/** Return the constantForceMix float value.
 * @return The constantForceMix float value.  */
@Override
public float getConstantForceMix() {
  if ( constantForceMix == null ) {
    constantForceMix = (SFFloat)getField( "constantForceMix" );
  }
  return( constantForceMix.getValue( ) );
}

/** Set the constantForceMix field.
 * @param val The float to set.  */
@Override
public void setConstantForceMix(float val) {
  if ( constantForceMix == null ) {
    constantForceMix = (SFFloat)getField( "constantForceMix" );
  }
  constantForceMix.setValue( val );
}

/** Return the contactSurfaceThickness float value.
 * @return The contactSurfaceThickness float value.  */
@Override
public float getContactSurfaceThickness() {
  if ( contactSurfaceThickness == null ) {
    contactSurfaceThickness = (SFFloat)getField( "contactSurfaceThickness" );
  }
  return( contactSurfaceThickness.getValue( ) );
}

/** Set the contactSurfaceThickness field.
 * @param val The float to set.  */
@Override
public void setContactSurfaceThickness(float val) {
  if ( contactSurfaceThickness == null ) {
    contactSurfaceThickness = (SFFloat)getField( "contactSurfaceThickness" );
  }
  contactSurfaceThickness.setValue( val );
}

/** Return the disableAngularSpeed float value.
 * @return The disableAngularSpeed float value.  */
@Override
public float getDisableAngularSpeed() {
  if ( disableAngularSpeed == null ) {
    disableAngularSpeed = (SFFloat)getField( "disableAngularSpeed" );
  }
  return( disableAngularSpeed.getValue( ) );
}

/** Set the disableAngularSpeed field.
 * @param val The float to set.  */
@Override
public void setDisableAngularSpeed(float val) {
  if ( disableAngularSpeed == null ) {
    disableAngularSpeed = (SFFloat)getField( "disableAngularSpeed" );
  }
  disableAngularSpeed.setValue( val );
}

/** Return the disableLinearSpeed float value.
 * @return The disableLinearSpeed float value.  */
@Override
public float getDisableLinearSpeed() {
  if ( disableLinearSpeed == null ) {
    disableLinearSpeed = (SFFloat)getField( "disableLinearSpeed" );
  }
  return( disableLinearSpeed.getValue( ) );
}

/** Set the disableLinearSpeed field.
 * @param val The float to set.  */
@Override
public void setDisableLinearSpeed(float val) {
  if ( disableLinearSpeed == null ) {
    disableLinearSpeed = (SFFloat)getField( "disableLinearSpeed" );
  }
  disableLinearSpeed.setValue( val );
}

/** Return the disableTime float value.
 * @return The disableTime float value.  */
@Override
public float getDisableTime() {
  if ( disableTime == null ) {
    disableTime = (SFFloat)getField( "disableTime" );
  }
  return( disableTime.getValue( ) );
}

/** Set the disableTime field.
 * @param val The float to set.  */
@Override
public void setDisableTime(float val) {
  if ( disableTime == null ) {
    disableTime = (SFFloat)getField( "disableTime" );
  }
  disableTime.setValue( val );
}

/** Return the enabled boolean value.
 * @return The enabled boolean value.  */
@Override
public boolean getEnabled() {
  if ( enabled == null ) {
    enabled = (SFBool)getField( "enabled" );
  }
  return( enabled.getValue( ) );
}

/** Set the enabled field.
 * @param val The boolean to set.  */
@Override
public void setEnabled(boolean val) {
  if ( enabled == null ) {
    enabled = (SFBool)getField( "enabled" );
  }
  enabled.setValue( val );
}

/** Return the errorCorrectionFactor float value.
 * @return The errorCorrectionFactor float value.  */
@Override
public float getErrorCorrectionFactor() {
  if ( errorCorrectionFactor == null ) {
    errorCorrectionFactor = (SFFloat)getField( "errorCorrectionFactor" );
  }
  return( errorCorrectionFactor.getValue( ) );
}

/** Set the errorCorrectionFactor field.
 * @param val The float to set.  */
@Override
public void setErrorCorrectionFactor(float val) {
  if ( errorCorrectionFactor == null ) {
    errorCorrectionFactor = (SFFloat)getField( "errorCorrectionFactor" );
  }
  errorCorrectionFactor.setValue( val );
}

/** Return the gravity value in the argument float[]
 * @param val The float[] to initialize.  */
@Override
public void getGravity(float[] val) {
  if ( gravity == null ) {
    gravity = (SFVec3f)getField( "gravity" );
  }
  gravity.getValue( val );
}

/** Set the gravity field.
 * @param val The float[] to set.  */
@Override
public void setGravity(float[] val) {
  if ( gravity == null ) {
    gravity = (SFVec3f)getField( "gravity" );
  }
  gravity.setValue( val );
}

/** Return the iterations int value.
 * @return The iterations int value.  */
@Override
public int getIterations() {
  if ( iterations == null ) {
    iterations = (SFInt32)getField( "iterations" );
  }
  return( iterations.getValue( ) );
}

/** Set the iterations field.
 * @param val The int to set.  */
@Override
public void setIterations(int val) {
  if ( iterations == null ) {
    iterations = (SFInt32)getField( "iterations" );
  }
  iterations.setValue( val );
}

/** Return the number of MFNode items in the joints field.
 * @return the number of MFNode items in the joints field.  */
@Override
public int getNumJoints() {
  if ( joints == null ) {
    joints = (MFNode)getField( "joints" );
  }
  return( joints.getSize( ) );
}

/** Return the joints value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
@Override
public void getJoints(X3DNode[] val) {
  if ( joints == null ) {
    joints = (MFNode)getField( "joints" );
  }
  joints.getValue( val );
}

/** Set the joints field.
 * @param val The X3DNode[] to set.  */
@Override
public void setJoints(X3DNode[] val) {
  if ( joints == null ) {
    joints = (MFNode)getField( "joints" );
  }
  joints.setValue( val.length, val );
}

/** Return the maxCorrectionSpeed float value.
 * @return The maxCorrectionSpeed float value.  */
@Override
public float getMaxCorrectionSpeed() {
  if ( maxCorrectionSpeed == null ) {
    maxCorrectionSpeed = (SFFloat)getField( "maxCorrectionSpeed" );
  }
  return( maxCorrectionSpeed.getValue( ) );
}

/** Set the maxCorrectionSpeed field.
 * @param val The float to set.  */
@Override
public void setMaxCorrectionSpeed(float val) {
  if ( maxCorrectionSpeed == null ) {
    maxCorrectionSpeed = (SFFloat)getField( "maxCorrectionSpeed" );
  }
  maxCorrectionSpeed.setValue( val );
}

/** Return the preferAccuracy boolean value.
 * @return The preferAccuracy boolean value.  */
@Override
public boolean getPreferAccuracy() {
  if ( preferAccuracy == null ) {
    preferAccuracy = (SFBool)getField( "preferAccuracy" );
  }
  return( preferAccuracy.getValue( ) );
}

/** Set the preferAccuracy field.
 * @param val The boolean to set.  */
@Override
public void setPreferAccuracy(boolean val) {
  if ( preferAccuracy == null ) {
    preferAccuracy = (SFBool)getField( "preferAccuracy" );
  }
  preferAccuracy.setValue( val );
}

/** Return the collider X3DNode value.
 * @return The collider X3DNode value.  */
@Override
public X3DNode getCollider() {
  if ( collider == null ) {
    collider = (SFNode)getField( "collider" );
  }
  return( collider.getValue( ) );
}

/** Set the collider field.
 * @param val The X3DNode to set.  */
@Override
public void setCollider(X3DNode val) {
  if ( collider == null ) {
    collider = (SFNode)getField( "collider" );
  }
  collider.setValue( val );
}

}
