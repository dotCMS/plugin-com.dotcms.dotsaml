package com.dotcms.plugin.saml.v3.service;

import com.dotmarketing.util.Logger;

import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.opensaml.xmlsec.SignatureSigningParameters;

import org.opensaml.core.xml.XMLObject;

import java.net.MalformedURLException;
import java.util.List;
// migrated
public class DotHTTPRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {

    private final boolean clearQueryParams;

    public DotHTTPRedirectDeflateEncoder() {
        clearQueryParams = true;
    }

    public DotHTTPRedirectDeflateEncoder(boolean clearQueryParams) {
        this.clearQueryParams = clearQueryParams;
    }

    @Override
    protected String buildRedirectURL(MessageContext<SAMLObject> messageContext, String endpoint, String message) throws MessageEncodingException {

        Logger.debug(this, "Building URL to redirect client to: " + endpoint + "");
        URLBuilder urlBuilder = null;

        try {
            urlBuilder = new URLBuilder(endpoint);
        } catch (MalformedURLException var12) {
            throw new MessageEncodingException("Endpoint URL " + endpoint + " is not a valid URL", var12);
        }

        List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();
        if (this.clearQueryParams) {
            queryParams.clear();
        }
        SAMLObject outboundMessage = (SAMLObject)messageContext.getMessage();
        if (outboundMessage instanceof RequestAbstractType) {
            queryParams.add(new Pair("SAMLRequest", message));
        } else {
            if (!(outboundMessage instanceof StatusResponseType)) {
                throw new MessageEncodingException("SAML message is neither a SAML RequestAbstractType nor StatusResponseType");
            }

            queryParams.add(new Pair("SAMLResponse", message));
        }

        String relayState = SAMLBindingSupport.getRelayState(messageContext);
        if (SAMLBindingSupport.checkRelayState(relayState)) {
            queryParams.add(new Pair("RelayState", relayState));
        }

        SignatureSigningParameters signingParameters = SAMLMessageSecuritySupport.getContextSigningParameters(messageContext);
        if (signingParameters != null && signingParameters.getSigningCredential() != null) {
            String sigAlgURI = this.getSignatureAlgorithmURI(signingParameters);
            Pair<String, String> sigAlg = new Pair("SigAlg", sigAlgURI);
            queryParams.add(sigAlg);
            String sigMaterial = urlBuilder.buildQueryString();
            queryParams.add(new Pair("Signature", this.generateSignature(signingParameters.getSigningCredential(), sigAlgURI, sigMaterial)));
        } else {
            Logger.debug(this, "No signing credential was supplied, skipping HTTP-Redirect DEFLATE signing");
        }

        return urlBuilder.buildURL();
    }

}
