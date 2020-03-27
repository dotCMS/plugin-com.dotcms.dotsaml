package com.dotcms.util.config;

import java.io.Serializable;

/**
 * Encapsulate a Filter configuration.
 * 
 * @author jsanca
 */
public class FilterConfigBean implements Serializable
{
	private static final long serialVersionUID = 1798102104840611958L;
	private final Class filterClass;
	private final String urlPattern;

	public FilterConfigBean( final Class filterClass, final String urlPattern )
	{
		this.filterClass = filterClass;
		this.urlPattern = urlPattern;
	}

	public Class getFilterClass()
	{
		return filterClass;
	}

	public String getUrlPattern()
	{
		return urlPattern;
	}
}
