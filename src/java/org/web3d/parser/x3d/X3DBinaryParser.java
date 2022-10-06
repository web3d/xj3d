/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package org.web3d.parser.x3d;

// Standard imports
import com.sun.xml.fastinfoset.sax.SAXDocumentParser;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;

import org.jvnet.fastinfoset.EncodingAlgorithm;
import org.jvnet.fastinfoset.sax.FastInfosetReader;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;

import org.j3d.util.ErrorReporter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A binary parser using FastInfoset to decode files.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class X3DBinaryParser implements X3DRelaxedParserConstants {

    /** Message for parsing report prefixing */
    private static final String PARSE_MSG = "Parsing error: ";

    /** Message for reporting PROFILE decl errors */
    private static final String PROFILE_MSG = "PROFILE declaration error: ";

    /** Message for parsing report prefixing */
    private static final String COMPONENT_MSG = "COMPONENT declaration error: ";

    /** Message for parsing report prefixing */
    private static final String META_MSG = "META declaration error: ";

    /** Default size for the string array buffer */
    private static final int DEFAULT_ARRAY_SIZE = 4096;

    /** Increment size for the string array buffer */
    private static final int DEFAULT_ARRAY_INC = 1024;

    /** X3D specific element reader */
    private FastInfosetElementReader fer;

    /** FastInfoset Reader */
    private SAXDocumentParser fr;

    /** The X3D binary vocabulary */
    private X3DBinaryVocabulary vocab;

    /**
     * Inner class implementation of the document locator so that we can use
     * the JavaCC token information for line info
     */
    private class VRMLRelaxedLocator implements Locator {
        /**
         * Get the column number that the error occurred at. This is defined
         * to be the starting column.
         */
        @Override
        public int getColumnNumber() {
            // TODO: need real values
            return 0;
        }

        /**
         * Get the line number that the error occurred at. This is defined
         * to be the starting line.
         */
        @Override
        public int getLineNumber() {
            return 0;
        }
    }

    // Variables for our general use during parsing

    /** The url of the current document */
    private String documentURL;

    /** The world URL of the current document*/
    private String worldURL;

    /** Reference to the registered content handler if we have one */
    private BinaryContentHandler contentHandler;

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

    /** The InputStream to read from */
    private InputStream is;

    /** The Reader to read from */
    private Reader reader;

    /**
     * Create a new parser instance that has all of the handlers set to the
     * given references. These can be overridden with the set methods below.
     * It is safe to pass in null references here.
     *
     * @param is The stream to read characters from
     * @param ch The content handler instance to use
     * @param rh The route handler instance to use
     * @param sh The script handler instance to use
     * @param ph The proto handler instance to use
     * @param eh The error handler instance to use
     */
    public X3DBinaryParser(InputStream is,
                            ContentHandler ch,
                            RouteHandler rh,
                            ScriptHandler sh,
                            ProtoHandler ph,
                            ErrorHandler eh) {

        locator = new VRMLRelaxedLocator();

        contentHandler = (BinaryContentHandler) ch;
        routeHandler = rh;
        scriptHandler = sh;
        protoHandler = ph;
        errorHandler = eh;

        ReInit(is);
    }

    /**
     * Create a new parser instance that has all of the handlers set to the
     * given references. These can be overridden with the set methods below.
     * It is safe to pass in null references here.
     *
     * @param rdr The reader to source characters from
     * @param ch The content handler instance to use
     * @param rh The route handler instance to use
     * @param sh The script handler instance to use
     * @param ph The proto handler instance to use
     * @param eh The error handler instance to use
     */
    public X3DBinaryParser(Reader rdr,
                            ContentHandler ch,
                            RouteHandler rh,
                            ScriptHandler sh,
                            ProtoHandler ph,
                            ErrorHandler eh) {


        locator = new VRMLRelaxedLocator();

        contentHandler = (BinaryContentHandler) ch;
        routeHandler = rh;
        scriptHandler = sh;
        protoHandler = ph;
        errorHandler = eh;

        ReInit(rdr);
    }

    /**
     * Create a new parser instance that has all of the handlers set to the
     * given references. These can be overridden with the set methods below.
     * It is safe to pass in null references here.
     *
     * @param is The stream to read characters from
     */
    public X3DBinaryParser(InputStream is) {
        // Reduce the number of small calls to the reportable input stream
        this.is = new BufferedInputStream(is);
    }

    /**
     * Create a new parser instance that has all of the handlers set to the
     * given references. These can be overridden with the set methods below.
     * It is safe to pass in null references here.
     *
     * @param rdr The reader to source characters from
     */
    public X3DBinaryParser(Reader rdr) {
        // TODO: XSBC only accepts readers currently
        //is = InputStreamReader(rdr);
    }

    public final void ReInit(Reader rdr) {
        // TODO: XSBC only accepts readers currently
        //is = InputStreamReader(rdr);

        fer = new FastInfosetElementReader();
        fr = getSAXParser();

        fer.setContentHandler(contentHandler);
        fer.setProtoHandler(protoHandler);
        fer.setScriptHandler(scriptHandler);
        fer.setRouteHandler(routeHandler);
        fer.setErrorReporter(errorHandler);
        fr.setContentHandler(fer);
        fr.setLexicalHandler(fer);
    }

    public final void ReInit(InputStream is) {
        // TODO: XSBC only accepts readers currently
        reader = new InputStreamReader(is);

        fer = new FastInfosetElementReader();
        fr = getSAXParser();

        fer.setContentHandler(contentHandler);
        fer.setProtoHandler(protoHandler);
        fer.setScriptHandler(scriptHandler);
        fer.setRouteHandler(routeHandler);
        fer.setErrorReporter(errorHandler);
        fr.setContentHandler(fer);
        fr.setLexicalHandler(fer);
    }

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    public void initialize() {
        if(locator == null)
            locator = new VRMLRelaxedLocator();

        fer = new FastInfosetElementReader();
        fr = getSAXParser();

        fer.setContentHandler(contentHandler);
        fer.setProtoHandler(protoHandler);
        fer.setScriptHandler(scriptHandler);
        fer.setRouteHandler(routeHandler);
        fer.setErrorReporter(errorHandler);
        fr.setContentHandler(fer);
        fr.setLexicalHandler(fer);
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
     * @param worldURL The worldURL
     */
    public void setDocumentUrl(String url, String worldURL) {
        documentURL = url;
        this.worldURL = worldURL;

        fer.setLoadState(worldURL, documentURL, false);
    }

    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * {@link #Scene()} but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    public Locator getDocumentLocator() {

        if(locator == null)
            locator = new VRMLRelaxedLocator();

        return locator;
    }

    /**
     * Set the content handler instance.
     *
     * @param ch The content handler instance to use
     */
    public void setContentHandler(ContentHandler ch) {

        if (ch instanceof BinaryContentHandler) {
            contentHandler = (BinaryContentHandler) ch;
            fer.setContentHandler(contentHandler);
        } else {
            errorHandler.messageReport("Inavlid ContentHandler provided, " +
                    "must be a BinaryContentHandler");
        }

    }

    /**
     * Set the route handler instance.
     *
     * @param rh The route handler instance to use
     */
    public void setRouteHandler(RouteHandler rh) {
        routeHandler = rh;
        fer.setRouteHandler(rh);
    }

    /**
     * Set the script handler instance.
     *
     * @param sh The script handler instance to use
     */
    public void setScriptHandler(ScriptHandler sh) {
        scriptHandler = sh;
        fer.setScriptHandler(sh);
    }

    /**
     * Set the proto handler instance.
     *
     * @param ph The proto handler instance to use
     */
    public void setProtoHandler(ProtoHandler ph) {
        protoHandler = ph;
        fer.setProtoHandler(ph);
    }

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
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
    public void setErrorReporter(ErrorReporter eh) {

        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorHandler = eh;
    }

    /**
     * Convenience method to take a current exception and turns it into
     * a parse exception so that it makes it out of the parser.
     *
     * @param se The SAV exception to be printed
     * @throws The parse exception that matches this
     */
    private void convertException(VRMLException se) throws ParseException {

        boolean handled = false;

        if((se instanceof InvalidFieldException) ||
           (se instanceof InvalidFieldValueException)) {
            if(errorHandler != null) {
                try {
                    errorHandler.warningReport(PARSE_MSG, se);
                    handled = true;
                } catch(VRMLException se1) {
                    se = se1;
                }
            }
        } else {
            if(errorHandler != null) {
                try {
                    errorHandler.errorReport(PARSE_MSG, se);
                    handled = true;
                } catch(VRMLException se1) {
                    se = se1;
                }
            }
        }

        if(!handled) {
            StringBuilder buf =
                new StringBuilder("There's an error in the file:\n");

            buf.append(documentURL);
            buf.append('\n');

            String txt = se.getMessage();
            if(txt == null)
                txt = se.getClass().getName();

            buf.append(txt);
            buf.append('\n');

/*
            buf.append("\nThe exception trace is:\n");

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(bao);
            se.printStackTrace(printer);

            buf.append(bao.toString());
*/

            throw new ParseException(buf.toString());
        }
    }

    /**
     * Parse the scene now
     * @throws org.web3d.parser.x3d.ParseException
     */
    public void Scene() throws ParseException {
        try {
            InputSource inputSource = new InputSource(is);

            fr.parse(inputSource);

        } catch(IOException ioe) {
            ParseException pe =
                new ParseException("Binary IO Error: " + ioe.getMessage());
            pe.initCause(ioe);

            throw pe;
        } catch(SAXException se) {
            ParseException pe =
                new ParseException("Binary Parsing Error: " + se.getMessage());
            pe.initCause(se);

            throw pe;
        }
    }

    /**
     * Get a SAXDocument Parser.
     */
    private SAXDocumentParser getSAXParser() {
        SAXDocumentParser parser = new SAXDocumentParser();

        try {
            parser.setProperty(FastInfosetReader.BUFFER_SIZE_PROPERTY, 25000000);
        } catch(SAXNotRecognizedException | SAXNotSupportedException e) {
            errorHandler.warningReport("Couldn't set octet buffer size", null);
            // ignore
        }

        ParserVocabulary externalVocabulary = new ParserVocabulary();
        externalVocabulary.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
        externalVocabulary.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
        externalVocabulary.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);


        Map<String, ParserVocabulary> externalVocabularies = new HashMap<>();
        externalVocabularies.put(X3DBinaryConstants.EXTERNAL_VOCABULARY_URI_STRING, X3DBinaryVocabulary.parserVoc);
        externalVocabularies.put(X3DBinaryConstants.EXTERNAL_VOCABULARY_URI_STRING_OLD, X3DBinaryVocabulary.parserVoc);

        try {
            parser.setProperty(FastInfosetReader.EXTERNAL_VOCABULARIES_PROPERTY, externalVocabularies);
        } catch(SAXException se) {
            errorHandler.errorReport("Can't setup external vocabulary?", se);
        }

        Map<String, EncodingAlgorithm> algorithms = new HashMap<>();
        algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
        algorithms.put(DeltazlibIntArrayAlgorithm.ALGORITHM_URI, new DeltazlibIntArrayAlgorithm());
        algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm());
        algorithms.put(QuantizedzlibFloatArrayAlgorithm2.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm2());
        parser.setRegisteredEncodingAlgorithms(algorithms);

        parser.setPrimitiveTypeContentHandler(fer);
        parser.setEncodingAlgorithmContentHandler(fer);
        return parser;
    }
}
