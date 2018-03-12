package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.json.JSONArray;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IdpConfigWriterReader {

    public static final String IDP_CONFIGS = "samlConfigs";

    public static File write(List<IdpConfig> idpConfigList, String idpConfigPath) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        for (IdpConfig idpConfig : idpConfigList) {
            final JSONObject joIdp = IdpJsonTransformer.idpToJson(idpConfig);
            final JSONObject joOnlyId = new JSONObject().put(idpConfig.getId(), joIdp);
            jsonArray.add(joOnlyId);
        }

        JSONObject jo = new JSONObject();
        jo.put(IDP_CONFIGS, jsonArray);

        File idpConfigFile = new File(idpConfigPath);
        if (!idpConfigFile.exists()) {
            idpConfigFile.getParentFile().mkdirs();
            idpConfigFile.createNewFile();
        }

        try (FileWriter file = new FileWriter(idpConfigFile)) {
            file.write(jo.toString());

        }

        return new File(idpConfigPath);
    }

    public static List<IdpConfig> read(final File idpConfigFile) throws IOException, JSONException {
        List<IdpConfig> idpConfigList = new ArrayList<>();

        if (idpConfigFile.exists()) {
            String content = new String(Files.readAllBytes(idpConfigFile.toPath()));
            JSONObject jsonObject = new JSONObject(content);
            final JSONArray jsonArray = jsonObject.getJSONArray(IDP_CONFIGS);

            for (int i = 0; i < jsonArray.size(); i++) {
                //joId = UUID:{idpConfigs}
                final JSONObject joId = jsonArray.getJSONObject(i);

                //I don't like this hack but we need to get the id.
                Iterator<String> keys = joId.keys();
                String idpId = keys.next();

                //Now we can get the real JSONObject.
                final JSONObject jo = joId.getJSONObject(idpId);
                final IdpConfig idpConfig = IdpJsonTransformer.jsonToIdp(jo);
                idpConfigList.add(idpConfig);
            }
        }

        return idpConfigList;
    }
}
