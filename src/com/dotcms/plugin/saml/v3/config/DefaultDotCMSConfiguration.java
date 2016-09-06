package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.BindingType;
import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.InputStreamUtils;
import com.dotcms.plugin.saml.v3.InstanceUtil;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotcms.plugin.saml.v3.meta.MetadataBean;
import com.dotmarketing.util.Config;
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

    public DefaultDotCMSConfiguration() {

        this.descriptorParser = InstanceUtil.newInstance(
                Config.getStringProperty(DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME,
                        null), DefaultMetaDescriptorServiceImpl.class);
        final String metaDescriptorResourcePath =
                this.getStringProperty(DotSamlConstants.DOTCMS_SAML_IDP_METADATA_PATH, null);

        this.metadataBean = (UtilMethods.isSet(metaDescriptorResourcePath))?
                this.getMetaData(metaDescriptorResourcePath):null;
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

            Logger.info(this, "Parsing the meta data: " + metaDescriptorResourcePath);
            metadataBean = this.descriptorParser.parse (inputStream);
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
        }

        return metadataBean;
    } // initMetaData.

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
    public Collection<Credential> getSigningCredentials() {

        return (null != this.metadataBean)?
                    this.metadataBean.getCredentialSigningList(): null;
    } // getSigningCredentials.

    @Override
    public String getRedirectIdentityProviderDestinationSSOURL() {

        String url = null;

        if (null != this.metadataBean &&
                null != this.metadataBean.getSingleSignOnBindingLocationMap() &&
                this.metadataBean.getSingleSignOnBindingLocationMap().
                        containsKey(BindingType.REDIRECT.getBinding())) {

            url = this.metadataBean.getSingleSignOnBindingLocationMap().get
                    (BindingType.REDIRECT.getBinding());
        }

        return url;
    } // getRedirectIdentityProviderDestinationSSOURL.


} // E:O:F:DefaultDotCMSConfiguration.
