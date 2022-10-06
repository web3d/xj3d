/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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

import org.j3d.util.I18nManager;

/**
 * Wrapper class for choosing the Replica operational mode. The
 * mode selection determines the application class to use.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class Replica {

    /** App name to register preferences under */
    private static final String APP_NAME = "xj3d.Replica";

    /** The logging identifier of this class */
    private static final String LOG_NAME = "Replica";

    /** Thumbnail image mode identifier */
    public static final String THUMBNAIL = "thumbnail";

    /** Sequenced image recording mode identifier */
    public static final String SEQUENCE = "sequence";

    /** Sequenced images of scene Viewpoint recording mode identifier */
    public static final String VIEWPOINTS = "viewpoints";

    /** Help mode identifier */
    public static final String HELP = "help";

    /** Usage message with command line options */
    private static final String USAGE =
        //"0---------1---------2---------3---------4---------5---------6---------7---------8"+
        //"012345678901234567890123456789012345678901234567890123456789012345678901234567890"+
        "Usage: "+ LOG_NAME +" [options] \n" +
        "  -help                  Print out this message to the stdout. If a -mode argument \n" +
        "                         preceeds a -help argument, the -help argument is passed \n" +
        "                         on to the operational application class. \n" +
        "  -mode id               The identifier of the mode of operation. \n" +
        "                         ["+ THUMBNAIL +"|"+ SEQUENCE +"|" + VIEWPOINTS + "] \n";

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Entry point. For a full list of valid arguments,
     * invoke with the -help argument. If a mode argument precedes
     * the a help request, the help request is passed on to the
     * operational application class.
     *
     * @param args The list of arguments
     */
    public static void main( String[] args ) {

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, "config.i18n.xj3dResources");

        String mode = parseArgs( args );

        if ( mode == null ) {
            System.err.println( LOG_NAME +": Mode of operation not specified" );
        } else if ( mode.equals( HELP ) ) {
            System.out.println( USAGE );
            System.out.println("For " + THUMBNAIL + ": " +      ThumbnailImager.USAGE );
            System.out.println("For " + VIEWPOINTS     + ": " + ViewpointSnapshotImager.USAGE );
            System.out.println("For " + SEQUENCE  + ": " +        SceneRecorder.USAGE );
        } else if ( mode.equals( THUMBNAIL ) ) {
            new ThumbnailImager( args );
        } else if ( mode.equals(VIEWPOINTS) ) {
            new ViewpointSnapshotImager( args );
        } else if ( mode.equals( SEQUENCE ) ) {
            new SceneRecorder( args );

        } else {
            System.err.println( LOG_NAME +": Unknown mode of operation: "+ mode );
            System.out.println( USAGE );
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Parse the command line arguments, extract and return the
     * mode of operation identifier.
     *
     * @param args The command line arguments
     * @return The mode of operation identifier or null if one is not found.
     */
    private static String parseArgs( String[] args ) {

        String mode = null;

        OUTER:
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith( "-" )) {
                try {
                    switch (argument) {
                        case "-mode":
                            mode = args[i+1];
                            break OUTER;
                        case "-help":
                            mode = HELP;
                            break OUTER;
                    }
                } catch ( Exception e ) {
                    String arguments = new String();
                    for (String arg : args) {
                        arguments += arg + ' ';
                    }
                    System.err.println ("*** Error parsing invocation arguments: " + arguments);
                    // this would be an IndexOutOfBounds - should arrange to log it
                    e.printStackTrace(System.err);
                }
            }
        }
        return( mode );
    }
}
