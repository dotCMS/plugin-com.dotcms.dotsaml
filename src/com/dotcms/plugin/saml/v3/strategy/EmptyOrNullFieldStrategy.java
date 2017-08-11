package com.dotcms.plugin.saml.v3.strategy;

import com.dotcms.plugin.saml.v3.AttributesBean;
import com.liferay.util.StringPool;

/**
 * Field Strategy when the field is empty or null.
 * @author jsanca
 */
public class EmptyOrNullFieldStrategy implements FieldStrategy {

    private final String defaultValue;
    private final String preFix;
    private final String postFix;

    public EmptyOrNullFieldStrategy(final String preFix, final String postFix) {
        this(null, preFix, postFix);
    }
    public EmptyOrNullFieldStrategy(final String defaultValue, final String preFix, final String postFix) {
        this.defaultValue = defaultValue;
        this.preFix  = preFix;
        this.postFix = postFix;
    }

    @Override
    public boolean canApply(Object fieldValue) {
        return null == fieldValue || (fieldValue.toString().trim().length() == 0);
    }

    @Override
    public Object apply(final AttributesBean attributesBean) {

        return  (null != this.defaultValue)?
                this.defaultValue:
                this.preFix + attributesBean.getNameID().getValue() + this.postFix;
    }
} // E:O:F:EmptyOrNullFieldStrategy.
