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

package xj3d.filter;

// External Imports
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.*;

import java.util.*;

// Internal Imports
// None

/**
 * Test scaffold for testing filters.
 *
 * @author Alan Hudson
 * @version
 */
public class TestTransformFilter extends BaseTestFilter {
    public static final String PARSETEST = "parsetest/filter/Transform/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTransformFilter.class);
    }

    /**
     * Transform Test Case.
     */
    public void testTransformNoExistingData() {
        String file = PARSETEST + "NoExistingData.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Transform");

        List<String> args = new ArrayList<String>();

        args.add("-resetTransforms");
        args.add("-rotate1");
        args.add("1");
        args.add("0");
        args.add("0");
        args.add("1.5707");

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Transform Test Case.
     */
    public void testTransformExistingDataOneMore() {
        String file = PARSETEST + "ExistingDataOneMore.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Transform");

        List<String> args = new ArrayList<String>();

        args.add("-resetTransforms");
        args.add("-rotate1");
        args.add("1");
        args.add("0");
        args.add("0");
        args.add("1.5707");
        args.add("-translate2");
        args.add("1");
        args.add("0");
        args.add("0");

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Transform Test Case.
     */
    public void testTransformExistingDataOneLess() {
        String file = PARSETEST + "ExistingDataOneLess.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Transform");

        List<String> args = new ArrayList<String>();

        args.add("-resetTransforms");
        args.add("-rotate1");
        args.add("1");
        args.add("0");
        args.add("0");
        args.add("1.5707");

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }
}

