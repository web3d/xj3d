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
import java.io.*;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;

/**
 * X3D XML exporter using a retained Scenegraph.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.30 $
 */
public class X3DXMLRetainedExporter extends X3DRetainedSAXExporter {

    /** The output stream */
    private OutputStream os;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     */
    public X3DXMLRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter) {

        super(major, minor, errorReporter, METHOD_STRINGS, 0);

        this.os = os;

        init();
    }

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param sigDigits The number of significant digits to use in printing floats
     */
    public X3DXMLRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int sigDigits) {

        this(os, major, minor, errorReporter);
        this.sigDigits = sigDigits;
    }

    /**
     *  Common initialization routine.
     */
    private void init() {
        encodingTo = ".x3d";
        printDocType = true;
        printXML = true;

        stripWhitespace = false;
    }

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <code>#</code> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)

        throws SAVException, VRMLException{

        super.startDocument(uri, url, encoding, type, version, comment);


        try {
            handler = new SAXPrinter(new OutputStreamWriter(os, "UTF8"),
                majorVersion, minorVersion, printDocType, printXML);
        } catch(IOException ioe) {
            errorReporter.errorReport(ioe.getMessage(), ioe);
        }
    }
}
