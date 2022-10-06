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
package org.web3d.util;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Creates the version stamp information when built via regex updates from Ant
 * @version $Id: Version.java 12657 2019-12-22 18:53:52Z tnorbraten $
 * @author <a href="mailto:tdnorbra@nps.edu?subject=org.web3d.util.Version">Terry Norbraten</a>
 */
public class Version {

    /** These must be programmatically changed by the developer */
    public static final String BUILD_MAJOR_VERSION = getProjectProperties().getString("product.version.major");
    public static final String BUILD_MINOR_VERSION = getProjectProperties().getString("product.version.level") + "-nps";
    public static final String JAVA_VERSION = System.getProperty("java.version");

    /** These strings are set by the timestamp task in the build file
     * so need not be modified here
     */
    public static final String BUILD_DSTAMP = "20090227";
    public static final String BUILD_TSTAMP = "1407";
    public static final String BUILD_TODAY  = "February 27 2009";

    public static final String SP = " ";
    public static final String PERIOD = ".";

    private static final String DIS_VER = getProjectProperties().getString("open-dis.jar");
    public static final String OPEN_DIS_VERSION = "NPS Open DIS v" +
            DIS_VER.substring(DIS_VER.indexOf("_")+1, DIS_VER.indexOf(".jar"));

    /** Customizable message to be displayed */
    public static final String DEVELOPER_CUSTOM_MESSAGE = "\nutilizing " +
            OPEN_DIS_VERSION + " with 3D rendering by\n" +
            com.jogamp.opengl.JoglVersion.getInstance();
	
	// https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
	public static final String OS_JAVA_VERSION_MESSAGE  = "Operating system: " + System.getProperty("os.name") + SP + System.getProperty("os.version") + "\n" +
                                                          "Java environment: " + System.getProperty("java.vendor") + SP + JAVA_VERSION + "\n";
    /**
     * The release version. Milestone format will be
     * <code>M<i>MainVersion</i>_<i>DevRelease#</i></code>
     */
    public static final String XJ3D_VERSION = "v" + BUILD_MAJOR_VERSION + PERIOD +
            BUILD_MINOR_VERSION + SP + DEVELOPER_CUSTOM_MESSAGE + SP + "\n" +
			OS_JAVA_VERSION_MESSAGE + 
            "\nBuildStamp time and date:" + SP + BUILD_TSTAMP + SP + "on" + SP + BUILD_TODAY;

    /**
     * <p>Project specific Project properties resourced from
     * configuration/project.properties.  These are not expected to dynamically
     * change during runtime.</p>
     * @return specific Project properties resourced from config/xj3d.properties
     */
    public static ResourceBundle getProjectProperties() {
        return PropertyResourceBundle.getBundle("config.xj3d");
    }

    /**
     * Command line entry point for this class
     * @param args command line arguments if any
     */
    public static void main(String args[]) {
        System.out.println("Xj3D" + SP + XJ3D_VERSION);
    }

} // end class file Version.java
