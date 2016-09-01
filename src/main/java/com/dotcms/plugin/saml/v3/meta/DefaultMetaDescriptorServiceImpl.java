package com.dotcms.plugin.saml.v3.meta;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.DotSamlException;
import com.dotcms.plugin.saml.v3.SamlUtils;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.*;
import org.opensaml.saml.saml2.metadata.impl.EncryptionMethodBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xml.util.Base64;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
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
 * Idp Meta Descriptor service default implementation.
 * @author jsanca
 */
public class DefaultMetaDescriptorServiceImpl implements MetaDescriptorService {

    private final ParserPool parserPool;

    private final UnmarshallerFactory unmarshallerFactory;

    private final XMLObjectBuilderFactory xmlObjectBuilderFactory;

    public DefaultMetaDescriptorServiceImpl() {

        this.parserPool =
                XMLObjectProviderRegistrySupport.getParserPool();

        this.unmarshallerFactory =
                XMLObjectProviderRegistrySupport.getUnmarshallerFactory();

        this.xmlObjectBuilderFactory =
                XMLObjectProviderRegistrySupport.getBuilderFactory();
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

    @Override
    public EntityDescriptor getServiceProviderEntityDescriptor() {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final SAMLObjectBuilder<EntityDescriptor>  entityDescriptorBuilder =
                (SAMLObjectBuilder<EntityDescriptor>) this.xmlObjectBuilderFactory.getBuilder
                        (EntityDescriptor.DEFAULT_ELEMENT_NAME);
        final SAMLObjectBuilder<SPSSODescriptor> spssoDescriptorBuilder =
                (SAMLObjectBuilder<SPSSODescriptor>) this.xmlObjectBuilderFactory.getBuilder
                        (SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        final SAMLObjectBuilder<AssertionConsumerService> assertionConsumerServiceBuilder =
                (SAMLObjectBuilder<AssertionConsumerService>) this.xmlObjectBuilderFactory.getBuilder
                        (AssertionConsumerService.DEFAULT_ELEMENT_NAME);

        final EntityDescriptor descriptor      = entityDescriptorBuilder.buildObject();
        final SPSSODescriptor  spssoDescriptor = spssoDescriptorBuilder.buildObject();

        descriptor.setEntityID(SamlUtils.getSPIssuerValue());

        Logger.info(this, "Generating the Entity Provider Descriptor for: " +
                descriptor.getEntityID());

        spssoDescriptor.setWantAssertionsSigned(configuration.getBooleanProperty
                (DotSamlConstants.DOTCMS_SAML_WANT_ASSERTIONS_SIGNED, true));
        spssoDescriptor.setAuthnRequestsSigned(configuration.getBooleanProperty
                (DotSamlConstants.DOTCMS_SAML_AUTHN_REQUESTS_SIGNED, true));
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20_NS);

        Logger.info(this, "Setting the key descriptors for: " +
                descriptor.getEntityID());
        this.setKeyDescriptors (spssoDescriptor);
        this.setFormat(configuration, spssoDescriptor);

        spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(0, SAMLConstants.SAML2_ARTIFACT_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));

        spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(1, SAMLConstants.SAML2_POST_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));

        spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(2, SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));

        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        descriptor.getRoleDescriptors().add(spssoDescriptor);

        return descriptor;
    } // getServiceProviderEntityDescriptor.

    protected AssertionConsumerService createAssertionConsumerService(final int    index,
                                                                      final String binding,
                                                                      final String location,
                                                                      final SAMLObjectBuilder<AssertionConsumerService> assertionConsumerServiceBuilder) {

        final AssertionConsumerService assertionConsumerServiceArtifact =
                assertionConsumerServiceBuilder.buildObject();
        assertionConsumerServiceArtifact.setIndex(index);
        assertionConsumerServiceArtifact.setBinding(binding);
        assertionConsumerServiceArtifact.setLocation(location);

        return assertionConsumerServiceArtifact;
    } // createAssertionConsumerService.



    protected void setFormat(final Configuration configuration,
                             final SPSSODescriptor spssoDescriptor) {

        final SAMLObjectBuilder<NameIDFormat> nameIDFormatBuilder =
                (SAMLObjectBuilder<NameIDFormat>) this.xmlObjectBuilderFactory.getBuilder
                        (NameIDFormat.DEFAULT_ELEMENT_NAME);

        final String [] formats = configuration.getStringArray (DotSamlConstants.DOTCMS_SAML_NAME_ID_FORMATS,
                new String[] { NameIDType.TRANSIENT, NameIDType.PERSISTENT });

        for (String format : formats) {

            spssoDescriptor.getNameIDFormats().add
                    (this.createFormat(format, nameIDFormatBuilder));
        }
    } // setFormat.

    protected NameIDFormat createFormat(final String format, final SAMLObjectBuilder<NameIDFormat> nameIDFormatBuilder) {

        NameIDFormat nameIDFormat = nameIDFormatBuilder.buildObject();
        nameIDFormat.setFormat(format);
        return nameIDFormat;
    }

    protected Credential getCredential () {

        return SamlUtils.getCredential();
    } // getCredential.

    protected void setKeyDescriptors(final SPSSODescriptor spssoDescriptor) {

        final SAMLObjectBuilder<KeyDescriptor> keyDescriptorBuilder =
                (SAMLObjectBuilder<KeyDescriptor>) this.xmlObjectBuilderFactory.
                        getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        final KeyDescriptor signKeyDescriptor;
        final KeyDescriptor encryptedKeyDescriptor;
        final Credential credential = getCredential();
        final EncryptionMethodBuilder encryptionMethodBuilder = new EncryptionMethodBuilder();
        final EncryptionMethod encryptionMethod;

        try {

            signKeyDescriptor = keyDescriptorBuilder.buildObject();
            encryptedKeyDescriptor = keyDescriptorBuilder.buildObject();

            signKeyDescriptor.setUse(UsageType.SIGNING);
            encryptedKeyDescriptor.setUse(UsageType.ENCRYPTION);

            try {

                signKeyDescriptor.setKeyInfo(getKeyInfo(credential));
                encryptedKeyDescriptor.setKeyInfo(getKeyInfo(credential));

                encryptionMethod = encryptionMethodBuilder.buildObject();
                encryptionMethod.setAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
                signKeyDescriptor.getEncryptionMethods().add(encryptionMethod);

                spssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);
                spssoDescriptor.getKeyDescriptors().add(encryptedKeyDescriptor);
            } catch (org.opensaml.xml.security.SecurityException e) {

                Logger.error(this, "Error generating credentials", e);
                throw new DotSamlException(e.getMessage(), e);
            }
        } catch (DotSamlException e) {

            throw e;
        } catch (Exception e) {

            Logger.error(this,"Error retrieving credentials", e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // setKeyDescriptors.

    protected KeyInfo getKeyInfo(final Credential credential) throws Exception {

        final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();

        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        final KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

        Logger.info(this, "Meta Data Credential: " + credential);

        return keyInfoGenerator.generate(credential);
    }

    protected List<Credential> getCredentialSigningList(final String entityId, final IDPSSODescriptor idpDescriptor) {

        return idpDescriptor.getKeyDescriptors().stream()
                .filter( key -> null != key.getKeyInfo() &&
                        key.getKeyInfo().getX509Datas().get(0).getX509Certificates().size() > 0 &&
                        UsageType.SIGNING == key.getUse() ) // not signing are relevant by now.
                .map   ( key ->  convertToCredential(entityId, key.getKeyInfo().getX509Datas()
                                        .get(0).getX509Certificates().get(0)) )
                .collect(Collectors.toList());
    }

    protected Credential convertToCredential(final String entityId,
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

    protected Map<String, String> getSingleSignOnMap(final IDPSSODescriptor idpDescriptor) {

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

    protected EntityDescriptor unmarshall(final InputStream is) throws Exception {

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
} // E:O:F:DefaultMetaDescriptorServiceImpl.
