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

package nl.esciencecenter.ptk.io.local;

import java.io.IOException;
import java.io.RandomAccessFile;

import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;

public class LocalFSReader implements RandomReadable
{
    protected LocalFSNode fsNode;

    protected RandomAccessFile randomFile = null;

    public LocalFSReader(LocalFSNode node) throws IOException
    {
        this.fsNode = node;
        this.randomFile = new RandomAccessFile(fsNode.toJavaFile(), "r");
    }

    public int readBytes(long fileOffset, byte[] buffer, int bufferOffset, int nrBytes) throws IOException
    {
        RandomAccessFile afile = null;

        try
        {
            // Seek sets position starting from beginnen, not current seek position.
            randomFile.seek(fileOffset);
            int nrRead = randomFile.read(buffer, bufferOffset, nrBytes);
            return nrRead;
        }
        catch (IOException e)
        {
            throw new IOException("Could open location for reading:" + this, e);
        }
        finally
        {
            if (afile != null)
            {
                try
                {
                    // Must close between Reads! (not fast but ensures consistency between reads).
                    afile.close();
                }
                catch (IOException e)
                {
                }
            }
        }

    }

    @Override
    public long getLength() throws IOException
    {
        return fsNode.getFileSize();
    }

    public boolean autoClose()
    {
        if (randomFile == null)
        {
            return false;
        }
        boolean status = IOUtil.autoClose(randomFile);
        randomFile = null;
        return status;
    }

    @Override
    public void close()
    {
        autoClose();
    }

}
