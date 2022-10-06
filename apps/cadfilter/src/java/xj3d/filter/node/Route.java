/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
// None

// Local imports
import org.web3d.vrml.sav.RouteHandler;

/**
 * Wrapper for Route data.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class Route {
	
	/** Source Node */
	private String srcNodeName;
	
	/** Source Field */
    private String srcFieldName;
	
	/** Destination Node */
    private String destNodeName;
	
	/** Destination Field */
    private String destFieldName;

    /** The routeHandler to issue results to */
    private RouteHandler routeHandler;

	/**
	 * Constructor
     * @param destFieldName
	 */
	public Route(
		String srcNodeName, 
		String srcFieldName,
        String destNodeName,
        String destFieldName) {
		
		this.srcNodeName = srcNodeName;
    	this.srcFieldName = srcFieldName;
    	this.destNodeName = destNodeName;
    	this.destFieldName = destFieldName;
	}
	
    /**
     * Push to the RouteHandler
     */
    public void encode() {
        if (routeHandler != null) {
            routeHandler.routeDecl(
				srcNodeName,
                srcFieldName,
                destNodeName,
                destFieldName);
		}
    }
    
    /**
     * Set the route handler to the given instance. If the value is null it
     * will clear the currently set instance.
     *
     * @param routeHandler The route handler instance to use
     */
    public void setRouteHandler(RouteHandler routeHandler) {
        this.routeHandler = routeHandler;
    }
}
