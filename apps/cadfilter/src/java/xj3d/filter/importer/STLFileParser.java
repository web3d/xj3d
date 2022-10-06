/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.loaders.stl.STLFileReader;

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.*;

import org.j3d.util.ErrorReporter;
import org.web3d.util.StringArray;
import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NonWeb3DFileParser;
import xj3d.filter.FilterProcessingException;
import xj3d.filter.FilterExitCodes;

/**
 * File parser implementation that reads STL files and generates an X3D stream
 * of events.
 * <p>
 *
 * @author Justin Couch
 * @version Grammar $Revision: 1.12 $
 */
public class STLFileParser implements NonWeb3DFileParser {
    /** Max triangles in count that we consider possible */
    private static final int MAX_SANE_TRIANGLES = 50_000_000;
    private static final float MAX_SANE_POSITION = 1e11f;
    private static final float MIN_SANE_POSITION = -MAX_SANE_POSITION;

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

    /**
     * Create a new instance of this parser
     */
    public STLFileParser() {
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
     * scene but there may be other times when it is not set.
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
     * @return Null if no parsing issues or a message detailing the issues
     * @throws IOException An I/O error while reading the stream
     * @throws VRMLParseException A parsing error occurred in the file
     */
    @Override
    public List<String> parse(InputSource input, String[] style)
        throws IOException, VRMLException {

        // Not good as this opens a second network connection, rather than
        // reusing the one that is already open when we checked the MIME type.
        // Need to recode some of the STL parser to deal with this.
        URL url = new URL(input.getURL());

        STLFileReader reader = null;
        try {
            reader = new STLFileReader(url, false);
        } catch(InvalidFormatException ife) {
            String msg = ife.getMessage();
            if (msg != null && msg.contains("no content defined")) {
                I18nUtils.printMsg("xj3d.filter.importer.STLFileParser.noData", I18nUtils.EXT_MSG, null);

                // ignore and this is really a valid file
            } else {
                throw new FilterProcessingException(ife.getMessage(),
                    FilterExitCodes.INVALID_INPUT_FILE);
            }
        }

        contentHandler.startDocument(input.getURL(),
                                     input.getBaseURL(),
                                     "utf8",
                                     "#X3D",
                                     "V3.0",
                                     "Auto converted STL file");

        contentHandler.profileDecl("Interchange");
        contentHandler.componentDecl("Rendering:3");

        if (reader != null) {
            generateTriSet(reader);
        }

        contentHandler.endDocument();
        if (reader != null) {
            reader.close();
            return reader.getParsingMessages();
        } else {
            ArrayList<String> ret = new ArrayList<>();
            return ret;
        }
    }

    /**
     * Generate the coordinate and normal information for the TriangleSet node
     * based on that read from the STL file.
     */
    private void generateTriSet(STLFileReader rdr)
        throws IOException, VRMLException {

        int num_objects = rdr.getNumOfObjects();
        int[] num_tris = rdr.getNumOfFacets();
        String[] obj_names = rdr.getObjectNames();
        int max_tris = 0;

        for(int j = 0; j < num_objects; j++) {
            if(num_tris[j] > max_tris)
                max_tris = num_tris[j];
        }

        if (max_tris == 0) {
            return;
        }

        if (num_objects == 1) {
            // Special case 1 object to read as many triangles as possible for binary miscounts
            num_tris[0] = Integer.MAX_VALUE;
        }
        boolean sane_count = true;

        if (max_tris > MAX_SANE_TRIANGLES) {
            System.out.println("Binary STL triangle count outside sane range, or solid keyword missing in ASCII STL.");
            max_tris = 250_000;
            sane_count = false;
        }

        double[] in_normal = new double[3];
        double[][] in_coords = new double[3][3];

        // Tweak the objectNames into something that is acceptable to
        // use as a DEF name ID.

        for(int i = 0; i < num_objects; i++) {
            if(obj_names[i] != null) {
                obj_names[i] = obj_names[i].replace('.', '_');
                obj_names[i] = obj_names[i].replace(' ', '_');
                obj_names[i] = obj_names[i].replace('\t', '_');
            }
        }

        if(contentHandler instanceof BinaryContentHandler) {
            BinaryContentHandler bch = (BinaryContentHandler)contentHandler;

            float[] out_coords = new float[max_tris * 9];
            float[] out_normals = new float[max_tris * 3];

            for(int i = 0; i < num_objects; i++) {
                if(num_tris[i] == 0)
                    continue;

                int idx = 0;
                int norm_idx = 0;

                for(int j = 0; j < num_tris[i]; j++) {
                    if (!rdr.getNextFacet(in_normal, in_coords)) {
                        break;
                    }
                    if (idx + 9 > out_coords.length) {
                        // Facet count was wrong, grow array
                        float[] new_coords = new float[out_coords.length * 2];
                        float[] new_normals = new float[out_normals.length * 2];
                        System.arraycopy(out_coords, 0, new_coords, 0, out_coords.length);
                        System.arraycopy(out_normals, 0, new_normals, 0, out_normals.length);

                        out_coords = new_coords;
                        out_normals = new_normals;
                    }
                    float n_x = (float)in_normal[0];
                    float n_y = (float)in_normal[1];
                    float n_z = (float)in_normal[2];

                    // do we need to autogenerate the normal?
                    if(n_x == 0 && n_y == 0 && n_z == 0) {
                        double x1 = in_coords[1][0] - in_coords[0][0];
                        double y1 = in_coords[1][1] - in_coords[0][1];
                        double z1 = in_coords[1][2] - in_coords[0][2];

                        double x2 = in_coords[2][0] - in_coords[0][0];
                        double y2 = in_coords[2][1] - in_coords[0][1];
                        double z2 = in_coords[2][2] - in_coords[0][2];

                        n_x = (float)(y1 * z2 - z1 * y2);
                        n_y = (float)(z1 * x2 - x1 * z2);
                        n_z = (float)(x1 * y2 - y1 * x2);
                    }

                    out_normals[norm_idx] = n_x;
                    out_normals[norm_idx + 1] = n_y;
                    out_normals[norm_idx + 2] = n_z;


                    out_coords[idx] = (float)in_coords[0][0];
                    out_coords[idx + 1] = (float)in_coords[0][1];
                    out_coords[idx + 2] = (float)in_coords[0][2];

                    out_coords[idx + 3] = (float)in_coords[1][0];
                    out_coords[idx + 4] = (float)in_coords[1][1];
                    out_coords[idx + 5] = (float)in_coords[1][2];

                    out_coords[idx + 6] = (float)in_coords[2][0];
                    out_coords[idx + 7] = (float)in_coords[2][1];
                    out_coords[idx + 8] = (float)in_coords[2][2];

                    idx += 9;
                    norm_idx += 3;
                }

                if (!sane_count) {
                    // verify coordinates are sane, otherwise likely was STL Ascii without solid
                    int len = out_coords.length;
                    for(int j=0; j < len; j++) {
                        if (out_coords[j] > MAX_SANE_POSITION || out_coords[j] < MIN_SANE_POSITION) {
                            String msg = I18nUtils.getMsg("xj3d.filter.importer.STLFileParser.invalidInput", null);
                            I18nUtils.printMsg("xj3d.filter.importer.STLFileParser.invalidInput", I18nUtils.CRIT_MSG, null);
                            throw new FilterProcessingException(msg, FilterExitCodes.INVALID_INPUT_FILE);
                        }
                    }

                }
                contentHandler.startNode("Shape", obj_names[i]);
                contentHandler.startField("geometry");
                contentHandler.startNode("TriangleSet", null);

                // Compact arrays if overallocated as some filters don't handle numValid well
                if (out_coords.length != idx) {
                    float[] new_coords = new float[idx];
                    float[] new_normals = new float[norm_idx];

                    System.arraycopy(out_coords, 0, new_coords, 0, idx);
                    System.arraycopy(out_normals, 0, out_normals, 0, norm_idx);

                    out_coords = new_coords;
                    out_normals = new_normals;
                }

                contentHandler.startField("normalPerVertex");
                bch.fieldValue(false);

                contentHandler.startField("coord");
                contentHandler.startNode("Coordinate", null);
                contentHandler.startField("point");
                bch.fieldValue(out_coords, idx);
                contentHandler.endNode();
                //contentHandler.endField();

                contentHandler.startField("normal");
                contentHandler.startNode("Normal", null);
                contentHandler.startField("vector");
                bch.fieldValue(out_normals, norm_idx);
                contentHandler.endNode();
                //contentHandler.endField();

                contentHandler.endNode();
                //contentHandler.endField();
                contentHandler.endNode();
            }

        } else {
            StringContentHandler sch = (StringContentHandler)contentHandler;
            StringArray out_coords = new StringArray();
            StringArray out_normals = new StringArray();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(5);

            for(int i = 0; i < num_objects; i++) {
                if(num_tris[i] == 0)
                    continue;

                out_coords.clear();
                out_normals.clear();

                contentHandler.startNode("Shape", obj_names[i]);
                contentHandler.startField("geometry");
                contentHandler.startNode("TriangleSet", null);

                for(int j = 0; j < num_tris[i]; j++) {

                    if (!rdr.getNextFacet(in_normal, in_coords)) {
                        break;
                    }

                    float n_x = (float)in_normal[0];
                    float n_y = (float)in_normal[1];
                    float n_z = (float)in_normal[2];

                    // do we need to autogenerate the normal?
                    if(n_x == 0 && n_y == 0 && n_z == 0) {
                        double x1 = in_coords[1][0] - in_coords[0][0];
                        double y1 = in_coords[1][1] - in_coords[0][1];
                        double z1 = in_coords[1][2] - in_coords[0][2];

                        double x2 = in_coords[2][0] - in_coords[0][0];
                        double y2 = in_coords[2][1] - in_coords[0][1];
                        double z2 = in_coords[2][2] - in_coords[0][2];

                        n_x = (float)(y1 * z2 - z1 * y2);
                        n_y = (float)(z1 * x2 - x1 * z2);
                        n_z = (float)(x1 * y2 - y1 * x2);
                    }

                    String n1 = formatter.format(n_x);
                    String n2 = formatter.format(n_y);
                    String n3 = formatter.format(n_z);

                    out_normals.add(n1);
                    out_normals.add(n2);
                    out_normals.add(n3);

                    out_coords.add(formatter.format(in_coords[0][0]));
                    out_coords.add(formatter.format(in_coords[0][1]));
                    out_coords.add(formatter.format(in_coords[0][2]));

                    out_coords.add(formatter.format(in_coords[1][0]));
                    out_coords.add(formatter.format(in_coords[1][1]));
                    out_coords.add(formatter.format(in_coords[1][2]));

                    out_coords.add(formatter.format(in_coords[2][0]));
                    out_coords.add(formatter.format(in_coords[2][1]));
                    out_coords.add(formatter.format(in_coords[2][2]));
                }

                contentHandler.startField("normalPerVertex");
                sch.fieldValue("FALSE");

                contentHandler.startField("coord");
                contentHandler.startNode("Coordinate", null);
                contentHandler.startField("point");
                sch.fieldValue(out_coords.toArray());
                contentHandler.endNode();
                //contentHandler.endField();

                contentHandler.startField("normal");
                contentHandler.startNode("Normal", null);
                contentHandler.startField("vector");
                sch.fieldValue(out_normals.toArray());
                contentHandler.endNode();
                //contentHandler.endField();

                contentHandler.endNode();
                //contentHandler.endField();
                contentHandler.endNode();
            }
        }
    }
}
