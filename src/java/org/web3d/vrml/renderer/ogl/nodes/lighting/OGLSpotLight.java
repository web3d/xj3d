/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.lighting;

// External imports
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.lighting.BaseSpotLight;
import org.web3d.vrml.renderer.ogl.nodes.OGLLightNodeType;

/**
 * OpenGL implementation of a spotlight.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class OGLSpotLight extends BaseSpotLight
    implements OGLLightNodeType, NodeUpdateListener  {

    /** Holds the OGL impl for the other light */
    private SpotLight implLight;

    /** Performance vars for local usage */
    private float[] flScratch;

    /** The bounding volume that determines the radius of the light */
    private BoundingSphere radiusBounds;

    /**
     * Construct a new default instance of this class.
     */
    public OGLSpotLight() {
        super();

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLSpotLight(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLLightNodeType
    //-------------------------------------------------------------

    /**
     * Get the light making up this LightNode.
     *
     * @return The OGL light instance
     */
    @Override
    public Light getLight() {
        return implLight;
    }


    //----------------------------------------------------------
    // Methods defined by UpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
        radiusBounds.setRadius(vfRadius);
        implLight.setEffectBounds(radiusBounds);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {

        flScratch[0] = vfColor[0] * vfAmbientIntensity;
        flScratch[1] = vfColor[1] * vfAmbientIntensity;
        flScratch[2] = vfColor[2] * vfAmbientIntensity;
        implLight.setAmbientColor(flScratch);

        flScratch[0] = vfColor[0] * vfIntensity;
        flScratch[1] = vfColor[1] * vfIntensity;
        flScratch[2] = vfColor[2] * vfIntensity;
        implLight.setDiffuseColor(flScratch);
        implLight.setSpecularColor(flScratch);

        implLight.setEnabled(vfOn);
        implLight.setGlobalOnly(vfGlobal);

        implLight.setPosition(vfLocation);
        implLight.setAttenuation(vfAttenuation);
        implLight.setCutOffAngle((float)(vfCutOffAngle / Math.PI * 180));

        flScratch[0] = vfDirection[0];
        flScratch[1] = vfDirection[1];
        flScratch[2] = vfDirection[2];

        implLight.setDirection(flScratch);

        float exp = 0.5f / vfBeamWidth;

        implLight.setDropOffRateExponent(exp);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLLightNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field ambientIntensity.
     *
     * @param intensity The new value
     */
    @Override
    public void setAmbientIntensity(float intensity)
        throws InvalidFieldValueException {

        super.setAmbientIntensity(intensity);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Accessor method to get current value of field color.
     *
     * @param newColor The new value
     */
    @Override
    public void setColor(float[] newColor)
        throws InvalidFieldValueException {

        super.setColor(newColor);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Accessor method to get current value of field Intensity.
     *
     * @param intensity The new value
     */
    @Override
    public void setIntensity(float intensity)
        throws InvalidFieldValueException {

        super.setIntensity(intensity);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Turn the light on or off.
     *
     * @param state The new value
     */
    @Override
    public void setOn(boolean state) {
        super.setOn(state);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Get the Aviatrix3d scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The AV3D representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return implLight;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if ((vrmlMajorVersion < 3) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion == 0)))
            vfGlobal = true;

        // cheat, and call it directly to do the updates.
        updateNodeDataChanges(null);
		updateNodeBoundsChanges(null);
    }

    /**
     * Set the current value of the global field.
     *
     * @param global true if this should have global effect, false for scoped
     * @throws InvalidFieldException Called on a node that belongs to VRML or
     *    X3D 3.0.
     */
    @Override
    public void setGlobal(boolean global)
        throws InvalidFieldException {

        super.setGlobal(global);

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    //----------------------------------------------------------
    // Methods defined by BaseSpotLight
    //----------------------------------------------------------

    /**
     * Set the direction of the spot light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param dir The new direction vector to use
     */
    @Override
    protected void setDirection(float[] dir) {
        super.setDirection(dir);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Set the location of the point light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param loc The new location to use
     */
    @Override
    protected void setLocation(float[] loc) {

        super.setLocation(loc);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Set the radius of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param radius The new radius to use
     * @throws InvalidFieldValueException Radius value was negative
     */
    @Override
    protected void setRadius(float radius)
        throws InvalidFieldValueException {

        super.setRadius(radius);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.boundsChanged(this);
        else
            updateNodeBoundsChanges(implLight);
    }

    /**
     * Set the attenuation factor of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param factor The new attenuation factor to use
     * @throws InvalidFieldValueException Radius value was negative
     */
    @Override
    protected void setAttenuation(float[] factor)
        throws InvalidFieldValueException {

        super.setAttenuation(factor);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Set the beamWidth of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param value The new width to use in radians
     * @throws InvalidFieldValueException Width was not 0 to PI/2
     */
    @Override
    protected void setBeamWidth(float value)
        throws InvalidFieldValueException {

        super.setBeamWidth(value);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Set the cutOffAngle of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param value The new width to use in radians
     * @throws InvalidFieldValueException Width was not 0 to PI/2
     */
    @Override
    protected void setCutOffAngle(float value)
        throws InvalidFieldValueException {

        super.setCutOffAngle(value);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Private, common initialization method for the constructors.
     */
    private void init() {
        flScratch = new float[3];
        implLight = new SpotLight();
        radiusBounds = new BoundingSphere(vfRadius);
    }
}
