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
import java.util.Properties;

import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.util.ResourceLoader;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Content Factory and Resource Manager for the various embedded Viewers.
 */
public class ViewerResourceLoader
{
    private static ClassLogger logger = ClassLogger.getLogger(ViewerResourceLoader.class);

    // ========
    // Instance
    // ======== 
    private VRSClient vrsClient;
    
    private ResourceLoader resourceLoader;

    private VRL viewersConfigDir;

    private CertificateStore certificateStore;

    // === //

    public ViewerResourceLoader(VRSClient vrsClient, VRL viewersConfigDir)
    {
        this.vrsClient=vrsClient;
        this.resourceLoader = vrsClient.createResourceLoader();
        logger.infoPrintf("ViewerConfigDir=%s\n", viewersConfigDir);
        this.viewersConfigDir = viewersConfigDir;
    }

    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    protected void setViewerConfigDir(VRL configDir)
    {
        this.viewersConfigDir = configDir;
    }

    public VRL getViewerConfigDir()
    {
        return viewersConfigDir;
    }

    public InputStream openInputStream(VRL uri) throws Exception
    {
        // register/cache streams ?
        return vrsClient.createInputStream(uri);
    }

    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }

    public void writeText(VRL vrl, String txt, String encoding) throws Exception
    {
        resourceLoader.writeTextTo(vrl.toURI(), txt, encoding);
    }
    
    public String readText(VRL vrl, String textEncoding) throws Exception
    {
        return resourceLoader.readText(vrl.toURI(), textEncoding);
    }

    public boolean hasReplicas(VRL vrl)
    {
        return false;
    }

    public VRL[] getReplicas(VRL vrl)
    {
        return null;
    }

    public Properties loadProperties(VRL vrl) throws Exception
    {
        if (vrl == null)
            return null;

        return resourceLoader.loadProperties(vrl.toURI());
    }

    public void saveProperties(VRL vrl, Properties properties) throws Exception
    {
        logger.infoPrintf("Saving Properties to:" + vrl);
        if (vrl == null)
            return;

        resourceLoader.saveProperties(vrl.toURI(), properties);
    }

    public void syncReadBytes(RandomReadable reader, long fileOffset, byte[] buffer, int bufferOffset, int numBytes) throws IOException
    {
        // delegate to IOUtil
        IOUtil.syncReadBytes(reader, fileOffset, buffer, bufferOffset, numBytes);
        // reader.close();
    }

    public void syncWriteBytes(RandomWritable writer, long fileOffset, byte[] buffer, int bufferOffset, int numBytes) throws IOException
    {
        writer.writeBytes(fileOffset, buffer, bufferOffset, numBytes);
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
    
    public String getMimeType(String path)
    {
        return MimeTypes.getDefault().getMimeType(path);
    }
    
    public RandomReadable createRandomReader(VRL loc) throws Exception
    {
        return vrsClient.createRandomReader(vrsClient.openPath(loc));
    }

    public RandomWritable createRandomWriter(VRL loc) throws Exception
    {
        return vrsClient.createRandomWriter(vrsClient.openPath(loc));
    }


}
