package nodetest;

/*

 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.web3d.x3d.sai.BrowserFactory;
import org.web3d.x3d.sai.BrowserFactoryImpl;
import org.web3d.x3d.sai.InvalidBrowserException;
import org.web3d.x3d.sai.InvalidNodeException;
import org.web3d.x3d.sai.InvalidX3DException;
import org.web3d.x3d.sai.X3DComponent;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/**
 *
 */
public class SmokeSAIEncodedNodes extends tController {

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
     * X3D console display choice
     */
    static boolean displayConsole;

    /**
     * Print encoded node choice
     */
    static boolean print;

    /**
     * Exit condition choice
     */
    static final int EXIT_ON_COMPLETION = 0;

    static final int EXIT_ON_ERROR = 1;

    static final int EXIT_ON_CLOSE = 2;

    static int exitCondition = EXIT_ON_COMPLETION;

    /**
     * Input filename, file containing list of nodes to test
     */
    static String infile = "nodetest/x3d_immersive_node_list.txt";

    /**
     * Output filename, file to direct the encoded output to
     */
    static String outfile;

    /**
     * Usage message
     */
    static final String USAGE
            = "Usage: SmokeSAIEncodedNodes [options]\n"
            + "  -help                                 Print this usage message and exit\n"
            + "  -render [ogl|j3d]                     Renderer selection, default is ogl\n"
            + "  -encode [xml|classic]                 Encoding selection, default is xml\n"
            + "  -exit [onClose|onCompletion|onError]  Exit condition selection, default is onCompletion\n"
            + "  -console                              Display the X3D browser console\n"
            + "  -print                                Print the encoded nodes prior to parsing\n"
            + "  -infile filename                      Optional input filename, the list of nodes to encode,\n"
            + "                                        the default is \"./x3d_immersive_node_list.txt\"\n"
            + "  -outfile filename                     Optional output filename for the encoded nodes,\n"
            + "                                        the file extension is determined by the encoding\n"
            + "                                        \"x3d\" for xml encoding, \"x3dv\" for classic encoding\n";

    public static void main(final String[] args) {
        //
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "-render": {
                        final String subArg = args[++i];
//                            if (subArg.equals("j3d")) {
//                                bfi = new org.web3d.j3d.browser.X3DJ3DBrowserFactoryImpl();
//                            }
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
                    case "-exit": {
                        final String subArg = args[++i];
                        switch (subArg) {
                            case "onCompletion":
                                break;
                            case "onError":
                                exitCondition = EXIT_ON_ERROR;
                                break;
                            case "onClose":
                                exitCondition = EXIT_ON_CLOSE;
                                break;
                            default:
                                System.err.println("Unknown exit condition: " + subArg + " - using default");
                                break;
                        }
                        break;
                    }
                    case "-console":
                        displayConsole = true;
                        break;
                    case "-print":
                        print = true;
                        break;
                    case "-infile":
                        infile = args[++i];
                        break;
                    case "-outfile":
                        outfile = args[++i];
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
        final int exitStatus = new SmokeSAIEncodedNodes().exec();
        //
        switch (exitCondition) {
            case EXIT_ON_COMPLETION:
            case EXIT_ON_ERROR:
                System.exit(exitStatus);
                break;
            case EXIT_ON_CLOSE:
        }
    }

    /**
     * Constructor
     */
    public SmokeSAIEncodedNodes() {
        final JFrame frame = new JFrame();
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        //
        Map<String, Object> params = new HashMap<>();
        params.put("Xj3D_ShowConsole", displayConsole);
        params.put("Xj3D_LocationShown", Boolean.FALSE);
        params.put("Xj3D_NavbarShown", Boolean.FALSE);
        //params.put("Xj3D_LocationReadOnly",Boolean.TRUE);
        //params.put("Xj3D_LocationPosition","Top");
        //params.put("Xj3D_NavigationPosition","Bottom");
        //
        BrowserFactory.setBrowserFactoryImpl(bfi);
        final X3DComponent component = BrowserFactory.createX3DComponent(params);
        contentPane.add((Component) component, BorderLayout.CENTER);
        this.browser = component.getBrowser();
        this.profile = this.browser.getProfile(IMMERSIVE);
        this.scene = this.browser.createScene(profile, null);
        //
        initialize();
        frame.pack();
        frame.setSize(200, 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //
        ///////////////////////////////////////////////////////////////////////////
        // if setVisible( true ) is not called, the browser is never initialize,
        // no events happen, and writes to node fields are never processed.
        Runnable r = () -> {
            frame.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
        // additionally, the j3d browser will not process events unless the
        // frame is visible, iconifying the frame prevents events from being
        // processed. seems to have no effect on the ogl browser
        //test.setExtendedState( JFrame.ICONIFIED );
        //
        ///////////////////////////////////////////////////////////////////////////
    }

    /**
     * Execute.
     *
     * @return the exit status
     */
    int exec() {
        //
        initWrapper();
        //
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(infile));
        } catch (FileNotFoundException fnfe) {
            logMessage(tMessageType.WARNING, "Node list: " + infile + " not found");
            return (ERROR);
        }
        BufferedWriter output = null;
        if (outfile != null) {
            String outfilename = null;
            if (encode == XML) {
                outfilename = outfile + "." + tEncodingUtils.X3D_XML_EXT;
            } else if (encode == CLASSIC) {
                outfilename = outfile + "." + tEncodingUtils.X3D_CLASSIC_EXT;
            }
            //
            try {
                output = new BufferedWriter(new FileWriter(new File(outfilename)));
                switch (encode) {
                    case XML:
                        addLine(output, tEncodingUtils.XML_HEADER);
                        addLine(output, tEncodingUtils.X3D_XML_IMMERSIVE_HEADER);
                        addLine(output, tEncodingUtils.X3D_XML_SCENE_HEADER);
                        break;
                    case CLASSIC:
                        addLine(output, tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER);
                        break;
                }
                output.newLine();
            } catch (IOException ioe) {
                logMessage(tMessageType.WARNING, ioe.getMessage());
                return (ERROR);
            }
            logMessage(tMessageType.STATUS, "Directing encoded output to: " + outfilename);
        }
        //
        // wait till the browser is ready to process events
        synchronized (this) {
            try {
                while (!browserInitialized) {
                    wait();
                }
            } catch (InterruptedException ie) {
            }
        }
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
                        // wait till the next event cascade to give the
                        // node time to complete the creation phase
//                        flushUpdate();
                        //
                        StringBuilder sceneBuffer = new StringBuilder(256);
                        StringBuffer nodeBuffer = new StringBuffer(256);
                        switch (encode) {
                            case XML:
                                sceneBuffer.append(tEncodingUtils.X3D_XML_IMMERSIVE_HEADER + "\n");
                                sceneBuffer.append(tEncodingUtils.X3D_XML_SCENE_HEADER + "\n");
                                nodeBuffer.append("<").append(nodeName).append("\n");
                                break;
                            case CLASSIC:
                                sceneBuffer.append(tEncodingUtils.X3D_CLASSIC_IMMERSIVE_HEADER + "\n");
                                nodeBuffer.append(nodeName).append(" {\n");
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
                                    //
                                    // do not include eventIns or eventOuts in the test encoding
                                    if ((access != X3DFieldTypes.INPUT_ONLY)
                                            && (access != X3DFieldTypes.OUTPUT_ONLY)) {
                                        switch (encode) {
                                            case XML:
                                                //
                                                // child nodes are implicit in the XML markup, so
                                                // do not attempt to include them in the encoding
                                                // as they are by default null
                                                final int type = def.getFieldType();
                                                if ((type != X3DFieldTypes.SFNODE)
                                                        && (type != X3DFieldTypes.MFNODE)) {
                                                    nodeBuffer.append(field.encode(tValue.SMOKE, tEncode.XML)).append("\n");
                                                }
                                                break;
                                            case CLASSIC:
                                                //
                                                // child nodes are explicit in the classic encoding,
                                                // so include them despite the fact that they are empty
                                                nodeBuffer.append(field.encode(tValue.SMOKE, tEncode.CLASSIC)).append("\n");
                                                break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logMessage(tMessageType.ERROR, nodeName + ":" + def.getName()
                                        + " field processing failed:", e);
                            }
                            if ((exitCondition == EXIT_ON_ERROR) && (exitStatus == ERROR)) {
                                break;
                            }
                        }
                        switch (encode) {
                            case XML:
                                nodeBuffer.append("/>\n");
                                wrap(nodeName, tEncode.XML, nodeBuffer);
                                sceneBuffer.append(nodeBuffer);
                                sceneBuffer.append(tEncodingUtils.X3D_XML_SCENE_FOOTER + "\n");
                                sceneBuffer.append(tEncodingUtils.X3D_XML_FOOTER + "\n");
                                break;
                            case CLASSIC:
                                nodeBuffer.append("}\n");
                                wrap(nodeName, tEncode.CLASSIC, nodeBuffer);
                                sceneBuffer.append(nodeBuffer);
                                break;
                        }
                        try {
                            if (print) {
                                logMessage(tMessageType.STATUS, new String[]{
                                    nodeName + ":" + " beginning parse",
                                    sceneBuffer.toString()});
                            }
                            scene = browser.createX3DFromString(sceneBuffer.toString());
                            if (scene != null) {
                                logMessage(tMessageType.SUCCESS, nodeName + ":" + " createX3DFromString complete");
                            } else {
                                logMessage(tMessageType.ERROR, nodeName + ":" + " createX3DFromString returned null");
                            }
                        } catch (InvalidBrowserException | InvalidX3DException e) {
                            logMessage(tMessageType.ERROR, nodeName + ":" + " createX3DFromString failed:", e);
                        }
                        if (output != null) {
                            addLine(output, nodeBuffer.toString());
                        }
                    }
                    if ((exitCondition == EXIT_ON_ERROR) && (exitStatus == ERROR)) {
                        break;
                    }
                }
            }
            if (output != null) {
                switch (encode) {
                    case XML:
                        addLine(output, tEncodingUtils.X3D_XML_SCENE_FOOTER);
                        addLine(output, tEncodingUtils.X3D_XML_FOOTER);
                        break;
                    case CLASSIC:
                        break;
                }
                output.flush();
                output.close();
            }
        } catch (IOException ioe) {
            logMessage(tMessageType.ERROR, "Exception reading node list: " + ioe.getMessage());
        }
        return (exitStatus);
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

    /**
     * Map, keyed by container field names, value is a <code>List</code> of node
     * names that must be included in a scene as a child node of the container
     * field
     */
    static Map<String, List<String>> nodeTypeMap = new HashMap<>();

    /**
     * Initialize the nodeTypeMap from a set of text files. The file
     * <code>child_node_types.txt</code> contains the set of map keys which also
     * are the names of the files which contain the list of nodes.
     */
    static void initWrapper() {
        BufferedReader type_input = null;
        try {
            type_input = new BufferedReader(new FileReader("nodetest/child_node_types.txt"));
        } catch (FileNotFoundException fnfe) {
        }
        try {
            String node_type;
            while ((node_type = type_input.readLine()) != null) {
                BufferedReader node_input = null;
                try {
                    node_input = new BufferedReader(new FileReader("nodetest/" + node_type + ".txt"));
                } catch (FileNotFoundException fnfe) {
                }
                List<String> nodeNameList = new ArrayList<>();
                String node_name;
                while ((node_name = node_input.readLine()) != null) {
                    nodeNameList.add(node_name);
                }
                nodeTypeMap.put(node_type, nodeNameList);
            }
        } catch (IOException ioe) {
        }
    }

    /**
     * Locate the node in the nodeTypeMap, thus determining the appropriate
     * container field. Construct a valid child node encoding for the node based
     * on the container.
     */
    static void wrap(final String nodeName, final tEncode encode, final StringBuffer encodedNode) {
        String nodeType = null;
        boolean nodeFound = false;

        System.out.println("Wrapping: " + nodeName);

        for (Iterator<String> keys = nodeTypeMap.keySet().iterator(); keys.hasNext();) {
            nodeType = keys.next();
            final List<String> nodeNameList = nodeTypeMap.get(nodeType);
            for (String nodeNameList1 : nodeNameList) {
                if (nodeNameList1.equals(nodeName)) {
                    nodeFound = true;
                    break;
                }
            }
            if (nodeFound) {
                break;
            }
        }
        if (nodeFound) {
            //System.out.println( nodeName +" is of type "+ nodeType );
            switch (nodeType) {
                case "children":
                    break;
                case "geometry":
                case "appearance":
                case "metadata":
                    if (encode == tEncode.XML) {
                        encodedNode.insert(0, SHAPE_XML[0]);
                        encodedNode.append(SHAPE_XML[1]);
                    } else if (encode == tEncode.CLASSIC) {
                        encodedNode.insert(0, SHAPE_CLASSIC[0] + nodeType + " ");
                        encodedNode.append(SHAPE_CLASSIC[1]);
                    }
                    break;
                case "material":
                case "texture":
                case "textureTransform":
                case "lineProperties":
                    if (encode == tEncode.XML) {
                        encodedNode.insert(0, SHAPE_XML[0] + APPEARANCE_XML[0]);
                        encodedNode.append(APPEARANCE_XML[1]).append(SHAPE_XML[1]);
                    } else if (encode == tEncode.CLASSIC) {
                        encodedNode.insert(0, SHAPE_CLASSIC[0] + APPEARANCE + APPEARANCE_CLASSIC[0] + nodeType + " ");
                        encodedNode.append(APPEARANCE_CLASSIC[1]).append(SHAPE_CLASSIC[1]);
                    }
                    break;
                case "color":
                case "coord":
                case "texCoord":
                case "normal":
                    if (encode == tEncode.XML) {
                        encodedNode.insert(0, SHAPE_XML[0] + IFS_XML[0]);
                        encodedNode.append(IFS_XML[1]).append(SHAPE_XML[1]);
                    } else if (encode == tEncode.CLASSIC) {
                        encodedNode.insert(0, SHAPE_CLASSIC[0] + GEOMETRY + IFS_CLASSIC[0] + nodeType + " ");
                        encodedNode.append(IFS_CLASSIC[1]).append(SHAPE_CLASSIC[1]);
                    }
                    break;
                case "fontStyle":
                    if (encode == tEncode.XML) {
                        encodedNode.insert(0, SHAPE_XML[0] + TEXT_XML[0]);
                        encodedNode.append(TEXT_XML[1]).append(SHAPE_XML[1]);
                    } else if (encode == tEncode.CLASSIC) {
                        encodedNode.insert(0, SHAPE_CLASSIC[0] + GEOMETRY + TEXT_CLASSIC[0] + nodeType + " ");
                        encodedNode.append(TEXT_CLASSIC[1]).append(SHAPE_CLASSIC[1]);
                    }
                    break;
                case "soundSource":
                    if (encode == tEncode.XML) {
                        encodedNode.insert(0, SOUND_XML[0]);
                        encodedNode.append(SOUND_XML[1]);
                    } else if (encode == tEncode.CLASSIC) {
                        encodedNode.insert(0, SOUND_CLASSIC[0] + SOURCE + nodeType + " ");
                        encodedNode.append(SOUND_CLASSIC[1]);
                    }
                    break;
            }
        }
    }

    /**
     * Strings used in wrap()
     */
    static String[] SHAPE_XML = {"<Shape>\n", "</Shape>\n"};

    static String[] SHAPE_CLASSIC = {"Shape { \n", "}\n"};

    static String[] APPEARANCE_XML = {"<Appearance>\n", "</Appearance>\n"};

    static String[] APPEARANCE_CLASSIC = {"Appearance { \n", "}\n"};

    static String APPEARANCE = "appearance ";

    static String GEOMETRY = "geometry ";

    static String SOURCE = "source ";

    static String[] TEXT_XML = {"<Text>\n", "</Text>\n"};

    static String[] TEXT_CLASSIC = {"Text { \n", "}\n"};

    static String[] IFS_XML = {"<IndexedFaceSet>\n", "</IndexedFaceSet>\n"};

    static String[] IFS_CLASSIC = {"IndexedFaceSet { \n", "}\n"};

    static String[] SOUND_XML = {"<Sound>\n", "</Sound>\n"};

    static String[] SOUND_CLASSIC = {"Sound { \n", "}\n"};

}
