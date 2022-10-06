package org;

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

// External Tests
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Tests
import xj3d.filter.*;

/**
 * Top level test that executes all tests available.
 *
 * @author Alan Hudson
 * @version
 */
public class TestFiltersTestSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Filter Tests");

        suite.addTest(TestAbsScaleFilter.suite());
        //suite.addTest(TestAppearanceReplacerFilter.suite());
        suite.addTest(TestCenterFilter.suite());
        suite.addTest(TestColladaFilter.suite());
        suite.addTest(TestColorRGBAtoRGBFilter.suite());
        //suite.addTest(TestCombineAppearanceFilter.suite());
        suite.addTest(TestCombineShapeFilter.suite());
        suite.addTest(TestCombineTransformFilter.suite());
        suite.addTest(TestDEFChooserFilter.suite());
        //suite.addTest(TestDEFReplacerFilter.suite());
        suite.addTest(TestFlattenTransformFilter.suite());
        suite.addTest(TestGenNormalsFilter.suite());
        suite.addTest(TestGlobalBoundsFilter.suite());
        suite.addTest(TestIdentityFilter.suite());
        suite.addTest(TestIndexFilter.suite());
        suite.addTest(TestLocalURLFilter.suite());
        suite.addTest(TestOBJExport.suite());
        suite.addTest(TestRemoveNodeFilter.suite());
        suite.addTest(TestTransformFilter.suite());
        suite.addTest(TestTriangulationFilter.suite());

        return suite;
    }
}
