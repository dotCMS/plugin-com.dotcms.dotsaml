package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.CredentialProvider;
import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.UtilMethods;
import org.opensaml.security.credential.Credential;

import java.io.Serializable;
import java.util.Collection;

/**
 * The configuration encapsulates all the info necessary for the open saml plugin
 * @author jsanca
 */
public interface Configuration extends Serializable {

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

        return Config.getStringProperty(propertyKey, defaultValue);
    } // getStringProperty

    /**
     * Gets a single property, defaultValue if the property does not exists in the configuration file
     * @param propertyKey {@link String}
     * @param defaultValue {@link String}
     * @return String
     */
    public default boolean getBooleanProperty(final String propertyKey, final boolean defaultValue) {

        return Config.getBooleanProperty(propertyKey, defaultValue);
    } // getBooleanProperty.

    /**
     * Gets an array string, defaultStringArray if does not exists
     * @param propertyKey {@link String}
     * @param defaultStringArray {@link String} array
     * @return String []
     */
    public default String[] getStringArray(final String propertyKey, final String[] defaultStringArray) {

        final String [] array = Config.getStringArrayProperty(propertyKey);
        return (null != array && array.length > 0)? array:defaultStringArray;
    } // getStringArray.

    /**
     * Returns the path to mapping the metadata.xml info for SP (Service Provider), in our case the SP is  dotCMS.
     * @return String
     */
    public default String getServiceProviderCustomMetadataPath() {

        return Config.getStringProperty(
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
     * If the user wants to do a verifyAssertionSignature, by default true.
     * There are some testing or diagnostic scenarios where you want to avoid the validation to identified issues, but in general on production this must be true.
     *
     * @return boolean
     */
    public default boolean isVerifyAssertionSignatureNeeded () {

        return Config.getBooleanProperty
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

        return Config.getBooleanProperty
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

        return Config.getBooleanProperty
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

        final String assertionConsumerEndpoint =
                Config.getStringProperty(DotSamlConstants.DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL, null);

        return UtilMethods.isSet(assertionConsumerEndpoint)?
                assertionConsumerEndpoint: null;
    } // getAssertionConsumerEndpoint.

    /**
     * In case you need a custom credentials for the Service Provider (DotCMS) overrides the
     * implementation class on the configuration.
     * By default it uses a Trust Storage to get the keys and creates the credential.
     * @return CredentialProvider
     */
    public default CredentialProvider getServiceProviderCustomCredentialProvider() {

        final String className = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null);

        final Class clazz = InstanceUtil.getClass(className);

        return null != clazz?
                (CredentialProvider) InstanceUtil.newInstance(clazz) :null;
    } // getServiceProviderCustomCredentialProvider.

    /**
     * In case you need a custom credentials for the ID Provider (DotCMS) overrides the
     * implementation class on the configuration.
     *
     * By default it uses the Idp metadata credentials info, from the XML to figure out this info.
     *
     * @return CredentialProvider
     */
    public default CredentialProvider getIdProviderCustomCredentialProvider()  {

        final String className = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null);

        final Class clazz = InstanceUtil.getClass(className);

        return null != clazz?
                (CredentialProvider) InstanceUtil.newInstance(clazz) :null;
    } // getIdProviderCustomCredentialProvider.

    /**
     * Gets the Redirect Identity Provider Destination Single Sign on URL
     * @return String
     */
    String getRedirectIdentityProviderDestinationSSOURL();


} // E:O:F:Configuration.