package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.exception.InvalidIssuerValueException;
import com.dotcms.plugin.saml.v3.key.BindingType;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.meta.DefaultMetaDescriptorServiceImpl;
import com.dotcms.plugin.saml.v3.meta.MetaDescriptorService;
import com.dotcms.plugin.saml.v3.meta.MetadataBean;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;
import com.dotcms.plugin.saml.v3.util.SamlUtils;

import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.opensaml.security.credential.Credential;

public class IdpConfig implements OptionalPropertiesHelper, MetaDataHelper, EndpointHelper
{
	private String id;
	private String idpName;
	private boolean enabled;
	private String sPIssuerURL;
	private String sPEndpointHostname;
	private File privateKey;
	private File publicCert;
	private File idPMetadataFile;
	private String signatureValidationType;
	private Properties optionalProperties;
	private Map<String, String> sites;

	public IdpConfig()
	{
		this.idpName = "";
		this.enabled = false;
		this.sPIssuerURL = "";
		this.sPEndpointHostname = "";
		this.privateKey = null;
		this.publicCert = null;
		this.idPMetadataFile = null;
		this.optionalProperties = new Properties();
	}

	@Override
	public boolean equals( Object object )
	{
		if ( this == object )
		{
			return true;
		}
		if ( object == null || getClass() != object.getClass() )
		{
			return false;
		}

		IdpConfig idpConfig = (IdpConfig) object;

		return Objects.equals( id, idpConfig.id );
	}

	@Override
	public String[] getAccessFilterArray()
	{
		final String accessFilterValues = this.getOptionString( DotSamlConstants.DOT_SAML_ACCESS_FILTER_VALUES, null );

		return ( UtilMethods.isSet( accessFilterValues ) ) ? accessFilterValues.split( "," ) : null;
	}

	@Override
	public String getAssertionConsumerEndpoint()
	{
		String spIssuerValue = SamlUtils.getSPIssuerValue( this );

		if ( null != spIssuerValue && !( spIssuerValue.trim().startsWith( DotSamlConstants.HTTP_SCHEMA ) || spIssuerValue.trim().startsWith( DotSamlConstants.HTTPS_SCHEMA ) ) )
		{
			throw new InvalidIssuerValueException( "The issuer: " + spIssuerValue + " should starts with http:// or https:// to be valid" );
		}

		spIssuerValue += DotSamlConstants.ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP;

		return this.getOptionString( DotSamlConstants.DOT_SAML_ASSERTION_CUSTOMER_ENDPOINT_URL, spIssuerValue );
	}

	public String getId()
	{
		return id;
	}

	@Override
	public String getIdentityProviderDestinationSLOURL()
	{
		String url = null;
		final String bindingType = this.getOptionString( DotSamlConstants.DOTCMS_SAML_BINDING_TYPE, BindingType.REDIRECT.getBinding() );
		final MetadataBean metadataBean = this.getMetaData();

		if ( null != metadataBean && null != metadataBean.getSingleLogoutBindingLocationMap() && metadataBean.getSingleLogoutBindingLocationMap().containsKey( bindingType ) )
		{

			url = metadataBean.getSingleLogoutBindingLocationMap().get( bindingType );
		}

		return url;
	}

	@Override
	public String getIdentityProviderDestinationSSOURL()
	{
		String url = null;
		final String bindingType = this.getOptionString( DotSamlConstants.DOTCMS_SAML_BINDING_TYPE, BindingType.REDIRECT.getBinding() );
		final MetadataBean metadataBean = this.getMetaData();

		if ( null != metadataBean && null != metadataBean.getSingleSignOnBindingLocationMap() && metadataBean.getSingleSignOnBindingLocationMap().containsKey( bindingType ) )
		{
			url = metadataBean.getSingleSignOnBindingLocationMap().get( bindingType );
		}

		return url;
	}

	public File getIdPMetadataFile()
	{
		return idPMetadataFile;
	}

	public String getIdpName()
	{
		return idpName;
	}

	/**
	 * In case you need a custom credentials for the ID Provider (dotCMS)
	 * overrides the implementation class on the configuration. By default it
	 * uses the Idp metadata credentials info, from the XML to figure out this
	 * info.
	 *
	 * @return CredentialProvider
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public CredentialProvider getIdProviderCustomCredentialProvider()
	{
		final String className = this.getOptionString( DotSamlConstants.DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null );

		final Class clazz = InstanceUtil.getClass( className );

		return null != clazz ? (CredentialProvider) InstanceUtil.newInstance( clazz ) : null;
	}

	@Override
	public String[] getIncludePathArray()
	{
		final String accessFilterValues = this.getOptionString( DotSamlConstants.DOT_SAML_INCLUDE_PATH_VALUES, "^" + DotSamlConstants.ASSERTION_CONSUMER_ENDPOINT_DOTSAML3SP + "$," + "^/dotCMS/login.*$," + "^/html/portal/login.*$," + "^/c/public/login.*$," + "^/c/portal_public/login.*$," + "^/c/portal/logout.*$," + "^/dotCMS/logout.*$," + "^/application/login/login.*$," + "^/dotAdmin.*$," + "^" + DotSamlConstants.LOGOUT_SERVICE_ENDPOINT_DOTSAML3SP + "$" );

		return ( UtilMethods.isSet( accessFilterValues ) ) ? accessFilterValues.split( "," ) : null;
	}

	@Override
	public String[] getLogoutPathArray()
	{
		final String logoutPathValues = this.getOptionString( DotSamlConstants.DOT_SAML_LOGOUT_PATH_VALUES, "/c/portal/logout,/dotCMS/logout,/dotsaml/request/logout" );

		return ( UtilMethods.isSet( logoutPathValues ) ) ? logoutPathValues.split( "," ) : null;
	}

	@Override
	public MetadataBean getMetaData()
	{
		MetadataBean metadataBean = null;
		final MetaDescriptorService descriptorParser = InstanceUtil.newInstance( this.getOptionString( DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME, null ), DefaultMetaDescriptorServiceImpl.class );

		try
		{
			metadataBean = descriptorParser.parse( new FileInputStream( this.getIdPMetadataFile() ), this );
		}
		catch ( Exception exception )
		{
			Logger.error( this, exception.getMessage(), exception );
		}

		return metadataBean;
	}

	@Override
	public final MetaDescriptorService getMetaDescriptorService()
	{
		final MetaDescriptorService metaDescriptorService = InstanceUtil.newInstance( this.getOptionString( DotSamlConstants.DOT_SAML_IDP_METADATA_PARSER_CLASS_NAME, null ), DefaultMetaDescriptorServiceImpl.class );

		return metaDescriptorService;
	}

	public Properties getOptionalProperties()
	{
		return optionalProperties;
	}

	@Override
	public boolean getOptionBoolean( final String propertyName )
	{
		return this.getOptionBoolean( propertyName, false );
	}

	@Override
	public boolean getOptionBoolean( final String propertyName, final boolean defaultValue )
	{
		boolean value = defaultValue;

		if ( this.optionalProperties.containsKey( propertyName ) )
		{
			try
			{
				value = (boolean) this.optionalProperties.get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( this, "Cast exception on " + propertyName + " property. idpConfigId: " + this.getId() );
			}
		}

		return value;
	}

	@Override
	public int getOptionInteger( final String propertyName, final int defaultValue )
	{
		int value = defaultValue;

		if ( this.optionalProperties.containsKey( propertyName ) )
		{
			try
			{
				value = (int) this.optionalProperties.get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( this, "Cast exception on " + propertyName + " property. idpConfigId: " + this.getId() );
			}
		}

		return value;
	}

	@Override
	public String getOptionString( final String propertyName )
	{
		return this.getOptionString( propertyName, null );
	}

	@Override
	public String getOptionString( final String propertyName, final String defaultValue )
	{
		String value = defaultValue;

		if ( this.optionalProperties.containsKey( propertyName ) )
		{
			try
			{
				value = (String) this.optionalProperties.get( propertyName );
			}
			catch ( Exception exception )
			{
				Logger.warn( this, "Cast exception on " + propertyName + " property. idpConfigId: " + this.getId() );
			}
		}

		return value;
	}

	@Override
	public String[] getOptionStringArray( String propertyName, String[] defaultArray )
	{
		String[] array = defaultArray;

		if ( this.optionalProperties.containsKey( propertyName ) )
		{
			array = StringUtils.split( (String) this.optionalProperties.get( propertyName ), DotSamlConstants.ARRAY_SEPARATOR_CHAR );
		}

		return array;
	}

	public File getPrivateKey()
	{
		return privateKey;
	}

	public File getPublicCert()
	{
		return publicCert;
	}

	/**
	 * In case you need custom credentials for the Service Provider (DotCMS)
	 * overwrites the implementation class on the configuration. By default it
	 * uses a Trust Storage to get the keys and creates the credential.
	 * 
	 * @return CredentialProvider
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public CredentialProvider getServiceProviderCustomCredentialProvider()
	{
		final String className = this.getOptionString( DotSamlConstants.DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME, null );

		final Class clazz = InstanceUtil.getClass( className );

		return null != clazz ? (CredentialProvider) InstanceUtil.newInstance( clazz ) : null;
	}

	@Override
	public String getServiceProviderCustomMetadataPath()
	{
		return this.getOptionString( DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH, DotSamlConstants.DOTCMS_SAML_SERVICE_PROVIDER_CUSTOM_METADATA_PATH_DEFAULT_VALUE );
	}

	public String getSignatureValidationType()
	{
		return signatureValidationType;
	}

	@Override
	public Collection<Credential> getSigningCredentials()
	{
		final MetadataBean metadataBean = this.getMetaData();

		return ( null != metadataBean ) ? metadataBean.getCredentialSigningList() : null;
	}

	@Override
	public String getSingleLogoutEndpoint()
	{
		String spIssuerValue = SamlUtils.getSPIssuerValue( this );

		if ( null != spIssuerValue && !( spIssuerValue.trim().startsWith( DotSamlConstants.HTTP_SCHEMA ) || spIssuerValue.trim().startsWith( DotSamlConstants.HTTPS_SCHEMA ) ) )
		{
			throw new InvalidIssuerValueException( "The issuer: " + spIssuerValue + " should starts with http:// or https:// to be valid" );
		}

		spIssuerValue += DotSamlConstants.LOGOUT_SERVICE_ENDPOINT_DOTSAML3SP;

		return this.getOptionString( DotSamlConstants.DOT_SAML_LOGOUT_SERVICE_ENDPOINT_URL, spIssuerValue );
	}

	public String getSiteNames()
	{
		return sites.values().stream().map( Object::toString ).collect( Collectors.joining( ", " ) );
	}

	public Map<String, String> getSites()
	{
		return sites;
	}

	public String getSpEndpointHostname()
	{
		return sPEndpointHostname;
	}

	public String getSpIssuerURL()
	{
		return sPIssuerURL;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( id );
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * If the user wants to do a verifyAssertionSignature, by default true.
	 * There are some testing or diagnostic scenarios where you want to avoid
	 * the validation to identified issues, but in general on production this
	 * must be true.
	 *
	 * @return boolean
	 */
	public boolean isVerifyAssertionSignatureNeeded()
	{
		if ( this.signatureValidationType.equals( DotSamlConstants.RESPONSE_AND_ASSERTION ) || this.signatureValidationType.equals( DotSamlConstants.ASSERTION ) )
		{
			return true;
		}

		return false;
	}

	/**
	 * If the user wants to do a verifyResponseSignature, by default true.
	 * There are some testing or diagnostic scenarios where you want to avoid
	 * the validation to identified issues, but in general on production this
	 * must be true.
	 *
	 * @return boolean
	 */
	public boolean isVerifyResponseSignatureNeeded()
	{
		if ( this.signatureValidationType.equals( DotSamlConstants.RESPONSE_AND_ASSERTION ) || this.signatureValidationType.equals( DotSamlConstants.RESPONSE ) )
		{
			return true;
		}

		return false;
	}

	/**
	 * If the user wants to do a verifySignatureCredentials, by default true
	 * There are some testing or diagnostic scenarios where you want to avoid
	 * the validation to identified issues, but in general on production this
	 * must be true. Note: if isVerifyAssertionSignatureNeeded is true, this is
	 * also skipped.
	 * 
	 * @return boolean
	 */
	public boolean isVerifySignatureCredentialsNeeded()
	{
		return this.getOptionBoolean( DotSamlConstants.DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS, true );
	}

	/**
	 * If the user wants to do a verifySignatureProfile, by default true There
	 * are some testing or diagnostic scenarios where you want to avoid the
	 * validation to identified issues, but in general on production this must
	 * be true. Note: if isVerifyAssertionSignatureNeeded is true, this is also
	 * skipped.
	 * 
	 * @return boolean
	 */
	public boolean isVerifySignatureProfileNeeded()
	{
		return this.getOptionBoolean( DotSamlConstants.DOT_SAML_VERIFY_SIGNATURE_PROFILE, true );
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public void setIdPMetadataFile( File idPMetadataFile )
	{
		this.idPMetadataFile = idPMetadataFile;
	}

	public void setIdpName( String idpName )
	{
		this.idpName = idpName;
	}

	public void setOptionalProperties( Properties optionalProperties )
	{
		this.optionalProperties = optionalProperties;
	}

	public void setPrivateKey( File privateKey )
	{
		this.privateKey = privateKey;
	}

	public void setPublicCert( File publicCert )
	{
		this.publicCert = publicCert;
	}

	public void setSignatureValidationType( String signatureValidationType )
	{
		this.signatureValidationType = signatureValidationType;
	}

	public void setSites( Map<String, String> sites )
	{
		this.sites = sites;
	}

	public void setSpEndpointHostname( String sPEndpointHostname )
	{
		this.sPEndpointHostname = sPEndpointHostname;
	}

	public void setSpIssuerURL( String sPIssuerURL )
	{
		this.sPIssuerURL = sPIssuerURL;
	}
}
