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

package nl.esciencecenter.ptk.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stateless File reader which opens and closes the specified path per read action.
 */
public class FSReader implements Readable, RandomReadable, AutoCloseable {

    protected Path _path;

    public FSReader(Path path) throws IOException {
        this._path = path;
    }

    /**
     * Perform stateless read which opens and closes file again after reading.
     */
    @Override
    public int readBytes(long fileOffset, byte[] buffer, int bufferOffset, int nrBytes) throws IOException {
        // perform 'atomic' read.
        try (RandomAccessFile rafile = new RandomAccessFile(_path.toFile(), "r")) {
            // Seek sets position starting from beginnen, not current seek position.
            rafile.seek(fileOffset);
            int nrRead = rafile.read(buffer, bufferOffset, nrBytes);
            return nrRead;
        } catch (IOException e) {
            throw new IOException("Failed to readBytes from:" + _path, e);
        }
    }

    @Override
    public long getLength() throws IOException {
        return Files.size(_path);
    }

    @Override
    public int read(byte[] buffer, int bufferOffset, int numBytes) throws IOException {
        return readBytes(0, buffer, bufferOffset, numBytes);
    }

    @Override
    public void close() {
    }

}
