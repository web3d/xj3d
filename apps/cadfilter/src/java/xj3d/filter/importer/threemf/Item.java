/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

package xj3d.filter.importer.threemf;

import org.xml.sax.Attributes;

/**
 * Item object
 *
 * @author Alan Hudson
 */
public class Item implements ThreeMFElement {
    public static int ELEMENT_ID = ThreeMFElementFactory.ITEM;

    private long objectID;

    public Item(Attributes atts) {
        String oid_st = atts.getValue("objectid");
        if (oid_st != null) {
            objectID = Long.parseLong(oid_st);
        }
    }

    public long getObjectID() {
        return objectID;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }
}
