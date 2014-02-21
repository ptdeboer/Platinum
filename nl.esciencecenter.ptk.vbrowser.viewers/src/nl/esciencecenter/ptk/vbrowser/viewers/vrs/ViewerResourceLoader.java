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

package nl.esciencecenter.ptk.vbrowser.viewers.vrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReader;
import nl.esciencecenter.ptk.io.RandomWriter;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;

/**
 * Content Factory and Resource Manager for the various embedded Viewers.
 */
public class ViewerResourceLoader
{
    private static ClassLogger logger = ClassLogger.getLogger(ViewerResourceLoader.class);

    private ResourceLoader resourceLoader;

    private URI viewersConfigDir;

    private CertificateStore certificateStore;

    // === //

    public ViewerResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
        viewersConfigDir = null;
    }

    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public void setViewerConfigDir(URI configDir)
    {
        logger.infoPrintf("ViewerConfigDir=%s\n", configDir);
        this.viewersConfigDir = configDir;
    }

    public URI getViewerConfigDir()
    {
        return viewersConfigDir;
    }

    public InputStream openInputStream(URI uri) throws IOException
    {
        // register/cache streams ?
        return resourceLoader.createInputStream(uri);
    }

    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }

    public void writeText(URI uri, String txt, String encoding) throws IOException
    {
        resourceLoader.writeTextTo(uri, txt, encoding);
    }

    public String getText(URI uri, String textEncoding) throws IOException
    {
        return resourceLoader.readText(uri, textEncoding);
    }

    public boolean hasReplicas(URI uri)
    {
        return false;
    }

    public URI[] getReplicas(URI uri)
    {
        return null;
    }

    public Properties loadProperties(URI uri) throws IOException
    {
        if (uri == null)
            return null;

        return resourceLoader.loadProperties(uri);
    }

    public void saveProperties(URI uri, Properties properties) throws IOException
    {
        logger.infoPrintf("Saving Properties to:" + uri);
        if (uri == null)
            return;

        resourceLoader.saveProperties(uri, properties);
    }

    public void syncReadBytes(RandomReader reader, long fileOffset, byte[] buffer, int bufferOffset, int numBytes) throws IOException
    {
        // delegate to IOUtil
        IOUtil.syncReadBytes(reader, fileOffset, buffer, bufferOffset, numBytes);
        // reader.close();
    }

    public void syncWriteBytes(RandomWriter writer, long fileOffset, byte[] buffer, int bufferOffset, int numBytes) throws IOException
    {
        writer.writeBytes(fileOffset, buffer, bufferOffset, numBytes);
        writer.close();
    }

    public CertificateStore getCertificateStore() throws CertificateStoreException
    {
        if (this.certificateStore == null)
        {
            certificateStore = CertificateStore.getDefault(true);
        }
        return certificateStore;
    }

    public void setCertificateStore(CertificateStore store)
    {
        this.certificateStore = store;
    }

    public String getMimeType(URI uri)
    {
        return MimeTypes.getDefault().getMimeType(uri.getPath());
    }

    public RandomReader createRandomReader(URI loc) throws IOException
    {
        return resourceLoader.createRandomReader(loc);
    }

    public RandomWriter createRandomWriter(URI loc) throws IOException
    {
        return resourceLoader.createRandomWriter(loc);
    }

}
