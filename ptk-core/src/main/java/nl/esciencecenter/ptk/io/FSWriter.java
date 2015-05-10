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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stateless File writer which opens and closes the specified path per write action.
 */
public class FSWriter implements Writable, RandomWritable, AutoCloseable {

    protected Path _path;

    public FSWriter(Path path) throws FileNotFoundException {
        _path = path;
    }

    /**
     * Perform stateless write which opens and closes file again after writing.
     */
    @Override
    public void writeBytes(long fileOffset, byte[] buffer, int bufferOffset, int nrBytes)
            throws IOException {
        try (RandomAccessFile randomFile = new RandomAccessFile(_path.toFile(), "rw")) {
            randomFile.seek(fileOffset);
            randomFile.write(buffer, bufferOffset, nrBytes);
            return;
        } catch (IOException e) {
            throw new IOException("Failed to writeBytes to:" + _path, e);
        }
    }

    @Override
    public void write(byte[] buffer, int bufferOffset, int numBytes) throws IOException {
        writeBytes(0, buffer, bufferOffset, numBytes);
    }

    @Override
    public long getLength() throws IOException {
        return Files.size(_path);
    }

    @Override
    public void close() {
    }

}
