/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
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
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStackInterface;
import org.web3d.util.SimpleStack;
import org.web3d.util.SimpleStackLogged;

import org.web3d.vrml.lang.VRMLException;

import xj3d.filter.BaseFilter;
import xj3d.filter.NodeMarker;

/**
 * A base filter that encodes nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class CommonEncodedBaseFilter extends BaseFilter {

	/**
     * Message for an unsupported node
     */
    private static final String UNSUPPORTED_NODE = "xj3d.filter.node.CommonEncodedBaseFilter.unsupportedNode";

    /** Node wrapper factory */
    protected CommonEncodableFactory factory;

    /** Map of apps to defName */
    protected Map<String, CommonEncodable> encMap;

    /** A stack of node wrappers */
    protected SimpleStackInterface encStack;

    /** Scene instance */
    protected CommonScene scene;

    /** Should nodes be encoded */
    protected boolean encodeNodes;

    /** Should nodes be encoded */
    protected boolean encodeRoutes;

    /**
     * Construct a default instance of the field handler
     */
    protected CommonEncodedBaseFilter() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param debug Should we run in debug mode
     */
    protected CommonEncodedBaseFilter(boolean debug) {
        super(debug);

        if (debug) {
            encStack = new SimpleStackLogged("Encoded");
            ((SimpleStackLogged)encStack).setDebugCalls(true);
        } else {
            encStack = new SimpleStack();
        }

        encodeNodes = true;
        encodeRoutes = false;
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
            ((SimpleStackLogged)encStack).setDebugCalls(true);
        } else {
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

        factory = new CommonEncodableFactory(contentHandler, scriptHandler, fieldReader);
        scene = (CommonScene)factory.getEncodable("Scene", null);
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

        super.endDocument();
        encStack.clear();
        encMap.clear();
        scene = null;
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
            CommonEncodable enc = factory.getEncodable(name, defName);

            if (enc == null) {
                I18nUtils.printMsg(UNSUPPORTED_NODE, I18nUtils.CRIT_MSG, new String[] {name});
                throw new VRMLException("Unsupported node: " + name);
            }

            if (defName != null) {
                encMap.put(defName, enc);
            }

            CommonEncodable parent = (CommonEncodable)encStack.peek();
            enc.setParent(parent);
            encStack.push(enc);

            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

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

        if (encodeNodes) {
            CommonEncodable use = encMap.get(defName);
            CommonEncodable parent = (CommonEncodable)encStack.peek();
            if ((parent != null) && (use != null)) {

                NodeMarker marker = (NodeMarker)nodeStack.peek();
                String fieldName = marker.fieldName;

                CommonEncodable dup = use.clone(false);
                dup.setParent(parent);
                dup.setUseName(defName);
                parent.setValue(fieldName, dup);
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
        super.endField();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
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
            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            CommonEncodable enc = (CommonEncodable)encStack.peek();
            if (enc != null) {
                enc.setValue(fieldName, value, len);
            }
        }

        super.fieldValue(value,len);
    }

    //---------------------------------------------------------------
    // Methods defined by RouteHandler
    //---------------------------------------------------------------

    /**
     * Notification of a ROUTE declaration in the file. The context of this
     * route should be assumed from the surrounding calls to start and end of
     * proto and node bodies.
     *
     * @param srcNodeName The name of the DEF of the source node
     * @param srcFieldName The name of the field to route values from
     * @param destNodeName The name of the DEF of the destination node
     * @param destFieldName The name of the field to route values to
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void routeDecl(String srcNodeName,
                          String srcFieldName,
                          String destNodeName,
                          String destFieldName)
        throws SAVException, VRMLException {

        if (encodeRoutes) {
            Route r = new Route(
                srcNodeName,
                srcFieldName,
                destNodeName,
                destFieldName);
            r.setRouteHandler(routeHandler);
            scene.addRoute(r);
        }
        super.routeDecl(
            srcNodeName,
            srcFieldName,
            destNodeName,
            destFieldName);
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
    public void startProtoDecl(String name) throws SAVException, VRMLException {
        I18nUtils.printMsg("xj3d.filter.node.CommonEncodedBaseFilter.noProto",I18nUtils.EXT_MSG,
                new String[]{name});
    }

    /**
     * Notification of the end of an ordinary proto declaration statement.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endProtoDecl() throws SAVException, VRMLException {
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
     * @param value The default value of the field. Null if not allowed.
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void protoFieldDecl(int access,
                               String type,
                               String name,
                               Object value)
        throws SAVException, VRMLException {

    }

    /**
     * Notification of a field value uses an IS statement. If we are running
     * in VRML97 mode, this will throw an exception if the field access types
     * do not match.
     *
     * @param fieldName The name of the field that is being IS'd
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void protoIsDecl(String fieldName) throws SAVException, VRMLException {
    }

    /**
     * Notification of the start of an ordinary proto body. All nodes
     * contained between here and the corresponding
     * {@link #endProtoBody()} statement form the body and not the normal
     * scenegraph information.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void startProtoBody() throws SAVException, VRMLException {
    }

    /**
     * Notification of the end of an ordinary proto body. Parsing now returns
     * to ordinary node declarations.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endProtoBody() throws SAVException, VRMLException {
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
    public void startExternProtoDecl(String name) throws SAVException, VRMLException {
        I18nUtils.printMsg("xj3d.filter.node.CommonEncodedBaseFilter.noExternProto",I18nUtils.EXT_MSG,
                new String[]{name});
    }

    /**
     * Notification of the end of an EXTERNPROTO declaration.
     *
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void endExternProtoDecl() throws SAVException, VRMLException {
    }

    /**
     * Notification of the URI list for an EXTERNPROTO. This is a complete
     * list of URIs. The calling application is required to interpret the
     * incoming strings. Even if the externproto has no URIs registered, this
     * method shall be called. If there are none available, this will be
     * called with a zero length list of values.
     *
     * @param values A list of strings representing all of the URI values
     * @throws SAVException Always thrown
     * @throws VRMLException Never thrown
     */
    @Override
    public void externProtoURI(String[] values) throws SAVException, VRMLException {
    }

    //---------------------------------------------------------------
    // Methods defined by ScriptHandler
    //---------------------------------------------------------------

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
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void startScriptDecl() throws SAVException, VRMLException {
        System.out.println("EXTMSG: Script declarations are not supported, ignoring.");
    }

    /**
     * Notification of the end of a script declaration. This is guaranteed to
     * be called before the ContentHandler <CODE>endNode()</CODE> callback.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void endScriptDecl() throws SAVException, VRMLException {
    }

    /**
     * Notification of a script's field declaration. This is used for all
     * fields except <CODE>url</CODE>, <CODE>mustEvaluate</CODE> and
     * <CODE>directOutput</CODE> fields. These fields use the normal field
     * callbacks of {@link ContentHandler}.
     *
     * @param access The access type (eg exposedField, field etc)
     * @param type The field type (eg SFInt32, MFVec3d etc)
     * @param name The name of the field
     * @param value The default value of the field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
    @Override
    public void scriptFieldDecl(int access,
                                String type,
                                String name,
                                Object value)
        throws SAVException, VRMLException {

    }
}
