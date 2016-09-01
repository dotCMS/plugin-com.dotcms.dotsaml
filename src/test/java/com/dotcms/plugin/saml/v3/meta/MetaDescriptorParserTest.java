package com.dotcms.plugin.saml.v3.meta;

import com.dotcms.plugin.saml.v3.BindingType;
import com.dotcms.plugin.saml.v3.InputStreamUtils;
import com.dotcms.plugin.saml.v3.init.DefaultInitializer;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Test for {@link MetaDescriptorService}
 * @author jsanca
 */
public class MetaDescriptorParserTest {

    static {
        try {
            new DefaultInitializer().init(null);
            //InitializationService.initialize();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    @Test
    public void parserTest () throws DotDataException, DotSecurityException, InitializationException {


        final MetaDescriptorService parser = new DefaultMetaDescriptorServiceImpl();

        try (InputStream inputStream = InputStreamUtils.getInputStream("idp-metadata.xml")) {

            final MetadataBean metadataBean =
                    parser.parse(inputStream);

            assertNotNull(metadataBean);
            assertNotNull(metadataBean.getEntityId());
            assertTrue("https://localhost.localdomain/idp/shibboleth".equals(metadataBean.getEntityId()));
            assertNotNull (metadataBean.getCredentialSigningList());
            assertTrue (metadataBean.getCredentialSigningList().size() == 2);
            assertNotNull (metadataBean.getSingleSignOnBindingLocationMap());
            assertNotNull (metadataBean.getSingleSignOnBindingLocationMap().size() == 3);
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.REDIRECT.getBinding()).equals("https://localhost.localdomain/idp/profile/SAML2/Redirect/SSO"));
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.AUTHN_REQUEST.getBinding()).equals("https://localhost.localdomain/idp/profile/Shibboleth/SSO"));
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.POST.getBinding()).equals("https://localhost.localdomain/idp/profile/SAML2/POST/SSO"));
        } catch (Exception e) {

            fail ("Error on open the idp-metadata: " + e.getMessage());
        }
    }

    @Test
    public void getServiceProviderEntityDescriptorTest () throws Exception {

        final MetaDescriptorService service = new DefaultMetaDescriptorServiceImpl();

        final EntityDescriptor descriptor = service.getServiceProviderEntityDescriptor();
        assertNotNull(descriptor);

        DocumentBuilder builder;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(descriptor);
        out.marshall(descriptor, document);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        DOMSource source = new DOMSource(document);
        transformer.transform(source, streamResult);
        stringWriter.close();
        String metadataXML = stringWriter.toString();

        System.out.println(metadataXML);

    }
}
