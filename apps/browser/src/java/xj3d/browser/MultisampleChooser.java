package xj3d.browser;

import com.jogamp.opengl.*;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * A class for determining the maximum antialiasing possible.
 *
 * @author Alan Hudson
 * @version $Id: $
 */
public class MultisampleChooser {

    /** Message when we can't find a matching format for the capabilities */
    private static final String NO_PIXEL_FORMATS_MSG =
        "WARNING: antialiasing will be disabled because none of the " +
        "available pixel formats had it to offer";

    /** Message the caller didn't request antialiasing */
    private static final String NO_AA_REQUEST_MSG =
        "WARNING: antialiasing will be disabled because the " +
        "DefaultGLCapabilitiesChooser didn't supply it";

    /** The number of samples we've discovered */
    private static int maxSamples = -1;

    private static boolean anyHaveSampleBuffers = false;

    /** Reporter instance for handing out errors */
    private static ErrorReporter errorReporter;

    public static int getMaximumNumSamples() {

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        GLDrawableFactory fac = GLDrawableFactory.getFactory(GLProfile.getDefault());

        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setSampleBuffers(true);

        // Set a max to compare with reality
        caps.setNumSamples(16);

        GLAutoDrawable ad = fac.createDummyAutoDrawable(fac.getDefaultDevice(), true, caps, new DefaultGLCapabilitiesChooser());
        ad.display();
        caps = (GLCapabilities) ad.getChosenGLCapabilities();

        if (caps != null) {
            if (caps.getNumSamples() > maxSamples) {
                maxSamples = caps.getNumSamples();
            }
            anyHaveSampleBuffers = caps.getSampleBuffers();
        }

        if (maxSamples == -1) {
            errorReporter.messageReport(NO_PIXEL_FORMATS_MSG);
        } else {
            if (!anyHaveSampleBuffers) {
                errorReporter.messageReport(NO_AA_REQUEST_MSG);
            }
        }

        return maxSamples;
    }

    public static void main(String[] args) {
        System.out.println("MAX NUM OF FSAA SAMPLES FOR THIS GPU ARE: " + getMaximumNumSamples());
    }
}
