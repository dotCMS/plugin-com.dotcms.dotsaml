package com.dotcms.plugin.saml.v3;

import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.soap.client.http.AbstractPipelineHttpSOAPClient;

import java.io.Serializable;

/**
 * Encapsulates an simple http client for soap messages on saml
 * @author jsanca
 */
public class HttpClient implements Serializable {

    /**
     * Send the context message to the url
     * @param url {@link String}
     * @param context {@link InOutOperationContext}
     * @throws Exception
     */
    public void send (final String url, final InOutOperationContext context) throws Exception {

        final AbstractPipelineHttpSOAPClient<SAMLObject, SAMLObject> soapClient =
                new DotPipelineHttpSOAPClient();

        final HttpClientBuilder clientBuilder = new HttpClientBuilder();

        soapClient.setHttpClient(clientBuilder.buildClient());
        soapClient.send(url, context);
    } // send.

} // E:O:F:HttpClient.
