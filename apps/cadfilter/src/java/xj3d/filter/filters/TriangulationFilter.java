/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.lang.reflect.InvocationTargetException;
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.BaseFilter;
import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;

/**
 * Generic triangulation filter that converts any geometry type it comes
 * across to an indexed triangle form.
 * <p>
 *
 * The output form is not always going to be an IndexedTriangleSet (though this
 * could be implemented by way of argument flags in a later revision). The
 * current implementation can process the following geometry types:
 * <p>
 * <ul>
 * <li>Box</li>
 * <li>Cone</li>
 * <li>Cylinder</li>
 * <li>Sphere</li>
 * <li>ElevationGridGeometry</li>
 * <li>ExtrusionGeometry</li>
 * <li>IndexedFaceSetGeometry</li>
 * <li>IndexedTriangleFanSetGeometry</li>
 * <li>IndexedTriangleSetGeometry</li>
 * <li>IndexedTriangleStripSetGeometry</li>
 * <li>TriangleFanSetGeometry</li>
 * <li>TriangleSetGeometry</li>
 * <li>TriangleStripSetGeometry</li>
 * </ul>
 *
 * The three basic indexed triangle nodes (Set, Strip, Fan) are passed through
 * unchanged.
 * <p>
 *
 * This filter will force an upgrade to X3D if VRML is detected. VRML Does not have
 * the IndexedTriangle* nodes that are generated as output of this filter.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public class TriangulationFilter extends BaseFilter {

    /** Error message when class can't be found */
    private static final String CREATE_MSG =
        "New node instantiation exception";

    /** Error message for constructor having non-public access type */
    private static final String ACCESS_MSG =
        "New node IllegalAccess exception";

    /** Set of geometry sub nodes that are useful to us during processing */
    private static Set<String> usableGeometryChildren;

    /** Mapping of geometry node types to their geometry holder class */
    private Map<String, TriangulationGeometry> geometryMap;

    /** List of all node types we've tried but failed to find */
    private Set<String> ignoredNodes;

    /** Are we inside an Geometry processing currently */
    private boolean insideGeometry;

    /** Also inside an Geometry field that uses a node (eg Coordinate) */
    private boolean insideGeometrySubNode;

    /** Set to true when we have the nested multitexture node. */
    private boolean insideMultiTexture;

    /** The current geometry representative object being processed */
    private TriangulationGeometry geometryData;

    /** Set of DEF names to ignore when we come to USE decls */
    private Set<String> ignoreDefSet;

    /** Counter when we ignore a node type we don't know about */
    private int ignoreNodeCounter;

    /**
     * Counts which mutlitexture Texture node we're at when processing
     * the MultiTexture children. Used so that the child filters know
     * how to differentiate between the various sets.
     */
    private int multitextureCount;

    /** Set of arguments passed to this filter */
    private String[] filterArgs;

    /** A stack of node wrappers */
    private SimpleStack encStack;

    /** Node wrapper factory */
    private EncodableFactory factory;

    /** Map of def'ed nodes keyed by DEF Name */
    private Map<String, Encodable> defMap;

    /** Scene instance */
    private Scene scene;

    /**
     * Static initialization of globals used by all instances.
     */
    static {
        usableGeometryChildren = new HashSet<>();

        usableGeometryChildren.add("FogCoordinate");
        usableGeometryChildren.add("Normal");
        usableGeometryChildren.add("TextureCoordinate");
        usableGeometryChildren.add("Color");
        usableGeometryChildren.add("ColorRGBA");
        usableGeometryChildren.add("Coordinate");
        usableGeometryChildren.add("MultiTextureCoordinate");
        usableGeometryChildren.add("TextureCoordinate");
        usableGeometryChildren.add("TextureCoordinateGenerator");
        usableGeometryChildren.add("FloatVertexAttribute");
        usableGeometryChildren.add("Matrix3VertexAttribute");
        usableGeometryChildren.add("Matrix4VertexAttribute");
    }

    /**
     * Create a new default filter for the conversion
     */
    public TriangulationFilter() {

        encStack = new SimpleStack();
        defMap = new HashMap<>();

        geometryMap = new HashMap<>();
        ignoredNodes = new HashSet<>();
        ignoreDefSet = new HashSet<>();

        insideGeometry = false;
        insideGeometrySubNode = false;
        insideMultiTexture = false;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        majorVersion = 3;
        minorVersion = 0;

        if(type.charAt(1) == 'V') {
            // we're in VRML model either 97 or 1.0.
            // Look at the 6th character to see the version number
            // ie "VRML V1.0" or "VRML V2.0"
            // If it is, then we need to force an upgrade to X3D for this
            // output to work. Arbitrarily set the profile to Immersive as that
            // is the closest to VRML spec.
            boolean is_20 = (version.charAt(1) == '2');

            if(is_20) {
                type = "#X3D";
                version = "3.0";
            }

            super.startDocument(uri, url, encoding, type, version, comment);
            super.profileDecl("Immersive");

        } else {
            // Parse the number string looking for the version minor number.
            int dot_index = version.indexOf('.');
            String minor_num = version.substring(dot_index + 1);

            // Should this look for a badly formatted number here or just
            // assume the parsing beforehad has correctly identified something
            // already dodgy?
            minorVersion = Integer.parseInt(minor_num);

            super.startDocument(uri, url, encoding, type, version, comment);

        }

        ignoreNodeCounter = 0;

        factory = new EncodableFactory(contentHandler, fieldReader);

        scene = (Scene)factory.getEncodable("Scene", null);
        encStack.push(scene);
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        super.endDocument();
        encStack.clear();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        // If one of our parent nodes is something we don't understand then we
        // need to ignore it. We do this as a first thing and don't even
        // attempt to process this child node. If we did, we would run into all
        // sorts of problems, such as having nested ignores. Basically, once we
        // start to ignore it, _everything_ is ignored until we get back to the
        // same level and we start processing the next sibling node.

        if(ignoreNodeCounter > 0) {
            // Don't increment unless we are already ignoring nodes
            ignoreNodeCounter++;

            if(defName != null) {
                ignoreDefSet.add(defName);
            }

            return;
        }

        Encodable enc = factory.getEncodable(name, defName);

        NodeMarker marker = (NodeMarker)nodeStack.peek();
        String fieldName = marker.fieldName;

        Encodable parent = (Encodable)encStack.peek();
        if (parent != null && fieldName != null) {
            parent.setValue(fieldName, enc);
        }
        encStack.push(enc);

        if (defName != null) {
            defMap.put(defName, enc);
        }

        // If we're already inside the geometry, we should look for the
        // real data. If not, then check to see if this starts geometry or
        // a node we're looking for. If not, then pass the node definition on
        // directly.

        // NOTE:
        // Does not handle proto versions of these correctly

        if (insideGeometry) {
            // Inside geometry means it is either a metadata node or
            // coordinate/normal/texCoord etc.
            // Next test: Are we in a geometry sub node already? If so, then
            // this is probably a multitexture. So special case that one first
            // and then go from there with other possible child nodes.
            if(usableGeometryChildren.contains(name)) {
                if(insideGeometrySubNode) {
                    insideMultiTexture = true;
                    multitextureCount++;
                } else {
                    insideGeometrySubNode = true;
                    multitextureCount = 0;
                }

            } else {
                insideGeometrySubNode = false;
                ignoreNodeCounter = 1;
            }

        } else {
            TriangulationGeometry geom = loadGeomHolder(name);

            if (geom != null) {
                geometryData = geom;
                geometryData.setDefName(defName);
                insideGeometry = true;
                suppressCalls(true);
            }
        }

        super.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0) {
            ignoreNodeCounter--;
            return;
        }

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        // call super before suppressCalls() is possibly called
        // so that the stacks are managed before the suppress
        // state changes
        super.endNode();

        if (geometryMap.containsKey(marker.nodeName)) {

            insideGeometry = false;
            suppressCalls(false);

            geometryData.generateOutput(contentHandler,
                                        scriptHandler,
                                        protoHandler,
                                        routeHandler);
            geometryData = null;
        } else if (insideGeometrySubNode) {
            if (insideMultiTexture) {
                insideMultiTexture = false;
            } else {
                insideGeometrySubNode = false;
            }
        }

        Encodable enc = (Encodable)encStack.pop();
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {
        // If ignoring this node, forget about any processing
        if(ignoreNodeCounter > 0)
            return;

        super.startField(name);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        // If ignoring this node, forget about any processing
        if((ignoreNodeCounter > 0) || ignoreDefSet.contains(defName))
            return;

        if (insideGeometry) {

            Encodable use = defMap.get(defName);

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {

                NodeMarker marker = (NodeMarker)nodeStack.peek();
                String fieldName = marker.fieldName;

                // don't bother cloning the node or setting it's USE name,
                // everything we're interested in (so far) is going to be
                // copied and re-arranged in the combine() operation.
                enc.setValue(fieldName, use);

                switch (fieldName) {
                    case "coord":{
                        Coordinate c = (Coordinate) use;
                        geometryData.addFieldValue("Coordinate", c.point, c.num_point);
                        break;
                        }
                    case "texCoord":
                        TextureCoordinate tc = (TextureCoordinate) use;
                        geometryData.addFieldValue("TextureCoordinate", tc.point, tc.num_point);
                        break;
                    case "color":{
                        Color c = (Color) use;
                        geometryData.addFieldValue("Color", c.color, c.num_color);
                        break;
                        }
                    case "normal":
                        Normal n = (Normal) use;
                        geometryData.addFieldValue("Normal", n.vector, n.num_vector);
                        break;
                }
            }
        }
        super.useDecl(defName);
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endField() throws SAVException, VRMLException {
        super.endField();
    }

    //-----------------------------------------------------------------------
    // Methods defined by StringContentHandler
    //-----------------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String value) throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0) {
            return;
        }

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if(insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, values);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, values);
        }
        super.fieldValue(values);
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value, len);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value, len);
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value, len);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value, len);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value, len);
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value, len);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value, len);
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value, len);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value,len);
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {

        if(ignoreNodeCounter > 0)
            return;

        NodeMarker marker = (NodeMarker)nodeStack.peek();

        if(insideGeometry) {

            // In this case, use the node name as the "field" name for
            // passing through to the triangulation utils.
            String field_name = null;

            if (insideGeometrySubNode) {
                field_name = marker.nodeName;

                // Override the field name in the multitexture case so
                // that we don't trash field values from each set of
                // coordinates.
                if(insideMultiTexture) {
                    field_name += multitextureCount;
                }
            } else {
                field_name = marker.fieldName;
            }

            geometryData.addFieldValue(field_name, value);
        }

        Encodable enc = (Encodable)encStack.peek();
        if (enc != null) {
            enc.setValue(marker.fieldName, value);
        }
        super.fieldValue(value, len);
    }

    //---------------------------------------------------------------
    // Methods defined by RouteHandler
    //---------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNodeName The name of the DEF of the source node
     * @param srcFieldName The name of the field to route values from
     * @param destNodeName The name of the DEF of the destination node
     * @param destFieldName The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void routeDecl(String srcNodeName,
                          String srcFieldName,
                          String destNodeName,
                          String destFieldName)
        throws SAVException, VRMLException {

        if(ignoreDefSet.contains(srcNodeName) ||
           ignoreDefSet.contains(destNodeName))
            return;

        if(routeHandler != null)
            routeHandler.routeDecl(srcNodeName,
                                   srcFieldName,
                                   destNodeName,
                                   destFieldName);
    }

    //---------------------------------------------------------------
    // Methods defined by AbstractFilter
    //---------------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {
        filterArgs = args;
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Load a filter class for the geometry type. Follows the basic form of
     * xj3d.filter.filters.<i>Node</i>Geometry.
     *
     * @param nodeName The name of the node to load the geometry class for
     * @return The matching geometry representation class
     */
    private TriangulationGeometry loadGeomHolder(String nodeName) {
        TriangulationGeometry ret_val = null;

        ret_val = geometryMap.get(nodeName);

        if(ret_val != null) {
            ret_val.reset();
            return ret_val;
        } else if(ignoredNodes.contains(nodeName)) {
            return null;
        }

        try {
            String classname = "xj3d.filter.filters." + nodeName + "Geometry";

            Class<?> cls = Class.forName(classname);
            ret_val = (TriangulationGeometry)cls.getDeclaredConstructor().newInstance();
            ret_val.setErrorReporter(errorHandler);
            ret_val.setFieldReader(fieldReader);
            ret_val.setArguments(filterArgs);

            geometryMap.put(nodeName, ret_val);

        } catch (ClassNotFoundException cnfe) {
            // ignore
        } catch(InstantiationException | NoSuchMethodException | InvocationTargetException ie) {
            errorHandler.errorReport(CREATE_MSG, ie);
        } catch(IllegalAccessException iae) {
            errorHandler.errorReport(ACCESS_MSG, iae);
        }

        if(ret_val == null)
            ignoredNodes.add(nodeName);

        return ret_val;
    }
}
