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
 * Wrapper for an X3D MatrixTransform node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MatrixTransform extends BaseGroup {

    /** Field value */
    public float[] matrix;

    /** Working variables for the computation */
    protected Matrix4f tmatrix;

    /**
     * Constructor
     */
    public MatrixTransform() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public MatrixTransform(String defName) {
        super("MatrixTransform", defName);

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
        matrix = null;
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

                if (matrix != null) {
                    handler.startField("matrix");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(matrix, 16);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(matrix, 16));
                        break;
                    }
                }

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
        if (name.equals("matrix")) {
            if (value instanceof String) {
                matrix = fieldReader.SFMatrix4f((String)value);
            } else if (value instanceof String[]) {
                matrix = fieldReader.SFMatrix4f((String[])value);
            } else if (value instanceof float[]) {
                matrix = (float[])value;
            }
        } else {
            super.setValue(name, value);
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
        if (name.equals("matrix")) {
            if (value instanceof float[]) {
                matrix = (float[])value;
            }
        } else {
            super.setValue(name, value, len);
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
        MatrixTransform t = new MatrixTransform();
        copy(t, full);
        if (full) {
            if (this.matrix != null) {
                t.matrix = new float[16];
                System.arraycopy(this.matrix, 0, t.matrix, 0, 16);
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
        if (tmatrix == null)
            tmatrix = new Matrix4f();

        if (matrix == null)
            tmatrix.setIdentity();
        else
            tmatrix.set(matrix);

        // Convert Row Matrix form to Col matrix form
        tmatrix.transpose();

        return tmatrix;
    }
}
