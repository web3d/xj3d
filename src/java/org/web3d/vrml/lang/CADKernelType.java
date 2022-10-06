package org.web3d.vrml.lang;

import java.util.List;

import org.j3d.aviatrix3d.NodeUpdateListener;
import org.web3d.vrml.nodes.VRMLBREPFaceNode;
import org.web3d.vrml.nodes.VRMLNodeType;

public interface CADKernelType {

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getNode(int)
     */
    Object getNode(int id);

    int getNodeNumber();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getBrepType()
     */
    int getBrepType();

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#addRelation(org.web3d.vrml.nodes.VRMLNodeType, org.web3d.vrml.nodes.VRMLNodeType)
     */
    void addRelation(VRMLNodeType n1, VRMLNodeType n2);

    void addRelationToThisInstance(VRMLNodeType n1,
            VRMLNodeType n2);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getLinkedObjectsFrom(org.web3d.vrml.lang.VRMLNode)
     */
    List<VRMLNode> getLinkedObjectsFrom(VRMLNode o);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#getLinkedObjectsTo(java.lang.Object)
     */
    List getLinkedObjectsTo(Object o);

    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#addManagedNode(org.web3d.vrml.nodes.VRMLNodeType)
     */
    void addManagedNode(VRMLNodeType node);

    void addManagedNodeToThisInstance(VRMLNodeType node);

    /*
     * The method is meant to be called once the brep object parsing is over.
     */
    /* (non-Javadoc)
     * @see org.web3d.vrml.renderer.ogl.nodes.cadgeometry.BaseCADKernelRenderer#initialise()
     */
    boolean initialise();

    Object getPointBREPImpl();

    Object getWireBREPImpl();

    Object getFaceImpl(VRMLNodeType n);

    boolean isFaceImplReady(VRMLNodeType n);

    void removeFaceImpl(VRMLNodeType n);

    int getReaddyFacesNumber();

    void renderFaceBREPNonBlocking(final VRMLBREPFaceNode n)
            throws Exception;

    void renderFaceBREP(VRMLBREPFaceNode n) throws Exception;

    Object computeAndGetFaceImpl(VRMLNodeType n);

    void render();

    Object getEmptyImpl();

    void updateGeometry(NodeUpdateListener f);
}