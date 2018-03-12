package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.liferay.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class IdpConfigHelper implements Serializable {

    private final String assetsPath;
    private final String idpfilePath;

    public IdpConfigHelper() {
        this.assetsPath = Config.getStringProperty("ASSET_REAL_PATH",
            FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", "/assets")));
        this.idpfilePath = assetsPath + File.separator + "saml" + File.separator + "config.json";
    }

    private static class SingletonHolder {
        private static final IdpConfigHelper INSTANCE = new IdpConfigHelper();
    }

    public static IdpConfigHelper getInstance() {
        return IdpConfigHelper.SingletonHolder.INSTANCE;
    }

    public List<IdpConfig> getIdpConfigs() throws IOException, JSONException {
        return IdpConfigWriterReader.read(new File(idpfilePath));
    }

    public IdpConfig saveIdpConfig(IdpConfig idpConfig) throws IOException, JSONException {
        List<IdpConfig> idpConfigList = getIdpConfigs();

        if (UtilMethods.isSet(idpConfig.getId())){
            //Update.
            idpConfigList.remove(idpConfig);
            idpConfigList.add(idpConfig);
            IdpConfigWriterReader.write(idpConfigList, idpfilePath);
        } else {
            //Create.
            idpConfig.setId(UUIDGenerator.generateUuid());
            idpConfigList.add(idpConfig);
            IdpConfigWriterReader.write(idpConfigList, idpfilePath);
        }

        return idpConfig;
    }

    public void deleteIdpConfig(IdpConfig idpConfig) throws IOException, JSONException {
        List<IdpConfig> idpConfigList = getIdpConfigs();

        if (idpConfigList.contains(idpConfig)){
            idpConfigList.remove(idpConfig);
            IdpConfigWriterReader.write(idpConfigList, idpfilePath);
        } else {
            Logger.warn(this, "IdpConfig with Id: " + idpConfig.getId() + "no longer exists in file.");
        }
    }
}
