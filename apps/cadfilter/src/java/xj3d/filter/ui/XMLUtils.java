/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.ui;

// External imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

/**
 * Utility methods for handling XML files
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class XMLUtils {

    /**
     * Create and return a new xml DOM object parsed from the argument
     * <code>URL</code>
     *
     * @param url The document <code>URL</code>
     * @param nsaware Enable or disable namespace awareness
     * @param schema The schema to use if validating while parsing
     * @return A new xml DOM object parsed from the argument <code>URL</code>,
     * or <code>null</code> if one cannot be created.
     */
    public static Document getDocument(URL url, boolean nsaware, Schema schema) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(nsaware);
            if (schema != null) {
                factory.setSchema(schema);
                factory.setValidating(true);
            }
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(url.openStream());
        } // unable to get a document builder factory
        catch (FactoryConfigurationError | ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace(System.err);
        }
        return (document);
    }

    /**
     * Create and return a new xml DOM object parsed from the argument
     * <code>File</code>
     *
     * @param file The document <code>File</code>
     * @param nsaware Enable or disable namespace awareness
     * @param schema The schema to use if validating while parsing
     * @return A new xml DOM object parsed from the argument <code>File</code>,
     * or <code>null</code> if one cannot be created.
     */
    public static Document getDocument(File file, boolean nsaware, Schema schema) {
        Document document = null;
        try {
            URL url = file.toURI().toURL();
            document = getDocument(url, nsaware, schema);
        } catch (MalformedURLException murle) {
            System.err.println(murle.getMessage());
        }
        return (document);
    }

    /**
     * Create and return a new empty xml DOM object
     *
     * @return a new empty xml DOM object, or <code>null</code> if one cannot be
     * created.
     */
    public static Document createNewDocument() {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        } // unable to get a document builder factory
        catch (FactoryConfigurationError | ParserConfigurationException e) {
            e.printStackTrace(System.err);
        }
        return (document);
    }

    /**
     * Write out the argument document to the specified URI
     *
     * @param doc The document to write.
     * @param file The file to write it to.
     */
    public static void putDocument(Document doc, File file) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            BufferedWriter writer =
                    new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8"));
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

        } catch (TransformerException | IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Print out a document node to the specified
     * <code>PrintStream</code>.
     *
     * @param node the document node.
     * @param stream the output <code>PrintStream</code>
     */
    public static void printNode(Node node, PrintStream stream) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);

        } catch (TransformerException te) {
            te.printStackTrace(System.err);
        }
    }
}
