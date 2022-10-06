/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;

/**
 * Converts a IndexedFaceSet to a TriangleSet.
 * <p>
 *
 * Right now this code assumes the IFS is all triangles.  Later versions will
 * actually triangulate the code for you.  Must have trailing -1 on coordIndex
 * Doesn't handle USE coordinates.
 *
 * NOTE: Doesn't convert if the incoming content is binary
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public class IFSToTSFilter extends AbstractFilter {

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** A stack of def names */
    private SimpleStack defStack;

    /** A list of current defnames and URL's.  Non ImageTextures will be null. */
    private Set<String> defNames;

    /** Are we inside an IndexedFaceSet */
    private boolean insideIFS;

    /**
     * Create a new default instance of this filter
     */
    public IFSToTSFilter() {
        fieldValuesStack = new SimpleStack();
        defStack = new SimpleStack();
        defNames = new HashSet<>();
        insideIFS = false;
    }

    //----------------------------------------------------------
    // Methods defined by ContentHandler
    //----------------------------------------------------------

    /**
     * Declaration of the end of the document. There will be no further parsing
     * and hence events after this.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endDocument() throws SAVException, VRMLException {
        super.endDocument();

        defNames.clear();
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
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startNode(String name, String defName)
        throws SAVException, VRMLException {
        if (defNames.contains(defName)) {
            System.out.println("Duplicate defName: " + defName);
        }

        if (name.equals("IndexedFaceSet")) {
            insideIFS = true;
            fieldValuesStack.push(new HashMap());
            if (defName != null)
                defNames.add(defName);

            defStack.push(defName);
            nodeStack.push(name);
            contentHandler.startNode("TriangleSet", defName);

            return;
        }

        if (defName != null)
            defNames.add(defName);

        defStack.push(defName);

        if (insideIFS) {
            nodeStack.push(name);
            return;
        }

        super.startNode(name, defName);
    }

    /**
     * Notification of the end of a node declaration.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    @SuppressWarnings("unchecked") // cast from home rolled Map derivative
    public void endNode() throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.peek();

        Map<String, String> fieldValues;

        if (nodeName.equals("IndexedFaceSet")) {
            insideIFS = false;
            fieldValues = (Map<String, String>) fieldValuesStack.pop();

            // Issue all other ITS fields

            int[] coordIndex = null;
            if (fieldValues.get("IndexedFaceSet.coordIndex") != null) {
                coordIndex = fieldReader.MFInt32(fieldValues.get("IndexedFaceSet.coordIndex"));
            }
            float[] coord = null;
            if (fieldValues.get("Coordinate.point") != null) {
                coord = fieldReader.MFVec3f(fieldValues.get("Coordinate.point"));
            }

            if (coord == null || coordIndex == null) {
                super.endNode();
                return;
            }

            int len = coordIndex.length;
            int idx = 0;

            int indices = (int) (len / 4f * 3);
            String coords[] = new String[indices * 3];

            if (len % 4 != 0)
                System.out.println("coordIndex not a multiple of 4");

            int cidx;

            for(int i=0; i < len; i++) {
                cidx = coordIndex[i];
                if (cidx == -1)
                    continue;

                coords[idx++] = String.valueOf(coord[cidx*3]);
                coords[idx++] = String.valueOf(coord[cidx*3+1]);
                coords[idx++] = String.valueOf(coord[cidx*3+2]);
            }

            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate",null);
            contentHandler.startField("point");

            fieldHandler.setFieldValue("Coordinate", "point", coords);

            contentHandler.endNode();
        }

        if (insideIFS)
            return;

        contentHandler.endNode();
    }

    /**
     * Notification of a field declaration. This notification is only called
     * if it is a standard node. If the node is a script or PROTO declaration
     * then the {@link ScriptHandler} or {@link ProtoHandler} methods are
     * used.
     *
     * @param name The name of the field declared
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void startField(String name) throws SAVException, VRMLException {

        if (insideIFS)
            fieldStack.push(name);
        else
            super.startField(name);
    }

    /**
     * The field value is a USE for the given node name. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     *
     * @param defName The name of the DEF string to use
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void useDecl(String defName) throws SAVException, VRMLException {
        if (insideIFS)
            fieldStack.pop();
        else
            contentHandler.useDecl(defName);
    }

    /**
     * Notification of the end of a field declaration. This is called only at
     * the end of an MFNode declaration. All other fields are terminated by
     * either {@link #useDecl(String)} or {@link #fieldValue(String)}. This
     * will only ever be called if there have been nodes declared. If no nodes
     * have been declared (ie "[]") then you will get a
     * <code>fieldValue()</code>. call with the parameter value of null.
     *
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void endField() throws SAVException, VRMLException {
        if (insideIFS)
            fieldStack.pop();
        else
            contentHandler.endField();
    }

    //-----------------------------------------------------------------------
    // Methods defined by StringContentHandler
    //-----------------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param values The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] values)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        // flatten the array
        StringBuilder value  = new StringBuilder();
        for (String value1 : values) {
            value.append(value1);
            value.append(" ");
        }

        if (insideIFS) {

            @SuppressWarnings("unchecked") // cast from home rolled Map derivative
            Map<String, StringBuilder> fieldValues = (Map<String, StringBuilder>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(values);
        }
    }

    /**
     * The value of a normal field. This is a string that represents the entire
     * value of the field. MFStrings will have to be parsed. This is a
     * terminating call for startField as well. The next call will either be
     * another <CODE>startField()</CODE> or <CODE>endNode()</CODE>.
     * <p>
     * If this field is an SFNode with a USE declaration you will have the
     * {@link #useDecl(String)} method called rather than this method. If the
     * SFNode is empty the value returned here will be "NULL".
     * <p>
     * There are times where we have an MFField that is declared in the file
     * to be empty. To signify this case, this method will be called with a
     * parameter value of null. A lot of the time this is because we can't
     * really determine if the incoming node is an MFNode or not.
     *
     * @param value The value of this field
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String value) throws SAVException, VRMLException {
        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideIFS) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, String> fieldValues = (Map<String, String>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }
}
