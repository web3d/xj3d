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

// External imports
import java.util.HashMap;
import java.util.Map;

import org.web3d.vrml.lang.FieldConstants;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.sav.ScriptHandler;

// Local imports
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * Factory class for producing CommonEncodable objects.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class CommonEncodableFactory {

    /** Map of the known nodes, and their respective info */
    private static Map<String, CommonEncodable> nodeMap;

    /** The field parser */
    private VRMLFieldReader reader;

    /** Content Handler reference */
    private ContentHandler handler;

    /** Script Handler reference */
    private ScriptHandler scriptHandler;

    /**
     * Constructor
     *
     * @param handler the ContentHandler
     * @param scriptHandler the ScriptHandler
     * @param reader the VRMLFieldReader
     */
    public CommonEncodableFactory(ContentHandler handler, ScriptHandler scriptHandler, VRMLFieldReader reader) {
        this.handler = handler;
        this.scriptHandler = scriptHandler;
        this.reader = reader;
        if (nodeMap == null) {
            initNodeMap();
        }
    }
    /**
     * Return the CommonEncodable object for the argument
     * node name. If the node is unknown, null is
     * returned.
     *
     * @param nodeName The node identifier
     * @param defName The DEF name to assign to the node
     * @return The node object
     */
    public CommonEncodable getEncodable(String nodeName, String defName) {
        if (nodeName.equals("Scene")) {
            return(new CommonScene());
        } else {
            CommonEncodable prototype = nodeMap.get(nodeName);
            if (prototype == null) {
                return(null);
            } else {
                CommonEncodable node = prototype.clone(false);
                node.setDefName(defName);
                node.setContentHandler(handler);
                node.setScriptHandler(scriptHandler);
                node.setFieldReader(reader);
                return(node);
            }
        }
    }

    /** Initialize the known nodes */
    private void initNodeMap() {

        nodeMap = new HashMap<>();
        X3DInterfaceMapper xim = new X3DInterfaceMapper();
        /////////////////////////////////////////////////////////////////////
        // note: only initializeOnly and inputOutput fields are enumerated.
        // since inputOnly and outputOnly fields cannot be initialized from
        // a file, they are ignored for the purposes of capturing the data
        // parsed from an x3d file.
        /////////////////////////////////////////////////////////////////////

        FieldInfo[] fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("name", FieldConstants.SFSTRING),
            new FieldInfo("reference", FieldConstants.SFSTRING),
            new FieldInfo("value", FieldConstants.MFINT32),
        };
        String nodeName = "MetadataInteger";
        TYPE[] nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("name", FieldConstants.SFSTRING),
            new FieldInfo("reference", FieldConstants.SFSTRING),
            new FieldInfo("value", FieldConstants.MFSTRING),
        };
        nodeName = "MetadataString";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("name", FieldConstants.SFSTRING),
            new FieldInfo("reference", FieldConstants.SFSTRING),
            new FieldInfo("value", FieldConstants.MFFLOAT),
        };
        nodeName = "MetadataFloat";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("name", FieldConstants.SFSTRING),
            new FieldInfo("reference", FieldConstants.SFSTRING),
            new FieldInfo("value", FieldConstants.MFDOUBLE),
        };
        nodeName = "MetadataDouble";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("name", FieldConstants.SFSTRING),
            new FieldInfo("reference", FieldConstants.SFSTRING),
            new FieldInfo("value", FieldConstants.MFNODE),
        };
        nodeName = "MetadataSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("groundAngle", FieldConstants.MFFLOAT),
            new FieldInfo("groundColor", FieldConstants.MFCOLOR),
            new FieldInfo("skyAngle", FieldConstants.MFFLOAT),
            new FieldInfo("skyColor", FieldConstants.MFCOLOR),
            new FieldInfo("backUrl", FieldConstants.MFSTRING),
            new FieldInfo("frontUrl", FieldConstants.MFSTRING),
            new FieldInfo("leftUrl", FieldConstants.MFSTRING),
            new FieldInfo("rightUrl", FieldConstants.MFSTRING),
            new FieldInfo("bottomUrl", FieldConstants.MFSTRING),
            new FieldInfo("topUrl", FieldConstants.MFSTRING),
            new FieldInfo("transparency", FieldConstants.SFFLOAT),
        };
        nodeName = "Background";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFCOLOR),
            new FieldInfo("fogType", FieldConstants.SFSTRING),
            new FieldInfo("visibilityRange", FieldConstants.SFFLOAT),
        };
        nodeName = "Fog";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("center", FieldConstants.SFVEC3F),
            new FieldInfo("size", FieldConstants.SFVEC3F),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
        };
        nodeName = "ProximitySensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("center", FieldConstants.SFVEC3F),
            new FieldInfo("size", FieldConstants.SFVEC3F),
        };
        nodeName = "VisibilitySensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
        };
        nodeName = "BooleanFilter";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFBOOL),
        };
        nodeName = "BooleanSequencer";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("toggle", FieldConstants.SFBOOL),
        };
        nodeName = "BooleanToggle";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
        };
        nodeName = "BooleanTrigger";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFINT32),
        };
        nodeName = "IntegerSequencer";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("integerKey", FieldConstants.SFINT32),
        };
        nodeName = "IntegerTrigger";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
        };
        nodeName = "TimeTrigger";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("lineSegments", FieldConstants.MFVEC2F),
        };
        nodeName = "Polyline2D";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("point", FieldConstants.MFVEC2F),
        };
        nodeName = "Polypoint2D";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("size", FieldConstants.SFVEC2F),
        };
        nodeName = "Rectangle2D";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("vertices", FieldConstants.MFVEC2F),
        };
        nodeName = "TriangleSet2D";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("size", FieldConstants.SFVEC3F),
        };
        nodeName = "Box";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("bottomRadius", FieldConstants.SFFLOAT),
            new FieldInfo("height", FieldConstants.SFFLOAT),
            new FieldInfo("bottom", FieldConstants.SFBOOL),
            new FieldInfo("side", FieldConstants.SFBOOL),
        };
        nodeName = "Cone";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("radius", FieldConstants.SFFLOAT),
            new FieldInfo("height", FieldConstants.SFFLOAT),
            new FieldInfo("bottom", FieldConstants.SFBOOL),
            new FieldInfo("side", FieldConstants.SFBOOL),
            new FieldInfo("top", FieldConstants.SFBOOL),
        };
        nodeName = "Cylinder";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("radius", FieldConstants.SFFLOAT),
        };
        nodeName = "Sphere";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("colorIndex", FieldConstants.MFINT32),
            new FieldInfo("coordIndex", FieldConstants.MFINT32),
            new FieldInfo("texCoordIndex", FieldConstants.MFINT32),
            new FieldInfo("normalIndex", FieldConstants.MFINT32),
            new FieldInfo("creaseAngle", FieldConstants.SFFLOAT),
            new FieldInfo("convex", FieldConstants.SFBOOL),
        };
        nodeName = "IndexedFaceSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("creaseAngle", FieldConstants.SFFLOAT),
            new FieldInfo("height", FieldConstants.MFFLOAT),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("xDimension", FieldConstants.SFINT32),
            new FieldInfo("xSpacing", FieldConstants.SFFLOAT),
            new FieldInfo("zDimension", FieldConstants.SFINT32),
            new FieldInfo("zSpacing", FieldConstants.SFFLOAT),
        };
        nodeName = "ElevationGrid";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("beginCap", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("convex", FieldConstants.SFBOOL),
            new FieldInfo("creaseAngle", FieldConstants.SFFLOAT),
            new FieldInfo("crossSection", FieldConstants.MFVEC2F),
            new FieldInfo("endCap", FieldConstants.SFBOOL),
            new FieldInfo("orientation", FieldConstants.MFROTATION),
            new FieldInfo("scale", FieldConstants.MFVEC2F),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("spine", FieldConstants.MFVEC3F),
        };
        nodeName = "Extrusion";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
        };
        nodeName = "Group";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("center", FieldConstants.SFVEC3F),
            new FieldInfo("rotation", FieldConstants.SFROTATION),
            new FieldInfo("scale", FieldConstants.SFVEC3F),
            new FieldInfo("scaleOrientation", FieldConstants.SFROTATION),
            new FieldInfo("translation", FieldConstants.SFVEC3F),
        };
        nodeName = "Transform";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("matrix", FieldConstants.SFMATRIX4F),
        };
        nodeName = "MatrixTransform";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("title", FieldConstants.SFSTRING),
            new FieldInfo("info", FieldConstants.MFSTRING),
        };
        nodeName = "WorldInfo";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("whichChoice", FieldConstants.SFINT32),
        };
        nodeName = "Switch";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFVEC3F),
        };
        nodeName = "CoordinateInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFROTATION),
        };
        nodeName = "OrientationInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFVEC3F),
        };
        nodeName = "PositionInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFFLOAT),
        };
        nodeName = "ScalarInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFCOLOR),
        };
        nodeName = "ColorInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("key", FieldConstants.MFFLOAT),
            new FieldInfo("keyValue", FieldConstants.MFVEC3F),
        };
        nodeName = "NormalInterpolator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
        };
        nodeName = "KeySensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("deletionAllowed", FieldConstants.SFBOOL),
        };
        nodeName = "StringSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("ambientIntensity", FieldConstants.SFFLOAT),
            new FieldInfo("color", FieldConstants.SFCOLOR),
            new FieldInfo("intensity", FieldConstants.SFFLOAT),
            new FieldInfo("on", FieldConstants.SFBOOL),
            new FieldInfo("global", FieldConstants.SFBOOL),
            new FieldInfo("direction", FieldConstants.SFVEC3F),
        };
        nodeName = "DirectionalLight";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("ambientIntensity", FieldConstants.SFFLOAT),
            new FieldInfo("color", FieldConstants.SFCOLOR),
            new FieldInfo("intensity", FieldConstants.SFFLOAT),
            new FieldInfo("on", FieldConstants.SFBOOL),
            new FieldInfo("global", FieldConstants.SFBOOL),
            new FieldInfo("attenuation", FieldConstants.SFVEC3F),
            new FieldInfo("location", FieldConstants.SFVEC3F),
            new FieldInfo("radius", FieldConstants.SFFLOAT),
        };
        nodeName = "PointLight";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("ambientIntensity", FieldConstants.SFFLOAT),
            new FieldInfo("color", FieldConstants.SFCOLOR),
            new FieldInfo("intensity", FieldConstants.SFFLOAT),
            new FieldInfo("on", FieldConstants.SFBOOL),
            new FieldInfo("global", FieldConstants.SFBOOL),
            new FieldInfo("attenuation", FieldConstants.SFVEC3F),
            new FieldInfo("beamWidth", FieldConstants.SFFLOAT),
            new FieldInfo("cutOffAngle", FieldConstants.SFFLOAT),
            new FieldInfo("direction", FieldConstants.SFVEC3F),
            new FieldInfo("location", FieldConstants.SFVEC3F),
            new FieldInfo("radius", FieldConstants.SFFLOAT),
        };
        nodeName = "SpotLight";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("avatarSize", FieldConstants.MFFLOAT),
            new FieldInfo("headlight", FieldConstants.SFBOOL),
            new FieldInfo("speed", FieldConstants.SFFLOAT),
            new FieldInfo("type", FieldConstants.MFSTRING),
            new FieldInfo("visibilityLimit", FieldConstants.SFFLOAT),
            new FieldInfo("transitionType", FieldConstants.MFSTRING),
            new FieldInfo("transitionTime", FieldConstants.MFFLOAT),
        };
        nodeName = "NavigationInfo";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("fieldOfView", FieldConstants.SFFLOAT),
            new FieldInfo("jump", FieldConstants.SFBOOL),
            new FieldInfo("orientation", FieldConstants.SFROTATION),
            new FieldInfo("position", FieldConstants.SFVEC3F),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("centerOfRotation", FieldConstants.SFVEC3F),
            new FieldInfo("retainUserOffsets", FieldConstants.SFBOOL),
        };
        nodeName = "Viewpoint";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("axisOfRotation", FieldConstants.SFVEC3F),
        };
        nodeName = "Billboard";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("collide", FieldConstants.SFBOOL),
            new FieldInfo("proxy", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
        };
        nodeName = "Collision";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("center", FieldConstants.SFVEC3F),
            new FieldInfo("range", FieldConstants.MFFLOAT),
            new FieldInfo("forceTransitions", FieldConstants.SFBOOL),
        };
        nodeName = "LOD";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("children", FieldConstants.MFNODE),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("parameter", FieldConstants.MFSTRING),
            new FieldInfo("url", FieldConstants.MFSTRING),
        };
        nodeName = "Anchor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("url", FieldConstants.MFSTRING),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("load", FieldConstants.SFBOOL),
        };
        nodeName = "Inline";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("watchList", FieldConstants.MFNODE),
            new FieldInfo("timeOut", FieldConstants.SFTIME),
        };
        nodeName = "LoadSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("autoOffset", FieldConstants.SFBOOL),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("maxAngle", FieldConstants.SFFLOAT),
            new FieldInfo("minAngle", FieldConstants.SFFLOAT),
            new FieldInfo("diskAngle", FieldConstants.SFFLOAT),
            new FieldInfo("offset", FieldConstants.SFFLOAT),
        };
        nodeName = "CylinderSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("autoOffset", FieldConstants.SFBOOL),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("maxPosition", FieldConstants.SFVEC2F),
            new FieldInfo("minPosition", FieldConstants.SFVEC2F),
            new FieldInfo("offset", FieldConstants.SFVEC3F),
        };
        nodeName = "PlaneSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("autoOffset", FieldConstants.SFBOOL),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("offset", FieldConstants.SFROTATION),
        };
        nodeName = "SphereSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
            new FieldInfo("description", FieldConstants.SFSTRING),
        };
        nodeName = "TouchSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.MFCOLOR),
        };
        nodeName = "Color";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.MFCOLORRGBA),
        };
        nodeName = "ColorRGBA";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("point", FieldConstants.MFVEC3F),
        };
        nodeName = "Coordinate";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("colorIndex", FieldConstants.MFINT32),
            new FieldInfo("coordIndex", FieldConstants.MFINT32),
        };
        nodeName = "IndexedLineSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("vertexCount", FieldConstants.MFINT32),
        };
        nodeName = "LineSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
        };
        nodeName = "PointSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("vector", FieldConstants.MFVEC3F),
        };
        nodeName = "Normal";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("index", FieldConstants.MFINT32),
        };
        nodeName = "IndexedTriangleFanSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("index", FieldConstants.MFINT32),
        };
        nodeName = "IndexedTriangleSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("index", FieldConstants.MFINT32),
        };
        nodeName = "IndexedTriangleStripSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("fanCount", FieldConstants.MFINT32),
        };
        nodeName = "TriangleFanSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
        };
        nodeName = "TriangleSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("coord", FieldConstants.SFNODE),
            new FieldInfo("color", FieldConstants.SFNODE),
            new FieldInfo("normal", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.SFNODE),
            new FieldInfo("solid", FieldConstants.SFBOOL),
            new FieldInfo("ccw", FieldConstants.SFBOOL),
            new FieldInfo("colorPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("normalPerVertex", FieldConstants.SFBOOL),
            new FieldInfo("fogCoord", FieldConstants.SFNODE),
            new FieldInfo("attrib", FieldConstants.SFNODE),
            new FieldInfo("stripCount", FieldConstants.MFINT32),
        };
        nodeName = "TriangleStripSet";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("url", FieldConstants.MFSTRING),
            new FieldInfo("mustEvaluate", FieldConstants.SFBOOL),
            new FieldInfo("directOutput", FieldConstants.SFBOOL),
        };
        nodeName = "Script";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("material", FieldConstants.SFNODE),
            new FieldInfo("texture", FieldConstants.SFNODE),
            new FieldInfo("textureTransform", FieldConstants.SFNODE),
            new FieldInfo("lineProperties", FieldConstants.SFNODE),
            new FieldInfo("pointProperties", FieldConstants.SFNODE),
            new FieldInfo("fillProperties", FieldConstants.SFNODE),
            new FieldInfo("textureProperties", FieldConstants.SFNODE),
        };
        nodeName = "Appearance";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("ambientIntensity", FieldConstants.SFFLOAT),
            new FieldInfo("diffuseColor", FieldConstants.SFCOLOR),
            new FieldInfo("emissiveColor", FieldConstants.SFCOLOR),
            new FieldInfo("shininess", FieldConstants.SFFLOAT),
            new FieldInfo("specularColor", FieldConstants.SFCOLOR),
            new FieldInfo("transparency", FieldConstants.SFFLOAT),
        };
        nodeName = "Material";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("appearance", FieldConstants.SFNODE),
            new FieldInfo("geometry", FieldConstants.SFNODE),
            new FieldInfo("bboxSize", FieldConstants.SFVEC3F),
            new FieldInfo("bboxCenter", FieldConstants.SFVEC3F),
        };
        nodeName = "Shape";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("linewidthScaleFactor", FieldConstants.SFFLOAT),
            new FieldInfo("linetype", FieldConstants.SFINT32),
            new FieldInfo("applied", FieldConstants.SFBOOL),
        };
        nodeName = "LineProperties";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("loop", FieldConstants.SFBOOL),
            new FieldInfo("startTime", FieldConstants.SFTIME),
            new FieldInfo("stopTime", FieldConstants.SFTIME),
            new FieldInfo("pauseTime", FieldConstants.SFTIME),
            new FieldInfo("resumeTime", FieldConstants.SFTIME),
            new FieldInfo("description", FieldConstants.SFSTRING),
            new FieldInfo("pitch", FieldConstants.SFFLOAT),
            new FieldInfo("url", FieldConstants.MFSTRING),
        };
        nodeName = "AudioClip";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("direction", FieldConstants.SFVEC3F),
            new FieldInfo("intensity", FieldConstants.SFFLOAT),
            new FieldInfo("location", FieldConstants.SFVEC3F),
            new FieldInfo("maxBack", FieldConstants.SFFLOAT),
            new FieldInfo("maxFront", FieldConstants.SFFLOAT),
            new FieldInfo("minBack", FieldConstants.SFFLOAT),
            new FieldInfo("minFront", FieldConstants.SFFLOAT),
            new FieldInfo("priority", FieldConstants.SFFLOAT),
            new FieldInfo("source", FieldConstants.SFNODE),
            new FieldInfo("spatialize", FieldConstants.SFBOOL),
        };
        nodeName = "Sound";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("family", FieldConstants.MFSTRING),
            new FieldInfo("horizontal", FieldConstants.SFBOOL),
            new FieldInfo("justify", FieldConstants.MFSTRING),
            new FieldInfo("language", FieldConstants.SFSTRING),
            new FieldInfo("leftToRight", FieldConstants.SFBOOL),
            new FieldInfo("size", FieldConstants.SFFLOAT),
            new FieldInfo("spacing", FieldConstants.SFFLOAT),
            new FieldInfo("style", FieldConstants.SFSTRING),
            new FieldInfo("topToBottom", FieldConstants.SFBOOL),
        };
        nodeName = "FontStyle";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("string", FieldConstants.MFSTRING),
            new FieldInfo("fontStyle", FieldConstants.SFNODE),
            new FieldInfo("length", FieldConstants.MFFLOAT),
            new FieldInfo("maxExtent", FieldConstants.SFFLOAT),
            new FieldInfo("solid", FieldConstants.SFBOOL),
        };
        nodeName = "Text";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("repeatS", FieldConstants.SFBOOL),
            new FieldInfo("repeatT", FieldConstants.SFBOOL),
            new FieldInfo("textureProperties", FieldConstants.SFNODE),
            new FieldInfo("url", FieldConstants.MFSTRING),
        };
        nodeName = "ImageTexture";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("repeatS", FieldConstants.SFBOOL),
            new FieldInfo("repeatT", FieldConstants.SFBOOL),
            new FieldInfo("textureProperties", FieldConstants.SFNODE),
            new FieldInfo("image", FieldConstants.SFIMAGE),
        };
        nodeName = "PixelTexture";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("point", FieldConstants.MFVEC2F),
        };
        nodeName = "TextureCoordinate";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("center", FieldConstants.SFVEC2F),
            new FieldInfo("rotation", FieldConstants.SFFLOAT),
            new FieldInfo("scale", FieldConstants.SFVEC2F),
            new FieldInfo("translation", FieldConstants.SFVEC2F),
        };
        nodeName = "TextureTransform";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("mode", FieldConstants.MFSTRING),
            new FieldInfo("texture", FieldConstants.MFNODE),
            new FieldInfo("color", FieldConstants.SFCOLOR),
            new FieldInfo("alpha", FieldConstants.SFFLOAT),
            new FieldInfo("function", FieldConstants.MFSTRING),
            new FieldInfo("source", FieldConstants.MFSTRING),
        };
        nodeName = "MultiTexture";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("texCoord", FieldConstants.MFNODE),
        };
        nodeName = "MultiTextureCoordinate";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("textureTransform", FieldConstants.MFNODE),
        };
        nodeName = "MultiTextureTransform";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("mode", FieldConstants.SFSTRING),
        };
        nodeName = "TextureCoordinateGenerator";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("borderColor", FieldConstants.SFCOLORRGBA),
            new FieldInfo("borderWidth", FieldConstants.SFINT32),
            new FieldInfo("boundaryModeS", FieldConstants.SFSTRING),
            new FieldInfo("boundaryModeT", FieldConstants.SFSTRING),
            new FieldInfo("boundaryModeR", FieldConstants.SFSTRING),
            new FieldInfo("magnificationFilter", FieldConstants.SFSTRING),
            new FieldInfo("minificationFilter", FieldConstants.SFSTRING),
            new FieldInfo("generateMipMaps", FieldConstants.SFBOOL),
            new FieldInfo("anisotropicMode", FieldConstants.SFSTRING),
            new FieldInfo("anisotropicDegree", FieldConstants.SFFLOAT),
            new FieldInfo("textureCompression", FieldConstants.SFSTRING),
            new FieldInfo("texturePriority", FieldConstants.SFFLOAT),
        };
        nodeName = "TextureProperties";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("loop", FieldConstants.SFBOOL),
            new FieldInfo("startTime", FieldConstants.SFTIME),
            new FieldInfo("stopTime", FieldConstants.SFTIME),
            new FieldInfo("pauseTime", FieldConstants.SFTIME),
            new FieldInfo("resumeTime", FieldConstants.SFTIME),
            new FieldInfo("speed", FieldConstants.SFFLOAT),
            new FieldInfo("url", FieldConstants.MFSTRING),
            new FieldInfo("repeatS", FieldConstants.SFBOOL),
            new FieldInfo("repeatT", FieldConstants.SFBOOL),
            new FieldInfo("textureProperties", FieldConstants.SFNODE),
        };
        nodeName = "MovieTexture";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));

        fieldMap = new FieldInfo[] {
            new FieldInfo("metadata", FieldConstants.SFNODE),
            new FieldInfo("loop", FieldConstants.SFBOOL),
            new FieldInfo("startTime", FieldConstants.SFTIME),
            new FieldInfo("stopTime", FieldConstants.SFTIME),
            new FieldInfo("pauseTime", FieldConstants.SFTIME),
            new FieldInfo("resumeTime", FieldConstants.SFTIME),
            new FieldInfo("cycleInterval", FieldConstants.SFTIME),
            new FieldInfo("enabled", FieldConstants.SFBOOL),
        };
        nodeName = "TimeSensor";
        nodeType = xim.getTypes(nodeName);
        nodeMap.put(nodeName, new CommonEncodable(nodeName, fieldMap, nodeType));
    }
}
