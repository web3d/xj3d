/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2009-2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer.collada;

// External imports
import java.io.IOException;

import java.net.URL;

import java.util.*;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import org.j3d.util.ErrorReporter;
import org.web3d.util.I18nUtils;

import xj3d.filter.FieldValueHandler;
import xj3d.filter.FilterExitCodes;
import xj3d.filter.FilterProcessingException;
import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser that reads Collada files and generates an X3D stream
 * of events.
 *
 * Output Styles:
 *
 *    UNCOLORED - Color information is stripped
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class ColladaFileParser implements NonWeb3DFileParser {

    /** Identifier */
    private static final String LOG_NAME = "ColladaFileParser";

    // Should the scale be applied directly the coordinates and translations
    // Will not change MATRIX specified forms
    //private boolean APPLY_SCALE = false;

    /** The 'supported' set of node content */
    private static final String[] INSTANCE = new String[] {
        ColladaStrings.INSTANCE_CAMERA,
        //ColladaStrings.INSTANCE_CONTROLLER,
        ColladaStrings.INSTANCE_GEOMETRY,
        //ColladaStrings.INSTANCE_LIGHT,
        ColladaStrings.INSTANCE_NODE,
        ColladaStrings.NODE,
    };

    /** The 'supported' set of node transforms */
    private static final String[] TRANSFORM = new String[] {
        ColladaStrings.TRANSLATE,
        ColladaStrings.ROTATE,
        ColladaStrings.SCALE,
        ColladaStrings.SKEW,
        ColladaStrings.MATRIX,
    };

    /** The 'supported' set of materials */
    private static final String[] MATERIAL = new String[] {
        ColladaStrings.BLINN,
        ColladaStrings.CONSTANT,
        ColladaStrings.LAMBERT,
        ColladaStrings.PHONG,
    };

    /** The Document Element */
    private CElement doc_element;

    /** Flag indicating that the content handler is an instance of a
    *  BinaryContentHandler, rather than a StringContentHandler */
    private boolean handlerIsBinary;

    /** Binary Content Handler reference */
    private BinaryContentHandler bch;

    /** String Content Handler reference */
    private StringContentHandler sch;

    /** The url of the current document */
    private String documentURL;

    /** Reference to the registered content handler if we have one */
    private ContentHandler contentHandler;

    /** Reference to the registered route handler if we have one */
    private RouteHandler routeHandler;

    /** Reference to the registered script handler if we have one */
    private ScriptHandler scriptHandler;

    /** Reference to the registered proto handler if we have one */
    private ProtoHandler protoHandler;

    /** Reference to the registered error handler if we have one */
    private ErrorReporter errorHandler;

    /** Reference to our Locator instance to hand to users */
    private Locator locator;

    /** Scratch vecmath objects used to calculate Transform fields */
    private Matrix4f tmpMatrix0;
    private AxisAngle4f tmpAxisAngle;
    private Vector3f tmpVector;

    // Global scale for units conversion
    private float scale = 1.0f;

    /** The node IDs that have been DEF'ed */
    private List<String> nodeInstanceList;

    /** The source IDs that have been DEF'ed */
    private List<String> sourceInstanceList;

    /** A map of DEF'ed effect IDs to DEF'ed image IDs */
    private Map<String, String> effectToTextureMap;

    /** The transform field IDs that have been DEF'ed */
    private Map<String, String> defedFieldMap;

    /** How to style our output.  Supports UNCOLORED, MATRIX_TRANSFORM or null for none */
    private HashSet<String> style;

    /** Used to format numbers for printing in X3D fields */
    //private NumberFormat numberFormater;

    /**
     * Constructor
     */
    public ColladaFileParser() {
        tmpMatrix0 = new Matrix4f();
        tmpAxisAngle = new AxisAngle4f();
        tmpVector = new Vector3f();

        nodeInstanceList = new ArrayList<>();
        sourceInstanceList = new ArrayList<>();
        effectToTextureMap = new HashMap<>();
        defedFieldMap = new HashMap<>();

        //numberFormater = NumberFormat.getNumberInstance();
        //numberFormater.setGroupingUsed(false);
    }

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    @Override
    public void initialize() {
        // Ignored for this implementation.
    }

    /**
     * Set the base URL of the document that is about to be parsed. Users
     * should always call this to make sure we have correct behaviour for the
     * ContentHandler's <code>startDocument()</code> call.
     * <p>
     * The URL is cleared at the end of each document run. Therefore it is
     * imperative that it get's called each time you use the parser.
     *
     * @param url The document url to set
     */
    @Override
    public void setDocumentUrl(String url) {
        documentURL = url;
    }

    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    @Override
    public Locator getDocumentLocator() {
        return locator;
    }

    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    @Override
    public void setContentHandler(ContentHandler ch) {
        contentHandler = ch;
        if (contentHandler instanceof BinaryContentHandler) {
            bch = (BinaryContentHandler)contentHandler;
            sch = null;
            handlerIsBinary = true;
        } else if (contentHandler instanceof StringContentHandler) {
            bch = null;
            sch = (StringContentHandler)contentHandler;
            handlerIsBinary = false;
        }
        // otherwise - we don't know how to deal with the content handler
    }

    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    @Override
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
    }

    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    @Override
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
    }

    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    @Override
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
    }

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorHandler(ErrorHandler eh) {
        errorHandler = eh;

        if(eh != null)
            eh.setDocumentLocator(getDocumentLocator());
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    @Override
    public void setErrorReporter(ErrorReporter eh) {
        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorHandler = eh;
    }

    /**
     * Parse the input now.
     *
     * @param input The stream to read from
     * @param style The style or null or no styling
     * @return True if no parsing issue, False if recovered.
     * @throws IOException An I/O error while reading the stream
     * @throws ImportFileFormatException A parsing error occurred in the file
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, ImportFileFormatException {

        this.style = new HashSet<>();
        if (style != null) {
            for(String s : style) {
                this.style.add(s);
            }
        }

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some to deal with this.
        URL url = new URL(input.getURL());

        // acquire the contents of the document
        ColladaReader cr = null;
        try {
            cr = new ColladaReader();
            cr.parse(new org.xml.sax.InputSource(input.getByteStream()));
        } catch (IOException ioe) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                ColladaParserConstants.LOG_NAME + ": IOException reading: "+ url);

            iffe.setStackTrace(ioe.getStackTrace());
            throw iffe;
        } catch (SAXException se) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                ColladaParserConstants.LOG_NAME + ": SAXException reading: "+ url);

            iffe.setStackTrace(se.getStackTrace());
            throw iffe;
        }
        // get the libraries
        doc_element = cr.getResult();

        contentHandler.startDocument(input.getURL(),
            input.getBaseURL(),
            "utf8",
            "#X3D",
            "V3.3",
            "Collada file conversion");

        contentHandler.profileDecl("Interchange");

        if (this.style.contains("MATRIX_TRANSFORM")) {
            contentHandler.componentDecl("EXT_Grouping:1");
        }
        contentHandler.startNode("Transform", "COLLADA_UNITS");

        // process asset information that affects the scene
        CElement asset_element =
            doc_element.getFirstElementByTagName(ColladaStrings.ASSET);
        CElement unit_element = null;
        CElement axis_element = null;

        if (asset_element != null) {
            unit_element = asset_element.getFirstElementByTagName(ColladaStrings.UNIT);
            axis_element = asset_element.getFirstElementByTagName(ColladaStrings.UP_AXIS);
        }

        if (unit_element != null) {
            // process the global scale setting
            String meter_attr = unit_element.getAttribute(ColladaStrings.METER);
            if (meter_attr != null) {
                scale = ColladaParserUtils.getFloatValue(unit_element, ColladaStrings.METER);
                //if (scale != 1 && !APPLY_SCALE) {
                if (scale != 1) {
                    contentHandler.startField("scale");
                    float[] s = new float[]{scale, scale, scale};
                    if (handlerIsBinary) {
                        bch.fieldValue(s, s.length);
                    } else {
                        sch.fieldValue(FieldValueHandler.toString(s));
                    }
                }
            }
        }
        if (axis_element != null) {
            // process the global orientation
            String up_axis = axis_element.getTextContent();
            switch (up_axis) {
                case ColladaStrings.X_UP:
                    contentHandler.startField("rotation");
                    if (handlerIsBinary) {
                        bch.fieldValue(new float[]{ 0, 0, 1, 1.570796f }, 4);
                    } else {
                        sch.fieldValue("0 0 1 1.570796");
                    }   break;
            // else - do nothing if Y_UP
                case ColladaStrings.Z_UP:
                    contentHandler.startField("rotation");
                    if (handlerIsBinary) {
                        bch.fieldValue(new float[]{ -1, 0, 0, 1.570796f }, 4);
                    } else {
                        sch.fieldValue("-1 0 0 1.570796");
                    }   break;
            }
        }

        contentHandler.startField("children");

        // get the nodes from the scene and process them.
        List<CElement> rootNodes = getColladaNodesFromScene();
        if (rootNodes != null) {
            for (CElement rootNode : rootNodes) {
                processNode(rootNode);
            }
        }

        contentHandler.endField();  // children
        contentHandler.endNode();   // Transform

        ////////////////////////////////////////////////////////////////////////////////
        // animation handling
        CElement animation_lib_element =
            doc_element.getFirstElementByTagName(ColladaStrings.LIBRARY_ANIMATIONS);
        if (animation_lib_element != null) {
            List<CElement> animation_element_list =
                animation_lib_element.getElementsByTagName(ColladaStrings.ANIMATION);
            for (CElement animation_element_list1 : animation_element_list) {
                processAnimation(animation_element_list1);
            }
        }

        // release references to any objects created from parsing the file
        doc_element.clearContent();
        doc_element = null;

        contentHandler.endDocument();

        return null;
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Return the Collada Nodes from the Scene in the argument Document.
     *
     * @return the Collada Nodes from the Document, or null if they could not
     * be found for any reason.
     */
    private List<CElement> getColladaNodesFromScene() {
        List<CElement> root_nodes = null;
        CElement scene_element =
            doc_element.getFirstElementByTagName(ColladaStrings.SCENE);
        if (scene_element != null) {
            CElement ivs_element =
                scene_element.getFirstElementByTagName(ColladaStrings.INSTANCE_VISUAL_SCENE);
            if (ivs_element != null) {
                String vs_url = ivs_element.getAttribute(ColladaStrings.URL);
                String vs_url_id = getElementId(vs_url);
                // find the visual scene, identified by it's url
                CElement vs_element =
                    getResourceElement(ColladaStrings.VISUAL_SCENE, vs_url_id);
                if (vs_element != null) {
                    root_nodes = vs_element.getElementsByTagName(ColladaStrings.NODE);
                }
            }
        }
        return(root_nodes);
    }

    /**
     * Create the X3D for the argument Collada Node
     *
     * @param node The Collada node element
     */
    private void processNode(CElement node) {

        List<CElement> instance_list =
            new ArrayList<>(node.getElements());
        cull(instance_list, INSTANCE);
        int num_instance = instance_list.size();
        if (num_instance >= 0) {

            String id = node.getAttribute(ColladaStrings.ID);
            if (id != null) {
                nodeInstanceList.add(id);
            }

            // the node contains supported instance_*, or node(s), get the
            // the transform elements and set up the grouping node(s).
            List<CElement> transform_list =
                new ArrayList<>(node.getElements());
            cull(transform_list, TRANSFORM);

            contentHandler.startNode("Transform", id);
            contentHandler.startField("children");
            if ( !transform_list.isEmpty() ) {
                processTransformElements(transform_list, id);
            }

            // add each of the instance elements
            for (int i = 0; i < num_instance; i++) {
                CElement element = instance_list.get(i);
                String tagName = element.getTagName();

                if (tagName.equals(ColladaStrings.INSTANCE_CAMERA)) {
                    processCameraInstance(element);
                }
                if (tagName.equals(ColladaStrings.INSTANCE_GEOMETRY)) {
                    processGeometryInstance(element);
                }
                if (tagName.equals(ColladaStrings.INSTANCE_NODE)) {
                    processNodeInstance(element);
                }
                if (tagName.equals(ColladaStrings.NODE)) {
                    processNode(element);
                }
            }

            for (CElement transform_list1 : transform_list) {
                // terminate the transform element hierarchy
                contentHandler.endField();  // children
                contentHandler.endNode();   // Transform
            }

            // fini, terminate the grouping node
            contentHandler.endField();  // children
            contentHandler.endNode();   // Transform
        }
    }

    /**
     * Add a node for the specified instance
     *
     * @param node A Collada node_instance
     */
    private void processNodeInstance(CElement node) {
        String url = node.getAttribute(ColladaStrings.URL);
        String url_id = getElementId(url);
        if (nodeInstanceList.contains(url_id)) {
            // an instance of this node already exists, USE it
            contentHandler.startNode("Transform", null);
            contentHandler.startField("children");
            contentHandler.useDecl(url_id);
            contentHandler.endNode();   // Transform
        } else {
            // otherwise, find the node resource, identified by it's url
            // and DEF it
            CElement node_element =
                getResourceElement(ColladaStrings.NODE, url_id);
            if (node_element != null) {
                processNode(node_element);
            }
        }
    }

    /**
     * Create the X3D Transform node's transformational fields.
     *
     * @param transform_list The list of transformational elements from the Collada node.
     */
    private void processTransformElements(
        List<CElement> transform_list,
        String node_id) {

        boolean use_matrix_transform = false;


        if (style != null && style.contains("MATRIX_TRANSFORM")) {
            use_matrix_transform = true;
        }

        int num_transforms = transform_list.size();
        if (num_transforms != 0) {
            TransformElement[] te =
                TransformUtils.getTransformElements(transform_list);
            for (int i = 0; i < num_transforms; i++) {
                TransformElement t = te[i];
                String defName = t.sid;
                String fieldName = t.x3d_field_name;
                if ((fieldName != null) && (defName != null)) {
                    defName = node_id +"/"+ defName;
                    defedFieldMap.put(defName, fieldName);
                }


                if (fieldName != null || !use_matrix_transform) {
                    contentHandler.startNode("Transform", defName);

                    if (fieldName != null) {
                        /*
                        if (fieldName.equals("translation") && APPLY_SCALE) {
                            if (handlerIsBinary) {
                                t.value[0] =  t.value[0] * scale;
                                t.value[1] =  t.value[1] * scale;
                                t.value[2] =  t.value[2] * scale;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for(int j=0; j < t.value.length; j++) {
                                    sb.append(numberFormater.format(t.value[j]));
                                    sb.append(" ");
                                }

                                t.content = new String[] {sb.toString()};
                            }
                        }
                        */
                        // for transform elements that have a direct x3d match,
                        // fill in the field value
                        contentHandler.startField(fieldName);
                        if (handlerIsBinary) {
                            bch.fieldValue(t.value, t.value.length);
                        } else {
                            sch.fieldValue(FieldValueHandler.toString(t.value));
                        }

                    } else {
                        // for transform elements that don't have a direct x3d match
                        // i.e. matrix, skew, lookat - extract values from their matrix
                        //////////////////////////////////////////////////////////////////////////////
                        // extract the rotation
                        t.getMatrix(tmpMatrix0);
                        tmpAxisAngle.set(tmpMatrix0);
                        if (tmpAxisAngle.angle != 0) {
                            contentHandler.startField("rotation");
                            float[] value = new float[4];
                            tmpAxisAngle.get(value);
                            if (handlerIsBinary) {
                                bch.fieldValue(value, 4);
                            } else {
                                sch.fieldValue(value[0] +" "+ value[1] +" "+ value[2] +" "+ value[3]);
                            }
                        }
                        //////////////////////////////////////////////////////////////////////////////
                        // extract the translation
                        tmpMatrix0.get(tmpVector);
                        float[] value = new float[3];
                        tmpVector.get(value);
                        contentHandler.startField("translation");
                        if (handlerIsBinary) {
                            bch.fieldValue(value, 3);
                        } else {
                            sch.fieldValue(value[0] +" "+ value[1] +" "+ value[2]);
                        }
                        //////////////////////////////////////////////////////////////////////////////
                        // extract the scale
                        // Matrix4f doesn't seem to have a get scale function that returns a
                        //   Vector3f, so get matrix values directly
                        value = new float[] {tmpMatrix0.m00, tmpMatrix0.m11, tmpMatrix0.m22};
                        contentHandler.startField("scale");
                        if (handlerIsBinary) {
                            bch.fieldValue(value, 3);
                        } else {
                            sch.fieldValue(value[0] +" "+ value[1] +" "+ value[2]);
                        }
                        //////////////////////////////////////////////////////////////////////////////
                    }
                    contentHandler.startField("children");
                } else {
                    contentHandler.startNode("MatrixTransform", defName);
                    contentHandler.startField("matrix");

                    // convert to X3D matrix row major
                    float[] rm = new float[16];
                    for(int r=0; r < 4; r++) {
                        for(int c=0; c < 4; c++) {
                            rm[c*4+r] = t.value[r*4+c];
                        }
                    }
                    if (handlerIsBinary) {
                        bch.fieldValue(rm, rm.length);
                    } else {
                        sch.fieldValue(FieldValueHandler.toString(rm));
                    }
                    contentHandler.startField("children");
                }
            }
        }
    }

    /**
     * Add Shape nodes for the specified geometry to the content handler
     *
     * @param node A Collada geometry_instance
     */
    private void processGeometryInstance(CElement node) {
        CElement bind_material_element =
            node.getFirstElementByTagName(ColladaStrings.BIND_MATERIAL);
        BindMaterial materialSource = new BindMaterial(bind_material_element);

        String url = node.getAttribute(ColladaStrings.URL);
        String url_id = getElementId(url);

        // find the geometry resource, identified by it's url
        CElement geometry_element =
            getResourceElement(ColladaStrings.GEOMETRY, url_id);
        if (geometry_element != null) {
            CElement mesh_element =
                geometry_element.getFirstElementByTagName(ColladaStrings.MESH);
            if (mesh_element != null) {
                List<CElement> source_element_list =
                    mesh_element.getElementsByTagName(ColladaStrings.SOURCE);
                Map<String, Source> sourceMap = Source.getSourceMap(source_element_list);
                CElement vertices_element =
                    mesh_element.getFirstElementByTagName(ColladaStrings.VERTICES);

                List<CElement> geom_list = new ArrayList<>();

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.TRIANGLES, geom_list);
                if (geom_list.size() > 0) {
                    processTriangles(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.TRIFANS, geom_list);
                if (geom_list.size() > 0) {
                    processTrifans(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.TRISTRIPS, geom_list);
                if (geom_list.size() > 0) {
                    processTristrips(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.LINES, geom_list);
                if (geom_list.size() > 0) {
                    processLines(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.LINESTRIPS, geom_list);
                if (geom_list.size() > 0) {
                    processLinestrips(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.POLYLIST, geom_list);
                if (geom_list.size() > 0) {
                    processPolylist(geom_list, sourceMap, vertices_element, materialSource);
                }

                geom_list = mesh_element.getElementsByTagName(ColladaStrings.POLYGONS, geom_list);
                if (geom_list.size() > 0) {
                    processPolygons(geom_list, sourceMap, vertices_element, materialSource);
                }
            }
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-triangles
     *
     * @param triangle_list A NodeList containing the triangles Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTriangles(
        List<CElement> triangle_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement triangle_list1 : triangle_list) {
            contentHandler.startNode("Shape", null);
            CElement triangle_element = triangle_list1;
            int num_triangles = ColladaParserUtils.getIntValue(
                    triangle_element, ColladaStrings.COUNT);
            // the inputs from the triangles element
            List<CElement> input_list =
                    triangle_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            // the inputs from the vertices element
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            boolean need_another_index = false;
            // normal, texture, and color inputs can be found as a child of the geometry
            // element or as a child of the vertices element
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            // TODO: Support multi-set texture coordinates (ex: semantic_texcoord_multi_set.dae)

            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            if (texCoord_input == null) {
                texCoord_input = Input.getInput(v_input, ColladaStrings.TEXCOORD);
            }
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            Input color_input = Input.getInput(t_input, ColladaStrings.SEMANTIC_COLOR);
            if (color_input == null) {
                color_input = Input.getInput(v_input, ColladaStrings.SEMANTIC_COLOR);
            }
            if (color_input != null) {
                need_another_index |= (color_input.offset != vertex_offset);
            }
            CElement p_element =
                    triangle_element.getFirstElementByTagName(ColladaStrings.P);
            int[] p_indices = ((IntContent)p_element).getIntContent();
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            if (vertexSource == null) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSource", I18nUtils.CRIT_MSG, new String[] {position_source_id});
                throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            // get the color coordinates
            Source colorSource = null;
            if ( color_input != null ) {
                String color_source_id = getElementId(color_input.source);
                colorSource = sourceMap.get(color_source_id);
            }
            if (need_another_index) {
                // there is not a single index - switch to an IndexedFaceSet

                int[] v_index = Indexer.getPolyIndices(
                        p_indices, num_triangles, vertex_offset, num_offsets);
                
                int[] n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        n_index = Indexer.getPolyIndices(
                                p_indices, num_triangles, normal_offset, num_offsets);
                        
                        n_index = validateIndices(n_index, normalSource, 3, "Normal");
                    }
                }

                int[] tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        tc_index = Indexer.getPolyIndices(
                                p_indices, num_triangles, texCoord_offset, num_offsets);
                        
                        tc_index = validateIndices(tc_index, texCoordSource, 2, "TextureCoordinate");

                    }
                }

                int[] color_index = null;
                if (colorSource != null) {
                    int color_offset = color_input.offset;
                    if (color_offset != vertex_offset) {
                        // colors have indices distinct from the vertices
                        color_index = Indexer.getPolyIndices(
                                p_indices, num_triangles, color_offset, num_offsets);
                        
                        color_index = validateIndices(color_index, colorSource, 3, "Color");

                    }
                }

                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index, colorSource, color_index);

            } else {
                contentHandler.startField("geometry");
                contentHandler.startNode("IndexedTriangleSet", null);
                contentHandler.startField("index");

                int[] vertex_indices = Indexer.getTrianglesIndices(
                        p_indices, num_triangles, vertex_offset, num_offsets);
                
                vertex_indices = validateIndices(vertex_indices, vertexSource, 3, "Coordinate");

                if (handlerIsBinary) {
                    bch.fieldValue(vertex_indices, vertex_indices.length);
                } else {
                    sch.fieldValue(FieldValueHandler.toString(vertex_indices));
                }

                buildCoordField(vertexSource);

                if ( normalSource != null ) {
                    buildNormalField(normalSource);
                }

                if ( texCoordSource != null ) {
                    buildTexCoordField(texCoordSource);
                }

                if ( colorSource != null ) {
                    buildColorField(colorSource);
                }

                contentHandler.endNode();   // IndexedTriangleSet
                //contentHandler.endField();  // geometry
            }
            String material = triangle_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-trifans
     *
     * @param trifans_list A NodeList containing the trifans Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTrifans(
        List<CElement> trifans_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement trifans_list1 : trifans_list) {
            contentHandler.startNode("Shape", null);
            CElement trifans_element = trifans_list1;
            int num_trifans = ColladaParserUtils.getIntValue(
                    trifans_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    trifans_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            input_list = vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            boolean need_another_index = false;
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            List<CElement> p_list =
                    trifans_element.getElementsByTagName(ColladaStrings.P);
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            if (vertexSource == null) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSource", I18nUtils.CRIT_MSG, new String[] {position_source_id});
                throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            ///////////////////////////////////////////////////////////////////////////
            // xj3d's indexed trifan handling seems problematic with some test cases
            // forcing to index face sets for now.
            //if (need_another_index) {
            if (true) {
                ///////////////////////////////////////////////////////////////////////////
                // there is not a single index - switch to an IndexedFaceSet

                int num_indices = 0;
                int[][] poly_indices = new int[num_trifans][];
                for (int j = 0; j < num_trifans; j++) {
                    CElement p_element = p_list.get(j);
                    int[] p_indices = ((IntContent)p_element).getIntContent();
                    poly_indices[j] = Indexer.getPolyIndicesForTrifan(
                            p_indices, vertex_offset, num_offsets);
                    num_indices += poly_indices[j].length;
                }
                int[] v_index = FieldValueHandler.flatten(poly_indices, num_indices);

                int[] n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        num_indices = 0;
                        for (int j = 0; j < num_trifans; j++) {
                            CElement p_element = p_list.get(j);
                            int[] p_indices = ((IntContent)p_element).getIntContent();
                            poly_indices[j] = Indexer.getPolyIndicesForTrifan(
                                    p_indices, normal_offset, num_offsets);
                            num_indices += poly_indices[j].length;
                        }
                        n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }

                int[] tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        num_indices = 0;
                        for (int j = 0; j < num_trifans; j++) {
                            CElement p_element = p_list.get(j);
                            int[] p_indices = ((IntContent)p_element).getIntContent();
                            poly_indices[j] = Indexer.getPolyIndicesForTrifan(
                                    p_indices, texCoord_offset, num_offsets);
                            num_indices += poly_indices[j].length;
                        }
                        tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }

                // get the color coordinates
                Source colorSource = null;
                int[] color_index = null;

                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index, colorSource, color_index);

            } /*else {
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedTriangleFanSet", null);
            contentHandler.startField("index");
            
            int num_indices = 0;
            int[][] trifans_indices = new int[num_trifans][];
            for (int j = 0; j < num_trifans; j++) {
            CElement p_element = p_list.get(j);
            int[] p_indices = ((IntContent)p_element).getIntContent();
            trifans_indices[j] = Indexer.getIndices(
            p_indices, vertex_offset, num_offsets);
            num_indices += trifans_indices[j].length;
            }
            int[] vertex_indices =
            FieldValueHandler.flatten(trifans_indices, num_indices);
            
            if (handlerIsBinary) {
            bch.fieldValue(vertex_indices, vertex_indices.length);
            } else {
            sch.fieldValue(FieldValueHandler.toString(vertex_indices));
            }
            
            buildCoordField(vertexSource);
            
            if ( normalSource != null ) {
            buildNormalField(normalSource);
            }
            
            if ( texCoordSource != null ) {
            buildTexCoordField(texCoordSource);
            }
            
            contentHandler.endNode();   // IndexedTriangleFanSet
            //contentHandler.endField();  // geometry
            }*/
            String material = trifans_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-tristrips
     *
     * @param tristrips_list A NodeList containing the tristrips Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processTristrips(
        List<CElement> tristrips_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement tristrips_list1 : tristrips_list) {
            contentHandler.startNode("Shape", null);
            CElement tristrips_element = tristrips_list1;
            int num_tristrips = ColladaParserUtils.getIntValue(
                    tristrips_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    tristrips_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] t_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(t_input);
            Input vertex_input = Input.getInput(t_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            boolean need_another_index = false;
            Input normal_input = Input.getInput(t_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if (normal_input != null) {
                need_another_index |= (normal_input.offset != vertex_offset);
            }
            Input texCoord_input = Input.getInput(t_input, ColladaStrings.TEXCOORD);
            if (texCoord_input != null) {
                need_another_index |= (texCoord_input.offset != vertex_offset);
            }
            List<CElement> p_list =
                    tristrips_element.getElementsByTagName(ColladaStrings.P);
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            if (vertexSource == null) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSource", I18nUtils.CRIT_MSG, new String[] {position_source_id});
                throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }
            Source normalSource = null;
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);
            }
            Source texCoordSource = null;
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);
            }
            if (need_another_index) {
                // there is not a single index - switch to an IndexedFaceSet

                int num_indices = 0;
                int[][] poly_indices = new int[num_tristrips][];
                for (int j = 0; j < num_tristrips; j++) {
                    CElement p_element = p_list.get(j);
                    int[] p_indices = ((IntContent)p_element).getIntContent();
                    poly_indices[j] = Indexer.getPolyIndicesForTristrip(
                            p_indices, vertex_offset, num_offsets);
                    num_indices += poly_indices[j].length;
                }
                int[] v_index = FieldValueHandler.flatten(poly_indices, num_indices);

                int[] n_index = null;
                if (normalSource != null) {
                    int normal_offset = normal_input.offset;
                    if (normal_offset != vertex_offset) {
                        // normals have indices distinct from the vertices
                        num_indices = 0;
                        for (int j = 0; j < num_tristrips; j++) {
                            CElement p_element = p_list.get(j);
                            int[] p_indices = ((IntContent)p_element).getIntContent();
                            poly_indices[j] = Indexer.getPolyIndicesForTristrip(
                                    p_indices, normal_offset, num_offsets);
                            num_indices += poly_indices[j].length;
                        }
                        n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }

                int[] tc_index = null;
                if (texCoordSource != null) {
                    int texCoord_offset = texCoord_input.offset;
                    if (texCoord_offset != vertex_offset) {
                        // texCoords have indices distinct from the vertices
                        num_indices = 0;
                        for (int j = 0; j < num_tristrips; j++) {
                            CElement p_element = p_list.get(j);
                            int[] p_indices = ((IntContent)p_element).getIntContent();
                            poly_indices[j] = Indexer.getPolyIndicesForTristrip(
                                    p_indices, texCoord_offset, num_offsets);
                            num_indices += poly_indices[j].length;
                        }
                        tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                    }
                }

                // get the color coordinates
                Source colorSource = null;
                int[] color_index = null;

                buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index, colorSource, color_index);

            } else {
                contentHandler.startField("geometry");
                contentHandler.startNode("IndexedTriangleStripSet", null);
                contentHandler.startField("index");

                int num_indices = 0;
                int[][] tristrips_indices = new int[num_tristrips][];
                for (int j = 0; j < num_tristrips; j++) {
                    CElement p_element = p_list.get(j);
                    int[] p_indices = ((IntContent)p_element).getIntContent();
                    tristrips_indices[j] = Indexer.getIndices(
                            p_indices, vertex_offset, num_offsets);
                    num_indices += tristrips_indices[j].length;
                }
                int[] vertex_indices =
                        FieldValueHandler.flatten(tristrips_indices, num_indices);
                
                if (handlerIsBinary) {
                    bch.fieldValue(vertex_indices, vertex_indices.length);
                } else {
                    sch.fieldValue(FieldValueHandler.toString(vertex_indices));
                }

                buildCoordField(vertexSource);

                if ( normalSource != null ) {
                    buildNormalField(normalSource);
                }

                if ( texCoordSource != null ) {
                    buildTexCoordField(texCoordSource);
                }

                contentHandler.endNode();   // IndexedTriangleStripSet
                //contentHandler.endField();  // geometry
            }
            String material = tristrips_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-polylist
     *
     * @param polylist_list A NodeList containing the polylist Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processPolylist(
        List<CElement> polylist_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement polylist_list1 : polylist_list) {
            contentHandler.startNode("Shape", null);
            CElement polylist_element = polylist_list1;
            int num_polys = ColladaParserUtils.getIntValue(
                    polylist_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    polylist_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] p_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(p_input);
            Input vertex_input = Input.getInput(p_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            CElement p_element =
                    polylist_element.getFirstElementByTagName(ColladaStrings.P);
            CElement vcount_element =
                    polylist_element.getFirstElementByTagName(ColladaStrings.VCOUNT);
            Vcount vertex_data = new Vcount((IntContent)vcount_element);
            int[] p_indices = ((IntContent)p_element).getIntContent();
            int[] v_index = Indexer.getPolyIndices(
                    p_indices, vertex_data, vertex_offset, num_offsets);
            // get the coordinate points
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            if (vertexSource == null) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSource", I18nUtils.CRIT_MSG, new String[] {position_source_id});
                throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }
            v_index = validateIndices(v_index, vertexSource, 3, "Coordinate");
            // get the normal points
            Source normalSource = null;
            int[] n_index = null;
            // search for the normal inputs from the polylist element
            // if not found, search in the vertices element
            Input normal_input = Input.getInput(p_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);

                int normal_offset = normal_input.offset;
                if (normal_offset != vertex_offset) {
                    // normals have indices distinct from the vertices
                    n_index = Indexer.getPolyIndices(
                            p_indices, vertex_data, normal_offset, num_offsets);
                }
            }
            // TODO: Support multi-set texture coordinates (ex: semantic_texcoord_multi_set.dae)

            // get the texture coordinates
            Source texCoordSource = null;
            int[] tc_index = null;
            // search for the texture coordinate input from the polylist element
            // if not found, search in the vertices element
            Input texCoord_input = Input.getInput(p_input, ColladaStrings.TEXCOORD);
            if (texCoord_input == null) {
                texCoord_input = Input.getInput(v_input, ColladaStrings.TEXCOORD);
            }
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);

                int texCoord_offset = texCoord_input.offset;
                if (texCoord_offset != vertex_offset) {
                    // texCoords have indices distinct from the vertices
                    tc_index = Indexer.getPolyIndices(
                            p_indices, vertex_data, texCoord_offset, num_offsets);
                }
            }
            // get the color coordinates
            Source colorSource = null;
            int[] color_index = null;
            // search for the color input from the polylist element
            // if not found, search in the vertices element
            Input color_input = Input.getInput(p_input, ColladaStrings.SEMANTIC_COLOR);
            if (color_input == null) {
                color_input = Input.getInput(v_input, ColladaStrings.SEMANTIC_COLOR);
            }
            if ( color_input != null ) {
                String color_source_id = getElementId(color_input.source);
                colorSource = sourceMap.get(color_source_id);

                int color_offset = color_input.offset;
                if (color_offset != vertex_offset) {
                    // color coordinates have indices distinct from the vertices
                    color_index = Indexer.getPolyIndices(
                            p_indices, vertex_data, color_offset, num_offsets);
                }
            }
            buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index, colorSource, color_index);
            String material = polylist_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Create a Shape node's geometry field, using an IndexedFaceSet node
     *
     * @param vertexSource The Source object containing vertex coordinates.
     * @param vertex_indices An array containing the vertex coordinate indices.
     * @param normalSource The Source object containing normal coordinates.
     * If null the normal and normalIndex field are not created.
     * @param normal_indices An array containing normal coordinate indices.
     * If null the normalIndex field is not created.
     * @param texCoordSource The Source object containing texture coordinates.
     * If null the texCoord and texCoordIndex field are not created.
     * @param texCoord_indices An array containing texture coordinate indices.
     * If null the texCoordIndex field is not created.
     */
    private void buildIFS(
        Source vertexSource, int[] vertex_indices,
        Source normalSource, int[] normal_indices,
        Source texCoordSource, int[] texCoord_indices,
        Source colorSource, int[] color_indices) {

        contentHandler.startField("geometry");
        contentHandler.startNode("IndexedFaceSet", null);

        buildCoordField(vertexSource);

        contentHandler.startField("coordIndex");
        if (handlerIsBinary) {
            //int[] indices = (int[])vertex_indices;
            bch.fieldValue(vertex_indices, vertex_indices.length);
        } else {
            sch.fieldValue(FieldValueHandler.toString(vertex_indices));
        }

        if (normalSource != null) {

            buildNormalField(normalSource);

            if (normal_indices != null) {
                // normals have indices distinct from the vertices
                contentHandler.startField("normalIndex");
                if (handlerIsBinary) {
                    bch.fieldValue(normal_indices, normal_indices.length);
                } else {
                    sch.fieldValue(FieldValueHandler.toString(normal_indices));
                }
            }
        }

        if (texCoordSource != null) {

            buildTexCoordField(texCoordSource);

            if (texCoord_indices != null) {
                // texCoords have indices distinct from the vertices
                contentHandler.startField("texCoordIndex");
                if (handlerIsBinary) {
                    bch.fieldValue(texCoord_indices, texCoord_indices.length);
                } else {
                    sch.fieldValue(FieldValueHandler.toString(texCoord_indices));
                }
            }
        }

        if (colorSource != null) {

        	buildColorField(colorSource);

            if (color_indices != null) {
                // color coordinates have indices distinct from the vertices
                contentHandler.startField("colorIndex");
                if (handlerIsBinary) {
                    bch.fieldValue(color_indices, color_indices.length);
                } else {
                    sch.fieldValue(FieldValueHandler.toString(color_indices));
                }
            }
        }

        contentHandler.endNode();   // IndexedFaceSet
        //contentHandler.endField();  // geometry
    }

    /**
     * Add Shape nodes corresponding to the mesh-polygons
     *
     * @param polygons_list A NodeList containing the polylist Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processPolygons(
        List<CElement> polygons_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement polygons_list1 : polygons_list) {
            contentHandler.startNode("Shape", null);
            CElement polygons_element = polygons_list1;
            int num_polys = ColladaParserUtils.getIntValue(
                    polygons_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    polygons_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] p_input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(p_input);
            Input vertex_input = Input.getInput(p_input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            List<CElement> p_list =
                    polygons_element.getElementsByTagName(ColladaStrings.P);
            List<CElement> ph_list =
                    polygons_element.getElementsByTagName(ColladaStrings.PH);
            if (ph_list.size() > 0) {
                I18nUtils.printMsg("xj3d.filter.BaseFilter.notAllGeometryConvertible", I18nUtils.CRIT_MSG, new String[] {"<ph> Polygon with holes"});
                throw new FilterProcessingException(
                        LOG_NAME,
                        FilterExitCodes.NOT_ALL_GEOMETRY_IS_CONVERTABLE,
                        "Geometry of type: Polygons with holes is not supported.");
            }
            int num_indices = 0;
            int[][] poly_indices = new int[num_polys][];
            for (int j = 0; j < num_polys; j++) {
                CElement p_element = p_list.get(j);
                int[] p_indices = ((IntContent)p_element).getIntContent();
                poly_indices[j] = Indexer.getIndices(
                        p_indices, vertex_offset, num_offsets);
                num_indices += poly_indices[j].length;
            }
            int[] v_index = FieldValueHandler.flatten(poly_indices, num_indices);
            // get the coordinate points
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            String position_source_id = getElementId(position_input.source);
            Source vertexSource = sourceMap.get(position_source_id);
            if (vertexSource == null) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSource", I18nUtils.CRIT_MSG, new String[] {position_source_id});
                throw new FilterProcessingException(ColladaParserConstants.LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }
            v_index = validateIndices(v_index, vertexSource, 3, "Coordinate");
            Source normalSource = null;
            int[] n_index = null;
            // search for the normal inputs from the polygon element
            // if not found, search in the vertices element
            Input normal_input = Input.getInput(p_input, ColladaStrings.NORMAL);
            if (normal_input == null) {
                normal_input = Input.getInput(v_input, ColladaStrings.NORMAL);
            }
            if ( normal_input != null ) {
                String normal_source_id = getElementId(normal_input.source);
                normalSource = sourceMap.get(normal_source_id);

                int normal_offset = normal_input.offset;
                if (normal_offset != vertex_offset) {
                    // normals have indices distinct from the vertices
                    num_indices = 0;
                    for (int j = 0; j < num_polys; j++) {
                        CElement p_element = p_list.get(j);
                        int[] p_indices = ((IntContent)p_element).getIntContent();
                        poly_indices[j] = Indexer.getIndices(
                                p_indices, normal_offset, num_offsets);
                        num_indices += poly_indices[j].length;
                    }
                    n_index = FieldValueHandler.flatten(poly_indices, num_indices);
                }
            }
            Source texCoordSource = null;
            int[] tc_index = null;
            // TODO: Support multi-set texture coordinates (ex: semantic_texcoord_multi_set.dae)
            // Note: Supports only a single texture per material/effect

            // search for the texture coordinate inputs from the polygon element
            // if not found, search in the vertices element
            Input texCoord_input = Input.getInput(p_input, ColladaStrings.TEXCOORD);
            if (texCoord_input == null) {
                texCoord_input = Input.getInput(v_input, ColladaStrings.TEXCOORD);
            }
            if ( texCoord_input != null ) {
                String texCoord_source_id = getElementId(texCoord_input.source);
                texCoordSource = sourceMap.get(texCoord_source_id);

                int texCoord_offset = texCoord_input.offset;
                if (texCoord_offset != vertex_offset) {
                    // texCoords have indices distinct from the vertices
                    num_indices = 0;
                    for (int j = 0; j < num_polys; j++) {
                        CElement p_element = p_list.get(j);
                        int[] p_indices = ((IntContent)p_element).getIntContent();
                        poly_indices[j] = Indexer.getIndices(
                                p_indices, texCoord_offset, num_offsets);
                        num_indices += poly_indices[j].length;
                    }
                    tc_index = FieldValueHandler.flatten(poly_indices, num_indices);
                }
            }
            // get the color coordinates
            Source colorSource = null;
            int[] color_index = null;
            // search for the color inputs from the polygon element
            // if not found, search in the vertices element
            Input color_input = Input.getInput(p_input, ColladaStrings.SEMANTIC_COLOR);
            if (color_input == null) {
                color_input = Input.getInput(v_input, ColladaStrings.SEMANTIC_COLOR);
            }
            if ( color_input != null ) {
                String color_source_id = getElementId(color_input.source);
                colorSource = sourceMap.get(color_source_id);

                int color_offset = color_input.offset;
                if (color_offset != vertex_offset) {
                    // colors have indices distinct from the vertices
                    num_indices = 0;
                    for (int j = 0; j < num_polys; j++) {
                        CElement p_element = p_list.get(j);
                        int[] p_indices = ((IntContent)p_element).getIntContent();
                        poly_indices[j] = Indexer.getIndices(
                                p_indices, color_offset, num_offsets);
                        num_indices += poly_indices[j].length;
                    }
                    color_index = FieldValueHandler.flatten(poly_indices, num_indices);
                }
            }
            buildIFS(vertexSource, v_index, normalSource, n_index, texCoordSource, tc_index, colorSource, color_index);
            String material = polygons_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-lines
     *
     * @param lines_list A NodeList containing the lines Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processLines(
        List<CElement> lines_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement lines_list1 : lines_list) {
            contentHandler.startNode("Shape", null);
            CElement lines_element = lines_list1;
            int num_lines = ColladaParserUtils.getIntValue(
                    lines_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    lines_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(input);
            Input vertex_input = Input.getInput(input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            CElement p_element =
                    lines_element.getFirstElementByTagName(ColladaStrings.P);
            int[] p_indices = ((IntContent)p_element).getIntContent();
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coordIndex");
            int[] vertex_indices = Indexer.getLinesIndices(
                    p_indices, num_lines, vertex_offset, num_offsets);
            if (handlerIsBinary) {
                bch.fieldValue(vertex_indices, vertex_indices.length);
            } else {
                sch.fieldValue(FieldValueHandler.toString(vertex_indices));
            }
            // get the coordinate points
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            String position_source_id = getElementId(position_input.source);
            Source verticesSource = sourceMap.get(position_source_id);
            buildCoordField(verticesSource);
            contentHandler.endNode();   // IndexedLineSet
            //contentHandler.endField();  // geometry
            String material = lines_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Add Shape nodes corresponding to the mesh-linestrips
     *
     * @param linestrips_list A NodeList containing the linestrips Elements of the mesh
     * @param sourceMap A Map of the Source Elements of the mesh, keyed by ID
     * @param vertices_element The vertices Element of the mesh
     * @param bindMaterial The data binding object that provides lookup of the material
     * to include with the shape.
     */
    private void processLinestrips(
        List<CElement> linestrips_list,
        Map<String, Source> sourceMap,
        CElement vertices_element,
        BindMaterial bindMaterial) {

        for (CElement linestrips_list1 : linestrips_list) {
            contentHandler.startNode("Shape", null);
            CElement linestrips_element = linestrips_list1;
            int num_linestrips = ColladaParserUtils.getIntValue(
                    linestrips_element, ColladaStrings.COUNT);
            List<CElement> input_list =
                    linestrips_element.getElementsByTagName(ColladaStrings.INPUT);
            Input[] input = Input.getInputs(input_list);
            int num_offsets = Input.getNumberOfOffsets(input);
            Input vertex_input = Input.getInput(input, ColladaStrings.VERTEX);
            int vertex_offset = vertex_input.offset;
            List<CElement> p_list =
                    linestrips_element.getElementsByTagName(ColladaStrings.P);
            contentHandler.startField("geometry");
            contentHandler.startNode("IndexedLineSet", null);
            contentHandler.startField("coordIndex");
            int num_indices = 0;
            int[][] strip_indices = new int[num_linestrips][];
            for (int j = 0; j < num_linestrips; j++) {
                CElement p_element = p_list.get(j);
                int[] p_indices = ((IntContent)p_element).getIntContent();
                strip_indices[j] = Indexer.getIndices(
                        p_indices, vertex_offset, num_offsets);
                num_indices += strip_indices[j].length;
            }
            int[] vertex_indices =
                    FieldValueHandler.flatten(strip_indices, num_indices);
            if (handlerIsBinary) {
                bch.fieldValue(vertex_indices, vertex_indices.length);
            } else {
                sch.fieldValue(FieldValueHandler.toString(vertex_indices));
            }
            // get the coordinate points
            input_list =
                    vertices_element.getElementsByTagName(ColladaStrings.INPUT, input_list);
            Input[] v_input = Input.getInputs(input_list);
            Input position_input = Input.getInput(v_input, ColladaStrings.POSITION);
            String position_source_id = getElementId(position_input.source);
            Source verticesSource = sourceMap.get(position_source_id);
            buildCoordField(verticesSource);
            contentHandler.endNode();   // IndexedLineSet
            //contentHandler.endField();  // geometry
            String material = linestrips_element.getAttribute(ColladaStrings.MATERIAL);
            CElement material_instance_element = bindMaterial.getTarget(material);
            processMaterialInstance(material_instance_element);
            contentHandler.endNode();   // Shape
        }
    }

    /**
     * Create the coord field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildCoordField(Source source) {

        String id = source.id;
        contentHandler.startField("coord");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            ///////////////////////////////////////////////////////////
            // rem: stop trying to def/use coordinates. shapeways #139
            //sourceInstanceList.add(id);
            //contentHandler.startNode("Coordinate", id);
            ///////////////////////////////////////////////////////////
            contentHandler.startNode("Coordinate", null);
            contentHandler.startField("point");
            float[] source_data = null;
            try {
            	source_data = (float[])source.getSourceData();
            } catch (Exception e) {
                I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSourceData", I18nUtils.CRIT_MSG, new String[] {id});
                throw new FilterProcessingException(LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
            }

            /*
            if (APPLY_SCALE) {
                for(int i=0; i < source_data.length; i++) {
                    source_data[i] = source_data[i] * scale;
                }
            }
            */
            if (handlerIsBinary) {
                ///////////////////////////////////////////////////////////
                // rem: clone the coordinates. shapeways #139
                //bch.fieldValue(source_data, source_data.length);
                ///////////////////////////////////////////////////////////
                int len = source_data.length;
                float[] source_data_copy = new float[len];
                System.arraycopy(source_data, 0, source_data_copy, 0, len);
                bch.fieldValue(source_data_copy, len);
            } else {
                sch.fieldValue(FieldValueHandler.toString(source_data));
            }
            contentHandler.endNode();   // Coordinate
            //contentHandler.endField();  // coord
        }
    }

    /**
     * Create the normal field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildNormalField(Source source) {
        String id = source.id;
        contentHandler.startField("normal");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("Normal", id);
            contentHandler.startField("vector");
                float[] source_data = (float[])source.getSourceData();
            if (handlerIsBinary) {
                bch.fieldValue(source_data, source_data.length);
            } else {
                sch.fieldValue(FieldValueHandler.toString(source_data));
            }
            contentHandler.endNode();   // Normal
            //contentHandler.endField();  // normal
        }
    }

    /**
     * Create the texCoord field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildTexCoordField(Source source) {
        String id = source.id;
        contentHandler.startField("texCoord");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("TextureCoordinate", id);
            contentHandler.startField("point");
            float[] source_data = (float[])source.getSourceData();
            if (handlerIsBinary) {
                bch.fieldValue(source_data, source_data.length);
            } else {
                sch.fieldValue(FieldValueHandler.toString(source_data));
            }
            contentHandler.endNode();   // TextureCoordinate
            //contentHandler.endField();  // texCcoord
        }
    }

    /**
     * Create the color field of an x3d geometry node from the
     * data in the Source.
     *
     * @param source The Source data object.
     */
    private void buildColorField(Source source) {
        String id = source.id;
        contentHandler.startField("color");
        if (sourceInstanceList.contains(id)) {
            contentHandler.useDecl(id);
        } else {
            sourceInstanceList.add(id);
            contentHandler.startNode("Color", id);
            contentHandler.startField("color");
            float[] source_data = (float[])source.getSourceData();
            if (handlerIsBinary) {
                bch.fieldValue(source_data, source_data.length);
            } else {
                sch.fieldValue(FieldValueHandler.toString(source_data));
            }
            contentHandler.endNode();   // Color
            //contentHandler.endField();  // color
        }
    }

    /**
     * Add an Appearance node for the specified material to the content handler
     *
     * @param node A Collada material_instance
     */
    private void processMaterialInstance(CElement node) {
        if (style != null && style.contains("UNCOLORED")) {
            return;
        }

        if (node != null) {
            contentHandler.startField("appearance");
            contentHandler.startNode("Appearance", null);

            String url = node.getAttribute(ColladaStrings.TARGET);
            String material_url_id = getElementId(url);

            // find the material resource, identified by it's url
            CElement material_element =
                getResourceElement(ColladaStrings.MATERIAL, material_url_id);
            if (material_element != null) {
                CElement instance_effect_element =
                    material_element.getFirstElementByTagName(ColladaStrings.INSTANCE_EFFECT);
                url = instance_effect_element.getAttribute(ColladaStrings.URL);
                String effect_url_id = getElementId(url);
                CElement effect_element =
                    getResourceElement(ColladaStrings.EFFECT, effect_url_id);

                if (effect_element != null) {
                    CElement profile_common_element =
                        effect_element.getFirstElementByTagName(ColladaStrings.PROFILE_COMMON);

                    if (profile_common_element != null) {
                        CElement technique_element =
                            profile_common_element.getFirstElementByTagName(ColladaStrings.TECHNIQUE);
                        List<CElement> material_list =
                            new ArrayList<>(technique_element.getElements());
                        cull(material_list, MATERIAL);

                        if (material_list.size() > 0) {
                        	String technique_texture_ref = null;
//                            contentHandler.startField("material");

                            if (effectToTextureMap.containsKey(effect_url_id)) {
                            	contentHandler.startField("material");
                                contentHandler.useDecl(effect_url_id);
                                String image_url_id = effectToTextureMap.get(effect_url_id);
                                if (image_url_id != null) {
                                	contentHandler.startField("texture");
                                	contentHandler.useDecl(image_url_id);
                                }
                            } else {
                            	contentHandler.startField("material");
                                contentHandler.startNode("Material", effect_url_id);
                                // there should be only one and only one
                                CElement material_type_element = material_list.get(0);
                                String id = material_type_element.getTagName();

                                    switch (id) {
                                        case ColladaStrings.BLINN:
                                            technique_texture_ref = processMaterialFields(material_type_element);
                                            break;
                                        case ColladaStrings.CONSTANT:
                                            technique_texture_ref = processMaterialFields(material_type_element);
                                            break;
                                        case ColladaStrings.LAMBERT:
                                            technique_texture_ref = processMaterialFields(material_type_element);
                                            break;
                                        case ColladaStrings.PHONG:
                                            technique_texture_ref = processMaterialFields(material_type_element);
                                            break;
                                    // this should be an error, there should be one....
                                        default:
                                            break;
                                    }
                                contentHandler.endNode();   // Material
                                //contentHandler.endField();  // material

                                if (technique_texture_ref != null) {
                                	processTextureReference(effect_url_id, technique_texture_ref, profile_common_element, effect_element);
                                } else {
                                	effectToTextureMap.put(effect_url_id, null);
                                }
                            }
                        }
                    }
                }
            }

            contentHandler.endNode();   // Appearance
            //contentHandler.endField();  // appearance

        }
/*        // Defaulting to non default appearance undesired.
        else {
            // a 'default' Appearance
            contentHandler.startField("material");
            contentHandler.startNode("Material", null);
            contentHandler.startField("diffuseColor");
            if (handlerIsBinary) {
                float[] content = new float[]{ 0.7f, 0.7f, 0.7f };
                bch.fieldValue(content, content.length);
            } else {
                sch.fieldValue("0.7 0.7 0.7");
            }
            contentHandler.endNode();   // Material
            contentHandler.endField();  // material
        }
*/
    }

    /**
     * Translates a COLLADA texture element into an X3D texture.
     * Takes the texture reference, get the source from the corresponding sampler2D,
     * and then the init_from from the corresponding surface. The init_from value
     * is the id of the referenced image.
     *
     * @param technique_texture_ref The texture reference to the sampler2D
     * @param profile_common_element The profile_COMMON element to search under
     * @param effect_element The effect element to search under
     */
    private void processTextureReference(
    		String effect_url_id,
    		String technique_texture_ref,
    		CElement profile_common_element,
    		CElement effect_element) throws ImportFileFormatException {

    	String sampler_source = findSampler2DSource(profile_common_element, technique_texture_ref);
    	if (sampler_source == null) {
    		sampler_source = findSampler2DSource(effect_element, technique_texture_ref);
    	}

    	if (sampler_source != null) {
    		String image_id = findSurfaceInit(profile_common_element, sampler_source);
    		if (image_id == null) {
    			image_id = findSurfaceInit(effect_element, sampler_source);
    		}

    		if (image_id != null) {
    			if (effectToTextureMap.containsValue(image_id)) {
    				contentHandler.startField("texture");
    				contentHandler.useDecl(image_id);
    		        effectToTextureMap.put(effect_url_id, image_id);
    			} else {
        			String image_name = findImageNameFromId(image_id, profile_common_element, effect_element);

	    			if (image_name != null) {
	    				contentHandler.startField("texture");
	    				contentHandler.startNode("ImageTexture", image_id);
	    				contentHandler.startField("url");

	    				String[] urls = new String[] {image_name};
	    		        if (handlerIsBinary) {
	    		            bch.fieldValue(urls, urls.length);
	    		        } else {
	    		            sch.fieldValue(java.util.Arrays.toString(urls));
	    		        }

	    		        contentHandler.endField(); // url
	    		        contentHandler.endNode();  // ImageTexture
	    		        effectToTextureMap.put(effect_url_id, image_id);
	    			}
    			}

    		}
    	}

    }

    /**
     * Get the source of the sampler2D.
     *
     * @param e The element to search under
     * @param textureRef The name of the texture reference
     * @return The source of the sampler2D. Returns null if not found.
     */
    private String findSampler2DSource(CElement e, String textureRef) {
    	String source = null;
    	CElement newparam_element =
    			e.getFirstElementsByTagNameAndAttribute(ColladaStrings.NEWPARAM, ColladaStrings.SID, textureRef);

    	if (newparam_element != null) {
    		CElement sampler2d_element = newparam_element.getFirstElementByTagName(ColladaStrings.SAMPLER2D);

    		if (sampler2d_element != null) {
    			CElement source_element = sampler2d_element.getFirstElementByTagName(ColladaStrings.SOURCE);

    			if (source_element != null) {
    				source = source_element.getTextContent();
    			}
    		}
    	}

    	return source;
    }

    /**
     * Get the content of the <init_from>, given the source of the sampler2D. The content
     * contains the id of the <image>.
     *
     * @param e The element to search under
     * @param samplerSource The name of the sampler2D source
     * @return The source of the sampler2D. Returns null if not found.
     */
    private String findSurfaceInit(CElement e, String samplerSource) {
    	String init = null;
    	CElement newparam_element =
    			e.getFirstElementsByTagNameAndAttribute(ColladaStrings.NEWPARAM, ColladaStrings.SID, samplerSource);

    	if (newparam_element != null) {
    		CElement surface_element = newparam_element.getFirstElementByTagName(ColladaStrings.SURFACE);

    		if (surface_element != null) {
    			CElement init_element = surface_element.getFirstElementByTagName(ColladaStrings.INIT_FROM);

    			if (init_element != null) {
    				init = init_element.getTextContent();
    			}
    		}
    	}

    	return init;
    }

    /**
     * Get the file name of the image. Searches for the image element under
     * <library_images>, <effect>, and <profile_COMMON>.
     *
     * @param imageId The id of the image
     * @param profileElement The profile_COMMON element to search under
     * @param effectElement The effect element to search under
     * @return The image file name referenced by imageId
     */
    private String findImageNameFromId(String imageId, CElement profileElement, CElement effectElement) {
    	// Check in library_images first
    	CElement library_images_element = doc_element.getFirstElementByTagName(ColladaStrings.LIBRARY_IMAGES);

    	if (library_images_element != null) {
    		CElement image_element =
    				library_images_element.getFirstElementsByTagNameAndAttribute(ColladaStrings.IMAGE, ColladaStrings.ID, imageId);

        	if (image_element != null) {
        		CElement init_element = image_element.getFirstElementByTagName(ColladaStrings.INIT_FROM);
        		return init_element.getTextContent();
        	}
    	}

    	// Check in profile_COMMON element
    	CElement image_element = profileElement.getFirstElementsByTagNameAndAttribute(ColladaStrings.IMAGE, ColladaStrings.ID, imageId);

    	if (image_element != null) {
    		CElement init_element = image_element.getFirstElementByTagName(ColladaStrings.INIT_FROM);
    		return init_element.getTextContent();
    	}

    	// Check in effects element
    	image_element = effectElement.getFirstElementsByTagNameAndAttribute(ColladaStrings.IMAGE, ColladaStrings.ID, imageId);

    	if (image_element != null) {
    		CElement init_element = image_element.getFirstElementByTagName(ColladaStrings.INIT_FROM);
    		return init_element.getTextContent();
    	}

    	return null;
    }

    /**
     * Translate the fields of the Collada 'material' to X3D
     *
     * @param e The Collada material type element.
     * @return
     */
    private String processMaterialFields(CElement e) throws ImportFileFormatException {
        if (style != null && style.equals("UNCOLORED")) {
            return null;
        }

        // If emission, diffuse, specular elements reference a texture, we return the reference
        // Note: we only support a single texture per material
        String texture_ref = null;

        CElement emission_element =
            e.getFirstElementByTagName(ColladaStrings.EMISSION);
        if (emission_element != null) {
            CElement color_element = emission_element.getFirstElementByTagName(ColladaStrings.COLOR);
            if (color_element != null) {
                contentHandler.startField("emissiveColor");
                processColor(color_element);
            }
            CElement texture_element = emission_element.getFirstElementByTagName(ColladaStrings.TEXTURE);
            if (texture_element != null) {
            	texture_ref = texture_element.getAttribute("texture");
            }
        }
        CElement diffuse_element =
            e.getFirstElementByTagName(ColladaStrings.DIFFUSE);
        if (diffuse_element != null) {
            CElement color_element = diffuse_element.getFirstElementByTagName(ColladaStrings.COLOR);
            if (color_element != null) {
                contentHandler.startField("diffuseColor");
                processColor(color_element);
            }
            CElement texture_element = diffuse_element.getFirstElementByTagName(ColladaStrings.TEXTURE);
            if (texture_element != null) {
            	texture_ref = texture_element.getAttribute("texture");
            }

        }
        CElement specular_element =
            e.getFirstElementByTagName(ColladaStrings.SPECULAR);
        if (specular_element != null) {
            CElement color_element = specular_element.getFirstElementByTagName(ColladaStrings.COLOR);
            if (color_element != null) {
                contentHandler.startField("specularColor");
                processColor(color_element);
            }
            CElement texture_element = specular_element.getFirstElementByTagName(ColladaStrings.TEXTURE);
            if (texture_element != null) {
            	texture_ref = texture_element.getAttribute("texture");
            }
        }
        CElement shininess_element =
            e.getFirstElementByTagName(ColladaStrings.SHININESS);
        if (shininess_element != null) {
            contentHandler.startField("shininess");
            CElement float_element =
                shininess_element.getFirstElementByTagName(ColladaStrings.FLOAT);
            float[] float_content = ((FloatContent)float_element).getFloatContent();
            float value = float_content[0];

            // note, special handling for the shininess value
            if ( value > 1 ) {
                value /= 128;
            }
            if (handlerIsBinary) {
                bch.fieldValue(value);
            } else {
                sch.fieldValue(Float.toString(value));
            }
        }

        return texture_ref;
    }

    /**
     * Translate a Collada 'color' element to an X3D SFColor field
     *
     * @param color_element A Collada element of the type 'common_color_or_texture_type'
     */
    private void processColor(CElement color_element) {
        float[] fcontent = ((FloatContent)color_element).getFloatContent();

        // note, dropping the alpha value....
        if (handlerIsBinary) {
            float[] content = new float[]{ fcontent[0], fcontent[1], fcontent[2] };
            bch.fieldValue(content, content.length);
        } else {
            sch.fieldValue(fcontent[0]+" "+fcontent[1]+" "+fcontent[2]);
        }
    }

    /**
     * Translate an element that contains a Collada 'float' element to an X3D SFFloat field
     *
     * @param float_element A Collada element of the type 'common_float_or_param_type'
     */
    private void processFloat(CElement float_element) {
        float[] fcontent = ((FloatContent)float_element).getFloatContent();
        if (handlerIsBinary) {
            bch.fieldValue(fcontent[0]);
        } else {
            sch.fieldValue(Float.toString(fcontent[0]));
        }
    }

    /**
     * Add a Viewpoint node for the specified camera to the content handler
     *
     * @param node A Collada camera_instance
     */
    private void processCameraInstance(CElement node) {

        String url = node.getAttribute(ColladaStrings.URL);
        String camera_url_id = getElementId(url);

        // find the camera resource, identified by it's url
        CElement camera_element =
            getResourceElement(ColladaStrings.CAMERA, camera_url_id);
        if (camera_element != null) {
            String name = camera_element.getAttribute(ColladaStrings.NAME);
            CElement optics_element =
                camera_element.getFirstElementByTagName(ColladaStrings.OPTICS);
            CElement technique_common_element =
                optics_element.getFirstElementByTagName(ColladaStrings.TECHNIQUE_COMMON);
            CElement perspective_element =
                technique_common_element.getFirstElementByTagName(ColladaStrings.PERSPECTIVE);
            if (perspective_element != null) {
                // for now we only deal with perspective
                contentHandler.startNode("Viewpoint", camera_url_id);
                contentHandler.startField("description");
                if (handlerIsBinary) {
                    bch.fieldValue(name);
                } else {
                    sch.fieldValue(name);
                }
                contentHandler.startField("position");
                if (handlerIsBinary) {
                    bch.fieldValue(new float[]{ 0, 0, 0 }, 3);
                } else {
                    sch.fieldValue("0 0 0");
                }

                contentHandler.endNode(); // Viewpoint
            } else {
                CElement orthographic_element =
                    technique_common_element.getFirstElementByTagName(ColladaStrings.ORTHOGRAPHIC);
                if (orthographic_element != null) {
                    // don't know how to deal with orthographic yet
                }
            }
        }
    }

    /**
     * Return the id string of a library element
     *
     * @param url The url String from which to extract the id
     * @return The id String, or null if we're baffled by the url......
     */
    private String getElementId(String url) {
        String id = null;
        if (url.startsWith("#")) {
            // a local url, trim off the identifier
            id = url.substring(1);
        } else {
            // don't know what to do with non local urls yet
        }
        return(id);
    }

    /**
     * Remove any Elements from the argument list that are not named
     * in the argument tagName array.
     *
     * @param list A list of Elements to inspect
     * @param children An array of element tags to search for
     */
    private void cull(List<CElement> list, String[] children) {
        for (int i = list.size()-1; i >= 0; i-- ) {
            CElement e = list.get(i);
            String name = e.getTagName();
            boolean supported = false;
            for (String children1 : children) {
                if (name.equals(children1)) {
                    supported = validate(e);
                    break;
                }
            }
            if ( !supported ) {
                list.remove(i);
            }
        }
    }

    /**
     * Called when an element is on the initial supported list to 'look deeper'
     * into the instance elements to determine if we can REALLY support it.
     *
     * @param element The element
     * @return true if the element is REALLY supported, false otherwise.
     */
    private boolean validate(CElement element) {
        boolean isSupported = true;
        String tagName = element.getTagName();
        switch (tagName) {
            case ColladaStrings.INSTANCE_GEOMETRY:
                {
                    String url = element.getAttribute(ColladaStrings.URL);
                    String url_id = getElementId(url);
                    // find the geometry resource, identified by it's url
                    CElement geometry_element =
                            getResourceElement(ColladaStrings.GEOMETRY, url_id);
                    if (geometry_element != null) {
                        CElement mesh_element =
                                geometry_element.getFirstElementByTagName(ColladaStrings.MESH);
                        if (mesh_element == null) {
                            // only mesh geometry supported so far
                            isSupported = false;
                        }
                    } else {
                        // the resource could not be found
                        isSupported = false;
            }       break;
                }
            case ColladaStrings.INSTANCE_CAMERA:
            {
                String url = element.getAttribute(ColladaStrings.URL);
                String url_id = getElementId(url);
                // find the camera resource, identified by it's url
                CElement camera_element =
                        getResourceElement(ColladaStrings.CAMERA, url_id);
                if (camera_element != null) {
                    CElement optics_element =
                            camera_element.getFirstElementByTagName(ColladaStrings.OPTICS);
                    CElement technique_common_element =
                            optics_element.getFirstElementByTagName(ColladaStrings.TECHNIQUE_COMMON);
                    CElement perspective_element =
                            technique_common_element.getFirstElementByTagName(ColladaStrings.PERSPECTIVE);
                    if (perspective_element == null) {
                        // for now we only deal with perspective
                        // this would be orthographic....
                        isSupported = false;
                    }
                } else {
                    // the resource could not be found
                isSupported = false;
            }       break;
                }
        }
        return(isSupported);
    }

    /**
     * Search the libraries of the document of the argument resource type for the
     * Element that matches the argument id.
     * <p>
     * For example, for the resource type "geometry", the "library_geometries"
     * Elements will be searched for a "geometry" Element with the specified id
     * attribute.
     *
     * @param resource The resource type
     * @param id The identifier of the resource
     * @return The resource Element, or null if the specified element was not found.
     */
    private CElement getResourceElement(String resource, String id) {
        CElement resource_element = null;
        String library = ColladaStrings.getLibraryTagName(resource);
        CElement library_element =
            doc_element.getFirstElementByTagName(library);
        if (library_element != null) {
            List<CElement> resource_list = library_element.getElements();
            for (CElement tmp_element : resource_list) {
                String resource_id =
                        tmp_element.getAttribute(ColladaStrings.ID);
                if (id.equals(resource_id)) {
                    resource_element = tmp_element;
                    break;
                }
            }
        }
        return(resource_element);
    }

    /**
     * Add interpolator capabilities to the content handler
     *
     * @param animation_element A Collada <animation> element
     */
    private void processAnimation(CElement animation_element) {

        List<CElement> list = new ArrayList<>();

        list = animation_element.getElementsByTagName(ColladaStrings.SAMPLER, list);
        Map<String, Sampler> samplerMap = Sampler.getSamplerMap(list);

        list = animation_element.getElementsByTagName(ColladaStrings.SOURCE, list);
        Map<String, Source> sourceMap = Source.getSourceMap(list);

        list = animation_element.getElementsByTagName(ColladaStrings.CHANNEL, list);
        Channel[] channel = Channel.getChannels(list);

        for (Channel channel1 : channel) {
            String target_def_id = channel1.target;
            if (defedFieldMap.containsKey(target_def_id)) {
                String target_def_field = defedFieldMap.get(target_def_id);
                String sampler_id = getElementId(channel1.source);
                Sampler sampler = samplerMap.get(sampler_id);
                Input input = Input.getInput(sampler.input, ColladaStrings.INPUT_SEMANTIC);
                String input_source_id = getElementId(input.source);
                Source input_source = sourceMap.get(input_source_id);
                float[] input_data = (float[])input_source.getSourceData();
                float begin_time = input_data[0];
                float end_time = input_data[input_data.length - 1];
                float cycle_interval = end_time - begin_time;
                // convert the input data array to be the interpolator keys
                for (int j = 0; j < input_data.length; j++) {
                    float key = input_data[j] / cycle_interval;
                    input_data[j] = key;
                }
                Input output = Input.getInput(sampler.input, ColladaStrings.OUTPUT);
                String output_source_id = getElementId(output.source);
                Source output_source = sourceMap.get(output_source_id);
                Input interp = Input.getInput(sampler.input, ColladaStrings.INTERPOLATION);
                String interp_source_id = getElementId(interp.source);
                //Source interp_source = sourceMap.get(interp_source_id);
                ////////////////////////////////////////////////////////////////////////
                // TODO: the DEF id on this interpolator needs more thought....
                // may not be unique?
                contentHandler.startNode("PositionInterpolator", interp_source_id);
                ////////////////////////////////////////////////////////////////////////
                contentHandler.startField("key");
                if (handlerIsBinary) {
                    bch.fieldValue(input_data, input_data.length);
                } else {
                    String[] keys = FieldValueHandler.toString(input_data);
                    sch.fieldValue(keys);
                }
                contentHandler.startField("keyValue");
                if (handlerIsBinary) {
                    float[] keyValue_data =
                            (float[])output_source.getSourceData();
                    bch.fieldValue(keyValue_data, keyValue_data.length);
                } else {
                    String[] keyValue_data =
                            (String[])output_source.getSourceData();
                    sch.fieldValue(keyValue_data);
                }
                contentHandler.endNode();   // PositionInterpolator
                ////////////////////////////////////////////////////////////////////////
                // TODO: the DEF id on this time sensor needs more thought....
                // may not be unique?
                contentHandler.startNode("TimeSensor", input_source_id);
                ////////////////////////////////////////////////////////////////////////
                contentHandler.startField("loop");
                if (handlerIsBinary) {
                    bch.fieldValue(true);
                } else {
                    sch.fieldValue(Boolean.TRUE.toString());
                }
                contentHandler.startField("cycleInterval");
                if (handlerIsBinary) {
                    bch.fieldValue((double)cycle_interval);
                } else {
                    sch.fieldValue(Float.toString(cycle_interval));
                }
                contentHandler.endNode();   // TimeSensor
                routeHandler.routeDecl(
                        input_source_id, "fraction_changed",
                        interp_source_id, "set_fraction");
                routeHandler.routeDecl(
                        interp_source_id, "value_changed",
                        target_def_id, target_def_field +"_changed");
            }
        }
    }

    /**
     * Validate a set of indices against a source.  Will remove a span worth of items
     * if any of the indices are invalid.
     *
     * @param indices
     * @param source
     * @param span
     * @return
     */
    private int[] validateIndices(int[] indices, Source source, int span, String type) {
        float[] source_data = null;
        try {
            source_data = (float[])source.getSourceData();
        } catch (Exception e) {
            // TODO: get real value
            I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.missingSourceData", I18nUtils.CRIT_MSG, new String[] {"Unknown"});
            throw new FilterProcessingException(LOG_NAME, FilterExitCodes.INVALID_INPUT_FILE);
        }

        int len = indices.length / span;
        int max = source_data.length - 1;
        boolean warningIssued = false;
        HashSet<Integer> invalid = new HashSet<>();

        for(int i=0; i < len; i++) {
            for(int j=0; j < span; j++) {
                int idx = indices[i*span+j];
                int pos = idx * span + (span - 1);
                if (pos > max) {
                    if (!warningIssued) {
                        I18nUtils.printMsg("xj3d.filter.importer.ColladaFileParser.invalidIndex", I18nUtils.EXT_MSG, new String[] {type, "" + idx});
                        warningIssued = true;
                    }

                    invalid.add(i*3);
                    break;
                }
            }
        }

        if (invalid.size() == 0) {
            return indices;
        } else {
            // repack array
            int[] ret_val = new int[indices.length - invalid.size() * span];
            int cnt = 0;
            for(int i=0; i < len; i++) {
                if (!invalid.contains(i*span)) {
                    for(int j=0; j < span; j++) {
                        int idx = indices[i*3+j];
                        ret_val[cnt++] = idx;
                    }
                }
            }

            return ret_val;
        }
    }
}
