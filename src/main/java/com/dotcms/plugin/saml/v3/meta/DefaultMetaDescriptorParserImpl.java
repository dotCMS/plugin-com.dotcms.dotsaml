package com.dotcms.plugin.saml.v3.meta;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.DotSamlException;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Idp Meta Descriptor parser default implementation.
 * @author jsanca
 */
public class DefaultMetaDescriptorParserImpl implements MetaDescriptorParser {

    private final ParserPool parserPool;

    private final UnmarshallerFactory unmarshallerFactory;

    public DefaultMetaDescriptorParserImpl () {

        this.parserPool =
                XMLObjectProviderRegistrySupport.getParserPool();

        this.unmarshallerFactory =
                XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    }

    @Override
    public MetadataBean parse(final InputStream inputStream) throws Exception {

        final EntityDescriptor descriptor = unmarshall(inputStream);
        final String protocol             = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_IDP_METADATA_PROTOCOL,
                        DotSamlConstants.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE);
        final IDPSSODescriptor idpDescriptor = descriptor.getIDPSSODescriptor(protocol);

        Logger.info(this, "Parsing the Id Provider, with the entityId: " +
                descriptor.getEntityID());

        return new MetadataBean(descriptor.getEntityID(),
                idpDescriptor.getErrorURL(),
                this.getSingleSignOnMap(idpDescriptor),
                this.getCredentialSigningList(descriptor.getEntityID(), idpDescriptor));
    } // parse.

    private List<Credential> getCredentialSigningList(final String entityId, final IDPSSODescriptor idpDescriptor) {

        return idpDescriptor.getKeyDescriptors().stream()
                .filter( key -> null != key.getKeyInfo() &&
                        key.getKeyInfo().getX509Datas().get(0).getX509Certificates().size() > 0 &&
                        UsageType.SIGNING == key.getUse() ) // not signing are relevant by now.
                .map   ( key ->  convertToCredential(entityId, key.getKeyInfo().getX509Datas()
                                        .get(0).getX509Certificates().get(0)) )
                .collect(Collectors.toList());
    }

    private Credential convertToCredential(final String entityId,
                                           final org.opensaml.xmlsec.signature.X509Certificate x509Certificate) {

        final byte[] decoded;
        final CertificateFactory cf;
        final java.security.cert.X509Certificate javaX509Certificate;
        ByteArrayInputStream bais = null;
        Credential credential = null;

        try {

            decoded = Base64.decode(x509Certificate.getValue());
            cf      = CertificateFactory.getInstance("X.509");
            bais    = new ByteArrayInputStream(decoded);
            javaX509Certificate =
                    java.security.cert.X509Certificate.class.cast(cf.generateCertificate(bais));

            javaX509Certificate.checkValidity();

            final BasicX509Credential signing = new BasicX509Credential(javaX509Certificate);
            signing.setEntityId(entityId);
            credential = signing;
        } catch (CertificateException e) {

            Logger.error(this, e.getMessage(), e);
            credential = null;
        } finally {

            IOUtils.closeQuietly(bais);
        }

        return credential;
    } // convertToCredential/

    private Map<String, String> getSingleSignOnMap(final IDPSSODescriptor idpDescriptor) {

        final Map<String, String> singleSignOnBindingLocationMap =
                new LinkedHashMap<>();

        idpDescriptor.getSingleSignOnServices().stream().forEach( sso -> {

            Logger.info(this,"Add SSO binding " + sso.getBinding()
                    + "(" + sso.getLocation() + ")");
            singleSignOnBindingLocationMap.put(sso.getBinding(),
                    sso.getLocation());
        } );

        return singleSignOnBindingLocationMap;
    } // getSingleSignOnMap.

    private EntityDescriptor unmarshall(final InputStream is) throws Exception {

        EntityDescriptor descriptor = null;

        try {
            // Parse metadata file
            final Element metadata = this.parserPool.parse(is).getDocumentElement();
            // Get apropriate unmarshaller
            final Unmarshaller unmarshaller = this.unmarshallerFactory.getUnmarshaller(metadata);
            // Unmarshall using the document root element, an EntitiesDescriptor in this case
            descriptor = EntityDescriptor.class.cast(unmarshaller.unmarshall(metadata));
        } catch(Exception e) {

            Logger.error(this, e.getMessage(),e);
            throw new DotSamlException(e.getMessage(), e);
        }

        return descriptor;
    } // unmarshall.
} // E:O:F:DefaultMetaDescriptorParserImpl.
