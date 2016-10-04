package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.DotSamlException;
import com.dotcms.plugin.saml.v3.HttpClient;
import com.dotcms.plugin.saml.v3.SiteConfigurationResolver;
import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;
import com.dotmarketing.util.Logger;
import com.liferay.util.InstancePool;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.saml2.core.Artifact;
import org.opensaml.saml.saml2.core.ArtifactResolve;
import org.opensaml.saml.saml2.core.ArtifactResponse;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static com.dotcms.plugin.saml.v3.SamlUtils.*;
import static com.dotcms.plugin.saml.v3.SamlUtils.toXMLObjectString;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * SOAP for the Artifact resolver approach assertion resolver handler
 * @author jsanca
 */
public class SOAPArtifactAssertionResolverHandlerImpl implements AssertionResolverHandler {

    public static final String SAML_ART_PARAM_KEY = "SAMLart";

    // this one is an object to send an artifact resolve by post.
    private final HttpClient httpClient;

    public SOAPArtifactAssertionResolverHandlerImpl() {

        this (new HttpClient());
    }

    @VisibleForTesting
    protected SOAPArtifactAssertionResolverHandlerImpl(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean isValidSamlRequest(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final String siteName) {

        return isSet(request.getParameter(SAML_ART_PARAM_KEY));
    } // isValidSamlRequest.

    @Override
    public Assertion resolveAssertion(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final String siteName) {

        final SiteConfigurationResolver resolver      = (SiteConfigurationResolver) InstancePool.get(SiteConfigurationResolver.class.getName());
        final Configuration configuration             = resolver.resolveConfiguration(request);
        final Artifact artifact;
        final ArtifactResolve artifactResolve;
        final ArtifactResponse artifactResponse;
        Assertion assertion = null;

        artifact = this.buildArtifactFromRequest(request);
        Logger.info(this, "Artifact: " + artifact.getArtifact());

        artifactResolve = buildArtifactResolve(artifact, configuration);

        Logger.info(this, "Sending ArtifactResolve");
        Logger.info(this, "ArtifactResolve: " + toXMLObjectString(artifactResolve));

        artifactResponse = this.sendAndReceiveArtifactResolve(artifactResolve,
                response, configuration);

        Logger.info(this, "ArtifactResponse received");
        Logger.info(this, "ArtifactResponse: " + toXMLObjectString(artifactResponse));

        this.validateDestinationAndLifetime(artifactResponse, request, configuration);

        assertion = getAssertion(artifactResponse, configuration);

        if (configuration.isVerifyAssertionSignatureNeeded()) {

            Logger.info(this, "Doing the verification assertion signature.");
            verifyAssertionSignature(assertion, configuration);
        } else {

            Logger.info(this, "The verification assertion signature was skipped.");
        }

        Logger.info(this, "Decrypted Assertion: " + toXMLObjectString(artifact));

        return assertion;
    } // resolveAssertion.


    private Artifact buildArtifactFromRequest(final HttpServletRequest req) {

        final Artifact artifact = buildSAMLObject(Artifact.class);
        artifact.setArtifact(req.getParameter(SAML_ART_PARAM_KEY));
        return artifact;
    } // buildArtifactFromRequest.

    private ArtifactResponse sendAndReceiveArtifactResolve(final ArtifactResolve artifactResolve,
                                                           final HttpServletResponse servletResponse,
                                                           final Configuration configuration) {

        final MessageContext<ArtifactResolve> messageContext = new MessageContext<ArtifactResolve>();
        final InOutOperationContext<ArtifactResponse, ArtifactResolve> context;

        final String artifactResolutionService  = configuration.getStringProperty(
                DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL, null);

        final  String canonicalizationAlgorithm = configuration.getStringProperty(
                DotSamlConstants.DOTCMS_SAML_SIGNATURE_CANONICALIZATION_ALGORITHM,
                SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        ArtifactResponse artifactResponse = null;

        if (!isSet(artifactResolutionService)) {

            throw new DotSamlException("The property: " + DotSamlConstants.DOT_SAML_ARTIFACT_RESOLUTION_SERVICE_URL +
                    " must be set on the dotmarketing-config.properties");
        }

        try {

            messageContext.setMessage(artifactResolve);
            this.setSignatureSigningParams(messageContext,
                    canonicalizationAlgorithm, configuration);
            context = new ProfileRequestContext<ArtifactResponse, ArtifactResolve>();
            context.setOutboundMessageContext(messageContext);

            Logger.info(this, "Sending the Artifact resolve to: " + artifactResolutionService);
            this.httpClient.send(artifactResolutionService, context);

            artifactResponse = context.getInboundMessageContext().getMessage();
        } catch (Exception e) {

            Logger.error(this, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }

        return artifactResponse;
    } // sendAndReceiveArtifactResolve.

    private void validateDestinationAndLifetime(final ArtifactResponse artifactResponse,
                                                final HttpServletRequest request,
                                                final Configuration configuration) {

        final long clockSkew = configuration.getIntProperty
                (DotSamlConstants.DOT_SAML_CLOCK_SKEW, DOT_SAML_CLOCK_SKEW_DEFAULT_VALUE);
        final long lifeTime  = configuration.getIntProperty
                (DotSamlConstants.DOT_SAML_MESSAGE_LIFE_TIME, DOT_SMAL_MESSAGE_LIFE_DEFAULT_VALUE);
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

        invokeMessageHandlerChain(handlerChain, context);
    } // validateDestinationAndLifetime.

    private void setSignatureSigningParams(final MessageContext context,
                                           final String canonicalizationAlgorithm,
                                           final Configuration configuration) {

        final SignatureSigningParameters signatureSigningParameters =
                new SignatureSigningParameters();

        final Credential credential = getCredential(configuration);

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
} // E:O:F:SOAPArtifactAssertionResolverHandlerImpl.
