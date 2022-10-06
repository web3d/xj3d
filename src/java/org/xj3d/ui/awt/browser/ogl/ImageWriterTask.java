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
import java.util.concurrent.atomic.AtomicInteger;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.xj3d.ui.construct.event.RecorderEvent;
import org.xj3d.ui.construct.event.RecorderListener;

/**
 * Writes an image out to disk.  Implemented as a runnable task to allow
 * multi-threading.
 *
 * @author Alan Hudson
 */
class ImageWriterTask extends Thread {
    private static final boolean DEBUG = false;

    private RecorderListener listener;
    private Object parent;
    private AtomicInteger completed;
    private int total;
    private String type;
    private File outputFile;
    private boolean hasAlpha;
    private int width;
    private int height;
    private ByteBuffer pixelsRGB;
    private boolean postProcess;

    /** RGB value for snap pixels that should be replaced */
    protected int snapRGB;

    /* ARGB value to replace the designated snap pixels */
    protected int imageARGB;

    /**
     * This task does the actual screen capture
     *
     * @param parent the ScreenCaptureListener instance
     * @param listener the RecorderListener instance
     * @param completed a thread safe integer keeping track of image capture counts
     * @param total the total number of expected screen captures
     * @param hasAlpha if true, make transparent
     * @param width the width of the image
     * @param height the hight of the image
     * @param buffer our pixel bugger
     * @param type the type of image, i.e. png, jpg, etc.
     * @param postProcess if true, add background color to the image
     * @param snapRGB alpha mask value
     * @param imageARGB image background color value
     * @param outputFile the file to write to
     */
    public ImageWriterTask(Object parent,
            RecorderListener listener, AtomicInteger completed, int total,
            boolean hasAlpha, int width, int height, ByteBuffer buffer,
            String type, boolean postProcess, int snapRGB, int imageARGB,
            File outputFile) {

        super(ImageWriterTask.class.getName());

        this.listener = listener;
        this.postProcess = postProcess;

        this.parent = parent;

        pixelsRGB = buffer;
        this.completed = completed;
        this.total = total;
        this.hasAlpha = hasAlpha;
        this.width = width;
        this.height = height;
        this.type = type;
        this.outputFile = outputFile;
        this.snapRGB = snapRGB;
    }

    @Override
    public void run() {
        doIt();
    }

    /** Performs the capture file write */
    public void doIt() {
        if (DEBUG) System.out.println("Running IO: " + Thread.currentThread().getName());

        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency.
        // Flip image vertically by reading the rows of pixels
        // in the byte buffer in reverse -
        // (0,0) is at bottom left in OpenGL.

        int p = width * height * 3; // Points to first byte (red) in each row.
        int q;                      // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int w3 = width*3;           // Number of bytes in each row

        if (postProcess) {
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

                    if (color == snapRGB) {
                        pixelInts[i++] = imageARGB;
                    } else {
                        pixelInts[i++] = 0xFF00_0000 | color;
                    }
                }
            }
        } else {
            for (int row = 0; row < height; row++) {
                p -= w3;
                q = p;

                for (int col = 0; col < width; col++) {
                    int iR = pixelsRGB.get(q++);
                    int iG = pixelsRGB.get(q++);
                    int iB = pixelsRGB.get(q++);

                    pixelInts[i++] =
                        0xFF00_0000 |
                        ((iR & 0x0000_00FF) << 16) |
                        ((iG & 0x0000_00FF) << 8) |
                        (iB & 0x0000_00FF);
                }
            }
        }


        BufferedImage bufferedImage;

        if (hasAlpha) {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);


        try {
            ImageIO.write(bufferedImage, type, outputFile);
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }

        int done = completed.incrementAndGet();

if (DEBUG) System.out.println("Done writing.  cnt: " + done + " total: " + total);
        if (done >= total) {
            if (listener != null) {
                listener.recorderStatusChanged(
                    new RecorderEvent(
                    parent,
                    RecorderEvent.COMPLETE,
                    done));
            }
        }

        parent = null;
        listener = null;
        pixelsRGB = null;
        outputFile = null;
    }
}
