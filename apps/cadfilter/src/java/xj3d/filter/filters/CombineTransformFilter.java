/*****************************************************************************
 *                        Web3d.org Copyright (c) 2010
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
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.VRMLException;

import org.web3d.vrml.sav.SAVException;

import xj3d.filter.FieldValueHandler;
import xj3d.filter.NodeMarker;

import xj3d.filter.node.*;


/**
 * Filter for combing transform hierarchies. The transformational
 * components of the Transforms are combined and when possible
 * the coordinates of the children nodes are transformed to retain
 * the information.  Will respect DEFed nodes to indicate that
 * a node is to be used by ROUTING or SCRIPS.  Run removeUnneededDEF
 * to preprocess the file.
 *
 * Differs a fair bit from FlattenTransform.  Might eventually combine
 * them but right now not certain this filter is a superset of the
 * other.
 *
 * This filter makes use of the MatrixTransform node that some X3D
 * browsers have.  If your target browser does not have that node then
 * don't use this filter.  Or send me a piece of code that can convert
 * an arbitrary 4x4 matrix back into Transform fields.
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class CombineTransformFilter extends EncodedBaseFilter {
	private static final boolean FLATTEN_SHAPES = false;

    /** Scratch translation, used in transform calculations */
    private Vector3f translation;

    /** Scratch rotation, used in transform calculations */
    private Matrix3f rotation;

    /** Have we issued the suppress.  */
    private boolean issuedSuppress;

	private int unDEFedTransformCount;
	private int transformsFolded;

    /**
     * Create an instance of the filter.
     */
    public CombineTransformFilter() {

        issuedSuppress = false;

        translation = new Vector3f();
        rotation = new Matrix3f();
    }

    /**
     * Declaration of the start of the document. The parameters are all of the
     * values that are declared on the header line of the file after the
     * <CODE>#</CODE> start. The type string contains the representation of
     * the first few characters of the file after the #. This allows us to
     * work out if it is VRML97 or the later X3D spec.
     * <p>
     * Version numbers change from VRML97 to X3D and aren't logical. In the
     * first, it is <code>#VRML V2.0</code> and the second is
     * <code>#X3D V1.0</code> even though this second header represents a
     * later spec.
     *
     * @param uri The URI of the file.
     * @param url The base URL of the file for resolving relative URIs
     *    contained in the file
     * @param encoding The encoding of this document - utf8 or binary
     * @param type The bytes of the first part of the file header
     * @param version The VRML version of this document
     * @param comment Any trailing text on this line. If there is none, this
     *    is null.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
        @Override
    public void startDocument(String uri,
                              String url,
                              String encoding,
                              String type,
                              String version,
                              String comment)
        throws SAVException, VRMLException {


		// Version needs to be at least 3.3.
		String new_version = version;

		try {
			float val = Float.parseFloat(version.substring(1));

			if (val < 3.3f) {
				new_version = "V3.3";
			}
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
		}

        super.startDocument(uri, url, encoding, type, new_version, comment);
    }

    /**
     * A profile declaration has been found in the code. IAW the X3D
     * specification, this method will only ever be called once in the lifetime
     * of the parser for this document. The name is the name of the profile
     * for the document to use.
     *
     * @param profileName The name of the profile to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
        @Override
    public void profileDecl(String profileName)
        throws SAVException, VRMLException {

		super.profileDecl(profileName);

		super.componentDecl("EXT_Grouping:1");
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
        @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        if (encodeNodes) {
            Encodable use = encMap.get(defName);
            Encodable enc = (Encodable)encStack.peek();
            if ((enc != null) && (use != null)) {
                NodeMarker marker = (NodeMarker)nodeStack.peek();
                String fieldName = marker.fieldName;

                Encodable dup = use.clone(true);
				((BaseEncodable)dup).useName = defName;

				// TODO: this seems wrong for USE on a MFNode
                enc.setValue(fieldName, dup);
            }
        }

        contentHandler.useDecl(defName);
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
        @Override
    public void endDocument() throws SAVException, VRMLException {

        suppressCalls(false);

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        Encodable[] enc = scene.getRootNodes();
		analyze(enc, matrix, false);
System.out.println("Transformed Collapsed: " + transformsFolded);
        scene = null;

        super.endDocument();
    }

    /**
     * Notification of the start of a node. This is the opening statement of a
     * node and it's DEF name. USE declarations are handled in a separate
     * method.
     *
     * @param name The name of the node that we are about to parse
     * @param defName The string associated with the DEF name. Null if not
     *   given for this node.
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document
     * @throws VRMLException The content provided is invalid for this
     *   part of the document or can't be parsed
     */
        @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {

        if (!issuedSuppress) {
            suppressCalls(true);
            issuedSuppress = true;
        }

        super.startNode(name, defName);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

	/**
	 * Identify Transforms to combine and shapes to collapse
	 */
    private void analyze(Encodable[] enc, Matrix4f matrix, boolean wrap) {
    	boolean containsUSE = false;

            for (Encodable node : enc) {
                // TODO: Should be any node we can fold
                if (node instanceof Shape) {
                    if (((BaseEncodable)node).useName != null) {
                        containsUSE = true;
                        break;
                    }
                }
            }
//System.out.println("containsUSE: " + containsUSE);
            for (Encodable node : enc) {
                if (wrap && node instanceof BaseGroup) {
                    super.startNode("Group", null);
                    super.startField("children");
                }
                
                if (((BaseEncodable)node).useName != null) {
                    ejectNode(node, matrix);
                    //node.encode();
                    continue;
                }
                
                if (node instanceof Shape) {
                    Shape shape = (Shape)node;
                    String def = ((BaseEncodable)node).defName;
                    
                    if (unDEFedTransformCount > 1 && def == null) {
                        System.out.println("Can fold Shape: " + (unDEFedTransformCount));
                        unDEFedTransformCount = 0;
                        transformsFolded += (unDEFedTransformCount);
                    }
                    ejectNode(node, matrix);
                } else if (node instanceof Group) {
                    Group group = (Group) node;
                    String def = ((BaseEncodable)node).defName;
                    
                    if (containsUSE || def != null) {
                        ejectNode(node, matrix);
                    } else {
                        unDEFedTransformCount++;
                        
                        Encodable[] children = group.getChildren();
                        analyze(children, matrix, false);
                    }
                } else if (node instanceof Switch) {
                    boolean inTransform = ejectTransform(matrix);
                    
                    Switch snode = (Switch)node;
                    Encodable[] children = snode.getChildren();
                    
                    if (inTransform)
                        super.startField("children");   // MatrixTransform.children
                    super.startNode("Switch", snode.defName);
                    
                    super.startField("whichChoice");
                    super.fieldValue(snode.whichChoice);
                    super.startField("children");
                    
                    
                    Matrix4f mat = new Matrix4f();
                    mat.setIdentity();
                    
                    analyze(children, mat, true);
                    super.endField();  // Switch.children
                    super.endNode();
                    if (inTransform) {
                        super.endField();  // MatrixTransform.children
                        super.endNode();
                    }
                } else if (node instanceof Collision) {
                    boolean inTransform = ejectTransform(matrix);
                    
                    Collision snode = (Collision) node;
                    Encodable[] children = snode.getChildren();
                    
                    if (inTransform)
                        super.startField("children");   // MatrixTransform.children
                    super.startNode("Collision", snode.defName);
                    super.startField("enabled");
                    super.fieldValue(snode.enabled);
                    super.startField("children");
                    Matrix4f mat = new Matrix4f();
                    mat.setIdentity();
                    
                    analyze(children, mat, false);
                    super.endField();  // Collision.children
                    super.endNode();
                    if (inTransform) {
                        super.endField();  // MatrixTransform.children
                        super.endNode();
                    }
                } else if (node instanceof Transform) {
                    String def = ((BaseEncodable)node).defName;
                    Transform transform = (Transform)node;
                    
                    Matrix4f child_matrix = transform.getMatrix();
                    child_matrix.mul(matrix, child_matrix);
                    
                    if (containsUSE || def != null) {
                        ejectNode(node, matrix);
                    } else {
                        unDEFedTransformCount++;
                        
                        Encodable[] children = transform.getChildren();
                        analyze(children, child_matrix, false);
                    }
                    
                } else if (node instanceof MatrixTransform) {
                    ejectNode(node, matrix);
                    
                    MatrixTransform transform = (MatrixTransform)node;
                    Encodable[] children = transform.getChildren();
                    analyze(children, matrix, false);
                } else {
                    ejectNode(node, matrix);
                }

			if (wrap && node instanceof BaseGroup) {
				super.endField();
				super.endNode();
			}
            }

	}

	/**
	 * Send a node and any collected Matrix into the stream.
	 *
	 * @param node The node
	 * @param matrix The current transform stack
	 */
	private void ejectNode(Encodable node, Matrix4f matrix) {
		if (unDEFedTransformCount > 1) {
			System.out.println("Can fold: " + (unDEFedTransformCount - 1));
			transformsFolded += unDEFedTransformCount;
		}

		unDEFedTransformCount = 0;

		if (node instanceof Shape || node instanceof Viewpoint) {
			String use = ((BaseEncodable)node).useName;
			String def = ((BaseEncodable)node).defName;

			if (FLATTEN_SHAPES && def == null && use == null) {
				Encodable[] nodes = new Encodable[] {node};
				flatten(nodes, matrix);
				return;
			}
		}

		if (isIdentity(matrix)) {
			node.encode();
		} else {
			super.startNode("MatrixTransform", null);
			super.startField("matrix");
			float[] txMatrix = new float[16];
			txMatrix[0]  = matrix.m00;
			txMatrix[1]  = matrix.m10;
			txMatrix[2]  = matrix.m20;
			txMatrix[3]  = matrix.m30;

			txMatrix[4]  = matrix.m01;
			txMatrix[5]  = matrix.m11;
			txMatrix[6]  = matrix.m21;
			txMatrix[7]  = matrix.m31;

			txMatrix[8]  = matrix.m02;
			txMatrix[9]  = matrix.m12;
			txMatrix[10] = matrix.m22;
			txMatrix[11] = matrix.m32;

			txMatrix[12] = matrix.m03;
			txMatrix[13] = matrix.m13;
			txMatrix[14] = matrix.m23;
			txMatrix[15] = matrix.m33;

			super.fieldValue(txMatrix, 16);
			super.startField("children");

			node.encode();
			super.endField();
			super.endNode();
		}
	}

	/**
	 * Send any collected Matrix into the stream.  Leaves the
	 * node open, caller must call endNode.
	 *
	 * @param matrix The current transform stack
	 */
	private boolean ejectTransform(Matrix4f matrix) {
		unDEFedTransformCount = 0;

		if (isIdentity(matrix)) {
			return false;
		} else {
			super.startNode("MatrixTransform", null);
			super.startField("matrix");
			float[] txMatrix = new float[16];

			txMatrix[0]  = matrix.m00;
			txMatrix[1]  = matrix.m10;
			txMatrix[2]  = matrix.m20;
			txMatrix[3]  = matrix.m30;

			txMatrix[4]  = matrix.m01;
			txMatrix[5]  = matrix.m11;
			txMatrix[6]  = matrix.m21;
			txMatrix[7]  = matrix.m31;

			txMatrix[8]  = matrix.m02;
			txMatrix[9]  = matrix.m12;
			txMatrix[10] = matrix.m22;
			txMatrix[11] = matrix.m32;

			txMatrix[12] = matrix.m03;
			txMatrix[13] = matrix.m13;
			txMatrix[14] = matrix.m23;
			txMatrix[15] = matrix.m33;
			super.fieldValue(txMatrix, 16);
		}

		return true;
	}

    /**
     * Walk the nodes, transform the coordinates and normals of Shapes
     * and the position and orientation of Viewpoints, then encode them.
     *
     * @param enc An array of nodes
     * @param matrix The matrix to use to transform the coordinates.
     */
    private void flatten(Encodable[] enc, Matrix4f matrix) {
            for (Encodable node : enc) {
                if (node instanceof Shape) {
                    Shape shape = (Shape)node;
                    BaseGeometry geometry = (BaseGeometry)shape.getGeometry();
                    if (geometry != null) {
                        float[] point = null;
                        Coordinate coord = (Coordinate)geometry.getCoordinate();
                        if (coord != null) {
                            point = coord.point;
                            if (point != null) {
                                int num_point = coord.num_point;
                                float[] point_xfrm = new float[num_point*3];
                                Point3f p = new Point3f();
                                int idx = 0;
                                for (int j = 0; j < num_point; j++) {
                                    p.x = point[idx];
                                    p.y = point[idx+1];
                                    p.z = point[idx+2];
                                    matrix.transform(p);
                                    point_xfrm[idx] = p.x;
                                    point_xfrm[idx+1] = p.y;
                                    point_xfrm[idx+2] = p.z;
                                    idx += 3;
                                }
                                coord.point = point_xfrm;
                                
                                // Transformed so can't be USED
                                coord.useName = null;
                            }
                        }   float[] vector = null;
                        Normal normal = null;
                        if (geometry instanceof BaseComposedGeometry) {
                            normal = (Normal) ((IComposedGeometry) geometry).getNormal();
                            if (normal != null) {
                                vector = normal.vector;
                                if (vector != null) {
                                    int num_vector = normal.num_vector;
                                    float[] vector_xfrm = new float[num_vector*3];
                                    Vector3f v = new Vector3f();
                                    int idx = 0;
                                    for (int j = 0; j < num_vector; j++) {
                                        v.x = vector[idx];
                                        v.y = vector[idx+1];
                                        v.z = vector[idx+2];
                                        matrix.transform(v);
                                        
                                        // TODO: Shouldn't we normalize this normal?
                                        vector_xfrm[idx] = v.x;
                                        vector_xfrm[idx+1] = v.y;
                                        vector_xfrm[idx+2] = v.z;
                                        idx += 3;
                                    }
                                    normal.vector = vector_xfrm;
                                    
                                    // Transformed so can't be USED
                                    normal.useName = null;
                                }
                            }
                        }
                        shape.encode();
                        // restore the original data in case it is reused
                    if (coord != null) {
                        coord.point = point;
                    }   if (normal != null) {
                        normal.vector = vector;
                    }
                    } else {
                        // no geometry, nothing to transform
                        shape.encode();
                    }
                } else if (node instanceof Group) {
                    Group group = (Group)node;
                    Encodable[] children = group.getChildren();
                    flatten(children, matrix);
                } else if (node instanceof Switch) {
                    Switch snode = (Switch)node;
                    Encodable[] children = snode.getChildren();
                    
                    super.startNode("Switch", snode.defName);
                    
                    super.startField("whichChoice");
                    if (children.length == 0) {
                        super.fieldValue(snode.whichChoice);
                    } else {
                        super.fieldValue(0);
                    }
                    super.startField("children");
                    
                    if (children.length > 0) {
                        super.startNode("Group", null);
                        super.startField("children");
                    }
                    flatten(children, matrix);
                    super.endField();
                    super.endNode();
                    
                    if (children.length > 0) {
                        super.endField();
                        super.endNode();
                    }
                } else if (node instanceof Collision) {
                    Collision collision = (Collision)node;
                    Encodable[] children = collision.getChildren();
                    flatten(children, matrix);
                    
                } else if (node instanceof Transform) {
                    Transform transform = (Transform)node;
                    Encodable[] children = transform.getChildren();
                    ////////////////////////////////////////////////////////
                    // rem: not sure why the translation was handled
                    // separately here, but it did not work correctly.
                    // commenting out for now.......
                    Matrix4f child_matrix = transform.getMatrix();
                    /*
                    matrix.get(translation);
                    matrix.get(rotation);
                    float scale = matrix.getScale();
                    
                    child_matrix.get(child_translation);
                    
                    rotation.transform(child_translation);
                    
                    child_translation.scale(scale);
                    
                    translation.add(child_translation);
                    */
                    child_matrix.mul(matrix, child_matrix);
                    //child_matrix.setTranslation(translation);
                    ////////////////////////////////////////////////////////
                    flatten(children, child_matrix);
                    
                } else if (node instanceof MatrixTransform) {
                    MatrixTransform transform = (MatrixTransform)node;
                    Encodable[] children = transform.getChildren();
                    Matrix4f child_matrix = transform.getMatrix();
                    child_matrix.mul(matrix, child_matrix);
                    flatten(children, child_matrix);
                } else if (node instanceof Viewpoint) {
                    Viewpoint viewpoint = (Viewpoint)node;
                    
                    // transform the viewpoint position
                    if (viewpoint.position == null) {
                        viewpoint.position = new float[]{0, 0, 10};
                    }
                    float[] p = viewpoint.position;
                    Point3f pos = new Point3f();
                    pos.set(p[0], p[1], p[2]);
                    matrix.transform(pos);
                    pos.get(p);
                    
                    // transform the viewpoint orientation
                    if (viewpoint.orientation == null) {
                        viewpoint.orientation = new float[]{0, 0, 1, 0};
                    }
                    float[] o = viewpoint.orientation;
                Quat4f quat = new Quat4f();
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(o[0], o[1], o[2], o[3]);
                quat.set(rot);
                matrix.transform(quat);
                rot.set(quat);
                rot.get(o);

                viewpoint.encode();
            }
            }
    }

    private boolean isIdentity(Matrix4f mat) {
    	if (mat.m00 == 1 && mat.m11 == 1 && mat.m22 == 1 && mat.m33 == 1 &&
    	    mat.m01 == 0 && mat.m02 == 0 && mat.m03 == 0 &&
    	    mat.m10 == 0 && mat.m12 == 0 && mat.m13 == 0 &&
    	    mat.m20 == 0 && mat.m21 == 0 && mat.m23 == 0 &&
    	    mat.m30 == 0 && mat.m31 == 0 && mat.m32 == 0
    	    ) {

    		return true;
		} else {
			return false;
		}
    }
}
