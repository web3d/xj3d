/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

/**
 * Listing of type constants for X3D nodes.
 * <p>
 *
 * The type constants only represent the node classification types (abstract
 * types), not the individual node type.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface X3DNodeTypes {
    /*
    int X3DBoundedObject = 1;
    int X3DBounded2DObject = 2;
    int X3DURLObject = 3;
    int X3DAppearanceNode = 10;
    int X3DX3DAppearanceChildNode = 11;
    int X3DMaterialNode = 12;
    int X3DTextureNode = 13;
    int X3DTexture2DNode = 14;
    int X3DTexture3DNode = 15;
    int X3DTextureTransformNode = 16;
    int X3DTextureTransform2DNode = 17;
    int X3DGeometryNode = 18;
    int X3DTextNode = 19;
    int X3DParametricGeometryNode = 20;
    int X3DGeometricPropertyNode = 21;
    int X3DColorNode = 22;
    int X3DCoordinateNode = 23;
    int X3DNormalNode = 24;
    int X3DTextureCoordinateNode = 25;
    int X3DFontStyleNode = 26;
    int X3DProtoInstance = 27;
    int X3DChildNode = 28;
    int X3DBindableNode = 29;
    int X3DBackgroundNode = 30;
    int X3DGroupingNode = 31;
    int X3DShapeNode = 32;

    int X3DInterpolatorNode = 33;
    int X3DLightNode = 34;
    int X3DScriptNode = 35;
    int X3DSensorNode = 36;
    int X3DEnvironmentalSensorNode = 37;
    int X3DKeyDeviceSensorNode = 38;
    int X3DNetworkSensorNode = 39;
    int X3DPointingDeviceSensorNode = 40;
    int X3DDragSensorNode = 41;
    int X3DTouchSensorNode = 42;
    int X3DSequencerNode  = 43;
    int X3DTimeDependentNode = 44;
    int X3DSoundSourceNode = 45;
    int X3DTriggerNode = 46;
    int X3DInfoNode = 47;
    */
    int X3DBoundedObject = 1;
    int X3DMetadataObject = 2;
    int X3DUrlObject = 3;
    int X3DAppearanceNode = 10;
    int X3DAppearanceChildNode = 11;
    int X3DMaterialNode = 12;
    int X3DTextureNode = 13;
    int X3DTexture2DNode = 14;
    int X3DTextureTransformNode = 16;
    int X3DTextureTransform2DNode = 17;
    int X3DGeometryNode = 18;
    int X3DGeometricPropertyNode = 19;
    int X3DParametricGeometryNode = 20;
    int X3DNurbsSurfaceGeometryNode = 21;
    int X3DColorNode = 22;
    int X3DCoordinateNode = 23;
    int X3DNormalNode = 24;
    int X3DTextureCoordinateNode = 25;
    int X3DFontStyleNode = 26;
    int X3DProtoInstance = 27;
    int X3DChildNode = 28;
    int X3DBindableNode = 29;
    int X3DBackgroundNode = 30;
    int X3DGroupingNode = 31;
    int X3DShapeNode = 32;
    int X3DInterpolatorNode = 33;
    int X3DLightNode = 34;
    int X3DScriptNode = 35;
    int X3DSensorNode = 36;
    int X3DEnvironmentalSensorNode = 37;
    int X3DKeyDeviceSensorNode = 38;
    int X3DNetworkSensorNode = 39;
    int X3DPointingDeviceSensorNode = 40;
    int X3DDragSensorNode = 41;
    int X3DTouchSensorNode = 42;
    int X3DSequencerNode = 43;
    int X3DTimeDependentNode = 44;
    int X3DSoundSourceNode = 45;
    int X3DTriggerNode = 46;
    int X3DInfoNode = 47;
    int X3DNurbsControlCurveNode = 48;
}
