package com.dotcms.plugin.saml.v3.beans;

import com.dotmarketing.util.Logger;

import java.io.Serializable;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.NameID;

/**
 * Encapsulates the attributes retrieve from the
 * {@link org.opensaml.saml.saml2.core.Assertion}
 * 
 * @author jsanca
 */
// Migrated
public class AttributesBean implements Serializable
{
	private static final long serialVersionUID = 1836313856887837731L;

	// user email from opensaml
	private final String email;

	// user last name from opensaml
	private final String lastName;

	// user first name from opensaml
	private final String firstName;

	// true if opensaml returned roles
	private final boolean addRoles;

	// Saml object with the roles info.
	private final Attribute roles;

	// Saml object with the NameID.
	private final NameID nameID;

	private AttributesBean( final Builder builder )
	{
		this.email = builder.email;
		this.lastName = builder.lastName;
		this.firstName = builder.firstName;
		this.addRoles = builder.addRoles;
		this.roles = builder.roles;
		this.nameID = builder.nameID;
	}

	public String getEmail()
	{
		return email;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public boolean isAddRoles()
	{
		return addRoles;
	}

	public Attribute getRoles()
	{
		return roles;
	}

	public NameID getNameID()
	{
		return nameID;
	}

	@Override
	public String toString()
	{
		return "AttributesBean{" + "nameID='" + nameID.getValue() + '\'' + ", email='" + email + '\'' + ", lastName='" + lastName + '\'' + ", firstName='" + firstName + '\'' + ", addRoles=" + addRoles + ", roles=" + roles + '}';
	}

	public static final class Builder
	{
		String email = "";
		String lastName = "";
		String firstName = "";
		boolean addRoles = false;
		Attribute roles = null;
		NameID nameID = null;

		public Builder email( String email )
		{
			Logger.debug( this, "Setting email: " + email );
			this.email = email;
			return this;
		}

		public Builder lastName( String lastName )
		{
			Logger.debug( this, "Setting lastName: " + lastName );
			this.lastName = lastName;
			return this;
		}

		public Builder firstName( String firstName )
		{
			Logger.debug( this, "Setting firstName: " + firstName );
			this.firstName = firstName;
			return this;
		}

		public Builder addRoles( boolean addRoles )
		{
			this.addRoles = addRoles;
			return this;
		}

		public Builder roles( Attribute roles )
		{
			this.roles = roles;
			return this;
		}

		public Builder nameID( NameID nameID )
		{
			this.nameID = nameID;
			return this;
		}

		public String getEmail()
		{
			return email;
		}

		public String getLastName()
		{
			return lastName;
		}

		public String getFirstName()
		{
			return firstName;
		}

		public boolean isAddRoles()
		{
			return addRoles;
		}

		public Attribute getRoles()
		{
			return roles;
		}

		public NameID getNameID()
		{
			return nameID;
		}

		public AttributesBean build()
		{
			return new AttributesBean( this );
		}
	}
}
