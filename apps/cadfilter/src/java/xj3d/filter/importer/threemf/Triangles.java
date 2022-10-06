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
public class Triangles implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.TRIANGLES;
    private static final int DEFAULT_SIZE = 1000 * 3;

    private int[] tris;
    private int count;

    public Triangles(Attributes atts) {
        tris = new int[DEFAULT_SIZE];
    }

    public void addTriangle(Attributes atts) {
        String v1_st = atts.getValue("v1");
        String v2_st = atts.getValue("v2");
        String v3_st = atts.getValue("v3");

        int v1 = Integer.parseInt(v1_st);
        int v2 = Integer.parseInt(v2_st);
        int v3 = Integer.parseInt(v3_st);

        addTriangle(v1,v2,v3);
    }

    public void addTriangle(int v1,int v2, int v3) {
        if (count + 3 >= tris.length) {
            resizeTris();
        }

        tris[count++] = v1;
        tris[count++] = v2;
        tris[count++] = v3;
    }

    public int[] getTris() {
        return tris;
    }

    public int getCount() {
        return count / 3;
    }

    private void resizeTris() {
        int[] ntris = new int[tris.length * 2];
        System.arraycopy(tris,0,ntris,0,tris.length);

        tris = ntris;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }
}
