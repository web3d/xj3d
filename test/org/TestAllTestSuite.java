package org;

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * JUnit 4 master level test suite
 * @author Terry Norbraten
 */
public class TestAllTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Run All JUnit Tests");

        suite.addTest(TestCompressionToolsTestSuite.suite());
        suite.addTest(TestContentLoaderTestSuite.suite());
        suite.addTest(TestFiltersTestSuite.suite());
        suite.addTest(TestJaxpResolversTestSuite.suite());
        suite.addTest(TestProtoBuilderTestSuite.suite());
        suite.addTest(TestVRML97FieldParserTestSuite.suite());
        suite.addTest(TestWeb3DUtilTestSuite.suite());

        return suite;
    }
}