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

// Local imports
import org.web3d.vrml.export.compressors.CompressionTools;

/**
 * An encoder for handling float arrays.  Second version without length bit.
 * Version used in final ISO text for binary.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class QuantizedzlibFloatArrayAlgorithm2 implements EncodingAlgorithm {

    /** The URI to use for FI tables */
    public static final String ALGORITHM_URI = "encoder://web3d.org/QuantizedzlibFloatArrayEncoder_v2";

    protected final static Pattern SPACE_PATTERN = Pattern.compile("\\s");
    public final static int BYTE_SIZE    = 1;

    // Tolerance for floats to give no error based on X3D Conformance
    private static final float NOERROR = 0.0000009f;

    /** The tolerance to use when quantizing floats. */
    private float tolerance;

    public QuantizedzlibFloatArrayAlgorithm2() {
        this.tolerance = NOERROR;
    }

    public QuantizedzlibFloatArrayAlgorithm2(float tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public void encodeToOutputStream(Object data, OutputStream s) throws IOException {
        if (!(data instanceof float[])) {
            throw new IllegalArgumentException("'data' not an instance of float[]");
        }

        final float[] idata = (float[])data;

        DataOutputStream dos = new DataOutputStream(s);

        CompressionTools.quantizeFloatArrayDeflater(dos, idata, tolerance, false);
    }


    @Override
    public final Object decodeFromBytes(byte[] b, int start, int length) throws EncodingAlgorithmException {
//        System.out.println("Decompress: start: " + start + " len: " + length);

        try {
            return CompressionTools.dequantizeFloatArrayInflater(b, start, length, false);
        } catch(IOException e) {
            throw new EncodingAlgorithmException(e);
        }
    }

    @Override
    public final Object decodeFromInputStream(InputStream s) throws IOException {
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
        void word(int start, int end);
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
}
