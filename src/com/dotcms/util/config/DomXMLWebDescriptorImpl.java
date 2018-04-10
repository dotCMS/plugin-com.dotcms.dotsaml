package com.dotcms.util.config;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the web descriptor by document XML
 * 
 * @author jsanca
 */
class DomXMLWebDescriptorImpl implements WebDescriptor
{
	private static final long serialVersionUID = -5203919465487623795L;
	private final Document document;

	protected DomXMLWebDescriptorImpl( final File file ) throws Exception
	{
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.document = builder.parse( file );
	}

	protected DomXMLWebDescriptorImpl( final InputStream stream ) throws Exception
	{
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.document = builder.parse( stream );
	}

	@Override
	public boolean existsElement( final String elementTag, final String subElementTag, final String elementValue )
	{
		final NodeList nodeList = this.document.getElementsByTagName( elementTag );
		Node currentNode = null;
		Element currentElement = null;
		String subElementValue = null;

		for ( int i = 0; i < nodeList.getLength(); ++i )
		{

			currentNode = nodeList.item( i );

			if ( currentNode.getNodeType() == Node.ELEMENT_NODE )
			{

				currentElement = (Element) currentNode; // <filter>
				subElementValue = this.getChildText( currentElement, subElementTag );

				if ( elementValue.equals( subElementValue ) )
				{

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void addFilterBefore( final String filterName, final FilterConfigBean filter )
	{
		final Element filterElement = this.createFilterElement( filter );
		final Element filterMappingElement = this.createFilterMappingElement( filter );

		this.addElementBefore( filterName, filterElement, "filter", "filter-name" );
		this.addElementBefore( filterName, filterMappingElement, "filter-mapping", "filter-name" );
	}

	@Override
	public void removeFilter( final Class filter )
	{
		this.removeFilter( filter.getName() );
	}

	@Override
	public void removeFilter( String filterClassName )
	{
		final Element filterElement = this.findElement( "filter", "filter-class", filterClassName );
		Node parentNode;

		if ( null != filterElement )
		{

			parentNode = filterElement.getParentNode();
			parentNode.removeChild( filterElement );
		}
	}

	protected Element findElement( final String elementNameToFind, final String subElementNameToFind, final String elementValue )
	{
		Node currentNode = null;
		Element currentElement = null;
		String subElementValue = null;
		final NodeList nodeList = this.document.getElementsByTagName( elementNameToFind );

		for ( int i = 0; i < nodeList.getLength(); ++i )
		{

			currentNode = nodeList.item( i );

			if ( currentNode.getNodeType() == Node.ELEMENT_NODE )
			{

				currentElement = (Element) currentNode; // <filter>
				subElementValue = this.getChildText( currentElement, subElementNameToFind );

				if ( elementValue.equals( subElementValue ) )
				{

					return currentElement;
				}
			}
		}

		return null;
	}

	/**
	 * Adds an element "newElement" before the element with the name:
	 * "beforeElementName" The element name to find is "elementNameToFind", for
	 * instance if you want to add a filter before another one, you have to set
	 * it as "filter" The sub element name to get the text to compare to
	 * "beforeFilterName" is "subElementNameToFind", for instance for a filter
	 * it will be "filter-name".
	 *
	 * @param beforeElementName
	 *            {@link String}
	 * @param newElement
	 *            {@link Element}
	 * @param elementNameToFind
	 *            {@link String}
	 * @param subElementNameToFind
	 *            {@link String}
	 */
	protected void addElementBefore( final String beforeElementName, final Element newElement, final String elementNameToFind, final String subElementNameToFind )
	{
		Node currentNode = null;
		Element currentElement = null;
		String elementName = null;
		boolean added = false;
		final NodeList nodeList = this.document.getElementsByTagName( elementNameToFind );

		for ( int i = 0; i < nodeList.getLength(); ++i )
		{
			currentNode = nodeList.item( i );

			if ( currentNode.getNodeType() == Node.ELEMENT_NODE )
			{
				currentElement = (Element) currentNode; // <filter>
				elementName = this.getChildText( currentElement, subElementNameToFind );

				if ( beforeElementName.equals( elementName ) )
				{
					currentElement.getParentNode().insertBefore( newElement, currentElement );
					added = true;
					break;
				}
			}
		}

		// if couldn't find the beforeFilterName, adds the newElement at the end of the list.
		if ( !added && null != currentElement )
		{

			this.document.insertBefore( currentElement, newElement );
		}
	}

	protected String getChildText( final Element currentElement, final String tagName )
	{

		String text = null;
		final NodeList nodeList = currentElement.getElementsByTagName( tagName );

		if ( nodeList.getLength() > 0 )
		{

			text = nodeList.item( 0 ).getTextContent();
		}

		return text;
	}

	protected Element createFilterElement( final FilterConfigBean filter )
	{
		/**
		 * <filter> <filter-name>CookiesFilter</filter-name>
		 * <filter-class>com.dotmarketing.filters.CookiesFilter</filter-class>
		 * </filter>
		 */
		final Element filterElement = this.document.createElement( "filter" );
		final Element filterNameElement = this.document.createElement( "filter-name" );
		final Element filterClassElement = this.document.createElement( "filter-class" );

		filterNameElement.appendChild( this.document.createTextNode( filter.getFilterClass().getSimpleName() ) );
		filterClassElement.appendChild( this.document.createTextNode( filter.getFilterClass().getName() ) );

		filterElement.appendChild( filterNameElement );
		filterElement.appendChild( filterClassElement );

		return filterElement;
	}

	protected Element createFilterMappingElement( final FilterConfigBean filter )
	{
		/**
		 * <filter-mapping> <filter-name>AutoLoginFilter</filter-name>
		 * <url-pattern>/*</url-pattern> </filter-mapping>
		 */

		final Element filterMappingElement = this.document.createElement( "filter-mapping" );
		final Element filterMappingNameElement = this.document.createElement( "filter-name" );
		final Element filterMappingUrlPatternElement = this.document.createElement( "url-pattern" );

		filterMappingNameElement.appendChild( this.document.createTextNode( filter.getFilterClass().getSimpleName() ) );
		filterMappingUrlPatternElement.appendChild( this.document.createTextNode( filter.getUrlPattern() ) );

		filterMappingElement.appendChild( filterMappingNameElement );
		filterMappingElement.appendChild( filterMappingUrlPatternElement );

		return filterMappingElement;
	}

	@Override
	public void transform( final String file ) throws Exception
	{
		this.transform( new File( file ) );
	}

	@Override
	public void transform( final File file ) throws Exception
	{
		this.transform( new FileOutputStream( file ) );
	}

	@Override
	public void transform( final OutputStream outputStream ) throws Exception
	{
		this.transform( new OutputStreamWriter( outputStream, "ISO-8859-1" ) );
	}

	@Override
	public void transform( final Writer writer ) throws Exception
	{
		final Source source = new DOMSource( this.document );
		StreamResult result = new StreamResult( writer );
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform( source, result );
	}
}
