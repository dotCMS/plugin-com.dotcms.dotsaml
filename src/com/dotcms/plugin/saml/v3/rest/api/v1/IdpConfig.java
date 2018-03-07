package com.dotcms.plugin.saml.v3.rest.api.v1;

public class IdpConfig {

    private String idpName;

    public IdpConfig(String idpName) {
        this.idpName = idpName;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }
}
