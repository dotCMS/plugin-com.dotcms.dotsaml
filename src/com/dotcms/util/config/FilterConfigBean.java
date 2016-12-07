package com.dotcms.util.config;

import com.dotcms.repackage.org.apache.commons.compress.archivers.dump.DumpArchiveEntry;

import java.io.Serializable;

/**
 * Encapsulate a Filter configuration.
 * @author jsanca
 */
public class FilterConfigBean implements Serializable {

    private final Class filterClass;
    private final String urlPattern;

    public FilterConfigBean(final Class filterClass,
                            final String urlPattern) {

        this.filterClass = filterClass;
        this.urlPattern = urlPattern;
    }

    public Class getFilterClass() {
        return filterClass;
    }

    public String getUrlPattern() {
        return urlPattern;
    }
} // FilterConfigBean.
