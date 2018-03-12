package com.dotcms.plugin.saml.v3.rest.api.v1;

import java.io.File;
import java.util.Objects;

public class IdpConfig {

    private String id;
    private String idpName;
    private boolean enabled;
    private String sPIssuerURL;
    private String sPEndponintHostname;
    private File privateKey;
    private File publicCert;
    private File idPMetadataFile;
    private String optionalProperties;

    public IdpConfig() {
        this.idpName = "";
        this.enabled = false;
        this.sPIssuerURL = "";
        this.sPEndponintHostname = "";
        this.privateKey = null;
        this.publicCert = null;
        this.idPMetadataFile = null;
        this.optionalProperties = "";
    }

    public IdpConfig(String idpName, boolean enabled, String sPIssuerURL, String sPEndponintHostname,
                     File privateKey, File publicCert, File idPMetadataFile, String optionalProperties) {
        this.idpName = idpName;
        this.enabled = enabled;
        this.sPIssuerURL = sPIssuerURL;
        this.sPEndponintHostname = sPEndponintHostname;
        this.privateKey = privateKey;
        this.publicCert = publicCert;
        this.idPMetadataFile = idPMetadataFile;
        this.optionalProperties = optionalProperties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getsPIssuerURL() {
        return sPIssuerURL;
    }

    public void setsPIssuerURL(String sPIssuerURL) {
        this.sPIssuerURL = sPIssuerURL;
    }

    public String getsPEndponintHostname() {
        return sPEndponintHostname;
    }

    public void setsPEndponintHostname(String sPEndponintHostname) {
        this.sPEndponintHostname = sPEndponintHostname;
    }

    public File getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(File privateKey) {
        this.privateKey = privateKey;
    }

    public File getPublicCert() {
        return publicCert;
    }

    public void setPublicCert(File publicCert) {
        this.publicCert = publicCert;
    }

    public File getIdPMetadataFile() {
        return idPMetadataFile;
    }

    public void setIdPMetadataFile(File idPMetadataFile) {
        this.idPMetadataFile = idPMetadataFile;
    }

    public String getOptionalProperties() {
        return optionalProperties;
    }

    public void setOptionalProperties(String optionalProperties) {
        this.optionalProperties = optionalProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdpConfig idpConfig = (IdpConfig) o;
        return Objects.equals(id, idpConfig.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
