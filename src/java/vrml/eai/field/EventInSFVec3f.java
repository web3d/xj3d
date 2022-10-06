/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

/**
 * VRML eventIn class for SFVec3f.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFVec3f extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFVec3f.
   */
  protected EventInSFVec3f()
  {
    super(SFVec3f);
  }

  /**
   * Set the vector value in the given eventIn.
   *  <p>
   * The value array must contain at least three elements. If the array
   * contains more than 3 values only the first 3 values will be used and
   * the rest ignored.
   *  <p>
   * If the array of values does not contain at least 3 elements an
   * ArrayIndexOutOfBoundsException will be generated.
   *
   * @param value The array of vector components where <br>
   *    value[0] = X <br>
   *    value[1] = Y <br>
   *    value[2] = Z
   *
   * @exception ArrayIndexOutOfBoundsException The value did not contain at least three
   *    values for the vector
   */
  public abstract void setValue(float[] value);
}









