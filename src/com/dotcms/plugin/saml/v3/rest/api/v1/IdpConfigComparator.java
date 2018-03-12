package com.dotcms.plugin.saml.v3.rest.api.v1;

import java.util.Comparator;

public class IdpConfigComparator implements Comparator<IdpConfig>{

    @Override
    public int compare(IdpConfig idpConfig1, IdpConfig idpConfig2) {
        return idpConfig1.getIdpName().compareToIgnoreCase(idpConfig2.getIdpName());
    }
}
