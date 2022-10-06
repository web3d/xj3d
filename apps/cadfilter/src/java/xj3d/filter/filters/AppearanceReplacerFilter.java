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

// External Imports
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

// Local Imports
import xj3d.filter.node.EncodedFilter;
import xj3d.filter.NewAbstractFilter;
import xj3d.filter.node.*;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;

/**
 * Replaces all appearances with the specified one.  Nodes without an appearance
 * will get one.
 *
 * Params supported:
 *     defFilter only apply the filter to a node with this DEF and its children
 *
 *     appearanceFile The X3D file containing the appearance to use.
 *        The X3D files must be a complete valid X3D file.  Only the first
 *        appearance found will be used.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class AppearanceReplacerFilter extends EncodedFilter {

    private static final boolean DEBUG = false;

    /** The logging identifier of this app */
    private static final String LOG_NAME = "AppearanceReplacer";

    /** Argument for the mapping file */
    private static final String APPEARANCE_FILE = "-appearanceFile";

    /** Argument for the mapping file */
    private static final String DEF_FILTER = "-defFilter";

    /** The appearance file to use */
    private String appearanceFile;

    /** The def name to start filtering from, or null for all */
    private String defFilter;

    /** Are we inside the DEF name filtered */
    private boolean inDEFContent;

    /** If inShape, was an appearance added? */
    private boolean appearanceAdded;

    /** Mapped name */
    private String mappedName;

    /** Container that keeps track of the defNames on the stack */
    private Stack<String> nodeDEFs;

    /** Has the appearance been DEFed */
    private boolean appearanceDEFed;

    /** The defName to use */
    private String defOverride;

    /** The count of instream dups of our chosen override */
    private int defOverrideNum;

    /** DEF names remapped to the appearance */
    private HashSet<String> remapDEF;

    /** Did we use an appearance inside of creating one */
    private boolean useApp;

    /**
     * Basic constructor.
     */
    public AppearanceReplacerFilter() {
        super(DEBUG);

        inDEFContent = true;
        defFilter = null;
        appearanceAdded = false;

        nodeDEFs = new Stack<>();
        appearanceDEFed = false;
        useApp = false;

        remapDEF = new HashSet<>();
        defOverrideNum = 0;
    }

    //----------------------------------------------------------
    // Methods overidding AbstractFilter
    //----------------------------------------------------------


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
    public void startNode(String name, String defName) throws SAVException,
            VRMLException {

        if (defName != null && defName.equals(defOverride)) {
            // Detected in stream is using our choosen defName
            defOverride = makeDEFName(appearanceFile, ++defOverrideNum);
        }

        nodeDEFs.push(defName);

        if (defFilter != null && defFilter.equals(defName)) {
//System.out.println("Inside DEF content");
            inDEFContent = true;
        }

        // Reset the flags for each shape node
        switch (name) {
            case "Shape":
                super.startNode(name, defName);
                appearanceAdded = false;
                break;
            case "Appearance":
                // Load file and issue sav calls.

                if (inDEFContent) {
                    mappedName = name;
                    useApp = filterAppearance(appearanceFile, name);

                    appearanceAdded = true;

                    if (defName != null)
                        remapDEF.add(defName);

//System.out.println("***Inside mapped content: " + useApp);

                    // Now suppress content
                    suppressCalls(true);

                    // For suppressed content pretend like we are in the right node
                    /*
                    super.startNode(name, defName);
                    */
                    // Copied from startNode, can't use as fieldStack is different
                    Encodable enc = factory.getEncodable(name, defName);
                    encStack.push(enc);

                    if (defName != null) {
//    System.out.println("Encoding def: " + defName + " enc: " + enc);
                        encMap.put(defName, enc);
                    }

                    nodeStack.push("Appearance");
                    parentTypeStack.push(fieldHandler.getFieldType("Shape", "appearance"));
                } else {
                    super.startNode(name, defName);
                }   break;
            default:
                super.startNode(name, defName);
                break;
        }
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
        String fieldName = (String) fieldStack.peek();

        String nodeName = (String) nodeStack.peek();

        if (fieldName.equals("appearance")) {
            if (inDEFContent) {
//System.out.println("Found USE of appearance: " + defName);
                // ignore USE and replace with appearance
                useApp = filterAppearance(appearanceFile, "Appearance");
//System.out.println("useApp: " + useApp);
                appearanceAdded = true;

                if (!useApp) {
                    super.endNode();
                }

                return;
            }
        }

        if (inDEFContent && remapDEF.contains(defName)) {
//System.out.println("Remapping " + defName + " to: " + defOverride);
            super.useDecl(defOverride);
            return;
        } else if (remapDEF.contains(defName)) {
//System.out.println("Found removed def, encoding: " + defName);
            Encodable enc = encMap.get(defName);
            enc.encode();

            suppressCalls(true);
            super.useDecl(defName);
            suppressCalls(false);

//System.out.println("Done encoding");
            return;
        }

        super.useDecl(defName);
    }

    /**
     * Notification of the end of a node declaration.
     * If boolean addMaterial is set to TRUE, then when a
     * Shape node ends we reset materialAdded, so that
     * future Shapes can also have Material nodes added
     * if necessary.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.peek();
        String defName = nodeDEFs.pop();

        if(nodeName.equals("Shape")){
            if(!appearanceAdded && inDEFContent) {
                startField("appearance");
//System.out.println("Filtering in appearance");
                boolean use = filterAppearance(appearanceFile, "Appearance");
//System.out.println("useDecl: " + use);
                if (!use)
                    super.endNode();
            }
        }

        if(nodeName != null && nodeName.equals(mappedName)) {
//System.out.println("***Outside mapped content: " + mappedName);
            // End the faked appearance node
            nodeStack.pop();
            parentTypeStack.pop();
            encStack.pop();     // pop the original appearnace we placed here
            suppressCalls(false);
            mappedName = null;

            if (!useApp) {
                super.endNode();   // Must perform here as SF was here
            }

            if (defFilter != null && defName != null && defName.equals(defFilter)) {
//    System.out.println("Finished defContent");
                inDEFContent = false;
            }
            return;
        }

        if (defFilter != null && defName != null && defName.equals(defFilter)) {
//System.out.println("Finished defContent");
            inDEFContent = false;
        }

        super.endNode();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param inFile The file to load, or null if the input is specified by the url argument.
     * @param nodeName The name of the node
     *
     * @return True for Issued a use, false for a node
     */
    private boolean filterAppearance(String inFile, String nodeName) {
        if (appearanceDEFed) {
            super.useDecl(defOverride);
            return true;
        }

        super.startNode("Appearance", defOverride);

        NewAbstractFilter chooser_filter = new NodeChooserFilter(true, nodeName, defOverride);
        if (DEBUG) chooser_filter.setDebug(DEBUG);

        chooser_filter.setContentHandler(getContentHandler());
        chooser_filter.setScriptHandler(getScriptHandler());
        chooser_filter.setProtoHandler(getProtoHandler());
        chooser_filter.setRouteHandler(getRouteHandler());

        InputSource is;

        File file = new File(inFile);
        if (file.exists()) {
            is = new InputSource(file);
        } else {
            try {
                InputStream iStream = AppearanceReplacerFilter.class.getClassLoader().getResourceAsStream(inFile);
                URL url = file.toURI().toURL();
                String baseURL = url.getHost() + ":" + url.getPort() + url.getPath();
                is = new InputSource(baseURL, iStream, inFile);
            } catch(MalformedURLException mfe) {
                mfe.printStackTrace(System.err);
                return false;
            }
        }

        VRMLParserFactory parserFactory;

        try {
            parserFactory = VRMLParserFactory.newVRMLParserFactory();
        } catch(FactoryConfigurationError fce) {
            System.err.println("Can't configure parser");
            //errorHandler.fatalErrorReport("Failed to load factory", fce);
            return false;
        }

        VRMLReader reader = parserFactory.newVRMLReader();

        reader.setContentHandler(chooser_filter);
        reader.setRouteHandler(chooser_filter);
        reader.setScriptHandler(chooser_filter);
        reader.setProtoHandler(chooser_filter);
        reader.setErrorReporter(errorHandler);

        try {
            reader.parse(is);
        } catch (IOException | VRMLException e) {
            e.printStackTrace(System.err);
        } finally {

            try {
                if (is != null) {
                    // clean up...
                    is.close();
                }
            } catch (IOException ioe) {}
        }

        appearanceDEFed = true;

        return false;
    }

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        String prefix = "-" + LOG_NAME + ":";
        String arg;

        for (int i = 0; i< args.length; i++) {
            arg = args[i];

            if (arg.startsWith(prefix)) {
                arg = "-" + arg.substring(prefix.length());
            }

            switch (arg) {
                case APPEARANCE_FILE:
                    if (i + 1 >= args.length){

                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting one more for appearance file.");
                    }   appearanceFile = args[i+1];
                    break;
                case DEF_FILTER:
                    if (i + 1 >= args.length){

                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting one more for defFilter.");
                    }   defFilter = args[i+1];
                    inDEFContent = false;
//System.out.println("Got defFilter: " + defFilter);
                    break;
            }
        }

        if (appearanceFile == null) {
            throw new IllegalArgumentException(
                "-appearanceFile required  ");
        }

        if (defFilter == null) {
            inDEFContent = true;
        }

        defOverride = makeDEFName(appearanceFile, 0);
    }

    /**
     * Make an acceptable X3D DEF name from a filename.
     *
     * @param file The filename
     * @param num The instance num to make unique
     * @return The DEF name
     */
     private String makeDEFName(String file, int num) {
        int idx1;

        idx1 = file.lastIndexOf("/") + 1;

        if (idx1 == 0) {
            idx1 = file.lastIndexOf("\\") + 1;
        }

        int idx2 = file.lastIndexOf(".");

        if (idx2 < 0)
            idx2 = file.length();

        String ret_val = file.substring(idx1, idx2);

        if (num > 0) {
            ret_val = ret_val + "_" + num;
        }

        return ret_val;
     }
}
