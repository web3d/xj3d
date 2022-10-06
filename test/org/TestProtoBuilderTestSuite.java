package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests
import org.web3d.vrml.nodes.proto.TestProtoBuilder;

/**
 * Top level test suite for the vrml proto node package
 * @author Terry Norbraten
 * @version
 */
public class TestProtoBuilderTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("VRML Proto Builder Tests");

        suite.addTest(TestProtoBuilder.suite());

        return suite;
    }

}