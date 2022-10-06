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
import org.web3d.util.SimpleStack;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.ScriptHandler;

// Local imports

import xj3d.filter.NewAbstractFilter;

/**
 * Search for Appearance nodes and add or replace the Texture Transform with
 * the specified params.  By default it does not replace existing content.
 *
 * Params:
 *    -defFilter name - Limit operation to only Appearances inside the DEFed node
 *    -replace - Replace any existing Texture Transforms
 *    -center x,y - The center
 *    -rotation a - The rotation
 *    -scale x,y - The scale
 *    -translation x,y - The translation
 *
 * @author Alan Hudson
 * @version $Revision: 1.0 $
 */
public class TextureTransformFilter extends NewAbstractFilter {
    /** The logging identifier of this app */
    private static final String LOG_NAME = "TextureTransform";

    /** DEFName filter param */
    private static final String DEF_FILTER = "-defFilter";

    /** Whether to replace existing texture transforms */
    private static final String REPLACE = "-replace";

    /** The center param */
    private static final String CENTER = "-center";

    /** The rotation param */
    private static final String ROTATION = "-rotation";

    /** The scale param */
    private static final String SCALE = "-scale";

    /** The translation param */
    private static final String TRANSLATION = "-translation";

    /** DEF Filter */
    private String defFilter;

    /** Replace existing TextureTransforms */
    private boolean replace;

    /** Center field */
    private float[] center;

    /** Center field */
    private Float rotation;

    /** Scale field */
    private float[] scale;

    /** Translation field */
    private float[] translation;

    /** Are we removing a node currently */
    private boolean removingNode;

    /** A stack of node def names */
    protected SimpleStack defStack;

    /** Has a transform been added */
    private boolean transformAdded;

    /** Are we inside a defFilter */
    private boolean insideDEF;

    /** Are we inside a TextureTransform */
    private boolean insideTransform;

    /** The field we are in, not valid for nodes */
    private String fieldName;

    /**
     * Basic constructor.
     */
    public TextureTransformFilter() {
        defStack = new SimpleStack();

        removingNode = false;
        transformAdded = false;
        insideTransform = false;
    }

    //----------------------------------------------------------
    // Overrides of AbstractFilter
    //----------------------------------------------------------
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

        fieldName = name;

        super.startField(name);
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
    public void startNode(String name, String defName) throws SAVException,
            VRMLException {

        switch (name) {
            case "Appearance":
                transformAdded = false;
                break;
            case "TextureTransform":
                if (replace)
                    insideTransform = true;
                break;
        }

        if (defFilter != null && defName != null) {
            if (defFilter.equals(defName)) {
                insideDEF = true;
            }
        }

        defStack.push(defName);

        super.startNode(name, defName);
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
        String defName = (String) defStack.pop();

        String nodeName = (String) nodeStack.peek();

        switch (nodeName) {
            case "TextureTransform":
                insideTransform = false;
                transformAdded = true;
                break;
            case "Appearance":
                if (insideDEF && !transformAdded) {
                    super.startField("textureTransform");
                    super.startNode("TextureTransform", null);
                    if (center != null) {
                        super.startField("center");
                        super.fieldValue(center, 2);
                    }   if (rotation != null) {
                        super.startField("rotation");
                        super.fieldValue(rotation);
                    }
                    if (scale != null) {
                        super.startField("scale");
                        super.fieldValue(scale, 2);
                    }   if (translation != null) {
                        super.startField("translation");
                        super.fieldValue(scale, 2);
                    }   super.endNode();
                    transformAdded = true;
                }
                break;
        }

        if (defFilter != null && defName != null && defName.equals(defFilter)) {
            insideDEF = false;
        }

        super.endNode();
    }


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
    public void fieldValue(String value) throws SAVException, VRMLException {
        if (insideDEF && insideTransform) {
            if (fieldName.equals("center") && center != null) {
                super.fieldValue(center, 2);

                return;
            }

            if (fieldName.equals("rotation") && rotation != null) {
                super.fieldValue(rotation);

                return;
            }

            if (fieldName.equals("scale") && scale != null) {
                super.fieldValue(scale, 2);

                return;
            }

            if (fieldName.equals("translation") && translation != null) {
                super.fieldValue(translation, 2);

                return;
            }
        }

        super.fieldValue(value);
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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void fieldValue(String[] values) throws SAVException, VRMLException {
        if (insideDEF && insideTransform) {

            if (fieldName.equals("center") && center != null) {
                super.fieldValue(center, 2);

                return;
            }

            if (fieldName.equals("rotation") && rotation != null) {
                super.fieldValue(rotation);

                return;
            }

            if (fieldName.equals("scale") && scale != null) {
                super.fieldValue(scale, 2);

                return;
            }

            if (fieldName.equals("translation") && translation != null) {
                super.fieldValue(translation, 2);

                return;
            }
        }

        super.fieldValue(values);
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

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
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        if (insideDEF && insideTransform && fieldName.equals("rotation") && rotation != null) {
            super.fieldValue(rotation);

            return;
        }

        super.fieldValue(value);
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

        if (insideDEF && insideTransform) {

            if (fieldName.equals("center") && center != null) {
                super.fieldValue(center, 2);

                return;
            }

            if (fieldName.equals("scale") && scale != null) {
                super.fieldValue(scale, 2);

                return;
            }

            if (fieldName.equals("translation") && translation != null) {
                super.fieldValue(translation, 2);

                return;
            }
        }
        super.fieldValue(value, len);
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {

        if (insideDEF && insideTransform) {

            if (fieldName.equals("center") && center != null) {
                super.fieldValue(center, 2);

                return;
            }

            if (fieldName.equals("rotation") && rotation != null) {
                super.fieldValue(rotation);

                return;
            }

            if (fieldName.equals("scale") && scale != null) {
                super.fieldValue(scale, 2);

                return;
            }

            if (fieldName.equals("translation") && translation != null) {
                super.fieldValue(translation, 2);

                return;
            }
        }

        super.fieldValue(value, len);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        String prefix = "-" + LOG_NAME + ":";
        String arg;

        for (int i = 0; i< args.length; i++) {
            arg = args[i];

            if (arg.startsWith(prefix)) {
                arg = "-" + arg.substring(prefix.length());
            }

            switch (arg) {
                case DEF_FILTER:
                    if (i + 1 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for TextureTransformFilter.  " +
                                        "Expecting one more to defName.");
                    }   defFilter = args[i+1];
                    break;
                case REPLACE:
                    if (i + 1 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for RemoveNodeFilter.  " +
                                        "Expecting one more to specify nodeName.");
                    }   replace = true;
                    break;
                case CENTER:
                    if (i + 2 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting two more for center.");
                    }   center = new float[2];
                    try {
                        center[0] = Float.parseFloat(args[i+1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                LOG_NAME + ": Illegal value for argument: " + args[i+1]);
                    }   try {
                        center[1] = Float.parseFloat(args[i+2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                LOG_NAME + ": Illegal value for argument: " + args[i+2]);
                    }   break;
                case ROTATION:
                    if (i + 1 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting one more for rotation.");
                    }   try {
                        rotation = Float.parseFloat(args[i+1]);
                    }catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                LOG_NAME + ": Illegal value for argument: " + args[i+1]);
                    }   break;
                case SCALE:
                    if (i + 2 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting two more for scale.");
                    }   scale = new float[2];
                    try {
                        scale[0] = Float.parseFloat(args[i+1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                LOG_NAME + ": Illegal value for argument: " + args[i+1]);
                    }   try {
                        scale[1] = Float.parseFloat(args[i+2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                LOG_NAME + ": Illegal value for argument: " + args[i+2]);
                    }   break;
                case TRANSLATION:
                    if (i + 2 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting two more for translation.");
                    }   translation = new float[2];
                    try {
                        translation[0] = Float.parseFloat(args[i+1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            LOG_NAME + ": Illegal value for argument: " + args[i+1]);
                }   try {
                    translation[1] = Float.parseFloat(args[i+2]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            LOG_NAME + ": Illegal value for argument: " + args[i+2]);
                }   break;
            }
        }

        if (defFilter == null) {
            insideDEF = true;
        }
    }
}
