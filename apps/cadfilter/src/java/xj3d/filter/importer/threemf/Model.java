/*****************************************************************************
 *                        Shapeways Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package xj3d.filter.importer.threemf;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model object
 *
 * @author Alan Hudson
 */
public class Model implements ThreeMFElement {
    public static int ELEMENT_ID = ThreeMFElementFactory.MODEL;

    private Map<Long,ModelResource> resourceMap;
    private List<ModelResource> resources;
    private Build build;
    private Unit unit;
    private String lang;
    private ModelThumbnail globalThumbnail;
    private List<ModelThumbnail> thumbnails;
    private List<ModelMetaData> metaData;
    private Map<String, ModelMetaData> metaDataMap;

    public Model(Attributes atts) {
        String unit_st = atts.getValue("unit");
        if (unit_st == null) unit_st = "millimeter";

        unit = Unit.valueOf(unit_st);
        lang = atts.getValue("unit");

        resourceMap = new HashMap<>();
        resources = new ArrayList<>();

        metaData = new ArrayList<>();
        metaDataMap = new HashMap<>();
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void addMetaData(String name, String val) {
        ModelMetaData md = new ModelMetaData(name,val);
        metaData.add(md);
        metaDataMap.put(name,md);
    }

    public ModelMetaData getMetaData(int idx) {
        return metaData.get(idx);
    }

    public void removeMetaData(int idx) {
        metaData.remove(idx);
    }

    public boolean hasMetaData(String name) {
        return metaDataMap.containsKey(name);
    }

    public Long generateResourceID() {
        // TODO: lib3mf code searched for an open value here, not sure that's necessary
        return (long) resourceMap.size() + 1;
    }

    public Build getBuild() {
        return build;
    }

    public ModelResource getResource(Long id) {
        return resourceMap.get(id);
    }

    @Override
    public void addElement(ThreeMFElement el) {
        switch(el.getElementID()) {
            case ModelMetaData.ELEMENT_ID:
                ModelMetaData md = (ModelMetaData) el;
                addMetaData(md.getName(),md.getValue());
                break;
            case Build.ELEMENT_ID:
                build = (Build) el;
                break;
            case ObjectResource.ELEMENT_ID:
                ObjectResource or = (ObjectResource)el;
                resources.add(or);
                resourceMap.put(or.getID(),or);

        }
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }
}
