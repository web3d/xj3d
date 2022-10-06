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
import java.util.HashSet;

import org.web3d.util.SimpleStack;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.ProtoHandler;
import org.web3d.vrml.sav.SAVException;
import org.web3d.vrml.sav.ScriptHandler;

// Local imports

import xj3d.filter.NewAbstractFilter;

/**
 * Removes a node from a stream.
 *
 * @author Alan Hudson
 * @version $Revision: 1.0 $
 */
public class RemoveNodeFilter extends NewAbstractFilter {
    /** The logging identifier of this app */
    private static final String LOG_NAME = "RemoveNode";

    /** DEFName param */
    private static final String DEF_NAME = "-defName";

    /** Node Name param */
    private static final String NODE_NAME = "-nodeName";

    /** The defName to remove */
    private String removeDEF;

    /** The node type to remove */
    private String removeNode;

    /** What field are we in */
    private String fieldName;

    /** Are we removing a node currently */
    private boolean removingNode;

    /** A stack of node def names */
    protected SimpleStack defStack;

    /** Set of DEF's removed */
    private HashSet<String> removedDEFs;

    /**
     * Basic constructor.
     */
    public RemoveNodeFilter() {
        defStack = new SimpleStack();
        removedDEFs = new HashSet<>();

        removingNode = false;
    }

    //----------------------------------------------------------
    // Overrides of AbstractFilter
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

        if (removeDEF != null && removeDEF.equals(defName)) {
            removingNode = true;
            if (defName != null) {
                removedDEFs.add(defName);
            }

            super.suppressCalls(true);
        }

        if (removeNode != null && removeNode.equals(name)) {
            removingNode = true;
            if (defName != null) {
                removedDEFs.add(defName);
            }
            super.suppressCalls(true);
        }

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }

        defStack.push(defName);

        super.startNode(name, defName);
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
        fieldName = name;
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
        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
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
        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }

        super.fieldValue(values);
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value);
    }

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

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value, len);
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

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value);
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

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value, len);
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
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
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

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value,len);
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
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value, len);
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
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value);
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
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

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }
        super.fieldValue(value, len);
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
        if (removedDEFs.contains(defName)) {
            super.suppressCalls(true);
        }

        if (fieldName != null) {
            super.startField(fieldName);
            fieldName = null;
        }

        super.useDecl(defName);

        if (removedDEFs.contains(defName)) {
            super.suppressCalls(false);
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
        String defName = (String) defStack.pop();

        if (removingNode) {
            String nodeName = (String) nodeStack.peek();

            if (removeNode != null && nodeName.equals(removeNode)) {
                removingNode = false;

                super.endNode();
                super.suppressCalls(false);
            } else if (removeDEF != null && defName != null && defName.equals(removeDEF)) {
                removingNode = false;
                super.endNode();
                super.suppressCalls(false);
            } else {
                super.endNode();
            }
        } else {
            super.endNode();
        }
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
                case DEF_NAME:
                    if (i + 1 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting one more to defName.");
                    }   removeDEF = args[i+1];
                    break;
                case NODE_NAME:
                    if (i + 1 >= args.length){
                        
                        throw new IllegalArgumentException(
                                "Not enough args for " + LOG_NAME + ".  " +
                                        "Expecting one more to specify nodeName.");
                }   removeNode = args[i+1];
                    break;
            }
        }
    }
}
