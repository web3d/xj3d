package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;
import org.web3d.vrml.parser.TestVRML97Reader;

// Internal Tests
import org.web3d.vrml.parser.vrml97.TestVRML97FieldParser;

/**
 * Top level test suite for the vrml 97 field parser package
 * @author Terry Norbraten
 * @version
 */
public class TestVRML97FieldParserTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("VRML97 Field Parser Tests");

        suite.addTest(TestVRML97Reader.suite());
        suite.addTest(TestVRML97FieldParser.suite());

        return suite;
    }

}