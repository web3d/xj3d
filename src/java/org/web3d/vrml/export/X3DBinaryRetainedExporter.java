/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export;

// External imports
import java.io.*;

// Local imports
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.nodes.*;


/**
 * X3D XML exporter using a retained Scenegraph.
 *
 * TODO: I do not think this class is used anymore and its binary processing
 * is now incorrect.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class X3DBinaryRetainedExporter extends X3DXMLRetainedExporter
    implements SceneGraphTraversalSimpleObserver {

    /** The original stream */
    private OutputStream origStream;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param compressionMethod The method to use for compression
     * @param quantizeParam The largest quantization acceptable using lossy methods
     */
    public X3DBinaryRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam) {

        super(new ByteArrayOutputStream(), major, minor, errorReporter);

        this.compressionMethod = compressionMethod;
        this.quantizeParam = quantizeParam;

        if (compressionMethod == X3DBinarySerializer.METHOD_SMALLEST_LOSSY) {
            useNC = true;
        }

        origStream = os;

        printDocType = false;
        stripWhitespace = true;

        encodingTo = ".x3db";
    }

    /**
     * Write a scene out.
     *
     * @param scene The scene to write
     */
    @Override
    public void writeScene(VRMLScene scene) {
        boolean wait = false;

//long stage = startTime;
//System.out.println("Initial Parsing Time     : " + (System.currentTimeMillis() - stage));
//if (wait) waitForInput();

        super.writeScene(scene);

//System.out.println("Write to XML encoded file: " + (System.currentTimeMillis() - stage));
//if (wait) waitForInput();
//stage = System.currentTimeMillis();

        String buff = ((StringWriter)filterWriter).toString();
//System.out.println("Convert to String        : " + (System.currentTimeMillis() - stage));
//stage = System.currentTimeMillis();

        StringBufferInputStream bis = new StringBufferInputStream(buff);
        BufferedInputStream buffis = new BufferedInputStream(bis);

        X3DBinarySerializer bs = new X3DBinarySerializer(compressionMethod,
           false, quantizeParam);

        bs.setProtoMap(protoMap);
        bs.writeFiltered(buffis, origStream);

        try {
            origStream.close();
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
//System.out.println("Write to FI Binary       : " + (System.currentTimeMillis() - stage));
//System.out.println("total time               : " + (System.currentTimeMillis() - startTime));
    }

    private void waitForInput() {
        InputStreamReader isr = new InputStreamReader ( System.in );
        BufferedReader br = new BufferedReader ( isr );
        String s = null;

        System.out.println("Press return to continue");
        try {
           s = br.readLine ();
        }
        catch ( IOException ioe ) {
           // won't happen too often from the keyboard
        }
    }
}
