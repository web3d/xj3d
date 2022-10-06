/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// Local imports
import org.web3d.util.SFImageUtils;

import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;

/**
 * Write PixelTextures to a file. The files will be named 
 * PixelTexture_N.TYPE where N is a number starting at 0 
 * (zero) and incrementing for each PixelTexture found and
 * TYPE is the image type.
 * PixelTexture's that are USE'ed are not exported. The files
 * are written to the current user directory. No check is performed 
 * to prevent files with the same name from being overwritten.
 * <p>

 * <b>Filter Options</b>
 * <br>
 * <code>-imageType name</code> Specify the image type. 
 * This can be any type that the ImageIO package supports. 
 * The default is png.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class ExportPixelTextureFilter extends CommonEncodedBaseFilter {

	/** The default image type */
	private static final String DEFAULT_IMAGE_TYPE = "png";
		
	/** The logging identifier of this app */
    private static final String LOG_NAME = "ExportPixelTexture";

    /** Image type param */
    private static final String IMAGE_TYPE_PARAM = "-imageType";

	/* The working image type */
	private String imageType;
	
	/** The number of pixel textures processed */
	private int num_pix_texture;
	
    /**
     * Create an instance of the filter.
     */
    public ExportPixelTextureFilter() {
		
		imageType = DEFAULT_IMAGE_TYPE;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
        @Override
    public void endDocument() throws SAVException, VRMLException {
		
        super.endDocument();
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
        @Override
    public void endNode() throws SAVException, VRMLException {
		
		CommonEncodable node = (CommonEncodable)encStack.peek();
		String nodeName = node.getNodeName();
		if (nodeName.equals("PixelTexture") && (node.getUseName() == null)) {
			
			ArrayData image_data = (ArrayData)node.getValue("image");
			if (image_data != null) {
				
				int num_value = image_data.num;
				int[] data = (int[])image_data.data;
				int width = data[0];
				int height = data[1];
				if ((width != 0) && (height != 0)) {
					
					int components = data[2]; 
					int num_pixel = num_value - 3;
					int[] pixels = new int[num_pixel];
					System.arraycopy(data, 2, pixels, 0, num_pixel);
					
					BufferedImage image = (BufferedImage)SFImageUtils.convertDataToRenderedImage(
						width, 
						height, 
						components, 
						pixels);
					
					File outputFile = new File("PixelTexture_"+ 
						Integer.toString(num_pix_texture++) +"."+ 
						imageType);
					
					try {
						System.out.println("Writing image file: "+ outputFile);
						ImageIO.write(image, imageType, outputFile);
					} catch (IOException e) {
						System.out.println("Error writing image file: "+ 
							outputFile +": "+ e.getMessage());
					}
				}
			}
		}
		super.endNode();
	}
	
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the argument parameters to control the filter operation.
     *
     * @param args The array of argument parameters.
     */
        @Override
    public void setArguments(String[] args) {

        super.setArguments(args);

        String prefix = "-" + LOG_NAME + ":";
        String arg;

        for (int i = 0; i < args.length; i++) {
            arg = args[i];

            if (arg.startsWith(prefix)) {
                arg = "-" + arg.substring(prefix.length());
            }

            if (arg.equals(IMAGE_TYPE_PARAM)) {
                if (i + 1 >= args.length){

                    throw new IllegalArgumentException(
                        "Not enough args for " + LOG_NAME + ".  " +
                        "Expecting one more to specify imageType.");
                }
                imageType = args[i+1];
            }
        }
    }
}
