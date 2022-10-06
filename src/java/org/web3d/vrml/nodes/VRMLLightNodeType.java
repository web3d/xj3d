/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// External imports
// none

// Local imports
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract representation of any form of light node.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface VRMLLightNodeType extends VRMLChildNodeType {

    /**
     * Get the current value of field ambientIntensity. Default value is 0.
     *
     * @return The current value of ambientIntensity
     */
    float getAmbientIntensity();

    /**
     * Get the current value of field color. Default value is 1 1 1
     *
     * @return The current value of color
     */
    float[] getColor();

    /**
     * Get the current value of field Intensity. Default value is 1.
     *
     * @return the current value of Intensity
     */
    float getIntensity();

    /**
     * Get the current value of field On. Default value is true.
     *
     * @return the current value of On
     */
    boolean getOn();

    /**
     * Accessor method to get current value of field global. Default value is
     * false if the node is from 3.1 onwards. The field does not exist in the
     * 3.0 or 2.0 specifications, so we return the default for the particular
     * light type and its spec-defined behaviour.
     *
     * @return the current value of the global field
     */
    boolean getGlobal();

    /**
     * Set the new value of the ambientIntensity field.
     *
     * @param newAmbientIntensity A value between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    void setAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException;

    /**
     * Set the new value of the color field.
     *
     * @param newColor The new value. Each component must be between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    void setColor(float[] newColor)
        throws InvalidFieldValueException;

    /**
     * Get the current value of field Intensity
     *
     * @param newIntensity A value between 0 and 1
     * @throws InvalidFieldValueException The value was out of the valid range
     */
    void setIntensity(float newIntensity)
        throws InvalidFieldValueException;

    /**
     * Set the value of field On.
     *
     * @param newOn true will turn the light on, false to turn it off
     */
    void setOn(boolean newOn);

    /**
     * Set the current value of the global field.
     *
     * @param global true if this should have global effect, false for scoped
     * @throws InvalidFieldException Called on a node that belongs to VRML or
     *    X3D 3.0.
     */
    void setGlobal(boolean global)
        throws InvalidFieldException;
}
