/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.filter;

// External imports
import java.util.HashSet;
import java.util.Set;

// Local imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.SAVException;

/**
 * Search for Shape nodes which do not specify an Appearance node and add a
 * default appearance or material to those nodes.
 * <p>
 *
 * If <code>-appAndMat</code> is specified in the filter options,
 * will add a Material node to the generated Appearance node.
 * <p>
 * The diffuse color for the Material node can be specified
 * using -diffuse a b c, for example
 *  -diffuse 1.0 0.8 0.2
 * By default, a 0.8 0.8 0.8 value will be supplied
 * (and the default value will be stripped from the output by
 * the default output processor).
 *
 *
 * @author Eric Fickenscher (orig. Brad Vender)
 * @version $Revision: 1.9 $
 */
public class AppearanceFilter extends BaseFilter {

    /** Argument so that an Material node is also added to the Appearance */
    private static final String SET_APPEARANCE_AND_MATERIAL="-appAndMat";

    /** Argument to mark diffuse color override */
    private static final String SET_DIFFUSE_COLOR = "-diffuse";

    /** The default diffuse color is 20% grey */
    private static final String DEFAULT_DIFFUSE_COLOR = "0.8 0.8 0.8";

    /** Set of node names that represent textures */
    private static final Set<String> TEXTURE_NODES;

    /** If inShape, was an appearance added? */
    private boolean appearanceAdded;

    /** If inAppearance, was a material added? */
    private boolean materialAdded;

    /** The color to provide the material field. */
    private String materialColor;

    /** Boolean to track if we should add a material node */
    private boolean addMaterial;

    /**
     * Static constructor to populate node name lists.
     */
    static {
        TEXTURE_NODES = new HashSet<>();
        TEXTURE_NODES.add("ImageTexture");
        TEXTURE_NODES.add("MovieTexture");
        TEXTURE_NODES.add("PixelTexture");
        TEXTURE_NODES.add("MutliTexture");
        TEXTURE_NODES.add("ImageTexture3D");
        TEXTURE_NODES.add("PixelTexture3D");
        TEXTURE_NODES.add("ComposedTexture3D");
    }

    /**
     * Basic constructor.  Note that boolean 'addMaterial'
     * is initially set to 'false', which means that we
     * will NOT be adding any material.  This boolean may be
     * set to true by calling the setArguments() method, in
     * which case we WILL add a Material node.
     */
    public AppearanceFilter() {

        appearanceAdded = false;
        materialAdded = true;
        addMaterial = false;
        materialColor = DEFAULT_DIFFUSE_COLOR;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

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

        super.startNode(name, defName);

        // Reset the flags for each shape node
        if (name.equals("Shape")) {
            appearanceAdded = false;
            materialAdded = false;
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
        NodeMarker marker = (NodeMarker)nodeStack.peek();


        // TODO: nodeName comes back as Shape which is likely wrong.
//        if (marker.nodeName.equals("Appearance")) {
        if (marker.fieldName.equals("appearance")) {
            appearanceAdded = true;
            materialAdded = true;  // with a use can't add a material easily
        }

        super.useDecl(defName);
    }

    /**
     * Notification of the end of a node declaration.
     * If boolean addMaterial is set to TRUE, then when a
     * Shape node ends we reset materialAdded, so that
     * future Shapes can also have Material nodes added
     * if necessary.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endNode() throws SAVException, VRMLException {

        NodeMarker marker = (NodeMarker)nodeStack.peek();
        String nodeName = marker.nodeName;

        if (nodeName.equals("Shape")) {

            if (!appearanceAdded) {
                startField("appearance");
                startNode("Appearance", null);
                endNode();
            }

            // this shape ended, but others may follow, so we must
            // reset the appearanceAdded, materialAdded, and materialColor
            // values for future Shapes
            appearanceAdded = false;
            if (addMaterial) {
                materialAdded = false;
            }
            materialColor = DEFAULT_DIFFUSE_COLOR;

        } else if (TEXTURE_NODES.contains(nodeName)) {

              // If we have a texture, ignore adding a material because that
              // would mess with the underlying lighting equations.
              materialAdded = true;

        } else if (nodeName.equals("Appearance")) {

            if (!materialAdded) {
                startField("material");
                startNode("Material", null);
                startField("diffuseColor");
                fieldValue(materialColor);
                endNode();
                endField();
            }
            appearanceAdded = true;

        } else if (nodeName.equals("Material")) {

            materialAdded = true;

        } else if (nodeName.equals("Color") || nodeName.equals("ColorRGBA")) {

            // No need to add appearances for colour per vertex node
            appearanceAdded = true;
            materialAdded = true;
        }

        super.endNode();
    }

    //----------------------------------------------------------
    // Methods defined by AbstractFilter
    //----------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation.
     * Note that the boolean 'addMaterial' will remain false unless
     * this method is called and one of the argument parameters contains
     * SET_APPEARANCE_AND_MATERIAL.
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        for (int i = 0; i<args.length; i++) {

            switch (args[i]) {
                case SET_APPEARANCE_AND_MATERIAL:
                    addMaterial = true;
                    materialAdded = false;
                    break;
                case SET_DIFFUSE_COLOR:
                    if (i + 3 >= args.length){

                        throw new IllegalArgumentException(
                                "Not enough args for Material Filter.  " +
                                        "Expecting three more to specify color.");
                    }   materialColor = args[i+1] + " " + args[i+2] + " " + args[i+3];
                    break;
            }
        }
    }
}
