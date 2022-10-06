package org.web3d.vrml.renderer.ogl.nodes.nurbs;

import java.util.ArrayList;
import java.util.List;

import net.jgeom.nurbs.BasicNurbsCurve;
import net.jgeom.nurbs.ControlPoint4f;
import net.jgeom.nurbs.MeshedNurbsCurve;
import net.jgeom.nurbs.evaluators.BasicNurbsCurveEvaluator;
import net.jgeom.nurbs.geomContainers.LineStripArray;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.VertexGeometry;
import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsCurve;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

public class OGLNurbsCurve extends BaseNurbsCurve implements
        OGLGeometryNodeType {

    private org.j3d.aviatrix3d.LineStripArray impl;

    public OGLNurbsCurve() {
    }

    @Override
    public Geometry getGeometry() {
        return impl;
    }

    @Override
    public SceneGraphObject getSceneGraphObject() {
        return impl;
    }

    @Override
    public void setupFinished() {

        float[] vfControlPoint = new float[0];
        if (vfCoord != null && vfCoord.getNumPoints() > 0) {
            vfControlPoint = new float[vfCoord.getNumPoints()];
            vfCoord.getPoint(vfControlPoint);
        } else {
            System.out.println("OGLNurbsCurve.setupFinished: null Coordinates node");
        }

        impl = new org.j3d.aviatrix3d.LineStripArray(true,
                VertexGeometry.VBO_HINT_STATIC);
        List<ControlPoint4f> cpsa = new ArrayList<>();
        int dimension = vfControlPoint.length / 3;
        {
            for (int i = 0; i < dimension; i++) {
                float wt;
                if (vfWeight != null && vfWeight.length > i) {
                    wt = (float) vfWeight[i];
                } else {
                    wt = (float) 1.0;
                }

                float winv = ((float) 1.0) / wt;

                cpsa.add(new ControlPoint4f(
                        vfControlPoint[3 * i] * winv,
                        vfControlPoint[3 * i + 1] * winv,
                        vfControlPoint[3 * i + 2] * winv,
                        winv));
            }
        }
        ControlPoint4f cps[] = new ControlPoint4f[cpsa.size()];
        cpsa.toArray(cps);

        //recompute knot vector if not supplied or wrong size
//		boolean computeKnot = false;
        float uk[];
        if (this.vfKnot != null) {
            uk = new float[this.vfKnot.length];
            for (int i = 0; i < uk.length; i++) {
                uk[i] = (float) vfKnot[i];
            }
        } else {    // compute  a uniform knot
            int Nk = dimension + vfOrder;   // number of knot values (including extraneous)
            uk = new float[Nk];
            int ic = 0;
            for (; ic < vfOrder; ic++) {
                uk[ic] = (float) 0.0;
            }
            int Nseg = dimension - vfOrder + 1; // number of internal segments
            float delt = (float) 1.0 / Nseg;
            for (int iseg = 1; iseg < Nseg; iseg++, ic++) {
                uk[ic] = iseg * delt;
            }
            for (; ic < Nk; ic++) {
                uk[ic] = (float) 1.0;
            }
        }


        BasicNurbsCurve nc = new BasicNurbsCurve(cps, uk, this.vfOrder - 1);

        MeshedNurbsCurve mnc = new MeshedNurbsCurve(nc);
        mnc.setEvaluator(new BasicNurbsCurveEvaluator());

        LineStripArray ga = (LineStripArray) mnc.getMeshedCurve();
        float[] coordinates = new float[ga.getValidVertexCount() * 3];
        ga.getCoordinates(0, coordinates);

        impl.setVertices(VertexGeometry.COORDINATE_3,
                coordinates);

        impl.setStripCount(new int[]{coordinates.length / 3}, 1);

        super.setupFinished();
    }

    @Override
    public void render() {
    }

    @Override
    public void set_renderer(Object renderer) {
    }
}
