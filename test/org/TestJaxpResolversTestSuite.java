package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

import org.web3d.x3d.jaxp.TestAll;

/**
 *
 * @author terry
 */
public class TestJaxpResolversTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("JAXP Entity Resolver Tests");

        suite.addTest(TestAll.suite());

        return suite;
    }

}