package org;

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

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests
import org.web3d.vrml.export.compressors.TestCompressionTools;

/**
 * Compression Tools Test Suite
 *
 * @author Alan Hudson
 * @version
 */
public class TestCompressionToolsTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("CompressionTools Tests");

        suite.addTest(TestCompressionTools.suite());

        return suite;
    }
}
