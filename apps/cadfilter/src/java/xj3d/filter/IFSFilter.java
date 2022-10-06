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
package xj3d.filter;

//External imports
import java.util.*;

//Local imports
import org.web3d.vrml.sav.*;

import org.web3d.util.SimpleStack;

import org.web3d.vrml.lang.VRMLException;

/**
 * Converts a IndexedTriangleSet, IndexedTriangleFanSet, or
 * IndexedTriangleStripSet to a IndexedFaceSet.
 *
 * <p>
 *
 * This filter assumes that any set of geometry that is
 * coming in through this filter is an indexed set.  Thus,
 * if un-indexed sets are going through this filter, this filter
 * wouldn't work unless geometry sets were gone through the index
 * filter before this filter.
 *
 * @author Sang Park
 * @version $Revision: 1.14 $
 */
public class IFSFilter extends AbstractFilter {

    /** A string version of -1 so that we can append it to the various generators */
    private static final String MINUS_ONE = "-1";

    /** Indices into the NODE_* arrays, by type */
    private static final int INDEXED_TRIANGLE = 0;
    private static final int INDEXED_TRIANGLE_FAN = 1;
    private static final int INDEXED_TRIANGLE_STRIP = 2;

    private static final int TRIANGLE = 3;
    private static final int TRIANGLE_FAN = 4;
    private static final int TRIANGLE_STRIP = 5;

    /** A stack of field values */
    private SimpleStack fieldValuesStack;

    /** A stack of def names */
    private SimpleStack defStack;

    /** The set of nodes that we want to convert to an IFS */
    private Set<String> nodesToConvert;

    /** A mapping of defs to node names */
    private Map<String, String> defNodeMap;

    /** Are we inside a triangle node */
    private boolean insideTNode;

    /**
     * Create a new default filter for the conversion
     */
    public IFSFilter() {
        fieldValuesStack = new SimpleStack();
        defStack = new SimpleStack();
        defNodeMap = new HashMap<>();

        nodesToConvert = new HashSet<>();
        nodesToConvert.add("IndexedTriangleSet");
        nodesToConvert.add("IndexedTriangleStripSet");
        nodesToConvert.add("IndexedTriangleFanSet");
        nodesToConvert.add("TriangleSet");
        nodesToConvert.add("TriangleStripSet");
        nodesToConvert.add("TriangleFanSet");

        insideTNode = false;
    }

    //----------------------------------------------------------
    // ContentHandler methods
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

        defNodeMap.clear();
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

        if (nodesToConvert.contains(name)) {

            insideTNode = true;
            fieldValuesStack.push(new HashMap<String, Object>());
            defStack.push(defName);

            nodeStack.push(name);
            contentHandler.startNode("IndexedFaceSet",
                                     defName);
        } else {

            defStack.push(defName);
            nodeStack.push(name);

            if (insideTNode) {
                if (defName != null)
                    defNodeMap.put(name,defName);

                return;
            }

            super.startNode(name, defName);
        }
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
    public void endNode() throws SAVException, VRMLException {
        String nodeName = (String) nodeStack.peek();
        String defName = (String) defStack.peek();

        if (nodesToConvert.contains(nodeName)) {
            switch (nodeName) {
                case "IndexedTriangleSet":
                    parseIndexedSets(INDEXED_TRIANGLE);
                    break;
                case "IndexedTriangleStripSet":
                    parseIndexedSets(INDEXED_TRIANGLE_STRIP);
                    break;
                case "IndexedTriangleFanSet":
                    parseIndexedSets(INDEXED_TRIANGLE_FAN);
                    break;
                case "TriangleSet":
                    parseNonIndexedSets(TRIANGLE);
                    break;
                case "TriangleStripSet":
                    parseNonIndexedSets(TRIANGLE_STRIP);
                    break;
                case "TriangleFanSet":
                    parseNonIndexedSets(TRIANGLE_FAN);
                    break;
            }

            insideTNode = false;
        }

        if (insideTNode) {
            nodeStack.pop();
            defStack.pop();
        } else {
            super.endNode();
        }
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

        if (insideTNode)
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
        if (!insideTNode)
            super.useDecl(defName);
        else {

            if (!fieldValuesStack.isEmpty()) {

                String fieldName = (String)fieldStack.peek();

                @SuppressWarnings("unchecked") // cast from home rolled Map derivative
                Map<String, Object> fieldValues =
                    (Map<String, Object>)fieldValuesStack.peek();
                fieldValues.put(fieldName, "USE." + defName);
            }

            fieldStack.pop();
        }
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
        if (insideTNode)
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

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, values);
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
    public void fieldValue(String value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    //---------------------------------------------------------------
    // Methods defined by BinaryContentHandler
    //---------------------------------------------------------------

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(int[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(boolean[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(float[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(long[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param value The new value to use for the node
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double value)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(double[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param value The new value to use for the node
     * @param len The number of valid entries in the value array
     * @throws SAVException This call is taken at the wrong time in the
     *   structure of the document.
     * @throws VRMLException This call is taken at the wrong time in the
     *   structure of the document.
     */
    @Override
    public void fieldValue(String[] value, int len)
        throws SAVException, VRMLException {

        String fieldName = (String) fieldStack.peek();
        String nodeName = (String) nodeStack.peek();

        if (insideTNode) {

            @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
            Map<String, Object> fieldValues =
                (Map<String, Object>) fieldValuesStack.peek();
            fieldValues.put(nodeName + "." + fieldName, value);
        } else {
            super.fieldValue(value, len);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Parses unindexed triangle, indexed triangle fan, or indexed
     * triangle strip sets into a indexed face sets.
     *
     * @param setType Type of the indexed sets.  This value could be
     *                in between 0 to 2.
     *                0 = triangle sets
     *                1 = triangle fan sets
     *                2 = triangle strip sets
     */
    private void parseNonIndexedSets(int setType) {

        if(setType != TRIANGLE &&
           setType != TRIANGLE_FAN &&
           setType != TRIANGLE_STRIP)
            return;

        String node_prefix = null;

        switch(setType) {
            case TRIANGLE:
                node_prefix = "TriangleSet.";
                break;

            case TRIANGLE_FAN:
                node_prefix = "TriangleFanSet.";
                break;

            case TRIANGLE_STRIP:
                node_prefix = "TriangleStripSet.";
                break;
        }

        @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
        Map<String, Object> fieldValues =
            (Map<String, Object>) fieldValuesStack.pop();

        Set found_field_names = fieldValues.keySet();
        Iterator itr = found_field_names.iterator();
        while(itr.hasNext()) {
            String name = (String)itr.next();
            // Write out all the other found fields

            if((name.startsWith(node_prefix)) && !name.endsWith("index")) {
                int dot_pos = name.indexOf('.');
                String field_name = name.substring(dot_pos + 1, name.length());

                contentHandler.startField(field_name);
                // generic field value handler writes out the final value
                Object field_value = fieldValues.get(name);
                fieldHandler.setFieldValue("IndexedFaceSet",
                                            field_name,
                                            field_value);
            }
        }

        float[] coords = getFloatCoord(fieldValues);

        int num_coords  = coords.length / 3;

        int[] outputIndex = null;
        int p1, p2, p3;
        int cnt = 0;

        switch(setType) {
            case TRIANGLE:
                // force floats, just to make sure.
                outputIndex = new int[4 * num_coords];

                for(int i = 0; i < num_coords; i += 3) {

                    //System.out.println("i:" + i);

                    outputIndex[cnt] = i;
                    outputIndex[cnt + 1] = i + 1;
                    outputIndex[cnt + 2] = i + 2;
                    outputIndex[cnt + 3] = -1;
                    cnt += 4;
                }

                break;

            case TRIANGLE_FAN:
                Object fan_count = fieldValues.get("TriangleSet.fanCount");

                int total_coords;
                // total number is the fan length plus 1 for each fan for the -1 at
                // the end.
                if(fan_count != null) {
                    int[] counts = convertToIntArray(fan_count);
                    total_coords = counts.length;

                    for(int i = 0; i < counts.length; i++)
                        total_coords += counts[i];

                    outputIndex = new int[total_coords];

                    int in_cnt = 0;
                    for(int i = 0; i < counts.length; i++) {
                        for(int j = 0; j < counts[i]; j++) {
                            outputIndex[cnt + j] = in_cnt + j;
                        }

                        outputIndex[cnt + counts[i]] = -1;
                        cnt += counts[i] + 1;
                        in_cnt += counts[i];
                    }
                } else {
                    outputIndex = new int[0];
                }

                break;

            case TRIANGLE_STRIP:
                // For the strip set we have to make every triangle it's
                // own face set. IFS requires that each face is planar, which
                // we can't guaranteeconverting from a strip set. So, let's
                // look at each strip length and do that to be the number of
                // triangles.
                Object strip_count = fieldValues.get("TriangleStripSet.stripCount");

                // total number is the fan length plus 1 for each fan for the -1 at
                // the end.
                if(strip_count != null) {
                    int[] counts = convertToIntArray(strip_count);
                    total_coords = 0;

                    for(int i = 0; i < counts.length; i++)
                        total_coords += (counts[i] - 2) * 3;

                    outputIndex = new int[total_coords];

                    cnt = 0;
                    int in_cnt = 0;

                    for(int i = 0; i < counts.length; i++) {
                        int strip_len = counts[i];
                        for(int j = 0; j < strip_len - 2; j++) {
                            // deal with even/odd triangle winding
                            if(j % 2 != 0) {
                                outputIndex[cnt] = in_cnt;
                                outputIndex[cnt + 1] = in_cnt + 2;
                                outputIndex[cnt + 2] = in_cnt + 1;
                                outputIndex[cnt + 3] = -1;
                            } else {
                                outputIndex[cnt] = in_cnt;
                                outputIndex[cnt + 1] = in_cnt + 1;
                                outputIndex[cnt + 2] = in_cnt + 2;
                                outputIndex[cnt + 3] = -1;
                            }
                            in_cnt++;
                            cnt += 4;
                        }

                        in_cnt += 3;
                    }
                } else {
                    outputIndex = new int[0];
                }


                break;
        }

        writeIFS(fieldValues, outputIndex);
    }

    /**
     * Parses indexed triangle, indexed triangle fan, or indexed
     * triangle strip sets into a indexed face sets.
     *
     * @param setType Type of the indexed sets.  This value could be
     *                in between 0 to 2.
     *                0 = Indexed triangle sets
     *                1 = Indexed triangle fan sets
     *                2 = Indexed triangle strip sets
     */
    private void parseIndexedSets(int setType) {

        if(setType != INDEXED_TRIANGLE &&
           setType != INDEXED_TRIANGLE_FAN &&
           setType != INDEXED_TRIANGLE_STRIP)
            return;

        String node_prefix = null;

        switch(setType) {
            case INDEXED_TRIANGLE:
                node_prefix = "IndexedTriangleSet.";
                break;

            case INDEXED_TRIANGLE_FAN:
                node_prefix = "IndexedTriangleFanSet.";
                break;

            case INDEXED_TRIANGLE_STRIP:
                node_prefix = "IndexedTriangleStripSet.";
                break;
        }

        @SuppressWarnings("unchecked") // cast from home rolled Stack derivative
        Map<String, Object> fieldValues =
            (Map<String, Object>) fieldValuesStack.pop();

        Set found_field_names = fieldValues.keySet();
        Iterator itr = found_field_names.iterator();
        while(itr.hasNext()) {
            String name = (String)itr.next();
            // Write out all the other found fields

            if((name.startsWith(node_prefix)) && !name.endsWith("index")) {
                int dot_pos = name.indexOf('.');
                String field_name = name.substring(dot_pos + 1, name.length());

                contentHandler.startField(field_name);
                // generic field value handler writes out the final value
                Object field_value = fieldValues.get(name);
                fieldHandler.setFieldValue("IndexedFaceSet",
                                           field_name,
                                           field_value);
            }
        }

        int[] coordIndex = getIndex(fieldValues, setType);
        int len = coordIndex.length;

        int[] outputIndex = null;
        int p1, p2, p3;
        int cnt = 0;

        switch(setType) {
            case INDEXED_TRIANGLE:

                outputIndex = new int[(int)(4.0f * len / 3.0f)];

                for(int i=0; i < len; i+=3) {

                    //System.out.println("i:" + i);

                    outputIndex[cnt] = coordIndex[i + 0];
                    outputIndex[cnt + 1] = coordIndex[i + 1];
                    outputIndex[cnt + 2] = coordIndex[i + 2];
                    outputIndex[cnt + 3] = -1;
                    cnt += 4;
                }
                break;

            case INDEXED_TRIANGLE_FAN:
                outputIndex = coordIndex;

                break;

            case INDEXED_TRIANGLE_STRIP:
                // For the strip set we have to make every triangle it's
                // own face set. IFS requires that each face is planar, which
                // we can't guaranteeconverting from a strip set. So, let's
                // look at each strip length and do that to be the number of
                // triangles.
                int tri_cnt = 0;
                int pt_cnt = 0;
                ArrayList<Integer> strips = new ArrayList<>();

                for(int i = 0; i < len; i++) {
                    if(coordIndex[i] == -1) {
                        tri_cnt += pt_cnt - 2;
                        strips.add(pt_cnt);
                        pt_cnt = 0;
                    } else {
                        pt_cnt++;
                    }
                }

                // Check for last strip not ending in -1
                if(coordIndex[coordIndex.length - 1] != -1) {
                    strips.add(pt_cnt);
                    tri_cnt += pt_cnt - 2;
                }

                outputIndex = new int[tri_cnt * 4];

                cnt = 0;
                tri_cnt = 0;

        for (Integer strip : strips) {
            int strip_len = strip;
            for(int j = 0; j < strip_len - 2; j++) {
                // deal with even/odd triangle winding
                if(j % 2 != 0) {
                    outputIndex[cnt] = coordIndex[tri_cnt];
                    outputIndex[cnt + 1] = coordIndex[tri_cnt + 2];
                    outputIndex[cnt + 2] = coordIndex[tri_cnt + 1];
                    outputIndex[cnt + 3] = -1;
                } else {
                    outputIndex[cnt] = coordIndex[tri_cnt];
                    outputIndex[cnt + 1] = coordIndex[tri_cnt + 1];
                    outputIndex[cnt + 2] = coordIndex[tri_cnt + 2];
                    outputIndex[cnt + 3] = -1;
                }
                tri_cnt++;
                cnt += 4;
            }
            tri_cnt += 3;
        }

                break;
        }

        writeIFS(fieldValues, outputIndex);
    }


    /**
     * Write out the IFS now given the set of field values and an index list
     *
     * @param fieldValues The list of other fields togo look at
     * @param outputIndex The output index list to write
     */
    private void writeIFS(Map<String, Object> fieldValues, int[] outputIndex) {
        boolean hasNormals = fieldValues.containsKey("Normal.vector");
        boolean hasColors = fieldValues.containsKey("Color.color") ||
                            fieldValues.containsKey("ColorRGBA.color");
        boolean hasTexCoords = fieldValues.containsKey("TextureCoordinate.point");

        // add the index field
        contentHandler.startField("coordIndex");

        // Create generic content handler.
        fieldHandler.setFieldValue("IndexedFaceSet",
                                          "coordIndex",
                                          outputIndex,
                                          outputIndex.length);

        // add the coord node

        if(isUse(fieldValues, "coord"))  {
            contentHandler.startField("coord");
            contentHandler.useDecl(getUseName(fieldValues, "coord"));
        } else {
            String def_name = defNodeMap.get("Coordinate");
            contentHandler.startField("coord");
            contentHandler.startNode("Coordinate", def_name);
            contentHandler.startField("point");

            float[] coords = getFloatCoord(fieldValues);

            fieldHandler.setFieldValue("Coordinate",
                                              "point",
                                              coords,
                                              coords.length);

            contentHandler.endNode();
            contentHandler.endField();
        }

        // Next color

        if(isUse(fieldValues, "color")) {
            contentHandler.startField("color");
            contentHandler.useDecl(getUseName(fieldValues, "color"));
        } else if(hasColors) {

            String def_name = defNodeMap.get("Color");
            contentHandler.startField("color");
            contentHandler.startNode("Color", def_name);
            contentHandler.startField("color");

            float[] coords = getFloatColor(fieldValues);

            fieldHandler.setFieldValue("Color",
                                              "color",
                                              coords,
                                              coords.length);

            contentHandler.endNode();
            contentHandler.endField();
        }

        // Next normals

        if(isUse(fieldValues, "normal")) {
            contentHandler.startField("normal");
            contentHandler.useDecl(getUseName(fieldValues, "normal"));
        } else if(hasNormals) {
            String def_name = defNodeMap.get("Normal");
            contentHandler.startField("normal");
            contentHandler.startNode("Normal", def_name);
            contentHandler.startField("vector");

            float[] coords = getFloatNormal(fieldValues);

            fieldHandler.setFieldValue("Normal",
                                              "vector",
                                              coords,
                                              coords.length);

            contentHandler.endNode();
            contentHandler.endField();
        }

        // Finally Texture coordinate. Assumes only single coords

        if(isUse(fieldValues, "texCoord")) {
            contentHandler.startField("texCoord");
            contentHandler.useDecl(getUseName(fieldValues, "texCoord"));
        } else if(hasTexCoords) {
            String def_name = defNodeMap.get("TextureCoordinate");
            contentHandler.startField("texCoord");
            contentHandler.startNode("TextureCoordinate", def_name);
            contentHandler.startField("point");

            float[] coords = getFloatTexCoord(fieldValues);

            fieldHandler.setFieldValue("TextureCoordinate",
                                              "point",
                                              coords,
                                              coords.length);

            contentHandler.endNode();
            contentHandler.endField();
        }

        defNodeMap.clear();
    }

    /**
     * Check to see if the given field name is actually a USE definition.
     *
     * @param fieldValues map of field names to values to check
     * @param fieldName The name of the field to check
     * @return true if this field is actually a USE decl
     */
    private boolean isUse(Map<String, Object> fieldValues, String fieldName) {
        Object field = fieldValues.get(fieldName);
        if(!(field instanceof String))
            return false;

        return ((String)field).startsWith("USE.");
    }

    /**
     * Get the actual field name used for the USE
     *
     * @return The DEF name for this field name
     */
    private String getUseName(Map<String, Object> fieldValues, String fieldName) {
        String value = (String)fieldValues.get(fieldName);

        return value.substring(4);
    }

    /**
     * Returns array of triangle set index
     *
     * @param fieldValues Field hash map containing triangle indexes.
     * @return Array of index
     */
    private int[] getIndex(Map<String, Object> fieldValues, int setType) {
        int[] coordIndex = null;

        switch(setType) {
            case INDEXED_TRIANGLE:
                if (fieldValues.get("IndexedTriangleSet.index") != null) {

                    Object indexVal = fieldValues.get("IndexedTriangleSet.index");

                    coordIndex = convertToIntArray(indexVal);
                }
                break;

            case INDEXED_TRIANGLE_FAN:
                if (fieldValues.get("IndexedTriangleFanSet.index") != null) {

                    Object indexVal = fieldValues.get("IndexedTriangleFanSet.index");

                    coordIndex = convertToIntArray(indexVal);
                }
                break;

            case INDEXED_TRIANGLE_STRIP:
                if (fieldValues.get("IndexedTriangleStripSet.index") != null) {

                    Object indexVal = fieldValues.get("IndexedTriangleStripSet.index");

                    coordIndex = convertToIntArray(indexVal);
                }
        }

        return coordIndex;
    }

    /**
     * Converts field value object into a integer array
     * if passed in parameter type is String, StringBuilder,
     * or int array.
     *
     * @param indexObj Field value object
     * @return Integer array containing indexes
     */
    private int[] convertToIntArray(Object indexObj) {

        int[] coordIndex = null;

        if(indexObj instanceof String) {
            coordIndex = fieldReader.MFInt32((String)indexObj);
        } else if (indexObj instanceof String[]){
            coordIndex = fieldReader.MFInt32((String[])indexObj);
        } else if (indexObj instanceof int[]) {
            coordIndex = (int[])indexObj;
        }

        return coordIndex;
    }

    /**
     * Returns coordinate list in an array of floats
     *
     * @param fieldValues Map containing coordinate list
     * @return Coordinate list in an array of floats
     */
    private float[] getFloatCoord(Map<String, Object> fieldValues) {
        Object coords = fieldValues.get("Coordinate.point");

        return getFieldAsFloats(coords);
    }

    /**
     * Returns normal list in an array of floats
     *
     * @param fieldValues Map containing coordinate list
     * @return Coordinate list in an array of floats
     */
    private float[] getFloatNormal(Map<String, Object> fieldValues) {
        Object coords = fieldValues.get("Normal.vector");

        return getFieldAsFloats(coords);
    }

    /**
     * Returns texture coordinate list in an array of floats
     *
     * @param fieldValues Map containing coordinate list
     * @return Coordinate list in an array of floats
     */
    private float[] getFloatTexCoord(Map<String, Object> fieldValues) {
        Object coords = fieldValues.get("TextureCoordinate.point");

        return getFieldAsFloats(coords);
    }

    /**
     * Returns color list in an array of floats. Works for both Color and
     * Color RGBA.
     *
     * @param fieldValues Map containing coordinate list
     * @return Coordinate list in an array of floats
     */
    private float[] getFloatColor(Map<String, Object> fieldValues) {
        Object colors = fieldValues.get("Color.color");

        return getFieldAsFloats(colors);
    }

    /**
     * Returns color list in an array of floats. Works for both Color and
     * Color RGBA.
     *
     * @param fieldValues Map containing coordinate list
     * @return Coordinate list in an array of floats
     */
    private float[] getFloatColorRGBA(Map<String, Object> fieldValues) {
        Object colors = fieldValues.get("ColorRGBA.color");

        // Note does not remove the alpha component from the colour array if
        // we found RGBA colours. Probably should do that.
        return getFieldAsFloats(colors);
    }

    /**
     * Get the field value as a float[]. Does not handle converting the
     * field from a string to a set of floats yet. A null parameter just
     * returns null.
     *
     * @param The incoming object instance to convert if needed
     * @return The field value converted to floats
     */
    private float[] getFieldAsFloats(Object values) {
        float[] retVal = null;

        // Use MFFloat here as the most generic form of float array
        // parsing rather than getting some more specific type. We
        // just want to turn strings in to numbers, nothing more
        if(values instanceof float[])
            retVal = (float[])values;
        else if(values instanceof String)
            retVal = fieldReader.MFFloat((String)values);
        else if(values instanceof String[])
            retVal = fieldReader.MFFloat((String[])values);

        return retVal;
    }
}
