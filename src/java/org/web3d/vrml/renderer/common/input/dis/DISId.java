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
 * DIS packets, SiteID, ApplicationID, and EntityID.
 *
 * Implements hashcode and equals.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class DISId implements Cloneable {
    protected int siteID;
    protected int applicationID;
    protected int entityID;
    private int hash;

    public DISId(int siteID, int applicationID, int entityID) {
        this.siteID = siteID;
        this.applicationID = applicationID;
        this.entityID = entityID;

        // Now regenerate the hash code
        long h = 0;

        h = 31 * h + siteID;
        h = 31 * h + applicationID;
        h = 31 * h + entityID;

        hash = (int)(h & 0xFFFFFFFF);
    }

    /**
     * Set the value of this id.
     * @param siteID
     * @param applicationID
     * @param entityID
     */
    public void setValue(int siteID, int applicationID, int entityID) {
        this.siteID = siteID;
        this.applicationID = applicationID;
        this.entityID = entityID;

        // Now regenerate the hash code
        long h = 0;

        h = 31 * h + siteID;
        h = 31 * h + applicationID;
        h = 31 * h + entityID;

        hash = (int)(h & 0xFFFFFFFF);
    }

    /**
     * Calculate the hashcode for this object.
     *
     */
    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof DISId)) {
            return false;
        }

        DISId id = (DISId)o;

        return !(id.siteID != siteID || id.applicationID != applicationID ||
                id.entityID != entityID);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("DISId(");
        buff.append(hash);
        buff.append(") siteID: ");
        buff.append(siteID);
        buff.append(" appID: ");
        buff.append(applicationID);
        buff.append(" entityID: ");
        buff.append(entityID);

        return buff.toString();
    }

    @Override
    public Object clone() {
        return new DISId(siteID, applicationID, entityID);
    }
}