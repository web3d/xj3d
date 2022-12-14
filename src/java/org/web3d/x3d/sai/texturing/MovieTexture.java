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

package org.web3d.x3d.sai.texturing;

import org.web3d.x3d.sai.X3DSoundSourceNode;
import org.web3d.x3d.sai.X3DTexture2DNode;
import org.web3d.x3d.sai.X3DUrlObject;

/** Defines the requirements of an X3D MovieTexture node
 * @author Rex Melton
 * @version $Revision: 1.1 $ */
public interface MovieTexture extends X3DTexture2DNode, X3DSoundSourceNode, X3DUrlObject {

/** Return the speed float value. 
 * @return The speed float value.  */
public float getSpeed();

/** Set the speed field. 
 * @param val The float to set.  */
public void setSpeed(float val);

}
