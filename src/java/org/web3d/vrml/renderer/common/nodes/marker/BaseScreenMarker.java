/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.marker;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

import org.web3d.vrml.util.URLChecker;

/**
 * Common implementation of a ScreenMarker node.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public abstract class BaseScreenMarker extends AbstractNode
    implements VRMLWorldRootChildNodeType, VRMLSingleExternalNodeType {

    // Field index constants

    /** The field index for enabled */
    protected static final int FIELD_ENABLED = LAST_NODE_INDEX + 1;

    /** The field index for width */
    protected static final int FIELD_WIDTH = LAST_NODE_INDEX + 2;

    /** The field index for height */
    protected static final int FIELD_HEIGHT = LAST_NODE_INDEX + 3;

    /** The field index for represents */
    protected static final int FIELD_REPRESENTS = LAST_NODE_INDEX + 4;

    /** The field index for iconUrl */
    protected static final int FIELD_ICON_URL = LAST_NODE_INDEX + 5;

    /** The last field index used by this class */
    protected static final int LAST_SCREENMARKER_INDEX = FIELD_ICON_URL;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_SCREENMARKER_INDEX + 1;

    /** Message for when the proto is not a Child Node */
    protected static final String BAD_PROTO_MSG =
        "Proto does not describe an X3DChildNode";

    /** Message for when the node in setValue() is not a Child Node */
    protected static final String BAD_NODE_MSG =
        "Node is not an X3DChildNode";

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** URL Load State */
    protected int loadState;

    /** base url */
    private String worldURL;

    /** List of those who want to know about Url changes.  Likely 1 */
    private List<VRMLUrlListener> urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private List<VRMLContentStateListener> contentListeners;

    /** The URI of the finally loaded texture. Null if not loaded yet */
    protected String loadedURI;

    // The field values

    /** The value of the enabled field */
    protected boolean vfEnabled;

    /** The value of the width field */
    protected float vfWidth;

    /** The value of the height field */
    protected float vfHeight;

    /** exposedField MFString url [] */
    protected String[] vfURL;

    /** The value of the "represents" SFNode exposedField */
    protected VRMLChildNodeType vfRepresents;

    /** The proto representation of the "represents" field */
    protected VRMLProtoInstance pRepresents;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[]{
            FIELD_METADATA,
            FIELD_REPRESENTS,
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
		fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "enabled");
		fieldDecl[FIELD_WIDTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "enabled");
		fieldDecl[FIELD_HEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "enabled");
        fieldDecl[FIELD_ICON_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "iconUrl");
        fieldDecl[FIELD_REPRESENTS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFNode",
                                    "represents");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = FIELD_ENABLED;
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        idx = FIELD_WIDTH;
        fieldMap.put("width", idx);
        fieldMap.put("set_width", idx);
        fieldMap.put("width_changed", idx);

        idx = FIELD_HEIGHT;
        fieldMap.put("height", idx);
        fieldMap.put("set_height", idx);
        fieldMap.put("height_changed", idx);

        idx = FIELD_ICON_URL;
        fieldMap.put("iconUrl", idx);
        fieldMap.put("set_iconUrl", idx);
        fieldMap.put("iconUrl_changed", idx);

        fieldMap.put("represents", FIELD_REPRESENTS);
    }

    /**
     * Construct a new default ScreenMarker object
     */
    protected BaseScreenMarker() {
        super("ScreenMarker");

        contentListeners = new ArrayList<>();
        urlListeners = new ArrayList<>(1);

        hasChanged = new boolean[NUM_FIELDS];
        vfURL = FieldConstants.EMPTY_MFSTRING;
		vfWidth = 1;
		vfHeight = 1;
		vfEnabled = true;

        loadState = NOT_LOADED;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseScreenMarker(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("iconUrl");

            VRMLFieldData field = node.getFieldValue(index);

            if (field.numElements != 0) {
                vfURL = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfURL, 0,
                  field.numElements);
            }

            index = node.getFieldIndex("enabled");
            field = node.getFieldValue(index);
            vfEnabled = field.booleanValue;

            index = node.getFieldIndex("width");
            field = node.getFieldValue(index);
            vfWidth = field.floatValue;

            index = node.getFieldIndex("height");
            field = node.getFieldValue(index);
            vfHeight = field.floatValue;

            // ? represents field ?

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined  by VRMLExternalNodeType
    //----------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    @Override
    public int getLoadState() {
        return(loadState);
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    @Override
    public void setLoadState(int state) {
        switch(state) {
            case VRMLSingleExternalNodeType.NOT_LOADED:
                break;
            case 2 :
//                System.out.println("Loading: " + loadedURI);
                break;

            case 3 :
//                System.out.println("Loading complete: " + loadedURI);
                break;

            case 4 :
                if(loadedURI != null)
                    System.out.println("Loading failed: " + loadedURI);
                break;

            default :
                System.out.println("Unknown state: " + state);
        }

        loadState = state;

        // Only file complete events when texture is ready to use
        if (state != VRMLSingleExternalNodeType.LOAD_COMPLETE) {
            fireContentStateChanged();
        }
    }

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    @Override
    public void setWorldUrl(String url) {
        if ((url == null) || (url.length() == 0)) {
            return;
        }

        // check for a trailing slash. If it doesn't have one, append it.
        if (url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        worldURL = url;
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    @Override
    public String getWorldUrl() {
        return(worldURL);
    }

    /**
     * Sets the URL to a new value.  We will load only one
     * of these URL's.  The list provides alternates.
     *
     * @param newURL Array of candidate URL strings
     * @param numValid The number of valid values to copy from the array
     */
    @Override
    public void setUrl(String[] newURL, int numValid) {
        if (numValid > 0) {
            if (worldURL != null) {
                vfURL = URLChecker.checkURLs(worldURL, newURL, false);
            } else {
                vfURL = newURL;
            }
        } else {
            vfURL = FieldConstants.EMPTY_MFSTRING;
        }

        if (!inSetup) {
            hasChanged[FIELD_ICON_URL] = true;
            fireFieldChanged(FIELD_ICON_URL);
        }
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    @Override
    public String[] getUrl() {
        if(worldURL != null) {
            URLChecker.checkURLsInPlace(worldURL, vfURL, false);
        }

        return(vfURL);
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    @Override
    public boolean checkValidContentType(String mimetype) {
        if (!mimetype.contains("image")) {
            return(false);
        }
        return(true);
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArgumentException The content object is not supported
     */
    @Override
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param uri The URI used to load this content
     */
    @Override
    public void setLoadedURI(String uri) {
        loadedURI = uri;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param ul The listener instance to add
     */
    @Override
    public void addUrlListener(VRMLUrlListener ul) {
        if (!urlListeners.contains(ul)) {
            urlListeners.add(ul);
        }
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param ul The listener to be removed
     */
    @Override
    public void removeUrlListener(VRMLUrlListener ul) {
        urlListeners.remove(ul);
    }

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    @Override
    public void addContentStateListener(VRMLContentStateListener l) {
        if(!contentListeners.contains(l)) {
            contentListeners.add(l);
        }
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    @Override
    public void removeContentStateListener(VRMLContentStateListener l) {
        contentListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return((index == null) ? -1 : index);
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return(nodeFields);
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if((index < 0) || (index > LAST_SCREENMARKER_INDEX)) {
            return(null);
        }

        return(fieldDecl[index]);
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return(fieldDecl.length);
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return(TypeConstants.OverlayNodeType);
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if (!inSetup) {
            return;
        }

        super.setupFinished();
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch (index) {
		case FIELD_ENABLED:
			fieldData.clear();
			fieldData.booleanValue = vfEnabled;
			fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
			break;

		case FIELD_WIDTH:
			fieldData.clear();
			fieldData.floatValue = vfWidth;
			fieldData.dataType = VRMLFieldData.FLOAT_DATA;
			break;

		case FIELD_HEIGHT:
			fieldData.clear();
			fieldData.floatValue = vfHeight;
			fieldData.dataType = VRMLFieldData.FLOAT_DATA;
			break;

        case FIELD_ICON_URL:
            fieldData.clear();
            fieldData.stringArrayValues = vfURL;
            fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
            fieldData.numElements = vfURL.length;
            break;

        case FIELD_REPRESENTS:
            fieldData.clear();
            if (pRepresents != null) {
                fieldData.nodeValue = pRepresents;
            } else {
                fieldData.nodeValue = vfRepresents;
            }
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        default:
            return(super.getFieldValue(index));
        }

        return(fieldData);
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch (srcIndex) {
			case FIELD_ENABLED:
				destNode.setValue(destIndex, vfEnabled);
				break;
			case FIELD_WIDTH:
				destNode.setValue(destIndex, vfWidth);
				break;
			case FIELD_HEIGHT:
				destNode.setValue(destIndex, vfHeight);
				break;
            case FIELD_ICON_URL:
                destNode.setValue(destIndex, vfURL, vfURL.length);
                break;

            case FIELD_REPRESENTS:
                if (pRepresents != null) {
                    destNode.setValue(destIndex, pRepresents);
                } else {
                    destNode.setValue(destIndex, vfRepresents);
                }
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                               ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a boolean. This is
     * be used to set SFBool field types isActive, enabled and loop.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

		switch(index) {
		case FIELD_ENABLED:
			setEnabled(value);
			break;

		default :
			super.setValue(index, value);
		}
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
		case FIELD_WIDTH:
			setWidth(value);
			break;

		case FIELD_HEIGHT:
			setHeight(value);
			break;

		default:
			super.setValue(index, value);
		}
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
        case FIELD_ICON_URL:
            vfURL = new String[1];
            vfURL[0] = value;

            if (!inSetup) {
                loadState = NOT_LOADED;
                fireUrlChanged(index);
                hasChanged[FIELD_ICON_URL] = true;
                fireFieldChanged(FIELD_ICON_URL);
            }
            break;

        default:
            super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
        case FIELD_ICON_URL :
            vfURL = new String[numValid];

            System.arraycopy(value, 0, vfURL, 0, numValid);

            if (!inSetup) {
                loadState = NOT_LOADED;
                fireUrlChanged(index);
                hasChanged[FIELD_ICON_URL] = true;
                fireFieldChanged(FIELD_ICON_URL);
            }

            break;

        default:
            super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param node The new node to use
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType node)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
        case FIELD_REPRESENTS:
            setRepresents(node);
            break;

        default:
            super.setValue(index, node);
        }

        if (!inSetup) {
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the sensor enabled or disabled.
     *
     * @param enabled The new enabled value
     */
	protected void setEnabled(boolean enabled) {
		vfEnabled = enabled;
	}

    /**
     * Set the image width scale.
     *
     * @param width The image width scale.
     */
	protected void setWidth(float width) {
		vfWidth = width;
	}

    /**
     * Set the image height scale.
     *
     * @param height The image height scale.
     */
	protected void setHeight(float height) {
		vfHeight = height;
	}

    /**
     * Set the node that should be used for the represents field. Setting a
     * value of null will clear the current represents value.
     *
     * @param node The new node instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setRepresents(VRMLNodeType node)
        throws InvalidFieldValueException {

        if (node == null) {
            vfRepresents = null;
            pRepresents = null;
        } else if (node instanceof VRMLProtoInstance) {
            VRMLNodeType new_node = ((VRMLProtoInstance)node).getImplementationNode();
            if (new_node instanceof VRMLChildNodeType) {
                vfRepresents = (VRMLChildNodeType)new_node;
                pRepresents = (VRMLProtoInstance)node;
            } else {
                throw new InvalidFieldValueException(BAD_PROTO_MSG);
            }
        } else if (node instanceof VRMLChildNodeType) {
            vfRepresents = (VRMLChildNodeType)node;
            pRepresents = null;
        } else {
            throw new InvalidFieldValueException(BAD_NODE_MSG);
        }

        if(!inSetup) {
            hasChanged[FIELD_REPRESENTS] = true;
            fireFieldChanged(FIELD_REPRESENTS);
        }
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged(int index) {

        // Notify listeners of new value
        for(VRMLUrlListener ul : urlListeners) {
            ul.urlChanged(this, index);
        }
    }

    /**
     * Send a notification to the registered listeners that the content state
     * has been changed. If no listeners have been registered, then this does
     * nothing, so always call it regardless.
     */
    protected void fireContentStateChanged() {

        // Notify listeners of new value
        for(VRMLContentStateListener csl : contentListeners) {
            csl.contentStateChanged(this, FIELD_ICON_URL, loadState);
        }
    }
}
