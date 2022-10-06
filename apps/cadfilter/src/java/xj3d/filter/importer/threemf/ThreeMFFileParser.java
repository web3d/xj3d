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

package xj3d.filter.importer.threemf;

// External imports
import java.io.*;
import java.net.URL;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import org.j3d.util.ErrorReporter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.vrml.sav.*;

import xj3d.filter.NonWeb3DFileParser;

/**
 * File parser that reads 3mf files and generates an X3D stream
 * of events.
 *
 * Output Styles:
 *
 *    UNCOLORED - Color information is stripped
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class ThreeMFFileParser implements NonWeb3DFileParser {
    private static final String MODEL_SCHEMA = "http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel";

    /** Identifier */
    private static final String LOG_NAME = "ThreeMFFileParser";

    /** The Document Element */
    private Object doc_element;

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

    // Global scale for units conversion
    private float scale = 1.0f;

    /** How to style our output.  Supports UNCOLORED, MATRIX_TRANSFORM or null for none */
    private Set<String> style;

    /** The main model file identified by the relationships */
    private String modelFile;

    /**
     * Constructor
     */
    public ThreeMFFileParser() {
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
     * @return a list containing the file structure and data
     * @throws IOException An I/O error while reading the stream
     * @throws ImportFileFormatException A parsing error occurred in the file
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, ImportFileFormatException {

        // Check for 3mf extension
        if (!input.getURL().contains("3mf")) {return null;}

        this.style = new HashSet<>();
        if (style != null) {
            this.style.addAll(Arrays.asList(style));
        }

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some to deal with this.
        URL url = new URL(input.getURL());

        // acquire the contents of the document
        ThreeMFReader cr = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            cr = new ThreeMFReader();

            File dir = createTempDir("threemf");
            dir.deleteOnExit();

            if (input.getURL().startsWith("file:")) {
                // special case this so we can use ZipFile instead of ZipArchiveEntry
                File f = new File(url.getPath());
                unzip(f,dir);
            } else {
                throw new IllegalArgumentException("Path not implemented");
            }

            // We should read .rels files and find the
            // For now assume the model is
            File[] files = dir.listFiles();
            String main_model = getMainModel(new File(dir.getAbsolutePath() + File.separator + "_rels" + File.separator + ".rels"));
            File model = new File(dir.getAbsolutePath() + File.separator + main_model);
            fis = new FileInputStream(model);
            bis = new BufferedInputStream(fis);

            // Expand the zip to a temporary file and then find the main model file
            cr.parse(new org.xml.sax.InputSource(bis));
        } catch (IOException ioe) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                LOG_NAME + ": IOException reading: "+ url);

            iffe.setStackTrace(ioe.getStackTrace());
            throw iffe;
        } catch (SAXException se) {

            ImportFileFormatException iffe = new ImportFileFormatException(
                LOG_NAME + ": SAXException reading: "+ url);

            iffe.setStackTrace(se.getStackTrace());
            throw iffe;
        } finally {
            if (bis != null) bis.close();
            if (fis != null) fis.close();
        }
        // get the libraries
        doc_element = cr.getResult();

        contentHandler.startDocument(input.getURL(),
            input.getBaseURL(),
            "utf8",
            "#X3D",
            "V3.3",
            "3mf file conversion");

        contentHandler.profileDecl("Interchange");


        Model model = (Model) cr.getResult();

        contentHandler.startNode("NavigationInfo",null);
        contentHandler.startField("avatarSize");
        bch.fieldValue(new float[] {0.1f,1.6f,0.75f},3);   // assume small objects
        contentHandler.endNode();
        contentHandler.startNode("Transform",model.getUnit() + "_TRANS");
        float scale = 1;
        switch(model.getUnit()) {
            case micron:
                scale = (float) (1e-6);
                break;
            case millimeter:
                scale = (float) (1e-3);
                break;
            case centimeter:
                scale = (float) (1e-2);
                break;
            case inch:
                scale = 0.0254f;
                break;
            case foot:
                scale = 0.3048f;
                break;
            case meter:
                scale = 1;
                break;
            default:
                throw new IllegalArgumentException("Unhandled unit");
        }
        contentHandler.startField("scale");
        bch.fieldValue(new float[] {scale,scale,scale},3);
        contentHandler.startField("rotation");
        bch.fieldValue(new float[] {1,0,0,-(float)Math.PI/2},4);  // change z up to y up
        contentHandler.startField("children");

        Build build = model.getBuild();
        List<Item> items = build.getItems();
        for(Item item : items) {
            ObjectResource resource = (ObjectResource) model.getResource(item.getObjectID());
            writeObject(resource);
        }

        contentHandler.endField();
        contentHandler.endNode();

        // release references to any objects created from parsing the file
        doc_element = null;

        contentHandler.endDocument();

        return null;
    }

    private void writeObject(ObjectResource resource) {
        Mesh mesh = resource.getMesh();
        Vertices verts = mesh.getVertices();
        Triangles tris = mesh.getTriangles();

        contentHandler.startNode("Shape","OBJ_" + resource.getID());
        contentHandler.startField("appearance");
        contentHandler.startNode("Appearance", null);
        contentHandler.startField("material");
        contentHandler.startNode("Material",null);
        contentHandler.endNode();  // Material
        contentHandler.endNode();  // Appearance
        contentHandler.startField("geometry");
        contentHandler.startNode("IndexedTriangleSet", null);
        contentHandler.startField("coord");
        contentHandler.startNode("Coordinate", null);
        contentHandler.startField("point");
        if (bch != null) {
            // TODO: should we compact these?
            bch.fieldValue(verts.getVerts(), verts.getCount() * 3);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
        contentHandler.endNode();  // Coordinate
        contentHandler.startField("index");
        if (bch != null) {
            bch.fieldValue(tris.getTris(), tris.getCount() * 3);
        }
        contentHandler.endNode();  // IndexedTriangleSet
        contentHandler.endNode();  // Shape
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Get the main model file by parsing the .rels file
     * @param relFile The relationship file
     * @return The main model or null if not found
     */
    private String getMainModel(File relFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(relFile);

            NodeList list = doc.getElementsByTagName("Relationship");
            for(int i=0; i < list.getLength(); i++) {
                Element n = (Element) list.item(i);
                String type = n.getAttribute("Type");
                if (type == null) continue;

                if (type.equals(MODEL_SCHEMA)) {
                    // found primary model
                    return n.getAttribute("Target");
                }
            }
        } catch(IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return null;
    }

    /**
     * Unzip a file into a destination directory
     *
     * @param src
     * @param dest
     */
    private void unzip(File src, File dest) throws IOException {
        try (ZipFile zipFile = new ZipFile(src)) {

            for (Enumeration e = zipFile.getEntries(); e.hasMoreElements(); ) {
                ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();
                unzipEntry(zipFile, entry, dest);
            }
        }
    }

    private void unzipEntry(ZipFile zipFile, ZipArchiveEntry entry, File dest) throws IOException {

        if (entry.isDirectory()) {
            new File(dest, entry.getName()).mkdirs();
            return;
        }

        File outputFile = new File(dest, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null ) outputStream.close();
            if (inputStream != null) inputStream.close();
        }
    }

    public static File createTempDir(String prefix)
            throws IOException
    {
        String tmpDirStr = System.getProperty("java.io.tmpdir");
        if (tmpDirStr == null) {
            throw new IOException(
                    "System property 'java.io.tmpdir' does not specify a tmp dir");
        }

        File tmpDir = new File(tmpDirStr);
        if (!tmpDir.exists()) {
            boolean created = tmpDir.mkdirs();
            if (!created) {
                throw new IOException("Unable to create tmp dir " + tmpDir);
            }
        }

        File resultDir = null;
        int suffix = (int)System.currentTimeMillis();
        int failureCount = 0;
        do {
            resultDir = new File(tmpDir, prefix + suffix % 10_000);
            suffix++;
            failureCount++;
        }
        while (resultDir.exists() && failureCount < 50);

        if (resultDir.exists()) {
            throw new IOException(failureCount +
                    " attempts to generate a non-existent directory name failed, giving up");
        }
        boolean created = resultDir.mkdir();
        if (!created) {
            throw new IOException("Failed to create tmp directory");
        }

        return resultDir;
    }
}
