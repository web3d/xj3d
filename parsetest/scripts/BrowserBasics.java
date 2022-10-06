/**
 * A simple test class show the basic values that the Browser class gives
 * us.
 */
import vrml.*;
import vrml.node.*;

public class BrowserBasics extends Script {
    public BrowserBasics() {
    }

    @Override
    public void initialize() {
        System.out.println("Initialise called. About to fetch browser");

        Browser b = getBrowser();

        if (b == null) {
            System.out.println("Null browser reference!");
            return;
        }

        System.out.println("World URL: " + b.getWorldURL());
        System.out.println("Description: " + b.getDescription());
        System.out.println("Name: " + b.getName());
        System.out.println("Version: " + b.getVersion());
        System.out.println("Speed: " + b.getCurrentSpeed());
        System.out.println("Frame Rate: " + b.getCurrentFrameRate());
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown called");
    }
}