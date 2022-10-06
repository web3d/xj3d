/****************************************************************************
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
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.sax.AttributesHolder;

import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

import java.io.*;
import java.util.*;

import org.xml.sax.SAXException;

// Local imports
import org.web3d.parser.x3d.*;
import org.web3d.util.DoubleToString;
import org.j3d.util.ErrorReporter;
import org.j3d.util.IntHashMap;
import org.web3d.util.XMLTools;
import org.web3d.vrml.export.compressors.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.nodes.proto.*;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.common.nodes.AbstractDynamicFieldNode;
import org.web3d.vrml.sav.*;

/**
 * A retained exporter that issues SAX events.  Extensions of this class
 * are expected to setup the content handler.  This class should contain
 * as much of the XML specific knowledge as possible.
 *
 * This implementation will use FastInfoSet's AttributesHolder to
 * gain a type aware wrapper for attributes.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public abstract class X3DRetainedSAXExporter extends BaseRetainedExporter
    implements SceneGraphTraversalSimpleObserver {

    // Encode files for fastest parsing
    public static final int METHOD_FASTEST_PARSING = 0;

    // Encode files for smallest size using non lossy techniques
    public static final int METHOD_SMALLEST_NONLOSSY = 1;

    // Encode files for smallest size using lossy techniques
    public static final int METHOD_SMALLEST_LOSSY = 2;

    // Encode files using Strings */
    public static final int METHOD_STRINGS = 3;

    /** The indent String to replicate per level */
    protected static String INDENT_STRING = "   ";

    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;

    /** An empty map to avoid null testing */
    protected Map EMPTY_MAP = new HashMap();

    /** The current indent level */
    protected int indent = 0;

    /** The current indent string */
    protected String indentString;

    /** A mapping of indent to String */
    protected IntHashMap<String> indentMap;

    /** Temporary map during traversal for use references */
    protected Set<VRMLNodeType> usedNodes;

    /** The passed in writer */
    protected Writer filterWriter;

    /** The current set of proto definitions */
    protected Set<PrototypeDecl> protoDeclSet;

    /** Traverser for printing proto's */
    protected SceneGraphTraverser traverser;

    /** The world root */
    protected VRMLWorldRootNodeType root;

    /** Should we print the DOCTYPE */
    protected boolean printDocType;

    /** Should we print the XML Element */
    protected boolean printXML;

    /** Should we use node compressors */
    protected boolean useNC = false;

    /** Should we ignore data, its handled by the compressor */
    protected boolean ignoreData;

    /** Single level proto map  */
    protected Map<String, VRMLFieldDeclaration> protoMap;

    /** The current compressor.  Needs to change to a generic interface */
    protected NodeCompressor currentCompressor;

    /** Switch between methods, should go away */
    protected boolean compressedAttWay = false;

    /** The contentHandler to write to */
    protected org.xml.sax.ContentHandler handler;

    /** The compression method to use for binary */
    protected int compressionMethod;

    /** The float lossy param */
    protected float quantizeParam;

    /** Are we exporting to binary */
    protected boolean binary;

    /** Extra attributes on the X3D tag */
    protected Map<String,String> x3dAtts;

    /** Should we strip all unnecessary characters */
    protected boolean compress = true;

    /** A newLine string */
    private static char[] newLineChar = {'\n'};

    /**
     * Create a new exporter for the given spec version
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param compressionMethod
     * @param quantizeParam
     */
    public X3DRetainedSAXExporter(int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam) {

        super(major, minor, errorReporter, -1);

        this.compressionMethod = compressionMethod;
        this.quantizeParam = quantizeParam;

        init();
    }

    /**
     * Create a new exporter for the given spec version
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param compressionMethod
     * @param quantizeParam
     * @param sigDigits
     */
    public X3DRetainedSAXExporter(int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam,
        int sigDigits) {

        this(major, minor, errorReporter, compressionMethod, quantizeParam);
        this.sigDigits = sigDigits;
    }

    private void init() {
        usedNodes = new HashSet<>();
        indentString = "";
        indentMap = new IntHashMap<>();
        protoDeclSet = new HashSet<>();
        traverser = new SceneGraphTraverser();
        protoMap = new HashMap<>();

        printDocType = false;
        printDocType = true;

        stripWhitespace = false;
        ignoreData = false;
    }

    /**
     * Set attributes to be added to the X3D tag.  Useful for namespace management.
     *
     * @param atts The attributes to add
     */
    public void setX3DAttributes(Map<String,String> atts) {
        if (atts != null) {
            x3dAtts = new HashMap<>();
            x3dAtts.putAll(atts);
        } else {
            x3dAtts = null;
        }
    }

    /**
     * Set the flag indicating that the doc type element should be included
     *
     * @param enable Flag indicating whether the doc type element should be included
     */
    public void setPrintDocType(boolean enable) {
        printDocType = enable;
    }

    /**
     * Return the flag indicating that the doc type element is to be included
     *
     * @return Whether the doc type element is to be included
     */
    public boolean getPrintDocType() {
        return(printDocType);
    }

    /**
     * Set the flag indicating that the xml element should be included
     *
     * @param enable Flag indicating whether the xml element should be included
     */
    public void setPrintXML(boolean enable) {
        printXML = enable;
    }

    /**
     * Return the flag indicating that the xml element is to be included
     *
     * @return Whether the xml element is to be included
     */
    public boolean getPrintXML() {
        return(printXML);
    }

    /**
     * Create an X3D string from an array of values
     *
     * @param decl The declaration
     * @param val The value
     * @param numElements The number of elements which make up a single value(ie 3 for MFVec3f)
     * @return The value as a string
     */
    protected String createX3DString(VRMLFieldDeclaration decl, float[] val, int numElements) {
        int span;
        int len2;
        boolean ismf;
        int idx;

        // Guess at possible length
        StringBuilder buff = new StringBuilder(val.length * (sigDigits+2));

        // Convert to String rep
        ismf = isMFField(decl);

        if (ismf) {
            len2 = numElements;

            if (len2 <= 0)
                return null;

            span = val.length / len2;
            idx = 0;
            for(int j=0; j < len2; j++) {
                for(int k=0; k < span; k++) {
                    if (sigDigits < 0)
                        buff.append(val[idx++]);
                    else {
                        DoubleToString.appendFormatted(buff, val[idx++], sigDigits);
                    }
                    if (k != span - 1)
                        buff.append(" ");
                }
                if (j != len2 -1) {
                    if (compress) {
                        buff.append(" ");
                    } else {
                        buff.append(", ");
                    }
                }
            }
        } else {
            len2 = val.length;
            for(int j=0; j < len2; j++) {
                if (sigDigits < 0)
                    buff.append(val[j]);
                else {
                    DoubleToString.appendFormatted(buff, val[j], sigDigits);
                }

                if (j != len2 -1 )
                    buff.append(" ");
            }
        }


        return buff.toString();
    }


    /**
     * Create an X3D string from an array of values
     *
     * @param decl The declaration
     * @param val The value
     * @param numElements The number of elements which make up a single value(ie 3 for MFVec3f)
     * @return The value as a string
     */
    protected String createX3DString(VRMLFieldDeclaration decl, double[] val, int numElements) {
        int span;
        int len2;
        boolean ismf;
        int idx;

        // Guess at possible length
        StringBuilder buff = new StringBuilder(val.length * 3);

        // Convert to String rep
        ismf = isMFField(decl);

        if (ismf) {
            len2 = numElements;

            if (len2 <= 0)
                return null;

            span = val.length / len2;
            idx = 0;
            for(int j=0; j < len2; j++) {
                for(int k=0; k < span; k++) {
                    buff.append(val[idx++]);
                    if (k != span - 1)
                        buff.append(" ");
                }

                if (j != len2 -1) {
                    buff.append(", ");
                }
            }
        } else {
            len2 = val.length;
            for(int j=0; j < len2; j++) {
                buff.append(val[j]);
                if (j != len2 -1 )
                    buff.append(" ");
            }
        }

        return buff.toString();
    }

    /**
     * Create an X3D string from an array of values
     *
     * @param decl The declaration
     * @param val The value
     * @param numElements The number of elements which make up a single value(ie 3 for MFVec3f)
     * @return The value as a string
     */
    protected String createX3DString(VRMLFieldDeclaration decl, int[] val, int numElements) {
        StringBuilder buff = new StringBuilder();

        for(int j=0; j < numElements - 1; j++) {
            buff.append(val[j]);
            buff.append(" ");
        }

        buff.append(val[numElements - 1]);

        return buff.toString();
    }

    /**
     * Encode float array data.
     * This base version will just use a string rep
     *
     * @param fval The parsed float array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    protected void encodeFloatArray(float[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            String st = createX3DString(decl, fval, numElements);

            if (st != null)
                aholder.addAttribute(new QualifiedName("", "",
                    qName), st);
        }
    }

    /**
     * Encode double array data.
     * This base version will just use a string rep.
     *
     * @param fval The parsed double array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    protected void encodeDoubleArray(double[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            int span;
            int len2;
            boolean ismf;
            int idx;

            // Guess at possible length
            StringBuilder buff = new StringBuilder(fval.length * 4);

            // Convert to String rep
            ismf = isMFField(decl);

            if (ismf) {
                len2 = numElements;

                if (len2 <= 0)
                    return;

                span = fval.length / len2;
                idx = 0;
                for(int j=0; j < len2; j++) {
                    for(int k=0; k < span; k++) {
                        buff.append(fval[idx++]);
                        if (k != span - 1)
                            buff.append(" ");
                    }
                    if (j != len2 -1) {
                        buff.append(", ");
                    }
                }
            } else {
                len2 = fval.length;
                for(int j=0; j < len2; j++) {
                    buff.append(fval[j]);
                    if (j != len2 -1 )
                        buff.append(" ");
                }
            }

            aholder.addAttribute(new QualifiedName("", "",
                qName), buff.toString());
        }
    }

    /**
     * Write a scene out.
     *
     * @param scene The scene to write
     */
    public void writeScene(VRMLScene scene) {
        // Traverse SG, call ContentHandler interface to write out
        usedNodes.clear();

        Map<String, VRMLNode> defs = scene.getDEFNodes();
        currentDefMap = new HashMap<>(defs.size());
        reverseMap(defs, currentDefMap);
        Map<VRMLNode, String> saveMap = currentDefMap;

        List<VRMLNodeTemplate> protoList = scene.getNodeTemplates();
        Iterator<VRMLNodeTemplate> itr = protoList.iterator();

        root = (VRMLWorldRootNodeType) scene.getRootNode();

        Object proto;
        traverser.setObserver(this);

        while(itr.hasNext()) {
            proto = itr.next();

            if (proto instanceof ExternalPrototypeDecl) {
                printExternalPrototypeDecl((CRExternPrototypeDecl)proto);

                usedNodes.clear();
            } else {
                printPrototypeDecl((PrototypeDecl)proto);
                List<VRMLFieldDeclaration> fields = ((VRMLNodeTemplate)proto).getAllFields();
                Iterator<VRMLFieldDeclaration> itr2 = fields.iterator();

                while(itr2.hasNext()) {
                    VRMLFieldDeclaration decl = itr2.next();

                    if(decl == null)
                        continue;

                    protoMap.put(((VRMLNodeTemplate)proto).getVRMLNodeName() + "." + decl.getName(), decl);
                }

                usedNodes.clear();
            }
        }

        currentDefMap = saveMap;
        traverse(root, true);

        printImports(scene.getImports());

        List<ROUTE> routeList = scene.getRoutes();
        int len = routeList.size();

        for(int i=0; i < len; i++) {
            printROUTE(routeList.get(i), currentDefMap);
        }

        printExports(scene.getExports());

        try {
            handler.endElement("","Scene","Scene");
            printNewLine();

            handler.endElement("","X3D","X3D");
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        // TODO: Double happen currently from SAVAdaptor and GeneralisedReader.
        if (!processingDocument)
            return;

        processingDocument = false;

        super.endDocument();

        writeScene(scene);

        try {
            handler.endDocument();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print the header.
     *
     * @param major The major version
     * @param minor The minor version
     */
    @Override
    public void printHeader(int major, int minor) {
        try {
            handler.startDocument();
        } catch(SAXException se) {
            handleSAXException(se);
        }

        loadContainerProperties(major, minor);
    }

    @Override
    public void printExporterInfo() {
//        p.print("# Exported by Xj3D's: ");
//        p.print(getClass().getName() + " v" + Version.BUILD_MAJOR_VERSION + PERIOD + BUILD_MINOR_VERSION);
//        p.println();
    }

    /**
     * Print the profile decl.
     *
     * @param profile The profile
     */
    @Override
    public void printProfile(String profile) {

        String publicId = getPublicId(majorVersion,minorVersion);
        String systemId = getSystemId(majorVersion,minorVersion);

        AttributesHolder atts = new AttributesHolder();
        atts.addAttribute(new QualifiedName("", "", "profile"),
            profile);

        atts.addAttribute(new QualifiedName("", "", "version"),
            majorVersion + "." + minorVersion);

        // Add user-specified attributes
        if (x3dAtts != null) {
            Iterator<Map.Entry<String,String>> itr = x3dAtts.entrySet().iterator();
            while(itr.hasNext()) {
                Map.Entry<String,String> entry = itr.next();
                atts.addAttribute(new QualifiedName("", "", entry.getKey()),
                    entry.getValue());
            }
        }

        try {
            handler.startElement("", "X3D", "X3D", atts);
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print the component decl
     *
     * @param comps The component list
     */
    @Override
    public void printComponents(ComponentInfo[] comps) {
        int len = comps.length;

        try {
            handler.startElement("","head","head", new AttributesHolder());
            printNewLine();


            for(int i=0; i < len; i++) {
                AttributesHolder atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "", "name"),
                    comps[i].getName());
                atts.addAttribute(new QualifiedName("", "", "level"),
                    Integer.toString(comps[i].getLevel()));

                handler.startElement("", "component", "component", atts);
                handler.endElement("","component","component");
                printNewLine();
            }

        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print the MetaData.
     *
     * @param meta The scene Metadata map
     */
    @Override
    public void printMetaData(Map<String, String> meta) {
        Map.Entry[] entries;

        entries = new Map.Entry[meta.size()];
        meta.entrySet().toArray(entries);

        int len = entries.length;

        indentUp();

        try {
            for(int i=0; i < len; i++) {
                printIndent();

                AttributesHolder atts = new AttributesHolder();

                atts.addAttribute(new QualifiedName("", "", "name"),
                    (String)entries[i].getKey());
                atts.addAttribute(new QualifiedName("", "", "key"),
                    (String)entries[i].getValue());

                // Changed from key which was incorrect, should of been content.  Leaving old value in for now.
                atts.addAttribute(new QualifiedName("", "", "content"),
                    (String)entries[i].getValue());

                handler.startElement("", "meta", "meta", atts);
                handler.endElement("","meta","meta");
            }

            handler.endElement("","head","head");
            printNewLine();

            handler.startElement("", "Scene", "Scene", new AttributesHolder());
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print a ROUTE statement.
     *
     * @param route The ROUTE to print
     * @param defMap The DEF map
     */
    @Override
    public void printROUTE(ROUTE route, Map<VRMLNode, String> defMap) {
        VRMLNode source = route.getSourceNode();
        VRMLNode dest = route.getDestinationNode();
        VRMLFieldDeclaration sourceDecl;
        VRMLFieldDeclaration destDecl;
        String sourceDEF;
        String destDEF;

        if (dest instanceof ImportNodeProxy) {
            destDEF = ((ImportNodeProxy)dest).getImportedName();
        } else {
            destDEF = defMap.get(dest);
        }

        if (source instanceof ImportNodeProxy) {
            sourceDEF = ((ImportNodeProxy)source).getImportedName();
        } else {
            sourceDEF = defMap.get(source);
        }

        sourceDecl = source.getFieldDeclaration(route.getSourceIndex());
        destDecl = dest.getFieldDeclaration(route.getDestinationIndex());

        AttributesHolder atts = new AttributesHolder();

        atts.addAttribute(new QualifiedName("", "", "fromNode"),
            sourceDEF);
        atts.addAttribute(new QualifiedName("", "", "fromField"),
            sourceDecl.getName());
        atts.addAttribute(new QualifiedName("", "", "toNode"),
            destDEF);
        atts.addAttribute(new QualifiedName("", "", "toField"),
            destDecl.getName());

        try {
            printIndent();
            handler.startElement("","ROUTE","ROUTE", atts);
            handler.endElement("","ROUTE","ROUTE");
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print Exports.
     *
     * @param exports A map of exports(name,AS).
     */
    @Override
    public void printExports(Map<String, String> exports) {

        @SuppressWarnings("unchecked") // generic array type
        Map.Entry<String, String>[] entries = new Map.Entry[exports.size()];
        exports.entrySet().toArray(entries);

        String name;
        String as;

        try {
            for (Map.Entry<String, String> entrie : entries) {
                name = entrie.getValue();
                as = entrie.getKey();
                AttributesHolder atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "", "localDEF"),
                        name);
                if (as != null && !name.equals(as)) {
                    atts.addAttribute(new QualifiedName("", "","AS"),
                            as);
                }
                printIndent();
                handler.startElement("","EXPORT","EXPORT",atts);
                handler.endElement("","EXPORT","EXPORT");
                printNewLine();
            }
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print Imports.
     *
     * @param imports A map of imports(exported, String[] {def, as}.
     */
    @Override
    public void printImports(Map<String, VRMLNode> imports) {
        Map.Entry[] entries;

        entries = new Map.Entry[imports.size()];
        imports.entrySet().toArray(entries);

        String exported;
        Object obj;
        String[] defas;
        ImportNodeProxy proxy;

        try {
            for (Map.Entry entrie : entries) {
                exported = (String) entrie.getKey();
                obj = entrie.getValue();
                AttributesHolder atts = new AttributesHolder();
                if (obj instanceof String[]) {
                    defas = (String[]) entrie.getValue();
                    atts.addAttribute(new QualifiedName("", "","inlineDEF"),
                            defas[0]);
                    atts.addAttribute(new QualifiedName("", "","exportedDEF"),
                            defas[1]);
                    atts.addAttribute(new QualifiedName("", "","AS"),
                            exported);
                    printIndent();
                    handler.startElement("","IMPORT","IMPORT",atts);
                    handler.endElement("","IMPORT","IMPORT");
                    printNewLine();
                } else {
                    proxy = (ImportNodeProxy) obj;

                    atts.addAttribute(new QualifiedName("", "","inlineDEF"),
                            proxy.getInlineDEFName());

                    atts.addAttribute(new QualifiedName("", "","exportedDEF"),
                            proxy.getExportedName());

                    atts.addAttribute(new QualifiedName("", "","AS"),
                            proxy.getImportedName());

                    printIndent();
                    handler.startElement("","IMPORT","IMPORT",atts);
                    handler.endElement("","IMPORT","IMPORT");
                    printNewLine();
                }
            }
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print a node and its children.
     *
     * @param source The root node
     * @param ignoreFirst Should we ignore the first node.  Used for WorldRoot and ProtoBody
     */
    public void traverse(VRMLNode source, boolean ignoreFirst) {

        if(source == null)
            return;

        if (ignoreFirst)
            recurseSimpleSceneGraphChild((VRMLNodeType)source, false, false);
        else
            processSimpleNode(null, -1, (VRMLNodeType)source, null);
    }

    /**
     * Process a single simple node with its callback
     */
    private void processSimpleNode(VRMLNodeType parent,
                                   int field,
                                   VRMLNodeType kid,
                                   String expectedField) {

        boolean use = usedNodes.contains(kid);
        String containerField = null;
        String nodeName = kid.getVRMLNodeName();

        if (convertOldContent) {
            String newName = oldProtos.get(nodeName);
            if (newName != null) {
                nodeName = newName;
            }
        }

        if(!use)
            usedNodes.add(kid);

        indentUp();

        // check container field
        if (expectedField != null) {

            String cfield = (String) containerFields.get(nodeName);

            if (cfield == null || !cfield.equals(expectedField)) {
                containerField = expectedField;
            }
        }

        if (use) {
            String defName = currentDefMap.get(kid);

            if (defName == null) {
                printDefMap(currentDefMap);
            }

            try {
                printIndent();

                AttributesHolder atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "","USE"),
                    defName);

                if (containerField != null) {
                    atts.addAttribute(new QualifiedName("", "","containerField"),
                        containerField);
                }

                handler.startElement("",nodeName,nodeName,atts);
                handler.endElement("",nodeName,nodeName);

                printNewLine();
            } catch(SAXException se) {
                handleSAXException(se);
            }

            indentDown();
        } else {
            boolean compressed = false;

            if (useNC) {
                String node_name = kid.getVRMLNodeName();
                if (node_name.equals("IndexedFaceSet") ||
                    node_name.equals("TriangleSet") ||
                    node_name.equals("IndexedTriangleSet"))
                {
                    ignoreData = true;
                    compressed = true;
                }
            }

            boolean isProtoInstance = printStartNode(kid, use, currentDefMap,
                currentIsMap, containerField, compressed);

            // now recurse
            boolean hadChildren = recurseSimpleSceneGraphChild(kid, isProtoInstance, compressed);

            //indentDown();

            try {
                printEndNode(kid, hadChildren, compressed, currentIsMap);
            } catch(SAXException se) {
                handleSAXException(se);
            }

            if (compressed) {
                ignoreData = false;
                currentCompressor = null;
            }
        }
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     * @param isProtoInstance Is the parent a protoInstance
     * @param latchMetadata Should metadata be ignored
     * @return Were there any children
     */
    private boolean recurseSimpleSceneGraphChild(VRMLNodeType parent,
        boolean isProtoInstance, boolean ignoreMetadata) {

        int[] fields = parent.getNodeFieldIndices();

        if(fields == null || fields.length == 0)
            return false;

        VRMLFieldData value;
        boolean printField;
        boolean printFieldDecl = false;
        boolean printValue = false;
        int accessType;
        boolean hasChildren = false;
        int fieldMetadata = parent.getFieldIndex("metadata");

        printField = isProtoInstance;

        String fieldName;

        for(int i = 0; i < fields.length; i++) {
            if (parent instanceof AbstractDynamicFieldNode) {
                printFieldDecl = true;
                printField = true;
            }

            if (isProtoInstance)
                printField = true;

            printValue = false;
            fieldName = null;

            if (printField && i == fieldMetadata) {
                printField = false;
            }

            // This doubles up IS, seems like it should be needed
/*
            String is = findIS(parent, fields[i], currentIsMap);

            if (is != null) {
                VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                p.print(indentString);
                p.print(decl.getName());
                p.print(" IS ");
                p.println(is);
                continue;
            }
*/
            try {
                value = parent.getFieldValue(fields[i]);
            } catch(InvalidFieldException ife) {
                // Ignore
                continue;
            }

            if (printFieldDecl && i == fieldMetadata) {
                if (value.nodeValue == null) {
                    // ignore empty metadata for script/proto
                    continue;
                }

                printFieldDecl = false;
            }

            if(value.dataType == VRMLFieldData.NODE_ARRAY_DATA) {
                if (value.nodeArrayValues != null && value.nodeArrayValues.length > 0)
                    hasChildren = true;
                else if (isProtoInstance) {
                    // Don't print empty node decls
                    continue;
                }

                if (printField) {
                    VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                    printIndent();

                    try {
                        if (printFieldDecl) {
                            accessType = decl.getAccessType();
                            if (accessType == FieldConstants.FIELD ||
                                accessType == FieldConstants.EXPOSEDFIELD) {

                                printValue = true;
                            }

                            printDeclNoValue(decl, !(value.numElements == 0));

                            if (value.numElements == 0) {
                                printIndent();
                                handler.endElement("","field","field");
                                printNewLine();
                            }
                        } else {
                            printIndent();

                            AttributesHolder atts = new AttributesHolder();
                                atts.addAttribute(new QualifiedName("", "", "name"),
                                    decl.getName());

                            handler.startElement("","fieldValue","fieldValue", atts);

                            if (value.numElements == 0) {
                                printIndent();
                                handler.endElement("","fieldValue","fieldValue");
                                printNewLine();
                            } else {
                                printNewLine();
                            }
                        }
                    } catch(SAXException se) {
                        handleSAXException(se);
                    }
                } else {
                    // Check for containerfield
                    VRMLFieldDeclaration decl =
                        parent.getFieldDeclaration(fields[i]);

                    fieldName = decl.getName();
                }

                for(int j = 0; j < value.numElements; j++) {
                    if(value.nodeArrayValues[j] == null)
                        continue;

                    if (convertOldContent) {
                        if (value.nodeArrayValues[j].getVRMLNodeName().equals("GeoOrigin") &&
                            !parent.getFieldDeclaration(fields[i]).getName().equals("geoOrigin")) {

                            continue;
                        }
                    }

                    processSimpleNode(parent,
                                      fields[i], value.nodeArrayValues[j],
                                      fieldName);
                }

                if (printField) {
                    try {
                        if (isProtoInstance) {
                            if (value.numElements != 0) {
                                printIndent();
                                handler.endElement("","fieldValue","fieldValue");
                                printNewLine();
                            }
                        } else {
                            if (value.numElements != 0) {
                                printIndent();
                                handler.endElement("","field","field");
                                printNewLine();
                            }
                        }
                    } catch(SAXException se) {
                        handleSAXException(se);
                    }
                }
            } else {
                if (fields[i] == fieldMetadata && ignoreMetadata) {
                    continue;
                }

                if (value.nodeValue != null)
                    hasChildren = true;

                if (value.nodeValue == null && !printFieldDecl)
                    continue;

                if (printField) {
                    VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                    printIndent();

                    try {
                        if (printFieldDecl) {
                            accessType = decl.getAccessType();
                            if (accessType == FieldConstants.FIELD ||
                                accessType == FieldConstants.EXPOSEDFIELD) {

                                printValue = true;
                            }

                            printDeclNoValue(decl, !(value.nodeValue == null));

                            if (value.nodeValue == null) {
                                printIndent();
                                handler.endElement("","field","field");
                                printNewLine();
                                continue;
                            }
                        } else {
                            AttributesHolder atts = new AttributesHolder();
                            atts.addAttribute(new QualifiedName("", "",
                                "name"), decl.getName());

                            handler.startElement("","fieldValue","fieldValue", atts);
                            printNewLine();
                        }
                    } catch(SAXException se) {
                        handleSAXException(se);
                    }
                } else {
                    // Check for containerfield
                    VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                    fieldName = decl.getName();
                }

                processSimpleNode(parent,
                                  fields[i],
                                  (VRMLNodeType)value.nodeValue,
                                  fieldName);
                if (printField) {
                    try {
                        if (isProtoInstance) {
                            printIndent();
                            handler.endElement("","fieldValue","fieldValue");
                            printNewLine();
                        } else {
                            printIndent();
                            handler.endElement("","field","field");
                            printNewLine();
                        }
                    } catch(SAXException se) {
                        handleSAXException(se);
                    }

                }
            }
        }

        return hasChildren;
    }

    /**
     * Print the start of a node, and all its non node fields.
     *
     * @param node The node to print
     * @param atts The current attributes if applicable
     * @param use Is it a USE
     * @param defMap The current mapping of nodes to DEF names
     * @param isMap The current mapping of fields to IS names
     */
    public void printStartScriptNode(AbstractDynamicFieldNode node,
        AttributesHolder atts, boolean use, Map<VRMLNode, String> defMap, Map<Integer, List<ProtoFieldInfo>> isMap) {

        List<VRMLFieldDeclaration> fields = node.getAllFields();

        Iterator<VRMLFieldDeclaration> itr = fields.iterator();

        int field_must_evaluate = node.getFieldIndex("mustEvaluate");
        int field_direct_output = node.getFieldIndex("directOutput");
        int field_url = node.getFieldIndex("url");
        int field_metadata = node.getFieldIndex("metadata");

        // Handle all attributes first
        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = itr.next();

            if(decl == null)
                continue;

            VRMLFieldData data;
            int idx = node.getFieldIndex(decl.getName());

            try {
                data = node.getFieldValue(idx);
            } catch(InvalidFieldException e) {
                StringBuilder buf = new StringBuilder("Can't get field: ");
                buf.append(decl.getName());
                buf.append(" for: ");
                buf.append(node);
                buf.append(" named: ");
                buf.append(node.getVRMLNodeName());
                buf.append("\nIndex: ");
                buf.append(field_url);

                errorReporter.errorReport(buf.toString(), null);
                continue;
            }

            if (idx == field_metadata) {
                // handled elsewhere

            } else if (idx == field_must_evaluate) {
                if (data.booleanValue) {
                    printFieldValue(node, atts, data, decl, false, true);
                }

            } else if (idx == field_direct_output) {
                if (data.booleanValue == true) {
                    printFieldValue(node, atts, data, decl, false, true);
                }

            } else if (idx == field_url) {
                // handled in printEndNode

            }
        }

        try {
            handler.startElement("","Script","Script",atts);
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }

        itr = fields.iterator();

        // Handle non-node <field> values
        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = itr.next();

            if(decl == null)
                continue;

            VRMLFieldData data;
            int idx = node.getFieldIndex(decl.getName());

            try {
                data = node.getFieldValue(idx);
            } catch(InvalidFieldException e) {
                StringBuilder buf = new StringBuilder("Can't get field: ");
                buf.append(decl.getName());
                buf.append(" for: ");
                buf.append(node);
                buf.append(" named: ");
                buf.append(node.getVRMLNodeName());
                buf.append("\nIndex: ");
                buf.append(field_url);

                errorReporter.errorReport(buf.toString(), null);
                continue;
            }

            if (idx == field_metadata) {
                // handled elsewhere
                continue;
            } else if (idx == field_must_evaluate) {
                // already handled
                continue;
            } else if (idx == field_direct_output) {
                // already handled
                continue;
            } else if (idx == field_url) {
                // handled in printEndNode
                continue;
            }

            printScriptFieldDecl(node,decl,idx,data,defMap, currentIsMap);
        }

        indentDown();
    }

    /**
     * Print the start of a node, and all its non node fields.
     *
     * @param node The node to print
     * @param use Is it a USE
     * @param defMap The current mapping of nodes to DEF names
     * @param isMap The current mapping of fields to IS names
     * @param containerField The container field value, null if not needed
     * @param compressed Is this the start of a sg compression
     * @return Did a protoInstance start at this level
     */
    public boolean printStartNode(VRMLNodeType node, boolean use, Map<VRMLNode, String> defMap,
        Map<Integer, List<ProtoFieldInfo>> isMap, String containerField, boolean compressed) {

        String defName = defMap.get(node);
        String name = node.getVRMLNodeName();
        boolean hasChildren = false;
        boolean isProtoInstance = false;

        if (node instanceof VRMLProtoInstance) {
            if (convertOldContent) {
                String newName = oldProtos.get(name);
                if (newName != null) {
                    // don't make these protoInstances

                    name = newName;
                } else {
                    isProtoInstance = true;
                }
            } else {
                isProtoInstance = true;
            }
        }

        printIndent();

        AttributesHolder atts = new AttributesHolder();
        String elementName;

        if (isProtoInstance) {
            atts.addAttribute(new QualifiedName("", "",
                "name"), name);

            elementName = "ProtoInstance";
        } else {
            elementName = name;
        }

        if (defName != null) {
            atts.addAttribute(new QualifiedName("", "",
                "DEF"), defName);
        }

        if (containerField != null) {
            atts.addAttribute(new QualifiedName("", "",
                "containerField"), containerField);
        }

        if (isProtoInstance) {
            try {
                handler.startElement("",elementName,elementName, atts);
                printNewLine();

                atts = new AttributesHolder();
            } catch(SAXException se) {
                handleSAXException(se);
            }
        }

        indentUp();

        if (node instanceof AbstractDynamicFieldNode) {
            printStartScriptNode((AbstractDynamicFieldNode)node, atts,
                use, defMap, isMap);

            return false;
        }

        defaultNode = (VRMLNodeType) defaultNodes.get(name);
        if (defaultNode == null && !(node instanceof CRProtoInstance) &&
           !(node instanceof ProtoInstancePlaceHolder)) {
            defaultNode = (VRMLNodeType) nodeFactory.createVRMLNode(name, false);

            if (defaultNode == null) {
                errorReporter.errorReport("Could not create node: " + name, null);
            }
            defaultNodes.put(name,defaultNode);
        }

        int len = node.getNumFields();
        boolean upgradeInline = false;
        boolean removeWorldUrl = false;
        Set<Integer> urlFields = null;
        String worldUrl = null;

        if (node instanceof VRMLExternalNodeType) {
            removeWorldUrl = true;
            worldUrl = ((VRMLExternalNodeType)node).getWorldUrl();

            urlFields = new HashSet<>();

            if (node instanceof VRMLSingleExternalNodeType) {
                urlFields.add(node.getFieldIndex("url"));
            } else {
                int[] indexes = ((VRMLMultiExternalNodeType)node).getUrlFieldIndexes();
                for(int i=0; i < indexes.length; i++) {
                    urlFields.add(indexes[i]);
                }
            }
        }

        if (upgrading) {
            if (name.equals("Inline")) {
                upgradeInline = true;
            }
        }

        // Create a fields list to approximate a getAllFields for everyone
        List<VRMLFieldDeclaration> fields = new ArrayList<>();

        if (node instanceof AbstractProto) {
            List<VRMLFieldDeclaration> pfields = ((VRMLNodeTemplate)node).getAllFields();
            Iterator<VRMLFieldDeclaration> itr = pfields.iterator();
            while(itr.hasNext()) {
                fields.add(itr.next());
            }
        } else {
            for(int i = 0; i < len; i++) {
                VRMLFieldDeclaration decl = node.getFieldDeclaration(i);
                fields.add(decl);
            }
        }

        Iterator<VRMLFieldDeclaration> itr = fields.iterator();
        List<String[]> isList = new ArrayList<>();
        int idx;
        int didx = 0;
        String fieldName;

        // Find IS relationships to pre-print
        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = itr.next();
            VRMLFieldDeclaration defaultDecl = null;

            if(decl == null)
                continue;

            fieldName = decl.getName();

            idx = node.getFieldIndex(fieldName);

            String is = findIS(node, idx, isMap);

            if (is != null) {
                isList.add(new String[] {decl.getName(), is});

            }
        }

        itr = fields.iterator();

        if (compressed) {
            hasChildren = true;

//            currentCompressor = new TestCompressor();
        }

        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = itr.next();
            VRMLFieldDeclaration defaultDecl = null;

            if(decl == null)
                continue;

            fieldName = decl.getName();

            idx = node.getFieldIndex(fieldName);
            if (defaultNode != null) {
                didx = defaultNode.getFieldIndex(fieldName);
                defaultDecl = defaultNode.getFieldDeclaration(didx);
            }

            String is = findIS(node, idx, isMap);

            if (is != null) {
                continue;
            }

            if (ignoreData) {
                boolean handled = false;

                if (currentCompressor != null)
                    handled = currentCompressor.handleData(name, fieldName);

                if (handled)
                    continue;
            }

            int access = decl.getAccessType();
            if (access == FieldConstants.EVENTIN || access == FieldConstants.EVENTOUT)
                continue;

            VRMLFieldData data;
            try {
                data = node.getFieldValue(idx);

            } catch(InvalidFieldException e) {
                // Don't print exceptions here.  There is a difference in spec setup
                // between the default node and real one.  Not sure how best to deal with this.
                // The auto upgrade logic needs 3.0 nodes

                //System.out.println("Can't get field: " + decl.getName() + " for: " + node + " named: " + node.getVRMLNodeName());
                //System.out.println("Index: " + idx);
                continue;
            }

            // Ignore Node types here, they are handled later
            if (data.dataType == VRMLFieldData.NODE_DATA) {
                if (data.nodeValue != null)
                    hasChildren = true;

                continue;
            } else if (data.dataType == VRMLFieldData.NODE_ARRAY_DATA) {
                if (data.nodeArrayValues != null && data.nodeArrayValues.length > 0) {
                    hasChildren = true;
                }

                continue;
            }

            if (defaultNode != null && isDefault(node, decl, didx, data, defaultNode)) {
                continue;
            } else if (node instanceof CRProtoInstance) {
                CRProtoInstance inst = (CRProtoInstance) node;

                if (inst.isDefaultValue(idx))
                    continue;
            }

            if (removeWorldUrl && (urlFields.contains(idx))) {
                String[] url = data.stringArrayValues;

                if (url != null && worldUrl != null) {
                    for(int j=0; j < url.length; j++) {
                        if (url[j] != null && url[j].startsWith(worldUrl)) {
                            url[j] = url[j].substring(worldUrl.length());
                        }
                    }

                    data.stringArrayValues = url;
                }
            }

            if (upgradeInline) {
                if (decl.getName().equals("url")) {
                    String[] url = data.stringArrayValues;

                    if (url != null) {
                        for(int j=0; j < url.length; j++) {
                            int pos = url[j].indexOf(".wrl");
                            if (pos >= 0) {
                                url[j] = url[j].substring(0, pos);
                                url[j] = url[j] + encodingTo;
                            }
                        }

                        data.stringArrayValues = url;
                    }
                }
            }

            printFieldValue(node, atts, data, decl, isProtoInstance, !isProtoInstance);
        }

        if (compressed && compressedAttWay) {
            atts.addAttribute(new QualifiedName("", "",
                "encoder"), "1");

            int[] data = currentCompressor.compress(node);

            atts.addAttributeWithAlgorithmData(
                new QualifiedName("", "", "", "data"),
                null,
                EncodingAlgorithmIndexes.INT,
                data);
        }

        if (hasChildren && !isProtoInstance) {
            try {
                handler.startElement("",elementName,elementName, atts);
            } catch(SAXException se) {
                handleSAXException(se);
            }

            printNewLine();
            printISList(isList);
        } else {
            if (!isProtoInstance) {
                try {
                    if (isList.size() > 0) {
                        handler.startElement("",elementName,elementName, atts);

                        printNewLine();
                        printISList(isList);
                        printIndent();

                        handler.endElement("",elementName,elementName);
                        printNewLine();
                    } else {
                        handler.startElement("",elementName,elementName, atts);
                        handler.endElement("",elementName,elementName);
                        printNewLine();
                    }
                } catch(SAXException se) {
                    handleSAXException(se);
                }
            } else {
                printISList(isList);
            }
        }

        if (compressed && !compressedAttWay) {
            try {
                printIndent();

                atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "",
                    "name"), ".x3db");

                handler.startElement("","MetadataSet","MetadataSet", atts);
                printNewLine();

                indentUp();

                int midx = node.getFieldIndex("metadata");
                VRMLFieldData fdata = node.getFieldValue(midx);

                // Print original metadata
                if (fdata.nodeValue != null) {
                    ignoreData = false;

                    processSimpleNode(null,
                                      -1,
                                      (VRMLNodeType)fdata.nodeValue,
                                      "metadata");
                    ignoreData = true;
                }

                // Print Encoding
                printIndent();

                atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "",
                    "encoding"), "1");

                handler.startElement("","MetadataInteger","MetadataInteger",atts);
                handler.endElement("","MetadataInteger","MetadataInteger");
                printNewLine();

                // Print data
                int[] data = currentCompressor.compress(node);

                printIndent();

                atts = new AttributesHolder();
                atts.addAttribute(new QualifiedName("", "",
                    "name"), "payload");

                atts.addAttributeWithAlgorithmData(
                    new QualifiedName("", "", "", "value"),
                    null,
                    EncodingAlgorithmIndexes.INT,
                    data);

                handler.startElement("","MetadataInteger","MetadataInteger", atts);
                handler.endElement("","MetadataInteger","MetadataInteger");
                printNewLine();


                String emd = currentCompressor.getEncoderMetadata();

                if (emd != null) {
                    // TODO: Need to fix this
                    errorReporter.warningReport(
                        "***XML String version of encoded Metadata not supported",
                        null);

                    atts = new AttributesHolder();
                    atts.addAttribute(new QualifiedName("", "",
                        "name"), "Encoder Metadata");

                    handler.startElement("","MetadataSet","MetadataSet", atts);
                        // Instert metadata here
                        //p.println(emd);
                    handler.endElement("","MetadataSet","MetadataSet");
                }

                indentDown();

                printIndent();
                handler.endElement("","MetadataSet","MetadataSet");
            } catch(SAXException se) {
                handleSAXException(se);
            }
        }

        indentDown();

        return isProtoInstance;
    }

    /**
     * Print the end of a node.
     *
     * @param node The node
     * @param hadChildren Where there any children nodes
     * @param compressed Is this node compressed
     * @param isMap The current isMap
     * @throws org.xml.sax.SAXException
     */
    public void printEndNode(VRMLNodeType node, boolean hadChildren, boolean compressed, Map<Integer, List<ProtoFieldInfo>> isMap)
        throws SAXException {
        String nodeName = node.getVRMLNodeName();
        boolean isProtoInstance = false;
/*
        // TODO: Hack
        if (compressed)
            nodeName = "TriangleStripSet";
*/
        if (node instanceof VRMLProtoInstance) {
            if (convertOldContent) {
                String newName = oldProtos.get(nodeName);
                if (newName != null) {
                    // don't make these protoInstances

                    nodeName = newName;
                } else {
                    isProtoInstance = true;
                }
            } else {
                isProtoInstance = true;
            }
        }

        if (node instanceof AbstractDynamicFieldNode) {

            // Print IS relationships

            List<VRMLFieldDeclaration> fields = ((AbstractDynamicFieldNode)node).getAllFields();

            Iterator<VRMLFieldDeclaration> itr = fields.iterator();

            List<String[]> isList = new ArrayList<>();
            while(itr.hasNext()) {
                VRMLFieldDeclaration decl = itr.next();

                if(decl == null)
                    continue;

                int idx = node.getFieldIndex(decl.getName());

                String is = findIS(node, idx, isMap);

                if (is == null) {
                    continue;
                }

                if (is != null) {
                    isList.add(new String[] {decl.getName(), is});
                }
            }

            printISList(isList);

            indentUp();

            int field_url = node.getFieldIndex("url");

            VRMLFieldDeclaration decl = node.getFieldDeclaration(field_url);

            VRMLFieldData data = null;
            try {
                data = node.getFieldValue(field_url);

            } catch(InvalidFieldException e) {
                StringBuilder buf = new StringBuilder("Can't get field: ");
                buf.append(decl.getName());
                buf.append(" for: ");
                buf.append(node);
                buf.append(" named: ");
                buf.append(node.getVRMLNodeName());
                buf.append("\nIndex: ");
                buf.append(field_url);

                errorReporter.errorReport(buf.toString(), null);
            }

            if (upgrading) {
                String[] urls = new String[data.stringArrayValues.length];

                boolean foundProtocol = false;

                int len = urls.length;
                int len2;

                for(int i=0; i < len; i++) {
                    urls[i] = data.stringArrayValues[i];

                    if (!foundProtocol && (urls[i].startsWith("javascript:") ||
                        urls[i].startsWith("vrmlscript:"))) {

                        urls[i] = "ecmascript:" + urls[i].substring(11);
                        foundProtocol = true;
                    }

                    len2 = scriptPatterns.length;

                    for(int j=0; j < len2; j++) {
                        urls[i] = scriptPatterns[j].matcher(urls[i]).replaceAll(scriptReplacements[j]);
                    }
                }

                data.stringArrayValues = urls;
            }

            printIndent();

            char[] c;

            if (!binary) {
                c = "<![CDATA[".toCharArray();
                handler.characters(c, 0,c.length);
            }

            indentUp();


            for(int i=0; i < data.stringArrayValues.length; i++) {
                if (i != 0)
                    printIndent();

                //p.print(indentString);
                handler.characters(data.stringArrayValues[i].toCharArray(), 0,data.stringArrayValues[i].length());
                //p.println(data.stringArrayValues[i]);
            }

            indentDown();

            if (!binary) {
                c = "]]>".toCharArray();
                handler.characters(c, 0,c.length);
            }
            printNewLine();

            printIndent();
            handler.endElement("",nodeName,nodeName);
            printNewLine();

            indentDown();
            return;
        }

        //indentUp();  // TODO: Not sure why
        if (isProtoInstance) {
            printIndent();

            handler.endElement("","ProtoInstance","ProtoInstance");
            printNewLine();
        } else if (hadChildren) {
            printIndent();
            handler.endElement("",nodeName,nodeName);
            printNewLine();
        } else {
        }

        indentDown();
    }

    //-------------------------------------------------------------------------
    // SceneGraphTraverserSimpleObserver methods
    //-------------------------------------------------------------------------

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    @Override
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {

        if (child instanceof ProtoInstancePlaceHolder) {
            Object def = ((ProtoInstancePlaceHolder)child).getProtoDefinition();

            if (!(def instanceof CRExternPrototypeDecl))
                protoDeclSet.add((PrototypeDecl)def);
        }
    }

    /**
     * Print a proto declaration.
     *
     * @param proto The decl to print
     */
    @Override
    public void printPrototypeDecl(PrototypeDecl proto) {
        currentDefMap = new HashMap<>();
        Map<VRMLNode, String> saveMap = currentDefMap;
        reverseMap(proto.getDEFMap(), currentDefMap);

        currentIsMap = proto.getISMaps();
        currentPrototypeDecl = proto;

        VRMLGroupingNodeType body = proto.getBodyGroup();
        VRMLNodeType[] children = body.getChildren();

        String name = proto.getVRMLNodeName();

        // Create an instance for default removal
        VRMLNode n = protoCreator.newInstance(proto,
                                              root,
                                              majorVersion,
                                              minorVersion,
                                              false);

        defaultNodes.put(proto.getVRMLNodeName(), n);

        if (convertOldContent && oldProtos.get(name) != null)
            return;

        try {
            printIndent();

            AttributesHolder atts = new AttributesHolder();
            atts.addAttribute(new QualifiedName("", "", "name"),
                name);

            handler.startElement("","ProtoDeclare","ProtoDeclare", atts);
            printNewLine();

            indentUp();
            printIndent();

            atts = new AttributesHolder();

            handler.startElement("","ProtoInterface","ProtoInterface", atts);
            printNewLine();

            indentUp();
            // Print Proto Interface
            List<VRMLFieldDeclaration> fields = proto.getAllFields();
            Iterator<VRMLFieldDeclaration> itr = fields.iterator();
            int idx;
            boolean valReq;

            while(itr.hasNext()) {
                VRMLFieldDeclaration decl = itr.next();

                idx = proto.getFieldIndex(decl.getName());
                VRMLFieldData val = null;
                int access = decl.getAccessType();

                if (access != FieldConstants.EVENTIN && access != FieldConstants.EVENTOUT) {
                    val = proto.getFieldValue(idx);
                    valReq = true;
                } else {
                    valReq = false;
                }

                if (decl.getName().equals("metadata")) {
                    if (val == null || val.nodeValue == null)
                    continue;
                }

                printProtoFieldDecl(decl,idx,val,currentDefMap,currentIsMap, valReq);
            }

            indentDown();

            printIndent();

            handler.endElement("","ProtoInterface","ProtoInterface");
            printNewLine();

            printIndent();
            atts = new AttributesHolder();

            handler.startElement("","ProtoBody","ProtoBody", atts);
            printNewLine();

            // Find all nested proto's
            protoDeclSet.clear();

            for (VRMLNodeType children1 : children) {
                traverser.reset();
                traverser.traverseGraph(children1);
            }

            PrototypeDecl[] pList = new PrototypeDecl[protoDeclSet.size()];
            protoDeclSet.toArray(pList);

            for (PrototypeDecl pList1 : pList) {
                printPrototypeDecl(pList1);
            }

            // Restore after printing subs, do we need a stack?
            currentDefMap = saveMap;
            currentIsMap = proto.getISMaps();
            currentPrototypeDecl = proto;

            for (VRMLNodeType children1 : children) {
                traverse(children1, false);
            }

            printImports(proto.getImportDecls());

            Set<ProtoROUTE> routeSet = proto.getRouteDecls();
            Iterator<ProtoROUTE> ritr = routeSet.iterator();

            while(ritr.hasNext()) {
                printROUTE(ritr.next(), currentDefMap);
            }

            printIndent();
            handler.endElement("","ProtoBody","ProtoBody");
            printNewLine();

            indentDown();

            printIndent();

            handler.endElement("","ProtoDeclare","ProtoDeclare");
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print an external proto declaration.
     *
     * @param proto The decl to print
     */
    public void printExternalPrototypeDecl(CRExternPrototypeDecl proto) {
        currentDefMap = new HashMap<>();

        String name = proto.getVRMLNodeName();

        if (convertOldContent && oldProtos.get(name) != null)
            return;

        printIndent();

        AttributesHolder atts = new AttributesHolder();
        atts.addAttribute(new QualifiedName("", "", "name"),
            name);

        String[] url = epToUrl.get(proto.getVRMLNodeName());

        // The url was saved in epToUrl
        // Might use getUrl, but it has worldURL baked in

        int len = url.length;
        StringBuilder urlAtt = new StringBuilder();

        for(int i=0; i < len; i++) {
            urlAtt.append("\"");

            if (upgrading) {
                int pos = url[i].indexOf(".wrl");
                int locpos = url[i].indexOf("#");

                if (pos >= 0) {

                    String original = url[i];
                    url[i] = url[i].substring(0, pos);

                    url[i] = url[i] + encodingTo;

                    if (locpos > 0) {
                        String target = original.substring(locpos);
                        url[i] = url[i] + target;
                    }
                }
            }

            urlAtt.append(url[i]);
            urlAtt.append("\"");

            if (i != len - 1) {
                urlAtt.append("\n");
            }
        }

        atts.addAttribute(new QualifiedName("", "", "url"),
            urlAtt.toString());

        try {
            handler.startElement("","ExternProtoDeclare","ExternProtoDeclare", atts);

            indentUp();
            // Print Proto Interface
            List<VRMLFieldDeclaration> fields = proto.getAllFields();
            Iterator<VRMLFieldDeclaration> itr = fields.iterator();
            int idx;

            while(itr.hasNext()) {
                VRMLFieldDeclaration decl = itr.next();
                idx = proto.getFieldIndex(decl.getName());
                VRMLFieldData val = null;

                // Ignore metadata for EP
                if (decl.getName().equals("metadata"))
                    continue;

                printProtoFieldDecl(decl,idx,null,currentDefMap,currentIsMap, false);
            }

            indentDown();
            printIndent();

            handler.endElement("","ExternProtoDeclare","ExternProtoDeclare");
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Determine if a field is a MF* or SF*.
     *
     * @return true if a MF*
     */
    private boolean isMFField(VRMLFieldDeclaration decl) {
        int ft = decl.getFieldType();

        // All MF* are even currently.  Change to a switch if that
        // changes.
        return ft % 2 == 0;
    }

    /**
     * Increment the indent level.  This updates the identString.
     */
    private void indentUp() {
        indent++;

        if (stripWhitespace)
            return;

        indentString = indentMap.get(indent);

        if (indentString == null) {
            StringBuilder buff = new StringBuilder(indent * INDENT_STRING.length());

            for(int i=0; i < indent; i++) {
                buff.append(INDENT_STRING);
            }

            indentString = buff.toString();
            indentMap.put(indent, indentString);
        }
    }

    /**
     * Decrement the indent level.  This updates the identString.
     */
    private void indentDown() {
        indent--;

        if (stripWhitespace)
            return;

        indentString = indentMap.get(indent);

        if (indentString == null) {
            StringBuilder buff = new StringBuilder(indent * INDENT_STRING.length());

            for(int i=0; i < indent; i++) {
                buff.append(INDENT_STRING);
            }

            indentString = buff.toString();
        }
    }

    /**
     * Print the decl for a proto field.
     *
     * @param decl The field declaration
     * @param idx The field index
     * @param val The field value
     * @param defMap The current DEF map
     * @param isMap The current IS map
     * @param valRequired Do we have to print something for the val
     */
    private void printProtoFieldDecl(VRMLFieldDeclaration decl, int idx,
        VRMLFieldData val, Map<VRMLNode, String> defMap, Map<Integer, List<ProtoFieldInfo>> isMap, boolean valRequired) {

        printIndent();

        int access = decl.getAccessType();
        String accessType = "Unknown";

        switch(access) {
            case FieldConstants.FIELD:
                accessType = "initializeOnly";
                break;
            case FieldConstants.EXPOSEDFIELD:
                accessType = "inputOutput";
                break;
            case FieldConstants.EVENTIN:
                accessType = "inputOnly";
                break;
            case FieldConstants.EVENTOUT:
                accessType = "outputOnly";
                break;
            default:
                errorReporter.warningReport("Unknown field type in X3DClassicExporter: " +
                                      access, null);
        }

        AttributesHolder atts = new AttributesHolder();
        atts.addAttribute(new QualifiedName("", "", "accessType"),
            accessType);

        atts.addAttribute(new QualifiedName("", "", "type"),
            decl.getFieldTypeString());

        atts.addAttribute(new QualifiedName("", "", "name"),
            decl.getName());

        // Print the field value

        VRMLNode node;

        try {
            if (val != null) {
                switch(val.dataType) {
                    case VRMLFieldData.NODE_DATA:
                        node = val.nodeValue;
                        if (node == null) {
                            handler.startElement("","field","field",atts);
                            handler.endElement("","field","field");
                            printNewLine();
                        } else {
                            handler.startElement("","field","field",atts);
                            printNewLine();
                            traverse(node, false);
                            printIndent();
                            handler.endElement("","field","field");
                            printNewLine();
                        }
                        break;
                    case VRMLFieldData.NODE_ARRAY_DATA:
                        handler.startElement("","field","field",atts);

                        VRMLNode[] nodes = val.nodeArrayValues;
                        int len = nodes.length;

                        if (len > 0)
                            printNewLine();

                        indentUp();
                        for(int i=0; i < len; i++) {
                            printIndent();
                            traverse(nodes[i], false);
                        }

                        indentDown();
                        printIndent();

                        handler.endElement("","field","field");
                        printNewLine();
                        break;
                    default:
                        printFieldValue(null, atts, val, decl, false, false);
                        handler.startElement("","field","field",atts);
                        handler.endElement("","field","field");
                        printNewLine();
                }
            } else {
                if (valRequired) {
                    printFieldValue(null, atts, val, decl, false, false);
                }

                handler.startElement("","field","field",atts);
                handler.endElement("","field","field");
                printNewLine();
            }
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }


    /**
     * Print a field decl with the value part.
     *
     * @param node The node
     * @param decl The decl
     * @param val The value
     */
    private void printDeclWithValue(VRMLNodeType node, VRMLFieldDeclaration decl, VRMLFieldData val) {
        int access = decl.getAccessType();
        String accessType = "Unknown";

        switch(access) {
            case FieldConstants.FIELD:
                accessType = "initializeOnly";
                break;
            case FieldConstants.EXPOSEDFIELD:
                accessType = "inputOutput";
                break;
            case FieldConstants.EVENTIN:
                accessType = "inputOnly";
                break;
            case FieldConstants.EVENTOUT:
                accessType = "outputOnly";
                break;
            default:
                errorReporter.warningReport("Unknown field type in X3DClassicExporter: " +
                                      access, null);
        }

        AttributesHolder atts = new AttributesHolder();
        atts.addAttribute(new QualifiedName("", "", "accessType"),
            accessType);

        atts.addAttribute(new QualifiedName("", "", "type"),
            decl.getFieldTypeString());

        atts.addAttribute(new QualifiedName("", "", "name"),
            decl.getName());

        // Print the field value

        VRMLNode n;

        if (val != null) {
            switch(val.dataType) {
                case VRMLFieldData.NODE_DATA:
                    // ignore
                    break;
                case VRMLFieldData.NODE_ARRAY_DATA:
                    // ignore
                    break;
                default:
                    printFieldValue(node, atts, val, decl, false, false);
            }
        }

        try {
            handler.startElement("","field","field",atts);

        } catch(SAXException se) {
            handleSAXException(se);
        }

        try {
            printIndent();
            handler.endElement("","field","field");
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print a field decl without the value part.
     *
     * @param decl The decl
     * @param issueNewLine Should we print a newline after this
     */
    private void printDeclNoValue(VRMLFieldDeclaration decl, boolean issueNewLine) {
        int access = decl.getAccessType();
        String accessType = "Unknown";

        switch(access) {
            case FieldConstants.FIELD:
                accessType = "initializeOnly";
                break;
            case FieldConstants.EXPOSEDFIELD:
                accessType = "inputOutput";
                break;
            case FieldConstants.EVENTIN:
                accessType = "inputOnly";
                break;
            case FieldConstants.EVENTOUT:
                accessType = "outputOnly";
                break;
            default:
                errorReporter.warningReport("Unknown field type in X3DClassicExporter: " +
                                      access, null);
        }

        AttributesHolder atts = new AttributesHolder();
        atts.addAttribute(new QualifiedName("", "", "accessType"),
            accessType);

        atts.addAttribute(new QualifiedName("", "", "type"),
            decl.getFieldTypeString());

        atts.addAttribute(new QualifiedName("", "", "name"),
            decl.getName());

        try {
            handler.startElement("","field","field",atts);

            if (issueNewLine)
                printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print a script field decl including value.
     *
     * @param node The script node
     * @param decl The field decl
     * @param idx The field index
     * @param val The field value
     * @param defMap The current DEF map
     * @param isMap The current IS map
     */
    private void printScriptFieldDecl(VRMLNodeType node,
        VRMLFieldDeclaration decl, int idx, VRMLFieldData val, Map<VRMLNode, String> defMap,
        Map<Integer, List<ProtoFieldInfo>> isMap) {

        String is = findIS(node, idx, isMap);

        if (val != null && (
            val.dataType == VRMLFieldData.NODE_DATA ||
            val.dataType == VRMLFieldData.NODE_ARRAY_DATA)) {
            // Don't print decl yet
            return;
        }

        printIndent();

        int access = decl.getAccessType();

        if (access == FieldConstants.EVENTIN ||
            access == FieldConstants.EVENTOUT) {

            printDeclNoValue(decl, !(access == FieldConstants.EVENTIN ||
                access == FieldConstants.EVENTOUT));

            try {
                printIndent();
                handler.endElement("","field","field");
                printNewLine();
            } catch(SAXException se) {
                handleSAXException(se);
            }
        } else {
            printDeclWithValue(node, decl, val);
        }
    }

    /**
     * Is this field a default value.
     *
     * @param node The node
     * @param decl The field decl
     * @param i The field index
     * @param data The value
     * @param defaultNode The default node to compare to
     *
     * @return true if its a default
     */
    private boolean isDefault(VRMLNodeType node, VRMLFieldDeclaration decl, int i,
        VRMLFieldData data, VRMLNodeType defaultNode) {

        VRMLFieldData defaultData;

        try {
            defaultData = defaultNode.getFieldValue(i);
        } catch(InvalidFieldException e) {
            StringBuilder buf = new StringBuilder("Can't get field: ");
            buf.append(decl.getName());
            buf.append(" for: ");
            buf.append(node);
            buf.append(" named: ");
            buf.append(node.getVRMLNodeName());
            buf.append("\nIndex: ");
            buf.append(i);

            errorReporter.errorReport(buf.toString(), e);
            return false;
        }

        if (defaultData == null) {
            return false;
        }

        // No value means a proto instance without a value registered
        if (data == null)
            return true;

        boolean same = true;
        int len2;

        switch(data.dataType) {
            case VRMLFieldData.BOOLEAN_DATA:
                if (defaultData.booleanValue != data.booleanValue)
                    same = false;
                break;
            case VRMLFieldData.INT_DATA:
                if (defaultData.intValue != data.intValue)
                    same = false;
                break;
            case VRMLFieldData.LONG_DATA:
                if (defaultData.longValue != data.longValue)
                    same = false;
                break;
            case VRMLFieldData.FLOAT_DATA:
                if (defaultData.floatValue != data.floatValue)
                    same = false;
                break;
            case VRMLFieldData.DOUBLE_DATA:
                if (defaultData.doubleValue != data.doubleValue)
                    same = false;
                break;
            case VRMLFieldData.STRING_DATA:
                if (defaultData.stringValue == null) {
                    return false;
                }

                if (!defaultData.stringValue.equals(data.stringValue))
                    same = false;
                break;
            case VRMLFieldData.NODE_DATA:
                // ignore
                break;

            case VRMLFieldData.BOOLEAN_ARRAY_DATA:
                if (defaultData.booleanArrayValues == null &&
                    data.booleanArrayValues == null) {

                    break;
                }

                if (defaultData.booleanArrayValues == null ||
                    data.booleanArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.booleanArrayValues.length != data.booleanArrayValues.length) {
                    same = false;
                    break;
                }
                len2 = defaultData.booleanArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.booleanArrayValues[j] != data.booleanArrayValues[j]) {
                        same = false;
                        break;
                    }
                }
                break;

            case VRMLFieldData.INT_ARRAY_DATA:
                if (defaultData.intArrayValues == null &&
                    data.intArrayValues == null) {

                    break;
                }

                if (defaultData.intArrayValues == null ||
                    data.intArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.intArrayValues.length != data.intArrayValues.length) {
                    same = false;
                    break;
                }
                len2 = defaultData.intArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.intArrayValues[j] != data.intArrayValues[j]) {
                        same = false;
                        break;
                    }
                }

                break;

            case VRMLFieldData.LONG_ARRAY_DATA:
                if (defaultData.longArrayValues == null &&
                    data.longArrayValues == null) {

                    break;
                }

                if (defaultData.longArrayValues == null ||
                    data.longArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.longArrayValues.length != data.longArrayValues.length) {
                    same = false;
                    break;
                }
                len2 = defaultData.longArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.longArrayValues[j] != data.longArrayValues[j]) {
                        same = false;
                        break;
                    }
                }

                break;

            case VRMLFieldData.FLOAT_ARRAY_DATA:
                if (defaultData.floatArrayValues == null &&
                    data.floatArrayValues == null) {

                    break;
                }

                if (defaultData.floatArrayValues == null ||
                    data.floatArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.floatArrayValues.length != data.floatArrayValues.length) {
                    same = false;
                    break;
                }
                len2 = defaultData.floatArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.floatArrayValues[j] != data.floatArrayValues[j]) {
                        same = false;
                        break;
                    }
                }
                break;

            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                if (defaultData.doubleArrayValues == null &&
                    data.doubleArrayValues == null) {

                    break;
                }

                if (defaultData.doubleArrayValues == null ||
                    data.doubleArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.doubleArrayValues.length != data.doubleArrayValues.length) {
                   same = false;
                   break;
                }

                len2 = defaultData.doubleArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.doubleArrayValues[j] != data.doubleArrayValues[j]) {
                        same = false;
                        break;
                    }
                }

                break;

            case VRMLFieldData.NODE_ARRAY_DATA:
                //ignore
                break;

            case VRMLFieldData.STRING_ARRAY_DATA:
                if (defaultData.stringArrayValues == null &&
                    data.stringArrayValues == null) {

                    break;
                }

                if (defaultData.stringArrayValues == null ||
                    data.stringArrayValues == null) {

                    same = false;
                    break;
                }

                if (defaultData.stringArrayValues.length != data.stringArrayValues.length) {
                    same = false;
                    break;
                }
                len2 = defaultData.stringArrayValues.length;

                for(int j=0; j < len2; j++) {
                    if (defaultData.stringArrayValues[j] == null && data.stringArrayValues[j] == null)
                        continue;

                    if (defaultData.stringArrayValues[j] == null ||
                        data.stringArrayValues[j] == null) {

                        same = false;
                        break;
                    }

                    if (!defaultData.stringArrayValues[j].equals(data.stringArrayValues[j])) {
                        same = false;
                        break;
                    }
                }
                break;
        }

        return same;
    }

    /**
     * Print a field value.  Ignores Node fields.
     *
     * @param node The current node
     * @param atts The atts for the current Node, null if not applicable
     * @param data The data to print
     * @param decl The field declaration
     * @param fieldWrap Should the field be wrapped in a <fieldValue> tag
     * @param fieldName true if the attribute is the field name, false its value.
     */
    private void printFieldValue(VRMLNodeType node, AttributesHolder atts,
        VRMLFieldData data, VRMLFieldDeclaration decl,
        boolean fieldWrap, boolean fieldName) {

        int len2;

        String attName;

        if (data == null) {
//            int ftype = decl.getFieldType();
//
//            if (ftype == FieldConstants.SFSTRING) {
//                attName = "value";
//            }

            return;
        }

        if (convertOldContent && node != null) {
            String fieldKey = node.getVRMLNodeName() + "." + decl.getName();
            Integer newFieldType = fieldRemap.get(fieldKey);

            if (newFieldType != null) {
                int newType = newFieldType;

                convertFieldData(newType, data, decl);
            }
        }

        if (fieldWrap) {
            atts.addAttribute(new QualifiedName("", "",
                "name"), decl.getName());
        }

        if (data.dataType == VRMLFieldData.NODE_ARRAY_DATA ||
            data.dataType == VRMLFieldData.NODE_DATA) {

System.out.println("*** pfv, Not sure if this case is handled");
            return;
        }

        if (fieldWrap) {
            attName = "value";
        } else {
            if (fieldName) {
                attName = decl.getName();
            } else {
                attName = "value";
            }
        }

        String strValue;

        switch(data.dataType) {
            // TODO: Any savings by making SF* binary?
            case VRMLFieldData.BOOLEAN_DATA:

                if (data.booleanValue)
                    strValue = "true";
                else
                    strValue = "false";

                atts.addAttribute(new QualifiedName("", "",
                    attName), strValue);
                break;

            case VRMLFieldData.INT_DATA:
                strValue = Integer.toString(data.intValue);

                atts.addAttribute(new QualifiedName("", "",
                    attName), strValue);
                break;

            case VRMLFieldData.LONG_DATA:
                strValue = Long.toString(data.longValue);

                atts.addAttribute(new QualifiedName("", "",
                    attName), strValue);
                break;

            case VRMLFieldData.FLOAT_DATA:
                strValue = Float.toString(data.floatValue);

                atts.addAttribute(new QualifiedName("", "",
                    attName), strValue);
                break;

            case VRMLFieldData.DOUBLE_DATA:
                strValue = Double.toString(data.doubleValue);

                atts.addAttribute(new QualifiedName("", "",
                    attName), strValue);
                break;

            case VRMLFieldData.STRING_DATA:
                if (data.stringValue != null) {
                    if (binary && false) {
                        atts.addAttribute(new QualifiedName("", "",
                            attName), data.stringValue);
                    } else {
                        atts.addAttribute(new QualifiedName("", "",
                            attName), XMLTools.XML.escape(data.stringValue));
                    }
                }
                break;

            case VRMLFieldData.BOOLEAN_ARRAY_DATA:
                if (data.booleanArrayValues == null)
                    break;

                atts.addAttributeWithAlgorithmData(new QualifiedName("", "", attName), null,
                        EncodingAlgorithmIndexes.BOOLEAN, data.booleanArrayValues);
                break;

            case VRMLFieldData.INT_ARRAY_DATA:
                if (data.intArrayValues == null)
                    break;

                if (data.numElements != data.intArrayValues.length) {
                    // TODO: Not sure of the best way to handle
                    errorReporter.warningReport("Unhandled resizing in exporter, int", null);
                }

                if (compressionMethod == METHOD_STRINGS) {
                    strValue = createX3DString(decl, data.intArrayValues, data.numElements);

                    atts.addAttribute(new QualifiedName("", "",
                        attName), strValue);
                } else {
//System.out.println("RetainedSaxExporter " + attName + " " +  java.util.Arrays.toString(data.intArrayValues));
                    atts.addAttributeWithAlgorithmData(new QualifiedName("", "", attName),
                        DeltazlibIntArrayAlgorithm.ALGORITHM_URI,
                        DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID,
                        data.intArrayValues);
                }

                break;

            case VRMLFieldData.LONG_ARRAY_DATA:
                if (data.longArrayValues == null)
                    break;

                if (data.numElements != data.longArrayValues.length) {
                    // TODO: Not sure of the best way to handle
                    errorReporter.warningReport("Unhandled resizing in exporter, long", null);
                }

                atts.addAttributeWithAlgorithmData(new QualifiedName("", "", attName),
                    null,
                    EncodingAlgorithmIndexes.LONG,
                    data.longArrayValues);

                break;

            case VRMLFieldData.FLOAT_ARRAY_DATA:
                if (data.floatArrayValues == null)
                    break;

                encodeFloatArray(data.floatArrayValues, data.numElements, decl, attName, atts, FieldConstants.MFFLOAT);

                break;

            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                if (data.doubleArrayValues == null)
                    break;

                encodeDoubleArray(data.doubleArrayValues, data.numElements, decl, attName, atts, FieldConstants.MFFLOAT);
                break;

            case VRMLFieldData.STRING_ARRAY_DATA:
                if (data.stringArrayValues == null)
                    break;

                StringBuilder sbuff = new StringBuilder();

                len2 = data.numElements;
                for(int j=0; j < len2; j++) {
                    if (j == len2 -1) {
                        sbuff.append("\"");
                        if (binary && false) {
System.out.println("Not escaping2: " + data.stringArrayValues[j]);

                            sbuff.append(data.stringArrayValues[j]);
                        } else
                            sbuff.append(XMLTools.XML.escape(data.stringArrayValues[j]));

                        sbuff.append("\"");
                    } else {
                        sbuff.append("\"");
                        if (binary && false) {
System.out.println("Not escaping3: " + data.stringArrayValues[j]);
                            sbuff.append(data.stringArrayValues[j]);
                        } else
                            sbuff.append(XMLTools.XML.escape(data.stringArrayValues[j]));

                        sbuff.append("\",");
                    }
                }

                atts.addAttribute(new QualifiedName("", "",
                    attName), sbuff.toString());

                break;
        }

        if (fieldWrap) {
            try {
                printIndent();
                handler.startElement("","fieldValue","fieldValue",atts);
                handler.endElement("","fieldValue","fieldValue");
                printNewLine();

                atts.clear();
            } catch(SAXException se) {
                handleSAXException(se);
            }
        }
    }

    /**
     * Print out an isList.
     *
     * @param isList List of String[], field, protoField
     */
    private void printISList(List<String[]> isList) {
        if (isList.isEmpty())
            return;

        try {

            Iterator<String[]> itr2 = isList.iterator();
            String[] isConnect;

            printIndent();

            AttributesHolder atts = new AttributesHolder();
            handler.startElement("","IS","IS", atts);
            printNewLine();

            indentUp();

            while(itr2.hasNext()) {
                isConnect = itr2.next();

                printIndent();

                atts = new AttributesHolder();

                atts.addAttribute(new QualifiedName("", "", "nodeField"),
                    isConnect[0]);

                atts.addAttribute(new QualifiedName("", "", "protoField"),
                    isConnect[1]);

                handler.startElement("","connect","connect", atts);
                handler.endElement("","connect", "connect");
                printNewLine();
            }
            indentDown();

            printIndent();
            handler.endElement("","IS","IS");
            printNewLine();
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Common handler for SAXException processing.
     *
     * @param se The SAXException
     */
    private void handleSAXException(SAXException se) {
        errorReporter.errorReport("SAXException: " + se.getMessage(), se);
    }

    /**
     * Print the current indent string.
     */
    private void printIndent() {
        if (stripWhitespace)
            return;

        try {
            handler.characters(indentString.toCharArray(),0,indentString.length());
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

    /**
     * Print the current indent string.
     */
    private void printNewLine() {
        if (stripWhitespace)
            return;

        try {
            handler.characters(newLineChar,0,1);
        } catch(SAXException se) {
            handleSAXException(se);
        }
    }

}
