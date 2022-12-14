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

package org.xj3d.sai.external.node.geometry2d;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.geometry2d.TriangleSet2D;

/** A concrete implementation of the TriangleSet2D node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAITriangleSet2D extends SAINode implements TriangleSet2D {

/** The solid initializeOnly field */
private SFBool solid;

/** The vertices inputOutput field */
private MFVec2f vertices;

/** Constructor
     * @param queue 
     * @param fieldFactory */ 
public SAITriangleSet2D ( 
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

/** Return the number of MFVec2f items in the vertices field. 
 * @return the number of MFVec2f items in the vertices field.  */
    @Override
    public int getNumVertices() {
  if ( vertices == null ) { 
    vertices = (MFVec2f)getField( "vertices" ); 
  }
  return( vertices.getSize( ) );
}

/** Return the vertices value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getVertices(float[] val) {
  if ( vertices == null ) { 
    vertices = (MFVec2f)getField( "vertices" ); 
  }
  vertices.getValue( val );
}

/** Set the vertices field. 
 * @param val The float[] to set.  */
    @Override
    public void setVertices(float[] val) {
  if ( vertices == null ) { 
    vertices = (MFVec2f)getField( "vertices" ); 
  }
  vertices.setValue( val.length/2, val );
}

}
