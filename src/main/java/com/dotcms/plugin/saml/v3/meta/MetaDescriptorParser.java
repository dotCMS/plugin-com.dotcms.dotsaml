package com.dotcms.plugin.saml.v3.meta;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Encapsulates the Idp Meta Data xml parsing.
 *
 * @author jsanca
 */
public interface MetaDescriptorParser extends Serializable {

    /**
     * Parse the xml encapsulate on the inputStream
     * @param inputStream {@link InputStream}
     * @return MetadataBean
     * @throws Exception
     */
    MetadataBean parse(InputStream inputStream) throws Exception // parse.
    ;
} // E:O:F:MetaDescriptorParser.
