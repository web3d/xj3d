/*****************************************************************************
 *                        Web3d.org Copyright (c) 2010
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
import java.util.HashMap;

// Local imports
import org.web3d.vrml.sav.SAVException;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NewAbstractFilter;

/**
 * Explodes the shapes in a file to create a cad-like exploded view.
 * <p>
 *
 * Assumes that the FlattenTransform filter is run before this and has squashed
 * all the objects down to single shape nodes that are at the file root. Works
 * on the coordinates of the geometry, not any explicit bbox definitions.
 * <p>
 *
 * <b>Filter Options</b><p>
 * -explodeFactor - Factor to multiply the center vector to space out items
 * -addDEFLabels - Add screen aligned labels of the DEF name?
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ExplodeShapesFilter extends NewAbstractFilter {

    /** The maximum number of digits for an fraction (float or double) */
    private final static int MAX_FRACTION_DIGITS = 4;

    /** The logging identifier of this app */
    private static final String LOG_NAME = "ExplodeShapesFilter";

    /** Should we add DEF name labels */
    private boolean addDEFLabels = true;

    /** The explosion factor */
    private float explosionFactor = 1.5f;

    /** Are we inside any of the geometry nodes */
    private boolean insideCoord;

    /** Flag to say the next fieldValue() call will be the points */
    private boolean insidePoint;

    /** Center of the current shape */
    private float[] center;

    /** Scratch field for translation */
    private float[] translation;

    /** DEF name for current shape */
    private String def;

    /** Map of DEF's to billboards */
    private HashMap<String, Billboard> defMap;

    /**
     * Recenters the geometry
     */
    public ExplodeShapesFilter() {
        insideCoord = false;
        insidePoint = false;

        center = new float[3];
        translation = new float[3];

        defMap = new HashMap<>();
    }

    //----------------------------------------------------------
    // ContentHandler methods
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

        super.startDocument(uri,url, encoding, type, version, comment);
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

        // TODO: Could add component but not sure what happens if redeclared lower
        super.profileDecl("Immersive");
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

        super.endDocument();
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

        if (name.equals("Shape")) {
            super.startNode("Transform", null);
            super.startField("children");
            def = defName;
        }

        if (name.equals("Coordinate")) {
            insideCoord = true;
        }

        super.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endNode() throws SAVException, VRMLException {
        String node_name = (String) nodeStack.peek();

        if (node_name.equals("Shape")) {
            super.endNode();

            translation[0] = center[0] * explosionFactor;
            translation[1] = center[1] * explosionFactor;
            translation[2] = center[2] * explosionFactor;


            if (addDEFLabels && def != null) {
                float[] txtTranslation = new float[] {translation[0], translation[1] + 0.25f, translation[2]};
                float textSize = 0.3f;
System.out.println("***Setting text to: " + textSize);

                Billboard b = new Billboard(def, txtTranslation, textSize, translation);
                defMap.put(def, b);
                encode(b);

            }

            super.endField();   // End children field

System.out.println("def: " + def + " center: " + java.util.Arrays.toString(center));
            super.startField("translation");

            super.fieldValue(translation,3);

            super.endNode();    // End added Transform
        } else {
            super.endNode();
        }
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link org.web3d.vrml.sav.ScriptHandler} or {@link org.web3d.vrml.sav.ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {
        super.startField(name);

        if(insideCoord && name.equals("point"))
            insidePoint = true;
    }

    //-----------------------------------------------------------------------
    //Methods for interface StringContentHandler
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
    public void fieldValue(String value) throws SAVException, VRMLException {

        if(insidePoint) {
            float[] coords = fieldReader.MFVec3f(value);
            processCoords(coords, coords.length / 3);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * The value of a field given as an array of strings.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values) throws SAVException, VRMLException {

        if(insidePoint) {
            float[] coords = fieldReader.MFVec3f(values);
            processCoords(coords, coords.length / 3);
        } else {
            super.fieldValue(values);
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
/*
    public void useDecl(String defName) throws SAVException, VRMLException {
        Billboard b = defMap.get(defName);

        if (b == null) {
            super.useDecl(defName);
            return;
        } else {
            super.startNode("Transform", null);
            super.startField("children");

            if (addDEFLabels) {
                encode(b);
            }

            float[] translation = b.getTranslation();
            super.endField();   // End children field

            super.startField("translation");

            super.fieldValue(translation,3);

            super.useDecl(defName);

            super.endNode();    // End added Transform
        }
    }
*/
    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

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

        if(insidePoint) {
            processCoords(value, len / 3);
        } else {
            super.fieldValue(value, len);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Take the array of points, find the bounds and then recenter and generate
     * the output from there.
     *
     * @param coords The list of coordinates to look at
     * @param numCoords The number of coords to process from the list
     */
    private void processCoords(float[] coords, int numCoords) {
        suppressCalls(true);
        super.fieldValue(coords, numCoords);
        suppressCalls(false);

        float min_x = Float.POSITIVE_INFINITY;
        float min_y = Float.POSITIVE_INFINITY;
        float min_z = Float.POSITIVE_INFINITY;

        float max_x = Float.NEGATIVE_INFINITY;
        float max_y = Float.NEGATIVE_INFINITY;
        float max_z = Float.NEGATIVE_INFINITY;

        int idx = 0;

        for(int i = 0; i < numCoords; i++) {
            if(coords[idx] < min_x)
                min_x = coords[idx];

            if(coords[idx] > max_x)
                max_x = coords[idx];

            if(coords[idx + 1] < min_y)
                min_y = coords[idx + 1];

            if(coords[idx + 1] > max_y)
                max_y = coords[idx + 1];

            if(coords[idx + 2] < min_z)
                min_z = coords[idx + 2];

            if(coords[idx + 2] > max_z)
                max_z = coords[idx + 2];

            idx += 3;
        }


        fieldHandler.setFieldValue("Coordinate",
                                    "point",
                                    coords,
                                    numCoords * 3);

        insidePoint = false;
        insideCoord = false;

        center[0] = (min_x + max_x) / 2.0f;
        center[1] = (min_y + max_y) / 2.0f;
        center[2] = (min_z + max_z) / 2.0f;
    }

    private void encode(Billboard bill) {
        float[] textTranslation = bill.getTextTranslation();
        float textSize = bill.getSize();
        String def = bill.getDEF();

        super.startNode("Transform", null);
        super.startField("translation");
        super.fieldValue(textTranslation, 3);
        super.startField("children");
        super.startNode("Billboard", null);
        super.startField("axisOfRotation");
        super.fieldValue(new float[] {0,0,0},3);
        // axisOfRotation 0 0 0
        super.startField("children");

        super.startNode("Shape", null);
        super.startField("geometry");
/*
        super.startNode("Sphere", null);
        super.startField("radius");
        super.fieldValue(0.1f);
        super.endNode();
*/

        super.startNode("Text", null);
        super.startField("string");
        super.fieldValue(new String[] {def});
        super.startField("fontStyle");
        super.startNode("FontStyle", null);
        super.startField("size");
        super.fieldValue(textSize);
        super.endNode();  // FontStyle
        super.endNode();  // Text


        super.endNode();  // Shape
        super.endNode();  // Billboard
        super.endField(); // children
        super.endNode(); // Transform
    }

}

class Billboard {
    private String def;
    private float[] textTrans;
    private float textSize;
    private float[] trans;

    public Billboard(String def, float[] textTrans, float textSize, float[] trans) {
        this.def = def;
        this.textTrans = new float[3];
        this.textTrans[0] = textTrans[0];
        this.textTrans[1] = textTrans[1];
        this.textTrans[2] = textTrans[2];

        this.textSize = textSize;

        this.trans = new float[3];
        this.trans[0] = trans[0];
        this.trans[1] = trans[1];
        this.trans[2] = trans[2];
    }

    public float[] getTextTranslation() {
        return textTrans;
    }

    public float[] getTranslation() {
        return trans;
    }

    public float getSize() {
        return textSize;
    }

    public String getDEF() {
        return def;
    }
}