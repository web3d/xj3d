/**
 * A test class to check on Browser.createVrmlFromString() functionality.
 *
 * Trigger the create process from an event.
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class CreateURLTest extends Script {

    /** The string of nodes that we are going to create */
    private static final String[] URL_LIST = { "create_url_target.wrl" };

    /** The field that holds the group we are writing values to */
    private BaseNode target;

    public CreateURLTest() {
    }

    @Override
    public void initialize() {
        SFNode group_field = (SFNode)getField("target");
        target = group_field.getValue();
    }

    @Override
    public void processEvent(Event evt) {

        System.out.println("About to call create method");

        URL_LIST[0] = browser.getWorldURL() + URL_LIST[0];

        browser.createVrmlFromURL(URL_LIST, target, "children");

    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown called");
    }
}