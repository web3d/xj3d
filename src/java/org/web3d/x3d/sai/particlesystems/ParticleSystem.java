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

package org.web3d.x3d.sai.particlesystems;

import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DParticleEmitterNode;
import org.web3d.x3d.sai.X3DProtoInstance;
import org.web3d.x3d.sai.X3DShapeNode;

/**
 * Defines the requirements of an X3D ParticleSystem node
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public interface ParticleSystem extends X3DShapeNode {

    /**
     * Return the geometryType String value.
     *
     * @return The geometryType String value.
     */
    String getGeometryType();

    /**
     * Set the geometryType field.
     *
     * @param val The String to set.
     */
    void setGeometryType(String val);

    /**
     * Return the enabled boolean value.
     *
     * @return The enabled boolean value.
     */
    boolean getEnabled();

    /**
     * Set the enabled field.
     *
     * @param val The boolean to set.
     */
    void setEnabled(boolean val);

    /**
     * Return the maxParticles int value.
     *
     * @return The maxParticles int value.
     */
    int getMaxParticles();

    /**
     * Set the maxParticles field.
     *
     * @param val The int to set.
     */
    void setMaxParticles(int val);

    /**
     * Return the particleLifetime float value.
     *
     * @return The particleLifetime float value.
     */
    float getParticleLifetime();

    /**
     * Set the particleLifetime field.
     *
     * @param val The float to set.
     */
    void setParticleLifetime(float val);

    /**
     * Return the lifetimeVariation float value.
     *
     * @return The lifetimeVariation float value.
     */
    float getLifetimeVariation();

    /**
     * Set the lifetimeVariation field.
     *
     * @param val The float to set.
     */
    void setLifetimeVariation(float val);

    /**
     * Return the emitter X3DNode value.
     *
     * @return The emitter X3DNode value.
     */
    X3DNode getEmitter();

    /**
     * Set the emitter field.
     *
     * @param val The X3DParticleEmitterNode to set.
     */
    void setEmitter(X3DParticleEmitterNode val);

    /**
     * Set the emitter field.
     *
     * @param val The X3DProtoInstance to set.
     */
    void setEmitter(X3DProtoInstance val);

    /**
     * Return the number of MFNode items in the physics field.
     *
     * @return the number of MFNode items in the physics field.
     */
    int getNumPhysics();

    /**
     * Return the physics value in the argument X3DNode[]
     *
     * @param val The X3DNode[] to initialize.
     */
    void getPhysics(X3DNode[] val);

    /**
     * Set the physics field.
     *
     * @param val The X3DNode[] to set.
     */
    void setPhysics(X3DNode[] val);

    /**
     * Return the colorRamp X3DNode value.
     *
     * @return The colorRamp X3DNode value.
     */
    X3DNode getColorRamp();

    /**
     * Set the colorRamp field.
     *
     * @param val The X3DNode to set.
     */
    void setColorRamp(X3DNode val);

    /**
     * Return the number of MFFloat items in the colorKey field.
     *
     * @return the number of MFFloat items in the colorKey field.
     */
    int getNumColorKey();

    /**
     * Return the colorKey value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getColorKey(float[] val);

    /**
     * Set the colorKey field.
     *
     * @param val The float[] to set.
     */
    void setColorKey(float[] val);

    /**
     * Return the isActive boolean value.
     *
     * @return The isActive boolean value.
     */
    boolean getIsActive();

    /**
     * Return the particleSize value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getParticleSize(float[] val);

    /**
     * Set the particleSize field.
     *
     * @param val The float[] to set.
     */
    void setParticleSize(float[] val);

    /**
     * Return the createParticles boolean value.
     *
     * @return The createParticles boolean value.
     */
    boolean getCreateParticles();

    /**
     * Set the createParticles field.
     *
     * @param val The boolean to set.
     */
    void setCreateParticles(boolean val);

    /**
     * Return the texCoordRamp X3DNode value.
     *
     * @return The texCoordRamp X3DNode value.
     */
    X3DNode getTexCoordRamp();

    /**
     * Set the texCoordRamp field.
     *
     * @param val The X3DNode to set.
     */
    void setTexCoordRamp(X3DNode val);

    /**
     * Return the number of MFFloat items in the texCoordKey field.
     *
     * @return the number of MFFloat items in the texCoordKey field.
     */
    int getNumTexCoordKey();

    /**
     * Return the texCoordKey value in the argument float[]
     *
     * @param val The float[] to initialize.
     */
    void getTexCoordKey(float[] val);

    /**
     * Set the texCoordKey field.
     *
     * @param val The float[] to set.
     */
    void setTexCoordKey(float[] val);
}
