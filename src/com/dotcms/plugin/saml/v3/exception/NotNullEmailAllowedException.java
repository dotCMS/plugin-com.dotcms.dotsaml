package com.dotcms.plugin.saml.v3.exception;

import javax.servlet.http.HttpServletResponse;

public class NotNullEmailAllowedException extends AttributesNotFoundException {

    public NotNullEmailAllowedException() {
    }

    public NotNullEmailAllowedException(String message) {
        super(message);
    }

    public NotNullEmailAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatus () {

        return HttpServletResponse.SC_UNAUTHORIZED;
    }
}
