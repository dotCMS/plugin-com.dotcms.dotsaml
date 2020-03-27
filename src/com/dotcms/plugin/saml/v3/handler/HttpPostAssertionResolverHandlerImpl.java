package com.dotcms.plugin.saml.v3.handler;

import static com.dotcms.plugin.saml.v3.util.SamlUtils.getAssertion;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.invokeMessageHandlerChain;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.toXMLObjectString;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.verifyAssertionSignature;
import static com.dotcms.plugin.saml.v3.util.SamlUtils.verifyResponseSignature;
import static com.dotmarketing.util.UtilMethods.isSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertiesService;
import com.dotcms.plugin.saml.v3.parameters.DotsamlPropertyName;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Handles the http post
 *
 * @author jsanca
 */

public class HttpPostAssertionResolverHandlerImpl implements AssertionResolverHandler {
	private static final long serialVersionUID = 3479922364325870009L;

	// This is the key to get the saml response from the request.
	private static final String SAML_RESPONSE_KEY = "SAMLResponse";

	@Override
	public boolean isValidSamlRequest(final HttpServletRequest request, final HttpServletResponse response,
			final IdpConfig idpConfig) {
		return isSet(request.getParameter(SAML_RESPONSE_KEY));
	}

	@Override
	public Assertion resolveAssertion(final HttpServletRequest request, final HttpServletResponse response,
			IdpConfig idpConfig) throws DotDataException, IOException, JSONException {
		Assertion assertion = null;
		HTTPPostDecoder decoder = new HTTPPostDecoder();
		MessageContext<SAMLObject> messageContext = null;
		Response samlResponse = null;

		Logger.debug(this, "Resolving SAML Artifact with AssertionResolverHandler implementation: " + this.getClass());

		try {
			Logger.debug(this, "Decoding the Post message: " + request.getParameter(SAML_RESPONSE_KEY));

			decoder.setHttpServletRequest(request);
			decoder.setParserPool(XMLObjectProviderRegistrySupport.getParserPool());

			decoder.initialize();
			decoder.decode();

			messageContext = decoder.getMessageContext();
			samlResponse = (Response) messageContext.getMessage();

			Logger.debug(this, "Post message context decoded:");
			Logger.debug(this, "\n\n" + toXMLObjectString(samlResponse));

		} catch (ComponentInitializationException | MessageDecodingException e) {
			Logger.error(this, "Error decoding inbound message context for IdP '" + idpConfig.getIdpName() + "'", e);
			throw new DotSamlException(e.getMessage(), e);
		} finally {
			decoder.destroy();
		}

		this.validateDestinationAndLifetime(messageContext, request, idpConfig);

		assertion = getAssertion(samlResponse, idpConfig);

		Logger.debug(this, "Decrypted Assertion:");
		Logger.debug(this, "\n\n" + toXMLObjectString(assertion));

		// Verify Signatures.
		verifyResponseSignature(samlResponse, idpConfig);
		verifyAssertionSignature(assertion, idpConfig);

		this.verifyStatus(samlResponse);

		return assertion;
	}

	private void verifyStatus(final Response response) {
		final Status status = response.getStatus();
		final StatusCode statusCode = status.getStatusCode();
		final String statusCodeURI = statusCode.getValue();

		if (!statusCodeURI.equals(StatusCode.SUCCESS)) {
			Logger.error(this, "SAML status code was NOT successful: " + statusCode.getStatusCode().getValue());
			throw new DotSamlException("SAML status code was NOT successful: " + statusCode.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private void validateDestinationAndLifetime(final MessageContext<SAMLObject> context,
			final HttpServletRequest request, final IdpConfig idpConfig) {

		// Just setting it to a value in case of exception.
		long clockSkew = DOT_SAML_CLOCK_SKEW_DEFAULT_VALUE;
		long lifeTime = DOT_SAML_MESSAGE_LIFE_DEFAULT_VALUE;

		try {
			Integer intClockSkew = DotsamlPropertiesService.getOptionInteger(idpConfig,
					DotsamlPropertyName.DOT_SAML_CLOCK_SKEW);
			if (intClockSkew != null) {
				clockSkew = new Long(intClockSkew).longValue();
			}

		} catch (Exception exception) {

			Logger.info(this,
					"Optional property not set: " + DotsamlPropertyName.DOT_SAML_CLOCK_SKEW + ". Using default.");
		}

		try {

			Integer intLifeTime = DotsamlPropertiesService.getOptionInteger(idpConfig,
					DotsamlPropertyName.DOT_SAML_MESSAGE_LIFE_TIME);
			if (intLifeTime != null) {
				lifeTime = new Long(intLifeTime).longValue();
			}

		} catch (Exception exception) {

			Logger.info(this, "Optional property not set: "
					+ DotsamlPropertyName.DOT_SAML_MESSAGE_LIFE_TIME.getPropertyName() + ". Using default.");
		}

		final SAMLMessageInfoContext messageInfoContext = context.getSubcontext(SAMLMessageInfoContext.class, true);
		final MessageLifetimeSecurityHandler lifetimeSecurityHandler = new MessageLifetimeSecurityHandler();
		final BasicMessageHandlerChain<SAMLObject> handlerChain = new BasicMessageHandlerChain<SAMLObject>();
		final List<MessageHandler<SAMLObject>> handlers = new ArrayList<MessageHandler<SAMLObject>>();
		final Response response = (Response) context.getMessage();
		
		messageInfoContext.setMessageIssueInstant(response.getIssueInstant());

		// message lifetime validation.
		lifetimeSecurityHandler.setClockSkew(clockSkew);
		lifetimeSecurityHandler.setMessageLifetime(lifeTime);
		lifetimeSecurityHandler.setRequiredRule(true);

		// validation of message destination.
		handlers.add(lifetimeSecurityHandler);
		handlerChain.setHandlers(handlers);

		invokeMessageHandlerChain(handlerChain, context);
	}

}
