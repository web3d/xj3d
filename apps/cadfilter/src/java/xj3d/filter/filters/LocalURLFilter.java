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
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashSet;

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.sav.StringContentHandler;
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.BaseFilter;

/**
 * Strips out all URL fields that are not local.
 * <p>
 * The following behaviour is implemented:
 * <ul>
 * <li>remove all fully qualified URL references</li>
 * <li>Change all relative URL references to remove any relative directory paths.
 * <ul>
 *   <li>/foo/texture.png becomes texture.png</li>
 *   <li>foo/texture.png becomes texture.png</li>
 * </ul>
 * <li>Any URLs that contain query or reference paths shall be removed</li>
 * </ul>
 *
 * <b>Filter Options</b><p>
 * None.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class LocalURLFilter extends BaseFilter {

    /** The logging identifier of this app */
    private static final String LOG_NAME = "LocalURLFilter";

    /** Are we inside any of the geometry nodes */
    private boolean insideURL;

    /**
     * Default constructor
     */
    public LocalURLFilter() {
    }

    //----------------------------------------------------------
    // ContentHandler methods
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

        super.startDocument(uri,url, encoding, type, version, comment);

        insideURL = false;
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the ScriptHandler or ProtoHandler methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {
        super.startField(name);

        if(name.equals("url")) {
            insideURL = true;
        }
    }

    //-----------------------------------------------------------------------
    //Methods for interface StringContentHandler
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
        if(insideURL) {
            value = value.replace('\\','/');
            String[] original_urls = fieldReader.MFString(value);
            processURLs(original_urls);
            insideURL = false;
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * The value of a field given as an array of strings.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        if(insideURL) {
            if (values != null) {
                for(int i=0; i < values.length; i++) {
                    values[i] = values[i].replace('\\','/');
                }
            }
            String[] original_urls = fieldReader.MFString(values);
            processURLs(original_urls);
            insideURL = false;
        } else {
            super.fieldValue(values);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

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

        if(insideURL) {
            String[] original_urls = new String[len];
            for(int i=0; i < len; i++) {
                original_urls[i] = value[i].replace('\\','/');
            }

            processURLs(original_urls);
            insideURL = false;
        } else {
            super.fieldValue(value, len);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Trim a set of URLs according to the rules and issue the cleaned up
     * URLs to the next item in the chain.
     *
     * @param urls The URL list to process
     */
    private void processURLs(String[] urls) {
        // Store in a hashset to remove duplicates
        HashSet<String> final_urls = new HashSet<>();

        for (String work_url : urls) {
            try {
                new URL(work_url);

                // Paths that start with "file:///" are valid URLs, but they
                // are also local, so accept it
                if (work_url.startsWith("file:///")) {
                    throw new MalformedURLException();
                }

                // URL is a valid and remote, add to extended message and continue to next
                I18nUtils.printMsg("xj3d.filter.filters.LocalURLFilter.invalidTextureReference", I18nUtils.EXT_MSG, new String[] {work_url});
                continue;
            } catch(MalformedURLException mue) {
                // good, we want this. Ignore an keep going
            }

            // remove everything containing a ? or #
            int idx = work_url.indexOf('?');

            if(idx != -1)
                continue;

            idx = work_url.indexOf('#');

            if(idx != -1)
                continue;

            // Strip all the leading directory items off it.
            idx = work_url.lastIndexOf('/');

            if(idx != -1)
                work_url = work_url.substring(idx + 1);

            if(work_url.length() != 0)
                final_urls.add(work_url);
        }

        String[] field_val = new String[final_urls.size()];
        final_urls.toArray(field_val);

        if(contentHandler instanceof BinaryContentHandler) {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;
            bch.fieldValue(field_val, field_val.length);
        } else if(contentHandler instanceof StringContentHandler) {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            sch.fieldValue(field_val);
        }
    }
}
