/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.x3d;

// Standard imports
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.FieldConstants;

import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * X3DConstants miscellaneous object.
 *  <p>
 *
 * All properties are fixed, read-only.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class X3DConstants extends AbstractScriptableObject {

    /** Set of the valid property names for this object */
    private static final Map<String, Integer> propertyValues;

    static {
        propertyValues = new HashMap<>();
        propertyValues.put("INITIALIZED_EVENT", 0);
        propertyValues.put("SHUTDOWN_EVENT", 1);
        propertyValues.put("CONNECTION_ERROR", 2);
        propertyValues.put("INITIALIZED_ERROR", 3);
        propertyValues.put("NOT_STARTED_STATE", 4);
        propertyValues.put("IN_PROGRESS_STATE", 5);
        propertyValues.put("COMPLETE_STATE", 6);
        propertyValues.put("FAILED_STATE", 7);
        propertyValues.put("SFBool", FieldConstants.SFBOOL);
        propertyValues.put("MFBool", FieldConstants.MFBOOL);
        propertyValues.put("MFInt32", FieldConstants.MFINT32);
        propertyValues.put("SFInt32", FieldConstants.SFINT32);
        propertyValues.put("SFFloat", FieldConstants.SFFLOAT);
        propertyValues.put("MFFloat", FieldConstants.MFFLOAT);
        propertyValues.put("SFDouble", FieldConstants.SFDOUBLE);
        propertyValues.put("MFDouble", FieldConstants.MFDOUBLE);
        propertyValues.put("SFTime", FieldConstants.SFTIME);
        propertyValues.put("MFTime", FieldConstants.MFTIME);
        propertyValues.put("SFNode", FieldConstants.SFNODE);
        propertyValues.put("MFNode", FieldConstants.MFNODE);
        propertyValues.put("SFVec2f", FieldConstants.SFVEC2F);
        propertyValues.put("MFVec2f", FieldConstants.MFVEC2F);
        propertyValues.put("SFVec3f", FieldConstants.SFVEC3F);
        propertyValues.put("MFVec3f", FieldConstants.MFVEC3F);
        propertyValues.put("SFVec3d", FieldConstants.SFVEC3D);
        propertyValues.put("MFVec3d", FieldConstants.MFVEC3D);
        propertyValues.put("SFRotation", FieldConstants.SFROTATION);
        propertyValues.put("MFRotation", FieldConstants.MFROTATION);
        propertyValues.put("SFColor", FieldConstants.SFCOLOR);
        propertyValues.put("MFColor", FieldConstants.MFCOLOR);
        propertyValues.put("SFColorRGBA", FieldConstants.SFCOLORRGBA);
        propertyValues.put("MFColorRGBA", FieldConstants.MFCOLORRGBA);
        propertyValues.put("SFImage", FieldConstants.SFIMAGE);
        propertyValues.put("MFImage", FieldConstants.MFIMAGE);
        propertyValues.put("SFString", FieldConstants.SFSTRING);
        propertyValues.put("MFString", FieldConstants.MFSTRING);
        propertyValues.put("inputOutput", FieldConstants.EXPOSEDFIELD);
        propertyValues.put("initializeOnly", FieldConstants.FIELD);
        propertyValues.put("inputOnly", FieldConstants.EVENTIN);
        propertyValues.put("outputOnly", FieldConstants.EVENTOUT);
        propertyValues.put("X3DBoundedObject", TypeConstants.BoundedNodeType);
        propertyValues.put("X3DUrlObject", TypeConstants.ExternalNodeType);
        propertyValues.put("X3DAppearanceNode", TypeConstants.AppearanceNodeType);
        propertyValues.put("X3DAppearanceChildNode", TypeConstants.AppearanceChildNodeType);
        propertyValues.put("X3DMaterialNode", TypeConstants.MaterialNodeType);
        propertyValues.put("X3DTextureNode", TypeConstants.TextureNodeType);
        propertyValues.put("X3DTexture2DNode", TypeConstants.Texture2DNodeType);
        propertyValues.put("X3DTexture3DNode", TypeConstants.Texture3DNodeType);
        propertyValues.put("X3DTextureTransformNode", TypeConstants.TextureTransformNodeType);
        propertyValues.put("X3DGeometryNode", TypeConstants.GeometryNodeType);
        propertyValues.put("X3DParametricGeometryNode", TypeConstants.ParametricGeometryNodeType);
        propertyValues.put("X3DGeometricPropertyNode", TypeConstants.GeometricPropertyNodeType);
        propertyValues.put("X3DColorNode", TypeConstants.ColorNodeType);
        propertyValues.put("X3DCoordinateNode", TypeConstants.CoordinateNodeType);
        propertyValues.put("X3DNormalNode", TypeConstants.NormalNodeType);
        propertyValues.put("X3DTextureCoordinateNode", TypeConstants.TextureCoordinateNodeType);
        propertyValues.put("X3DFontStyleNode", TypeConstants.FontStyleNodeType);
        propertyValues.put("X3DProtoInstance", TypeConstants.ProtoInstance);
        propertyValues.put("X3DChildNode", TypeConstants.ChildNodeType);
        propertyValues.put("X3DBindableNode", TypeConstants.BindableNodeType);
        propertyValues.put("X3DBackgroundNode", TypeConstants.BackgroundNodeType);
        propertyValues.put("X3DGroupingNode", TypeConstants.GroupingNodeType);
        propertyValues.put("X3DShapeNode", TypeConstants.ShapeNodeType);
        propertyValues.put("X3DInterpolatorNode", TypeConstants.InterpolatorNodeType);
        propertyValues.put("X3DLightNode", TypeConstants.LightNodeType);
        propertyValues.put("X3DScriptNode", TypeConstants.ScriptNodeType);
        propertyValues.put("X3DSensorNode", TypeConstants.SensorNodeType);
        propertyValues.put("X3DDeviceSensorNode", TypeConstants.DeviceSensorNodeType);
        propertyValues.put("X3DEnvironmentalSensorNode", TypeConstants.EnvironmentalSensorNodeType);
        propertyValues.put("X3DKeyDeviceSensorNode", TypeConstants.KeyDeviceSensorNodeType);
        propertyValues.put("X3DNetworkSensorNode", TypeConstants.LAST_NODE_TYPE_ID + 1);
        propertyValues.put("X3DPointingDeviceSensorNode", TypeConstants.PointingDeviceSensorNodeType);
        propertyValues.put("X3DDragSensorNode", TypeConstants.DragSensorNodeType);
        propertyValues.put("X3DTouchSensorNode", TypeConstants.TouchSensorNodeType);
        propertyValues.put("X3DSequencerNode", TypeConstants.LAST_NODE_TYPE_ID + 2);
        propertyValues.put("X3DTimeDependentNode", TypeConstants.TimeDependentNodeType);
        propertyValues.put("X3DSoundNode", TypeConstants.SoundNodeType);
        propertyValues.put("X3DTriggerNode", TypeConstants.LAST_NODE_TYPE_ID + 3);
        propertyValues.put("X3DInfoNode", TypeConstants.InfoNodeType);
    }

    /**
     * Construct an instance of this class.
     */
    public X3DConstants() {
        super("X3DConstants");
    }

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    @Override
    public boolean has(String name, Scriptable start) {
        return propertyValues.containsKey(name);
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = propertyValues.get(name);

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }
}
