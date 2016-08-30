package com.dotcms.plugin.saml.v3;

/**
 * Encapsulates the idp meta datas binding tye names
 */
public enum BindingType {

    AUTHN_REQUEST("urn:mace:shibboleth:1.0:profiles:AuthnRequest"),
    POST("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"),
    REDIRECT("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"),
    SOAP("urn:oasis:names:tc:SAML:2.0:bindings:SOAP");

    private final String binding;

    private BindingType(final String value) {
        this.binding = value;
    }

    public String getBinding() {
        return binding;
    }
} // E:O:F:BindingType.
