package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.BindingType;
import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InputStreamUtils;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotcms.plugin.saml.v3.meta.MetadataBean;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import org.opensaml.security.credential.Credential;

import java.io.InputStream;
import java.util.Collection;

/**
 * Default implementation, it is a wrapper of the Dot Cms {@link com.dotmarketing.util.Config}
 * In addition will determine if any idp-metainfo.xml is there, if it is will create a Credential for each signing
 * also will stored the endpoints.
 * @author jsanca
 */
public class DefaultDotCMSConfiguration implements Configuration {

    private final MetadataBean metadataBean;
    private final MetaDescriptorService descriptorParser;
    private final SiteConfigurationBean siteConfigurationBean;
    private final String siteName;

    public DefaultDotCMSConfiguration(final SiteConfigurationBean siteConfigurationBean, final String siteName) {

        this.siteConfigurationBean = siteConfigurationBean;

        this.descriptorParser = InstanceUtil.newInstance(
                this.getStringProperty(DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME, null),
                    DefaultMetaDescriptorServiceImpl.class);

        final String metaDescriptorResourcePath =
                this.getStringProperty(DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH, null);

        Logger.info(this, "For the sitename: " + siteName
                + ", the metaDescriptorResourcePath is " + metaDescriptorResourcePath);

        this.metadataBean = (UtilMethods.isSet(metaDescriptorResourcePath))?
                this.getMetaData(metaDescriptorResourcePath):null;

        this.siteName     = siteName;
    } // DefaultDotCMSConfiguration.

    /**
     * Gets the metadata, null if it can not be created.
     * @param metaDescriptorResourcePath {@link String}
     * @return MetadataBean
     */
    protected MetadataBean getMetaData (final String metaDescriptorResourcePath) {

        MetadataBean metadataBean = null;

        try (InputStream inputStream =
                     InputStreamUtils.getInputStream(metaDescriptorResourcePath)) {

            Logger.debug(this, "Parsing the meta data: " + metaDescriptorResourcePath);
            metadataBean = this.descriptorParser.parse (inputStream, this.siteConfigurationBean);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
        }

        return metadataBean;
    } // initMetaData.

    @Override
    public String getSiteName() {

        return this.siteName;
    }

    @Override
    public SiteConfigurationBean getSiteConfiguration() {

        return this.siteConfigurationBean;
    }

    @Override
    public MetaDescriptorService getMetaDescriptorService() {
        return this.descriptorParser;
    }

    @Override
    public String[] getAccessFilterArray() {

        final String accessFilterValues =
                this.getStringProperty(DotSamlConstants.DOT_SAML_ACCESS_FILTER_VALUES, null);

        return (UtilMethods.isSet(accessFilterValues))?
                    accessFilterValues.split(","):null;
    }

    @Override
    public String[] getIncludePathArray() {

        final String accessFilterValues =
                this.getStringProperty(DotSamlConstants.DOT_SAML_INCLUDE_PATH_VALUES,
                        "^/" + ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP + "*$,"
                                + "^/dotCMS/login.*$,"
                                + "^/html/portal/login.*$,"
                                + "^/c/public/login.*$,"
                                + "^/c/portal_public/login.*$,"
                                + "^/c/portal/logout.*$,"
                                + "^/dotCMS/logout.*$,"
                                + "^/application/login/login.*$,"
                                + "^/dotAdmin.*$"
                );

        return (UtilMethods.isSet(accessFilterValues))?
                accessFilterValues.split(","):null;
    }


    public  String[] getLogoutPathArray() {

        final String logoutPathValues =
                this.getStringProperty(DotSamlConstants.DOT_SAML_LOGOUT_PATH_VALUES,
                                 "/c/portal/logout,/dotCMS/logout");

        return (UtilMethods.isSet(logoutPathValues))?
                logoutPathValues.split(","):null;
    } // getLogoutPathArray,

    @Override
    public Collection<Credential> getSigningCredentials() {

        return (null != this.metadataBean)?
                    this.metadataBean.getCredentialSigningList(): null;
    } // getSigningCredentials.

    @Override
    public String getIdentityProviderDestinationSSOURL(final Configuration configuration) {

        String url               = null;
        final String bindingType = configuration.getStringProperty(DotSamlConstants.DOTCMS_SAML_BINDING_TYPE,
                BindingType.REDIRECT.getBinding());

        if (null != this.metadataBean &&
                null != this.metadataBean.getSingleSignOnBindingLocationMap() &&
                this.metadataBean.getSingleSignOnBindingLocationMap().
                        containsKey(bindingType)) {

            url = this.metadataBean.
                    getSingleSignOnBindingLocationMap().get(bindingType);
        }

        return url;
    } // getIdentityProviderDestinationSSOURL.


    @Override
    public String getIdentityProviderDestinationSLOURL(final Configuration configuration) {

        String url               = null;
        final String bindingType = configuration.getStringProperty(DotSamlConstants.DOTCMS_SAML_BINDING_TYPE,
                BindingType.REDIRECT.getBinding());

        if (null != this.metadataBean &&
                null != this.metadataBean.getSingleLogoutBindingLocationMap() &&
                this.metadataBean.getSingleLogoutBindingLocationMap().
                        containsKey(bindingType)) {

            url = this.metadataBean.
                    getSingleLogoutBindingLocationMap().get(bindingType);
        }

        return url;
    } // getIdentityProviderDestinationSLOURL.


} // E:O:F:DefaultDotCMSConfiguration.
