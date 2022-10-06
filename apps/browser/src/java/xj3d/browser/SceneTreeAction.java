/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// External imports
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.web3d.vrml.renderer.ogl.browser.OGLStandardBrowserCore;

/**
 * An action that displays a tree view of the scene.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class SceneTreeAction extends AbstractAction {

    /** The brower core */
    private OGLStandardBrowserCore core;

    /** The position to place the viewer.  Layout constant */
    private String position;

    /** The tree viewer */
    private SceneTreeViewer sceneTree;

    Xj3DBrowser surfaceManager;

    /**
     * Create an instance of the action class.
     *
     * @param core the browser core containing node info
     * @param position the position to insert the tree in the main frame
     * @param surfaceManager the surface resetting action object
     */
    public SceneTreeAction(OGLStandardBrowserCore core, String position, Xj3DBrowser surfaceManager) {
        super("Scene Tree");

        this.core = core;
        this.position = position;
        this.surfaceManager = surfaceManager;

        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                                   KeyEvent.ALT_DOWN_MASK);

        putValue(ACCELERATOR_KEY, acc_key);
        putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        putValue(SHORT_DESCRIPTION, "View Scene Tree");
    }

    //----------------------------------------------------------
    // Methods required for ActionListener
    //----------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (sceneTree == null) {
            sceneTree = new SceneTreeViewer(core, new NullNodeFilter());
            surfaceManager.add(sceneTree, position);
        } else {
            sceneTree.setVisible(false);
            surfaceManager.getContentPane().remove(sceneTree);
            sceneTree = null;
        }
        surfaceManager.resetSurface();
    }
}
