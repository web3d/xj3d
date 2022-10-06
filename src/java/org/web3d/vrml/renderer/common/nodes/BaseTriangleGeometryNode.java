/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes;

// External imports
import org.j3d.geom.GeometryData;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract implementation of the Triangle* nodes.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public abstract class BaseTriangleGeometryNode
    extends BaseComponentGeometryNode {

    /** Message for when the proto is not a Normal */
    protected static final String NORMAL_PROTO_MSG =
        "Proto does not describe a Normal object";

    /** Message for when the node in setValue() is not a Normal */
    protected static final String NORMAL_NODE_MSG =
        "Node does not describe a Normal object";

    /** Message for when the proto is not a TexCoord */
    protected static final String TEXCOORD_PROTO_MSG =
        "Proto does not describe a TexCoord object";

    /** Message for when the node in setValue() is not a TexCoord */
    protected static final String TEXCOORD_NODE_MSG =
        "Node does not describe a TexCoord object";

    /** Userdata kept in the triangle geometry */
    protected GeometryData geomData;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseTriangleGeometryNode(String name) {
        super(name);

        changeFlags = 0;
    }

    /**
     * Build the render specific implementation.
     */
    protected abstract void buildImpl();

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propagate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    @Override
    public void allEventsComplete() {
        buildImpl();
    }


    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        buildImpl();
    }

    @Override
     public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if(inSetup)
            return;

        switch(index) {
            case FIELD_COLOR:
                if(node.getPrimaryType() != TypeConstants.ColorNodeType)
                    throw new InvalidFieldValueException(COLOR_PROTO_MSG);

                changeFlags |= COLORS_CHANGED;
                buildImpl();
                break;

            case FIELD_COORD:
                if(node.getPrimaryType() != TypeConstants.CoordinateNodeType)
                    throw new InvalidFieldValueException(COORD_PROTO_MSG);

                changeFlags |= COORDS_CHANGED;
                buildImpl();
                break;

            case FIELD_NORMAL:
                if (node.getPrimaryType() != TypeConstants.NormalNodeType)
                    throw new InvalidFieldValueException(NORMAL_PROTO_MSG);

                changeFlags |= NORMALS_CHANGED;
                buildImpl();
                break;

            case FIELD_TEXCOORD:
                if(node.getPrimaryType() != TypeConstants.TextureCoordinateNodeType)
                    throw new InvalidFieldValueException(TEXCOORD_PROTO_MSG);

                changeFlags |= TEXCOORDS_CHANGED;
                buildImpl();
                break;

            default:
                System.out.println("BaseTriangleNode: Unknown field for notifyExternProtoLoaded");
        }

        stateManager.addEndOfThisFrameListener(this);
    }


    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param child The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;
        boolean notif = false;

        switch(index) {
            case FIELD_COORD:
                if (child == null) {
                    pCoord = null;
                    if (vfCoord != null) {
                            vfCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pCoord = (VRMLProtoInstance) child;
                    node = pCoord.getImplementationNode();

                    if (!(node instanceof VRMLCoordinateNodeType)) {
                        pCoord = null;
                        throw new InvalidFieldValueException(COORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLCoordinateNodeType)) {
                    throw new InvalidFieldValueException(COORD_NODE_MSG);
                }

                vfCoord = (VRMLCoordinateNodeType) node;

                if (vfCoord != null)
                    vfCoord.addComponentListener(this);

                changeFlags |= COORDS_CHANGED;
                notif = true;
                break;

            case FIELD_NORMAL:
                if (child == null) {
                    pNormal = null;
                    if (vfNormal != null) {
                        vfNormal.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pNormal = (VRMLProtoInstance) child;
                    node = pNormal.getImplementationNode();

                    if (!(node instanceof VRMLNormalNodeType)) {
                        pNormal = null;
                        throw new InvalidFieldValueException(NORMAL_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLNormalNodeType)) {
                    System.out.println("node: " + node);
                    throw new InvalidFieldValueException(NORMAL_NODE_MSG);
                }

                vfNormal = (VRMLNormalNodeType) node;
                if (vfNormal != null)
                    vfNormal.addComponentListener(this);

                changeFlags |= NORMALS_CHANGED;
                notif = true;
                break;

            case FIELD_COLOR :
                if (child == null) {
                    pColor = null;
                    if (vfColor != null)
                        vfColor.removeComponentListener(this);
                } else if (child instanceof VRMLProtoInstance) {
                    pColor = (VRMLProtoInstance) child;
                    node = pColor.getImplementationNode();

                    if (!(node instanceof VRMLColorNodeType)) {
                        pColor = null;
                        throw new InvalidFieldValueException(COLOR_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLColorNodeType)) {
                    throw new InvalidFieldValueException(COLOR_NODE_MSG);
                }

                vfColor = (VRMLColorNodeType) node;
                if (vfColor != null) {
                    vfColor.addComponentListener(this);
                    if (!localColors)
                       fireLocalColorsChanged(true);
                    localColors = true;
                } else {
                    if (localColors)
                        fireLocalColorsChanged(false);
                    localColors = false;
                }

                changeFlags |= COLORS_CHANGED;
                notif = true;
                break;

            case FIELD_TEXCOORD :
                if (child == null) {
                    pTexCoord = null;
                    if (vfTexCoord != null) {
                        vfTexCoord.removeComponentListener(this);
                    }
                } else if (child instanceof VRMLProtoInstance) {
                    pTexCoord = (VRMLProtoInstance) child;
                    node = pTexCoord.getImplementationNode();

                    if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                        pTexCoord = null;
                        throw new
                            InvalidFieldValueException(TEXCOORD_PROTO_MSG);
                    }
                } else if (!(node instanceof VRMLTextureCoordinateNodeType)) {
                    throw new InvalidFieldValueException(TEXCOORD_NODE_MSG);
                }

                vfTexCoord = (VRMLTextureCoordinateNodeType) node;

                if (vfTexCoord != null)
                    vfTexCoord.addComponentListener(this);

                changeFlags |= TEXCOORDS_CHANGED;
                notif = true;
                break;

            default:
                super.setValue(index, child);
        }

        if(!inSetup && notif) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseComponentGeometryNode
    //----------------------------------------------------------

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    @Override
    protected void setCoordinateNode(VRMLCoordinateNodeType node) {
        if(inSetup)
            return;

        changeFlags |= COORDS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the color node being set. If the passed value is
     * null then that clears the node. The node passed is the actual color,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    @Override
    protected void setColorNode(VRMLColorNodeType node) {

        if(inSetup)
            return;

        changeFlags |= COLORS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the coordinate node being set. If the passed value is
     * null then that clears the node. The node passed is the actual geometry,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    protected void setTextureCoordinateNode(VRMLTextureCoordinateNodeType node) {
        if(inSetup)
            return;

        changeFlags |= TEXCOORDS_CHANGED;

        buildImpl();
    }

    /**
     * Notification of the color node being set. If the passed value is
     * null then that clears the node. The node passed is the actual color,
     * not any proto wrapper, that will have been previously stripped. The
     * default implementation does nothing.
     *
     * @param node The node to use
     */
    @Override
    protected void setNormalNode(VRMLNormalNodeType node) {

        changeFlags |= NORMALS_CHANGED;

        buildImpl();
    }



    //-------------------------------------------------------------
    // Methods defined by VRMLNodeComponentListener
    //-------------------------------------------------------------

    @Override
    public void fieldChanged(VRMLNodeType node, int index) {
        if(node == vfCoord) {
            changeFlags |= COORDS_CHANGED;
            buildImpl();
        } else if(node == vfColor) {
            changeFlags |= COLORS_CHANGED;
            buildImpl();
        } else if(node == vfNormal) {
            changeFlags |= NORMALS_CHANGED;
            buildImpl();
        } else if(node == vfTexCoord) {
            changeFlags |= TEXCOORDS_CHANGED;
            buildImpl();
        } else
            System.out.println("BaseIndexedTriangleSet: Unknown field for fieldChanged");

        stateManager.addEndOfThisFrameListener(this);
    }


    //----------------------------------------------------------
    // Local public methods
    //----------------------------------------------------------

    /**
     * Update the coordinate array in geomData based on the coordinate data.
     */
    protected void updateCoordinateArray() {
        if(vfCoord == null)
            geomData.vertexCount = 0;
        else {
            geomData.vertexCount = vfCoord.getNumPoints() / 3;
            geomData.coordinates = vfCoord.getPointRef();
        }
    }

    /**
     * Update the normal array in geomData based on the normal node and
     * normalPerVertex flag.
     */
    protected void updateNormalArray() {
        if((vfNormal != null) && vfNormalPerVertex) {
            geomData.normals = vfNormal.getVectorRef();
        }
    }

    /**
     * Update the normal array in geomData based on the normal node and
     * normalPerVertex flag.
     */
    protected void updateColorArray() {
        if(vfColor != null) {
            int index = vfColor.getFieldIndex("color");
            VRMLFieldData data = vfColor.getFieldValue(index);

            if(data.numElements != 0)
                geomData.colors = data.floatArrayValues;
            else
                geomData.colors = null;
        }
    }
}
