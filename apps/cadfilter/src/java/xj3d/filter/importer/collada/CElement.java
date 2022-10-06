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
import java.util.List;

/**
 * Defines the requirements of a CElement
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
interface CElement {

    /**
     * Return the tag name
     *
     * @return The tag name
     */
    String getTagName();

    /**
     * Return the named attribute value
     *
     * @param The attribute name
     * @return The attribute value, or null if one does not exist.
     */
    String getAttribute(String name);

    /**
     * Set the text content of the CElement
     *
     * @param content The text content of the CElement
     */
    void setTextContent(String content);

    /**
     * Return the text content of the CElement
     *
     * @return The text content of the CElement
     */
    String getTextContent();

    /**
     * Null out the content of the CElement
     */
    void clearContent();

    /**
     * Add a child element
     *
     * @param element The element to add
     */
    void addElement(CElement element);

    /**
     * Return the child elements that match the specified tag name
     *
     * @param tag_name The tag name to search for
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    List<CElement> getElementsByTagName(String tag_name);

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
    List<CElement> getElementsByTagName(String tag_name, List<CElement> children);

    /**
     * Return the first child element that matchs the specified tag name
     *
     * @param tag_name The tag name to search for
     * @return The element. If none are found, null is returned
     */
    CElement getFirstElementByTagName(String tag_name);

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
    List<CElement> getElementsByTagNameAndAttribute(
    		String tag_name, 
    		List<CElement> children,
    		String attrName,
    		String attrVal);
    	

    /**
     * Return the first child elements that match the specified tag name and attribute
     *
     * @param tag_name The tag name to search for
     * @param attrName The name of the attribute to match.
     * @param attrVal The value of the attribute to match.
     * @return An ArrayList of elements. If none are found, the list will be
     * empty
     */
    CElement getFirstElementsByTagNameAndAttribute(
    		String tag_name, 
    		String attrName,
    		String attrVal);
    
    /**
     * Return the first child element
     *
     * @return The element. If none, null is returned
     */
    CElement getFirstElement();

    /**
     * Return the child elements
     *
     * @return The child elements. If none, the list will be empty
     */
    List<CElement> getElements();
}
