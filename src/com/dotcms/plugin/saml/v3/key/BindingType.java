package com.dotcms.plugin.saml.v3.key;

/**
 * Encapsulates the idp meta datas binding tye names
 * 
 * @author jsanca
 */
public enum BindingType
{
	AUTHN_REQUEST( "urn:mace:shibboleth:1.0:profiles:AuthnRequest" ),
	POST( "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" ),
	REDIRECT( "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" );

	private final String binding;

	private BindingType( final String value )
	{
		this.binding = value;
	}

	public String getBinding()
	{
		return binding;
	}
}
