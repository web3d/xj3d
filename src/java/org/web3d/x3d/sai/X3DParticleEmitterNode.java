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

package org.web3d.x3d.sai;

// Standard library imports
// None

// Local imports
// None

/**
 * Defines the requirements of an X3DParticleEmitterNode abstract node type
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface X3DParticleEmitterNode extends X3DNode {

    /**
     * Return the speed float value.
     *
     * @return The speed float value.
     */
    float getSpeed();

    /**
     * Set the speed field.
     *
     * @param val The float to set.
     */
    void setSpeed(float val);

    /**
     * Return the mass float value.
     *
     * @return The mass float value.
     */
    float getMass();

    /**
     * Set the mass field.
     *
     * @param val The float to set.
     */
    void setMass(float val);

    /**
     * Return the surfaceArea float value.
     *
     * @return The surfaceArea float value.
     */
    float getSurfaceArea();

    /**
     * Set the surfaceArea field.
     *
     * @param val The float to set.
     */
    void setSurfaceArea(float val);

    /**
     * Return the variation float value.
     *
     * @return The variation float value.
     */
    float getVariation();

    /**
     * Set the variation field.
     *
     * @param val The float to set.
     */
    void setVariation(float val);
}
