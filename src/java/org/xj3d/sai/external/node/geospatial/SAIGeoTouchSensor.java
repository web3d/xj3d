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

package org.xj3d.sai.external.node.geospatial;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.geospatial.GeoTouchSensor;

/** A concrete implementation of the GeoTouchSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoTouchSensor extends SAINode implements GeoTouchSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The hitNormal_changed outputOnly field */
private SFVec3f hitNormal_changed;

/** The hitPoint_changed outputOnly field */
private SFVec3f hitPoint_changed;

/** The hitTexCoord_changed outputOnly field */
private SFVec2f hitTexCoord_changed;

/** The hitGeoCoord_changed outputOnly field */
private SFVec3d hitGeoCoord_changed;

/** The isOver outputOnly field */
private SFBool isOver;

/** The touchTime outputOnly field */
private SFTime touchTime;

/** The description inputOutput field */
private SFString description;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The geoSystem initializeOnly field */
private MFString geoSystem;

/** Constructor
     * @param queue */ 
public SAIGeoTouchSensor ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
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

/** Return the isActive boolean value. 
 * @return The isActive boolean value.  */
    @Override
    public boolean getIsActive() {
  if ( isActive == null ) { 
    isActive = (SFBool)getField( "isActive" ); 
  }
  return( isActive.getValue( ) );
}

/** Return the hitNormal_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getHitNormal(float[] val) {
  if ( hitNormal_changed == null ) { 
    hitNormal_changed = (SFVec3f)getField( "hitNormal_changed" ); 
  }
  hitNormal_changed.getValue( val );
}

/** Return the hitPoint_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getHitPoint(float[] val) {
  if ( hitPoint_changed == null ) { 
    hitPoint_changed = (SFVec3f)getField( "hitPoint_changed" ); 
  }
  hitPoint_changed.getValue( val );
}

/** Return the hitTexCoord_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getHitTexCoord(float[] val) {
  if ( hitTexCoord_changed == null ) { 
    hitTexCoord_changed = (SFVec2f)getField( "hitTexCoord_changed" ); 
  }
  hitTexCoord_changed.getValue( val );
}

/** Return the hitGeoCoord_changed value in the argument double[]
 * @param val The double[] to initialize.  */
    @Override
    public void getHitGeoCoord(double[] val) {
  if ( hitGeoCoord_changed == null ) { 
    hitGeoCoord_changed = (SFVec3d)getField( "hitGeoCoord_changed" ); 
  }
  hitGeoCoord_changed.getValue( val );
}

/** Return the isOver boolean value. 
 * @return The isOver boolean value.  */
    @Override
    public boolean getIsOver() {
  if ( isOver == null ) { 
    isOver = (SFBool)getField( "isOver" ); 
  }
  return( isOver.getValue( ) );
}

/** Return the touchTime double value. 
 * @return The touchTime double value.  */
    @Override
    public double getTouchTime() {
  if ( touchTime == null ) { 
    touchTime = (SFTime)getField( "touchTime" ); 
  }
  return( touchTime.getValue( ) );
}

/** Return the description String value. 
 * @return The description String value.  */
    @Override
    public String getDescription() {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  return( description.getValue( ) );
}

/** Set the description field. 
 * @param val The String to set.  */
    @Override
    public void setDescription(String val) {
  if ( description == null ) { 
    description = (SFString)getField( "description" ); 
  }
  description.setValue( val );
}

/** Return the geoOrigin X3DNode value. 
 * @return The geoOrigin X3DNode value.  */
    @Override
    public X3DNode getGeoOrigin() {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  return( geoOrigin.getValue( ) );
}

/** Set the geoOrigin field. 
 * @param val The X3DNode to set.  */
    @Override
    public void setGeoOrigin(X3DNode val) {
  if ( geoOrigin == null ) { 
    geoOrigin = (SFNode)getField( "geoOrigin" ); 
  }
  geoOrigin.setValue( val );
}

/** Return the number of MFString items in the geoSystem field. 
 * @return the number of MFString items in the geoSystem field.  */
    @Override
    public int getNumGeoSystem() {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  return( geoSystem.getSize( ) );
}

/** Return the geoSystem value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.getValue( val );
}

/** Set the geoSystem field. 
 * @param val The String[] to set.  */
    @Override
    public void setGeoSystem(String[] val) {
  if ( geoSystem == null ) { 
    geoSystem = (MFString)getField( "geoSystem" ); 
  }
  geoSystem.setValue( val.length, val );
}

}
