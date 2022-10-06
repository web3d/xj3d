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
 * Wrapper for the X3D ImageTexture node.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class ImageTexture extends BaseTexture2D implements IUrl {

    /** Field value */
    public String[] url;

    /** Number of urls in the array */
    public int num_url;

    /**
     * Constructor
     */
    public ImageTexture() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param defName The node's DEF name
     */
    public ImageTexture(String defName) {
        super("ImageTexture", defName);
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
        url = null;
        num_url = 0;
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

                if (url != null) {
                    handler.startField("url");
                    switch (handlerType) {
                    case HANDLER_BINARY:
                        bch.fieldValue(url, num_url);
                        break;
                    case HANDLER_STRING:
                        // ? no length, could this cause problems ?
                        sch.fieldValue(url);
                        break;
                    }
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

        if (name.equals("url")) {
            if (value instanceof String) {
                this.url = fieldReader.MFString((String)value);
                num_url = this.url.length;
            } else if (value instanceof String[]) {
                this.url = fieldReader.MFString((String[])value);
                num_url = this.url.length;
            }
        } else {
            super.setValue(name, value);
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

        if (name.equals("url")) {
            if (value instanceof String[]) {
                this.url = (String[])value;
                num_url = len;
            }
        } else {
            super.setValue(name, value, len);
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
        ImageTexture it = new ImageTexture();
        copy(it, full);
        if (full) {
            if (url != null) {
                it.num_url = this.num_url;
                it.url = new String[this.num_url];
                System.arraycopy(this.url, 0, it.url, 0, this.num_url);
            }
        }
        return(it);
    }

    /**
     * Compares this appearance to another and checks if
     * all fields are the same and that all children fields
     * are the same.
     * @return 
     */
    @Override
    public boolean deepEquals(Encodable enc) {
        if (!(enc instanceof ImageTexture))
            return false;

        ImageTexture tex = (ImageTexture) enc;


        if (num_url != tex.num_url)
            return false;

        String[] val = tex.url;

        if (url != null) {
            if (val == null)
                return false;

            for(int i=0; i < num_url; i++) {
                if (!url[i].equals(val[i]))
                    return false;
            }
        } else if (val != null)
            return false;

        return true;
    }

}
