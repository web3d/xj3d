/*****************************************************************************
 *                        Web3d.org Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import xj3d.filter.node.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.ScriptHandler;
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;


import xj3d.filter.AbstractFilter;

import org.web3d.vrml.renderer.common.nodes.GeometryHolder;
import org.web3d.vrml.renderer.common.nodes.GeometryUtils;

/**
 * Takes an IndexedTriangleSet and a creaseAngle parameter and outputs a geometry
 * that accounts for the creaseAngle.  Valid output with be either another
 * IndexedTriangleSet or a TriangleSet.  Preference will be given to an IndexedTriangleSet
 * for speed purposes.  All faces
 * who's geometric normal is less then the creaseAngle will be calculated so
 * that the faces are shaded smoothly across the edge; otherwise, normals shall
 * be calculated so that a lighting discontinuity across the edge is produced.
 *
 * Input normals are ignored.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class ITSCreaseAnglerFilter extends AbstractFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The creaseAngle argument option identifier */
    private static final String CREASE_ANGLE_ARG = "-creaseAngle";

    /** The logging identifier of this app */
    private static final String LOG_NAME = "ITSCreaseAnglerFilter";

    /** The default creaseAngle */
    private static final float DEFAULT_CREASE_ANGLE = 0f;

    /** Flag indicating that we are processing a node that requires translation */
    private boolean intercept;

    /** A stack of node wrappers */
    private SimpleStack encStack;

    /** Node wrapper factory */
    private EncodableFactory factory;

    /** The node that is being intercepted */
    private IndexedTriangleSet node;

    /** Map of def'ed nodes keyed by DEF Name */
    private HashMap<String, Encodable> defMap;

    /** The creaseAngle to calculate */
    private float creaseAngle;

    /** Are we in a proto currently? */
    private boolean isProtoInstance;

    /** A stack of proto instance names */
    private SimpleStack protoStack;

    /** List of Proto and externProto declation names */
    private ArrayList<String> protoList;

    /** Flag indicating that an SFNode has ended and implicitly this
    *  means that the parent field of the node has ended as well */
    private boolean fieldHasEndedImplicitly;

    /** A stack of parent node types */
    private SimpleStack parentTypeStack;

    /**
     * Default Constructor
     */
    public ITSCreaseAnglerFilter() {

        fieldHasEndedImplicitly = false;
        isProtoInstance = false;
        intercept = false;

        encStack = new SimpleStack();
        defMap = new HashMap<>();
        protoStack = new SimpleStack();
        protoList = new ArrayList<>();
        parentTypeStack = new SimpleStack();
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

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
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startDocument(String uri,
        String url,
        String encoding,
        String type,
        String version,
        String comment)
        throws SAVException, VRMLException {

        // this is a crude way to get revision numbers.....
        super.startDocument(uri, url, encoding, type, version, comment);
        factory = new EncodableFactory(contentHandler, fieldReader);
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

        if (protoList.contains(name)) {
            protoStack.push(name);
            isProtoInstance = true;
        }
        String fieldName = null;
        if (!fieldStack.isEmpty() && !nodeStack.isEmpty()) {
            String nodeName = (String)nodeStack.peek();
            fieldName = (String)fieldStack.peek();
            parentTypeStack.push(fieldHandler.getFieldType(nodeName, fieldName));
        }
        if (!intercept) {
            // check if this is a node that must be translated
            if (name.equals("IndexedTriangleSet")) {
                intercept = true;
                node = (IndexedTriangleSet) factory.getEncodable(name, defName);
                encStack.push(node);
                if (defName != null) {
                    defMap.put(defName, node);
                }
            }
        } else {
            Encodable enc = factory.getEncodable(name, defName);
            Encodable parent = (Encodable)encStack.peek();
            if (parent != null) {
                parent.setValue(fieldName, enc);
            }
            encStack.push(enc);
            if (defName != null) {
                defMap.put(defName, enc);
            }
        }
        nodeStack.push(name);
        if (!intercept) {
            contentHandler.startNode(name, defName);
        }
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
    public void endNode() throws SAVException, VRMLException {

        String nodeName = (String)nodeStack.pop();
        if (!protoStack.isEmpty()) {
            String protoName = (String)protoStack.peek();
            if (nodeName.equals(protoName)) {
                protoStack.pop();
                if (protoStack.isEmpty()) {
                    isProtoInstance = false;
                }
            }
        }
        if (!parentTypeStack.isEmpty()) {
            int fieldType = (int) parentTypeStack.pop();
            if (fieldType == FieldConstants.SFNODE) {
                fieldHasEndedImplicitly = true;
                String fieldName = (String)fieldStack.pop();
            }
        }
        if (intercept) {
            Encodable enc = (Encodable)encStack.pop();
            if (nodeName.equals("IndexedTriangleSet")) {
                convert();
                intercept = false;
                node = null;
            }
        } else {
            contentHandler.endNode();
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

        if (!intercept) {
            contentHandler.startField(name);
        }
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

        if (intercept) {
            /////////////////////////////////////////////////////////
            // only nodes that we are intercepting are cached in the
            // defMap, if a use is declared while intercepting for a
            // node that we have not cached - then the USE will not be
            // declared in the output file
            String fieldName = (String)fieldStack.pop();
            Encodable use = defMap.get(defName);
            /////////////////////////////////////////////////////////
            Encodable enc = (Encodable)encStack.peek();
            if ((enc != null) && (use != null)) {
                Encodable dup = use.clone(false);
                dup.setUseName(defName);
                enc.setValue(fieldName, dup);
            }
        } else {
            super.useDecl(defName);
        }
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
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

        if (!parentTypeStack.isEmpty()) {
            int fieldType = (int) parentTypeStack.peek();
            if ((fieldType == FieldConstants.MFNODE) && !fieldHasEndedImplicitly) {
                String fieldName = (String)fieldStack.pop();
                parentTypeStack.pop();
            }
        }
        fieldHasEndedImplicitly = false;
        if (!intercept) {
            super.endField();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String value)
        throws SAVException, VRMLException {

        if (isProtoInstance) {
            super.fieldValue(value);
        } else if (intercept) {
            String fieldName = (String)fieldStack.pop();
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
        } else {
            String fieldName = (String)fieldStack.pop();
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values)
        throws SAVException, VRMLException {

        if (isProtoInstance) {
            super.fieldValue(values);
        } else if (intercept) {
            String fieldName = (String)fieldStack.pop();
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, values);
            }
        } else {
            String fieldName = (String)fieldStack.pop();
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, values);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else if (intercept) {
            String fieldName = (String)fieldStack.pop();
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        } else {
            String fieldName = (String)fieldStack.pop();
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
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

        if (isProtoInstance) {
            super.fieldValue(value);
        } else if (intercept) {
            String fieldName = (String)fieldStack.pop();
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
        } else {
            String fieldName = (String)fieldStack.pop();
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {

        if (isProtoInstance) {
            super.fieldValue(value, len);
        } else if (intercept) {
            String fieldName = (String)fieldStack.pop();
            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        } else {
            String fieldName = (String)fieldStack.pop();
            String nodeName = (String)nodeStack.peek();
            fieldHandler.setFieldValue(nodeName, fieldName, value, len);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ProtoHandler
    //---------------------------------------------------------------

    /**
     * Notification of the start of an ordinary (inline) proto declaration.
     * The proto has the given node name.
     *
     * @param name The name of the proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startProtoDecl(String name)
        throws SAVException, VRMLException {
        protoList.add(name);
        super.startProtoDecl(name);
    }

    /**
     * Notification of the start of an EXTERNPROTO declaration of the given
     * name. Between here and the matching {@link #endExternProtoDecl()} call
     * you should only receive {@link #protoFieldDecl} calls.
     *
     * @param name The node name of the extern proto
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startExternProtoDecl(String name)
        throws SAVException, VRMLException {
        protoList.add(name);
        super.startExternProtoDecl(name);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------


    /**
     * Convert and encode the node.
     */
    private void convert() {
        GeometryUtils gutils = new GeometryUtils();
        GeometryHolder gholder = new GeometryHolder();

        float[] coord = ((Coordinate)node.getCoordinate()).point;
        float[] color = null;

        if (node.getColor() != null)
            color = ((Color)node.getColor()).color;
        float[] texture = null;
        if (node.getTextureCoordinate() != null)
            texture = ((TextureCoordinate)node.getTextureCoordinate()).point;
        int[] indexes;

        int numIndex = node.index.length;
        int size = numIndex + numIndex / 3;

        indexes = new int[size];

        int pos = 0;
        int currIndex = 0;

        try {
            while (pos < numIndex - 1) {
                indexes[currIndex++] = node.index[pos++];
                indexes[currIndex++] = node.index[pos++];
                indexes[currIndex++] = node.index[pos++];
                indexes[currIndex++] = -1;
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid index in TriangleSet at position: " + pos);
        }


/*
    public boolean generateTriangleArrays(float[] coord,    // replaces vfCoord
                                        float[] color,      // replaces vfColor
                                        float[] normal,     // replaces vfNormal
                                        float[] texture,    // replaces vfTexCoord
                                        int changeFlags,
                                        boolean genTexCoords,
                                        boolean genNormals,
                                        int[] vfCoordIndex,
                                        int numCoordIndex,
                                        int[] vfColorIndex,
                                        int[] vfNormalIndex,
                                        int[] vfTexCoordIndex,
                                        boolean ccw,
                                        boolean convex,
                                        boolean colorPerVertex,
                                        boolean normalPerVertex,
                                        float creaseAngle,
                                        boolean initialBuild,

                                        GeometryHolder geomData){
*/

        gutils.generateTriangleArrays(coord, color, null, texture, 0, false, true,
           indexes, indexes.length, indexes, indexes,
           indexes, node.ccw, true, true, node.normalPerVertex,
           3, creaseAngle, true, gholder);

        TriangleSet ts = (TriangleSet) factory.getEncodable("TriangleSet", null);
        Coordinate newCoord = (Coordinate) factory.getEncodable("Coordinate", null);
        newCoord.point = gholder.coordinates;
        newCoord.num_point = gholder.vertexCount;
        ts.setCoordinate(newCoord);

        Normal normal = (Normal) factory.getEncodable("Normal", null);
        normal.vector = gholder.normals;
        normal.num_vector = gholder.vertexCount;
        ts.setNormal(normal);

        // pass through for now
        ts.encode();
    }

    //---------------------------------------------------------------
    // AbstractFilter Methods
    //---------------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {

        int argIndex = -1;
        String creaseAngleArg = String.valueOf(DEFAULT_CREASE_ANGLE);

        //////////////////////////////////////////////////////////////////////
        // parse the arguments
        for (int i = 0; i < arg.length; i++) {
            String argument = arg[i];
            if (argument.startsWith("-")) {
                try {
                    if (argument.equals(CREASE_ANGLE_ARG)) {
                        creaseAngleArg = arg[i+1];
                        argIndex = i+1;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        LOG_NAME + ": Error parsing filter arguments");
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        // validate the arguments
        if (creaseAngleArg != null) {

            try {
                creaseAngle = Float.parseFloat(creaseAngleArg);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        LOG_NAME + ": Illegal value for argument: " + creaseAngleArg);
            }

        } else {
            creaseAngle = DEFAULT_CREASE_ANGLE;
        }
    }
}
