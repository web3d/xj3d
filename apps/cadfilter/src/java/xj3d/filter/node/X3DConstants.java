/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

/**
 * Constant values associated with X3D nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public interface X3DConstants {

    /** Enumeration of the node related abstract types */
    enum TYPE {
        X3DAppearanceChildNode,
        X3DAppearanceNode,
        X3DBackgroundNode,
        X3DBindableNode,
        X3DBoundedObject,
        X3DChaserNode,
        X3DChildNode,
        X3DColorNode,
        X3DComposedGeometryNode,
        X3DCoordinateNode,
        X3DDamperNode,
        X3DDragSensorNode,
        X3DEnvironmentTextureNode,
        X3DEnvironmentalSensorNode,
        X3DFogObject,
        X3DFollowerNode,
        X3DFontStyleNode,
        X3DGeometricPropertyNode,
        X3DGeometryNode,
        X3DGroupingNode,
        X3DInfoNode,
        X3DInterpolatorNode,
        X3DKeyDeviceSensorNode,
        X3DLayerNode,
        X3DLayoutNode,
        X3DLightNode,
        X3DMaterialNode,
        X3DMetadataObject,
        X3DNBodyCollidableNode,
        X3DNBodyCollisionSpaceNode,
        X3DNetworkSensorNode,
        X3DNode,
        X3DNormalNode,
        X3DNurbsControlCurveNode,
        X3DNurbsSurfaceGeometryNode,
        X3DParametricGeometryNode,
        X3DParticleEmitterNode,
        X3DParticlePhysicsModelNode,
        X3DPickableObject,
        X3DPickingNode,
        X3DPointingDeviceSensorNode,
        X3DProductStructureChildNode,
        X3DProgrammableShaderObject,
        X3DProtoInstance,
        X3DRigidJointNode,
        X3DScriptNode,
        X3DSensorNode,
        X3DSequencerNode,
        X3DShaderNode,
        X3DShapeNode,
        X3DSoundNode,
        X3DSoundSourceNode,
        X3DTexture2DNode,
        X3DTexture3DNode,
        X3DTextureCoordinateNode,
        X3DTextureNode,
        X3DTextureTransform2DNode,
        X3DTextureTransformNode,
        X3DTimeDependentNode,
        X3DTouchSensorNode,
        X3DTriggerNode,
        X3DUrlObject,
        X3DVertexAttributeNode,
        X3DViewpointNode,
        X3DViewportNode
    };
}
