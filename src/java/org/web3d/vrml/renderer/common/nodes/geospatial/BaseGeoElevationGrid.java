/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;
import org.web3d.vrml.util.FieldValidator;

import org.xj3d.core.eventmodel.OriginManager;

/**
 * Common renderer-independent implementation of a GeoElevationGrid
 * <p>
 *
 * TODO:
 *    Needed Listeners: solid
 *    Needed Observers: color, normal, texCoord
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public class BaseGeoElevationGrid extends AbstractNode
    implements VRMLComponentGeometryNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.OriginManagedNodeType
    };

    /** Index of the set_height field */
    protected static final int FIELD_SET_HEIGHT = LAST_NODE_INDEX + 1;

    /** Index of the color field */
    protected static final int FIELD_COLOR = LAST_NODE_INDEX + 2;

    /** Index of the normal field */
    protected static final int FIELD_NORMAL = LAST_NODE_INDEX + 3;

    /** Index of the texCoord field */
    protected static final int FIELD_TEXCOORD = LAST_NODE_INDEX + 4;

    /** Index of the ccw field */
    protected static final int FIELD_CCW = LAST_NODE_INDEX + 5;

    /** Index of the colorPerVertex field */
    protected static final int FIELD_COLORPERVERTEX = LAST_NODE_INDEX + 6;

    /** Index of the creaseAngle field */
    protected static final int FIELD_CREASEANGLE = LAST_NODE_INDEX + 7;

    /** Index of the geoGridOrigin field */
    protected static final int FIELD_GEO_GRID_ORIGIN = LAST_NODE_INDEX + 8;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_NODE_INDEX + 9;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_NODE_INDEX + 10;

    /** Index of the height field */
    protected static final int FIELD_HEIGHT = LAST_NODE_INDEX + 11;

    /** Index of the normalPerVertex field */
    protected static final int FIELD_NORMALPERVERTEX = LAST_NODE_INDEX + 12;

    /** Index of the solid field */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 13;

    /** Index of the xDimension field */
    protected static final int FIELD_XDIMENSION = LAST_NODE_INDEX + 14;

    /** Index of the xSpacing field */
    protected static final int FIELD_XSPACING = LAST_NODE_INDEX + 15;

    /** Index of the zDimension field */
    protected static final int FIELD_ZDIMENSION = LAST_NODE_INDEX + 16;

    /** Index of the zSpacing field */
    protected static final int FIELD_ZSPACING = LAST_NODE_INDEX + 17;

    /** Index of the yScale field */
    protected static final int FIELD_YSCALE = LAST_NODE_INDEX + 18;


    /** The last field index used by this class */
    protected static final int LAST_ELEVATIONGRID_INDEX = FIELD_YSCALE;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_ELEVATIONGRID_INDEX + 1;

    /** Message for when the proto is not a Color */
    private static final String COLOR_PROTO_MSG =
        "Proto does not describe a Color object";

    /** Message for when the node in setValue() is not a Color */
    private static final String COLOR_NODE_MSG =
        "Node does not describe a Color object";

    /** Message for when the proto is not a Normal */
    private static final String NORMAL_PROTO_MSG =
        "Proto does not describe a Normal object";

    /** Message for when the node in setValue() is not a Normal */
    private static final String NORMAL_NODE_MSG =
        "Node does not describe a Normal object";

    /** Message for when the proto is not a TexCoord */
    private static final String TEXCOORD_PROTO_MSG =
        "Proto does not describe a TexCoord object";

    /** Message for when the node in setValue() is not a TexCoord */
    private static final String TEXCOORD_NODE_MSG =
        "Node does not describe a TexCoord object";

    /** Message for when the proto is not a GeoOrigin */
    private static final String GEO_ORIGIN_PROTO_MSG =
        "Proto does not describe a GeoOrigin object";

    /** Message for when the node in setValue() is not a GeoOrigin */
    private static final String GEO_ORIGIN_NODE_MSG =
        "Node does not describe a GeoOrigin object";

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    protected static final String TRANSFORM_ERR_MSG =
        "Unable to transform the coordinate values for some reason.";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration fieldDecl[];

    /** Hashmap between a field name and its index */
    private static final Map<String, Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // VRML Field declarations

    /** Proto version of the color */
    protected VRMLProtoInstance pColor;

    /** exposedField SFNode color */
    protected VRMLColorNodeType vfColor;

    /** Proto version of the normal */
    protected VRMLProtoInstance pNormal;

    /** exposedField SFNode normal */
    protected VRMLNormalNodeType vfNormal;

    /** Proto version of the texCoord */
    protected VRMLProtoInstance pTexCoord;

    /** exposedField SFNode texCoord */
    protected VRMLTextureCoordinateNodeType vfTexCoord;

    /** field SFBool ccw TRUE */
    protected boolean vfCcw;

    /** field MFDouble height [] */
    protected double[] vfHeight;

    /** The number of active items in the height field */
    protected int heightLen;

    /** field SFDouble creaseAngle 0 */
    protected double vfCreaseAngle;

    /** field SFBool normalPerVertex TRUE*/
    protected boolean vfColorPerVertex;

    /** field SFBool normalPerVertex TRUE*/
    protected boolean vfNormalPerVertex;

    /** The solid field value */
    protected boolean vfSolid;

    /** field SFInt32 xDimension 0 */
    protected int vfXDimension;

    /** field SFDouble xSpacing 0.0 */
    protected double vfXSpacing;

    /** field SFInt32 zDimension 0 */
    protected int vfZDimension;

    /** field SFDouble zSpacing 0.0 */
    protected double vfZSpacing;

    /** field SFFloat yScale 1.0 */
    protected float vfYScale;

    /** field MFDouble geoGridOrigin 0 0 0 */
    protected double[] vfGeoGridOrigin;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /** The converted floating point array */
    private float[] renderPoints;

    /**
     * The calculated local version of the points taking into account both the
     * projection information and the GeoOrigin setting.
     */
    protected double[] localCoords;

    /**
     * Transformation used to make the coordinates to the local system. Does
     * not include the geoOrigin offset calcs.
     */
    protected MathTransform geoTransform;

    /**
     * Flag to say if the translation geo coords need to be swapped before
     * conversion.
     */
    protected boolean geoCoordSwap;

    /** Should local color node be used for diffuse lighting */
    protected boolean localColors;

    /** The list of listeners for localColor changes */
    protected List<LocalColorsListener> localColorsListeners;

    /** Manager for precision control */
    protected OriginManager originManager;

    /** Flag indicating that the OriginManager is enabled */
    protected boolean useOriginManager;

    /** The origin in use */
    protected double[] local_origin;

    /**
     * Static constructor to initialise the field handling
     */
    static {
        nodeFields = new int[] {
            FIELD_METADATA,
            FIELD_COLOR,
            FIELD_NORMAL,
            FIELD_TEXCOORD
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_HEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFDouble",
                                     "height");
        fieldDecl[FIELD_SET_HEIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFDouble",
                                     "set_height");
        fieldDecl[FIELD_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "color");
        fieldDecl[FIELD_NORMAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "normal");
        fieldDecl[FIELD_TEXCOORD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "texCoord");
        fieldDecl[FIELD_YSCALE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "yScale");
        fieldDecl[FIELD_CCW] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "ccw");
        fieldDecl[FIELD_COLORPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "colorPerVertex");
        fieldDecl[FIELD_CREASEANGLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "creaseAngle");
        fieldDecl[FIELD_NORMALPERVERTEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "normalPerVertex");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");
        fieldDecl[FIELD_XDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "xDimension");
        fieldDecl[FIELD_ZDIMENSION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFInt32",
                                     "zDimension");
        fieldDecl[FIELD_XSPACING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "xSpacing");
        fieldDecl[FIELD_ZSPACING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFDouble",
                                     "zSpacing");
        fieldDecl[FIELD_GEO_GRID_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3d",
                                     "geoGridOrigin");
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");

        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("height", FIELD_HEIGHT);
        fieldMap.put("set_height", FIELD_SET_HEIGHT);

        idx = FIELD_COLOR;
        fieldMap.put("color", idx);
        fieldMap.put("set_color", idx);
        fieldMap.put("color_changed", idx);

        idx = FIELD_NORMAL;
        fieldMap.put("normal", idx);
        fieldMap.put("set_normal", idx);
        fieldMap.put("normal_changed", idx);

        idx = FIELD_TEXCOORD;
        fieldMap.put("texCoord", idx);
        fieldMap.put("set_texCoord", idx);
        fieldMap.put("texCoord_changed", idx);

        idx = FIELD_YSCALE;
        fieldMap.put("yScale", idx);
        fieldMap.put("set_yScale", idx);
        fieldMap.put("yScale_changed", idx);

        fieldMap.put("colorPerVertex", FIELD_COLORPERVERTEX);
        fieldMap.put("ccw", FIELD_CCW);
        fieldMap.put("creaseAngle", FIELD_CREASEANGLE);
        fieldMap.put("normalPerVertex", FIELD_NORMALPERVERTEX);
        fieldMap.put("solid", FIELD_SOLID);
        fieldMap.put("xDimension", FIELD_XDIMENSION);
        fieldMap.put("xSpacing", FIELD_XSPACING);
        fieldMap.put("zDimension", FIELD_ZDIMENSION);
        fieldMap.put("zSpacing", FIELD_ZSPACING);
        fieldMap.put("geoGridOrigin", FIELD_GEO_GRID_ORIGIN);
        fieldMap.put("geoSystem", FIELD_GEO_SYSTEM);
        fieldMap.put("geoOrigin", FIELD_GEO_ORIGIN);
    }

    /**
     * Construct a default instance of this node.
     */
    public BaseGeoElevationGrid() {
        super("GeoElevationGrid");

        hasChanged = new boolean[NUM_FIELDS];

        vfColorPerVertex = true;
        vfNormalPerVertex = true;
        vfGeoSystem = new String[] {"GD","WE"};
        vfGeoGridOrigin = new double[3];

        vfHeight = FieldConstants.EMPTY_MFDOUBLE;
        heightLen = 0;
        vfCreaseAngle = 0;
        vfXDimension = 0;
        vfXSpacing = 0;
        vfZDimension = 0;
        vfZSpacing = 0;
        vfYScale = 1;

        vfSolid = true;
        vfColorPerVertex = true;
        vfNormalPerVertex = true;
        vfCcw = true;

        localColors = false;
        localColorsListeners = new ArrayList<>(1);
        localCoords = FieldConstants.EMPTY_MFDOUBLE;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseGeoElevationGrid(VRMLNodeType node) {
        this(); // invoke default constructor

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("ccw");
            VRMLFieldData field = node.getFieldValue(index);
            vfCcw = field.booleanValue;

            index = node.getFieldIndex("creaseAngle");
            field = node.getFieldValue(index);
            vfCreaseAngle = field.floatValue;

            index = node.getFieldIndex("normalPerVertex");
            field = node.getFieldValue(index);
            vfNormalPerVertex = field.booleanValue;

            index = node.getFieldIndex("colorPerVertex");
            field = node.getFieldValue(index);
            vfColorPerVertex = field.booleanValue;

            index = node.getFieldIndex("xDimension");
            field = node.getFieldValue(index);
            vfXDimension = field.intValue;

            index = node.getFieldIndex("zDimension");
            field = node.getFieldValue(index);
            vfZDimension = field.intValue;

            index = node.getFieldIndex("xSpacing");
            field = node.getFieldValue(index);
            vfXSpacing = field.doubleValue;

            index = node.getFieldIndex("zSpacing");
            field = node.getFieldValue(index);
            vfZSpacing = field.doubleValue;

            index = node.getFieldIndex("yScale");
            field = node.getFieldValue(index);
            vfYScale = field.floatValue;

            index = node.getFieldIndex("height");
            field = node.getFieldValue(index);
            vfHeight = field.doubleArrayValues;
            heightLen = field.numElements;

            index = node.getFieldIndex("solid");
            field = node.getFieldValue(index);
            vfSolid = field.booleanValue;

            index = node.getFieldIndex("geoGridOrigin");
            field = node.getFieldValue(index);
            vfGeoGridOrigin[0] = field.doubleArrayValues[0];
            vfGeoGridOrigin[1] = field.doubleArrayValues[1];
            vfGeoGridOrigin[2] = field.doubleArrayValues[2];

            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValues, 0, vfGeoSystem, 0,
                    field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    @Override
    public boolean hasLocalColors() {
        return localColors;
    }

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    @Override
    public boolean hasLocalColorAlpha() {
        return (vfColor != null) && (vfColor.getNumColorComponents() == 4);
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addLocalColorsListener(LocalColorsListener l) {
        if (l != null)
            localColorsListeners.add(l);
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeLocalColorsListener(LocalColorsListener l) {
        localColorsListeners.remove(l);
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
        System.out.println("GeoEV TexCoordGenMode changes not implemented");
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    @Override
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    @Override
    public String getTexCoordGenMode(int setNum) {
        if(vfTexCoord == null)
            return null;

        return vfTexCoord.getTexCoordGenMode(setNum);
    }

    /**
     * Set the number of textures that were found on the accompanying Appearance
     * node. Used to set the number of texture coordinates that need to be
     * passed in to the renderer when no explicit texture coordinates were
     * given.
     *
     * @param count The number of texture coordinate sets to add
     */
    @Override
    public void setTextureCount(int count) {
        // default implementation does nothing
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    @Override
    public int getNumSets() {
        if(vfTexCoord == null)
            return 0;

        return vfTexCoord.getNumSets();
    }

    /*
     * Specifies whether a geometry object is a solid opject.
     * If true, then back-face culling can be performed
     *
     * @params newSolid The new value of solid
     */
    @Override
    public boolean isSolid() {
        return vfSolid;
    }

    /**
     * Get the value of the CCW field.
     *
     * @return true Vertices are declared in counter-clockwise order
     */
    @Override
    public boolean isCCW() {
        return vfCcw;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    @Override
    public boolean isLightingEnabled() {
        return true;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLComponentGeometryNodeType
    //-------------------------------------------------------------

    /**
     * Check to see if the colors are per vertex or per face.
     *
     * @return true The colors are per vertex
     */
    @Override
    public boolean hasColorPerVertex() {
        return vfColorPerVertex;
    }

    /**
     * Check to see if the normals are per vertex or per face.
     *
     * @return true The normals are per vertex
     */
    @Override
    public boolean hasNormalPerVertex() {
        return vfNormalPerVertex;
    }

    /**
     * Check to see if this geometry implementation type requires unlit color
     * values to be set. For the most part this will always return false, but
     * some will need it (points and lines). This value should be constant for
     * the geometry regardless of whether a Color component has been provided
     * or not. It is up to the implementation to decide when to pass these
     * values on to the underlying rendering structures or not.
     * <p>
     *
     * The default implementation returns false. Override if different
     * behaviour is needed.
     *
     * @return true if we need unlit colour information
     */
    @Override
    public boolean requiresUnlitColor() {
        return false;
    }

    /**
     * Set the local colour override for this geometry. Typically used to set
     * the emissiveColor from the Material node into the geometry for the line
     * and point-type geometries which are unlit in the X3D/VRML model.
     * <p>
     *
     * The default implementation does nothing. Override to do something useful.
     *
     * @param color The colour value to use
     */
    @Override
    public void setUnlitColor(float[] color) {
    }

    /**
     * Get the components that compose a geometry object.
     * <p>
     * If there are no components then a zero length array will be returned.
     * @return VRMLNodeType[] The components
     */
    @Override
    public VRMLNodeType[] getComponents() {

        int cnt = 3;
        if(vfNormal == null)
            cnt--;
        if(vfTexCoord == null)
            cnt--;

        if(vfColor == null)
            cnt--;

        VRMLNodeType[] ret = new VRMLNodeType[cnt];

        cnt = 0;
        if(pNormal != null)
            ret[cnt++] = pNormal;
        else if (vfNormal != null)
            ret[cnt++] = vfNormal;

        if(pTexCoord != null)
            ret[cnt++] = pTexCoord;
        else if (pTexCoord != null)
            ret[cnt++] = vfTexCoord;

        if(pColor != null)
            ret[cnt++] = pColor;
        else if (vfColor != null)
            ret[cnt++] = vfColor;

        return ret;
    }

    /**
     * Set the components that compose a geometry object.
     *
     * @param comps An array of geometric properties
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setComponents(VRMLNodeType[] comps)
        throws InvalidFieldValueException {

        VRMLProtoInstance proto;
        VRMLNodeType node;

        for (VRMLNodeType comp : comps) {
            node = comp;
            if (node instanceof VRMLProtoInstance) {
                proto = (VRMLProtoInstance) node;
                node = proto.getImplementationNode();
            } else {
                proto = null;
            }
            switch(node.getPrimaryType()) {
                case TypeConstants.NormalNodeType:
                    pNormal = proto;
                    vfNormal = (VRMLNormalNodeType) node;
                    break;

                case TypeConstants.TextureCoordinateNodeType:
                    pTexCoord = proto;
                    vfTexCoord = (VRMLTextureCoordinateNodeType) node;
                    break;

                case TypeConstants.ColorNodeType:
                    pColor = proto;
                    vfColor = (VRMLColorNodeType) node;

                    if (vfColor != null) {
                        //vfColor.addComponentListener(this);
                        if (!localColors)
                            fireLocalColorsChanged(true);
                        localColors = true;
                    } else {
                        if (localColors)
                            fireLocalColorsChanged(false);
                        localColors = false;
                    }

                    break;

                default:
                    throw new InvalidFieldValueException("Unknown component type");
            }
        }
    }

    /**
     * Set a component that composes part of a geometry object.
     *
     * @param comp A geometric property
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    @Override
    public void setComponent(VRMLNodeType comp)  throws
        InvalidFieldValueException {

        VRMLProtoInstance proto;
        VRMLNodeType node = comp;

        if(node instanceof VRMLProtoInstance) {
            proto = (VRMLProtoInstance) node;
            node = proto.getImplementationNode();
        } else {
            proto = null;
        }

        switch(node.getPrimaryType()) {
            case TypeConstants.NormalNodeType:
                pNormal = proto;
                vfNormal = (VRMLNormalNodeType) node;
                break;

            case TypeConstants.TextureCoordinateNodeType:
                pTexCoord = proto;
                vfTexCoord = (VRMLTextureCoordinateNodeType) node;
                break;

            case TypeConstants.ColorNodeType:
                pColor = proto;
                vfColor = (VRMLColorNodeType) node;

                if (vfColor != null) {
                    //vfColor.addComponentListener(this);
                    if (!localColors)
                        fireLocalColorsChanged(true);
                    localColors = true;
                } else {
                    if (localColors)
                        fireLocalColorsChanged(false);
                    localColors = false;
                }
                break;

            default:
                throw new InvalidFieldValueException("Unknown component type");
        }
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

        if(pColor != null)
            pColor.setupFinished();
        else if(vfColor != null)
            vfColor.setupFinished();

        if(pNormal != null)
            pNormal.setupFinished();
        else if(vfNormal != null)
            vfNormal.setupFinished();

        if(pTexCoord != null)
            pTexCoord.setupFinished();
        else if(vfTexCoord != null)
            vfTexCoord.setupFinished();

        if(pGeoOrigin != null)
            pGeoOrigin.setupFinished();
        else if(vfGeoOrigin != null)
            vfGeoOrigin.setupFinished();

        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            geoTransform = gtu.createSystemTransform(vfGeoSystem, swap);
            geoCoordSwap = swap[0];
        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0 || index > LAST_ELEVATIONGRID_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @return The number of fields.
     */
    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    @Override
    public int getPrimaryType() {
        return TypeConstants.ComponentGeometryNodeType;
    }

    /**
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    @Override
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    @Override
    public VRMLFieldData getFieldValue(int index)
        throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_COLOR:
                fieldData.clear();
                if (pColor != null)
                    fieldData.nodeValue = pColor;
                else
                    fieldData.nodeValue = vfColor;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_NORMAL:
                fieldData.clear();
                if (pNormal != null)
                    fieldData.nodeValue = pNormal;
                else
                    fieldData.nodeValue = vfNormal;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_TEXCOORD:
                fieldData.clear();
                if (pTexCoord != null)
                    fieldData.nodeValue = pTexCoord;
                else
                    fieldData.nodeValue = vfTexCoord;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if (pGeoOrigin != null)
                    fieldData.nodeValue = pGeoOrigin;
                else
                    fieldData.nodeValue = vfGeoOrigin;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValues = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
                break;

            case FIELD_GEO_GRID_ORIGIN:
                fieldData.clear();
                fieldData.doubleArrayValues = vfGeoGridOrigin;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_HEIGHT:
                fieldData.clear();
                fieldData.doubleArrayValues = vfHeight;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = heightLen;
                break;

            case FIELD_CCW:
                fieldData.clear();
                fieldData.booleanValue = vfCcw;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_CREASEANGLE:
                fieldData.clear();
                fieldData.doubleValue = vfCreaseAngle;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_COLORPERVERTEX:
                fieldData.clear();
                fieldData.booleanValue = vfColorPerVertex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_NORMALPERVERTEX:
                fieldData.clear();
                fieldData.booleanValue = vfNormalPerVertex;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_SOLID:
                fieldData.clear();
                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_XDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfXDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_ZDIMENSION:
                fieldData.clear();
                fieldData.intValue = vfZDimension;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FIELD_XSPACING:
                fieldData.clear();
                fieldData.doubleValue = vfXSpacing;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_ZSPACING:
                fieldData.clear();
                fieldData.doubleValue = vfZSpacing;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_YSCALE:
                fieldData.clear();
                fieldData.floatValue = vfYScale;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_COLOR:
                    if (pColor != null)
                        destNode.setValue(destIndex, pColor);
                    else
                        destNode.setValue(destIndex, vfColor);
                    break;

                case FIELD_NORMAL:
                    if (pNormal != null)
                        destNode.setValue(destIndex, pNormal);
                    else
                        destNode.setValue(destIndex, vfNormal);
                    break;

                case FIELD_TEXCOORD:
                    if (pTexCoord != null)
                        destNode.setValue(destIndex, pTexCoord);
                    else
                        destNode.setValue(destIndex, vfTexCoord);
                    break;
                case FIELD_YSCALE:
                    destNode.setValue(destIndex, vfYScale);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CCW:
               if(!inSetup)
                   throwInitOnlyWriteException("ccw");

                vfCcw = value;
                break;

            case FIELD_COLORPERVERTEX:
               if(!inSetup)
                   throwInitOnlyWriteException("colorPerVertex");

                vfColorPerVertex = value;
                break;

            case FIELD_NORMALPERVERTEX:
               if(!inSetup)
                   throwInitOnlyWriteException("normalPerVertex");

                vfNormalPerVertex = value;
                break;

            case FIELD_SOLID:
               if(!inSetup)
                   throwInitOnlyWriteException("solid");

                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an int.
     * This would be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_XDIMENSION:
               if(!inSetup)
                   throwInitOnlyWriteException("xDimension");

                FieldValidator.checkDoublePosInfinity("ElevationGrid.xDimension", value);
                vfXDimension = value;
                break;

            case FIELD_ZDIMENSION:
               if(!inSetup)
                   throwInitOnlyWriteException("zDimension");

                vfZDimension = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFDouble and SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CREASEANGLE:
                if(!inSetup)
                    throwInitOnlyWriteException("creaseAngle");

                FieldValidator.checkDoublePosInfinity("ElevationGrid.creaseAngle",value);
                vfCreaseAngle = value;
                break;

            case FIELD_XSPACING:
                if(!inSetup)
                    throwInitOnlyWriteException("xSpacing");

                FieldValidator.checkDoublePosInfinity("ElevationGrid.xSpacing",value);
                vfXSpacing = value;
                break;

            case FIELD_ZSPACING:
                if(!inSetup)
                    throwInitOnlyWriteException("zSpacing");

                FieldValidator.checkDoublePosInfinity("ElevationGrid.zSpacing",value);
                vfZSpacing = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index is not a valid field
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    @Override
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_YSCALE:
                FieldValidator.checkDoublePosInfinity("GeoElevationGrid.yScale",value);
                vfYScale = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    @Override
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_HEIGHT:
                if(!inSetup)
                   throwInitOnlyWriteException("height");


                vfHeight = value;
                heightLen = numValid;
                break;

            case FIELD_SET_HEIGHT:
                if(inSetup)
                   throwInputOnlyWriteException("set_height");

                if(vfHeight.length < numValid)
                    vfHeight = new double[numValid];

                System.arraycopy(vfHeight, 0, value, 0, numValid);
                heightLen = numValid;

                hasChanged[FIELD_SET_HEIGHT] = true;
                fireFieldChanged(FIELD_SET_HEIGHT);
                break;

            case FIELD_GEO_GRID_ORIGIN:
                if(!inSetup)
                   throwInitOnlyWriteException("geoGridOrigin");

                vfGeoGridOrigin[0] = value[0];
                vfGeoGridOrigin[1] = value[1];
                vfGeoGridOrigin[2] = value[2];
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    @Override
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
                if(!inSetup)
                   throwInitOnlyWriteException("geoSystem");

                if(vfGeoSystem.length != numValid)
                    vfGeoSystem = new String[numValid];

                System.arraycopy(value, 0, vfGeoSystem, 0, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
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

        switch(index) {
            case FIELD_NORMAL:
                setNormal(child);
                break;

            case FIELD_COLOR:
                setColor(child);
                break;

            case FIELD_TEXCOORD:
                setTexCoord(child);
                break;

            case FIELD_GEO_ORIGIN:
                if(!inSetup)
                   throwInitOnlyWriteException("geoOrigin");

                setGeoOrigin(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Send the localColorsChanged event to LocalColorsListeners
     *
     * @param enabled Whether local colors are used.
     */
    protected void fireLocalColorsChanged(boolean enabled) {
        int size = localColorsListeners.size();
        LocalColorsListener l;

        boolean has_alpha = (vfColor != null) &&
                            (vfColor.getNumColorComponents() == 4);

        for(int i = 0; i < size; i++) {
            try {
                l = localColorsListeners.get(i);
                l.localColorsChanged(enabled, has_alpha);
            } catch(Exception e) {
                System.err.println("Error sending localColorsChanged message: "
                                   + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeoOrigin(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        BaseGeoOrigin node;
        VRMLNodeType old_node;

        if(pGeoOrigin != null)
            old_node = pGeoOrigin;
        else
            old_node = vfGeoOrigin;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoOrigin))
                throw new InvalidFieldValueException(GEO_ORIGIN_PROTO_MSG);

            node = (BaseGeoOrigin)impl;
            pGeoOrigin = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoOrigin)) {
            throw new InvalidFieldValueException(GEO_ORIGIN_NODE_MSG);
        } else {
            pGeoOrigin = null;
            node = (BaseGeoOrigin)geo;
        }

        vfGeoOrigin = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, false);

    }

    /**
     * Set node content for the normal node.
     *
     * @param norm The new normal
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setNormal(VRMLNodeType norm)
        throws InvalidFieldValueException {

        VRMLNormalNodeType node;
        VRMLNodeType old_node;

        if(pNormal != null)
            old_node = pNormal;
        else
            old_node = vfNormal;

        if(norm instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)norm).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLNormalNodeType))
                throw new InvalidFieldValueException(NORMAL_PROTO_MSG);

            node = (VRMLNormalNodeType)impl;
            pNormal = (VRMLProtoInstance)norm;

        } else if(norm != null && !(norm instanceof VRMLNormalNodeType)) {
            throw new InvalidFieldValueException(NORMAL_NODE_MSG);
        } else {
            pNormal = null;
            node = (VRMLNormalNodeType)norm;
        }

        vfNormal = node;
        if(norm != null)
            updateRefs(norm, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_NORMAL] = true;
            fireFieldChanged(FIELD_NORMAL);
        }
    }

    /**
     * Set node content for the color node.
     *
     * @param col The new color
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setColor(VRMLNodeType col)
        throws InvalidFieldValueException {

        VRMLColorNodeType node;
        VRMLNodeType old_node;

        if(pColor != null)
            old_node = pColor;
        else
            old_node = vfColor;

        if(col instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)col).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLColorNodeType))
                throw new InvalidFieldValueException(COLOR_PROTO_MSG);

            node = (VRMLColorNodeType)impl;
            pColor = (VRMLProtoInstance)col;

        } else if(col != null && !(col instanceof VRMLColorNodeType)) {
            throw new InvalidFieldValueException(COLOR_NODE_MSG);
        } else {
            pColor = null;
            node = (VRMLColorNodeType)col;
        }

        vfColor = node;
        if(col != null)
            updateRefs(col, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if (vfColor != null) {
            //vfColor.addComponentListener(this);
            if (!localColors)
                fireLocalColorsChanged(true);
            localColors = true;
        } else {
            if (localColors)
                fireLocalColorsChanged(false);
            localColors = false;
        }

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_COLOR] = true;
            fireFieldChanged(FIELD_COLOR);
        }
    }

    /**
     * Set node content for the texCoord node.
     *
     * @param tex The new tex coordinate node instance
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setTexCoord(VRMLNodeType tex)
        throws InvalidFieldValueException {

        VRMLTextureCoordinateNodeType node;
        VRMLNodeType old_node;

        if(pTexCoord != null)
            old_node = pTexCoord;
        else
            old_node = vfTexCoord;

        if(tex instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)tex).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof VRMLTextureCoordinateNodeType))
                throw new InvalidFieldValueException(TEXCOORD_PROTO_MSG);

            node = (VRMLTextureCoordinateNodeType)impl;
            pTexCoord = (VRMLProtoInstance)tex;

        } else if(tex != null && !(tex instanceof VRMLTextureCoordinateNodeType)) {
            throw new InvalidFieldValueException(TEXCOORD_NODE_MSG);
        } else {
            pTexCoord = null;
            node = (VRMLTextureCoordinateNodeType)tex;
        }

        vfTexCoord = node;
        if(tex != null)
            updateRefs(tex, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_TEXCOORD] = true;
            fireFieldChanged(FIELD_TEXCOORD);
        }
    }
}
