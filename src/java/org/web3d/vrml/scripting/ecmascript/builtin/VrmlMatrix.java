/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.builtin;

// Standard imports
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

// Application specific imports
import org.j3d.util.HashSet;

/**
 * ECMAScript VrmlMatrix builtin object.
 *  <p>
 *
 * The implementation of all the functionality in this class is according to
 * the Matrix and Quaternion FAQ, which is currently located at:
 * <a href="http://www.cs.ualberta.ca/~andreas/math/matrfaq_latest.html">http://www.cs.ualberta.ca/~andreas/math/matrfaq_latest.html</a>
 * <p>
 * The spec is sort of a bit wishy-washy about how the flat references of
 * array indices. Internally the matrix is stored with the translations values
 * down the right "column" where the VRML spec has them across the bottom. Not
 * entirely convinced that this is correct. Just makes it easy to check this
 * code against the FAQ code, which uses them stored in the same way.
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class VrmlMatrix extends FieldScriptableObject {

    // Implementation Note:
    // Originally I was going to use the java3d Matrix4d to back this class.
    // However that implied that the user would need to download and install
    // Java3D even if they didn't want it. Matrix4d only comes as part of J3D
    // as the javax.vecmath package is not separately downloadable :(
    // JC.

    /** Set of the valid function names for this object */
    private static final HashSet<String> functionNames;

    /** The default values according to the VRML spec */
    private static final double[] DEFAULT_TRANSLATION = {0, 0, 0};
    private static final double[] DEFAULT_ROTATION = {0, 0, 1, 0};
    private static final double[] DEFAULT_SCALE = {1, 1, 1};
    private static final double[] DEFAULT_SCALE_ORIENT = {0, 0, 1, 0};
    private static final double[] DEFAULT_CENTER = {0, 0, 0};

    /** The values of the matrix */
    private double[] matrix;

    /** Temporary work variables */
    private double[] workMatrix;

    private double[] workTranslation;
    private double[] workRotation;
    private double[] workScale;
    private double[] workOrientation;
    private double[] workCenter;

    static {
        functionNames = new HashSet<>();
        functionNames.add("setTransform");
        functionNames.add("getTransform");
        functionNames.add("inverse");
        functionNames.add("transpose");
        functionNames.add("multLeft");
        functionNames.add("multRight");
        functionNames.add("multVecMatrix");
        functionNames.add("multMatrixVec");
        functionNames.add("toString");
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call.
     */
    public VrmlMatrix() {
        super("VrmlMatrix");

        workMatrix = new double[16];
        workTranslation = new double[3];
        workRotation = new double[4];
        workScale = new double[3];
        workOrientation = new double[4];
        workCenter = new double[3];

        matrix = new double[16];
    }

    /**
     * Default public constructor required by Rhino for when created by
     * an Ecmascript call. This is in row-major form.
     * @param data
     */
    public VrmlMatrix(double[] data) {
        this(); // invoke default constructor

        if(data == null)
            return;

        matrix[0] = data[0];
        matrix[1] = data[1];
        matrix[2] = data[2];
        matrix[3] = data[3];

        matrix[4] = data[4];
        matrix[5] = data[5];
        matrix[6] = data[6];
        matrix[7] = data[7];

        matrix[8] = data[8];
        matrix[9] = data[9];
        matrix[10] = data[10];
        matrix[11] = data[11];

        matrix[12] = data[12];
        matrix[13] = data[13];
        matrix[14] = data[14];
        matrix[15] = data[15];
    }

    // NOTE:
    // No need for a jsConstructor in this class because the constructor above
    // does the job. If you did define one with the same arguments, then rhino
    // would issue an error message about a bad constructor!
    public void jsConstructor(double f11, double f12, double f13, double f14,
                              double f21, double f22, double f23, double f24,
                              double f31, double f32, double f33, double f34,
                              double f41, double f42, double f43, double f44) {

        matrix[0]  =  Double.isNaN(f11) ? 1 : f11;
        matrix[1]  =  Double.isNaN(f21) ? 0 : f21;
        matrix[2]  =  Double.isNaN(f31) ? 0 : f31;
        matrix[3]  =  Double.isNaN(f41) ? 0 : f41;

        matrix[4]  =  Double.isNaN(f12) ? 0 : f12;
        matrix[5]  =  Double.isNaN(f22) ? 1 : f22;
        matrix[6]  =  Double.isNaN(f32) ? 0 : f32;
        matrix[7]  =  Double.isNaN(f42) ? 0 : f42;

        matrix[8]  =  Double.isNaN(f13) ? 0 : f13;
        matrix[9]  =  Double.isNaN(f23) ? 0 : f23;
        matrix[10] =  Double.isNaN(f33) ? 1 : f33;
        matrix[11] =  Double.isNaN(f43) ? 0 : f43;

        matrix[12] =  Double.isNaN(f14) ? 0 : f14;
        matrix[13] =  Double.isNaN(f24) ? 0 : f24;
        matrix[14] =  Double.isNaN(f34) ? 0 : f34;
        matrix[15] =  Double.isNaN(f44) ? 1 : f44;
    }

    //----------------------------------------------------------
    // Methods overridden from the Scriptable interface.
    //----------------------------------------------------------

    /**
     * Check to see if a numeric property is valid. We only accept the first
     * 16 as that is the dimension of the array.
     *
     * @param index the index of the property
     * @param start the object where lookup began
     * @return true if 0 &lt; index &lt; 16
     */
    @Override
    public boolean has(int index, Scriptable start) {
        return ((index >= 0) && (index < 16));
    }

    //
    // has(String, Scriptable) is implemented by the base class as the only
    // thing you can get using a string is the function names. There is no
    // named properties for VrmlMatrix
    //

    /**
     * Look up the element in the associated matrix and return it if it
     * exists. If it doesn't exist return
     * @param index the index of the integral property
     * @param start the object where the lookup began
     * @return 
     */
    @Override
    public Object get(int index, Scriptable start) {
        if ((index < 0) && (index > 15))
            return NOT_FOUND;

        return matrix[index];
    }

    /**
     * Get the value of the named function. If no function object is
     * registered for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    @Override
    public Object get(String name, Scriptable start) {
        Object ret_val = super.get(name, start);

        // it could be that this instance is dynamically created and so
        // the function name is not automatically registex by the
        // runtime. Let's check to see if it is a standard method for
        // this object and then create and return a corresponding Function
        // instance.
        if((ret_val == null) && functionNames.contains(name))
            ret_val = locateFunction(name);

        if(ret_val == null)
            ret_val = NOT_FOUND;

        return ret_val;
    }

    /**
     * Set an indexed property. Only accept the first 16 values.
     *
     * @param index The index of the property to look up
     * @param start The object where the lookup began
     * @param value The value of the object to use
     */
    @Override
    public void put(int index, Scriptable start, Object value) {
        if ((index < 0) && (index > 15))
            return;

        Number num = (Number)value;
        matrix[index] = num.doubleValue();
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    @Override
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            registerFunction(name, value);
        }

        // ignore anything else
    }

    //
    // Methods for the Javascript ScriptableObject handling. Defined by
    // Table C.31
    //

    /**
     * Set the transform to the new values of translation, rotation, scale
     * etc. Takes a variable list of arguments. Anything more than 5 and we
     * ignore the extras. There may by up to 5 items in the array. Zero items
     * means to set the array to an identity matrix. After that, the
     * arguments are:
     * <pre>
     * args[0] SFVec3f translation
     * args[1] SFRotation rotation
     * args[2] SFVec3f scale
     * args[3] SFRotation scaleOrientation
     * args[4] SFVec3f center
     * </pre>
     * @param center
     */
    public void jsFunction_setTransform(Scriptable trans,
                                        Scriptable rot,
                                        Scriptable scale,
                                        Scriptable scaleOrient,
                                        Scriptable center) {

        if(trans != null && !(trans instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(rot != null && !(rot instanceof SFRotation))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(scale != null && !(scale instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(scaleOrient != null && !(scaleOrient instanceof SFRotation))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(center != null && !(center instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);


        // If the first argument is null, that means we treat it like a clear
        // of the matrix, thus reseting to Identity matrix
        if(trans == null) {
            setIdentity();
            return;
        }

        manualSetup();
        ((SFVec3f)trans).getRawData(workTranslation);

        if(rot != null)
            ((SFRotation)rot).getRawData(workRotation);

        if(scale != null)
            ((SFVec3f)scale).getRawData(workScale);

        if(scaleOrient != null)
            ((SFRotation)scaleOrient).getRawData(workOrientation);

        if(center != null)
            ((SFVec3f)center).getRawData(workCenter);

        // Calculate the bits for the rotation Quat -&gt; Matrix conversion:
        double xx = workRotation[0] * workRotation[0];
        double xy = workRotation[0] * workRotation[1];
        double xz = workRotation[0] * workRotation[2];
        double xw = workRotation[0] * workRotation[3];

        double yy = workRotation[1] * workRotation[1];
        double yz = workRotation[1] * workRotation[2];
        double yw = workRotation[1] * workRotation[3];

        double zz = workRotation[2] * workRotation[2];
        double zw = workRotation[2] * workRotation[3];

        // Now do the matrix calcs
        matrix[0] =  workScale[0] * (1 - 2 * (yy + zz));
        matrix[1] =  2 * (xy + zw);
        matrix[2] =  2 * (xz - yw);
        matrix[3] =  0;
        matrix[4] =  2 * (xy - zw);
        matrix[5] =  workScale[1] * (1 - 2 * (xx + zz));
        matrix[6] =  2 * (yz + xw);
        matrix[7] =  0;
        matrix[8] =  2 * (xz + yw);
        matrix[9] =  2 * (yz - xw);
        matrix[10] =  workScale[2] * (1 - 2 * (xx + yy));
        matrix[11] =  0;
        matrix[12] =  workTranslation[0];
        matrix[13] =  workTranslation[1];
        matrix[14] =  workTranslation[2];
        matrix[15] =  1;
    }

    /**
     * Get the value of the transform.
     *
     * @param tx The transform component
     * @param rot The orientation component
     * @param sc The scale component
     */
    public void jsFunction_getTransform(Scriptable tx,
                                        Scriptable rot,
                                        Scriptable sc) {

        if(tx != null && !(tx instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(rot != null && !(rot instanceof SFRotation))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        if(sc != null && !(sc instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);


        SFVec3f translation = (SFVec3f)tx;
        SFRotation rotation = (SFRotation)rot;
        SFVec3f scale = (SFVec3f)sc;

        // Varargs handling. If the first is null, then the rest will be
        // and there's no point going any further.
        if(translation == null)
            return;

         workTranslation[0] = matrix[12];
         workTranslation[1] = matrix[13];
         workTranslation[2] = matrix[14];

         translation.setRawData(workTranslation);

         if(rotation == null)
            return;

        // calculate the trace of the matrix
        double trace = 1 + matrix[0] + matrix[5] + matrix[10];

        if(trace > 0.00000001) {
            double s = Math.sqrt(trace) * 2;
            double inv_s = 1 / s;   // save doing lots of divs

            workRotation[0] = (matrix[6] - matrix[9]) * inv_s;
            workRotation[1] = (matrix[8] - matrix[2]) * inv_s;
            workRotation[2] = (matrix[1] - matrix[4]) * inv_s;
            workRotation[3] = 0.25 * s;
        } else {
            // effectively trace is zero, so go looking for the major diag
            if((matrix[0] > matrix[5]) && (matrix[0] > matrix[10])) {
                // Column 0
                double s =
                    Math.sqrt((1 + matrix[0] - matrix[5] - matrix[10]) * 2);
                double inv_s = 1 / s;

                workRotation[0] = 0.25 * s;
                workRotation[1] = (matrix[1] + matrix[4]) * inv_s;
                workRotation[2] = (matrix[8] + matrix[2]) * inv_s;
                workRotation[3] = (matrix[6] - matrix[9]) * inv_s;

            } else if(matrix[5] > matrix[10]) {
                // Column 1
                double s =
                    Math.sqrt((1 + matrix[5] - matrix[0] - matrix[10]) * 2);
                double inv_s = 1 / s;

                workRotation[1] = (matrix[1] + matrix[4]) * inv_s;
                workRotation[0] = 0.25 * s;
                workRotation[3] = (matrix[6] + matrix[9]) * inv_s;
                workRotation[2] = (matrix[8] - matrix[2]) * inv_s;
            } else {
                // Column 2
                double s =
                    Math.sqrt((1 + matrix[10] - matrix[0] - matrix[5]) * 2);
                double inv_s = 1 / s;

                workRotation[2] = (matrix[8] + matrix[2]) * inv_s;
                workRotation[3] = (matrix[6] + matrix[9]) * inv_s;
                workRotation[0] = 0.25 * s;
                workRotation[1] = (matrix[1] - matrix[4]) * inv_s;
            }
        }

        rotation.setRawData(workRotation);

        if(scale == null)
            return;

        workScale[0] = matrix[0];
        workScale[1] = matrix[5];
        workScale[2] = matrix[10];

        scale.setRawData(workScale);
    }

    /**
     * Create the inverse of the matrix and return that in a new matrix.
     * @return 
     */
    public VrmlMatrix jsFunction_inverse() {

        double[] tmp_matrix = new double[9];
        double mdet = determinant4x4(matrix, tmp_matrix);
        int i;
        int j;
        int sign;

        if(Math.abs(mdet) < 0.0005)
            setIdentity();
        else {
            for(i = 0; i < 4; i++) {
                for(j = 0; j < 4; j++) {
                    sign = 1 - ((i + j) % 2) * 2;

                    submatrix(matrix, i, j, tmp_matrix);

                    workMatrix[i + j * 4] =
                        (determinant3x3(tmp_matrix) * sign) / mdet;
                }
            }
        }

        return new VrmlMatrix(workMatrix);
    }

    /**
     * Create the transpose of this matrix and return it in a new matrix.
     *
     * @return A matrix containing the transpose of this one
     */
    public VrmlMatrix jsFunction_transpose() {
        workMatrix[0] =  matrix[0];
        workMatrix[1] =  matrix[4];
        workMatrix[2] =  matrix[8];
        workMatrix[3] =  matrix[12];
        workMatrix[4] =  matrix[1];
        workMatrix[5] =  matrix[5];
        workMatrix[6] =  matrix[9];
        workMatrix[7] =  matrix[13];
        workMatrix[8] =  matrix[2];
        workMatrix[9] =  matrix[6];
        workMatrix[10] =  matrix[10];
        workMatrix[11] =  matrix[14];
        workMatrix[12] =  matrix[3];
        workMatrix[13] =  matrix[7];
        workMatrix[14] =  matrix[11];
        workMatrix[15] =  matrix[15];

        return new VrmlMatrix(workMatrix);
    }

    /**
     * Multiply the passed matrix this matrix and return the value in a
     * new matrix instance.
     *
     * @param m The left hand matrix to use
     * @return A new matrix with the new values
     */
    public VrmlMatrix jsFunction_multLeft(Scriptable m) {

        if(!(m instanceof VrmlMatrix))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        VrmlMatrix mat = (VrmlMatrix)m;
        double[] left = mat.matrix;

        workMatrix[0] =  left[0] * matrix[0] +
                         left[1] * matrix[4] +
                         left[2] * matrix[8] +
                         left[3] * matrix[12];
        workMatrix[1] =  left[0] * matrix[1] +
                         left[1] * matrix[5] +
                         left[2] * matrix[9] +
                         left[3] * matrix[13];
        workMatrix[2] =  left[0] * matrix[2] +
                         left[1] * matrix[6] +
                         left[2] * matrix[10] +
                         left[3] * matrix[14];
        workMatrix[3] =  left[0] * matrix[3] +
                         left[1] * matrix[7] +
                         left[2] * matrix[11] +
                         left[3] * matrix[15];

        workMatrix[4] =  left[4] * matrix[0] +
                         left[5] * matrix[4] +
                         left[6] * matrix[8] +
                         left[7] * matrix[12];
        workMatrix[5] =  left[4] * matrix[1] +
                         left[5] * matrix[5] +
                         left[6] * matrix[9] +
                         left[7] * matrix[13];
        workMatrix[6] =  left[4] * matrix[2] +
                         left[5] * matrix[6] +
                         left[6] * matrix[10] +
                         left[7] * matrix[14];
        workMatrix[7] =  left[4] * matrix[3] +
                         left[5] * matrix[7] +
                         left[6] * matrix[11] +
                         left[7] * matrix[15];

        workMatrix[8] =  left[8]  * matrix[0] +
                         left[9]  * matrix[4] +
                         left[10] * matrix[8] +
                         left[11] * matrix[12];
        workMatrix[9] =  left[8]  * matrix[1] +
                         left[9]  * matrix[5] +
                         left[10] * matrix[9] +
                         left[11] * matrix[13];
        workMatrix[10] = left[8]  * matrix[2] +
                         left[9]  * matrix[6] +
                         left[10] * matrix[10] +
                         left[11] * matrix[14];
        workMatrix[11] = left[8]  * matrix[3] +
                         left[9]  * matrix[7] +
                         left[10] * matrix[11] +
                         left[11] * matrix[15];

        workMatrix[12] = left[12] * matrix[0] +
                         left[13] * matrix[4] +
                         left[14] * matrix[8] +
                         left[15] * matrix[12];
        workMatrix[13] = left[12] * matrix[1] +
                         left[13] * matrix[5] +
                         left[14] * matrix[9] +
                         left[15] * matrix[13];
        workMatrix[14] = left[12] * matrix[2] +
                         left[13] * matrix[6] +
                         left[14] * matrix[10] +
                         left[15] * matrix[14];
        workMatrix[15] = left[12] * matrix[3] +
                         left[13] * matrix[7] +
                         left[14] * matrix[11] +
                         left[15] * matrix[15];

        return new VrmlMatrix(workMatrix);
    }

    /**
     * Multiply this matrix by the passed matrix and return the value in a
     * new matrix instance.
     *
     * @param m The left hand matrix to use
     * @return A new matrix with the new values
     */
    public VrmlMatrix jsFunction_multRight(Scriptable m) {

        if(!(m instanceof VrmlMatrix))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        VrmlMatrix mat = (VrmlMatrix)m;
        double[] right = mat.matrix;

        workMatrix[0] =  matrix[0] * right[0] +
                         matrix[1] * right[4] +
                         matrix[2] * right[8] +
                         matrix[3] * right[12];
        workMatrix[1] =  matrix[0] * right[1] +
                         matrix[1] * right[5] +
                         matrix[2] * right[9] +
                         matrix[3] * right[13];
        workMatrix[2] =  matrix[0] * right[2] +
                         matrix[1] * right[6] +
                         matrix[2] * right[10] +
                         matrix[3] * right[14];
        workMatrix[3] =  matrix[0] * right[3] +
                         matrix[1] * right[7] +
                         matrix[2] * right[11] +
                         matrix[3] * right[15];

        workMatrix[4] =  matrix[4] * right[0] +
                         matrix[5] * right[4] +
                         matrix[6] * right[8] +
                         matrix[7] * right[12];
        workMatrix[5] =  matrix[4] * right[1] +
                         matrix[5] * right[5] +
                         matrix[6] * right[9] +
                         matrix[7] * right[13];
        workMatrix[6] =  matrix[4] * right[2] +
                         matrix[5] * right[6] +
                         matrix[6] * right[10] +
                         matrix[7] * right[14];
        workMatrix[7] =  matrix[4] * right[3] +
                         matrix[5] * right[7] +
                         matrix[6] * right[11] +
                         matrix[7] * right[15];

        workMatrix[8] =  matrix[8]  * right[0] +
                         matrix[9]  * right[4] +
                         matrix[10] * right[8] +
                         matrix[11] * right[12];
        workMatrix[9] =  matrix[8]  * right[1] +
                         matrix[9]  * right[5] +
                         matrix[10] * right[9] +
                         matrix[11] * right[13];
        workMatrix[10] = matrix[8]  * right[2] +
                         matrix[9]  * right[6] +
                         matrix[10] * right[10] +
                         matrix[11] * right[14];
        workMatrix[11] = matrix[8]  * right[3] +
                         matrix[9]  * right[7] +
                         matrix[10] * right[11] +
                         matrix[11] * right[15];

        workMatrix[12] = matrix[12] * right[0] +
                         matrix[13] * right[4] +
                         matrix[14] * right[8] +
                         matrix[15] * right[12];
        workMatrix[13] = matrix[12] * right[1] +
                         matrix[13] * right[5] +
                         matrix[14] * right[9] +
                         matrix[15] * right[13];
        workMatrix[14] = matrix[12] * right[2] +
                         matrix[13] * right[6] +
                         matrix[14] * right[10] +
                         matrix[15] * right[14];
        workMatrix[15] = matrix[12] * right[3] +
                         matrix[13] * right[7] +
                         matrix[14] * right[11] +
                         matrix[15] * right[15];

        return new VrmlMatrix(workMatrix);
    }

    /**
     * Return a vector that is this multiplied by the given vector as a
     * row.
     *
     * @param vector The row vector to use for multiplication
     * @return A new vector containing the results
     */
    public SFVec3f jsFunction_multVecMatrix(Scriptable vector) {

        if(vector != null && !(vector instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        SFVec3f vec = (SFVec3f)vector;

        // Use the translation temp to do the multiplication but rename them
        // to make the code easier to understand.
        double[] row_vec = workTranslation;
        double[] result = workScale;

        vec.getRawData(row_vec);

        result[0] = matrix[0] * row_vec[0] + matrix[1] + matrix[2] + matrix[3];
        result[1] = matrix[0] * row_vec[1] + matrix[1] + matrix[2] + matrix[3];
        result[2] = matrix[0] * row_vec[2] + matrix[1] + matrix[2] + matrix[3];

        return new SFVec3f(result);
    }

    /**
     * Return a vector that is this multiplied by the given vector as a
     * column.
     *
     * @param vector The column vector to use for multiplication
     * @return A new vector containing the results
     */
    public SFVec3f jsFunction_multMatrixVec(Scriptable vector) {

        if(vector != null && !(vector instanceof SFVec3f))
            Context.reportRuntimeError(INVALID_TYPE_MSG);

        SFVec3f vec = (SFVec3f)vector;

        // Use the translation temp to do the multiplication but rename them
        // to make the code easier to understand.
        double[] col_vec = workTranslation;
        double[] result = workScale;

        vec.getRawData(col_vec);

        result[0] = matrix[0] * col_vec[0] +
                    matrix[1] * col_vec[1] +
                    matrix[2] * col_vec[2] +
                    matrix[3];
        result[1] = matrix[4] * col_vec[0] +
                    matrix[5] * col_vec[1] +
                    matrix[6] * col_vec[2] +
                    matrix[7];
        result[2] = matrix[8] * col_vec[0] +
                    matrix[9] * col_vec[1] +
                    matrix[10] * col_vec[2] +
                    matrix[11];

        return new SFVec3f(result);
    }

    /**
     * Creates a string version of this node. Just calls the standard
     * toString() method of the object.
     *
     * @return A VRML string representation of the field
     */
    public String jsFunction_toString() {
        return toString();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Format the internal values of this matrix as a string. Does some nice
     * pretty formatting.
     *
     * @return A string representation of this matrix
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("| ");
        buf.append(matrix[0]);
        buf.append(' ');
        buf.append(matrix[1]);
        buf.append(' ');
        buf.append(matrix[2]);
        buf.append(' ');
        buf.append(matrix[3]);
        buf.append(" |\n");

        buf.append("| ");
        buf.append(matrix[4]);
        buf.append(' ');
        buf.append(matrix[5]);
        buf.append(' ');
        buf.append(matrix[6]);
        buf.append(' ');
        buf.append(matrix[7]);
        buf.append(" |\n");

        buf.append("| ");
        buf.append(matrix[8]);
        buf.append(' ');
        buf.append(matrix[9]);
        buf.append(' ');
        buf.append(matrix[10]);
        buf.append(' ');
        buf.append(matrix[11]);
        buf.append(" |\n");

        buf.append("| ");
        buf.append(matrix[12]);
        buf.append(' ');
        buf.append(matrix[13]);
        buf.append(' ');
        buf.append(matrix[14]);
        buf.append(' ');
        buf.append(matrix[15]);
        buf.append(" |\n");

        return buf.toString();
    }

    /**
     * Reset the matrix to the the identity matrix
     */
    private void setIdentity() {
        matrix[0] = 1;
        matrix[1] = 0;
        matrix[2] = 0;
        matrix[3] = 0;

        matrix[4] = 0;
        matrix[5] = 1;
        matrix[6] = 0;
        matrix[7] = 0;

        matrix[8] = 0;
        matrix[9] = 0;
        matrix[10] = 1;
        matrix[11] = 0;

        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;
    }

    /**
     * A manual setup of the array data. Much faster than doing
     * System.arraycopy() on each array as we don't have much to copy over
     * and it saves us making the jump to native code. Also, nice big juicy
     * Hotspot optimisable code.
     */
    private void manualSetup() {
        workTranslation[0] = DEFAULT_TRANSLATION[0];
        workTranslation[1] = DEFAULT_TRANSLATION[1];
        workTranslation[2] = DEFAULT_TRANSLATION[2];

        workRotation[0] = DEFAULT_ROTATION[0];
        workRotation[1] = DEFAULT_ROTATION[1];
        workRotation[2] = DEFAULT_ROTATION[2];
        workRotation[3] = DEFAULT_ROTATION[3];

        workScale[0] = DEFAULT_SCALE[0];
        workScale[1] = DEFAULT_SCALE[1];
        workScale[2] = DEFAULT_SCALE[2];

        workOrientation[0] = DEFAULT_SCALE_ORIENT[0];
        workOrientation[1] = DEFAULT_SCALE_ORIENT[1];
        workOrientation[2] = DEFAULT_SCALE_ORIENT[2];
        workOrientation[3] = DEFAULT_SCALE_ORIENT[3];

        workCenter[0] = DEFAULT_CENTER[0];
        workCenter[1] = DEFAULT_CENTER[1];
        workCenter[2] = DEFAULT_CENTER[2];
    }

    /**
     * Calculate the determine of this 3x3 matrix.
     */
    private double determinant3x3(double[] mat) {
        double ret_val = mat[0] * (mat[4] * mat[8] - mat[7] * mat[5]) -
                         mat[1] * (mat[3] * mat[8] - mat[6] * mat[5]) +
                         mat[2] * (mat[3] * mat[7] - mat[6] * mat[4]);

        return ret_val;
    }

    /**
     * Calculate the determinant of this 4x4 matrix. Also provide a temporary
     * matrix to use internally just to save on object allocation.
     *
     * @param mat The matrix to create the determinant for
     * @param tmpMatrix A 3x3 matrix to use for temporary storage
     */
    private double determinant4x4(double[] mat, double[] tmpMatrix) {
        double determinant;
        double result = 0;
        double i = 1;
        int n;

        for(n = 0; n < 4; n++, i *= -1) {
            submatrix(mat, 0, n, tmpMatrix);
            determinant = determinant3x3(tmpMatrix);
            result += mat[n] * determinant * i;
        }

        return result;
    }

    /**
     * Find the 3x3 sub-matrix of the source matrix omitting row i and column j.
     */
    private void submatrix(double[] src, int i, int j, double[] dest) {
        int di, dj, si, sj;

        // loop through 3x3 submatrix
        for(di = 0; di < 3; di++) {
            for(dj = 0; dj < 3; dj++) {

              // map 3x3 element (destination) to 4x4 element (source)
              si = di + ((di >= i) ? 1 : 0);
              sj = dj + ((dj >= j) ? 1 : 0);

              // copy element
              dest[di * 3 + dj] = src[si * 4 + sj];
            }
        }
    }
}
