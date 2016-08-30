package com.dotcms.plugin.saml.v3.meta;

import org.opensaml.security.credential.Credential;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the metadata bean stored in the idp-metadata.xml
 */
public class MetadataBean implements Serializable {
    // the entity id on the xml
    private final String entityId;

    // the error url
    private final String errorURL;

    // list of single sign on location indexed by binding name
    private final Map<String, String> singleSignOnBindingLocationMap;

    // credential signing list
    private final List<Credential> credentialSigningList;

    public MetadataBean(String entityId, String errorURL, Map<String, String> singleSignOnBindingLocationMap, List<Credential> credentialSigningList) {
        this.entityId = entityId;
        this.errorURL = errorURL;
        this.singleSignOnBindingLocationMap = singleSignOnBindingLocationMap;
        this.credentialSigningList = credentialSigningList;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getErrorURL() {
        return errorURL;
    }

    public Map<String, String> getSingleSignOnBindingLocationMap() {
        return singleSignOnBindingLocationMap;
    }

    public List<Credential> getCredentialSigningList() {
        return credentialSigningList;
    }
} // E:O:F:MetadataBean.
