/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import com.sun.xml.internal.fastinfoset.QualifiedName;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithmFactory;
import com.sun.xml.internal.fastinfoset.sax.AttributesHolder;
import com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer;
import com.sun.xml.internal.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.parsers.*;
import javax.xml.transform.sax.SAXResult;


import org.xml.sax.*;

import org.web3d.vrml.lang.*;
import org.web3d.vrml.renderer.DefaultNodeFactory;
import org.web3d.parser.x3d.*;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.x3d.jaxp.X3DEntityResolver;

/**
 *  Serializes an X3D XML encoded document into an X3D binary document.
 *
 * TODO: Stats collection for non builtins isn't written
 * TODO: proto and script fields are encoded as strings
 * TODO: script cdata doesn't work
 *
 * @author Alan Hudson
 * @version
 */
public class X3DSerializer {
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
    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    protected static final int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;
    protected static final String EXTERNAL_VOCABULARY_URI_STRING = "urn:external-vocabulary";

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
    private HashMap nodeCache;

    /** A field parser */
    private X3DFieldReader fieldParser;

    private int[] fieldTypeStats;
    private int[] fieldTypeOrigSize;
    private int[] fieldTypeNewSize;
    private HashMap fieldTypeMap;
    private boolean collectStats = true;

    /** What method should we use to compress */
    private int method;

    /** Should we remove defaults */
    private boolean removeDefaults;

    /** How many default values where removed */
    private int defaults;

    /**
     * Creates a new instance of X3DSerializer
     *
     * @param compMethod What
     * @param rd
     */
    public X3DSerializer(int compMethod, boolean rd) {
        method = compMethod;
        removeDefaults = rd;

        try {
            // get a transformer and document builder
            _transformer = TransformerFactory.newInstance().newTransformer();
            _docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace(System.err);
        }

        nodeFactory = DefaultNodeFactory.newInstance(DefaultNodeFactory.NULL_RENDERER);
        nodeFactory.setSpecVersion(3,1);
        nodeFactory.setProfile("Immersive");

        nodeCache = new HashMap();

        fieldParser = new X3DFieldReader();
        fieldParser.setCaseSensitive(false);

        fieldTypeStats = new int[43];
        fieldTypeOrigSize = new int[43];
        fieldTypeNewSize = new int[43];
        fieldTypeMap = new HashMap();
        fieldTypeMap.put(FieldConstants.SFINT32,     "SFInt32    ");
        fieldTypeMap.put(FieldConstants.MFINT32,     "MFInt32    ");
        fieldTypeMap.put(FieldConstants.SFFLOAT,     "SFFloat    ");
        fieldTypeMap.put(FieldConstants.MFFLOAT,     "MFFloat    ");
        fieldTypeMap.put(FieldConstants.SFDOUBLE,    "SFDouble   ");
        fieldTypeMap.put(FieldConstants.MFDOUBLE,    "MFDouble   ");
        fieldTypeMap.put(FieldConstants.SFLONG,      "SFLong     ");
        fieldTypeMap.put(FieldConstants.MFLONG,      "MFLong     ");
        fieldTypeMap.put(FieldConstants.SFBOOL,      "SFBool     ");
        fieldTypeMap.put(FieldConstants.MFBOOL,      "MFBool     ");
        fieldTypeMap.put(FieldConstants.SFVEC2F,     "SFVec2f    ");
        fieldTypeMap.put(FieldConstants.MFVEC2F,     "MFVec2f    ");
        fieldTypeMap.put(FieldConstants.SFVEC2D,     "SFVec2d    ");
        fieldTypeMap.put(FieldConstants.MFVEC2D,     "MFVec2d    ");
        fieldTypeMap.put(FieldConstants.SFVEC3F,     "SFVec3f    ");
        fieldTypeMap.put(FieldConstants.MFVEC3F,     "MFVec3f    ");
        fieldTypeMap.put(FieldConstants.SFVEC3D,     "SFVec3d    ");
        fieldTypeMap.put(FieldConstants.MFVEC3D,     "MFVec3d    ");
        fieldTypeMap.put(FieldConstants.SFVEC4F,     "SFVec4f    ");
        fieldTypeMap.put(FieldConstants.MFVEC4F,     "MFVec4f    ");
        fieldTypeMap.put(FieldConstants.SFVEC4D,     "SFVec4d    ");
        fieldTypeMap.put(FieldConstants.MFVEC4D,     "MFVec4d    ");
        fieldTypeMap.put(FieldConstants.SFIMAGE,     "SFImage    ");
        fieldTypeMap.put(FieldConstants.MFIMAGE,     "MFImage    ");
        fieldTypeMap.put(FieldConstants.SFTIME,      "SFTime     ");
        fieldTypeMap.put(FieldConstants.MFTIME,      "MFTime     ");
        fieldTypeMap.put(FieldConstants.SFNODE,      "SFNode     ");
        fieldTypeMap.put(FieldConstants.MFNODE,      "MFNode     ");
        fieldTypeMap.put(FieldConstants.SFSTRING,    "SFString   ");
        fieldTypeMap.put(FieldConstants.MFSTRING,    "MFString   ");
        fieldTypeMap.put(FieldConstants.SFROTATION,  "SFRotation ");
        fieldTypeMap.put(FieldConstants.MFROTATION,  "MFRotation ");
        fieldTypeMap.put(FieldConstants.SFCOLOR,     "SFColor    ");
        fieldTypeMap.put(FieldConstants.MFCOLOR,     "MFColor    ");
        fieldTypeMap.put(FieldConstants.SFCOLORRGBA, "SFColorRGBA");
        fieldTypeMap.put(FieldConstants.MFCOLORRGBA, "MFColorRGBA");
        fieldTypeMap.put(FieldConstants.SFMATRIX3F,  "SFMatrix3f ");
        fieldTypeMap.put(FieldConstants.MFMATRIX3F,  "MFMatrix3f ");
        fieldTypeMap.put(FieldConstants.SFMATRIX3D,  "SFMatrix3d ");
        fieldTypeMap.put(FieldConstants.MFMATRIX3D,  "MFMatrix3d ");
        fieldTypeMap.put(FieldConstants.SFMATRIX4F,  "SFMatrix4f ");
        fieldTypeMap.put(FieldConstants.MFMATRIX4F,  "MFMatrix4f ");
        fieldTypeMap.put(FieldConstants.SFMATRIX4D,  "SFMatrix4d ");
        fieldTypeMap.put(FieldConstants.MFMATRIX4D,  "MFMatrix4d ");
    }


    public void writeFiltered(File input, File output) {

        try {

            FileInputStream fis;
            try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(output))) {
                SAXDocumentSerializer serializer = new SAXDocumentSerializer();
                serializer.setOutputStream(fos);
                SerializerVocabulary externalVocabulary = new SerializerVocabulary();
                externalVocabulary.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
                // Replace with encoder://web3d.org/QuantizedFloatArrayEncoder  to align with Spec
                externalVocabulary.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
                externalVocabulary.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);
                SerializerVocabulary initialVocabulary = new SerializerVocabulary();
                initialVocabulary.setExternalVocabulary(
                        EXTERNAL_VOCABULARY_URI_STRING,
                        externalVocabulary, false);
                serializer.setVocabulary(initialVocabulary);
                Map algorithms = new HashMap();
                algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
                algorithms.put(DeltazlibIntArrayAlgorithm.ALGORITHM_URI, new DeltazlibIntArrayAlgorithm());
                if (method == METHOD_SMALLEST_LOSSY) {
                    // Only global control available currently
                    algorithms.put(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI,
                            new QuantizedzlibFloatArrayAlgorithm2(PARAM_FLOAT_LOSSY));
                } else {
                    // Default is no loss
                    algorithms.put(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm2());
                }   serializer.setRegisteredEncodingAlgorithms(algorithms);
                // Obtain an instance of an XMLReader implementation
                // from a system property
                XMLReader
                        parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                parser.setFeature(VALIDATION_FEATURE_ID, false);
                X3DEntityResolver resolver = new X3DEntityResolver();
                parser.setEntityResolver(resolver);
                //parser.setFeature(LOAD_DTD_ID, false);

                //parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, false);

                // Create a new instance and register it with the parser
                ContentHandler contentHandler = new X3DFilter(serializer);
                parser.setContentHandler(contentHandler);
                fis = new FileInputStream(input);
                InputSource is = new InputSource(fis);
                parser.parse(is);
            }
            fis.close();
        } catch(IOException | SAXException e) {
            e.printStackTrace(System.err);
        }
    }


    /** Starts the sample
     * @param args XML input file name and FI output document name
     */
    public static void main(String[] args) {
        try {
            int pnum = 0;
            int method = X3DSerializer.METHOD_SMALLEST_NONLOSSY;
            boolean removeDefaults = true;

            if (args.length < 2) {
                System.out.println(X3DSerializer.USAGE);
                return;
            }

            for(int i=0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    switch (args[i]) {
                        case "-fastest":
                            System.out.println("Fasting parsing method");
                            method = X3DSerializer.METHOD_FASTEST_PARSING;
                            break;
                        case "-smallest":
                            System.out.println("Smallest parsing method");
                            method = X3DSerializer.METHOD_SMALLEST_NONLOSSY;
                            break;
                        case "-lossy":
                            System.out.println("Lossy parsing method");
                            method = X3DSerializer.METHOD_SMALLEST_LOSSY;
                            break;
                        case "-strings":
                            System.out.println("Strings method");
                            method = X3DSerializer.METHOD_STRINGS;
                            break;
                        case "-savedefaults":
                            removeDefaults = false;
                            break;
                        case "-quantizeParam":
                            pnum++;
                            i++;
                            String st = args[i];
                            PARAM_FLOAT_LOSSY = Float.parseFloat(st);
                            break;
                        default:
                            System.out.println("Unknown option: " + args[i]);
                            break;
                    }

                    pnum++;
                } else
                    break;
            }

            if (pnum + 2 < args.length) {
                System.out.println(X3DSerializer.USAGE);
                return;
            }

            File input = new File(args[pnum++]);
            File ouput = new File(args[pnum++]);
            X3DSerializer docSerializer = new X3DSerializer(method, removeDefaults);
            docSerializer.writeFiltered(input, ouput);
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }


public class X3DFilter implements ContentHandler {

    private ContentHandler parent;

    public X3DFilter(ContentHandler parent) {
      System.out.println("Parent: " + parent);

      this.parent = parent;
    }

    @Override
    public void startElement(String namespaceURI, String localName,
      String qualifiedName, Attributes atts) throws SAXException {

        int len = atts.getLength();

//System.out.println("SE: " + localName + " atts: " + atts.getLength());
        if (atts.getLength() > 0) {

            try {
                VRMLNodeType node;

                node = (VRMLNodeType) nodeCache.get(localName);

                if (node == null) {
                    node = (VRMLNodeType) nodeFactory.createVRMLNode(localName, false);
                    nodeCache.put(localName, node);
                }

                AttributesHolder aholder = new AttributesHolder();

                for(int i=0; i < len; i++) {

                    int idx = node.getFieldIndex(atts.getLocalName(i));

                    VRMLFieldData field = null;
                    if (idx != -1) {
                        VRMLFieldDeclaration fdecl = node.getFieldDeclaration(idx);

                        int accessType = fdecl.getAccessType();

                        if (accessType == FieldConstants.EVENTIN ||
                            accessType == FieldConstants.EVENTOUT) {

//                            System.out.println("Skiping: " + atts.getLocalName(i));
                            // Skip inputOnly and outputOnly DTD defaults
                            continue;
                        }

                        field = node.getFieldValue(idx);
                    }

//System.out.println("att: " + atts.getLocalName(i) + " idx: " + idx);
//System.out.println("att_val: " + atts.getValue(i) + ":");
                    if (idx > -1 && method != METHOD_STRINGS) {

                        VRMLFieldDeclaration decl = node.getFieldDeclaration(idx);

                        float f;
                        boolean b;
                        float[] fval;
                        int[] ival;
                        int k;
                        double[] dval;
                        String attVal;
                        int clen;

                        attVal = atts.getValue(i);
                        fieldTypeStats[decl.getFieldType()]++;
                        fieldTypeOrigSize[decl.getFieldType()] += attVal.length();


                        switch(decl.getFieldType()) {
                            case FieldConstants.SFINT32:
                                if (removeDefaults && field != null) {
                                    k = fieldParser.SFInt32(atts.getValue(i));
                                    if (k == field.intValue) {
                                        defaults++;
                                        break;
                                    }
                                }

                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFTIME:
                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFLONG:
                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFDOUBLE:
                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFBOOL:
                                if (removeDefaults && field != null) {
                                    b = fieldParser.SFBool(atts.getValue(i));
                                    if (b == field.booleanValue) {
                                        defaults++;
                                        break;
                                    }
                                }

                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFFLOAT:
                                if (removeDefaults && field != null) {
                                    f = fieldParser.SFFloat(atts.getValue(i));
                                    if (Math.abs(f - field.floatValue) <= FLOAT_EPS) {
                                        defaults++;
                                        break;
                                    }
                                }

                                // TODO: Should we use FLOATS, or string?
                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFSTRING:
                                if (removeDefaults && field != null) {
                                    if (atts.getValue(i).equals(field.stringValue)) {
                                        defaults++;
                                        break;
                                    }
                                }

                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.MFSTRING:
                                aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                                    atts.getQName(i)), atts.getValue(i));
                                break;
                            case FieldConstants.SFROTATION:
                                fval = fieldParser.SFRotation(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
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
                                fval = fieldParser.MFRotation(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFCOLORRGBA:
                                fval = fieldParser.SFColorRGBA(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.FLOAT,
                                        fval);
                                }
                                break;
                            case FieldConstants.MFCOLORRGBA:
                                fval = fieldParser.MFColorRGBA(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFCOLOR:
                                fval = fieldParser.SFColor(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.FLOAT,
                                        fval);
                                }
                                break;
                            case FieldConstants.MFCOLOR:
                                fval = fieldParser.MFColor(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFVEC2D:
                                dval = fieldParser.SFVec2d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.MFVEC2D:
                                dval = fieldParser.MFVec2d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.SFVEC3D:
                                dval = fieldParser.SFVec3d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.MFVEC3D:
                                dval = fieldParser.MFVec3d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.SFVEC2F:
                                fval = fieldParser.SFVec2f(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.FLOAT,
                                        fval);
                                }
                                break;
                            case FieldConstants.MFVEC2F:
                                fval = fieldParser.MFVec2f(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFVEC3F:
                                fval = fieldParser.SFVec3f(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
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
                                fval = fieldParser.MFVec3f(attVal);

                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());

                                break;
                            case FieldConstants.SFVEC4F:
                                fval = fieldParser.SFVec4f(atts.getValue(i));
                                if (fval != null && fval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.FLOAT,
                                        fval);
                                }
                                break;
                            case FieldConstants.MFVEC4F:
                                fval = fieldParser.MFVec4f(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.MFFLOAT:
                                fval = fieldParser.MFFloat(atts.getValue(i));

                                encodeFloatArray(attVal, fval, atts, aholder, i, FieldConstants.MFFLOAT);
                                break;
                            case FieldConstants.SFMATRIX3F:
                                fval = fieldParser.SFMatrix3f(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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
                                    switch(method) {
                                        case METHOD_SMALLEST_LOSSY:
                                            aholder.addAttributeWithAlgorithmData(
                                                new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                                QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                                                QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                                                fval);
                                                break;
                                        default:
                                            aholder.addAttributeWithAlgorithmData(
                                                new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                                null,
                                                EncodingAlgorithmIndexes.FLOAT,
                                                fval);
                                                break;
                                    }
                                }
                                break;
                            case FieldConstants.MFMATRIX3F:
                                fval = fieldParser.MFMatrix3f(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFMATRIX4F:
                                fval = fieldParser.SFMatrix4f(atts.getValue(i));

                                if (removeDefaults && field != null) {
                                    if (fval.length == field.floatArrayValues.length) {

                                        boolean equal = true;

                                        for(int j=0; j < fval.length; j++) {

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

                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.MFMATRIX4F:
                                fval = fieldParser.MFMatrix4f(atts.getValue(i));
                                encodeFloatArray(attVal, fval, atts, aholder, i, decl.getFieldType());
                                break;
                            case FieldConstants.SFMATRIX3D:
                                dval = fieldParser.SFMatrix3d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.MFTIME:
                                dval = fieldParser.MFTime(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.MFMATRIX3D:
                                dval = fieldParser.MFMatrix3d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.SFMATRIX4D:
                                dval = fieldParser.SFMatrix4d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.MFMATRIX4D:
                                dval = fieldParser.MFMatrix4d(atts.getValue(i));
                                if (dval != null && dval.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.DOUBLE,
                                        dval);
                                }
                                break;
                            case FieldConstants.SFIMAGE:
                                ival = fieldParser.SFImage(atts.getValue(i));
                                if (ival != null && ival.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.INT,
                                        ival);
                                }
                                break;
                            case FieldConstants.MFIMAGE:
                                ival = fieldParser.MFImage(atts.getValue(i));
                                if (ival != null && ival.length != 0) {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.INT,
                                        ival);
                                }
                                break;
                            case FieldConstants.MFINT32:
                                ival = fieldParser.MFInt32(atts.getValue(i));

                                aholder.addAttributeWithAlgorithmData(
                                    new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                    DeltazlibIntArrayAlgorithm.ALGORITHM_URI,
                                    DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID,
                                    ival);

/*

                                boolean byteData = true;
                                boolean shortData = true;
//                                boolean byteData = false;
//                                boolean shortData = false;

                                int ilen = ival.length;

                                for(int j=0; j < ilen; j++) {
                                    if (ival[j] < Byte.MIN_VALUE || ival[j] > Byte.MAX_VALUE) {
                                        byteData = false;
                                    }

                                    if (ival[j] < Short.MIN_VALUE || ival[j] > Short.MAX_VALUE) {
                                        shortData = false;
                                    }
//System.out.println(j + " Checking: " + ival[j] + " byte: " + byteData + " short: " + shortData + " bigger? " + (ival[j] > Short.MAX_VALUE));

                                }
//    System.out.println("int array: byte: " + byteData + " short: " + shortData);
                                if (byteData) {
                                    byte bval[] = new byte[ilen];

System.out.println("Downcast values:");
                                    for(int j=0; j < ilen; j++) {
                                        bval[j] = (byte) ival[j];
System.out.println("old val: " + ival[j] + " new val: " + bval[j]);
                                    }

                                    if (ilen > 0) {
System.out.println("Down casted to byte");
                                        aholder.addAttributeWithAlgorithmData(
                                            new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                            null,
                                            BYTE_ALGORITHM_ID,
                                            bval);
                                    }
                                } else if (shortData) {
                                    short sval[] = new short[ilen];

                                    for(int j=0; j < ilen; j++) {
                                        sval[j] = (short) ival[j];
                                    }

                                    if (ilen > 0) {
System.out.println("Down casted to short");
                                        aholder.addAttributeWithAlgorithmData(
                                            new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                            null,
                                            EncodingAlgorithmIndexes.SHORT,
                                            sval);
                                    }
                                } else {
                                    aholder.addAttributeWithAlgorithmData(
                                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                                        null,
                                        EncodingAlgorithmIndexes.INT,
                                        ival);
                                }
*/
                                break;
                            default:
                                System.out.println("Unhandled field: " + atts.getLocalName(i));

                        }
                    } else {
                        //System.out.println("Non X3D field: " + atts.getLocalName(i));
                        aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), atts.getValue(i));

                    }
                }
                parent.startElement(namespaceURI, localName, qualifiedName, aholder);
            } catch(UnsupportedNodeException une) {
                // Any unknown nodes should be passed along anyway
                parent.startElement(namespaceURI, localName, qualifiedName, atts);
            } catch(InvalidFieldException | InvalidFieldFormatException | SAXException e) {
                // Any unknown nodes should be passed along anyway
                System.err.println("***Unhandled element: " + localName);
                e.printStackTrace(System.err);
            }
        } else {
            parent.startElement(namespaceURI, localName, qualifiedName, atts);
        }
    }

    @Override
  public void endElement(String namespaceURI, String localName,
   String qualifiedName) throws SAXException {

      parent.endElement(namespaceURI, localName, qualifiedName);
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

        if (removeDefaults)
            System.out.println(defaults + " default fields removed");

        for(int i=1; i < fieldTypeStats.length; i++) {
            sval = Integer.toString(fieldTypeStats[i]);

            if (sval.length() < 8) {
                int pad = 8 - sval.length();
                for(int j=0; j < pad; j++) {
                    sval = sval + " ";
                }
            }

            sval2 = Integer.toString(fieldTypeOrigSize[i]);

            if (sval2.length() < 8) {
                int pad = 8 - sval2.length();
                for(int j=0; j < pad; j++) {
                    sval2 = sval2 + " ";
                }
            }

            System.out.println(fieldTypeMap.get(i) + ": " + sval + " orig: " + sval2 + " new: " + fieldTypeNewSize[i]);
        }
  }

int chars = 0;
    @Override
  public void characters(char[] text, int start, int length)
   throws SAXException {

   // TODO: Can we ignore some of this?
//System.out.println("chars: " + length + " tot: " + chars);
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

/**
 * Encode float array data.  This is optimized for MF* types, not types like SFVec3f.
 *
 * @param attVal The field value
 * @param fval The parsed float array value
 * @param atts The current attributes array
 * @param aholder The current attributes holder
 * @param i The attribute index being processed
 * @param ftype The X3D field type, defined in FieldConstants
 */
private void encodeFloatArray(String attVal, float[] fval, Attributes atts, AttributesHolder aholder,
    int i, int ftype) {

    int clen;

    if (fval != null && fval.length != 0) {
        switch(method) {
            case METHOD_SMALLEST_LOSSY:
                aholder.addAttributeWithAlgorithmData(
                    new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                    QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                    QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                    fval);
                break;
            case METHOD_SMALLEST_NONLOSSY:
                aholder.addAttributeWithAlgorithmData(
                    new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                    QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                    QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                    fval);
                break;
            default:
                clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                if (clen <= attVal.length()) {
                    aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                        null,
                        EncodingAlgorithmIndexes.FLOAT,
                        fval);
                    if (collectStats) {
                        fieldTypeNewSize[ftype] += clen;
                    }
                } else {
                    aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                        atts.getQName(i)), attVal);
                    if (collectStats) {
                        fieldTypeNewSize[ftype] += attVal.length();
                    }
                }
                break;
        }
    }
}
}


/*
   Saved code for type finding

                // Look for float types
                String data = atts.getValue(i).trim();

                int dpos = data.indexOf(".");

                String expStr = null;
                String mantStr = null;
                boolean list = false;
                boolean floatData = true;
                boolean anyFloats = false;
                boolean intData = true;
                boolean shortData = true;
                boolean byteData = true;
                boolean booleanData = true;
                boolean stringData = false;

                float ftmp;
                int itmp;
                boolean btmp;

                // Determine if this is a list.  Look for multiple seperators in the first 50 characters.
                int dlen = data.length();
                String small = data.substring(0,dlen > 50 ? 50 : dlen);

                StringTokenizer stok = new StringTokenizer(small);
                String tok;

                if (stok.countTokens() > 1)
                    list = true;

                if (list) {
System.out.println(atts.getLocalName(i) + " is a list");
                    stok = new StringTokenizer(data);
                    int tokCnt = 0;
                    int cnt = 0;

                    while(stok.hasMoreTokens()) {
                        tok = stok.nextToken();

                        if (floatData) {
                            try {
                                ftmp = Float.parseFloat(tok);
                            } catch(Exception e) {
                                floatData = false;
                            }
                        }

                        if (intData) {
                            try {
                                itmp = Integer.parseInt(tok);

                                if (itmp < Byte.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                    byteData = false;
                                } else if (itmp < Short.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                    shortData = false;
                                }
                            } catch(Exception e2) {
                                intData = false;
                                shortData = false;
                                byteData = false;
                            }
                        }

                        if (booleanData) {
                            if (!tok.equalsIgnoreCase("true") || !tok.equalsIgnoreCase("false"))
                                booleanData = false;
                        }

                        if (!floatData && !intData && !booleanData) {
                            stringData = true;
                            break;
                        }

                        tokCnt++;
                    }

                    if (stringData) {
System.out.println(atts.getLocalName(i) + " is String");
                        aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), atts.getValue(i));
                    } else if (shortData) {
System.out.println(atts.getLocalName(i) + " is Short list");
                        stok = new StringTokenizer(data);
                        short[] sval = new short[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                sval[cnt++] = Short.parseShort(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting short?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.SHORT, sval);
                    } else if (intData) {
System.out.println(atts.getLocalName(i) + " is Int list");
                        stok = new StringTokenizer(data);
                        int[] ival = new int[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                ival[cnt++] = Integer.parseInt(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting integer?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.INT, ival);

                    } else if (floatData) {
System.out.println(atts.getLocalName(i) + " is Float list");
                        stok = new StringTokenizer(data);
                        float[] fval = new float[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                fval[cnt++] = Float.parseFloat(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting float?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.FLOAT, fval);
                    } else if (booleanData) {
System.out.println(atts.getLocalName(i) + " is boolean list");
                        stok = new StringTokenizer(data);
                        boolean[] bval = new boolean[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            bval[cnt++] = tok.equalsIgnoreCase("true");
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.BOOLEAN, bval);
                    }

                } else {
                    // Store all singleton values as String right now, till we can work out
                    // the right setValue call on decode
                    aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                        atts.getQName(i)), atts.getValue(i));
                }
            }
*/
