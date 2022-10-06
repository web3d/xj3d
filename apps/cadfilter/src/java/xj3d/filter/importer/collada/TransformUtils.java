/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer.collada;

// External imports
import java.util.List;

import javax.vecmath.Matrix4f;


/**
 * Utility methods for handling TransformElements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
abstract class TransformUtils {

    /**
     * Return the data bound objects for the transform elements
     *
     * @param transform_list A list containing collada node transform elements
     * @return the data bound object for each transform element
     */
    static TransformElement[] getTransformElements(List<CElement> transform_list) {
        int num = transform_list.size();
        TransformElement[] te = new TransformElement[num];
        for (int i = 0; i < num; i++) {
            CElement element = transform_list.get(i);
            String name = element.getTagName();
            switch (name) {
                case ColladaStrings.TRANSLATE:
                    te[i] = new Translate(element);
                    break;
                case ColladaStrings.ROTATE:
                    te[i] = new Rotate(element);
                    break;
                case ColladaStrings.SCALE:
                    te[i] = new Scale(element);
                    break;
                case ColladaStrings.SKEW:
                    te[i] = new Skew(element);
                    break;
                case ColladaStrings.MATRIX:
                    te[i] = new Matrix(element);
                    break;
                case ColladaStrings.LOOKAT:
                    te[i] = new Lookat(element);
                    break;
            }
        }
        return(te);
    }

    /**
     * Consolidated the array of transform elements into the argument matrix
     *
     * @param te The array of TransformElements
     */
    static void getMatrix(TransformElement[] te, Matrix4f matrix) {
        Matrix4f m = new Matrix4f();
        matrix.setIdentity();
        for (TransformElement te1 : te) {
            te1.getMatrix(m);
            matrix.mul(m);
        }
    }
}
