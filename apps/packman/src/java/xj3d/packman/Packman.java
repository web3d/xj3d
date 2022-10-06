/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.packman;

// External imports
import java.io.*;
import java.util.*;

import org.ietf.uri.*;

import java.net.MalformedURLException;
import java.net.URL;

// Local imports
import org.web3d.vrml.export.*;

import org.web3d.vrml.sav.*;

import org.j3d.util.ErrorReporter;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.net.protocol.JavascriptResourceFactory;

import org.web3d.vrml.lang.VRMLException;

/**
 * General X3D filter driver. Takes an input file or URL and jar, filters
 * the file and sub urls and writes them to the jar.
 *
 * @author Ben Yarger
 * @version $Revision: 1.6 $
 */
public class Packman
{
    /** Error message when class can't be found */
    private static final String CREATE_MSG =
        "New node instantiation exception";

    /** Error message for constructor having non-public access type */
    private static final String ACCESS_MSG =
        "New now IllegalAccess exception";

    /** Error message for Parsing format error */
    private static final String PARSER_VRMLEXCEPTION_MSG =
        "File format error encountered";

    /** Error message for Parsing IO error */
    private static final String PARSER_IOEXCEPTION_MSG =
        "IO error encountered";

    /** Error message for internal exception in one of the filters */
    private static final String FILTER_INTERNAL_EXCEPTION =
        "Error encountered in filter";

    /** The usage error message */
    private static final String USAGE_MESSAGE =
    	"Packman - usage: packman input output [-convert type] " +
    	"[-compressionMethod type]\n" +
    	"\n" +
    	" -convert type [X3D|X3DV|X3DB|NONE]\n" +
    	"	The type of conversion to use on files processed. " +
    	"Default is NONE.\n"+
    	"\n" +
    	" -compressionMethod type [FASTEST|SMALLEST|LOSSY|STRINGS]\n" +
    	"	Define what sort of compression algorithms should be used " +
        "when X3D Binary format is used for the output. Ignored in " +
        "all other cases\n";


    /** Exit code for general error condition */
    public static final int ERROR = -1;

    /** Exit code for success condition */
    public static final int SUCCESS = 0;

    /** Exit code for input file not found condition */
    public static final int INPUT_FILE_NOT_FOUND = 1;

    /** Exit code for invalid input file format condition */
    public static final int INVALID_INPUT_FILE_FORMAT = 2;

    /** Exit code for output file write error condition */
    public static final int OUTPUT_FILE_ERROR = 5;

    /** Exit code for invalid filter argument condition */
    public static final int INVALID_FILTER_ARGUMENTS = 6;

    /** Exit code for invalid filter specified condition */
    public static final int INVALID_FILTER_SPECIFIED = 7;

    /** Exit code for out of memory */
    public static final int OUT_OF_MEMORY_ERROR = 102;

    /* Exit code for an unhandled exception in a filter */
    public static final int UNHANDLED_EXCEPTION = 101;

    /* Exit code for an unhandled Error being thrown */
    public static final int UNHANDLED_ERROR = 103;

    /** Default Largest acceptable error for float quantization */
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    /** Default major spec version to export as in X3D */
    private static final int DEFAULT_OUPUT_MAJOR_VERSION = 3;

    /** Default major spec version to export as in X3D */
    private static final int DEFAULT_OUPUT_MINOR_VERSION = 1;

    /** Conversion specifier for files examined specified as none. */
    public static final int CONVERT_TYPE_NONE = 0;

    /** Conversion specifier for files examined specified as x3d. */
    public static final int CONVERT_TYPE_X3D = 1;

    /** Conversion specifier for files examined specified as x3dv. */
    public static final int CONVERT_TYPE_X3DV = 2;

    /** Conversion specifier for files examined specified as x3db. */
    public static final int CONVERT_TYPE_X3DB = 3;

    /** Conversion type specified by user. */
    private static int conversion_type;

    /** List of known filter class names, mapped from a short name */
    private HashMap<String, String> filters;

    /** Output for sending messages to the outside world */
    private ErrorReporter errorReporter;

    /**
     * Create an instance of the demo class.
     */
    public Packman()
    {
        setupProperties();
        initFilters();

        conversion_type = CONVERT_TYPE_NONE;
    }

    /**
     * Returns back the type of conversion requested by the user.
     *
     * @return int The conversion type. See conversion specifier fields.
     */
    public static int getConversionType()
    {
		return conversion_type;
	}

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(URL url, String out, String[] fargs)
    {
        return load(url, null, out, fargs);
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load.
     * @param out The output filename.
     * @param fargs The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    public int filter(File file, String out, String[] fargs)
    {
        return load(null, file, out, fargs);
    }


    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Initialize the filters.
     *
     */
    private void initFilters()
    {
        filters = new HashMap();
        filters.put("Identity", "xj3d.packman.IdentityFilter");
        filters.put("URL", "xj3d.packman.URLFilter");
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param filter The identifier of the filter type.
     * @param url The URL to open, or null if the input is specified by the file argument.
     * @param inFile The file to load, or null if the input is specified by the url argument.
     * @param out The output filename.
     * @param filter_args The argument array to pass into the filter class.
     * @return The status code indicating success or failure.
     */
    private int load(URL url,
                     File inFile,
                     String out,
                     String[] filterArgs)
    {
		// Instead of PRINT_FATAL_ERRORS, could use:
		// PRINT_ALL|PRINT_WARNINGS|PRINT_ERRORS|PRINT_FATAL_ERRORS|PRINT_NONE
        int log_level = FilterErrorReporter.PRINT_FATAL_ERRORS;

        int export_major_version = DEFAULT_OUPUT_MAJOR_VERSION;
        int export_minor_version = DEFAULT_OUPUT_MINOR_VERSION;
        int method = X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY;

        // parse the optional arguments.
        if(filterArgs.length > 0)
        {
            for(int i = 0; i < filterArgs.length; i++)
            {
                switch (filterArgs[i]) {
                    case "-compressionMethod":
                        switch (filterArgs[i + 1]) {
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
                                System.out.println("Unknown compression method. Defaulting to fastest.");
                                method = X3DBinarySerializer.METHOD_FASTEST_PARSING;
                                break;
                        }
                        i++;
                        break;
                    case "-convert":
                        switch (filterArgs[i+1]) {
                            case "X3D":
                                conversion_type = CONVERT_TYPE_X3D;
                                break;
                            case "X3DV":
                                conversion_type = CONVERT_TYPE_X3DV;
                                break;
                            case "X3DB":
                                conversion_type = CONVERT_TYPE_X3DB;
                                break;
                            default:
                                System.out.println("Unidentified conversion type specified. Defaulting to none.");
                                conversion_type = CONVERT_TYPE_NONE;
                                break;
                        }
                        i++;
                        break;
                }
            }
        }

        errorReporter = new FilterErrorReporter(log_level);

        errorReporter.messageReport("Exporting as version: " +
                                    export_major_version +
                                    "." +
                                    export_minor_version);

        AbstractFilter previous_filter = null;
        AbstractFilter last_filter = null;
        AbstractFilter first_filter = null;

		String filter_name = filters.get("URL");

		AbstractFilter filter = loadFilterClass(filter_name);

		if (filter == null)
		{
			return INVALID_FILTER_SPECIFIED;
		}

		// Create queue and add path to filter arguments
		URLQueue urlQueue = new URLQueue();
		urlQueue.initialize();
		String rootPathValue = "";

		if (url != null)
		{
			errorReporter.messageReport("No parsing of URL's is performed at this time.");
			return ERROR;
		}
		else if (inFile != null)
		{
			String fileName = inFile.getName();

			if(conversion_type != CONVERT_TYPE_NONE)
			{
				int extensionIndex = fileName.lastIndexOf(".");

				if(extensionIndex != -1)
				{
					String extension = fileName.substring(extensionIndex+1);

					if(extension.equals("x3d") || extension.equals("x3dv") || extension.equals("x3db"))
					{
						if(conversion_type == CONVERT_TYPE_X3D)
						{
							fileName = fileName.substring(0, extensionIndex) + ".x3d";
						}
						else if(conversion_type == CONVERT_TYPE_X3DV)
						{
							fileName = fileName.substring(0, extensionIndex) + ".x3dv";
						}
						else if(conversion_type == CONVERT_TYPE_X3DB)
						{
							fileName = fileName.substring(0, extensionIndex) + ".x3db";
						}
						else
						{
							errorReporter.fatalErrorReport("Unable to interpret conversion type specified.", null);
							return ERROR;
						}
					}
				}
				else
				{
					errorReporter.fatalErrorReport("Unable to retreive file extension.", null);
					return ERROR;
				}
			}

			urlQueue.push(inFile.getPath(), fileName);
			rootPathValue = inFile.getPath();
        }

		String[] filterArgsPlus = new String[filterArgs.length+2];
		System.arraycopy(filterArgs, 0, filterArgsPlus, 0, filterArgs.length);
		filterArgsPlus[filterArgsPlus.length-2] = "-rootpath";
		filterArgsPlus[filterArgsPlus.length-1] = rootPathValue;

		try
		{
			filter.setArguments(filterArgsPlus);
		}
		catch (IllegalArgumentException iae)
		{
			return INVALID_FILTER_ARGUMENTS;
		}

        filter.setErrorReporter(errorReporter);

		first_filter = filter;
		previous_filter = filter;
		last_filter = filter;

        // Create jar output
        PackmanJarMaker jarMaker = new PackmanJarMaker();

        if(jarMaker.startJar(out) == false)
        {
			errorReporter.errorReport("Unable to create output jar.", null);
            return OUTPUT_FILE_ERROR;
		}

		// Create temp folder in same location as jar
		File jarFile = new File(out);
		File tempFileDir = new File((jarFile.getParent()+File.separator+"tmp"+File.separator));
		tempFileDir.mkdirs();

		// Get the jar output path and then create the temp directory to give to the parser to generate temp files in.
		while(!urlQueue.isEmpty())
		{
			urlQueue.pop();

			// Check the file type. If not .x3d, .x3dv or .x3db then immediately
			// add to the jar.
			String encoding;
			String urlPath = urlQueue.getUrlPath();

			int idxDot = urlPath.lastIndexOf(".");
			if (idxDot < 0)
			{
				errorReporter.messageReport("Unknown destination file type");
				return OUTPUT_FILE_ERROR;
			}

			encoding = urlPath.substring(idxDot+1);

			if(encoding.equals("x3d") || encoding.equals("x3dv") || encoding.equals("x3db"))
			{
				InputSource is = null;
				File srcFile = new File(urlQueue.getUrlPath());

				if (srcFile.exists())
				{
					is = new InputSource(srcFile);
				}
				else
				{
					try
					{
						URL testUrl = new URL(urlQueue.getUrlPath());
						is = new InputSource(testUrl);
					}
					catch(MalformedURLException mfe)
					{
						System.out.println("Malformed URL: " + urlQueue.getUrlPath());
					}
				}

				// Generate temp file objects
				String tempFilePath = null;
				File tempFile = null;
				FileOutputStream fos = null;
				OutputStream tempOutputStream = null;

				try
				{
					tempFilePath = tempFileDir.getPath()+File.separator+srcFile.getName();

					// Get the encoding type for the file if batch converting
					if(conversion_type != CONVERT_TYPE_NONE)
					{
						idxDot = tempFilePath.lastIndexOf(".");

						switch(conversion_type)
						{
							case CONVERT_TYPE_X3D:
								encoding = "x3d";
								tempFilePath = tempFilePath.substring(0, idxDot)+".x3d";
								break;
							case CONVERT_TYPE_X3DV:
								encoding = "x3dv";
								tempFilePath = tempFilePath.substring(0, idxDot)+".x3dv";
								break;
							case CONVERT_TYPE_X3DB:
								encoding = "x3db";
								tempFilePath = tempFilePath.substring(0, idxDot)+".x3db";
								break;
							default:
								encoding = "x3d";
								tempFilePath = tempFilePath.substring(0, idxDot)+".x3d";
								break;
						}
					}

					tempFile = new File(tempFilePath);
					fos = new FileOutputStream(tempFile);
					tempOutputStream = fos;
				}
				catch(FileNotFoundException e)
				{
					errorReporter.fatalErrorReport(PARSER_IOEXCEPTION_MSG, e);
					return ERROR;
				}

				// JC:
				// Fixed output for the spec version. Not so good. We should have some
				// sort of flags that allow us to specify which spec version we want as
				// output and then let the stream handle it.

				Exporter writer = null;
				boolean upgrade = false;

                            switch (encoding) {
                                case "x3db":
                                    writer = new X3DBinaryRetainedDirectExporter(tempOutputStream,
                                            export_major_version,
                                            export_minor_version,
                                            errorReporter,
                                            method,
                                            PARAM_FLOAT_LOSSY);
                                    ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);
                                    break;
                                case "x3dv":
                                    writer = new X3DClassicRetainedExporter(tempOutputStream,
															export_major_version,
															export_minor_version,
															errorReporter);
                                    ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);
                                    break;
                                case "x3d":
                                    writer = new X3DXMLRetainedExporter(tempOutputStream,
														export_major_version,
														export_minor_version,
														errorReporter);
                                    ((BaseRetainedExporter) writer).setConvertOldContent(upgrade);
                                    break;
                                default:
                                    errorReporter.fatalErrorReport("Unknown destination encoding:" +
												   encoding, null);
					return OUTPUT_FILE_ERROR;
                            }

				last_filter.setContentHandler(writer);
				last_filter.setRouteHandler(writer);
				last_filter.setScriptHandler(writer);
				last_filter.setProtoHandler(writer);

				FileParserReader reader = new FileParserReader();

				reader.setContentHandler(first_filter);
				reader.setRouteHandler(first_filter);
				reader.setScriptHandler(first_filter);
				reader.setProtoHandler(first_filter);

				try
				{
					reader.parse(is);
				}
				catch (FilterProcessingException fpe)
				{
					errorReporter.fatalErrorReport("Filter Error for " +
							fpe.getFilterName(), null);
					return fpe.getErrorCode();
				}
				catch (VRMLException e)
				{
					int errorCode = previous_filter.getLastErrorCode();

					if (errorCode == 0)
					{
						errorReporter.fatalErrorReport(PARSER_VRMLEXCEPTION_MSG, e);
						return INVALID_INPUT_FILE_FORMAT;
					}
					else
					{
						errorReporter.fatalErrorReport(FILTER_INTERNAL_EXCEPTION + " " + filter_name, null);
						return errorCode;
					}
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace(System.err);
					errorReporter.fatalErrorReport(PARSER_IOEXCEPTION_MSG, ioe);
					return ERROR;
				}
				catch(Exception e)
				{
					// something unexpected...
					errorReporter.fatalErrorReport(FILTER_INTERNAL_EXCEPTION, e);
					return UNHANDLED_EXCEPTION;
				}
				catch (OutOfMemoryError oom)
				{
					// Unable to use error reporting because Error and Exception are
					// different subclasses
					errorReporter.messageReport("Out of memory error.");
					errorReporter.messageReport(oom.getMessage());
					return OUT_OF_MEMORY_ERROR;
				}
				catch (Error e)
				{
					// Unable to use error reporting because Error and Exception are
					// different subclasses
					errorReporter.messageReport("Unhandled error of type "+e.getClass().getName());
					errorReporter.messageReport(e.getMessage());
					// Check for thread death to avoid doing the cleanup
					if (e instanceof ThreadDeath)
						throw e;
					return UNHANDLED_ERROR;
				}

				// Close files
				try
				{
					// clean up...
					is.close();
					fos.close();
					tempOutputStream.flush();
					tempOutputStream.close();
				}
				catch ( IOException ioe )
				{
					errorReporter.fatalErrorReport(PARSER_IOEXCEPTION_MSG, ioe);
					return ERROR;
				}

				// Write the JAR.
				jarMaker.addFileToJar(tempFilePath, urlQueue.getJarUrlPath());

				// Delete temp file
				try
				{
					tempFile.delete();
				}
				catch(Exception e)
				{
	//				e.printStackTrace(System.err);
					errorReporter.fatalErrorReport("Could not delete temporary " +
												"file " + tempFile, null);
					return ERROR;
				}
			}
			else
			{
				jarMaker.addFileToJar(urlQueue.getUrlPath(), urlQueue.getJarUrlPath());
			}
		}

		// Delete temp directory
		try
		{
			tempFileDir.delete();
		}
		catch(Exception e)
		{
	//		e.printStackTrace(System.err);
			errorReporter.fatalErrorReport("Could not delete temporary "
										+ "folder " + tempFileDir, null);
		}

		// Close jar
		jarMaker.closeJar();

        return SUCCESS;
    }

    /**
     * Load a filter class from the given class name. If the filter cannot be
     * loaded, null is returned.
     *
     * @param classname The fully qualified name of the class needed
     * @return The filter loaded up from the class name
     */
    private AbstractFilter loadFilterClass(String classname)
    {
        AbstractFilter ret_val = null;

        try
        {
            Class cls = Class.forName(classname);
            ret_val = (AbstractFilter)cls.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException cnfe)
        {
            // ignore
            errorReporter.errorReport(CREATE_MSG, cnfe);
        }
        catch(IllegalAccessException iae)
        {
            errorReporter.errorReport(ACCESS_MSG, iae);
        }

        return ret_val;
    }


    /**
     * Set up the system properties needed to run the browser. This involves
     * registering all the properties needed for content and protocol
     * handlers used by the URI system. Only needs to be run once at startup.
     */
    private void setupProperties()
    {
        System.setProperty("uri.content.handler.pkgs",
                           "vlc.net.content");

        System.setProperty("uri.protocol.handler.pkgs",
                           "vlc.net.protocol");

        URIResourceStreamFactory res_fac = URI.getURIResourceStreamFactory();
        if(!(res_fac instanceof JavascriptResourceFactory))
        {
            res_fac = new JavascriptResourceFactory(res_fac);
            URI.setURIResourceStreamFactory(res_fac);
        }

        FileNameMap fn_map = URI.getFileNameMap();
        if(!(fn_map instanceof VRMLFileNameMap))
        {
            fn_map = new VRMLFileNameMap(fn_map);
            URI.setFileNameMap(fn_map);
        }
    }

    /**
     * Print out usage information
     *
     * @param filterer The filter instance to use
     */
    private static void printUsage(Packman packman)
    {
        System.out.println(USAGE_MESSAGE);
    }

    /**
     * Create an instance of this class and run it. The single argument, if
     * supplied is the name of the file to load initially. If not supplied it
     * will start with a blank document.
     *
     * @param args The list of arguments for this application.
     */
    public static void main(String[] args)
    {
        Packman packman = new Packman();

        String filename = null;
        String outfile = null;
        String[] optional_args = null;

        int num_args = args.length;

        if (num_args < 2)
        {
            printUsage(packman);

            System.exit(0);
        }
        else
        {
            if (num_args > 2)
            {
                filename = args[0];
                outfile = args[1];

                int num_optional_args = args.length-2;

                optional_args = new String[num_optional_args];

                System.arraycopy(args,
                                 2,
                                 optional_args,
                                 0,
                                 num_optional_args);
            }
            else
            {
                filename = args[0];
                outfile = args[1];
                optional_args = new String[0];
            }
        }

        int status;
        File fil = new File(filename);

        try
        {
            if (fil.exists())
            {
                status = packman.filter(fil, outfile, optional_args);
            }
            else
            {
                try
                {
                    URL url = new URL(filename);
                    status = packman.filter(url, outfile, optional_args);
                }
                catch(MalformedURLException mfe)
                {
                    System.out.println("Malformed URL: " + filename);
                    status = INPUT_FILE_NOT_FOUND;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Unhandled exception.");
            status = UNHANDLED_EXCEPTION;
        }
        catch (OutOfMemoryError oom)
        {
            System.out.println("Out of memory error.");
            status = OUT_OF_MEMORY_ERROR;
        }
        catch (Error e)
        {
            System.out.println("Unhandled error.");
            status = UNHANDLED_ERROR;
            // Check for thread death to avoid doing the cleanup
            if (e instanceof ThreadDeath)
                throw e;
        }
        System.exit(status);
    }
}
