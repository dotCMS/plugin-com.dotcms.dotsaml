package com.dotcms.util.config;

import java.io.*;

/**
 * Encapsulates methods to modified the web.xml
 * @author jsanca
 */
public interface WebDescriptor extends Serializable {

    /**
     * Returns true if the element exists, otherwise false.
     * @param elementTag String
     * @param elementName String
     * @return boolean
     */
    public boolean existsElement (String elementTag, String subElementTag, String elementName);

    /**
     * Adds a {@link FilterConfigBean} before the filter named filterName
     * @param filterName {@link String}
     * @param filter {@link FilterConfigBean}
     */
    public void addFilterBefore (String filterName, FilterConfigBean filter);

    /**
     * Remove the filter class
     * @param filter Class
     */
    public void removeFilter (Class filter);

    /**
     * Remove the filter class name
     * @param filterName String
     */
    public void removeFilter (String filterName);

    /**
     * Transform the document to an {@link File} using the file parameter as a system path
     * @param file {@link String}
     */
    public void transform (final String file) throws Exception;

    /**
     * Transform the document to an {@link File}
     * @param file {@link File}
     */
    public void transform (final File file) throws Exception;

    /**
     * Transform the document to an {@link OutputStream}
     * @param outputStream {@link OutputStream}
     */
    public void transform (final OutputStream outputStream) throws Exception;

    /**
     * Transform the document to an {@link Writer}
     * @param writer {@link Writer}
     */
    public void transform (final Writer writer) throws Exception;
} // E:O:F:WebDescriptor.
