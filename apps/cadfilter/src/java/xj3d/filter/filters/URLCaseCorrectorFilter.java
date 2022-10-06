/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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
import java.io.*;
import java.net.MalformedURLException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

// Local imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;

/**
 * Fix files that contain url references that are the wrong case.  These
 * url's work on Windows but not real operating systems like UNIX.
 * <p>
 *
 * Only works when used in the directory where the file exists.  Need
 * a way to get content directory to generalize.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class URLCaseCorrectorFilter extends AbstractFilter {

    /** Are we inside a URL field */
    private boolean insideURL;

    /** The current directory */
    private String currentDir;
    /**
     * Create an instance of the filter.
     */
    public URLCaseCorrectorFilter() {
        insideURL = false;
    }

    //-----------------------------------------------------------------------
    // Methods defined by ContentHandler
    //-----------------------------------------------------------------------

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
        throws SAVException, VRMLException {

        if(url.startsWith("file:/")) {
            try {
                URL file_url = new URL(url);
                URI file_uri = file_url.toURI();
                File f = new File(file_uri);

                currentDir = f.getAbsolutePath();
            } catch(MalformedURLException | URISyntaxException mue) {
                System.err.println("Invalid root file path in URLCaseCorrectionFilter");
            }
        } else {
            File f = new File(".");
            currentDir = f.getAbsolutePath();
        }

        super.startDocument(uri, url, encoding, type, version, comment);
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
        if (name.equals("url")) {
            insideURL = true;
        }

        super.startField(name);
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
        insideURL = false;

        super.endField();
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String value) throws SAVException, VRMLException {
        if (insideURL) {
            super.fieldValue(correctURL(value));
            insideURL = false;
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        if (insideURL) {
            String[] newVals = new String[values.length];
            for(int i=0; i < values.length; i++) {
                newVals[i] = correctURL(values[i]);
            }
            insideURL = false;

            super.fieldValue(newVals);
        } else {
            super.fieldValue(values);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String[] values, int len)
        throws SAVException, VRMLException {

        if (insideURL) {
            String[] newVals = new String[len];
            for(int i=0; i < len; i++) {
                newVals[i] = correctURL(values[i]);
            }
            insideURL = false;

            super.fieldValue(newVals, len);
        } else {
            super.fieldValue(values, len);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Correct the url if the case is wrong.
     *
     * @param url The original url
     * @return The corrected url
     */
    private String correctURL(String url) {
        // Remove outer quotes
        String filename;

        if (url.startsWith("\"") || url.startsWith("'")) {
            filename = url.substring(1, url.length() - 1);
        } else {
            filename = url;
        }

        File file = new File(filename);
        String fileAbsolute = file.getAbsolutePath();

        String pathStr = file.getAbsolutePath();
        pathStr = pathStr.substring(0, pathStr.lastIndexOf(File.separator));

        File path = new File(pathStr);

        File[] files = path.listFiles();

        String fileStr;

        for (File file1 : files) {
            fileStr = file1.toString();
            if (fileStr.equalsIgnoreCase(fileAbsolute)) {
                if (!fileStr.equals(fileAbsolute)) {
                    url = fileStr.substring(currentDir.length()+1);
                }
            }
        }
        return url;
    }
}
