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
// None

// Local imports
// none

/**
 * Container for array field type data.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class ArrayData {

    /**
     * Data types
     */
    public static final int BOOLEAN = 0;
    public static final int INT = 1;
    public static final int LONG = 2;
    public static final int FLOAT = 3;
    public static final int DOUBLE = 4;
    public static final int STRING = 5;

    /**
     * The array object
     */
    public final Object data;

    /**
     * The array primitive type
     */
    public final int type;

    /**
     * The number of items from the array that are valid
     */
    public final int num;

    /**
     * Constructor
     * @param num
     */
    public ArrayData(Object data, int num) {
        if (data instanceof float[]) {
            type = FLOAT;
        } else if (data instanceof int[]) {
            type = INT;
        } else if (data instanceof double[]) {
            type = DOUBLE;
        } else if (data instanceof String[]) {
            type = STRING;
        } else if (data instanceof boolean[]) {
            type = BOOLEAN;
        } else if (data instanceof long[]) {
            type = LONG;
        } else {
            throw new IllegalArgumentException("Invalid data type: " + data);
        }
        this.data = data;
        this.num = num;
    }

    /**
     * Constructor
     * @param num
     */
    public ArrayData(Object data, int type, int num) {
        this.data = data;
        this.type = type;
        this.num = num;
    }

    /**
     * Compare the data arrays
     *
     * @param that The object to compare
     * @return true if equals, false if not
     */
    public boolean equals(ArrayData that) {
        boolean rval;
        if (that == null) {
            rval = false;
        } else {
            if ((this.type == that.type) && (this.num == that.num)) {
                rval = true;
                switch (this.type) {
                    case FLOAT:
                        float[] f0 = (float[]) this.data;
                        float[] f1 = (float[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (f0[i] != f1[i]) {
                                rval = false;
                                break;
                            }
                        }
                        break;
                    case INT:
                        int[] i0 = (int[]) this.data;
                        int[] i1 = (int[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (i0[i] != i1[i]) {
                                rval = false;
                                break;
                            }
                        }
                        break;
                    case DOUBLE:
                        double[] d0 = (double[]) this.data;
                        double[] d1 = (double[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (d0[i] != d1[i]) {
                                rval = false;
                                break;
                            }
                        }
                        break;
                    case STRING:
                        String[] s0 = (String[]) this.data;
                        String[] s1 = (String[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (!s0[i].equals(s1[i])) {
                                rval = false;
                                break;
                            }
                        }
                        break;
                    case BOOLEAN:
                        boolean[] b0 = (boolean[]) this.data;
                        boolean[] b1 = (boolean[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (b0[i] != b1[i]) {
                                rval = false;
                                break;
                            }
                        }

                        break;
                    case LONG:
                        long[] ii0 = (long[]) this.data;
                        long[] ii1 = (long[]) that.data;
                        for (int i = 0; i < this.num; i++) {
                            if (ii0[i] != ii1[i]) {
                                rval = false;
                                break;
                            }
                        }
                        break;
                }
            } else {
                rval = false;
            }
        }
        return (rval);
    }

    /**
     * Return a unique copy of this object
     *
     * @return a unique copy of the object
     */
    @Override
    public ArrayData clone() {
        Object data_copy = null;
        switch (type) {
            case FLOAT:
                float[] f0 = (float[]) data;
                float[] f1 = new float[num];
                System.arraycopy(f0, 0, f1, 0, num);
                data_copy = f1;
                break;
            case INT:
                int[] i0 = (int[]) data;
                int[] i1 = new int[num];
                System.arraycopy(i0, 0, i1, 0, num);
                data_copy = i1;
                break;
            case DOUBLE:
                double[] d0 = (double[]) data;
                double[] d1 = new double[num];
                System.arraycopy(d0, 0, d1, 0, num);
                data_copy = d1;
                break;
            case STRING:
                String[] s0 = (String[]) data;
                String[] s1 = new String[num];
                System.arraycopy(s0, 0, s1, 0, num);
                data_copy = s1;
                break;
            case BOOLEAN:
                boolean[] b0 = (boolean[]) data;
                boolean[] b1 = new boolean[num];
                System.arraycopy(b0, 0, b1, 0, num);
                data_copy = b1;
                break;
            case LONG:
                long[] ii0 = (long[]) data;
                long[] ii1 = new long[num];
                System.arraycopy(ii0, 0, ii1, 0, num);
                data_copy = ii1;
                break;
        }
        return (new ArrayData(data_copy, type, num));
    }
}
