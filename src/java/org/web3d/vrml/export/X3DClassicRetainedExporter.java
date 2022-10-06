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
import java.util.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.nodes.proto.*;

import org.j3d.util.IntHashMap;
import org.j3d.util.ErrorReporter;
import org.web3d.util.Version;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.common.nodes.AbstractDynamicFieldNode;

/**
 * X3D Classic exporter using a retained Scenegraph.
 * <p>
 *
 * Known Issues:
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.25 $
 */
public class X3DClassicRetainedExporter extends BaseRetainedExporter
    implements SceneGraphTraversalSimpleObserver {

    /** The indent String to replicate per level */
    private static String INDENT_STRING = "   ";

    /** The current indent level */
    private int indent;

    /** The current indent string */
    private String indentString;

    /** A mapping of indent to String */
    private IntHashMap<String> indentMap;

    /** Temporary map during traversal for use references */
    private Set<VRMLNodeType> usedNodes;

    /** The current set of proto definitions */
    private Set<VRMLNodeTemplate> protoDeclSet;

    /** Traverser for printing proto's */
    private SceneGraphTraverser traverser;

    /** The world root */
    private VRMLWorldRootNodeType root;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     */
    public X3DClassicRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter) {

        this(os, major, minor, errorReporter, -1);
    }

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     * @param sigDigits The number of significant digits to use in printing floats
     */
    public X3DClassicRetainedExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int sigDigits) {

        super(major, minor, errorReporter, sigDigits);

        p = new PrintWriter(os, true);
        init();
    }

    /**
     *  Common initialization routine.
     */
    private void init() {
        usedNodes = new HashSet<>();
        indentString = "";
        indentMap = new IntHashMap<>();
        protoDeclSet = new HashSet<>();

        traverser = new SceneGraphTraverser();
    }

    /**
     * Write a scene out.
     *
     * @param scene The scene to write
     */
    public void writeScene(VRMLScene scene) {
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
            } else {
                printPrototypeDecl((PrototypeDecl)proto);
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
        p.flush();
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
    }

    /**
     * Print the header.
     *
     * @param major The major version
     * @param minor The minor version
     */
    @Override
    public void printHeader(int major, int minor) {
        p.print("#X3D V");
        p.print(major);
        p.print(".");
        p.print(minor);
        p.println(" utf8");
        p.println();
    }

    @Override
    public void printExporterInfo() {
        p.print("# Exported by Xj3D's: ");
        p.print(getClass().getName() + " v" + Version.BUILD_MAJOR_VERSION + Version.PERIOD + Version.BUILD_MINOR_VERSION);
        p.println();
    }

    /**
     * Print the profile decl.
     *
     * @param profile The profile
     */
    @Override
    public void printProfile(String profile) {
        p.println();
        p.print("PROFILE ");
        p.println(profile);
        p.println();
    }

    /**
     * Print the component decl
     *
     * @param comps The component list
     */
    @Override
    public void printComponents(ComponentInfo[] comps) {
        int len = comps.length;

        for(int i=0; i < len; i++) {
            p.print("COMPONENT ");
            p.print(comps[i].getName());
            p.print(":");
            p.println(comps[i].getLevel());
        }

        if (len > 0)
            p.println();
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

        String key;
        String value;

        for(int i=0; i < len; i++) {
            key = (String) entries[i].getKey();
            value = (String) entries[i].getValue();

            key = key.replace("\\","\\\\");
            value = value.replace("\\","\\\\");

            // Make sure to also retain double quoted terms in this string
            value = value.replace("\"", "\\\"");

            p.println("META \"" + key + "\" \"" +  value + "\"");
        }

        if (len > 0)
            p.println();
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

        p.print(indentString);
        p.print("ROUTE ");
        p.print(sourceDEF);
        p.print(".");
        p.print(sourceDecl.getName());
        p.print(" TO ");
        p.print(destDEF);
        p.print(".");
        p.println(destDecl.getName());
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

        for (Map.Entry<String, String> entrie : entries) {
            name = entrie.getValue();
            as = entrie.getKey();
            p.print(indentString);
            p.print("EXPORT ");
            p.print(name);
            if (as != null && !name.equals(as)) {
                p.print(" AS ");
                p.println(as);
            } else {
                p.println();
            }
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

        for (Map.Entry entrie : entries) {
            exported = (String) entrie.getKey();
            obj = entrie.getValue();
            if (obj instanceof String[]) {
                defas = (String[]) entrie.getValue();
                p.print(indentString);
                p.print("IMPORT ");
                p.print(defas[0]);
                p.print(".");
                p.print(defas[1]);
                p.print(" AS ");
                p.println(exported);
            } else {
                proxy = (ImportNodeProxy) obj;

                p.print(indentString);
                p.print("IMPORT ");
                p.print(proxy.getInlineDEFName());
                p.print(".");
                p.print(proxy.getExportedName());
                p.print(" AS ");
                p.println(proxy.getImportedName());
            }
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
            recurseSimpleSceneGraphChild((VRMLNodeType)source);
        else
            processSimpleNode(null, -1, (VRMLNodeType)source);
    }

    /**
     * Process a single simple node with its callback
     */
    private void processSimpleNode(VRMLNodeType parent,
                                   int field,
                                   VRMLNodeType kid) {

        boolean use = usedNodes.contains(kid);

        if(!use)
            usedNodes.add(kid);

        indentUp();

        if (use) {
            String defName = currentDefMap.get(kid);

            if (defName == null) {
                errorReporter.warningReport("Can't find DEF for: " + kid, null);
                printDefMap(currentDefMap);
            }

            p.print("USE ");
            p.println(defName);

            indentDown();
        } else {
            printStartNode(kid, use, currentDefMap, currentIsMap);

            // now recurse
            recurseSimpleSceneGraphChild(kid);

            indentDown();
            printEndNode(kid);
        }
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     */
    private void recurseSimpleSceneGraphChild(VRMLNodeType parent) {

        int[] fields = parent.getNodeFieldIndices();

        if(fields == null || fields.length == 0)
            return;

        VRMLFieldData value;
        boolean printField;
        boolean printFieldDecl = false;
        boolean printValue;
        int accessType;
        int fieldMetadata = parent.getFieldIndex("metadata");

        if (parent instanceof AbstractDynamicFieldNode)
            printFieldDecl = true;

        // TODO: likely will fail when proto's are indented correctly
        printField = indent > 0;

        for(int i = 0; i < fields.length; i++) {
            printValue = false;

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
                if (printField) {
                    VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                    p.print(indentString);
                    if (printFieldDecl) {
                        accessType = decl.getAccessType();
                        if (accessType == FieldConstants.FIELD ||
                            accessType == FieldConstants.EXPOSEDFIELD) {

                            printValue = true;
                        }

                        printDeclNoValue(decl);
                        p.print(decl.getName());

                        if (printValue)
                            p.print(" [");
                    } else {
                        if (value.numElements != 0) {
                            p.print(decl.getName());
                            p.print(" [");
                        }
                    }
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
                                      fields[i], value.nodeArrayValues[j]);
                }

                if (printFieldDecl) {
                    if (printValue)
                        p.println("]");
                    else
                        p.println();
                } else if (printField) {
                    if (value.numElements != 0) {
                        p.print(indentString);
                        p.println("]");
                    } else {
                        p.println();
                    }
                }

            } else {
                if (value.nodeValue == null && !printFieldDecl)
                    continue;

                if (printField) {
                    VRMLFieldDeclaration decl = parent.getFieldDeclaration(fields[i]);
                    p.print(indentString);

                    if (printFieldDecl) {
                        printDeclNoValue(decl);

                        accessType = decl.getAccessType();
                        if (accessType == FieldConstants.FIELD ||
                            accessType == FieldConstants.EXPOSEDFIELD) {

                            printValue = true;
                        }

                    }

                    p.print(decl.getName());
                    p.print(" ");

                    if (value.nodeValue == null && printFieldDecl) {
                        if (printValue)
                            p.println("NULL");
                        else
                            p.println();
                        continue;
                    }
                }

                processSimpleNode(parent,
                                  fields[i],
                                  (VRMLNodeType)value.nodeValue);
                if (printField && !printFieldDecl)
                    p.println();
            }
        }
    }

    /**
     * Print the start of a node, and all its non node fields.
     *
     * @param node The node to print
     * @param use Is it a USE
     * @param defMap The current mapping of nodes to DEF names
     * @param isMap The current mapping of fields to IS names
     */
    public void printStartScriptNode(AbstractDynamicFieldNode node, boolean use, Map<VRMLNode, String> defMap, Map<Integer, List<ProtoFieldInfo>> isMap) {
        List<VRMLFieldDeclaration> fields = node.getAllFields();

        Iterator<VRMLFieldDeclaration> itr = fields.iterator();

        int field_must_evaluate = node.getFieldIndex("mustEvaluate");
        int field_direct_output = node.getFieldIndex("directOutput");
        int field_url = node.getFieldIndex("url");
        int field_metadata = node.getFieldIndex("metadata");

        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = itr.next();

            if(decl == null)
                continue;

            VRMLFieldData data;
            int idx = node.getFieldIndex(decl.getName());

            try {
                data = node.getFieldValue(idx);
            } catch(InvalidFieldException e) {
                errorReporter.errorReport("Can't get field: " + decl.getName() +
                                    " for: " + node, null);
                continue;
            }

            if (idx == field_metadata) {
                // handled elsewhere
                continue;
            } else if (idx == field_must_evaluate) {
                if (data.booleanValue) {
                    p.print(indentString);
                    p.print(decl.getName());
                    p.print(" ");

                    printFieldValue(node, data, decl);
                }
                continue;
            } else if (idx == field_direct_output) {
                if (data.booleanValue) {
                    p.print(indentString);
                    p.print(decl.getName());
                    p.print(" ");

                    printFieldValue(node, data, decl);
                }
                continue;
            } else if (idx == field_url) {
                // handled in printEndNode
                continue;
            }

            printScriptFieldDecl(node,decl,idx,data,defMap,currentIsMap);
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
     */
    public void printStartNode(VRMLNodeType node, boolean use, Map<VRMLNode, String> defMap, Map<Integer, List<ProtoFieldInfo>> isMap) {
        String defName = defMap.get(node);

        if (defName != null) {
            p.print("DEF ");
            p.print(defName);
            p.print(" ");
        }

        String name = node.getVRMLNodeName();

        if (convertOldContent) {
            String newName = oldProtos.get(name);
            if (newName != null) {
                name = newName;
            }
        }

        p.print(name);
        p.println(" {");
        indentUp();

        if (node instanceof AbstractDynamicFieldNode) {
            printStartScriptNode((AbstractDynamicFieldNode)node, use, defMap, isMap);
            return;
        }

        defaultNode = (VRMLNodeType) defaultNodes.get(name);
        if (defaultNode == null && !(node instanceof CRProtoInstance)) {
            defaultNode = (VRMLNodeType) nodeFactory.createVRMLNode(name, false);

            if (defaultNode == null) {
                errorReporter.errorReport("Could not create node: " + name, null);
            }

            defaultNodes.put(name,defaultNode);
        }

        int len = node.getNumFields();
        int len2;
        boolean upgradeInline = false;
        boolean removeWorldUrl = false;
        HashSet<Integer> urlFields = null;
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
        int idx;
        int didx = 0;
        String fieldName;

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
                p.print(indentString);
                p.print(decl.getName());
                p.print(" IS ");
                p.println(is);
                continue;
            }

            int access = decl.getAccessType();
            if (access == FieldConstants.EVENTIN || access == FieldConstants.EVENTOUT)
                continue;

            VRMLFieldData data;
            try {
                data = node.getFieldValue(idx);

            } catch(InvalidFieldException e) {
                //System.out.println("Can't get field: " + decl.getName() + " for: " + node + " named: " + node.getVRMLNodeName());
                //System.out.println("Index: " + idx);
                continue;
            }

            if (defaultNode != null && isDefault(node, decl, didx, data, defaultNode)) {
                continue;
            } else if (node instanceof CRProtoInstance) {
                CRProtoInstance inst = (CRProtoInstance) node;

                if (inst.isDefaultValue(idx))
                    continue;
            }

            // Ignore Node types here, they are handled later
            if (data.dataType == VRMLFieldData.NODE_DATA ||
                data.dataType == VRMLFieldData.NODE_ARRAY_DATA)
                    continue;

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
                                url[j] = url[j] + ".x3dv";
                            }
                        }

                        data.stringArrayValues = url;
                    }
                }
            }

            p.print(indentString);
            p.print(decl.getName());

            p.print(" ");

            printFieldValue(node, data, decl);
        }

        indentDown();
    }

    /**
     * Print the end of a node.
     *
     * @param node The node
     */
    public void printEndNode(VRMLNodeType node) {
        if (node instanceof AbstractDynamicFieldNode) {
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

            p.print(indentString);
            p.print(decl.getName());
            p.print(" ");

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
            printFieldValue(node, data, decl);

            indentDown();
        }

        p.print(indentString);
        p.println("}");
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
            protoDeclSet.add(((ProtoInstancePlaceHolder)child).getProtoDefinition());
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

        p.print(indentString);
        p.print("PROTO ");
        p.print(name);
        p.println(" [");

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
        p.print(indentString);
        p.println("] {");
        indentUp();

        // Find all nested proto's
        protoDeclSet.clear();
        for (VRMLNodeType children1 : children) {
            traverser.reset();
            traverser.traverseGraph(children1);
        }

        PrototypeDecl[] pList = new PrototypeDecl[protoDeclSet.size()];
        protoDeclSet.toArray((VRMLNodeTemplate[])pList);

        for (PrototypeDecl pList1 : pList) {
            printPrototypeDecl(pList1);
        }

        // Restore after printing subs, do we need a stack?
        currentDefMap = saveMap;
        currentIsMap = proto.getISMaps();
        currentPrototypeDecl = proto;

        for (VRMLNodeType children1 : children) {
            p.print(indentString);
            traverse(children1, false);
        }

        printImports(proto.getImportDecls());

        Set<ProtoROUTE> routeSet = proto.getRouteDecls();
        Iterator<ProtoROUTE> ritr = routeSet.iterator();

        while(ritr.hasNext()) {
            printROUTE(ritr.next(), currentDefMap);
        }

        indentDown();
        p.print(indentString);
        p.println("}");
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

        p.print(indentString);
        p.print("EXTERNPROTO ");
        p.print(name);
        p.println(" [");

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
        p.print(indentString);
        p.print("] ");

        // The url was saved in epToUrl
        // Might use getUrl, but it has worldURL baked in

        String[] url = epToUrl.get(proto.getVRMLNodeName());

        p.print("[");
        int len = url.length;

        for(int i=0; i < len; i++) {
            p.print("\"");

            if (upgrading) {
                int pos = url[i].indexOf(".wrl");
                int locpos = url[i].indexOf("#");

                if (pos >= 0) {
                    String original = url[i];
                    url[i] = url[i].substring(0, pos);

                    url[i] = url[i] + ".x3dv";

                    if (locpos > 0) {
                        String target = original.substring(locpos);

                        url[i] = url[i] + target;
                    }
                }
            }

            p.print(url[i]);
            p.print("\"");

            if (i != len - 1)
                p.println();
        }
        p.println("]\n");
        // No defaults for extern protos
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

        indentString = indentMap.get(indent);

        if (indentString == null) {
            StringBuilder buff = new StringBuilder(indent * INDENT_STRING.length());

            for(int i=0; i < indent; i++) {
                buff.append(INDENT_STRING);
            }

            indentString = buff.toString();
        }
    }

    private void printProtoFieldDecl(VRMLFieldDeclaration decl, int idx,
        VRMLFieldData val, Map<VRMLNode, String> defMap, Map<Integer, List<ProtoFieldInfo>> isMap, boolean valRequired) {

        p.print(indentString);
        int access = decl.getAccessType();
        switch(access) {
            case FieldConstants.FIELD:
                p.print("initializeOnly ");
                break;
            case FieldConstants.EXPOSEDFIELD:
                p.print("inputOutput ");
                break;
            case FieldConstants.EVENTIN:
                p.print("inputOnly ");
                break;
            case FieldConstants.EVENTOUT:
                p.print("outputOnly ");
                break;
            default:
                errorReporter.errorReport("Unknown field type in X3DClassicExporter: " +
                                    access, null);
        }

        p.print(decl.getFieldTypeString());
        p.print(" ");
        p.print(decl.getName());
        p.print(" ");

        // Print the field value

        VRMLNode node;

        if (val != null) {
            switch(val.dataType) {
                case VRMLFieldData.NODE_DATA:
                    node = val.nodeValue;
                    if (node == null)
                        p.println(" NULL");
                    else
                        traverse(node, false);

                    break;
                case VRMLFieldData.NODE_ARRAY_DATA:
                    p.println("[");
                    VRMLNode[] nodes = val.nodeArrayValues;
                    int len = nodes.length;

                    indentUp();
                    for(int i=0; i < len; i++) {
                        p.print(indentString);
                        traverse(nodes[i], false);
                    }

                    indentDown();
                    p.print(indentString);
                    p.println("]");
                    break;
                default:
                    // NULL node is ok as it will never need name conversion
                    printFieldValue(null, val, decl);
            }
        } else {
            if (valRequired) {
                // Null is ok as it will never need name conversion
                printFieldValue(null, val, decl);
            }

            p.println();
        }
    }

    /**
     * Print a field decl with the value part.
     */
    private void printDeclNoValue(VRMLFieldDeclaration decl) {
        int access = decl.getAccessType();
        switch(access) {
            case FieldConstants.FIELD:
                p.print("initializeOnly ");
                break;
            case FieldConstants.EXPOSEDFIELD:
                p.print("inputOutput ");
                break;
            case FieldConstants.EVENTIN:
                p.print("inputOnly ");
                break;
            case FieldConstants.EVENTOUT:
                p.print("outputOnly ");
                break;
            default:
                errorReporter.errorReport("Unknown field type in X3DClassicExporter: " +
                                          access, null);
        }

        p.print(decl.getFieldTypeString());
        p.print(" ");
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

        if (is == null && val != null && (
            val.dataType == VRMLFieldData.NODE_DATA ||
            val.dataType == VRMLFieldData.NODE_ARRAY_DATA)) {
            // Don't print decl yet
            return;
        }

        p.print(indentString);

        printDeclNoValue(decl);

        if (is != null) {
            p.print(decl.getName());
            p.print(" ");
            p.print("IS ");
            p.println(is);
            return;
        }

        int access = decl.getAccessType();
        if (access == FieldConstants.EVENTIN ||
            access == FieldConstants.EVENTOUT) {

            p.println(decl.getName());
            return;
        }

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
                    p.print(decl.getName());
                    p.print(" ");
                    printFieldValue(node, val, decl);
                    p.println();
            }
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
//                e.printStackTrace(System.err);
            StringBuilder buf = new StringBuilder("Can't get field: ");
            buf.append(decl.getName());
            buf.append(" for: ");
            buf.append(node);
            buf.append(" named: ");
            buf.append(node.getVRMLNodeName());
            buf.append("\nIndex: ");
            buf.append(i);

            errorReporter.errorReport(buf.toString(), null);

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
     * @param node The node.  Used for field remap, null is ok if not desired.
     * @param data The data to print
     * @param decl The field declaration
     */
    private void printFieldValue(VRMLNodeType node, VRMLFieldData data, VRMLFieldDeclaration decl) {
        int len2;
        boolean ismf;
        int span;
        int idx;

        if (data == null) {
            // All MF* are even currently.  Change to a switch if that
            // changes.

            int ftype = decl.getFieldType();

            if (ftype % 2 == 0) {
                p.print("[ ]");
            } else if (ftype == FieldConstants.SFNODE)
                p.print("NULL");
            else {
                // We have a SF without a value field.  Not sure exactly what do here.
                // I think its illegal except for SFString.  But it might mean default value.
                // Handle for SFString, leave a message for others
                if (ftype == FieldConstants.SFSTRING) {
                    p.print("\"\"");
                } else {
                    errorReporter.warningReport("Empty value field for: " + decl, null);
                }
            }

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

        switch(data.dataType) {
            case VRMLFieldData.NODE_ARRAY_DATA:
                //ignore, handled elsewhere
                break;

            case VRMLFieldData.NODE_DATA:
                // ignore, handled elsewhere
                break;

            case VRMLFieldData.BOOLEAN_DATA:
                if (data.booleanValue)
                    p.println("TRUE");
                else
                    p.println("FALSE");

                break;

            case VRMLFieldData.INT_DATA:
                p.println(data.intValue);
                break;

            case VRMLFieldData.LONG_DATA:
                p.println(data.longValue);
                break;

            case VRMLFieldData.FLOAT_DATA:
                p.println(data.floatValue);
                break;

            case VRMLFieldData.DOUBLE_DATA:
                p.println(data.doubleValue);
                break;

            case VRMLFieldData.STRING_DATA:
                if (data.stringValue == null) {
                    p.println("\"\"");
                } else {
                    p.print("\"");
                    p.print(data.stringValue);
                    p.println("\"");
                }
                break;

            case VRMLFieldData.BOOLEAN_ARRAY_DATA:
                if (data.booleanArrayValues == null)
                    break;

                p.print("[");
                len2 = data.numElements;
                for(int j=0; j < len2 - 1; j++) {
                    if(data.booleanArrayValues[j]) {
                        p.print("TRUE,");
                    } else {
                        p.print("FALSE,");
                    }
                }

                if(data.booleanArrayValues[len2 - 1]) {
                    p.print("TRUE");
                } else {
                    p.print("FALSE");
                }

                p.println("]");
                break;

            case VRMLFieldData.INT_ARRAY_DATA:
                if (data.intArrayValues == null)
                    break;

                p.print("[");
                len2 = data.numElements;
                for(int j=0; j < len2 - 1; j++) {
                    p.print(data.intArrayValues[j]);
                    p.print(" ");
                }

                p.print(data.intArrayValues[len2 - 1]);
                p.println("]");

                break;

            case VRMLFieldData.LONG_ARRAY_DATA:
                if (data.longArrayValues == null)
                    break;

                p.print("[");
                len2 = data.numElements;
                for(int j=0; j < len2 - 1; j++) {
                    p.print(data.longArrayValues[j]);
                    p.print(" ");
                }

                p.print(data.longArrayValues[len2 - 1]);
                p.println("]");
                break;

            case VRMLFieldData.FLOAT_ARRAY_DATA:
                if (data.floatArrayValues == null)
                    break;

                ismf = isMFField(decl);

                if (ismf) {
                    p.print("[");
                    len2 = data.numElements;

                    if (len2 > 0) {
                        span = data.floatArrayValues.length / len2;
                        idx = 0;
                        for(int j=0; j < len2; j++) {
                            for(int k=0; k < span; k++) {
                                p.print(data.floatArrayValues[idx++]);
                                if (k != span - 1)
                                    p.print(" ");
                            }
                            if (j != len2 -1) {
                                p.print(", ");
                            }
                        }
                    }
                    p.println("]");
                } else {
                    len2 = data.floatArrayValues.length;
                    for(int j=0; j < len2; j++) {
                        p.print(data.floatArrayValues[j]);
                        if (j != len2 -1 )
                            p.print(" ");
                    }
                    p.println();
                }
                break;

            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                if (data.doubleArrayValues == null)
                    break;

                ismf = isMFField(decl);

                if (ismf) {
                    p.print("[");
                    len2 = data.numElements;

                    if (len2 > 0) {
                        span = data.doubleArrayValues.length / len2;
                        idx = 0;
                        for(int j=0; j < len2; j++) {
                            for(int k=0; k < span; k++) {
                                p.print(data.doubleArrayValues[idx++]);
                                if (k != span - 1)
                                    p.print(" ");
                            }
                            if (j != len2 -1) {
                                p.print(", ");
                            }
                        }
                    }
                    p.println("]");
                } else {
                    len2 = data.doubleArrayValues.length;
                    for(int j=0; j < len2; j++) {
                        p.print(data.doubleArrayValues[j]);
                        if (j != len2 -1 )
                            p.print(" ");
                    }
                    p.println();
                }
                break;

            case VRMLFieldData.STRING_ARRAY_DATA:
                if (data.stringArrayValues == null)
                    break;

                p.print("[");

				len2 = data.numElements;
                if (len2 > 0) {
					for(int j=0; j < len2 - 1; j++) {
						p.print("\"");
						p.print(data.stringArrayValues[j]);
						p.print("\",");
					}
					p.print("\"");
					p.print(data.stringArrayValues[len2 - 1]);
					p.print("\"");
				}
                p.println("]");
                break;

            default:
                errorReporter.messageReport("Unhandled case in switch printFieldValue");
        }
    }

}
