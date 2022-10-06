
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.X3DScene;

/**
 * SAIUtilities /**
 *
 */
public class SAIUtilities {

    /**
     * Detaches the root nodes from a scene so that they can be used elsewhere.
     *
     * @param scene The scene to remove the nodes from
     * @return The newly detached nodes
     */
    public static X3DNode[] extractRootNodes(X3DScene scene) {
        X3DNode rootNodes[] = scene.getRootNodes();
        for (X3DNode rootNode : rootNodes) {
            scene.removeRootNode(rootNode);
        }
        return rootNodes;
    }

}
