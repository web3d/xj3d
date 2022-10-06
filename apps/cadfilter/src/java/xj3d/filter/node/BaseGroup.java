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
import java.util.ArrayList;

// Local imports

/**
 * Base abstract impl wrapper for an X3D grouping nodes.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public abstract class BaseGroup extends BaseEncodable implements IChild, IGrouping {

    /** The root nodes */
    protected ArrayList<Encodable> children;

    /**
     * Constructor
     *
     * @param name The node name
     * @param defName The node's DEF name
     */
    protected BaseGroup(String name, String defName) {
        super(name, defName);
        children = new ArrayList<>();
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
        children.clear();
    }

    /**
     * Push the node contents to the ContentHandler
     */
    @Override
    public void encode() {

        if (handler != null) {

            super.encode();

			if (children.size() > 0) {
				handler.startField("children");

                for (Encodable e : children) {
                    e.encode();
                }

				handler.endField();
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

        if (name.equals("children")) {
            if (value instanceof IChild) {
                children.add((Encodable)value);
            } else if (value instanceof Encodable[]) {
                Encodable[] enc = (Encodable[])value;
                for (Encodable enc1 : enc) {
                    if (enc1 instanceof IChild) {
                        children.add(enc1);
                    }
                }
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

        if (name.equals("children")) {
            if (value instanceof Encodable[]) {
                Encodable[] enc = (Encodable[])value;
                for (int i = 0; i < len; i++) {
                    if (enc[i] instanceof IChild) {
                        children.add(enc[i]);
                    }
                }
            }
        } else {
            super.setValue(name, value, len);
        }
    }

    //----------------------------------------------------------
    // Methods defined by BaseEncodable
    //----------------------------------------------------------

    /**
     * Copy the working objects of this into the argument. Used
     * by subclasses to initialize a clone.
     *
     * @param enc The encodable to initialize.
     * @param deep true to initialize this nodes fields, false
     * otherwise.
     */
    @Override
    protected void copy(BaseEncodable enc, boolean deep) {
        super.copy(enc, deep);
        if (deep && (enc instanceof BaseGroup)) {
            BaseGroup that = (BaseGroup)enc;
            for (Encodable e : children) {
                that.addChild(e.clone(true));
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by IGrouping
    //----------------------------------------------------------

    /**
     * Clear and set the children of this grouping node.
     *
     * @param enc The Child nodes to set as children of this grouping node.
     * A value of null just performs a clear.
     */
    public void setChildren(Encodable[] enc) {
        children.clear();
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof IChild) {
                    children.add(enc1);
                }
            }
        }
    }

    /**
     * Return the children of this grouping node.
     *
     * @return The Child nodes of this grouping node.
     */
    public Encodable[] getChildren() {
        return(children.toArray(new Encodable[children.size()]));
    }

    /**
     * Add children to this grouping node.
     *
     * @param enc The Child nodes to add to this grouping node.
     */
    public void addChildren(Encodable[] enc) {
        if (enc != null) {
            for (Encodable enc1 : enc) {
                if (enc1 instanceof IChild) {
                    children.add(enc1);
                }
            }
        }
    }

    /**
     * Add a child to this grouping node.
     *
     * @param enc A Child node to add to this grouping node.
     */
    public void addChild(Encodable enc) {
        if ((enc != null) && (enc instanceof IChild)) {
            children.add(enc);
        }
    }
}
