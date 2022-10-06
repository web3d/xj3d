package org.web3d.vrml.renderer.ogl.nodes.nurbs;

import org.web3d.vrml.renderer.ogl.nodes.nurbs.mesh.RectangularMesh;

import net.jgeom.nurbs.BasicNurbsSurface;
import net.jgeom.nurbs.ControlNet;
import net.jgeom.nurbs.ControlPoint4f;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsPatchSurface;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;

public class OGLNurbsPatchSurface extends BaseNurbsPatchSurface implements
        OGLGeometryNodeType {

    private org.j3d.aviatrix3d.VertexGeometry impl;

    private BasicNurbsSurface nurbsSurfaceImpl;

    public OGLNurbsPatchSurface() {
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
    public BasicNurbsSurface getNurbsImpl() {
        return nurbsSurfaceImpl;
    }

    @Override
    public void setupFinished() {



        // for(double f: vfControlPoint)
        // {
        // System.out.println(f);
        // }

        // System.out.println("cps : ");
        // for(double f:vfControlPoint)
        // System.out.print(f+", ");

        float[] vfControlPoint = new float[0];
        if (vfCoord != null && vfCoord.getNumPoints() > 0) {
            vfControlPoint = new float[vfCoord.getNumPoints()];
            vfCoord.getPoint(vfControlPoint);
        } else {
            System.out.println("OGLNurbsPatchSurface.setupFinished: null Coordinates node");
        }


        ControlPoint4f cpss[][] = new ControlPoint4f[this.vfUDimension][this.vfVDimension];
        if (vfControlPoint.length < this.vfUDimension * this.vfVDimension) {
            errorReporter.messageReport("Not enough control points for this surface");
            return;
        }
        /*
         System.out.println("Unwrapping control points list");
         System.out.println("Nu: "+this.vfUDimension+" Nv: "+this.vfVDimension);
         System.out.println("vfControlPoint.length: "+ vfControlPoint.length);
         */

        // this needs to be modified in light that ControlPoint4f looks like it should
        // be the coordinates in rational space
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
                //System.out.println("OGLNurbsPatchSurface: index " + k + " weight: "+vfWeight[k]);
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

        // the BasicNurbsSurface needs to be initialized with the
        // polynomial degree, not the order
        nurbsSurfaceImpl = new BasicNurbsSurface(cn, uk, vk,
                (this.vfUOrder - 1), (this.vfVOrder - 1));

        RectangularMesh mesh = new RectangularMesh(nurbsSurfaceImpl, 20, 20);
        impl = mesh.sceneGraphObject();
        super.setupFinished();

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

    @Override
    public void render() {
    }

    @Override
    public void set_renderer(Object renderer) {
        //this.renderer=(CADKernelRenderer)renderer;
    }

    // method defined in interface import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
    @Override
    public boolean isSolid() {
        return vfSolid;
    }
}
