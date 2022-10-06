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
 * Created by giles on 8/19/2015.
 */
public class Vertices implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.VERTICES;
    private static final int DEFAULT_SIZE = 1000 * 3;

    private float[] verts;
    private int count;

    public Vertices(Attributes atts) {
        verts = new float[DEFAULT_SIZE];
    }

    public void addVertex(Attributes atts) {
        String x_st = atts.getValue("x");
        String y_st = atts.getValue("y");
        String z_st = atts.getValue("z");

        float x = Float.parseFloat(x_st);
        float y = Float.parseFloat(y_st);
        float z = Float.parseFloat(z_st);

        addVertex(x,y,z);
    }

    public void addVertex(float x, float y, float z) {
        if (count + 3 >= verts.length) {
            resizeVerts();
        }

        verts[count++] = x;
        verts[count++] = y;
        verts[count++] = z;
    }

    private void resizeVerts() {
        float[] nverts = new float[verts.length * 2];
        System.arraycopy(verts,0,nverts,0,verts.length);

        verts = nverts;
    }

    public float[] getVerts() {
        return verts;
    }

    public int getCount() {
        return count / 3;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }
}
