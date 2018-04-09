package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SiteJsonTransformer {

    public static JSONObject getJsonObjecFromtMap(Map<String, String> map) throws JSONException {
        JSONObject jo = new JSONObject();

        if (UtilMethods.isSet(map)) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jo.put(entry.getKey(), entry.getValue());
            }
        }

        return jo;
    }

    public static Map<String, String> getMapFromJsonObject(JSONObject jo) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<?> keys = jo.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = jo.getString(key);

            map.put(key, value);
        }

        return map;
    }
}
