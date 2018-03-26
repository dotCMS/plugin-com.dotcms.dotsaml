package com.dotcms.plugin.saml.v3.content;

import com.dotcms.plugin.saml.v3.DotSamlConstants;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.repackage.com.google.common.annotations.VisibleForTesting;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.cache.FieldsCache;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.structure.business.StructureAPI;
import com.dotmarketing.portlets.structure.factories.FieldFactory;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.services.StructureServices;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

import java.util.List;

import static com.dotcms.plugin.saml.v3.DotSamlConstants.DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT;

/**
 * Util for the interaction with the DotCMS content types.
 * 
 * @author jsanca
 */
public class SamlContentTypeUtil
{
	private final static String HOST_VARIABLE_NAME = "Host";

	private final StructureAPI structureAPI;
	private final UserAPI userAPI;

	public final static String DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME = UtilMethods.toCamelCase( DotSamlConstants.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME );

	public final static String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME = UtilMethods.toCamelCase( DotSamlConstants.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME );

	public final static String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_ENABLED = "Enabled";
	public final static String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED = "Disabled";
	public final static String DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DEFAULT = "Default";
	private final static String DEFAULT_VALUES = "Enabled|" + DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_ENABLED + "\r\n" + "Disabled|" + DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED + "\r\n" + "Use Default|" + DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DEFAULT;

	public SamlContentTypeUtil()
	{
		this( APILocator.getStructureAPI(), APILocator.getUserAPI() );
	}

	@VisibleForTesting
	public SamlContentTypeUtil( final StructureAPI structureAPI, final UserAPI userAPI )
	{
		this.structureAPI = structureAPI;
		this.userAPI = userAPI;
	}

	/**
	 * Checks if the
	 * {@link DotSamlConstants}DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME and
	 * {@link DotSamlConstants}DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME
	 * fields exists on the Host ContentType. If they do not exists will create
	 * them into the Host.
	 */
	public void checkORCreateSAMLField()
	{
		final Structure hostStructure;

		try
		{
			hostStructure = this.structureAPI.findByVarName( HOST_VARIABLE_NAME, this.userAPI.getSystemUser() );

			if ( !hasSAMLFields( hostStructure ) )
			{
				Logger.info( this, "Creating SAML fields under Host with inode: " + hostStructure.getInode() );

				FieldFactory.saveField( this.createSamlEnabledField( hostStructure ) );
				FieldFactory.saveField( this.createSamlConfigField( hostStructure ) );

				FieldsCache.removeFields( hostStructure );
				CacheLocator.getContentTypeCache().remove( hostStructure );
				StructureServices.removeStructureFile( hostStructure );
				StructureFactory.saveStructure( hostStructure );
				FieldsCache.addFields( hostStructure, hostStructure.getFields() );
			}
			else
			{
				Logger.info( this, "SAML fields already exists under Host with inode: " + hostStructure.getInode() );
			}
		}
		catch ( DotDataException | DotSecurityException e )
		{
			Logger.error( this, e.getMessage(), e );
			throw new DotSamlException( e.getMessage(), e );
		}
	}

	private Field createSamlEnabledField( final Structure hostStructure )
	{
		return new Field( DotSamlConstants.DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME, Field.FieldType.SELECT, Field.DataType.TEXT, hostStructure, false, false, true, 4, DEFAULT_VALUES, DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_DISABLED, "", false, false, true );
	}

	private Field createSamlConfigField( final Structure hostStructure )
	{
		return new Field( DotSamlConstants.DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME, Field.FieldType.TEXT_AREA, Field.DataType.LONG_TEXT, hostStructure, false, false, true, 4, "", DOTCMS_SAML_DEFAULT_CONF_FIELD_CONTENT, "", false, false, true );
	}

	/**
	 * Check is the Structure has a SAML Field.
	 * {@link DotSamlConstants}DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_NAME and
	 * {@link DotSamlConstants}DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_NAME
	 *
	 * @param hostStructure
	 * @return true is the strcture has a SAML fields, false else.
	 */
	private boolean hasSAMLFields( final Structure hostStructure )
	{
		final List<Field> fieldsByHost = FieldsCache.getFieldsByStructureInode( hostStructure.getInode() );

		boolean existsSamlEnabledField = false;
		boolean existsSamlConfigField = false;

		for ( final Field field : fieldsByHost )
		{
			existsSamlConfigField |= ( DOTCMS_SAML_CONTENT_TYPE_FIELD_CONFIG_VELOCITY_VAR_NAME.equals( field.getVelocityVarName() ) );
			existsSamlEnabledField |= ( DOTCMS_SAML_CONTENT_TYPE_FIELD_AUTHENTICATION_VELOCITY_VAR_NAME.equals( field.getVelocityVarName() ) );
		}

		return existsSamlEnabledField && existsSamlConfigField;
	}
}
