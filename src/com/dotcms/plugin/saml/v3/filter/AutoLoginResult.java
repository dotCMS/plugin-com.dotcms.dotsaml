package com.dotcms.plugin.saml.v3.filter;

import javax.servlet.http.HttpSession;

// todo: no migrated
public class AutoLoginResult
{
	private final HttpSession session;
	private final boolean autoLogin;

	public AutoLoginResult( HttpSession session, boolean autoLogin )
	{
		this.session = session;
		this.autoLogin = autoLogin;
	}

	public HttpSession getSession()
	{
		return session;
	}

	public boolean isAutoLogin()
	{
		return autoLogin;
	}
}
