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

package org.xj3d.sai.external.node.texturing;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFNode;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFColor;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.texturing.MultiTexture;

/** A concrete implementation of the MultiTexture node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIMultiTexture extends SAINode implements MultiTexture {

/** The mode inputOutput field */
private MFString mode;

/** The texture inputOutput field */
private MFNode texture;

/** The color inputOutput field */
private SFColor color;

/** The alpha inputOutput field */
private SFFloat alpha;

/** The function inputOutput field */
private MFString function;

/** The source inputOutput field */
private MFString source;

/** Constructor
     * @param queue 
     * @param nodeFactory 
     * @param fieldFactory */ 
public SAIMultiTexture ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFString items in the mode field. 
 * @return the number of MFString items in the mode field.  */
    @Override
    public int getNumMode() {
  if ( mode == null ) { 
    mode = (MFString)getField( "mode" ); 
  }
  return( mode.getSize( ) );
}

/** Return the mode value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getMode(String[] val) {
  if ( mode == null ) { 
    mode = (MFString)getField( "mode" ); 
  }
  mode.getValue( val );
}

/** Set the mode field. 
 * @param val The String[] to set.  */
    @Override
    public void setMode(String[] val) {
  if ( mode == null ) { 
    mode = (MFString)getField( "mode" ); 
  }
  mode.setValue( val.length, val );
}

/** Return the number of MFNode items in the texture field. 
 * @return the number of MFNode items in the texture field.  */
    @Override
    public int getNumTexture() {
  if ( texture == null ) { 
    texture = (MFNode)getField( "texture" ); 
  }
  return( texture.getSize( ) );
}

/** Return the texture value in the argument X3DNode[]
 * @param val The X3DNode[] to initialize.  */
    @Override
    public void getTexture(X3DNode[] val) {
  if ( texture == null ) { 
    texture = (MFNode)getField( "texture" ); 
  }
  texture.getValue( val );
}

/** Set the texture field. 
 * @param val The X3DNode[] to set.  */
    @Override
    public void setTexture(X3DNode[] val) {
  if ( texture == null ) { 
    texture = (MFNode)getField( "texture" ); 
  }
  texture.setValue( val.length, val );
}

/** Return the color value in the argument float[]
 * @param val The float[] to initialize.  */
    @Override
    public void getColor(float[] val) {
  if ( color == null ) { 
    color = (SFColor)getField( "color" ); 
  }
  color.getValue( val );
}

/** Set the color field. 
 * @param val The float[] to set.  */
    @Override
    public void setColor(float[] val) {
  if ( color == null ) { 
    color = (SFColor)getField( "color" ); 
  }
  color.setValue( val );
}

/** Return the alpha float value. 
 * @return The alpha float value.  */
    @Override
    public float getAlpha() {
  if ( alpha == null ) { 
    alpha = (SFFloat)getField( "alpha" ); 
  }
  return( alpha.getValue( ) );
}

/** Set the alpha field. 
 * @param val The float to set.  */
    @Override
    public void setAlpha(float val) {
  if ( alpha == null ) { 
    alpha = (SFFloat)getField( "alpha" ); 
  }
  alpha.setValue( val );
}

/** Return the number of MFString items in the function field. 
 * @return the number of MFString items in the function field.  */
    @Override
    public int getNumFunction() {
  if ( function == null ) { 
    function = (MFString)getField( "function" ); 
  }
  return( function.getSize( ) );
}

/** Return the function value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getFunction(String[] val) {
  if ( function == null ) { 
    function = (MFString)getField( "function" ); 
  }
  function.getValue( val );
}

/** Set the function field. 
 * @param val The String[] to set.  */
    @Override
    public void setFunction(String[] val) {
  if ( function == null ) { 
    function = (MFString)getField( "function" ); 
  }
  function.setValue( val.length, val );
}

/** Return the number of MFString items in the source field. 
 * @return the number of MFString items in the source field.  */
    @Override
    public int getNumSource() {
  if ( source == null ) { 
    source = (MFString)getField( "source" ); 
  }
  return( source.getSize( ) );
}

/** Return the source value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getSource(String[] val) {
  if ( source == null ) { 
    source = (MFString)getField( "source" ); 
  }
  source.getValue( val );
}

/** Set the source field. 
 * @param val The String[] to set.  */
    @Override
    public void setSource(String[] val) {
  if ( source == null ) { 
    source = (MFString)getField( "source" ); 
  }
  source.setValue( val.length, val );
}

}
