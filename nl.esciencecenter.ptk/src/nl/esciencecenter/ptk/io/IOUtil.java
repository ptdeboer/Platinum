/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.TaskMonitorAdaptor;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * IO helper methods.
 */
public class IOUtil
{
    private static int defaultBufferSize = 1 * 1024 * 1024;

    private static ClassLogger logger;

    static
    {
        logger = ClassLogger.getLogger(IOUtil.class);
        // logger.setLevelToDebug();
    }

    public static int syncReadBytes(InputStream inps, byte[] buffer, int bufferOffset, int nrOfBytes) throws IOException
    {
        return syncReadBytes(inps, 0, buffer, bufferOffset, nrOfBytes);
    }

    /**
     * Synchronized read helper method. Since some read() method only read small
     * chunks each time, this method tries to read until either EOF is reached,
     * or the desired nrOfBytes has been read.
     */
    public static int syncReadBytes(InputStream inps, long fileOffset, byte[] buffer, int bufferOffset, int nrOfBytes) throws IOException
    {
        // basic checks
        if (inps == null)
            return -1;

        if (nrOfBytes < 0)
            return 0;

        if (nrOfBytes == 0)
            return 0;

        // actual read
        try
        {
            if (fileOffset > 0)
                inps.skip(fileOffset);

            int nrRead = 0;
            int result = 0;

            // actual read loop:
            while ((nrRead < nrOfBytes) && (result >= 0))
            {
                result = inps.read(buffer, bufferOffset + nrRead, nrOfBytes - nrRead);

                if (result < 0)
                {
                    // EOF ! either return -1, or return nr of actual read
                    // bytes.
                    logger.debugPrintf("Warning InputStream.read(): EOF when reading %d bytes\n", (nrOfBytes - nrRead));
                    logger.debugPrintf("> total nrRead/nrToRead=%d/%d\n", nrRead, nrOfBytes);

                    // EOF while nothing has been read: return -1;
                    if (nrRead == 0)
                        return -1;
                    else
                        // break out of read loop and return actual nr. of read
                        // bytes.
                        break; // negative result should take care of it
                }
                else
                {
                    nrRead += result;
                }
            }

            try
            {
                inps.close();
            }
            catch (IOException e)
            {
                // ingore: can happen if remote side already has closed the
                // connection !
                logger.warnPrintf("Warning. Got exception when closing InputStream after succesfull read:%s\n", e);
            }
            return nrRead;
        }
        catch (IOException e)
        {
            throw e; // new IOException("Got IO Exception during read !\n"+
                     // e.getMessage(), e);
        }
    }

    /**
     * Copy all the data from the InputStream to the OutputStream.
     * 
     * @param autoCloseStream
     *            - set to true to close the Input- and OuputStream after a
     *            copy.
     * @throws IOException
     */
    public static long copyStreams(InputStream input, OutputStream output, boolean autoCloseStreams) throws IOException
    {
        return directStreamCopy(null, input, output, defaultBufferSize, -1, autoCloseStreams);
    }

    public static long circularCopyStreams(InputStream input, OutputStream output, boolean autoCloseStreams) throws IOException
    {
        return circularStreamCopy(null, input, output, -1, defaultBufferSize, autoCloseStreams);
    }

    public static long copyStreams(
            ITaskMonitor monitor,
            int bufSize,
            long nrBytes,
            InputStream input,
            OutputStream output,
            boolean autoClose) throws IOException
    {
        return directStreamCopy(monitor, input, output, bufSize, nrBytes, autoClose);
        // return
        // circularStreamCopy(null,input,output,-1,defaultBufferSize,true);
    }

    /**
     * Copy all the data from the InputStream to the OutputStream.
     * 
     * @param InputStream
     *            - the InputStream to read from.
     * @param OutputStream
     *            - the OutputStream to write to.
     * @param bufSize
     *            - the buffer size to use during copy. For big files, use a big
     *            copy buffer.
     * @param totalToTransfer
     *            - number of bytes to transfer, or -1 for copy until EOF
     *            occurs.
     * @param autoCloseStream
     *            - set to true to close the Input- and OuputStream after a
     *            copy.
     * @throws IOException
     */
    public static long directStreamCopy(ITaskMonitor monitor,
            InputStream input,
            OutputStream output,
            int bufSize,
            long totalToTransfer, // -1 => continue
            boolean autoClose) throws IOException
    {
        logger.debugPrintf("directStreamCopy():START: bufSize=%d,totalToTransfer=%d\n", bufSize, totalToTransfer);

        if (input == null)
            return -1;

        if (output == null)
            return -1;

        byte buffer[] = new byte[bufSize];

        boolean eof = false;
        boolean stop = false;
        int chunkSize = bufSize;
        int totalRead = 0;
        int idle = 0;

        while ((eof == false) && (stop == false))
        {
            // check chunksize:
            if ((totalToTransfer > 0) && (totalRead + chunkSize) > totalToTransfer)
            {
                // read remainder:
                chunkSize = toInt(totalToTransfer - totalRead);
            }

            int numRead = input.read(buffer, 0, chunkSize);

            if (numRead == 0)
            {
                idle++;
                logger.debugPrintf("directStreamCopy():IDLE:%d\n", idle);
                continue;
            }

            if (numRead < 0)
            {
                // fixed tranfer:
                if ((totalToTransfer > 0) && (totalRead < totalToTransfer))
                {
                    logger.warnPrintf("directCopy: Got EOF. Number of bytes read:%d < expected:%d\n",
                            totalRead, totalToTransfer);

                    throw new IOException("Failed to transfer number of expected bytes."
                            + "Number of bytes read=" + totalRead
                            + ", expected=" + totalToTransfer);
                }
                else
                {
                    logger.debugPrintf(" - directStreamCopy: Got EOF. Stopping stream transfer.Number of bytes read:%d\n",
                            totalRead);

                }

                eof = true;
                break;
            }

            output.write(buffer, 0, numRead);
            totalRead += numRead;

            logger.debugPrintf(" - directCopy numread         = %d\n", numRead);
            logger.debugPrintf(" - directCopy totalRead       = %d\n", totalRead);
            logger.debugPrintf(" - directCopy totalToTransfer = %d\n", totalToTransfer);

            if ((totalToTransfer >= 0) && (totalRead >= totalToTransfer))
            {
                stop = true;
                break;
            }

        }// WHILE

        if (autoClose)
        {
            try
            {
                // writer task done or Exception :
                output.flush();
                output.close();
            }
            catch (Exception e)
            {
                logger.warnPrintf("Warning: Got error when flushing and closing outputstream:%s\n", e);
            }

            try
            {
                // istr.flush();
                input.close();
            }
            catch (Exception e)
            {
                logger.warnPrintf("Warning: Got exception when closing inputstream (after read):%s\n", e);
            }
        }

        logger.debugPrintf("directStreamCopy():DONE: totalRead=%d,totalToTransfer=%d\n", totalRead, totalToTransfer);

        return totalRead;
    }

    private static int toInt(long longVal)
    {
        // todo max int checking:
        return (int) longVal;
    }

    /**
     * Parallel StreamCopy using circular stream buffer. Starts a read in the
     * background to enable full duplex reading and writing.
     */
    public static long circularStreamCopy(
            ITaskMonitor monitor,
            InputStream inps,
            OutputStream outps,
            long nrToTransfer,
            int bufferSize,
            boolean autoClose) throws IOException

    {
        logger.debugPrintf("circularStreamCopy():START: bufSize=%d, totalToTransfer=%d\n", bufferSize, nrToTransfer);

        if (monitor == null)
            monitor = new TaskMonitorAdaptor(); // defaut:

        // Setup & Initiate Stream Copy:
        //
        String subTaskName = "Performing stream copy";

        try
        {

            monitor.startSubTask(subTaskName, nrToTransfer);

            // do not allocate buffer size bigger than than file size
            if ((nrToTransfer > 0) && (nrToTransfer < bufferSize))
                bufferSize = (int) nrToTransfer;

            // Use CirculareStreamBuffer to copy from InputStream =>
            // OutputStream
            RingBufferStreamTransferer cbuffer = new RingBufferStreamTransferer(bufferSize);

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

            if (optimalReadChunkSize > 0)
                cbuffer.setMaxReadChunkSize(optimalReadChunkSize);

            logger.debugPrintf(" + streamCopy transferSize   =%d\n", nrToTransfer);
            logger.debugPrintf(" + streamCopy readChunkSize  =%d\n", cbuffer.getReadChunkSize());
            logger.debugPrintf(" + streamCopy writeChunkSize =%d\n", cbuffer.getWriteChunkSize());
            logger.debugPrintf(" + streamCopy buffer size    =%d\n", cbuffer.getCopyBufferSize());

            // start background writer:
            cbuffer.setInputStream(inps);
            cbuffer.setOutputstream(outps);

            // ====================================
            // Transfer !
            // ====================================

            // will end when done
            // startTransfer will close the streams and updates transferMonitor
            // !
            cbuffer.startTransfer(nrToTransfer);

            // ====================================
            // POST Chunk Copy Loop
            // ====================================

            if (autoClose)
            {
                try
                {
                    // writer task done or Exception :
                    outps.flush();
                    outps.close();
                }
                catch (Exception e)
                {
                    logger.warnPrintf("Warning: Got error when flushing and closing outputstream:%s\n", e);
                }

                try
                {
                    // istr.flush();
                    inps.close();
                }
                catch (Exception e)
                {
                    logger.warnPrintf("Warning: Got exception when closing inputstream (after read):%s\n", e);
                }
            }

            long numTransferred = cbuffer.getTotalWritten();
            monitor.updateSubTaskDone(subTaskName, numTransferred);
            monitor.endSubTask(subTaskName);

            logger.debugPrintf("circularStreamCopy():DONE: totalTransfered=%s\n", numTransferred);

            return numTransferred;
        }
        catch (Exception ex)
        {
            monitor.endSubTask("Performing stream copy: Error");

            if (ex instanceof IOException)
            {
                throw (IOException) ex;
            }
            else
            {
                throw new IOException("StreamCopy Failed\n Message=" + ex.getMessage(), ex);
            }
        }
        finally
        {

        }
    }

    /**
     * Synchronized read loop which performs several readBytes() calls to fill
     * buffer. Helper method for read() method with should be done in a loop.
     * This since File.read() or InputStream reads() don't always return the
     * desired number of bytes. This method keeps on reading until either End Of
     * File is reached (EOF) or the desired number of bytes is read.
     * <p>
     * Returns -1 when EOF is encountered, or actual number of bytes read. If
     * the return value doesn't match the nrBytes wanted, no extra bytes could
     * be read so this method doesn't have to be called again.
     */
    public static int syncReadBytes(
            InputStream input,
            byte[] buffer,
            int bufferOffset,
            long nrBytes) throws Exception
    {
        int totalRead = 0;
        int numRead = 0;
        int nullReads = 0;
        int timeOut = 60 * 1000; // 60 secs timeout.

        // ***
        // read loop
        // read as much as possible until EOF occures or nrOfBytes is read.
        // ***

        do
        {
            long numToRead = (nrBytes - totalRead);

            // will encounter out-of-mem anyway, but who knows, be robuust here
            // !
            if (numToRead > Integer.MAX_VALUE)
                numToRead = Integer.MAX_VALUE;

            numRead = input.read(buffer, bufferOffset, (int) numToRead);

            if (numRead > 0)
            {
                totalRead += numRead;
            }
            else if (numRead == 0)
            {
                nullReads++;

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // 100 seconds time out
                if (nullReads * 100 == timeOut)
                    throw new IOException("Reading Timeout");
            }
            else if (numRead < 0)
            {
                // EOF !
                if (totalRead > 0)
                {
                    break;// stop & return nr of bytes actual read !
                }
                else
                {
                    logger.debugPrintf("Warning:got EOF after read()\n");
                    return -1; // signal EOF without reading any bytes !
                }
            }

            // throw new
            // nl.uva.vlet.exception.VlIOException("EOF Exception when reading from:"+file);

            logger.debugPrintf("syncReadBytes: Current numRead/totalRead=%d/%d\n", numRead, totalRead);
        } while ((totalRead < nrBytes) && (numRead >= 0));

        logger.debugPrintf("syncReadBytes: Finished totalRead=%d\n", totalRead);

        return totalRead;
    }

    /**
     * Synchronized read loop which performs several readBytes() calls to fill
     * buffer. Helper method for read() method with should be done in a loop.
     * (This since File.read() or InputStream reads() don't always return the
     * desired nr. of bytes. This method keeps on reading until either End Of
     * File is reached (EOF) or the desired nr of bytes is read.
     * <p>
     * Returns -1 when EOF is encountered, or actual nr of bytes read. If the
     * return value doesn't match the nrBytes wanted, no extra bytes could be
     * read so this method doesn't have to be called again.
     */
    public static int syncReadBytes(RandomReader source, long fileOffset, byte[] buffer, int bufferOffset, long nrBytes) throws IOException
    {
        int totalRead = 0;
        int numRead = 0;
        int nullReads = 0;
        int timeOut = 60 * 1000; // 60 secs timeout.

        // ***
        // read loop
        // read as much as possible until EOF occures or nrOfBytes is read.
        // ***

        do
        {
            long numToRead = (nrBytes - totalRead);

            // will encounter out-of-mem anyway, but who knows, be robuust here
            // !
            if (numToRead > Integer.MAX_VALUE)
                numToRead = Integer.MAX_VALUE;

            numRead = source.readBytes(fileOffset + totalRead, buffer, totalRead, (int) numToRead);

            if (numRead > 0)
            {
                totalRead += numRead;
            }
            else if (numRead == 0)
            {
                nullReads++;

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                // 100 seconds time out
                if (nullReads * 100 == timeOut)
                    throw new IOException("Time out when reading from:" + source);
            }
            else if (numRead < 0)
            {
                // EOF !
                if (totalRead > 0)
                {
                    break;// stop & return nr of bytes actual read !
                }
                else
                {
                    logger.debugPrintf("Warning:got EOF after read():%s\n", source);
                    return -1; // signal EOF without reading any bytes !
                }
            }

            // throw new
            // nl.uva.vlet.exception.VlIOException("EOF Exception when reading from:"+file);

            logger.debugPrintf("syncReadBytes: Current numRead/totalRead=%d/%d\n", numRead, totalRead);

        } while ((totalRead < nrBytes) && (numRead >= 0));

        logger.debugPrintf("syncReadBytes: Finished totalRead=%d\n", totalRead);

        return totalRead;
    }

}
