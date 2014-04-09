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
    private static ClassLogger logger = ClassLogger.getLogger(IOUtil.class);

    private static int defaultBufferSize = 1 * 1024 * 1024;

    static
    {
        logger.setLevelToDebug(); 
    }
    
    public static interface Readable
    {
        public int read(byte buffer[], int bufferOffset, int numBytes) throws IOException;
    }

    public static class ReadFunctor implements Readable
    {
        protected InputStream inps;

        public ReadFunctor(InputStream inps)
        {
            this.inps = inps;
        }

        public int read(byte buffer[], int bufferOffset, int numBytes) throws IOException
        {
            return inps.read(buffer, bufferOffset, numBytes);
        }
    }

    public static class RandomReaderFunctor implements Readable
    {
        protected RandomReadable reader;

        protected long offset; 
        
        public RandomReaderFunctor(RandomReadable reader, long offset)
        {
            this.reader = reader;
            this.offset=offset; 
        }

        public int read(byte buffer[], int bufferOffset, int numBytes) throws IOException
        {
            return reader.readBytes(offset, buffer, bufferOffset, numBytes);
        }
    }

    /**
     * Copy all the data from the InputStream to the OutputStream.
     * 
     * @param autoCloseStream
     *            - set to true to close the Input- and OuputStream after a copy.
     * @throws IOException
     */
    public static long copyStreams(InputStream input, OutputStream output, boolean autoCloseStreams) throws IOException
    {
        return circularStreamCopy(input, output, -1, defaultBufferSize, autoCloseStreams, null);
    }

    public static long circularStreamCopy(InputStream input, OutputStream output, boolean autoCloseStreams) throws IOException
    {
        return circularStreamCopy(input, output, -1, defaultBufferSize, autoCloseStreams, null);
    }

    /**
     * Parallel StreamCopy using circular stream buffer. Starts a read in the background to enable full duplex reading
     * and writing.
     */
    public static long circularStreamCopy(
            InputStream inps,
            OutputStream outps,
            long nrToTransfer,
            int bufferSize,
            boolean autoClose, ITaskMonitor monitor) throws IOException
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
            {
                bufferSize = (int) nrToTransfer;
            }
            
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
            {
                cbuffer.setMaxReadChunkSize(optimalReadChunkSize);
            }

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

            // Will end when done.
            // StartTransfer will close the streams and updates transferMonitor
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
     * Read exactly numBytes, if actually return number of bytes < numBytes the end of the file was encountered ! 
     * @throws IOException 
     */
    public static int syncReadLoop(Readable readerF,byte buffer[], int bufferOffset,int numBytes, int timeOutMillies) throws IOException
    {
        int totalRead=0; 
        int nullReads=0; 
        
        while (totalRead<numBytes)
        {
            int numToRead = (numBytes - totalRead);
            int numRead = readerF.read(buffer, bufferOffset, (int) numToRead);

            if (numRead > 0)
            {
                totalRead += numRead;
                // reset; 
                nullReads=0; 
                
                logger.debugPrintf("syncReadLoop: Current numRead/totalRead=%d/%d\n", numRead, totalRead);
            }
            else if (numRead == 0)
            {
                // ---
                // Although java read() specifies 0 is only returned when numBytes==0, the underlaying implementation
                // might stil return 0 when there is no data (yet) available. 
                // For asynchronous reads, returning 0 is actaully allowed. 
                // --- 
                
                // micro sleep: 10 milli seconds time out
                if ((timeOutMillies>0) && (nullReads * 10 >= timeOutMillies))
                {
                    throw new IOException("Reading Timeout waiting time>"+timeOutMillies);
                }
                
                nullReads++;
                
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    // contract of Interrupted is to stop immediatly. 
                    e.printStackTrace();
                    break ;
                }
            }
            else if (numRead < 0)
            {
                if (totalRead>0)
                {
                    // no more bytes left, return remainder 
                    return totalRead;
                }
                else
                {
                    throw new IOException("EOF: Can not read past end.\n"); 
                }
            }
        }

        logger.debugPrintf("syncReadLoop: Finished totalRead=%d\n", totalRead);
        return totalRead; 
    }

    /**
     * Synchronized read loop which performs several readBytes() calls to fill buffer. Helper method for read() method
     * with should be done in a loop. (This since File.read() or InputStream reads() don't always return the desired nr.
     * of bytes. This method keeps on reading until either End Of File is reached (EOF) or the desired nr of bytes is
     * read.
     * <p>
     * Returns -1 when EOF is encountered, or actual nr of bytes read. If the return value doesn't match the nrBytes
     * wanted, no extra bytes could be read so this method doesn't have to be called again.
     */
    public static int syncReadBytes(RandomReadable source, long fileOffset, byte[] buffer, int bufferOffset, int nrBytes) throws IOException
    {
        // Delegate to functor: 
        RandomReaderFunctor reader= new RandomReaderFunctor(source, fileOffset); 
        return syncReadLoop(reader,buffer,bufferOffset,nrBytes,-1);
    }

    public static int syncReadBytes(InputStream inps, byte[] buffer, int bufferOffset, int nrOfBytes, boolean autoClose) throws IOException
    {
        return syncReadBytes(inps, 0, buffer, bufferOffset, nrOfBytes, autoClose);
    }

    /**
     * Synchronized read helper method. Since some read() method only read small chunks each time, this method tries to
     * read until either EOF is reached, or the desired nrOfBytes has been read.
     */
    public static int syncReadBytes(InputStream inps, long fileOffset, byte[] buffer, int bufferOffset, int nrOfBytes, boolean autoClose)
            throws IOException
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
            {
                inps.skip(fileOffset);
            }
            
            return syncReadLoop(new ReadFunctor(inps),buffer,bufferOffset,nrOfBytes,-1); 
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (autoClose)
            {
                IOUtil.autoClose(inps);
            }
        }
    }

    public static void autoClose(InputStream inps)
    {
        if (inps == null)
        {
            return;
        }

        try
        {
            inps.close();
        }
        catch (IOException e)
        {
            logger.logException(ClassLogger.DEBUG, e, "Exception when closing input stream:%s\n", inps);
        }

    }

    public static void autoClose(OutputStream outps)
    {
        if (outps == null)
        {
            return;
        }

        try
        {
            outps.close();
        }
        catch (IOException e)
        {
            logger.logException(ClassLogger.DEBUG, e, "Exception when closing output stream:%s\n", outps);
        }

    }

}
