/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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
import java.util.Arrays;

import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.vrml.lang.VRMLException;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.parser.VRMLFieldReader;

import org.web3d.parser.DefaultFieldParserFactory;

import xj3d.filter.AbstractFilter;
import xj3d.filter.FieldValueHandler;

/**
 * A filter which changes the position and orientation of a viewpoint.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class ModifyViewpointFilter extends AbstractFilter {

    /** The viewpoint position argument option identifier */
    private static final String POSITION = "-location";

    /** The viewpoint orientation argument option identifier */
    private static final String ORIENTATION = "-orientation";

    /** The viewpoint type argument option identifier */
    private static final String TYPE = "-type";

    /**
     * The viewpoint type argument option value indicating that
     * the default viewpoint should be modified
     */
    private static final String DEFAULT = "default";

    /**
     * The viewpoint type argument option value indicating that
     * the icon viewpoint should be modified
     */
    private static final String ICON = "icon";

    /** The logging identifier of this app */
    private static final String LOG_NAME = "ModifyViewpointFilter";

    /** The def name of the icon viewpoint. */
    private static final String ICON_DEF_NAME = "ICON_VIEWPOINT";

    /** Type identifier for modifying the icon viewpoint */
    private static final int ICON_TYPE = 1;

    /** Type identifier for modifying the default viewpoint */
    private static final int DEFAULT_TYPE = 2;

    /** Flag used during parsing to determining the viewpoint type to modify */
    private int typeFlag;

    /** Flag used during parsing indicating that the position value should be modified */
    private boolean modifyPosition;

    /** Flag used during parsing indicating that the position field has started */
    private boolean modifyPositionInProgress;

    /** Flag used during parsing indicating that the position value has been modified */
    private boolean modifyPositionComplete;

    /** Flag used during parsing indicating that the orientation value should be modified */
    private boolean modifyOrientation;

    /** Flag used during parsing indicating that the orientation field has started */
    private boolean modifyOrientationInProgress;

    /** Flag used during parsing indicating that the orientation value has been modified */
    private boolean modifyOrientationComplete;

    /** Flag used during parsing indicating that the viewpoint to modify has
    * been identified and the fields should be processed. */
    private boolean modifyInProgress;

    /** Flag used during parsing indicating that the modification has been completed */
    private boolean modifyComplete;

    /** Data for the position value */
    private float[] positionData;

    /** Data for the orientation value */
    private float[] orientationData;

    /** Default Constructor */
    public ModifyViewpointFilter() {

        typeFlag = -1;

        modifyPosition = false;
        modifyOrientation = false;
        modifyComplete = false;
    }

    //----------------------------------------------------------
    //  Methods defined by AbstractFilter
    //----------------------------------------------------------

    /**
     * Parse and validate the argument parameters.
     *
     * @param arg The array of argument parameters.
     */
    @Override
    public void setArguments(String[] arg) {

        int argIndex = -1;
        String type_string = null;
        String[] position_val = null;
        String[] orientation_val = null;

        //////////////////////////////////////////////////////////////////////
        // parse the arguments
        for (int i = 0; i < arg.length; i++) {
            String argument = arg[i];
            if (argument.startsWith("-")) {
                try {
                    switch (argument) {
                        case TYPE:
                            type_string = arg[i+1];
                            argIndex = i+1;
                            break;
                        case POSITION:
                            position_val = new String[3];
                            position_val[0] = arg[i+1];
                            position_val[1] = arg[i+2];
                            position_val[2] = arg[i+3];
                            argIndex = i+3;
                            break;
                        case ORIENTATION:
                            orientation_val = new String[4];
                            orientation_val[0] = arg[i+1];
                            orientation_val[1] = arg[i+2];
                            orientation_val[2] = arg[i+3];
                            orientation_val[3] = arg[i+4];
                            argIndex = i+4;
                            break;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        LOG_NAME +": Error parsing filter arguments");
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        // validate the viewpoint type
        if (type_string != null) {
            switch (type_string) {
                case ICON:
                    typeFlag = ICON_TYPE;
                    break;
                case DEFAULT:
                    typeFlag = DEFAULT_TYPE;
                    break;
                default:
                    throw new IllegalArgumentException(
                            LOG_NAME +": Unknown Type argument: "+ type_string);
            }
        } else {
            typeFlag = DEFAULT_TYPE;
        }

        //////////////////////////////////////////////////////////////////////
        // validate the position and location
        if ((position_val != null) || (orientation_val != null)) {
            if (position_val != null) {
                try {
                    positionData = toFloat(position_val);
                    modifyPosition = true;
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                        LOG_NAME +": Invalid position value: "+
                        Arrays.toString(position_val));
                }
            }
            if (orientation_val != null) {
                try {
                    orientationData = toFloat(orientation_val);
                    // normalize the vector portion of the axis-angle rotation
                    Vector3f vec = new Vector3f(orientationData);
                    vec.normalize();
                    vec.get(orientationData);
                    modifyOrientation = true;
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                        LOG_NAME +": Invalid orientation value: "+
                        Arrays.toString(orientation_val));
                }
            }
        } else {
            //both position and orientation are unspecified, this becomes an identity filter...
            //System.out.println(LOG_NAME +": No position or orientation arguments, nothing to do...");
            //throw new IllegalArgumentException(LOG_NAME +": No position or orientation arguments, nothing to do...");
        }
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
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
        if ((modifyPosition || modifyOrientation) && !modifyComplete) {
            // didn't find the viewpoint to modify and a change is required,
            // create a new viewpoint.
            switch (typeFlag) {
                case DEFAULT_TYPE:
                    contentHandler.startNode("Viewpoint", null);
                    nodeStack.push("Viewpoint");
                    break;

                case ICON_TYPE:
                    contentHandler.startNode("Viewpoint", ICON_DEF_NAME);
                    nodeStack.push("Viewpoint");
                    break;
            }

            boolean need_pop = false;

            if (modifyPosition) {
                contentHandler.startField("position");

                String node_name = (String)nodeStack.peek();
                need_pop = true;

                fieldHandler.setFieldValue(node_name,
                                           "position",
                                           positionData,
                                           positionData.length);
            }

            if (modifyOrientation) {
                contentHandler.startField("orientation");

                String node_name = (String)nodeStack.peek();
                need_pop = true;

                fieldHandler.setFieldValue(node_name,
                                           "orientation",
                                           orientationData,
                                           orientationData.length);
            }

            if(need_pop)
                nodeStack.pop();

            contentHandler.endNode();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (!modifyComplete && (name.equals("Viewpoint"))) {
            switch (typeFlag) {
                case DEFAULT_TYPE:
                    modifyInProgress = true;
                    break;
                case ICON_TYPE:
                    if (ICON_DEF_NAME.equals(defName)) {
                        modifyInProgress = true;
                    }
                    break;
            }
        }

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
        if (modifyInProgress) {
            if (modifyPosition && !modifyPositionComplete) {
                contentHandler.startField("position");

                String node_name = (String)nodeStack.peek();

                fieldHandler.setFieldValue(node_name,
                                           "position",
                                           positionData,
                                           positionData.length);
                modifyPositionComplete = true;
            }

            if (modifyOrientation && !modifyOrientationComplete) {
                contentHandler.startField("orientation");

                String node_name = (String)nodeStack.peek();

                fieldHandler.setFieldValue(node_name,
                                           "orientation",
                                           orientationData,
                                           orientationData.length);
                modifyOrientationComplete = true;
            }
            modifyInProgress = false;
            modifyComplete = true;
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
        if (modifyInProgress) {
            if (modifyPosition) {
                modifyPositionInProgress = name.equals("position");
            }
            if (modifyOrientation) {
                modifyOrientationInProgress = name.equals("orientation");
            }
        }

        super.startField(name);
    }

    //-----------------------------------------------------------------------
    // Methods for interface StringContentHandler
    //-----------------------------------------------------------------------

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
    public void fieldValue(String value) throws SAVException, VRMLException {
        if (modifyPositionInProgress) {
            super.fieldValue(positionData, positionData.length);

            modifyPositionInProgress = false;
            modifyPositionComplete = true;

        } else if (modifyOrientationInProgress) {
            super.fieldValue(orientationData,
                             orientationData.length);

            modifyOrientationInProgress = false;
            modifyOrientationComplete = true;

        } else {
            super.fieldValue(value);
        }
    }

    //-----------------------------------------------------------------------
    //Methods defined by BinaryContentHandler
    //-----------------------------------------------------------------------

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

        if (modifyPositionInProgress) {
            super.fieldValue(positionData, positionData.length);

            modifyPositionInProgress = false;
            modifyPositionComplete = true;

        } else if (modifyOrientationInProgress) {
            super.fieldValue(orientationData,
                             orientationData.length);

            modifyOrientationInProgress = false;
            modifyOrientationComplete = true;

        } else {
            super.fieldValue(value, len);
        }
    }

    //---------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------

    /**
     * Return an array of float values for String values
     *
     * @param sArray An array of String values to convert
     * @return A float array of converted values
     */
    private float[] toFloat(String[] sArray) {
        int num = sArray.length;
        float[] fArray = new float[num];
        for (int i = 0; i < num; i++) {
            fArray[i] = Float.parseFloat(sArray[i]);
        }

        return fArray;
    }
}
