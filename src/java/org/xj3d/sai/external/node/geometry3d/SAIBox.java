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

package org.xj3d.sai.external.node.geometry3d;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.geometry3d.Box;

/** A concrete implementation of the Box node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIBox extends SAINode implements Box {

/** The solid initializeOnly field */
private SFBool solid;

/** The size initializeOnly field */
private SFVec3f size;

/** Constructor
     * @param queue */ 
public SAIBox ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the solid boolean value. 
 * @return The solid boolean value.  */
    @Override
    public boolean getSolid() {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  return( solid.getValue( ) );
}

/** Set the solid field. 
 * @param val The boolean to set.  */
    @Override
    public void setSolid(boolean val) {
  if ( solid == null ) { 
    solid = (SFBool)getField( "solid" ); 
  }
  solid.setValue( val );
}

/** Return the size value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getSize(float[] val) {
  if ( size == null ) { 
    size = (SFVec3f)getField( "size" ); 
  }
  size.getValue( val );
}

/** Set the size field. 
 * @param val The float[] to set.  */
    @Override
    public void setSize(float[] val) {
  if ( size == null ) { 
    size = (SFVec3f)getField( "size" ); 
  }
  size.setValue( val );
}

}
