/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.geospatial;

// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;

import org.j3d.aviatrix3d.BoundingVoid;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TransformGroup;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoViewpoint;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

import org.xj3d.core.eventmodel.OriginListener;

/**
 * OpenGL implementation of an GeoViewpoint
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class OGLGeoViewpoint extends BaseGeoViewpoint
    implements OGLViewpointNodeType, NodeUpdateListener, OGLTransformNodeType, OriginListener {

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the geoCoord value for some reason.";

    /** The transform group that holds the viewpoint */
    private TransformGroup transform;

    /** Flag to say the tx matrix has changed */
    private boolean matrixChanged;

    private Vector3f trans;

    /** Matrix that represents the local offsets */
    private Matrix4f implTrans;

    /** A tmp matrix for reading back actual navigated values */
    private Matrix4f tmpMatrix;

    /** The world scale */
    private float worldScale;

    /** Has the worldScale changed */
    private boolean scaleChanged;

    /** Has the matrix changed */
    private boolean updateMatrix;

    /**
     * Construct a default geoviewpoint instance
     */
    public OGLGeoViewpoint() {
        super();
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLGeoViewpoint(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return transform;
    }

    //----------------------------------------------------------
    // Methods defined by BaseViewpoint
    //----------------------------------------------------------

    /**
     * Convenience method to set the position of the viewpoint.
     *
     * @param pos The position vector to use
     */
    @Override
    protected void setPosition(double[] pos) {
        super.setPosition(pos);

        if (inSetup)
            return;

        updateMatrix = true;

        updateViewTrans();

        if (transform.isLive())
            transform.boundsChanged(this);
        else
            updateNodeBoundsChanges(transform);
    }

    /**
     * Convenience method to set the orientation of the viewpoint.
     *
     * @param dir The orientation quaternion to use
     */
    @Override
    protected void setOrientation(float[] dir) {
        super.setOrientation(dir);

        if (inSetup)
            return;

        updateMatrix = true;

        updateViewTrans();

        if (transform.isLive())
            transform.boundsChanged(this);
        else
            updateNodeBoundsChanges(transform);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLViewpointNodeType
    //-------------------------------------------------------------

    /**
     * Set the center of rotation of this viewpoint. The center is a position
     * in 3-space.
     *
     * @param pos The new position to use
     * @throws InvalidFieldValueException The field used is not 3 values
     */
    @Override
    public void setCenterOfRotation(float[] pos)
        throws InvalidFieldValueException {

		//if (centerOfRotation == null) {
		//	centerOfRotation = new float[3];
		//}

        centerOfRotation[0] = pos[0];
        centerOfRotation[1] = pos[1];
        centerOfRotation[2] = pos[2];

		if (local_origin != null) {
			centerOfRotation[0] -= local_origin[0];
			centerOfRotation[1] -= local_origin[1];
			centerOfRotation[2] -= local_origin[2];
		}

        fireCenterOfRotationChanged(centerOfRotation);
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

        OGLUserData data = new OGLUserData();
        transform.setUserData(data);
        data.owner = this;

        updateViewTrans();

        transform.setTransform(implTrans);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src) {
        if (updateMatrix) {
            //updateViewTrans();
            transform.setTransform(implTrans);

            scaleChanged = false;
            updateMatrix = false;
        } else if (scaleChanged) {
            // TODO: Restore this logic when we fix
            scaleChanged = false;
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Methods defined by OGLTransformNodeType
    //----------------------------------------------------------

    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.
     *
     * @return The matrix.
     */
    @Override
    public Matrix4f getTransform() {
        return implTrans;
    }

    //----------------------------------------------------------
    // Methods defined by OriginListener
    //----------------------------------------------------------

	/**
	 * Notification that the origin has changed.
	 */
    @Override
	public void originChanged() {
		Vector3f currentPosition = new Vector3f();
		implTrans.get(currentPosition);

		if (local_origin != null) {
			currentPosition.x += local_origin[0];
			currentPosition.y += local_origin[1];
			currentPosition.z += local_origin[2];

			localPosition[0] += local_origin[0];
			localPosition[1] += local_origin[1];
			localPosition[2] += local_origin[2];

			centerOfRotation[0] += local_origin[0];
			centerOfRotation[1] += local_origin[1];
			centerOfRotation[2] += local_origin[2];
		}

		Vector3d origin = originManager.getOrigin();
		if (origin == null) {
			local_origin = null;
		} else {
			if (local_origin == null) {
				local_origin = new double[3];
			}
			local_origin[0] = origin.x;
			local_origin[1] = origin.y;
			local_origin[2] = origin.z;

			currentPosition.x -= local_origin[0];
			currentPosition.y -= local_origin[1];
			currentPosition.z -= local_origin[2];

			localPosition[0] -= local_origin[0];
			localPosition[1] -= local_origin[1];
			localPosition[2] -= local_origin[2];

			centerOfRotation[0] -= local_origin[0];
			centerOfRotation[1] -= local_origin[1];
			centerOfRotation[2] -= local_origin[2];
		}

		implTrans.setTranslation(currentPosition);

		if (!vfIsBound) {
			// rem: only push the changed position into the TransformGroup if
			// this node is not active. If it is active, an updated matrix
			// will be pushed in by the NavigationProcessor.
			updateMatrix = true;
			if (transform.isLive()) {
				transform.boundsChanged(this);
			} else {
				updateNodeBoundsChanges(transform);
			}
		}
	}

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Get the default Transform representation of this viewpoint based on
     * its current position and orientation values. This is used to reset the
     * viewpoint to the original position after the user has moved around or
     * we transition between two viewpoints. It should remain independent of
     * the underlying TransformGroup.
     *
     * @return The default transform of this viewpoint
     */
    @Override
    public Matrix4f getViewTransform() {
        updateMatrix = true;
        updateViewTrans();

        return implTrans;
    }

    /**
     * Get the parent transform used to control the view platform. Used for
     * the navigation controls.
     *
     * @return The current view TransformGroup
     */
    @Override
    public TransformGroup getPlatformGroup() {
        return transform;
    }

    /**
     * Set a new transform for this viewpoint.  Used to notify
     * vp of navigation changes.
     *
     * @param trans The view transform
     */
    @Override
    public void setNavigationTransform(Matrix4f trans) {
        implTrans.set(trans);
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    @Override
    public void setWorldScale(float scale) {
        worldScale = scale;

        if(!inSetup) {
            scaleChanged = true;

            if (transform.isLive())
                transform.boundsChanged(this);
            else
                updateNodeBoundsChanges(transform);
        }
    }

    /**
     * Updates the TransformGroup fields from the VRML fields
     */
    private void updateViewTrans() {

        convOriToAxisAngle();
        implTrans.setIdentity();
        implTrans.set(axis);


        trans.x = (float)localPosition[0];
        trans.y = (float)localPosition[1];
        trans.z = (float)localPosition[2];

        implTrans.setTranslation(trans);
        implTrans.setScale(1f / worldScale);
    }

    /**
     * Private, internal, common iniitialisation.
     */
    private void init() {
        trans = new Vector3f();
        implTrans = new Matrix4f();
        tmpMatrix = new Matrix4f();
        implTrans.setIdentity();
        worldScale = 1;

        transform = new TransformGroup();
        transform.setPickMask(0);

        transform.setTransform(implTrans);
        transform.setBounds(new BoundingVoid());
    }
}
