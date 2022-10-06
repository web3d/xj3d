/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

/**
 * VRML eventIn class for SFImage.
 *  <p>
 * Images are represented as arrays of integers as per the VRML IS
 * specification Section 5.5 SFImage. Pixel values are between 0 and 256 and
 * represented as integers to maintain consistency with java's ImageConsumer
 * interface and PixelGrabber class.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventInSFImage extends EventIn
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFImage.
   */
  protected EventInSFImage()
  {
    super(SFImage);
  }

  /**
   * Set the image value in the given eventIn.
   *  <p>
   * Image values are specified using a width, height and the number of
   * components. The number of items in the pixels array must be at least
   * <code>width * height</code>. If there are less items than this an
   * ArrayIndexOutOfBoundsException will be generated. The integer values
   * are represented according to the number of components. If the integer
   * contains values in bytes that are not used by the number of components
   * for that image, the values are ignored.
   *  <p>
   *  <b>1 Component Images </b> <br>
   * The integer has the intensity value stored in the lowest byte and can be
   * obtained:
   *  <pre>
   *    intensity = pixel[i] &amp;0xFF;
   *  </pre>
   *  <p>
   *  <b>2 Component Images </b> <br>
   * The integer has the intensity value stored in the lowest byte and the
   * transparency in the top byte:
   *  <pre>
   *    intensity = pixel[i] &amp;0xFF;
   *    alpha = (pixel[i] &gt;&gt; 24) &amp;0xFF00;
   *  </pre>
   * <i>Note</i> that this is different to the VRML representation of the image
   * which would store the values in the text file as
   * <code>alpha = pixel[i] &gt;&gt; 8) &amp;0xFF</code>. The reason for the difference
   * is to maintain as much compatibility with the java image model as
   * possible. Java does not contain a separate intensity only image model,
   * instead it sets all three colour components to the same value. This way,
   * the user of SFImages can take a full colour image and turn it to black + white by
   * just setting the number of components to 2 - particularly if the three
   * colour components are the same (which they are for an intensity only
   * image) which will result in the correct intepretation of the image.
   *  <p>
   *  <b>3 Component Images </b> <br>
   * The three colour components are stored in the integer array as follows:
   *  <pre>
        red   = (pixel[i] &gt;&gt; 16) &amp;0xFF;
        green = (pixel[i] &gt;&gt;  8) &amp;0xFF;
        blue  = (pixel[i]      ) &amp;0xFF;
   *  </pre>
   *  <p>
   *  <b>4 Component Images </b> <br>
   * The integer has the value stored in the array as follows:
   *  <pre>
        alpha = (pixel &gt;&gt; 24) &amp;0xff;
        red   = (pixel &gt;&gt; 16) &amp;0xff;
        green = (pixel &gt;&gt;  8) &amp;0xff;
        blue  = (pixel      ) &amp;0xff;
   *  </pre>
   *  <p>
   * The width and height values must be greater than or equal to zero. The
   * number of components is between 1 and 4. Any value outside of these
   * bounds will generate an IllegalArgumentException.
   *
   * @param width The width of the image in pixels
   * @param height The height of the image in pixels
   * @param components The number of colour components [1-4]
   * @param pixels The array of pixel values as specified above.
   *
   * @exception IllegalArgumentException The number of components or width/
   *    height are illegal values.
   * @exception ArrayIndexOutOfBoundsException The number of pixels provided by the
   *    caller is not enough for the width * height.
   */
  public abstract void setValue(int width,
                                int height,
                                int components,
                                int[] pixels);
}









