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

import java.util.ArrayList;
import java.util.List;

/**
 * Build object
 *
 * @author Alan Hudson
 */
public class Build implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.BUILD;

    private List<Item> items;

    public Build(Attributes atts) {
        items = new ArrayList<>();
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.ITEM:
                items.add((Item)el);
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public List<Item> getItems() {
        return items;
    }
}
