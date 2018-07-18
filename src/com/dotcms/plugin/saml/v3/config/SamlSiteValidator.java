package com.dotcms.plugin.saml.v3.config;

import static com.dotcms.plugin.saml.v3.key.DotSamlConstants.*;

/**
 * This class is in charge of validating site IdpConfig
 * based on the business rules.
 * 
 * @author jsanca
 */
public class SamlSiteValidator
{
	public static boolean checkBuildRoles( final String buildRolesProperty )
	{
		return DOTCMS_SAML_BUILD_ROLES_ALL_VALUE.equalsIgnoreCase( buildRolesProperty ) || DOTCMS_SAML_BUILD_ROLES_IDP_VALUE.equalsIgnoreCase( buildRolesProperty ) || DOTCMS_SAML_BUILD_ROLES_STATIC_ONLY_VALUE.equalsIgnoreCase( buildRolesProperty ) || DOTCMS_SAML_BUILD_ROLES_STATIC_ADD_VALUE.equalsIgnoreCase( buildRolesProperty ) || DOTCMS_SAML_BUILD_ROLES_NONE_VALUE.equalsIgnoreCase( buildRolesProperty );
	}
}
