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

package vrml.field;

// Standard imports
// none

// Application specific imports
import vrml.ConstField;

/**
 * VRML JSAI type class containing a fixed image field.
 * <p>
 *
 * Unlike th e other fields, this field always replaces the existing array
 * rather than copy values in. It assumes that pixel textures are not changed
 * all that often.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.5 $
 */
public class ConstSFImage extends ConstField {

    /** The width of the image */
    protected int imageWidth;

    /** The height of the image */
    protected int imageHeight;

    /** The number of components in the image pixels */
    protected int imageComponents;

    /** The raw pixel data */
    protected byte[] imagePixels;

    /**
     * Construct an instance with default values. Not available to mere
     * mortals.
     */
    protected ConstSFImage() {
        imagePixels = new byte[0];
    }

    /**
     * Create a new image based on the given details.
     *
     * @param width The width in pixels of the image
     * @param height The height in pixels of the image
     * @param components The number of components in the image [0-4]
     * @param pixels The raw pixels of the image
     */
    public ConstSFImage(int width, int height, int components, byte[] pixels) {
        imageWidth = width;
        imageHeight = width;
        imageComponents = components;

        if(pixels.length != imagePixels.length) {
            imagePixels = new byte[pixels.length];
        }

        System.arraycopy(pixels, 0, imagePixels, 0, pixels.length);
    }

    /**
     * Get the width of the image in pixels.
     *
     * @return The width of the image
     */
    public int getWidth() {
        return imageWidth;
    }

    /**
     * Get the height of the image in pixels.
     *
     * @return The height of the image
     */
    public int getHeight() {
        return imageHeight;
    }

    /**
     * Get the number of components of the image
     *
     * @return The number of components [0-4]
     */
    public int getComponents() {
        return imageComponents;
    }

    /**
     * Copy the image pixels into the user provided array.
     *
     * @param pixels The array to copy data into
     */
    public void getPixels(byte[] pixels) {
        System.arraycopy(imagePixels, 0, pixels, 0, imagePixels.length);
    }

    /**
     * Create a string representation of this field. Not implemented yet.
     *
     * @return A string representation of the vec
     */
    @Override
    public String toString() {
        return null;
    }

    /**
     * Create a cloned copy of this node.
     *
     * @return A complete copy of the node
     */
    @Override
    public Object clone() {
        return new ConstSFImage(imageWidth,
                           imageHeight,
                           imageComponents,
                           imagePixels);
    }
}
