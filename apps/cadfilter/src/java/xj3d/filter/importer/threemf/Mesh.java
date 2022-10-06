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
 * Mesh object
 *
 * @author Alan Hudson
 */
public class Mesh implements ThreeMFElement {
    public static int ELEMENT_ID = ThreeMFElementFactory.MESH;

    private Vertices verts;
    private Triangles tris;

    public Mesh(Attributes atts) {
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ThreeMFElementFactory.VERTICES:
                verts = (Vertices) el;
                break;
            case ThreeMFElementFactory.TRIANGLES:
                tris = (Triangles)el;
                break;
        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public Vertices getVertices() {
        return verts;
    }

    public Triangles getTriangles() {
        return tris;
    }
}
