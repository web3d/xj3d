/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export;

// External imports
import com.sun.xml.fastinfoset.algorithm.BuiltInEncodingAlgorithmFactory;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.sax.AttributesHolder;
import com.sun.xml.fastinfoset.sax.SAXDocumentSerializer;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;

import org.jvnet.fastinfoset.EncodingAlgorithm;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.parsers.*;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.*;

// Local imports
import org.web3d.util.SimpleStack;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.parser.x3d.X3DFieldReader;
import org.web3d.parser.x3d.*;
import org.web3d.vrml.nodes.*;
import org.web3d.x3d.jaxp.X3DEntityResolver;
import org.web3d.vrml.renderer.CRProtoCreator;
import org.web3d.vrml.renderer.norender.NRProtoCreator;
import org.web3d.vrml.renderer.CRVRMLScene;

/**
 *  Serializes an X3D XML encoded document into an X3D binary document.
 *
 * TODO: Stats collection for non builtins isn't written
 * TODO: proto and script fields are encoded as strings
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class X3DBinarySerializer {

    // Encode files for fastest parsing
    public static final int METHOD_FASTEST_PARSING = 0;

    // Encode files for smallest size using non lossy techniques
    public static final int METHOD_SMALLEST_NONLOSSY = 1;

    // Encode files for smallest size using lossy techniques
    public static final int METHOD_SMALLEST_LOSSY = 2;

    // Encode files using Strings */
    public static final int METHOD_STRINGS = 3;

    // Smallest float difference for equality
    private static final float FLOAT_EPS = 0.0000009f;

    // Largest acceptable error for float quantization
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    // Usage docs
    private static final String USAGE = "Usage:  X3DSerializer [options] <input> <output>\n" +
                                        "options:  -method [fastest, smallest, lossy]\n" +
                                        "          -quantizeParam n\n" +
                                        "          -savedefaults";

    private Transformer _transformer;
    private DocumentBuilder _docBuilder;
    private Source _source = null;
    private SAXResult _result = null;

    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    protected static final String LOAD_DTD_ID = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";

    /** The node factory used to create real node instances */
    private VRMLNodeFactory nodeFactory;

    /** Cache of nodes used for type information. NodeName --> Node*/
    private Map<String, VRMLNodeType> nodeCache;

    /** A field parser */
    private X3DFieldReader fieldParser;

    private int[] fieldTypeStats;
    private int[] fieldTypeOrigSize;
    private int[] fieldTypeNewSize;
    private Map<Integer, String> fieldTypeMap;
    private boolean collectStats = true;

    /** What method should we use to compress */
    private int method;

    /** Should we remove defaults */
    private boolean removeDefaults;

    /** How many default values where removed */
    private int defaults;

    /** Single level proto map  */
    private Map<String, VRMLFieldDeclaration> protoMap;

    /** The creator used to instantiate protos */
    protected CRProtoCreator protoCreator;

    /** The spec major version to use */
    private int majorVersion;

    /** The spec minor version to use */
    private int minorVersion;

    /** Root node used for proto creation */
    protected VRMLWorldRootNodeType root;

    /** A Stack of element names */
    private SimpleStack elementStack;

    /**
     * Creates a new instance of X3DSerializer
     *
     * @param compMethod What method to use
     * @param rd Should we remove defaults
     * @param lossParam maximum loss for lossy compression
     */
    public X3DBinarySerializer(int compMethod, boolean rd, float lossParam) {
        method = compMethod;
        removeDefaults = rd;
        PARAM_FLOAT_LOSSY = lossParam;

        elementStack = new SimpleStack();
        elementStack.push("WorldRoot");

        try {
            // get a transformer and document builder
            _transformer = TransformerFactory.newInstance().newTransformer();
            _docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            e.printStackTrace(System.err);
        }

        majorVersion = 3;
        minorVersion = 1;

        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
        nodeFactory.setSpecVersion(majorVersion, minorVersion);
        nodeFactory.setProfile("Immersive");

        nodeCache = new HashMap<>();

        fieldParser = new X3DFieldReader();
        fieldParser.setCaseSensitive(false);

        fieldTypeStats = new int[43];
        fieldTypeOrigSize = new int[43];
        fieldTypeNewSize = new int[43];
        fieldTypeMap = new HashMap<>();
        fieldTypeMap.put(FieldConstants.SFINT32, "SFInt32    ");
        fieldTypeMap.put(FieldConstants.MFINT32, "MFInt32    ");
        fieldTypeMap.put(FieldConstants.SFFLOAT, "SFFloat    ");
        fieldTypeMap.put(FieldConstants.MFFLOAT, "MFFloat    ");
        fieldTypeMap.put(FieldConstants.SFDOUBLE, "SFDouble   ");
        fieldTypeMap.put(FieldConstants.MFDOUBLE, "MFDouble   ");
        fieldTypeMap.put(FieldConstants.SFLONG, "SFLong     ");
        fieldTypeMap.put(FieldConstants.MFLONG, "MFLong     ");
        fieldTypeMap.put(FieldConstants.SFBOOL, "SFBool     ");
        fieldTypeMap.put(FieldConstants.MFBOOL, "MFBool     ");
        fieldTypeMap.put(FieldConstants.SFVEC2F, "SFVec2f    ");
        fieldTypeMap.put(FieldConstants.MFVEC2F, "MFVec2f    ");
        fieldTypeMap.put(FieldConstants.SFVEC2D, "SFVec2d    ");
        fieldTypeMap.put(FieldConstants.MFVEC2D, "MFVec2d    ");
        fieldTypeMap.put(FieldConstants.SFVEC3F, "SFVec3f    ");
        fieldTypeMap.put(FieldConstants.MFVEC3F, "MFVec3f    ");
        fieldTypeMap.put(FieldConstants.SFVEC3D, "SFVec3d    ");
        fieldTypeMap.put(FieldConstants.MFVEC3D, "MFVec3d    ");
        fieldTypeMap.put(FieldConstants.SFVEC4F, "SFVec4f    ");
        fieldTypeMap.put(FieldConstants.MFVEC4F, "MFVec4f    ");
        fieldTypeMap.put(FieldConstants.SFVEC4D, "SFVec4d    ");
        fieldTypeMap.put(FieldConstants.MFVEC4D, "MFVec4d    ");
        fieldTypeMap.put(FieldConstants.SFIMAGE, "SFImage    ");
        fieldTypeMap.put(FieldConstants.MFIMAGE, "MFImage    ");
        fieldTypeMap.put(FieldConstants.SFTIME, "SFTime     ");
        fieldTypeMap.put(FieldConstants.MFTIME, "MFTime     ");
        fieldTypeMap.put(FieldConstants.SFNODE, "SFNode     ");
        fieldTypeMap.put(FieldConstants.MFNODE, "MFNode     ");
        fieldTypeMap.put(FieldConstants.SFSTRING, "SFString   ");
        fieldTypeMap.put(FieldConstants.MFSTRING, "MFString   ");
        fieldTypeMap.put(FieldConstants.SFROTATION, "SFRotation ");
        fieldTypeMap.put(FieldConstants.MFROTATION, "MFRotation ");
        fieldTypeMap.put(FieldConstants.SFCOLOR, "SFColor    ");
        fieldTypeMap.put(FieldConstants.MFCOLOR, "MFColor    ");
        fieldTypeMap.put(FieldConstants.SFCOLORRGBA, "SFColorRGBA");
        fieldTypeMap.put(FieldConstants.MFCOLORRGBA, "MFColorRGBA");
        fieldTypeMap.put(FieldConstants.SFMATRIX3F, "SFMatrix3f ");
        fieldTypeMap.put(FieldConstants.MFMATRIX3F, "MFMatrix3f ");
        fieldTypeMap.put(FieldConstants.SFMATRIX3D, "SFMatrix3d ");
        fieldTypeMap.put(FieldConstants.MFMATRIX3D, "MFMatrix3d ");
        fieldTypeMap.put(FieldConstants.SFMATRIX4F, "SFMatrix4f ");
        fieldTypeMap.put(FieldConstants.MFMATRIX4F, "MFMatrix4f ");
        fieldTypeMap.put(FieldConstants.SFMATRIX4D, "SFMatrix4d ");
        fieldTypeMap.put(FieldConstants.MFMATRIX4D, "MFMatrix4d ");

        protoMap = new HashMap<>();
    }

    /**
     * Set the map of node names to prototype decl. This will only have the top
     * level protos. Nested proto's will not have field type information for
     * encoding.
     *
     * @param protoMap The top level proto map
     */
    public void setProtoMap(Map<String, VRMLFieldDeclaration> protoMap) {
        this.protoMap = protoMap;

        protoCreator = new NRProtoCreator(nodeFactory,
                "",
                majorVersion,
                minorVersion);

        root =
                (VRMLWorldRootNodeType) nodeFactory.createVRMLNode("WorldRoot",
                false);

        CRVRMLScene scene = new CRVRMLScene(majorVersion, minorVersion);
        scene.setNodeFactory(nodeFactory);
        WriteableSceneMetaData metaData = new WriteableSceneMetaData("3.0",
                true,
                SceneMetaData.BINARY_ENCODING);

        scene.setMetaData(metaData);

        root.setContainedScene(scene);

    }

    public void writeFiltered(InputStream input, OutputStream output) {

        System.out.println("Code used****");
        try {

            try (OutputStream fos = output) {
                SAXDocumentSerializer serializer = new SAXDocumentSerializer();
                //  TODO: We'd like to catch most DEF name limits
                serializer.setAttributeValueMapMemoryLimit(32);
                serializer.setOutputStream(fos);
                SerializerVocabulary initialVocabulary = new SerializerVocabulary();
                // TODO: Does this do a copy?
                initialVocabulary.setExternalVocabulary(X3DBinaryConstants.EXTERNAL_VOCABULARY_URI_STRING,
                        X3DBinaryVocabulary.serializerVoc, false);
                serializer.setVocabulary(initialVocabulary);
                Map<String, EncodingAlgorithm> algorithms = new HashMap<>();
                algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
                algorithms.put(DeltazlibIntArrayAlgorithm.ALGORITHM_URI, new DeltazlibIntArrayAlgorithm());
                if (method == METHOD_SMALLEST_LOSSY) {
                    // Only global control available currently
                    algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                            new QuantizedzlibFloatArrayAlgorithm(PARAM_FLOAT_LOSSY));
                } else {
                    // Default is no loss
                    algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm());
                }   serializer.setRegisteredEncodingAlgorithms(algorithms);
                // Obtain an instance of an XMLReader implementation
                // from a system property
                XMLReader parser = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                parser.setFeature(VALIDATION_FEATURE_ID, false);
                X3DEntityResolver resolver = new X3DEntityResolver();
                parser.setEntityResolver(resolver);

                // Create a new instance and register it with the parser
                ContentHandler contentHandler = new X3DFilter(serializer);
                parser.setContentHandler(contentHandler);
                InputSource is = new InputSource(input);
                parser.parse(is);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace(System.err);
        }
    }

    public class X3DFilter implements ContentHandler {

        private ContentHandler parent;

        public X3DFilter(ContentHandler parent) {
            this.parent = parent;
        }

        @Override
        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {

            int len = atts.getLength();

            if (atts.getLength() > 0) {

                if (localName.equals("fieldValue")) {
                    AttributesHolder aholder = new AttributesHolder();

                    String fieldName = atts.getValue("name");
                    aholder.addAttribute(new QualifiedName("", "", "name",
                            "name"), fieldName);

//                System.out.println("Looking for: " + (String)elementStack.peek() + "." + fieldName);
                    VRMLFieldDeclaration decl = protoMap.get(elementStack.peek() + "." + fieldName);

                    if (decl != null) {
                        encodeField(decl.getFieldType(), "value", "value", atts.getValue("value"), aholder, null);
                    } else {
                        aholder.addAttribute(new QualifiedName("", "", "value",
                                "value"), atts.getValue("value"));
                    }

                    parent.startElement(namespaceURI, localName, qualifiedName, aholder);

                    elementStack.push("fieldValue");
                    return;
                }

                try {
                    VRMLNodeType node;

                    node = nodeCache.get(localName);

                    if (localName.equals("ProtoInstance")) {
                        parent.startElement(namespaceURI, localName, qualifiedName, atts);
                        elementStack.push(atts.getValue("name"));

                        return;
                    }

                    if (node == null) {
                        node = (VRMLNodeType) nodeFactory.createVRMLNode(localName, false);
                        nodeCache.put(localName, node);
                    }

                    AttributesHolder aholder = new AttributesHolder();
                    VRMLFieldDeclaration decl = null;
                    String attName;

                    boolean inMI = localName.equals("MetadataInteger");

                    for (int i = 0; i < len; i++) {
                        attName = atts.getLocalName(i);

                        int idx = node.getFieldIndex(attName);

                        if (inMI && attName.equals("value")) {
                            if (atts.getValue("name").equals("payload")) {
                                decl = new VRMLFieldDeclaration(FieldConstants.FIELD,
                                        "MFInt32", "value");
                                idx = 0;
                                int[] ival = fieldParser.MFInt32(atts.getValue(i));

                                aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.INT,
                                        ival);
                                continue;
                            }
                        }

                        if (idx > -1) {
                            decl = node.getFieldDeclaration(idx);
                        } else if (attName.equals("encoder")) {
                            decl = new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFInt32", "encoder");
                            idx = 0;
                        } else if (attName.equals("data")) {
                            decl = new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "MFInt32", "data");
                            idx = 0;
                            int[] ival = fieldParser.MFInt32(atts.getValue(i));

                            aholder.addAttributeWithAlgorithmData(
                                    new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                    null,
                                    EncodingAlgorithmIndexes.INT,
                                    ival);
                            continue;
                        }

                        VRMLFieldData field = null;
                        if (idx != -1) {
                            int accessType = decl.getAccessType();

                            if (accessType == FieldConstants.EVENTIN
                                    || accessType == FieldConstants.EVENTOUT) {

//                            System.out.println("Skiping: " + atts.getLocalName(i));
                                // Skip inputOnly and outputOnly DTD defaults
                                continue;
                            }

                            field = node.getFieldValue(idx);
                        }

//System.out.println("att: " + atts.getLocalName(i) + " idx: " + idx);
//System.out.println("att_val: " + atts.getValue(i) + ":");
                        if (idx > -1 && method != METHOD_STRINGS) {
                            encodeField(decl.getFieldType(), atts.getLocalName(i), atts.getQName(i), atts.getValue(i), aholder, field);
                        } else {
                            //System.out.println("Non X3D field: " + atts.getLocalName(i));
                            aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));

                        }
                    }
                    parent.startElement(namespaceURI, localName, qualifiedName, aholder);
                } catch (UnsupportedNodeException une) {
                    // Any unknown nodes should be passed along anyway
                    parent.startElement(namespaceURI, localName, qualifiedName, atts);
                } catch (SAXException | InvalidFieldTypeException | InvalidFieldFormatException | InvalidFieldException e) {
                    // Any unknown nodes should be passed along anyway
                    System.err.println("***Unhandled element: " + localName);
                    e.printStackTrace(System.err);
                }
            } else {
                parent.startElement(namespaceURI, localName, qualifiedName, atts);
            }

            elementStack.push(localName);
        }

        @Override
        public void endElement(String namespaceURI, String localName,
                String qualifiedName) throws SAXException {

            parent.endElement(namespaceURI, localName, qualifiedName);

            elementStack.pop();
        }

        // Methods that pass data along unchanged:
        @Override
        public void startDocument() throws SAXException {
            parent.startDocument();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            parent.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix)
                throws SAXException {
            parent.endPrefixMapping(prefix);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            parent.setDocumentLocator(locator);
        }

        @Override
        public void endDocument() throws SAXException {
            parent.endDocument();

            String sval;
            String sval2;

            if (removeDefaults) {
                System.out.println(defaults + " default fields removed");
            }
        }
        int chars = 0;

        @Override
        public void characters(char[] text, int start, int length)
                throws SAXException {

            chars += length;
            parent.characters(text, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] text, int start,
                int length) throws SAXException {

            // TODO: Should we get rid of this?
//System.out.println("iws: " + length);
            parent.ignorableWhitespace(text, start, length);
        }

        @Override
        public void processingInstruction(String target, String data)
                throws SAXException {
            parent.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name)
                throws SAXException {
            parent.skippedEntity(name);
        }
    }

// Local Methods
    public void encodeField(int fieldType, String name, String qName, String value, AttributesHolder aholder, VRMLFieldData field) {
        float f;
        boolean b;
        float[] fval;
        int[] ival;
        int k;
        double[] dval;
        int clen;

        fieldTypeStats[fieldType]++;
        fieldTypeOrigSize[fieldType] += value.length();

//System.out.println("encode: " + name + " qName: " + qName + " fieldType: " + fieldType + " value: " + value);
        switch (fieldType) {
            case FieldConstants.SFINT32:
                if (removeDefaults && field != null) {
                    k = fieldParser.SFInt32(value);
                    if (k == field.intValue) {
                        defaults++;
                        break;
                    }
                }

                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFTIME:
                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFLONG:
                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFDOUBLE:
                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFBOOL:
                if (removeDefaults && field != null) {
                    b = fieldParser.SFBool(value);
                    if (b == field.booleanValue) {
                        defaults++;
                        break;
                    }
                }

                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFFLOAT:
                if (removeDefaults && field != null) {
                    f = fieldParser.SFFloat(value);
                    if (Math.abs(f - field.floatValue) <= FLOAT_EPS) {
                        defaults++;
                        break;
                    }
                }

                // TODO: Should we use FLOATS, or string?
                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFSTRING:
                if (removeDefaults && field != null) {
                    if (value.equals(field.stringValue)) {
                        defaults++;
                        break;
                    }
                }

                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.MFSTRING:
                aholder.addAttribute(new QualifiedName("", "", name,
                        qName), value);
                break;
            case FieldConstants.SFROTATION:
                fval = fieldParser.SFRotation(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);

                    if (collectStats) {
                        clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);
                        fieldTypeNewSize[FieldConstants.SFROTATION] += clen;
                    }
                }
                break;
            case FieldConstants.MFROTATION:
                fval = fieldParser.MFRotation(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFCOLORRGBA:
                fval = fieldParser.SFColorRGBA(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                }
                break;
            case FieldConstants.MFCOLORRGBA:
                fval = fieldParser.MFColorRGBA(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFCOLOR:
                fval = fieldParser.SFColor(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                }
                break;
            case FieldConstants.MFCOLOR:
                fval = fieldParser.MFColor(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFVEC2D:
                dval = fieldParser.SFVec2d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.MFVEC2D:
                dval = fieldParser.MFVec2d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.SFVEC3D:
                dval = fieldParser.SFVec3d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.MFVEC3D:
                dval = fieldParser.MFVec3d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.SFVEC2F:
                fval = fieldParser.SFVec2f(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                }
                break;
            case FieldConstants.MFVEC2F:
                fval = fieldParser.MFVec2f(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFVEC3F:
                fval = fieldParser.SFVec3f(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                    if (collectStats) {
                        clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);
                        fieldTypeNewSize[FieldConstants.SFVEC3F] += clen;
                    }
                }
                break;
            case FieldConstants.MFVEC3F:
                fval = fieldParser.MFVec3f(value);

                encodeFloatArray(value, fval, name, qName, aholder, fieldType);

                break;
            case FieldConstants.SFVEC4F:
                fval = fieldParser.SFVec4f(value);
                if (fval != null && fval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                }
                break;
            case FieldConstants.MFVEC4F:
                fval = fieldParser.MFVec4f(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.MFFLOAT:
                fval = fieldParser.MFFloat(value);
                encodeFloatArray(value, fval, name, qName, aholder, FieldConstants.MFFLOAT);
                break;
            case FieldConstants.MFDOUBLE:
                dval = fieldParser.MFDouble(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                //encodeFloatArray(value, fval, name, qName, aholder, FieldConstants.MFFLOAT);
                break;
            case FieldConstants.SFMATRIX3F:
                fval = fieldParser.SFMatrix3f(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                if (fval != null && fval.length != 0) {
                    switch (method) {
                        case METHOD_SMALLEST_LOSSY:
                            aholder.addAttributeWithAlgorithmData(
                                    new QualifiedName("", "", name, qName),
                                    QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                                    X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                                    fval);
                            break;
                        default:
                            aholder.addAttributeWithAlgorithmData(
                                    new QualifiedName("", "", name, qName),
                                    null,
                                    EncodingAlgorithmIndexes.FLOAT,
                                    fval);
                            break;
                    }
                }
                break;
            case FieldConstants.MFMATRIX3F:
                fval = fieldParser.MFMatrix3f(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFMATRIX4F:
                fval = fieldParser.SFMatrix4f(value);

                if (removeDefaults && field != null) {
                    if (fval.length == field.floatArrayValues.length) {

                        boolean equal = true;

                        for (int j = 0; j < fval.length; j++) {

                            if (Math.abs(fval[j] - field.floatArrayValues[j]) > FLOAT_EPS) {
                                equal = false;

                                break;
                            }
                        }

                        if (equal) {
                            defaults++;
                            break;
                        }
                    }
                }

                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.MFMATRIX4F:
                fval = fieldParser.MFMatrix4f(value);
                encodeFloatArray(value, fval, name, qName, aholder, fieldType);
                break;
            case FieldConstants.SFMATRIX3D:
                dval = fieldParser.SFMatrix3d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.MFTIME:
                dval = fieldParser.MFTime(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.MFMATRIX3D:
                dval = fieldParser.MFMatrix3d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.SFMATRIX4D:
                dval = fieldParser.SFMatrix4d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.MFMATRIX4D:
                dval = fieldParser.MFMatrix4d(value);
                if (dval != null && dval.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            dval);
                }
                break;
            case FieldConstants.SFIMAGE:
                ival = fieldParser.SFImage(value);
                if (ival != null && ival.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.INT,
                            ival);
                }
                break;
            case FieldConstants.MFIMAGE:
                ival = fieldParser.MFImage(value);
                if (ival != null && ival.length != 0) {
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", name, qName),
                            null,
                            EncodingAlgorithmIndexes.INT,
                            ival);
                }
                break;
            case FieldConstants.MFINT32:
                ival = fieldParser.MFInt32(value);

                System.out.println("BinarySerializer " + name + " " + java.util.Arrays.toString(ival));

                aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", name, qName),
                        DeltazlibIntArrayAlgorithm.ALGORITHM_URI,
                        X3DBinaryConstants.DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID,
                        ival);

                break;
            default:
                System.out.println("Unhandled field: " + name);

        }
    }

    /**
     * Encode float array data. This is optimized for MF* types, not types like
     * SFVec3f.
     *
     * @param attVal The field value
     * @param fval The parsed float array value
     * @param atts The current attributes array
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    private void encodeFloatArray(String attVal, float[] fval, String localName, String qName, AttributesHolder aholder,
            int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            switch (method) {
                case METHOD_SMALLEST_LOSSY:
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", localName, qName),
                            QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                            X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                            fval);
                    break;
                case METHOD_SMALLEST_NONLOSSY:
                    aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", localName, qName),
                            QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                            X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                            fval);
                    break;
                default:
                    clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    if (clen <= attVal.length()) {
                        aholder.addAttributeWithAlgorithmData(
                                new QualifiedName("", "", localName, qName),
                                null,
                                EncodingAlgorithmIndexes.FLOAT,
                                fval);
                        if (collectStats) {
                            fieldTypeNewSize[ftype] += clen;
                        }
                    } else {
                        aholder.addAttribute(new QualifiedName("", "", localName,
                                qName), attVal);
                        if (collectStats) {
                            fieldTypeNewSize[ftype] += attVal.length();
                        }
                    }
                    break;
            }
        }
    }
}
