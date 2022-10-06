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

package org.web3d.util;

// Standard imports
import junit.framework.TestCase;

import java.util.*;
import java.text.NumberFormat;


/**
 * Tests the implementation of the DoubleToString class.  This class should behave like
 * the NumberFormat class with these parameters:
 *            floatFormat = NumberFormat.getInstance(Locale.US);
 *            floatFormat.setMinimumFractionDigits(0);
 *            floatFormat.setMaximumFractionDigits(sigDigits);
 *            floatFormat.setMinimumIntegerDigits(0);
 *            floatFormat.setGroupingUsed(false);
 *
 *  It should also be considerably faster.
 *
 */
public class DoubleToStringTest extends TestCase {

    public DoubleToStringTest(String name) {
        super(name);
    }

    public void testSpecifics() {
        int sigDigits = 6;

        double[] tests = new double[] {
            0,
            0.0000000001,
            0.01,
            0.125,
            0.124567890,
            1.0,
            1.01,
            10,
            10.6,
            1000,
            10000,
            100001,
            100001.42,
            106789.87788978797887,
            1001111.37783877834,
            100001111.37783877834,
            -0,
            -0.0000000001,
            -0.1,
            -0.123456789,
            -1.0,
            -1.1234566799,
            -10,
            -10.6,
            -1000,
            -10000,
            -100001,
            -100001.42,
            -106789.87788978797887,
            -1001111.37783877834,
            -100001111.37783877834,
            -9.992718696594238E-5,
            -3.5460107028484344E-6,
            -9.997189044952393E-5,
            -7.852213457226753E-7,
            -1.00008984576E11,
//            -9.983651479552E12
        };

        NumberFormat floatFormat = NumberFormat.getInstance(Locale.US);
        floatFormat.setMinimumFractionDigits(0);
        floatFormat.setMaximumFractionDigits(sigDigits);
        floatFormat.setMinimumIntegerDigits(0);
        floatFormat.setGroupingUsed(false);

        StringBuilder buff = new StringBuilder();
        DoubleToString dts = new DoubleToString();

        for(int i=0; i < tests.length; i++) {
            buff.setLength(0);

            double f = tests[i];

            String st1 = floatFormat.format(f);

            //DoubleFormatUtil.formatDoubleFast(f,sigDigits,sigDigits, buff);
            DoubleToString.appendFormatted(buff, f, sigDigits);
            String st2 = buff.toString();

            //System.out.println("val: " + f + " orig: " + st1 + " new: " + st2);

            if (!st1.equals(st2)) {
                int slack;

                if (f > 1e17) {
                    slack = 2;
                } else  {
                    slack = 1;
                }

                slack = 0;
                if (!st1.substring(0,st1.length()-slack).equals(st2.substring(0,st2.length()-slack))) {
                    fail("error: idx: " + i + " f: " + f + " st1: " + st1 + " st2: " + st2);
                }
            }

            //assertEquals("Strings match", st1, st2);
        }
    }

    public void _testEquivalence() {
        int sigDigits = 6;
        long step = 1024l * 1024 * 1024 * 1024;

        NumberFormat floatFormat = NumberFormat.getInstance(Locale.US);
        floatFormat.setMinimumFractionDigits(0);
        floatFormat.setMaximumFractionDigits(sigDigits);
        floatFormat.setMinimumIntegerDigits(0);
        floatFormat.setGroupingUsed(false);

        StringBuilder buff = new StringBuilder();

        for(long i=Long.MIN_VALUE; i < Long.MAX_VALUE; i += step) {
            buff.setLength(0);

            double f = Double.longBitsToDouble(i);

            if (i % (100000000) == 0) {
                System.out.println("at idx: " + i + " val: " + f);
            }

            if (Double.isNaN(f)) {
                continue;
            }

            String st1 = floatFormat.format(f);

            DoubleToString.appendFormatted(buff, f, sigDigits);
            String st2 = buff.toString();

            if (!st1.equals(st2)) {
                // Allow for different rounding logic
                int slack;

                if (f > 1e17) {
                    slack = 2;
                } else  {
                    slack = 1;
                }

                if (!st1.substring(0,st1.length()-slack).equals(st2.substring(0,st2.length()-slack))) {
                    System.out.println("error: idx: " + i + " f: " + f + " st1: " + st1 + " st2: " + st2);
                }
            }

            //assertEquals("Strings match", st1, st2);
        }

    }

}
