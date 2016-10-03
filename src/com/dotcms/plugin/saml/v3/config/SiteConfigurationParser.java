package com.dotcms.plugin.saml.v3.config;

import com.dotcms.plugin.saml.v3.InputStreamUtils;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotcms.repackage.org.json.JSONArray;
import com.dotcms.repackage.org.json.JSONException;
import com.dotcms.repackage.org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Take a json and creates a SiteConfigurationBean
 * @author jsanca
 */
public class SiteConfigurationParser implements Serializable {

    /**
     * Parser a json into a map String -> {@link SiteConfigurationBean}
     * @param resourceName {@link String}
     * @return Map
     */
    public Map<String, SiteConfigurationBean> parser (final String resourceName) throws IOException, JSONException {

        final JSONObject config     = new JSONObject(this.getSource(resourceName));
        final JSONArray sitesArray  = config.getJSONArray("config");
        final Map<String, SiteConfigurationBean> configurationMap = new HashMap<>();

        for (int i = 0; i < sitesArray.length(); ++i) {

            final JSONObject site          = sitesArray.getJSONObject(i);
            this.populateSite(site, configurationMap);
        }

        return Collections.unmodifiableMap(configurationMap);
    } // parser.

    private void populateSite(final JSONObject site,
                              final Map<String, SiteConfigurationBean> configurationMap) throws JSONException {

        final Map<String, String> siteMap = new HashMap<>();
        final JSONArray siteNames         = site.names();
        final String     siteName         = siteNames.getString(0);
        final JSONObject siteData         = site.getJSONObject(siteName);
        final JSONArray  siteDataNames    = siteData.names();
        for (int j = 0; j < siteDataNames.length(); ++j) {

            siteMap.put(siteDataNames.getString(j), siteData.getString(siteDataNames.getString(j)));
        }

        configurationMap.put(siteName, new SiteConfigurationBean(siteMap));
    } // populateSite.

    /**
     * Get the config as String
     * @param resourceName String, could be a class path resource or filesyste,=m
     * @return String
     * @throws IOException
     */
    private String getSource(final String resourceName) throws IOException {

        return IOUtils.toString(InputStreamUtils.getInputStream(resourceName));
    } // getSource.


    public static void main(String[] args) throws Exception {

        final SiteConfigurationParser parser =
                new SiteConfigurationParser();

        System.out.println(parser.parser("file://sites-config.json"));
    }
} // E:O:F:SiteConfigurationParser.
