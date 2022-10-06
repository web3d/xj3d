/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2009
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer.collada;

// External imports
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * Base implementation of a CElement
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
class CElementBase implements CElement {

    /**
     * The element tag name
     */
    protected final String tag_name;

    /**
     * The text content
     */
    protected String content;

    /**
     * The element content
     */
    protected List<CElement> elements;

    /**
     * The attributes
     */
    protected String[][] attr;

    /**
     * Constructor
     */
    CElementBase(String tag_name, Attributes atts) {
        this.tag_name = tag_name;
        int num_attr = atts.getLength();
        attr = new String[num_attr][];
        for (int i = 0; i < num_attr; i++) {
            attr[i] = new String[]{atts.getQName(i), atts.getValue(i)};
        }
        elements = new ArrayList<>();
    }

    /**
     * Return the tag name
     *
     * @return The tag name
     */
    @Override
    public String getTagName() {
        return (tag_name);
    }

    /**
     * Return the named attribute value
     *
     * @param The attribute name
     * @return The attribute value, or null if one does not exist.
     */
    @Override
    public String getAttribute(String name) {
        String value = null;
        for (String[] attr1 : attr) {
            if (attr1[0].equals(name)) {
                value = attr1[1];
                break;
            }
        }
        return (value);
    }

    /**
     * Set the text content of the CElement
     *
     * @param content The text content of the CElement
     */
    @Override
    public void setTextContent(String content) {
        this.content = content;
    }

    /**
     * Return the text content of the CElement
     *
     * @return The text content of the CElement
     */
    @Override
    public String getTextContent() {
        return (content);
    }

    /**
     * Null out the content of the CElement
     */
    @Override
    public void clearContent() {
        content = null;
        int num = elements.size();
        for (int i = 0; i < num; i++) {
            elements.get(i).clearContent();
        }
    }

    /**
     * Add a child element
     *
     * @param element The element to add
     */
    @Override
    public void addElement(CElement element) {
        elements.add(element);
    }

    /**
     * Return the child elements that match the specified tag name
     *
     * @param tag_name The tag name to search for
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    @Override
    public List<CElement> getElementsByTagName(String tag_name) {
        return (getElementsByTagName(tag_name, null));
    }

    /**
     * Return the child elements that match the specified tag name
     *
     * @param tag_name The tag name to search for
     * @param children An ArrayList to initialize with the children. If null a
     * new ArrayList will be created and returned. The ArrayList will be cleared
     * before initialization.
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    @Override
    public List<CElement> getElementsByTagName(String tag_name, List<CElement> children) {
        if (children == null) {
            children = new ArrayList<>();
        } else {
            children.clear();
        }
        for (CElement element : elements) {
            if (element.getTagName().equals(tag_name)) {
                children.add(element);
            }
        }
        return (children);
    }

    /**
     * Return the first child element that matches the specified tag name
     *
     * @param tag_name The tag name to search for
     * @return The element. If none are found, null is returned
     */
    @Override
    public CElement getFirstElementByTagName(String tag_name) {
        CElement child = null;
        for (CElement element : elements) {
            if (element.getTagName().equals(tag_name)) {
                child = element;
                break;
            }
        }
        return (child);
    }
    
    /**
     * Return the child elements that match the specified tag name and attribute
     *
     * @param tag_name The tag name to search for
     * @param children An ArrayList to initialize with the children. If null a
     * new ArrayList will be created and returned. The ArrayList will be cleared
     * before initialization.
     * @param attrName The name of the attribute to match.
     * @param attrVal The value of the attribute to match.
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    @Override
    public List<CElement> getElementsByTagNameAndAttribute(
    		String tag_name, 
    		List<CElement> children,
    		String attrName,
    		String attrVal) {
    	
        if (children == null) {
            children = new ArrayList<>();
        } else {
            children.clear();
        }
        for (CElement element : elements) {
            if (element.getTagName().equals(tag_name)) {
                String val = element.getAttribute(attrName);
                if (val != null && val.equals(attrVal)) {
                    children.add(element);
                }
            }
        }
        return (children);
    }
    
    /**
     * Return the first child elements that match the specified tag name and attribute
     *
     * @param tag_name The tag name to search for
     * @param attrName The name of the attribute to match.
     * @param attrVal The value of the attribute to match.
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    @Override
    public CElement getFirstElementsByTagNameAndAttribute(
    		String tag_name, 
    		String attrName,
    		String attrVal) {
    	
    	CElement child = null;
        for (CElement element : elements) {
            if (element.getTagName().equals(tag_name)) {
                String val = element.getAttribute(attrName);
                if (val != null && val.equals(attrVal)) {
                    child = element;
                    break;
                }
            }
        }
        return (child);
    }

    /**
     * Return the first child element
     *
     * @return The element. If none, null is returned
     */
    @Override
    public CElement getFirstElement() {
        CElement child = null;
        if (!elements.isEmpty()) {
            child = elements.get(0);
        }
        return (child);
    }

    /**
     * Return the child elements
     *
     * @return The child elements. If none, the list will be empty
     */
    @Override
    public List<CElement> getElements() {
        return (elements);
    }
}
