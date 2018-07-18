package com.dotcms.plugin.saml.v3.util;

import static com.dotmarketing.util.UtilMethods.isSet;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Element;

import com.dotcms.plugin.saml.v3.config.CredentialHelper;
import com.dotcms.plugin.saml.v3.config.CredentialProvider;
import com.dotcms.plugin.saml.v3.config.EndpointHelper;
import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.MetaDataHelper;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotmarketing.util.Logger;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy;

/**
 * Provides utils method for the Saml
 * 
 * @author jsanca
 */

public class SamlUtils {
	private final static RandomIdentifierGenerationStrategy secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();

	private final static XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

	private final static MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();

	private static final String DEFAULT_ELEMENT_NAME = "DEFAULT_ELEMENT_NAME";
	public static final String SAML_SESSION_INDEX = "SAMLSessionIndex";
	public static final String SAML_NAME_ID = "SAMLNameID";
	public static final String SINGLE_LOGOUT_REASON = "urn:oasis:names:tc:SAML:2.0:logout:user";

	private static Map<String, Credential> credentialMap = new ConcurrentHashMap<>();

	private static Map<String, Credential> idpCredentialMap = new ConcurrentHashMap<>();

	/**
	 * Build a SAML Object
	 * 
	 * @param clazz
	 * @param <T>
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T buildSAMLObject(final Class<T> clazz) {
		T object = null;
		QName defaultElementName = null;

		try {
			defaultElementName = (QName) clazz.getDeclaredField(DEFAULT_ELEMENT_NAME).get(null);
			object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
		} catch (IllegalAccessException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new IllegalArgumentException("Could not create SAML object");
		} catch (NoSuchFieldException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new IllegalArgumentException("Could not create SAML object");
		}

		return object;
	}

	public static LogoutRequest buildLogoutRequest(final IdpConfig idpConfig, final NameID nameID,
			final String sessionIndexValue) {
		final LogoutRequest logoutRequest = buildSAMLObject(LogoutRequest.class);
		final String idpSingleLogoutDestionation = getIPDSLODestination(idpConfig);
		SessionIndex sessionIndex = null;

		logoutRequest.setIssueInstant(new DateTime());
		logoutRequest.setID(generateSecureRandomId());

		// IDP logout url
		if (!isSet(idpSingleLogoutDestionation)) {
			Logger.error(SamlUtils.class,
					"The idpSingleLogoutDestionation is not set in the idp metadata, neither the configuration files");
			throw new DotSamlException("The property: "
					+ DotsamlPropertyName.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL.getPropertyName()
					+ " must be set on the host");
		}

		if (!isSet(nameID) || !isSet(sessionIndexValue)) {
			Logger.error(SamlUtils.class, "The nameID or sessionIndex are null");
			throw new DotSamlException("The nameID or sessionIndex are null");
		}

		Logger.debug(SamlUtils.class, "NameID: " + nameID + ", SessionIndex: " + sessionIndexValue);

		// id for the sender
		logoutRequest.setDestination(idpSingleLogoutDestionation);
		logoutRequest.setIssuer(buildIssuer(idpConfig));

		final NameID newNameID = buildSAMLObject(NameID.class);
		newNameID.setValue(nameID.getValue());
		newNameID.setFormat(nameID.getFormat());
		logoutRequest.setNameID(newNameID);

		sessionIndex = buildSAMLObject(SessionIndex.class);
		sessionIndex.setSessionIndex(sessionIndexValue);
		logoutRequest.getSessionIndexes().add(sessionIndex);

		logoutRequest.setReason(SINGLE_LOGOUT_REASON);
		logoutRequest.setVersion(SAMLVersion.VERSION_20);

		return logoutRequest;
	}

	/**
	 * Return the value of the /AuthnStatement@SessionIndex element in an
	 * assertion
	 *
	 * @return The value. <code>null</code>, if the assertion does not contain
	 *         the element.
	 */
	public static String getSessionIndex(final Assertion assertion) {
		String sessionIndex = null;

		if (assertion != null && assertion.getAuthnStatements() != null) {
			if (assertion.getAuthnStatements().size() > 0) {
				// We only look into the first AuthnStatement
				AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
				sessionIndex = authnStatement.getSessionIndex();
			}
		}

		return sessionIndex;
	}

	/**
	 * Build an authentication request.
	 * 
	 * @return AuthnRequest
	 */
	public static AuthnRequest buildAuthnRequest(final HttpServletRequest request, final IdpConfig idpConfig) {
		final String ipDSSODestination = getIPDSSODestination(idpConfig);
		final AuthnRequest authnRequest = buildSAMLObject(AuthnRequest.class);

		// this ensure that the message redirected is not too old
		authnRequest.setIssueInstant(new DateTime());

		// IDP url
		if (!isSet(ipDSSODestination)) {
			Logger.error(SamlUtils.class,
					"The ipDSSODestination is not set in the idp metadata, neither the configuration files");
			throw new DotSamlException("The property: "
					+ DotsamlPropertyName.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL.getPropertyName()
					+ " must be set on the host");
		}

		authnRequest.setDestination(ipDSSODestination);

		// Get the protocol from the user, or use a default one:
		// SAMLConstants.SAML2_ARTIFACT_BINDING_URI
		authnRequest.setProtocolBinding(
				DotsamlPropertiesService.getOptionString(idpConfig, DotsamlPropertyName.DOTCMS_SAML_PROTOCOL_BINDING));

		// this is the address that receives the SAML Assertion, after a
		// successful authentication on the IdP.
		authnRequest.setAssertionConsumerServiceURL(getAssertionConsumerEndpoint(request, idpConfig));

		// this is a uid or random id just to identified the response.
		authnRequest.setID(generateSecureRandomId());

		// id for the sender
		authnRequest.setIssuer(buildIssuer(idpConfig));

		authnRequest.setNameIDPolicy(buildNameIdPolicy(idpConfig));
		authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(idpConfig));
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setForceAuthn(
				DotsamlPropertiesService.getOptionBoolean(idpConfig, DotsamlPropertyName.DOTCMS_SAML_FORCE_AUTHN));

		return authnRequest;
	}

	/**
	 * Gets from the dotmarketing-config.properties the destination sso url
	 * 
	 * @return String
	 */
	public static String getIPDSSODestination(final IdpConfig idpConfig) {
		final String redirectIdentityProviderDestinationSSOURL = MetaDataHelper
				.getIdentityProviderDestinationSSOURL(idpConfig);

		// first check the meta data info., secondly the idpConfig
		return (null != redirectIdentityProviderDestinationSSOURL) ? redirectIdentityProviderDestinationSSOURL
				: DotsamlPropertiesService.getOptionString(idpConfig,
						DotsamlPropertyName.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SSO_URL);
	}

	/**
	 * Gets from the dotmarketing-config.properties the destination slo url
	 * 
	 * @return String
	 */
	public static String getIPDSLODestination(final IdpConfig idpConfig) {
		final String redirectIdentityProviderDestinationSLOURL = MetaDataHelper
				.getIdentityProviderDestinationSLOURL(idpConfig);

		// first check the meta data info., secondly the idpConfig
		return (null != redirectIdentityProviderDestinationSLOURL) ? redirectIdentityProviderDestinationSLOURL
				: DotsamlPropertiesService.getOptionString(idpConfig,
						DotsamlPropertyName.DOTCMS_SAML_IDENTITY_PROVIDER_DESTINATION_SLO_URL);
	}

	public static String getAssertionConsumerEndpoint(final HttpServletRequest request, final IdpConfig idpConfig) {
		final String assertionConsumerEndpoint = EndpointHelper.getAssertionConsumerEndpoint(idpConfig);

		// this is the same original request. Consequently where should be
		// redirected when the authentication is done.
		final StringBuilder builder = new StringBuilder(request.getRequestURI());

		if (null != request.getQueryString()) {
			builder.append('?').append(request.getQueryString());
		}

		return (null != assertionConsumerEndpoint) ? assertionConsumerEndpoint : builder.toString();
	}

	/**
	 * Generate the Random id
	 * 
	 * @return String
	 */
	public static String generateSecureRandomId() {
		return secureRandomIdGenerator.generateIdentifier();
	}

	/**
	 * Build the Id for the sender.
	 * 
	 * @return Issuer
	 */

	public static Issuer buildIssuer(final IdpConfig idpConfig) {
		final Issuer issuer = buildSAMLObject(Issuer.class);

		issuer.setValue(getSPIssuerValue(idpConfig));

		return issuer;
	}

	/**
	 * Get the od for the Issuer, it is the SP identifier on the IdP
	 * 
	 * @return String
	 */
	public static String getSPIssuerValue(final IdpConfig idpConfig) {
		// spIssuerURL is a required field. It should have value.
		return idpConfig.getSpIssuerURL();
	}

	/**
	 * Return the policy for the Name ID (which is the IdP identifier for the
	 * user)
	 * 
	 * @return NameIDPolicy
	 */
	public static NameIDPolicy buildNameIdPolicy(final IdpConfig idpConfig) {

		final NameIDPolicy nameIDPolicy = buildSAMLObject(NameIDPolicy.class);

		// True if you want that when the user does not exists, allows to create
		nameIDPolicy.setAllowCreate(DotsamlPropertiesService.getOptionBoolean(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_POLICY_ALLOW_CREATE));

		// todo: should set the SPNameQualifier

		// it supports several formats, such as Kerberos, email, Windows Domain
		// Qualified Name, etc.
		// “The transient identifier is a random identifier that does not have
		// any connection to the user.
		// A transient identifier will be different for every time the user
		// signs in.”
		nameIDPolicy.setFormat(DotsamlPropertiesService.getOptionString(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_NAME_ID_POLICY_FORMAT));

		return nameIDPolicy;
	}

	/**
	 * if(Properties.getBoolean(Constants.ADD_AUTHN_POLICYNAME,true)){ final
	 * NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
	 * NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
	 * nameIdPolicy.setFormat(Properties.getString(Constants.POLICYNAME_FORMAT,Constants.NAMEID_TRASIENT_FORMAT));
	 * nameIdPolicy.setSPNameQualifier(Properties.getString(Constants.POLICYNAME_SPNAMEQUALIFIER,Constants.ISSUER));
	 * nameIdPolicy.setAllowCreate(true);
	 * authRequest.setNameIDPolicy(nameIdPolicy); }
	 */

	// todo: keep in mind more than one authentication can be defined

	/**
	 * Build the Authentication context, with the login and password strategies
	 * 
	 * @return RequestedAuthnContext
	 */
	public static RequestedAuthnContext buildRequestedAuthnContext(final IdpConfig idpConfig) {
		final RequestedAuthnContext requestedAuthnContext = buildSAMLObject(RequestedAuthnContext.class);

		requestedAuthnContext.setComparison(getAuthnContextComparisonTypeEnumeration(idpConfig));

		final AuthnContextClassRef passwordAuthnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);

		passwordAuthnContextClassRef.setAuthnContextClassRef(DotsamlPropertiesService.getOptionString(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_AUTHN_CONTEXT_CLASS_REF));

		requestedAuthnContext.getAuthnContextClassRefs().add(passwordAuthnContextClassRef);

		return requestedAuthnContext;
	}

	/**
	 * Based on the configuration properties get the desire comparison type
	 * 
	 * @return AuthnContextComparisonTypeEnumeration
	 */
	public static AuthnContextComparisonTypeEnumeration getAuthnContextComparisonTypeEnumeration(
			final IdpConfig idpConfig) {
		AuthnContextComparisonTypeEnumeration comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.MINIMUM;

		final String enumName = DotsamlPropertiesService.getOptionString(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_AUTHN_COMPARISON_TYPE);

		if (isSet(enumName)) {
			if (AuthnContextComparisonTypeEnumeration.BETTER.toString().equalsIgnoreCase(enumName)) {
				comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.BETTER;
			} else if (AuthnContextComparisonTypeEnumeration.EXACT.toString().equalsIgnoreCase(enumName)) {
				comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.EXACT;
			} else if (AuthnContextComparisonTypeEnumeration.MAXIMUM.toString().equalsIgnoreCase(enumName)) {
				comparisonTypeEnumeration = AuthnContextComparisonTypeEnumeration.MAXIMUM;
			}
			// MINIMUN is not necessary since it is the default one.
		}

		return comparisonTypeEnumeration;
	}

	/**
	 * Get the Logout Identity Provider Destination
	 * 
	 * @return Endpoint
	 */
	public static Endpoint getIdentityProviderSLODestinationEndpoint(final IdpConfig idpConfig) {
		final SingleLogoutService endpoint = buildSAMLObject(SingleLogoutService.class);

		endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		endpoint.setLocation(getIPDSLODestination(idpConfig));

		return endpoint;
	}

	/**
	 * Get the Identity Provider Destination
	 * 
	 * @return Endpoint
	 */
	public static Endpoint getIdentityProviderDestinationEndpoint(final IdpConfig idpConfig) {
		final SingleSignOnService endpoint = buildSAMLObject(SingleSignOnService.class);

		endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		endpoint.setLocation(getIPDSSODestination(idpConfig));

		return endpoint;
	}

	/**
	 * Get the Assertion decrypted
	 *
	 * @param artifactResponse
	 *            {@link ArtifactResponse}
	 * @return Assertion
	 */
	public static Assertion getAssertion(final ArtifactResponse artifactResponse, final IdpConfig idpConfig) {
		final EncryptedAssertion encryptedAssertion = getEncryptedAssertion(artifactResponse);
		final Assertion assertion = decryptAssertion(encryptedAssertion, idpConfig); /// this
																						/// is
																						/// the
																						/// user
																						/// message
																						/// itself

		return assertion;
	}

	/**
	 * Get the Assertion decrypted
	 *
	 * @param response
	 *            {@link Response}
	 * @return Assertion
	 */
	public static Assertion getAssertion(final Response response, final IdpConfig idpConfig) {
		final EncryptedAssertion encryptedAssertion;
		Assertion assertion = null;

		if (DotsamlPropertiesService.getOptionBoolean(idpConfig,
				DotsamlPropertyName.DOTCMS_SAML_IS_ASSERTION_ENCRYPTED)) {
			encryptedAssertion = response.getEncryptedAssertions().get(0);
			assertion = decryptAssertion(encryptedAssertion, idpConfig); /// this
																			/// is
																			/// the
																			/// user
																			/// message
																			/// itself
		} else {
			assertion = response.getAssertions().get(0);
		}

		return assertion;
	}

	/**
	 * Just get the Encrypted assertion from the {@link ArtifactResponse}
	 *
	 * @param artifactResponse
	 *            {@link ArtifactResponse}
	 * @return EncryptedAssertion
	 */
	public static EncryptedAssertion getEncryptedAssertion(final ArtifactResponse artifactResponse) {
		final Response response = (Response) artifactResponse.getMessage();
		return response.getEncryptedAssertions().get(0);
	}

	/**
	 * Decrypt an {@link EncryptedAssertion}
	 *
	 * @param encryptedAssertion
	 *            {@link EncryptedAssertion}
	 * @return Assertion
	 */
	public static Assertion decryptAssertion(final EncryptedAssertion encryptedAssertion, final IdpConfig idpConfig) {
		Assertion assertion = null;
		final StaticKeyInfoCredentialResolver keyInfoCredentialResolver = new StaticKeyInfoCredentialResolver(
				getCredential(idpConfig));

		final Decrypter decrypter = new Decrypter(null, keyInfoCredentialResolver, new InlineEncryptedKeyResolver());

		try {
			decrypter.setRootInNewDocument(true);
			assertion = decrypter.decrypt(encryptedAssertion);
		} catch (DecryptionException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new DotSamlException(e.getMessage(), e);
		}

		return assertion;
	}

	private static void validateSignature(final Assertion assertion, final Collection<Credential> credentials)
			throws SignatureException {
		
		for (Credential credential : credentials) {
			try {
				SignatureValidator.validate(assertion.getSignature(), credential);

				return;
			} catch (SignatureException ignore) {
				Logger.info(SamlUtils.class, "Validation failed with credential: " + ignore.getMessage());
			}
		}

		throw new SignatureException("Signature cannot be validated");
	}

	private static void validateSignature(final Response response, final Collection<Credential> credentials)
			throws SignatureException {
		
		Logger.debug(SamlUtils.class, "validateSignature : credentials : " + ((credentials != null)?"has value":"is null"));
		Logger.debug(SamlUtils.class, "validateSignature : credentials : " + ((response != null)?"has value":"is null"));
		for (Credential credential : credentials) {
			try {
				Logger.debug(SamlUtils.class, "validateSignature : credential : " + ((credential != null)?"has value":"is null"));
				Logger.debug(SamlUtils.class, "validateSignature : response.getSignature : " + ((response.getSignature() != null)?"has value":"is null"));
				SignatureValidator.validate(response.getSignature(), credential);

				return;
			} catch (SignatureException ignore) {
				Logger.info(SamlUtils.class, "Validation failed with credential: " + ignore.getMessage());
			}
		}

		throw new SignatureException("Signature cannot be validated");
	}

	/**
	 * Does the verification of the assertion
	 *
	 * @param assertion
	 *            {@link Assertion}
	 */
	public static void verifyAssertionSignature(final Assertion assertion, final IdpConfig idpConfig) {
		final SAMLSignatureProfileValidator profileValidator;

		if (CredentialHelper.isVerifyAssertionSignatureNeeded(idpConfig) != assertion.isSigned()) {
			Logger.error(SamlUtils.class, "The assertion signatures do not match...");
			throw new DotSamlException("The SAML Assertion does not match");
		}

		// If unsigned, No need to go further.
		if (!CredentialHelper.isVerifyAssertionSignatureNeeded(idpConfig)) {
			Logger.debug(SamlUtils.class, "The verification assertion signature and status code was skipped.");
			return; // Exit
		}

		// Here on out we are checking signature
		try {
			if (CredentialHelper.isVerifySignatureProfileNeeded(idpConfig)) {
				Logger.debug(SamlUtils.class, "Doing Profile Validation");
				profileValidator = new SAMLSignatureProfileValidator();
				profileValidator.validate(assertion.getSignature());
				Logger.debug(SamlUtils.class, "Done Profile Validation");
			} else {
				Logger.debug(SamlUtils.class, "Skipping the Verify Signature Profile check");
			}

			// Ask on the config if the app wants signature validator
			if (CredentialHelper.isVerifySignatureCredentialsNeeded(idpConfig)) {
				if (null != MetaDataHelper.getSigningCredentials(idpConfig)) {
					Logger.debug(SamlUtils.class,
							"Validating the signatures: " + MetaDataHelper.getSigningCredentials(idpConfig));
					validateSignature(assertion, MetaDataHelper.getSigningCredentials(idpConfig));
					Logger.debug(SamlUtils.class, "Doing signatures validation");
				} else {
					Logger.debug(SamlUtils.class, "Validating the signature with a IdP Credentials ");
					SignatureValidator.validate(assertion.getSignature(), getIdPCredentials(idpConfig));
					Logger.debug(SamlUtils.class, "Done validation of the signature with a IdP Credentials ");
				}
			} else {
				Logger.debug(SamlUtils.class, "Skipping the Verify Signature Profile check");
			}

			Logger.debug(SamlUtils.class, "SAML Assertion signature verified");

		} catch (SignatureException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new DotSamlException(e.getMessage(), e);
		}
	}

	/**
	 * Does the verification of the assertion
	 *
	 * @param response
	 *            {@link Assertion}
	 */
	public static void verifyResponseSignature(final Response response, final IdpConfig idpConfig) {
		final SAMLSignatureProfileValidator profileValidator;

		// The check signature in dotCMS and IdP must match
		if (CredentialHelper.isVerifyResponseSignatureNeeded(idpConfig) != response.isSigned()) {
			Logger.error(SamlUtils.class, "The response signatures do not match...");
			throw new DotSamlException("The SAML Response does not match");
		}

		// If unsigned, No need to go further.
		if (!CredentialHelper.isVerifyResponseSignatureNeeded(idpConfig)) {
			Logger.debug(SamlUtils.class, "The verification response signature and status code was skipped.");
			return; // Exit
		}

		// Here on out we are checking signature
		try {
			if (CredentialHelper.isVerifySignatureProfileNeeded(idpConfig)) {
				Logger.debug(SamlUtils.class, "Doing Profile Validation");
				profileValidator = new SAMLSignatureProfileValidator();
				profileValidator.validate(response.getSignature());
				Logger.debug(SamlUtils.class, "Done Profile Validation");
			} else {
				Logger.debug(SamlUtils.class, "Skipping the Verify Signature Profile check");
			}

			// Ask on the config if the app wants signature validator
			if (CredentialHelper.isVerifySignatureCredentialsNeeded(idpConfig)) {
				if (null != MetaDataHelper.getSigningCredentials(idpConfig)) {
					Logger.debug(SamlUtils.class,
							"Validating the signatures: " + MetaDataHelper.getSigningCredentials(idpConfig));
					validateSignature(response, MetaDataHelper.getSigningCredentials(idpConfig));
					Logger.debug(SamlUtils.class, "Doing signatures validation");
				} else {
					Logger.debug(SamlUtils.class, "Validating the signature with a IdP Credentials ");
					SignatureValidator.validate(response.getSignature(), getIdPCredentials(idpConfig));
					Logger.debug(SamlUtils.class, "Done validation of the signature with a IdP Credentials ");
				}
			} else {
				Logger.debug(SamlUtils.class, "Skipping the Verify Signature Profile check");
			}

			Logger.debug(SamlUtils.class, "SAML Response signature verified");

		} catch (SignatureException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new DotSamlException(e.getMessage(), e);
		}
	}

	public static Credential createCredential(final IdpConfig idpConfig) {
		IdpConfigCredentialResolver resolver;
		final Criterion criterion;
		final CriteriaSet criteriaSet;
		final CredentialProvider customCredentialProvider = CredentialHelper
				.getServiceProviderCustomCredentialProvider(idpConfig);
		Credential credential = null;

		try {
			if (null != customCredentialProvider) {
				credential = customCredentialProvider.createCredential();
			} else {
				Logger.debug(SamlUtils.class, "Creating the credentials, using id: " + idpConfig.getId());

				resolver = new IdpConfigCredentialResolver();

				criterion = new EntityIdCriterion(idpConfig.getId());
				criteriaSet = new CriteriaSet();
				criteriaSet.add(criterion);
				credential = resolver.resolveSingle(criteriaSet);

				Logger.debug(SamlUtils.class, "Created the credentials: " + credential);
			}
		} catch (ResolverException resolverException) {
			Logger.error(SamlUtils.class, resolverException.getMessage(), resolverException);
			throw new DotSamlException("Something went wrong reading credentials", resolverException);
		}

		return credential;
	}

	/**
	 * Get the SP credential
	 * 
	 * @return Credential
	 */
	public static Credential getCredential(final IdpConfig idpConfig) {
		if (!credentialMap.containsKey(idpConfig.getSpEndpointHostname())) {
			final Credential credential = createCredential(idpConfig);

			if (null != credential) {
				credentialMap.put(idpConfig.getSpEndpointHostname(), credential);
			} else {
				Logger.error(SamlUtils.class,
						"The credential is null for the site: " + idpConfig.getSpEndpointHostname());

				throw new DotSamlException("The credential is null for the site: " + idpConfig.getSpEndpointHostname());
			}

		}

		return credentialMap.get(idpConfig.getSpEndpointHostname());
	}

	private static Credential createIdpCredential(final IdpConfig idpConfig) {
		KeyPair keyPair = null;
		final CredentialProvider customCredentialProvider = CredentialHelper
				.getIdProviderCustomCredentialProvider(idpConfig);
		Credential idpCredential = null;

		try {
			Logger.debug(SamlUtils.class, "Creating Idp credential");

			if (null != customCredentialProvider) {
				Logger.debug(SamlUtils.class, "Using custom credential provider");
				idpCredential = customCredentialProvider.createCredential();
			} else {
				Logger.debug(SamlUtils.class, "Using standard credential algorithm");
				// this fallback generates just a random keypair not very useful
				// to validate the signature.
				keyPair = KeySupport.generateKeyPair("RSA", 1024, null);
				idpCredential = CredentialSupport.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());
			}

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new DotSamlException(e.getMessage(), e);
		}

		return idpCredential;
	}

	public static Credential getIdPCredentials(final IdpConfig idpConfig) {
		if (!idpCredentialMap.containsKey(idpConfig)) {
			idpCredentialMap.put(idpConfig.getSpEndpointHostname(), createIdpCredential(idpConfig));
		}

		return idpCredentialMap.get(idpConfig.getSpEndpointHostname());
	}

	/**
	 * Convert to String an {@link XMLObject}
	 *
	 * @param object
	 *            {@link XMLObject}
	 * @return String
	 */
	public static String toXMLObjectString(final XMLObject object) {
		final Element element = (object instanceof SignableSAMLObject
				&& SignableSAMLObject.class.cast(object).isSigned() && object.getDOM() != null) ? object.getDOM()
						: toElement(object);

		return toElementString(element);
	}

	/**
	 * Convert to String an {@link Element}
	 *
	 * @param element
	 *            {@link Element}
	 * @return String
	 */
	public static String toElementString(final Element element) {

		final Transformer transformer;
		final StreamResult result;
		final DOMSource source;
		String xmlString = null;

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			result = new StreamResult(new StringWriter());
			source = new DOMSource(element);

			transformer.transform(source, result);
			xmlString = result.getWriter().toString();
		} catch (TransformerException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
		}

		return xmlString;
	}

	public static Element toElement(final XMLObject object) {
		final Marshaller out = marshallerFactory.getMarshaller(object);

		try {
			out.marshall(object);
		} catch (MarshallingException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
		}

		return object.getDOM();
	}

	/**
	 * Invoke a message handler chain
	 *
	 * @param handlerChain
	 *            {@link BasicMessageHandlerChain}
	 * @param context
	 *            MessageContext
	 */
	public static <T> void invokeMessageHandlerChain(final BasicMessageHandlerChain<T> handlerChain,
			final MessageContext<T> context) {
		try {
			handlerChain.initialize();
			handlerChain.doInvoke(context);
		} catch (ComponentInitializationException | MessageHandlerException e) {
			Logger.error(SamlUtils.class, e.getMessage(), e);
			throw new DotSamlException(e.getMessage(), e);
		}
	}

	/**
	 * Check if the File Path exists, can access and readIdpConfigs.
	 *
	 * @param properties
	 *            {@link Properties} (Key, Value) = (PropertyName, FilePath).
	 * @param filePathPropertyKeys
	 *            keys name that have File path in the value.
	 * @return
	 */
	public static Set<String> validateFiles(Properties properties, Set<String> filePathPropertyKeys) {
		Set<String> missingFiles = new HashSet<>();

		for (String fileField : filePathPropertyKeys) {
			Logger.debug(SamlUtils.class, "Validating the field: " + fileField);

			// If field is missing we don't need to validate.
			if (properties.getProperty(fileField) == null) {
				continue;
			}

			// Check if the file exists.
			String filePath = properties.getProperty(fileField);
			File file = new File(filePath);

			if (!file.exists() && !file.canRead()) {
				try {
					// Let's try with the URI.
					URI uri = new URI(filePath);
					file = new File(uri);

					if (!file.exists() && !file.canRead()) {
						missingFiles.add(filePath);
					}

				} catch (URISyntaxException e) {
					Logger.debug(SamlUtils.class, "Problem reading file from URI: " + filePath, e);
					missingFiles.add(fileField + ": " + filePath);
				} catch (Exception e) {
					Logger.debug(SamlUtils.class, "Problem reading file from URI: " + filePath, e);
					missingFiles.add(fileField + ": " + filePath + " (" + e.getMessage() + ")");
				}
			}
		}

		return missingFiles;
	}

	/**
	 * Validates that properties contains the keys required.
	 *
	 * @param properties
	 *            {@link Properties} (Key, Value) = (PropertyName, FilePath).
	 * @param keys
	 *            Required Property keys required to be in the samlProperties.
	 * @return
	 */
	public static Set<String> getMissingProperties(final Properties properties, final Set<String> keysRequired) {
		final Set<String> missingProperties = new HashSet<>(); // todo: make
																// this
																// immutable on
																// 4.x

		for (String keyRequired : keysRequired) {
			Logger.debug(SamlUtils.class, "Validating missing prop: " + keyRequired);

			if (properties.getProperty(keyRequired) == null || properties.getProperty(keyRequired).trim().equals("")) {
				missingProperties.add(keyRequired);
			}

		}

		return missingProperties;
	}
}
