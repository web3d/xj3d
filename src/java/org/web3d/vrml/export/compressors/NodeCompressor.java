/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.sav.*;

public interface NodeCompressor {
    /**
     * Compress the given Node.
     *
     * @param node The geometry to compress
     * @return An int array for the geometry
     */
    int[] compress(VRMLNodeType node);

    /**
     * Decompress the data.
     *
     * @param data The compressed data
     */
    void decompress(int[] data);

    /**
     * Checks whether this compressors handles this data.  If not then
     * the data should be written to the stream.
     *
     * @param nodeName the node
     * @param fieldName the field
     * @return true if data should be written to the stream
     */
    boolean handleData(String nodeName, String fieldName);

    /**
     * Fill in the data handled by the compressor.
     *
     * @param nodeName The nodeName
     * @param ch The handler to write to
     */
    void fillData(String nodeName, BinaryContentHandler ch);

    /** Get any metadata the encoder would like to write to the file.
     *
     *  @return The metadata in X3D XML encoding
     */
    String getEncoderMetadata();
}