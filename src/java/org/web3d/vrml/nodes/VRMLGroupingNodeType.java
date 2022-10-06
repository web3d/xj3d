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
package org.web3d.vrml.nodes;

/**
 * A node which can contain other nodes.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public interface VRMLGroupingNodeType extends VRMLBoundedNodeType {

    /**
     * Get the children, provides a live reference not a copy
     *
     * @return An array of VRMLNodeTypes
     */
    VRMLNodeType[] getChildren();

    /**
     * Accesor method to set the children field.
     * If passed null this method will act like removeChildren
     *
     * @param newChildren Array of new children
     */
    void setChildren(VRMLNodeType[] newChildren);

    /**
     * Accessor method to set the children field. Creates an array
     * containing only newChild. If passed null this method will act like
     * removeChildren.
     *
     * @param newChild The new child
     */
    void setChildren(VRMLNodeType newChild);

    /**
     * Append a new child node to the existing collection. Should be used
     * sparingly. It is really only provided for Proto handling purposes.
     *
     * @param newChild The new child
     */
    void addChild(VRMLNodeType newChild);

    /**
     * Returns the number of children.
     *
     * @return The number of children
     */
    int getChildrenSize();

    /**
     * A check to see if this grouping node contains any bindable nodes.
     * Bindables do not allow the reuse of a shared node and will effect how
     * the DEF/USE handling is implemented.
     *
     * @return true if this or any of its children contain bindable nodes
     */
    boolean containsBindableNodes();
}
