/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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

import java.io.IOException;
import java.util.*;

/**
 * Prototype Huffman node.  Borrowed original impl from Sun HuffmanNode, need to decide
 * if copyright still stands for heavily modified code.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
class IntegerHuffmanNode extends HuffmanNode {

    int data;
    int dataLength;

    protected IntegerHuffmanNode() {
    }

    protected IntegerHuffmanNode(int data) {
        this.data = data;
    }

    public void setValue(int value) {
        data = value;
    }

    public int getValue() {
        return data;
    }

    /**
     * Write the data for this node out to a stream.
     *
     * @param packer The place to write the bits
     * @param len The number of bits to use
     */
    @Override
    public void writeData(BitPacker packer, int len) throws IOException {
        packer.pack(data, len);
    }

    @Override
    void collectLeaves(int tag, int tagLength, Collection<HuffmanNode> collection) {
        this.tag = tag;
        this.tagLength = tagLength;
        collection.add(this);
    }

    @Override
    public String toString() {
        return "data: " + data
                + "\ntag 0x" + Integer.toBinaryString(tag) + " tag length " + tagLength
                + "\nfrequency: " + frequency;
    }

    @Override
    public int hashCode() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerHuffmanNode) {
            return data == ((IntegerHuffmanNode)obj).getValue();
        }
        return false;
    }

    /**
     * Sorts nodes in descending order by tag bit length.
     */
    static class DataComparator implements Comparator<HuffmanNode> {

        @Override
        public final int compare(HuffmanNode o1, HuffmanNode o2) {
            return ((IntegerHuffmanNode)o1).data - ((IntegerHuffmanNode)o2).data;
        }
    }

    static DataComparator dataComparator = new DataComparator();

}
