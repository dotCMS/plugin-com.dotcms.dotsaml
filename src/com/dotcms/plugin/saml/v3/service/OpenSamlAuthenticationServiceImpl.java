package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.beans.AttributesBean;
import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.OptionalPropertiesHelper;
import com.dotcms.plugin.saml.v3.config.SamlSiteValidator;
import com.dotcms.plugin.saml.v3.exception.AttributesNotFoundException;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.exception.NotNullEmailAllowedException;
import com.dotcms.plugin.saml.v3.exception.SamlUnauthorizedException;
import com.dotcms.plugin.saml.v3.handler.AssertionResolverHandler;
import com.dotcms.plugin.saml.v3.handler.AssertionResolverHandlerFactory;
import com.dotcms.plugin.saml.v3.util.SiteIdpConfigResolver;

import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

import com.dotmarketing.business.*;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.*;
import com.dotmarketing.util.json.JSONException;

import com.liferay.portal.model.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import static com.dotcms.plugin.saml.v3.key.DotSamlConstants.*;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.*;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * Authentication with Open SAML
 * 
 * @author jsanca
 */
public class OpenSamlAuthenticationServiceImpl implements SamlAuthenticationService
{
	private static final long serialVersionUID = -7767096221297369583L;

	protected static final String NULL = "null";
	public static final String NO_REPLY = "no-reply";
	public static final String NO_REPLY_DOTCMS_COM = "@no-reply.dotcms.com";
	public static final String AT_SYMBOL = "@";
	public static final String AT_ = "_at_";

	protected final UserAPI userAPI;
	protected final RoleAPI roleAPI;
	protected final AssertionResolverHandlerFactory assertionResolverHandlerFactory;

	public OpenSamlAuthenticationServiceImpl()
	{
		this( APILocator.getUserAPI(), APILocator.getRoleAPI(), new AssertionResolverHandlerFactory() );
	}

	@VisibleForTesting
	protected OpenSamlAuthenticationServiceImpl( final UserAPI userAPI, final RoleAPI roleAPI, final AssertionResolverHandlerFactory assertionResolverHandlerFactory )
	{
		this.userAPI = userAPI;
		this.roleAPI = roleAPI;
		this.assertionResolverHandlerFactory = assertionResolverHandlerFactory;
	}

	private void addRole( final User user, final String roleKey, final boolean createRole, final boolean isSystem ) throws DotDataException
	{
		Role role = this.roleAPI.loadRoleByKey( roleKey );

		// create the role, in case it does not exist
		if ( role == null && createRole )
		{
			Logger.info( this, "Role not found. Creating Role with key: " + roleKey );
			role = createNewRole( roleKey, isSystem );
		}

		if ( null != role )
		{
			if ( !this.roleAPI.doesUserHaveRole( user, role ) )
			{
				this.roleAPI.addRoleToUser( role, user );
				Logger.debug( this, "Added role: " + role.getName() + " to user:" + user.getEmailAddress() );
			}
			else
			{
				Logger.debug( this, "The user: " + user.getEmailAddress() + " already has the role: " + role + ", so not added" );
			}
		}
		else
		{
			Logger.debug( this, "The role: " + roleKey + ", does not exists on dotCMS, not added to the user." );
		}
	}

	private void addRole( final User user, final String removeRolePrefix, final XMLObject roleObject ) throws DotDataException
	{
		// remove role prefix
		final String roleKey = ( isSet( removeRolePrefix ) ) ? roleObject.getDOM().getFirstChild().getNodeValue().replaceFirst( removeRolePrefix, StringUtils.EMPTY ) : roleObject.getDOM().getFirstChild().getNodeValue();

		addRole( user, roleKey, false, false );
	}

	private void addRoles( final User user, final AttributesBean attributesBean, final IdpConfig idpConfig )
	{
		final String buildRolesStrategy = this.getBuildRoles( idpConfig );

		Logger.debug( this, "Using the build roles Strategy: " + buildRolesStrategy );

		if ( !DOTCMS_SAML_BUILD_ROLES_NONE_VALUE.equalsIgnoreCase( buildRolesStrategy ) )
		{
			try
			{
				// remove previous roles
				if ( !DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE.equalsIgnoreCase( buildRolesStrategy ) )
				{
					Logger.debug( this, "Removing user previous roles" );
					this.roleAPI.removeAllRolesFromUser( user );
				}
				else
				{
					Logger.debug( this, "The buildRoles Strategy is: 'staticadd', so didn't remove any dotCMS existing role" );
				}

				this.handleRoles( user, attributesBean, idpConfig, buildRolesStrategy );
			}
			catch ( DotDataException e )
			{
				Logger.error( this, "Error creating user:" + e.getMessage(), e );
				throw new DotSamlException( e.getMessage() );
			}
		}
		else
		{
			Logger.info( this, "The build roles strategy is 'none', so not alter any user role" );
		}
	}

	private void addRolesFromIDP( final User user, final AttributesBean attributesBean, final IdpConfig idpConfig, final String buildRolesStrategy ) throws DotDataException
	{
		String role = null;
		final boolean includeIDPRoles = DOTCMS_SAML_BUILD_ROLES_ALL_VALUE.equalsIgnoreCase( buildRolesStrategy ) || DOTCMS_SAML_BUILD_ROLES_IDP_VALUE.equalsIgnoreCase( buildRolesStrategy );

		Logger.debug( this, "Including roles from IDP: " + includeIDPRoles + ", for the build roles Strategy: " + buildRolesStrategy );

		if ( includeIDPRoles && attributesBean.isAddRoles() && null != attributesBean.getRoles() && null != attributesBean.getRoles().getAttributeValues() && attributesBean.getRoles().getAttributeValues().size() > 0 )
		{
			final String removeRolePrefix = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_REMOVE_ROLES_PREFIX, StringUtils.EMPTY );
			final String[] rolePatterns = OptionalPropertiesHelper.getOptionStringArray( idpConfig, DOTCMS_SAML_INCLUDE_ROLES_PATTERN, null );

			Logger.debug( this, "Role Patterns: " + this.toString( rolePatterns ) + ", remove role prefix: " + removeRolePrefix );

			// add roles
			for ( XMLObject roleObject : attributesBean.getRoles().getAttributeValues() )
			{
				if ( null != rolePatterns && rolePatterns.length > 0 )
				{
					role = roleObject.getDOM().getFirstChild().getNodeValue();

					if ( !this.isValidRole( role, rolePatterns ) )
					{
						// when there are role filters and the current roles is not
						// a valid role, we have to filter it.

						Logger.debug( this, "Skipping the role: " + role );
						continue;
					}
					else
					{
						Logger.debug( this, "Role Patterns: " + this.toString( rolePatterns ) + ", remove role prefix: " + removeRolePrefix + ": true" );
					}
				}

				this.addRole( user, removeRolePrefix, roleObject );
			}
		}
		else
		{
			Logger.info( this, "Roles have been ignore by the build roles strategy: " + buildRolesStrategy + ", or roles have been not set from the idp!" );
		}
	}

	private boolean anyAttributeNullOrBlank( final AttributesBean originalAttributes )
	{
		return !UtilMethods.isSet( originalAttributes.getEmail() ) || !UtilMethods.isSet( originalAttributes.getFirstName() ) || !UtilMethods.isSet( originalAttributes.getLastName() );
	}

	/**
	 * Authentication with Open SAML 3 is basically a redirect to the IDP to
	 * show the login page to the user.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void authentication( final HttpServletRequest request, final HttpServletResponse response ) throws DotDataException, IOException, JSONException
	{
		final IdpConfig idpConfig = SiteIdpConfigResolver.getInstance().resolveIdpConfig( request );
		final MessageContext context = new MessageContext(); // main context
		final AuthnRequest authnRequest = buildAuthnRequest( request, idpConfig );

		context.setMessage( authnRequest );

		// peer entity (Idp to SP and viceversa)
		final SAMLPeerEntityContext peerEntityContext = context.getSubcontext( SAMLPeerEntityContext.class, true );
		// info about the endpoint of the peer entity
		final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext( SAMLEndpointContext.class, true );

		endpointContext.setEndpoint( getIdentityProviderDestinationEndpoint( idpConfig ) );

		this.setSignatureSigningParams( context, idpConfig );
		this.doRedirect( context, response, authnRequest );
	}

	/**
	 * Authentication with Open SAML 3 is basically a redirect to the IDP to
	 * show the login page to the user.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void authentication( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, IOException
	{
		final MessageContext context = new MessageContext(); // main context
		final AuthnRequest authnRequest = buildAuthnRequest( request, idpConfig );

		context.setMessage( authnRequest );

		// peer entity (Idp to SP and viceversa)
		final SAMLPeerEntityContext peerEntityContext = context.getSubcontext( SAMLPeerEntityContext.class, true );
		// info about the endpoint of the peer entity
		final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext( SAMLEndpointContext.class, true );

		endpointContext.setEndpoint( getIdentityProviderDestinationEndpoint( idpConfig ) );

		this.setSignatureSigningParams( context, idpConfig );
		this.doRedirect( context, response, authnRequest );
	}

	private String checkDefaultValue( final String lastNameForNullValue, final String logMessage, final String exceptionMessage )
	{
		if ( !UtilMethods.isSet( lastNameForNullValue ) )
		{
			throw new SamlUnauthorizedException( exceptionMessage );
		}

		Logger.info( this, logMessage );

		return lastNameForNullValue;
	}

	private AttributesBean checkDefaultValues( final AttributesBean originalAttributes, final String firstNameField, final String firstNameForNullValue, final String lastNameField, final String lastNameForNullValue, final boolean allowNullEmail )
	{
		final AttributesBean.Builder attrBuilder = new AttributesBean.Builder();

		attrBuilder.nameID( originalAttributes.getNameID() );
		attrBuilder.roles( originalAttributes.getRoles() );
		attrBuilder.addRoles( originalAttributes.isAddRoles() );

		if ( !UtilMethods.isSet( originalAttributes.getEmail() ) )
		{
			attrBuilder.email( this.createNoReplyEmail( originalAttributes.getNameID().getValue(), allowNullEmail ) );
		}
		else
		{
			attrBuilder.email( originalAttributes.getEmail() );
		}

		if ( !UtilMethods.isSet( originalAttributes.getFirstName() ) )
		{
			attrBuilder.firstName( checkDefaultValue( firstNameForNullValue, firstNameField + " attribute is null", firstNameField + " is null and the default is null too" ) );
		}
		else
		{
			attrBuilder.firstName( originalAttributes.getFirstName() );
		}

		if ( !UtilMethods.isSet( originalAttributes.getLastName() ) )
		{
			attrBuilder.lastName( checkDefaultValue( lastNameForNullValue, lastNameField + " attribute is null", lastNameField + " is null and the default is null too" ) );
		}
		else
		{
			attrBuilder.lastName( originalAttributes.getLastName() );
		}

		return attrBuilder.build();
	}

	private Role createNewRole( String roleKey, boolean isSystem ) throws DotDataException
	{
		Role role = new Role();
		role.setName( roleKey );
		role.setRoleKey( roleKey );
		role.setEditUsers( true );
		role.setEditPermissions( false );
		role.setEditLayouts( false );
		role.setDescription( "" );
		role.setId( UUIDGenerator.generateUuid() );

		// Setting SYSTEM role as a parent
		role.setSystem( isSystem );
		Role parentRole = roleAPI.loadRoleByKey( Role.SYSTEM );
		role.setParent( parentRole.getId() );

		String date = DateUtil.getCurrentDate();

		ActivityLogger.logInfo( ActivityLogger.class, getClass() + " - Adding Role", "Date: " + date + "; " + "Role:" + roleKey );
		AdminLogger.log( AdminLogger.class, getClass() + " - Adding Role", "Date: " + date + "; " + "Role:" + roleKey );

		try
		{
			role = roleAPI.save( role, role.getId() );
		}
		catch ( DotDataException | DotStateException e )
		{
			ActivityLogger.logInfo( ActivityLogger.class, getClass() + " - Error Adding Role", "Date: " + date + ";  " + "Role:" + roleKey );
			AdminLogger.log( AdminLogger.class, getClass() + " - Error Adding Role", "Date: " + date + ";  " + "Role:" + roleKey );
			throw e;
		}

		return role;
	}

	protected User createNewUser( final User systemUser, final AttributesBean attributesBean )
	{
		User user = null;

		try
		{
			user = this.userAPI.createUser( attributesBean.getNameID().getValue(), attributesBean.getEmail() );

			user.setFirstName( attributesBean.getFirstName() );
			user.setLastName( attributesBean.getLastName() );
			user.setActive( true );

			user.setCreateDate( new Date() );
			user.setPassword( PublicEncryptionFactory.digestString( UUIDGenerator.generateUuid() + "/" + UUIDGenerator.generateUuid() ) );
			user.setPasswordEncrypted( true );

			this.userAPI.save( user, systemUser, false );
			Logger.debug( this, "new user created. email: " + attributesBean.getEmail() );
		}
		catch ( Exception e )
		{
			Logger.error( this, "Error creating user:" + e.getMessage(), e );
			throw new DotSamlException( e.getMessage() );
		}

		return user;
	}

	private String createNoReplyEmail( final String nameId, final boolean allowNullEmail )
	{
		if ( !allowNullEmail )
		{
			throw new NotNullEmailAllowedException( "The email is null and it is not allowed" );
		}

		Logger.info( this, "The userid : " + nameId + " has the email attribute null, creating a new one" );

		final String emailValue = new StringBuilder( NO_REPLY ).append( sanitizeNameId( nameId ) ).append( NO_REPLY_DOTCMS_COM ).toString();

		Logger.debug( this, "For the userid : " + nameId + " the generated email is: " + emailValue );

		return emailValue;
	}

	// this makes the redirect to the IdP
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private void doRedirect( final MessageContext context, final HttpServletResponse response, final XMLObject xmlObject )
	{
		final HTTPRedirectDeflateEncoder encoder;

		try
		{
			encoder = new HTTPRedirectDeflateEncoder();

			encoder.setMessageContext( context );
			encoder.setHttpServletResponse( response );

			encoder.initialize();

			Logger.debug( this, "XMLObject: " + toXMLObjectString( xmlObject ) );
			Logger.debug( this, "Redirecting to IDP" );

			encoder.encode();
		}
		catch ( ComponentInitializationException | MessageEncodingException e )
		{
			Logger.error( this, e.getMessage(), e );
			throw new DotSamlException( e.getMessage(), e );
		}

	}

	private AttributesBean doubleCheckAttributes( final AttributesBean originalAttributes, final String firstNameField, final String firstNameForNullValue, final String lastNameField, final String lastNameForNullValue, final boolean allowNullEmail )
	{
		return ( this.anyAttributeNullOrBlank( originalAttributes ) ) ? this.checkDefaultValues( originalAttributes, firstNameField, firstNameForNullValue, lastNameField, lastNameForNullValue, allowNullEmail ) : originalAttributes;
	}

	private String getBuildRoles( final IdpConfig idpConfig )
	{
		final String buildRolesStrategy = OptionalPropertiesHelper.getOptionString( idpConfig, DOTCMS_SAML_BUILD_ROLES, DOTCMS_SAML_BUILD_ROLES_ALL_VALUE );

		return SamlSiteValidator.checkBuildRoles( buildRolesStrategy ) ? buildRolesStrategy : this.getDefaultBuildRoles( buildRolesStrategy );
	}

	private String getDefaultBuildRoles( final String invalidBuildRolesStrategy )
	{
		Logger.info( this, "The build.roles: " + invalidBuildRolesStrategy + ", is invalid; using as default: " + DOTCMS_SAML_BUILD_ROLES_ALL_VALUE );

		return DOTCMS_SAML_BUILD_ROLES_ALL_VALUE;
	}

	/**
	 * When the authentication is performed and redirected to SO (DotCMS) you
	 * can call this method. If the request contains a parameter called SAMLart,
	 * will try to get the {@link org.opensaml.saml.saml2.core.Assertion} with
	 * the user information via the Resolver Implementation. - If the user
	 * exists, will just return the instance of it. - If the user does not
	 * exists on DotCMS will create a new one - If the existing user is active
	 * will also populate the roles. Note: if the parameter "SAMLart" does not
	 * exists, will return null. In addition this method is receiving the
	 * session, the reason of that is to store the SAML_SESSION_INDEX and the
	 * SAML_NAME_ID with them we can
	 *
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @param loginHttpSession {@link HttpSession} session to store the
	 * @return User
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	@Override
	public User getUser( final HttpServletRequest request, final HttpServletResponse response, final HttpSession loginHttpSession, final IdpConfig idpConfig ) throws DotDataException, JSONException, IOException
	{
		User user = null;
		final Assertion assertion;

		if ( this.isValidSamlRequest( request, response, idpConfig ) )
		{
			assertion = this.resolveAssertion( request, response, idpConfig );

			Logger.debug( this, "Resolved assertion: " + assertion );

			user = this.resolveUser( assertion, idpConfig );

			Logger.debug( this, "Resolved user: " + user );

			if ( null != loginHttpSession && null != user && null != assertion )
			{
				final String samlSessionIndex = getSessionIndex( assertion );

				if ( null != samlSessionIndex )
				{
					Logger.debug( this, "SAMLSessionIndex: " + samlSessionIndex );
					loginHttpSession.setAttribute( idpConfig.getId() + SAML_SESSION_INDEX, samlSessionIndex );
					loginHttpSession.setAttribute( idpConfig.getId() + SAML_NAME_ID, assertion.getSubject().getNameID() );
					Logger.debug( this, "Already set the session index with key:" + ( idpConfig.getId() + SAML_SESSION_INDEX ) + " and value" + loginHttpSession.getAttribute( idpConfig.getId() + SAML_SESSION_INDEX ) );
					Logger.debug( this, "Already set the name id with key:" + ( idpConfig.getId() + SAML_NAME_ID ) + " and value" + loginHttpSession.getAttribute( idpConfig.getId() + SAML_NAME_ID ) );
				}

			}

		}

		return user;
	}

	/**
	 * When the authentication is performed and redirected to SO (DotCMS) you
	 * can call this method. If the request contains a parameter called AMLart,
	 * will try to get the {@link org.opensaml.saml.saml2.core.Assertion} with
	 * the user information via the Resolver Implementation. - If the user
	 * exists, will just return the instance of it. - If the user does not
	 * exists on DotCMS will create a new one - If the existing user is active
	 * will also populate the roles. Note: if the parameter "SAMLart" does not
	 * exists, will return null.
	 * 
	 * @param request {@link HttpServletRequest}
	 * @param response {@link HttpServletResponse}
	 * @return User
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws DotDataException 
	 */
	@Override
	public User getUser( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, JSONException, IOException
	{
		User user = null;
		final Assertion assertion;

		if ( this.isValidSamlRequest( request, response, idpConfig ) )
		{
			assertion = this.resolveAssertion( request, response, idpConfig );

			Logger.debug( this, "Resolved assertion: " + assertion );

			user = this.resolveUser( assertion, idpConfig );

			Logger.debug( this, "Resolved user: " + user );
		}

		return user;
	}

	private void handleRoles( final User user, final AttributesBean attributesBean, final IdpConfig idpConfig, final String buildRolesStrategy ) throws DotDataException
	{
		this.addRolesFromIDP( user, attributesBean, idpConfig, buildRolesStrategy );

		// Add SAML User role
		addRole( user, OptionalPropertiesHelper.getOptionString( idpConfig, DOTCMS_SAML_USER_ROLE, "SAML User" ), true, true );
		Logger.debug( this, "Default SAML User role has been assigned" );

		// the only strategy that does not include the saml user role is the "idp"
		if ( !DOTCMS_SAML_BUILD_ROLES_IDP_VALUE.equalsIgnoreCase( buildRolesStrategy ) )
		{
			// Add DOTCMS_SAML_OPTIONAL_USER_ROLE
			if ( OptionalPropertiesHelper.getOptionString( idpConfig, DOTCMS_SAML_OPTIONAL_USER_ROLE, null ) != null )
			{
				addRole( user, OptionalPropertiesHelper.getOptionString( idpConfig, DOTCMS_SAML_OPTIONAL_USER_ROLE, null ), false, false );
				Logger.debug( this, "Optional user role: " + OptionalPropertiesHelper.getOptionString( idpConfig, DOTCMS_SAML_OPTIONAL_USER_ROLE, null ) + " has been assigned" );
			}
		}
		else
		{
			Logger.info( this, "The build roles strategy is 'idp' so not any saml_user_role added" );
		}

	}

	private boolean isValidRole( final String role, final String[] rolePatterns )
	{
		boolean isValidRole = false;

		if ( null != rolePatterns )
		{
			for ( String rolePattern : rolePatterns )
			{
				Logger.debug( this, "Is Valid Role, role: " + role + ", pattern: " + rolePattern );
				isValidRole |= this.match( role, rolePattern );
			}
		}
		else
		{
			// if not pattern, role is valid.
			isValidRole = true;
		}

		return isValidRole;
	}

	// if the SAML_ART_PARAM_KEY parameter is in the request, it is a valid SAML request
	@Override
	public boolean isValidSamlRequest( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig )
	{
		final AssertionResolverHandler assertionResolverHandler = this.assertionResolverHandlerFactory.getAssertionResolverForSite( idpConfig );

		return assertionResolverHandler.isValidSamlRequest( request, response, idpConfig );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public void logout( final HttpServletRequest request, final HttpServletResponse response, final NameID nameID, final String sessionIndexValue, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException
	{
		final MessageContext context = new MessageContext(); // main context
		final LogoutRequest logoutRequest = buildLogoutRequest( idpConfig, nameID, sessionIndexValue );

		context.setMessage( logoutRequest );

		// peer entity (Idp to SP and viceversa)
		final SAMLPeerEntityContext peerEntityContext = context.getSubcontext( SAMLPeerEntityContext.class, true );
		// info about the endpoint of the peer entity
		final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext( SAMLEndpointContext.class, true );

		endpointContext.setEndpoint( getIdentityProviderSLODestinationEndpoint( idpConfig ) );

		this.setSignatureSigningParams( context, idpConfig );
		this.doRedirect( context, response, logoutRequest );
	}

	private boolean match( final String role, final String rolePattern )
	{
		String uftRole = null;

		try
		{
			uftRole = URLDecoder.decode( role, "UTF-8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			uftRole = role;
		}

		return RegEX.contains( uftRole, rolePattern );
	}

	@Override
	public Assertion resolveAssertion( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig ) throws DotDataException, IOException, JSONException
	{
		final AssertionResolverHandler assertionResolverHandler = this.assertionResolverHandlerFactory.getAssertionResolverForSite( idpConfig );

		return assertionResolverHandler.resolveAssertion( request, response, idpConfig );
	}

	// resolve the attributes from the assertion resolved from the OpenSaml artifact resolver via
	protected AttributesBean resolveAttributes( final Assertion assertion, final IdpConfig idpConfig ) throws AttributesNotFoundException
	{
		final String emailField = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_EMAIL_ATTRIBUTE, "mail" );
		final String firstNameField = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_FIRSTNAME_ATTRIBUTE, "givenName" );
		final String lastNameField = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_LASTNAME_ATTRIBUTE, "sn" );
		final String rolesField = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_ROLES_ATTRIBUTE, "authorizations" );
		final String firstNameForNullValue = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE, null );
		final String lastNameForNullValue = OptionalPropertiesHelper.getOptionString( idpConfig, DOT_SAML_LASTNAME_ATTRIBUTE_NULL_VALUE, null );
		final boolean allowNullEmail = OptionalPropertiesHelper.getOptionBoolean( idpConfig, DOT_SAML_EMAIL_ATTRIBUTE_ALLOW_NULL, true );

		final String customConfiguration = new StringBuilder( DOT_SAML_EMAIL_ATTRIBUTE ).append( "=" ).append( emailField ).append( "," ).append( DOT_SAML_FIRSTNAME_ATTRIBUTE ).append( "=" ).append( firstNameField ).append( "," ).append( DOT_SAML_LASTNAME_ATTRIBUTE ).append( "=" ).append( lastNameField ).append( "," ).append( DOT_SAML_ROLES_ATTRIBUTE ).append( "=" ).append( rolesField ).toString();

		final AttributesBean.Builder attrBuilder = new AttributesBean.Builder();

		validateAttributes( assertion );

		final String nameId = assertion.getSubject().getNameID().getValue();

		Logger.debug( this, "Resolving attributes - Name ID : " + assertion.getSubject().getNameID().getValue() );

		attrBuilder.nameID( assertion.getSubject().getNameID() );

		Logger.debug( this, "Elements of type AttributeStatement in assertion : " + assertion.getAttributeStatements().size() );

		assertion.getAttributeStatements().forEach( attributeStatement ->
		{
			Logger.debug( this, "Attribute Statement - local name: " + AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME + ", type: " + AttributeStatement.TYPE_LOCAL_NAME + ", number of attributes: " + attributeStatement.getAttributes().size() );

			attributeStatement.getAttributes().forEach( attribute ->
			{
				Logger.debug( this, "Attribute - friendly name: " + attribute.getFriendlyName() + ", name: " + attribute.getName() + ", type: " + Attribute.TYPE_LOCAL_NAME + ", number of values: " + attribute.getAttributeValues().size() );

				if ( ( attribute.getName() != null && attribute.getName().equals( emailField ) ) || ( attribute.getFriendlyName() != null && attribute.getFriendlyName().equals( emailField ) ) )
				{
					this.resolveEmail( emailField, attrBuilder, attribute, nameId, allowNullEmail );
				}
				else if ( ( attribute.getName() != null && attribute.getName().equals( lastNameField ) ) || ( attribute.getFriendlyName() != null && attribute.getFriendlyName().equals( lastNameField ) ) )
				{
					Logger.debug( this, "Resolving attribute - LastName : " + lastNameField );

					final String lastName = ( UtilMethods.isSet( attribute.getAttributeValues().get( 0 ).getDOM().getFirstChild().getNodeValue() ) ) ? attribute.getAttributeValues().get( 0 ).getDOM().getFirstChild().getNodeValue() : checkDefaultValue( lastNameForNullValue, lastNameField + " attribute is null", lastNameField + " is null and the default is null too" );

					attrBuilder.lastName( lastName );

					Logger.debug( this, "Resolved attribute - lastName : " + attrBuilder.getLastName() );
				}
				else if ( ( attribute.getName() != null && attribute.getName().equals( firstNameField ) ) || ( attribute.getFriendlyName() != null && attribute.getFriendlyName().equals( firstNameField ) ) )
				{
					Logger.debug( this, "Resolving attribute - firstName : " + firstNameField );

					final String firstName = ( UtilMethods.isSet( attribute.getAttributeValues().get( 0 ).getDOM().getFirstChild().getNodeValue() ) ) ? attribute.getAttributeValues().get( 0 ).getDOM().getFirstChild().getNodeValue() : checkDefaultValue( firstNameForNullValue, firstNameField + " attribute is null", firstNameField + " is null and the default is null too" );

					attrBuilder.firstName( firstName );

					Logger.debug( this, "Resolved attribute - firstName : " + attrBuilder.getFirstName() );
				}
				else if ( ( attribute.getName() != null && attribute.getName().equals( rolesField ) ) || ( attribute.getFriendlyName() != null && attribute.getFriendlyName().equals( rolesField ) ) )
				{
					Logger.debug( this, "Resolving attribute - roles : " + rolesField );
					attrBuilder.addRoles( true ).roles( attribute );
					Logger.debug( this, "Resolving attributes - roles : " + attribute );
				}
				else
				{
					final String attributeName = attribute.getName();
					Logger.warn( this, attributeName + " attribute did not match any user property on the idpConfig: " + customConfiguration );
				}
			} );
		} );

		return this.doubleCheckAttributes( attrBuilder.build(), firstNameField, firstNameForNullValue, lastNameField, lastNameForNullValue, allowNullEmail );
	}

	private void resolveEmail( final String emailField, final AttributesBean.Builder attributesBuilder, final Attribute attribute, final String nameId, final boolean allowNullEmail )
	{
		Logger.debug( this, "Resolving attribute - Email : " + emailField );

		String emailValue = attribute.getAttributeValues().get( 0 ).getDOM().getFirstChild().getNodeValue();

		emailValue = ( !UtilMethods.isSet( emailValue ) ) ? createNoReplyEmail( nameId, allowNullEmail ) : emailValue;

		attributesBuilder.email( emailValue );

		Logger.debug( this, "Resolved attribute - Email : " + attributesBuilder.getEmail() );
	}

	// Gets the attributes from the Assertion, based on the attributes
	// see if the user exists return it from the dotCMS records, if does not exist then, tries to create it.
	// the existing or created user, will be updated the roles if they present on the assertion.
	protected User resolveUser( final Assertion assertion, final IdpConfig idpConfig )
	{
		User systemUser = null;
		User user = null;
		AttributesBean attributesBean = null;

		try
		{
			attributesBean = this.resolveAttributes( assertion, idpConfig );

			Logger.debug( this, "Validating user - " + attributesBean );

			systemUser = this.userAPI.getSystemUser();

			user = this.userAPI.loadUserById( attributesBean.getNameID().getValue(), systemUser, false );
		}
		catch ( AttributesNotFoundException e )
		{
			Logger.error( this, e.getMessage() );
			return null;
		}
		catch ( NoSuchUserException e )
		{
			Logger.error( this, "No matching user, creating" );
			user = null;
		}
		catch ( Exception e )
		{
			Logger.error( this, "Unknown exception", e );
			user = null;
		}

		if ( null == user )
		{
			// if user does not exists, create a new one.
			user = this.createNewUser( systemUser, attributesBean );
		}
		else
		{
			// update it, since exists
			user = this.updateUser( user, systemUser, attributesBean );
		}

		if ( user.isActive() )
		{
			this.addRoles( user, attributesBean, idpConfig );
		}
		else
		{
			Logger.info( this, "The user " + user.getEmailAddress() + " is not active, not roles added" );
		}

		return user;
	}

	private String sanitizeNameId( final String nameId )
	{
		return StringUtils.replace( nameId, AT_SYMBOL, AT_ );
	}

	@SuppressWarnings( "rawtypes" )
	private void setSignatureSigningParams( final MessageContext context, final IdpConfig idpConfig )
	{
		final SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();

		signatureSigningParameters.setSigningCredential( getCredential( idpConfig ) );
		signatureSigningParameters.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256 );

		context.getSubcontext( SecurityParametersContext.class, true ).setSignatureSigningParameters( signatureSigningParameters );
	}

	private String toString( String[] rolePatterns )
	{
		return null == rolePatterns ? NULL : Arrays.asList( rolePatterns ).toString();
	}

	private User updateUser( final User user, final User systemUser, final AttributesBean attributesBean )
	{
		try
		{
			user.setEmailAddress( attributesBean.getEmail() );
			user.setFirstName( attributesBean.getFirstName() );
			user.setLastName( attributesBean.getLastName() );

			this.userAPI.save( user, systemUser, false );
			Logger.debug( this, "User updated. email: " + attributesBean.getEmail() );
		}
		catch ( Exception e )
		{
			Logger.error( this, "Error creating user:" + e.getMessage(), e );
			throw new DotSamlException( e.getMessage() );
		}

		return user;
	}

	protected void validateAttributes( Assertion assertion ) throws AttributesNotFoundException
	{
		if ( assertion == null || assertion.getAttributeStatements() == null || assertion.getAttributeStatements().isEmpty() || assertion.getSubject() == null || assertion.getSubject().getNameID() == null || assertion.getSubject().getNameID().getValue().isEmpty() )
		{
			throw new AttributesNotFoundException( "No attributes found" );
		}
	}
}
