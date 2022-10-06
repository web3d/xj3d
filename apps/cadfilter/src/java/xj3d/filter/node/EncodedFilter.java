/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2010
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package xj3d.filter.node;

// External imports
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStackInterface;
import org.web3d.util.SimpleStack;
import org.web3d.util.SimpleStackLogged;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.NewAbstractFilter;

/**
 * A base filter that encodes nodes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class EncodedFilter extends NewAbstractFilter {

    /** Node wrapper factory */
    protected EncodableFactory factory;

    /** Map of apps to defName */
    protected Map<String, Encodable> encMap;

    /** A stack of node wrappers */
    protected SimpleStackInterface encStack;

    /** Should nodes be encoded */
    protected boolean encodeNodes;

    /**
     * Construct a default instance of the field handler
     */
    protected EncodedFilter() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param debug Should we run in debug mode
     */
    protected EncodedFilter(boolean debug) {
        super(debug);

        if (debug) {

            encStack = new SimpleStackLogged("Encoded");

            ((SimpleStackLogged)encStack).setDebugCalls(true);
        } else {
            encStack = new SimpleStack();
        }

        encodeNodes = true;
        encMap = new HashMap<>();
    }

    /**
     * Set the filter into debug mode.  This must be called before
     * any filtering occurs.
     *
     * @param debug True to debug
     */
    @Override
    public void setDebug(boolean debug) {
        super.setDebug(debug);

        if (debug) {
            encStack = new SimpleStackLogged("Encoded");
        } else {
            if (fieldStack instanceof SimpleStack)
                return;

            encStack = new SimpleStack();
        }
    }

    /**
     * Should nodes be encoded.  Default is to encode.
     *
     * @param val The new value
     */
    protected void encode(boolean val) {
        encodeNodes = val;
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


        super.startDocument(uri, url, encoding, type, version, comment);

        factory = new EncodableFactory(contentHandler, fieldReader);
        Encodable scene = factory.getEncodable("Scene", null);
        encStack.push(scene);
    }


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
        if(contentHandler != null && !suppressCalls)
            contentHandler.endDocument();

        super.endDocument();
        encStack.clear();
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


        if (encodeNodes) {
            Encodable enc = factory.getEncodable(name, defName);

            if (defName != null) {
                encMap.put(defName, enc);
            }

            Encodable parent = (Encodable)encStack.peek();
            encStack.push(enc);
            String fieldName = (String)fieldStack.peek();

            if (parent != null) {
                parent.setValue(fieldName, enc);
            }
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

        if (encodeNodes) {
            encStack.pop();
        }

        super.endNode();
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

        String fieldName = (String)fieldStack.peek();

        if (encodeNodes) {
            Encodable use = encMap.get(defName);
            Encodable enc = (Encodable)encStack.peek();
            if ((enc != null) && (use != null)) {
                Encodable dup = use.clone(true);
                enc.setValue(fieldName, dup);
            }
        }

        super.useDecl(defName);
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
        // TODO: do we need some logic here?
/*
        int fieldType = ((Integer)parentTypeStack.peek()).intValue();
        if ((fieldType == FieldConstants.MFNODE) && !fieldHasEndedImplicitly) {
System.out.println("ENC Pop(EF)");
            encStack.pop();
        }

        super.endField();
*/
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
    public void fieldValue(String value) throws SAVException, VRMLException {

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, values);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        }

        super.fieldValue(value,len);
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        }

        super.fieldValue(value,len);
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value);
            }
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        }

        super.fieldValue(value,len);
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

        if (encodeNodes) {
            String fieldName = (String)fieldStack.peek();

            Encodable enc = (Encodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        }

        super.fieldValue(value,len);
    }
}
