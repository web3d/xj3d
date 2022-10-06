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

import java.util.Stack;

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.ScriptHandler;

/**
 * Search for Appearance nodes which do not specify
 * a Material node, and add
 *   material Material {
 *     diffuseColor ...
 *   }
 * to those nodes.
 *
 * The diffuse color for the Material node can be specified
 * using -diffuse a b c, for example
 *  -diffuse 1.0 0.8 0.2
 * By default, a 0.8 0.8 0.8 value will be supplied
 * (and the default value will be stripped from the output by
 * the default output processor).
 *
 * @author Brad Vender
 */
public class MaterialFilter extends AbstractFilter {

    /** Argument to mark diffuse color override */
    private static final String DIFFUSE_COLOR = "-diffuse";

    /** Flag for debugging messages */
    private static final boolean DEBUG = false;

    /** The color to provide the material field. */
    private String materialColor;

    /** Stack for keeping track of which nodes are Appearance nodes */
    private Stack<Boolean> appNodeStack;

    /** Is the current node decl for Appearance? */
    private boolean inAppearance;

    /** If inAppearance, is the current field decl Material */
    private boolean inMaterialField;

    /** If inAppearance, was a material field value supplied. */
    private boolean materialAdded;

    /**
     * Basic constructor.
     */
    public MaterialFilter() {
        inMaterialField=false;
        inAppearance=false;
        materialAdded=false;
        appNodeStack=new Stack<>();
        materialColor="0.8 0.8 0.8";

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
        appNodeStack.push(inAppearance);
        inAppearance=name.equals("Appearance");
        if (inAppearance)
            // Reset the state in case of a previous node.
            materialAdded=false;
        if (DEBUG)
            System.out.println("startNode of name="+name+".  New value="+inAppearance);
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
        super.startField(name);
        if (inAppearance) {
            inMaterialField=name.equals("material");
            if (DEBUG)
                System.out.println("In material field of Appearance");
        }
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
        if (inAppearance && inMaterialField) {
            if (DEBUG)
                System.out.println("Got USE for material field.  USE="+defName);
            materialAdded=true;
            inMaterialField=false;
        }
        super.useDecl(defName);
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
        if (inMaterialField && appNodeStack.peek()) {
            if (DEBUG)
                System.out.println("End node while inMaterialField==true => materialAdded.");
            materialAdded=true;
        } else if (inAppearance) {
            if (!materialAdded) {
                if (DEBUG) {
                    System.out.println("An appearance without a Material specified is closing.");
                    System.out.println("..Supplying color "+materialColor);
                }
                startField("material");
                startNode("Material",null);
                startField("diffuseColor");
                fieldValue(materialColor);
                endNode();
            } else {
                if (DEBUG)
                    System.out.println("Appearance already specified.");
            }
        }
        super.endNode();
        inAppearance=appNodeStack.pop();
        if (DEBUG)
            System.out.println("endNode.  New inAppearance value="+inAppearance);
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

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
        if (inAppearance && inMaterialField) {
            if (DEBUG)
                System.out.println("Got value="+value+" for material field.");
            materialAdded=((value!=null)&&!("null".equalsIgnoreCase(value.trim())));
            inMaterialField=false;
        }
        super.fieldValue(value);
    }

    //----------------------------------------------------------
    // Methods defined by AbstractFilter
    //----------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation
     *
     * @param args The array of argument parameters.
     */
    @Override
    public void setArguments(String[] args) {
        super.setArguments(args);
        int i=0;
        while (i<args.length) {
            if (args[i].equals(DIFFUSE_COLOR)) {
                if (i+3>=args.length)
                    throw new IllegalArgumentException("Not enough args for Material Filter.  Expecting three more to specify color.");
                materialColor=args[i+1]+" "+args[i+2]+" "+args[i+3];
                i+=4;
            } else i++;
        }
    }

}
