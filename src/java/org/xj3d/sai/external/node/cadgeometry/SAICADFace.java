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

package org.xj3d.sai.external.node.cadgeometry;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.cadgeometry.CADFace;

/** A concrete implementation of the CADFace node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAICADFace extends SAINode implements CADFace {

/** The name inputOutput field */
private SFString name;

/** The shape inputOutput field */
private SFNode shape;

/** The bboxCenter initializeOnly field */
private SFVec3f bboxCenter;

/** The bboxSize initializeOnly field */
private SFVec3f bboxSize;

/** Constructor
     * @param queue */ 
public SAICADFace ( 
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

/** Return the shape X3DNode value. 
 * @return The shape X3DNode value.  */
    @Override
    public X3DNode getShape() {
  if ( shape == null ) { 
    shape = (SFNode)getField( "shape" ); 
  }
  return( shape.getValue( ) );
}

/** Set the shape field. 
 * @param val The X3DNode to set.  */
    @Override
    public void setShape(X3DNode val) {
  if ( shape == null ) { 
    shape = (SFNode)getField( "shape" ); 
  }
  shape.setValue( val );
}

/** Return the bboxCenter value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.getValue( val );
}

/** Set the bboxCenter field. 
 * @param val The float[] to set.  */
    @Override
    public void setBboxCenter(float[] val) {
  if ( bboxCenter == null ) { 
    bboxCenter = (SFVec3f)getField( "bboxCenter" ); 
  }
  bboxCenter.setValue( val );
}

/** Return the bboxSize value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.getValue( val );
}

/** Set the bboxSize field. 
 * @param val The float[] to set.  */
    @Override
    public void setBboxSize(float[] val) {
  if ( bboxSize == null ) { 
    bboxSize = (SFVec3f)getField( "bboxSize" ); 
  }
  bboxSize.setValue( val );
}

}
