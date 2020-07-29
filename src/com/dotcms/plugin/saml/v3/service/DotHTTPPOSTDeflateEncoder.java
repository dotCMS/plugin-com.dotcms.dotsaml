package com.dotcms.plugin.saml.v3.service;

import com.dotmarketing.util.VelocityUtil;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;

// migrated
public class DotHTTPPOSTDeflateEncoder extends HTTPPostEncoder {


    public DotHTTPPOSTDeflateEncoder() {
        this.setVelocityTemplateId("/templates/saml2-post-binding.vm");
        this.setVelocityEngine(VelocityUtil.getEngine());
    }
}
