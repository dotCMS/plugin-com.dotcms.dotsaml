package com.dotcms.plugin.saml.v3.handler;

import com.dotcms.plugin.saml.v3.config.IdpConfig;
import com.dotcms.plugin.saml.v3.config.IdpConfigHelper;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.key.DotSamlConstants;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.*;

import static com.dotcms.plugin.saml.v3.util.SamlUtils.*;
import static com.dotmarketing.util.UtilMethods.isSet;

/**
 * Handles the http post
 *
 * @author jsanca
 */
public class HttpPostAssertionResolverHandlerImpl implements AssertionResolverHandler
{
	private static final long serialVersionUID = 3479922364325870009L;

	// This is the key to get the saml response from the request.
	private static final String SAML_RESPONSE_KEY = "SAMLResponse";

	@Override
	public boolean isValidSamlRequest( final HttpServletRequest request, final HttpServletResponse response, final IdpConfig idpConfig )
	{
		return isSet( request.getParameter( SAML_RESPONSE_KEY ) );
	}

	@Override
	public Assertion resolveAssertion( final HttpServletRequest request, final HttpServletResponse response, IdpConfig idpConfig ) throws DotDataException, IOException, JSONException
	{
		Assertion assertion = null;
		HTTPPostDecoder decoder = new HTTPPostDecoder();
		MessageContext<SAMLObject> messageContext = null;
		Response samlResponse = null;

		Logger.debug( this, "Resolving the Artifact with the implementation: " + this.getClass() );

		try
		{
			Logger.debug( this, "Decoding the Post message: " + request.getParameter( SAML_RESPONSE_KEY ) );

			decoder.setHttpServletRequest( request );
			decoder.setParserPool( XMLObjectProviderRegistrySupport.getParserPool() );

			decoder.initialize();
			decoder.decode();

			messageContext = decoder.getMessageContext();
			samlResponse = (Response) messageContext.getMessage();

			Logger.debug( this, "Post message context decoded: " + toXMLObjectString( samlResponse ) );

		}
		catch ( ComponentInitializationException | MessageDecodingException e )
		{
			Logger.error( this, "Error decoding inbound message context", e );
			throw new DotSamlException( e.getMessage(), e );
		}
		finally
		{
			decoder.destroy();
		}

		this.validateDestinationAndLifetime( messageContext, request, idpConfig );

		assertion = getAssertion( samlResponse, idpConfig );

		Logger.debug( this, "Decrypted Assertion: " + toXMLObjectString( assertion ) );

		if ( idpConfig.isVerifyAssertionSignatureNeeded() )
		{
			Logger.debug( this, "Doing the verification assertion signature." );

			verifyAssertionSignature( assertion, idpConfig );

			this.verifyStatus( samlResponse );
		}
		else
		{
			Logger.debug( this, "The verification assertion signature and status code was skipped." );
		}

		Logger.debug( this, "Decrypted Assertion: " + toXMLObjectString( assertion ) );

		return assertion;
	}

	private void verifyStatus( final Response response )
	{
		final Status status = response.getStatus();
		final StatusCode statusCode = status.getStatusCode();
		final String statusCodeURI = statusCode.getValue();

		if ( !statusCodeURI.equals( StatusCode.SUCCESS ) )
		{
			Logger.error( this, "Incorrect SAML message code : " + statusCode.getStatusCode().getValue() );
			throw new DotSamlException( "Incorrect SAML message code : " + statusCode.getValue() );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void validateDestinationAndLifetime( final MessageContext<SAMLObject> context, final HttpServletRequest request, final IdpConfig idpConfig )
	{
		long clockSkew = DOT_SAML_CLOCK_SKEW_DEFAULT_VALUE;
		long lifeTime = DOT_SAML_MESSAGE_LIFE_DEFAULT_VALUE;

		try
		{
			clockSkew = (long) idpConfig.getOptionalProperties().get( DotSamlConstants.DOT_SAML_CLOCK_SKEW );
		}
		catch ( Exception exception )
		{
			Logger.info( this, "Optional property not set: " + DotSamlConstants.DOT_SAML_CLOCK_SKEW + " Using default." );
		}

		try
		{
			lifeTime = (long) idpConfig.getOptionalProperties().get( DotSamlConstants.DOT_SAML_MESSAGE_LIFE_TIME );
		}
		catch ( Exception exception )
		{
			Logger.info( this, "Optional property not set: " + DotSamlConstants.DOT_SAML_MESSAGE_LIFE_TIME + " Using default." );
		}

		final SAMLMessageInfoContext messageInfoContext = context.getSubcontext( SAMLMessageInfoContext.class, true );
		final MessageLifetimeSecurityHandler lifetimeSecurityHandler = new MessageLifetimeSecurityHandler();
		final BasicMessageHandlerChain<SAMLObject> handlerChain = new BasicMessageHandlerChain<SAMLObject>();
		final List<MessageHandler<SAMLObject>> handlers = new ArrayList<MessageHandler<SAMLObject>>();
		final Response response = (Response) context.getMessage();

		messageInfoContext.setMessageIssueInstant( response.getIssueInstant() );

		// message lifetime validation.
		lifetimeSecurityHandler.setClockSkew( clockSkew );
		lifetimeSecurityHandler.setMessageLifetime( lifeTime );
		lifetimeSecurityHandler.setRequiredRule( true );

		// validation of message destination.
		handlers.add( lifetimeSecurityHandler );
		handlerChain.setHandlers( handlers );

		invokeMessageHandlerChain( handlerChain, context );
	}
}
