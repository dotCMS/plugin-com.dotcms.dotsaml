package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.liferay.util.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class IdpConfigHelper implements Serializable {

    private final String assetsPath;
    private final String idpfilePath;
    private final String certsParentPath;
    private final String metadataParentPath;

    public IdpConfigHelper() {
        this.assetsPath = Config.getStringProperty("ASSET_REAL_PATH",
            FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", "/assets")));
        this.idpfilePath = assetsPath + File.separator + "saml" + File.separator + "config.json";
        this.certsParentPath = assetsPath + File.separator + "certs" + File.separator;
        this.metadataParentPath = assetsPath + File.separator + "metadata" + File.separator;
    }

    private static class SingletonHolder {
        private static final IdpConfigHelper INSTANCE = new IdpConfigHelper();
    }

    public static IdpConfigHelper getInstance() {
        return IdpConfigHelper.SingletonHolder.INSTANCE;
    }

    public List<IdpConfig> getIdpConfigs() throws IOException, JSONException {
        final List<IdpConfig> idpConfigs = IdpConfigWriterReader.read(new File(idpfilePath));
        Collections.sort(idpConfigs, new IdpConfigComparator());
        return idpConfigs;
    }

    public IdpConfig findIdpConfig(String id) throws IOException, JSONException, DotDataException{
        List<IdpConfig> idpConfigList = getIdpConfigs();
        if (UtilMethods.isSet(id)){
            IdpConfig idpConfig = new IdpConfig();
            idpConfig.setId(id);
            return idpConfigList.get(idpConfigList.indexOf(idpConfig));
        } else {
            throw new DotDataException("Idp with id:" + id + " not found in file.");
        }
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
            //Delete from list.
            idpConfig = idpConfigList.get(idpConfigList.indexOf(idpConfig));
            idpConfigList.remove(idpConfig);
            IdpConfigWriterReader.write(idpConfigList, idpfilePath);
            //Delete files from FS.
            deleteFile(idpConfig.getPrivateKey());
            deleteFile(idpConfig.getPublicCert());
            deleteFile(idpConfig.getIdPMetadataFile());

        } else {
            Logger.warn(this, "IdpConfig with Id: " + idpConfig.getId() + "no longer exists in file.");
        }
    }

    public File writeCertFile(InputStream stream, String fileName) throws IOException{
        return this.writeFile(stream, this.certsParentPath, fileName);
    }

    public File writeMetadataFile(InputStream stream, String fileName) throws IOException{
        return this.writeFile(stream, this.metadataParentPath, fileName);
    }

    private File writeFile(InputStream stream, String parentPath, String fileName) throws IOException{
        File file = new File(parentPath + fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileUtils.copyInputStreamToFile(stream, file);

        return file;
    }

    private void deleteFile(File fileToDelete){
        if (fileToDelete != null){
            if (fileToDelete.exists()){
                fileToDelete.delete();
            } else {
                Logger.warn(this, "File doesn't exist: " + fileToDelete.getName());
            }
        }
    }
}
