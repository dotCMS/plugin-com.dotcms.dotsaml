package com.dotcms.plugin.saml.v3;

/**
 * Exception to report things related to the dot saml exception
 * @author jsanca
 */
public class DotSamlException extends RuntimeException {

    public DotSamlException() {
    }

    public DotSamlException(String message) {
        super(message);
    }

    public DotSamlException(String message, Throwable cause) {
        super(message, cause);
    }
} // E:O:F:DotSamlException.
