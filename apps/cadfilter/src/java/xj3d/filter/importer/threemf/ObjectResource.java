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
 * Object resource object
 *
 * @author Alan Hudson
 */
public class ObjectResource extends ModelResource implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.OBJECT;

    private long id;
    private String name;
    private String partNumber;
    private ObjectType type;
    private Mesh mesh;

    public ObjectResource(Attributes atts) {
        String st = atts.getValue("id");
        if (st != null) {
            id = Long.parseLong(st);
        }

        name = atts.getValue("name");
        partNumber = atts.getValue("partNumber");

        st = atts.getValue("type");
        if (st != null) {
            type = ObjectType.valueOf(st);
        }
    }

    @Override
    public void addElement(ThreeMFElement el) {
        if (el.getElementID() == ThreeMFElementFactory.MESH) {
            mesh = (Mesh) el;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ObjectType getType() {
        return type;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public Mesh getMesh() {
        return mesh;
    }
}
