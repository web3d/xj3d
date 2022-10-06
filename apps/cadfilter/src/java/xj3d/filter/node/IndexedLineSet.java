/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.node;

// External imports

// Local imports
import xj3d.filter.FieldValueHandler;

/**
 * Wrapper for an X3D IndexedLineSet node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class IndexedLineSet extends BaseGeometry {

    /** Field value */
    public boolean colorPerVertex;

    /** Field value */
    public int[] coordIndex;

    /** Number of indices in the coordIndex array */
    public int num_coordIndex;

    /** Field value */
    public int[] colorIndex;

    /** Number of indices in the colorIndex array */
    public int num_colorIndex;

    /**
     * Constructor
     */
    public IndexedLineSet() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public IndexedLineSet(String defName) {
        super("IndexedLineSet", defName);

        colorPerVertex = true;
    }

    //----------------------------------------------------------
    // Methods defined by Encodable
    //----------------------------------------------------------

    /**
     * Clear the node fields to their initial values
     */
    @Override
    public void clear() {
        super.clear();
        coordIndex = null;
        num_coordIndex = 0;
        colorIndex = null;
        num_colorIndex = 0;
        colorPerVertex = true;
    }

    /**
     * Push the node contents to the ContentHandler.
     */
    @Override
    public void encode() {

        if (handler != null) {
            if (useName == null) {
                handler.startNode(nodeName, defName);

                super.encode();

                if (coordIndex != null) {
                    handler.startField("coordIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(coordIndex, num_coordIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(coordIndex, num_coordIndex));
                        break;
                    }
                }

                if (colorIndex != null) {
                    handler.startField("colorIndex");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(colorIndex, num_colorIndex);
                        break;
                    case HANDLER_STRING:
                        sch.fieldValue(FieldValueHandler.toString(colorIndex, num_colorIndex));
                        break;
                    }
                }

                handler.startField("colorPerVertex");
                switch (handlerType) {
                case HANDLER_BINARY:
                    bch.fieldValue(colorPerVertex);
                    break;
                case HANDLER_STRING:
                    sch.fieldValue(Boolean.toString(colorPerVertex));
                    break;
                }

                handler.endNode();
            } else {
                handler.useDecl(useName);
            }
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     */
    @Override
    public void setValue(String name, Object value) {

        switch (name) {
            case "colorPerVertex":
                if (value instanceof String) {
                    colorPerVertex = Boolean.parseBoolean((String)value);
                } else if (value instanceof Boolean) {
                    colorPerVertex = ((Boolean)value);
                }
                break;
            case "coordIndex":
                if (value instanceof String) {
                    coordIndex = fieldReader.MFInt32((String)value);
                    num_coordIndex = coordIndex.length ;
                } else if (value instanceof String[]) {
                    coordIndex = fieldReader.MFInt32((String[])value);
                    num_coordIndex = coordIndex.length;
                } else if (value instanceof int[]) {
                    coordIndex = (int[])value;
                    num_coordIndex = coordIndex.length;
                }   break;
            case "colorIndex":
                if (value instanceof String) {
                    colorIndex = fieldReader.MFInt32((String)value);
                    num_colorIndex = colorIndex.length ;
                } else if (value instanceof String[]) {
                    colorIndex = fieldReader.MFInt32((String[])value);
                    num_colorIndex = colorIndex.length;
                } else if (value instanceof int[]) {
                    colorIndex = (int[])value;
                    num_colorIndex = colorIndex.length;
                }   break;
            default:
                super.setValue(name, value);
                break;
        }
    }

    /**
     * Set the value of the named field.
     *
     * @param name The name of the field to set.
     * @param value The value of the field.
     * @param len The number of values in the array.
     */
    @Override
    public void setValue(String name, Object value, int len) {

        switch (name) {
            case "coordIndex":
                if (value instanceof int[]) {
                    coordIndex = (int[])value;
                    num_coordIndex = len;
                }   break;
            case "colorIndex":
                if (value instanceof int[]) {
                    colorIndex = (int[])value;
                    num_colorIndex = len;
                }   break;
            default:
                super.setValue(name, value, len);
                break;
        }
    }

    /**
     * Create and return a copy of this object.
     *
     * @param full true if the clone should contain a copy of
     * the complete contents of this node and it's children,
     * false returns a new instance of this node type.
     * @return a copy of this.
     */
    @Override
    public Encodable clone(boolean full) {
        IndexedLineSet ils = new IndexedLineSet();
        copy(ils, full);
        if (full) {
            ils.colorPerVertex = this.colorPerVertex;
            if (this.coordIndex != null) {
                ils.coordIndex = new int[this.num_coordIndex];
                ils.num_coordIndex = this.num_coordIndex;
                System.arraycopy(this.coordIndex, 0, ils.coordIndex, 0, this.num_coordIndex);
            }
            if (this.colorIndex != null) {
                ils.colorIndex = new int[this.num_colorIndex];
                ils.num_colorIndex = this.num_colorIndex;
                System.arraycopy(this.colorIndex, 0, ils.colorIndex, 0, this.num_colorIndex);
            }
        }
        return(ils);
    }
}
