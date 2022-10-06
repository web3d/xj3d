package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.Shape3D;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseWireBREP;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

/**
 *
 * @author terry
 */
public class OGLWireBREP extends BaseWireBREP implements OGLGeometryNodeType {

    /**
     * The geometry implementation
     */
    private LineStripArray impl;

    /**
     * Construct a default WireBREP instance
     */
    public OGLWireBREP() {
        cadKernelRenderer = new CADKernelRenderer();
    }

    /**
     * Construct a new instance of this node based on the details from the given
     * node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLWireBREP(VRMLNodeType node) {
        super(node);
    }

    /**
     * Returns a OGL Geometry node
     *
     * @return A Geometry node
     */
    @Override
    public Geometry getGeometry() {
        return impl;
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
        Shape3D s = new Shape3D();
        s.setGeometry(impl);
        return s;
    }

    @Override
    public void setupFinished() {

        for (VRMLNodeType an : vfWire) {
            an.setupFinished();
        }

        cadKernelRenderer.initialise();

//    	errorReporter.messageReport("rendering WireBREP");
        if (!inSetup) {
            return;
        }

        super.setupFinished();

        impl = (LineStripArray) cadKernelRenderer.getWireBREPImpl();
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
