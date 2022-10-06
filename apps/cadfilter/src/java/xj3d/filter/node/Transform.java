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
import javax.vecmath.Matrix4f;

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for an X3D Transform node.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class Transform extends BaseGroup {
    
    /** Yet another wrapper class, around BaseTransform, used for it's
    *  calculation of the matrix from the field values. */
    private TransformMatrix matrixSource;
    
    /** Field value */
    public float[] translation;
    
    /** Field value */
    public float[] rotation;
    
    /** Field value */
    public float[] scale;
    
    /** Field value */
    public float[] scaleOrientation;
    
    /** Field value */
    public float[] center;
    
    /**
     * Constructor
     */
    public Transform() {
        this(null);
    }
    
    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Transform(String defName) {
        super("Transform", defName);
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
        translation = null;
        rotation = null;
        scale = null;
        scaleOrientation = null;
        center = null;
    }
    
    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {
        
        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);
                
                super.encode();
                
                if (translation != null) {
                    handler.startField("translation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(translation, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(translation, 3));
                        break;
                    }
                }
                
                if (scale != null) {
                    handler.startField("scale");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(scale, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(scale, 3));
                        break;
                    }
                }
                
                if (center != null) {
                    handler.startField("center");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(center, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(center, 3));
                        break;
                    }
                }
                
                if (rotation != null) {
                    handler.startField("rotation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(rotation, 4);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(rotation, 4));
                        break;
                    }
                }
                
                if (scaleOrientation != null) {
                    handler.startField("scaleOrientation");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(scaleOrientation, 4);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(scaleOrientation, 4));
                        break;
                    }
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
            case "translation":
                if (value instanceof String) {
                    translation = fieldReader.SFVec3f((String)value);
                } else if (value instanceof String[]) {
                    translation = fieldReader.SFVec3f((String[])value);
                } else if (value instanceof float[]) {
                    translation = (float[])value;
                }   break;
            case "center":
                if (value instanceof String) {
                    center = fieldReader.SFVec3f((String)value);
                } else if (value instanceof String[]) {
                    center = fieldReader.SFVec3f((String[])value);
                } else if (value instanceof float[]) {
                    center = (float[])value;
                }   break;
            case "scale":
                if (value instanceof String) {
                    scale = fieldReader.SFVec3f((String)value);
                } else if (value instanceof String[]) {
                    scale = fieldReader.SFVec3f((String[])value);
                } else if (value instanceof float[]) {
                    scale = (float[])value;
                }   break;
            case "rotation":
                if (value instanceof String) {
                    rotation = fieldReader.SFRotation((String)value);
                } else if (value instanceof String[]) {
                    rotation = fieldReader.SFRotation((String[])value);
                } else if (value instanceof float[]) {
                    rotation = (float[])value;
                }   break;
            case "scaleOrientation":
                if (value instanceof String) {
                    scaleOrientation = fieldReader.SFRotation((String)value);
                } else if (value instanceof String[]) {
                    scaleOrientation = fieldReader.SFRotation((String[])value);
                } else if (value instanceof float[]) {
                    scaleOrientation = (float[])value;
            }   break;
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
            case "translation":
                if (value instanceof float[]) {
                    translation = (float[])value;
                }   break;
            case "center":
                if (value instanceof float[]) {
                    center = (float[])value;
                }   break;
            case "scale":
                if (value instanceof float[]) {
                    scale = (float[])value;
            }   break;
            case "rotation":
                if (value instanceof float[]) {
                    rotation = (float[])value;
            }   break;
            case "scaleOrientation":
                if (value instanceof float[]) {
                    scaleOrientation = (float[])value;
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
        Transform t = new Transform();
        copy(t, full);
        if (full) {
            if (this.translation != null) {
                t.translation = new float[3];
                System.arraycopy(this.translation, 0, t.translation, 0, 3);
            }
            if (this.center != null) {
                t.center = new float[3];
                System.arraycopy(this.center, 0, t.center, 0, 3);
            }
            if (this.scale != null) {
                t.scale = new float[3];
                System.arraycopy(this.scale, 0, t.scale, 0, 3);
            }
            if (this.rotation != null) {
                t.rotation = new float[4];
                System.arraycopy(this.rotation, 0, t.rotation, 0, 4);
            }
            if (this.scaleOrientation != null) {
                t.scaleOrientation = new float[4];
                System.arraycopy(this.scaleOrientation, 0, t.scaleOrientation, 0, 4);
            }
        }
        return(t);
    }
    
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    
    /**
     * Return the transform matrix
     *
     * @return the transform matrix
     */
    public Matrix4f getMatrix() {
        if (matrixSource == null) {
            matrixSource = new TransformMatrix();
        }
        if (translation != null) {
            matrixSource.setTranslation(translation);
        }
        if (rotation != null) {
            matrixSource.setRotation(rotation);
        }
        if (scale != null) {
            matrixSource.setScale(scale);
        }
        if (scaleOrientation != null) {
            matrixSource.setScaleOrientation(scaleOrientation);
        }
        if (center != null) {
            matrixSource.setCenter(center);
        }
        return(matrixSource.getMatrix());
    }
}
