/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.NodeMarker;
import xj3d.filter.node.ArrayData;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;
import xj3d.filter.node.TransformMatrix;
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * Filter for removing transform hierarchies. The transformational
 * components of the Transforms are combined and the coordinates of
 * the children nodes are transformed to retain the information. The
 * end result is a file containing geometry (Shape nodes) without the
 * grouping node.
 * <p>
 * At present - IndexedTriangleSet, IndexedTriangleStripSet,
 * IndexedTriangleFanSet, and Viewpoint nodes are transformed and
 * output.
 *
 * @author Rex Melton
 * @version $Revision: 1.18 $
 */
public class FlattenTransformFilter extends CommonEncodedBaseFilter {

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
    public FlattenTransformFilter() {

        issuedSuppress = false;

        defMap = new HashMap<>(1_000);
        useSet = new HashSet<>(1_000);

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
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
     * the document or can't be parsed
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {

        suppressCalls(false);

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        List<CommonEncodable> enc = scene.getRootNodes();
        flatten(enc, matrix, false,false);

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
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
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
     * @throws SAVException This call is taken at the wrong time in the
     * structure of the document
     * @throws VRMLException The content provided is invalid for this part of
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
     * @param matrix The matrix to use to transform the coordinates.
     * @param insideDEF Flag indicating that an ancestor of the child nodes in
     * the argument array of nodes was DEF'ed. If this is false, then the nodes
     * can be released for garbage collection.
     */
    private void flatten(List<CommonEncodable> enc, Matrix4f matrix, boolean insideDEF, boolean encUSE) {

        for (int i = 0; i < enc.size(); i++) {
            CommonEncodable node = enc.get(i);
            if (!insideDEF) {
                // null out the reference, make available for gc
                enc.set(i, null);
            }
            node = checkUse(node,encUSE);

            String nodeName = node.getNodeName();
            if (node.isType(TYPE.X3DShapeNode)) {
                CommonEncodable shape = node;
                CommonEncodable geometry = checkUse((CommonEncodable) shape.getValue("geometry"),encUSE);

                if (geometry != null) {
                    CommonEncodable coord = checkUse((CommonEncodable) geometry.getValue("coord"),encUSE);
                    if (coord != null) {
                        CommonEncodable coord_xfrm = coord.clone(false);
                        ArrayData point_data = (ArrayData) coord.getValue("point");
                        if (point_data != null) {
                            float[] point = (float[]) point_data.data;
                            int num_coord = point_data.num;
                            int num_point = num_coord / 3;
                            float[] point_xfrm = new float[num_coord];
                            int idx = 0;
                            for (int j = 0; j < num_point; j++) {
                                pnt.x = point[idx];
                                pnt.y = point[idx + 1];
                                pnt.z = point[idx + 2];
                                matrix.transform(pnt);
                                point_xfrm[idx] = pnt.x;
                                point_xfrm[idx + 1] = pnt.y;
                                point_xfrm[idx + 2] = pnt.z;
                                idx += 3;
                            }
                            coord_xfrm.setValue("point", point_xfrm, num_coord);
                            geometry.setValue("coord", coord_xfrm);
                        }
                    }

                    CommonEncodable normal = null;
                    if (geometry.isType(TYPE.X3DComposedGeometryNode)) {
                        normal = checkUse((CommonEncodable) geometry.getValue("normal"),encUSE);
                        if (normal != null) {
                            CommonEncodable normal_xfrm = normal.clone(false);
                            ArrayData vector_data = (ArrayData) normal.getValue("vector");
                            if (vector_data != null) {
                                float[] vector = (float[]) vector_data.data;
                                int num_coord = vector_data.num;
                                int num_vector = num_coord / 3;
                                float[] vector_xfrm = new float[num_coord];
                                int idx = 0;
                                for (int j = 0; j < num_vector; j++) {
                                    vec.x = vector[idx];
                                    vec.y = vector[idx + 1];
                                    vec.z = vector[idx + 2];
                                    matrix.transform(vec);
                                    vector_xfrm[idx] = vec.x;
                                    vector_xfrm[idx + 1] = vec.y;
                                    vector_xfrm[idx + 2] = vec.z;
                                    idx += 3;
                                }
                                normal_xfrm.setValue("vector", vector_xfrm, num_coord);
                                geometry.setValue("normal", normal_xfrm);
                            }
                        }
                    }

                    shape.encode();

                    // restore the original data in case it is reused
                    if (coord != null) {
                        geometry.setValue("coord", coord);
                    }
                    if (normal != null) {
                        geometry.setValue("normal", normal);
                    }
                } else {
                    // no geometry, nothing to transform
                    shape.encode();
                }
            } else if (node.isType(TYPE.X3DGroupingNode)) {
                boolean encd = node.getNodeName().equals("Switch");
                if (encUSE) encd = true;

                @SuppressWarnings("unchecked") // cast from Object type
                List<CommonEncodable> children =
                        (List<CommonEncodable>) node.getValue("children");

                String defName = node.getDefName();
                boolean isDEFed = (defName != null) & useSet.contains(defName);

                if ((children != null) && (children.size() > 0)) {
                    switch (nodeName) {
                        case "Transform":
                            {
                                Matrix4f child_matrix = getMatrix(node);
                                child_matrix.mul(matrix, child_matrix);
                                flatten(children, child_matrix, (insideDEF | isDEFed),encd);
                                break;
                            }
                        case "MatrixTransform":
                            {
                                ArrayData mtx_data = (ArrayData) node.getValue("matrix");
                                Matrix4f child_matrix;
                                if (mtx_data != null) {

                                    float[] mtx_array = (float[]) mtx_data.data;
                                    child_matrix = new Matrix4f(mtx_array);
                                    child_matrix.transpose();
                                    child_matrix.mul(matrix, child_matrix);
                                    flatten(children, child_matrix, (insideDEF | isDEFed),encd);

                                } else {
                                    flatten(children, matrix, (insideDEF | isDEFed),encd);
                                }       break;
                            }
                        default:
                            flatten(children, matrix, (insideDEF | isDEFed),encd);
                            break;
                    }
                }
            } else if (nodeName.equals("Viewpoint")) {

                CommonEncodable viewpoint = node;

                ArrayData position_data = (ArrayData) viewpoint.getValue("position");

                float[] p = new float[3];
                if (position_data == null) {
                    p[0] = 0;
                    p[1] = 0;
                    p[2] = 10;
                } else {
                    float[] pos = (float[]) position_data.data;
                    p[0] = pos[0];
                    p[1] = pos[1];
                    p[2] = pos[2];
                }
                pnt.set(p[0], p[1], p[2]);
                matrix.transform(pnt);
                pnt.get(p);
                viewpoint.setValue("position", p, 3);

                ArrayData cor_data = (ArrayData) viewpoint.getValue("centerOfRotation");

                float[] c = new float[3];
                if (cor_data == null) {
                    c[0] = 0;
                    c[1] = 0;
                    c[2] = 10;
                } else {
                    float[] cor = (float[]) cor_data.data;
                    c[0] = cor[0];
                    c[1] = cor[1];
                    c[2] = cor[2];
                }
                pnt.set(c[0], c[1], c[2]);
                matrix.transform(pnt);
                pnt.get(c);
                viewpoint.setValue("centerOfRotation", c, 3);

                ArrayData orientation_data = (ArrayData) viewpoint.getValue("orientation");

                float[] o = new float[4];
                if (orientation_data == null) {
                    o[0] = 0;
                    o[1] = 0;
                    o[2] = 1;
                    o[3] = 0;
                } else {
                    float[] ori = (float[]) orientation_data.data;
                    o[0] = ori[0];
                    o[1] = ori[1];
                    o[2] = ori[2];
                    o[3] = ori[3];
                }

                rot.set(o[0], o[1], o[2], o[3]);
                quat.set(rot);
                matrix.transform(quat);
                rot.set(quat);
                rot.get(o);
                viewpoint.setValue("orientation", o, 4);

                viewpoint.encode();

                // restore the original settings
                if (position_data != null) {
                    viewpoint.setValue("position", position_data);
                }
                if (orientation_data != null) {
                    viewpoint.setValue("orientation", orientation_data);
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
        TransformMatrix matrixSource = new TransformMatrix();
        ArrayData translation_data = (ArrayData) transform.getValue("translation");
        if (translation_data != null) {
            matrixSource.setTranslation((float[]) translation_data.data);
        }
        ArrayData rotation_data = (ArrayData) transform.getValue("rotation");
        if (rotation_data != null) {
            matrixSource.setRotation((float[]) rotation_data.data);
        }
        ArrayData scale_data = (ArrayData) transform.getValue("scale");
        if (scale_data != null) {
            matrixSource.setScale((float[]) scale_data.data);
        }
        ArrayData scaleOrientation_data = (ArrayData) transform.getValue("scaleOrientation");
        if (scaleOrientation_data != null) {
            matrixSource.setScaleOrientation((float[]) scaleOrientation_data.data);
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
    private CommonEncodable checkUse(CommonEncodable ce,boolean encUSE) {
        CommonEncodable rval = null;

        if (ce != null) {
            if (encUSE) {
                ce.useName = null;
                return ce;
            }

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
