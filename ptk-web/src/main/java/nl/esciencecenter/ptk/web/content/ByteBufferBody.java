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

import nl.esciencecenter.ptk.web.PutMonitor;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Byte Buffer body which can be monitored during the upload.
 */
public class ByteBufferBody extends AbstractContentBody {

    private final byte[] data;

    private final String filename;

    private final PutMonitor putMonitor;

    public ByteBufferBody(final byte[] data, final String mimeType, final String filename,
                          PutMonitor optPutMonitor) {
        super(mimeType);
        if (data == null) {
            throw new IllegalArgumentException("byte[] may not be null");
        }
        this.data = data;
        this.filename = filename;
        this.putMonitor = optPutMonitor;
    }

    public ByteBufferBody(final byte[] data, final String filename) {
        this(data, "application/octet-stream", filename, null);
    }

    public String getFilename() {
        return filename;
    }

    public void writeTo(final OutputStream out) throws IOException {
        //
        int chunkSize = 32 * 1024; // 32k chunks 
        int numWritten = 0;

        while (numWritten < data.length) {

            int numToWrite = data.length - numWritten;
            if (numToWrite > chunkSize) {
                numToWrite = chunkSize;
            }

            out.write(data, numWritten, numToWrite);
            numWritten += numToWrite;
            if (putMonitor != null) {
                putMonitor.bytesWritten(numWritten);
            }
        }

        if (putMonitor != null) {
            putMonitor.putDone();
        }

    }

    public String getCharset() {
        return null;
    }

    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public long getContentLength() {
        return data.length;
    }

}
