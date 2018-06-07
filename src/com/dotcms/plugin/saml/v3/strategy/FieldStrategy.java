package com.dotcms.plugin.saml.v3.strategy;

import com.dotcms.plugin.saml.v3.beans.AttributesBean;

import java.io.Serializable;

/**
 * A field strategy helps about what to do
 * 
 * @author jsanca
 */
public interface FieldStrategy extends Serializable
{
	/**
	 * Determine if the fieldValue can be applied by the strategy
	 * 
	 * @param fieldValue
	 *            Object
	 * @return boolean
	 */
	boolean canApply( Object fieldValue );

	/**
	 * Apply the strategy logic
	 * 
	 * @param attributesBean
	 *            {@link AttributesBean}
	 * @return Object
	 */
	Object apply( final AttributesBean attributesBean );

}
