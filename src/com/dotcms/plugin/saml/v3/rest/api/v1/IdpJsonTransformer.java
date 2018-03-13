package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
        jo.put("sites", getJsonObjecFromtMap(idpConfig.getSites()));

        return jo;
    }

    public static IdpConfig jsonToIdp(JSONObject jsonObject) throws JSONException {
        IdpConfig idpConfig = new IdpConfig();

        idpConfig.setId(jsonObject.getString("id"));
        idpConfig.setIdpName(jsonObject.getString("idpName"));
        idpConfig.setEnabled(jsonObject.getBoolean("enabled"));
        idpConfig.setsPIssuerURL(jsonObject.getString("sPIssuerURL"));
        idpConfig.setsPEndponintHostname(jsonObject.getString("sPEndponintHostname"));
        idpConfig.setPrivateKey(getFileFromCanonicalPath(jsonObject.getString("privateKey")));
        idpConfig.setPublicCert(getFileFromCanonicalPath(jsonObject.getString("publicCert")));
        idpConfig.setIdPMetadataFile(getFileFromCanonicalPath(jsonObject.getString("idPMetadataFile")));
        idpConfig.setSignatureValidationType(jsonObject.getString("signatureValidationType"));
        idpConfig.setOptionalProperties(getPropertiesFromJsonObject(jsonObject.getJSONObject("optionalProperties")));
        idpConfig.setSites(getMapFromJsonObject(jsonObject.getJSONObject("sites")));

        return idpConfig;
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

    private static JSONObject getJsonObjecFromtMap(Map<String, String> map) throws JSONException {
        JSONObject jo = new JSONObject();

        if (UtilMethods.isSet(map)){
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                jo.put(entry.getKey(), entry.getValue());
            }
        }

        return jo;
    }

    private static Map<String, String> getMapFromJsonObject(JSONObject jo) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<?> keys = jo.keys();

        while( keys.hasNext() ) {
            String key = (String)keys.next();
            String value = jo.getString(key);

            map.put(key, value);
        }

        return map;
    }

}
