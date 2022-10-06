package org.web3d.vrml.renderer.common.nodes.cadgeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.j3d.aviatrix3d.NodeUpdateListener;
import org.web3d.vrml.lang.CADKernelType;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLBREPFaceNode;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.util.NodeArray;

public abstract class BaseCADKernelRenderer implements CADKernelType {

    static List<BaseCADKernelRenderer> instanciatedRenderers = new ArrayList<>();
    protected boolean isUpToDate = false;
    private boolean debug = false;
    protected int brepType;
    protected VRMLNode brepRoot;
    protected NodeArray nodes;
    protected int precision = 35;
    protected List<Vector<VRMLNode>> relations = new ArrayList<>();

    public BaseCADKernelRenderer() {
        instanciatedRenderers.add(BaseCADKernelRenderer.this);
    }

    public static List<BaseCADKernelRenderer> getInstanciatedRenderers() {
        return instanciatedRenderers;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getNode(int)
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getNode(int)
     */
    @Override
    public Object getNode(int id) {
        return nodes.get(id);
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getNodeNumber()
     */
    @Override
    public int getNodeNumber() {
        return nodes.size();
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getBrepType()
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getBrepType()
     */
    @Override
    public int getBrepType() {
        return brepType;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#addRelation(org.web3d.vrml.nodes.VRMLNodeType, org.web3d.vrml.nodes.VRMLNodeType)
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#addRelation(org.web3d.vrml.nodes.VRMLNodeType, org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    public void addRelation(VRMLNodeType n1, VRMLNodeType n2) {
        if (!debug) {
            addRelationToThisInstance(n1, n2);
        } else {
            for (CADKernelType bk : instanciatedRenderers) {
                bk.addRelationToThisInstance(n1, n2);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#addRelationToThisInstance(org.web3d.vrml.nodes.VRMLNodeType, org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    public void addRelationToThisInstance(VRMLNodeType n1, VRMLNodeType n2) {
        this.addManagedNode(n1);
        this.addManagedNode(n2);
        Vector<VRMLNode> v = new Vector<>();
        v.add(n1);
        v.add(n2);
        relations.add(v);
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getLinkedObjectsFrom(org.web3d.vrml.lang.VRMLNode)
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getLinkedObjectsFrom(org.web3d.vrml.lang.VRMLNode)
     */
    @Override
    public List<VRMLNode> getLinkedObjectsFrom(VRMLNode o) {
        List<VRMLNode> al = new ArrayList<>();
        for (Vector<VRMLNode> v : relations) {
            if (v.get(0) == o) {
                al.add(nodes.get(nodes.indexOf(v.get(1))));
            }
        }
        return al;
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getLinkedObjectsTo(java.lang.Object)
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getLinkedObjectsTo(java.lang.Object)
     */
    @Override
    public List getLinkedObjectsTo(Object o) {
        List<Object> al = new ArrayList<>();
        for (Vector<VRMLNode> v : relations) {
            if (v.get(1) == o) {
                al.add(nodes.get(nodes.indexOf(v.get(0))));
            }
        }
        return al;
    }


    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#addManagedNode(org.web3d.vrml.nodes.VRMLNodeType)
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#addManagedNode(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    public void addManagedNode(VRMLNodeType node) {
        if (!debug) {
            addManagedNodeToThisInstance(node);
        } else {
            for (CADKernelType bk : instanciatedRenderers) {
                bk.addManagedNodeToThisInstance(node);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#addManagedNodeToThisInstance(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    public void addManagedNodeToThisInstance(VRMLNodeType node) {
        if (nodes.indexOf(node) == -1) {
            nodes.add(node);
            isUpToDate = false;
        }
    }

    /*
     * The method is meant to be called once the brep object parsing is over.
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#initialise()
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#initialise()
     */
    @Override
    public boolean initialise() {

        // find the BREP object root
        for (int i = 0; i < nodes.size(); i++) {
            if ((nodes.get(i).getSecondaryType() != null) && (nodes.get(i).getSecondaryType().length == 1)) {
                int type = nodes.get(i).getSecondaryType()[0];
                switch (type) {
                    case TypeConstants.BREPPointBREPType:
                    case TypeConstants.BREPShellBREPType:
                    case TypeConstants.BREPWireBREPType:
                    case TypeConstants.BREPSolidBREPType:
                        brepRoot = nodes.get(i);
                        brepType = type;
                        break;
                }
            }
        }
        return true;
    }


    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getPointBREPImpl()
     */
    @Override
    abstract public Object getPointBREPImpl();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getWireBREPImpl()
     */
    @Override
    abstract public Object getWireBREPImpl();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getFaceImpl(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    abstract public Object getFaceImpl(VRMLNodeType n);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#isFaceImplReady(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    abstract public boolean isFaceImplReady(VRMLNodeType n);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#removeFaceImpl(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    abstract public void removeFaceImpl(VRMLNodeType n);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getReaddyFacesNumber()
     */
    @Override
    abstract public int getReaddyFacesNumber();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#renderFaceBREPNonBlocking(org.web3d.vrml.nodes.VRMLBREPFaceNode)
     */
    @Override
    abstract public void renderFaceBREPNonBlocking(final VRMLBREPFaceNode n) throws Exception;

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#renderFaceBREP(org.web3d.vrml.nodes.VRMLBREPFaceNode)
     */
    @Override
    abstract public void renderFaceBREP(VRMLBREPFaceNode n) throws Exception;

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#computeAndGetFaceImpl(org.web3d.vrml.nodes.VRMLNodeType)
     */
    @Override
    abstract public Object computeAndGetFaceImpl(VRMLNodeType n);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#render()
     */
    @Override
    abstract public void render();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#getEmptyImpl()
     */
    @Override
    abstract public Object getEmptyImpl();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.common.nodes.cadgeometry.CADKernelType#updateGeometry(org.j3d.aviatrix3d.NodeUpdateListener)
     */
    @Override
    abstract public void updateGeometry(NodeUpdateListener f);
}