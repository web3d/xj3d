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
import junit.textui.TestRunner;

import java.io.*;

import java.util.*;

// Internal Imports
// None

/**
 * Test Collada Filter
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class TestColladaFilter extends BaseTestFilter {

	   /**
     * The filter argument name
     */
    public static final String FILTER_NAME = "Identity";

    /**
     * The directory containing the files to pass through the filter
     */
    public static final String PARSETEST = "parsetest/filter/collada/";

    /**
     * The file types from the directory to use
     */
    public static final String[] EXTENSIONS = new String[]{".dae"};

    /**
     * Creates a test suite consisting of all the methods that start with
     * "test".
     * @return
     */
    public static Test suite() {
        return new TestSuite(TestColladaFilter.class);
    }

    /**
     * Main method to kick everything off with.
     * @param argv
     */
    public static void main(String[] argv) {
        TestRunner.run(suite());
    }

    /**
     * Collada Test Case.
     * <p>
     * This does nothing more than pass the test file set through the filter.
     * Whether the filter does what it's supposed to other than 'not crash', is
     * not determined.
     */
    public void testCollada() {

        List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME);

        List<String> args = new ArrayList<>();

        List<String> validArgs = new ArrayList<>();

        for (String ext : EXTENSIONS) {
            ExtFilter extFileFilter = new ExtFilter(ext);
            File sourceDir = new File(PARSETEST);
            File[] sourceFiles = sourceDir.listFiles(extFileFilter);
            if (sourceFiles != null) {
                for (File sourceFile : sourceFiles) {
                    String file = sourceFile.getPath();
                    try {
                        int err_code = executeFilter(filters, args, file, PARSETEST, ".x3db",
                                null, validArgs);

                        assertEquals(FILTER_NAME + ": Error code not 0: " + file, 0, err_code);

                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        fail(FILTER_NAME + ": IOError in parsing: " + file);
                    }
                }
            }
        }
    }
}
