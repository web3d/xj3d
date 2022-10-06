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
import org.j3d.aviatrix3d.output.graphics.PbufferSurface;

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * An abstract sub-class of NEWTOGLConstruct that provides offscreen rendering
 * capabilities to the base Construct which is decoupled from the Swing/AWT EDT
 * model
 *
 * @author @author <a href="mailto:tdnorbra@nps.edu?subject=org.xj3d.ui.newt.offscreen.browser.ogl.OffscreenOGLConstruct">Terry Norbraten, NPS MOVES</a>
 * @version $Id: OffscreenOGLConstruct.java 12586 2016-09-21 23:01:24Z tnorbraten $
 */
public abstract class OffscreenOGLConstruct extends NEWTOGLConstruct {

    /** The width of the rendering surface */
    protected int width;

    /** The height of the rendering surface */
    protected int height;

    /**
     * Constructor
     */
    protected OffscreenOGLConstruct() {
        this(null, 0, 0);
    }

    /**
     * Constructor
     *
     * @param reporter The error reporter
     * @param width
     * @param height
     */
    protected OffscreenOGLConstruct(ErrorReporter reporter, int width, int height) {
        super(reporter);
        UI_DEVICE_FACTORY = null;
        this.width = width;
        this.height = height;
    }

    //----------------------------------------------------------
    // Methods defined by Construct
    //----------------------------------------------------------

    @Override
    protected void buildGraphicsRenderingDevice() {

        graphicsDevice =
            new PbufferSurface(glCapabilities, width, height);
    }

    @Override
    protected void buildAudioRenderingDevice() {
    }
}
