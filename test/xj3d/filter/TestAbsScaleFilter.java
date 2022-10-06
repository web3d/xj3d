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
 * Test AbsScale Filter
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class TestAbsScaleFilter extends BaseTestFilter {

    public static final String PARSETEST = "parsetest/filter/AbsScale/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     * @return
     */
    public static Test suite() {
        return new TestSuite(TestAbsScaleFilter.class);
    }

    /**
     * AbsScale Test Case.
     */
    public void testAbsScale() {
        String file = PARSETEST + "square.x3dv";

        List<String> filters = new ArrayList<>();
        filters.add("AbsScale");

        List<String> args = new ArrayList<>();

        args.add("-scale");
        args.add("2");

        List<String> validArgs = new ArrayList<>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                "xj3d.filter.IdentityFilterValidator", validArgs);

            assertEquals("Error code not 0", 0, err_code);
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }
}

