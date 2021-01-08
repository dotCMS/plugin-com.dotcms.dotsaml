package com.dotcms.plugin.saml.v3.service;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotmarketing.util.Logger;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.dotcms.plugin.saml.v3.util.SamlUtils.buildAuthnRequest;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.getCredential;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.getIdentityProviderSLODestinationEndpoint;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.toXMLObjectString;

/**
 * Implements the authentication handler by POST
 * @author jsanca
 */
public class HttpPOSTAuthenticationHandler implements AuthenticationHandler {

    private final VelocityEngine  velocityEngine;

    public HttpPOSTAuthenticationHandler(final VelocityEngine velocityEngine) {

        this.velocityEngine  = velocityEngine;
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
                       final IdpConfig idpConfig) {

        final MessageContext context    = new MessageContext(); // main context
        final AuthnRequest authnRequest = buildAuthnRequest(request, idpConfig, SAMLConstants.SAML2_POST_BINDING_URI);

        context.setMessage(authnRequest);

        // peer entity (Idp to SP and viceversa)
        final SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);
        // info about the endpoint of the peer entity
        final SAMLEndpointContext endpointContext     = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);

        endpointContext.setEndpoint(getIdentityProviderSLODestinationEndpoint(idpConfig));

        this.setSignatureSigningParams(context, idpConfig);
        this.doPost(context, response, authnRequest, idpConfig);
    }

    @SuppressWarnings("rawtypes")
    private void setSignatureSigningParams(final MessageContext context, final IdpConfig idpConfig) {
        final SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();

        signatureSigningParameters.setSigningCredential(getCredential(idpConfig));
        signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        context.getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
    }

    // this makes the post to the IdP
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void doPost(final MessageContext context, final HttpServletResponse response,
                        final XMLObject xmlObject, final IdpConfig idpConfig) {
        final HTTPPostEncoder encoder;

        try {
            encoder = new DotHTTPPOSTDeflateEncoder(this.velocityEngine);

            encoder.setMessageContext(context);
            encoder.setHttpServletResponse(response);

            encoder.initialize();

            Logger.debug(this.getClass().getName(), "Printing XMLObject:");
            Logger.debug(this.getClass().getName(), "\n\n" + toXMLObjectString(xmlObject));
            Logger.debug(this.getClass().getName(), "Redirecting to IdP '" + idpConfig.getIdpName() + "'");

            response.setHeader("Access-Control-Allow-Origin", "*");
            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {

            final String errorMsg = "An error occurred when executing Posting to IdP '" +
                    idpConfig.getIdpName() + "': " + e.getMessage();
            Logger.error(this.getClass().getName(), errorMsg, e);
            throw new DotSamlException(errorMsg, e);
        }
    }
}