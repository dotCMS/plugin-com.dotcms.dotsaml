package com.dotcms.plugin.saml.v3.hooks;

import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Permission;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

import java.util.List;

/**
 * This Hook is called when the Host is checking and it is only doing a configuration fields validation, not affecting the saml configuration.
 * Created by nollymar on 3/17/17.
 */
public class SamlHostPostHook extends ContentletAPIPostHookAbstractImp {

    private final SiteConfigurationParser siteConfigurationParser = new SiteConfigurationParser();

    public SamlHostPostHook() {
        super();
    }

    @Override
    public void checkin(final Contentlet currentContentlet,
                        final ContentletRelationships relationshipsData,
                        final List<Category> cats,
                        final List<Permission> selectedPermissions,
                        final User user,
                        final boolean respectFrontendRoles,
                        final Contentlet returnValue) {

        final Host host = new Host(currentContentlet);

        try {

            Logger.info(this, "Validating Saml settings for the host with inode: " + currentContentlet.getInode());

            //Validate the configuration.
            this.siteConfigurationParser.validateConfigurationByHost(host);
        } catch (DotDataException | DotSecurityException e) {
            Logger.error(this, "Error updating Saml configuration", e);
        }
    } // checkin.

} // E:O:F:SamlHostPostHook.
