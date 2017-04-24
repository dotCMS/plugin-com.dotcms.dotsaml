package com.dotcms.plugin.saml.v3.config;

/**
 * Exception to report an issue with the issuer value.
 * @author jsanca
 */
public class InvalidIssuerValueException extends RuntimeException {

    public InvalidIssuerValueException(String message) {
        super(message);
    }
}
