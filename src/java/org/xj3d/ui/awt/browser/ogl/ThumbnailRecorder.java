/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.awt.browser.ogl;

// External imports
import com.jogamp.opengl.GLDrawable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.j3d.aviatrix3d.pipeline.OutputDevice;

import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.browser.ScreenCaptureListener;

import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.navigation.BaseViewpoint;

import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;

import org.xj3d.ui.construct.ogl.AutoConfigureViewpoint;
import org.xj3d.ui.construct.ogl.OGLConstruct;

import org.web3d.vrml.nodes.FrameStateListener;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.renderer.ogl.nodes.navigation.OGLNavigationInfo;
import org.web3d.vrml.nodes.VRMLScene;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.lang.VRMLNodeFactory;

/**
 * A function module that performs an image capture of an
 * X3D scene for the purpose of creating a thumbnail image.
 * The requirements of the X3D world are:
 * <ul>
 * <li>Optionally there may be a DEF'ed Viewpoint with a known DEF name.</li>
 * <li>By default the Viewpoint's DEF name is "ICON_VIEWPOINT". The Viewpoint's
 * DEF name is programatically configurable in this module.</li>
 * <li>In the absence of a DEF'ed Viewpoint, the default viewpoint will be
 * captured.</li>
 * </ul>
 * The image capture is managed as follows:
 * <ul>
 * <li>On the completion of the world and content loading, the DEF'ed Viewpoint
 * node is searched for and bound if necessary.</li>
 * <li>The image capture is initiated immediately if the Def'ed Viewpoint is not found,
 * or the DEF'ed Viewpoint is found and currently bound.</li>
 * <li>Otherwise, the capture is initiated immediately upon notification that the
 * DEF'ed Viewpoint has been bound.</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.8 $
 */
public class ThumbnailRecorder implements ScreenCaptureListener, VRMLNodeListener,
    FrameStateListener {

    /** Default def name used to specify a viewpoint */
    private static final String DEFAULT_VIEWPOINT = "ICON_VIEWPOINT";

    /** The logging identifier of this class */
    private static final String LOG_NAME = "ThumbnailRecorder";

	/** Constant value for converting nanoseconds to milliseconds */
	private static final double NANOS_PER_MILLI = 1000000.0;

    /** The construct instance to record from */
    protected OGLConstruct construct;

    /** The browser core */
    protected OGLStandardBrowserCore core;

    /** The error reporting mechanism */
    protected ErrorReporter errorReporter;

    /** The rendering surface */
    protected Object canvas;

    /** The viewpoint */
    protected OGLNavigationInfo navInfo;

    /** The scene root, parent for the nav info and viewpoint nodes */
    protected VRMLWorldRootNodeType root;

    /** Flag used in the end of frame listener, indicating that the new nodes
    * may be added to the scene */
    protected boolean addNodes;

    /** The frame state manager */
    protected FrameStateManager fsm;

    /** Synchronization flag */
    protected boolean configComplete;

    /** The sequence capture number, somewhat unnecessary as in this
    *  application, it only ever reaches one (1) */
    protected int number;

    /** The name of the x3d viewpoint to capture.
    *  Default value is "ICON_VIEWPOINT" */
    protected String viewpointName = DEFAULT_VIEWPOINT;

    /** The viewpoint node, used when we have to wait for the
    *  named viewpoint to be bound */
    protected BaseViewpoint viewpoint;

    /** The index of the viewpoint's isBound field. */
    protected int isBound_index;

    /** The output file for the captured images */
    protected File outputFile;

    /** Listener for recorder status events */
    protected RecorderListener listener;

    /** Should the navigation info be replaced to use the headlight value */
    protected boolean replaceNavInfo;

    /** Should the headlight be on */
    protected boolean headlight;

    /** The width of the output images */
    protected int width;

    /** The height of the output images */
    protected int height;

    /** The image encoding type */
    protected String type = "png";

    /** Flag indicating that the selected encoding type may have an alpha */
    protected boolean hasAlpha;

    /** Image encoding types that may have an alpha */
    protected String[] alphaTypes = { "png", "gif", };

    /////////////////////////////////////////////////////////////////////////
    // statistic variables

    /** The total file write time */
    protected long fileTime;

    /** The total frame rendering time */
    protected long renderTime;

    /** The last start of frame rendering time */
    protected long startFrameTime;

    /////////////////////////////////////////////////////////////////////////

    /** Flag indicating that the image capture should be post processed */
    protected boolean postProcess;

    /** RGB value for snap pixels that should be replaced */
    protected int snapRGB;

    /* ARGB value to replace the designated snap pixels */
    protected int imageARGB;

    /**
     * Constructor
     *
     * @param construct The construct instance to record from
     * @param replaceNavInfo Should the navigationInfo be replaced
     * @param headlight Should the headlight be on
     */
    public ThumbnailRecorder( OGLConstruct construct, boolean replaceNavInfo, boolean headlight ) {
        if ( construct == null ) {
            throw new IllegalArgumentException(
                LOG_NAME +": construct instance must be non-null" );
        }

        this.headlight = headlight;
        this.replaceNavInfo = replaceNavInfo;
        this.construct = construct;
        fsm = construct.getFrameStateManager( );
        errorReporter = construct.getErrorReporter( );
        core = construct.getBrowserCore( );
        canvas = ((OutputDevice)construct.getGraphicsObject()).getSurfaceObject();
    }

    //----------------------------------------------------------
    // Method required for ScreenCaptureListener
    //----------------------------------------------------------

    /**
     * Notification of a new screen capture.
     * The buffer will be in openGL pixel order.
     *
     * @param buffer The screen capture
     * @param width The width in pixels of the captured screen
     * @param height The height in pixels of the captured screen
     */
    @Override
    public void screenCaptured( Buffer buffer, int width, int height ) {

        renderTime += System.nanoTime() - startFrameTime;

        saveScreen( buffer, width, height );

        number++;

        if ( listener != null ) {
            listener.recorderStatusChanged(new RecorderEvent(
                this, RecorderEvent.COMPLETE,
                number ) );
        }
    }

    //----------------------------------------------------------
    // Method defined by VRMLNodeListener
    //----------------------------------------------------------

    /**
     * Listener for the viewpoint binding, if necessary
     */
    @Override
    public void fieldChanged( int index ) {
        if ( index == isBound_index ) {
            initiateCapture( );
            viewpoint.removeNodeListener( this );
        }
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

	/**
	 * Add nodes to the scene, wait a frame, then signal that the
	 * operation has been completed.
	 */
    @Override
    public void allEventsComplete( ) {
        if ( addNodes ) {
            // add and bind the new viewpoint and nav info
            root.addChild( navInfo );
            int index = navInfo.getFieldIndex( "set_bind" );
            navInfo.setValue( index, true );
            navInfo.setupFinished( );

            addNodes = false;
            fsm.addEndOfThisFrameListener( this );

        } else {
            synchronized ( this ) {
                configComplete = true;
                notify( );
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Return the frame rendering time in milliseconds
     *
     * @return the frame rendering time in milliseconds
     */
    public double getRenderTime( ) {
        return( (renderTime/number)/NANOS_PER_MILLI );
    }

    /**
     * Return the file write time in milliseconds
     *
     * @return the file write time in milliseconds
     */
    public double getFileTime( ) {
        return( (fileTime/number)/NANOS_PER_MILLI );
    }

    /**
     * Set the image encoding type
     *
     * @param type The image encoding type
     * @return true if the encoding type is valid, false otherwise.
     */
    public boolean setEncoding( String type ) {
        boolean found = false;
        String[] format = ImageIO.getWriterFormatNames( );
        for (String format1 : format) {
            if (type.equals(format1)) {
                this.type = type;
                found = true;
                break;
            }
        }
        hasAlpha = false;
        if ( !found ) {
            errorReporter.errorReport(
                LOG_NAME +": Unknown image encoding type: "+ type, null );
        } else {
            for (String alphaType : alphaTypes) {
                if (type.equalsIgnoreCase(alphaType)) {
                    hasAlpha = true;
                    break;
                }
            }
        }
        return( found );
    }

    /**
     * Set the image size
     *
     * @param width The image width
     * @param height The image height
     */
    public void setSize( int width, int height ) {
        this.width = width;
        this.height = height;
    }

    /**
     * Set the background color for the image.
     *
     * @param x3dBackgroundColor The background color of the X3D model to
     * replace with the image background color. If null, the image background
     * will not be changed from the capture.
     * @param imageBackgroundColor The color to set for the image background.
     * If null, the image background will not be changed from the capture.
     */
    public void setBackgroundColor(
        Color x3dBackgroundColor,
        Color imageBackgroundColor ) {

        postProcess = ( x3dBackgroundColor != null ) & ( imageBackgroundColor != null );
        if ( postProcess ) {
            // mask off the alpha, the snap image is RGB only
            snapRGB = 0x00FF_FFFF & x3dBackgroundColor.getRGB( );

            imageARGB = imageBackgroundColor.getRGB( );
        }
    }
    /**
     * Set the output file for the image
     *
     * @param file The output file for the image
     * @return true if the output file is of a valid encoding type,
     * false otherwise.
     */
    public boolean setOutputFile( File file ) {
        outputFile = null;
        String filename = file.toString( );
        int index = filename.lastIndexOf( "." );
        if ( index == -1 ) {
            errorReporter.errorReport(
                LOG_NAME +": Invalid output file: Unknown image "+
                "encoding type for file name: "+ filename, null );
        } else if ( setEncoding( filename.substring( index+1 ) ) ) {
            outputFile = file;
        }
        return outputFile != null;
    }

    /**
     * Set the DEF'ed name of the Viewpoint
     *
     * @param name The DEF'ed name of the Viewpoint
     */
    public void setViewpointName( String name ) {
        viewpointName = name;
    }

    /**
     * Initiate the capture
     * @param listener
     */
    public void start( RecorderListener listener ) {
        fileTime = 0;
        renderTime = 0;
        if ( outputFile == null ) {
            errorReporter.warningReport(
                LOG_NAME +": Unable to record, output file "+
                "is not initialized.", null );
            return;
        }

        this.listener = listener;

        int tmp_width = 0;
        int tmp_height = 0;
        if ( canvas instanceof Component ) {
            Dimension size = ((Component)canvas).getSize( );
            tmp_width = (int)size.getWidth( );
            tmp_height = (int)size.getHeight( );
        } else if ( canvas instanceof GLDrawable ) {
            tmp_width = ((GLDrawable)canvas).getSurfaceWidth( );
            tmp_height = ((GLDrawable)canvas).getSurfaceHeight( );
        }

        // configure the size from the rendering surface,
        // only if the surface returns something non-zero
        if ( ( tmp_width > 0 ) && ( tmp_height > 0 ) ) {
            width = tmp_width;
            height = tmp_height;
        }

        number = 0;

        if (replaceNavInfo) {

            // rem: this is pointless if the viewpointName is set for auto-configure.
            // this adds a navInfo node that is subsequently ignored when the auto
            // configure class adds and binds it's own.

            VRMLScene scene = core.getScene( );
            VRMLNodeFactory factory = scene.getNodeFactory( );

            VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( DEFAULT_VIEWPOINT ));

            if (node != null) {
                // 3 Point Lighting doesn't work for user specified lighting right now
                headlight = true;
            } else {

                navInfo = (OGLNavigationInfo)factory.createVRMLNode( "NavigationInfo", false );
                // TODO: We lose information here, if it was EXAMINE it will be a different spot

                navInfo.setHeadlight( headlight );

                root = (VRMLWorldRootNodeType)scene.getRootNode( );
                addNodes = true;

                // wait for the new nodes to be added to the scene and bound before returning.
                synchronized ( this ) {
                    fsm.addEndOfThisFrameListener( this );
                    configComplete = false;
                    while( !configComplete ) {
                        try {
                            wait( );
                        } catch ( InterruptedException ie ) {
                        }
                    }
                }
            }
        }

        // determine how to take the screen capture
        if ( viewpointName != null ) {

            if ( viewpointName.equalsIgnoreCase( "FIT" ) ) {

                // Always fit to world

                // auto configure the viewpoint for the snap
                AutoConfigureViewpoint auto = new AutoConfigureViewpoint( construct, headlight );
                boolean success = auto.configure( );
                // regardless of whether the configure succeeded, do the capture
                initiateCapture( );

            } else if ( viewpointName.equalsIgnoreCase( "AUTO" ) ) {

                // check to see if there is a user specified, use it
                // otherwise fit to world

                VRMLScene scene = core.getScene( );
                VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( DEFAULT_VIEWPOINT ));
                if ( node != null ) {

                    // if the named node exists, ensure that it is bound
                    // prior to performing the capture operation.
                    viewpoint = (BaseViewpoint)node;
                    if ( viewpoint.getIsBound( ) ) {
                        initiateCapture( );
                    } else {
                        // wait for the named viewpoint to be bound.
                        isBound_index = viewpoint.getFieldIndex( "isBound" );
                        viewpoint.addNodeListener( this );
                        construct.getViewpointManager( ).setViewpoint( viewpoint );
                    }
                } else {

                    // otherwise, auto configure the viewpoint for the snap
                    AutoConfigureViewpoint auto = new AutoConfigureViewpoint( construct, headlight );
                    boolean success = auto.configure( );
                    // regardless of whether the configure succeeded, do the capture
                    initiateCapture( );
                }

            } else {

                // assume it is specified, if found use it
                // otherwise default to basic xj3d viewpoint logic

                VRMLScene scene = core.getScene( );
                VRMLNodeType node = (VRMLNodeType)(scene.getDEFNodes( ).get( viewpointName ));
                if ( node != null ) {
                    // if the named node exists, ensure that it is bound
                    // prior to performing the capture operation.
                    viewpoint = (BaseViewpoint)node;
                    if ( viewpoint.getIsBound( ) ) {
                        initiateCapture( );
                    } else {
                        // wait for the named viewpoint to be bound.
                        isBound_index = viewpoint.getFieldIndex( "isBound" );
                        viewpoint.addNodeListener( this );
                        construct.getViewpointManager( ).setViewpoint( viewpoint );
                    }
                } else {
                    // otherwise, just capture the default bound viewpoint
                    initiateCapture( );
                }
            }
        } else {
            // otherwise, just capture the default bound viewpoint
            initiateCapture( );
        }

    }

    /**
     * Process the screen capture buffer into a BufferedImage and save it to a file
     *
     * @param buffer The screen capture buffer
     * @param width The width of the image
     * @param height The height of the image
     */
    public void saveScreen( Buffer buffer, int width, int height ) {

        ByteBuffer pixelsRGB = (ByteBuffer)buffer;
        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency.
        // Flip image vertically by reading the rows of pixels
        // in the byte buffer in reverse -
        // (0,0) is at bottom left in OpenGL.

        int p = width * height * 3; // Points to first byte (red) in each row.
        int q;                      // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int w3 = width*3;           // Number of bytes in each row

        if ( postProcess ) {
            for (int row = 0; row < height; row++) {
                p -= w3;
                q = p;

                for (int col = 0; col < width; col++) {
                    int iR = pixelsRGB.get(q++);
                    int iG = pixelsRGB.get(q++);
                    int iB = pixelsRGB.get(q++);

                    int color = ((iR & 0x0000_00FF) << 16)
                        | ((iG & 0x0000_00FF) << 8)
                        | (iB & 0x0000_00FF);

                    if ( color == snapRGB ) {
                        pixelInts[i++] = imageARGB;
                    } else {
                        pixelInts[i++] = 0xFF00_0000 | color;
                    }
                }
            }
        } else {
            for ( int row = 0; row < height; row++ ) {
                p -= w3;
                q = p;

                for ( int col = 0; col < width; col++ ) {
                    int iR = pixelsRGB.get( q++ );
                    int iG = pixelsRGB.get( q++ );
                    int iB = pixelsRGB.get( q++ );

                    pixelInts[i++] =
                        0xFF00_0000 |
                        ((iR & 0x0000_00FF) << 16) |
                        ((iG & 0x0000_00FF) << 8) |
                        (iB & 0x0000_00FF);
                }
            }
        }

        BufferedImage bufferedImage;
        if ( hasAlpha ) {
            bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB);
        }

        bufferedImage.setRGB( 0, 0, width, height, pixelInts, 0, width );

        try {
            errorReporter.messageReport(
                LOG_NAME +": Writing image file: "+ outputFile );

            long startTime = System.nanoTime();
            ImageIO.write( bufferedImage, type, outputFile );
            fileTime += System.nanoTime() - startTime;

        } catch (IOException e) {
            errorReporter.errorReport(
                LOG_NAME +": Error writing image file: "+ outputFile, e );
        }
    }

    /**
     * Initiate the capture
     */
    private void initiateCapture( ) {

        startFrameTime = System.nanoTime();

        core.captureScreenOnce( this );

        if ( listener != null ) {
            listener.recorderStatusChanged(new RecorderEvent(
                this, RecorderEvent.ACTIVE,
                number ) );
        }
    }
}
