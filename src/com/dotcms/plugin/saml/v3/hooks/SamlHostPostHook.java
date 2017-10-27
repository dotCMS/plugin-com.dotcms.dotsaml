package com.dotcms.plugin.saml.v3.hooks;

import com.dotcms.plugin.saml.v3.config.SiteConfigurationParser;
import com.dotcms.plugin.saml.v3.content.SamlContentTypeUtil;
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

            Logger.info(this, "Validating Saml settings for the site: " + host.getHostname());

            //Validate the configuration.
            final String hostSAMLAuthentication  = (String)host.getMap()
                    .get(SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME);
            final boolean isDisabled =
                    SamlContentTypeUtil.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED
                            .equalsIgnoreCase(hostSAMLAuthentication);

            if (isDisabled) {

                this.siteConfigurationParser.validateConfigurationByDisableHost(host, hostSAMLAuthentication);
            } else {
                this.siteConfigurationParser.validateConfigurationByHost(host);
            }

            Logger.info(this, "DONE Validating Saml settings for the site: " + host.getHostname());
        } catch (DotDataException | DotSecurityException e) {
            Logger.error(this, "Error Validating Saml configuration", e);
        }
    } // checkin.

} // E:O:F:SamlHostPostHook.
