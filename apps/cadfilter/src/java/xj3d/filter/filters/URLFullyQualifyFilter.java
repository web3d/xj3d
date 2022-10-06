/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
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

import java.net.*;

// Local imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.*;

import xj3d.filter.AbstractFilter;
import xj3d.filter.FieldValueHandler;

/**
 * Fully qualify all relative urls in a file.
 * <p>
 *
 * <b>Filter Options</b><p>
 * -baseURL - The base url to use to qualify urls
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class URLFullyQualifyFilter extends AbstractFilter {
    /** The logging identifier of this app */
    private static final String LOG_NAME = "URLFullyQualifyFilter";

    /** The scale argument option identifier */
    private static final String BASEURL_ARG = "-baseURL";

    /** The base url */
    private String baseURL;

    /** Are we inside a URL field */
    private boolean insideURL;

    /**
     * Create an instance of the filter.
     */
    public URLFullyQualifyFilter() {
        insideURL = false;
    }

    //-----------------------------------------------------------------------
    // Methods defined by ContentHandler
    //-----------------------------------------------------------------------

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
            super.fieldValue(qualifyURL(value));
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
                newVals[i] = qualifyURL(values[i]);
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
                newVals[i] = qualifyURL(values[i]);
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
     * Qualify the url if the case is wrong.
     *
     * @param url_st The original url
     * @return The qualified url
     */
    private String qualifyURL(String url_st) {
        // Remove outer quotes
        String surl_st = null;

        if (url_st.startsWith("\"") || url_st.startsWith("'")) {
            surl_st = url_st.substring(1, url_st.length() - 1);
        } else {
            surl_st = url_st;
        }

        try {
            URL url = new URL(surl_st);

            if (url.getProtocol() != null) {
                return surl_st;
            }
        } catch(MalformedURLException mue) {
            // ignore
        }

        String ret_val = baseURL + surl_st;

        return ret_val;
    }

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {

        //////////////////////////////////////////////////////////////////////
        //
        // parse the arguments
        //
        for(int i = 0; i < arg.length; i++) {
            String argument = arg[i];
            if(argument.startsWith("-")) {
                try {
                    if(argument.equals(BASEURL_ARG)) {
                        baseURL = arg[i + 1];
                        i += 1;  // skip the next one that we've just read

                        if (!baseURL.endsWith("/")) {
                            baseURL += "/";
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        LOG_NAME + ": Error parsing filter arguments");
                }
            }
        }
    }
}
