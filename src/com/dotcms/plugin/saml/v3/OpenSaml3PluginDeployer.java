package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.filter.SamlAccessFilter;
import com.dotcms.util.config.FilterConfigBean;
import com.dotcms.util.config.WebDescriptor;
import com.dotcms.util.config.WebDescriptorFactory;
import com.dotmarketing.plugin.PluginDeployer;

import java.io.File;

/**
 * This deployer will modified the web.xml in order to add the {@link com.dotcms.plugin.saml.v3.filter.SamlAccessFilter} to the xml before the
 * AutoLoginFilter.
 * @author jsanca
 */
public class OpenSaml3PluginDeployer implements PluginDeployer {


    @Override
    public boolean deploy() {

        String webpath = System.getenv("web.path");
        if (null == webpath) {

            webpath = System.getProperty("web.path",
                    "./dotserver/tomcat-8.0.18/webapps/ROOT/WEB-INF/web.xml");
        }

        final File file = new File(webpath);

        if (file.exists()) {

            try {
                final WebDescriptor webDescriptor =
                        WebDescriptorFactory.createWebDescriptor(file);

                webDescriptor.addFilterBefore("AutoLoginFilter",
                        new FilterConfigBean(SamlAccessFilter.class, "/*"));
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }

        }

        return true;
    }

    @Override
    public boolean redeploy(String version) {

        // todo: if applies the version?

        String webpath = System.getenv("web.path");
        if (null == webpath) {

            webpath = System.getProperty("web.path",
                    "./dotserver/tomcat-8.0.18/webapps/ROOT/WEB-INF/web.xml");
        }

        final File file = new File(webpath);

        if (file.exists()) {

            try {
                final WebDescriptor webDescriptor =
                        WebDescriptorFactory.createWebDescriptor(file);

                if (!webDescriptor.existsElement("filter", "filter-name", SamlAccessFilter.class.getSimpleName())) {
                    webDescriptor.addFilterBefore("AutoLoginFilter",
                            new FilterConfigBean(SamlAccessFilter.class, "/*"));
                }
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }

        }

        return true;
    }
} // E:O:F:OpenSaml3PluginDeployer.
