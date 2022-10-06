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

package xj3d.filter.node;

// External imports

// Local imports

/**
 * Marker interface for the X3DShapeNode.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface IShape extends IChild { 
    
    /**
     * Set the Geometry node wrapper
     *
     * @param geometry The Geometry node wrapper
     */
    public void setGeometry(Encodable geometry);
    
    /**
     * Get the Geometry node wrapper
     *
     * @return The Geometry node wrapper
     */
    public Encodable getGeometry();
    
    /**
     * Set the Appearance node wrapper
     *
     * @param appearance The Appearance node wrapper
     */
    public void setAppearance(Encodable appearance);
    
    /**
     * Get the Appearance node wrapper
     *
     * @return The Appearance node wrapper
     */
    public Encodable getAppearance();
}
