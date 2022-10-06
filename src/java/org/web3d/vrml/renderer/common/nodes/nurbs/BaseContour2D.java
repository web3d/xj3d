/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.nurbs;

// External imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a Contour2D node.
 *
 * @author Vincent Marchetti
 * @version $Revision: 1.17 $
 *
 */
public abstract class BaseContour2D extends AbstractNode
    implements VRMLNodeType {

    /** Index of the point field */
    protected static final int FIELD_CHILDREN = LAST_NODE_INDEX + 1;

    /**
     *
     */
    protected static final int FIELD_ADDCHILDREN = LAST_NODE_INDEX + 2;

    /**
     *
     */
    protected static final int FIELD_REMOVECHILDREN = LAST_NODE_INDEX + 3;

    /** The last field index used by this class */
    protected static final int LAST_COORDINATE_INDEX = FIELD_REMOVECHILDREN;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_COORDINATE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final Map<String,Integer> fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /* attached trimming contours will be implemented as a java ListArray */

    /**
     *
     */
    
    protected List<VRMLNodeType> vfChildren;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_CHILDREN };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap<>(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        Integer idx = FIELD_METADATA;
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                 "MFNode",
                                 "children");

        idx = FIELD_CHILDREN;
        fieldMap.put("children", idx);

    }

    /**
     *
     */
    public BaseContour2D() {
        super("Contour2D");
        hasChanged = new boolean[NUM_FIELDS];
        vfChildren = new ArrayList<>();
    }


    //----------------------------------------------------------
    // Methods defined by VRMLNodeType interface
    //----------------------------------------------------------

    @Override
    public int getFieldIndex(String fieldName) {
        Integer index = fieldMap.get(fieldName);

        return (index == null) ? -1 : index;
    }

    @Override
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    @Override
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if (index < 0  || index > LAST_COORDINATE_INDEX)
            return null;

        return fieldDecl[index];
    }

    @Override
    public int getNumFields() {
        return fieldDecl.length;
    }

    @Override
    public int getPrimaryType() {
        return TypeConstants.NodeType;
    }

    /* ToDo VJM: This needs to be implemented
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }
    */

    @Override
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {

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

    @Override
    public void setValue(int index, VRMLNodeType child)
        /* The logic of when child should be added and if/when the
        existing array of children should be cleared is based on the logic
        implemented in BaseGroupingNode
        */

        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_CHILDREN:
                if (!inSetup) vfChildren.clear();
                if (child != null) vfChildren.add(child);
                if (!inSetup){
                    hasChanged[FIELD_CHILDREN] = true;
                    fireFieldChanged(FIELD_CHILDREN);
                }
                break;

            case FIELD_ADDCHILDREN:
                if(inSetup)
                throw new InvalidFieldAccessException(
                    "Cannot set an inputOnly field in a file: addChildren");
                setValue(FIELD_CHILDREN, child);
                break;

            /* TODO VJM 17 Mar 2012: Handle REMOVECHILD and metadata */
            default:
                super.setValue(index, child);
        }
    }

    /* ToDo VJM 17 Mar 2012
    Implement setValue(int index, VRMLNodeType[] children, int numValid) case
    */

    @Override
    public void setupFinished() {

        super.setupFinished();

        Iterator<VRMLNodeType> ix = vfChildren.iterator();
        while (ix.hasNext()) ix.next().setupFinished();
    }

}
