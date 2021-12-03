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
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Managed ResponseOutputStream. Under construction.
 */
public class ResponseOutputStream extends OutputStream implements WebStream, Disposable,
        AutoCloseable {

    protected WebClient webClient;

    protected HttpPut putMethod;

    protected OutputStream sourceStream;

    private final URI uri;

    public ResponseOutputStream(WebClient client, HttpPut putMethod) throws IllegalStateException,
            IOException {
        this.webClient = client;
        this.putMethod = putMethod;
        this.uri = putMethod.getURI();
    }

    @Override
    public void write(int b) throws IOException {
        sourceStream.write(b);
    }

    @Override
    public void write(byte[] bytes, int offset, int numBytes) throws IOException {
        sourceStream.write(bytes, offset, numBytes);
    }

    @Override
    public void flush() throws IOException {
        sourceStream.flush();
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
        if ((this.sourceStream == null) && (putMethod == null)) {
            return false;
        }
        //
        try {
            close();
            //webClient.getLogger().log.debug("autoClose(): successful for:"+this);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void dispose() {
        autoClose();
    }

    public void finalize() {
        autoClose();
    }

    public String toString() {
        return "<ResponseOutputStream:> open=" + (sourceStream == null ? "open" : "close")
                + ", uri=" + uri;
    }

}
