package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class IdpJsonTransformer {

    public static JSONObject idpToJson(IdpConfig idpConfig) throws JSONException, IOException {
        JSONObject jo = new JSONObject();

        jo.put("id", idpConfig.getId());
        jo.put("idpName", idpConfig.getIdpName());
        jo.put("enabled", idpConfig.isEnabled());
        jo.put("sPIssuerURL", idpConfig.getsPIssuerURL());
        jo.put("sPEndponintHostname", idpConfig.getsPEndponintHostname());
        jo.put("privateKey", getCanonicalPathIfExists(idpConfig.getPrivateKey()));
        jo.put("publicCert", getCanonicalPathIfExists(idpConfig.getPublicCert()));
        jo.put("idPMetadataFile", getCanonicalPathIfExists(idpConfig.getIdPMetadataFile()));
        jo.put("signatureValidationType", idpConfig.getSignatureValidationType());
        jo.put("optionalProperties", getJsonObjectFromProperties(idpConfig.getOptionalProperties()));
        jo.put("sites", SiteJsonTransformer.getJsonObjecFromtMap(idpConfig.getSites()));

        return jo;
    }

    public static IdpConfig jsonToIdp(JSONObject jsonObject) throws JSONException {
        IdpConfig.Builder builder = new IdpConfig.Builder();

        builder.id(jsonObject.getString("id"));
        builder.idpName(jsonObject.getString("idpName"));
        builder.enabled(jsonObject.getBoolean("enabled"));
        builder.sPIssuerURL(jsonObject.getString("sPIssuerURL"));
        builder.sPEndponintHostname(jsonObject.getString("sPEndponintHostname"));
        builder.privateKey(getFileFromCanonicalPath(jsonObject.getString("privateKey")));
        builder.publicCert(getFileFromCanonicalPath(jsonObject.getString("publicCert")));
        builder.idPMetadataFile(getFileFromCanonicalPath(jsonObject.getString("idPMetadataFile")));
        builder.signatureValidationType(jsonObject.getString("signatureValidationType"));
        builder.optionalProperties(getPropertiesFromJsonObject(jsonObject.getJSONObject("optionalProperties")));
        builder.sites(SiteJsonTransformer. getMapFromJsonObject(jsonObject.getJSONObject("sites")));

        return builder.build();
    }

    private static String getCanonicalPathIfExists(File file) throws IOException{
        String canonicalPath = "";
        if (file != null){
            canonicalPath = file.getCanonicalPath();
        }
        return canonicalPath;
    }

    private static File getFileFromCanonicalPath(String canonicalPath){
        File file = null;

        if (UtilMethods.isSet(canonicalPath)){
            File fileFromPath = new File(canonicalPath);
            if (fileFromPath.exists()){
                file = fileFromPath;
            } else {
                Logger.error(IdpJsonTransformer.class, "File doesn't exists: " + canonicalPath);
            }
        }

        return file;
    }

    private static JSONObject getJsonObjectFromProperties(Properties properties) throws JSONException {
        JSONObject jo = new JSONObject();

        if (UtilMethods.isSet(properties)){
            for (String key : properties.stringPropertyNames()) {
                jo.put(key, properties.getProperty(key));
            }
        }

        return jo;
    }

    private static Properties getPropertiesFromJsonObject(JSONObject jo) throws JSONException {
        Properties properties = new Properties();
        Iterator<?> keys = jo.keys();

        while( keys.hasNext() ) {
            String key = (String)keys.next();
            String value = jo.getString(key);

            properties.setProperty(key, value);
        }

        return properties;
    }

}
