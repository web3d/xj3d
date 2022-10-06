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
 * Test OBJ export
 *
 * @author Alan Hudson
 * @version
 */
public class TestOBJExport extends BaseTestFilter {
    public static final String PARSETEST = "parsetest/filter/obj/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestOBJExport.class);
    }

    /**
     * Single geometry test with only coords
     */
    public void testSingleGeometry() {
        String file = PARSETEST + "its.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Identity");

        List<String> args = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".obj",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Single geometry test with coords and normals
     */
    public void testGeomWithNormals() {
        String file = PARSETEST + "its_normal.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Identity");

        List<String> args = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".obj",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Single geometry test with coords and normals
     */
    public void testGeomWithTexCoord() {
        String file = PARSETEST + "its_texcoord.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Identity");

        List<String> args = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".obj",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

    /**
     * Single geometry test with coords and normals
     */
    public void testGeomWithAll() {
        String file = PARSETEST + "its_normal_texcoord.x3dv";

        List<String> filters = new ArrayList<String>();
        filters.add("Identity");

        List<String> args = new ArrayList<String>();

        try {
            int err_code = executeFilter(filters, args, file, PARSETEST, ".obj",
                "xj3d.filter.IdentityFilterValidator", null);

            assertEquals("Error code not 0", 0, err_code );
        } catch(IOException e) {
            e.printStackTrace(System.err);
            fail("IOError in parsing");
        }
    }

}

