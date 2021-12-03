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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.task.ITaskMonitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO helper methods. Collected from legacy code, could be cleaned up.
 */
@Slf4j
public class IOUtil {

    private static final int defaultBufferSize = 1 * 1024 * 1024;

    public static class ReadFunctor implements Readable {

        protected InputStream inps;

        public ReadFunctor(InputStream inps) {
            this.inps = inps;
        }

        public int read(byte[] buffer, int bufferOffset, int numBytes) throws IOException {
            return inps.read(buffer, bufferOffset, numBytes);
        }

        public void close() throws Exception {
            inps.close();
        }
    }

    public static class RandomReaderFunctor implements Readable {

        protected RandomReadable reader;

        protected long offset;

        public RandomReaderFunctor(RandomReadable reader, long offset) {
            this.reader = reader;
            this.offset = offset;
        }

        public int read(byte[] buffer, int bufferOffset, int numBytes) throws IOException {
            return reader.readBytes(offset, buffer, bufferOffset, numBytes);
        }

        public void close() throws Exception {
            reader.close();
        }

    }

    /**
     * Copy all the data from the InputStream to the OutputStream.
     *
     * @param input
     * @param output
     * @param autoCloseStreams - set to true to close the Input- and OuputStream after a copy.
     * @throws IOException
     */
    public static long copyStreams(InputStream input, OutputStream output, boolean autoCloseStreams) throws IOException {
        return circularStreamCopy(input, output, -1, defaultBufferSize, autoCloseStreams, null);
    }

    public static long circularStreamCopy(InputStream input, OutputStream output, boolean autoCloseStreams)
            throws IOException {
        return circularStreamCopy(input, output, -1, defaultBufferSize, autoCloseStreams, null);
    }

    /**
     * Dual threaded stream copy starts a reader thread in the background and performs the writes
     * during the current thread. This way full duplex reading and writing occurs during the copy.
     *
     * @param inputs       - InputStream to read from.
     * @param outputs      - OutputStream to write to.
     * @param nrToTransfer - number of bytes to transfer. Set to -1 for continue until EOF encountered.
     * @param bufferSize   - buffer size to use during transfer. This is not the actual chunk size used
     *                     during read and write operations.
     * @param autoClose    - whether to close the streams after the transfer.
     * @param monitor      - optional Task Monitor
     * @return number of bytes transferred. This either matches nrToTransfer, or in the case if
     * nrToTransfer has been set to -1, the actual number of transferred bytes.
     * @throws IOException - an EOF exception is thrown if the nrToTransfer bytes > 0 and that number could
     *                     not be transferred.
     */
    public static long circularStreamCopy(InputStream inputs, OutputStream outputs, long nrToTransfer, int bufferSize,
                                          boolean autoClose, ITaskMonitor monitor) throws IOException {
        log.debug("circularStreamCopy():START: bufSize={}, totalToTransfer={}", bufferSize, nrToTransfer);

        // Setup & Initiate Stream Copy:
        //
        String subTaskName = "Performing stream copy";

        try {
            if (monitor != null) {
                monitor.startSubTask(subTaskName, nrToTransfer);
            }

            // do not allocate buffer size bigger than than file size
            if ((nrToTransfer > 0) && (nrToTransfer < bufferSize)) {
                bufferSize = (int) nrToTransfer;
            }

            // Use CirculareStreamBuffer to copy from InputStream =>
            // OutputStream
            BufferStreamTransferer cbuffer = new BufferStreamTransferer(bufferSize);

            // update into this object please:
            cbuffer.setTaskMonitor(monitor);
            //
            // nrToTransfer=-1 -> then UNKNOWN !

            // ***
            // SFTP-WRITE-OUTPUTSTREAM-32000
            // Bug in SFTP. The OutputStream has problems when writing
            // chunks > 32000.
            // ***
            cbuffer.setMaxWriteChunkSize(32000);

            cbuffer.setMaxReadChunkSize(1024 * 1014);
            // check optimal read buffer size.
            int optimalReadChunkSize = 1024 * 1024;

            if (optimalReadChunkSize > 0) {
                cbuffer.setMaxReadChunkSize(optimalReadChunkSize);
            }

            log.trace("- streamCopy transferSize   ={}", nrToTransfer);
            log.trace("- streamCopy readChunkSize  ={}", cbuffer.getReadChunkSize());
            log.trace("- streamCopy writeChunkSize ={}", cbuffer.getWriteChunkSize());
            log.trace("- streamCopy buffer size    ={}", cbuffer.getCopyBufferSize());

            // start background writer:
            cbuffer.setInputStream(inputs);
            cbuffer.setOutputstream(outputs);

            // ====================================
            // Transfer !
            // ====================================

            // Will end when done.
            // StartTransfer will close the streams and updates transferMonitor
            cbuffer.startTransfer(nrToTransfer);

            // ====================================
            // POST Chunk Copy Loop
            // ====================================

            if (autoClose) {
                outputs.flush();
                autoClose(outputs);
                autoClose(inputs);
            }

            long numTransferred = cbuffer.getTotalWritten();

            if (monitor != null) {
                monitor.updateSubTaskDone(subTaskName, numTransferred);
                monitor.endSubTask(subTaskName);
            }

            log.trace("circularStreamCopy():DONE: totalTransferred={}", numTransferred);

            return numTransferred;
        } catch (Exception ex) {
            if (monitor != null) {
                monitor.endSubTask("Performing stream copy: Error");
            }

            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException("StreamCopy Failed:" + ex.getMessage(), ex);
            }
        } finally {
            // done
        }
    }

    /**
     * Read exactly numBytes, if actually returned number of bytes < numBytes the end of the file
     * was encountered !
     *
     * @throws IOException
     */
    public static int syncReadLoop(Readable readerF, byte[] buffer, int bufferOffset, int numBytes, int timeOutMillies)
            throws IOException {
        log.debug("syncReadLoop: Will try to read:{}", numBytes);

        int microSleepTime = 10;

        int totalRead = 0;
        int nullReads = 0;

        while (totalRead < numBytes) {
            int numToRead = (numBytes - totalRead);
            int numRead = readerF.read(buffer, bufferOffset, numToRead);

            if (numRead > 0) {
                totalRead += numRead;
                // reset;
                nullReads = 0;

                log.trace("syncReadLoop: Current numRead/totalRead={}/{}", numRead, totalRead);
            } else if (numRead == 0) {
                // ---
                // Although java read() specifies 0 is only returned when numBytes==0, the underlaying implementation
                // might stil return 0 when there is no data (yet) available.
                // For asynchronous reads, returning 0 is actaully allowed.
                // ---

                // micro sleep: 10 milli seconds time out
                if ((timeOutMillies > 0) && (nullReads * microSleepTime >= timeOutMillies)) {
                    throw new IOException("Reading Timeout waiting time>" + timeOutMillies);
                }

                nullReads++;

                try {
                    Thread.sleep(microSleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted:{}", e);
                    // Contract of Interrupted is to stop immediately.
                    throw new IOException("Copy thread was Interrupted:" + e.getMessage(), e);
                }
            } else if (numRead < 0) {
                if (totalRead > 0) {
                    // No more bytes left, return remainder
                    return totalRead;
                } else {
                    throw new IOException("EOF: Can not read past end.\n");
                }
            }
        }

        log.debug("syncReadLoop: Finished totalRead={}", totalRead);
        return totalRead;
    }

    /**
     * Synchronized read loop which performs several readBytes() calls to fill buffer. Helper method
     * for read() method with should be done in a loop. (This since File.read() or InputStream
     * reads() don't always return the desired nr. of bytes. This method keeps on reading until
     * either End Of File is reached (EOF) or the desired nr of bytes is read.
     * <p>
     * Returns -1 when EOF is encountered, or actual nr of bytes read. If the return value doesn't
     * match the nrBytes wanted, no extra bytes could be read so this method doesn't have to be
     * called again.
     */
    public static int readAll(RandomReadable source, long fileOffset, byte[] buffer, int bufferOffset, int nrBytes)
            throws IOException {
        // Delegate to functor:
        RandomReaderFunctor reader = new RandomReaderFunctor(source, fileOffset);
        return syncReadLoop(reader, buffer, bufferOffset, nrBytes, -1);
    }

    /**
     * Read all bytes from InputStream until an EOF or other IOException occored. InputStream won't
     * be closed.
     *
     * @param autoClose
     */
    public static byte[] readAll(InputStream inps, boolean autoClose) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buf = new byte[32 * 1024]; // typical TCP/IP packet size:
        int len = 0;

        try {
            while ((len = inps.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new IOException("Couldn't read from input stream", e);
        }
        if (autoClose) {
            inps.close();
        }
        byte[] data = bos.toByteArray();
        return data;
    }

    /**
     * Close InputStream and ignore exceptions.
     */
    public static boolean autoClose(InputStream inps) {
        if (inps == null) {
            return false;
        }

        try {
            inps.close();
            return true;
        } catch (IOException e) {
            log.debug("Silent closing exception:" + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Close OutputStream and ignore exceptions.
     */
    public static boolean autoClose(OutputStream outps) {
        if (outps == null) {
            return false;
        }

        try {
            outps.close();
            return true;
        } catch (IOException e) {
            log.debug("Silent closing exception:" + e.getMessage(), e);
            return false;
        }
    }

}
