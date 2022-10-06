/****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export;

// External imports
import com.sun.xml.fastinfoset.sax.SAXDocumentSerializer;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.fastinfoset.sax.AttributesHolder;
import com.sun.xml.fastinfoset.algorithm.*;

import org.jvnet.fastinfoset.EncodingAlgorithm;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

import java.io.*;
import java.util.*;
import java.util.zip.Deflater;

// Local imports
import org.web3d.parser.x3d.*;
import org.web3d.parser.x3d.X3DBinaryConstants;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

/**
 * X3D binary exporter using a retained Scenegraph.
 * This will directly write to X3D binary instead of
 * reusing the XML code to generate an XML file in memory.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class X3DBinaryRetainedDirectExporter extends X3DRetainedSAXExporter
    implements SceneGraphTraversalSimpleObserver {

    // Smallest float difference for equality
    private static final float FLOAT_EPS = 0.0000009f;

    // Largest acceptable error for float quantization
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    /** Default minimum length of an array to use QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM */
    private static int PARAM_FLOAT_ARRAY_MIN_SIZE = 7;

    /** The output stream to write to */
    private OutputStream os;

    /** Minimum length of an array to use QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM */
    private int minFloatArraySizeToEncode;

    /** The uri to use for quantized zlib float array encoding */
    private String qzfaaURI;

    /** The uri to use for quantized zlib float array encoding */
    private int qzfaaID;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param compressionMethod The strategy for choosing compressors
     * @param quantizeParam The maximum acceptable error for quantization
     */
    public X3DBinaryRetainedDirectExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam) {

        this(os,major,minor,errorReporter,compressionMethod, quantizeParam, false);
    }

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param compressionMethod The strategy for choosing compressors
     * @param quantizeParam The maximum acceptable error for quantization
     * @param oldMethod Use the old method before ISO finalization
     */
    public X3DBinaryRetainedDirectExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam, boolean oldMethod) {


        super(major, minor, errorReporter, compressionMethod, quantizeParam);

        this.os = os;
        binary = true;
        minFloatArraySizeToEncode = PARAM_FLOAT_ARRAY_MIN_SIZE;

/*      Node Compressors don't work yet
        if (compressionMethod == X3DBinarySerializer.METHOD_SMALLEST_LOSSY) {
            useNC = true;
        }
*/

        encodingTo = ".x3db";
        printDocType = false;

        stripWhitespace = false;

        // Setup Fast InfoSet

        SAXDocumentSerializer serializer = new SAXDocumentSerializer();

        //  TODO: We'd like to catch most DEF name limits
        serializer.setAttributeValueMapMemoryLimit(32);
        serializer.setOutputStream(os);

        SerializerVocabulary initialVocabulary = new SerializerVocabulary();

        if (oldMethod) {
            // TODO: Seems this does a removeEntry which can cause a multithreaded issue.
            // Might have to clone the serializerVoc.  yuck!
            initialVocabulary.setExternalVocabulary(X3DBinaryConstants.EXTERNAL_VOCABULARY_URI_STRING_OLD,
                    X3DBinaryVocabulary.serializerVoc, false);
        } else {
            initialVocabulary.setExternalVocabulary(X3DBinaryConstants.EXTERNAL_VOCABULARY_URI_STRING,
                    X3DBinaryVocabulary.serializerVoc, false);
        }

        serializer.setVocabulary(initialVocabulary);

        Map<String, EncodingAlgorithm> algorithms = new HashMap<>();
        algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
        if (compressionMethod == METHOD_FASTEST_PARSING ) {
            algorithms.put(
                DeltazlibIntArrayAlgorithm.ALGORITHM_URI,
                new DeltazlibIntArrayAlgorithm(Deflater.BEST_SPEED));
        } else {
            algorithms.put(
                DeltazlibIntArrayAlgorithm.ALGORITHM_URI,
                new DeltazlibIntArrayAlgorithm());
        }

        if (oldMethod) {
            System.out.println("Using old binary methods");

            qzfaaURI = QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI;
            qzfaaID = X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID;
        } else {
            qzfaaURI = QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI;
            qzfaaID = X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID2;
        }

        if (compressionMethod == METHOD_SMALLEST_LOSSY) {
            // Only global control available currently

            if (oldMethod) {
            algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                new QuantizedzlibFloatArrayAlgorithm(PARAM_FLOAT_LOSSY));
            } else {
                algorithms.put(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm2(PARAM_FLOAT_LOSSY));

                algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                    new QuantizedzlibFloatArrayAlgorithm(PARAM_FLOAT_LOSSY));
            }
        } else {
            if (oldMethod) {
                // Default is no loss
                algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                    new QuantizedzlibFloatArrayAlgorithm());
            } else {
                // Default is no loss
                algorithms.put(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm2());
                algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm());
            }
        }
        serializer.setRegisteredEncodingAlgorithms(algorithms);

        handler = serializer;
    }

    /**
     * Set the minimum float length array to using deltazlib encoding.
     * Under a certain length the algorithm increases size.  This could
     * be calculated but the runtime cost is pretty high.
     * @param len
     */
    public void setMinFloatArraySizeToEncode(int len) {
        minFloatArraySizeToEncode = len;
    }

    /**
     * Encode double array data.
     * This base version will just use a string rep.
     *
     * @param fval The parsed double array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    @Override
    protected void encodeDoubleArray(double[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            switch(compressionMethod) {
                case METHOD_STRINGS:
                    String st = createX3DString(decl, fval, numElements);

                    aholder.addAttribute(new QualifiedName("", "",
                        qName), st);
                    break;
                case METHOD_SMALLEST_LOSSY:
                case METHOD_SMALLEST_NONLOSSY:
                default:
                    clen = BuiltInEncodingAlgorithmFactory.doubleEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    String st2 = createX3DString(decl, fval, numElements);

                    if (clen <= st2.length()) {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            fval);
                    } else {
                        aholder.addAttribute(new QualifiedName("", "",
                            qName), st2);
                    }
                    break;
            }
        }
    }

    /**
     * Encode float array data.
     * This base version will just use a string rep
     *
     * @param fval The parsed float array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    @Override
    protected void encodeFloatArray(float[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            switch(compressionMethod) {
                case METHOD_SMALLEST_LOSSY:

                    aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", qName),
                        qzfaaURI,
                        qzfaaID,
                        fval);
                    break;
                case METHOD_SMALLEST_NONLOSSY:
                    // This compressor doesn't work well with small number of floats
                    // of wide range.  Would suggest avoiding with <= 6 floats as the
                    // fixed cost isn't worth it.  Could force a calc of the
                    // actual size but then we'd end up compressing twice.
                    if (fval.length >= minFloatArraySizeToEncode) {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            qzfaaURI,
                            qzfaaID,
                            fval);
                    } else {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                    }

                    break;
                case METHOD_STRINGS:
                    String st = createX3DString(decl, fval, numElements);

                    aholder.addAttribute(new QualifiedName("", "",
                        qName), st);
                    break;

                case METHOD_FASTEST_PARSING:
                    clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", qName),
                        null,
                        EncodingAlgorithmIndexes.FLOAT,
                        fval);
                    break;

                default:
                    clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    String st2 = createX3DString(decl, fval, numElements);

                    if (clen <= st2.length()) {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                    } else {
                        aholder.addAttribute(new QualifiedName("", "",
                            qName), st2);
                    }
                    break;
            }
        }
    }
}
