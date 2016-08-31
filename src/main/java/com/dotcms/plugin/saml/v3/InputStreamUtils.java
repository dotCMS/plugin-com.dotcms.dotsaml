package com.dotcms.plugin.saml.v3;

import com.dotcms.repackage.org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple util that determine if the stream path is a file or classpath.
 * Depending on it will return the right InputStream
 *
 * If the resource name parameter starts with file:// will return a FileInputStream, otherwise will try to get from the classpath.
 */
public class InputStreamUtils {

    private final static String PREFIX_FILE = "file://"; // file://

    public static InputStream getInputStream (final String resourceName) throws IOException {

        InputStream inputStream = null;

        if (StringUtils.startsWith(resourceName, PREFIX_FILE)) {

            final String normalizedName = resourceName.
                    replaceFirst(PREFIX_FILE, StringUtils.EMPTY);
            inputStream = new FileInputStream(normalizedName);
        } else {

            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            inputStream = classLoader.getResourceAsStream(resourceName);
        }

        return inputStream;
    } // getInputStream.

    public static void main(String [] args) throws IOException {
        InputStream inputStream =
                getInputStream("file:///Users/jsanca/gitsources/3.6/plugin-dotcms-openSAML3/src/test/resources/idp-metadata.xml");

        System.out.print(inputStream);
    }

} // E:O:F:InputStreamUtils.
