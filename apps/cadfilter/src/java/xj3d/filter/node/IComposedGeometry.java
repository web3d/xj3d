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
 * Marker interface for the ComposedGeometryNode.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface IComposedGeometry extends IGeometry {
    
    /**
     * Set the Coordinate node wrapper
     *
     * @param coord The Coordinate node wrapper
     */
    public void setCoordinate(Encodable coord);
    
    /**
     * Get the Coordinate node wrapper
     *
     * @return The Coordinate node wrapper
     */
    public Encodable getCoordinate();
    
    /**
     * Set the Color node wrapper
     *
     * @param color The Color node wrapper
     */
    public void setColor(Encodable color);
    
    /**
     * Get the Color node wrapper
     *
     * @return The Color node wrapper
     */
    public Encodable getColor();
    
    /**
     * Set the Normal node wrapper
     *
     * @param normal The Normal node wrapper
     */
    public void setNormal(Encodable normal);
    
    /**
     * Get the Normal node wrapper
     *
     * @return The Normal node wrapper
     */
    public Encodable getNormal();
    
    /**
     * Set the TextureCoordinate node wrapper
     *
     * @param texCoord The TextureCoordinate node wrapper
     */
    public void setTextureCoordinate(Encodable texCoord);
    
    /**
     * Get the TextureCoordinate node wrapper
     *
     * @return The TextureCoordinate node wrapper
     */
    public Encodable getTextureCoordinate();
}
