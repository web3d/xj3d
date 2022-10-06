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
import javax.vecmath.Matrix4f;

/**
 * Data binding for Collada <scale> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Scale extends TransformElement {
    
    /**
     * Constructor
     * 
     * @param scale_element The Element
     */
    Scale(CElement scale_element) {
        super("scale", scale_element);
        x3d_field_name = "scale";
    }
    
    /**
     * Return the value of this transform element in the argument Matrix
     * 
     * @return the value of this transform element in the argument Matrix
     */
    @Override
    void getMatrix(Matrix4f matrix) {
        matrix.setIdentity();
        matrix.m00 = value[0];
        matrix.m11 = value[1];
        matrix.m22 = value[2];
    }
}
