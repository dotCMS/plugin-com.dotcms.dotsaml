package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;
import com.dotcms.plugin.saml.v3.exception.AttributesNotFoundException;
import com.dotcms.plugin.saml.v3.exception.DotSamlException;
import com.dotcms.plugin.saml.v3.strategy.EmptyOrNullFieldStrategy;
import com.dotcms.plugin.saml.v3.strategy.FieldStrategy;
import com.dotmarketing.business.DuplicateUserException;
import com.dotmarketing.cms.factories.PublicEncryptionFactory;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;
import com.liferay.portal.model.User;
import com.liferay.util.StringPool;
import org.opensaml.saml.saml2.core.Assertion;

import java.util.Date;

/**
 * This is basically the same implementation of the {@link OpenSamlAuthenticationServiceImpl} except for the {@link OpenSamlAuthenticationServiceImpl#resolveUser(Assertion, Configuration)}
 * The resolveUser method only requires the user id (nameId) it is the reason it is tolerance since all values should be required,
 * the rest of the properties could be optional. For each of attribute will be a strategy when it is empty or null in order to create a generated value.
 * @author jsanca
 */
public class ToleranceOpenSamlAuthenticationServiceImpl extends OpenSamlAuthenticationServiceImpl {

    private static final String DEFAULT_EMAIL_POSTFIX = "@noemail.com";
    private final FieldStrategy emailFieldStrategy;
    private final FieldStrategy nameFieldStrategy;
    private final FieldStrategy lastNameFieldStrategy;

    public ToleranceOpenSamlAuthenticationServiceImpl() {

        final String emailPreFix     = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_EMAIL_PREFIX,      StringPool.BLANK);
        final String namePreFix      = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_NAME_PREFIX,       StringPool.BLANK);
        final String lastnamePreFix  = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_LASTNAME_PREFIX,   StringPool.BLANK);

        final String emailPostFix     = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_EMAIL_POSTFIX,    DEFAULT_EMAIL_POSTFIX);
        final String namePostFix      = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_NAME_POSTFIX,     StringPool.BLANK);
        final String lastnamePostFix  = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_LASTNAME_POSTFIX, StringPool.BLANK);

        final String emailDefault     = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_EMAIL_DEFAULT,    null);
        final String nameDefault      = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_NAME_DEFAULT,     null);
        final String lastnameDefault  = Config.getStringProperty(DotSamlConstants.DOTCMS_SAML_LASTNAME_DEFAULT, null);

        this.emailFieldStrategy       = new EmptyOrNullFieldStrategy(emailDefault, emailPreFix, emailPostFix);
        this.nameFieldStrategy        = new EmptyOrNullFieldStrategy(nameDefault, namePreFix, namePostFix);
        this.lastNameFieldStrategy    = new EmptyOrNullFieldStrategy(lastnameDefault, lastnamePreFix, lastnamePostFix);
    }

    protected FieldStrategy getEmailFieldStrategy() {
        return emailFieldStrategy;
    }

    protected FieldStrategy getNameFieldStrategy() {
        return nameFieldStrategy;
    }

    protected FieldStrategy getLastNameFieldStrategy() {
        return lastNameFieldStrategy;
    }

    @Override
    protected User createNewUser(final User systemUser,
                                 final AttributesBean attributesBean) {

        User user = null;

        try {

            final AttributesBean newAttributesBean = this.applyStrategies(attributesBean);

            try {

                user = this.userAPI.createUser(newAttributesBean.getNameID().getValue(),
                        newAttributesBean.getEmail());
            } catch (DuplicateUserException e) {

                Logger.debug(this, "The NameId " + newAttributesBean.getNameID().getValue()
                        + " or The email: " + newAttributesBean.getEmail() +
                        ", are duplicated, could not create the user, trying a new email strategy");

                final String newEmail = (String)this.getEmailFieldStrategy().apply(attributesBean);
                user = this.userAPI.createUser(newAttributesBean.getNameID().getValue(), newEmail);

                Logger.debug(this, "The user was successfully created with the email: " + newEmail);
            }

            user.setFirstName(newAttributesBean.getFirstName());
            user.setLastName (newAttributesBean.getLastName());
            user.setActive(true);

            user.setCreateDate(new Date());
            user.setPassword(PublicEncryptionFactory.digestString
                    (UUIDGenerator.generateUuid() + "/" + UUIDGenerator.generateUuid()));
            user.setPasswordEncrypted(true);

            this.userAPI.save(user, systemUser, false);
            Logger.debug(this, "new user created. email: " + newAttributesBean.getEmail());
        } catch (Exception e) {

            Logger.error(this, "Error creating user:" + e.getMessage(), e);
            throw new DotSamlException(e.getMessage());
        }

        return user;
    } // createNewUser.


    protected AttributesBean applyStrategies (final AttributesBean attributesBean) {

        final AttributesBean.Builder builder = new AttributesBean.Builder();

        builder.nameID(attributesBean.getNameID());
        builder.addRoles(attributesBean.isAddRoles());
        builder.roles(attributesBean.getRoles());

        if (this.getEmailFieldStrategy().canApply(attributesBean.getEmail())) {

            builder.email((String)this.getEmailFieldStrategy().apply(attributesBean));
        }

        if (this.getNameFieldStrategy().canApply(attributesBean.getFirstName())) {

            builder.firstName((String)this.getNameFieldStrategy().apply(attributesBean));
        }

        if (this.getLastNameFieldStrategy().canApply(attributesBean.getLastName())) {

            builder.lastName((String)this.getLastNameFieldStrategy().apply(attributesBean));
        }

        return builder.build();
    } // applyStrategies.

    @Override
    protected void validateAttributes(Assertion assertion) throws AttributesNotFoundException {

        // only the subject is a most
        if (assertion == null
                || assertion.getSubject() == null
                || assertion.getSubject().getNameID() == null
                || assertion.getSubject().getNameID().getValue().isEmpty()) {

            throw new AttributesNotFoundException("No attributes found");
        }
    }

} // E:O:F:ToleranceOpenSamlAuthenticationServiceImpl.
