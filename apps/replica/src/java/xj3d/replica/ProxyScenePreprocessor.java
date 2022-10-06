/*****************************************************************************
 *                        Web3d.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.replica;

// External Imports
import java.util.Iterator;
import java.util.List;

// Local imports
import org.web3d.vrml.nodes.VRMLScene;

import org.xj3d.ui.construct.Construct;
import org.xj3d.ui.construct.ScenePreprocessor;

/**
 * This module contains lists of scene preprocessor object which
 * gets pre processed before it gets processed in the scene manager.
 * Any scene from the previous call will be appended with the current
 * scene.
 *
 * @author Sang Park
 * @version $Revision: 1.1 $
 */
public class ProxyScenePreprocessor implements ScenePreprocessor
{
    /**
     * Lists of the scene preprocessors
     */
    private List<ScenePreprocessor> preprocessorList;

    /**
     * Constructs instance of ProxyScenePreprocessor
     *
     * @param preprocessorList The lists of the preprocessors
     */
    public ProxyScenePreprocessor(List<ScenePreprocessor> preprocessorList) {

        this.preprocessorList = preprocessorList;
    }

    //----------------------------------------------------------
    // Methods defined by ScenePreprocessor
    //----------------------------------------------------------
    @Override
    public void preprocess(VRMLScene scene, Construct construct) {

        // Preprocess all the scene processors in the list
        Iterator<ScenePreprocessor> i = preprocessorList.iterator();
        while (i.hasNext()) {
            ScenePreprocessor obj = i.next();
            obj.preprocess(scene, construct);
        }
    }
}
