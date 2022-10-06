/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes;

// Application specific imports
// None

/**
 * VRML representation of 2D texture properties.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface VRMLTextureProperties2DNodeType extends VRMLAppearanceChildNodeType {

    /**
     * Get the boundary color.  This is a 4 component color.
     *
     * @param color A preallocated 4 component color array;
     */
    void getBorderColor(float[] color);

    /**
     * Get the boundary width.
     *
     * @return The boundary width
     */
     int getBorderWidth();

     /**
      * Get the boundary mode for S.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     int getBoundaryModeS();

     /**
      * Get the boundary mode for T.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     int getBoundaryModeT();

     /**
      * Get the magnification filter.
      *
      * @return The mag filter.  Defined in TextureConstants.
      */
     int getMagnificationFilter();

     /**
      * Get the minification filter.
      *
      * @return The min filter.  Defined in TextureConstants.
      */
     int getMinificationFilter();

     /**
      * Get the generateMipsMaps field.
      *
      * @return Should mips be generated for this object.
      */
     boolean getGenerateMipMaps();

     /**
      * Get the Anisotropic Mode.
      *
      * @return The anisotropic mode.  Defined in TextureConstants.
      */
     int getAnisotropicMode();

     /**
      * Get the AnisotropicFilter Degree.
      *
      * @return The anisotropic degree.
      */
     float getAnisotropicDegree();

    /**
     * Get the texture compression setting.
     *
     * @return What texture compression mode to use
     */
    String getTextureCompression();

    /**
     * Get the texture priority.
     *
     * @return The texture priority for memory management.
     */
    float getTexturePriority();
}
