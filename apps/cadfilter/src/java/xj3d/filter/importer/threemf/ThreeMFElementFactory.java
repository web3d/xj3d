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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by giles on 8/19/2015.
 */
public class ThreeMFElementFactory {
    public static final int MODEL = 0;
    public static final int METADATA = 1;
    public static final int RESOURCES = 2;
    public static final int OBJECT = 3;
    public static final int MESH = 4;
    public static final int VERTICES = 5;
    public static final int VERTEX = 6;
    public static final int TRIANGLES = 7;
    public static final int TRIANGLE = 8;
    public static final int BUILD = 9;
    public static final int ITEM = 10;

    /** Mapping from element name to ThreeMF data model */
    private static final Map<String,Integer> MAPPING;

    static {
        MAPPING = new HashMap<>();
        MAPPING.put("model",MODEL);
        MAPPING.put("metadata",METADATA);
        MAPPING.put("resources",RESOURCES);
        MAPPING.put("object",OBJECT);
        MAPPING.put("mesh",MESH);
        MAPPING.put("vertices",VERTICES);
        MAPPING.put("vertex",VERTEX);
        MAPPING.put("triangles",TRIANGLES);
        MAPPING.put("triangle",TRIANGLE);
        MAPPING.put("build",BUILD);
        MAPPING.put("item",ITEM);
    }

    /**
     * Constructor
     */
    public ThreeMFElementFactory() {
    }

    /**
     * Return the CElement per the argument name
     *
     * @param parent The parent element, used for element batching
     * @param name The tag name of the Element
     * @param atts     The Attributes of the Element
     * @return the CElement per the argument name
     */
    public static ThreeMFElement getElement(ThreeMFElement parent, String name, Attributes atts) {
        Integer elemId = MAPPING.get(name);

        if (elemId == null) return null;

        switch(elemId) {
            case MODEL:
                return new Model(atts);
            case METADATA:
                return new ModelMetaData(atts);
            case RESOURCES:
                return parent;
            case OBJECT:
                return new ObjectResource(atts);
            case MESH:
                return new Mesh(atts);
            case VERTICES:
                return new Vertices(atts);
            case TRIANGLES:
                return new Triangles(atts);
            case BUILD:
                return new Build(atts);
            case ITEM:
                return new Item(atts);
            default:
                System.out.printf("Cannot find element: %s\n",name);
                return null;
        }
    }
}
