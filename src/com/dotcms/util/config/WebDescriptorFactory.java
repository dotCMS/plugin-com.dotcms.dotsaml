package com.dotcms.util.config;

import com.dotcms.plugin.saml.v3.filter.SamlAccessFilter;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Factory for a {@link WebDescriptor}
 * 
 * @author jsanca
 */
public class WebDescriptorFactory
{
	public static WebDescriptor createWebDescriptor( final String file ) throws Exception
	{
		return createWebDescriptor( new File( file ) );
	}

	public static WebDescriptor createWebDescriptor( final File file ) throws Exception
	{
		return new DomXMLWebDescriptorImpl( file );
	}

	public static WebDescriptor createWebDescriptor( final InputStream stream ) throws Exception
	{
		return new DomXMLWebDescriptorImpl( stream );
	}

	public static void main( String[] args ) throws Exception
	{
		WebDescriptor webDescriptor = WebDescriptorFactory.createWebDescriptor( "web.xml" );

		webDescriptor.addFilterBefore( "AutoLoginFilter", new FilterConfigBean( SamlAccessFilter.class, "/*" ) );

		StringWriter stringWriter = new StringWriter();
		webDescriptor.transform( stringWriter );

		System.out.println( stringWriter );
		String filterClassName = SamlAccessFilter.class.getName();
		System.out.println( "Looking for :" + filterClassName );
		System.out.println( stringWriter.toString().contains( filterClassName ) );

		System.out.println( "Exists SamlAccessFilter : " + webDescriptor.existsElement( "filter", "filter-name", SamlAccessFilter.class.getSimpleName() ) );

		webDescriptor.removeFilter( SamlAccessFilter.class );

		stringWriter = new StringWriter();
		webDescriptor.transform( stringWriter );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( "**********************************************" );
		System.out.println( stringWriter );
		System.out.println( "Looking for :" + filterClassName );
		System.out.println( stringWriter.toString().contains( filterClassName ) );
	}
}
