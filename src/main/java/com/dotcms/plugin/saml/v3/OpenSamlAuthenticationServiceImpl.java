package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotcms.repackage.org.apache.commons.lang.StringUtils;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.Role;
import com.dotmarketing.business.RoleAPI;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.dotcms.plugin.saml.v3.SamlUtils.*;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * Authentication with Open SAML
 * @author jsanca
 */
public class OpenSamlAuthenticationServiceImpl implements SamlAuthenticationService {

    private final HttpClient httpClient;
    private final UserAPI userAPI;
    private final RoleAPI roleAPI;

    public OpenSamlAuthenticationServiceImpl() {

        this(new HttpClient(), APILocator.getUserAPI(), APILocator.getRoleAPI());
    }

    @VisibleForTesting
    public OpenSamlAuthenticationServiceImpl(final HttpClient httpClient,
                                             final UserAPI userAPI,
                                             final RoleAPI roleAPI) {

        this.httpClient = httpClient;
        this.userAPI    = userAPI;
        this.roleAPI    = roleAPI;
    }

    /**
     * Authentication with Open SAML 3 is basically a redirect to the IDP to show the login page to the user.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @Override
    public void authentication(final HttpServletRequest request,
                               final HttpServletResponse response) {

        final MessageContext context = new MessageContext(); // main context
        final AuthnRequest authnRequest = buildAuthnRequest(request);

        context.setMessage(authnRequest);

        final SAMLPeerEntityContext peerEntityContext = // peer entity (Idp to SP and viceversa)
                context.getSubcontext(SAMLPeerEntityContext.class, true);
        final SAMLEndpointContext endpointContext = // info about the endpoint of the peer entity
                peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);

        endpointContext.setEndpoint(
                getIdentityProviderDestinationEndpoint());

        this.setSignatureSigningParams(context);
        this.doRedirect(context, response, authnRequest);
    } // authentication.

    /**
     * When the authentication is performed and redirected to SO (DotCMS) you can call this method.
     * If the request contains a parameter called AMLart, will try to get the {@link org.opensaml.saml.saml2.core.Assertion}
     * with the user information via SOAP.
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
                        final HttpServletResponse response) {

        User user = null;
        final Assertion assertion;

        if (this.isValidSamlRequest (request)) {

            assertion = this.resolveAssertion(request, response);

            Logger.info (this, "Resolved assertion: " + assertion);

            user      = this.resolveUser(assertion);

            Logger.info (this, "Resolved user: " + user);
        }

        return user;
    } // getUser.

    private AttributesBean resolveAttributes (final Assertion assertion) {

        final String emailField       = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_EMAIL_ATTRIBUTE, "mail");
        final String firstNameField   = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_FIRSTNAME_ATTRIBUTE, "givenName");
        final String lastNameField    = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_LASTNAME_ATTRIBUTE, "sn");
        final String rolesField       = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_ROLES_ATTRIBUTE, "authorisations");

        final AttributesBean.Builder attrBuilder = new AttributesBean.Builder();

        assertion.getAttributeStatements().get(0).getAttributes().forEach(attribute -> {

            if (attribute.getFriendlyName().equals(emailField)) {

                attrBuilder.email
                        (attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue());
            } else if (attribute.getFriendlyName().equals(lastNameField)) {

                attrBuilder.lastName
                        (attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue());
            } else if(attribute.getFriendlyName().equals(firstNameField)){

                attrBuilder.firstName
                        (attribute.getAttributeValues().get(0).getDOM().getFirstChild().getNodeValue());
            }else if (attribute.getFriendlyName().equals(rolesField)) {

                attrBuilder.addRoles(true).roles(attribute);
            }
        });

        return attrBuilder.build();
    } // resolveAttributes.

    private User resolveUser(final Assertion assertion) {

        User systemUser  = null;
        User user        = null;
        final AttributesBean attributesBean =
                this.resolveAttributes(assertion);

        try {

            Logger.debug(this,
                    "Validating user - " + attributesBean);

            systemUser = this.userAPI.getSystemUser();
            user       = this.userAPI.loadByUserByEmail(attributesBean.getEmail(), systemUser, false);
        } catch (Exception e) {

            Logger.error(this, "No matching user, creating", e);
            user = null;
        }

        if (null == user) { // if user does not exists, create a new one.

            user = this.createNewUser(systemUser, attributesBean);
        }

        if (user.isActive() && attributesBean.isAddRoles() &&
                null != attributesBean.getRoles() &&
                null != attributesBean.getRoles().getAttributeValues()) {

            this.addRoles(user, attributesBean);
        }

        return user;
    } // resolveUser.

    private void addRoles(final User user,
                          final AttributesBean attributesBean) {

        final String removeRolePrefix = Config.getStringProperty
                (DotSamlConstants.DOT_SAML_REMOVE_ROLES_PREFIX, StringUtils.EMPTY);

        try {

            // remove previous roles
            Logger.debug(this, "Removing user previous roles");
            this.roleAPI.removeAllRolesFromUser(user);

            //add roles
            for(XMLObject roleObject : attributesBean.getRoles().getAttributeValues()){

                this.addRole(user, removeRolePrefix, roleObject);
            }
        } catch (DotDataException e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }
    } // addRoles.

    private void addRole(final User user, final String removeRolePrefix,
                         final XMLObject roleObject) throws DotDataException {

        //remove role prefix
        final String roleKey = (isSet(removeRolePrefix))?
                roleObject.getDOM().getFirstChild().getNodeValue()
                        .replaceFirst(removeRolePrefix, StringUtils.EMPTY):
                roleObject.getDOM().getFirstChild().getNodeValue();

        final Role role = this.roleAPI.loadRoleByKey(roleKey);

        if(null != role && !this.roleAPI.doesUserHaveRole(user, role)) {

            this.roleAPI.addRoleToUser(role, user);
            Logger.debug(this, "Added role: " + role.getName() +
                    " to user:" + user.getEmailAddress());
        }
    } // addRole.

    private User createNewUser(final User systemUser,
                               final AttributesBean attributesBean) {

        User user = null;
        final String userId;

        try {

            userId = UUIDGenerator.generateUuid();
            user   = this.userAPI.createUser(userId, attributesBean.getEmail());

            user.setFirstName(attributesBean.getFirstName());
            user.setLastName (attributesBean.getLastName());
            user.setActive(true);

            user.setCreateDate(new Date());
            user.setPassword(PublicEncryptionFactory.digestString
                    (UUIDGenerator.generateUuid() + "/" + UUIDGenerator.generateUuid()));
            user.setPasswordEncrypted(true);

            this.userAPI.save(user, systemUser, false);
            Logger.debug(this, "new user created. email: " + attributesBean.getEmail());
        } catch (Exception e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }

        return user;
    } // createNewUser.

    @Override
    public Assertion resolveAssertion(final HttpServletRequest request,
                        final HttpServletResponse response) {

        final Configuration configuration =
                (Configuration) InstancePool.get(Configuration.class.getName());
        final Artifact artifact;
        final ArtifactResolve artifactResolve;
        final ArtifactResponse artifactResponse;
        Assertion assertion = null;

        artifact = this.buildArtifactFromRequest(request);
        Logger.info(this, "Artifact: " + artifact.getArtifact());

        artifactResolve = buildArtifactResolve(artifact);

        Logger.info(this, "Sending ArtifactResolve");
        Logger.info(this, "ArtifactResolve: " + toXMLObjectString(artifactResolve));

        artifactResponse = this.sendAndReceiveArtifactResolve(artifactResolve, response);

        Logger.info(this, "ArtifactResponse received");
        Logger.info(this, "ArtifactResponse: " + toXMLObjectString(artifactResponse));

        this.validateDestinationAndLifetime(artifactResponse, request);

        assertion = getAssertion(artifactResponse);

        if (configuration.isVerifyAssertionSignatureNeeded()) {

            Logger.info(this, "Doing the verification assertion signature.");
            verifyAssertionSignature(assertion);
        } else {

            Logger.info(this, "The verification assertion signature was skipped.");
        }

        Logger.info(this, "Decrypted Assertion: " + toXMLObjectString(artifact));

        return assertion;
    } // getAssertion.

    private Artifact buildArtifactFromRequest(final HttpServletRequest req) {

        final Artifact artifact = buildSAMLObject(Artifact.class);
        artifact.setArtifact(req.getParameter(SAML_ART_PARAM_KEY));
        return artifact;
    } // buildArtifactFromRequest.

    // if the SAML_ART_PARAM_KEY parameter is in the request, it is a valid SAML request
    private boolean isValidSamlRequest(final HttpServletRequest request) {

        return isSet(request.getParameter(SAML_ART_PARAM_KEY));
    } // isValidSamlRequest.

    private void validateDestinationAndLifetime(final ArtifactResponse artifactResponse,
                                                final HttpServletRequest request) {

        final long clockSkew = Config.getIntProperty
                (DotSamlConstants.DOT_SAML_CLOCK_SKEW, 1000);
        final long lifeTime  = Config.getIntProperty
                (DotSamlConstants.DOT_SAML_MESSAGE_LIFE_TIME, 2000);
        final MessageContext context = new MessageContext<ArtifactResponse>();
        final SAMLMessageInfoContext messageInfoContext =
                context.getSubcontext(SAMLMessageInfoContext.class, true);
        final MessageLifetimeSecurityHandler lifetimeSecurityHandler =
                new MessageLifetimeSecurityHandler();
        final ReceivedEndpointSecurityHandler receivedEndpointSecurityHandler =
                new ReceivedEndpointSecurityHandler();
        final BasicMessageHandlerChain<ArtifactResponse> handlerChain =
                new BasicMessageHandlerChain<ArtifactResponse>();
        final List handlers = new ArrayList<MessageHandler>();

        context.setMessage(artifactResponse);
        messageInfoContext.setMessageIssueInstant
                (artifactResponse.getIssueInstant());

        // message lifetime validation.
        lifetimeSecurityHandler.setClockSkew(clockSkew);
        lifetimeSecurityHandler.setMessageLifetime(lifeTime);
        lifetimeSecurityHandler.setRequiredRule(true);

        // validation of message destination.
        receivedEndpointSecurityHandler.setHttpServletRequest(request);
        handlers.add(lifetimeSecurityHandler);
        handlers.add(receivedEndpointSecurityHandler);
        handlerChain.setHandlers(handlers);

        try {

            handlerChain.initialize();
            handlerChain.doInvoke(context);
        } catch (ComponentInitializationException | MessageHandlerException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // validateDestinationAndLifetime.

    // this makes the redirect to the IdP
    private void doRedirect (final MessageContext context,
                             final HttpServletResponse response,
                             final AuthnRequest authnRequest) {

        final HTTPRedirectDeflateEncoder encoder;

        try {

            encoder =
                    new HTTPRedirectDeflateEncoder();

            encoder.setMessageContext(context);
            encoder.setHttpServletResponse(response);

            encoder.initialize();

            Logger.info(this, "AuthnRequest: " + toXMLObjectString(authnRequest));
            Logger.info(this, "Redirecting to IDP");

            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // doRedirect.

    // this makes the redirect to the IdP
    private void doPost (final MessageContext context,
                             final HttpServletResponse response,
                             final AuthnRequest authnRequest) {

        final HTTPPostEncoder encoder;

        try {

            encoder =
                    new HTTPPostEncoder();

            encoder.setMessageContext(context);
            encoder.setHttpServletResponse(response);

            encoder.initialize();

            Logger.info(this, "AuthnRequest: " + toXMLObjectString(authnRequest));
            Logger.info(this, "Redirecting to IDP");

            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // doRedirect.

    private void setSignatureSigningParams(final MessageContext context) {

        final SignatureSigningParameters signatureSigningParameters =
                new SignatureSigningParameters();

        signatureSigningParameters.setSigningCredential
                (getCredential());
        signatureSigningParameters.setSignatureAlgorithm
                (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        context.getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
    } // setSignatureSigningParams.

    private void setSignatureSigningParams(final MessageContext context,
                                           final String canonicalizationAlgorithm) {

        final SignatureSigningParameters signatureSigningParameters =
                new SignatureSigningParameters();

        final Credential credential = getCredential();

        Logger.info(this, "context: " + context);
        Logger.info(this, "Credential: " + credential);
        Logger.info(this, "canonicalizationAlgorithm: " + canonicalizationAlgorithm);

        signatureSigningParameters.setSigningCredential
                (credential);
        signatureSigningParameters.setSignatureAlgorithm
                (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        signatureSigningParameters.setSignatureCanonicalizationAlgorithm
                (canonicalizationAlgorithm);

        Logger.info(this, "signatureSigningParameters: " + signatureSigningParameters);

        context.getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
    } // setSignatureSigningParams.

    private ArtifactResponse sendAndReceiveArtifactResolve(final ArtifactResolve artifactResolve,
                                                           final HttpServletResponse servletResponse) {

        final MessageContext<ArtifactResolve> messageContext = new MessageContext<ArtifactResolve>();
        final InOutOperationContext<ArtifactResponse, ArtifactResolve> context;
        final String artifactResolutionService = Config.getStringProperty(
                DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL, null);
        final  String canonicalizationAlgorithm = Config.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_SIGNATURE_CANONICALIZATION_ALGORITHM,
                    SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        ArtifactResponse artifactResponse = null;

        if (!isSet(artifactResolutionService)) {

            throw new DotSamlException ("The property: " + DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL +
                    " must be set on the dotmarketing-config.properties");
        }

        try {

            messageContext.setMessage(artifactResolve);
            this.setSignatureSigningParams(messageContext, canonicalizationAlgorithm);
            context = new ProfileRequestContext<ArtifactResponse, ArtifactResolve>();
            context.setOutboundMessageContext(messageContext);

            Logger.info(this, "Sending the Artifact resolve");
            this.httpClient.send(artifactResolutionService, context);

            artifactResponse = context.getInboundMessageContext().getMessage();
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        return artifactResponse;
    } // sendAndReceiveArtifactResolve.
} // E:O:F:OpenSamlAuthenticationServiceImpl.
