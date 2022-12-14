/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// Standard imports

/**
 * DIS helper class for Hashmap usage.  Holds fields needed for uniqueness in
 * DIS connections, address and port
 *
 * Implements hashcode and equals.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class DISConnectionId {
    protected int port;
    protected String address;
    private int hash;

    public DISConnectionId(String address, int port) {
        setValue(address, port);
    }

    /**
     * Set the value of this id.
     * @param address
     * @param port
     */
    public final void setValue(String address, int port) {
        this.port = port;
        this.address = address;

        // Now regenerate the hash code
        long h = address.hashCode();
        h = 31 * h + port;

        hash = (int)(h & 0xFFFFFFFF);
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof DISConnectionId)) {
            return false;
        }

        DISConnectionId id = (DISConnectionId)o;

        return !(id.port != port || !id.address.equals(address));
    }

}