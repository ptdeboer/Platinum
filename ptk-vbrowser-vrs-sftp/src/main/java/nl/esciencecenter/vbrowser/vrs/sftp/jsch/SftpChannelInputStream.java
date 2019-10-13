/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

package nl.esciencecenter.vbrowser.vrs.sftp.jsch;

import java.io.IOException;
import java.io.InputStream;

/**
 * Private SftpChannel+ SftpInputStream combination. Autocloses both InputStream and SftpChannel.
 */
public class SftpChannelInputStream extends InputStream implements AutoCloseable {

    private InputStream inps = null;

    private SftpChannel channel;

    private long numRead = 0;

    public SftpChannelInputStream(SftpChannel newChannel, InputStream inps) {
        this.inps = inps;
        this.channel = newChannel;
    }

    public int read() throws IOException {
        int val = inps.read();
        numRead++;
        return val;
    }

    public int read(byte[] buffer) throws IOException {
        int val = inps.read(buffer);
        if (val > 0)
            numRead += val;
        return val;
    }

    public int read(byte[] buffer, int offset, int len) throws IOException {
        int val = inps.read(buffer, offset, len);
        if (val > 0)
            numRead += val;
        return val;
    }

    public void reset() throws IOException {
        inps.reset();
    }

    public void close() throws IOException {
        inps.close();
        channel.close();
    }

    public int available() throws IOException {
        return inps.available();
    }

    public boolean markSupported() {
        return inps.markSupported();
    }

    public void mark(int limit) {
        inps.mark(limit);
    }

    public long skip(long len) throws IOException {
        return inps.skip(len);
    }

    public long getNumRead() {
        return numRead;
    }

}
