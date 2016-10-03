package com.dotcms.plugin.saml.v3;

import com.dotcms.plugin.saml.v3.config.Configuration;

import java.io.Serializable;

/**
 * Encapsulates the current Configuration for the current site request.
 * @author jsanca
 */
public class ThreadLocalConfiguration implements Serializable {

    private static ThreadLocal<Configuration> threadLocal = new ThreadLocal<>();

    public static Configuration getCurrentSiteConfiguration () {

        return threadLocal.get();
    }

    public static void setCurrentSiteConfiguration(final Configuration configuration) {

        threadLocal.set(configuration);
    }

} // E:O:F:ThreadLocalConfiguration.
