/*
 * Fast Infoset ver. 0.1 software ("Software")
 *
 * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Software is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations.
 *
 *    Sun supports and benefits from the global community of open source
 * developers, and thanks the community for its important contributions and
 * open standards-based technology, which Sun has adopted into many of its
 * products.
 *
 *    Please note that portions of Software may be provided with notices and
 * open source licenses from such communities and third parties that govern the
 * use of those portions, and any licenses granted hereunder do not alter any
 * rights and obligations you may have under such open source licenses,
 * however, the disclaimer of warranty and limitation of liability provisions
 * in this License will apply to all Software in this distribution.
 *
 *    You acknowledge that the Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 */
package org.web3d.parser.x3d;

import org.jvnet.fastinfoset.EncodingAlgorithmException;
import org.jvnet.fastinfoset.EncodingAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 * An encoder for handling integer arrays.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class DeltazlibIntArrayAlgorithm implements EncodingAlgorithm {

    /** The URI to use for FI tables */
    public static final String ALGORITHM_URI = "encoder://web3d.org/DeltazlibIntArrayEncoder";

    protected final static Pattern SPACE_PATTERN = Pattern.compile("\\s");
    public final static int BYTE_SIZE    = 1;

    /** Default compression level option for Deflater */
    protected static final int DEFAULT_COMPRESSION_LEVEL = Deflater.BEST_COMPRESSION;

    /** The working compression level option for the Deflater */
    protected int compression_level;

    /**
     * Default Constructor
     */
    public DeltazlibIntArrayAlgorithm() {
        this(DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Constructor
     *
     * @param compression_level Specifies the compression level. Must be a valid
     * level constant from the <code>java.util.zip.Deflater</code> class.
     */
    public DeltazlibIntArrayAlgorithm(int compression_level) {
        this.compression_level = compression_level;
    }

    @Override
    public void encodeToOutputStream(Object data, OutputStream s) throws IOException {
        if (!(data instanceof int[])) {
            throw new IllegalArgumentException("'data' not an instance of int[]");
        }

        final int[] idata = (int[])data;

        zlibCompressIntArray(s, idata);
    }


    @Override
    public final Object decodeFromBytes(byte[] b, int start, int length) throws EncodingAlgorithmException {
        //        System.out.println("Decompress: start: " + start + " len: " + length);

        try {
            return zlibDecompressIntArray(b, start, length);
        } catch(IOException | DataFormatException e) {
            throw new EncodingAlgorithmException(e);
        }
    }

    @Override
    public final Object decodeFromInputStream(InputStream s) throws IOException {
        System.out.println("D2");
        return decodeFromInputStreamToByteArray(s);
    }


    @Override
    public final Object convertFromCharacters(char[] ch, int start, int length) {
        final CharBuffer cb = CharBuffer.wrap(ch, start, length);
        final List<Byte> byteList = new ArrayList<>();

        matchWhiteSpaceDelimnatedWords(cb, new WordListener() {
            @Override
            public void word(int start, int end) {
                String iStringValue = cb.subSequence(start, end).toString();
                byteList.add(Byte.valueOf(iStringValue));
            }
        });

        return generateArrayFromList(byteList);
    }

    @Override
    public final void convertToCharacters(Object data, StringBuffer s) {
        if (!(data instanceof byte[])) {
            throw new IllegalArgumentException("'data' not an instance of byte[]");
        }

        final byte[] idata = (byte[])data;

        convertToCharactersFromByteArray(idata, s);
    }


    public final void decodeFromBytesToByteArray(byte[] sdata, int istart, byte[] b, int start, int length) {
        final int size = length / BYTE_SIZE;
        for (int i = 0; i < size; i++) {
            //            sdata[istart++] = (byte) (b[start++] &amp;0xFF));
            sdata[istart++] = b[start++];
        }
    }

    public final byte[] decodeFromInputStreamToByteArray(InputStream s) throws IOException {
        final List<Byte> byteList = new ArrayList<>();
        final byte[] b = new byte[BYTE_SIZE];

        while (true) {
            int n = s.read(b);
            if (n != 1) {
                if (n == -1) {
                    break;
                }

                while(n != 1) {
                    final int m = s.read(b, n, BYTE_SIZE - n);
                    if (m == -1) {
                        throw new EOFException();
                    }
                    n += m;
                }
            }

            final int i = (b[0] & 0xFF);
            byteList.add((byte)i);
        }

        return generateArrayFromList(byteList);
    }


    public final void convertToCharactersFromByteArray(byte[] sdata, StringBuffer s) {
        for (int i = 0; i < sdata.length; i++) {
            s.append(Byte.toString(sdata[i]));
            if (i != sdata.length) {
                s.append(' ');
            }
        }
    }


    public final byte[] generateArrayFromList(List<Byte> array) {
        byte[] sdata = new byte[array.size()];
        for (int i = 0; i < sdata.length; i++) {
            sdata[i] = array.get(i);
        }

        return sdata;
    }

    public interface WordListener {
        public void word(int start, int end);
    }

    public void matchWhiteSpaceDelimnatedWords(CharBuffer cb, WordListener wl) {
        Matcher m = SPACE_PATTERN.matcher(cb);
        int i = 0;
        while(m.find()) {
            int s = m.start();
            if (s != i) {
                wl.word(i, s);
            }
            i = m.end();
        }
    }

    private void zlibCompressIntArray(OutputStream os, int[] data) throws IOException {
        byte[] bvals;
        int len = data.length;
        byte[] result;
        boolean removeMarkers = false;
        int idx;

        int max;
        int min;

        // Sniff for delta span.  Only check
        int span=-1;
        int cnt=0;
        int checkLen;
        int shift=0;

        if (len < 20)
            checkLen = len;
        else
            checkLen = 20;

        for(int i=0; i < checkLen; i++) {
            cnt++;
            if (data[i] == -1) {
                if (span == -1) {
                    span = cnt;
                    cnt = 0;
                }
                else if (span != cnt) {
                    span = -1;
                    break;
                } else {
                    cnt = 0;
                }
            }
        }

        if (span == -1) {
            span = 0;

            bvals = new byte[len * 4];
            idx = 0;

            for(int i=0; i < len; i++) {
                bvals[idx++] = (byte) (((data[i]+1) >>> 24) & 0xFF);
                bvals[idx++] = (byte) (((data[i]+1) >>> 16) & 0xFF);
                bvals[idx++] = (byte) (((data[i]+1) >>> 8)  & 0xFF);
                bvals[idx++] = (byte) ((data[i]+1)  & 0xFF);
            }

        } else {
            int scnt;
            int hold;
            int[] delts = new int[len];

            min = data[0];
            max = data[0];

            scnt = 0;
            int[] lastVal = new int[span];

            int[] newDelts = delts;
            idx = 0;

            if (removeMarkers) {
                newDelts = new int[delts.length * (span-1) / span];
            }

            for(int i=0; i < len; i++) {
                hold = data[i];
                delts[i] = data[i] - lastVal[scnt];

                if (delts[i] > max)
                    max = delts[i];
                else if (delts[i] < min)
                    min = delts[i];

                lastVal[scnt++] = hold;
                if (scnt == span)
                    scnt=0;

                if (removeMarkers && ((i+1) % span != 0)) {
                    newDelts[idx++] = delts[i];
                }

            }

            if (removeMarkers) {
                delts = newDelts;
                len = delts.length;
            }

            len = delts.length;
            bvals = new byte[len * 4];
            idx = 0;

            // TODO: Stop cheating with +1 and encode correctly or use shift

            //System.out.println("Delts: ");
            for(int i=0; i < len; i++) {
                bvals[idx++] = (byte) (((delts[i]+1) >>> 24) & 0xFF);
                bvals[idx++] = (byte) (((delts[i]+1) >>> 16) & 0xFF);
                bvals[idx++] = (byte) (((delts[i]+1) >>> 8)  & 0xFF);
                bvals[idx++] = (byte) ((delts[i]+1)  & 0xFF);
            }
        }


        // Compress the bytes
        Deflater compressor = new Deflater(compression_level, false);

        ByteArrayOutputStream out=new ByteArrayOutputStream();
        DeflaterOutputStream dout = new DeflaterOutputStream(out, compressor,len*4);
        dout.write(bvals);
        dout.finish();

        compressor.end();

        //System.out.println("writing: rate: " + ((float)compressedDataLength / (data.length*4.0f)) + " " + (data.length*4) + " = " + compressedDataLength);

        DataOutputStream dos = new DataOutputStream(os);

        //System.out.println("Len: " + len);
        // Write the number of ints
        dos.writeInt(len);
        dos.write((byte)span);
        out.writeTo(dos);
    }

    private int[] zlibDecompressIntArray(byte[] data, int start, int length)
        throws IOException, DataFormatException {

        int len = (((data[start++] & 255) << 24) + ((data[start++] & 255) << 16) +
            ((data[start++] & 255) << 8) + (data[start++] & 255));

        int span = data[start++];

        //        System.out.println("num ints: " + len + " span: " + span);

        Inflater decompresser = new Inflater(); // TODO: Shouldnt this be nowrap=false?
        decompresser.setInput(data, start, length - 5);
        byte[] result = new byte[len * 4];
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // Turn byte[] to int[], Can we avoid this?
        int idx = 0;
        int[] idata = new int[len];

        //System.out.println("delts:");
        for(int i=0; i < len; i++) {
            idata[i] = ((result[idx++] & 255) << 24) + ((result[idx++] & 255) << 16) +
                ((result[idx++] & 255) << 8) + (result[idx++] & 255) - 1;
            //System.out.println(idata[i]);
        }


        if (span == 0) {
            return idata;
        }

        int[] lastVal = new int[span];
        System.arraycopy(idata, 0, lastVal, 0, span);

        int scnt=0;

        for(int i=span; i < len; i++) {
            idata[i] = idata[i] + lastVal[scnt];

            lastVal[scnt++] = idata[i];
            if (scnt == span)
                scnt=0;
        }

        return idata;
    }
}
