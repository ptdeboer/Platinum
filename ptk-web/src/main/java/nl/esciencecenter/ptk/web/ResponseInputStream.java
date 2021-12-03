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

package nl.esciencecenter.ptk.web;

import nl.esciencecenter.ptk.object.Disposable;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Managed ResponseInputStream from a HttpGet Request. <br>
 * This Stream auto closes the Actual HTTP STream when an IOException occurs no input is available.
 * This to release the HTTP Connection ASAP. This is needed when using a Multi-threaded connection
 * manager. Either call close() or autoClose(), which both are idempotent methods, they can be
 * called more then once, and don't throw exceptions when closing streams.
 */
public class ResponseInputStream extends InputStream implements WebStream, Disposable,
        AutoCloseable {

    protected WebClient webClient;

    protected HttpGet getMethod;

    protected HttpEntity responseEntity;

    protected InputStream sourceStream;

    private final URI uri;

    private IOException closeException;

    public ResponseInputStream(WebClient client, HttpGet getMethod, HttpEntity entity)
            throws IllegalStateException, IOException {
        this.webClient = client;
        this.getMethod = getMethod;
        this.responseEntity = entity;
        this.sourceStream = entity.getContent();
        this.uri = getMethod.getURI();
    }

    @Override
    public int read() throws IOException {

        if (sourceStream == null) {
            return -1;
        }

        try {
            return sourceStream.read();
        } catch (IOException e) {
            autoClose();
            throw e;
        }
    }

    @Override
    public int read(byte[] bytes) throws IOException {

        if (sourceStream == null) {
            return -1;
        }

        try {
            return sourceStream.read(bytes);
        } catch (IOException e) {
            autoClose();
            throw e;
        }
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {

        if (sourceStream == null) {
            return -1;
        }

        try {
            return sourceStream.read(bytes, offset, length);
        } catch (IOException e) {
            autoClose();
            throw e;
        }
    }

    @Override
    public void reset() throws IOException {
        sourceStream.reset();
    }

    @Override
    public void mark(int readLimit) {
        sourceStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return sourceStream.markSupported();
    }

    /**
     * Returns whether HTTPStream is chunked
     */
    public boolean isChunked() {

        if (responseEntity == null)
            return false;

        return responseEntity.isChunked();
    }

    /**
     * Get MimeType of ResponseStream.
     */
    public String getMimeType() {
        //
        if (responseEntity == null)
            return null;

        Header type = responseEntity.getContentType();

        if (type == null)
            return null;

        return type.getValue();
    }

    /**
     * Get Content Encoding of ResponseStream.
     */
    public String getContentEncoding() {
        Header encoding = responseEntity.getContentEncoding();
        if (encoding == null)
            return null;
        return encoding.getValue();
    }

    /**
     * Close the underlying InputStream. If the InputStream was already closed or an IOException
     * occure this method will return false. If the close was successful the method return true
     *
     * @returns - true if the close was successful, false if the stream was already close or an
     * Exception occured.
     */
    public boolean autoClose() {
        //
        if ((this.sourceStream == null) && (getMethod == null)) {
            return false;
        }
        //
        try {
            close();
            //webClient.getLogger().log.debug("autoClose(): successful for:"+this);
            return true;
        } catch (IOException e) {
            closeException = e;
            return false;
        }
    }

    public void close() throws IOException {
        //
        if (getMethod != null) {
            getMethod.releaseConnection();
            getMethod = null;
        }
        //
        if (this.sourceStream != null) {
            try {
                sourceStream.close();
                sourceStream = null;
            } catch (IOException e) {
                throw e; // ignore here ? 
            } finally {
                sourceStream = null;
            }
        }
    }

    // For debugging:... 
    public IOException getCloseException() {
        return this.closeException;
    }

    public void dispose() {
        autoClose();
    }

    public void finalize() {
        autoClose();
    }

    public String toString() {
        return "<ResponseInputStream:> open=" + (sourceStream == null ? "open" : "close")
                + ", uri=" + uri;
    }
}
