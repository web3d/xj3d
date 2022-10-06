/*****************************************************************************
 *                        Shapeways, Inc. Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.filters;

// External imports

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.SAVException;
import xj3d.filter.NodeMarker;
import xj3d.filter.node.*;
import xj3d.filter.node.X3DConstants.TYPE;

import javax.vecmath.*;
import java.util.*;

// Local imports

/**
 * Filter for removing texture transforms. The transformational
 * components of the Transforms are combined and the coordinates of
 * the children nodes are transformed to retain the information. The
 * end result is a file containing geometry (Shape nodes) without the
 * grouping node.
 * <p>
 *
 * All geometry must be triangulated for this to work(TriangulationFilter).
 *
 * @author Rex Melton
 * @version $Revision: 1.18 $
 */
public class FlattenTextureTransformFilter extends CommonEncodedBaseFilter {

    /** Scratch translation, used in transform calculations */
    private Vector3f translation;

    /** Scratch rotation, used in transform calculations */
    private Matrix3f rotation;

    /** Scratch vecmath objs */
    private Point3f pnt;
    private Vector3f vec;
    private Quat4f quat;
    private AxisAngle4f rot;

    /** Have we issued the suppress.  */
    private boolean issuedSuppress;

    /** DEF'ed nodes, keyed by defName */
    private Map<String, CommonEncodable> defMap;

    /** USE'ed defNames */
    private Set<String> useSet;

    /**
     * Create an instance of the filter.
     */
    public FlattenTextureTransformFilter() {

        issuedSuppress = false;

        defMap = new HashMap<>(1_000);
        useSet = new HashSet<>(1_000);

        translation = new Vector3f();
        rotation = new Matrix3f();
        pnt = new Point3f();
        vec = new Vector3f();
        quat = new Quat4f();
        rot = new AxisAngle4f();
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------
    
    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws org.web3d.vrml.sav.SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws org.web3d.vrml.lang.VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        suppressCalls(false);

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        List<CommonEncodable> enc = scene.getRootNodes();
        flatten(enc, false);

        scene = null;
        defMap.clear();
        useSet.clear();

        super.endDocument();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not given
     * for this node.
     * @throws org.web3d.vrml.sav.SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws org.web3d.vrml.lang.VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void startNode(String name, String defName)
            throws SAVException, VRMLException {

        if (!issuedSuppress) {
            suppressCalls(true);
            issuedSuppress = true;
        }

        super.startNode(name, defName);

        if (defName != null) {
            CommonEncodable ce = (CommonEncodable) encStack.peek();
            defMap.put(defName, ce);
        }
    }

    /**
     * The field value is a USE for the given node name. This is a terminating
     * call for startField as well. The next call will either be another
     * <CODE>startField()</CODE> or
     * <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws org.web3d.vrml.sav.SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws org.web3d.vrml.lang.VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {

        CommonEncodable use = encMap.get(defName);
        CommonEncodable parent = (CommonEncodable)encStack.peek();
        if ((parent != null) && (use != null)) {

            NodeMarker marker = (NodeMarker)nodeStack.peek();
            String fieldName = marker.fieldName;

            if (fieldName.equals("geometry")) {
                CommonEncodable dup = use.clone(true);
                dup.setParent(parent);
                parent.setValue(fieldName, dup);

                return;
            }
        }

        super.useDecl(defName);

        // keep a set of the DEF's that are actually used
        useSet.add(defName);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Walk the nodes, transform the coordinates and normals of Shapes and the
     * position and orientation of Viewpoints, then encode them.
     *
     * @param enc An array of nodes
     * @param insideDEF Flag indicating that an ancestor of the child nodes in
     * the argument array of nodes was DEF'ed. If this is false, then the nodes
     * can be released for garbage collection.
     */
    private void flatten(List<CommonEncodable> enc, boolean insideDEF) {

        CommonEncodable transform = null;

        for (int i = 0; i < enc.size(); i++) {
            CommonEncodable node = enc.get(i);
            if (!insideDEF) {
                // null out the reference, make available for gc
                enc.set(i, null);
            }
            node = checkUse(node);

            if (node.isType(TYPE.X3DShapeNode)) {
                CommonEncodable shape = node;
                CommonEncodable geometry = checkUse((CommonEncodable) shape.getValue("geometry"));

                CommonEncodable appearance = checkUse((CommonEncodable) shape.getValue("appearance"));
                if (appearance != null) {
                    transform = checkUse((CommonEncodable) appearance.getValue("textureTransform"));
                    appearance.setValue("textureTransform",null);
                }

                if (transform != null && geometry != null) {
                    CommonEncodable coord = checkUse((CommonEncodable) geometry.getValue("texCoord"));
                    if (coord != null) {
                        CommonEncodable coord_xfrm = coord.clone(false);
                        ArrayData point_data = (ArrayData) coord.getValue("point");

                        Matrix4f matrix = getMatrix(transform);

                        if (point_data != null) {
                            float[] point = (float[]) point_data.data;
                            int num_coord = point_data.num;
                            int num_point = num_coord / 2;
                            float[] point_xfrm = new float[num_coord];
                            int idx = 0;
                            for (int j = 0; j < num_point; j++) {
                                pnt.x = point[idx];
                                pnt.y = point[idx + 1];
                                matrix.transform(pnt);
                                point_xfrm[idx] = pnt.x;
                                point_xfrm[idx + 1] = pnt.y;
                                idx += 2;
                            }
                            coord_xfrm.setValue("point", point_xfrm, num_coord);
                            geometry.setValue("texCoord", coord_xfrm);
                        }
                    }

                    shape.encode();

                    // restore the original data in case it is reused
                    if (coord != null) {
                        geometry.setValue("texCoord", coord);
                    }
                } else {
                    // no geometry, nothing to transform
                    shape.encode();
                }
            } else if (node.isType(TYPE.X3DGroupingNode)) {
                @SuppressWarnings("unchecked") // cast from Object type
                List<CommonEncodable> children =
                        (List<CommonEncodable>) node.getValue("children");

                String defName = node.getDefName();
                boolean isDEFed = (defName != null) & useSet.contains(defName);

                if ((children != null) && (children.size() > 0)) {
                    flatten(children, (insideDEF | isDEFed));
                }
            }
        }
    }

    /**
     * Return the transform matrix
     *
     * @return the transform matrix
     */
    private Matrix4f getMatrix(CommonEncodable transform) {
        TextureTransformMatrix matrixSource = new TextureTransformMatrix();
        ArrayData translation_data = (ArrayData) transform.getValue("translation");
        if (translation_data != null) {
            matrixSource.setTranslation((float[]) translation_data.data);
        }
        Float rotation_data = (Float) transform.getValue("rotation");
        if (rotation_data != null) {
            matrixSource.setRotation(rotation_data);
        }
        ArrayData scale_data = (ArrayData) transform.getValue("scale");
        if (scale_data != null) {
            matrixSource.setScale((float[]) scale_data.data);
        }

        ArrayData center_data = (ArrayData) transform.getValue("center");
        if (center_data != null) {
            matrixSource.setCenter((float[]) center_data.data);
        }
        return (matrixSource.getMatrix());
    }

    /**
     * Determine whether the argument node use's a def'ed node. Return the def
     * if so, otherwise return the argument
     */
    private CommonEncodable checkUse(CommonEncodable ce) {
        CommonEncodable rval = null;
        if (ce != null) {
            String useName = ce.getUseName();
            if (useName != null) {
                rval = defMap.get(useName);
            } else {
                rval = ce;
            }
        }
        return (rval);
    }
}
