package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.json.JSONArray;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IdpConfigWriterReader {

    public static final String IDP_CONFIGS = "idpConfigs";

    public static File write(List<IdpConfig> idpConfigList, String idpConfigPath) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        for (IdpConfig idpConfig : idpConfigList) {
            final JSONObject jo = IdpJsonTransformer.idpToJson(idpConfig);
            jsonArray.add(jo);
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
                final JSONObject jo = jsonArray.getJSONObject(i);
                final IdpConfig idpConfig = IdpJsonTransformer.jsonToIdp(jo);
                idpConfigList.add(idpConfig);
            }
        }

        return idpConfigList;
    }
}
