package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests
import org.web3d.util.*;
import org.web3d.util.spatial.*;

/**
 * Top level test suite for the Web3D Util package
 * @author Terry Norbraten
 * @version
 */
public class TestWeb3DUtilTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Web3D Util Tests");

        suite.addTest(IntHashSetTest.suite());
        suite.addTest(TestArrayUtils.suite());
        suite.addTest(GridTrianglePartitionTest.suite());

        return suite;
    }

}
