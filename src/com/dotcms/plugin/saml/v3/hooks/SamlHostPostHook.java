package com.dotcms.plugin.saml.v3.hooks;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.config.SiteConfigurationService;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.business.HostAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;
import com.liferay.util.InstancePool;

import java.io.IOException;
import java.util.List;

/**
 * Contains the logic that updates the saml configuration for a host after editing it
 * Created by nollymar on 3/17/17.
 */
public class SamlHostPostHook extends ContentletAPIPostHookAbstractImp {

    private final HostAPI hostAPI = APILocator.getHostAPI();

    public SamlHostPostHook() {
        super();
    }

    @Override
    public void checkin(Contentlet currentContentlet, ContentletRelationships relationshipsData, List<Category> cats,
                        List<Permission> selectedPermissions, User user, boolean respectFrontendRoles,
                        Contentlet returnValue) {

        Host host = new Host(currentContentlet);
        Configuration siteConfiguration;
        SiteConfigurationService siteConfigurationService;
        SiteConfigurationParser siteConfigurationParser = new SiteConfigurationParser();
        try {

            Logger.info(this, "Reconfiguring Saml settings for the host with inode: " + currentContentlet.getInode());

            //Validate the configuration.
            siteConfigurationParser.validateConfigurationByHost(host);
            //Loading current configuration
            siteConfiguration = siteConfigurationParser.getConfigurationByHost(host);

            if (siteConfiguration != null) {
                siteConfigurationService =
                    (SiteConfigurationService) InstancePool.get(SiteConfigurationService.class.getName());

                //Updating configuration
                siteConfigurationService.setConfigurationBySite(host.getHostname(), siteConfiguration);

                //save the same map for each host alias
                hostAPI.parseHostAliases(host).forEach(alias -> siteConfigurationService.setConfigurationBySite(alias, siteConfiguration));
            }
        } catch (IOException | DotDataException | DotSecurityException e) {
            Logger.error(this, "Error updating Saml configuration", e);
        }
    }

}
