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

package org.web3d.vrml.export.compressors;

// External imports
import java.io.DataInputStream;
import java.io.IOException;

// Local imports
// None

/**
 * All classes capable of decompressing a field must implement this interface
 *
 * @author Alan Hudson.
 * @version $Revision: 1.4 $
 */
public interface FieldDecompressor {

    /**
     * Can this fieldCompressor support this compression method
     *
     * @param fieldType What type of field, defined in FieldConstants.
     * @param method What method of compression.  0-127 defined by Web3D Consortium.
     * @return
     */
    boolean canSupport(int fieldType, int method);

    /**
     * Get the length of variable length field.
     *
     * @param dis
     * @return The length of the upcoming field in number of type units.
     * @throws java.io.IOException
     */
    int nextLength(DataInputStream dis) throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    int decompressInt(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressInt(DataInputStream dis, int fieldType, int[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    boolean decompressBoolean(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressBoolean(DataInputStream dis, int fieldType, boolean[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    float decompressFloat(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressFloat(DataInputStream dis, int fieldType, float[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressFloat(DataInputStream dis, int fieldType, float[][] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    long decompressLong(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressLong(DataInputStream dis, int fieldType, long[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    double decompressDouble(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressDouble(DataInputStream dis, int fieldType, double[] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressDouble(DataInputStream dis, int fieldType, double[][] data)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @return The field value
     * @throws java.io.IOException
     */
    String decompressString(DataInputStream dis, int fieldType)
        throws IOException;

    /**
     * Decompress this field.
     *
     * @param dis The stream to read from
     * @param fieldType The type of field to compress from FieldConstants.
     * @param data The field data, must be preallocated
     * @throws java.io.IOException
     */
    void decompressString(DataInputStream dis, int fieldType, String[] data)
        throws IOException;
}
