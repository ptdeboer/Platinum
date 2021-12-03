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

package nl.esciencecenter.ptk.web.content;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.web.PutMonitor;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File Body which uses FSNodes. <br>
 * Based on Apache FileBody. Example how to upload custom Object types. Since FSNode can be
 * sub-classed, any file type can be uploaded using this Content Body Type.
 */
@Slf4j
public class FSNodeBody extends AbstractContentBody {

    private final FSPath fsNode;

    private long totalWritten = 0;

    private PutMonitor putMonitor = null;

    private final int defaultChunkSize = 4096;

    public FSNodeBody(final FSPath node, final String mimeType, PutMonitor putMonitor) {
        super(mimeType);
        this.putMonitor = putMonitor;

        if (node == null) {
            throw new IllegalArgumentException("File may not be null");
        }

        this.fsNode = node;
    }

    public FSNodeBody(final FSPath node) {
        this(node, "application/octet-stream", null);
    }

    public FSNodeBody(final FSPath node, PutMonitor putMonitor) {
        this(node, "application/octet-stream", putMonitor);
    }

    public InputStream getInputStream() throws IOException {
        return fsNode.getFSInterface().createInputStream(fsNode);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        InputStream in = fsNode.getFSInterface().createInputStream(fsNode);

        try {
            byte[] tmp = new byte[defaultChunkSize];

            int numRead = 0;

            while ((numRead = in.read(tmp)) >= 0) {
                out.write(tmp, 0, numRead);

                if (numRead == 0) {
                    // micro sleep: allow IO to happen when thread sleeps. 
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                totalWritten += numRead;
                debug("Total written={}", totalWritten);

                if (putMonitor != null) {
                    putMonitor.bytesWritten(totalWritten);
                }
            }

            out.flush();

            if (putMonitor != null) {
                putMonitor.bytesWritten(totalWritten);
                putMonitor.putDone();
            }

            debug("Done: Total written={}", totalWritten);
        } finally {
            in.close();
        }
    }


    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public String getCharset() {
        return null;
    }

    public long getContentLength() {
        try {
            return this.fsNode.getFileSize();
        } catch (IOException e) {
            return 0;
        }
    }

    public String getFilename() {
        return this.fsNode.getBasename();
    }

    public long getProgress() {
        return totalWritten;
    }

    private void debug(String format, Object... args) {
        log.debug(format, args);
    }

}
