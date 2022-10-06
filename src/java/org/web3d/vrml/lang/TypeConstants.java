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

package org.web3d.vrml.lang;

// External imports
// None

// Local imports
// None

/**
 * Listing of type constants for nodes.
 * <p>
 * Each interface in the vrml.nodes area will have an entry.  These will be
 * used to make parsing faster by allowing the use of switch statements instead
 * of large if/else
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.38 $
 */
public interface TypeConstants {

    /** Convenience representation for no secondary node type */
    int[] NO_SECONDARY_TYPE = new int[0];

    /** Indicator that there is no primary type set */
    int NONE = -1;

    /**
     * The node is currently a proxy for an import statement that has not yet
     * been resolved. Once it has been resolved, we will know better and this
     * value will be replaced by that of the underlying node.
     */
    int UNRESOLVED_IMPORT_PROXY = -2;

    int AppearanceChildNodeType = 1;
    int AppearanceNodeType = 2;
    int AudioClipNodeType = 3;
    int BackgroundNodeType = 4;
    int BindableNodeType = 5;
    int BoundedNodeType = 6;
    int ChildNodeType = 7;
    int CollidableNodeType = 8;
    int ColorNodeType = 9;
    int CoordinateNodeType = 11;
    int DragSensorNodeType = 13;
    int EnvironmentalSensorNodeType = 14;
    int ExternalNodeType = 16;
    int ExternProtoDeclare = 17;
    int FogNodeType = 18;
    int FontStyleNodeType = 19;
    int GeometricPropertyNodeType = 20;
    int GeometryNodeType = 21;
    int GroupingNodeType = 22;
    int InlineNodeType = 24;
    int InterpolatorNodeType = 25;
    int KeyDeviceSensorNodeType = 26;
    int LightNodeType = 27;
    int LinkNodeType = 28;
    int MaterialNodeType = 29;
    int MultiExternalNodeType = 30;
    int NavigationInfoNodeType = 31;
    int NodeType = 33;
    int NormalNodeType = 34;
    int ParametricGeometryNodeType = 35;
    int PointingDeviceSensorNodeType = 36;
    int ProtoDeclare = 37;
    int ProtoInstance = 38;
    int ScriptNodeType = 41;
    int SensorNodeType = 42;
    int ShapeNodeType = 43;
    int SingleExternalNodeType = 44;
    int SoundNodeType = 45;
    int SurfaceMaterialNodeType = 46;
    int Texture2DNodeType = 47;
    int TextureCoordinateNodeType = 48;
    int TextureNodeType = 50;
    int TextureTransformNodeType = 51;
    int TimeDependentNodeType = 52;
    int TimeControlledNodeType = 53;
    int ViewpointNodeType = 55;
    int VisualMaterialNodeType = 56;
    int WorldRootNodeType = 57;
    int ComponentGeometryNodeType = 58;
    int StaticNodeType = 59;
    int SurfaceNodeType = 60;
    int SurfaceChildNodeType = 61;
    int SurfaceLayoutNodeType = 62;
    int OverlayNodeType = 63;

    /** Primary type indicating the node emits particles for a particle system */
    int ParticleEmitterNodeType = 64;

    /**
     * Primary type indicating the node controls the particles trajectory during
     * a running particle system.
     */
    int ParticlePhysicsModelNodeType = 65;

    /** Primary type indicating the node is a complete particle system */
    int ParticleSystemNodeType = 66;
    int InfoNodeType = 67;
    int SequencerNodeType = 68;
    int Texture3DNodeType = 69;
    int TouchSensorNodeType = 70;
    int EnvironmentTextureNodeType = 71;
    int MetadataObjectNodeType = 72;
    int PickingSensorNodeType = 73;
    int ExternalSynchronizedNodeType = 74;
    int NetworkInterfaceNodeType = 75;
    int ProductStructureChildNodeType = 76;
    int HumanoidNodeType = 77;
    int DeviceSensorNodeType = 78;
    int DeviceManagerNodeType = 79;

    /** Secondary type indicating the node can be a target for picking */
    int PickTargetNodeType = 80;

    /** Primary type for being a Joint in a rigid body physics system */
    int RigidJointNodeType = 81;

    /** Primary type for being a single body for physics */
    int RigidBodyNodeType = 82;

    /** Primary type for being a collection of bodies for physics */
    int RigidBodyCollectionNodeType = 83;

    /** Primary type for nodes that can collide against each other */
    int nBodyCollidableNodeType = 84;

    /** Primary type for being a collision space */
    int nBodyCollisionSpaceNodeType = 85;

    /** Primary type for being a collection of collision spaces */
    int nBodyCollisionCollectionNodeType = 86;

    /** Primary type for being a nbody collision sensor */
    int nBodyCollisionSensorNodeType = 87;

    /** Secondard type for being dependent on viewer movements */
    int ViewDependentNodeType = 88;

    /** Primary type for nodes that hold a single rendering layer */
    int LayerNodeType = 89;

    /** Primary type for nodes that contain collections of layers */
    int LayerSetNodeType = 90;

    /** Primary type for nodes that contain source for a shader program */
    int ShaderProgramNodeType = 91;

    /** Primary type for nodes that are a viewport for a layer */
    int ViewportNodeType = 92;

    /** Primary type for PointProperties node */
    int PointPropertiesNodeType = 93;

    /** A secondary type for CADLayers */
    int CADLayerNodeType = 94;

    /**
     * A secondary type for CADAssemblies, in addition to being a product
     * structure node.
     */
    int CADAssemblyNodeType = 95;

    /** Primary type for annotation target nodes */
    int AnnotationTargetType = 96;

    /** Primary type for annotation nodes that go into annotation targets */
    int AnnotationType = 97;

    /** Primary type for Geospatial SRF Parameters */
    int GeoSRFParamNodeType = 98;

    /** Primary type for Geospatial SRF Info Parameters */
    int GeoSRFParamInfoNodeType = 99;

    /** Primary type for Geospatial SRFT Parameters */
    int GeoSRFTParamNodeType = 100;

    /** Primary type for BREP node*/
    int BREPNodeType=101;

    /** Secondary type for BREP nodes */
    int BREPPointBREPType = 102;
    int BREPWireBREPType = 103;
    int BREPShellBREPType = 104;
    int BREPSolidBREPType = 105;

    /** Primary type for origin managed nodes */
    int OriginManagedNodeType = 101;

    /**
     * The last identifier used by the internal representations. If you are
     * extending Xj3D with your own node types, then any identifiers you assign
     * should start with a number greater than this number.
     */
    int LAST_NODE_TYPE_ID = 1000;
}
