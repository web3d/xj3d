/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.net.content;

// External imports
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;

import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ietf.uri.ResourceConnection;

import org.j3d.util.ImageUtils;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.net.content.ImageContentHandler;

import org.web3d.util.MathUtils;

/**
 * Content handler implementation for loading images with the AWT
 * toolkit from a URI resource connection. The loaded images are
 * preprocessed to the appropriate size (power of 2) and mipmaps
 * are generated if specified.
 * <p>
 *
 * The following properties are used by this class:
 * <ul>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.rescale</code> The
 *    method to use for rescaling textures.  Valid values are
 *    "NEAREST_NEIGHBOR, BILINEAR"</li>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.maxTextureSize</code> The
 *    maximum texture size to use.  By default texture sizes are unlimited.  Textures
 *    with a dimension over this value will be resized.  The resizing will try to
 *    preserve the aspect ratio.  This must be a power of two.</li>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.useMipMaps</code> Force the
 *    use of mipmaps</li>
 * </ul>
 *
 * @author  Rex Melton
 * @version $Revision: 1.5 $
 */
class AWTImageContentHandler extends ImageContentHandler {

    /**
     * Construct a new instance of the content handler.
     */
    AWTImageContentHandler( ) {
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Given a fresh stream from a ResourceConnection, read and create an object
     * instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    @Override
    public Object getContent(ResourceConnection resc)
        throws IOException {

        try {
            String url_string = resc.getURI( ).toExternalForm( );
            ///////////////////////////////////////////////////////////////
            // rem: workaround for bug #444.
            resc.getInputStream().close();
            ///////////////////////////////////////////////////////////////
            // first, check for the existance of our prefered image format
            image_class = getPreferedImageClass( );
            if ( image_class != null ) {
                // if the image class exists, we presume that
                // it's handler is configured
                Class<?>[] c = new Class[]{image_class};
                URLConnection connection = new URL( url_string ).openConnection( );
                Object content = connection.getContent( c );
                if ( image_class.isInstance( content ) ) {
                    NIOBufferImage niobi = convert( image_class, content );
                    if ( niobi != null ) {
                        // rescale the image if necessary and create mipmaps
                        NIOBufferImage[] imageArray = preprocess( niobi );
                        NIOBufferImage ret_img = consolidate( imageArray );
                        return( ret_img );
                    }
                }
            }
            // otherwise, fallback to default content and toolkit types
            BufferedImage image = null;

            // load the image through the ordinary content handler
            // so that we can convert it's format and pre-process
            URLConnection connection = new URL( url_string ).openConnection( );
            Object content = connection.getContent( );
            if ( content instanceof BufferedImage ) {
                image = (BufferedImage)content;
            }
            else if ( content instanceof ImageProducer ) {
                image = ImageUtils.createBufferedImage((ImageProducer)content);
            }

            if ( image != null ) {
                BufferedImage[] imageArray = preprocess( image );
                NIOBufferImage ret_img = consolidate( imageArray );
                return ret_img;
            }
        } catch (IOException ioe) {
            System.err.println( ioe.getMessage( ) );
        }

        return null;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Return an <code>NIOBufferImage</code> representation of the argument image.
     * If the image format could not be handled, <code>null</code> is returned.
     *
     * @param image The image to covert
     * @return The <code>NIOBufferImage</code> representation, or <code>null</code>
     * if the image could not be converted.
     */
    private NIOBufferImage toNIOBufferImage( BufferedImage image ) {

        int height = image.getHeight( );
        int width = image.getWidth( );

        ByteBuffer buffer = null;
        NIOBufferImageType format = null;
        boolean isGrayScale = false;

        int imageType = image.getType( );
        switch( imageType ) {

            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_INT_ARGB:

                format = NIOBufferImageType.RGBA;
                break;

            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:

                format = NIOBufferImageType.RGB;
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_USHORT_GRAY:

                format = NIOBufferImageType.INTENSITY;
                isGrayScale = true;
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_CUSTOM:

                int num_cmp = image.getColorModel( ).getNumComponents( );
                switch ( num_cmp ) {
                case 1:
                    format = NIOBufferImageType.INTENSITY;
                    isGrayScale = true;
                    break;
                case 2:
                    format = NIOBufferImageType.INTENSITY_ALPHA;
                    isGrayScale = true;
                    break;
                case 3:
                    format = NIOBufferImageType.RGB;
                    isGrayScale = checkGrayScale( image );
                    break;
                case 4:
                    format = NIOBufferImageType.RGBA;
                    isGrayScale = checkGrayScale( image );
                    break;
                }
        }

        if ( format != null ) {

            int num_cmp = format.size;

            buffer = ByteBuffer.allocateDirect( width * height * num_cmp );
            buffer.order( ByteOrder.nativeOrder( ) );

            switch ( num_cmp ) {

            case 4:

                int[] pixel = new int[width];

                for( int y = height - 1; y >= 0; y-- ) {
                    image.getRGB(0, y, width, 1, pixel, 0, width);
                    for( int x = 0; x < width; x++ ) {
                        int tmp = pixel[x];
                        buffer.put((byte)(tmp >> 16));
                        buffer.put((byte)(tmp >> 8));
                        buffer.put((byte)tmp);
                        buffer.put((byte)(tmp >> 24));
                    }
                }
                break;

            case 3:

                pixel = new int[width];

                for( int y = height - 1; y >= 0; y-- ) {
                    image.getRGB(0, y, width, 1, pixel, 0, width);
                    for( int x = 0; x < width; x++ ) {
                        int tmp = pixel[x];
                        buffer.put((byte)(tmp >> 16));
                        buffer.put((byte)(tmp >> 8));
                        buffer.put((byte)tmp);
                    }
                }
                break;

            case 2:

                Raster raster = image.getData( );
                ColorModel colorModel = image.getColorModel( );
                Object pixel_data = null;
                int[] cmp = null;

                for( int y = height - 1; y >= 0; y-- ) {
                    for( int x = 0; x < width; x++ ) {
                        pixel_data = raster.getDataElements( x, y, pixel_data );
                        cmp = colorModel.getComponents( pixel_data, cmp, 0 );
                        buffer.put((byte)cmp[0]);
                        buffer.put((byte)cmp[1]);
                    }
                }
                break;

            case 1:

                raster = image.getData( );
                DataBuffer imgData = raster.getDataBuffer( );

                for( int y = height - 1; y >= 0; y-- ) {
                    int image_index = y * width;
                    for( int x = 0; x < width; x++ ) {
                        buffer.put((byte)imgData.getElem( image_index++ ));
                    }
                }
                break;

            default:
                // Shouldn't get here unless a new - unsupported type is created
                System.err.println( "Unhandled NIOBufferImageType: " + format );
            }
        }
        return new NIOBufferImage(width, height, format, isGrayScale, buffer);
    }

    /**
     * Scale and create mipmaps of the argument image. The initial image will
     * be resized as necessary to dimensions that are a power of 2 and less
     * than or equal to the maximum size (if defined). Mipmaps will be generated
     * as specified from the resized initial image and the array of images returned.
     *
     * @param image The initial image
     * @return The array containing the scaled image and it's mipmaps
     */
    private BufferedImage[] preprocess( BufferedImage image ) {

        //int imageType = image.getType( );
        int width = image.getWidth( );
        int height = image.getHeight( );

        int newWidth = MathUtils.nearestPowerTwo( width, imageScaleUp );
        int newHeight = MathUtils.nearestPowerTwo( height, imageScaleUp );

        if (maxTextureSize > 0) {
            float factor;
            if (newWidth == newHeight) {
                if (newWidth > maxTextureSize) {
                    factor = maxTextureSize / (float) newWidth;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            } else if (newWidth > newHeight) {
                if (newWidth > maxTextureSize) {
                    factor = maxTextureSize / (float) newWidth;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            } else {
                if (newHeight > maxTextureSize) {
                    factor = maxTextureSize / (float) newHeight;
                    newWidth = (int)(factor * newWidth);
                    newHeight = (int)(factor * newHeight);
                }
            }
        }

        if ( width != newWidth || height != newHeight ) {
            image = scale( image, newWidth, newHeight );
        }

        BufferedImage[] ret_val;

        if ( useMipMaps ) {

            int level = Math.max( MathUtils.computeLog( newWidth ), MathUtils.computeLog( newHeight ) ) + 1;

            ret_val = new BufferedImage[level];
            ret_val[0] = image;

            for( int i = 1; i < level; i++ ) {
                if ( newWidth > 1 ) {
                    newWidth >>= 1;
                }
                if ( newHeight > 1 ) {
                    newHeight >>= 1;
                }
                ret_val[i] = scale( image, newWidth, newHeight );
            }
        } else {
            ret_val = new BufferedImage[1];
            ret_val[0] = image;
        }
        return ret_val;
    }

    /**
     * Convert the BufferedImage's into NIOBufferImages and
     * extract the individual image byte buffers from the resulting
     * NIOBufferImages and consolidate them into a single byte buffer
     * array in the returned NIOBufferImage. This is used to place
     * the rescaled image and it's mipmaps into a single container.
     *
     * @param imageArray The primary image and set of mipmaps
     * @return The consolidated NIOBufferImage
     */
    private NIOBufferImage consolidate( BufferedImage[] imageArray ) {
        NIOBufferImage ret_img = null;
        if ( ( imageArray != null ) && ( imageArray[0] != null ) ) {
            ret_img = toNIOBufferImage( imageArray[0] );
            int levels = imageArray.length;
            if ( levels > 1 ) {
                // if there are mipmaps, create the buffer array
                ByteBuffer[] buffer = new ByteBuffer[levels];
                buffer[0] = ret_img.getBuffer( );
                for ( int i = 1; i < levels; i++ ) {
                    NIOBufferImage image = toNIOBufferImage( imageArray[i] );
                    buffer[i] = image.getBuffer( );
                }
                ret_img.setBuffer( buffer );
            }
            for ( int i = 0; i < levels; i++ ) {
                imageArray[i].flush( );
                imageArray[i] = null;
            }
        }
        return ret_img;
    }

    /**
     * Scale an image.  Generally used to scale an image to a power of 2.
     *
     * @param image The image to scale
     * @param newWidth The new width
     * @param newHeight The new height
     */
    private BufferedImage scale(
        BufferedImage image,
        int newWidth,
        int newHeight ) {

        int width = image.getWidth( );
        int height = image.getHeight( );
        if ( ( width == newWidth ) && ( height == newHeight ) ) {
            return( image );
        }
        boolean hasAlpha = image.getColorModel( ).hasAlpha( );

        if ( hasAlpha && ( newWidth <= 64 ) && ( newHeight <= 64 ) ) {
            return scalePretty( image, newWidth, newHeight );
        }

        // NOTE: floating point cast required here (TDN)
        double xScale = (float)newWidth / (float)width;
        double yScale = (float)newHeight / (float)height;

        AffineTransform at = AffineTransform.getScaleInstance( xScale, yScale );
        AffineTransformOp atop = new AffineTransformOp( at, rescale );

        BufferedImage ret_image;
        if ( hasAlpha ) {
            ret_image = atop.filter( image, null );
        } else {
            ret_image = new BufferedImage( newWidth, newHeight, image.getType( ) );
            atop.filter( image, ret_image );
        }
        return ret_image;
    }

    /**
     * Scale an image.  Generally used to scale an image to a power of 2.
     *
     * @param image The image to scale
     * @param newWidth The new width
     * @param newHeight The new height
     */
    private BufferedImage scalePretty(
        BufferedImage image,
        int newWidth,
        int newHeight ) {

        Image rimg = image.getScaledInstance(
            newWidth,
            newHeight,
            Image.SCALE_AREA_AVERAGING );

        boolean hasAlpha = image.getColorModel( ).hasAlpha( );

        BufferedImage ret_image;
        // Not sure why .getType doesn't work right for this
        if ( hasAlpha ) {
            ret_image = new BufferedImage( newWidth, newHeight, BufferedImage.TYPE_INT_ARGB );
        } else {
            ret_image = new BufferedImage( newWidth, newHeight, BufferedImage.TYPE_INT_RGB );
        }
        Graphics2D g2 = ret_image.createGraphics( );
        g2.drawImage( rimg, 0, 0, null );

        return ret_image;
    }

    /**
     * Return whether the argument image should be treated as grayscale.
     *
     * @return whether the argument image should be treated as grayscale.
     */
    private boolean checkGrayScale( BufferedImage image ) {
        // rem //////////////////////////////////////////////////////////
        // this is a hack really. the vlc image loader purposely
        // creates gray-alpha images as 4 component with a
        // specific configuration that we can detect. this
        // is done to prevent the rescaling issues that seem
        // to be specific to 2 component images of type
        // BufferedImage.TYPE_CUSTOM
        //////////////////////////////////////////////////////////////////
        boolean isGrayScale = false;
        ColorModel colorModel = image.getColorModel( );
        if (colorModel instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel)colorModel;

            int rmask = dcm.getRedMask();
            int gmask = dcm.getGreenMask();
            int bmask = dcm.getBlueMask();

            if (rmask == gmask && gmask == bmask) {
                isGrayScale = true;
            }
        }
        return isGrayScale;
    }
}
