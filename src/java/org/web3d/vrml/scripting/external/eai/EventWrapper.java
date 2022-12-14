package org.web3d.vrml.scripting.external.eai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 *  EventWrapper is an interface used to avoid the annoying circular
 *  dependency introduced by the EAI specification requiring that
 *  an EventOutX and and EventInX which both refer to the same underlying
 *  field are equal (equals returns true).
 *   <p>
 *  This interface exposes implementation details because they are necessary
 *  for the implementation of the equals method, and should not be used
 *  for evil.
 */

interface EventWrapper {
    /** The underlying field ID */
    int getFieldID();
  
    /** The underlying implementation node. */
    VRMLNodeType getFieldNode();

    /** The type of the underlying field.
      * Method name chosen to coincide with vrml.eai.field.BaseField.
      */
    int getType();

}
