/**
 * A test class to check on Browser.createVrmlFromString() functionality.
 *
 * Trigger the create process from an event.
 */
import vrml.*;
import vrml.node.*;

public class LoadURLTest extends Script {

    /** The string of URLs that we are going to create */
    private static final String[] URL_LIST = { "create_url_target.wrl" };

    /** Parameter list for loadURL call - empty */
    private static final String[] PARAMETERS = {};

    public LoadURLTest() {
    }

    @Override
    public void processEvent(Event evt) {

        System.out.println("About to call load method");

        URL_LIST[0] = browser.getWorldURL() + URL_LIST[0];

        browser.loadURL(URL_LIST, PARAMETERS);
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown called");
    }
}
