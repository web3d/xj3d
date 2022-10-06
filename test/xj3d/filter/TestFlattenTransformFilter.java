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
 * Test FlattenTransform Filter
 *
 * @author Alan Hudson
 * @version
 */
public class TestFlattenTransformFilter extends BaseTestFilter {
    public static final String PARSETEST = "parsetest/filter/FlattenTransform/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestFlattenTransformFilter.class);
    }

    /**
     * Transform Test Case.
     */
    public void testSimple() {
        String file = PARSETEST + "simple.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("FlattenTransform");

        List<String> args = new ArrayList<String>();

        List<String> validArgs = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", validArgs);

            assertEquals("Error code not 0", 0, err_code);
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Transform Test Case.
     */
    public void testCoffeeCup() {
        String file = PARSETEST + "coffeeCup.x3d";

        List<String> filters = new ArrayList<String>();
        filters.add("FlattenTransform");

        List<String> args = new ArrayList<String>();

        List<String> validArgs = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", validArgs);

            assertEquals("Error code not 0", 0, err_code);
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Transform Test Case.
     */
    public void testConglomeration() {
        String file = PARSETEST + "conglomeration.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("FlattenTransform");

        List<String> args = new ArrayList<String>();

        List<String> validArgs = new ArrayList<String>();
		validArgs.add("-countNode1");
        validArgs.add("Transform EQ 0");
		validArgs.add("-countNode2");
        validArgs.add("Group EQ 0");
		validArgs.add("-countNode3");
        validArgs.add("Shape EQ 15");

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.CountFilterValidator", validArgs);

            assertEquals("Error code not 0", 0, err_code);
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }
}

