package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

public class IdpJsonTransformer {

    public static JSONObject idpToJson(IdpConfig idpConfig) throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("id", idpConfig.getId());
        jo.put("idpName", idpConfig.getIdpName());
        jo.put("enabled", idpConfig.isEnabled());
        jo.put("sPIssuerURL", idpConfig.getsPIssuerURL());
        jo.put("sPEndponintHostname", idpConfig.getsPEndponintHostname());
        jo.put("optionalProperties", idpConfig.getOptionalProperties());

        return jo;
    }

    public static IdpConfig jsonToIdp(JSONObject jsonObject) throws JSONException {
        IdpConfig idpConfig = new IdpConfig();

        idpConfig.setId(jsonObject.getString("id"));
        idpConfig.setIdpName(jsonObject.getString("idpName"));
        idpConfig.setEnabled(jsonObject.getBoolean("enabled"));
        idpConfig.setsPIssuerURL(jsonObject.getString("sPIssuerURL"));
        idpConfig.setsPEndponintHostname(jsonObject.getString("sPEndponintHostname"));
        idpConfig.setOptionalProperties(jsonObject.getString("optionalProperties"));

        return idpConfig;
    }
}
