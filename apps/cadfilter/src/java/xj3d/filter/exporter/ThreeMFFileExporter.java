/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.exporter;

// External imports
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import org.j3d.util.ErrorReporter;

import org.web3d.parser.DefaultFieldParserFactory;
import org.web3d.util.SimpleStack;
import org.web3d.vrml.export.Exporter;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.sav.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

// Local imports

/**
 * File exporter implementation that writes a 3mf file from an X3D stream
 * of events.  Supports an IndexedTriangleSet node.  Use Triangulation and Index filters as needed.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class ThreeMFFileExporter extends Exporter
    implements BinaryContentHandler {

    private static final String NAME = "ThreeMFFileExporter";

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 6;

    /** Reference to our Locator instance to hand to users */
    private Locator locator;

    /** The output writer */
    private OutputStreamWriter writer;

    /** Are we processing a document */
    private boolean processingDocument;

    /** Are we inside an IndexedFaceSet */
    private boolean insideITS;

    /** A stack of node names */
    private SimpleStack nodeStack;

    /** A stack of field names */
    private SimpleStack fieldStack;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    private NumberFormat numberFormater;

    private VRMLFieldReader fieldReader;

    private int objectCount;
    private ZipArchiveOutputStream zaos;

    /**
     * Create a new instance of this exporter
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param reporter The error reporter to use
     */
    public ThreeMFFileExporter(OutputStream os, int major, int minor,
                               ErrorReporter reporter) {

        super(major, minor, reporter);


        try {
            zaos = (ZipArchiveOutputStream) os;
            writer = new OutputStreamWriter(os, "UTF8");
            outputConstant();

            ZipArchiveEntry zentry = new ZipArchiveEntry("3D/3dmodel.model");
            zaos.putArchiveEntry(zentry);

        } catch(IOException ioe) {
            // Comes from FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE
            lastErrorCode = 5;
            ExporterException ee = new ExporterException(NAME,5,"Failed to write an 3mf value.  msg: " + ioe.getMessage());
            throw ee;
        }

        insideITS = false;
        processingDocument = false;
        objectCount = 0;

        nodeStack = new SimpleStack();
        fieldStack = new SimpleStack();
        fieldValuesStack = new SimpleStack();

        numberFormater = NumberFormat.getNumberInstance();
        numberFormater.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        numberFormater.setGroupingUsed(false);

        DefaultFieldParserFactory parserFactory = new DefaultFieldParserFactory();
        fieldReader = parserFactory.newFieldParser(major, minor);
    }

    /**
     * Output constant 3mf file format stuff
     */
    private void outputConstant() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("_rels/.rels");
        zaos.putArchiveEntry(entry);
        OutputStreamWriter w = new OutputStreamWriter(zaos,"UTF8");
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        w.write("\t<Relationship Target=\"/3D/3dmodel.model\" Id=\"rel0\" Type=\"http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel\" />");
        w.write("</Relationships>\n");
        w.flush();
        zaos.closeArchiveEntry();

        entry = new ZipArchiveEntry("[Content_Types].xml");
        zaos.putArchiveEntry(entry);
        w = new OutputStreamWriter(zaos,"UTF8");
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n");
        w.write("\t<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\" />\n");
        w.write("\t<Default Extension=\"model\" ContentType=\"application/vnd.ms-package.3dmanufacturing-3dmodel+xml\" />\n");
        w.write("\t<Default Extension=\"png\" ContentType=\"application/vnd.ms-package.3dmanufacturing-3dmodeltexture\" />\n");
        w.write("</Types>");
        w.flush();
        zaos.closeArchiveEntry();

        writeln("<model units=\"meter\" xml:lang=\"en-US\" xmlns=\"http://schemas.microsoft.com/3dmanufacturing/core/2015/02\" >");
        writeln("\t<resources>");
    }

    /**
     * Initialise the internals of the parser at start up. If you are not using
     * the detailed constructors, this needs to be called to ensure that all
     * internal state is correctly set up.
     */
    public void initialize() {
        // Ignored for this implementation.
    }

    //-----------------------------------------------------------------------
    // Methods for interface ContentHandler
    //-----------------------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <CODE>startDocument()</CODE> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    @Override
    public void setDocumentLocator(Locator loc) {
        locator = loc;
    }


    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The full VRML version string of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {

        if (processingDocument)
            return;

        processingDocument = true;
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * A component declaration has been found in the code. There may be zero
     * or more component declarations in the file, appearing just after the
     * profile declaration. The textual information after the COMPONENT keyword
     * is left unparsed and presented through this call. It is up to the user
     * application to parse the component information.
     *
     * @param componentName The name of the component to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void componentDecl(String componentName)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * A META declaration has been found in the code. There may be zero
     * or more meta declarations in the file, appearing just after the
     * component declaration. Each meta declaration has a key and value
     * strings. No information is to be implied from this. It is for extra
     * data only.
     *
     * @param key The value of the key string
     * @param value The value of the value string
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void metaDecl(String key, String value)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * An IMPORT declaration has been found in the document. All three
     * parameters will always be provided, regardless of whether the AS keyword
     * has been used or not. The parser implementation will automatically set
     * the local import name as needed.
     *
     * @param inline The name of the inline DEF nodes
     * @param exported The exported name from the inlined file
     * @param imported The local name to use for the exported name
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void importDecl(String inline, String exported, String imported)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * An EXPORT declaration has been found in the document. Both paramters
     * will always be provided regardless of whether the AS keyword has been
     * used. The parser implementation will automatically set the exported
     * name as needed.
     *
     * @param defName The DEF name of the nodes to be exported
     * @param exported The name to be exported as
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void exportDecl(String defName, String exported)
        throws SAVException, VRMLException {
        // ignore
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

        writeln("\t</resources>\n");
        writeln("\t<build>");
        for(int i=0; i < objectCount; i++) {
            write("\t\t<item objectid=\"");
            write("" + (i + 1));
            writeln("\" />");
        }
        writeln("\t</build>");
        writeln("</model>");
        // now close the writer
        try {
            if (writer != null) {
                writer.flush();
                //writer.close();     // not sure about this
                writer = null;
            }

            zaos.closeArchiveEntry();
        } catch(IOException ioe) {
            // Comes from FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE
            lastErrorCode = 5;
            ExporterException ee = new ExporterException(NAME,5,"Failed to write a 3mf Binary Value.  msg: " + ioe.getMessage());
            throw ee;
        }
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (name.equals("IndexedTriangleSet")) {

            insideITS = true;
            fieldValuesStack.push(new HashMap());

        }

        nodeStack.push(name);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void endNode() throws SAVException, VRMLException {

        String nodeName = (String) nodeStack.pop();

        Map<String, String> fieldValues;

        if (nodeName.equals("IndexedTriangleSet")) {

            write("\t\t<object id=\"");
            write(""+(objectCount+1));
            writeln("\" type=\"model\" >");

            writeln("\t\t\t<mesh>");
            insideITS = false;
            fieldValues = (Map<String, String>) fieldValuesStack.pop();

            // Get the geometry information

            int[] coordIndex = null;
            if (fieldValues.get("IndexedTriangleSet.index") != null) {
                coordIndex = fieldReader.MFInt32(fieldValues.get("IndexedTriangleSet.index"));
            }
            float[] coord = null;
            if (fieldValues.get("Coordinate.point") != null) {
                coord = fieldReader.MFVec3f(fieldValues.get("Coordinate.point"));
            }

            float[] normal;
            if (fieldValues.get("Normal.vector") != null) {
                normal = fieldReader.MFVec3f(fieldValues.get("Normal.vector"));
            }

            float[] texture;
            if (fieldValues.get("TextureCoordinate.point") != null) {
                texture = fieldReader.MFVec2f(fieldValues.get("TextureCoordinate.point"));
            }

            if (coord == null || coordIndex == null) {
                System.out.println("geometry information not found!");
                return;
            }

            int len = coord.length / 3;

            writeln("\t\t\t\t<vertices>");
            for(int i=0; i < len; i++) {
                write("\t\t\t\t\t<vertex x=\"");
                write(numberFormater.format(coord[i*3]));
                write("\" y=\"");
                write(numberFormater.format(coord[i*3+1]));
                write("\" z=\"");
                write(numberFormater.format(coord[i*3+2]));
                writeln("\" />");
            }

            writeln("\t\t\t\t</vertices>");
            /*
            if (texture != null) {
                len = texture.length / 2;

                for(int i=0; i < len; i++) {
                    writeln("vt " + numberFormater.format(texture[i*2]) + " " +
                            numberFormater.format(texture[i*2+1]));
                }
            }
            */

            int cidx;

            // sanity check before we begin writing
            if (coordIndex.length % 3 != 0) {
                System.out.println("coordIndex not a multiple of 3, " + len);
                return;
            }

            len = coordIndex.length / 3;

            // TODO: need to handle ccw
            if (true || (texture == null && normal == null)) {

                writeln("\t\t\t\t<triangles>");
                for(int i=0; i < len; i++) {
                    cidx = coordIndex[i];
                    if (cidx == -1)
                        continue;

                    write("\t\t\t\t\t<triangle v1=\"");
                    write(numberFormater.format(coordIndex[i * 3]));
                    write("\" v2=\"");
                    write(numberFormater.format(coordIndex[i*3+1]));
                    write("\" v3=\"");
                    write(numberFormater.format(coordIndex[i*3+2]));
                    writeln("\" />");
                }

                writeln("\t\t\t\t</triangles>");

            } else if (texture != null && normal == null) {
                // write the faces
                for(int i=0; i < len; i++) {
                    cidx = coordIndex[i];
                    if (cidx == -1)
                        continue;


                    // write vertices
                    writeln("f " +
                            numberFormater.format(coordIndex[i*3] + 1) + "/" + (coordIndex[i*3] + 1) +" " +
                            numberFormater.format(coordIndex[i*3+1] + 1) + "/" + (coordIndex[i*3+1] + 1) + " " +
                            numberFormater.format(coordIndex[i*3+2] + 1) + "/" + (coordIndex[i*3+2] + 1));

                }
            } else if (texture == null && normal != null) {
                // write the faces
                for(int i=0; i < len; i++) {
                    cidx = coordIndex[i];
                    if (cidx == -1)
                        continue;


                    // write vertices
                    writeln("f " +
                            numberFormater.format(coordIndex[i*3] + 1) + "//" + (coordIndex[i*3] + 1) +" " +
                            numberFormater.format(coordIndex[i*3+1] + 1) + "//" + (coordIndex[i*3+1] + 1) + " " +
                            numberFormater.format(coordIndex[i*3+2] + 1) + "//" + (coordIndex[i*3+2] + 1));

                }
            } else {
                // write the faces
                for(int i=0; i < len; i++) {
                    cidx = coordIndex[i];
                    if (cidx == -1)
                        continue;


                    // write vertices
                    writeln("f " +
                            numberFormater.format(coordIndex[i*3] + 1) + "/" + (coordIndex[i*3] + 1) + "/" + (coordIndex[i*3] + 1) + " " +
                            numberFormater.format(coordIndex[i*3+1] + 1) + "/" + (coordIndex[i*3+1] + 1) + "/" + (coordIndex[i*3+1] + 1) + " " +
                            numberFormater.format(coordIndex[i*3+2] + 1) + "/" + (coordIndex[i*3+2] + 1) + "/" + (coordIndex[i*3+2] + 1));

                }
            }

            writeln("\t\t\t</mesh>");
            writeln("\t\t</object>");
            objectCount++;
        }

    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {
        fieldStack.push(name);

//System.out.println("    Exporter.startField: " + name);

    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)},
     * {@link StringContentHandler#fieldValue(String)}. or any of the
     * fieldValue methods in {@link BinaryContentHandler}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endField() throws SAVException, VRMLException {
        String name = (String) fieldStack.pop();

//System.out.println("    Exporter.endField: " + name);

    }

    //-----------------------------------------------------------------------
    // Methods for interface StringContentHandler
    //-----------------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(String value) throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>)fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        }

    }


    /**
     * The value of an MFField where the underlying parser knows about how the
     * values are broken up. The parser is not required to support this
     * callback, but implementors of this interface should understand it. The
     * most likely time we will have this method called is for MFString or
     * URL lists. If called, it is guaranteed to split the strings along the
     * SF node type boundaries.
     *
     * @param values The list of string representing the values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(String[] values) throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        // flatten the array
        StringBuilder value  = new StringBuilder();

        if (values != null) {
            for (String value1 : values) {
                value.append(value1);
                value.append(" ");
            }
        }

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    //-----------------------------------------------------------------------
    //Methods for interface BinaryContentHandler
    //-----------------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new values to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, String.valueOf(value));
        }

    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param values The new values to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(int[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, numberFormater.format(value));
        }

    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param values The new values to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(float[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

/*
System.out.println("        Exporter.fieldValue(float[] values, int len)");
System.out.println("            nodeName: " + nodeName);
System.out.println("            fieldName: " + fieldName);
System.out.println("            insideITS: " + insideITS);
*/

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

//System.out.println("    value: " + value.toString().trim());

            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, numberFormater.format(value));
        }

    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param values The new values to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(long[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, numberFormater.format(value));
        }

    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param values The new values to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(double[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                value.append(values[i]);
                value.append(" ");
            }

            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new values to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fieldValue(String[] values, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideITS) {

            // flatten the array
            StringBuilder value  = new StringBuilder();
            for (String value1 : values) {
                value.append(value1);
                value.append(" ");
            }

            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value.toString().trim());
        }

    }

    //-----------------------------------------------------------------------
    // Methods for interface RouteHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNode The name of the DEF of the source node
     * @param srcField The name of the field to route values from
     * @param destNode The name of the DEF of the destination node
     * @param destField The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void routeDecl(String srcNode,
                          String srcField,
                          String destNode,
                          String destField)
        throws SAVException, VRMLException {
        // ignore
    }

    //-----------------------------------------------------------------------
    // Methods for interface ScriptHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of the start of a script declaration. All calls between
     * now and the corresponding {@link #endScriptDecl} call belong to this
     * script node. This method will be called <I>after</I> the ContentHandler
     * <CODE>startNode()</CODE> method call. All DEF information is contained
     * in that method call and this just signifies the start of script
     * processing so that we know to treat the field parsing callbacks a
     * little differently.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startScriptDecl() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endScriptDecl() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     * <p>
     * If the current parsing is in a proto and the field "value" is defined
     * with an IS statement then the value returned here is null. There will
     * be a subsequent call to the ProtoHandlers <CODE>protoIsDecl()</CODE>
     * method with the name of the field included.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException {
        // ignore
    }

    //-----------------------------------------------------------------------
    // Methods for interface ProtoHandler
    //-----------------------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startProtoDecl(String name) throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #startProtoBody()} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endProtoDecl() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of a proto's field declaration. This is used for both
     * external and ordinary protos. Externprotos don't allow the declaration
     * of a value for the field. In this case, the parameter value will be
     * null.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field as either String or
     *   String[]. Null if not allowed.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of a field value uses an IS statement.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void protoIsDecl(String fieldName) throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startProtoBody() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endProtoBody() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     * This is called just after the closing bracket of the declaration and
     * before the opening of the body statement. If the next thing called is
     * not a {@link #externProtoURI} Then that method should toss an
     * exception.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endExternProtoDecl() throws SAVException, VRMLException {
        // ignore
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list as an array of URI strings. The calling application is required to
     * interpret the incoming string. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
        // ignore
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Fetch the locator used by this parser. This is here so that the user of
     * this parser can ask for it and set it before calling startDocument().
     * Once the scene has started parsing in this class it is too late for the
     * locator to be set. This parser does set it internally when asked for a
     * but there may be other times when it is not set.
     *
     * @return The locator used for syntax errors
     */
    public Locator getDocumentLocator() {
        return locator;
    }

    /**
     * Set the error handler instance.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorHandler(ErrorHandler eh) {
        errorReporter = eh;

        if(eh != null)
            eh.setDocumentLocator(getDocumentLocator());
    }

    /**
     * Set the error reporter instance. If this is also an ErrorHandler
     * instance, the document locator will also be set.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh) {
        if(eh instanceof ErrorHandler)
            setErrorHandler((ErrorHandler)eh);
        else
            errorReporter = eh;
    }

    /**
     * Helper method to write a some characters of STL code but without
     * terminating the current line.
     *
     * @param chars The characters to write
     */
    private void write(String chars) {

        // write the line of STL code
        try {
            if (writer != null) {
                writer.write(chars);
            }
        } catch(IOException ioe) {
            // Comes from FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE
            lastErrorCode = 5;
            ExporterException ee = new ExporterException(NAME,5,"Failed to write an STL Binary Value.  msg: " + ioe.getMessage());
            throw ee;
        }

    }

    /**
     * Helper method to write a line of STL code
     *
     * @param line The line of code to write
     */
    private void writeln(String line) {

        // write the line of STL code
        try {
            if (writer != null) {
                writer.write(line);
                writer.write("\r\n");
            }
        } catch(IOException ioe) {
            // Comes from FilterExitCodes.CANNOT_WRITE_OUTPUT_FILE
            lastErrorCode = 5;
            ExporterException ee = new ExporterException(NAME,5,"Failed to write an STL Binary Value.  msg: " + ioe.getMessage());
            throw ee;
        }

    }
}
