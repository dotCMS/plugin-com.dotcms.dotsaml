package com.dotcms.plugin.saml.v3.rest.api.v1;

import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONException;
import com.liferay.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class IdpConfigHelper implements Serializable {

    private final String assetsPath;
    private final String idpfilePath;
    private final String certsParentPath;
    private final String metadataParentPath;

    final static String SAML = "saml";

    public IdpConfigHelper() {
        this.assetsPath = Config.getStringProperty("ASSET_REAL_PATH",
            FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", "/assets")));

        this.idpfilePath = assetsPath + File.separator + SAML + File.separator + "config.json";
        this.certsParentPath = assetsPath + File.separator + SAML + File.separator + "certs" + File.separator;
        this.metadataParentPath = assetsPath + File.separator + SAML + File.separator + "metadata" + File.separator;
    } // IdpConfigHelper.

    private static class SingletonHolder {
        private static final IdpConfigHelper INSTANCE = new IdpConfigHelper();
    } // SingletonHolder

    public static IdpConfigHelper getInstance() {
        return IdpConfigHelper.SingletonHolder.INSTANCE;
    } // getInstance.

    public List<IdpConfig> getIdpConfigs() throws IOException, JSONException {
        final List<IdpConfig> idpConfigs = IdpConfigWriterReader.readIdpConfigs(new File(idpfilePath));
        return idpConfigs;
    } // getIdpConfigs.

    public IdpConfig findIdpConfig(String id) throws IOException, JSONException, DotDataException {
        if (UtilMethods.isSet(id)){
            List<IdpConfig> idpConfigList = getIdpConfigs();
            IdpConfig idpConfig = new IdpConfig.Builder().id(id).build();
            return idpConfigList.get(idpConfigList.indexOf(idpConfig));
        } else {
            throw new DotDataException("Idp with id:" + id + " not found in file.");
        }
    } // findIdpConfig.

    public IdpConfig saveIdpConfig(IdpConfig idpConfig) throws IOException, JSONException {
        List<IdpConfig> idpConfigList = getIdpConfigs();

        if (UtilMethods.isSet(idpConfig.getId())){
            //Update.

            //Renaming files
            idpConfig = renameIdpConfigFiles(idpConfig);

            idpConfigList.remove(idpConfig);
            idpConfigList.add(idpConfig);
            IdpConfigWriterReader.writeIdpConfigs(idpConfigList, idpfilePath);
        } else {
            //Create.
            IdpConfig.Builder builder = IdpConfig.convertIdpConfigToBuilder(idpConfig);
            //Creating new UUID.
            builder.id(UUIDGenerator.generateUuid());
            idpConfig = builder.build();
            //Renaming files
            idpConfig = renameIdpConfigFiles(idpConfig);

            idpConfigList.add(idpConfig);
            IdpConfigWriterReader.writeIdpConfigs(idpConfigList, idpfilePath);
        }

        return idpConfig;
    } // saveIdpConfig.

    public void deleteIdpConfig(IdpConfig idpConfig) throws IOException, JSONException {
        List<IdpConfig> idpConfigList = getIdpConfigs();
        String defaultIdpConfigId = IdpConfigWriterReader.readDefaultIdpConfigId(new File(idpfilePath));

        //We need to clean the defaultIdpConfigId if we are deleting the same IDP.
        if (idpConfig.getId().equals(defaultIdpConfigId)){
            defaultIdpConfigId = "";
        }

        if (idpConfigList.contains(idpConfig)){
            //Delete from list.
            idpConfig = idpConfigList.get(idpConfigList.indexOf(idpConfig));
            idpConfigList.remove(idpConfig);
            IdpConfigWriterReader.writeDefaultIdpConfigId(idpConfigList, defaultIdpConfigId, idpfilePath);
            //Delete files from FS.
            deleteFile(idpConfig.getPrivateKey());
            deleteFile(idpConfig.getPublicCert());
            deleteFile(idpConfig.getIdPMetadataFile());

        } else {
            Logger.warn(this, "IdpConfig with Id: " + idpConfig.getId() + "no longer exists in file.");
        }
    } // deleteIdpConfig.

    public void setDefaultIdpConfig(String idpConfigId) throws IOException, JSONException, DotDataException {
        List<IdpConfig> idpConfigList = getIdpConfigs();

        final IdpConfig idpConfig = new IdpConfig.Builder().id(idpConfigId).build();

        if (idpConfigList.contains(idpConfig)){
            IdpConfigWriterReader.writeDefaultIdpConfigId(idpConfigId, idpfilePath);
        }
        else {
            Logger.error(this, "IdpConfig with Id: " + idpConfig.getId() + "no longer exists in file.");
            throw new DotDataException("IdpConfig with Id: " + idpConfig.getId() + "no longer exists in file.");
        }
    } // setDefaultIdpConfig.

    public String getDefaultIdpConfigId() throws IOException, JSONException {
        return IdpConfigWriterReader.readDefaultIdpConfigId(new File(idpfilePath));
    } // setDefaultIdpConfig.

    public void saveDisabledSiteIds(Map<String, String> disablebSitesMap) throws IOException, JSONException {
        IdpConfigWriterReader.writeDisabledSIteIds(disablebSitesMap, idpfilePath);
    } // saveDisabledSiteIds.

    public Map<String, String> getDisabledSiteIds() throws IOException, JSONException {
        return IdpConfigWriterReader.readDisabledSiteIds(new File(idpfilePath));
    } // getDisabledSiteIds.

    private IdpConfig renameIdpConfigFiles(IdpConfig idpConfig) throws IOException {
        IdpConfig.Builder builder = IdpConfig.convertIdpConfigToBuilder(idpConfig);

        if (UtilMethods.isSet(idpConfig.getPrivateKey())){
            builder.privateKey(writeCertFile(idpConfig.getPrivateKey(), idpConfig.getId() + ".key"));
        }
        if (UtilMethods.isSet(idpConfig.getPublicCert())){
            builder.publicCert(writeCertFile(idpConfig.getPublicCert(), idpConfig.getId() + ".crt"));
        }
        if (UtilMethods.isSet(idpConfig.getIdPMetadataFile())){
            builder.idPMetadataFile(writeMetadataFile(idpConfig.getIdPMetadataFile(), idpConfig.getId() + ".xml"));
        }

        return builder.build();
    } // renameIdpConfigFiles.

    private File writeCertFile(File sourceFile, String fileName) throws IOException{
        return this.writeFile(sourceFile, this.certsParentPath, fileName);
    } // writeCertFile.

    private File writeMetadataFile(File sourceFile, String fileName) throws IOException{
        return this.writeFile(sourceFile, this.metadataParentPath, fileName);
    } // writeMetadataFile.

    private File writeFile(File sourceFile, String parentPath, String fileName) throws IOException{
        File targetFile = new File(parentPath + fileName);

        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();
        }

        final Path movedPath = Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return movedPath.toFile();
    } // writeFile.

    private void deleteFile(File fileToDelete){
        if (fileToDelete != null){
            if (fileToDelete.exists() && fileToDelete.canWrite()){
                fileToDelete.delete();
            } else {
                Logger.warn(this, "File doesn't exist or can't write: " + fileToDelete.getName());
            }
        }
    } // deleteFile.
}
