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

package org.xj3d.sai.external.node.scripting;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;
import org.web3d.vrml.scripting.external.sai.SAIFieldFactory;
import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.vrml.scripting.external.sai.SAINodeFactory;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.scripting.Script;

/** A concrete implementation of the Script node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIScript extends SAINode implements Script {

/** The url inputOutput field */
private MFString url;

/** The mustEvaluate initializeOnly field */
private SFBool mustEvaluate;

/** The directOutput initializeOnly field */
private SFBool directOutput;

/** Constructor
     * @param queue */ 
public SAIScript ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFString items in the url field. 
 * @return the number of MFString items in the url field.  */
    @Override
    public int getNumUrl() {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  return( url.getSize( ) );
}

/** Return the url value in the argument String[]
 * @param val The String[] to initialize.  */
    @Override
    public void getUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.getValue( val );
}

/** Set the url field. 
 * @param val The String[] to set.  */
    @Override
    public void setUrl(String[] val) {
  if ( url == null ) { 
    url = (MFString)getField( "url" ); 
  }
  url.setValue( val.length, val );
}

/** Return the mustEvaluate boolean value. 
 * @return The mustEvaluate boolean value.  */
    @Override
    public boolean getMustEvaluate() {
  if ( mustEvaluate == null ) { 
    mustEvaluate = (SFBool)getField( "mustEvaluate" ); 
  }
  return( mustEvaluate.getValue( ) );
}

/** Set the mustEvaluate field. 
 * @param val The boolean to set.  */
    @Override
    public void setMustEvaluate(boolean val) {
  if ( mustEvaluate == null ) { 
    mustEvaluate = (SFBool)getField( "mustEvaluate" ); 
  }
  mustEvaluate.setValue( val );
}

/** Return the directOutput boolean value. 
 * @return The directOutput boolean value.  */
    @Override
    public boolean getDirectOutput() {
  if ( directOutput == null ) { 
    directOutput = (SFBool)getField( "directOutput" ); 
  }
  return( directOutput.getValue( ) );
}

/** Set the directOutput field. 
 * @param val The boolean to set.  */
    @Override
    public void setDirectOutput(boolean val) {
  if ( directOutput == null ) { 
    directOutput = (SFBool)getField( "directOutput" ); 
  }
  directOutput.setValue( val );
}

}
