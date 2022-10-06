package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import org.j3d.aviatrix3d.SceneGraphObject;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseCADKernelRenderer;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseEdge;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 *
 * @author terry
 */
public class OGLEdge extends BaseEdge implements OGLVRMLNode{
	
    /**
     *
     */
    public OGLEdge(){
		super();
	}

    /**
     *
     */
    @Override
    public void render() {
		
	}

    /**
     *
     * @return
     */
    public BaseCADKernelRenderer getMesh() {
		return null;
	}
	
	@Override
	public void setupFinished() {
		for(VRMLNodeType vn:vfCurve)
		{
			vn.setupFinished();
		}
		
		for(VRMLNodeType vpn:vfPCurve)
		{
			vpn.setupFinished();
		}
		
		super.setupFinished();
	}

    @Override
	public SceneGraphObject getSceneGraphObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
