package com.dotcms.plugin.saml.v3.meta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.opensaml.security.credential.Credential;

/**
 * Encapsulates the metadata bean stored in the idp-metadata.xml
 */
public class MetadataBean implements Serializable
{
	private static final long serialVersionUID = -2988699344160818527L;

	// the entity id on the xml
	private final String entityId;

	// the error url
	private final String errorURL;

	// list of single sign on location indexed by binding name
	private final Map<String, String> singleSignOnBindingLocationMap;

	// list of single logout on location indexed by binding name
	private final Map<String, String> singleLogoutBindingLocationMap;

	// credential signing list
	private final List<Credential> credentialSigningList;

	public MetadataBean( final String entityId, final String errorURL, final Map<String, String> singleSignOnBindingLocationMap, final Map<String, String> singleLogoutBindingLocationMap, final List<Credential> credentialSigningList )
	{
		this.entityId = entityId;
		this.errorURL = errorURL;
		this.singleSignOnBindingLocationMap = singleSignOnBindingLocationMap;
		this.credentialSigningList = credentialSigningList;
		this.singleLogoutBindingLocationMap = singleLogoutBindingLocationMap;
	}

	public String getEntityId()
	{
		return entityId;
	}

	public String getErrorURL()
	{
		return errorURL;
	}

	public Map<String, String> getSingleSignOnBindingLocationMap()
	{
		return singleSignOnBindingLocationMap;
	}

	public List<Credential> getCredentialSigningList()
	{
		return credentialSigningList;
	}

	public Map<String, String> getSingleLogoutBindingLocationMap()
	{
		return singleLogoutBindingLocationMap;
	}
}
