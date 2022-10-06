package org.web3d.vrml.renderer.ogl.nodes.nurbs;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.web3d.vrml.renderer.common.nodes.nurbs.BaseContourPolyline2D;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

import net.jgeom.nurbs.UVCoord2f;

public class OGLContourPolyline2D extends BaseContourPolyline2D implements
        TrimSegment, OGLGeometryNodeType {

    public OGLContourPolyline2D() {
    }

    @Override
    public void setupFinished() {
        super.setupFinished();
    }

    @Override
    public void setQuality(float q) {
    }

    @Override
    public UVCoord2f[] getPoints() {
        UVCoord2f[] retVal = new UVCoord2f[vfControlPoint.length / 2];
        for (int i = 0, j = 0; i < vfControlPoint.length; i += 2, j++) {
            retVal[j] = new UVCoord2f((float) vfControlPoint[i], (float) vfControlPoint[i + 1]);
        }
        return retVal;
    }

    @Override
    public Geometry getGeometry() {

        return null;
    }

    @Override
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }
}
