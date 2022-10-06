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

package xj3d.filter;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;

/**
 * Finds duplicate url references in ImageTextures and DEF/USE them
 * This version is 1 pass, so it must DEF all ImageTextures in the
 * scene.
 * <p>
 * Metadata on ImageTextures will be lost.  We would need a node
 * buffering strategy to fix this.
 * <p>
 * Assumes that urls with the first entry the same are equal.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public class DEFUSEImageTextureFilter extends AbstractFilter {

    /** Integer representing no length specified */
    private static Integer NO_LENGTH_VALUE = -1;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** A stack of field lengths for arrays */
    private SimpleStack fieldLengthsStack;

    /** A stack of def names */
    private SimpleStack defStack;

    /** A list of current defnames and URL's.  Non ImageTextures will be null. */
    private Set<String> defNames;

    /** A mapping of urls to field values */
    private Map<String, Map<String, Object>> urlMap;

    /** A mapping of urls to def names */
    private Map<String, String> urlDefMap;

    /** The index of the last DEF named of the form "TEX*" */
    private int texNum;

    /** Are we inside an ImageTexture */
    private boolean insideIT;

    /**
     * Construct a default instance of this filter
     */
    public DEFUSEImageTextureFilter() {
        fieldValuesStack = new SimpleStack();
        fieldLengthsStack = new SimpleStack();
        defStack = new SimpleStack();
        texNum = 1;
        defNames = new HashSet<>();
        urlMap = new HashMap<>();
        urlDefMap = new HashMap<>();
        insideIT = false;
    }

    //----------------------------------------------------------
    // ContentHandler methods
    //----------------------------------------------------------

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

        defNames.clear();
        urlMap.clear();
        urlDefMap.clear();
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

        if (defNames.contains(defName)) {
            System.out.println("Duplicate defName: " + defName);
        }

        if (name.equals("ImageTexture")) {
            insideIT = true;
            fieldValuesStack.push(new HashMap());
            fieldLengthsStack.push(new HashMap());

            if (defName == null) {
                String newName = "TEXAG_" + texNum;
                while(defNames.contains(newName)) {
                    texNum++;
                    newName = "TEXAG_" + texNum;
                }

                defName = newName;

                defNames.add(defName);

                defStack.push(defName);
                nodeStack.push(name);

                // Do not issue startNode
                return;
            }
        }

        if (defName != null)
            defNames.add(defName);

        defStack.push(defName);
        nodeStack.push(name);

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
    @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
    public void endNode() throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.pop();
        String defName = (String) defStack.pop();
        Map<String, Object> fieldValues;
        Map<String, Integer> fieldLengths;

        if (nodeName.equals("ImageTexture")) {
            insideIT = false;
            fieldValues = (Map<String, Object>)fieldValuesStack.pop();
            fieldLengths = (Map<String, Integer>)fieldLengthsStack.pop();

            Object urlVal = fieldValues.get("url");

            if (urlVal == null) {
                sendNode(defName, fieldValues, fieldLengths);
                return;
            }
            String url;

            if (urlVal instanceof String[]) {
                String[] urlArrayVal = (String[]) urlVal;

                if (urlArrayVal.length < 1) {
                    sendNode(defName, fieldValues, fieldLengths);
                    return;
                } else
                    url = urlArrayVal[0];
            } else {
                url = (String) urlVal;
            }

            Map<String, Object> prevFieldValues = urlMap.get(url);

            if (prevFieldValues == null) {
                urlMap.put(url, fieldValues);
                sendNode(defName, fieldValues, fieldLengths);
                return;
            } else {
                // Compare field values
                String st;
                boolean repeatS1 = true;
                boolean repeatT1 = true;
                boolean repeatS2 = true;
                boolean repeatT2 = true;

                st = (String) fieldValues.get("repeatS");
                if (st != null)
                    repeatS1 = (Boolean.valueOf(st));

                st = (String) fieldValues.get("repeatT");
                if (st != null)
                    repeatT1 = (Boolean.valueOf(st));

                st = (String) prevFieldValues.get("repeatS");
                if (st != null)
                    repeatS1 = (Boolean.valueOf(st));

                st = (String) prevFieldValues.get("repeatT");
                if (st != null)
                    repeatT1 = (Boolean.valueOf(st));

                if (repeatS1 == repeatS2 && repeatT1 == repeatT2) {
                    String urlDefName = urlDefMap.get(url);

                    super.useDecl(urlDefName);
                }
                return;
            }
        }

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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {

        if (insideIT)
            fieldStack.push(name);
        else
            super.startField(name);
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
        if (insideIT)
            fieldStack.pop();
        else
            super.useDecl(defName);

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
        if (insideIT)
            fieldStack.pop();
        else
            super.endField();
    }

    //-----------------------------------------------------------------------
    //Methods for interface StringContentHandler
    //-----------------------------------------------------------------------

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
    @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
    public void fieldValue(String[] values) throws SAVException, VRMLException {

        String nodeName = (String) nodeStack.peek();

        if (nodeName.equals("ImageTexture")) {
            String fieldName = (String) fieldStack.pop();

            Map<String, Object> fieldValues = (Map<String, Object>) fieldValuesStack.peek();
            Map<String, Integer> fieldLengths = (Map<String, Integer>) fieldLengthsStack.peek();
            fieldValues.put(fieldName, values);
            fieldLengths.put(fieldName, NO_LENGTH_VALUE);
        } else {
            super.fieldValue(values);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
    public void fieldValue(String[] values, int len)
        throws SAVException, VRMLException {

        String nodeName = (String) nodeStack.peek();

        if (nodeName.equals("ImageTexture")) {
            String fieldName = (String) fieldStack.pop();

            Map<String, Object> fieldValues = (Map<String, Object>) fieldValuesStack.peek();
            Map<String, Integer> fieldLengths = (Map<String, Integer>) fieldLengthsStack.peek();
            fieldValues.put(fieldName, values);
            fieldLengths.put(fieldName, len);
        } else {
            super.fieldValue(values, len);
        }
    }

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
    @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
    public void fieldValue(String value) throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.peek();

        if (nodeName.equals("ImageTexture")) {
            String fieldName = (String) fieldStack.pop();

            Map<String, Object> fieldValues = (Map<String, Object>) fieldValuesStack.peek();
            Map<String, Integer> fieldLengths = (Map<String, Integer>) fieldLengthsStack.peek();
            fieldLengths.put(fieldName, NO_LENGTH_VALUE);
            fieldValues.put(fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }


    //-----------------------------------------------------------------------
    //Local methods
    //-----------------------------------------------------------------------

    /**
     * Send a node to the contentHandler.
     * This map may have extra fields from metadata nodes lost.
     *
     * @param defName The nodes defName.
     * @param fieldValues The nodes field values.
     * @param fieldLengths The nodes array length
     */
    private void sendNode(String defName, Map<String, Object> fieldValues, Map<String, Integer> fieldLengths) {
        Object urlObj = fieldValues.get("url");
        Integer len = fieldLengths.get("url");

        contentHandler.startNode("ImageTexture", defName);

        if (urlObj instanceof String[]) {
            String[] url = (String[]) urlObj;

            if (url.length > 0) {
                urlDefMap.put(url[0], defName);

                contentHandler.startField("url");

                if (Objects.equals(len, NO_LENGTH_VALUE))
                    fieldHandler.setFieldValue("ImageTexture", "url", url);
                else
                    fieldHandler.setFieldValue("ImageTexture", "url", url, url.length);
            }
        } else {
            String url = (String) urlObj;

            if (url != null) {
                urlDefMap.put(url, defName);
                contentHandler.startField("url");
                fieldHandler.setFieldValue("ImageTexture", "url", url);
            }
        }

        String repeatS = (String) fieldValues.get("repeatS");

        if (repeatS != null) {
            contentHandler.startField("repeatS");
            fieldHandler.setFieldValue("ImageTexture", "repeatS", repeatS);
        }

        String repeatT = (String) fieldValues.get("repeatT");

        if (repeatT != null) {
            contentHandler.startField("repeatT");
            fieldHandler.setFieldValue("ImageTexture", "repeatT", repeatT);
        }

        contentHandler.endNode();
    }
}
