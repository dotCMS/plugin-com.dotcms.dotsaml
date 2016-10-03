package com.dotcms.plugin.saml.v3;

import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import com.dotmarketing.util.Logger;
import org.apache.http.HttpResponse;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.pipeline.httpclient.BasicHttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipeline;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.saml2.binding.decoding.impl.HttpClientResponseSOAP11Decoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HttpClientRequestSOAP11Encoder;
import org.opensaml.soap.client.http.AbstractPipelineHttpSOAPClient;
import org.opensaml.soap.common.SOAPException;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Dot Pipeline http soap client implementation.
 * This is just an encapsulation from the original code.
 * @author jsanca
 */
public class DotPipelineHttpSOAPClient extends AbstractPipelineHttpSOAPClient {

    @Override
    protected HttpClientMessagePipeline newPipeline() throws SOAPException {

        final BasicHttpClientMessagePipeline pipeline = new BasicHttpClientMessagePipeline(
                new HttpClientRequestSOAP11Encoder(),
                new HttpClientResponseSOAP11Decoder() {

                    @Nullable
                    @Override
                    public HttpResponse getHttpResponse() {

                        final HttpResponse response = super.getHttpResponse();
                        final HttpEntity   httpEntity = response.getEntity();
                        InputStream  originalInputStream = null;
                        ByteArrayOutputStream baos = null;
                        StringBuilder builder = new StringBuilder();

                        try {

                            originalInputStream = httpEntity.getContent();
                            baos = new ByteArrayOutputStream();
                            final byte[] buffer = new byte[1024];
                            int len;
                            while ((len = originalInputStream.read(buffer)) > -1 ) {
                                baos.write(buffer, 0, len);
                                builder.append(new String(buffer));
                            }

                            baos.flush();
                            originalInputStream = new ByteArrayInputStream(baos.toByteArray());
                        } catch (Exception e) {

                            Logger.error(this, e.getMessage(), e);
                        }

                        final InputStream  newInputStream = originalInputStream;

                        response.setEntity(new HttpEntity() {
                            @Override
                            public boolean isRepeatable() {
                                return httpEntity.isRepeatable();
                            }

                            @Override
                            public boolean isChunked() {
                                return httpEntity.isChunked();
                            }

                            @Override
                            public long getContentLength() {
                                return httpEntity.getContentLength();
                            }

                            @Override
                            public Header getContentType() {
                                return httpEntity.getContentType();
                            }

                            @Override
                            public Header getContentEncoding() {
                                return httpEntity.getContentEncoding();
                            }

                            @Override
                            public InputStream getContent() throws IOException, IllegalStateException {
                                return newInputStream;
                            }

                            @Override
                            public void writeTo(OutputStream outputStream) throws IOException {
                                 httpEntity.writeTo(outputStream);
                            }

                            @Override
                            public boolean isStreaming() {
                                return httpEntity.isStreaming();
                            }

                            @Override
                            public void consumeContent() throws IOException {
                                httpEntity.consumeContent();
                            }
                        });

                        Logger.debug(this, "response: " + response);
                        Logger.debug(this, "response.getStatusLine().getStatusCode(): "
                                + response.getStatusLine().getStatusCode());
                        Logger.debug(this, "response.getEntity(): " + response.getEntity());

                        try {
                            Logger.debug(this, "response.getEntity().getContent: " + response.getEntity().getContent());
                            Logger.debug(this, "response.getEntity().getContent.Response: " +
                                    builder.toString());
                        } catch (IOException e) {

                            Logger.error(this, e.getMessage(), e);
                        }

                        return response;

                    }
                }
        );

        pipeline.setOutboundPayloadHandler(new SAMLOutboundProtocolMessageSigningHandler());

        return pipeline;
    } // newPipeline
} // E:O:F:DotPipelineHttpSOAPClient.
