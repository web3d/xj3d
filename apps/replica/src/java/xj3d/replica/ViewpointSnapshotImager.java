/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External imports
import com.jogamp.opengl.GLOffscreenAutoDrawable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.j3d.aviatrix3d.pipeline.OutputDevice;

import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;

// Local imports
import org.web3d.vrml.sav.InputSource;

import org.xj3d.ui.awt.browser.ogl.SceneThumbnailRecorder;

import org.xj3d.ui.construct.BlockingWorldLoader;
import org.xj3d.ui.construct.DeathTimer;
import org.xj3d.ui.construct.LoggingErrorReporter;
import org.xj3d.ui.construct.ScenePreprocessor;
import org.xj3d.ui.construct.SystemErrorReporter;
import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;
import org.xj3d.ui.construct.ogl.JFrameNotifyWrapper;
import org.xj3d.ui.construct.ogl.ShutdownManager;

import org.xj3d.ui.newt.offscreen.browser.ogl.NEWTOGLConstruct;

/**
 * A simple application intended primarily for command line operation
 * for capturing thumbnail images of all viewpoints of an X3D scene.
 *
 * @author Alan Hudson
 * @version $Revision: 1.13 $
 */
public class ViewpointSnapshotImager implements ActionListener, RecorderListener {

    /** The default image encoding */
    private static final String DEFAULT_ENCODING = "png";

    /** The logging identifier of this app */
    private static final String LOG_NAME = ViewpointSnapshotImager.class.getName();

    /** App name to register preferences under */
    private static final String APP_NAME = "xj3d.replica." + LOG_NAME;

    /** The default screen size */
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;

    /** The default number of antialias samples */
    private static final int DEFAULT_ANTIALIAS_SAMPLES = 1;

    /** The default anisotropic degree setting */
    private static final int DEFAULT_ANISOTROPIC_DEGREE = 1;

    /** Usage message with command line options */
    protected static final String USAGE =
        //"0---------1---------2---------3---------4---------5---------6---------7---------8"+
        //"012345678901234567890123456789012345678901234567890123456789012345678901234567890"+
        "Usage: "+ LOG_NAME +" [options] sourcefile \n" +
        "  -help                  Print out this message to the stdout \n" +
        "  -log filename          The name of the log file. If unspecified, logging \n" +
        "                         output is directed to stdout. \n" +
        "  -outdir dirname        The name of the output directory to save the image \n" +
        "                         sequence in. If unspecified, a directory named output \n" +
        "                         will be created in the current user directory. \n" +
        "  -filePrefix name       The prefix of the filename of the saved image.  Will \n" +
        "                         save as {name}00.{encoding}. \n" +
        "  -encode name           The name of the image encoding type [png|jpg|gif] \n" +
        "                         If unspecified, defaults to png. \n" +
        "  -size widthxheight     The size of the image capture in pixels. \n" +
        "                         Defaults to "+ DEFAULT_WIDTH +"x"+ DEFAULT_HEIGHT +" \n" +
        "  -background r g b a    Specify the background. r, g, b, a are float values in \n" +
        "                         the range of 0.0 to 1.0. \n" +
        "  -sbuffer               Use single buffering. If unspecified, double \n" +
        "                         buffering is used. \n" +
        "  -mipmaps               Use mipmaps. If unspecified, mipmap generation is \n" +
        "                         disabled. \n" +
        "  -antialias n           The number of antialias samples to use. If unspecified, \n" +
        "                         antialiasing is disabled. \n" +
        "  -anisotropicDegree n   The anistropic degree setting to use. If unspecified, \n" +
        "                         anisotropic filtering is disabled. \n" +
        "  -verbose               Enable message logging level. \n" +
        "  -stats                 Enable statistics generation on rendering and image file \n" +
        "                         creation times. Used with the -verbose option to display. \n" +
        "  -show                  Set the X3D browser window visible while recording. \n" +
        "  -timeout ms            Set the timeout interval in milliseconds before interrupting and exiting. \n" +
        "  -interactive           Run the application in interactive mode. The sourcefile \n" +
        "                         argument is ignored and the x3d browser window is displayed. \n" +
        "                         A file chooser dialog is available for selecting the file \n" +
        "                         to record. \n";

    //////////////////////////////////////////////////////////////////////
    // Browser construct function modules

    /** The browser construct world loader */
    private BlockingWorldLoader loader;

    /** The image recorder */
    private SceneThumbnailRecorder recorder;

    //////////////////////////////////////////////////////////////////////
    // configuration parameters

    /** Run in interactive mode */
    private boolean interactive;

    /** In non-interactive mode, show the browser window while recording */
    private boolean showWindow;

    /** Enable message logging level */
    private boolean verbose;

    /** Enable statistics reporting */
    private boolean stats;

    /** The image encoding identifier */
    private String encoding;

    /** The browser/recorder width */
    private int width;

    /** The browser/recorder height */
    private int height;

    /** The background color */
    private Color background;

    /** Use mipmaps */
    private boolean useMipMaps;

    /** Use single buffering */
    private boolean sbuffer;

    /** The antialias samples */
    private int antialiasSamples;

    /** The anisotropic degree */
    private int anisotropicDegree;

    /** The x3d file to record */
    private File sourceFile;

    /** The input source to record */
    private InputSource is;

    /** Are we running Standalone or inside a program */
    private boolean standalone;

    /** The directory where the image sequence will be written */
    private File outputDir;

    /** Prefix to filenames */
    private String filePrefix;

    /** Interval before asserting control, stopping and continuing */
    private int timeoutIntervalMilliseconds = 20_000;

    //////////////////////////////////////////////////////////////////////
    // UI components

    /** The UI Window */
    private JFrame frame;

    /** UI control for opening the open file chooser dialog */
    private JMenuItem openItem;

    /** File chooser for picking an x3d world to open */
    private JFileChooser chooser;

    /** The construct in use */
    private NEWTOGLConstruct construct = null;

    /** The error reporter */
    private static ErrorReporter logger;

    /** JVM Death Timer */
    private final DeathTimer deathTimer;

    //////////////////////////////////////////////////////////////////////

    /**
     * Constructor
     *
     * @param args The command line arguments
     */
    public ViewpointSnapshotImager (String[] args) {
        //////////////////////////////////////////////////////////////////////
        // parse the command line arguments, set up the working parameters
        boolean success = false;
        Exception exc = null;
        standalone = true;

        try {
            success = parseArgs(args);
        } catch (Exception e) {
            exc = e;
        }

        if (!success) {
            if (logger != null) {
                logger.fatalErrorReport(
                    LOG_NAME +": Error parsing command line arguments", exc);
            }
            // ensure Xj3D is completely killed so that any invoking process (such as Ant) can proceed
            shutdown(ReplicaConstants.INVALID_ARGUMENTS);
        }

        // Give the BlockingWorldLoader an allowed interval, else kill the JVM
        deathTimer = new DeathTimer(timeoutIntervalMilliseconds);
        deathTimer.setDaemon(true);

        takeSnapshots();
    }

    /**
     * Take snapshots of a file.
     *
     * @param arg Arguments to use, follows command line usage
     * @param is The input source
     */
    public void takeSnapshots(String[] arg, InputSource is) {
        standalone = false;

        this.is = is;

        takeSnapshots();
    }

    /**
     * Take snapshots of a file.
     */
    private void takeSnapshots() {
/*
        System.out.println("Displaying source");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is.getByteStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
 */
        //////////////////////////////////////////////////////////////////////
        // configure a browser and the functional units to manage the recording

        // we like multi-threaded content loading
        System.setProperty("org.xj3d.core.loading.threads", "4");

        //////////////////////////////////////////////////////////////////////
        // uncomment to disable the ImageIO native plugins
        //System.setProperty("com.sun.media.imageio.disableCodecLib", "true");
        //////////////////////////////////////////////////////////////////////
        // uncomment to see what the GLCapabilitiesChooser is doing
        //System.setProperty("jogl.debug.DefaultGLCapabilitiesChooser", "true");
        //////////////////////////////////////////////////////////////////////

        boolean headless = false;

        if (construct == null) {
            if (showWindow || interactive) {
                construct = new ThumbnailConstruct(logger);
                headless = GraphicsEnvironment.isHeadless();
                if (interactive && headless) {
                    interactive = false;
                }
            } else {
                construct = new OffscreenThumbnailConstruct(logger, width, height);
            }

            // push the command line preferences for graphics capabilities into
            // the browser construct before the renderer & capabilities are built
            ((ConfigGraphicsCapabilities)construct).setGraphicsCapabilitiesParameters(
                useMipMaps,
                !sbuffer,
                antialiasSamples,
                anisotropicDegree);

            // instantiate and configure all the xj3d objects
            construct.buildAll();

            // the shutdown controller must be instantiated AFTER the renderer
            // and x3d manager's have been created
            new ShutdownManager(construct);
        }

        // create and configure the world loader and thumbnail recorder
        loader = new BlockingWorldLoader(construct);

        // set the proxy scene processor for multiple scene pre process handling
        List<ScenePreprocessor> procList = new ArrayList<>();

        recorder = new SceneThumbnailRecorder(construct);
        recorder.setSize(width, height);
        recorder.setOutputDirectory(outputDir);
        recorder.setFilePrefix(filePrefix);
        recorder.setEncoding(encoding);

        if (background != null) {
            int alpha = background.getAlpha();
            if (alpha != 255) {
                // if transparency is specified in the background, we must
                // configure the recorder to postprocess the image. the capture
                // takes place with a 'green screen' background, which the
                // recorder then replaces with the designated background color
                procList.add(new ConfigureBackground(Color.GREEN));
                //loader.setScenePreprocessor(new ConfigureBackground(Color.GREEN));
                recorder.setBackgroundColor(Color.GREEN, background);
            } else {
                // no transparency, then just take the snap with the designated color
                procList.add(new ConfigureBackground(background));
                //loader.setScenePreprocessor(new ConfigureBackground(background));
            }
        }

        // Add proxy scene pre processor into loader
        if (background != null) {
            ProxyScenePreprocessor scenePreProc = new ProxyScenePreprocessor(procList);
            loader.setScenePreprocessor(scenePreProc);
        }

        if (showWindow || interactive) {
            if (headless) {
                Container contentPane = new Container();
                contentPane.setLayout(new BorderLayout());

                // set the image size for recording
                contentPane.setPreferredSize(new Dimension(width, height));
                contentPane.add((Component) ((OutputDevice)construct.getGraphicsObject()).getSurfaceObject(), BorderLayout.CENTER);
                construct.getRenderManager().setEnabled(true);
            } else {
                // create the 'UI' components
                frame = new JFrameNotifyWrapper(LOG_NAME, construct.getRenderManager());

                Container contentPane = frame.getContentPane();
                contentPane.setLayout(new BorderLayout());

                // set the image size for recording
                contentPane.setPreferredSize(new Dimension(width, height));
                contentPane.add((Component) ((OutputDevice)construct.getGraphicsObject()).getSurfaceObject(), BorderLayout.CENTER);

                if (interactive) {
                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    JMenuBar mb = new JMenuBar();
                    frame.setJMenuBar(mb);

                    JMenu fileMenu = new JMenu("File");
                    mb.add(fileMenu);

                    openItem = new JMenuItem("Open World");
                    openItem.addActionListener(this);
                    fileMenu.add(openItem);

                    File dir = new File(System.getProperty("user.dir"));
                    chooser = new JFileChooser(dir);
                }

                // MUST call pack(), else 'mysterious' problems occur
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Dimension screenSize = frame.getToolkit().getScreenSize();
                Dimension frameSize = frame.getSize();
                frame.setLocation(
                    (screenSize.width - frameSize.width)/2,
                    (screenSize.height - frameSize.height)/2);
                frame.setVisible(true);

            }
        } else {
            construct.getRenderManager().setEnabled(true);
        }

        // This call critical for offscreen rendering.  Will get stuck otherwise
        GLOffscreenAutoDrawable.FBO fbo = (GLOffscreenAutoDrawable.FBO) ((OutputDevice)construct.getGraphicsObject()).getSurfaceObject();
        fbo.display();

        //////////////////////////////////////////////////////////////////////
        // configuration complete, lock & load

        if (!interactive) {
            if (is == null) {
                is = new InputSource(sourceFile);
            }

            deathTimer.start();

            // This is a blocking call.  If we don't set the DeathTimer, then we
            // would block indefinitely here.
            // Also the InputStream is closed before the RecorderEvent.COMPLETE
            // is fired
            boolean success = loader.load(is);

            if (success) {

                // don't stop the deathTimer! hopefully this should ensure that the
                // overall set of processes all finish or else get honorably killed by deathTimer
//              deathTimer.exit();

                // start the screen captures. The completion will be handled
                // by the RecorderListener method
                recorder.start(this);

            } else {
                logger.fatalErrorReport(
                    LOG_NAME +": Error loading source file: " + sourceFile, null);

                if (standalone) {
                    // ensure Xj3D is completely killed so that any invoking process (such as Ant) can proceed
                    shutdown(ReplicaConstants.INPUT_FILE_NOT_FOUND);
                } else {
                    throw new IllegalArgumentException("Input file not found: " + sourceFile);
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == openItem) {
            int returnVal = chooser.showDialog(frame, "Open World");
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    is = new InputSource(file);

                    deathTimer.start();
                    boolean success = loader.load(is);

                    if (success) {
                        recorder.start(this);
                    }
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by RecorderListener
    //----------------------------------------------------------

    @Override
    public void recorderStatusChanged(RecorderEvent evt) {
        switch(evt.id) {
            case RecorderEvent.ACTIVE:
                // Do nothing
                break;

            case RecorderEvent.COMPLETE:
                if (stats) {
                    logger.messageReport(LOG_NAME +
                        ": Last viewpoint render time: "+
                        recorder.getRenderTime() +" ms");
                    logger.messageReport(LOG_NAME +
                        ": Last viewpoint IO write time: "+
                        recorder.getFileTime() +" ms");
                }

                if (standalone) {
                    if (!interactive) {

                        // For some reason, we need just a little more time to
                        // produce the image
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {}
                        shutdown(ReplicaConstants.NORMAL_JVM_EXIT);
                    }
                }
                break;

            default:
                System.err.println("Unknown RecorderEvent: " + evt.id);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /** Shutdown this ThumbnailImager
     *
     * @param exitStatus the status to exit the JVM with
     */
    private void shutdown(int exitStatus) {

        // Kill the DT thread
        deathTimer.exit();

        // Clean up resources
        if (construct != null) {
            construct.getRenderManager().setEnabled(false);
            ((OutputDevice)construct.getGraphicsObject()).dispose();
        }

        // ensure Xj3D is completely killed so that any invoking process (such as Ant) can proceed
        System.exit(exitStatus);
    }

    /**
     * Parse and validate the command line arguments, initialize the working
     * parameters.
     *
     * @param arg The command line arguments
     * @return true if the app should continue, false if it should exit.
     */
    private boolean parseArgs(String[] arg) {

        int argIndex = -1;
        String log_file_name = null;
        String output_dir_name = null;
        String encoding_string = null;
        String size_string = null;
        String[] rgba = null;
        String antialias_samples_string = null;
        String anisotropic_degree_string = null;

        //////////////////////////////////////////////////////////////////////
        // parse the arguments, sort out a help request
        for (int i = 0; i < arg.length; i++) {
            String argument = arg[i];

            if (argument.startsWith("-")) {
                try {
                    switch (argument) {
                        case "-help":
                            // presumably, a help request won't be generated
                            // in a headless environment - send to stdout
                            System.out.println(USAGE);
                            return(false);
                        case "-log":
                            log_file_name = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-outdir":
                            output_dir_name = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-filePrefix":
                            filePrefix = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-encode":
                            encoding_string = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-show":
                            showWindow = true;
                            argIndex = i;
                            break;
                        case "-size":
                            size_string = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-background":
                            rgba = new String[4];
                            rgba[0] = arg[i+1];
                            rgba[1] = arg[i+2];
                            rgba[2] = arg[i+3];
                            rgba[3] = arg[i+4];
                            argIndex = i+4;
                            break;
                        case "-sbuffer":
                            sbuffer = true;
                            argIndex = i;
                            break;
                        case "-mipmaps":
                            useMipMaps = true;
                            argIndex = i;
                            break;
                        case "-antialias":
                            antialias_samples_string = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-anisotropicDegree":
                            anisotropic_degree_string = arg[i+1];
                            argIndex = i+1;
                            break;
                        case "-verbose":
                            verbose = true;
                            argIndex = i;
                            break;
                        case "-stats":
                            stats = true;
                            argIndex = i;
                            break;
                        case "-interactive":
                            interactive = true;
                            argIndex = i;
                            break;
                        case "-timeout":
                            timeoutIntervalMilliseconds = Integer.parseInt(arg[i+1]);
                            argIndex = i+1;
                            break;
                    }
                } catch (NumberFormatException e) {
                    // this would be an IndexOutOfBounds - should arrange to log it
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        // establish the error logger first

        if (log_file_name != null) {
            try {
                File log_file = new File(log_file_name);
                if (!log_file.exists() || log_file.isFile()) {
                    logger = new LoggingErrorReporter(log_file, verbose, true, true, true);
                }
            } catch (Exception e) {
            }
        }

        if (logger == null) {
            logger = new SystemErrorReporter(verbose, true, true, true);
        }

        //////////////////////////////////////////////////////////////////////
        // the input source file should be the last unused arg -
        // validate if running in non-interactive mode, otherwise ignore

        if (!interactive && is == null) {
            String source_file_name;
            if ((arg.length > 0) && (argIndex + 1 < arg.length)) {
                source_file_name = arg[arg.length - 1];
                try {
                    sourceFile = new File(source_file_name);
                    if (!sourceFile.exists()) {
                        logger.errorReport(LOG_NAME +
                            ": Source file: "+ source_file_name +
                            " does not exist.", null);
                        return(false);
                    } else if (sourceFile.isDirectory()) {
                        logger.errorReport(LOG_NAME +
                            ": Source file: "+ source_file_name +
                            " is a directory.", null);
                        return(false);
                    }
                } catch (Exception e) {
                    logger.errorReport(LOG_NAME +
                        ": Source file error.", e);
                    return(false);
                }
            } else {
                logger.errorReport(LOG_NAME +
                    ": No source file specified.", null);
                return false;
            }
        }
        //////////////////////////////////////////////////////////////////////
        // validate the destination directory

        if ( output_dir_name == null ) {
            File dir = new File( System.getProperty( "user.dir" ) );
            outputDir = new File( dir, "output" );
            if ( outputDir.exists( ) ) {
                if ( !outputDir.isDirectory( ) ) {
                    logger.errorReport( LOG_NAME +
                        ": Output directory: "+ outputDir.getName( ) +
                        " is not a directory.", null );
                    return( false );
                }
            } else if ( !outputDir.mkdir( ) ) {
                logger.errorReport( LOG_NAME +
                    ": Creating output directory failed.", null );
                return( false );
            }
        }  else {
            try {
                outputDir = new File( output_dir_name );
                if ( outputDir.exists( ) ) {
                    if ( !outputDir.isDirectory( ) ) {
                        logger.errorReport( LOG_NAME +
                            ": Output directory: "+ output_dir_name +
                            " is not a directory.", null );
                        return( false );
                    }
                } else if ( !outputDir.mkdirs( ) ) {
                    logger.errorReport( LOG_NAME +
                        ": Creating output directory failed.", null );
                    return( false );
                }
            } catch ( Exception e ) {
                logger.errorReport( LOG_NAME +
                    ": Output directory error.", e );
                return false;
            }
        }

        //////////////////////////////////////////////////////////////////////
        // options....

        // encoding
        if ( encoding_string == null ) {
            encoding = DEFAULT_ENCODING;
        } else {
            boolean found = false;
            String[] format = ImageIO.getWriterFormatNames( );
            for (String format1 : format) {
                if (encoding_string.equals(format1)) {
                    encoding = encoding_string;
                    found = true;
                    break;
                }
            }
            if ( !found ) {
                logger.errorReport(
                    LOG_NAME +": Unknown image encoding type: "+ encoding_string, null );
                return false;
            }
        }

        // width & height
        if (size_string == null) {
            width = DEFAULT_WIDTH;
            height = DEFAULT_HEIGHT;
        }
        else {
            boolean success = false;
            int separator_index = size_string.indexOf("x");
            try {
                width = Integer.valueOf(size_string.substring(0, separator_index));
                height = Integer.valueOf(size_string.substring(separator_index+1));
                success = true;
            } catch(NumberFormatException e) {
            }
            if (!success) {
                logger.warningReport(
                    LOG_NAME +": Unable to parse width & height values: "+
                    size_string +", using defaults.", null);
                width = DEFAULT_WIDTH;
                height = DEFAULT_HEIGHT;
            }
        }

        // background color
        if (rgba != null) {
            try {
                background = new Color(
                        Float.valueOf(rgba[0]),
                        Float.valueOf(rgba[1]),
                        Float.valueOf(rgba[2]),
                        Float.valueOf(rgba[3]));
            } catch (NumberFormatException e) {
                logger.warningReport(
                    LOG_NAME +": Unable to parse background color values: "+
                    java.util.Arrays.toString(rgba) +", using default background.", null);
                background = null;
            }
        }

        // antialias
        if (antialias_samples_string == null) {
            antialiasSamples = DEFAULT_ANTIALIAS_SAMPLES;
        }
        else {
            try {
                antialiasSamples = Integer.valueOf(antialias_samples_string);
            } catch(NumberFormatException e) {
                logger.warningReport(
                    LOG_NAME +": Unable to parse antialias sample value: "+
                    antialias_samples_string +", using default.", null);
                antialiasSamples = DEFAULT_ANTIALIAS_SAMPLES;
            }
        }

        // anistropic filtering
        if (anisotropic_degree_string == null) {
            anisotropicDegree = DEFAULT_ANISOTROPIC_DEGREE;
        }
        else {
            try {
                anisotropicDegree = Integer.valueOf(anisotropic_degree_string);
            } catch(NumberFormatException e) {
                logger.warningReport(
                    LOG_NAME +": Unable to parse anisotropic degree value: "+
                    anisotropic_degree_string +", using default.", null);
                anisotropicDegree = DEFAULT_ANISOTROPIC_DEGREE;
            }
        }

        //////////////////////////////////////////////////////////////////////
        return true;
    }

    /**
     * Entry point. For a full list of valid arguments,
     * invoke with the -help argument.
     *
     * @param arg The list of arguments
     */
    public static void main(String[] arg) {
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.xj3dResources");

        new ViewpointSnapshotImager(arg);
    }
}
