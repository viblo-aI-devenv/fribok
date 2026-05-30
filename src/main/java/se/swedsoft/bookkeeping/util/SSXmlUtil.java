package se.swedsoft.bookkeeping.util;


import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Small XML helper for standard JDK DOM parsing and writing.
 */
public final class SSXmlUtil {

    private SSXmlUtil() {}

    /**
     * Creates an empty DOM document.
     *
     * @return an empty document
     * @throws ParserConfigurationException if the JDK XML parser cannot be configured
     */
    public static Document newDocument() throws ParserConfigurationException {
        return newDocumentBuilder().newDocument();
    }

    /**
     * Parses a DOM document from an input stream.
     *
     * @param inputStream the input stream
     * @return parsed document
     * @throws ParserConfigurationException if the JDK XML parser cannot be configured
     * @throws IOException if reading fails
     * @throws SAXException if parsing fails
     */
    public static Document parse(InputStream inputStream)
            throws ParserConfigurationException, IOException, SAXException {
        return newDocumentBuilder().parse(new InputSource(inputStream));
    }

    /**
     * Writes a DOM document to a file as UTF-8 XML.
     *
     * @param document the document
     * @param file the destination file
     * @throws IOException if writing fails
     * @throws TransformerException if XML serialization fails
     */
    public static void write(Document document, File file) throws IOException, TransformerException {
        try (OutputStream outputStream = java.nio.file.Files.newOutputStream(file.toPath())) {
            write(document, outputStream);
        }
    }

    /**
     * Writes a DOM document to an output stream as UTF-8 XML.
     *
     * @param document the document
     * @param outputStream the destination stream
     * @throws TransformerException if XML serialization fails
     */
    public static void write(Document document, OutputStream outputStream) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));
    }

    private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
    }
}
