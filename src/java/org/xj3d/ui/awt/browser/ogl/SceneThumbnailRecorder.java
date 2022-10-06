/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
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

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

import org.j3d.aviatrix3d.pipeline.OutputDevice;
import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.browser.ScreenCaptureListener;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;
import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * A function module that performs an image capture of an
 * X3D scene for the purpose of creating a thumbnail image for all viewpoints.
 * The image capture is managed as follows:
 * <ul>
 * <li>On the completion of the world and content loading, all the Viewpoint
 * nodes are searched for and bound if necessary.</li>
 * <li>The image capture is initiated immediately if the Viewpoint is not found,
 * or the DEF'ed Viewpoint is found and currently bound.</li>
 * <li>Otherwise, the capture is initiated immediately upon notification that the
 * Viewpoint has been bound.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class SceneThumbnailRecorder implements ScreenCaptureListener {

    private static final boolean DEBUG = false;

    /** Maximum number of concurrent threads for image saving */
    private static int poolSize;

    /** The logging identifier of this class */
    private static final String LOG_NAME = "SceneThumbnailRecorder";

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

    /** Flag used in the end of frame listener, indicating that the new nodes
    * may be added to the scene */
    protected boolean addNodes;

    /** Synchronization flag */
    protected boolean configComplete;

    /** The sequence capture number */
    protected int sequenceCaptureNumber;

    /** The current unknown count */
    private int unknownCount;

    /** Name of the screen capture file */
    private String name;

    /** The viewpoint node, used when we have to wait for the
     *  named viewpoint to be bound
     */
    protected VRMLViewpointNodeType viewpoint;

    /** The output file for the captured images */
    protected File outputDir;

    /** Listener for recorder status events */
    protected RecorderListener listener;

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

    /** Prefix to filenames */
    private String filePrefix;

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

    /** The viewpoints left to process */
    protected List<VRMLViewpointNodeType> viewpoints;

    private ExecutorService pool;
    private int total;
    private AtomicInteger completed;

    /**
     * Constructor
     *
     * @param construct The construct instance to record from
     */
    public SceneThumbnailRecorder(OGLConstruct construct) {
        if (construct == null) {
            throw new IllegalArgumentException(
                LOG_NAME +": construct instance must be non-null");
        }

        this.construct = construct;
        errorReporter = construct.getErrorReporter();
        core = construct.getBrowserCore();
        core.setMinimumFrameInterval(0, true);
        canvas = ((OutputDevice)construct.getGraphicsObject()).getSurfaceObject();

        poolSize = java.lang.Runtime.getRuntime().availableProcessors();

        if(poolSize > 1) {
            pool = Executors.newFixedThreadPool(poolSize);
        } else {
            pool = Executors.newSingleThreadExecutor();
        }

        completed = new AtomicInteger();
    }

    //----------------------------------------------------------
    // Method required for ScreenCaptureListener
    //----------------------------------------------------------

    @Override
    public void screenCaptured(Buffer buffer, int width, int height) {

        // Start screen capture count
        sequenceCaptureNumber++;
if (DEBUG) System.out.println("Saving Screen: " + name);

        renderTime += System.nanoTime() - startFrameTime;
        saveScreen(buffer, name, width, height);
        fileTime += System.nanoTime() - startFrameTime;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Return the frame rendering time in milliseconds
     *
     * @return the frame rendering time in milliseconds
     */
    public double getRenderTime() {
        return((renderTime/sequenceCaptureNumber)/NANOS_PER_MILLI);
    }

    /**
     * Return the file write time in milliseconds
     *
     * @return the file write time in milliseconds
     */
    public double getFileTime() {
        return((fileTime/sequenceCaptureNumber)/NANOS_PER_MILLI);
    }

    /**
     * Set the image encoding type
     *
     * @param type The image encoding type
     * @return true if the encoding type is valid, false otherwise.
     */
    public boolean setEncoding(String type) {
        boolean found = false;
        String[] format = ImageIO.getWriterFormatNames();
        for (String format1 : format) {
            if (type.equals(format1)) {
                this.type = type;
                found = true;
                break;
            }
        }
        hasAlpha = false;
        if (!found) {
            errorReporter.errorReport(
                LOG_NAME +": Unknown image encoding type: "+ type, null);
        } else {
            for (String alphaType : alphaTypes) {
                if (type.equalsIgnoreCase(alphaType)) {
                    hasAlpha = true;
                    break;
                }
            }
        }
        return(found);
    }

    /**
     * Set the image size
     *
     * @param width The image width
     * @param height The image height
     */
    public void setSize(int width, int height) {
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
    @SuppressWarnings("null") // postProcess protects from NPE
    public void setBackgroundColor(
        Color x3dBackgroundColor,
        Color imageBackgroundColor) {

        postProcess = (x3dBackgroundColor != null) & (imageBackgroundColor != null);
        if (postProcess) {
            // mask off the alpha, the snap image is RGB only
            snapRGB = 0x00FF_FFFF & x3dBackgroundColor.getRGB();

            imageARGB = imageBackgroundColor.getRGB();
        }
    }

    /**
     * Set the output directory for images
     *
     * @param dir The output directory for images
     */
    public void setOutputDirectory(File dir) {
        outputDir = dir;
    }

    /**
     * Set the name prefix for images
     *
     * @param prefix The prefix for file names
     */
    public void setFilePrefix(String prefix) {
        filePrefix = prefix;
    }

    /**
     * Initiate the capture
     * @param listener
     */
    public void start(RecorderListener listener) {
        fileTime = 0;
        renderTime = 0;
        if (outputDir == null) {
            errorReporter.warningReport(
                LOG_NAME +": Unable to record, output dir "+
                "is not initialized.", null);
            return;
        }

        this.listener = listener;

        int tmp_width = 0;
        int tmp_height = 0;
        if (canvas instanceof Component) {
            Dimension size = ((Component)canvas).getSize();
            tmp_width = (int)size.getWidth();
            tmp_height = (int)size.getHeight();
        } else if (canvas instanceof GLDrawable) {
            tmp_width = ((GLDrawable)canvas).getSurfaceWidth();
            tmp_height = ((GLDrawable)canvas).getSurfaceHeight();
        }

        // configure the size from the rendering surface,
        // only if the surface returns something non-zero
        if ((tmp_width > 0) && (tmp_height > 0)) {
            width = tmp_width;
            height = tmp_height;
        }

        sequenceCaptureNumber = 0; // always taking at least one snapshot for default viewpoint

        viewpoints = construct.getViewpointManager().getActiveViewpoints();
        total = viewpoints.size();

        if (viewpoints.isEmpty()) {
            name = "_VP_Unnamed_1";
            initiateCapture();
        } else {

            for (VRMLViewpointNodeType vp : viewpoints)
            {
                if (!vp.getIsBound())
                    construct.getViewpointManager().setViewpoint(vp);

                String description = vp.getDescription();

                if (description != null) {
                    description = description.replace(" ", "_");
                    description = description.replace("\\", "_");
                    description = description.replace("/", "_");
					// omit special characters
                    description = description.replace("'",  "");
                    description = description.replace("\"", "");
                    description = description.replace("+",  "");
                } else {
                    unknownCount++;
                    description = "ViewpointMissingDescription_" + unknownCount;
                }
                name = ".x3d" + "._VP_" + description;
		
				// do not allow colons in file name
				if (name.contains(":") || name.contains("#"))
				{
					name = name.replaceAll(":","").replaceAll("#","");
					System.err.println( "SceneThumbnailRecorder: name cannot include colon or # character, corrected as " + name);
				}
				if (name.isEmpty())
				{
					System.err.println( "SceneThumbnailRecorder: name cannot be empty, ignored!");
					return;
				}

				String outputImagePath = outputDir.getAbsolutePath() + "/" + filePrefix + name + ".png"; // TODO fix
				System.out.println("Saving viewpoint as image file " + outputImagePath);
				
/* Not fixed but handled instead by build.xml process
                // first save a default error image to intended destination, which is soon overwritten if the operation goes well.
                // TODO relative path still not working  8(  8(
                String captureFailedImagePath = "images/Xj3dRenderCaptureFailed.png"; // org/xj3d/ui/awt/browser/ogl/images
                try
                {
                    File captureFailedImage = new File(captureFailedImagePath);
                    BufferedImage failureImage = ImageIO.read(captureFailedImage );

                    ImageIO.write(failureImage, "jpg", new File(outputImagePath));
                }
                catch (IOException e)
                {
                    System.out.println("Xj3D intermediate error for SceneThumbnailRecorder saving Xj3dRenderCaptureFailed.png as preliminary image file prior to capture...");
                    System.out.println("  image path:" + captureFailedImagePath);
                    System.out.println("  " + e.getMessage());
                }
*/

                // This delay is key in letting the viewpoint fully render before an
                // adequate screen capture can occur
                try {
                    Thread.sleep(250);
                } catch(InterruptedException e) {}

                initiateCapture();
            }
        }
    }

    /**
     * Process the screen capture buffer into a BufferedImage and save it to a file
     *
     * @param buffer The screen capture buffer
     * @param name The file name prefix
     * @param width The width of the image
     * @param height The height of the image
     */
    private void saveScreen(Buffer buffer, String name, int width, int height) {
        ByteBuffer pixelsRGB = (ByteBuffer)buffer;
		
		// do not allow colons in file name
		if (name.contains(":") || name.contains("#"))
		{
			name = name.replaceAll(":","").replaceAll("#","");
			System.err.println( "saveScreen name cannot include colon or # character, corrected as " + name);
		}
		if (name.isEmpty())
		{
			System.err.println( "saveScreen name cannot be empty, ignored!");
			return;
		}

        File outputFile = new File(outputDir, filePrefix + name + "." + type);

        ImageWriterTask iwt = new ImageWriterTask(this, listener, completed,
                total, hasAlpha, width, height, pixelsRGB, type, postProcess,
                snapRGB, imageARGB, outputFile);
        iwt.doIt();
        finished = true;
//        pool.execute((Runnable) iwt);
    }
    boolean finished;

    /**
     * Initiate the capture and record status changed events
     */
    private void initiateCapture() {
if (DEBUG) System.out.println("Starting capture: " + sequenceCaptureNumber);
        finished = false;
        startFrameTime = System.nanoTime();

        core.captureScreenOnce(this);

        if (listener != null) {
            listener.recorderStatusChanged(new RecorderEvent(
                this,
                RecorderEvent.ACTIVE,
                sequenceCaptureNumber));
        }

        while (!finished) {
            Thread.yield();
        }
    }
}
