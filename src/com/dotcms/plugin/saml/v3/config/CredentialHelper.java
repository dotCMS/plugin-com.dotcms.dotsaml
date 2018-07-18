package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.key.DotSamlConstants;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotcms.plugin.saml.v3.util.InstanceUtil;

public class CredentialHelper
{
	/**
	 * In case you need a custom credentials for the ID Provider (dotCMS)
	 * overrides the implementation class on the configuration. By default it
	 * uses the Idp metadata credentials info, from the XML to figure out this
	 * info.
	 *
	 * @param idpConfig IdpConfig
	 * @return CredentialProvider
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static CredentialProvider getIdProviderCustomCredentialProvider( IdpConfig idpConfig )
	{
		String className = DotsamlPropertiesService.getOptionString( idpConfig, DotsamlPropertyName.DOT_SAML_ID_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME);

		Class clazz = InstanceUtil.getClass( className );

		return null != clazz ? (CredentialProvider) InstanceUtil.newInstance( clazz ) : null;
	}

	/**
	 * In case you need custom credentials for the Service Provider (DotCMS)
	 * overwrites the implementation class on the configuration. By default it
	 * uses a Trust Storage to get the keys and creates the credential.
	 * 
	 * @param idpConfig IdpConfig
	 * @return CredentialProvider
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static CredentialProvider getServiceProviderCustomCredentialProvider( IdpConfig idpConfig )
	{
		String className = DotsamlPropertiesService.getOptionString( idpConfig, DotsamlPropertyName.DOT_SAML_SERVICE_PROVIDER_CUSTOM_CREDENTIAL_PROVIDER_CLASSNAME);

		Class clazz = InstanceUtil.getClass( className );

		return null != clazz ? (CredentialProvider) InstanceUtil.newInstance( clazz ) : null;
	}

	/**
	 * If the user wants to do a verifyAssertionSignature, by default true.
	 * There are some testing or diagnostic scenarios where you want to avoid
	 * the validation to identified issues, but in general on production this
	 * must be true.
	 *
	 * @param idpConfig IdpConfig
	 * @return boolean
	 */
	public static boolean isVerifyAssertionSignatureNeeded( IdpConfig idpConfig )
	{
		if ( idpConfig.getSignatureValidationType().equals( DotSamlConstants.RESPONSE_AND_ASSERTION ) || idpConfig.getSignatureValidationType().equals( DotSamlConstants.ASSERTION ) )
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
	 * @param idpConfig IdpConfig
	 * @return boolean
	 */
	public static boolean isVerifyResponseSignatureNeeded( IdpConfig idpConfig )
	{
		if ( idpConfig.getSignatureValidationType().equals( DotSamlConstants.RESPONSE_AND_ASSERTION ) || idpConfig.getSignatureValidationType().equals( DotSamlConstants.RESPONSE ) )
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
	 * @param idpConfig IdpConfig
	 * @return boolean
	 */
	public static boolean isVerifySignatureCredentialsNeeded( IdpConfig idpConfig )
	{
		return DotsamlPropertiesService.getOptionBoolean( idpConfig, DotsamlPropertyName.DOT_SAML_VERIFY_SIGNATURE_CREDENTIALS);

	}

	/**
	 * If the user wants to do a verifySignatureProfile, by default true There
	 * are some testing or diagnostic scenarios where you want to avoid the
	 * validation to identified issues, but in general on production this must
	 * be true. Note: if isVerifyAssertionSignatureNeeded is true, this is also
	 * skipped.
	 * 
	 * @param idpConfig IdpConfig
	 * @return boolean
	 */
	public static boolean isVerifySignatureProfileNeeded( IdpConfig idpConfig )
	{
		return DotsamlPropertiesService.getOptionBoolean( idpConfig, DotsamlPropertyName.DOT_SAML_VERIFY_SIGNATURE_PROFILE);
	}
}
