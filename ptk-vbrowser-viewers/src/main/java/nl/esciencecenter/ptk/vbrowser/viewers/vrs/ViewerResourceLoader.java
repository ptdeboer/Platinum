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

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ui.icons.IconProvider;
import nl.esciencecenter.ptk.util.ContentReader;
import nl.esciencecenter.ptk.util.ContentWriter;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Content Factory and Resource Manager for the various embedded Viewers.<br>
 * 
 */
public class ViewerResourceLoader {

    private static Logger logger = LoggerFactory.getLogger(ViewerResourceLoader.class);

    // === Instance === 

    private VRSClient vrsClient;

    private String viewersConfigSubDirName;

    public ViewerResourceLoader(VRSClient vrsClient, String viewersConfigSubDirName) {
        logger.info("ViewerResourceLoader():viewersConfigSubDirName={}", viewersConfigSubDirName);
        this.viewersConfigSubDirName = viewersConfigSubDirName;
        this.vrsClient = vrsClient;
    }

    public VRL getViewerConfigDir() {
        VRL conf = vrsClient.getVRSContext().getPersistantConfigLocation();
        if (conf == null)
            return null;
        return conf.appendPath(viewersConfigSubDirName);
    }

    public InputStream openInputStream(VRL uri) throws Exception {
        // register/cache streams ?
        return vrsClient.createInputStream(uri);
    }

    public void writeText(VRL vrl, String txt, String encoding) throws Exception {
        try (OutputStream outps = vrsClient.createOutputStream(vrl)) {
            new ContentWriter(outps, encoding, false).write(txt);
        }
        // autoclose
    }

    public String readText(VRL vrl, String textEncoding) throws Exception {
        try (InputStream inps = vrsClient.createInputStream(vrl)) {
            return new ContentReader(inps, textEncoding, false).readString();
        }
        // autoclose
    }

    public Properties loadProperties(VRL vrl) throws Exception {
        if (vrl == null)
            return null;

        try (InputStream inps = vrsClient.createInputStream(vrl)) {
            return new ContentReader(inps).loadProperties();
        }
    }

    public void saveProperties(VRL vrl, Properties properties, String comments) throws Exception {
        createViewersConfigDir();
        logger.info("Saving Properties to:{}", vrl);
        try (OutputStream outps = vrsClient.createOutputStream(vrl)) {
            new ContentWriter(outps).saveProperties(properties, comments);
        }
        //finally: autoclose
    }

    protected void createViewersConfigDir() throws VrsException {
        VRL vrl = this.getViewerConfigDir();
        VFSPath path = vrsClient.openVFSPath(vrl);
        if (path.exists()==false) { 
            path.mkdir(true);
        }
    }

    public void syncReadBytes(RandomReadable reader, long fileOffset, byte[] buffer, int bufferOffset, int numBytes)
            throws IOException {
        // delegate to IOUtil
        IOUtil.readAll(reader, fileOffset, buffer, bufferOffset, numBytes);
        // reader.close();
    }

    public void syncWriteBytes(RandomWritable writer, long fileOffset, byte[] buffer, int bufferOffset, int numBytes)
            throws IOException {
        writer.writeBytes(fileOffset, buffer, bufferOffset, numBytes);
    }

    public CertificateStore getCertificateStore() throws VrsException {
        return vrsClient.getVRSContext().getCertificateStore();
    }

    public String getMimeType(String path) {
        return MimeTypes.getDefault().getMimeType(path);
    }

    public RandomReadable createRandomReader(VRL loc) throws Exception {
        return vrsClient.createRandomReader(vrsClient.openPath(loc));
    }

    public RandomWritable createRandomWriter(VRL loc) throws Exception {
        return vrsClient.createRandomWriter(vrsClient.openPath(loc));
    }

    public String getMimeTypeOf(VRL vrl) throws VrsException {
        return vrsClient.openPath(vrl).getMimeType();
    }

    /**
     * Warning: VRSClient might not be visible in future interface definitions.
     */
    public VRSClient getVRSClient() {
        return this.vrsClient;
    }

    public IconProvider createIconProvider(Component component) {
        return new IconProvider(component, vrsClient.createResourceLoader());
    }

}
