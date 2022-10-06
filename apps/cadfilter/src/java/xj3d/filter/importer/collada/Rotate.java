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
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

/**
 * Data binding for Collada <rotate> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Rotate extends TransformElement {
    
    /** The rotation value */
    AxisAngle4f axisAngle;
    
    /**
     * Constructor
     * 
     * @param rotate_element The Element
     */
    Rotate(CElement rotate_element) {
        super("rotate", rotate_element);
        x3d_field_name = "rotation";
        if (value[3] != 0) {
            // convert the angle from degrees to radians
            value[3] = (float)(value[3] * Math.PI / 180);
        }
        axisAngle = new AxisAngle4f(value);
    }
    
    /**
     * Return the value of this transform element in the argument Matrix
     * 
     * @return the value of this transform element in the argument Matrix
     */
    @Override
    void getMatrix(Matrix4f matrix) {
        matrix.setIdentity();
        matrix.setRotation(axisAngle);
    }
}
