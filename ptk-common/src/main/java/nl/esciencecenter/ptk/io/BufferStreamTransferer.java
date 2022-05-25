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
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BufferStreamTransferer copies data from in InputStream to an OutputStream. <br>
 * It uses a circular buffer to transfer bytes from the InputStream to the OutputStream. It starts
 * the reader in a background thread while waiting for the reader to fill the buffer and starting
 * writing the data in current thread. This parallel read/write will better use the available
 * bandwidth by both reading and writing in parallel.
 */
@Slf4j
public class BufferStreamTransferer {

    // === instance ===

    private final String streamCopySubTaskName = "Performing StreamCopy";

    // data buffer: acces is synchronized.
    // This object is also used as general mutex.
    private byte[] buffer = null;

    private long totalRead = 0;

    private long nrWritten = 0;

    /**
     * either unkownSize==true or nrToTransfer>0)
     */
    private boolean unknownSize = true;

    /**
     * either unkownSize==true or nrToTransfer>0)
     */
    private long nrToTransfer = -1; // keep unknown for now

    /**
     * time in milliseconds
     */
    private long readTime = 0;

    /**
     * time in milliseconds
     */
    private long writeTime = 0;

    /**
     * Optional Transfer info. Class will update current transfer by updating the nr of bytes
     * currently transferred by this buffer.
     */
    ITaskMonitor transferInfo = null;

    // ***
    // optimization/limitation options
    // ***

    /**
     * nr of bytes thats get read per read iteration:32k buffers
     */
    private int readChunkSize = 32 * 1024;

    /**
     * nr of bytes thats get written each write iteration: 32k buffer.
     */
    private int writeChunkSize = 32 * 1024;

    /**
     * buffer size to use
     */
    private int bufferSize = 0;

    /**
     * Creates new RingBuffer using as internal buffer with size "size"
     */
    public BufferStreamTransferer(int size) {
        init(size);
    }

    /**
     * Creates new RingBuffer using as internal buffer with size "size"
     */
    public BufferStreamTransferer(int size, InputStream input, OutputStream output) {
        init(size);
        this.setInputStream(input);
        this.setOutputStream(output);
    }

    private void init(int size) {
        this.buffer = new byte[size];
        this.bufferSize = size;
    }

    private final Boolean readerWait = true;

    private final Boolean writerWait = true;

    private OutputStream outputStream = null;

    private InputStream inputStream = null;

    private boolean cancelTransfer = false;

    private ActionTask readerTask = null;

    /**
     * Sets the OutputStream to write to. Can be called only once.
     */
    public void setOutputStream(OutputStream outp) {
        if (this.outputStream != null)
            throw new Error("Cannot set OutputStream twice!");

        this.outputStream = outp;
    }

    /**
     * Sets the InputStream to read from. Can be called only once
     */
    public void setInputStream(InputStream inp) {
        if (this.inputStream != null)
            throw new Error("Cannot set InputStream twice!");

        this.inputStream = inp;
    }

    /**
     * Limits the nr of bytes thats get written each write iteration. If the OutputStream can not
     * efficiently (or not at all!) handle big writes, limit the maximum with this method.
     */
    public void setMaxWriteChunkSize(int size) {
        this.writeChunkSize = size;
    }

    /**
     * Limits the nr of bytes thats get read each read iteration. If the InputStream can not
     * efficiently (or not at all!) handle big reads, limit the maximum with this method. Usually
     * the 'read()' method already reads the nr. bytes it can handle per read (which it returns).
     */
    public void setMaxReadChunkSize(int size) {
        this.readChunkSize = size;
    }

    /**
     * Starts read loop. Is started in the background by startTransfer().
     */
    protected void readLoop() throws Exception {
        int buflen = buffer.length;

        try {
            // do loop while there is data left
            while ((unknownSize == true) || (totalRead < nrToTransfer)) {
                if (mustStop())
                    throw new InterruptedException("Transfer interrupted!");

                int start = 0;
                int delta = 0;

                // Do buffer calculations in MuTex'd time:
                synchronized (buffer) {
                    delta = buflen; // ? istr.available();
                    // free space in buffer
                    int free = buflen - (int) (totalRead - nrWritten);
                    // do not read past free space in buffer
                    if (delta > free)
                        delta = free;

                    // do not read too much at once:
                    if (delta > readChunkSize)
                        delta = readChunkSize;

                    // do not read past end of file (if size is known)
                    if (nrToTransfer >= 0)
                        if (totalRead + delta > nrToTransfer)
                            delta = (int) (nrToTransfer - totalRead);

                    // start in cicular buffer
                    start = (int) (totalRead % buflen);

                    // do not read past buffer end (wrap around)
                    if (start + delta > buflen)
                        delta = buflen - start;
                }

                log.trace("reader: nrRead    ={}", totalRead);
                log.trace("reader: nrWritten ={}", nrWritten);
                log.trace("reader: nrToRead  ={}", nrToTransfer);
                log.trace("reader: start     ={}", start);
                log.trace("reader: delta     ={}", delta);

                // new data to tranfer ?
                if (delta > 0) {
                    long startTime = System.currentTimeMillis();

                    int n = inputStream.read(buffer, start, delta);

                    if (n < 0) {
                        if (unknownSize == true) {
                            // EOF when reading from unknown InputStream:
                            // We know the size now, update nrToTransfer to
                            // current
                            // number bytes read. This will stop the read and
                            // the write.
                            // Set unknownSize to false to trigger updating the
                            // stats.

                            unknownSize = false;
                            nrToTransfer = totalRead;
                        } else {
                            throw new IOException("Failed to read expected number of bytes: read=" + totalRead
                                    + " while expected=" + nrToTransfer);
                        }
                    } else if (n > 0) {
                        // MUTEX save: field is only updated by reader:
                        totalRead += n; // update totalRead;
                    } else if (n == 0) {
                        log.trace("read(): Got 0 bytes ...");
                        // ok, try again could be time out.
                    }

                    readTime += System.currentTimeMillis() - startTime;
                    log.trace("reader: after read, nrRead={}", totalRead);

                    // notify writer there is data (if writer is waiting)
                    synchronized (writerWait) {
                        writerWait.notify();
                    }
                } else if ((unknownSize == true) || (totalRead < nrToTransfer)) {
                    // Wait for writer to free space.
                    // Wait for .1 second max. or until writer notifies reader.
                    synchronized (readerWait) {
                        readerWait.wait(100);
                    }
                }
            }

            log.trace("--- Reader done ---");
            log.trace("reader total nrRead    ={}", totalRead);
            log.trace("reader total nrWritten ={}", nrWritten);
            log.trace("reader nrToTransfer    ={}", nrToTransfer);

        } catch (Throwable err) {
            log.error("Reader Exception:" + err.getMessage(), err);
            // Signal Strop:
            this.cancelTransfer = true;
            // notify writer since there is a read error !

            synchronized (writerWait) {
                writerWait.notify();
            }

            throw new IOException("Exception while reading", err);
        }

    }

    public void setStop(boolean val) {
        cancelTransfer = val;
    }

    protected boolean mustStop() {
        // ActionTask.mustStop() is called
        if (cancelTransfer == true)
            return true;

        // User interaction through the dialog: Cancel!
        return (this.transferInfo != null) && (transferInfo.isCancelled());

    }

    protected void writeLoop() throws IOException {
        int buflen = buffer.length;

        try {
            while ((unknownSize == true) || (nrWritten < nrToTransfer)) {
                if (mustStop())
                    throw new InterruptedException("Transfer interrupted!");

                int delta = 0;
                int start = 0;

                // Do buffer calculations in MuTex'd time:
                synchronized (buffer) {
                    // nr bytes to be written
                    delta = (int) (totalRead - nrWritten);
                    // start in cicular buffer
                    start = (int) (nrWritten % buflen);

                    if (start + delta > buflen)
                        delta = buflen - start; // wrap around buffer;

                    if (delta > writeChunkSize)
                        delta = writeChunkSize;
                }

                log.trace("writer: nrRead    ={}", totalRead);
                log.trace("writer: nrWritten ={}", nrWritten);
                log.trace("writer: nrToRead  ={}", nrToTransfer);
                log.trace("writer: start     ={}", start);
                log.trace("writer: delta     ={}", delta);


                // data to write ?
                if (delta > 0) {
                    long startTime = System.currentTimeMillis();

                    outputStream.write(buffer, start, delta);

                    writeTime += System.currentTimeMillis() - startTime;

                    // MUTEX safe: field is only updated by writer:
                    nrWritten += delta;

                    if (transferInfo != null) {
                        // update current transfer:
                        transferInfo.updateSubTaskDone(streamCopySubTaskName, nrWritten);
                    }
                    // notify reader that buffer is empty
                    synchronized (readerWait) {
                        readerWait.notify();
                    }
                } else if ((unknownSize == true) || (nrWritten < nrToTransfer)) {
                    // Wait for reader to fill buffer.
                    // wait for .1 second or until reader notifies writer.
                    synchronized (writerWait) {
                        writerWait.wait(100);
                    }
                }
            }

            log.trace("--- Writer done ---");
            log.trace("writer total nrRead    ={}", totalRead);
            log.trace("writer total nrWritten ={}", nrWritten);
            log.trace("writer nrToTransfer    ={}", nrToTransfer);

            // in the case the reader still is waiting for the writer
            // to finish :

            synchronized (readerWait) {
                readerWait.notify();
            }
        } catch (Throwable err) {
            log.error("Writer Exception:" + err.getMessage(), err);
            // Signal Strop:
            this.cancelTransfer = true;
            // notify reader since there is a read error !
            synchronized (readerWait) {
                readerWait.notify();
            }
            // rethrow
            throw new IOException("Exception while writing", err);
        }

    }

    /**
     * Transfer upto numTranfer bytes, or -1 for all.
     */
    public void startTransfer(long numTransfer) throws IOException {
        // =============================================================
        // Pre Transfer
        // =============================================================

        long start = System.currentTimeMillis();

        // fixed streamcopy or unknown.

        if (numTransfer >= 0) {
            this.nrToTransfer = numTransfer;
            this.unknownSize = false;
        } else {
            // copy all (Stream Copy)
            this.nrToTransfer = -1;
            this.unknownSize = true;
        }

        // update transferinfo
        if (this.transferInfo != null) {
            transferInfo.startSubTask(streamCopySubTaskName, numTransfer);
            // set to 0 if unknown; !
            if (numTransfer < 0)
                transferInfo.updateSubTaskDone(streamCopySubTaskName, 0);
        }

        // start reader in background:
        readerTask = new ActionTask(nl.esciencecenter.ptk.task.TaskWatcher.getTaskWatcher(), "RingBuffer.readerTask") {

            @Override
            public void doTask() throws Exception {
                readLoop();
            }

            @Override
            public void stopTask() {
                setStop(true);
            }
        };

        // =============================================================
        // Transfer Loop
        // =============================================================

        readerTask.startTask();

        // writer will be last to finish so start in CURRENT thread.
        writeLoop();

        // =============================================================
        // Post Transfer
        // =============================================================

        // join tasks:
        try {
            readerTask.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted:" + e.getMessage(), e);
        }
        // after transfer make sure all streams are flushes and closed !

        long totalTime = System.currentTimeMillis() - start;

        // do not divide by zero:

        if (readTime <= 0)
            readTime = 1;
        if (writeTime <= 0)
            writeTime = 1;
        if (totalTime <= 0)
            totalTime = 1;

        // unit B/ms= kB/s
        log.trace("read speed={} kB/s", (nrToTransfer / readTime));
        log.trace("write speed={} kB/s", (nrToTransfer / writeTime));

        // bytes per second:
        long totalSpeed = 1000L * nrToTransfer / totalTime;

        String totalSpeedStr = Presentation.createDefault().speedString(totalSpeed, "bytes/s");

        log.trace("total speed={}", totalSpeedStr);

        // Ugly:
        // if (this.transferInfo!=null)
        // transferInfo.addLogText("Total stream transfer speed="+totalSpeedStr+"\n");

        if (readerTask.hasException()) {
            Throwable e = readerTask.getException();
            throw new IOException("Reader Exception:" + e.getMessage(), e);
        }
    }

    public void setTaskMonitor(ITaskMonitor transfer) {
        this.transferInfo = transfer;
    }

    public int getReadChunkSize() {
        return this.readChunkSize;
    }

    public int getWriteChunkSize() {
        return this.writeChunkSize;
    }

    public int getCopyBufferSize() {
        return this.bufferSize;
    }

    public long getTotalWritten() {
        return this.nrWritten;
    }

}
