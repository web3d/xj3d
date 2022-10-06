/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for the X3D Viewpoint node.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class Viewpoint extends BaseEncodable implements IBindable {

    /** Field value */
    public float[] position;

    /** Field value */
    public float[] orientation;

    /** Field value */
    public float[] centerOfRotation;

    /** Field value */
    public float fieldOfView;

    /** Field value */
    public String description;

    /** Field value */
    public boolean jump;

    /**
     * Constructor
     */
    public Viewpoint() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Viewpoint(String defName) {
        super("Viewpoint", defName);
        jump = true;
        fieldOfView = (float)Math.PI/4;
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        super.clear();
        position = null;
        orientation = null;
        centerOfRotation = null;
        description = null;
        jump = true;
        fieldOfView = (float)Math.PI/4;
    }

    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {

        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);

                super.encode();

                if (description != null) {
                    handler.startField("description");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(description);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(description);
                        break;
                    }
                }

                if (position != null) {
                    handler.startField("position");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(position, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(position, 3));
                        break;
                    }
                }

                if (orientation != null) {
                    handler.startField("orientation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(orientation, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(orientation, 3));
                        break;
                    }
                }

                if (centerOfRotation != null) {
                    handler.startField("centerOfRotation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(centerOfRotation, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(centerOfRotation, 3));
                        break;
                    }
                }

                handler.startField("fieldOfView");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(fieldOfView);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Float.toString(fieldOfView));
                    break;
                }

                handler.startField("jump");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(jump);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(jump));
                    break;
                }

                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String name, Object value) {

        switch (name) {
            case "description":
                if (value instanceof String) {
                    description = (String)value;
                }   break;
            case "position":
                if (value instanceof String) {
                    position = fieldReader.SFVec3f((String)value);
                } else if (value instanceof String[]) {
                    position = fieldReader.SFVec3f((String[])value);
                } else if (value instanceof float[]) {
                    position = (float[])value;
                }   break;
            case "orientation":
                if (value instanceof String) {
                    orientation = fieldReader.SFRotation((String)value);
                } else if (value instanceof String[]) {
                    orientation = fieldReader.SFRotation((String[])value);
                } else if (value instanceof float[]) {
                    orientation = (float[])value;
            }   break;
            case "centerOfRotation":
                if (value instanceof String) {
                    centerOfRotation = fieldReader.SFVec3f((String)value);
                } else if (value instanceof String[]) {
                    centerOfRotation = fieldReader.SFVec3f((String[])value);
                } else if (value instanceof float[]) {
                    centerOfRotation = (float[])value;
            }   break;
            case "jump":
                if (value instanceof String) {
                    jump = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    jump = ((Boolean)value);
                }
                break;
            case "fieldOfView":
                if (value instanceof String) {
                    fieldOfView = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    fieldOfView = (Float) value;
                }
                break;
            default:
                super.setValue(name, value);
                break;
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String name, Object value, int len) {

        switch (name) {
            case "position":
                if (value instanceof float[]) {
                    position = (float[])value;
                }   break;
            case "orientation":
                if (value instanceof float[]) {
                    orientation = (float[])value;
                }   break;
            case "centerOfRotation":
                if (value instanceof float[]) {
                    centerOfRotation = (float[])value;
            }   break;
            default:
                super.setValue(name, value, len);
                break;
        }
    }

    /**
     * Create and return a copy of this object.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children,
     * false returns a new instance of this node type.
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        Viewpoint v = new Viewpoint();
        copy(v, full);
        if (full) {
            v.description = this.description;
            v.fieldOfView = this.fieldOfView;
            v.jump = this.jump;
            if (this.position != null) {
                v.position = new float[3];
                System.arraycopy(this.position, 0, v.position, 0, 3);
            }
            if (this.orientation != null) {
                v.orientation = new float[4];
                System.arraycopy(this.orientation, 0, v.orientation, 0, 4);
            }
            if (this.centerOfRotation != null) {
                v.centerOfRotation = new float[3];
                System.arraycopy(this.centerOfRotation, 0, v.centerOfRotation, 0, 3);
            }
        }
        return(v);
    }
}
