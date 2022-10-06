/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// External imports
// None

// Local imports
import org.xj3d.sai.Xj3DBrowser;

/**
 * Collection of constants used across the browser internals.
 *
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.19 $
 */
public interface Xj3DConstants {
    
    /**
     * The release version. Milestone format will be
     * <code>M<i>MainVersion</i>_<i>DevRelease#</i></code>
     */
    String VERSION = "2-0_RC1";

    /** Definition of the Java3D renderer */
    int JAVA3D_RENDERER = 1;

    /** ID String of the Java3D renderer */
    String JAVA3D_ID = "java3d";

    /** Definition of the null renderer */
    int NULL_RENDERER = 2;

    /** ID String of the null renderer */
    String NULL_ID = "norender";

    /** Definition of the OpenGL immersive renderer */
    int OPENGL_RENDERER = 3;

    /** ID String of the OpenGL renderer */
    String OPENGL_ID = "aviatrix3d";

    /** Definition of the OpenGL mobile device renderer */
    int MOBILE_RENDERER = 4;

    /** ID String of the mobile renderer */
    String MOBILE_ID = "mobile";

    /** ID String of the AWT ui toolkit */
    String AWT_ID = "awt";

    /** ID String of the SWT ui toolkit */
    String SWT_ID = "swt";

    /** The rendering style uses point mode */
    int RENDER_POINTS = Xj3DBrowser.RENDER_POINTS;

    /** The rendering style uses wireframe mode */
    int RENDER_LINES = Xj3DBrowser.RENDER_LINES;

    /** The rendering style uses flat shading mode */
    int RENDER_FLAT = Xj3DBrowser.RENDER_FLAT;

    /** The rendering style uses a generic shading model */
    int RENDER_SHADED = Xj3DBrowser.RENDER_SHADED;

    /** Nav mode string representing the None mode */
    String NONE_NAV_MODE = "NONE";

    /** Nav mode string representing the Any mode */
    String ANY_NAV_MODE = "ANY";

    /** Nav mode string representing the Fly mode */
    String FLY_NAV_MODE = "FLY";

    /** Nav mode string representing the Walk mode */
    String WALK_NAV_MODE = "WALK";

    /** Nav mode string representing the Examine mode */
    String EXAMINE_NAV_MODE = "EXAMINE";

    /** Nav mode string representing the Orbit mode */
    String ORBIT_NAV_MODE = "ORBIT";

    /** Nav mode string representing the LookAt mode */
    String LOOKAT_NAV_MODE = "LOOKAT";

    /** Nav mode string representing the Pan mode */
    String PAN_NAV_MODE = "xj3d_PAN";

    /** Nav mode string representing the Tilt mode */
    String TILT_NAV_MODE = "xj3d_TILT";

    /** Nav mode string representing the Inspect mode */
    String INSPECT_NAV_MODE = "xj3d_INSPECT";

    /** Nav mode string representing the tracking Examine mode */
    String TRACK_EXAMINE_NAV_MODE = "xj3d_TRACK_EXAMINE";

    /** Nav mode string representing the tracking Pan mode */
    String TRACK_PAN_NAV_MODE = "xj3d_TRACK_PAN";

    /** Nav mode string representing the Fly Level */
    String FLY_LEVEL_NAV_MODE = "xj3d_FLY_LEVEL";
}
