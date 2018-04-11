package com.dotcms.plugin.saml.v3.rest.api.v1;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class IdpConfig {

    private String id;
    private String idpName;
    private boolean enabled;
    private String sPIssuerURL;
    private String sPEndponintHostname;
    private File privateKey;
    private File publicCert;
    private File idPMetadataFile;
    private String signatureValidationType;
    private Properties optionalProperties;
    private Map<String, String> sites;

    private IdpConfig() {
    }

    public static class Builder {
        private IdpConfig idpConfigToBuild;

        Builder() {
            idpConfigToBuild = new IdpConfig();
        }

        IdpConfig build() {
            IdpConfig builtIdpConfig = idpConfigToBuild;
            idpConfigToBuild = new IdpConfig();

            return builtIdpConfig;
        }

        public Builder id(String id) {
            this.idpConfigToBuild.id = id;
            return this;
        }

        public Builder idpName(String idpName) {
            this.idpConfigToBuild.idpName = idpName;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.idpConfigToBuild.enabled = enabled;
            return this;
        }

        public Builder sPIssuerURL(String sPIssuerURL) {
            this.idpConfigToBuild.sPIssuerURL = sPIssuerURL;
            return this;
        }

        public Builder sPEndponintHostname(String sPEndponintHostname) {
            this.idpConfigToBuild.sPEndponintHostname = sPEndponintHostname;
            return this;
        }

        public Builder privateKey(File privateKey) {
            this.idpConfigToBuild.privateKey = privateKey;
            return this;
        }

        public Builder publicCert(File publicCert) {
            this.idpConfigToBuild.publicCert = publicCert;
            return this;
        }

        public Builder idPMetadataFile(File idPMetadataFile) {
            this.idpConfigToBuild.idPMetadataFile = idPMetadataFile;
            return this;
        }

        public Builder signatureValidationType(String signatureValidationType) {
            this.idpConfigToBuild.signatureValidationType = signatureValidationType;
            return this;
        }

        public Builder optionalProperties(Properties optionalProperties) {
            this.idpConfigToBuild.optionalProperties = optionalProperties;
            return this;
        }

        public Builder sites(Map<String, String> sites) {
            this.idpConfigToBuild.sites = sites;
            return this;
        }
    }

    public static IdpConfig.Builder convertIdpConfigToBuilder(IdpConfig idpConfig){
        IdpConfig.Builder builder = new IdpConfig.Builder();

        builder.id(idpConfig.getId())
                .idpName(idpConfig.getIdpName())
                .enabled(idpConfig.isEnabled())
                .sPIssuerURL(idpConfig.getsPIssuerURL())
                .sPEndponintHostname(idpConfig.getsPEndponintHostname())
                .privateKey(idpConfig.getPrivateKey())
                .publicCert(idpConfig.getPublicCert())
                .idPMetadataFile(idpConfig.getIdPMetadataFile())
                .signatureValidationType(idpConfig.getSignatureValidationType())
                .optionalProperties(idpConfig.getOptionalProperties())
                .sites(idpConfig.getSites());

        return builder;
    }

    public String getId() {
        return id;
    }

    public String getIdpName() {
        return idpName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getsPIssuerURL() {
        return sPIssuerURL;
    }

    public String getsPEndponintHostname() {
        return sPEndponintHostname;
    }

    public File getPrivateKey() {
        return privateKey;
    }

    public File getPublicCert() {
        return publicCert;
    }

    public File getIdPMetadataFile() {
        return idPMetadataFile;
    }

    public String getSignatureValidationType() {
        return signatureValidationType;
    }

    public Properties getOptionalProperties() {
        return optionalProperties;
    }

    public Map<String, String> getSites() {
        return sites;
    }

    private String getSearchable() {
        StringBuilder sb = new StringBuilder();

        //config name.
        sb.append(this.idpName);
        sb.append(" ");
        //SP Issuer URL.
        sb.append(this.sPIssuerURL);
        sb.append(" ");
        //SP Endpoint Hostname.
        sb.append(this.sPEndponintHostname);
        sb.append(" ");
        //sites related to the IdP.
        for (Map.Entry<String, String> entry : this.sites.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" ");
            sb.append(entry.getValue());
            sb.append(" ");
        }
        //any override parameter.
        sb.append(this.optionalProperties);

        return sb.toString();
    }

    public boolean contains(String string) {
        return getSearchable().toLowerCase().contains(string.trim().toLowerCase());
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
