import java.util.Map;
import org.web3d.x3d.sai.Browser;
import org.web3d.x3d.sai.SFTime;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DRoute;
import org.web3d.x3d.sai.X3DScriptImplementation;
import org.web3d.x3d.sai.X3DScriptNode;
import org.web3d.x3d.sai.X3DExecutionContext;

public class AddRemoveRoute implements X3DScriptImplementation, X3DFieldEventListener {
    private Map myFieldMap=null;
    private X3DScriptNode mySelf=null;
    private SFTime redBoxClicked;
    private Browser myBrowser;
    private X3DRoute myRoute=null;

    /** Creates a new instance of First */
    public AddRemoveRoute() {
    }

    public void setBrowser(Browser browser) {
        this.myBrowser=browser;
    }

    public void setFields(X3DScriptNode externalView, Map fields) {
        this.mySelf=externalView;
        this.myFieldMap=fields;
    }

    public void initialize() {
        redBoxClicked=(SFTime)myFieldMap.get("redBoxClicked");
        redBoxClicked.addX3DEventListener(this);
        System.out.println("Script Loaded!");
    }

    public void shutdown() {
    }

    public void eventsProcessed() {
    }

    public void readableFieldChanged(X3DFieldEvent evt) {
        System.out.println("Clicked!");
        X3DExecutionContext ec=myBrowser.getExecutionContext();
        if(myRoute==null) {
            System.out.println("Route added");
            myRoute=ec.addRoute(ec.getNamedNode("OI"),"value_changed",
                                ec.getNamedNode("greenBox"),"set_rotation");
        }else {
            System.out.println("Route removed");
            ec.removeRoute(myRoute);
            myRoute=null;
        }
    }

}
