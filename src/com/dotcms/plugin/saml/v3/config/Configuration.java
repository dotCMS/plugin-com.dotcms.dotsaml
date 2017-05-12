package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.CredentialProvider;
import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.SamlUtils;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotmarketing.util.UtilMethods;
import org.opensaml.security.credential.Credential;

import java.io.Serializable;
import java.util.Collection;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOT_SAML_DEFAULT_SERVICE_PROVIDER_PROTOCOL;

/**
 * The configuration encapsulates all the info necessary for the open saml plugin
 * Note: an implementation of {@link Configuration} must has a constructor with {@link SiteConfigurationBean} arguments and a {@link String} siteName
 * @author jsanca
 */
public interface Configuration extends Serializable {

    public static final String HTTP_SCHEMA  = "http://";
    public static final String HTTPS_SCHEMA = "https://";
    String HTTPS_SCHEMA_PREFIX = "https";
    String ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP = "/dotsaml3sp";

    /**
     * Returns the site name associated to this configuration
     * @return String
     */
    String getSiteName ();

    /**
     * Returns the site configuration for the site associated to this configuration.
     * @return SiteConfigurationBean
     */
    public SiteConfigurationBean getSiteConfiguration();

    /**
     * The meta descriptor service is created on the configuration, so we take advance and return the instance from it.
     * @return MetaDescriptorService
     */
    // Todo: this could be in the InstancePool
    MetaDescriptorService getMetaDescriptorService();

    /**
     * Gets a single property, defaultValue if the property does not exists in the configuration file
     * @param propertyKey {@link String}
     * @param defaultValue {@link String}
     * @return String
     */
    public default String getStringProperty (final String propertyKey, final String defaultValue) {

        return this.getSiteConfiguration().getString(propertyKey, defaultValue);
    } // getStringProperty

    /**
     * Gets a single property, defaultValue if the property does not exists in the configuration file
     * @param propertyKey {@link String}
     * @param defaultValue {@link String}
     * @return String
     */
    public default boolean getBooleanProperty(final String propertyKey, final boolean defaultValue) {

        return this.getSiteConfiguration().getBoolean(propertyKey, defaultValue);
    } // getBooleanProperty.

    /**
     * Gets a single property, defaultValue if the property does not exists in the configuration file
     * @param propertyKey {@link String}
     * @param defaultValue {@link String}
     * @return int
     */
    default int getIntProperty(final String propertyKey, final int defaultValue) {

        return this.getSiteConfiguration().getInteger(propertyKey, defaultValue);
    }

    /**
     * Gets an array string, defaultStringArray if does not exists
     * @param propertyKey {@link String}
     * @param defaultStringArray {@link String} array
     * @return String []
     */
    public default String[] getStringArray(final String propertyKey, final String[] defaultStringArray) {

        final String [] array = this.getSiteConfiguration().getStringArray(propertyKey);
        return (null != array && array.length > 0)? array:defaultStringArray;
    } // getStringArray.

    /**
     * Returns the path to mapping the metadata.xml info for SP (Service Provider), in our case the SP is  dotCMS.
     * @return String
     */
    public default String getServiceProviderCustomMetadataPath() {

        return this.getSiteConfiguration().getString(
                    DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH,
                        DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE);
    } // getServiceProviderCustomMetadataPath.

    /**
     * Get's the access filter array, which are the exceptional cases to avoid to evaluate on the {@link com.dotcms.plugin.saml.v3.filter.SamlAccessFilter}
     * For instance if you include a file that shouldn't need any mapping, you can use it.
     * @return String []
     */
    String [] getAccessFilterArray();

    /**
     * Get's the include urls to be analized by the open saml plugin, usually the admin
     * They can be a pattern
     * @return String []
     */
    String[] getIncludePathArray();

    /**
     * If the user wants to do a verifyAssertionSignature, by default true.
     * There are some testing or diagnostic scenarios where you want to avoid the validation to identified issues, but in general on production this must be true.
     *
     * @return boolean
     */
    public default boolean isVerifyAssertionSignatureNeeded () {

        return this.getSiteConfiguration().getBoolean
                (DotSamlConstants.DOT_SAML_VERIFY_ASSERTION_SIGNATURE, true);
    } // isVerifyAssertionSignatureNeeded

    /**
     * If the user wants to do a verifySignatureProfile, by default true
     * There are some testing or diagnostic scenarios where you want to avoid the validation to identified issues, but in general on production this must be true.
     *
     * Note: if isVerifyAssertionSignatureNeeded is true, this is also skipped.
     * @return boolean
     */
    public default boolean isVerifySignatureProfileNeeded() {

        return this.getSiteConfiguration().getBoolean
                (DotSamlConstants.DOT_SAML_VERIFY_SIGNATURE_PROFILE, true);
    } // isVerifySignatureProfileNeeded

    /**
     * If the user wants to do a verifySignatureCredentials, by default true
     *
     * There are some testing or diagnostic scenarios where you want to avoid the validation to identified issues, but in general on production this must be true.
     *
     * Note: if isVerifyAssertionSignatureNeeded is true, this is also skipped.
     * @return boolean
     */
    public default boolean isVerifySignatureCredentialsNeeded() {

        return this.getSiteConfiguration().getBoolean
                (DotSamlConstants.DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS, true);
    } // isVerifySignatureCredentialsNeeded.

    /**
     * Gets the signing credentials, usually they are pull from the idp metadata xml, the idp metadata is generated from shibboleth, for instance or any other Idp.
     * @return Credential array
     */
    Collection<Credential> getSigningCredentials();

    /**
     * In case the user wants some specific customer url, otherwise null.
     * This URL is used on the metadata to fill out the AssertionConsumerService's
     * @return String
     */
    public default String getAssertionConsumerEndpoint() {

        String spIssuerValue = SamlUtils.getSPIssuerValue(this);

        if (null != spIssuerValue && !(spIssuerValue.trim().startsWith(HTTP_SCHEMA) || spIssuerValue.trim().startsWith(HTTPS_SCHEMA))) {

            throw new InvalidIssuerValueException ("The issuer: " + spIssuerValue + " should starts with http:// or https:// to be valid");
        }

        spIssuerValue += ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP;

        return
                this.getSiteConfiguration().getString(DotSamlConstants.DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL, spIssuerValue);
    } // getAssertionConsumerEndpoint.

    /**
     * In case you need custom credentials for the Service Provider (DotCMS) overwrites the
     * implementation class on the configuration.
     * By default it uses a Trust Storage to get the keys and creates the credential.
     * @return CredentialProvider
     */
    public default CredentialProvider getServiceProviderCustomCredentialProvider() {

        final String className = this.getSiteConfiguration().getString
                (DotSamlConstants.DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null);

        final Class clazz = InstanceUtil.getClass(className);

        return null != clazz?
                (CredentialProvider) InstanceUtil.newInstance(clazz) :null;
    } // getServiceProviderCustomCredentialProvider.

    /**
     * In case you need a custom credentials for the ID Provider (dotCMS) overrides the
     * implementation class on the configuration.
     *
     * By default it uses the Idp metadata credentials info, from the XML to figure out this info.
     *
     * @return CredentialProvider
     */
    public default CredentialProvider getIdProviderCustomCredentialProvider()  {

        final String className = this.getSiteConfiguration().getString
                (DotSamlConstants.DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null);

        final Class clazz = InstanceUtil.getClass(className);

        return null != clazz?
                (CredentialProvider) InstanceUtil.newInstance(clazz) :null;
    } // getIdProviderCustomCredentialProvider.

    /**
     * Gets the  Identity Provider Destination Single Sign on URL
     * @param configuration {@link Configuration}
     * @return String
     */
    String getIdentityProviderDestinationSSOURL(Configuration configuration);



} // E:O:F:Configuration.
