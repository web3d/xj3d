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

// External imports
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.j3d.util.ErrorReporter;

// Local imports
import org.xj3d.ui.newt.offscreen.browser.ogl.NEWTOGLConstruct;

/**
 * The customized browser Construct sub-class supporting the ThumbnailImager.
 * This Construct is built with the following modifications to the
 * 'standard' Construct.
 * <ul>
 * <li>The construct is built without support for UI devices,
 * such as Mouse or Keyboard.</li>
 * </ul>
 *
 * @author Rex Melton
 * @version $Revision: 1.3 $
 */
public class ThumbnailConstruct extends NEWTOGLConstruct
    implements ConfigGraphicsCapabilities {

    /** The logging identifier of this class */
    private static final String LOG_NAME = ThumbnailConstruct.class.getName();

    /**
     * Constructor
     *
     * @param reporter The error reporter
     */
    public ThumbnailConstruct( ErrorReporter reporter ) {
        super( reporter );
    }

    //----------------------------------------------------------
    // Methods defined by ConstructBuilder
    //----------------------------------------------------------

    /**
     * Override to build a UI 'device-less' browser.
     */
    @Override
    public void buildAll( ) {
        buildRenderingCapabilities( );
        buildRenderingDevices( );
        //buildInterfaceDevices( );
        buildRenderer( );
        buildManagers( );

        // If we snapshot a scripted scene, will throw NPE without these (TDN)
        buildScriptEngines();
        buildNetworkCapabilities( );
    }

    @Override
    public void buildNetworkCapabilities( ) {
        super.buildNetworkCapabilities( );
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>( ) {
                    @Override
                    public Object run( ) {
                        String prop = System.getProperty("java.content.handler.pkgs","" );
                        if( !prop.contains("vlc.net.content")) {
                            System.setProperty( "java.content.handler.pkgs",
                                "vlc.net.content" );
                        }

                        return null;
                    }
                });
        } catch ( PrivilegedActionException pae ) {
            errorReporter.warningReport(
                LOG_NAME +": Exception setting System properties", pae );
        }
    }

    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------

    @Override
    protected void buildAudioRenderingDevice( ) {
    }

    //----------------------------------------------------------
    // Methods defined by ConfigGraphicsCapabilities
    //----------------------------------------------------------

    @Override
    public void setGraphicsCapabilitiesParameters(
        boolean useMipMaps,
        boolean doubleBuffered,
        int antialiasSamples,
        int anisotropicDegree ) {

        this.useMipMaps = useMipMaps;
        this.doubleBuffered = doubleBuffered;
        this.antialiasSamples = antialiasSamples;
        this.anisotropicDegree = anisotropicDegree;
    }
}
