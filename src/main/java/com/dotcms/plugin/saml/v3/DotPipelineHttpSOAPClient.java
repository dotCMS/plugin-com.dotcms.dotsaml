package com.dotcms.plugin.saml.v3;

import org.opensaml.messaging.pipeline.httpclient.BasicHttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipeline;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.saml2.binding.decoding.impl.HttpClientResponseSOAP11Decoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HttpClientRequestSOAP11Encoder;
import org.opensaml.soap.client.http.AbstractPipelineHttpSOAPClient;
import org.opensaml.soap.common.SOAPException;

/**
 * Dot Pipeline http soap client implementation.
 * @author jsanca
 */
public class DotPipelineHttpSOAPClient extends AbstractPipelineHttpSOAPClient {

    @Override
    protected HttpClientMessagePipeline newPipeline() throws SOAPException {

        final BasicHttpClientMessagePipeline pipeline = new BasicHttpClientMessagePipeline(
                new HttpClientRequestSOAP11Encoder(),
                new HttpClientResponseSOAP11Decoder()
        );

        pipeline.setOutboundPayloadHandler(new SAMLOutboundProtocolMessageSigningHandler());

        return pipeline;
    } // newPipeline
} // E:O:F:DotPipelineHttpSOAPClient.
