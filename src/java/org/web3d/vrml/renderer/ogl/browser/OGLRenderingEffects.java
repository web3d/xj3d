/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.j3d.aviatrix3d.SimpleScene;
import org.j3d.aviatrix3d.rendering.RenderEffectsProcessor;
import org.j3d.aviatrix3d.rendering.ProfilingData;

// Local imports
import org.web3d.browser.ProfilingListener;
import org.web3d.browser.ScreenCaptureListener;
import org.xj3d.sai.Xj3DBrowser;

/**
 * Pre and post frame rendering effects processing for any OpenGL renderer.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class OGLRenderingEffects implements RenderEffectsProcessor {

    /** Message when an invalid rendering style was provided */
    private static final String INVALID_STYLE_MSG =
        "An invalid rendering style was provided to the rendering effects " +
        "processor. Must be one of RENDER_*";

    /** The current polygon rendering mode */
    private int polygonMode;

    /** The Xj3D-specific rendering style constant */
    private int renderStyle;

    /** The screen capture listener */
    private ScreenCaptureListener screenCaptureListener;

    /** Are we in movie mode where each frame we capture an image */
    private boolean movieMode;

    /** The global scene this effects is on */
    private SimpleScene globalScene;

    /** The buffered used for screen captures */
    private ByteBuffer captureBuffer;

    /** The list of Profiling listeners */
    private List<ProfilingListener> profilingListeners;

    /** The width of the viewport in pixels */
    private int viewWidth;

    /** The height of the viewport in pixels */
    private int viewHeight;

    /** The last width */
    private int lastWidth;

    /** The last height */
    private int lastHeight;

    /** The screen size has changed */
    private boolean sizeChanged;

    /**
     * Construct a default instance initialised to have a mode of
     * shaded rendering.
     * @param scene
     */
    public OGLRenderingEffects(SimpleScene scene) {
        globalScene = scene;
        renderStyle = Xj3DBrowser.RENDER_SHADED;
        polygonMode = GL2.GL_FILL;

        movieMode = false;
        sizeChanged = false;

        profilingListeners = new ArrayList<>(1);
    }

    //----------------------------------------------------------
    // Methods defined by RenderEffectsProcessor
    //----------------------------------------------------------

    /**
     * Perform any pre-rendering setup that you may need for this scene. After
     * this call, all normal scene graph rendering is performed by the surface.
     *
     * @param gl The current GL context wrapper to draw with
     * @param userData Some identifiable data provided by the user
     */
    @Override
    public void preDraw(GL gl, Object userData) {
        if (polygonMode != GL2.GL_FILL) {
            gl.getGL2().glPolygonMode(GL2.GL_FRONT, polygonMode);
            gl.getGL2().glPolygonMode(GL2.GL_BACK, polygonMode);
        }
    }

    /**
     * Perform any post-rendering actions that you may need for this scene.
     * Called after the renderer has completed all drawing and just before the
     * buffer swap. The only thing to be called after calling this method is
     * glFlush().
     *
     * @param gl The current GL context wrapper to draw with
     * @param profilingData
     * @param userData Some identifiable data provided by the user
     */
    @Override
    public void postDraw(GL gl, ProfilingData profilingData, Object userData) {
        if (polygonMode != GL2.GL_FILL) {
            gl.getGL2().glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.getGL2().glPolygonMode(GL2.GL_BACK, GL2.GL_FILL);
        }

        if (screenCaptureListener != null) {
            if (captureBuffer == null || lastWidth != viewWidth || lastHeight != lastHeight) {
                captureBuffer = ByteBuffer.allocate(viewWidth * viewHeight * 3);
                lastWidth = viewWidth;
                lastHeight = viewHeight;
            } else {
                captureBuffer.rewind();
            }

            gl.glFlush();

            gl.getGL2().glReadBuffer(GL2.GL_BACK);
            gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
            gl.glReadPixels(0, 0, viewWidth, viewHeight,
                GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, captureBuffer);

            screenCaptureListener.screenCaptured(captureBuffer,
                                                 viewWidth,
                                                 viewHeight);

            if (!movieMode) {
                captureBuffer = null;
                screenCaptureListener = null;
            }
        }

        int len = profilingListeners.size();

        OGLProfilingInfo profilingInfo = new OGLProfilingInfo(profilingData);

        for(int i=0; i < len; i++) {
            ProfilingListener l = profilingListeners.get(i);
            l.profilingDataChanged(profilingInfo);
        }

//        System.out.println("Cull: " + Math.round(profilingData.sceneCullTime / 1000000) +
//           " Sort: " + Math.round(profilingData.sceneSortTime / 1000000) + " draw: " + Math.round(profilingData.sceneDrawTime / 1000000));
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Request notification of profiling information.
     *
     * @param l The listener
     */
    public void addProfilingListener(ProfilingListener l) {
        if(l == null)
            return;

        if(!profilingListeners.contains(l))
            profilingListeners.add(l);
    }

    /**
     * Remove notification of profiling information.
     *
     * @param l The listener
     */
    public void removeProfilingListener(ProfilingListener l) {
        if(l == null)
            return;

        profilingListeners.remove(l);
    }

    /**
     * Change the rendering style that the browser should currently be using.
     * Various options are available based on the constants defined in this
     * interface.
     *
     * @param style One of the RENDER_* constants
     * @throws IllegalArgumentException A style constant that is not recognized
     *   by the implementation was provided
     */
    public void setRenderingStyle(int style)
        throws IllegalArgumentException {

        switch(style) {
            case Xj3DBrowser.RENDER_POINTS:
                polygonMode = GL2.GL_POINT;
                break;

            case Xj3DBrowser.RENDER_LINES:
                polygonMode = GL2.GL_LINE;
                break;

            case Xj3DBrowser.RENDER_FLAT:
                polygonMode = GL2.GL_FILL;
                break;

            case Xj3DBrowser.RENDER_SHADED:
                polygonMode = GL2.GL_FILL;
                break;

            default:
                throw new IllegalArgumentException(INVALID_STYLE_MSG);
        }

        renderStyle = style;
    }

    /**
     * Get the currently set rendering style. The default style is
     * RENDER_SHADED.
     *
     * @return one of the RENDER_ constants
     */
    public int getRenderingStyle() {
        return renderStyle;
    }

    /**
     * Capture the screen on the next render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenOnce(ScreenCaptureListener listener, int width, int height) {
        screenCaptureListener = listener;

        viewWidth = width;
        viewHeight = height;
    }

    /**
     * Capture the screen on each render.
     *
     * @param listener Listener for capture results
     * @param width The screen width
     * @param height The screen height
     */
    public void captureScreenStart(ScreenCaptureListener listener, int width, int height) {
        screenCaptureListener = listener;

        viewWidth = width;
        viewHeight = height;
        movieMode = true;
    }

    /**
     * Stop capturing the screen on each render.
     */
    public void captureScreenEnd() {
        movieMode = false;
        screenCaptureListener = null;
    }
}
