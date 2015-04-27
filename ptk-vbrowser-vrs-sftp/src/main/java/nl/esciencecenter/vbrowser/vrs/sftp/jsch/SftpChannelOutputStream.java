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
import java.io.OutputStream;

/**
 * Private SftpChannel+ SftpOutputStream combination. 
 * Autocloses both OutputStream and SftpChannel. 
 */
public class SftpChannelOutputStream extends OutputStream implements AutoCloseable
{
    private OutputStream outps = null;

    private SftpChannel channel;

    public SftpChannelOutputStream(OutputStream outps, SftpChannel outputChannel)
    {
        this.outps = outps;
        this.channel = outputChannel;
    }

    @Override
    public void write(int b) throws IOException
    {
        outps.write(b);
    }

    @Override
    public void write(byte bytes[]) throws IOException
    {
        outps.write(bytes);
    }

    @Override
    public void write(byte bytes[], int offset, int len) throws IOException
    {
        outps.write(bytes, offset, len);
    }

    public void close() throws IOException
    {
        outps.close();
        channel.disconnect();
    }

    public void flush() throws IOException
    {
        outps.flush();
    }

}
