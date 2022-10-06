package org.web3d.vrml.renderer.ogl.nodes.nurbs;

import javax.vecmath.Point3f;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;

import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsCurve2D;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;


import net.jgeom.nurbs.UVCoord2f;
import net.jgeom.nurbs.BasicNurbsCurve;

public class OGLNurbsCurve2D extends BaseNurbsCurve2D implements
        TrimSegment, OGLGeometryNodeType {

    public OGLNurbsCurve2D() {
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

        UVCoord2f[] retVal;
        if (vfOrder == 2) {
            retVal = new UVCoord2f[vfControlPoint.length / 2];
            for (int i = 0, j = 0; i < vfControlPoint.length; i += 2, j++) {
                retVal[j] = new UVCoord2f((float) vfControlPoint[i], (float) vfControlPoint[i + 1]);
            }
        } else {
            BasicNurbsCurve curve = getCurveImpl();
            int Nt = 32;
            int p = curve.getKnotVector().getDegree();
            int M = curve.getKnotVector().length();
            float umin = curve.getKnotVector().get()[p];
            float umax = curve.getKnotVector().get()[M - p - 1];
            float ustep = (umax - umin) / Nt;
            Point3f container = new Point3f();
            retVal = new UVCoord2f[Nt + 1];

            for (int i = 0; i <= Nt; ++i) {
                float u;
                if (i < Nt) {
                    u = umin + i * ustep;
                } else {
                    u = umax;
                }
                curve.pointOnCurve(u, container);
                retVal[i] = new UVCoord2f(container.x, container.y);
            }
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
