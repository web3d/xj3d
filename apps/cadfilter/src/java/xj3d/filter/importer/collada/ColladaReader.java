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
import org.web3d.util.SimpleStack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.XMLReaderAdapter;

// Local imports
import org.web3d.vrml.sav.ImportFileFormatException;

/**
 * Implementation of an XMLReader for extracting the content from
 * a Collada file.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class ColladaReader extends XMLReaderAdapter {
	
	/** Aggregator for the text content of an Element */
	private StringBuilder text;
	
	/** Stack of Elements being processed */
	private SimpleStack stack;
	
	/** Factory for the data binding classes */
	private CElementFactory cef;
	
	/** The root element of the document */
	private CElement root;

	boolean isAppendable;
	static final int LOW_WATER = 0x4100;
	static final int CAPACITY = 0x5000;
	
	/**
	 * Constructor
     * @throws org.xml.sax.SAXException
	 */
	public ColladaReader() throws SAXException {
	}
	
	/**
	 * Return the results of the parse
	 *
	 * @return The results of the parse
	 */
	public CElement getResult() {
		return(root);
	}
	
	/** 
	 * @see org.xml.sax.helpers.XMLReaderAdapter#startDocument 
	 */
    @Override
	public void startDocument() {
		text = new StringBuilder(CAPACITY);
        stack = new SimpleStack();
		cef = new CElementFactory();
	}
	
	/** 
	 * @see org.xml.sax.helpers.XMLReaderAdapter#startElement
	 */
    @Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		text.setLength(0);
		CElement ce = cef.getCElement(qName, atts);
		if (!stack.isEmpty()) {
			if (ce != null) {
				CElement parent = (CElement)stack.peek();
				if (parent != null) {
					parent.addElement(ce);
				}
			} else {
				if (ColladaParserConstants.DEBUG) {
					System.out.println(ColladaParserConstants.LOG_NAME +
						": Unsupported Element: <"+ qName +">" );
				}
			}
		} else {
			if ((ce != null) && (ce.getTagName().equals(ColladaStrings.COLLADA))) {
				root = ce;
			} else {
				throw new ImportFileFormatException(
					ColladaParserConstants.LOG_NAME +
					": Not a Collada file. Document Element: "+ qName);
			}
		}
		isAppendable = (ce instanceof Appendable);
		stack.push(ce);
	}

	/** 
	 * @see org.xml.sax.helpers.XMLReaderAdapter#characters 
	 */
    @Override
	public void characters(char[] ch, int start, int length) {
		if (isAppendable) {
			int current_length = text.length();
			if ((current_length + length) > LOW_WATER) {
				int remainder = 0;
				for (int i = current_length - 1; i >= 0; i--) {
					if (Character.isWhitespace(text.charAt(i))) {
						break;
					} else {
						remainder++;
					}
				}
				int transfer_length = current_length - remainder;
				Appendable ce = (Appendable)stack.peek();
				ce.appendTextContent(text.substring(0, transfer_length));
				text.delete(0, transfer_length);
			}
			text.append(ch, start, length);
		} else {
			text.append(ch, start, length);
		}
	}
	
	/** 
	 * @see org.xml.sax.helpers.XMLReaderAdapter#endElement
	 */
    @Override
	public void endElement(String uri, String localName, String qName) {
		CElement ce = (CElement)stack.pop();
		if (ce != null) {
			ce.setTextContent(text.toString());
		}
		text.setLength(0);
		if (!stack.isEmpty()) {
			isAppendable = (stack.peek() instanceof Appendable);
		} else {
			isAppendable = false;
		}
	}
	
	/** 
	 * @see org.xml.sax.helpers.XMLReaderAdapter#endDocument
	 */
    @Override
	public void endDocument() {
		text = null;
        stack = null;
		cef = null;
	}
}
