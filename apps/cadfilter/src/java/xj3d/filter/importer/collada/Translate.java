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
import javax.vecmath.Vector3f;

/**
 * Data binding for Collada <translate> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
class Translate extends TransformElement {
    
    /** The translation value */
    Vector3f vector;
    
    /**
     * Constructor
     * 
     * @param translate_element The Element
     */
    Translate(CElement translate_element) {
        super("translate", translate_element);
        x3d_field_name = "translation";
        vector = new Vector3f(value);
    }
    
    /**
     * Return the value of this transform element in the argument Matrix
     * 
     * @return the value of this transform element in the argument Matrix
     */
    @Override
    void getMatrix(Matrix4f matrix) {
        matrix.setIdentity();
        matrix.setTranslation(vector);
    }
}
