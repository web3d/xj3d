/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.nodes.proto;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Internal Proto handler representation of a ROUTE.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ProtoROUTE implements ROUTE {

    /** Reference to the source node of this route */
    private VRMLNodeType srcNode;

    /** Index of the source field of this route */
    private int srcIndex;

    /** Reference to the destination node of this route */
    private VRMLNodeType destNode;

    /** Index of the destination field of this route */
    private int destIndex;

    /**
     * Create a new representation of a Java 3D route.
     *
     * @param sn The source node reference
     * @param si The field index of the source node
     * @param dn The destination node reference
     * @param di The field index of the destination node
     */
    public ProtoROUTE(VRMLNodeType sn, int si, VRMLNodeType dn, int di) {
        srcNode = sn;
        srcIndex = si;
        destNode = dn;
        destIndex = di;
    }

    /**
     * Get the reference to the source node of this route
     *
     * @return The source node reference
     */
    @Override
    public VRMLNode getSourceNode() {
        return srcNode;
    }

    /**
     * Get the index of the source field of this route
     *
     * @return The source node field index
     */
    @Override
    public int getSourceIndex() {
        return srcIndex;
    }

    /**
     * Get the reference to the destination node of this route
     *
     * @return The destination node reference
     */
    @Override
    public VRMLNode getDestinationNode() {
        return destNode;
    }

    /**
     * Get the index of the destination field of this route
     *
     * @return The destination node field index
     */
    @Override
    public int getDestinationIndex() {
        return destIndex;
    }

    /**
     * Print out a string representation of this route.
     *
     * @return A string representation of the route
     */
    @Override
    public String toString() {
        VRMLFieldDeclaration src_decl = srcNode.getFieldDeclaration(srcIndex);
        VRMLFieldDeclaration dest_decl =
            destNode.getFieldDeclaration(destIndex);

        StringBuilder buf = new StringBuilder("ROUTE Source: ");
        buf.append(srcNode.getVRMLNodeName());
        buf.append('.');
        buf.append(src_decl.getName());
        buf.append(" Dest: ");
        buf.append(destNode.getVRMLNodeName());
        buf.append('.');
        buf.append(dest_decl.getName());

        return buf.toString();
    }
}
