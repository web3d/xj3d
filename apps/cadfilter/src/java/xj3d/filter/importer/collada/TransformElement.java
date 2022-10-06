/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
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

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Data binding for Collada <translate> elements.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
abstract class TransformElement {
    
    /** The X3D Transform field name */
    String x3d_field_name;
    
    /** sid attribute */
    String sid;
    
    /** the content value */
    float[] value;
    
    /**
     * Constructor
     * 
     * @param tagName The required element tag name.
     * @param element The Element
     */
    TransformElement(String tagName, CElement element) {
        
        sid = element.getAttribute(ColladaStrings.SID);
        value = ((FloatContent)element).getFloatContent();
    }
    
    /**
     * Return the value of this transform element in the argument Matrix
     * 
     * @return the value of this transform element in the argument Matrix
     */
    void getMatrix(Matrix4f matrix) {
        matrix.setIdentity();
    }
}
