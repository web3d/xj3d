/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.lang;

/**
 * Listing of type constants for textures.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface TextureConstants {

    // Texture stage modes
    int MODE_MODULATE = 0;
    int MODE_REPLACE = 1;
    int MODE_MODULATE_2X = 2;
    int MODE_MODULATE_4X = 3;
    int MODE_ADD = 4;
    int MODE_ADD_SIGNED = 5;
    int MODE_ADD_SIGNED_2X = 6;
    int MODE_SUBTRACT = 7;
    int MODE_ADD_SMOOTH = 8;
    int MODE_BLEND_DIFFUSE_ALPHA = 9;
    int MODE_BLEND_TEXTURE_ALPHA = 10;
    int MODE_BLEND_FACTOR_ALPHA = 11;
    int MODE_BLEND_CURRENT_ALPHA = 12;
    int MODE_MODULATE_ALPHA_ADD_COLOR = 13;
    int MODE_MODULATE_INVCOLOR_ADD_ALPHA = 14;
    int MODE_OFF = 15;
    int MODE_SELECT_ARG1 = 16;
    int MODE_SELECT_ARG2 = 17;
    int MODE_DOTPRODUCT3 = 18;

    // Texture Types
    int TYPE_SINGLE_2D = 0;
    int TYPE_SINGLE_3D = 1;
    int TYPE_MULTI = 2;
    int TYPE_CUBIC_ENVIRONMAP = 3;
    int TYPE_PBUFFER = 4;

    // Source Types
    int SRC_COMBINE_PREVIOUS = 0;
    int SRC_DIFFUSE = 1;
    int SRC_SPECULAR = 2;
    int SRC_FACTOR = 3;

    // Function Types
    int FUNC_NONE = 0;
    int FUNC_COMPLEMENT = 1;
    int FUNC_ALPHA_REPLICATE = 2;

    // Boundary Modes
    int BM_WRAP = 0;
    int BM_CLAMP = 1;
    int BM_CLAMP_EDGE = 2;
    int BM_CLAMP_BOUNDARY = 3;
    int BM_MIRRORED_REPEAT = 4;

    // Magnification Filter Techniques
    int MAGFILTER_FASTEST = 0;
    int MAGFILTER_NICEST = 1;
    int MAGFILTER_BASE_LEVEL_POINT = 2;
    int MAGFILTER_BASE_LEVEL_LINEAR = 3;
    int MAGFILTER_LINEAR_DETAIL = 4;
    int MAGFILTER_LINEAR_DETAIL_RGB = 5;
    int MAGFILTER_LINEAR_DETAIL_ALPHA = 6;

    // Minification Filter Techniques
    int MINFILTER_FASTEST = 0;
    int MINFILTER_NICEST = 1;
    int MINFILTER_BASE_LEVEL_POINT = 2;
    int MINFILTER_BASE_LEVEL_LINEAR = 3;
    int MINFILTER_MULTI_LEVEL_POINT = 4;
    int MINFILTER_MULTI_LEVEL_LINEAR = 5;

    // Anistropic Mode
    int ANISOTROPIC_MODE_NONE = 0;
    int ANISOTROPIC_MODE_SINGLE = 1;
}
