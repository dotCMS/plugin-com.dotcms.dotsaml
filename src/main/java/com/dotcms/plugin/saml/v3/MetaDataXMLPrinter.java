package com.dotcms.plugin.saml.v3;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

/**
 * Marshall to XML a {@link org.opensaml.saml.saml2.metadata.EntityDescriptor}
 * @author jsanca
 */
public class MetaDataXMLPrinter implements Serializable {

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    /**
     * Prints into the writer the descriptor as XL
     * @param descriptor {@link EntityDescriptor}
     * @param writer {@link Writer}
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     * @throws MarshallingException
     */
    public void print (final EntityDescriptor descriptor,
                       final Writer writer) throws ParserConfigurationException, TransformerException, IOException, MarshallingException {

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.newDocument();
        final Marshaller out = XMLObjectProviderRegistrySupport
                .getMarshallerFactory().getMarshaller(descriptor);
        final Transformer transformer = TransformerFactory
                .newInstance().newTransformer();

        out.marshall(descriptor, document);

        transformer.transform(new DOMSource(document), new StreamResult(writer));
    } // print.

} // E:O:F:MetaDataXMLPrinter.
