/*
Copyright (c) 1995-2014 held by the author(s).  All rights reserved.

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
package org.xj3d.ui.newt.offscreen.browser.ogl;

// External imports
import org.j3d.aviatrix3d.output.graphics.SimpleNEWTSurface;

// Local imports
import org.j3d.util.ErrorReporter;

import org.xj3d.ui.construct.ogl.OGLConstruct;

/**
 * An abstract sub-class of OGLConstruct that provides NEWT UI capabilities
 * to the base Construct.
 *
 * @author @author <a href="mailto:tdnorbra@nps.edu?subject=org.xj3d.ui.newt.offscreen.browser.ogl.NEWTOGLConstruct">Terry Norbraten, NPS MOVES</a>
 * @version $Id: NEWTOGLConstruct.java 12256 2015-08-10 13:55:34Z tnorbraten $
 */
public abstract class NEWTOGLConstruct extends OGLConstruct {

    ///////////////////////////////////////////////////////////////////
    // Toolkit specific classes

    /** AWT Toolkit specific device factory */
    protected String AWT_UI_DEVICE_FACTORY =
        "org.xj3d.ui.awt.device.AWTDeviceFactory";

    /** AWT Toolkit specific content handler factory */
    protected String AWT_CONTENT_HANDLER_FACTORY =
        "org.xj3d.ui.awt.net.content.AWTContentHandlerFactory";

    ///////////////////////////////////////////////////////////////////

    /**
     * Constructor
     */
    protected NEWTOGLConstruct( ) {
        this( null );
    }

    /**
     * Constructor
     *
     * @param reporter The error reporter
     */
    protected NEWTOGLConstruct( ErrorReporter reporter ) {
        super( reporter );
        UI_DEVICE_FACTORY = AWT_UI_DEVICE_FACTORY;
        CONTENT_HANDLER_FACTORY = AWT_CONTENT_HANDLER_FACTORY;
    }

    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------

    /**
     * Create the toolkit specific graphics rendering device
     */
    @Override
    protected void buildGraphicsRenderingDevice( ) {

        graphicsDevice = new SimpleNEWTSurface(
            glCapabilities,
            glCapabilitiesChooser);
    }

} // end class file NEWTOGLConstruct.java
