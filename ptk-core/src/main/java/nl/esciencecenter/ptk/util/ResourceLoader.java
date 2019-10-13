/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.util;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.io.ResourceProvider;
import nl.esciencecenter.ptk.net.URIFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Generic ResourceLoader class which supports (relative) URIs and URLs. It is recommended to use
 * relative URLs and URIs to local files and other resources which might be on the classpath and not
 * rely on absolute file locations.<br>
 * This allows for a more flexible run time environment in applet and servlet environments.
 */
@Slf4j
public class ResourceLoader {

    /**
     * Default UTF-8
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * UTF-16 Big Endian or "wide char"
     */
    public static final String CHARSET_UTF16BE = "UTF-16BE";

    /**
     * UTF-16 Little Endian or "wide char"
     */
    public static final String CHARSET_UTF16LE = "UTF-16LE";

    /**
     * 7-bits (US) ASCII
     */
    public static final String CHARSET_US_ASCII = "US-ASCII";

    /**
     * 8-bits US and Euro 'standard' encoding
     */
    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    /**
     * Latin is an alias for ISO-8859-1 (All Roman/Latin languages)
     */
    public static final String CHARSET_LATIN = "ISO-8869-1";

    /**
     * Legacy extended (US) ASCII Code Page 437 for ascii-art.
     */
    public static final String CHARSET_CP437 = "CP437";

    /**
     * Default is UTF-8
     */
    public static final String DEFAULT_CHARSET = CHARSET_UTF8;

    /**
     * Supported character sets.
     */
    public static final String[] charEncodings = {CHARSET_UTF8, CHARSET_UTF16BE, CHARSET_UTF16LE, CHARSET_US_ASCII,
            CHARSET_ISO_8859_1, CHARSET_LATIN, CHARSET_CP437};

    private static ResourceLoader instance;

    // =================================================================
    //
    // =================================================================

    /**
     * Util class for all the URL resolve methods. Relative URLs are different in that they can be
     * resolved against to the Java Classpath.
     */
    public static class URLResolver {

        public URLResolver() {
        }

        /**
         * Resolve relative path and return URL to existing resource.
         */
        public URL resolveUrlPath(String relativePath) {
            return resolveUrlPath(null, relativePath);
        }

        /**
         * Resolve relative path and return URL to existing resource.
         */
        public java.net.URL resolveUrlPath(ClassLoader optClassLoader, String relativeUrl) {
            log.debug("resolveUrlPath():{}", relativeUrl);

            URL resolvedUrl = null;
            // Normalize to URI style path:
            String urlStr = URIFactory.uripath(relativeUrl, false, File.separatorChar);

            if (urlStr == null) {
                throw new NullPointerException("URL String can not be null");
            }

            // (I) First optional Class Loader. This might be a plugin classloader.
            if (optClassLoader != null) {
                resolvedUrl = optClassLoader.getResource(urlStr);

                if (resolvedUrl != null) {
                    log.trace("resolveUrl() I: Resolved URL by using extra class loader:{}", resolvedUrl);
                }
            }

            // (II) Check context classloader for resources which are on the global classpath.
            if (resolvedUrl == null) {
                resolvedUrl = Thread.currentThread().getContextClassLoader().getResource(urlStr);

                if (resolvedUrl != null) {
                    log.trace("resolveURL() III:Resolved URL by using context classloader:{}", resolvedUrl);
                }
            }

            // (III) keep absolute url as is:
            if (resolvedUrl == null) {
                try {
                    URL url2 = new URL(urlStr);
                    resolvedUrl = url2;
                } catch (MalformedURLException e) {
                    log.warn("resolveURL() IV: Not an absolute url:{}", urlStr);
                }
            }

            log.debug("resolveURL(): '{}' -> '{}'", urlStr, resolvedUrl);
            return resolvedUrl;
        }

    }

    // =================================================================
    // Static methods
    // =================================================================

    public static String[] getDefaultCharEncodings() {
        return charEncodings;
    }

    public static ResourceLoader getDefault() {
        if (instance == null)
            instance = new ResourceLoader(null);
        return instance;
    }

    // =================================================================
    // Instance
    // =================================================================

    protected URLResolver urlResolver = null;

    protected ResourceProvider resourceProvider;

    /**
     * Initialize ResourceLoader with extra URL search path. When resolving relative URLs these path
     * URLs will be searched as well similar as using a PATH environment variable.
     *
     * @param resourceProvider custom resource utility.
     */
    public ResourceLoader(ResourceProvider resourceProvider) {
        init(resourceProvider);
    }

    protected void init(ResourceProvider resourceProvider) {
        urlResolver = new URLResolver();

        this.resourceProvider = resourceProvider;

        if (this.resourceProvider == null) {
            this.resourceProvider = FSUtil.fsutil();
        }
    }

    // =================================================================
    // URI/URL resolving
    // =================================================================

    /**
     * Resolve URL string to absolute URL using the URLResolver.
     *
     * @see ResourceLoader#resolveUrl(ClassLoader, String)
     */
    public URL resolveUrl(String urlString) {
        return resolveUrl(null, urlString);
    }

    /**
     * Resolve relative resource String and return absolute URL. The URL String can be matched
     * against the optional ClassLoader in the case the URL points to a resource loaded by a custom
     * ClassLoader that is not accessible by the classloader which loaded this (ResourceLoader)
     * class.
     * <p>
     *
     * @param optClassLoader - Optional ClassLoader from plugin class Loader
     * @param url            - relative URL String, might be absolute but then there is nothing to 'resolve'.
     * @return resolved Absolute URL
     */
    public URL resolveUrl(ClassLoader optClassLoader, String url) {
        // delegate:
        return urlResolver.resolveUrlPath(optClassLoader, url);
    }

    // =================================================================
    // Input- and OutputStreams
    // =================================================================

    /**
     * Creates a new InputStream from the specified URL.
     */
    public InputStream createInputStream(URL url) throws IOException {
        if (url == null) {
            throw new NullPointerException("createInputStream():URL is NULL!");
        }
        try {
            return resourceProvider.createInputStream(new URIFactory(url).toURI());
        } catch (URISyntaxException | IOException e) {
            // wrap:
            throw new IOException("Failed to get inputstream from" + url + ":" + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Creates a new InputStream from the specified URL.
     */
    public OutputStream createOutputStream(URL url) throws IOException {
        if (url == null) {
            throw new NullPointerException("createInputStream():URL is NULL!");
        }
        try {
            return resourceProvider.createOutputStream(new URIFactory(url).toURI());
        } catch (URISyntaxException e) {
            // wrap:
            throw new IOException("Invalid URL: Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Creates a new InputStream from the specified URI.
     */
    public InputStream createInputStream(java.net.URI uri) throws IOException {
        if (uri == null) {
            throw new NullPointerException("createInputStream():URL is NULL!");
        }
        try {
            return resourceProvider.createInputStream(uri);
        } catch (Exception e) {
            throw new IOException("Cannot get inputstream from" + uri + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Creates a new InputStream from the specified URI.
     */
    public OutputStream createOutputStream(java.net.URI uri) throws IOException {
        if (uri == null) {
            throw new NullPointerException("createInputStream():URL is NULL!");
        }
        try {
            return resourceProvider.createOutputStream(uri);
        } catch (Exception e) {
            throw new IOException("Cannot get inputstream from" + uri + "\n" + e.getMessage(), e);
        }
    }

    // =================================================================
    // Properties
    // =================================================================

    /**
     * Load properties file from specified location.<br>
     */
    public Properties loadProperties(URL url) throws IOException {
        return new ContentReader(this.createInputStream(url), true).loadProperties();
    }

    /**
     * Save properties file to specified location.
     */
    public void saveProperties(URL loc, Properties props, String comments) throws IOException {
        try (OutputStream outps = createOutputStream(loc)) {
            props.store(outps, comments);
            outps.flush();
        }
    }

}
