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

package org.web3d.vrml.lang;

/**
 * Listing of various constants relating to fields that might be useful during
 * the parsing process.
 * <p>
 * The set of field types is a superset of the basic field types defined for
 * VRML97 and VRML 3.0. We've done this to provide the ultimate amount of
 * flexibility for new data types that may be needed in profiles.
 */
public interface FieldConstants {

    /**
     * A field type where we don't know anything about it. The field came from
     * an IMPORT statement, so we have no idea about it's origin currently.
     */
    int UNKNOWN_IMPORT_TYPE = -1;

    /**
     * A field type where we don't know anything about it. The field came from
     * an IMPORT statement, so we have no idea about it's origin currently.
     */
    int UNKNOWN_IMPORT_ACCESS = -1;

    /** The field access type is eventIn */
    int EVENTIN = 1;

    /** The field access type is field */
    int FIELD = 2;

    /** The field access type is exposedField */
    int EXPOSEDFIELD = 3;

    /** The field access type is eventOut */
    int EVENTOUT = 4;

    /** The field type is SFBOOL */
    int SFBOOL = 1;

    /** The field type is MFBOOL */
    int MFBOOL = 2;

    /** The field type is SFInt32 */
    int SFINT32 = 3;

    /** The field type is MFInt32 */
    int MFINT32 = 4;

    /** The field type is SFFloat */
    int SFFLOAT = 5;

    /** The field type is MFFloat */
    int MFFLOAT = 6;

    /** The field type is SFDouble */
    int SFDOUBLE = 7;

    /** The field type is MFDouble */
    int MFDOUBLE = 8;

    /** The field type is SFTime */
    int SFTIME = 9;

    /** The field type is MFTime */
    int MFTIME = 10;

    /** The field type is SFNode */
    int SFNODE = 11;

    /** The field type is MFNode */
    int MFNODE = 12;

    /** The field type is SFVec2f */
    int SFVEC2F = 13;

    /** The field type is MFVec2f */
    int MFVEC2F = 14;

    /** The field type is SFVec3f */
    int SFVEC3F = 15;

    /** The field type is MFVec3f */
    int MFVEC3F = 16;

    /** The field type is SFVec2d */
    int SFVEC3D = 17;

    /** The field type is MFVec2d */
    int MFVEC3D = 18;

    /** The field type is SFRotation */
    int SFROTATION = 19;

    /** The field type is SFRotation */
    int MFROTATION = 20;

    /** The field type is SFColor */
    int SFCOLOR = 21;

    /** The field type is MFColor */
    int MFCOLOR = 22;

    /** The field type is SFColorRGBA */
    int SFCOLORRGBA = 23;

    /** The field type is MFColorRGBA */
    int MFCOLORRGBA = 24;

    /** The field type is SFImage */
    int SFIMAGE = 25;

    /** The field type is MFImage */
    int MFIMAGE = 26;

    /** The field type is SFString */
    int SFSTRING = 27;

    /** The field type is MFString */
    int MFSTRING = 28;

    /** The field type is SFVec2d */
    int SFVEC2D = 29;

    /** The field type is MFVec2d */
    int MFVEC2D = 30;

    // These are not in the spec but might be later

    /** The field type is SFLong */
    int SFLONG = 31;

    /** The field type is MFLong */
    int MFLONG = 32;

    /** The field type is SFVec4f */
    int SFVEC4F = 33;

    /** The field type is MFVec4f */
    int MFVEC4F = 34;

    /** The field type is SFVec4d */
    int SFVEC4D = 35;

    /** The field type is MFVec4d */
    int MFVEC4D = 36;

    /** The field type is SFMatrix3f */
    int SFMATRIX3F = 37;

    /** The field type is MFMatrix3f */
    int MFMATRIX3F = 38;

    /** The field type is SFMatrix4f */
    int SFMATRIX4F = 39;

    /** The field type is MFMatrix4f */
    int MFMATRIX4F = 40;

    /** The field type is SFMatrix3d */
    int SFMATRIX3D = 41;

    /** The field type is MFMatrix3d */
    int MFMATRIX3D = 42;

    /** The field type is SFMatrix4d */
    int SFMATRIX4D = 43;

    /** The field type is MFMatrix4d */
    int MFMATRIX4D = 44;

    /** Empty field decls */
    float[] EMPTY_SFVEC2F = new float[2];
    float[] EMPTY_SFVEC3F = new float[3];
    float[] EMPTY_SFVEC4F = new float[4];
    float[] EMPTY_SFROTATION = new float[3];
    float[] EMPTY_SFCOLOR = new float[3];
    float[] EMPTY_SFCOLORRGBA = new float[4];
    double[] EMPTY_SFVEC2D = new double[2];
    double[] EMPTY_SFVEC3D = new double[3];
    double[] EMPTY_SFVEC4D = new double[4];
    int[] EMPTY_SFIMAGE = new int[0];
    float[] EMPTY_SFMATRIX3F = new float[9];
    float[] EMPTY_SFMATRIX4F = new float[16];
    double[] EMPTY_SFMATRIX3D = new double[9];
    double[] EMPTY_SFMATRIX4D = new double[16];

    int[] EMPTY_MFINT32 = new int[0];
    float[] EMPTY_MFFLOAT = new float[0];
    double[] EMPTY_MFDOUBLE = new double[0];
    float[] EMPTY_MFVEC2F = new float[0];
    float[] EMPTY_MFVEC3F = new float[0];
    float[] EMPTY_MFVEC4F = new float[0];
    double[] EMPTY_MFVEC2D = new double[0];
    double[] EMPTY_MFVEC3D = new double[0];
    double[] EMPTY_MFVEC4D = new double[0];
    int[] EMPTY_MFIMAGE = new int[0];
    long[] EMPTY_MFLONG = new long[0];
    boolean[] EMPTY_MFBOOL = new boolean[0];
    String[] EMPTY_MFSTRING = new String[0];
    float[] EMPTY_MFROTATION = new float[0];
    float[] EMPTY_MFCOLOR = new float[0];
    float[] EMPTY_MFCOLORRGBA = new float[0];
    float[] EMPTY_MFMATRIX3F = new float[0];
    float[] EMPTY_MFMATRIX4F = new float[0];
    double[] EMPTY_MFMATRIX3D = new double[0];
    double[] EMPTY_MFMATRIX4D = new double[0];
}
