/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package vrml;

// Standard imports
// none

// Application specific imports
// None

/**
 * Java VRML97 script binding for Fields
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public abstract class Field implements Cloneable {

    /** Flag indicating that the underlying object has changed */
    protected boolean valueChanged;

    /**
     * Create a clone of this object
     * @return a clone of this object
     */
    @Override
    public abstract Object clone();
}
