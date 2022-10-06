package org.web3d.vrml.renderer.ogl.nodes.nurbs;

// Standard library imports
import java.util.ArrayList;
import java.util.List;

// Application specific imports
import net.jgeom.nurbs.BasicNurbsSurface;
import net.jgeom.nurbs.ControlNet;
import net.jgeom.nurbs.ControlPoint4f;
import net.jgeom.nurbs.TrimCurve;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsTrimmedSurface;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.renderer.ogl.nodes.nurbs.mesh.TrimmedNurbsMesh;

public class OGLNurbsTrimmedSurface extends BaseNurbsTrimmedSurface implements
        OGLGeometryNodeType {

    private org.j3d.aviatrix3d.VertexGeometry impl;

    private BasicNurbsSurface nurbsSurfaceImpl;

    public BasicNurbsSurface getNurbsImpl() {
        return nurbsSurfaceImpl;
    }

    public OGLNurbsTrimmedSurface() {
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

        super.setupFinished();

        float[] vfControlPoint = new float[0];
        if (vfCoord != null && vfCoord.getNumPoints() > 0) {
            vfControlPoint = new float[vfCoord.getNumPoints()];
            vfCoord.getPoint(vfControlPoint);
        } else {
            System.out.println("OGLNurbsTrimmedSurface.setupFinished: null Coordinates node");
        }


        ControlPoint4f cpss[][] = new ControlPoint4f[this.vfUDimension][this.vfVDimension];
        if (vfControlPoint.length < this.vfUDimension * this.vfVDimension) {
            errorReporter.messageReport("Not enough control points for this surface");
            return;
        }


        for (int k = 0, j = 0; j < this.vfVDimension; j++) {
            for (int i = 0; i < this.vfUDimension; i++) {
                float wt;
                if (vfWeight != null && vfWeight.length > k) {
                    wt = (float) vfWeight[k];
                } else {
                    wt = (float) 1.0;
                }

                float winv = ((float) 1.0) / wt;

                cpss[i][j] = new ControlPoint4f(
                        vfControlPoint[3 * k] * winv,
                        vfControlPoint[3 * k + 1] * winv,
                        vfControlPoint[3 * k + 2] * winv,
                        wt);
                k += 1;

            }
        }

        // recompute knot vector if not supplied or wrong size
        float uk[];
        if (this.vfUKnot != null) {
            uk = new float[this.vfUKnot.length];
            for (int i = 0; i < uk.length; i++) {
                uk[i] = (float) vfUKnot[i];
            }
        } else {
            uk = uniformKnotVector(vfUOrder, vfUDimension);
        }

        float vk[];
        if (this.vfVKnot != null) {
            vk = new float[this.vfVKnot.length];
            for (int i = 0; i < vk.length; i++) {
                vk[i] = (float) vfVKnot[i];
            }
        } else {
            vk = uniformKnotVector(vfVOrder, vfVDimension);
        }

        ControlNet cn = new ControlNet(cpss);
        nurbsSurfaceImpl = new BasicNurbsSurface(cn, uk, vk,
                (this.vfUOrder - 1), (this.vfVOrder - 1));

        List<TrimCurve> outerBounds = new ArrayList<>();
        List<TrimCurve> innerBounds = new ArrayList<>();
        for (VRMLNodeType nd : vfContour) {
            OGLContour2D contour = (OGLContour2D) nd;
            TrimCurve tc = contour.getTrimCurve();
            if (contour.getSense()) {
                outerBounds.add(tc);
            } else {
                innerBounds.add(tc);
            }
        }

        if (outerBounds.size() != 1) {
            //throw new Exception("no outerBounds");
            System.out.println("OGLNurbsTrimmedCurve: No outer bound");
        } else {
            nurbsSurfaceImpl.addOuterTrimCurve(outerBounds.get(0));
        }

        for (TrimCurve tc : innerBounds) {
            nurbsSurfaceImpl.addInnerTrimCurve(tc);
        }

        TrimmedNurbsMesh mesh = new TrimmedNurbsMesh(nurbsSurfaceImpl);
        impl = mesh.sceneGraphObject();


    }

    /**
     * Returns a uniform vector of knot values (clamped) for a spline of order
     * (order=polynomial degree + 1; order=4 are cubic splines) and dimension
     * (dimension = number of control points)
     *
     * returns for parameter range 0 to 1 follows definition of uniform knot
     * from "The Nurbs Book" (L. Piegl &amp; W Tiller, 2nd ed) section 2.4
     *
     * @param dimension
     * @return float[] array of knot values
     */
    public float[] uniformKnotVector(int order, int dimension) {
        int Nk = dimension + order;   // number of knot values
        float uk[] = new float[Nk];
        int ic = 0;
        for (; ic < order; ic++) {
            uk[ic] = (float) 0.0;
        }
        int Nseg = dimension - order + 1; // number of internal segments
        float delt = (float) 1.0 / Nseg;
        for (int iseg = 1; iseg < Nseg; iseg++, ic++) {
            uk[ic] = iseg * delt;
        }
        for (; ic < Nk; ic++) {
            uk[ic] = (float) 1.0;
        }
        return uk;
    }

    // method defined in interface import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
    @Override
    public boolean isSolid() {
        return vfSolid;
    }
}
