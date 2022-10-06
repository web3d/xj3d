/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai;

// Standard imports
import java.util.logging.Level;
import java.util.logging.Logger;

// Local imports
import vrml.eai.field.EventIn;
import vrml.eai.field.EventOut;
import vrml.eai.field.InvalidEventInException;
import vrml.eai.field.InvalidEventOutException;

/**
 * The representation of the VRML node as a java class. This is the basic node
 * class that all nodes represent.
 *
 * @version 1.1 17th June 1998
 */
public abstract class Node
{
    /**
     * Get the type of this node. The string returned should be the name of
     * the VRML node or the name of the proto instance this node represents.
     *
     * @return The type of this node.
     * @exception InvalidNodeException The node has had it's resources
     *   disposed of
     */
    public abstract String getType()
      throws InvalidNodeException;

    /**
     * Get an eventIn for this node.
     *  <p>
     * If the basic field required is an exposedField you can use either the
     * standard name (such as <i>translation</i>) or you can use the <i>set_</i>
     * modifier (such as <i>set_translation</i>).
     *
     * @param name The name of the eventIn that is required
     * @return A reference to the eventIn requested.
     * @exception InvalidEventInException The named eventIn does not exist for
     *   this node.
     * @exception InvalidNodeException The node has had it's resources disposed
     *   of
     */
    public abstract EventIn getEventIn(String name)
        throws InvalidEventInException, InvalidNodeException;

    /**
     * Get an eventOut for this node.
     *  <p>
     * If the basic field required is an exposedField you can use either the
     * standard name (such as <i>translation</i>) or you can use the
     * <i>_changed</i> modifier (such as <i>translation_changed</i>).
     *
     * @param name The name of the eventIn that is required
     * @return A reference to the eventIn requested.
     * @exception InvalidEventOutException The named eventIn does not exist for
     *   this node.
     * @exception InvalidNodeException The node has had it's resources disposed
     *   of
     */
    public abstract EventOut getEventOut(String name)
        throws InvalidEventOutException, InvalidNodeException;

    /**
     * Dispose of this node's resources. This is used to indicate to the
     * browser that the java side of the application does not require the
     * resources represented by this node. The browser is now free to do
     * what it likes with the node.
     *  <p>
     * This in no way implies that the browser is to remove this node from
     * the scene graph, only that the java applet is no longer interested
     * in this particular node through this reference.
     *  <p>
     * Once this method has been called, any further calls to methods of
     * this instance of the class is will generate an InvalidNodeException.
     *
     * @throws InvalidNodeException The node is no longer valid and can't be
     *    disposed of again.
     */
    public abstract void dispose()
      throws InvalidNodeException;

    /**
     * Clean up any resources used by this class. Explicitly calls the dispose
     * method of this class to ensure all resources as disposed of
     */
    @Override
    public void finalize()
    {
        try {
            dispose();
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}





