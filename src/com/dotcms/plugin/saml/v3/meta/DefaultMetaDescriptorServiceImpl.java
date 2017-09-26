package com.dotcms.plugin.saml.v3.meta;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.SamlUtils;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationBean;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Logger;
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

    /**
     * By default the pivot to get the {@link IDPSSODescriptor} by using {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE
     * However if there is any different protocol you want to use, please change the {@link DotSamlConstants}.DOT_SAML_IDP_METADATA_PROTOCOL
     * to override the value on the dotmarketing-config.properties
     * @param inputStream {@link InputStream} this is the stream of the Idp-metadata.xml
     *                                       (get it from the IdP, for instance the idp-metadata.xml from the shibboleth installation.
     * @return MetadataBean
     * @throws Exception
     */
    @Override
    public MetadataBean parse(final InputStream inputStream, final SiteConfigurationBean siteConfigurationBean) throws Exception {

        final EntityDescriptor descriptor = unmarshall(inputStream);
        final String protocol             = siteConfigurationBean.getString
                (DotSamlConstants.DOT_SAML_IDP_METADATA_PROTOCOL,
                        DotSamlConstants.DOT_SAML_IDP_METADATA_PROTOCOL_DEFAULT_VALUE);
        final IDPSSODescriptor idpDescriptor = descriptor.getIDPSSODescriptor(protocol);

        Logger.info(this, "Parsing the Id Provider, with the entityId: " +
                descriptor.getEntityID());

        return new MetadataBean(descriptor.getEntityID(),
                idpDescriptor.getErrorURL(),
                this.getSingleSignOnMap(idpDescriptor),
                this.getSingleLogoutMap(idpDescriptor),
                this.getCredentialSigningList(descriptor.getEntityID(), idpDescriptor));
    } // parse.

    protected Map<String, String> getSingleLogoutMap(IDPSSODescriptor idpDescriptor) {
        final Map<String, String> singleLogoutBindingLocationMap =
                new LinkedHashMap<>();

        idpDescriptor.getSingleLogoutServices().stream().forEach( sso -> {

            Logger.debug(this,"Add SLO binding " + sso.getBinding()
                    + "(" + sso.getLocation() + ")");
            singleLogoutBindingLocationMap.put(sso.getBinding(),
                    sso.getLocation());
        } );

        return singleLogoutBindingLocationMap;
    }

    /**
     * This creates from the runtime configuration, the {@link EntityDescriptor} for dotCMS Service Provider.
     *
     * Things to keep in mind:
     * 1) By default WantAssertionsSigned is true, if you want to overrides it please use {@link DotSamlConstants}.DOTCMS_SAML_WANT_ASSERTIONS_SIGNED on the dotmarketing-config.properties
     * 2) By default AuthnRequestsSigned is true,  if you want to overrides it please use {@link DotSamlConstants}.DOTCMS_SAML_AUTHN_REQUESTS_SIGNED on the dotmarketing-config.properties
     * 3) All Assertion Consumer Services use the url returned by: {@link DotSamlConstants}.DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL, this value usually will be the dotCMS landing page.
     * 4) By default as a formats we return: {@link NameIDType}.TRANSIENT, {@link NameIDType}.PERSISTENT
     *          however if you want to override it change the {@link DotSamlConstants}.DOTCMS_SAML_NAME_ID_FORMATS on the dotmarketing-config.properties (comma separated)
     * @param configuration {@link Configuration}
     * @return EntityDescriptor
     */
    @Override
    public EntityDescriptor getServiceProviderEntityDescriptor(final Configuration configuration) {

        final SAMLObjectBuilder<EntityDescriptor>  entityDescriptorBuilder =
                (SAMLObjectBuilder<EntityDescriptor>) this.xmlObjectBuilderFactory.getBuilder
                        (EntityDescriptor.DEFAULT_ELEMENT_NAME);
        final SAMLObjectBuilder<SPSSODescriptor> spssoDescriptorBuilder =
                (SAMLObjectBuilder<SPSSODescriptor>) this.xmlObjectBuilderFactory.getBuilder
                        (SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        final SAMLObjectBuilder<AssertionConsumerService> assertionConsumerServiceBuilder =
                (SAMLObjectBuilder<AssertionConsumerService>) this.xmlObjectBuilderFactory.getBuilder
                        (AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        final SAMLObjectBuilder<SingleLogoutService> singleLogoutServiceBuilder =
                (SAMLObjectBuilder<SingleLogoutService>) this.xmlObjectBuilderFactory.getBuilder
                        (SingleLogoutService.DEFAULT_ELEMENT_NAME);

        final EntityDescriptor descriptor      = entityDescriptorBuilder.buildObject();
        final SPSSODescriptor  spssoDescriptor = spssoDescriptorBuilder.buildObject();

        descriptor.setEntityID(SamlUtils.getSPIssuerValue(configuration)); // this get from the dotmarketing-config.properties.

        Logger.info(this, "Creating the MetaData for the site: " + configuration.getSiteName());
        Logger.debug(this, "Generating the Entity Provider Descriptor for: " +
                descriptor.getEntityID());

        spssoDescriptor.setWantAssertionsSigned(configuration.getBooleanProperty
                (DotSamlConstants.DOTCMS_SAML_WANT_ASSERTIONS_SIGNED, true));
        spssoDescriptor.setAuthnRequestsSigned(configuration.getBooleanProperty
                (DotSamlConstants.DOTCMS_SAML_AUTHN_REQUESTS_SIGNED, true));
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20_NS);

        Logger.debug(this, "Setting the key descriptors for: " +
                descriptor.getEntityID());

        // set's the SIGNING and ENCRYPTION keyinfo.
        this.setKeyDescriptors (spssoDescriptor, configuration);

        // set's the messages format.
        this.setFormat(configuration, spssoDescriptor);

        // set's the assertion customer services, this will be a fixed url on dotCMS.
        /*spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(0, SAMLConstants.SAML2_ARTIFACT_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));*/

        spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(0, SAMLConstants.SAML2_POST_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));

        spssoDescriptor.getSingleLogoutServices().add
                (this.createSingleLogoutService(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI,
                        configuration.getSingleLogoutEndpoint(), singleLogoutServiceBuilder));

        /*spssoDescriptor.getAssertionConsumerServices().add
                (this.createAssertionConsumerService(2, SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI,
                        configuration.getAssertionConsumerEndpoint(), assertionConsumerServiceBuilder));*/

        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        descriptor.getRoleDescriptors().add(spssoDescriptor);

        return descriptor;
    } // getServiceProviderEntityDescriptor.

    /**
     * Template method to create an assertion consumer service
     * @param index {@link Integer}
     * @param binding {@link String}
     * @param location {@link String}
     * @param assertionConsumerServiceBuilder {@link SAMLObjectBuilder}
     * @return AssertionConsumerService
     */
    protected AssertionConsumerService createAssertionConsumerService(final int    index,
                                                                      final String binding,
                                                                      final String location,
                                                                      final SAMLObjectBuilder<AssertionConsumerService> assertionConsumerServiceBuilder) {

        Logger.debug(this, "Assertion consumer service, location: " + location);

        final AssertionConsumerService assertionConsumerServiceArtifact =
                assertionConsumerServiceBuilder.buildObject();
        assertionConsumerServiceArtifact.setIndex(index);
        assertionConsumerServiceArtifact.setBinding(binding);
        assertionConsumerServiceArtifact.setLocation(location);

        return assertionConsumerServiceArtifact;
    } // createAssertionConsumerService.

    /**
     * Template method to create single logout service
     * @param binding {@link String}
     * @param location {@link String}
     * @param singleLogoutServiceBuilder {@link SAMLObjectBuilder}
     * @return SingleLogoutService
     */
    protected SingleLogoutService createSingleLogoutService(final String binding,
                                                                      final String location,
                                                                      final SAMLObjectBuilder<SingleLogoutService> singleLogoutServiceBuilder) {

        Logger.debug(this, "Assertion consumer service, location: " + location);

        final SingleLogoutService assertionConsumerService =
                singleLogoutServiceBuilder.buildObject();

        assertionConsumerService.setBinding(binding);
        assertionConsumerService.setLocation(location);

        return assertionConsumerService;
    } // createSingleLogoutService.

    /**
     * Sets the format's supported for the messages.
     *
     * @param configuration {@link Configuration}
     * @param spssoDescriptor {@link SPSSODescriptor}
     */
    protected void setFormat(final Configuration configuration,
                             final SPSSODescriptor spssoDescriptor) {

        final SAMLObjectBuilder<NameIDFormat> nameIDFormatBuilder =
                (SAMLObjectBuilder<NameIDFormat>) this.xmlObjectBuilderFactory.getBuilder
                        (NameIDFormat.DEFAULT_ELEMENT_NAME);

        final String [] formats = configuration.getStringArray (DotSamlConstants.DOTCMS_SAML_NAME_ID_POLICY_FORMAT,
                new String[] {  NameIDType.PERSISTENT });

        for (String format : formats) {

            spssoDescriptor.getNameIDFormats().add
                    (this.createFormat(format, nameIDFormatBuilder));
        }
    } // setFormat.

    protected NameIDFormat createFormat(final String format, final SAMLObjectBuilder<NameIDFormat> nameIDFormatBuilder) {

        final NameIDFormat nameIDFormat = nameIDFormatBuilder.buildObject();
        nameIDFormat.setFormat(format);
        return nameIDFormat;
    } // createFormat.

    /**
     * This method is created in this way, just in case some subclass would like to override it.
     * @return Credential
     */
    protected Credential getCredential (final Configuration configuration) {

        return SamlUtils.getCredential(configuration);
    } // getCredential.

    /**
     * Set the Key Descriptors, SIGNING and ENCRYPTION keyInfo.
     * @param spssoDescriptor {@link SPSSODescriptor}
     */
    protected void setKeyDescriptors(final SPSSODescriptor spssoDescriptor, final Configuration configuration) {

        final boolean isEncryptedDescriptor =
                configuration.getBooleanProperty(DotSamlConstants.DOTCMS_SAML_USE_ENCRYPTED_DESCRIPTOR, false);
        final SAMLObjectBuilder<KeyDescriptor> keyDescriptorBuilder =
                (SAMLObjectBuilder<KeyDescriptor>) this.xmlObjectBuilderFactory.
                        getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        final KeyDescriptor signKeyDescriptor;
        KeyDescriptor encryptedKeyDescriptor = null;
        final Credential credential = this.getCredential(configuration);
        final EncryptionMethodBuilder encryptionMethodBuilder = new EncryptionMethodBuilder();
        final EncryptionMethod encryptionMethod;

        try {

            signKeyDescriptor = keyDescriptorBuilder.buildObject();
            signKeyDescriptor.setUse(UsageType.SIGNING);

            if (isEncryptedDescriptor) {
                encryptedKeyDescriptor = keyDescriptorBuilder.buildObject();
                encryptedKeyDescriptor.setUse(UsageType.ENCRYPTION);
            }

            try {

                signKeyDescriptor.setKeyInfo(getKeyInfo(credential));

                encryptionMethod = encryptionMethodBuilder.buildObject();
                encryptionMethod.setAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
                signKeyDescriptor.getEncryptionMethods().add(encryptionMethod);

                spssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);

                if (isEncryptedDescriptor) {
                    encryptedKeyDescriptor.setKeyInfo(getKeyInfo(credential));
                    spssoDescriptor.getKeyDescriptors().add(encryptedKeyDescriptor);
                }
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

    /**
     * Using the {@link Credential} gets the KeyInfo.
     * @param credential {@link Credential}
     * @return KeyInfo
     * @throws Exception
     */
    protected KeyInfo getKeyInfo(final Credential credential) throws Exception {

        final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();

        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        final KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

        Logger.debug(this, "Meta Data Credential: " + credential);

        return keyInfoGenerator.generate(credential);
    } // getKeyInfo.

    /**
     * Gets from the idp metada the list of signing credential's
     * @param entityId {@link String}
     * @param idpDescriptor {@link IDPSSODescriptor}
     * @return
     */
    protected List<Credential> getCredentialSigningList(final String entityId,
                                                        final IDPSSODescriptor idpDescriptor) {

        return idpDescriptor.getKeyDescriptors().stream()
                .filter( key -> null != key.getKeyInfo() &&
                        key.getKeyInfo().getX509Datas().get(0).getX509Certificates().size() > 0 &&
                        UsageType.SIGNING == key.getUse() ) // not signing are relevant by now.
                .map   ( key ->  convertToCredential(entityId, key.getKeyInfo().getX509Datas()
                                        .get(0).getX509Certificates().get(0)) )
                .collect(Collectors.toList());
    } // getCredentialSigningList.

    /**
     * Convert the x509Certificate {@link org.opensaml.xmlsec.signature.X509Certificate} to {@link Credential}
     * @param entityId {@link String}
     * @param x509Certificate {@link org.opensaml.xmlsec.signature.X509Certificate}
     * @return Credential
     */
    protected Credential convertToCredential(final String entityId,
                                           final org.opensaml.xmlsec.signature.X509Certificate x509Certificate) {

        final byte[] decoded;
        final CertificateFactory cf;
        final java.security.cert.X509Certificate javaX509Certificate;
        ByteArrayInputStream bais = null;
        Credential credential = null;

        try {

            decoded = Base64.decode(x509Certificate.getValue());
            cf      = CertificateFactory.getInstance(X_509);
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

    /**
     * Generates a map of binding type -> location
     * @param idpDescriptor IDPSSODescriptor
     * @return Map
     */
    protected Map<String, String> getSingleSignOnMap(final IDPSSODescriptor idpDescriptor) {

        final Map<String, String> singleSignOnBindingLocationMap =
                new LinkedHashMap<>();

        idpDescriptor.getSingleSignOnServices().stream().forEach( sso -> {

            Logger.debug(this,"Add SSO binding " + sso.getBinding()
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
