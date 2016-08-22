package com.dotcms.plugin.saml.v3;

import com.dotmarketing.util.Logger;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication with Open SAML
 * @author jsanca
 */
public class OpenSamlAuthenticationServiceImpl implements SamlAuthenticationService {


    /**
     * Authentication with Open SAML 3 is basically a redirect to the IDP to show the login page to the user.
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @Override
    public void authentication(final HttpServletRequest request,
                               final HttpServletResponse response) {

        final MessageContext context = new MessageContext();
        final AuthnRequest authnRequest =
                SamlUtils.buildAuthnRequest(request);

        context.setMessage(authnRequest);

        final SAMLPeerEntityContext peerEntityContext =
                context.getSubcontext(SAMLPeerEntityContext.class, true);
        final SAMLEndpointContext endpointContext =
                peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);

        endpointContext.setEndpoint(
                SamlUtils.getIdentityProviderDestinationEndpoint());

        this.setSignatureSigningParams(context);
        this.doRedirect(context, response, authnRequest);
    } // authentication.

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

            Logger.info(SamlUtils.class, "AuthnRequest: " + SamlUtils.toString(authnRequest));
            Logger.info(SamlUtils.class, "Redirecting to IDP");

            encoder.encode();
        } catch (ComponentInitializationException | MessageEncodingException e) {

            Logger.error(SamlUtils.class, e.getMessage(), e);
            throw new DotSamlException(e.getMessage(), e);
        }
    } // doRedirect.

    private void setSignatureSigningParams(final MessageContext context) {

        final SignatureSigningParameters signatureSigningParameters =
                new SignatureSigningParameters();

        signatureSigningParameters.setSigningCredential
                (SamlUtils.getCredential());
        signatureSigningParameters.setSignatureAlgorithm
                (SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

        context.getSubcontext(SecurityParametersContext.class, true)
                .setSignatureSigningParameters(signatureSigningParameters);
    } // setSignatureSigningParams.
} // E:O:F:OpenSamlAuthenticationServiceImpl.
