/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import org.ietf.uri.FileNameMap;
import org.ietf.uri.URI;
import org.ietf.uri.URIResourceStreamFactory;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.JavascriptResourceFactory;

import org.web3d.util.I18nUtils;

import org.web3d.vrml.export.*;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.InputSource;
import org.web3d.vrml.sav.VRMLParseException;

import xj3d.filter.exporter.STLBinaryFileExporter;
import xj3d.filter.exporter.STLFileExporter;
import xj3d.filter.exporter.TriangleCounter;
import xj3d.filter.exporter.OBJFileExporter;
import xj3d.filter.exporter.ThreeMFFileExporter;

/**
 * General X3D filter driver. Takes an input file or URL, filters
 * it and writes it out to a file.
 *
 * @author Alan Hudson
 * @version $Revision: 1.57 $
 */
public class CDFFilter {

    private static final int EXPORT_TYPE_ASCII = 0;
    private static final int EXPORT_TYPE_BINARY = 1;

    /**
     * App name to register preferences under
     */
    private static final String APP_NAME = "xj3d.filter.CDFFilter";

    /**
     * Error message when class can't be found
     */
    private static final String CREATE_MSG =
            "New filter instantiation exception";

    /**
     * Error message for constructor having non-public access type
     */
    private static final String ACCESS_MSG =
            "New filter IllegalAccess exception";

    /**
     * Error message for Parsing format error
     */
    private static final String PARSER_VRMLEXCEPTION_MSG =
            "File format error encountered in filter: ";

    /**
     * Error message for Parsing IO error
     */
    private static final String PARSER_IOEXCEPTION_MSG =
            "IO error encountered";

    private static final String PARSER_INVALID_FORMAT_MSG =
            "File is not formated as per specification";

    /**
     * Error message for internal exception in one of the filters
     */
    private static final String FILTER_INTERNAL_EXCEPTION =
            "Error encountered in filter: ";

    /**
     * Message for when a format is not handled
     */
    private static final String UNSUPPORTED_FORMAT_MSG_PROP =
            "xj3d.filter.CDFFilter.unsupportedFormat";

    private static final String UNKNOWN_FORMAT_MSG_PROP =
            "xj3d.filter.CDFFilter.unknownFormat";

    /**
     * The usage error message
     */
    private static final String USAGE_MESSAGE =
            "CDFFilter - usage:  filter [filters] input output [-loglevel type]\n" +
                    "   [-exportVersion n] [-compressionMethod n ] [-quantization n ] [-upgrade]\n" +
                    "   [-maxRunTime n] [filter_args] \n" +
                    "\n" +
                    "  -loglevel type [ALL|WARNINGS|ERRORS|FATAL|NONE]\n" +
                    "                 The minimum level that logs should be written at\n" +
                    "\n" +
                    "  -exportVersion n.n\n" +
                    "                 The exported version of the X3D specification to generate\n" +
                    "                 No error checking is performed for invalid version numbers\n" +
                    "                 Assumes 3.1 if not supplied\n" +
                    "\n" +
                    " -compressionMethod [FASTEST|SMALLEST|LOSSY|STRINGS]\n" +
                    "                 Define what sort of compression algorithms should be used\n" +
                    "                 when X3D Binary format is used for the output. Ignored in\n" +
                    "                 all other cases\n" +
                    "\n" +
                    " -exportType [ASCII|BINARY]\n" +
                    "                 When a format supports multiple types of encoding with the\n" +
                    "                 same file ending use this parameter to specify the ASCII or\n" +
                    "                 BINARY version. ASCII is the default. \n" +
                    "\n" +
                    " -quantization n\n" +
                    "                 Positive floating point value that states how much quantization\n" +
                    "                 of values is allowed in LOSSY mode. Default is 0.001\n" +
                    "\n" +
                    " -minFloatArraySizeToEncode n\n" +
                    "                 The smallest array size to encode with deltazlib encoder. \n" +
                    "                 Default is 7.\n" +
                    "\n" +
                    " -maxRunTime n\n" +
                    "                 Positive floating point value representing the maximum number of\n" +
                    "                 minutes the application is allowed to run before termination.\n" +
                    "                 If maxRunTime is not specified, no upper bound limits runtime." +
                    "\n" +
                    " -parsing [STRICT|TOLERANT]\n" +
                    "                 When parsing files do we attempt to fix common mistakes.  \n" +
                    "                 Default is strict parsing.\n" +
                    "\n" +
                    " -nonWeb3DStyle [UNCOLORED,MATRIX_TRANSFORM]\n" +
                    "                 Comma separated list of instructions to non web3d format importers on how to create X3D content  \n" +
                    "\n" +
                    " -upgrade\n" +
                    "                 When declared, any VRML style PROTO content that can be\n" +
                    "                 upgraded to X3D native nodes, will be\n";


    /**
     * Exit code for an IO exception - use the {@link #PARSER_IOEXCEPTION_MSG}.
     */
    public static final int IO_EXCEPTION = 2;

    /**
     * Default Largest acceptable error for float quantization
     */
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    /**
     * Default major spec version to export as in X3D
     */
    private static final int DEFAULT_OUPUT_MAJOR_VERSION = 3;

    /**
     * Default major spec version to export as in X3D
     */
    private static final int DEFAULT_OUPUT_MINOR_VERSION = 1;

    /**
     * List of known filter class names, mapped from a short name
     */
    private static Map<String, String> filters;

    /**
     * Output for sending messages to the outside world
     */
    private ErrorReporter errorReporter;

    /**
     * Application timer.  If -maxRunTime is set to a positive parseable number,
     * deathTimer will terminate application once that many minutes have passed.
     */
    private DeathTimer deathTimer;

    /**
     * How to style non-web3d input.  Particular to a specific importer.
     */
    private String[] style;

    static {
        initFilters();
    }

    /**
     * Create an instance of the demo class.
     */
    public CDFFilter() {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.xj3dResources");

        setupProperties();

        ParserNameMap content_map = new ParserNameMap();
        content_map.registerType("stl", "model/x-stl");
        content_map.registerType("obj", "model/x-obj");
        content_map.registerType("dae", "application/xml");
        content_map.registerType("ac", "application/x-ac3d");
        content_map.registerType("3mf", "application/xml");

        URI.setFileNameMap(content_map);

        style = null;
    }

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param filters The identifier of the filter type.
     * @param url     The URL to open.
     * @param out     The output filename.
     * @param fargs   The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, URL url, String out, String[] fargs) {
        return load(filters, url, null, out, null, null, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file    The file to load.
     * @param out     The output filename.
     * @param fargs   The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, File file, String out, String[] fargs) {
        return load(filters, null, file, out, null, null, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param filters The identifier of the filter type.
     * @param file    The file to load.
     * @param out     The output stream
     * @param enc     The encoding to write
     * @param fargs   The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(String[] filters, File file, OutputStream out, String enc, String[] fargs) {
        return load(filters, null, file, null, out, enc, fargs);
    }

    /**
     * Print out the filters available.
     */
    public void printFilters() {

        Set<String> ks = filters.keySet();
        Iterator<String> itr = ks.iterator();

        System.out.println("Available filters:");
        while (itr.hasNext()) {
            System.out.println("   " + itr.next());
        }

    }

    /**
     * Return an unmodifiable Map of the locally known filters.
     *
     * @return An unmodifiable Map of the locally known filters.
     */
    public static Map<String, String> getFilterMap() {
        return Collections.unmodifiableMap(filters);
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Initialize the filters.
     */
    private static void initFilters() {

        filters = new HashMap<>();

        filters.put("AbsScale", "xj3d.filter.filters.AbsScaleFilter");
        filters.put("AppearanceFilter", "xj3d.filter.AppearanceFilter");

        filters.put("Center", "xj3d.filter.filters.CenterFilter");
        filters.put("CenterFilter", "xj3d.filter.filters.CenterFilter");
        filters.put("ColorRGBAtoRGB",
                "xj3d.filter.filters.ColorRGBAtoRGBFilter");
        filters.put("CombineAppearances",
                "xj3d.filter.filters.CombineAppearanceFilter");
        filters.put("CombineShapes",
                "xj3d.filter.filters.CombineShapeFilter");
        filters.put("CombineTransform",
                "xj3d.filter.filters.CombineTransformFilter");
        filters.put("Combiner",
                "xj3d.filter.filters.CombinerFilter");

        filters.put("Debug", "xj3d.filter.filters.DebugFilter");
        filters.put("DEFChooser",
                "xj3d.filter.filters.DEFChooserFilter");
        filters.put("DEFUSEImageTexture",
                "xj3d.filter.DEFUSEImageTextureFilter");
        filters.put("DEFReplacer",
                "xj3d.filter.filters.DEFReplacerFilter");

        filters.put("ExplodeShapes",
                "xj3d.filter.filters.ExplodeShapesFilter");
        filters.put("ExportPixelTexture",
                "xj3d.filter.filters.ExportPixelTextureFilter");

        filters.put("FlattenSelectable",
                "xj3d.filter.filters.FlattenSelectableFilter");
        filters.put("FlattenTextureTransform",
                "xj3d.filter.filters.FlattenTextureTransformFilter");
        filters.put("FlattenTransform",
                "xj3d.filter.filters.FlattenTransformFilter");

        filters.put("Identity", "xj3d.filter.IdentityFilter");
        filters.put("Index", "xj3d.filter.filters.IndexFilter");
        filters.put("IFSFilter", "xj3d.filter.IFSFilter");
        filters.put("IFSToITS", "xj3d.filter.filters.IFSToITSFilter");
        filters.put("IFSToTS", "xj3d.filter.IFSToTSFilter");
        filters.put("ITSCreaseAngler",
                "xj3d.filter.filters.ITSCreaseAnglerFilter");

        filters.put("GenNormals", "xj3d.filter.filters.GenNormalsFilter");
        filters.put("GlobalBounds",
                "xj3d.filter.filters.GlobalBoundsFilter");

        filters.put("LocalURL",
                "xj3d.filter.filters.LocalURLFilter");

        filters.put("ManifoldInfo",
                "xj3d.filter.filters.manifold.ManifoldInfoFilter");
        filters.put("MaterialFilter", "xj3d.filter.MaterialFilter");
        filters.put("MinProfile", "xj3d.filter.MinimizeProfileFilter");
        filters.put("ModifyViewpoint", "xj3d.filter.ModifyViewpointFilter");

        filters.put("NodeCountInfo",
                "xj3d.filter.filters.NodeCountInfoFilter");

        filters.put("ReIndex", "xj3d.filter.filters.ReindexFilter");
        filters.put("RemoveNode",
                "xj3d.filter.filters.RemoveNodeFilter");
        filters.put("RemoveNormals",
                "xj3d.filter.RemoveNormalsFilter");
        filters.put("RemoveUnusedDEF",
                "xj3d.filter.filters.RemoveUnusedDEFFilter");

        filters.put("ShortenDEF", "xj3d.filter.ShortenDEFFilter");
        filters.put("StackTracer", "xj3d.filter.filters.StackTracerFilter");

        filters.put("Transform", "xj3d.filter.filters.TransformFilter");
        filters.put("TextureTransform",
                "xj3d.filter.filters.TextureTransformFilter");
        filters.put("TriangleCountInfo",
                "xj3d.filter.filters.TriangleCountInfoFilter");
        filters.put("Triangulation",
                "xj3d.filter.filters.TriangulationFilter");

        filters.put("URLCaseCorrector",
                "xj3d.filter.filters.URLCaseCorrectorFilter");
        filters.put("URLFullyQualify",
                "xj3d.filter.filters.URLFullyQualifyFilter");
        filters.put("USERedundantNode",
                "xj3d.filter.filters.USERedundantNodeFilter");

        filters.put("ValidateIndex",
                "xj3d.filter.filters.ValidateIndexFilter");

        filters.put("WorldOffset",
                "xj3d.filter.filters.WorldOffsetFilter");
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param filterNames The identifier of the filter type.
     * @param url         The URL to open, or null if the input is specified by the file argument.
     * @param inFile      The file to load, or null if the input is specified by the url argument.
     * @param out         The output filename.
     * @param outEncoding The encoding to use on output
     * @param filterArgs  The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private int load(String[] filterNames,
                     URL url,
                     File inFile,
                     String out,
                     OutputStream outStream,
                     String outEncoding,
                     String[] filterArgs) {

        int log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;

        int export_major_version = DEFAULT_OUPUT_MAJOR_VERSION;
        int export_minor_version = DEFAULT_OUPUT_MINOR_VERSION;
        //int method = X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY;
        int method = X3DBinarySerializer.METHOD_FASTEST_PARSING;
        int export_type = EXPORT_TYPE_ASCII;
        ParsingType parsing_type = ParsingType.STRICT;
        float quantization = PARAM_FLOAT_LOSSY;
        int minFloatArraySizeToEncode = -1;

        boolean upgrade = false;
        long millisToWait = 0;
        boolean old_method = false;

        //
        // EMF: If for some reason we have an indexing problem or for
        // some reason we cannot parse a value, it means that the
        // filter arguments have not been formatted correctly, and
        // so we will throw a FilterExitCodes.INVALID_ARGUMENTS error.
        //
        try {
            for (int i = 0; i < filterArgs.length; i++) {

                String currentArg = filterArgs[i];

                //
                // old comment: "find the log level. Should always be
                // the first argument, if supplied."
                //
                switch (currentArg) {
                    case "-loglevel":
                        String lvl = filterArgs[++i];
                        switch (lvl) {
                            case "ALL":
                                log_level = FilterErrorReporter.PRINT_ALL;
                                break;
                            case "WARNINGS":
                                log_level = FilterErrorReporter.PRINT_WARNINGS;
                                break;
                            case "ERRORS":
                                log_level = FilterErrorReporter.PRINT_ERRORS;
                                break;
                            case "FATAL":
                                log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;
                                break;
                            case "NONE":
                                log_level = FilterErrorReporter.PRINT_NONE;
                                break;
                        }
                        break;
                    case "-exportVersion":
                        int[] v = parseVersionString(filterArgs[++i]);
                        export_major_version = v[0];
                        export_minor_version = v[1];
                        break;
                    case "-upgrade":
                        upgrade = true;
                        break;
                    case "-compressionMethod":
                        String compMethod = filterArgs[++i];
                        switch (compMethod) {
                            case "FASTEST":
                                method = X3DBinarySerializer.METHOD_FASTEST_PARSING;
                                break;
                            case "SMALLEST":
                                method = X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY;
                                break;
                            case "LOSSY":
                                method = X3DBinarySerializer.METHOD_SMALLEST_LOSSY;
                                break;
                            case "STRINGS":
                                method = X3DBinarySerializer.METHOD_STRINGS;
                                break;
                            default:
                                // System.out.println("Unknown compression method");
                                System.exit(FilterExitCodes.INVALID_ARGUMENTS);
                        }
                        break;
                    case "-exportType":
                        String exportTypeStr = filterArgs[++i];
                        switch (exportTypeStr) {
                            case "ASCII":
                                export_type = EXPORT_TYPE_ASCII;
                                break;
                            case "BINARY":
                                export_type = EXPORT_TYPE_BINARY;
                                break;
                            default:
                                System.err.println("Unknown export type: " + exportTypeStr);
                                System.exit(FilterExitCodes.INVALID_ARGUMENTS);
                        }
                        break;
                    case "-parsing":
                        String parsingTypeStr = filterArgs[++i];
                        if (parsingTypeStr.equalsIgnoreCase("STRICT")) {
                            parsing_type = ParsingType.STRICT;
                        } else if (parsingTypeStr.equals("TOLERANT")) {
                            parsing_type = ParsingType.TOLERANT;
                        } else {
                            System.err.println("Unknown parsing type: " + parsingTypeStr);
                            System.exit(FilterExitCodes.INVALID_ARGUMENTS);
                        }
                        break;
                    case "-useOldBinary":
                        old_method = true;
                        break;
                    case "-minFloatArraySizeToEncode":
                        //
                        // As with -maxRunTime, if we are unable to parse quantization
                        // this will be caught by the enclosing try/catch bloack.
                        //
                        minFloatArraySizeToEncode = Integer.parseInt(filterArgs[++i]);
                        break;
                    case "-quantization":
                        //
                        // As with -maxRunTime, if we are unable to parse quantization
                        // this will be caught by the enclosing try/catch bloack.
                        //
                        quantization = Float.parseFloat(filterArgs[++i]);
                        break;
                    case "-maxRunTime":
                        //
                        // Convert the float value of minutes to wait into
                        // milliseconds.  There are 1000 millis per second,
                        // and therefore 60k milliseconds per minute.
                        // Note: NumberFormatExceptions should be caught
                        // by the outer try/catch block
                        //
                        Float maxTime = Float.parseFloat(filterArgs[++i]);
                        millisToWait = (long) (60000f * maxTime);
                        break;
                    case "-nonWeb3DStyle":
                        style = filterArgs[++i].split(",");
                        break;
                }
            }
        } catch (NumberFormatException e) {
            //
            // Expecting to catch IndexOutOfBoundsExceptions and
            // NumberFormatExceptions
            //
            System.exit(FilterExitCodes.INVALID_ARGUMENTS);
        }

        errorReporter = new FilterErrorReporter(log_level);

        errorReporter.messageReport("Exporting as version: " +
                export_major_version +
                "." +
                export_minor_version);

        AbstractFilter previous_filter = null;
        AbstractFilter last_filter = null;
        AbstractFilter first_filter = null;

        List<AbstractFilter> all_filters = new ArrayList<>();

        for (int i = 0; i < filterNames.length; i++) {
            String filter_name = filters.get(filterNames[i]);

            // If not one of the stock ones, try seeing if it is a class name
            // and load it directly as one.

            if (filter_name == null) {
                filter_name = filterNames[i];
            }

            AbstractFilter filter = loadFilterClass(filter_name);

            if (filter == null) {
                System.err.println("Invalid filter: " + filter_name);
                return FilterExitCodes.INVALID_FILTER_SPECIFIED;
            }

            filter.setParsingType(parsing_type);

            if (filter instanceof TwoPassFilter) {
                filter = new TwoPassFilterWrapper((TwoPassFilter) filter);
            }

            try {
                filter.setArguments(filterArgs);
            } catch (IllegalArgumentException iae) {
                return FilterExitCodes.INVALID_ARGUMENTS;
            }

// Do we have one of these yet?
//            filter.setLocator();
            filter.setErrorReporter(errorReporter);

            if (i != 0) {
                previous_filter.setContentHandler(filter);
                previous_filter.setScriptHandler(filter);
                previous_filter.setProtoHandler(filter);
                previous_filter.setRouteHandler(filter);
            } else {
                first_filter = filter;
            }

            previous_filter = filter;
            last_filter = filter;

            all_filters.add(filter);
        }

        InputSource is = null;
        if (url != null) {
            is = new InputSource(url);
        } else if (inFile != null) {
            is = new InputSource(inFile);
        }

        File tmpOutFile = null;
        File outFile = null;

        if (out != null) {
            outFile = new File(out);
        }

        if ((inFile != null) && (inFile.equals(outFile))) {
            // the input and output files are the same, arrange
            // for the output to be written to a tmp file
            tmpOutFile = new File(outFile.getParentFile(), "tmp_" + outFile.getName());
        }

        OutputStream fos;
        OutputStream outputStream;
        Exporter writer;

        try {
            if (tmpOutFile != null) {
                fos = new BufferedOutputStream(new FileOutputStream(tmpOutFile), 8 * 1_024);
            } else {
                if (out != null) {
                    fos = new BufferedOutputStream(new FileOutputStream(outFile), 8 * 1_024);
                } else if (outStream != null)
                    fos = outStream;
                else
                    fos = new BufferedOutputStream(new ByteArrayOutputStream(), 8 * 1_024);
            }
        } catch (FileNotFoundException ioe) {
            errorReporter.errorReport("Unable to open output file: ", ioe);
            return FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE;
        }

        String encoding = null;
        boolean gzipCompression = false;
        boolean zipCompression = false;

        if (out != null) {
            int idxDot = out.lastIndexOf(".");
            if (idxDot < 0) {
                errorReporter.messageReport("Unknown destination file type");
                return FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE;
            }

            encoding = out.substring(idxDot + 1);

            // Forcing gzip compression? Let's go find the next index of the
            // period to find out the raw encoding to be used.
            switch (encoding) {
                case "gz":
                    gzipCompression = true;
                    String baseFilename = out.substring(0, idxDot - 1);

                    idxDot = baseFilename.lastIndexOf(".");
                    encoding = baseFilename.substring(idxDot + 1);
                    break;
                case "x3dvz":
                case "x3dz":
                    gzipCompression = true;
                    break;
                case "3mf":
                    zipCompression = true;
                    break;
            }

            errorReporter.messageReport("Encoding: " + encoding);
        } else if (outStream != null) {
            encoding = outEncoding;
        }

        // If gzip is needed, change the output stream to wrap it in a
        // compressed version.
        if (gzipCompression) {
            try {
                outputStream = new GZIPOutputStream(fos);
            } catch (IOException ioe) {
                errorReporter.warningReport("Unable to create GZIP output", ioe);
                outputStream = fos;
            }
        } else if (zipCompression) {
            try {
                outputStream = new ArchiveStreamFactory()
                        .createArchiveOutputStream(ArchiveStreamFactory.ZIP, fos);
            } catch(ArchiveException ae) {
                errorReporter.warningReport("Unable to create ZIP output", ae);
                outputStream = fos;
            }
        } else {
            outputStream = fos;
        }

        // JC:
        // Fixed output for the spec version. Not so good. We should have some
        // sort of flags that allow us to specify which spec version we want as
        // output and then let the stream handle it.

        if (out == null && outStream == null) {
            writer = new NullExporter(export_major_version,
                    export_minor_version,
                    errorReporter);
        } else if (encoding.equals("x3db")) {
            writer = new X3DBinaryRetainedDirectExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter,
                    method,
                    quantization, old_method);
            ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);

            if (minFloatArraySizeToEncode >= 0) {
                ((X3DBinaryRetainedDirectExporter) writer).setMinFloatArraySizeToEncode(minFloatArraySizeToEncode);
            }
        } else if (encoding.equals("x3dv")) {
            writer = new X3DClassicRetainedExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter);
            ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);
        } else if (encoding.equals("x3d")) {
            writer = new X3DXMLRetainedExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter);
            ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);
        } else if (encoding.equals("stl")) {
            if (export_type == EXPORT_TYPE_ASCII) {
                writer = new STLFileExporter(outputStream,
                        export_major_version,
                        export_minor_version,
                        errorReporter);
            } else {
                last_filter = addTriangleCounter(last_filter);
                writer = new STLBinaryFileExporter(outputStream,
                        export_major_version,
                        export_minor_version,
                        (TriangleCounter) last_filter,
                        errorReporter);
            }
        } else if (encoding.equals("obj")) {
            writer = new OBJFileExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter);
        } else if (encoding.equals("wrl")) {
            writer = new VrmlExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter, true);
        } else if (encoding.equals("3mf")) {
            writer = new ThreeMFFileExporter(outputStream,
                    export_major_version,
                    export_minor_version,
                    errorReporter);

        } else {
            errorReporter.fatalErrorReport("Unknown destination encoding:" +
                    encoding, null);
            return FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE;
        }

        last_filter.setContentHandler(writer);
        last_filter.setRouteHandler(writer);
        last_filter.setScriptHandler(writer);
        last_filter.setProtoHandler(writer);

        FileParserReader reader = new FileParserReader(style, parsing_type);
        reader.registerImporter("model/x-stl",
                "xj3d.filter.importer.STLFileParser",
                "stl");
        reader.registerImporter("model/x-obj",
                "xj3d.filter.importer.OBJFileParser",
                "obj");
        reader.registerImporter("application/xml",
                "xj3d.filter.importer.collada.ColladaFileParser",
                "dae");
        reader.registerImporter("application/x-ac3d",
                "xj3d.filter.importer.AC3DFileParser",
                "ac3d");
        reader.registerImporter("application/xml",
                "xj3d.filter.importer.threemf.ThreeMFFileParser",
                "3mf");

        reader.setContentHandler(first_filter);
        reader.setRouteHandler(first_filter);
        reader.setScriptHandler(first_filter);
        reader.setProtoHandler(first_filter);
        reader.setErrorReporter(errorReporter);

        List<String> parsing_messages;

        try {

            //
            // Begin the main parsing work.
            // Start the timeout counter if millisToWait has been set.
            //
            if (millisToWait > 0) {
                deathTimer = new DeathTimer(millisToWait);
                deathTimer.start();
            }

            parsing_messages = reader.parse(is, style);
        } catch (FilterProcessingException fpe) {
            errorReporter.fatalErrorReport("Filter Error for " +
                    fpe.getFilterName(), null);
            return fpe.getErrorCode();
        } catch (VRMLException e) {
            int error_code;
            String filter_name;

            // Find the error code in the filters. Look for the first non-zero
            // item.

            error_code = writer.getLastErrorCode();
            filter_name = "Exporter";

            if (error_code == 0) {
                for (int i = 0; i < all_filters.size(); i++) {
                    AbstractFilter f = all_filters.get(i);
                    error_code = f.getLastErrorCode();

                    if (error_code != 0) {
                        filter_name = filterNames[i];
                        break;
                    }
                }
            }

            if (error_code == 0) {
                if (e instanceof VRMLParseException) {
                    String msg = e.getMessage();
                    if (msg.equalsIgnoreCase("Unsupported version V1.0")) {
                        I18nUtils.printMsg(UNSUPPORTED_FORMAT_MSG_PROP, I18nUtils.CRIT_MSG,
                                new String[]{"VRML 1.0"});
                        return FilterExitCodes.UNSUPPORTED_FORMAT;
                    } else if (msg.equalsIgnoreCase("Unsupported Format")) {
                        I18nUtils.printMsg(UNKNOWN_FORMAT_MSG_PROP, I18nUtils.CRIT_MSG,
                                null);
                        return FilterExitCodes.UNSUPPORTED_FORMAT;
                    } else if (msg.equalsIgnoreCase("Header missing #VRML or #X3D statement")) {
                        I18nUtils.printMsg(UNSUPPORTED_FORMAT_MSG_PROP, I18nUtils.CRIT_MSG,
                                new String[]{"Unknown format, missing header"});

                        return FilterExitCodes.UNSUPPORTED_FORMAT;
                    } else if (msg.contains("ClassCastException")) {
                        System.err.println("Class Cast Exception?");
                        return FilterExitCodes.EXCEPTIONAL_ERROR;
                    }
                }

                errorReporter.fatalErrorReport(PARSER_VRMLEXCEPTION_MSG + filter_name, e);
                return FilterExitCodes.INVALID_INPUT_FILE;
            } else {
                String msg = FILTER_INTERNAL_EXCEPTION + filter_name + " msg: " + e.getMessage();
                errorReporter.fatalErrorReport(msg, null);
                return error_code;
            }
        } catch (IOException ioe) {
            errorReporter.fatalErrorReport(PARSER_IOEXCEPTION_MSG, ioe);
            return IO_EXCEPTION;
        } catch (InvalidFormatException ife) {

            errorReporter.fatalErrorReport(PARSER_INVALID_FORMAT_MSG, ife);

            return FilterExitCodes.INVALID_INPUT_FILE;
        } catch (OutOfMemoryError oom) {
            // Unable to use error reporting because Error and Exception are
            // different subclasses
            errorReporter.messageReport("Out of memory error.");
            errorReporter.messageReport(oom.getMessage());
            return FilterExitCodes.OUT_OF_MEMORY;
        } catch (Error e) {

            // Unable to use error reporting because Error and Exception are
            // different subclasses
            errorReporter.messageReport("Unhandled error of type " +
                    e.getClass().getName());
            errorReporter.messageReport(e.getMessage());
            // Check for thread death to avoid doing the cleanup
            if (e instanceof ThreadDeath)
                throw e;

            return FilterExitCodes.EXCEPTIONAL_ERROR;
        } finally {
            try {
                // clean up...
                if (is != null)
                    is.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException ioe) {
                errorReporter.errorReport("Unable to write output file", ioe);
                return FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE;
            }
        }

        if (tmpOutFile != null) {
            // the presence of a tmpOutFile means that what we really
            // want is to replace the input file with the output
            if (!inFile.delete()) {
                errorReporter.fatalErrorReport("Could not delete original input file " +
                        inFile, null);
            } else if (!tmpOutFile.renameTo(outFile)) {
                errorReporter.fatalErrorReport("Could not rename tmp output file " +
                        tmpOutFile, null);
            }
        }

        if (parsing_messages != null) {
            Iterator<String> itr = parsing_messages.iterator();
            String base_msg = I18nManager.getManager().getString("xj3d.filter.CDFFilter.parsingProblem");
            while (itr.hasNext()) {
                System.err.println(I18nUtils.EXT_MSG + base_msg + itr.next());
            }

            // Revisit a separate exit code later
//            return FilterExitCodes.RECOVERED_PARSING;
            return FilterExitCodes.SUCCESS;
        }

        return FilterExitCodes.SUCCESS;
    }

    /**
     * Load a filter class from the given class name. If the filter cannot be
     * loaded, null is returned.
     *
     * @param classname The fully qualified name of the class needed
     * @return The filter loaded up from the class name
     */
    private AbstractFilter loadFilterClass(String classname) {
        AbstractFilter ret_val = null;

        try {
            Class<?> cls = Class.forName(classname);
            ret_val = (AbstractFilter) cls.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | InvocationTargetException cnfe) {
            errorReporter.errorReport(CREATE_MSG, cnfe);
        } catch (IllegalAccessException iae) {
            errorReporter.errorReport(ACCESS_MSG, iae);
        }

        return ret_val;
    }

    /**
     * Parse the given version string and return the version numbers. It is
     * expecting the form "major.minor". If the minor is missing (ie there is
     * no period character then treat it as the major version number and use
     * the default version number from the class constants.
     *
     * @param version The string to parse
     * @return The major and minor versions in the array [major, minor]
     */
    private int[] parseVersionString(String version) {
        int[] ret_val = {DEFAULT_OUPUT_MAJOR_VERSION,
                DEFAULT_OUPUT_MINOR_VERSION};

        int idx = version.indexOf(".");

        if (idx == -1) {
            try {
                ret_val[0] = Integer.parseInt(version);
            } catch (NumberFormatException nfe) {
            }
        } else {
            String maj = version.substring(0, idx);
            String min = version.substring(idx + 1);

            try {
                ret_val[0] = Integer.parseInt(maj);
                ret_val[1] = Integer.parseInt(min);
            } catch (NumberFormatException nfe) {
            }
        }

        return ret_val;
    }

    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties() {
        System.setProperty("uri.content.handler.pkgs",
                "vlc.net.content");

        System.setProperty("uri.protocol.handler.pkgs",
                "vlc.net.protocol");

        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if (!(res_fac instanceof JavascriptResourceFactory)) {
            res_fac = new JavascriptResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }
/*
        ContentHandlerFactory c_fac = URI.getContentHandlerFactory();
        if(!(c_fac instanceof VRMLContentHandlerFactory)) {
            c_fac = new VRMLContentHandlerFactory(core, loader, c_fac);
            URI.setContentHandlerFactory(c_fac);
        }
*/
        FileNameMap fn_map = URI.getFileNameMap();
        if (!(fn_map instanceof VRMLFileNameMap)) {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }
    }

    /**
     * Print out usage information
     *
     * @param filterer The filter instance to use
     */
    private static void printUsage(CDFFilter filterer) {
        System.out.println(USAGE_MESSAGE);

        filterer.printFilters();
    }

    /**
     * Add a triangle counter to the end of the stream if not there.
     * TODO:  Stream must be non-streaming for this to work.  In the future
     * add in a stream buffering version of TriangleCountInfo.
     */
    private AbstractFilter addTriangleCounter(AbstractFilter lastFilter) {
        if (!(lastFilter instanceof TriangleCounter)) {
            AbstractFilter filter = loadFilterClass("xj3d.filter.filters.TriangleCountInfoFilter");

            if (filter == null) {
                System.out.println("Cannot load TriangleCountInfoFilter");
            }

            lastFilter.setContentHandler(filter);
            lastFilter.setScriptHandler(filter);
            lastFilter.setProtoHandler(filter);
            lastFilter.setRouteHandler(filter);

            return filter;
        } else {
            return lastFilter;
        }
    }

    /**
     * Execute a chain of filters.
     *
     * @param args    The list of arguments for this application.
     * @param exit    Should we use system exit
     * @param ostream If present output to this stream instead of a file
     * @param enc     If ostream is used this specifies the encoding otherwise it's ignored.
     * @return The exit code
     */
    public static int executeFilters(String[] args, boolean exit, OutputStream ostream, String enc) {
        CDFFilter filterer = new CDFFilter();

        String filename = null;
        String outfile = null;
        String[] filters = null;

        String[] filter_args = null;
        filterer.deathTimer = null;

        int num_args = args.length;
        if (num_args < 3) {
            printUsage(filterer);

            if (exit)
                System.exit(FilterExitCodes.INVALID_ARGUMENTS);
            else
                return FilterExitCodes.INVALID_ARGUMENTS;
        } else {
            if (num_args > 3) {
                // Work through the list of arguments looking for the first one
                // that starts with a - The first two items before that are
                int filter_count = 0;
                for (String arg : args) {
                    if (arg.charAt(0) == '-') {
                        break;
                    }
                    filter_count++;
                }

                filters = new String[filter_count - 2];

                filename = args[filter_count - 2];
                outfile = args[filter_count - 1];

                if (outfile.equals("NULL"))
                    outfile = null;

                System.arraycopy(args, 0, filters, 0, filter_count - 2);
                int num_filter_args = num_args - filter_count;

                filter_args = new String[num_filter_args];
                System.arraycopy(args,
                        filter_count,
                        filter_args,
                        0,
                        num_filter_args);

            } else {
                filters = new String[1];
                filters[0] = args[0];
                filename = args[1];
                outfile = args[2];
                filter_args = new String[0];

                if (outfile.equals("NULL"))
                    outfile = null;
            }
        }

        int status;
        File fil = new File(filename);

        //
        // Begin the filter process.
        //
        try {
            if (fil.exists()) {
                if (fil.length() == 0) {
                    System.out.println("Empty File: " + filename);
                    status = FilterExitCodes.INVALID_INPUT_FILE;
                } else {
                    if (outfile != null)
                        status = filterer.filter(filters, fil, outfile, filter_args);
                    else
                        status = filterer.filter(filters, fil, ostream, enc, filter_args);
                }
            } else {
                try {
                    URL url = new URL(filename);
                    status = filterer.filter(filters, url, outfile, filter_args);
                } catch (MalformedURLException mfe) {
                    System.err.println("Malformed URL: " + filename);
                    status = FilterExitCodes.FILE_NOT_FOUND;
                }
            }
        } catch (InvalidFormatException ife) {
            String base_msg = I18nManager.getManager().getString("xj3d.filter.CDFFilter.parsingProblem");
            System.err.println("CRITMSG:" + base_msg + ife.getMessage());
            return FilterExitCodes.INVALID_INPUT_FILE;
        } catch (Exception e) {
            System.err.println("Unhandled exception: " + e);
            status = FilterExitCodes.ABNORMAL_CRASH;
        } catch (OutOfMemoryError oom) {
            System.err.println("Out of memory error: " + oom);
            status = FilterExitCodes.OUT_OF_MEMORY;
        } catch (Error e) {
            System.err.println("Unhandled error: " + e);
            status = FilterExitCodes.EXCEPTIONAL_ERROR;
            // Check for thread death to avoid doing the cleanup
            if (e instanceof ThreadDeath)
                throw e;
        }
        //
        // Application has finished, so shut down deathTimer to avoid an
        // out-of-time termination.  Note that calls to deathTimer.exit()
        // are fine to make whether or not deathTimer.start() has been called
        //
        if (filterer.deathTimer != null) {
            filterer.deathTimer.exit();
        }

        if (exit) {
            if (status != 0) {
                System.err.println("Exiting with error: " + status);
            }
            System.exit(status);
        }

        return status;
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param args The list of arguments for this application.
     */
    public static void main(String[] args) {
        executeFilters(args, true, null, null);
    }
}
