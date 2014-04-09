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

import nl.esciencecenter.ptk.io.RandomWritable;

public class LocalFSWriter implements RandomWritable
{

    private LocalFSNode fsNode;

    public LocalFSWriter(LocalFSNode node)
    {
        fsNode=node;
    }

    @Override
    public void writeBytes(long fileOffset, byte[] buffer, int bufferOffset,  int nrBytes) throws IOException
    {
        RandomAccessFile afile = null;

        try
        {
            afile = new RandomAccessFile(fsNode.toJavaFile(), "rw");
            afile.seek(fileOffset);
            afile.write(buffer, bufferOffset, nrBytes);
            afile.close(); // MUST CLOSE !
            // if (truncate)
            // afile.setLength(fileOffset+nrBytes);
            return;// if failed, some exception occured !
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (afile!=null)
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
    public void close() throws IOException
    {
        
    }
    

}
