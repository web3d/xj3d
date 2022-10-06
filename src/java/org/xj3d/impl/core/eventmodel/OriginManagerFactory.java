/*****************************************************************************
 *                        Web3d.org Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// External imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Local imports
import org.web3d.vrml.nodes.FrameStateManager;
import org.xj3d.core.eventmodel.OriginManager;

/**
 * Factory class for obtaining an OriginManager instance for a browser.
 * The OriginManager's are unique per browser instance and are identified
 * by the browser's FrameStateManager.
 *
 * @author Rex Melton
 * @version $Revision: 1.5 $
 */
public class OriginManagerFactory {

    /** Store of OriginManagers, keyed by FrameStateManager */
    private static Map<FrameStateManager, OriginManager> map;

    /** Protected Constructor */
    private OriginManagerFactory() {
    }

    /**
     * Return the OriginManager for the argument FrameStateManager.
     * If none exists, create one.
     *
     * @param fsm The browser's FrameStateManager.
     * @return An OriginManager
     */
    public static OriginManager getInstance(FrameStateManager fsm) {
        OriginManager om = null;

        if (fsm == null) {
            if (map.size() > 0) {
                // alan: Proto's nodes have no frame state manager till they
                // are instanced into a real renderer.  Just return something
                // sensable as it won't get used.
                Iterator<OriginManager> i = map.values().iterator();
                om = i.next();
            }
        }

        if (map == null) {
            map = new HashMap<>();
        } else {
            om = map.get(fsm);
        }

        if (om == null) {
            om = new DefaultOriginManager();
            map.put(fsm, om);
        }
        return om;
    }

    /**
     * Delete the OriginManager for the argument FrameStateManager.
     *
     * @param fsm The browser's FrameStateManager.
     */
    public static void removeInstance(FrameStateManager fsm) {
        if (map != null) {
            map.remove(fsm);
        }
    }
}
