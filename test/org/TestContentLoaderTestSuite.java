package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests
import org.xj3d.core.loading.*;

/**
 * Top level test suite for the core loading package
 * @author Terry Norbraten
 * @version
 */
public class TestContentLoaderTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Content Loader Tests");

        suite.addTest(TestContentLoader.suite());

        return suite;
    }

}