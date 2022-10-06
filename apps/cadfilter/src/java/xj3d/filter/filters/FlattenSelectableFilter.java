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

import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.SAVException;
import xj3d.filter.NodeMarker;
import xj3d.filter.node.CommonEncodable;
import xj3d.filter.node.CommonEncodedBaseFilter;

import javax.vecmath.*;
import java.util.*;

// Local imports

/**
 * Filter for removing children not part of a selectable node such as Switch or LOD. The
 * end result is a file containing geometry (Shape nodes) without the
 * grouping node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public class FlattenSelectableFilter extends CommonEncodedBaseFilter {

    /** Scratch vecmath objs */
    private Point3f pnt;
    private Vector3f vec;
    private Quat4f quat;
    private AxisAngle4f rot;

    /** Have we issued the suppress.  */
    private boolean issuedSuppress;

    /** Have we found a selectable node */
    private boolean selectableFound;

    /**
     * Create an instance of the filter.
     */
    public FlattenSelectableFilter() {

        issuedSuppress = false;

        pnt = new Point3f();
        vec = new Vector3f();
        quat = new Quat4f();
        rot = new AxisAngle4f();

        selectableFound = false;
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
        flatten(enc, matrix, false,false);

        scene = null;

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

        if (name.equals("Switch") || name.equals("LOD")) {
            selectableFound = true;
        }

        super.startNode(name, defName);
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

            if (selectableFound) {
                CommonEncodable dup = use.clone(true);
                dup.setParent(parent);
                parent.setValue(fieldName, dup);

                return;
            }
        }

        super.useDecl(defName);
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

        for (CommonEncodable node : enc) {
            String nodeName = node.getNodeName();
            switch (nodeName) {
                case "Switch":
                    {
                        // remove all but the selected one
                        int which = (Integer) node.getValue("whichChoice");
                        @SuppressWarnings("unchecked") // cast from Object type
                        List<CommonEncodable> children =
                                (List<CommonEncodable>) node.getValue("children");
                        if (which > -1) {
                            children.get(which).encode();
                        }       break;
                    }
                case "LOD":
                {
                    @SuppressWarnings("unchecked") // cast from Object type
                    List<CommonEncodable> children =
                            (List<CommonEncodable>) node.getValue("children");
                    if (children != null && children.size() > 0) {
                        children.get(0).encode();
                }       break;
                    }
                default:
                    node.encode();
                    break;
            }
        }
    }
}
