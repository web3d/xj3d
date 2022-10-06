/*****************************************************************************
 *                        Yumetech Copyright (c) 2010
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

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

// Internal Imports

/**
 * Test for CompressionTools
 *
 * @author Alan Hudson
 * @version
 */
public class TestCompressionTools extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     * @return
     */
    public static Test suite() {
        return new TestSuite(TestCompressionTools.class);
    }

    /**
     * Test that an array of all floats encodes and decodes correctly
     *
     */
    public void testQuantizeFloatArrayDeflaterAllFloats() {
        int size = 10000;
        int start = (int) -(size / 2f);
//        int start = 0;
        int step = 1;
        float tolerance = 0.000001f;

        int numTested = 0;
        float[] array = new float[size];
        int startI = Float.floatToIntBits(start);

        while(numTested < size) {
            int i = startI + numTested * step;

            //i = 1;
            float var = Float.intBitsToFloat(i);

            if (Float.isNaN(var))
                continue;

            array[numTested] = var;

            numTested++;

        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(size*4);
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            CompressionTools.quantizeFloatArrayDeflater(dos, array, tolerance, false);

            byte[] buff = baos.toByteArray();

            float[] decoded_vals = CompressionTools.dequantizeFloatArrayInflater(buff, 0, buff.length, false);

            float diff;

            for(int i=0; i < size; i++) {
                diff = decoded_vals[i] - array[i];
                if (Math.abs(diff) > tolerance) {
                    fail("Float: " + array[i] + " is off: " + diff + " orig: " + array[i] + " decoded: " + decoded_vals[i]);
                }
            }

            float compression = buff.length / (size * 4f);
            System.out.println("compression percent: " + compression);
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
            fail("IO error in deflating data");
        }
    }
}

