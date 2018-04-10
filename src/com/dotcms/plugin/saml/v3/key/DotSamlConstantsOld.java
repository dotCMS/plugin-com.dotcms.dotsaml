package com.dotcms.plugin.saml.v3.key;

/**
 * Encapsulates constants for the dot SAML SP
 *
 * @author jsanca
 */
@Deprecated
public final class DotSamlConstantsOld
{
	public static final String DEFAULT_SAML_CONFIG_FILE_NAME = "dotcms-saml-default.properties";

	/**
	 * By default dotcms use:
	 * {@link com.dotcms.plugin.saml.v3.config.DefaultDotCMSConfiguration} but
	 * you can override by addding a full class name to this property.
	 */
	public static final String DOT_SAML_CONFIGURATION_CLASS_NAME = "configuration.classname";

	/**
	 * Key for dotmarketing-config.properties By default dotcms use:
	 * {@link DefaultInitializer} but you can override by adding a full class
	 * name to this property.
	 */
	public static final String DOT_SAML_INITIALIZER_CLASS_NAME = "initializer.classname";

	/**
	 * By default the system will do the verification of the assertion
	 * signature, if for some reason you want to avoid it feel free to set it to
	 * "false".
	 */
	public static final String DOT_SAML_VERIFY_ASSERTION_SIGNATURE = "verify.assertion.signature";

	/**
	 * Host field that enables or disables the current saml configuration for
	 * the host or use the default configuration instead
	 */
	public static final String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME = "SAML Authentication";

	/**
	 * Host field that contains the SAML configuration
	 */
	public static final String DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME = "SAML Configuration";

	/**
	 * Default value for text_area field
	 */
	public static final String DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT = "idp.metadata.path=\n" + "keystore.path=\n" + "keystore.password=\n";

	/**
	 * Key for dotmarketing-config.properties This optional property defines the
	 * default host used as a fallback to get its SAML configuration, in case a
	 * host does not have any
	 */
	public static final String DOTCMS_SAML_FALLBACK_SITE = "saml.fallback.site";

	/**
	 * In case you have a idp-metadata.xml you can get it from the classpath or
	 * file system. For the classpath you overrides the property with the right
	 * path in your class path. If you want to get the XML from the file system
	 * use the prefix; file://
	 */
	public static final String DOTCMS_SAML_IDP_METADATA_PATH = "idp.metadata.path";

	/**
	 * Delay between calls to rerun the updater task, by default 10 seconds:
	 * time is in seconds
	 */
	public static final String SCHEDULE_UPDATER_TASK_DELAY_SECONDS = "schedule.updater.task.delay";

	/**
	 * Initial delay (10 seconds per default) to start the updater task: time is
	 * in seconds
	 */
	public static final String SCHEDULE_UPDATER_TASK_INITIAL_DELAY = "schedule.updater.task.initial.delay";
}
