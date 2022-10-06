package nodetest;

/*

 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.InvalidNodeException;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/**
 *
 */
public class EncodeNodes extends tController {

    /**
     * Immersive profile ID
     */
    static final String IMMERSIVE = "Immersive";

    /**
     * Renderer choice
     */
    static BrowserFactoryImpl bfi = new org.xj3d.ui.awt.browser.ogl.X3DOGLBrowserFactoryImpl();

    /**
     * Encoding choice
     */
    static final int XML = 0;

    static final int CLASSIC = 1;

    static int encode = XML;

    /**
     * Field value source choice
     */
    static tValue source = tValue.FIELD;

    /**
     * Input filename, file containing list of nodes to test
     */
    static String infile = "nodetest/x3d_immersive_node_list.txt";

    /**
     * Output filename, file to direct the encoded output to
     */
    static String outfile = "output";

    /**
     * Usage message
     */
    static final String USAGE
            = "Usage: EncodeNodes [options]\n"
            + "  -help                   Print this usage message and exit\n"
            + "  -render [ogl|j3d]       Renderer selection, default is ogl\n"
            + "  -encode [xml|classic]   Encoding selection, default is xml\n"
            + "  -value [default|smoke]  Field value source, default reads the value from\n"
            + "                          the node, smoke sets the value used in the smoke test\n"
            + "  -infile filename        Optional input filename, the list of nodes to encode\n"
            + "                          the default is \"./x3d_immersive_node_list.txt\"\n"
            + "  -outfile filename       Output filename, default is \"output\",\n"
            + "                          the file extension is determined by the encoding,\n"
            + "                          \"x3d\" for xml encoding, \"x3dv\" for classic encoding\n";

    /**
     * Entry point of this node test
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        //
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "-render": {
                        final String subArg = args[++i];
//                        if (subArg.equals("j3d")) {
//                            bfi = new org.web3d.j3d.browser.X3DJ3DBrowserFactoryImpl();
//                        }
                        if (subArg.equals("ogl")) {
                            //bfi = new org.web3d.ogl.browser.X3DOGLBrowserFactoryImpl( );
                        } else {
                            System.err.println("Unknown renderer: " + subArg + " - using default");
                        }
                        break;
                    }
                    case "-encode": {
                        final String subArg = args[++i];
                        switch (subArg) {
                            case "xml":
                                encode = XML;
                                break;
                            case "classic":
                                encode = CLASSIC;
                                break;
                            default:
                                System.err.println("Unknown encoding: " + subArg + " - using default");
                                break;
                        }
                        break;
                    }
                    case "-value": {
                        final String subArg = args[++i];
                        switch (subArg) {
                            case "default":
                                source = tValue.FIELD;
                                break;
                            case "smoke":
                                source = tValue.SMOKE;
                                break;
                            default:
                                System.err.println("Unknown value source: " + subArg + " - using default");
                                break;
                        }
                        break;
                    }
                    case "-outfile":
                        outfile = args[++i];
                        break;
                    case "-infile":
                        infile = args[++i];
                        break;
                    case "-help":
                        System.out.println(USAGE);
                        System.exit(0);
                    default:
                        System.err.println("Unknown argument: " + arg + " - ignored");
                        break;
                }
            } else {
                System.err.println("Unknown argument: " + arg + " - ignored");
            }
        }
        //
        final int exitStatus = new EncodeNodes().exec();
        System.exit(exitStatus);
    }

    /**
     * Constructor
     */
    public EncodeNodes() {
        //
        Map<String, Object> params = new HashMap<>();
        params.put("Xj3D_ShowConsole", Boolean.FALSE);
        params.put("Xj3D_LocationShown", Boolean.FALSE);
        params.put("Xj3D_NavbarShown", Boolean.FALSE);
        //params.put("Xj3D_LocationReadOnly",Boolean.TRUE);
        //params.put("Xj3D_LocationPosition","Top");
        //params.put("Xj3D_NavigationPosition","Bottom");
        //
        BrowserFactory.setBrowserFactoryImpl(bfi);
        X3DComponent component = BrowserFactory.createX3DComponent(params);
        this.browser = component.getBrowser();
        this.profile = this.browser.getProfile(IMMERSIVE);
        switch (encode) {
            case XML:
                this.scene = this.browser.createX3DFromString(tEncodingUtils.X3D_XML_IMMERSIVE_HEADER
                        + tEncodingUtils.X3D_XML_FOOTER);
                break;
            case CLASSIC:
                this.scene = this.browser.createX3DFromString(tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER);
                break;
        }
    }

    /**
     * Execute
     *
     * @return the exit status
     */
    int exec() {
        //
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(infile));
        } catch (FileNotFoundException fnfe) {
            logMessage(tMessageType.WARNING, "Node list: " + infile + " not found");
            return ERROR;
        }
        String outfilename = null;
        if (encode == XML) {
            outfilename = outfile + "." + tEncodingUtils.X3D_XML_EXT;
        } else if (encode == CLASSIC) {
            outfilename = outfile + "." + tEncodingUtils.X3D_CLASSIC_EXT;
        }
        //
        BufferedWriter output;
        try {
            output = new BufferedWriter(new FileWriter(new File(outfilename)));
            switch (encode) {
                case XML:
                    addLine(output, tEncodingUtils.XML_HEADER);
                    addLine(output, tEncodingUtils.X3D_XML_IMMERSIVE_HEADER);
                    addLine(output, "  " + tEncodingUtils.X3D_XML_SCENE_HEADER);
                    break;
                case CLASSIC:
                    addLine(output, tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER);
                    break;
            }
            output.newLine();
        } catch (IOException ioe) {
            logMessage(tMessageType.WARNING, ioe.getMessage());
            return ERROR;
        }
        logMessage(tMessageType.STATUS, "Directing encoded output to: " + outfilename);
        //
        final List<tX3DField> childNodeList = new ArrayList<>();
        //
        try {
            String nodeName;
            while ((nodeName = input.readLine()) != null) {
                if (nodeName.startsWith("#")) {
                    logMessage(tMessageType.STATUS, "Skipping processing of " + nodeName.substring(1));
                } else {
                    X3DNode node = null;
                    X3DFieldDefinition[] fieldDefs = null;
                    boolean success;
                    try {
                        node = this.scene.createNode(nodeName);
                        fieldDefs = node.getFieldDefinitions();
                        success = true;
                    } catch (InvalidNodeException e) {
                        logMessage(tMessageType.ERROR, nodeName + " node processing failed:", e);
                        success = false;
                    }
                    if (success) {
                        //
                        childNodeList.clear();
                        switch (encode) {
                            case XML:
                                addLine(output, "    <" + nodeName);
                                break;
                            case CLASSIC:
                                addLine(output, nodeName + " {");
                                break;
                        }
                        //
                        for (X3DFieldDefinition def : fieldDefs) {
                            try {
                                tX3DField field = tX3DFieldFactory.getInstance(node, def, this);
                                if (field == null) {
                                    logMessage(tMessageType.ERROR, nodeName + ":" + def.getName() + ":"
                                            + " unknown field type: " + def.getFieldTypeString());
                                } else {
                                    final int access = def.getAccessType();
                                    switch (encode) {
                                        case XML:
                                            if ((field instanceof tSFNode)
                                                    || (field instanceof tMFNode)) {
                                                childNodeList.add(field);
                                            } else if (access != X3DFieldTypes.INPUT_ONLY) {
                                                // don't try to read an eventIn
                                                addLine(output, "      " + field.encode(source, tEncode.XML));
                                            } else {
                                                // include the eventIn with a null value
                                                addLine(output, "      " + def.getName() + "=\"\"");
                                            }
                                            break;
                                        case CLASSIC:
                                            if (access != X3DFieldTypes.INPUT_ONLY) {
                                                // don't try to read an eventIn
                                                addLine(output, "  " + field.encode(source, tEncode.CLASSIC));
                                            } else {
                                                // include the eventIn with a null value
                                                addLine(output, "  " + def.getName());
                                            }
                                            break;
                                    }
                                }
                            } catch (IOException e) {
                                logMessage(tMessageType.ERROR, nodeName + ":" + def.getName() + " field processing failed:", e);
                            }
                        }
                        switch (encode) {
                            case XML:
                                final int children = childNodeList.size();
                                if (children == 0) {
                                    addLine(output, "      />");
                                } else {
                                    addLine(output, "      >");
                                    for (int i = 0; i < children; i++) {
                                        tX3DField childNode = childNodeList.get(i);
                                        addLine(output, "      " + childNode.encode(source, tEncode.XML));
                                    }
                                    addLine(output, "    </" + nodeName + ">");
                                }
                                break;
                            case CLASSIC:
                                addLine(output, "}");
                                break;
                        }
                        output.newLine();
                    }
                }
            }
            switch (encode) {
                case XML:
                    addLine(output, "  " + tEncodingUtils.X3D_XML_SCENE_FOOTER);
                    addLine(output, tEncodingUtils.X3D_XML_FOOTER);
                    break;
                case CLASSIC:
                    break;
            }
            output.flush();
            output.close();
        } catch (IOException ioe) {
            logMessage(tMessageType.ERROR, "Exception reading node list: " + ioe.getMessage());
        }
        return exitStatus;
    }

    /**
     * Add a <code>String</code> to the specified <code>BufferedWriter</code>
     * output
     *
     * @param output the <code>BufferedWriter</code>
     * @param line the <code>String</code> to add
     */
    static void addLine(final BufferedWriter output, final String line) throws IOException {
        output.write(line, 0, line.length());
        output.newLine();
    }
}
