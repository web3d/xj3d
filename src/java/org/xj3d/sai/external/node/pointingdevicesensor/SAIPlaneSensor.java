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

package org.xj3d.sai.external.node.pointingdevicesensor;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec2f;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.pointingdevicesensor.PlaneSensor;

/** A concrete implementation of the PlaneSensor node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIPlaneSensor extends SAINode implements PlaneSensor {

/** The enabled inputOutput field */
private SFBool enabled;

/** The isActive outputOnly field */
private SFBool isActive;

/** The autoOffset inputOutput field */
private SFBool autoOffset;

/** The trackPoint_changed outputOnly field */
private SFVec3f trackPoint_changed;

/** The description inputOutput field */
private SFString description;

/** The isOver outputOnly field */
private SFBool isOver;

/** The maxPosition inputOutput field */
private SFVec2f maxPosition;

/** The minPosition inputOutput field */
private SFVec2f minPosition;

/** The translation_changed outputOnly field */
private SFVec3f translation_changed;

/** The offset inputOutput field */
private SFVec3f offset;

/** Constructor
     * @param queue */ 
public SAIPlaneSensor ( 
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

/** Return the autoOffset boolean value. 
 * @return The autoOffset boolean value.  */
    @Override
    public boolean getAutoOffset() {
  if ( autoOffset == null ) { 
    autoOffset = (SFBool)getField( "autoOffset" ); 
  }
  return( autoOffset.getValue( ) );
}

/** Set the autoOffset field. 
 * @param val The boolean to set.  */
    @Override
    public void setAutoOffset(boolean val) {
  if ( autoOffset == null ) { 
    autoOffset = (SFBool)getField( "autoOffset" ); 
  }
  autoOffset.setValue( val );
}

/** Return the trackPoint_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getTrackPoint(float[] val) {
  if ( trackPoint_changed == null ) { 
    trackPoint_changed = (SFVec3f)getField( "trackPoint_changed" ); 
  }
  trackPoint_changed.getValue( val );
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

/** Return the isOver boolean value. 
 * @return The isOver boolean value.  */
    @Override
    public boolean getIsOver() {
  if ( isOver == null ) { 
    isOver = (SFBool)getField( "isOver" ); 
  }
  return( isOver.getValue( ) );
}

/** Return the maxPosition value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getMaxPosition(float[] val) {
  if ( maxPosition == null ) { 
    maxPosition = (SFVec2f)getField( "maxPosition" ); 
  }
  maxPosition.getValue( val );
}

/** Set the maxPosition field. 
 * @param val The float[] to set.  */
    @Override
    public void setMaxPosition(float[] val) {
  if ( maxPosition == null ) { 
    maxPosition = (SFVec2f)getField( "maxPosition" ); 
  }
  maxPosition.setValue( val );
}

/** Return the minPosition value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getMinPosition(float[] val) {
  if ( minPosition == null ) { 
    minPosition = (SFVec2f)getField( "minPosition" ); 
  }
  minPosition.getValue( val );
}

/** Set the minPosition field. 
 * @param val The float[] to set.  */
    @Override
    public void setMinPosition(float[] val) {
  if ( minPosition == null ) { 
    minPosition = (SFVec2f)getField( "minPosition" ); 
  }
  minPosition.setValue( val );
}

/** Return the translation_changed value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getTranslation(float[] val) {
  if ( translation_changed == null ) { 
    translation_changed = (SFVec3f)getField( "translation_changed" ); 
  }
  translation_changed.getValue( val );
}

/** Return the offset value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getOffset(float[] val) {
  if ( offset == null ) { 
    offset = (SFVec3f)getField( "offset" ); 
  }
  offset.getValue( val );
}

/** Set the offset field. 
 * @param val The float[] to set.  */
    @Override
    public void setOffset(float[] val) {
  if ( offset == null ) { 
    offset = (SFVec3f)getField( "offset" ); 
  }
  offset.setValue( val );
}

}
