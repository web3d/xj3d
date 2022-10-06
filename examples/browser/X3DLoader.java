/*
Copyright (c) 1995-2015 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.movesinstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

// Standard imports
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.j3d.util.I18nManager;

// Local imports
import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.geospatial.GeoCoordinate;
import org.web3d.x3d.sai.geospatial.GeoOrigin;
import org.web3d.x3d.sai.geospatial.GeoTransform;
import org.web3d.x3d.sai.rendering.Color;
import org.web3d.x3d.sai.rendering.IndexedLineSet;
import org.web3d.x3d.sai.shape.Shape;
import org.xj3d.sai.Xj3DBrowser;

/**
 * Xj3D loader that can be used to load 3D simulation scenes that render using
 * JOGL's high performance native windowing toolkit (NEWT) frame which is
 * parented by a Swing UI which allows for high performance rendering that is
 * decoupled from Swing's EDT model.
 *
 * @since        : 19 November 2003
 * @author Lee, Chin Siong Daryl, Terry Norbraten
 * @version $Id: X3DLoader.java 145 2009-09-23 18:22:27Z tdnorbra $
 */
public class X3DLoader extends JFrame {

    /** Property describing the DIS Protocol Handler to use */
    public static final String DISPROTOCOL_HANDLER_PROP =
            "org.web3d.xj3d.dis.protocol.handler.class";

    /** Property describing positional dead reckoning */
    protected static final String DEADRECKON_POSITION_PROP =
            "org.web3d.vrml.renderer.common.dis.input.deadreckonPosition";

    /** Property describing rotational dead reckoning */
    protected static final String DEADRECKON_ROTATION_PROP =
            "org.web3d.vrml.renderer.common.dis.input.deadreckonRotation";

    /** Name of the application that we'll default to */
    private static final String APP_NAME = "X3DLoader";

    /** Typical usage message with program options */
    private static final String USAGE_MSG = "Usage: " + APP_NAME + " [options] [filename]\n" +
      "  -help                   Prints out this help message\n" +
      "  -newt                   Use a GLWindow (NEWT) component to render\n" +
      "  -swing-lightweight      Use a GLJPanel (Swing) component to render\n" +
      "  -swing                  Use a GLCanvas (AWT) component to render\n" +
      "\n" +
      "  Please specify the full path to a scenegraph file for [filename]";

    /** The location of the resource property bundle in the classpath */
    private static final String CONFIG_FILE = "config.i18n.xj3dResources";

    /** Render frame type */
    private static String renderFrameType;
    
    /** Capture our base file URL */
    private static File fileUrl;

    /** Interface to the browser to do stuff with it */
    private Xj3DBrowser _x3dBrowser;

    /** Scene for the main file to load */
    private X3DScene _mainScene;

    /** The External Browser component for Xj3D */
    private X3DComponent x3d_comp;

    /** External Browser initialization parameters */
    private Map<String, Object> requestedParameters;

    /** Creates a new instance of X3DLoader */
    public X3DLoader() {
        super("3D Simulation Viewer");
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        setPreferredSize(new Dimension(800, 600));
        initBrowser();
        pack();

        Runnable run = () -> {
            setVisible(true);
        };
        SwingUtilities.invokeLater(run);
    }

    /**
     * Get the X3D component used by this loader.
     *
     * @return The component used.
     */
    public X3DComponent getX3DComponent() {
        return x3d_comp;
    }

    /**
     * Set the minimum frame cycle interval to throttle rendering.  0 is full speed.
     *
     * @param millis The number of milliseconds.
     */
    public void setMinimumFrameInterval(int millis) {
        _x3dBrowser.setMinimumFrameInterval(millis);
    }

    /**
     * Get the X3D browser used by this loader.
     *
     * @return The browser object.
     */
    public ExternalBrowser getBrowser() {
        return _x3dBrowser;
    }

    /** @return a reference to the active scene */
    public X3DScene getMainScene() {
        return _mainScene;
    }

    /**
     * Load a new scene.  This will replace the currently loaded scene.
     * @param url the scenegraph file url
     */
    public void load(File url) {
        String path = url.getPath();

        // Strip out any offending single quote characters
        if (path.contains("'"))
            path = path.split("'")[1];

        if (!new File(path).exists()) {
            writeErr("File " + url + " does not exist");
            return;
        }
        setMainScene(new String[] {path});
        render();
        writeLn("main scene URL is: " + _mainScene.getWorldURL()); // test
    }

    /**
     * Load a new scene.  This will replace the currently loaded scene.
     * @param is the scenegraph file stream
     */
    public void load(InputStream is) {
        setMainScene(is);
        render();
        writeLn("main scene URL is: " + _mainScene.getWorldURL()); // test
    }

    /**
     * Add an inlined scene to the scene currently loaded
     *
     * @param strURL The urls to load in priority order.
     * @return An object token for later deletion
     */
    public X3DNode addInlineNode(String[] strURL) {
        writeLn("Adding: " + strURL[0]);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        X3DNode inline = _mainScene.createNode("Inline");
        MFString url_field = (MFString) inline.getField("url");
        url_field.setValue(strURL.length, strURL);

        _x3dBrowser.beginUpdate();
        _mainScene.addRootNode(inline);
        _x3dBrowser.endUpdate();

        return inline;
    }

    /**
     * Add a GeoTransform to the scene currently loaded
     *
     * @param points the array containing the tunnel start and end points
     * @param tunnelID
     * @return An object token for later deletion
     */
    public X3DNode addGeoTransformNode(double[] points, int tunnelID) {

        // TODO: This method hardcodes most known values for a particular use.  Add
        // method parameters that will allow variation on the various GeoTransform
        // node values

        writeLn("Adding GeoTransform Node for tunnelID: " + tunnelID);

        _x3dBrowser.beginUpdate();

        GeoTransform geoTransform = (GeoTransform) _mainScene.createNode("GeoTransform");

        // GeoCenter of Nogales, AZ area of interest (AOI)
        geoTransform.setGeoCenter(new double[] {31.332678d, -110.94267d, 0d});

        // Add the nominal height of these tunnel ILS renders
        geoTransform.setTranslation(new float[] {0f, 1330f, 0f});

        // Create the GeoTransform children node
        Shape shape = (Shape) _mainScene.createNode("Shape");

        IndexedLineSet ils = (IndexedLineSet) _mainScene.createNode("IndexedLineSet");
        shape.setGeometry(ils);

        Color color = (Color) _mainScene.createNode("Color");

        // Make tunnel ILS yellow
        color.setColor(new float[] {1f, 1f, 0f, 1f, 1f, 0f});
        ils.setColor(color);

        // How do we write to an initializeOnly field on a realized node?
        // Throws an InvalidWritableFieldException.  Default is true which
        // doesn't seem to hurt anything
//        ils.setColorPerVertex(false);

        // Now work the GeoCoordinate node up
        GeoCoordinate geoCoordinate = (GeoCoordinate) _mainScene.createNode("GeoCoordinate");

        GeoOrigin geoOrigin = (GeoOrigin) _mainScene.getNamedNode("ORIGIN");
        geoCoordinate.setGeoOrigin(geoOrigin);

        geoCoordinate.setPoint(points);

        ils.setCoord(geoCoordinate);

        ils.setCoordIndex(new int[] {0, 1});

        geoTransform.setChildren(new X3DNode[] {shape});

        _mainScene.addRootNode(geoTransform);
        _x3dBrowser.endUpdate();

        return geoTransform;
    }

    /**
     * <p>Remove a node from the scene.  Must provide the object
     * returned from an addInlineNode call.</p>
     *
     * @param node The object returned from an addInlineNode call.
     */
    public void removeSAINode(X3DNode node) {

        _x3dBrowser.beginUpdate();
        _mainScene.removeRootNode(node);
        _x3dBrowser.endUpdate();
        node.dispose();
    }

    //----------------------------------------------------------------------------
    /**
     * write a error message to console
     * @param aStr line to be written to console
     */
    private void writeErr(String aStr) {
        System.err.println(aStr);
    }

    /**
     * write a line to console
     * @param aStr line to be written to console
     */
    private void writeLn(String aStr) {
        System.out.println(aStr);
    }

    /**
     * write a line to console
     */
    private void writeLn() {
        writeLn("");
    }

    //---------------------------------------------------------------
    // Methods defined by Window
    //---------------------------------------------------------------

    @Override
    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {

            // This sequence instead of System.exit(0)

            // This is causing JVM crashes during JComponent.removeNotify
//            getContentPane().remove((Component) x3d_comp);

            // Includes JOGL, JOAL and browser resource disposal
            getX3DComponent().shutdown();
            dispose();
            requestedParameters.clear();
            requestedParameters = null;
            _mainScene = null;
            _x3dBrowser = null;
            x3d_comp = null;
            fileUrl = null;
            
            // Crash issue w/ JOGL v2.4 (fix)
//            System.exit(0);
        } else if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            writeLn("Shutting down Xj3D");
        }

        // Pass along all other events
        super.processWindowEvent(e);
    }

    private void initBrowser() {

        /* Enable messages from the I18nManager.  NOTE: By default, it is first
         * set in org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl.
         * We are re-setting here to override which properties we want
         * available from the I18nManager.
         */
        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication(APP_NAME, CONFIG_FILE);

        // Turn off rotation dead reckoning
        System.setProperty(DEADRECKON_ROTATION_PROP, "false");

        // Turn off position dead reckoning.  This helps with recorded packet
        // single stepping
        System.setProperty(DEADRECKON_POSITION_PROP, "false");

        // Enable the origin manager
        System.setProperty("org.xj3d.core.eventmodel.OriginManager.enabled", "true");

        // Beef up the # of content thread loaders (for GeoSpatial LODs)
        System.setProperty("org.xj3d.core.loading.threads", "4");

        // Note: anisotropicDegree will be set to max if texture quality is set
        // to high.  MipMaps will automatically be turned on if aniotropicDegree is > 2.

        // http://xj3d.org/tutorials/xj3d_application.html
        // http://www.web3d.org/files/specifications/19775-1/V3.2/Part01/components/networking.html#t-BrowserProperties
        requestedParameters = new HashMap<>();

        // Setting awt for renderType does not work.
        requestedParameters.put("Xj3D_InterfaceType", renderFrameType);  // default is newt
        requestedParameters.put("Xj3D_FPSShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_LocationPosition", "top");
        requestedParameters.put("Xj3D_LocationReadOnly", Boolean.FALSE);
        requestedParameters.put("Xj3D_OpenButtonShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_ReloadButtonShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_ShowConsole", Boolean.FALSE);
        requestedParameters.put("Xj3D_StatusBarShown", Boolean.TRUE);
        requestedParameters.put("Xj3D_AntialiasingQuality", "high");
        requestedParameters.put("Antialiased", Boolean.TRUE);
        requestedParameters.put("PrimitiveQuality", "high");
        requestedParameters.put("TextureQuality", "high");
        requestedParameters.put("Shading", Boolean.TRUE);
        // Xj3D anisotropicDegree 16 derived from TextureQuality high in BrowserComposite.java

        requestedParameters.put("Xj3D_ContentDirectory", fileUrl.getPath());

        x3d_comp = BrowserFactory.createX3DComponent(requestedParameters);

        _x3dBrowser = (Xj3DBrowser) x3d_comp.getBrowser();

        // Add the component to the UI
        getContentPane().add((Component) x3d_comp);

        // Show where from and what resource is being set for the I18nManager
//        writeLn(intl_mgr.getApplication());
//        writeLn(intl_mgr.getResourceName());
    }

    private void setMainScene(String[] args) {

        try {
            _mainScene = getBrowser().createX3DFromURL(args);
        } catch (InvalidBrowserException | InvalidX3DException ex) {
            writeErr(ex.getMessage());
        }
    }

    /** The first "/" of the URL needs to be stripped (suspect the org.ietf.uri
     * stuff is not handling good file:/// formation).  The file separator is
     * then required to resolve other URLs (i.e. images, etc)
     * @param is the InputStream to process
     */
    private void setMainScene(InputStream is) {

        try {
            _mainScene = getBrowser().createX3DFromStream(fileUrl.getParent().replaceFirst("/", "") + File.separator, is);
        } catch (IOException ex) {
            writeErr(ex.getMessage());
        }
    }

    private void render() {
        setMinimumFrameInterval(40);
        _x3dBrowser.replaceWorld(_mainScene);
    }

    /** Command line entry point to X3DLoader
     * @param args command line arguments if any
     */
    public static void main(final String[] args) {

        int lastUsed = -1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    case "-newt":
                        renderFrameType = args[i].substring(args[i].indexOf("-")+1);
                        lastUsed = i;
                        break;
                    case "-swing-lightweight":
                        renderFrameType = args[i].substring(args[i].indexOf("-")+1);
                        lastUsed = i;
                        break;
                    case "-swing":
                        renderFrameType = args[i].substring(args[i].indexOf("-")+1);
                        lastUsed = i;
                        break;
                    case "-help":
                        System.out.println(USAGE_MSG);
                        return;
                    default:
                        System.err.println("Unknown render type flag: " + args[i] + " Defaulting to NEWT.");
                        renderFrameType = "newt";
                        lastUsed = i;
                        break;
                }
            }
        }

        if((args.length > 0) && (lastUsed + 1 < args.length)) {

            fileUrl = new File(args[args.length - 1]);
            X3DLoader loader = new X3DLoader();

            // File invocation
            loader.load(fileUrl);

            // *** InputStream invocation ***

            // Strip out any offending single quote characters
//            if (args[args.length - 1].contains("'")) {
//                args[args.length - 1] = args[args.length - 1].split("'")[1];
//            }
//
//            InputStream fis = null;
//            try {
//                fis = new FileInputStream(fileUrl);
//                loader.load(fis);
//            } catch (FileNotFoundException ex) {
//                loader.writeErr(ex.getMessage());
//            } finally {
//                try {
//                    if (fis != null) {
//                        fis.close();
//                    }
//                } catch (IOException ex) {}
//            }

            // *** End InputStream invocation ***
        }
    }

} // end class file X3DLoader.java
