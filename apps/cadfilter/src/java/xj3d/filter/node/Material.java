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
 * Wrapper for the X3D Material node.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class Material extends BaseEncodable implements IMaterial {

    /** Field value */
    public float ambientIntensity;

    /** Field value */
    public float[] diffuseColor;

    /** Field value */
    public float[] emissiveColor;

    /** Field value */
    public float shininess;

    /** Field value */
    public float[] specularColor;

    /** Field value */
    public float transparency;

    /**
     * Constructor
     */
    public Material() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public Material(String defName) {
        super("Material", defName);

        ambientIntensity = -1;
        shininess = -1;
        transparency = -1;
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
        diffuseColor = null;
        emissiveColor = null;
        specularColor = null;

        ambientIntensity = -1;
        shininess = -1;
        transparency = -1;
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

                if ((ambientIntensity >= 0) && (ambientIntensity <= 1)) {
                    handler.startField("ambientIntensity");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(ambientIntensity);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(ambientIntensity));
                        break;
                    }
                }

                if (diffuseColor != null) {
                    handler.startField("diffuseColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(diffuseColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(diffuseColor, 3));
                        break;
                    }
                }

                if (emissiveColor != null) {
                    handler.startField("emissiveColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(emissiveColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(emissiveColor, 3));
                        break;
                    }
                }

                if ((shininess >= 0) && (shininess <= 1)) {
                    handler.startField("shininess");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(shininess);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(shininess));
                        break;
                    }
                }

                if (specularColor != null) {
                    handler.startField("specularColor");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(specularColor, 3);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(specularColor, 3));
                        break;
                    }
                }

                if ((transparency >= 0) && (transparency <= 1)) {
                    handler.startField("transparency");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(transparency);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(Float.toString(transparency));
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
            case "ambientIntensity":
                if (value instanceof String) {
                    ambientIntensity = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    ambientIntensity = (Float) value;
                }
                break;
            case "diffuseColor":
                if (value instanceof String) {
                    diffuseColor = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    diffuseColor = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    diffuseColor = (float[])value;
                }   break;
            case "emissiveColor":
                if (value instanceof String) {
                    emissiveColor = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    emissiveColor = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    emissiveColor = (float[])value;
            }   break;
            case "shininess":
                if (value instanceof String) {
                    shininess = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    shininess = (Float) value;
                }
                break;
            case "specularColor":
                if (value instanceof String) {
                    specularColor = fieldReader.SFColor((String)value);
                } else if (value instanceof String[]) {
                    specularColor = fieldReader.SFColor((String[])value);
                } else if (value instanceof float[]) {
                    specularColor = (float[])value;
            }   break;
            case "transparency":
                if (value instanceof String) {
                    transparency = Float.parseFloat((String)value);
                } else if (value instanceof Float) {
                    transparency = (Float) value;
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
            case "diffuseColor":
                if (value instanceof float[]) {
                    diffuseColor = (float[])value;
                }   break;
            case "emissiveColor":
                if (value instanceof float[]) {
                    emissiveColor = (float[])value;
                }   break;
            case "specularColor":
                if (value instanceof float[]) {
                    specularColor = (float[])value;
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
        Material m = new Material();
        copy(m, full);
        if (full) {
            m.ambientIntensity = this.ambientIntensity;
            m.shininess = this.shininess;
            m.transparency = this.transparency;
            if (this.diffuseColor != null) {
                m.diffuseColor = new float[3];
                System.arraycopy(this.diffuseColor, 0, m.diffuseColor, 0, 3);
            }
            if (this.emissiveColor != null) {
                m.emissiveColor = new float[3];
                System.arraycopy(this.emissiveColor, 0, m.emissiveColor, 0, 3);
            }
            if (this.specularColor != null) {
                m.specularColor = new float[3];
                System.arraycopy(this.specularColor, 0, m.specularColor, 0, 3);
            }
        }
        return(m);
    }

    @Override
    public boolean deepEquals(Encodable enc) {
        if (!(enc instanceof Material))
            return false;

        Material mat = (Material) enc;

        if (mat.ambientIntensity != this.ambientIntensity)
            return false;

        if (mat.shininess != this.shininess)
            return false;

        if (mat.transparency != this.transparency)
            return false;

        float[] val = mat.diffuseColor;
        if (diffuseColor != null) {
            if (val == null)
                return false;

            if (val[0] != diffuseColor[0] ||
                val[1] != diffuseColor[1] ||
                val[2] != diffuseColor[2]) {

                return false;
            }
        } else if (val != null)
            return false;

        val = mat.emissiveColor;
        if (emissiveColor != null) {
            if (val == null)
                return false;

            if (val[0] != emissiveColor[0] ||
                val[1] != emissiveColor[1] ||
                val[2] != emissiveColor[2]) {

                return false;
            }
        } else if (val != null)
            return false;

        val = mat.specularColor;
        if (specularColor != null) {
            if (val == null)
                return false;

            if (val[0] != specularColor[0] ||
                val[1] != specularColor[1] ||
                val[2] != specularColor[2]) {

                return false;
            }
        } else if (val != null)
            return false;

        return true;
    }
}
