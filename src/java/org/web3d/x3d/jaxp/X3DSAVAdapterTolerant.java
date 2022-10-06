/*****************************************************************************
 *                        Shapeways Copyright (c) 20012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.jaxp;

// External imports
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

// Local imports
import org.web3d.util.I18nUtils;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.lang.InvalidFieldException;

/**
 * Interface adapter between XML input from a SAX source to the SAV source
 * used by the other parsers.
 * <p>
 * The implementation is a simplistic way for quickly getting XML content into
 * the normal VRML input sources by adapting the SAX feeds to SAV feeds.
 *
 * This implementation of the SAVAdapter is tolerant of incorrect files.  It will attempt
 * to correct most common errors found in user content.
 *
 * @author Alan Hudson
 * @version $Revision: 1.48 $
 */
public class X3DSAVAdapterTolerant extends X3DSAVAdapter
    implements LexicalHandler, org.xml.sax.ContentHandler {

    /** Mapping of element names strings to constants */
    protected static final Map<String, Integer> elementLCMap;

    static {
        elementLCMap = new HashMap<>(elementMap.size());
        for(Map.Entry<String, Integer> entry : elementMap.entrySet()) {
            elementLCMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * Construct a default instance of this class.
     */
    public X3DSAVAdapterTolerant() {
        super();
    }

    //----------------------------------------------------------
    // Methods defined by LexicalHandler
    //----------------------------------------------------------

    /**
     * Report the start of DTD declarations, if any. There are two
     * formal doctypes supported. The first type is the required type by the
     * X3D specification:
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC &quot;-//Web3D//DTD X3D 3.0//EN&quot;
     *    &quot;http://www.web3d.org/specification/x3d/x3d-3_0.dtd&quot;&gt;
     * </pre>
     *
     * The second type are the transitional DOCTYPEs used during development
     * of the X3D specification.
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC
     *    &quot;http://www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd&quot;
     *    &quot;/www.web3D.org/TaskGroups/x3d/translation/x3d-compact.dtd&quot;&gt;
     * </pre>
     *
     * <pre>
     *   &lt;!DOCTYPE X3D PUBLIC
     *    &quot;http://www.web3d.org/specifications/x3d-3.0.dtd&quot;
     *    &quot;/www.web3d.org/TaskGroups/x3d/translation/x3d-3.0.dtd&quot;&gt;
     * </pre>

     * It is advised that these forms are never used, it is for transitional
     * content only. When this header is detected, a warning message is
     * generated on the output, but parsing continues normally.
     *
     * @param name The DTD name string
     * @param publicId The Public ID used for the content
     * @param systemId The system ID used for the content
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {

        if(!name.equals(X3DConstants.DTD_NAME)) {
            errorReporter.warningReport(I18nUtils.EXT_MSG + INVALID_DTD_NAME_MSG + name, null);
        }

        boolean warned = false;

        if(allowedPublicIDs.contains(publicId)) {
            if(null != publicId) switch (publicId) {
                case X3DConstants.OLD_PUBLIC_ID:
                    warned = true;
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
                    break;
                case X3DConstants.TRANS_PUBLIC_ID:
                    warned = true;
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
                    break;
            }

            if(allowedSystemIDs.contains(systemId)) {
                if(X3DConstants.OLD_SYSTEM_ID.equals(systemId) && !warned)
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
                else if(X3DConstants.TRANS_SYSTEM_ID.equals(systemId) && !warned)
                    errorReporter.warningReport(TRANSITIONAL_HEADER_MSG, null);
            } else {
                errorReporter.warningReport(I18nUtils.EXT_MSG + INVALID_SYSTEM_ID_MSG + ": " + systemId, null);
            }
            Float version = specVersionMap.get(publicId);
            versionString = specStringMap.get(version);

        } else {
            errorReporter.warningReport(UNKNOWN_DTD_MSG, null);

            // Can't error out as a custom DTD could be used
            //
            // Spec language: A DOCTYPE or schema declaration is optional. The
            // reference DTD and location is specified in this document. To
            // allow for extensible tag sets (supersets of the base specification),
            // authors may point to a document definition other than the ones
            // listed in this specification. However, the alternate definition
            // shall specify an equivalent document to the base X3D DTD/Schema
            // (i.e., the tag instances shall look exactly the same regardless
            // of the source DTD/s chema).

            //throw new SAXException(INVALID_PUBLIC_ID_MSG);

            // No clue, will need to wait for X3D tag
            versionString = "V3.1";
        }

        // So everything is OK, now find the spec version and load the right
        // config file.
    }

    /**
     * Start the processing of a new element with the given collection of
     * attribute information.
     *
     * @param namespace The namespace for the element. Null if not used
     * @param localName The local name of the element to create
     * @param qName The qualified name of the element including prefix
     * @param attribs The collection of attributes to use
     * @throws SAXException The element can't be found in the underlying
     *     factory
     */
    @Override
    public void startElement(String namespace,
                             String localName,
                             String qName,
                             Attributes attribs)
        throws SAXException {


        if(useIsCurrent)
            throw new SAXException(USE_WITH_KIDS_MSG);

        int colon_idx = qName.indexOf(':');
        if(colon_idx != -1) {
            if(checkForX3DNamespace && qName.startsWith(x3dNamespaceId))
                qName = qName.substring(x3dNamespaceId.length());
            else {
                String ns = qName.substring(0, colon_idx);

                if(namespacePrefixes.contains(ns))
                    return;
            }
        }

        Integer el_type = elementMap.get(qName);
        if (el_type == null) {
            el_type = elementMap.get(qName.toLowerCase());
        }

        String value;
        String field_name;

        if(el_type == null) {
            if(checkForSceneTag) {
                errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                checkForSceneTag = false;
            }
            // We're obviously in a new node, so go look for
            // the container field attribute to do a "startField"
            // call. However, no point doing anything if we don't have a
            // content handler to call. Need to weed out the one case were
            // we are the root node of a proto declaration body.

            if(fieldDeclDepth != 0) {
                field_name = attribs.getValue(CONTAINER_ATTR);

                // No container field name defined? Look one up. If that
                // fails, guess and put in "children" since that is the
                // most commonly used default.
                if(field_name == null) {
                    field_name = containerFields.getProperty(qName);

                    if(field_name == null)
                        field_name = "children";
                }

                if(contentHandler != null) {
                    try {
                        contentHandler.startField(field_name);
                    } catch(InvalidFieldException ife) {
                       errorReporter.errorReport("No field: " + field_name +
                                                  " for: " + qName, ife);
                    }
                }
            }

            value = attribs.getValue(USE_ATTR);
            fieldDeclDepth++;

            if(value != null) {
                if(contentHandler != null)
                    contentHandler.useDecl(value);
                useIsCurrent = true;
            } else {
                value = attribs.getValue(DEF_ATTR);

                contentHandler.startNode(qName, value);
            }

            // now process all the attributes!
            int num_attr = attribs.getLength();
            for(int i = 0; !useIsCurrent && i < num_attr; i++) {

                String name = attribs.getQName(i);

                // Is it an attribute prefixed by one of the namespaces?
                // if so, then ignore it and go to the next attribute
                colon_idx = name.indexOf(':');
                if(colon_idx != -1) {
                     String ns = name.substring(0, colon_idx);
                     if(namespacePrefixes.contains(ns))
                        continue;
                }

                // Check to see if the attribute is one of the reserved
                // set. If so, treat separately from the normal field
                // processing.
                //
                // If we don't have a string content handler, then do not
                // pass this information along. Ideally we want this to
                // automatically convert to the binary form if we have a binary
                // content handler, but that's too much work for me right now
                // trying to get this darn project finished.... JC
                //
                if(contentHandler instanceof StringContentHandler) {
                    StringContentHandler sch =
                        (StringContentHandler)contentHandler;

                    Integer id_int = attributeMap.get(name);
                    if(id_int == null && contentHandler != null) {
                        String field_value = attribs.getValue(i);

                        // zero length string? Could well be an eventOut or
                        // eventIn, so ignore it.
                        if(field_value.length() == 0)
                            continue;

                        sch.startField(name);
                        sch.fieldValue(field_value);
                    }
                }
            }

            return;
        }

        switch(el_type) {
            case X3D_TAG:
                value = attribs.getValue(VERSION_ATTR);

                if (value != null)
                    versionString = "V" + value;
                else
                    value = versionString.substring(1);

                loadContainerProperties(Float.parseFloat(value));

                if(contentHandler != null) {
                    contentHandler.startDocument(fullURL,
                                                 worldURL,
                                                 XML_ENCODING,
                                                 "#X3D",
                                                 versionString,
                                                 null);
                }

                value = attribs.getValue("profile");

                if(value == null) {
                    errorReporter.warningReport(NO_PROFILE_MSG + " Using Immersive Profile", null);
                    value = "Immersive";
                }

                if(contentHandler != null)
                    contentHandler.profileDecl(value);

                // Setup cData
                if(overrideLex)
                    characterDataBuffer.append("\"");

                useIsCurrent = false;
                checkForSceneTag = true;

                // Now go looking for anything that starts with a xmlns:
                int num_attribs = attribs.getLength();
                for(int i = 0; i < num_attribs; i++) {
                    String attrib = attribs.getQName(i);
                    if(attrib.startsWith("xmlns:")) {
                        String space = attrib.substring(6);
                        // Ignore the Schema definition
                        if(!space.equals("xsd")) {
                            namespacePrefixes.add(space);

                            String ns_uri = attribs.getValue(i);

                            // TODO:
                            // This fixed URI right now is subject to
                            // specification. We have a single fixed URI, but
                            // this may be changed by the X3D spec process to
                            // be something that is relative to the spec version
                            // of the containing document (eg the DTD).
                            if(ns_uri.equals(X3DConstants.X3D_NAMESPACE_URI)) {
                                x3dNamespaceId = space;
                                checkForX3DNamespace = true;
                            }

                        }
                    } else if(attrib.equals("xsd:noNamespaceSchemaLocation")) {
                        // If there is a pre-defined non-namespace schema, and
                        // it is not the X3D schema, then we want to automatically
                        // turn on namespace checks for all elements.
                        String ns_val = attribs.getValue(i);
                        checkForX3DNamespace = !systemSchemaIDs.contains(ns_val);
                    }
                }

                break;

            case HEAD_TAG:
                // do nothing
                break;

            case COMPONENT_TAG:
                value = attribs.getValue(NAME_ATTR) + ':' +
                        attribs.getValue("level");
                if(contentHandler != null)
                    contentHandler.componentDecl(value);
                break;

            case SCENE_TAG:
                checkForSceneTag = false;
                break;

            case PROTO_DECL_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(protoHandler != null)
                    protoHandler.startProtoDecl(attribs.getValue(NAME_ATTR));

                scriptFlagStack.push(false);
                inScript = false;
                break;

            case PROTO_INTERFACE_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                // Don't do anything from here. We've already started the
                // proto decl handling in the PROTO_DECL_TAG.
                break;

            case PROTO_BODY_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(protoHandler != null) {
                    protoHandler.endProtoDecl();
                    protoHandler.startProtoBody();
                }

                break;

            case EXTERNPROTO_DECL_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(protoHandler != null) {
                    value = attribs.getValue(NAME_ATTR);
                    protoHandler.startExternProtoDecl(value);

                    epUrl = attribs.getValue("url");

                }

                scriptFlagStack.push(false);
                inScript = false;
                break;

            case IS_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                break;

            case CONNECT_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(contentHandler != null)
                    contentHandler.startField(attribs.getValue("nodeField"));

                if(protoHandler != null)
                    protoHandler.protoIsDecl(attribs.getValue("protoField"));
                break;

            case FIELD_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                field_name = attribs.getValue(NAME_ATTR);
                String field_type = attribs.getValue("type");

                int access_type =
                    processFieldAccess(attribs.getValue("accessType"),
                                       field_name);

                // perhaps this should do some prior checking of the
                // access type to make sure that the user don't do anything
                // dumb like set a value for eventIn/Out.
                boolean is_used = false;
                depthCountStack.push(fieldDeclDepth);
                fieldDeclDepth = 0;

                if((value = attribs.getValue("USE")) != null)
                    is_used = true;
                else
                    value = attribs.getValue(VALUE_ATTR);

                if(inScript) {
                    if(is_used) {
                        if(scriptHandler != null) {
                            scriptHandler.scriptFieldDecl(access_type,
                                                          field_type,
                                                          field_name,
                                                          null);
                        }

                        if(contentHandler != null)
                            contentHandler.useDecl(value);
                    } else {
                        if (value != null && value.length() == 0) {
                            value = null;
                        }

                        if(scriptHandler != null)
                            scriptHandler.scriptFieldDecl(access_type,
                                                          field_type,
                                                          field_name,
                                                          value);
                    }
                } else {
                    if(is_used) {
                        if(protoHandler != null) {
                            protoHandler.protoFieldDecl(access_type,
                                                        field_type,
                                                        field_name,
                                                        null);
                        }

                        if(contentHandler != null)
                            contentHandler.useDecl(value);
                    } else {
                        if (value != null && value.length() == 0) {
                            value = null;
                        }

                        if(protoHandler != null)
                            protoHandler.protoFieldDecl(access_type,
                                                        field_type,
                                                        field_name,
                                                        value);
                    }
                }
                break;

            case META_TAG:
                if(contentHandler != null) {
                    String key = attribs.getValue("key");
                    String content = attribs.getValue("content");

                    if (content == null) {
                        // Support old incorrect style
                        content = key;
                    }
                    contentHandler.metaDecl(attribs.getValue(NAME_ATTR),
                                            content);
                }
                break;

            case PROTO_INSTANCE_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(contentHandler != null) {
                    field_name = attribs.getValue(CONTAINER_ATTR);

                    // No container field name defined? Look one up. If that
                    // fails, guess and put in "children" since that is the
                    // most commonly used default.
                    if(field_name == null) {
                        field_name = containerFields.getProperty(localName);

                        if(field_name == null)
                            field_name = "children";
                    }

                    if(fieldDeclDepth != 0) {
                        contentHandler.startField(field_name);
                    }

                    fieldDeclDepth++;
                    contentHandler.startNode(attribs.getValue(NAME_ATTR),
                                             attribs.getValue(DEF_ATTR));
                }

                inScript = false;
                break;

            case IMPORT_TAG:
                if(contentHandler != null)
                    contentHandler.importDecl(attribs.getValue("inlineDEF"),
                                              attribs.getValue("exportedDEF"),
                                              attribs.getValue(AS_ATTR));
                break;

            case EXPORT_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(contentHandler != null)
                    contentHandler.exportDecl(attribs.getValue("localDEF"),
                                              attribs.getValue(AS_ATTR));
                break;

            case ROUTE_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                if(routeHandler != null)
                    routeHandler.routeDecl(attribs.getValue("fromNode"),
                                           attribs.getValue("fromField"),
                                           attribs.getValue("toNode"),
                                           attribs.getValue("toField"));
                break;

            case SCRIPT_TAG:
                if(checkForSceneTag) {
                    errorReporter.warningReport(I18nUtils.EXT_MSG + NO_SCENE_TAG_MSG, null);
                    checkForSceneTag = false;
                }

                // Force an automatic startScript in case parser doesn't
                startScript();

                scriptFlagStack.push(true);
                inScript = true;
                is_used = false;

                // Clear any CData garbage
                characterDataBuffer.setLength(0);
                characterDataBuffer.append('\"');

                field_name = attribs.getValue(CONTAINER_ATTR);

                // No container field name defined? Look one up. If that
                // fails, guess and put in "children" since that is the
                // most commonly used default.
                if(field_name == null) {
                    field_name = containerFields.getProperty(localName);

                    if(field_name == null)
                        field_name = "children";
                }

                if(fieldDeclDepth != 0) {
                    if(contentHandler != null)
                        contentHandler.startField(field_name);
                }

                fieldDeclDepth++;
                value = attribs.getValue(USE_ATTR);

                if(value != null) {
                    if(contentHandler != null)
                        contentHandler.useDecl(value);
                    is_used = true;
                    useIsCurrent = true;
                } else {
                    value = attribs.getValue(DEF_ATTR);
                    is_used = false;
                    if(contentHandler != null)
                        contentHandler.startNode(qName, value);
                }

                if(scriptHandler != null && !is_used)
                    scriptHandler.startScriptDecl();

                // The definite fields of a script need to be passed through.
                // Only pass through if you're not in a USE though.
                if(!is_used && contentHandler != null) {
                    value = attribs.getValue("url");
                    if(value != null) {
                        scriptUrlStack.push(true);
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("url");
                            sch.fieldValue(value);
                        }
                    } else {
                        scriptUrlStack.push(false);
                    }

                    value = attribs.getValue("mustEvaluate");
                    if(value != null) {
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("mustEvaluate");
                            sch.fieldValue(value);
                        }
                    }

                    value = attribs.getValue("directOutput");
                    if(value != null) {
                        if(contentHandler instanceof StringContentHandler) {
                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.startField("directOutput");
                            sch.fieldValue(value);
                        }
                    }
                }

                break;

            case FIELD_VALUE_TAG:
                if(contentHandler != null) {
                    contentHandler.startField(attribs.getValue(NAME_ATTR));

                    value = attribs.getValue(USE_ATTR);
                    if(value != null) {
                        contentHandler.useDecl(value);
                    } else {
                        value = attribs.getValue(VALUE_ATTR);
                        if((value != null && value.length() > 0) &&
                           (contentHandler instanceof StringContentHandler)) {

                            StringContentHandler sch =
                                (StringContentHandler)contentHandler;
                            sch.fieldValue(value);
                        }

                        // Reset the character buffer regardless. If CDATA
                        // is provided, it always overrides the attribute value
                        characterDataBuffer.setLength(0);
                    }
                }

                declDepthStack.push(fieldDeclDepth);
                fieldDeclDepth = 0;
                break;

            default:
                errorReporter.errorReport(UNKNOWN_ELEMENT_MSG + qName,
                                          null);
        }
    }

    /**
     * End the element processing.
     *
     * @param namespace The namespace for the element. Null if not used
     * @param name The local name of the element to create
     * @param qName The qualified name of the element including prefix
     * @throws SAXException Not thrown in this implementation
     */
    @Override
    public void endElement(String namespace, String name, String qName)
        throws SAXException {

        int colon_idx = qName.indexOf(':');
        if(colon_idx != -1) {
            if(checkForX3DNamespace && qName.startsWith(x3dNamespaceId))
                qName = qName.substring(x3dNamespaceId.length());
            else {
                String ns = qName.substring(0, colon_idx);

                if(namespacePrefixes.contains(ns))
                    return;
            }
        }

        Integer el_type = elementMap.get(qName);
        if (el_type == null) {
            el_type = elementLCMap.get(qName.toLowerCase());
        }

        String value;

        // Handle the case where we don't receive start/endCData events

        if(overrideLex && characterDataBuffer.length() > 1) {
            if(inScript && (contentHandler != null)) {
                characterDataBuffer.append('\"');

                // TODO: Cover up inScript bug?  Only set URL's if they have a
                // value. Sometimes we are trying to set URL on something
                // other then a script
                String url = characterDataBuffer.toString();

                int len = url.length();
                url = url.substring(1,len-2);
                url = url.trim();

                if (url.length() > 1) {
                    url = "\"" + url + "\"";
                    if(contentHandler instanceof StringContentHandler) {
                        StringContentHandler sch =
                            (StringContentHandler)contentHandler;
                        sch.startField("url");
                        sch.fieldValue(characterDataBuffer.toString());
                    }
                }

                characterDataBuffer.setLength(0);
                characterDataBuffer.append('\"');
            }
        }

        if(el_type == null) {
            fieldDeclDepth--;

            if(contentHandler != null) {
                if(useIsCurrent) {
//                    contentHandler.endField();
                    useIsCurrent = false;
                } else {
                    contentHandler.endNode();
                }
            }
            return;
        }

        switch(el_type) {

            case PROTO_DECL_TAG:
                inScript = scriptFlagStack.pop();
                break;

            case PROTO_INTERFACE_TAG:
                break;

            case PROTO_BODY_TAG:
                if(protoHandler != null)
                    protoHandler.endProtoBody();
                break;

            case EXTERNPROTO_DECL_TAG:
                if(protoHandler != null) {
                    // Make sure externProtoURI call is after proto decl is done

                    protoHandler.endExternProtoDecl();

                    // This need to be parsed.  Do we already have a MFString
                    // parsing service available?
                    // TODO; Generates garbage, might change to String.split
                    // after 1.4 is required

                    StringTokenizer st = new StringTokenizer(epUrl, "\"");
                    ArrayList<String> list = new ArrayList<>();

                    while(st.hasMoreTokens()) {
                        list.add(st.nextToken());
                    }

                    int len = list.size();
                    String values[] = new String[len];

                    for(int i=0; i < len; i++) {
                        values[i] = list.get(i);
                    }

                    protoHandler.externProtoURI(values);
                }

                inScript = scriptFlagStack.pop();
                break;

            case IS_TAG:
                break;

            case CONNECT_TAG:
                break;

            case FIELD_TAG:
                fieldDeclDepth = depthCountStack.pop();
                break;

            case PROTO_INSTANCE_TAG:

                if(contentHandler != null)
                    contentHandler.endNode();

                // TODO: This seems dodgy
                inScript = true;
                fieldDeclDepth--;

                break;

            case SCRIPT_TAG:

                if (!useIsCurrent) {
                    boolean urlValue = scriptUrlStack.pop();

                    // Ignore CDATA if url provided
                    if (!urlValue)
                        endScript();

                    if(scriptHandler != null)
                        scriptHandler.endScriptDecl();
                }
                inScript = scriptFlagStack.pop();

                fieldDeclDepth--;

                if(contentHandler != null) {
                    if(useIsCurrent) {
//                        contentHandler.endField();
                        useIsCurrent = false;
                    } else {
                        contentHandler.endNode();
                    }
                }
                break;

            case X3D_TAG:
                if(overrideLex) {
                    if(contentHandler != null)
                        contentHandler.endDocument();
                }
                break;

            case HEAD_TAG:
                checkForSceneTag = true;
                break;

            // do nothing with these elements.
            case COMPONENT_TAG:
            case SCENE_TAG:
            case META_TAG:
            case IMPORT_TAG:
            case EXPORT_TAG:
            case ROUTE_TAG:
                break;

            case FIELD_VALUE_TAG:
                // Did we get a field value as CDATA? If so, pass that along
                // to the field value now.
                //
                // NOTE: This should only be allowed to happen when the spec
                // version is 3.2 or later as earlier encodings did not
                // permit this change.
                if(characterDataBuffer.length() > 0) {
                    value = characterDataBuffer.toString();
                    if(contentHandler instanceof StringContentHandler) {
                        StringContentHandler sch =
                            (StringContentHandler)contentHandler;
                        sch.fieldValue(value);
                    }
                }

                fieldDeclDepth = declDepthStack.pop();
                break;

            default:
                errorReporter.errorReport(UNKNOWN_ELEMENT_MSG + qName,
                                          null);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

}
