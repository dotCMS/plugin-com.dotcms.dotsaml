package com.dotcms.plugin.saml.v3.config;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class IdpConfig
{
	private String id;
	private String idpName;
	private boolean enabled;
	private String sPIssuerURL;
	private String sPEndpointHostname;
	private File privateKey;
	private File publicCert;
	private File idPMetadataFile;
	private String signatureValidationType;
	private Properties optionalProperties;
	private Map<String, String> sites;

	public IdpConfig()
	{
		this.idpName = "";
		this.enabled = false;
		this.sPIssuerURL = "";
		this.sPEndpointHostname = "";
		this.privateKey = null;
		this.publicCert = null;
		this.idPMetadataFile = null;
		this.optionalProperties = new Properties();
	}

	@Override
	public boolean equals( Object object )
	{
		if ( this == object )
		{
			return true;
		}
		if ( object == null || getClass() != object.getClass() )
		{
			return false;
		}

		IdpConfig idpConfig = (IdpConfig) object;

		return Objects.equals( id, idpConfig.id );
	}

	public String getId()
	{
		return id;
	}

	public File getIdPMetadataFile()
	{
		return idPMetadataFile;
	}

	public String getIdpName()
	{
		return idpName;
	}

	public Properties getOptionalProperties()
	{
		return optionalProperties;
	}

	public File getPrivateKey()
	{
		return privateKey;
	}

	public File getPublicCert()
	{
		return publicCert;
	}

	public String getSignatureValidationType()
	{
		return signatureValidationType;
	}

	public String getSiteNames()
	{
		return sites.values().stream().map( Object::toString ).collect( Collectors.joining( ", " ) );
	}

	public Map<String, String> getSites()
	{
		return sites;
	}

	public String getSpEndpointHostname()
	{
		return sPEndpointHostname;
	}

	public String getSpIssuerURL()
	{
		return sPIssuerURL;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( id );
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public void setIdPMetadataFile( File idPMetadataFile )
	{
		this.idPMetadataFile = idPMetadataFile;
	}

	public void setIdpName( String idpName )
	{
		this.idpName = idpName;
	}

	public void setOptionalProperties( Properties optionalProperties )
	{
		this.optionalProperties = optionalProperties;
	}

	public void setPrivateKey( File privateKey )
	{
		this.privateKey = privateKey;
	}

	public void setPublicCert( File publicCert )
	{
		this.publicCert = publicCert;
	}

	public void setSignatureValidationType( String signatureValidationType )
	{
		this.signatureValidationType = signatureValidationType;
	}

	public void setSites( Map<String, String> sites )
	{
		this.sites = sites;
	}

	private String getSearchable(){
		StringBuilder sb = new StringBuilder();
	
		//config name.
		sb.append(this.idpName);
		sb.append(" ");
		//SP Issuer URL.
		sb.append(this.sPIssuerURL);
		sb.append(" ");
		//SP Endpoint Hostname.
		sb.append(this.sPEndpointHostname);
		sb.append(" ");
		//sites related to the IdP.
		for (Map.Entry<String,String> entry : this.sites.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
			sb.append(" ");
		}
		//any override parameter.
		sb.append(this.optionalProperties);
	
		return sb.toString();
	}
	public boolean contains(String string){
		return getSearchable().toLowerCase().contains(string.trim().toLowerCase());
	}
	
	public void setSpEndpointHostname( String sPEndpointHostname )
	{
		this.sPEndpointHostname = sPEndpointHostname;
	}

	public void setSpIssuerURL( String sPIssuerURL )
	{
		this.sPIssuerURL = sPIssuerURL;
	}
}
