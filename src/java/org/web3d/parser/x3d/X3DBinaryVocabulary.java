/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2006-2009
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.parser.x3d;

import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.tools.PrintTable;
import com.sun.xml.fastinfoset.util.StringArray;
import com.sun.xml.fastinfoset.util.LocalNameQualifiedNamesMap;
import com.sun.xml.fastinfoset.util.DuplicateAttributeVerifier;
import com.sun.xml.fastinfoset.util.KeyIntMap;

/**
 * A fixed vocabulary for X3D FastInfoset files.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class X3DBinaryVocabulary {
    private static final String RESERVED_ELEMENT_NAME = "RES_ELMNT_";
    private static final String RESERVED_ATTRIBUTE_NAME = "RES_ATT_";

    public static final SerializerVocabulary serializerVoc;
    public static final ParserVocabulary parserVoc;
    private static QualifiedName name;
    private static LocalNameQualifiedNamesMap.Entry entry;

    static {
        serializerVoc = new SerializerVocabulary();
        parserVoc = new ParserVocabulary();
        serializerVoc.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
        serializerVoc.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
        serializerVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);
        serializerVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI);

        parserVoc.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
        parserVoc.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
        parserVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);
        parserVoc.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI);

        addElement("Shape");
        addElement("Appearance");
        addElement("Material");
        addElement("IndexedFaceSet");
        addElement("ProtoInstance");
        addElement("Transform");
        addElement("ImageTexture");
        addElement("TextureTransform");
        addElement("Coordinate");
        addElement("Normal");
        addElement("Color");
        addElement("ColorRGBA");
        addElement("TextureCoordinate");
        addElement("ROUTE");
        addElement("fieldValue");
        addElement("Group");
        addElement("LOD");
        addElement("Switch");
        addElement("Script");
        addElement("IndexedTriangleFanSet");
        addElement("IndexedTriangleSet");
        addElement("IndexedTriangleStripSet");
        addElement("MultiTexture");
        addElement("MultiTextureCoordinate");
        addElement("MultiTextureTransform");
        addElement("IndexedLineSet");
        addElement("PointSet");
        addElement("StaticGroup");
        addElement("Sphere");
        addElement("Box");
        addElement("Cone");
        addElement("Anchor");
        addElement("Arc2D");
        addElement("ArcClose2D");
        addElement("AudioClip");
        addElement("Background");
        addElement("Billboard");
        addElement("BooleanFilter");
        addElement("BooleanSequencer");
        addElement("BooleanToggle");
        addElement("BooleanTrigger");
        addElement("Circle2D");
        addElement("Collision");
        addElement("ColorInterpolator");
        addElement("Contour2D");
        addElement("ContourPolyline2D");
        addElement("CoordinateDouble");
        addElement("CoordinateInterpolator");
        addElement("CoordinateInterpolator2D");
        addElement("Cylinder");
        addElement("CylinderSensor");
        addElement("DirectionalLight");
        addElement("Disk2D");
        addElement("EXPORT");
        addElement("ElevationGrid");
        addElement("EspduTransform");
        addElement("ExternProtoDeclare");
        addElement("Extrusion");
        addElement("FillProperties");
        addElement("Fog");
        addElement("FontStyle");
        addElement("GeoCoordinate");
        addElement("GeoElevationGrid");
        addElement("GeoLOD");
        addElement("GeoLocation");
        addElement("GeoMetadata");
        addElement("GeoOrigin");
        addElement("GeoPositionInterpolator");
        addElement("GeoTouchSensor");
        addElement("GeoViewpoint");
        addElement("HAnimDisplacer");
        addElement("HAnimHumanoid");
        addElement("HAnimJoint");
        addElement("HAnimSegment");
        addElement("HAnimSite");
        addElement("IMPORT");
        addElement("IS");
        addElement("Inline");
        addElement("IntegerSequencer");
        addElement("IntegerTrigger");
        addElement("KeySensor");
        addElement("LineProperties");
        addElement("LineSet");
        addElement("LoadSensor");
        addElement("MetadataDouble");
        addElement("MetadataFloat");
        addElement("MetadataInteger");
        addElement("MetadataSet");
        addElement("MetadataString");
        addElement("MovieTexture");
        addElement("NavigationInfo");
        addElement("NormalInterpolator");
        addElement("NurbsCurve");
        addElement("NurbsCurve2D");
        addElement("NurbsOrientationInterpolator");
        addElement("NurbsPatchSurface");
        addElement("NurbsPositionInterpolator");
        addElement("NurbsSet");
        addElement("NurbsSurfaceInterpolator");
        addElement("NurbsSweptSurface");
        addElement("NurbsSwungSurface");
        addElement("NurbsTextureCoordinate");
        addElement("NurbsTrimmedSurface");
        addElement("OrientationInterpolator");
        addElement("PixelTexture");
        addElement("PlaneSensor");
        addElement("PointLight");
        addElement("Polyline2D");
        addElement("Polypoint2D");
        addElement("PositionInterpolator");
        addElement("PositionInterpolator2D");
        addElement("ProtoBody");
        addElement("ProtoDeclare");
        addElement("ProtoInterface");
        addElement("ProximitySensor");
        addElement("ReceiverPdu");
        addElement("Rectangle2D");
        addElement("ScalarInterpolator");
        addElement("Scene");
        addElement("SignalPdu");
        addElement("Sound");
        addElement("SphereSensor");
        addElement("SpotLight");
        addElement("StringSensor");
        addElement("Text");
        addElement("TextureBackground");
        addElement("TextureCoordinateGenerator");
        addElement("TimeSensor");
        addElement("TimeTrigger");
        addElement("TouchSensor");
        addElement("TransmitterPdu");
        addElement("TriangleFanSet");
        addElement("TriangleSet");
        addElement("TriangleSet2D");
        addElement("TriangleStripSet");
        addElement("Viewpoint");
        addElement("VisibilitySensor");
        addElement("WorldInfo");
        addElement("X3D");
        addElement("component");
        addElement("connect");
        addElement("field");
        addElement("head");
        addElement("humanoidBodyType");
        addElement("meta");

        // New Elements from ISO/IEC FCD 19776-3 Ed. 2:200x
        addElement("CADAssembly");
        addElement("CADFace");
        addElement("CADLayer");
        addElement("CADPart");
        addElement("ComposedCubeMapTexture");
        addElement("ComposedShader");
        addElement("ComposedTexture3D");
        addElement("FloatVertexAttribute");
        addElement("FogCoordinate");
        addElement("GeneratedCubeMapTexture");
        addElement("ImageCubeMapTexture");
        addElement("ImageTexture3D");
        addElement("IndexedQuadSet");
        addElement("LocalFog");
        addElement("Matrix3VertexAttribute");
        addElement("Matrix4VertexAttribute");
        addElement("PackagedShader");
        addElement("PixelTexture3D");
        addElement("ProgramShader");
        addElement("QuadSet");
        addElement("ShaderPart");
        addElement("ShaderProgram");
        addElement("TextureCoordinate3D");
        addElement("TextureCoordinate4D");
        addElement("TextureTransform3D");
        addElement("TextureTransformMatrix3D");
        addElement("BallJoint");
        addElement("BoundedPhysicsModel");
        addElement("ClipPlane");
        addElement("CollidableOffset");
        addElement("CollidableShape");
        addElement("CollisionCollection");
        addElement("CollisionSensor");
        addElement("CollisionSpace");
        addElement("ColorDamper");
        addElement("ConeEmitter");
        addElement("Contact");
        addElement("CoordinateDamper");
        addElement("DISEntityManager");
        addElement("DISEntityTypeMapping");
        addElement("DoubleAxisHingeJoint");
        addElement("EaseInEaseOut");
        addElement("ExplosionEmitter");
        addElement("ForcePhysicsModel");
        addElement("GeoProximitySensor");
        addElement("GeoTransform");
        addElement("Layer");
        addElement("LayerSet");
        addElement("Layout");
        addElement("LayoutGroup");
        addElement("LayoutLayer");
        addElement("LinePickSensor");
        addElement("MotorJoint");
        addElement("OrientationChaser");
        addElement("OrientationDamper");
        addElement("OrthoViewpoint");
        addElement("ParticleSystem");
        addElement("PickableGroup");
        addElement("PointEmitter");
        addElement("PointPickSensor");
        addElement("PolylineEmitter");
        addElement("PositionChaser");
        addElement("PositionChaser2D");
        addElement("PositionDamper");
        addElement("PositionDamper2D");
        addElement("PrimitivePickSensor");
        addElement("RigidBody");
        addElement("RigidBodyCollection");
        addElement("ScalarChaser");
        addElement("ScreenFontStyle");
        addElement("ScreenGroup");
        addElement("SingleAxisHingeJoint");
        addElement("SliderJoint");
        addElement("SplinePositionInterpolator");
        addElement("SplinePositionInterpolator2D");
        addElement("SplineScalarInterpolator");
        addElement("SquadOrientationInterpolator");
        addElement("SurfaceEmitter");
        addElement("TexCoordDamper");
        addElement("TextureProperties");
        addElement("TransformSensor");
        addElement("TwoSidedMaterial");
        addElement("UniversalJoint");
        addElement("ViewpointGroup");
        addElement("Viewport");
        addElement("VolumeEmitter");
        addElement("VolumePickSensor");
        addElement("WindPhysicsModel");

        // New Elements proposed in 3.3  If we get them right then we don't have
        // to reprocess the content
        addElement("MatrixTransform");


        addAttribute("DEF");
        addAttribute("USE");
        addAttribute("containerField");
        addAttribute("fromNode");
        addAttribute("fromField");
        addAttribute("toNode");
        addAttribute("toField");
        addAttribute("name");
        addAttribute("value");
        addAttribute("color");
        addAttribute("colorIndex");
        addAttribute("coordIndex");
        addAttribute("texCoordIndex");
        addAttribute("normalIndex");
        addAttribute("colorPerVertex");
        addAttribute("normalPerVertex");
        addAttribute("rotation");
        addAttribute("scale");
        addAttribute("center");
        addAttribute("scaleOrientation");
        addAttribute("translation");
        addAttribute("url");
        addAttribute("repeatS");
        addAttribute("repeatT");
        addAttribute("point");
        addAttribute("vector");
        addAttribute("range");
        addAttribute("ambientIntensity");
        addAttribute("diffuseColor");
        addAttribute("emissiveColor");
        addAttribute("shininess");
        addAttribute("specularColor");
        addAttribute("transparency");
        addAttribute("whichChoice");
        addAttribute("index");
        addAttribute("mode");
        addAttribute("source");
        addAttribute("function");
        addAttribute("alpha");
        addAttribute("vertexCount");
        addAttribute("radius");
        addAttribute("size");
        addAttribute("height");
        addAttribute("solid");
        addAttribute("ccw");
        addAttribute("key");
        addAttribute("keyValue");
        addAttribute("enabled");
        addAttribute("direction");
        addAttribute("position");
        addAttribute("orientation");
        addAttribute("bboxCenter");
        addAttribute("bboxSize");
        addAttribute("AS");
        addAttribute("InlineDEF");
        addAttribute("accessType");
        addAttribute("actionKeyPress");
        addAttribute("actionKeyRelease");
        addAttribute("address");
        addAttribute("altKey");
        addAttribute("antennaLocation");
        addAttribute("antennaPatternLength");
        addAttribute("antennaPatternType");
        addAttribute("applicationID");
        addAttribute("articulationParameterArray");
        addAttribute("articulationParameterChangeIndicatorArray");
        addAttribute("articulationParameterCount");
        addAttribute("articulationParameterDesignatorArray");
        addAttribute("articulationParameterIdPartAttachedArray");
        addAttribute("articulationParameterTypeArray");
        addAttribute("attenuation");
        addAttribute("autoOffset");
        addAttribute("avatarSize");
        addAttribute("axisOfRotation");
        addAttribute("backUrl");
        addAttribute("beamWidth");
        addAttribute("beginCap");
        addAttribute("bindTime");
        addAttribute("bottom");
        addAttribute("bottomRadius");
        addAttribute("bottomUrl");
        addAttribute("centerOfMass");
        addAttribute("centerOfRotation");
        addAttribute("child1Url");
        addAttribute("child2Url");
        addAttribute("child3Url");
        addAttribute("child4Url");
        addAttribute("class");
        addAttribute("closureType");
        addAttribute("collideTime");
        addAttribute("content");
        addAttribute("controlKey");
        addAttribute("controlPoint");
        addAttribute("convex");
        addAttribute("coordinateSystem");
        addAttribute("copyright");
        addAttribute("creaseAngle");
        addAttribute("crossSection");
        addAttribute("cryptoKeyID");
        addAttribute("cryptoSystem");
        addAttribute("cutOffAngle");
        addAttribute("cycleInterval");
        addAttribute("cycleTime");
        addAttribute("data");
        addAttribute("dataFormat");
        addAttribute("dataLength");
        addAttribute("dataUrl");
        addAttribute("date");
        addAttribute("deadReckoning");
        addAttribute("deletionAllowed");
        addAttribute("description");
        addAttribute("detonateTime");
        addAttribute("dir");
        addAttribute("directOutput");
        addAttribute("diskAngle");
        addAttribute("displacements");
        addAttribute("documentation");
        addAttribute("elapsedTime");
        addAttribute("ellipsoid");
        addAttribute("encodingScheme");
        addAttribute("endAngle");
        addAttribute("endCap");
        addAttribute("enterTime");
        addAttribute("enteredText");
        addAttribute("entityCategory");
        addAttribute("entityCountry");
        addAttribute("entityDomain");
        addAttribute("entityExtra");
        addAttribute("entityID");
        addAttribute("entityKind");
        addAttribute("entitySpecific");
        addAttribute("entitySubcategory");
        addAttribute("exitTime");
        addAttribute("extent");
        addAttribute("family");
        addAttribute("fanCount");
        addAttribute("fieldOfView");
        addAttribute("filled");
        addAttribute("finalText");
        addAttribute("fireMissionIndex");
        addAttribute("fired1");
        addAttribute("fired2");
        addAttribute("firedTime");
        addAttribute("firingRange");
        addAttribute("firingRate");
        addAttribute("fogType");
        addAttribute("forceID");
        addAttribute("frequency");
        addAttribute("frontUrl");
        addAttribute("fuse");
        addAttribute("geoCoords");
        addAttribute("geoGridOrigin");
        addAttribute("geoSystem");
        addAttribute("groundAngle");
        addAttribute("groundColor");
        addAttribute("hatchColor");
        addAttribute("hatchStyle");
        addAttribute("hatched");
        addAttribute("headlight");
        addAttribute("horizontal");
        addAttribute("horizontalDatum");
        addAttribute("http-equiv");
        addAttribute("image");
        addAttribute("importedDEF");
        addAttribute("info");
        addAttribute("innerRadius");
        addAttribute("inputFalse");
        addAttribute("inputNegate");
        addAttribute("inputSource");
        addAttribute("inputTrue");
        addAttribute("integerKey");
        addAttribute("intensity");
        addAttribute("jump");
        addAttribute("justify");
        addAttribute("keyPress");
        addAttribute("keyRelease");
        addAttribute("knot");
        addAttribute("lang");
        addAttribute("language");
        addAttribute("leftToRight");
        addAttribute("leftUrl");
        addAttribute("length");
        addAttribute("lengthOfModulationParameters");
        addAttribute("level");
        addAttribute("limitOrientation");
        addAttribute("lineSegments");
        addAttribute("linearAcceleration");
        addAttribute("linearVelocity");
        addAttribute("linetype");
        addAttribute("linewidthScaleFactor");
        addAttribute("llimit");
        addAttribute("load");
        addAttribute("loadTime");
        addAttribute("localDEF");
        addAttribute("location");
        addAttribute("loop");
        addAttribute("marking");
        addAttribute("mass");
        addAttribute("maxAngle");
        addAttribute("maxBack");
        addAttribute("maxExtent");
        addAttribute("maxFront");
        addAttribute("maxPosition");
        addAttribute("metadataFormat");
        addAttribute("minAngle");
        addAttribute("minBack");
        addAttribute("minFront");
        addAttribute("minPosition");
        addAttribute("modulationTypeDetail");
        addAttribute("modulationTypeMajor");
        addAttribute("modulationTypeSpreadSpectrum");
        addAttribute("modulationTypeSystem");
        addAttribute("momentsOfInertia");
        addAttribute("multicastRelayHost");
        addAttribute("multicastRelayPort");
        addAttribute("munitionApplicationID");
        addAttribute("munitionEndPoint");
        addAttribute("munitionEntityID");
        addAttribute("munitionQuantity");
        addAttribute("munitionSiteID");
        addAttribute("munitionStartPoint");
        addAttribute("mustEvaluate");
        addAttribute("navType");
        addAttribute("networkMode");
        addAttribute("next");
        addAttribute("nodeField");
        addAttribute("offset");
        addAttribute("on");
        addAttribute("order");
        addAttribute("originator");
        addAttribute("outerRadius");
        addAttribute("parameter");
        addAttribute("pauseTime");
        addAttribute("pitch");
        addAttribute("points");
        addAttribute("port");
        addAttribute("power");
        addAttribute("previous");
        addAttribute("priority");
        addAttribute("profile");
        addAttribute("progress");
        addAttribute("protoField");
        addAttribute("radioEntityTypeCategory");
        addAttribute("radioEntityTypeCountry");
        addAttribute("radioEntityTypeDomain");
        addAttribute("radioEntityTypeKind");
        addAttribute("radioEntityTypeNomenclature");
        addAttribute("radioEntityTypeNomenclatureVersion");
        addAttribute("radioID");
        addAttribute("readInterval");
        addAttribute("receivedPower");
        addAttribute("receiverState");
        addAttribute("reference");
        addAttribute("relativeAntennaLocation");
        addAttribute("resolution");
        addAttribute("resumeTime");
        addAttribute("rightUrl");
        addAttribute("rootUrl");
        addAttribute("rotateYUp");
        addAttribute("rtpHeaderExpected");
        addAttribute("sampleRate");
        addAttribute("samples");
        addAttribute("shiftKey");
        addAttribute("side");
        addAttribute("siteID");
        addAttribute("skinCoordIndex");
        addAttribute("skinCoordWeight");
        addAttribute("skyAngle");
        addAttribute("skyColor");
        addAttribute("spacing");
        addAttribute("spatialize");
        addAttribute("speed");
        addAttribute("speedFactor");
        addAttribute("spine");
        addAttribute("startAngle");
        addAttribute("startTime");
        addAttribute("stiffness");
        addAttribute("stopTime");
        addAttribute("string");
        addAttribute("stripCount");
        addAttribute("style");
        addAttribute("summary");
        addAttribute("tdlType");
        addAttribute("tessellation");
        addAttribute("tessellationScale");
        addAttribute("time");
        addAttribute("timeOut");
        addAttribute("timestamp");
        addAttribute("title");
        addAttribute("toggle");
        addAttribute("top");
        addAttribute("topToBottom");
        addAttribute("topUrl");
        addAttribute("touchTime");
        addAttribute("transmitFrequencyBandwidth");
        addAttribute("transmitState");
        addAttribute("transmitterApplicationID");
        addAttribute("transmitterEntityID");
        addAttribute("transmitterRadioID");
        addAttribute("transmitterSiteID");
        addAttribute("transparent");
        addAttribute("triggerTime");
        addAttribute("triggerTrue");
        addAttribute("triggerValue");
        addAttribute("type");
        addAttribute("uDimension");
        addAttribute("uKnot");
        addAttribute("uOrder");
        addAttribute("uTessellation");
        addAttribute("ulimit");
        addAttribute("vDimension");
        addAttribute("vKnot");
        addAttribute("vOrder");
        addAttribute("vTessellation");
        addAttribute("version");
        addAttribute("verticalDatum");
        addAttribute("vertices");
        addAttribute("visibilityLimit");
        addAttribute("visibilityRange");
        addAttribute("warhead");
        addAttribute("weight");
        addAttribute("whichGeometry");
        addAttribute("writeInterval");
        addAttribute("xDimension");
        addAttribute("xSpacing");
        addAttribute("yScale");
        addAttribute("zDimension");
        addAttribute("zSpacing");

        // New Elements from ISO/IEC FCD 19776-3 Ed. 2:200x

        addAttribute("visible");
        addAttribute("repeatR");
        addAttribute("texture");
        addAttribute("back");
        addAttribute("front");
        addAttribute("left");
        addAttribute("right");
        addAttribute("parts");
        addAttribute("isSelected");
        addAttribute("isValid");
        addAttribute("numComponents");
        addAttribute("depth");
        addAttribute("update");
        addAttribute("fogCoord");
        addAttribute("texCoord");
        addAttribute("activate");
        addAttribute("programs");
        addAttribute("matrix");
        addAttribute("anchorPoint");
        addAttribute("body1");
        addAttribute("body2");
        addAttribute("mustOutput");
        addAttribute("body1AnchorPoint");
        addAttribute("body2AnchorPoint");
        addAttribute("plane");
        addAttribute("appliedParameters");
        addAttribute("bounce");
        addAttribute("frictionCoefficients");
        addAttribute("minBounceSpeed");
        addAttribute("slipFactors");
        addAttribute("softnessConstantForceMix");
        addAttribute("softnessErrorCorrection");
        addAttribute("surfaceSpeed");
        addAttribute("isActive");
        addAttribute("useGeometry");
        addAttribute("set_destination");
        addAttribute("set_value");
        addAttribute("tau");
        addAttribute("tolerance");
        addAttribute("value_changed");
        addAttribute("initialDestination");
        addAttribute("initialValue");
        addAttribute("angle");
        addAttribute("variation");
        addAttribute("surfaceArea");
        addAttribute("frictionDirection");
        addAttribute("slipCoefficients");
        addAttribute("category");
        addAttribute("country");
        addAttribute("domain");
        addAttribute("extra");
        addAttribute("kind");
        addAttribute("specific");
        addAttribute("subcategory");
        addAttribute("axis1");
        addAttribute("axis2");
        addAttribute("desiredAngularVelocity1");
        addAttribute("desiredAngularVelocity2");
        addAttribute("maxAngle1");
        addAttribute("maxTorque1");
        addAttribute("maxTorque2");
        addAttribute("minAngle1");
        addAttribute("stopBounce1");
        addAttribute("stopConstantForceMix1");
        addAttribute("stopErrorCorrection1");
        addAttribute("suspensionErrorCorrection");
        addAttribute("suspensionForce");
        addAttribute("body1Axis");
        addAttribute("body2Axis");
        addAttribute("hinge1Angle");
        addAttribute("hinge1AngleRate");
        addAttribute("hinge2Angle");
        addAttribute("hinge2AngleRate");
        addAttribute("set_fraction");
        addAttribute("easeInEaseOut");
        addAttribute("modifiedFraction_changed");
        addAttribute("force");
        addAttribute("geoCenter");
        addAttribute("centerOfRotation_changed");
        addAttribute("geoCoord_changed");
        addAttribute("orientation_changed");
        addAttribute("position_changed");
        addAttribute("isPickable");
        addAttribute("viewport");
        addAttribute("activeLayer");
        addAttribute("align");
        addAttribute("offsetUnits");
        addAttribute("scaleMode");
        addAttribute("sizeUnits");
        addAttribute("layout");
        addAttribute("objectType");
        addAttribute("pickedNormal");
        addAttribute("pickedPoint");
        addAttribute("pickedTextureCoordinate");
        addAttribute("intersectionType");
        addAttribute("sortOrder");
        addAttribute("axis1Angle");
        addAttribute("axis1Torque");
        addAttribute("axis2Angle");
        addAttribute("axis2Torque");
        addAttribute("axis3Angle");
        addAttribute("axis3Torque");
        addAttribute("enabledAxies");
        addAttribute("motor1Axis");
        addAttribute("motor2Axis");
        addAttribute("motor3Axis");
        addAttribute("stop1Bounce");
        addAttribute("stop1ErrorCorrection");
        addAttribute("stop2Bounce");
        addAttribute("stop2ErrorCorrection");
        addAttribute("stop3Bounce");
        addAttribute("stop3ErrorCorrection");
        addAttribute("motor1Angle");
        addAttribute("motor1AngleRate");
        addAttribute("motor2Angle");
        addAttribute("motor2AngleRate");
        addAttribute("motor3Angle");
        addAttribute("motor3AngleRate");
        addAttribute("autoCalc");
        addAttribute("duration");
        addAttribute("retainUserOffsets");
        addAttribute("isBound");
        addAttribute("appearance");
        addAttribute("createParticles");
        addAttribute("lifetimeVariation");
        addAttribute("maxParticles");
        addAttribute("particleLifetime");
        addAttribute("particleSize");
        addAttribute("colorKey");
        addAttribute("geometryType");
        addAttribute("texCoordKey");
        addAttribute("pickable");
        addAttribute("angularDampingFactor");
        addAttribute("angularVelocity");
        addAttribute("autoDamp");
        addAttribute("autoDisable");
        addAttribute("disableAngularSpeed");
        addAttribute("disableLinearSpeed");
        addAttribute("disableTime");
        addAttribute("finiteRotationAxis");
        addAttribute("fixed");
        addAttribute("forces");
        addAttribute("inertia");
        addAttribute("linearDampingFactor");
        addAttribute("torques");
        addAttribute("useFiniteRotation");
        addAttribute("useGlobalForce");
        addAttribute("constantForceMix");
        addAttribute("constantSurfaceThickness");
        addAttribute("errorCorrection");
        addAttribute("iterations");
        addAttribute("maxCorrectionSpeed");
        addAttribute("preferAccuracy");
        addAttribute("pointSize");
        addAttribute("stopBounce");
        addAttribute("stopErrorCorrection");
        addAttribute("angleRate");
        addAttribute("maxSeparation");
        addAttribute("minSeparation");
        addAttribute("separation");
        addAttribute("separationRate");
        addAttribute("closed");
        addAttribute("keyVelocity");
        addAttribute("normalizeVelocity");
        addAttribute("surface");
        addAttribute("anisotropicDegree");
        addAttribute("borderColor");
        addAttribute("borderWidth");
        addAttribute("boundaryModeS");
        addAttribute("boundaryModeT");
        addAttribute("boundaryModeR");
        addAttribute("magnificationFilter");
        addAttribute("minificationFilter");
        addAttribute("textureCompression");
        addAttribute("texturePriority");
        addAttribute("generateMipMaps");
        addAttribute("targetObject");
        addAttribute("backAmbientIntensity");
        addAttribute("backDiffuseColor");
        addAttribute("backEmissiveColor");
        addAttribute("backShininess");
        addAttribute("backSpecularColor");
        addAttribute("separateBackColor");
        addAttribute("displayed");
        addAttribute("clipBoundary");
        addAttribute("internal");
        addAttribute("gustiness");
        addAttribute("turbulence");

        addAttributeValue("false");
        addAttributeValue("true");


        reserveElement(512);
        reserveAttribute(1024);
    }


    /**
     * Add an element to the element table.
     *
     * @param eName The element name
     */
    public static void addElement(String eName) {
        int localNameIndex = serializerVoc.localName.obtainIndex(eName);

        if (localNameIndex > -1)
            System.out.println("Duplicate Element found: " + eName);
        else
            parserVoc.localName.add(eName);

        int idx = serializerVoc.elementName.getNextIndex();
        name = new QualifiedName("", "", eName, idx, -1, -1, idx);
        parserVoc.elementName.add(name);
        entry = serializerVoc.elementName.obtainEntry(eName);
        entry.addQualifiedName(name);
    }

    /**
     * Reserve element entries to the specified value.
     *
     * @param val The value to reserve to
     */
    public static void reserveElement(int val) {
        int idx = serializerVoc.elementName.getNextIndex();

        if (idx >= val)
            return;

        int len = val - idx;

        for (int i=0; i < len; i++) {
            String eName = RESERVED_ELEMENT_NAME + (idx + 1);

            int localNameIndex = serializerVoc.localName.obtainIndex(eName);

            if (localNameIndex > -1)
                System.out.println("Duplicate Element found: " + eName);
            else
                parserVoc.localName.add(eName);

            name = new QualifiedName("", "", eName, idx, -1, -1, idx);
            parserVoc.elementName.add(name);
            entry = serializerVoc.elementName.obtainEntry(eName);
            entry.addQualifiedName(name);
            idx = serializerVoc.elementName.getNextIndex();
        }
    }

    /**
     * Add an attribute to the attribute table.
     *
     * @param aName The attribute name
     */
    public static void addAttribute(String aName) {
        int localNameIndex = serializerVoc.localName.obtainIndex(aName);
        if (localNameIndex > -1)
            System.out.println("Duplicate Attribute found: " + aName);
        else
            parserVoc.localName.add(aName);

        int idx = serializerVoc.attributeName.getNextIndex();
        name = new QualifiedName("", "", aName, idx, -1, -1, idx);
        name.createAttributeValues(DuplicateAttributeVerifier.MAP_SIZE);
        parserVoc.attributeName.add(name);
        entry = serializerVoc.attributeName.obtainEntry(aName);
        entry.addQualifiedName(name);
    }


    /**
     * Reserve attribute entries to the specified value.
     *
     * @param val The value to reserve to
     */
    public static void reserveAttribute(int val) {
        int idx = serializerVoc.attributeName.getNextIndex();

        if (idx >= val)
            return;

        int len = val - idx;

        for (int i=0; i < len; i++) {
            String aName = RESERVED_ATTRIBUTE_NAME + (idx + 1);

            int localNameIndex = serializerVoc.localName.obtainIndex(aName);

            if (localNameIndex > -1)
                System.out.println("Duplicate Attribute found: " + aName);
            else
                parserVoc.localName.add(aName);

            name = new QualifiedName("", "", aName, idx, -1, -1, idx);
            name.createAttributeValues(DuplicateAttributeVerifier.MAP_SIZE);
            parserVoc.attributeName.add(name);
            entry = serializerVoc.attributeName.obtainEntry(aName);
            entry.addQualifiedName(name);
            idx = serializerVoc.attributeName.getNextIndex();
        }
    }

    /**
     * Add an attribute value to the tables.
     *
     * @param s the value
     */
    private static void addAttributeValue(String s) {
        if (serializerVoc.attributeValue.obtainIndex(s) == KeyIntMap.NOT_PRESENT) {
            parserVoc.attributeValue.add(s);
        }
    }

    /*
     * Print tables needed for ISO specification.
     */
    public static void main(String[] args) {

        PrintTable.printVocabulary(X3DBinaryVocabulary.parserVoc);

        StringArray a = X3DBinaryVocabulary.parserVoc.encodingAlgorithm;
        for (int i = 0; i < a.getSize(); i++) {
            String lName = a.get(i);
            System.out.println("<tr><td>" + (32+i) + "</td><td>"+ lName + "</td></tr>");
        }
    }
}