/*****************************************************************************
 *                        Web3f.org Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.marker;

// External imports
import javax.vecmath.*;

import org.j3d.aviatrix3d.*;

import org.j3d.geom.GeometryData;

import org.j3d.renderer.aviatrix3d.nodes.MarkerGroup;

import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.image.NIOBufferImage;
import org.web3d.image.NIOBufferImageType;

import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.TypeConstants;

import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.marker.BaseScreenMarker;

import org.web3d.vrml.renderer.ogl.nodes.OGLAreaListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLGlobalStatus;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;


/**
 * OpenGL-renderer implementation of a ScreenMarker node.
 * <p>
 *
 * @author Rex Melton
 * @version $Revision: 1.8 $
 */
public class OGLScreenMarker extends BaseScreenMarker
    implements OGLVRMLNode,
               OGLAreaListener,
               NodeUpdateListener {

    /** Default marker geometry size */
    private static final float DEFAULT_SIZE = 0.1f;

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.SingleExternalNodeType,
        TypeConstants.ViewDependentNodeType  };

    /** The group holding the children */
    private TransformGroup implGroup;

    /** The group holding the children */
    private MarkerGroup markerGroup;

    /** The icon, in texture form */
    private Texture2D texture;
    private TextureUnit textureUnit;

    /** Flag to indicate the icon image has changed */
    private boolean iconImageChanged;
    private boolean textureChanged;

    /** Flag indicating the represents node has changed */
    private boolean targetChanged;

    /** MatrixUtils for gc free inversion */
    private MatrixUtils matrixUtils;

    /** Flag indicating the marker matrix has changed */
    private boolean matrixChanged;

    /** The matrix describing the marker transform */
    private Matrix4f markerMatrix;

    /** The marker position  */
    private Vector3f markerVector;

    private Vector3f inVector;
    private Matrix4f viewMatrix;

    private IndexedTriangleArray geom;
    private boolean aspectRatioChanged;

    /**
     * Construct a default instance of this node.
     */
    public OGLScreenMarker() {
        super();

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Collision node
     */
    public OGLScreenMarker(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    @Override
    public void allEventsComplete() {
        if (iconImageChanged) {
            if (textureUnit.isLive()) {
                textureUnit.dataChanged(this);
            } else {
                updateNodeDataChanges(textureUnit);
            }
            iconImageChanged = false;
        }
		if (aspectRatioChanged) {
			if (geom.isLive()) {
				geom.boundsChanged(this);
			} else {
				configAspectRatio();
				aspectRatioChanged = false;
			}
		}
        if (targetChanged) {
            if (vfRepresents != null) {
                Node target = (Node)((OGLVRMLNode)vfRepresents).getSceneGraphObject();
                if (target != null) {
                    markerGroup.setTarget(target);
                    targetChanged = false;
                } else {
                    stateManager.addEndOfThisFrameListener(this);
                }
            } else {
                markerGroup.setTarget(null);
                targetChanged = false;
            }
        }
    }

    //-------------------------------------------------------------
    // Methods defined by OGLVRMLNode
    //-------------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    @Override
    public SceneGraphObject getSceneGraphObject() {
        return(implGroup);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    @Override
    public int[] getSecondaryType() {
        return(SECONDARY_TYPE);
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    @Override
    public void setupFinished() {
        if (!inSetup)
            return;

        super.setupFinished();

		markerGroup.setEnabled(vfEnabled);
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
        if ((src == implGroup) && matrixChanged) {
            implGroup.setTransform(markerMatrix);
            matrixChanged = false;
        }
		if ((src == geom) && aspectRatioChanged) {
			configAspectRatio();
			aspectRatioChanged = false;
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
        if (src == textureUnit) {
            textureUnit.setTexture(texture);
        }
    }

    //-------------------------------------------------------------
    // Methods from OGLAreaListener
    //-------------------------------------------------------------

    /**
     * Invoked when the user enters an area.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param vpMatrix
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    @Override
    public void areaEntry(
        Point3f position,
        Vector3f orientation,
        Matrix4f vpMatrix,
        Matrix4f localPosition) {

		if (vfEnabled) {
        	process( position, orientation, vpMatrix, localPosition);
		}
    }

    /**
     * Notification that the user is still in the area, but that the
     * viewer reference point has changed.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user
     * @param vpMatrix
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    @Override
    public void userPositionChanged(
        Point3f position,
        Vector3f orientation,
        Matrix4f vpMatrix,
        Matrix4f localPosition) {

		if (vfEnabled) {
        	process( position, orientation, vpMatrix, localPosition);
		}
    }

    /**
     * Invoked when the tracked object exits then area.
     */
    @Override
    public void areaExit() {
    }

    //--------------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //--------------------------------------------------------------

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArgumentException The content object is not supported
     */
    @Override
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {

        if (content == null) {

            texture = null;

            iconImageChanged = true;
            stateManager.addEndOfThisFrameListener(this);

        } else if (content instanceof NIOBufferImage) {

            NIOBufferImage image = (NIOBufferImage)content;

            texture = new Texture2D();
            int format = getFormat(image);
            TextureComponent[] comp = new TextureComponent2D[1];

            comp[0] = new ByteBufferTextureComponent2D(
                format,
                image.getWidth( ),
                image.getHeight( ),
                image.getBuffer( null ) );

            int texType = getTextureFormat(comp[0]);

            texture.setSources(
                Texture2D.MODE_BASE_LEVEL,
                texType,
                comp,
                1 );

            iconImageChanged = true;
            stateManager.addEndOfThisFrameListener(this);
        }

        loadState = LOAD_COMPLETE;
        fireContentStateChanged();
    }

    //----------------------------------------------------------
    // Methods defined by BaseScreenMarker
    //----------------------------------------------------------

    /**
     * Set the sensor enabled or disabled.
     *
     * @param enabled The new enabled value
     */
    @Override
	protected void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		markerGroup.setEnabled(enabled);

		////////////////////////////////////////////////////////
		// this is bogus, but it forces a render
		iconImageChanged = true;
        stateManager.addEndOfThisFrameListener(this);
		////////////////////////////////////////////////////////
	}

    /**
     * Set the image width scale.
     *
     * @param width The image width scale.
     */
    @Override
	protected void setWidth(float width) {
		super.setWidth(width);

		aspectRatioChanged = true;
        stateManager.addEndOfThisFrameListener(this);
	}

    /**
     * Set the image height scale.
     *
     * @param height The image height scale.
     */
    @Override
	protected void setHeight(float height) {
		super.setHeight(height);

		aspectRatioChanged = true;
        stateManager.addEndOfThisFrameListener(this);
	}

    /**
     * Set the node that should be used for the represents field. Setting a
     * value of null will clear the current represents value.
     *
     * @param node The new node instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setRepresents(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.setRepresents(node);

        targetChanged = true;
        stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Common Initialization code.
     */
    private void init() {

        matrixUtils = new MatrixUtils();

        markerVector = new Vector3f();

        markerMatrix = new Matrix4f();
		markerMatrix.setIdentity();

        viewMatrix = new Matrix4f();
        inVector = new Vector3f();

        GeometryData geomData = new GeometryData();
        geomData.geometryType = GeometryData.QUADS;
        geomData.coordinates = new float[4 * 6 * 3];
        geomData.vertexCount = 4 * 6;

        OGLUserData user_data = new OGLUserData();
        user_data.geometryData = geomData;
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.areaListener = this;

        QuadArray geometry = new QuadArray();

        setBounds(geometry, geomData);

        Shape3D proxyShape = new Shape3D();
        proxyShape.setAppearance(OGLGlobalStatus.invisibleAppearance);
        proxyShape.setGeometry(geometry);
        proxyShape.setUserData(user_data);
		proxyShape.setPickMask(Shape3D.PROXIMITY_OBJECT);

        implGroup = new TransformGroup();

        user_data = new OGLUserData();
        user_data.isTransform = true;
        implGroup.setUserData(user_data);

        markerGroup = new MarkerGroup();

        user_data = new OGLUserData();
        user_data.collidable = false;
        user_data.isTerrain = false;
        markerGroup.setUserData(user_data);

        Shape3D markerShape = new Shape3D();
        markerShape.setGeometry(getGeometry());
        markerShape.setAppearance(getAppearance());

        markerGroup.addChild(markerShape);
        implGroup.addChild(markerGroup);
        implGroup.addChild(proxyShape);
    }

    /**
     * Create the base marker geometry
     */
    private IndexedTriangleArray getGeometry() {

        float[] vertex = new float[]{
            -0.1f, -0.1f, 0,
            -0.1f, 0.1f, 0,
            0.1f, 0.1f, 0,
            0.1f, -0.1f, 0};

        float[] normal = new float[]{
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1};

        float[][] tex_coord = new float[][]{{0, 0, 0, 1, 1, 1, 1, 0}};
        int[] tex_type = new int[]{VertexGeometry.TEXTURE_COORDINATE_2};
        int[] index = new int[]{0, 2, 1, 0, 3, 2};

        geom = new IndexedTriangleArray();
        geom.setVertices(VertexGeometry.COORDINATE_3, vertex, 4);
        geom.setIndices(index, 6);
        geom.setNormals(normal);
        geom.setTextureCoordinates(tex_type, tex_coord, 1);

        return(geom);
    }

    /**
     * Create the base marker appearance
     */
    private Appearance getAppearance() {

        Material material = new Material();
        material.setDiffuseColor(new float[] { 0, 0, 0 });
        material.setEmissiveColor(new float[] { 0, 0, 0 });
        material.setSpecularColor(new float[] { 1, 1, 1 });

        TextureUnit[] tu = new TextureUnit[1];
        textureUnit = new TextureUnit();
        tu[0] = textureUnit;
        textureUnit.setTexture(texture);

        Appearance app = new Appearance();
        app.setMaterial(material);
        app.setTextureUnits(tu, 1);

        return(app);
    }

    /**
     * Set the bounding box.
     */
    private void setBounds(QuadArray geometry, GeometryData geomData) {

        float[] coord = geomData.coordinates;
        float x = 1e10f;
        float y = 1e10f;
        float z = 1e10f;

        // face 1: +ve Z axis
        coord[0] =  x;
        coord[1] = -y;
        coord[2] =  z;

        coord[3] =  x;
        coord[4] =  y;
        coord[5] =  z;

        coord[6] = -x;
        coord[7] =  y;
        coord[8] =  z;

        coord[9] = -x;
        coord[10] = -y;
        coord[11] =  z;

        // face 2: +ve X axis
        coord[12] =  x;
        coord[13] = -y;
        coord[14] = -z;

        coord[15] =  x;
        coord[16] =  y;
        coord[17] = -z;

        coord[18] =  x;
        coord[19] =  y;
        coord[20] =  z;

        coord[21] =  x;
        coord[22] = -y;
        coord[23] =  z;

        // face 3: -ve Z axis
        coord[24] = -x;
        coord[25] = -y;
        coord[26] = -z;

        coord[27] = -x;
        coord[28] =  y;
        coord[29] = -z;

        coord[30] =  x;
        coord[31] =  y;
        coord[32] = -z;

        coord[33] =  x;
        coord[34] = -y;
        coord[35] = -z;

        // face 4: -ve X axis
        coord[36] = -x;
        coord[37] = -y;
        coord[38] =  z;

        coord[39] = -x;
        coord[40] =  y;
        coord[41] =  z;

        coord[42] = -x;
        coord[43] =  y;
        coord[44] = -z;

        coord[45] = -x;
        coord[46] = -y;
        coord[47] = -z;

        // face 5: +ve Y axis
        coord[48] =  x;
        coord[49] =  y;
        coord[50] =  z;

        coord[51] =  x;
        coord[52] =  y;
        coord[53] = -z;

        coord[54] = -x;
        coord[55] =  y;
        coord[56] = -z;

        coord[57] = -x;
        coord[58] =  y;
        coord[59] =  z;

        // face 6: -ve Y axis
        coord[60] = -x;
        coord[61] = -y;
        coord[62] = -z;

        coord[63] = -x;
        coord[64] = -y;
        coord[65] =  z;

        coord[66] =  x;
        coord[67] = -y;
        coord[68] =  z;

        coord[69] =  x;
        coord[70] = -y;
        coord[71] = -z;

        geometry.setVertices(
            QuadArray.COORDINATE_3,
            geomData.coordinates,
            geomData.vertexCount);
    }

    /**
     * Determine the marker position
     */
    private void process(
        Point3f position,
        Vector3f orientation,
        Matrix4f vpMatrix,
        Matrix4f localPosition) {

        // account for the position of this node in the scenegraph
        matrixUtils.inverse(vpMatrix, viewMatrix);

        inVector.set(viewMatrix.m02, viewMatrix.m12, viewMatrix.m22);
        inVector.scale(-10);

        viewMatrix.get(markerVector);
        markerVector.add(inVector);

        //markerMatrix.setIdentity();
        markerMatrix.setTranslation(markerVector);

        if (implGroup.isLive()) {
            matrixChanged = true;
            implGroup.boundsChanged(this);
        } else {
            implGroup.setTransform(markerMatrix);
        }
    }

    /**
     * From the image component format, generate the appropriate texture
     * format.
     *
     * @param comp The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    private int getTextureFormat(TextureComponent comp) {

        int ret_val = Texture.FORMAT_RGB;

        switch(comp.getFormat(0)) {
        case TextureComponent.FORMAT_SINGLE_COMPONENT:
            ret_val = Texture.FORMAT_LUMINANCE;
            break;

        case TextureComponent.FORMAT_INTENSITY_ALPHA:
            ret_val = Texture.FORMAT_LUMINANCE_ALPHA;
            break;

        case TextureComponent.FORMAT_RGB:
            ret_val = Texture.FORMAT_RGB;
            break;

        case TextureComponent.FORMAT_RGBA:
            ret_val = Texture.FORMAT_RGBA;
            break;
        }

        return ret_val;
    }

    /**
     * From the image information, generate the appropriate TextureComponent type.
     *
     * @param image The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    private int getFormat( NIOBufferImage image ) {

        int format=0;
        NIOBufferImageType type = image.getType( );

        if ( type == NIOBufferImageType.INTENSITY ) {

            format = TextureComponent.FORMAT_SINGLE_COMPONENT;

        } else if ( type == NIOBufferImageType.INTENSITY_ALPHA ) {

            format = TextureComponent.FORMAT_INTENSITY_ALPHA;

        } else if ( type == NIOBufferImageType.RGB ) {

            format = TextureComponent.FORMAT_RGB;

        } else if ( type == NIOBufferImageType.RGBA ) {

            format = TextureComponent.FORMAT_RGBA;

        } else {

            System.err.println("Unknown NIOBufferImageType: " + type.name);
        }

        return( format );
    }

	/**
	 * Reconfigure the size of the marker geometry
	 */
	private void configAspectRatio() {

		float w = DEFAULT_SIZE * vfWidth;
		float h = DEFAULT_SIZE * vfHeight;

		float[] vertex = new float[]{
			-w, -h, 0,
			-w, h, 0,
			w, h, 0,
			w, -h, 0};

		geom.setVertices(VertexGeometry.COORDINATE_3, vertex, 4);
	}
}
