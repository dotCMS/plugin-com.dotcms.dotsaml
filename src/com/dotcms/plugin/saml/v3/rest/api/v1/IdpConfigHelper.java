package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.google.common.collect.Lists;

import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.WebResource;
import com.dotmarketing.exception.DotDataException;
import com.liferay.portal.language.LanguageException;
import com.liferay.portal.model.User;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class IdpConfigHelper implements Serializable {

    public IdpConfigHelper() {
        this.webResource = new WebResource();
    }

    private static class SingletonHolder {
        private static final IdpConfigHelper INSTANCE = new IdpConfigHelper();
    }

    public static IdpConfigHelper getInstance() {
        return IdpConfigHelper.SingletonHolder.INSTANCE;
    }

    private final WebResource webResource;


    public List<IdpConfig> getIdpConfigs(HttpServletRequest request) throws DotDataException, LanguageException {
        final InitDataObject initData = this.webResource.init(null, true, request, true, null); // should logged in
        final User user = initData.getUser();

        //TODO: Dummy data.
        IdpConfig idpConfigA = new IdpConfig("idp.A.sitename");
        IdpConfig idpConfigB = new IdpConfig("idp.B.sitename");
        IdpConfig idpConfigC = new IdpConfig("idp.C.sitename");

        List<IdpConfig> configs = Lists.newArrayList();

        configs.add(idpConfigA);
        configs.add(idpConfigB);
        configs.add(idpConfigC);

        return configs;
    }
}
