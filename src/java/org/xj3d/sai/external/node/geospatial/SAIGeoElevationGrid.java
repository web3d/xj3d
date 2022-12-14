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
import org.web3d.x3d.sai.MFDouble;
import org.web3d.x3d.sai.MFString;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFDouble;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.SFVec3d;
import org.web3d.x3d.sai.X3DColorNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DNormalNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DTextureCoordinateNode;
import org.web3d.x3d.sai.geospatial.GeoElevationGrid;

/** A concrete implementation of the GeoElevationGrid node interface
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public class SAIGeoElevationGrid extends SAINode implements GeoElevationGrid {

/** The set_height inputOnly field */
private MFDouble set_height;

/** The color inputOutput field */
private SFNode color;

/** The normal inputOutput field */
private SFNode normal;

/** The texCoord inputOutput field */
private SFNode texCoord;

/** The ccw initializeOnly field */
private SFBool ccw;

/** The colorPerVertex initializeOnly field */
private SFBool colorPerVertex;

/** The creaseAngle initializeOnly field */
private SFDouble creaseAngle;

/** The geoGridOrigin initializeOnly field */
private SFVec3d geoGridOrigin;

/** The geoOrigin initializeOnly field */
private SFNode geoOrigin;

/** The geoSystem initializeOnly field */
private MFString geoSystem;

/** The height initializeOnly field */
private MFDouble height;

/** The normalPerVertex initializeOnly field */
private SFBool normalPerVertex;

/** The solid initializeOnly field */
private SFBool solid;

/** The xDimension initializeOnly field */
private SFInt32 xDimension;

/** The xSpacing initializeOnly field */
private SFDouble xSpacing;

/** The zDimension initializeOnly field */
private SFInt32 zDimension;

/** The zSpacing initializeOnly field */
private SFDouble zSpacing;

/** The yScale inputOutput field */
private SFFloat yScale;

/** Constructor
     * @param queue */ 
public SAIGeoElevationGrid ( 
  VRMLNodeType node, 
  SAINodeFactory nodeFactory, 
  SAIFieldFactory fieldFactory, 
  ExternalEventQueue queue ) { 
    super( node, nodeFactory, fieldFactory, queue ); 
}

/** Return the number of MFDouble items in the height field. 
 * @return the number of MFDouble items in the height field.  */
    @Override
    public int getNumHeight() {
  if ( height == null ) { 
    height = (MFDouble)getField( "height" ); 
  }
  return( height.getSize( ) );
}

/** Return the height value in the argument double[]
 * @param val The double[] to initialize.  */
    @Override
    public void getHeight(double[] val) {
  if ( height == null ) { 
    height = (MFDouble)getField( "height" ); 
  }
  height.getValue( val );
}

/** Set the height field. 
 * @param val The double[] to set.  */
    @Override
    public void setHeight(double[] val) {
  if ( !isRealized( ) ) { 
    if ( height == null ) { 
      height = (MFDouble)getField( "height" ); 
    } 
    height.setValue( val.length, val ); 
  } else { 
    if ( set_height == null ) { 
      set_height = (MFDouble)getField( "set_height" ); 
    } 
    set_height.setValue( val.length, val ); 
  } 
}

/** Return the color X3DNode value. 
 * @return The color X3DNode value.  */
    @Override
    public X3DNode getColor() {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  return( color.getValue( ) );
}

/** Set the color field. 
 * @param val The X3DColorNode to set.  */
    @Override
    public void setColor(X3DColorNode val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Set the color field. 
 * @param val The X3DProtoInstance to set.  */
    @Override
    public void setColor(X3DProtoInstance val) {
  if ( color == null ) { 
    color = (SFNode)getField( "color" ); 
  }
  color.setValue( val );
}

/** Return the normal X3DNode value. 
 * @return The normal X3DNode value.  */
    @Override
    public X3DNode getNormal() {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  return( normal.getValue( ) );
}

/** Set the normal field. 
 * @param val The X3DNormalNode to set.  */
    @Override
    public void setNormal(X3DNormalNode val) {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  normal.setValue( val );
}

/** Set the normal field. 
 * @param val The X3DProtoInstance to set.  */
    @Override
    public void setNormal(X3DProtoInstance val) {
  if ( normal == null ) { 
    normal = (SFNode)getField( "normal" ); 
  }
  normal.setValue( val );
}

/** Return the texCoord X3DNode value. 
 * @return The texCoord X3DNode value.  */
    @Override
    public X3DNode getTexCoord() {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  return( texCoord.getValue( ) );
}

/** Set the texCoord field. 
 * @param val The X3DTextureCoordinateNode to set.  */
    @Override
    public void setTexCoord(X3DTextureCoordinateNode val) {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  texCoord.setValue( val );
}

/** Set the texCoord field. 
 * @param val The X3DProtoInstance to set.  */
    @Override
    public void setTexCoord(X3DProtoInstance val) {
  if ( texCoord == null ) { 
    texCoord = (SFNode)getField( "texCoord" ); 
  }
  texCoord.setValue( val );
}

/** Return the ccw boolean value. 
 * @return The ccw boolean value.  */
    @Override
    public boolean getCcw() {
  if ( ccw == null ) { 
    ccw = (SFBool)getField( "ccw" ); 
  }
  return( ccw.getValue( ) );
}

/** Set the ccw field. 
 * @param val The boolean to set.  */
    @Override
    public void setCcw(boolean val) {
  if ( ccw == null ) { 
    ccw = (SFBool)getField( "ccw" ); 
  }
  ccw.setValue( val );
}

/** Return the colorPerVertex boolean value. 
 * @return The colorPerVertex boolean value.  */
    @Override
    public boolean getColorPerVertex() {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  return( colorPerVertex.getValue( ) );
}

/** Set the colorPerVertex field. 
 * @param val The boolean to set.  */
    @Override
    public void setColorPerVertex(boolean val) {
  if ( colorPerVertex == null ) { 
    colorPerVertex = (SFBool)getField( "colorPerVertex" ); 
  }
  colorPerVertex.setValue( val );
}

/** Return the creaseAngle double value. 
 * @return The creaseAngle double value.  */
    @Override
    public double getCreaseAngle() {
  if ( creaseAngle == null ) { 
    creaseAngle = (SFDouble)getField( "creaseAngle" ); 
  }
  return( creaseAngle.getValue( ) );
}

/** Set the creaseAngle field. 
 * @param val The double to set.  */
    @Override
    public void setCreaseAngle(double val) {
  if ( creaseAngle == null ) { 
    creaseAngle = (SFDouble)getField( "creaseAngle" ); 
  }
  creaseAngle.setValue( val );
}

/** Return the geoGridOrigin value in the argument double[]
 * @param val The double[] to initialize.  */
    @Override
    public void getGeoGridOrigin(double[] val) {
  if ( geoGridOrigin == null ) { 
    geoGridOrigin = (SFVec3d)getField( "geoGridOrigin" ); 
  }
  geoGridOrigin.getValue( val );
}

/** Set the geoGridOrigin field. 
 * @param val The double[] to set.  */
    @Override
    public void setGeoGridOrigin(double[] val) {
  if ( geoGridOrigin == null ) { 
    geoGridOrigin = (SFVec3d)getField( "geoGridOrigin" ); 
  }
  geoGridOrigin.setValue( val );
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

/** Return the normalPerVertex boolean value. 
 * @return The normalPerVertex boolean value.  */
    @Override
    public boolean getNormalPerVertex() {
  if ( normalPerVertex == null ) { 
    normalPerVertex = (SFBool)getField( "normalPerVertex" ); 
  }
  return( normalPerVertex.getValue( ) );
}

/** Set the normalPerVertex field. 
 * @param val The boolean to set.  */
    @Override
    public void setNormalPerVertex(boolean val) {
  if ( normalPerVertex == null ) { 
    normalPerVertex = (SFBool)getField( "normalPerVertex" ); 
  }
  normalPerVertex.setValue( val );
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

/** Return the xDimension int value. 
 * @return The xDimension int value.  */
    @Override
    public int getXDimension() {
  if ( xDimension == null ) { 
    xDimension = (SFInt32)getField( "xDimension" ); 
  }
  return( xDimension.getValue( ) );
}

/** Set the xDimension field. 
 * @param val The int to set.  */
    @Override
    public void setXDimension(int val) {
  if ( xDimension == null ) { 
    xDimension = (SFInt32)getField( "xDimension" ); 
  }
  xDimension.setValue( val );
}

/** Return the xSpacing double value. 
 * @return The xSpacing double value.  */
    @Override
    public double getXSpacing() {
  if ( xSpacing == null ) { 
    xSpacing = (SFDouble)getField( "xSpacing" ); 
  }
  return( xSpacing.getValue( ) );
}

/** Set the xSpacing field. 
 * @param val The double to set.  */
    @Override
    public void setXSpacing(double val) {
  if ( xSpacing == null ) { 
    xSpacing = (SFDouble)getField( "xSpacing" ); 
  }
  xSpacing.setValue( val );
}

/** Return the zDimension int value. 
 * @return The zDimension int value.  */
    @Override
    public int getZDimension() {
  if ( zDimension == null ) { 
    zDimension = (SFInt32)getField( "zDimension" ); 
  }
  return( zDimension.getValue( ) );
}

/** Set the zDimension field. 
 * @param val The int to set.  */
    @Override
    public void setZDimension(int val) {
  if ( zDimension == null ) { 
    zDimension = (SFInt32)getField( "zDimension" ); 
  }
  zDimension.setValue( val );
}

/** Return the zSpacing double value. 
 * @return The zSpacing double value.  */
    @Override
    public double getZSpacing() {
  if ( zSpacing == null ) { 
    zSpacing = (SFDouble)getField( "zSpacing" ); 
  }
  return( zSpacing.getValue( ) );
}

/** Set the zSpacing field. 
 * @param val The double to set.  */
    @Override
    public void setZSpacing(double val) {
  if ( zSpacing == null ) { 
    zSpacing = (SFDouble)getField( "zSpacing" ); 
  }
  zSpacing.setValue( val );
}

/** Return the yScale float value. 
 * @return The yScale float value.  */
    @Override
    public float getYScale() {
  if ( yScale == null ) { 
    yScale = (SFFloat)getField( "yScale" ); 
  }
  return( yScale.getValue( ) );
}

/** Set the yScale field. 
 * @param val The float to set.  */
    @Override
    public void setYScale(float val) {
  if ( yScale == null ) { 
    yScale = (SFFloat)getField( "yScale" ); 
  }
  yScale.setValue( val );
}

}
