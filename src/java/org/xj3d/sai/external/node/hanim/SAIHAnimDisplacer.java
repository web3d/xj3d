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

package org.xj3d.sai.external.node.hanim;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFFloat;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.SFString;
import org.web3d.x3d.sai.hanim.HAnimDisplacer;

/** A concrete implementation of the HAnimDisplacer node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIHAnimDisplacer extends SAINode implements HAnimDisplacer {

/** The coordIndex inputOutput field */
private MFInt32 coordIndex;

/** The displacements inputOutput field */
private MFFloat displacements;

/** The name inputOutput field */
private SFString name;

/** Constructor
     * @param queue */ 
public SAIHAnimDisplacer ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFInt32 items in the coordIndex field. 
 * @return the number of MFInt32 items in the coordIndex field.  */
    @Override
    public int getNumCoordIndex() {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  return( coordIndex.getSize( ) );
}

/** Return the coordIndex value in the argument int[]
 * @param val The int[] to initialize.  */
    @Override
    public void getCoordIndex(int[] val) {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  coordIndex.getValue( val );
}

/** Set the coordIndex field. 
 * @param val The int[] to set.  */
    @Override
    public void setCoordIndex(int[] val) {
  if ( coordIndex == null ) { 
    coordIndex = (MFInt32)getField( "coordIndex" ); 
  }
  coordIndex.setValue( val.length, val );
}

/** Return the number of MFFloat items in the displacements field. 
 * @return the number of MFFloat items in the displacements field.  */
    @Override
    public int getNumDisplacements() {
  if ( displacements == null ) { 
    displacements = (MFFloat)getField( "displacements" ); 
  }
  return( displacements.getSize( ) );
}

/** Return the displacements value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getDisplacements(float[] val) {
  if ( displacements == null ) { 
    displacements = (MFFloat)getField( "displacements" ); 
  }
  displacements.getValue( val );
}

/** Set the displacements field. 
 * @param val The float[] to set.  */
    @Override
    public void setDisplacements(float[] val) {
  if ( displacements == null ) { 
    displacements = (MFFloat)getField( "displacements" ); 
  }
  displacements.setValue( val.length, val );
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

}
