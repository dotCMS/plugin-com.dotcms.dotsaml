package com.dotcms.plugin.saml.v3.meta;

import com.dotcms.plugin.saml.v3.BindingType;
import com.dotcms.plugin.saml.v3.InputStreamUtils;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.security.credential.Credential;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link MetaDescriptorParser}
 * @author jsanca
 */
public class MetaDescriptorParserTest {

    @Test
    public void parserTest () throws DotDataException, DotSecurityException, InitializationException {

        InitializationService.initialize();
        final MetaDescriptorParser parser = new DefaultMetaDescriptorParserImpl();

        try (InputStream inputStream = InputStreamUtils.getInputStream("idp-metadata.xml")) {

            final MetadataBean metadataBean =
                    parser.parse(inputStream);

            assertNotNull(metadataBean);
            assertNotNull(metadataBean.getEntityId());
            assertTrue("https://localhost.localdomain/idp/shibboleth".equals(metadataBean.getEntityId()));
            assertNotNull (metadataBean.getCredentialSigningList());
            assertTrue (metadataBean.getCredentialSigningList().size() == 2);
            assertNotNull (metadataBean.getSingleSignOnBindingLocationMap());
            assertNotNull (metadataBean.getSingleSignOnBindingLocationMap().size() == 3);
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.REDIRECT.getBinding()).equals("https://localhost.localdomain/idp/profile/SAML2/Redirect/SSO"));
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.AUTHN_REQUEST.getBinding()).equals("https://localhost.localdomain/idp/profile/Shibboleth/SSO"));
            assertTrue (metadataBean.getSingleSignOnBindingLocationMap().get(BindingType.POST.getBinding()).equals("https://localhost.localdomain/idp/profile/SAML2/POST/SSO"));
        } catch (Exception e) {

            fail ("Error on open the idp-metadata: " + e.getMessage());
        }
    }
}
