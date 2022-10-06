/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// Standard imports
import java.util.Map;

// Application specific imports
import org.web3d.x3d.sai.*;

public class SAIExample4
    implements X3DScriptImplementation, X3DFieldEventListener {

    /** A mapping for fieldName(String) to an X3DField object */
    private Map<String, X3DField> fields;

    /** A reference to the browser */
    private Browser browser;

    /** inputOnly touchTime */
    private SFTime touchTime;

    /** initializeOnly selfRef */
    private X3DScriptNode selfRef;

    //----------------------------------------------------------
    // Methods from the X3DScriptImplementation interface.
    //----------------------------------------------------------

    /**
     * Set the browser instance to be used by this script implementation.
     *
     * @param browser The browser reference to keep
     */
    @Override
    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    /**
     * Set the listing of fields that have been declared in the file for
     * this node. .
     *
     * @param externalView the external view of ourselves, so you can add routes to yourself
     *    using the standard API calls
     * @param fields The mapping of field names to instances
     */
    @Override
    public void setFields(X3DScriptNode externalView, Map<String, X3DField> fields) {
        this.fields = fields;
        selfRef = externalView;
    }

    /**
     * Notification that the script has completed the setup and should go
     * about its own internal initialization.
     */
    @Override
    public void initialize() {
        touchTime = (SFTime) fields.get("touchTime");

        // Listen to events on touchTime
        touchTime.addX3DEventListener(this);

        // Create nodes directly in the parent scene
        X3DScene scene = (X3DScene) browser.getExecutionContext();

        X3DShapeNode shape = (X3DShapeNode) scene.createNode("Shape");
        X3DGeometryNode box = (X3DGeometryNode) scene.createNode("Box");
        X3DNode touchSensor = scene.createNode("TouchSensor");

        shape.setGeometry(box);

        // Create a Group to hold the nodes
        X3DGroupingNode group = (X3DGroupingNode) scene.createNode("Group");

        // Add the nodes to the scene
        scene.addRootNode(group);

        MFNode addChildren = (MFNode) group.getField("addChildren");

        // Add the shape and sensor to the group
        addChildren.setValue(1, new X3DNode[] {shape});
        addChildren.setValue(1, new X3DNode[] {touchSensor});

        // Get a handle to the toplevel execution context
        scene.addRoute(touchSensor,"touchTime", selfRef, "touchTime");
    }

    /**
     * Notification that this script instance is no longer in use by the
     * scene graph and should now release all resources.
     */
    @Override
    public void shutdown() {
    }

    /**
     * Notification that all the events in the current cascade have finished
     * processing.
     */
    @Override
    public void eventsProcessed() {
    }

    //----------------------------------------------------------
    // Methods from the X3DFieldEventListener interface.
    //----------------------------------------------------------

    /**
     * Handle field changes.
     *
     * @param evt The field event
     */
    @Override
    public void readableFieldChanged(X3DFieldEvent evt) {
        if (evt.getSource() == touchTime) {
            System.out.println("Poke!");
        } else {
            System.out.println("Unhandled event: " + evt);
        }
    }
}