package org.web3d.vrml.renderer.ogl.nodes.cadgeometry;

import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jgeom.nurbs.BasicNurbsSurface;
import net.jgeom.nurbs.MeshedNurbsSurface;
import net.jgeom.nurbs.NurbsCurve;
import net.jgeom.nurbs.TrimNurbsUV;
import net.jgeom.nurbs.MeshedNurbsSurface.TesselationType;
import net.jgeom.nurbs.geomContainers.IndexedGeometryArray;

import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.IndexedTriangleArray;
import org.j3d.aviatrix3d.IndexedVertexGeometry;
import org.j3d.aviatrix3d.LineStripArray;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.PointArray;
import org.j3d.aviatrix3d.QuadArray;
import org.j3d.aviatrix3d.VertexGeometry;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLBREPCurve2DNode;
import org.web3d.vrml.nodes.VRMLBREPCurve3DNode;
import org.web3d.vrml.nodes.VRMLBREPFaceNode;
import org.web3d.vrml.nodes.VRMLBREPSurfaceNode;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.cadgeometry.BaseCADKernelRenderer;
import org.web3d.vrml.renderer.common.nodes.nurbs.BaseNurbsPatchSurface;
import org.web3d.vrml.renderer.common.nodes.render.BaseCoordinate;
import org.web3d.vrml.renderer.ogl.nodes.OGLGeometryNodeType;
import org.web3d.vrml.util.NodeArray;
import org.xj3d.impl.core.eventmodel.DefaultBrepManager;

public class CADKernelRenderer extends BaseCADKernelRenderer {

    /**
     * Reporter instance for handing out errors
     */
    protected ErrorReporter errorReporter;

    int cptFace = 0;

    int maxThreads = 10;

    static Integer runningThreadNumber = 0;

    static Integer threadCounter = 0;

    TesselationType typeMesh = MeshedNurbsSurface.TesselationType.NO_TRIM;

    private Geometry impl;

    /**
     *
     */
    public Map<VRMLNodeType, Geometry> faceImpl;

    @Override
    public void updateGeometry(NodeUpdateListener f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * ugly tuning for the Aviatrix rendering
     */
    class PointArrayBREP extends PointArray {

        public PointArrayBREP(boolean arg1, int arg2) {
            super(arg1, arg2);
        }

        @Override
        public void render(GL2 gl) {
            if ((vertexFormat & COORDINATE_MASK) == 0) {
                return;
            }
            setVertexState(gl);
            gl.glPointSize(10.0f);
            gl.glDrawArrays(GL2.GL_POINTS, 0, numCoords);
            clearVertexState(gl);
        }
    }

    /**
     *
     */
    public CADKernelRenderer() {
        super();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

//		errorReporter.messageReport("New CAD Kernel Instance");
        nodes = new NodeArray();

        faceImpl = new HashMap<>();
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getBREPVertices()
     */

    /**
     *
     * @return
     */

    public List<BaseCoordinate> getBREPVertices() {
        List<BaseCoordinate> coord = new ArrayList<>();
        List<VRMLNode> children = this.getLinkedObjectsFrom(brepRoot);
        for (VRMLNode n : children) {
            if (n instanceof BaseCoordinate) {
                coord.add((BaseCoordinate) n);
            }
        }
        return coord;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getBREPVertices()
     */

    /**
     *
     * @return
     */

    public List<VRMLBREPCurve3DNode> getBREPCurves3D() {
        List<VRMLBREPCurve3DNode> curves3d = new ArrayList<>();
        List<VRMLNode> children = this.getLinkedObjectsFrom(brepRoot);
        for (int i = 0; i < nodes.size(); i++) {
            VRMLNode n = nodes.get(i);
            if (n instanceof VRMLBREPCurve3DNode) {
                curves3d.add((VRMLBREPCurve3DNode) n);
            }
        }
        return curves3d;
    }

	// public VRMLNode getPointBREPContainer()
    // {
    //    	for(int i=0;i<nodes.size();i++)
    //    	{
    //    		int type=nodes.get(i).getPrimaryType();
    //    		switch(type)
    //    		{
    //    			case TypeConstants.BREPPointBREPType:
    //    				return nodes.get(i);
    //    		}
    //    	}
    //    	return null;
    //    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getPointBREPImpl()
     */
    @Override
    public PointArray getPointBREPImpl() {
        //to be able to render something on the first frame.
        if (impl == null) {
            this.render();
        }

        return (PointArray) impl;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getWireBREPImpl()
     */
    @Override
    public LineStripArray getWireBREPImpl() {
        //to be able to render something on the first frame.
        if (impl == null) {
            this.render();
        }

        return (LineStripArray) impl;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#render()
     */
    @Override
    public void render() {
        if (!isUpToDate) {
            switch (brepType) {
                case TypeConstants.BREPPointBREPType:
                    renderPointBREP();
                    break;
                case TypeConstants.BREPWireBREPType:
                    renderWireBREP();
                    break;
                case TypeConstants.BREPShellBREPType:
                    //render all faces
//				for (final BaseFace f : this.getAllFaces()) {
//					try {
//						renderFaceBREP(f);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace(System.err);
//					}
//				}
                    break;
                case TypeConstants.BREPSolidBREPType:
                    break;
            }
        }
        isUpToDate = true;
    }

    private List<VRMLBREPCurve2DNode> getPcurves(VRMLBREPFaceNode f) {
        //parse BREP tree to get pcurves from Face
        List<VRMLBREPCurve2DNode> pcurves = new ArrayList<>();
        for (VRMLNode wire : this.getLinkedObjectsFrom(f)) {
            for (VRMLNode edge : this.getLinkedObjectsFrom(wire)) {
                for (VRMLNode pcurve : this.getLinkedObjectsFrom(edge)) {
                    if (pcurve instanceof VRMLBREPCurve2DNode) {
                        pcurves.add((VRMLBREPCurve2DNode) pcurve);
                    }
                }
            }
        }
        return pcurves;
    }

    @Override
    public void renderFaceBREPNonBlocking(final VRMLBREPFaceNode n) throws Exception {
//		errorReporter.messageReport("triangulation request for face : "+n.hashCode());
        if (!faceImpl.containsKey(n)) {

            //block if too many threads running
            new Thread(new Runnable() {
                int id = 0;

                @Override
                public void run() {
                    try {
                        while (runningThreadNumber > maxThreads) {
                            Thread.sleep(100);
                        }

                        synchronized (runningThreadNumber) {
                            runningThreadNumber++;
                        }
                        synchronized (threadCounter) {
                            id = threadCounter++;
                        }

                        renderFaceBREP(n);
                        //notify manager that a face is ready to be rendered
                        DefaultBrepManager.getInstance().addReadyFace(n);
//						errorReporter
//								.messageReport("Face triangulation computed for face : "
//										+ n.hashCode());
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                    synchronized (runningThreadNumber) {
                        runningThreadNumber--;
                    }
                }
            }).start();
        }
    }

    @Override
    public void renderFaceBREP(VRMLBREPFaceNode n) throws Exception {

        VRMLBREPSurfaceNode surface = this.getSurfaceFromFace(n);

        if (surface instanceof BaseNurbsPatchSurface) {
            BasicNurbsSurface nurbsSurfaceImpl = (BasicNurbsSurface) surface
                    .getNurbsImpl();

            //add trimming pcurves
            List<VRMLBREPCurve2DNode> pcurves = getPcurves(n);
            for (VRMLBREPCurve2DNode pc : pcurves) {
                if (pc.getNurbsImpl() != null) {
                    TrimNurbsUV tc = new TrimNurbsUV((NurbsCurve) pc
                            .getNurbsImpl(), 40);
                    nurbsSurfaceImpl.addInnerTrimCurve(tc);
                }
            }

            net.jgeom.nurbs.geomContainers.GeometryArray ga = null;

            MeshedNurbsSurface mns = null;
            if (nurbsSurfaceImpl != null) {
                mns = new MeshedNurbsSurface(nurbsSurfaceImpl, typeMesh);

                //tesselation heuristic
                mns.setPrecision(mns.getUKnots().length * 3, mns.getVKnots().length * 3);
                if (pcurves.isEmpty()) {
                    mns.setEvaluator(TesselationType.NO_TRIM);
                } else {
                    mns.setEvaluator(typeMesh);
                }
                ga = mns.getMeshedSurface();
                if (ga == null) {
                    throw new Exception("Couldn't mesh surface");
                }
            }

            if ((ga == null) && (nurbsSurfaceImpl != null) && (mns != null)) {
                errorReporter
                        .messageReport("Tesselation Problem, removing trimming curves and retry...");
                nurbsSurfaceImpl.removeTrimmingCurves();
                mns = new MeshedNurbsSurface(nurbsSurfaceImpl, typeMesh);
                mns.setPrecision(precision, precision);
                mns.setEvaluator(this.typeMesh);
                try {
                    ga = mns.getMeshedSurface();
                } catch (Exception e) {
                    throw new Exception(
                            "There really is a problem tesselating this face");
                }
            }

            if (ga != null) {
                float[] coordinates = new float[ga.getValidVertexCount() * 3];
                ga.getCoordinates(0, coordinates);
                VertexGeometry nurbsMesh = this.getEmptyImpl();
                nurbsMesh.setValidVertexCount(coordinates.length);
                nurbsMesh.setVertices(VertexGeometry.COORDINATE_3, coordinates);
                nurbsMesh.setSingleColor(false, new float[]{1, 0, 0});
                if (ga instanceof IndexedGeometryArray) {
                    int[] indexes = new int[((IndexedGeometryArray) ga).getIndexCount()];
                    ((IndexedGeometryArray) ga).getCoordinateIndices(0, indexes);
                    if (nurbsMesh instanceof org.j3d.aviatrix3d.IndexedTriangleArray) {

                        // TODO: Not sure what lib was in mind for this index conversion
                        int[] indexesTriangles = null/* = this.convertQuadIndexesToTriangleIndexes(indexes)*/;
                        ((IndexedVertexGeometry) nurbsMesh).setIndices(indexesTriangles, indexesTriangles.length);

                        float[][] faceNormals = new float[indexesTriangles.length / 3][3];
                        for (int i = 0; i < indexesTriangles.length / 3; i++) {
                            int p = i * 3 * 3;

                            // TODO: Not sure which J3D geometry was meant for this normal creation
//                            createFaceNormal(coordinates, p, 3, faceNormals[i]);
                        }

                        float[] faceNormals_1D = new float[faceNormals.length * faceNormals[0].length];
                        int k = 0;
                        for (float[] faceNormal : faceNormals) {
                            for (int j = 0; j < faceNormals[0].length; j++) {
                                faceNormals_1D[k++] = faceNormal[j];
                            }
                        }
                        nurbsMesh.setNormals(faceNormals_1D);
                    } else {
                        ((IndexedVertexGeometry) nurbsMesh).setIndices(indexes, indexes.length);
                    }
                }
                faceImpl.put(n, nurbsMesh);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#renderWireBREP()
     */
    public void renderWireBREP() {
        impl = new LineStripArray(false, VertexGeometry.VBO_HINT_STATIC);
        List<Float> coordinates = new ArrayList<>();
        List<Integer> strips = new ArrayList<>();

        List<VRMLBREPCurve3DNode> curves = this.getBREPCurves3D();
        for (VRMLBREPCurve3DNode c : curves) {
            LineStripArray la = (LineStripArray) ((OGLGeometryNodeType) c)
                    .getGeometry();
            float ff[] = new float[la.getValidVertexCount() * 3];
            if (la.getValidVertexCount() != 0) {
                la.getVertices(ff);
            }
            strips.add(la.getValidVertexCount());
            for (float f : ff) {
                coordinates.add(f);
            }
        }
        float coords_array[] = new float[coordinates.size()];
        for (int i = 0; i < coords_array.length; i++) {
            coords_array[i] = coordinates.get(i);
        }
        ((VertexGeometry) impl).setVertices(VertexGeometry.COORDINATE_3,
                coords_array);

        int strips_array[] = new int[strips.size()];
        for (int i = 0; i < strips_array.length; i++) {
            strips_array[i] = strips.get(i);
        }
        ((LineStripArray) impl)
                .setStripCount(strips_array, strips_array.length);

    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#renderPointBREP()
     */
    public void renderPointBREP() {
        List<BaseCoordinate> coord = this.getBREPVertices();

        List<Float> coordinates = new ArrayList<>();

        for (BaseCoordinate c : coord) {
            float[] points = new float[c.getNumPoints()];
            c.getPoint(points);
            for (float f : points) {
                coordinates.add(f);
            }
        }

        float[] coordinates_array = new float[coordinates.size()];
        {
            int ind = 0;
            for (Object f : coordinates.toArray()) {
                coordinates_array[ind++] = (Float) f;
            }
        }

        impl = new PointArrayBREP(false, VertexGeometry.VBO_HINT_STATIC);
        ((VertexGeometry) impl).setVertices(VertexGeometry.COORDINATE_3,
                coordinates_array);
    }

    @Override
    public Object getFaceImpl(VRMLNodeType n) {
        return faceImpl.get(n);
    }

    @Override
    public Object computeAndGetFaceImpl(VRMLNodeType n) {
        try {
            this.renderFaceBREP((VRMLBREPFaceNode) n);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(System.err);
        }
        return faceImpl.get(n);
    }

    @Override
    public boolean isFaceImplReady(VRMLNodeType n) {
        return (faceImpl.containsKey(n));
    }

    @Override
    public void removeFaceImpl(VRMLNodeType n) {
        faceImpl.remove(n);
    }

    /**
     *
     * @param f
     * @return
     */
    public VRMLBREPSurfaceNode getSurfaceFromFace(VRMLNodeType f) {
        for (VRMLNode n : this.getLinkedObjectsFrom(f)) {
            if (n instanceof VRMLBREPSurfaceNode) {
                return (VRMLBREPSurfaceNode) n;
            }
        }
        return null;
    }

    @Override
    public int getReaddyFacesNumber() {
        return faceImpl.keySet().size();
    }

    @Override
    public VertexGeometry getEmptyImpl() {
        if (typeMesh == MeshedNurbsSurface.TesselationType.QUADS_TRIM) {
            return new IndexedTriangleArray();
        }

        if (typeMesh == MeshedNurbsSurface.TesselationType.DELAUNAY_TRIM) {
            return new IndexedTriangleArray();
        }

        if (typeMesh == MeshedNurbsSurface.TesselationType.POINT_CLOUD_TRIM) {
            return new PointArray(true, VertexGeometry.VBO_HINT_STATIC);
        }

        if (typeMesh == MeshedNurbsSurface.TesselationType.NO_TRIM) {
            return new QuadArray();
        }

        return null;
    }

}
