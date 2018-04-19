package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.key.BindingType;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotcms.plugin.saml.v3.meta.MetadataBean;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;

import com.dotmarketing.util.Logger;

import java.io.FileInputStream;
import java.util.Collection;

import org.opensaml.security.credential.Credential;

/**
 * Provides a helper to read xml metadata file of the SAML config.
 * 
 * @author Nathan (Ethode)
 * @version 4.3.2
 * @since 03-31-2018
 */
public class MetaDataHelper
{
	/**
	 * Gets the metadata, null if it can not be created.
	 * 
	 * @param idpConfig IdpConfig
	 * @return MetadataBean
	 */
	public static MetadataBean getMetaData( IdpConfig idpConfig )
	{
		MetadataBean metadataBean = null;
		MetaDescriptorService descriptorParser = InstanceUtil.newInstance( OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME, null ), DefaultMetaDescriptorServiceImpl.class );

		try
		{
			metadataBean = descriptorParser.parse( new FileInputStream( idpConfig.getIdPMetadataFile() ), idpConfig );
		}
		catch ( Exception exception )
		{
			Logger.error( MetaDataHelper.class, exception.getMessage(), exception );
		}

		return metadataBean;
	}

	/**
	 * The meta descriptor service is created on the configuration, so we take
	 * advance and return the instance from it.
	 * 
	 * @param idpConfig IdpConfig
	 * @return MetaDescriptorService
	 */
	// Todo: this could be in the InstancePool
	public static MetaDescriptorService getMetaDescriptorService( IdpConfig idpConfig )
	{
		MetaDescriptorService metaDescriptorService = InstanceUtil.newInstance( OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME, null ), DefaultMetaDescriptorServiceImpl.class );

		return metaDescriptorService;
	}

	public static Collection<Credential> getSigningCredentials( IdpConfig idpConfig )
	{
		MetadataBean metadataBean = getMetaData( idpConfig );

		return ( null != metadataBean ) ? metadataBean.getCredentialSigningList() : null;
	}

	/**
	 * Gets the Identity Provider Destination Single Sign on URL
	 * 
	 * @param idpConfig IdpConfig
	 * @return String
	 */
	public static String getIdentityProviderDestinationSSOURL( IdpConfig idpConfig )
	{
		String url = null;
		String bindingType = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOTCMS_SAML_BINDING_TYPE, BindingType.REDIRECT.getBinding() );
		MetadataBean metadataBean = getMetaData( idpConfig );

		if ( null != metadataBean && null != metadataBean.getSingleSignOnBindingLocationMap() && metadataBean.getSingleSignOnBindingLocationMap().containsKey( bindingType ) )
		{
			url = metadataBean.getSingleSignOnBindingLocationMap().get( bindingType );
		}

		return url;
	}

	/**
	 * Gets the Identity Provider Destination Single Logout URL
	 * 
	 * @param idpConfig IdpConfig
	 * @return String
	 */
	public static String getIdentityProviderDestinationSLOURL( IdpConfig idpConfig )
	{
		String url = null;
		String bindingType = OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOTCMS_SAML_BINDING_TYPE, BindingType.REDIRECT.getBinding() );
		MetadataBean metadataBean = getMetaData( idpConfig );

		if ( null != metadataBean && null != metadataBean.getSingleLogoutBindingLocationMap() && metadataBean.getSingleLogoutBindingLocationMap().containsKey( bindingType ) )
		{

			url = metadataBean.getSingleLogoutBindingLocationMap().get( bindingType );
		}

		return url;
	}

	/**
	 * Returns the path to mapping the metadata.xml info for SP (Service
	 * Provider), in our case the SP is dotCMS.
	 * 
	 * @param idpConfig IdpConfig
	 * @return String
	 */
	public static String getServiceProviderCustomMetadataPath( IdpConfig idpConfig )
	{
		return OptionalPropertiesHelper.getOptionString( idpConfig, DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH, DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE );
	}
}
