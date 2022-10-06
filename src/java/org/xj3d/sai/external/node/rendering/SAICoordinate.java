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

package org.xj3d.sai.external.node.rendering;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.rendering.Coordinate;

/** A concrete implementation of the Coordinate node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICoordinate extends SAINode implements Coordinate {

/** The point inputOutput field */
private MFVec3f point;

/** Constructor
     * @param queue */ 
public SAICoordinate ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFVec3f items in the point field. 
 * @return the number of MFVec3f items in the point field.  */
    @Override
    public int getNumPoint() {
  if ( point == null ) { 
    point = (MFVec3f)getField( "point" ); 
  }
  return( point.getSize( ) );
}

/** Return the point value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getPoint(float[] val) {
  if ( point == null ) { 
    point = (MFVec3f)getField( "point" ); 
  }
  point.getValue( val );
}

/** Set the point field. 
 * @param val The float[] to set.  */
    @Override
    public void setPoint(float[] val) {
  if ( point == null ) { 
    point = (MFVec3f)getField( "point" ); 
  }
  point.setValue( val.length/3, val );
}

}
