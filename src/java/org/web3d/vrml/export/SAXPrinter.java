/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export;

import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.fastinfoset.sax.AttributesHolder;

import org.web3d.x3d.jaxp.X3DConstants;
import org.web3d.vrml.lang.UnsupportedSpecVersionException;

import org.web3d.parser.x3d.X3DBinaryConstants;

/**
 * Converts a SAX stream of events to an X3D textual representation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class SAXPrinter extends DefaultHandler {

    /** The stream to write to */
    private Writer out;

    /** A temporary buffer for character data */
    private StringBuffer textBuffer;

    /** The major version of the spec this file belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this file belongs to. */
    protected int minorVersion;

    /** Should we print the DOC type */
    private boolean printDocType;

    /** Should we print the XML Element */
    protected boolean printXML;

    public SAXPrinter(Writer out, int majorVersion, int minorVersion,
        boolean printDocType) {

        this(out, majorVersion, minorVersion, printDocType, true);
    }

    public SAXPrinter(Writer out, int majorVersion, int minorVersion,
        boolean printDocType, boolean printXML) {

        this.out = out;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.printDocType = printDocType;
        this.printXML = printXML;
    }

    //----------------------------------------------------------
    // SAXDocumentHandler methods
    //----------------------------------------------------------
    
    @Override
    public void startDocument() throws SAXException {
        if (printXML) {
            print("<?xml version='1.0' encoding='UTF-8'?>");
            printNewLine();
        }

        if (printDocType) {
            String publicId = getPublicId(majorVersion,minorVersion);
            String systemId = getSystemId(majorVersion,minorVersion);

            print("<!DOCTYPE X3D PUBLIC \"");
            print(publicId);
            print("\" \"");
            print(systemId);
            print("\">\n");
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            printNewLine();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("SAX error", e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        String s = new String(ch, start, length);

        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName,
        String qName, Attributes attributes) throws SAXException {

        flushText();

        String name;

        if (localName.equals("")) {
            name = qName;
        } else {
            name = localName;
        }

        print("<" + name);

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String aName = attributes.getLocalName(i);

                if ("".equals(aName)) {
                    aName = attributes.getQName(i);
                }

                String value = attributes.getValue(i);


                if (value == null) {
                    AttributesHolder atts = (AttributesHolder) attributes;
                    Object type = atts.getAlgorithmData(i);
                    String strValue = "unknown";

                    switch(atts.getAlgorithmIndex(i)) {
                        case X3DBinaryConstants.BYTE_ALGORITHM_ID:
                            byte[] bval = (byte[]) atts.getAlgorithmData(i);
                            break;
                        case X3DBinaryConstants.DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID:
                            int[] i4val = (int[]) atts.getAlgorithmData(i);

                            break;
                        case X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID:
                        case X3DBinaryConstants.QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID2:
                            float[] f2val = (float[]) atts.getAlgorithmData(i);
                            break;
                        default:
                            System.out.println("Unhandled algorithm in SAXPrinter: " + atts.getAlgorithmIndex(i));
                    }
                    print(" " + aName + "='");
                    print(strValue);
                    print("'");

                } else {
                    print(" " + aName + "='" );
                    print(value);
                    print("'");
                }
            }

        }
        print(">");
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName )
        throws SAXException {

        flushText();

        String name = localName;

        if ("".equals(name)) {
            name = qName;
        }

        print("</" + name + ">");
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Display a string.
     */
    private void print(String s) throws SAXException {
        try {
            out.write(s);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Display a newLine.
     */
    private void printNewLine() throws SAXException {
        try {
            out.write(System.getProperty("line.separator"));
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    /**
     * Flush the text accumulated in the character buffer.
     */
    private void flushText() throws SAXException {
        if (textBuffer == null) {
            return;
        }

        print(textBuffer.toString());
        textBuffer = null;
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    protected String getPublicId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_PUBLIC_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_PUBLIC_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_PUBLIC_ID_3_2;
            case 3:
                return X3DConstants.GENERAL_PUBLIC_ID_3_3;
            case 4:
                return X3DConstants.GENERAL_PUBLIC_ID_4_0;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }

    /**
     * Get the publicId for this spec version
     *
     * @param major The major version
     * @param minor The minor version
     * @return The speced public id
     */
    protected String getSystemId(int major, int minor) {
        switch(minor) {
            case 0:
                return X3DConstants.GENERAL_SYSTEM_ID_3_0;
            case 1:
                return X3DConstants.GENERAL_SYSTEM_ID_3_1;
            case 2:
                return X3DConstants.GENERAL_SYSTEM_ID_3_2;
            case 3:
                return X3DConstants.GENERAL_SYSTEM_ID_3_3;
            case 4:
                return X3DConstants.GENERAL_SYSTEM_ID_4_0;
            default:
                throw new UnsupportedSpecVersionException("Unhandled minor version: " + minor);
        }
    }
}
