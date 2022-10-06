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
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NewAbstractFilter;

import org.web3d.util.SimpleStack;

/**
 * TransformFilter places up to n transforms around an object.
 * <p>
 * Arguments are:
 * <ul>
 * <li>-scale1 x y z,</li>
 * <li>-rotate1 x y z a(radians),</li>
 * <li>-translate1 x y z, or</li>
 * <li>-resetTransforms</li>
 * </ul>
 *
 * We keep looking for scale/rotate/translate params until we run out of them.
 * We don't assume that the user provides all three (scale/rotate/translate) for
 * every transform group.  In other words, this is valid:
 * <br> -scale1 1 2 1 -rotate2 0 1 0 3.1415<br>
 *
 * For each transform specified add a Transform around the input stream.
 * So the above input would do this:
 * <p>
 * DEF TRANSFORM_FILTER1 Transform {
 *   scale 1 2 1
 *   children [
 *      DEF TRANFORM_FILTER2 Transform {
 *         rotation 0 1 0 3.115
 *         children [
 *            ....  &lt;original content&gt;
 *         ]
 *      }
 *   ]
 *}
 *
 * ResetTransforms will remove any previous TransformFilter.  This will only be
 * detected if first node is a Transform.  This allows multiple files to
 * be combined together without error.  This will be detected by using a
 * DEF name of TRANSFORM_FILTER*.
 *
 * @author Alan Hudson
 * @version $Revision: 1.0 $
 */
public class TransformFilter extends NewAbstractFilter {
    private static final boolean DEBUG = false;

    /** The DEF name start to use */
    private static final String DEF_BASE = "TRANSFORM_FILTER";

    /** Marker for false */
    private static final Boolean FALSE = false;

    /** Marker for true */
    private static final Boolean TRUE = true;

    /** This hash map contains the set of all scales we found */
    private HashMap<Integer, float[]> scaleMap;

    /** This hash map contains the set of all rotations we found */
    private HashMap<Integer, float[]> rotateMap;

    /** This hash map contains the set of all translations we found */
    private HashMap<Integer, float[]> translateMap;

    /** number of transforms we encounter when parsing the arguments */
    private int transforms;

    /** Should we check for old DEFs.  Stop after first node */
    private boolean transformsInserted;

    /** Has the reset completed */
    private boolean resetCompleted;

    /** A stack of suppress flags */
    private SimpleStack suppressStack;

    /** Should we reset old values, ignores all other params */
    private boolean reset;

    public TransformFilter() {
        super(DEBUG);

        //
        // initialize our containers
        //
        scaleMap = new HashMap<>();
        rotateMap = new HashMap<>();
        translateMap = new HashMap<>();
        transforms = 0;
        transformsInserted = false;

        suppressStack = new SimpleStack();
        reset = false;
        resetCompleted = false;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        suppressCalls(false);
        for(int i=0; i < transforms; i++) {
            super.endField();
            super.endNode();
        }

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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (reset && !resetCompleted) {
            if (defName != null && defName.startsWith(DEF_BASE)) {
                suppressStack.push(TRUE);

                suppressCalls(true);
                super.startNode(name, defName);
                return;
            } else {
                resetCompleted = true;
            }
        }

        if (!transformsInserted) {

            transformsInserted = true;

            suppressCalls(false);

            for(int i=0; i < transforms; i++) {
                super.startNode("Transform", DEF_BASE+i);

                float[] scale = scaleMap.get(i);

                if (scale != null) {
                    super.startField("scale");
                    super.fieldValue(scale, 3);
                }

                float[] rotate = rotateMap.get(i);

                if (rotate != null) {
                    super.startField("rotation");
                    super.fieldValue(rotate, 4);
                }

                float[] translate = translateMap.get(i);

                if (translate != null) {
                    super.startField("translation");
                    super.fieldValue(translate, 3);
                }

                super.startField("children");
            }
        }

        suppressStack.push(FALSE);

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
        boolean suppressFields = ((Boolean)suppressStack.pop());

        suppressCalls(suppressFields);
        super.endNode();
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
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
        boolean suppressFields = ((Boolean) suppressStack.peek());

        suppressCalls(suppressFields);
        super.startField(name);
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endField() throws SAVException, VRMLException {
        boolean suppressFields = ((Boolean) suppressStack.peek());

        suppressCalls(suppressFields);
        super.endField();
    }

    //---------------------------------------------------------------
    // Methods defined by StringContentHandler
    //---------------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        //
        // Note: If for some reason we have an indexing problem or for
        // some reason we cannot parse a value, it means that the
        // filter arguments have not been formatted correctly, and
        // so we will throw a FilterExitCodes.INVALID_ARGUMENTS error.
        //
        try {
            for( int i = 0; i< args.length; i++) {
                String argument = args[i];
                if (argument.equals("-resetTransforms")) {
                    reset = true;

                    scaleMap.clear();
                    rotateMap.clear();
                    translateMap.clear();
                    transforms = 0;
                } else if(argument.startsWith("-scale")) {

                    int scaleIndex = Integer.parseInt(argument.substring(6));
                    if( scaleIndex > transforms ){
                        transforms = scaleIndex;
                    }

                    float widthScalar = Float.parseFloat(args[++i]);
                    float heightScalar = Float.parseFloat(args[++i]);
                    float depthScalar = Float.parseFloat(args[++i]);

                    float[] scalar = new float[]{widthScalar, heightScalar, depthScalar};

                    scaleIndex--;   // Allow users to start counting at 1 like humans
                    scaleMap.put(scaleIndex, scalar);
                } else if (argument.startsWith("-rotate")) {
                    int rotateIndex = Integer.parseInt(argument.substring(7));
                    if( rotateIndex > transforms ){
                        transforms = rotateIndex;
                    }

                    float x = Float.parseFloat(args[++i]);
                    float y = Float.parseFloat(args[++i]);
                    float z = Float.parseFloat(args[++i]);
                    float aRadians = Float.parseFloat(args[++i]);

                    float[] rotation = new float[]{x, y, z, aRadians};

                    rotateIndex--;
                    rotateMap.put(rotateIndex, rotation);
                } else if (argument.startsWith("-translate")) {
                    int translateIndex = Integer.parseInt(argument.substring(10));
                    if( translateIndex > transforms ){
                        transforms = translateIndex;
                    }

                    float x = Float.parseFloat(args[++i]);
                    float y = Float.parseFloat(args[++i]);
                    float z = Float.parseFloat(args[++i]);

                    float[] translation = new float[] {x, y, z};

                    translateIndex--;
                    translateMap.put(translateIndex, translation);
                }

            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
            throw new IllegalArgumentException("Invalid arguments in TransformFilter");
        }
    }
}