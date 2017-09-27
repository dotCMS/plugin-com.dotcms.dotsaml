package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.exception.AttributesNotFoundException;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.handler.AssertionResolverHandler;
import com.dotcms.plugin.saml.v3.handler.AssertionResolverHandlerFactory;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.business.*;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.*;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.*;
import static com.dotcms.plugin.saml.v3.SamlUtils.*;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * Authentication with Open SAML
 * @author jsanca
 */
public class OpenSamlAuthenticationServiceImpl implements SamlAuthenticationService {


    protected static final String NULL = "null";
    public static final String NO_REPLY = "no-reply";
    public static final String NO_REPLY_DOTCMS_COM = "@no-reply.dotcms.com";
    public static final String AT_SYMBOL = "@";
    public static final String AT_ = "_at_";
    protected final UserAPI userAPI;
    protected final RoleAPI roleAPI;
    protected final AssertionResolverHandlerFactory assertionResolverHandlerFactory;

    public OpenSamlAuthenticationServiceImpl() {

        this(APILocator.getUserAPI(), APILocator.getRoleAPI(), new AssertionResolverHandlerFactory());
    }

    @VisibleForTesting
    protected OpenSamlAuthenticationServiceImpl(final UserAPI userAPI,
                                                final RoleAPI roleAPI,
                                                final AssertionResolverHandlerFactory assertionResolverHandlerFactory) {

        this.userAPI    = userAPI;
        this.roleAPI    = roleAPI;
        this.assertionResolverHandlerFactory =
                assertionResolverHandlerFactory;
    }

    /**
     * Authentication with Open SAML 3 is basically a redirect to the IDP to show the login page to the user.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @Override
    public void authentication(final HttpServletRequest request,
                               final HttpServletResponse response,
                               final String siteName) {

        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver)InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration configuration = resolver.resolveConfiguration(request);
        final MessageContext context       = new MessageContext(); // main context
        final AuthnRequest authnRequest  = buildAuthnRequest(request, configuration);

        context.setMessage(authnRequest);

        final SAMLPeerEntityContext peerEntityContext = // peer entity (Idp to SP and viceversa)
                context.getSubcontext(SAMLPeerEntityContext.class, true);
        final SAMLEndpointContext endpointContext = // info about the endpoint of the peer entity
                peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);

        endpointContext.setEndpoint(
                getIdentityProviderDestinationEndpoint(configuration));

        this.setSignatureSigningParams(context, configuration);
        this.doRedirect(context, response, authnRequest);
    } // authentication.


    public void logout(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final NameID nameID,
                       final String sessionIndexValue,
                       final String siteName) {

        final SiteConfigurationResolver resolver = (SiteConfigurationResolver)InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration configuration = resolver.resolveConfiguration(request);
        final MessageContext context      = new MessageContext(); // main context
        final LogoutRequest logoutRequest = buildLogoutRequest(configuration, nameID, sessionIndexValue);

        context.setMessage(logoutRequest);
        final SAMLPeerEntityContext peerEntityContext = // peer entity (Idp to SP and viceversa)
                context.getSubcontext(SAMLPeerEntityContext.class, true);
        final SAMLEndpointContext endpointContext = // info about the endpoint of the peer entity
                peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);

        endpointContext.setEndpoint(
                getIdentityProviderSLODestinationEndpoint(configuration));

        this.setSignatureSigningParams(context, configuration);
        this.doRedirect(context, response, logoutRequest);
    } // logout.

    /**
     * When the authentication is performed and redirected to SO (DotCMS) you can call this method.
     * If the request contains a parameter called AMLart, will try to get the {@link org.opensaml.saml.saml2.core.Assertion}
     * with the user information via the Resolver Implementation.
     *
     * - If the user exists, will just return the instance of it.
     *
     * - If the user does not exists on DotCMS will create a new one
     *
     * - If the existing user is active will also populate the roles.
     *
     * Note: if the parameter "SAMLart" does not exists, will return null.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return User
     */
    @Override
    public User getUser(final HttpServletRequest request,
                        final HttpServletResponse response,
                        final String siteName) {

        User user = null;
        final Assertion assertion;
        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver)InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration             configuration = resolver.resolveConfiguration(request);
        Subject subject                               = null;

        if (this.isValidSamlRequest (request, response, siteName)) {

            assertion = this.resolveAssertion(request, response, siteName);

            Logger.debug (this, "Resolved assertion: " + assertion);

            user      = this.resolveUser(assertion, configuration);

            Logger.debug (this, "Resolved user: " + user);
        }

        return user;
    } // getUser.

    /**
     * When the authentication is performed and redirected to SO (DotCMS) you can call this method.
     * If the request contains a parameter called SAMLart, will try to get the {@link org.opensaml.saml.saml2.core.Assertion}
     * with the user information via the Resolver Implementation.
     *
     * - If the user exists, will just return the instance of it.
     *
     * - If the user does not exists on DotCMS will create a new one
     *
     * - If the existing user is active will also populate the roles.
     *
     * Note: if the parameter "SAMLart" does not exists, will return null.
     *
     * In addition this method is receiving the session, the reason of that is to store the SAML_SESSION_INDEX and the SAML_NAME_ID
     * with them we can
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @param loginHttpSession {@link HttpSession} session to store the
     * @return User
     */
    @Override
    public User getUser(final HttpServletRequest  request,
                        final HttpServletResponse response,
                        final HttpSession         loginHttpSession,
                        final String siteName) {

        User user = null;
        final Assertion assertion;
        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver)InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration             configuration = resolver.resolveConfiguration(request);

        if (this.isValidSamlRequest (request, response, siteName)) {

            assertion = this.resolveAssertion(request, response, siteName);

            Logger.debug (this, "Resolved assertion: " + assertion);

            user      = this.resolveUser(assertion, configuration);

            Logger.debug (this, "Resolved user: " + user);

            if (null != loginHttpSession && null != user && null != assertion) {

                final String samlSessionIndex = getSessionIndex(assertion);

                if (null != samlSessionIndex) {

                    Logger.debug (this, "SAMLSessionIndex: " + samlSessionIndex);
                    loginHttpSession.setAttribute(configuration.getSiteName()+SAML_SESSION_INDEX, samlSessionIndex);
                    loginHttpSession.setAttribute(configuration.getSiteName()+SAML_NAME_ID,       assertion.getSubject().getNameID());
                    Logger.debug (this, "Already set the session index with key:" +
                            (configuration.getSiteName()+SAML_SESSION_INDEX) + " and value" +
                            loginHttpSession.getAttribute(configuration.getSiteName()+SAML_SESSION_INDEX));
                    Logger.debug (this, "Already set the name id with key:" +
                            (configuration.getSiteName()+SAML_NAME_ID) + " and value" +
                            loginHttpSession.getAttribute(configuration.getSiteName()+SAML_NAME_ID));
                }
            }
        }

        return user;
    } // getUser.

    private boolean isValidRole (final String role, final String [] rolePatterns) {

        boolean isValidRole = false;

        if (null != rolePatterns) {

            for (String rolePattern : rolePatterns) {

                Logger.debug(this, "Is Valid Role, role: " + role +
                                ", pattern: " + rolePattern);
                isValidRole |= this.match(role, rolePattern);
            }
        } else {

            isValidRole = true; // if not pattern, role is valid.
        }

        return isValidRole;
    } // isValidRole.

    private boolean match (final String role, final String rolePattern) {

        String uftRole = null;

        try {

            uftRole = URLDecoder.decode(role, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            uftRole = role;
        }

        return RegEX.contains(uftRole, rolePattern);
    } // match.


    // resolve the attributes from the assertion resolved from the OpenSaml artifact resolver via
    protected AttributesBean resolveAttributes (final Assertion assertion, final Configuration configuration) throws AttributesNotFoundException {

        final String emailField       = configuration.getStringProperty
                (DOT_SAML_EMAIL_ATTRIBUTE, "mail");
        final String firstNameField   = configuration.getStringProperty
                (DOT_SAML_FIRSTNAME_ATTRIBUTE, "givenName");
        final String lastNameField    = configuration.getStringProperty
                (DOT_SAML_LASTNAME_ATTRIBUTE, "sn");
        final String rolesField       = configuration.getStringProperty
                (DOT_SAML_ROLES_ATTRIBUTE, "authorizations");
        final String firstNameForNullValue = configuration.getStringProperty
                (DOT_SAML_FIRSTNAME_ATTRIBUTE_NULL_VALUE, null);
        final String lastNameForNullValue = configuration.getStringProperty
                (DOT_SAML_LASTNAME_ATTRIBUTE_NULL_VALUE, null);

        final AttributesBean.Builder attrBuilder = new AttributesBean.Builder();

        validateAttributes(assertion);

        final String nameId = assertion.getSubject().getNameID().getValue();
        Logger.debug(this, "Resolving attributes - Name ID : " + assertion.getSubject().getNameID().getValue());
        attrBuilder.nameID(assertion.getSubject().getNameID());
        
        Logger.debug(this, "Elements of type AttributeStatement in assertion : " + assertion.getAttributeStatements().size());

        assertion.getAttributeStatements().forEach(attributeStatement -> {
        	
        	Logger.debug(this, "Attribute Statement - local name: " + attributeStatement.DEFAULT_ELEMENT_LOCAL_NAME +  ", type: " 
        			+ attributeStatement.TYPE_LOCAL_NAME + ", number of attributes: " + attributeStatement.getAttributes().size());
        	
        	attributeStatement.getAttributes().forEach(attribute -> {
        		
        		Logger.debug(this, "Attribute - friendly name: " + attribute.getFriendlyName() + ", name: " 
        				+ attribute.getName() + ", type: " + attribute.TYPE_LOCAL_NAME + ", number of values: " + attribute.getAttributeValues().size());

        		if ( (attribute.getName() != null && attribute.getName().equals(emailField)) 
        				|| (attribute.getFriendlyName() != null && attribute.getFriendlyName().equals(emailField)) ) {

                    this.resolveEmail(emailField, attrBuilder, attribute, nameId);
        		} else if ( (attribute.getName() != null && attribute.getName().equals(lastNameField))
        				|| (attribute.getFriendlyName() != null && attribute.getFriendlyName().equals(lastNameField)) ) {

        			Logger.debug(this, "Resolving attribute - LastName : " + lastNameField);

        			final String lastName = (UtilMethods.isSet(attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue()))?
                            attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue():
                            lastNameForNullValue;

        			attrBuilder.lastName(lastName);

        			Logger.debug(this, "Resolved attribute - lastName : " + attrBuilder.lastName);
        			
        		} else if ( (attribute.getName() != null && attribute.getName().equals(firstNameField))
        				|| (attribute.getFriendlyName() != null && attribute.getFriendlyName().equals(firstNameField)) ) {

        			Logger.debug(this, "Resolving attribute - firstName : " + firstNameField);

                    final String firstName = (UtilMethods.isSet(attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue()))?
                            attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue():
                            firstNameForNullValue;

        			attrBuilder.firstName(firstName);

        			Logger.debug(this, "Resolved attribute - firstName : " + attrBuilder.firstName);
        			
        		}else if ( (attribute.getName() != null && attribute.getName().equals(rolesField)) 
        				|| (attribute.getFriendlyName() != null && attribute.getFriendlyName().equals(rolesField)) ) {

        			Logger.debug(this, "Resolving attribute - roles : " + rolesField);
            		attrBuilder.addRoles(true).roles(attribute);
            		Logger.debug(this, "Resolving attributes - roles : " + attribute);
        		} else {
        			Logger.debug(this, "Attribute did not match any user property");
        		}
        	});
        });
        

        return attrBuilder.build();
    } // resolveAttributes.

    private void resolveEmail(final String                  emailField,
                              final AttributesBean.Builder  attributesBuilder,
                              final Attribute               attribute,
                              final String                  nameId) {

        Logger.debug(this, "Resolving attribute - Email : " + emailField);

        String emailValue = attribute.getAttributeValues().get(0)
                .getDOM().getFirstChild().getNodeValue();

        emailValue        = (!UtilMethods.isSet(emailValue))?
                createNoReplyEmail(nameId):emailValue;

        attributesBuilder.email(emailValue);

        Logger.debug(this, "Resolved attribute - Email : " + attributesBuilder.email);
    } // resolveEmail.

    private String createNoReplyEmail (final String nameId) {

        Logger.debug(this, "The userid : " + nameId
                + " has the email null, creating a new one");

        final String emailValue =
                new StringBuilder(NO_REPLY).append(sanitizeNameId(nameId))
                        .append(NO_REPLY_DOTCMS_COM).toString();

        Logger.debug(this, "For the userid : " + nameId
                + " the generated email is: " + emailValue);

        return emailValue;
    } // createNoReplyEmail.

    private String sanitizeNameId(final String nameId) {

        return StringUtils.replace(nameId, AT_SYMBOL, AT_);
    } // sanitizeNameId.

    protected void validateAttributes(Assertion assertion) throws AttributesNotFoundException {
        if (assertion == null
            || assertion.getAttributeStatements() == null
            || assertion.getAttributeStatements().isEmpty()
            || assertion.getSubject() == null
            || assertion.getSubject().getNameID() == null
            || assertion.getSubject().getNameID().getValue().isEmpty()) {
        	
            throw new AttributesNotFoundException("No attributes found");
        }

    }

    // Gets the attributes from the Assertion, based on the attributes
    // see if the user exists return it from the dotCMS records, if does not exist then, tries to create it.
    // the existing or created user, will be updated the roles if they present on the assertion.
    protected User resolveUser(final Assertion assertion, final Configuration configuration) {

        User systemUser  = null;
        User user        = null;
        AttributesBean attributesBean = null;

        try {

            attributesBean = this.resolveAttributes(assertion, configuration);

            Logger.debug(this,
                "Validating user - " + attributesBean);

            systemUser = this.userAPI.getSystemUser();

            user = this.userAPI.loadUserById(attributesBean.getNameID().getValue(), systemUser, false);
        } catch (AttributesNotFoundException e){
            Logger.error(this, e.getMessage());
            return null;
        } catch (NoSuchUserException e) {
            Logger.error(this, "No matching user, creating");
            user = null;
        } catch (Exception e) {
            Logger.error(this, "Unknown exception", e);
            user = null;
        }

        if (null == user) { // if user does not exists, create a new one.

            user = this.createNewUser(systemUser, attributesBean);
        } else { // update it, since exists
            user = this.updateUser (user, systemUser, attributesBean);
        }

        if (user.isActive()) {

            this.addRoles(user, attributesBean, configuration);
        } else {

            Logger.info(this, "The user " + user.getEmailAddress() +
                            " is not active");
        }

        return user;
    } // resolveUser.

    private User updateUser(final User user, final User systemUser,
                            final AttributesBean attributesBean) {

        try {

            user.setEmailAddress(attributesBean.getEmail());
            user.setFirstName(attributesBean.getFirstName());
            user.setLastName (attributesBean.getLastName());

            this.userAPI.save(user, systemUser, false);
            Logger.info(this, "User updated. email: " + attributesBean.getEmail());
        } catch (Exception e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }

        return user;
    }

    private void addRoles(final User user,
                          final AttributesBean attributesBean, final Configuration configuration) {

        String role;

        try {

        	if (attributesBean.isAddRoles() ||
        			configuration.getStringProperty(DOTCMS_SAML_OPTIONAL_USER_ROLE, null) != null ) {
        		// remove previous roles
        		Logger.info(this, "Removing user previous roles");
        		this.roleAPI.removeAllRolesFromUser(user);
        	} else {
        		Logger.debug(this, "No roles will be removed");
        	}

            if (attributesBean.isAddRoles() &&
                null != attributesBean.getRoles() &&
                null != attributesBean.getRoles().getAttributeValues() &&
                attributesBean.getRoles().getAttributeValues().size() > 0) {

                final String removeRolePrefix = configuration.getStringProperty
                    (DOT_SAML_REMOVE_ROLES_PREFIX, StringUtils.EMPTY);
                final String [] rolePatterns   = configuration.getStringArray
                    (DOTCMS_SAML_INCLUDE_ROLES_PATTERN, null);

                Logger.debug(this, "Role Patterns: " + this.toString(rolePatterns) +
                    ", remove role prefix: " + removeRolePrefix);

                //add roles
                for (XMLObject roleObject : attributesBean.getRoles().getAttributeValues()) {

                    if (null != rolePatterns && rolePatterns.length > 0) {

                        role = roleObject.getDOM().getFirstChild().getNodeValue();
                        if (!this.isValidRole(role, rolePatterns)) {
                            // when there are role filters and the current roles is not
                            // a valid role, we have to filter it.

                            Logger.info(this, "Skipping the role: " + role);
                            continue;
                        }
                    }

                    this.addRole(user, removeRolePrefix, roleObject);
                }
            }
            //Add SAML User role
            addRole(user, configuration.getStringProperty(DOTCMS_SAML_USER_ROLE, "SAML User"), true, true);
            Logger.debug(this, "Default SAML User role has been assigned");

            //Add DOTCMS_SAML_OPTIONAL_USER_ROLE
            if (configuration.getStringProperty(DOTCMS_SAML_OPTIONAL_USER_ROLE, null) != null) {
                addRole(user, configuration.getStringProperty(DOTCMS_SAML_OPTIONAL_USER_ROLE, null), false, false);
                Logger.debug(this, "Optional user role: " + configuration.getStringProperty(DOTCMS_SAML_OPTIONAL_USER_ROLE, null) + " has been assigned");
            }

        } catch (DotDataException e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }
    } // addRoles.

    private String toString(String[] rolePatterns) {

        return null == rolePatterns? NULL : Arrays.asList(rolePatterns).toString();
    }

    private void addRole(final User user, final String roleKey, final boolean createRole, final boolean isSystem) throws DotDataException{

        Role role = this.roleAPI.loadRoleByKey(roleKey);

        //create the role, in case it does not exist
        if (role == null && createRole){
            Logger.info(this, "Role not found. Creating Role with key: " + roleKey);
            role = createNewRole(roleKey, isSystem);
        }

        if(null != role && !this.roleAPI.doesUserHaveRole(user, role)) {

            this.roleAPI.addRoleToUser(role, user);
            Logger.info(this, "Added role: " + role.getName() +
                " to user:" + user.getEmailAddress());
        }
    } // addRole.

    private void addRole(final User user, final String removeRolePrefix,
                         final XMLObject roleObject) throws DotDataException {

        //remove role prefix
        final String roleKey = (isSet(removeRolePrefix))?
                roleObject.getDOM().getFirstChild().getNodeValue()
                        .replaceFirst(removeRolePrefix, StringUtils.EMPTY):
                roleObject.getDOM().getFirstChild().getNodeValue();

        addRole(user, roleKey, false, false);
    } // addRole.

    private Role createNewRole(String roleKey, boolean isSystem) throws DotDataException {
        Role role = new Role();
        role.setName(roleKey);
        role.setRoleKey(roleKey);
        role.setEditUsers(true);
        role.setEditPermissions(false);
        role.setEditLayouts(false);
        role.setDescription("");
        role.setId(UUIDGenerator.generateUuid());

        //Setting SYSTEM role as a parent
        role.setSystem(isSystem);
        Role parentRole = roleAPI.loadRoleByKey(Role.SYSTEM);
        role.setParent(parentRole.getId());

        String date = DateUtil.getCurrentDate();

        ActivityLogger.logInfo(ActivityLogger.class, getClass() + " - Adding Role", "Date: " + date + "; " + "Role:" + roleKey);
        AdminLogger.log(AdminLogger.class, getClass() + " - Adding Role", "Date: " + date + "; " + "Role:" + roleKey);

        try {
            role = roleAPI.save(role, role.getId());
        } catch (DotDataException | DotStateException e) {
            ActivityLogger.logInfo(ActivityLogger.class, getClass() + " - Error Adding Role", "Date: " + date + ";  " + "Role:" + roleKey);
            AdminLogger.log(AdminLogger.class, getClass() + " - Error Adding Role", "Date: " + date + ";  " + "Role:" + roleKey);
            throw e;
        }

        return role;
    }

    protected User createNewUser(final User systemUser,
                               final AttributesBean attributesBean) {

        User user = null;
        final String userId;

        try {

            user   = this.userAPI.createUser(attributesBean.getNameID().getValue(), attributesBean.getEmail());

            user.setFirstName(attributesBean.getFirstName());
            user.setLastName (attributesBean.getLastName());
            user.setActive(true);

            user.setCreateDate(new Date());
            user.setPassword(PublicEncryptionFactory.digestString
                    (UUIDGenerator.generateUuid() + "/" + UUIDGenerator.generateUuid()));
            user.setPasswordEncrypted(true);

            this.userAPI.save(user, systemUser, false);
            Logger.info(this, "new user created. email: " + attributesBean.getEmail());
        } catch (Exception e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }

        return user;
    } // createNewUser.

    @Override
    public Assertion resolveAssertion(final HttpServletRequest request,
                        final HttpServletResponse response, final String siteName) {

        final AssertionResolverHandler assertionResolverHandler =
                this.assertionResolverHandlerFactory.getAssertionResolverForSite(siteName);

        return assertionResolverHandler.resolveAssertion(request, response, siteName);
    } // getAssertion.



    // if the SAML_ART_PARAM_KEY parameter is in the request, it is a valid SAML request
    @Override
    public boolean isValidSamlRequest(final HttpServletRequest request, final HttpServletResponse response, final String siteName) {

        final AssertionResolverHandler assertionResolverHandler =
                this.assertionResolverHandlerFactory.getAssertionResolverForSite(siteName);

        return assertionResolverHandler.isValidSamlRequest(request, response, siteName);
    } // isValidSamlRequest.



    // this makes the redirect to the IdP
    private void doRedirect (final MessageContext context,
                             final HttpServletResponse response,
                             final XMLObject xmlObject) {

        final HTTPRedirectDeflateEncoder encoder;

        try {

            encoder =
                    new HTTPRedirectDeflateEncoder();

            encoder.setMessageContext(context);
            encoder.setHttpServletResponse(response);

            encoder.initialize();

            Logger.debug(this, "XMLObject: " + toXMLObjectString(xmlObject));
            Logger.info(this, "Redirecting to IDP");

            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // doRedirect.

    private void setSignatureSigningParams(final MessageContext context, final Configuration configuration) {

        final SignatureSigningParameters signatureSigningParameters =
                new SignatureSigningParameters();

        signatureSigningParameters.setSigningCredential
                (getCredential(configuration));
        signatureSigningParameters.setSignatureAlgorithm
                (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        context.getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
    } // setSignatureSigningParams.

} // E:O:F:OpenSamlAuthenticationServiceImpl.
