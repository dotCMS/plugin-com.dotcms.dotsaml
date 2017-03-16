package com.dotcms.plugin.saml.v3.exception;

/**
 * Runtime exception used to handle errors when attributes might not be extracted from the Assertion object
 * Created by nollymar on 3/15/17.
 */
public class AttributesNotFoundException extends RuntimeException {

    public AttributesNotFoundException() {

    }

    public AttributesNotFoundException(String message) {
        super(message);
    }

    public AttributesNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} // E:O:F:AttributesNotFoundException.

