package com.dotcms.plugin.saml.v3;

import org.opensaml.saml.saml2.core.Attribute;
import java.io.Serializable;

/**
 * Encapsulates the attributes retrieve from the {@link org.opensaml.saml.saml2.core.Assertion}
 * @author jsanca
 */
public class AttributesBean implements Serializable {

    // user email from opensaml
    private final String    email;

    // user last name from opensaml
    private final String    lastName;

    // user first name from opensaml
    private final String    firstName;

    // true if opensaml returned roles
    private final boolean   addRoles;

    // Saml object with the roles info.
    private final Attribute roles;

    private AttributesBean(final Builder builder) {
        this.email     = builder.email;
        this.lastName  = builder.lastName;
        this.firstName = builder.firstName;
        this.addRoles  = builder.addRoles;
        this.roles     = builder.roles;
    }

    public String getEmail() {
        return email;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean isAddRoles() {
        return addRoles;
    }

    public Attribute getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "AttributesBean{" +
                "email='" + email + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", addRoles=" + addRoles +
                ", roles=" + roles +
                '}';
    }

    public static final class Builder {

        String email     = "";
        String lastName  = "";
        String firstName = "";
        boolean addRoles = false;
        Attribute roles  = null;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder addRoles(boolean addRoles) {
            this.addRoles = addRoles;
            return this;
        }

        public Builder roles(Attribute roles) {
            this.roles = roles;
            return this;
        }

        public AttributesBean build() {
            return new AttributesBean(this);
        }
    }
} // E:O:F:AttributesBean.
