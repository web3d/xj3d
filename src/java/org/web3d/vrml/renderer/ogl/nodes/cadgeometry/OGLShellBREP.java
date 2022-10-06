package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseShellBREP;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

/**
 *
 * @author terry
 */
public class OGLShellBREP extends BaseShellBREP implements OGLGeometryNodeType {

    /**
     * Construct a default WireBREP instance
     */
    public OGLShellBREP() {
        renderer = new CADKernelRenderer();
    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLShellBREP(VRMLNodeType node) {
        super(node);
    }

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    @Override
    public Geometry getGeometry() {
        return null;
    }

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    @Override
    public void setupFinished() {
        if (!inSetup) {
            return;
        }

        renderer.initialise();

	//    	Each face is rendered independently
        //    	renderer.render();
        for (VRMLNodeType f : vfFace) {
            f.setupFinished();
        }

        if (!inSetup) {
            return;
        }
        super.setupFinished();

    }

    @Override
    public boolean isSolid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setTextureCount(int count) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
